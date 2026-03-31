CREATE OR REPLACE PROCEDURE PRT_S_CE_RICLASSIFICATO_J
-- =============================================================================
-- Procedura : PRT_S_CE_RICLASSIFICATO_J
-- Descrizione: Vista di stampa Conto Economico Riclassificato
-- =============================================================================
-- HISTORY
-- -----------------------------------------------------------------------------
-- Data       | Ver | Autore | Descrizione
-- -----------+-----+--------+--------------------------------------------------
-- 28/07/2004 | 1.0 |        | Creazione
-- 06/08/2004 | 1.1 |        | Modificati nome view e procedura, eliminato nome
--            |     |        | schema, aggiunto history
-- 06/08/2004 | 1.2 |        | Aggiunto HINT RULE alle SELECT (performance)
-- =============================================================================
(
    p_ist_comm        IN VARCHAR2,
    p_es              IN NUMBER,
    p_cds             IN VARCHAR2,
    p_uo              IN VARCHAR2,
    p_conti_sn        IN CHAR,
    p_cd_tipo_bilancio IN VARCHAR2
)
IS
    -- -------------------------------------------------------------------------
    -- Variabili di lavoro
    -- -------------------------------------------------------------------------
    v_id              NUMBER;
    v_i               NUMBER;
    v_indice          NUMBER        := 0;
    v_num1            NUMBER;

    -- Livelli di aggregazione per l'output
    v_liv1            VARCHAR2(10);
    v_liv2            VARCHAR2(10);
    v_liv3            VARCHAR2(10);
    v_liv4            VARCHAR2(10);

    -- Importi parziali e totali anno corrente / anno precedente
    v_parz1           NUMBER(20,3);
    v_tot1            NUMBER(20,3);
    v_parz2           NUMBER(20,3);
    v_tot2            NUMBER(20,3);
    v_tot_incr1       NUMBER(20,3);
    v_tot_incr2       NUMBER(20,3);

    -- Dare/Avere aggregati
    v_dare1           NUMBER(20,3);
    v_avere1          NUMBER(20,3);
    v_dare_conto      NUMBER(20,3);
    v_avere_conto     NUMBER(20,3);

    -- Contatori movimenti
    v_contamov        NUMBER        := 0;

    -- Indici per parsing formula
    v_m1              NUMBER;
    v_m2              NUMBER;
    v_m3              NUMBER;
    v_m4              NUMBER;

    -- Flag totale riga
    v_flag_tot        VARCHAR2(1);

    -- Record di supporto
    v_chius_coep      CHIUSURA_COEP%ROWTYPE;
    v_conto           VOCE_EP%ROWTYPE;

    -- Conti speciali da configurazione
    v_conto_avanzo    VOCE_EP.CD_VOCE_EP%TYPE;
    v_prof_perd       VOCE_EP.CD_VOCE_EP%TYPE;
    v_tipo_bilancio   VARCHAR2(20);

    -- -------------------------------------------------------------------------
    -- Cursori
    -- -------------------------------------------------------------------------

    -- Schema di riclassificazione CE
CURSOR c_schema_ce (p_tipo_bilancio IN VARCHAR2) IS
SELECT *
FROM   CNR_GRUPPO_EP
WHERE  CD_PIANO_GRUPPI  = 'CE'
  AND  CD_TIPO_BILANCIO = p_tipo_bilancio
ORDER BY SEQUENZA;

-- Conti associati ad un gruppo EP
CURSOR c_conti_associati (
        p_conto_riclassificato IN VARCHAR2,
        p_tipo_bilancio        IN VARCHAR2,
        p_anno_comp            IN NUMBER
    ) IS
SELECT ESERCIZIO, CD_VOCE_EP, SEZIONE
FROM   CNR_ASS_CONTO_GRUPPO_EP
WHERE  CD_PIANO_GRUPPI  = 'CE'
  AND  ESERCIZIO        = p_anno_comp
  AND  CD_GRUPPO_EP     = p_conto_riclassificato
  AND  CD_TIPO_BILANCIO = p_tipo_bilancio;

-- Record dei cursori
r_conti_ass   c_conti_associati%ROWTYPE;
    r_schema_ce   c_schema_ce%ROWTYPE;

BEGIN

    -- -------------------------------------------------------------------------
    -- 1. Generazione ID univoco per la sessione di stampa
    -- -------------------------------------------------------------------------
SELECT IBMSEQ00_CR_PACKAGE.NEXTVAL
INTO   v_id
FROM   DUAL;

-- -------------------------------------------------------------------------
-- 2. Recupero conti speciali da configurazione
-- -------------------------------------------------------------------------
SELECT VAL01
INTO   v_conto_avanzo
FROM   CONFIGURAZIONE_CNR
WHERE  ESERCIZIO             = p_es
  AND  CD_UNITA_FUNZIONALE   = '*'
  AND  CD_CHIAVE_PRIMARIA    = 'VOCEEP_SPECIALE'
  AND  CD_CHIAVE_SECONDARIA  = 'UTILE_PERDITA_ESERCIZIO';

SELECT VAL01
INTO   v_prof_perd
FROM   CONFIGURAZIONE_CNR
WHERE  ESERCIZIO             = p_es
  AND  CD_UNITA_FUNZIONALE   = '*'
  AND  CD_CHIAVE_PRIMARIA    = 'VOCEEP_SPECIALE'
  AND  CD_CHIAVE_SECONDARIA  = 'CONTO_ECONOMICO';

-- -------------------------------------------------------------------------
-- 3. Normalizzazione tipo bilancio (IRES → CIVILISTICO)
-- -------------------------------------------------------------------------
v_i := 0;

    IF p_cd_tipo_bilancio = 'IRES' THEN
        v_tipo_bilancio := 'CIVILISTICO';
ELSE
        v_tipo_bilancio := p_cd_tipo_bilancio;
END IF;

    -- =========================================================================
    -- 4. Loop principale sullo schema di riclassificazione
    -- =========================================================================
OPEN c_schema_ce(v_tipo_bilancio);

LOOP  -- c_schema_ce

FETCH c_schema_ce INTO r_schema_ce;
        EXIT WHEN c_schema_ce%NOTFOUND;

        -- Azzeramento valori di riga
        v_parz1    := NULL;
        v_tot1     := NULL;
        v_parz2    := NULL;
        v_tot2     := NULL;
        v_flag_tot := 'N';

        -- Determinazione livello gerarchico della voce
        IF r_schema_ce.CD_GRUPPO_PADRE IS NULL THEN
            v_liv1 := r_schema_ce.NOME;
            v_liv2 := NULL;
            v_liv3 := NULL;
        ELSIF LOWER(SUBSTR(r_schema_ce.NOME, 1, 1)) BETWEEN 'a' AND 'z' THEN
            v_liv1 := NULL;
            v_liv2 := NULL;
            v_liv3 := r_schema_ce.NOME;
ELSE
            v_liv1 := NULL;
            v_liv2 := r_schema_ce.NOME;
            v_liv3 := NULL;
END IF;

        -- =====================================================================
        -- CASO A: Voce di dettaglio (mastrino) → loop sui conti associati
        -- =====================================================================
        IF r_schema_ce.FL_MASTRINO = 'Y' THEN

            OPEN c_conti_associati(r_schema_ce.CD_GRUPPO_EP, v_tipo_bilancio, p_es);

            LOOP  -- c_conti_associati

FETCH c_conti_associati INTO r_conti_ass;
                EXIT WHEN c_conti_associati%NOTFOUND;

                v_conto := cnrctb002.getVoceEp(r_conti_ass.ESERCIZIO, r_conti_ass.CD_VOCE_EP);

                DBMS_OUTPUT.PUT_LINE('CONTI_ASS: ' || r_conti_ass.CD_VOCE_EP);

                -- Azzeramento dare/avere per il conto corrente
                v_dare1  := 0;
                v_avere1 := 0;

                -- -----------------------------------------------------------------
                -- Blocco ottimizzazione: evita il loop sui CDS quando non necessario
                -- -----------------------------------------------------------------
                DECLARE
v_conta_cds_validi  NUMBER;
                    v_conta_chiusi      NUMBER;
                    v_parziale_i_anno   NUMBER;
                    v_uo_ente           VARCHAR2(30);
BEGIN
                    -- Recupero UO Ente
                    v_uo_ente := CNRCTB020.getuoente(p_es).cd_unita_organizzativa;

SELECT COUNT(*)
INTO   v_conta_cds_validi
FROM   V_UNITA_ORGANIZZATIVA_VALIDA
WHERE  ESERCIZIO = p_es
  AND  FL_CDS   = 'Y';

SELECT COUNT(*)
INTO   v_conta_chiusi
FROM   CHIUSURA_COEP
WHERE  ESERCIZIO = p_es
  AND  STATO    IN ('P', 'C');

DBMS_OUTPUT.PUT_LINE('CONTA_CDS_VALIDI: ' || v_conta_cds_validi
                                         || '  CONTA_CHIUSI: '  || v_conta_chiusi);

                    -- -----------------------------------------------------------------
                    -- PERCORSO VELOCE: tutti i CDS sono chiusi e non è richiesta
                    -- filtro su CDS/UO → una sola SELECT aggregata sull'ente
                    -- -----------------------------------------------------------------
                    IF v_conta_cds_validi = v_conta_chiusi
                       AND p_cds = '*'
                       AND p_uo  = '*'
                    THEN

                        IF r_conti_ass.CD_VOCE_EP = v_conto_avanzo THEN

                            -- Conto Avanzo/Perdita: lettura da bilancio riclassificato
                            -- o da movimenti con causale determinazione utile/perdita
                            v_flag_tot := 'S';
                            DBMS_OUTPUT.PUT_LINE('SELECT SU CONTO (avanzo): ' || r_conti_ass.CD_VOCE_EP);

BEGIN
SELECT IMPORTO_FINALE
INTO   v_parziale_i_anno
FROM   BIL_RICLASSIFICATO
WHERE  ESERCIZIO              = p_es
  AND  CD_VOCE_EP             = r_conti_ass.CD_VOCE_EP
  AND  CD_TIPO_BILANCIO       = p_cd_tipo_bilancio
  AND  CD_UNITA_ORGANIZZATIVA = v_uo_ente;
EXCEPTION
                                WHEN NO_DATA_FOUND THEN
SELECT NVL(SUM(DECODE(D.SEZIONE, 'D', D.IM_MOVIMENTO)), 0),
       NVL(SUM(DECODE(D.SEZIONE, 'A', D.IM_MOVIMENTO)), 0)
INTO   v_dare_conto, v_avere_conto
FROM   MOVIMENTO_COGE           D
   , SCRITTURA_PARTITA_DOPPIA T
WHERE  T.CD_CDS                 = D.CD_CDS
  AND  T.ESERCIZIO              = D.ESERCIZIO
  AND  T.CD_UNITA_ORGANIZZATIVA = D.CD_UNITA_ORGANIZZATIVA
  AND  T.PG_SCRITTURA           = D.PG_SCRITTURA
  AND  T.ATTIVA                 = 'Y'
  AND  (   T.CD_CAUSALE_COGE IS NULL
    OR (    T.CD_CAUSALE_COGE = 'DETERMINAZIONE_UTILE_PERDITA'
        AND r_conti_ass.CD_VOCE_EP = v_conto_avanzo))
  AND  T.ESERCIZIO              = p_es
  AND  D.CD_VOCE_EP             = r_conti_ass.CD_VOCE_EP
  AND  (   NVL(TO_CHAR(D.DT_DA_COMPETENZA_COGE,'YYYY'), D.ESERCIZIO) = TO_CHAR(p_es)
    OR NVL(TO_CHAR(D.DT_A_COMPETENZA_COGE, 'YYYY'), D.ESERCIZIO) = TO_CHAR(p_es))
  AND  D.TI_ISTITUZ_COMMERC     = DECODE(p_ist_comm, '*', D.TI_ISTITUZ_COMMERC, p_ist_comm)
  AND  EXISTS (
    SELECT 1
    FROM   MOVIMENTO_COGE M2
    WHERE  M2.CD_CDS                 = D.CD_CDS
      AND  M2.ESERCIZIO              = D.ESERCIZIO
      AND  M2.CD_UNITA_ORGANIZZATIVA = D.CD_UNITA_ORGANIZZATIVA
      AND  M2.PG_SCRITTURA           = D.PG_SCRITTURA
      AND  M2.CD_VOCE_EP             = v_prof_perd
);

v_parziale_i_anno :=
                                        CASE r_conti_ass.SEZIONE
                                            WHEN 'D' THEN v_dare_conto  - v_avere_conto
                                            WHEN 'A' THEN v_avere_conto - v_dare_conto
                                            ELSE 0
END;
END;

                            IF p_conti_sn = 'Y' THEN
                                v_i := v_i + 1;
INSERT INTO PRT_VPG_BIL_RICLASSIFICATO (
    ID, CHIAVE, TIPO, SEQUENZA,
    ORDINE, CONTO_RICLASS,
    I_LIVELLO, II_LIVELLO, III_LIVELLO, IV_LIVELLO,
    DESCRIZIONE,
    PARZIALE_I_ANNO, TOTALE_I_ANNO,
    PARZIALE_II_ANNO, TOTALE_II_ANNO,
    SN_TOTALE
) VALUES (
             v_id, 'chiave', 't', v_i,
             r_schema_ce.SEQUENZA, NULL, NULL, NULL, NULL, NULL,
             r_conti_ass.CD_VOCE_EP || ' ' || v_conto.DS_VOCE_EP,
             v_parziale_i_anno,
             NULL, NULL, NULL, 'N'
         );
END IF;

                            v_dare1  := v_dare1  + v_dare_conto;
                            v_avere1 := v_avere1 + v_avere_conto;

ELSE
                            -- Conto normale: lettura da bilancio riclassificato o da movimenti
                            DBMS_OUTPUT.PUT_LINE('SELECT SU CONTO (normale): ' || r_conti_ass.CD_VOCE_EP);

BEGIN
SELECT IMPORTO_FINALE, COUNT(0)
INTO   v_parziale_i_anno, v_contamov
FROM   BIL_RICLASSIFICATO
WHERE  ESERCIZIO              = p_es
  AND  CD_VOCE_EP             = r_conti_ass.CD_VOCE_EP
  AND  CD_TIPO_BILANCIO       = p_cd_tipo_bilancio
  AND  CD_UNITA_ORGANIZZATIVA = v_uo_ente;
EXCEPTION
                                WHEN NO_DATA_FOUND THEN
SELECT NVL(SUM(DECODE(D.SEZIONE, 'D', D.IM_MOVIMENTO)), 0),
       NVL(SUM(DECODE(D.SEZIONE, 'A', D.IM_MOVIMENTO)), 0),
       COUNT(0)
INTO   v_dare_conto, v_avere_conto, v_contamov
FROM   MOVIMENTO_COGE           D
   , SCRITTURA_PARTITA_DOPPIA T
WHERE  T.CD_CDS                 = D.CD_CDS
  AND  T.ESERCIZIO              = D.ESERCIZIO
  AND  T.CD_UNITA_ORGANIZZATIVA = D.CD_UNITA_ORGANIZZATIVA
  AND  T.PG_SCRITTURA           = D.PG_SCRITTURA
  AND  T.ATTIVA                 = 'Y'
  AND  (   T.CD_CAUSALE_COGE IS NULL
    OR (    T.CD_CAUSALE_COGE != 'CHIUSURA_CONTO_ECONOMICO'
                                                AND T.CD_CAUSALE_COGE != 'CHIUSURA_STATO_PATRIMONIALE'
                                                AND T.CD_CAUSALE_COGE != 'DETERMINAZIONE_UTILE_PERDITA'))
  AND  T.ESERCIZIO              = p_es
  AND  D.CD_VOCE_EP             = r_conti_ass.CD_VOCE_EP
  -- Competenza: anno corrente
  AND  (   (    NVL(TO_CHAR(D.DT_DA_COMPETENZA_COGE,'YYYY'), D.ESERCIZIO) = TO_CHAR(p_es)
    AND  NVL(TO_CHAR(D.DT_A_COMPETENZA_COGE, 'YYYY'), D.ESERCIZIO) = TO_CHAR(p_es))
    -- A cavallo anno precedente / anno corrente
    OR  (    NVL(TO_CHAR(D.DT_DA_COMPETENZA_COGE,'YYYY'), D.ESERCIZIO) = TO_CHAR(p_es - 1)
        AND  NVL(TO_CHAR(D.DT_A_COMPETENZA_COGE, 'YYYY'), D.ESERCIZIO) = TO_CHAR(p_es))
    -- A cavallo anno corrente / anno successivo (risconti)
    OR  (    NVL(TO_CHAR(D.DT_DA_COMPETENZA_COGE,'YYYY'), D.ESERCIZIO) = TO_CHAR(p_es)
        AND  NVL(TO_CHAR(D.DT_A_COMPETENZA_COGE, 'YYYY'), D.ESERCIZIO) = TO_CHAR(p_es + 1))
    -- Completamente in anno successivo (risconti)
    OR  (    NVL(TO_CHAR(D.DT_DA_COMPETENZA_COGE,'YYYY'), D.ESERCIZIO) = TO_CHAR(p_es + 1)
        AND  NVL(TO_CHAR(D.DT_A_COMPETENZA_COGE, 'YYYY'), D.ESERCIZIO) = TO_CHAR(p_es + 1)))
  AND  D.TI_ISTITUZ_COMMERC     = DECODE(p_ist_comm, '*', D.TI_ISTITUZ_COMMERC, p_ist_comm);

v_parziale_i_anno :=
                                        CASE r_conti_ass.SEZIONE
                                            WHEN 'D' THEN v_dare_conto  - v_avere_conto
                                            WHEN 'A' THEN v_avere_conto - v_dare_conto
                                            ELSE 0
END;
END;

                            IF p_conti_sn = 'Y' AND v_contamov > 0 THEN
                                v_i := v_i + 1;
INSERT INTO PRT_VPG_BIL_RICLASSIFICATO (
    ID, CHIAVE, TIPO, SEQUENZA,
    ORDINE, CONTO_RICLASS,
    I_LIVELLO, II_LIVELLO, III_LIVELLO, IV_LIVELLO,
    DESCRIZIONE,
    PARZIALE_I_ANNO, TOTALE_I_ANNO,
    PARZIALE_II_ANNO, TOTALE_II_ANNO,
    SN_TOTALE
) VALUES (
             v_id, 'chiave', 't', v_i,
             r_schema_ce.SEQUENZA, NULL, NULL, NULL, NULL, NULL,
             r_conti_ass.CD_VOCE_EP || ' ' || v_conto.DS_VOCE_EP,
             v_parziale_i_anno,
             NULL, NULL, NULL, 'N'
         );
END IF;

                            v_dare1  := v_dare1  + v_dare_conto;
                            v_avere1 := v_avere1 + v_avere_conto;

END IF;  -- conto avanzo / conto normale

                    -- -----------------------------------------------------------------
                    -- PERCORSO STANDARD: loop CDS per CDS
                    -- -----------------------------------------------------------------
ELSE
                        DECLARE
v_dare_cds          NUMBER(20,3) := 0;
                            v_avere_cds         NUMBER(20,3) := 0;
                            v_contamov_cds      NUMBER       := 0;
                            v_parziale_i_anno_cds NUMBER     := 0;
BEGIN
FOR r_cds IN (
                                SELECT CD_UNITA_ORGANIZZATIVA
                                FROM   V_UNITA_ORGANIZZATIVA_VALIDA
                                WHERE  ESERCIZIO              = p_es
                                  AND  FL_CDS                 = 'Y'
                                  AND  CD_UNITA_ORGANIZZATIVA = DECODE(p_cds, '*', CD_UNITA_ORGANIZZATIVA, p_cds)
                                ORDER BY CD_UNITA_ORGANIZZATIVA
                            ) LOOP  -- CDS validi

                                IF r_conti_ass.CD_VOCE_EP = v_conto_avanzo THEN

                                    -- Conto Avanzo per singolo CDS
                                    v_flag_tot := 'S';
BEGIN
SELECT IMPORTO_FINALE,
       v_contamov_cds + 1
INTO   v_parziale_i_anno_cds, v_contamov_cds
FROM   BIL_RICLASSIFICATO
WHERE  ESERCIZIO              = p_es
  AND  CD_VOCE_EP             = r_conti_ass.CD_VOCE_EP
  AND  CD_TIPO_BILANCIO       = p_cd_tipo_bilancio
  AND  CD_UNITA_ORGANIZZATIVA = v_uo_ente;
EXCEPTION
                                        WHEN NO_DATA_FOUND THEN
SELECT v_dare_cds  + NVL(SUM(DECODE(D.SEZIONE, 'D', D.IM_MOVIMENTO)), 0),
       v_avere_cds + NVL(SUM(DECODE(D.SEZIONE, 'A', D.IM_MOVIMENTO)), 0),
       -- CONTA SEMPRE > 0 PER STAMPARE L'AVANZO
       v_contamov_cds + 1
INTO   v_dare_cds, v_avere_cds, v_contamov_cds
FROM   MOVIMENTO_COGE           D
   , SCRITTURA_PARTITA_DOPPIA T
WHERE  T.CD_CDS                 = D.CD_CDS
  AND  T.ESERCIZIO              = D.ESERCIZIO
  AND  T.CD_UNITA_ORGANIZZATIVA = D.CD_UNITA_ORGANIZZATIVA
  AND  T.PG_SCRITTURA           = D.PG_SCRITTURA
  AND  T.ATTIVA                 = 'Y'
  AND  (   T.CD_CAUSALE_COGE IS NULL
    OR (    T.CD_CAUSALE_COGE = 'DETERMINAZIONE_UTILE_PERDITA'
        AND r_conti_ass.CD_VOCE_EP = v_conto_avanzo))
  AND  T.CD_CDS                 = r_cds.CD_UNITA_ORGANIZZATIVA
  AND  T.ESERCIZIO              = p_es
  AND  T.CD_UNITA_ORGANIZZATIVA = DECODE(p_uo, '*', T.CD_UNITA_ORGANIZZATIVA, p_uo)
  AND  D.CD_VOCE_EP             = r_conti_ass.CD_VOCE_EP
  AND  (   NVL(TO_CHAR(D.DT_DA_COMPETENZA_COGE,'YYYY'), D.ESERCIZIO) = TO_CHAR(p_es)
    OR NVL(TO_CHAR(D.DT_A_COMPETENZA_COGE, 'YYYY'), D.ESERCIZIO) = TO_CHAR(p_es))
  AND  D.TI_ISTITUZ_COMMERC     = DECODE(p_ist_comm, '*', D.TI_ISTITUZ_COMMERC, p_ist_comm)
  AND  EXISTS (
    SELECT 1
    FROM   MOVIMENTO_COGE M2
    WHERE  M2.CD_CDS                 = D.CD_CDS
      AND  M2.ESERCIZIO              = D.ESERCIZIO
      AND  M2.CD_UNITA_ORGANIZZATIVA = D.CD_UNITA_ORGANIZZATIVA
      AND  M2.PG_SCRITTURA           = D.PG_SCRITTURA
      AND  M2.CD_VOCE_EP             = v_prof_perd
);

v_parziale_i_anno_cds :=
                                                CASE r_conti_ass.SEZIONE
                                                    WHEN 'D' THEN v_dare_cds  - v_avere_cds
                                                    WHEN 'A' THEN v_avere_cds - v_dare_cds
                                                    ELSE 0
END;
END;

ELSE
                                    -- Conto normale per singolo CDS
BEGIN
SELECT IMPORTO_FINALE,
       v_contamov_cds + 1
INTO   v_parziale_i_anno_cds, v_contamov_cds
FROM   BIL_RICLASSIFICATO
WHERE  ESERCIZIO              = p_es
  AND  CD_VOCE_EP             = r_conti_ass.CD_VOCE_EP
  AND  CD_TIPO_BILANCIO       = p_cd_tipo_bilancio
  AND  CD_UNITA_ORGANIZZATIVA = v_uo_ente;
EXCEPTION
                                        WHEN NO_DATA_FOUND THEN
SELECT v_dare_cds  + NVL(SUM(DECODE(D.SEZIONE, 'D', D.IM_MOVIMENTO)), 0),
       v_avere_cds + NVL(SUM(DECODE(D.SEZIONE, 'A', D.IM_MOVIMENTO)), 0),
       v_contamov_cds + COUNT(0)
INTO   v_dare_cds, v_avere_cds, v_contamov_cds
FROM   MOVIMENTO_COGE           D
   , SCRITTURA_PARTITA_DOPPIA T
WHERE  T.CD_CDS                 = D.CD_CDS
  AND  T.ESERCIZIO              = D.ESERCIZIO
  AND  T.CD_UNITA_ORGANIZZATIVA = D.CD_UNITA_ORGANIZZATIVA
  AND  T.PG_SCRITTURA           = D.PG_SCRITTURA
  AND  T.ATTIVA                 = 'Y'
  AND  (   T.CD_CAUSALE_COGE IS NULL
    OR (    T.CD_CAUSALE_COGE != 'CHIUSURA_CONTO_ECONOMICO'
                                                        AND T.CD_CAUSALE_COGE != 'CHIUSURA_STATO_PATRIMONIALE'
                                                        AND T.CD_CAUSALE_COGE != 'DETERMINAZIONE_UTILE_PERDITA'))
  AND  T.CD_CDS                 = r_cds.CD_UNITA_ORGANIZZATIVA
  AND  T.ESERCIZIO              = p_es
  AND  T.CD_UNITA_ORGANIZZATIVA = DECODE(p_uo, '*', T.CD_UNITA_ORGANIZZATIVA, p_uo)
  AND  D.CD_VOCE_EP             = r_conti_ass.CD_VOCE_EP
  -- Competenza: anno corrente
  AND  (   (    NVL(TO_CHAR(D.DT_DA_COMPETENZA_COGE,'YYYY'), D.ESERCIZIO) = TO_CHAR(p_es)
    AND  NVL(TO_CHAR(D.DT_A_COMPETENZA_COGE, 'YYYY'), D.ESERCIZIO) = TO_CHAR(p_es))
    -- A cavallo anno precedente / anno corrente
    OR  (    NVL(TO_CHAR(D.DT_DA_COMPETENZA_COGE,'YYYY'), D.ESERCIZIO) = TO_CHAR(p_es - 1)
        AND  NVL(TO_CHAR(D.DT_A_COMPETENZA_COGE, 'YYYY'), D.ESERCIZIO) = TO_CHAR(p_es))
    -- A cavallo anno corrente / anno successivo (risconti)
    OR  (    NVL(TO_CHAR(D.DT_DA_COMPETENZA_COGE,'YYYY'), D.ESERCIZIO) = TO_CHAR(p_es)
        AND  NVL(TO_CHAR(D.DT_A_COMPETENZA_COGE, 'YYYY'), D.ESERCIZIO) = TO_CHAR(p_es + 1))
    -- Completamente in anno successivo (risconti)
    OR  (    NVL(TO_CHAR(D.DT_DA_COMPETENZA_COGE,'YYYY'), D.ESERCIZIO) = TO_CHAR(p_es + 1)
        AND  NVL(TO_CHAR(D.DT_A_COMPETENZA_COGE, 'YYYY'), D.ESERCIZIO) = TO_CHAR(p_es + 1)))
  AND  D.TI_ISTITUZ_COMMERC     = DECODE(p_ist_comm, '*', D.TI_ISTITUZ_COMMERC, p_ist_comm);

v_parziale_i_anno_cds :=
                                                CASE r_conti_ass.SEZIONE
                                                    WHEN 'D' THEN v_dare_cds  - v_avere_cds
                                                    WHEN 'A' THEN v_avere_cds - v_dare_cds
                                                    ELSE 0
END;
END;

                                    -- Storno quota risconto se la chiusura COEP non è ancora avvenuta
BEGIN
SELECT *
INTO   v_chius_coep
FROM   CHIUSURA_COEP
WHERE  ESERCIZIO = p_es
  AND  CD_CDS    = r_cds.CD_UNITA_ORGANIZZATIVA;
EXCEPTION
                                        WHEN NO_DATA_FOUND THEN
                                            v_chius_coep.STATO := NULL;
END;

                                    IF v_chius_coep.STATO IS NULL
                                       OR v_chius_coep.STATO NOT IN ('P', 'C')
                                    THEN
SELECT v_dare_cds  - NVL(SUM(ROUND(IM_MOVIMENTO *
                                   ROUND((D.DT_A_COMPETENZA_COGE
                                              - TO_DATE('0101' || TO_CHAR(p_es + 1), 'DDMMYYYY') + 1)
                                             / (D.DT_A_COMPETENZA_COGE - D.DT_DA_COMPETENZA_COGE + 1), 2), 2)), 0),
       v_avere_cds - NVL(SUM(ROUND(IM_MOVIMENTO *
                                   ROUND((D.DT_A_COMPETENZA_COGE
                                              - TO_DATE('0101' || TO_CHAR(p_es + 1), 'DDMMYYYY') + 1)
                                             / (D.DT_A_COMPETENZA_COGE - D.DT_DA_COMPETENZA_COGE + 1), 2), 2)), 0),
       v_contamov_cds + COUNT(0)
INTO   v_dare_cds, v_avere_cds, v_contamov_cds
FROM   MOVIMENTO_COGE           D
   , SCRITTURA_PARTITA_DOPPIA T
WHERE  T.CD_CDS                 = D.CD_CDS
  AND  T.ESERCIZIO              = D.ESERCIZIO
  AND  T.CD_UNITA_ORGANIZZATIVA = D.CD_UNITA_ORGANIZZATIVA
  AND  T.PG_SCRITTURA           = D.PG_SCRITTURA
  AND  T.ATTIVA                 = 'Y'
  AND  (   T.CD_CAUSALE_COGE IS NULL
    OR (    T.CD_CAUSALE_COGE != 'CHIUSURA_CONTO_ECONOMICO'
                                                    AND T.CD_CAUSALE_COGE != 'CHIUSURA_STATO_PATRIMONIALE'
                                                    AND T.CD_CAUSALE_COGE != 'DETERMINAZIONE_UTILE_PERDITA'))
  AND  T.CD_CDS                 = r_cds.CD_UNITA_ORGANIZZATIVA
  AND  T.ESERCIZIO              = p_es
  AND  T.CD_UNITA_ORGANIZZATIVA = DECODE(p_uo, '*', T.CD_UNITA_ORGANIZZATIVA, p_uo)
  AND  D.CD_VOCE_EP             = r_conti_ass.CD_VOCE_EP
  -- Solo movimenti a cavallo con anno successivo (risconti)
  AND  NVL(TO_CHAR(D.DT_DA_COMPETENZA_COGE,'YYYY'), D.ESERCIZIO) = TO_CHAR(p_es)
  AND  NVL(TO_CHAR(D.DT_A_COMPETENZA_COGE, 'YYYY'), D.ESERCIZIO) = TO_CHAR(p_es + 1)
  AND  D.TI_ISTITUZ_COMMERC     = DECODE(p_ist_comm, '*', D.TI_ISTITUZ_COMMERC, p_ist_comm);
END IF;  -- chiusura COEP

END IF;  -- conto avanzo / conto normale

END LOOP;  -- CDS validi

                            IF p_conti_sn = 'Y' AND v_contamov_cds > 0 THEN
                                v_i := v_i + 1;
INSERT INTO PRT_VPG_BIL_RICLASSIFICATO (
    ID, CHIAVE, TIPO, SEQUENZA,
    ORDINE, CONTO_RICLASS,
    I_LIVELLO, II_LIVELLO, III_LIVELLO, IV_LIVELLO,
    DESCRIZIONE,
    PARZIALE_I_ANNO, TOTALE_I_ANNO,
    PARZIALE_II_ANNO, TOTALE_II_ANNO,
    SN_TOTALE
) VALUES (
             v_id, 'chiave', 't', v_i,
             r_schema_ce.SEQUENZA, NULL, NULL, NULL, NULL, NULL,
             r_conti_ass.CD_VOCE_EP || ' ' || v_conto.DS_VOCE_EP,
             v_parziale_i_anno_cds,
             NULL, NULL, NULL, 'N'
         );
END IF;

                            v_dare1  := v_dare1  + v_dare_cds;
                            v_avere1 := v_avere1 + v_avere_cds;

END;  -- DECLARE loop CDS
END IF;  -- percorso veloce / percorso standard

END;  -- DECLARE blocco ottimizzazione

                -- Accumulo parziale per la voce di schema
                IF r_conti_ass.SEZIONE = 'D' THEN
                    v_parz1 := NVL(v_parz1, 0) + NVL(v_dare1, 0) - NVL(v_avere1, 0);
                ELSIF r_conti_ass.SEZIONE = 'A' THEN
                    v_parz1 := NVL(v_parz1, 0) + NVL(v_avere1, 0) - NVL(v_dare1, 0);
END IF;

                v_tot1 := NULL;

END LOOP;  -- c_conti_associati
CLOSE c_conti_associati;

-- =====================================================================
-- CASO B: Voce di totale con formula → calcolo per operatori +/-
-- =====================================================================
ELSIF r_schema_ce.FORMULA IS NOT NULL THEN

            v_parz1    := NULL;
            v_parz2    := NULL;
            v_tot1     := NULL;
            v_tot2     := NULL;
            v_flag_tot := 'S';
            v_indice   := 0;

            LOOP  -- parsing formula

v_indice := v_indice + 1;

SELECT INSTR(r_schema_ce.FORMULA, '[', 1, v_indice) INTO v_m1 FROM DUAL;
SELECT INSTR(r_schema_ce.FORMULA, ']', 1, v_indice) INTO v_m2 FROM DUAL;
SELECT INSTR(r_schema_ce.FORMULA, ',', 1, v_indice) INTO v_m3 FROM DUAL;
SELECT INSTR(r_schema_ce.FORMULA, '}', 1, v_indice) INTO v_m4 FROM DUAL;

IF v_m1 > 0 THEN
SELECT NVL(PARZIALE_I_ANNO, 0),
       NVL(PARZIALE_II_ANNO, 0)
INTO   v_tot_incr1, v_tot_incr2
FROM   PRT_VPG_BIL_RICLASSIFICATO
WHERE  CONTO_RICLASS = SUBSTR(r_schema_ce.FORMULA, v_m3 + 1, v_m4 - (v_m3 + 1));

IF SUBSTR(r_schema_ce.FORMULA, v_m1 + 1, v_m2 - (v_m1 + 1)) = '+' THEN
                        v_tot1 := NVL(v_tot1, 0) + v_tot_incr1;
                        v_tot2 := NVL(v_tot2, 0) + v_tot_incr2;
                    ELSIF SUBSTR(r_schema_ce.FORMULA, v_m1 + 1, v_m2 - (v_m1 + 1)) = '-' THEN
                        v_tot1 := NVL(v_tot1, 0) - v_tot_incr1;
                        v_tot2 := NVL(v_tot2, 0) - v_tot_incr2;
END IF;
END IF;

                EXIT WHEN v_m1 = 0;

END LOOP;  -- parsing formula

        -- =====================================================================
        -- CASO C: Voce di intestazione/separatore (né mastrino né formula)
        -- =====================================================================
ELSE
            v_parz1 := NULL;
            v_parz2 := NULL;
            v_tot1  := NULL;
            v_tot2  := NULL;
END IF;

        -- =====================================================================
        -- Inserimento riga nello schema di output
        -- =====================================================================
        v_i := v_i + 1;

INSERT INTO PRT_VPG_BIL_RICLASSIFICATO (
    ID, CHIAVE, TIPO, SEQUENZA,
    ORDINE, CONTO_RICLASS,
    I_LIVELLO, II_LIVELLO, III_LIVELLO, IV_LIVELLO,
    DESCRIZIONE,
    PARZIALE_I_ANNO, TOTALE_I_ANNO,
    PARZIALE_II_ANNO, TOTALE_II_ANNO,
    SN_TOTALE
) VALUES (
             v_id, 'chiave', 't', v_i,
             r_schema_ce.SEQUENZA,
             r_schema_ce.CD_GRUPPO_EP,
             v_liv1, v_liv2, v_liv3, NULL,
             DECODE(r_schema_ce.CD_GRUPPO_EP,
                    'AVA', r_schema_ce.DS_GRUPPO_EP || ': ' || TO_CHAR(v_parz1, '999G999G999G999D99'),
                    r_schema_ce.DS_GRUPPO_EP),
             DECODE(r_schema_ce.CD_GRUPPO_EP, 'AVA', NULL, v_parz1),
             DECODE(r_schema_ce.CD_GRUPPO_EP, 'AVA', NULL, v_tot1),
             v_parz2,
             v_tot2,
             v_flag_tot
         );

END LOOP;  -- c_schema_ce
CLOSE c_schema_ce;

END PRT_S_CE_RICLASSIFICATO_J;
/

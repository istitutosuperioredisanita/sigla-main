CREATE OR REPLACE PROCEDURE PRT_S_SP_RICLASSIFICATO
--
-- Date: 06/08/2004
-- Version: 1.1
--
-- Vista di stampa Stato Patrimoniale Riclassificato
--
-- History:
--
-- Date: 28/07/2004
-- Version: 1.0
-- Creazione
--
-- Date: 06/08/2004
-- Version: 1.1
-- Modificati il nome della view e della procedure, eliminato il nome dello
-- schema, corretta l'indicazione dei parametri nel documento, aggiunto history
-- nella procedure
--
-- Body
--
(ATTPAS         IN VARCHAR2,
 IST_COMM       IN VARCHAR2,
 inEs           in number,
 CDS            in varchar2,
 uo             in varchar2,
 conti_sn       In CHAR,
 cdTipoBilancio in VARCHAR2
) is

 aId    number;
 i      number;
 INDICE NUMBER := 0;
 aNum1  number;
 LIV1   VARCHAR2(10);
 LIV2   VARCHAR2(10);
 LIV3   VARCHAR2(10);
 LIV4   VARCHAR2(10);
 PARZ1  NUMBER(20,3);
 TOT1   NUMBER(20,3);
 PARZ2  NUMBER(20,3);
 TOT2   NUMBER(20,3);
 TOT_INCR1  NUMBER(20,3);
 TOT_INCR2  NUMBER(20,3);
 DARE1  NUMBER(20,3);
 AVERE1 NUMBER(20,3);
 DARE2  NUMBER(20,3);
 AVERE2 NUMBER(20,3);
 CONTAMOV  NUMBER := 0;
 M1     NUMBER;
 M2     NUMBER;
 M3     NUMBER;
 M4     NUMBER;
 FLAG_TOT VARCHAR2(1);

ANNO_PREC NUMBER;

 CONTO_AVANZO   VOCE_EP.CD_VOCE_EP%TYPE;
 STATO_PATR     VOCE_EP.CD_VOCE_EP%TYPE;

 CONTO_AVANZO_AP   VOCE_EP.CD_VOCE_EP%TYPE;
 STATO_PATR_AP     VOCE_EP.CD_VOCE_EP%TYPE;
 aConto voce_ep%Rowtype;

CURSOR SCHEMA_SP_RICLASSIFICATO IS
 SELECT *
 FROM   CNR_GRUPPO_EP
 WHERE  CD_PIANO_GRUPPI = ATTPAS
   AND  CD_TIPO_BILANCIO = cdTipoBilancio

 ORDER BY SEQUENZA;

CURSOR CONTI_ASSOCIATI (CONTO_RICLASSIFICATO IN VARCHAR, TIPO_BILANCIO IN VARCHAR, ANNO_COMP NUMBER) IS
 SELECT ESERCIZIO, CD_VOCE_EP, SEZIONE, SEGNO
 FROM   CNR_ASS_CONTO_GRUPPO_EP
 WHERE  esercizio = ANNO_COMP
   AND CD_PIANO_GRUPPI = ATTPAS
   AND CD_GRUPPO_EP = CONTO_RICLASSIFICATO
   AND CD_TIPO_BILANCIO = TIPO_BILANCIO
 ORDER BY CD_VOCE_EP;

CURSOR CONTI_MOVIMENTATI_NON_ASSOCIATI(ANNO_COMP NUMBER) IS
  SELECT D.CD_VOCE_EP
  FROM   MOVIMENTO_COGE D, SCRITTURA_PARTITA_DOPPIA T
  WHERE  T.CD_CDS  = D.CD_CDS   AND
         T.ESERCIZIO = D.ESERCIZIO AND
         T.CD_UNITA_ORGANIZZATIVA = D.CD_UNITA_ORGANIZZATIVA and
         T.PG_SCRITTURA = D.PG_SCRITTURA AND
         T.ATTIVA = 'Y' AND
       ((T.CD_CAUSALE_COGE IS NULL) OR
        (T.CD_CAUSALE_COGE != 'CHIUSURA_CONTO_ECONOMICO' AND
         T.CD_CAUSALE_COGE != 'CHIUSURA_STATO_PATRIMONIALE' AND
         T.CD_CAUSALE_COGE != 'DETERMINAZIONE_UTILE_PERDITA')) AND
         T.ESERCIZIO  = ANNO_COMP
  GROUP BY D.CD_VOCE_EP
  HAVING Nvl(sum(DECODE(D.SEZIONE, 'D', D.im_movimento, -D.im_movimento)), 0)!=0
  MINUS
  SELECT CD_VOCE_EP
  FROM   CNR_ASS_CONTO_GRUPPO_EP
  WHERE  esercizio = ANNO_COMP
  MINUS
  SELECT VAL01
  FROM   CONFIGURAZIONE_CNR
  WHERE  ESERCIZIO = ANNO_COMP
  AND CD_UNITA_FUNZIONALE = '*'
  AND CD_CHIAVE_PRIMARIA   = 'VOCEEP_SPECIALE'
  AND CD_CHIAVE_SECONDARIA = 'STATO_PATRIMONIALE'
  ORDER BY 1;

CONTI_ASS       CONTI_ASSOCIATI%ROWTYPE;
SCHEMA_SP       SCHEMA_SP_RICLASSIFICATO%ROWTYPE;
CONTI_MOV       CONTI_MOVIMENTATI_NON_ASSOCIATI%ROWTYPE;

begin

ANNO_PREC := INES - 1;

 select IBMSEQ00_CR_PACKAGE.nextval
 into aId
 from dual;

 SELECT VAL01
 INTO   CONTO_AVANZO
 FROM   CONFIGURAZIONE_CNR
 WHERE  ESERCIZIO = INES AND
        CD_UNITA_FUNZIONALE = '*' AND
        CD_CHIAVE_PRIMARIA   = 'VOCEEP_SPECIALE' AND
        CD_CHIAVE_SECONDARIA = 'UTILE_PERDITA_ESERCIZIO';

 SELECT VAL01
 INTO   STATO_PATR
 FROM   CONFIGURAZIONE_CNR
 WHERE  ESERCIZIO = INES AND
        CD_UNITA_FUNZIONALE = '*' AND
        CD_CHIAVE_PRIMARIA   = 'VOCEEP_SPECIALE' AND
        CD_CHIAVE_SECONDARIA = 'STATO_PATRIMONIALE';

 i := 0;

OPEN SCHEMA_SP_RICLASSIFICATO;
LOOP

  FETCH SCHEMA_SP_RICLASSIFICATO INTO SCHEMA_SP;
  EXIT WHEN SCHEMA_SP_RICLASSIFICATO%NOTFOUND;

  PARZ1 := NULL;
  TOT1  := NULL;
  PARZ2 := NULL;
  TOT2  := NULL;

  FLAG_TOT := 'N';

IF SCHEMA_SP.CD_GRUPPO_PADRE IS NULL THEN
  LIV1 := SCHEMA_SP.NOME;
  LIV2 := NULL;
  LIV3 := NULL;
  LIV4 := NULL;
ELSIF SUBSTR(SCHEMA_SP.NOME, 1, 1) BETWEEN 'a' AND 'z' THEN
  LIV1 := NULL;
  LIV2 := NULL;
  LIV3 := NULL;
  LIV4 := SCHEMA_SP.NOME;
ELSIF SUBSTR(SCHEMA_SP.NOME,1,1) in ('0', '1', '2', '3', '4', '5', '6', '7', '8', '9') THEN
  LIV1 := NULL;
  LIV2 := NULL;
  LIV3 := SCHEMA_SP.NOME;
  LIV4 := NULL;
ELSE
  LIV1 := NULL;
  LIV2 := SCHEMA_SP.NOME;
  LIV3 := NULL;
  LIV4 := NULL;
END IF;

--- GIRO SUI CONTI ASSOCIATI AL CONTO RICLASSIFICATO SOLO PER I MASTRI

IF SCHEMA_SP.FL_MASTRINO = 'Y' THEN
   OPEN CONTI_ASSOCIATI (SCHEMA_SP.CD_GRUPPO_EP, cdTipoBilancio, INES);
   LOOP

     FETCH CONTI_ASSOCIATI INTO CONTI_ASS;
     EXIT WHEN CONTI_ASSOCIATI%NOTFOUND;

     aConto := cnrctb002.getVoceEp(CONTI_ASS.Esercizio, CONTI_ASS.Cd_Voce_ep);

---------------------------------------------------------------------------------
------------ INIZIO GIRO SULLE CIFRE DELL'ESERCIZIO IN GESTIONE -----------------
---------------------------------------------------------------------------------

  IF CONTI_ASS.CD_VOCE_EP = CONTO_AVANZO THEN
        SELECT Nvl(sum(DECODE(D.SEZIONE, 'D', D.im_movimento)), 0),
               Nvl(sum(DECODE(D.SEZIONE, 'A', D.im_movimento)), 0),
               1 --NEL CAMPO CONTA METTO SEMPRE > 0 PER STAMPARE L'AVANZO
        INTO   DARE1, AVERE1, CONTAMOV
        FROM   MOVIMENTO_COGE D, SCRITTURA_PARTITA_DOPPIA T
        WHERE  T.CD_CDS  = D.CD_CDS   AND
               T.ESERCIZIO = D.ESERCIZIO AND
               T.CD_UNITA_ORGANIZZATIVA = D.CD_UNITA_ORGANIZZATIVA and
               T.PG_SCRITTURA = D.PG_SCRITTURA AND
               T.ATTIVA = 'Y' AND
             ((T.CD_CAUSALE_COGE IS NULL) OR
              (T.CD_CAUSALE_COGE = 'DETERMINAZIONE_UTILE_PERDITA' AND CONTI_ASS.CD_VOCE_EP = CONTO_AVANZO) OR
              (T.CD_CAUSALE_COGE = 'RIAPERTURA_CONTI' AND CONTI_ASS.CD_VOCE_EP = CONTO_AVANZO)) AND
               T.CD_CDS = Decode(CDS, '*', T.CD_CDS, CDS) and
               T.ESERCIZIO  = INES                 AND
               T.CD_UNITA_ORGANIZZATIVA = Decode(uo, '*', T.CD_UNITA_ORGANIZZATIVA, uo) and
               D.CD_VOCE_EP = CONTI_ASS.CD_VOCE_EP AND
              (NVL(TO_CHAR(D.DT_DA_COMPETENZA_COGE, 'YYYY'), D.ESERCIZIO) = TO_CHAR(INES) OR
               NVL(TO_CHAR(D.DT_A_COMPETENZA_COGE,  'YYYY'), D.ESERCIZIO) = TO_CHAR(INES)) AND
               D.TI_ISTITUZ_COMMERC = Decode(IST_COMM, '*', D.TI_ISTITUZ_COMMERC, IST_COMM);
  ELSE
        SELECT Nvl(sum(DECODE(D.SEZIONE, 'D', D.im_movimento)), 0),
               Nvl(sum(DECODE(D.SEZIONE, 'A', D.im_movimento)), 0),
               COUNT(0)
        INTO   DARE1, AVERE1, CONTAMOV
        FROM   MOVIMENTO_COGE D, SCRITTURA_PARTITA_DOPPIA T
        WHERE  T.CD_CDS  = D.CD_CDS   AND
               T.ESERCIZIO = D.ESERCIZIO AND
               T.CD_UNITA_ORGANIZZATIVA = D.CD_UNITA_ORGANIZZATIVA and
               T.PG_SCRITTURA = D.PG_SCRITTURA AND
               T.ATTIVA = 'Y' AND
             ((T.CD_CAUSALE_COGE IS NULL) OR
              (T.CD_CAUSALE_COGE != 'CHIUSURA_CONTO_ECONOMICO' AND
               T.CD_CAUSALE_COGE != 'CHIUSURA_STATO_PATRIMONIALE' AND
               T.CD_CAUSALE_COGE != 'DETERMINAZIONE_UTILE_PERDITA')) AND
               T.CD_CDS = Decode(CDS, '*', T.CD_CDS, CDS) and
               T.ESERCIZIO  = INES                 AND
               T.CD_UNITA_ORGANIZZATIVA = Decode(uo, '*', T.CD_UNITA_ORGANIZZATIVA, uo) and
               D.CD_VOCE_EP = CONTI_ASS.CD_VOCE_EP AND
               D.TI_ISTITUZ_COMMERC = Decode(IST_COMM, '*', D.TI_ISTITUZ_COMMERC, IST_COMM);

  END IF;

If conti_sn = 'Y' AND CONTAMOV>0 Then
   I := I + 1;
   -- inserimento dello schema di riclassificazione NELLA VIEW (FISSO)
   Insert into PRT_VPG_BIL_RICLASSIFICATO (ID, CHIAVE, TIPO, SEQUENZA,
        ORDINE, CONTO_RICLASS, I_LIVELLO, II_LIVELLO, III_LIVELLO, IV_LIVELLO,
        DESCRIZIONE, PARZIALE_I_ANNO, TOTALE_I_ANNO, PARZIALE_II_ANNO, TOTALE_II_ANNO,
        SN_TOTALE)
   Values (aId, 'chiave', 't', i, SCHEMA_SP.SEQUENZA, Null, Null, Null, Null, Null,
           CONTI_ASS.CD_VOCE_EP||' '||aConto.ds_voce_ep,
           Decode(CONTI_ASS.SEZIONE, 'D', DARE1-AVERE1, 'A', AVERE1-DARE1),
           Null, Null, Null, 'N');
End If;

  IF CONTI_ASS.SEZIONE = 'D' THEN
     IF CONTI_ASS.SEGNO = '+' THEN
          PARZ1 := NVL(PARZ1, 0) + (NVL(DARE1, 0) - NVL(AVERE1, 0));
     ELSIF CONTI_ASS.SEGNO = '-' THEN
          PARZ1 := NVL(PARZ1, 0) - (NVL(DARE1, 0) - NVL(AVERE1, 0));
     END IF;
  ELSIF CONTI_ASS.SEZIONE = 'A' THEN
     IF CONTI_ASS.SEGNO = '+' THEN
          PARZ1 := NVL(PARZ1, 0) + (NVL(AVERE1, 0) - NVL(DARE1, 0));
     ELSIF CONTI_ASS.SEGNO = '-' THEN
          PARZ1 := NVL(PARZ1, 0) - (NVL(AVERE1, 0) - NVL(DARE1, 0));
     END IF;
  END IF;

  TOT1  := NULL;

-------------- FINE GIRO SULLE CIFRE DELL'ESERCIZIO IN GESTIONE -----------------
------------ E INIZIO GIRO SULLE CIFRE DELL'ESERCIZIO PRECEDENTE  ---------------

  IF CONTI_ASS.CD_VOCE_EP = CONTO_AVANZO THEN

        SELECT Nvl(sum(DECODE(D.SEZIONE, 'D', D.im_movimento)), 0),
               Nvl(sum(DECODE(D.SEZIONE, 'A', D.im_movimento)), 0)
        INTO   DARE2, AVERE2
        FROM   MOVIMENTO_COGE D, SCRITTURA_PARTITA_DOPPIA T
        WHERE  T.CD_CDS  = D.CD_CDS   AND
               T.ESERCIZIO = D.ESERCIZIO AND
               T.CD_UNITA_ORGANIZZATIVA = D.CD_UNITA_ORGANIZZATIVA and
               T.PG_SCRITTURA = D.PG_SCRITTURA AND
               T.ATTIVA = 'Y' AND
             ((T.CD_CAUSALE_COGE IS NULL) OR
              (T.CD_CAUSALE_COGE = 'DETERMINAZIONE_UTILE_PERDITA' AND CONTI_ASS.CD_VOCE_EP = CONTO_AVANZO) OR
              (T.CD_CAUSALE_COGE = 'RIAPERTURA_CONTI' AND CONTI_ASS.CD_VOCE_EP = CONTO_AVANZO)) AND
               T.CD_CDS = Decode(CDS, '*', T.CD_CDS, CDS) and
               T.ESERCIZIO  = ANNO_PREC                 AND
               T.CD_UNITA_ORGANIZZATIVA = Decode(uo, '*', T.CD_UNITA_ORGANIZZATIVA, uo) and
               D.CD_VOCE_EP = CONTI_ASS.CD_VOCE_EP AND
               D.TI_ISTITUZ_COMMERC = Decode(IST_COMM, '*', D.TI_ISTITUZ_COMMERC, IST_COMM);

  ELSE

        SELECT Nvl(sum(DECODE(D.SEZIONE, 'D', D.im_movimento)), 0),
               Nvl(sum(DECODE(D.SEZIONE, 'A', D.im_movimento)), 0)
        INTO   DARE2, AVERE2
        FROM   MOVIMENTO_COGE D, SCRITTURA_PARTITA_DOPPIA T
        WHERE  T.CD_CDS  = D.CD_CDS   AND
               T.ESERCIZIO = D.ESERCIZIO AND
               T.CD_UNITA_ORGANIZZATIVA = D.CD_UNITA_ORGANIZZATIVA and
               T.PG_SCRITTURA = D.PG_SCRITTURA AND
               T.ATTIVA = 'Y' AND
             ((T.CD_CAUSALE_COGE IS NULL) OR
              (T.CD_CAUSALE_COGE != 'CHIUSURA_CONTO_ECONOMICO' AND
               T.CD_CAUSALE_COGE != 'CHIUSURA_STATO_PATRIMONIALE' AND
               T.CD_CAUSALE_COGE != 'DETERMINAZIONE_UTILE_PERDITA')) AND
               T.CD_CDS = Decode(CDS, '*', T.CD_CDS, CDS) and
               T.ESERCIZIO  = ANNO_PREC                 AND
               T.CD_UNITA_ORGANIZZATIVA = Decode(uo, '*', T.CD_UNITA_ORGANIZZATIVA, uo) and
               D.CD_VOCE_EP = CONTI_ASS.CD_VOCE_EP AND
               D.TI_ISTITUZ_COMMERC = Decode(IST_COMM, '*', D.TI_ISTITUZ_COMMERC, IST_COMM);
  END IF;

  IF CONTI_ASS.SEZIONE = 'D' THEN
     IF CONTI_ASS.SEGNO = '+' THEN
          PARZ2 := NVL(PARZ2, 0) + (NVL(DARE2, 0) - NVL(AVERE2, 0));
     ELSIF CONTI_ASS.SEGNO = '-' THEN
          PARZ2 := NVL(PARZ2, 0) - (NVL(DARE2, 0) - NVL(AVERE2, 0));
     END IF;

  ELSIF CONTI_ASS.SEZIONE = 'A' THEN

     IF CONTI_ASS.SEGNO = '+' THEN
          PARZ2 := NVL(PARZ2, 0) + (NVL(AVERE2, 0) - NVL(DARE2, 0));
     ELSIF CONTI_ASS.SEGNO = '-' THEN
          PARZ2 := NVL(PARZ2, 0) - (NVL(AVERE2, 0) - NVL(DARE2, 0));
     END IF;
  END IF;

  TOT2  := NULL;

---------------------------------------------------------------------------------
-------------- FINE GIRO SULLE CIFRE DELL'ESERCIZIO PRECEDENTE ------------------
---------------------------------------------------------------------------------

   END LOOP;
   CLOSE CONTI_ASSOCIATI;

ELSIF SCHEMA_SP.FORMULA IS NOT NULL THEN

   PARZ1 := NULL;
   PARZ2 := NULL;

   TOT1 := NULL;
   TOT2 := NULL;

   FLAG_TOT := 'S';

---------------------------------------------------------------------------------
------------------------ CALCOLO DEI TOTALI CON LA FORMULA ----------------------
---------------------------------------------------------------------------------

   INDICE := 0;

   LOOP

     INDICE := INDICE + 1;

     SELECT INSTR(SCHEMA_SP.FORMULA, '[', 1, INDICE) INTO M1 FROM DUAL;
     SELECT INSTR(SCHEMA_SP.FORMULA, ']', 1, INDICE) INTO M2 FROM DUAL;
     SELECT INSTR(SCHEMA_SP.FORMULA, ',', 1, INDICE) INTO M3 FROM DUAL;
     SELECT INSTR(SCHEMA_SP.FORMULA, '}', 1, INDICE) INTO M4 FROM DUAL;

     IF M1 > 0 THEN

      SELECT SUM(NVL(PARZIALE_I_ANNO, 0)), SUM(NVL(PARZIALE_II_ANNO, 0))
      INTO   TOT_INCR1, TOT_INCR2
      FROM   PRT_VPG_BIL_RICLASSIFICATO
      WHERE  CONTO_RICLASS = SUBSTR(SCHEMA_SP.FORMULA, M3+1, M4-(M3+1))
        And id = aId;

      IF SUBSTR(SCHEMA_SP.FORMULA, M1+1, M2-(M1+1)) = '+' THEN
            TOT1 :=  NVL(TOT1, 0) + TOT_INCR1;
            TOT2 :=  NVL(TOT2, 0) + TOT_INCR2;
      ELSIF SUBSTR(SCHEMA_SP.FORMULA, M1+1, M2-(M1+1)) = '-' THEN
            TOT1 :=  NVL(TOT1, 0) - TOT_INCR1;
            TOT2 :=  NVL(TOT2, 0) - TOT_INCR2;
      END IF;

     END IF;

   EXIT WHEN M1 = 0;

   END LOOP;

ELSE
   PARZ1 := NULL;
   PARZ2 := NULL;

   TOT1  := NULL;
   TOT2  := NULL;
END IF;

I := I + 1;

-- inserimento dello schema di riclassificazione NELLA VIEW (FISSO)

insert into PRT_VPG_BIL_RICLASSIFICATO (ID, CHIAVE, TIPO, SEQUENZA,
        ORDINE, CONTO_RICLASS, I_LIVELLO, II_LIVELLO, III_LIVELLO, IV_LIVELLO,
        DESCRIZIONE, PARZIALE_I_ANNO, TOTALE_I_ANNO, PARZIALE_II_ANNO, TOTALE_II_ANNO,
        SN_TOTALE)
VALUES (aId, 'chiave', 't', i, SCHEMA_SP.SEQUENZA, SCHEMA_SP.CD_GRUPPO_EP,
        LIV1, LIV2, LIV3, LIV4, SCHEMA_SP.DS_GRUPPO_EP, PARZ1, TOT1, PARZ2, TOT2, FLAG_TOT);

END LOOP; -- FINE LOOP PRINCIPALE SULLO SCHEMA DI RICLASSIFICAZIONE
CLOSE SCHEMA_SP_RICLASSIFICATO;

DECLARE
  CONTA NUMBER:=0;
BEGIN
OPEN CONTI_MOVIMENTATI_NON_ASSOCIATI (INES);
LOOP
    FETCH CONTI_MOVIMENTATI_NON_ASSOCIATI INTO CONTI_MOV;
    EXIT WHEN CONTI_MOVIMENTATI_NON_ASSOCIATI%NOTFOUND;

    IF CONTI_MOV.Cd_Voce_ep IS NOT NULL THEN
       aConto := cnrctb002.getVoceEp(INES, CONTI_MOV.Cd_Voce_ep);
	ELSE
	   aConto := NULL;
    END IF;

    SELECT Nvl(sum(DECODE(D.SEZIONE, 'D', D.im_movimento)), 0),
           Nvl(sum(DECODE(D.SEZIONE, 'A', D.im_movimento)), 0),
           COUNT(0)
    INTO   DARE1, AVERE1, CONTAMOV
    FROM   MOVIMENTO_COGE D, SCRITTURA_PARTITA_DOPPIA T
    WHERE  T.CD_CDS  = D.CD_CDS   AND
           T.ESERCIZIO = D.ESERCIZIO AND
           T.CD_UNITA_ORGANIZZATIVA = D.CD_UNITA_ORGANIZZATIVA and
           T.PG_SCRITTURA = D.PG_SCRITTURA AND
           T.ATTIVA = 'Y' AND
         ((T.CD_CAUSALE_COGE IS NULL) OR
          (T.CD_CAUSALE_COGE != 'CHIUSURA_CONTO_ECONOMICO' AND
           T.CD_CAUSALE_COGE != 'CHIUSURA_STATO_PATRIMONIALE' AND
           T.CD_CAUSALE_COGE != 'DETERMINAZIONE_UTILE_PERDITA')) AND
           T.ESERCIZIO  = INES                 AND
           ((CONTI_MOV.CD_VOCE_EP IS NOT NULL AND D.CD_VOCE_EP = CONTI_MOV.CD_VOCE_EP) OR
            (CONTI_MOV.CD_VOCE_EP IS NULL AND D.CD_VOCE_EP IS NULL));

     CONTA := CONTA + 1;

     IF CONTA=1 THEN
        -- inserimento dello schema di riclassificazione NELLA VIEW (FISSO)
        I := I + 1;
        insert into PRT_VPG_BIL_RICLASSIFICATO (ID, CHIAVE, TIPO, SEQUENZA,
                ORDINE, CONTO_RICLASS, I_LIVELLO, II_LIVELLO, III_LIVELLO, IV_LIVELLO,
                DESCRIZIONE, PARZIALE_I_ANNO, TOTALE_I_ANNO, PARZIALE_II_ANNO, TOTALE_II_ANNO,
                SN_TOTALE)
        VALUES (aId, 'chiave', 't', i, 999999, 'XXXXX',
                NULL, NULL, NULL, NULL, '***** CONTI NON ASSOCIATI *****', NULL, NULL, NULL, NULL, 'Y');
     END IF;
     I := I + 1;
     insert into PRT_VPG_BIL_RICLASSIFICATO (ID, CHIAVE, TIPO, SEQUENZA,
             ORDINE, CONTO_RICLASS, I_LIVELLO, II_LIVELLO, III_LIVELLO, IV_LIVELLO,
             DESCRIZIONE, PARZIALE_I_ANNO, TOTALE_I_ANNO, PARZIALE_II_ANNO, TOTALE_II_ANNO,
             SN_TOTALE)
     VALUES (aId, 'chiave', 't', i, 999999, 'XXXXX',
             'Z', NULL, NULL, NULL, CONTI_MOV.CD_VOCE_EP||' '||aConto.ds_voce_ep, DARE1-AVERE1, NULL, NULL, NULL, 'N');

END LOOP; -- FINE LOOP PRINCIPALE SULLO SCHEMA DI RICLASSIFICAZIONE
CLOSE CONTI_MOVIMENTATI_NON_ASSOCIATI;
End;
End;
/

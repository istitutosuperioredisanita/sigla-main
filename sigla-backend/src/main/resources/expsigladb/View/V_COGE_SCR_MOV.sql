--------------------------------------------------------
--  DDL for View V_COGE_SCR_MOV
--------------------------------------------------------
CREATE OR REPLACE FORCE EDITIONABLE VIEW "V_COGE_SCR_MOV" ("DT_CONTABILIZZAZIONE", "CD_CDS", "ESERCIZIO", "CD_UNITA_ORGANIZZATIVA", "PG_SCRITTURA", "CD_TERZO", "ORIGINE_SCRITTURA", "CD_CAUSALE_COGE", "TI_SCRITTURA", "CD_TIPO_DOCUMENTO", "DS_TIPO_DOCUMENTO", "CD_CDS_DOCUMENTO", "CD_UO_DOCUMENTO", "PG_NUMERO_DOCUMENTO", "ESERCIZIO_DOCUMENTO_AMM", "CD_COMP_DOCUMENTO", "IM_SCRITTURA", "STATO", "ATTIVA", "CD_VOCE_EP", "DS_VOCE_EP", "CDR_GAE", "CD_GAE", "DS_GAE", "SEZIONE", "IM_MOVIMENTO", "DT_DA_COMPETENZA_COGE", "DT_A_COMPETENZA_COGE", "TI_ISTITUZ_COMMERC", "FL_MOV_TERZO", "DS_SCRITTURA", "CD_CAUSALE_CONTABILE", "DS_CAUSALE_CONTABILE", "DESC_DOCUMENTO") AS
SELECT
--
-- Date: 14/04/2004
-- Version: 1.0
--
-- Vista di estrazione join scritture movimenti conti
--
-- History:
-- Date: 14/04/2004
-- Version: 1.0
-- Creazione
--
-- Date: 26/07/2005
-- Version:
-- Modifica: aggiunti 2 campi DT_CONTABILIZZAZIONE e DS_TIPO_DOCUMENTO per la  "Stampa Elenco Movimenti"
--
-- Body:
--
-- Date: 27/01/2006
-- Version:
-- Modifica: rielaborata la select perchè troppo lenta la consultazione Scheda Analitica Conto
--
-- Body:
--
    t.dt_contabilizzazione, t.cd_cds, t.esercizio,
    t.cd_unita_organizzativa, t.pg_scrittura, t.cd_terzo,
    t.origine_scrittura, t.cd_causale_coge, t.ti_scrittura,
    t.cd_tipo_documento,
    DECODE
    (t.origine_scrittura,
     'DOCAMM', decode(nvl(t.pg_numero_documento,0),0,null, cnrctb002.getdestipodocamm (t.cd_tipo_documento)),
     'DOCCONT', decode(nvl(t.pg_numero_documento,0),0,null, cnrctb002.getdestipodoccont (t.cd_tipo_documento)),
     NULL
    ) ds_tipo_documento,
    t.cd_cds_documento, t.cd_uo_documento, t.pg_numero_documento,
    t.esercizio_documento_amm, t.cd_comp_documento, t.im_scrittura,
    t.stato, t.attiva, d.cd_voce_ep,
    cnrctb002.getdesvoceep (d.esercizio, d.cd_voce_ep) ds_voce_ep,
    d2.CD_CENTRO_RESPONSABILITA CDR_GAE, d2.CD_LINEA_ATTIVITA CD_GAE, la.DENOMINAZIONE DS_LINEA_ATTIVITA,
    nvl(d2.sezione, d.sezione), nvl(d2.im_movimento, d.im_movimento),
    d.dt_da_competenza_coge,
    d.dt_a_competenza_coge, d.ti_istituz_commerc, d.fl_mov_terzo,
    t.ds_scrittura,
    CASE WHEN nvl(t.cd_tipo_documento,'X') IN ('GENERICO_S','GENERICO_E')
             THEN (SELECT dg.CD_CAUSALE_CONTABILE FROM DOCUMENTO_GENERICO dg
                   WHERE dg.ESERCIZIO = t.esercizio_documento_amm
                     AND dg.CD_CDS = t.cd_cds_documento
                     AND DG.CD_UNITA_ORGANIZZATIVA = t.cd_uo_documento
                     AND dg.CD_TIPO_DOCUMENTO_AMM = t.cd_tipo_documento
                     AND dg.PG_DOCUMENTO_GENERICO = t.pg_numero_documento)
         ELSE NULL
        END CD_CAUSALE_CONTABILE,
    CASE WHEN nvl(t.cd_tipo_documento,'X') IN ('GENERICO_S','GENERICO_E')
             THEN (SELECT cc.DS_CAUSALE FROM DOCUMENTO_GENERICO dg
                                                 LEFT JOIN CAUSALE_CONTABILE cc ON CC.CD_CAUSALE = DG.CD_CAUSALE_CONTABILE
                   WHERE dg.ESERCIZIO = t.esercizio_documento_amm
                     AND dg.CD_CDS = t.cd_cds_documento
                     AND DG.CD_UNITA_ORGANIZZATIVA = t.cd_uo_documento
                     AND dg.CD_TIPO_DOCUMENTO_AMM = t.cd_tipo_documento
                     AND dg.PG_DOCUMENTO_GENERICO = t.pg_numero_documento)
         ELSE NULL
        END ds_CAUSALE_CONTABILE,
    CASE WHEN nvl(t.cd_tipo_documento,'X') IN ('GENERICO_S','GENERICO_E')
             THEN (SELECT dg.DS_DOCUMENTO_GENERICO FROM DOCUMENTO_GENERICO dg
                   WHERE dg.ESERCIZIO = t.esercizio_documento_amm
                     AND dg.CD_CDS = t.cd_cds_documento
                     AND DG.CD_UNITA_ORGANIZZATIVA = t.cd_uo_documento
                     AND dg.CD_TIPO_DOCUMENTO_AMM = t.cd_tipo_documento
                     AND dg.PG_DOCUMENTO_GENERICO = t.pg_numero_documento)
         ELSE CASE WHEN nvl(t.cd_tipo_documento,'X') IN ('FATTURA_P')
                       THEN (SELECT fp.DS_FATTURA_PASSIVA FROM FATTURA_PASSIVA fp
                             WHERE fp.ESERCIZIO = t.esercizio_documento_amm
                               AND fp.CD_CDS = t.cd_cds_documento
                               AND fp.CD_UNITA_ORGANIZZATIVA = t.cd_uo_documento
                               AND fp.PG_FATTURA_PASSIVA = t.pg_numero_documento)
                   ELSE CASE WHEN nvl(t.cd_tipo_documento,'X') IN ('FATTURA_A')
                                 THEN (SELECT fp.DS_FATTURA_ATTIVA FROM FATTURA_ATTIVA fp
                                       WHERE fp.ESERCIZIO = t.esercizio_documento_amm
                                         AND fp.CD_CDS = t.cd_cds_documento
                                         AND fp.CD_UNITA_ORGANIZZATIVA = t.cd_uo_documento
                                         AND fp.PG_FATTURA_ATTIVA = t.pg_numero_documento)
                             ELSE CASE WHEN nvl(t.cd_tipo_documento,'X') IN ('ORDINE_CNS')
                                           THEN (SELECT oar.NOTA FROM ORDINE_ACQ oar
                                                 WHERE oar.ESERCIZIO = t.esercizio_documento_amm
                                                   AND oar.CD_CDS = t.cd_cds_documento
                                                   AND oar.CD_UNITA_OPERATIVA = t.CD_UNITA_OPERATIVA
                                                   AND oar.CD_NUMERATORE = t.CD_NUMERATORE_ORDINE
                                                   AND oar.NUMERO = t.pg_numero_documento)
                                       ELSE NULL
                                 END
                       END
             END
        END DESC_DOCUMENTO
FROM scrittura_partita_doppia t
LEFT JOIN movimento_coge d ON d.cd_cds = t.cd_cds AND d.esercizio = t.esercizio AND d.cd_unita_organizzativa = t.cd_unita_organizzativa
    AND d.pg_scrittura = t.pg_scrittura
LEFT OUTER JOIN movimento_coan d2 ON d2.CD_CDS_MOVCOGE = d.CD_CDS AND d2.ESERCIZIO_MOVCOGE = d.ESERCIZIO AND d2.CD_UO_MOVCOGE = d.CD_UNITA_ORGANIZZATIVA
    AND d2.PG_SCRITTURA_MOVCOGE = d.PG_SCRITTURA AND d2.PG_MOVIMENTO_MOVCOGE = d.PG_MOVIMENTO
LEFT OUTER JOIN LINEA_ATTIVITA la ON la.CD_CENTRO_RESPONSABILITA = d2.CD_CENTRO_RESPONSABILITA AND la.CD_LINEA_ATTIVITA = d2.CD_LINEA_ATTIVITA;
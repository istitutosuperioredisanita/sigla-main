CREATE OR REPLACE FORCE VIEW "V_DOCAMM_ELETTRONICI_ATTIVI" ("TIPO_DOCAMM", "CD_CDS", "CD_UNITA_ORGANIZZATIVA", "ESERCIZIO", "PG_DOCAMM", "CD_CDS_ORIGINE", "CD_UO_ORIGINE", "DT_REGISTRAZIONE", "DS_DOCAMM", "CD_TERZO", "COGNOME", "NOME", "RAGIONE_SOCIALE", "CODICE_FISCALE", "PARTITA_IVA", "PROTOCOLLO_IVA", "DT_EMISSIONE", "IM_TOTALE_DOCAMM", "CODICE_UNIVOCO_UFFICIO_IPA", "STATO_INVIO_SDI", "TI_FATTURA") AS
  SELECT 'FATTURA_A' TIPO_DOCAMM, FATTURA_ATTIVA.CD_CDS, FATTURA_ATTIVA.CD_UNITA_ORGANIZZATIVA, FATTURA_ATTIVA.ESERCIZIO, FATTURA_ATTIVA.PG_FATTURA_ATTIVA PG_DOCAMM,
	   FATTURA_ATTIVA.CD_CDS_ORIGINE, FATTURA_ATTIVA.CD_UO_ORIGINE,
       FATTURA_ATTIVA.DT_REGISTRAZIONE, FATTURA_ATTIVA.DS_FATTURA_ATTIVA DS_DOCAMM,
       FATTURA_ATTIVA.CD_TERZO, FATTURA_ATTIVA.COGNOME, FATTURA_ATTIVA.NOME, FATTURA_ATTIVA.RAGIONE_SOCIALE,
       FATTURA_ATTIVA.CODICE_FISCALE, FATTURA_ATTIVA.PARTITA_IVA, FATTURA_ATTIVA.PROTOCOLLO_IVA,
       FATTURA_ATTIVA.DT_EMISSIONE, FATTURA_ATTIVA.IM_TOTALE_FATTURA IM_TOTALE_DOCAMM,
       FATTURA_ATTIVA.CODICE_UNIVOCO_UFFICIO_IPA, FATTURA_ATTIVA.STATO_INVIO_SDI, FATTURA_ATTIVA.TI_FATTURA
FROM FATTURA_ATTIVA
WHERE FATTURA_ATTIVA.FL_FATTURA_ELETTRONICA = 'Y'
AND   FATTURA_ATTIVA.STATO_COFI <> 'A'
UNION ALL
SELECT 'AUTOFATT', af.CD_CDS, af.CD_UNITA_ORGANIZZATIVA, af.ESERCIZIO, af.PG_AUTOFATTURA,
	   af.CD_CDS_ORIGINE, af.CD_UO_ORIGINE,
       af.DT_REGISTRAZIONE, fp.DS_FATTURA_PASSIVA,
       fp.CD_TERZO, fp.COGNOME, fp.NOME, fp.RAGIONE_SOCIALE,
       fp.CODICE_FISCALE, fp.PARTITA_IVA, af.PROTOCOLLO_IVA,
       af.DT_REGISTRAZIONE DT_EMISSIONE, fp.IM_TOTALE_FATTURA,
       af.CODICE_UNIVOCO_UFFICIO_IPA, af.STATO_INVIO_SDI, af.TI_FATTURA
FROM AUTOFATTURA af
LEFT JOIN FATTURA_PASSIVA fp ON fp.CD_CDS = af.CD_CDS_FT_PASSIVA AND fp.CD_UNITA_ORGANIZZATIVA = af.CD_UO_FT_PASSIVA
AND fp.ESERCIZIO = af.ESERCIZIO AND fp.PG_FATTURA_PASSIVA = af.PG_FATTURA_PASSIVA
WHERE af.FL_FATTURA_ELETTRONICA = 'Y'
AND af.STATO_COFI <> 'A'
ORDER BY esercizio, PG_DOCAMM;
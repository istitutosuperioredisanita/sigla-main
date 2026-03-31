CREATE OR REPLACE FORCE VIEW "V_CONS_REG_IVA" (
  "ESERCIZIO", "TIPO", "DATA_REGISTRAZIONE", "DT_EMISSIONE", "DT_EMISS_AMM",
  "TI_BENE_SERVIZIO", "CD_TIPO_SEZIONALE", "CD_UNITA_ORGANIZZATIVA",
  "CD_CDS_ORIGINE", "CD_UO_ORIGINE", "PG_FATTURA", "PROT_IVA", "NRGREG_IVA",
  "PGR_U", "DS_FATTURA", "IMP", "IVA", "TOT", "CD_FORN", "DENO_FORN",
  "CF_PIVA", "NR_FATTURA", "UE", "TIPO_FATTURA", "PGR_D", "IMP_D", "IVA_D",
  "TOT_D", "CD_VOCE_IVA", "CD_GRUPPO_IVA", "DS_GRUPPO_IVA", "PERCENTUALE",
  "D", "PERC_D", "SP", "TIPO_DOCUMENTO", "FL_AUTOFATTURA", "FL_SPEDIZIONIERE",
  "FL_BOLLA_DOGANALE"
) AS
--==============================================================================
--
-- Date: 12/02/2025
-- Version: 1.0
--
-- Vista per l'estrazione dei dati delle fatture attive e passive ai fini IVA
--
-- History:
--
-- Date: 12/02/2025
-- Version: 1.0
-- Creazione vista
--
-- Body:
--
--==============================================================================
SELECT
  a.esercizio,
  DECODE(a.ti_istituz_commerc,'I','Istituzionale','C','Commerciale','') tipo,
  TO_CHAR(a.dt_registrazione,'yyyy-mm') DataRegistrazione,
  TO_CHAR(a.dt_registrazione,'yyyy-mm-dd') dt_emissione,
  TO_CHAR(a.dt_registrazione,'yyyy-mm') dt_emissAAmm,
  a.ti_bene_servizio,
  a.cd_tipo_sezionale,
  a.cd_unita_organizzativa,
  a.cd_cds_origine,
  a.cd_uo_origine,
  a.pg_fattura_passiva,
  a.protocollo_iva prot_iva,
  a.protocollo_iva||'-'||a.protocollo_iva_generale nrgreg_iva,
  a.pg_fattura_passiva PgrU,
  a.ds_fattura_passiva,
  DECODE(a.ti_fattura,'C', a.im_totale_imponibile*(-1),a.im_totale_imponibile) Imp,
  DECODE(a.ti_fattura,'C', a.im_totale_iva*(-1),a.im_totale_iva) Iva,
  DECODE(a.ti_fattura,'C', a.im_totale_fattura*(-1),a.im_totale_fattura) tot,
  a.cd_terzo CdForn,
  a.ragione_sociale DenoForn,
  NVL(a.codice_fiscale,'')||'/'||NVL(a.partita_iva,'') as "CF/Piva",
  a.nr_fattura_fornitore NrFattura,
  CASE WHEN a.fl_intra_ue='Y' THEN 'I'
       WHEN a.fl_extra_ue='Y' THEN 'E'
       ELSE 'N'
  END AS Ue,
  a.ti_fattura TipoFattura,
  r.progressivo_riga PgrD,
  DECODE(a.ti_fattura,'C', r.im_imponibile*(-1),r.im_imponibile) ImpD,
  DECODE(a.ti_fattura,'C', r.im_iva*(-1),r.im_iva) IvaD,
  DECODE(a.ti_fattura,'C', r.im_totale_divisa*(-1),r.im_totale_divisa) TotD,
  r.cd_voce_iva,
  g.cd_gruppo_iva,
  g.ds_gruppo_iva,
  v.percentuale "%",
  v.fl_detraibile D,
  v.percentuale_detraibilita "%D",
  a.fl_split_payment SP,
  'Passivo' tipoDocumento,
  a.fl_autofattura,
  a.fl_spedizioniere,
  a.fl_bolla_doganale
FROM fattura_passiva a
INNER JOIN fattura_passiva_riga r
  ON a.cd_cds=r.cd_cds
  AND a.cd_unita_organizzativa=r.cd_unita_organizzativa
  AND a.esercizio=r.esercizio
  AND a.pg_fattura_passiva=r.pg_fattura_passiva
INNER JOIN voce_iva v
  ON r.cd_voce_iva=v.cd_voce_iva
INNER JOIN gruppo_iva g
  ON g.cd_gruppo_iva=v.cd_gruppo_iva

UNION ALL

SELECT
  a.esercizio,
  'Commerciale' tipo,
  TO_CHAR(a.dt_registrazione,'yyyy-mm') DataRegistrazione,
  TO_CHAR(a.dt_emissione,'yyyy-mm-dd') dt_emissione,
  TO_CHAR(a.dt_emissione,'yyyy-mm') dt_emissAAmm,
  a.ti_bene_servizio,
  a.cd_tipo_sezionale,
  a.cd_unita_organizzativa,
  a.cd_cds_origine,
  a.cd_uo_origine,
  a.pg_fattura_attiva,
  NVL(a.protocollo_iva,'') prot_iva,
  NVL(a.protocollo_iva,'')||'-'||NVL(a.protocollo_iva_generale,'') nrgreg_iva,
  a.pg_fattura_attiva PgrU,
  a.ds_fattura_attiva,
  DECODE(a.ti_fattura,'C',a.im_totale_imponibile,a.im_totale_imponibile*(-1)) Imp,
  DECODE(a.ti_fattura,'C',a.im_totale_iva,a.im_totale_iva*(-1))Iva,
  DECODE(a.ti_fattura,'C',a.im_totale_fattura,a.im_totale_fattura*(-1)) tot,
  a.cd_terzo CdForn,
  a.ragione_sociale DenoForn,
  NVL(a.codice_fiscale,'')||'/'||NVL(a.partita_iva,'') as "CF/Piva",
  TO_CHAR(a.pg_fattura_attiva) NrFattura,
  CASE WHEN a.fl_intra_ue='Y' THEN 'I'
       WHEN a.fl_extra_ue='Y' THEN 'E'
       ELSE 'N'
  END AS Ue,
  a.ti_fattura TipoFattura,
  r.progressivo_riga PgrD,
  DECODE(a.ti_fattura,'C',r.im_imponibile,r.im_imponibile*(-1)) ImpD,
  DECODE(a.ti_fattura,'C',r.im_iva,r.im_iva*(-1)) IvaD,
  DECODE(a.ti_fattura,'C',r.im_totale_divisa,r.im_totale_divisa*(-1)) TotD,
  r.cd_voce_iva,
  g.cd_gruppo_iva,
  g.ds_gruppo_iva,
  v.percentuale "%",
  v.fl_detraibile D,
  v.percentuale_detraibilita "%D",
  a.fl_liquidazione_differita SP,
  'Attivo' tipoDocumento,
  NULL fl_autofattura,
  NULL fl_spedizioniere,
  NULL fl_bolla_doganale
FROM fattura_attiva a
INNER JOIN fattura_attiva_riga r
  ON a.cd_cds=r.cd_cds
  AND a.cd_unita_organizzativa=r.cd_unita_organizzativa
  AND a.esercizio=r.esercizio
  AND a.pg_fattura_attiva=r.pg_fattura_attiva
INNER JOIN voce_iva v
  ON r.cd_voce_iva=v.cd_voce_iva
INNER JOIN gruppo_iva g
  ON g.cd_gruppo_iva=v.cd_gruppo_iva;
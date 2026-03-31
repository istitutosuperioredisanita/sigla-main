--------------------------------------------------------
--  DDL for View V_CONS_SALDI_RESIDUI_ENT
--------------------------------------------------------

CREATE OR REPLACE FORCE VIEW "V_CONS_SALDI_RESIDUI_ENT" (
  "ESERCIZIO",
  "ESERCIZIO_RES",
  "CD_CENTRO_RESPONSABILITA",
  "CD_LINEA_ATTIVITA",
  "TI_APPARTENENZA",
  "TI_GESTIONE",
  "CD_ELEMENTO_VOCE",
  "DS_ELEMENTO_VOCE",
  "CDS_ACCERTAMENTO",
  "PG_ACCERTAMENTO",
  "DS_ACCERTAMENTO",
  "PG_ACCERTAMENTO_SCADENZARIO",
  "CD_TIPO_DOCUMENTO_CONT",
  "DS_SCADENZA",
  "IM_OBBL_RES_PRO",
  "PG_VAR_RES_PRO",
  "DS_VAR_RES_PRO",
  "VAR_PIU_ACC_RES_PRO",
  "VAR_MENO_ACC_RES_PRO",
  "CD_CDS_REV",
  "PG_REVERSALE",
  "DS_REVERSALE",
  "RISCOSSO_RES_PROPRIO"
) AS
-- ACCERTAMENTI RESIDUI PROPRI
SELECT
  o.esercizio,
  o.esercizio_originale esercizio_res,
  osv.cd_centro_responsabilita,
  osv.cd_linea_attivita,
  o.ti_appartenenza,
  o.ti_gestione,
  o.cd_elemento_voce,
  ev.ds_elemento_voce,
  osv.cd_cds cds_accertamento,
  osv.pg_accertamento,
  o.ds_accertamento,
  osv.pg_accertamento_scadenzario,
  o.cd_tipo_documento_cont,
  os.ds_scadenza,
  osv.im_voce im_obbl_res_pro,
  NULL pg_var_res_pro,
  NULL ds_var_res_pro,
  0 var_piu_acc_res_pro,
  0 var_meno_acc_res_pro,
  NULL cd_cds_rev,
  NULL pg_reversale,
  NULL ds_reversale,
  0 riscosso_res_proprio
FROM accertamento o
INNER JOIN accertamento_scad_voce osv
  ON osv.cd_cds = o.cd_cds
  AND osv.esercizio = o.esercizio
  AND osv.esercizio_originale = o.esercizio_originale
  AND osv.pg_accertamento = o.pg_accertamento
INNER JOIN accertamento_scadenzario os
  ON os.cd_cds = osv.cd_cds
  AND os.esercizio = osv.esercizio
  AND os.esercizio_originale = osv.esercizio_originale
  AND os.pg_accertamento = osv.pg_accertamento
  AND os.pg_accertamento_scadenzario = osv.pg_accertamento_scadenzario
INNER JOIN elemento_voce ev
  ON ev.esercizio = o.esercizio
  AND ev.cd_elemento_voce = o.cd_elemento_voce
WHERE o.cd_tipo_documento_cont IN ('ACR_RES', 'ACR_PGIR_R', 'ACR_PGIRO')
  AND o.pg_accertamento > 0
  AND osv.im_voce > 0

UNION ALL

-- VARIAZIONI IN + RESIDUI PROPRI
SELECT
  o.esercizio,
  o.esercizio_originale,
  omv.cd_centro_responsabilita,
  omv.cd_linea_attivita,
  o.ti_appartenenza,
  o.ti_gestione,
  o.cd_elemento_voce,
  ev.ds_elemento_voce,
  o.cd_cds,
  o.pg_accertamento,
  o.ds_accertamento,
  NULL,
  o.cd_tipo_documento_cont,
  o.ds_accertamento,
  0,
  om.pg_modifica,
  om.ds_modifica,
  omv.im_modifica,
  0,
  NULL,
  NULL,
  NULL,
  0
FROM accertamento o
INNER JOIN accertamento_modifica om
  ON om.cd_cds = o.cd_cds
  AND om.esercizio = o.esercizio
  AND om.pg_accertamento = o.pg_accertamento
  AND om.esercizio_originale = o.esercizio_originale
INNER JOIN accertamento_mod_voce omv
  ON omv.cd_cds = om.cd_cds
  AND omv.esercizio = om.esercizio
  AND omv.pg_modifica = om.pg_modifica
INNER JOIN elemento_voce ev
  ON ev.esercizio = o.esercizio
  AND ev.cd_elemento_voce = o.cd_elemento_voce
WHERE o.cd_tipo_documento_cont IN ('ACR_RES', 'ACR_PGIR_R', 'ACR_PGIRO')
  AND o.pg_accertamento > 0
  AND omv.im_modifica > 0
  AND om.pg_modifica > 0

UNION ALL

-- VARIAZIONI IN - RESIDUI PROPRI
SELECT
  o.esercizio,
  o.esercizio_originale,
  omv.cd_centro_responsabilita,
  omv.cd_linea_attivita,
  o.ti_appartenenza,
  o.ti_gestione,
  o.cd_elemento_voce,
  ev.ds_elemento_voce,
  o.cd_cds,
  o.pg_accertamento,
  o.ds_accertamento,
  NULL,
  o.cd_tipo_documento_cont,
  o.ds_accertamento,
  0,
  om.pg_modifica,
  om.ds_modifica,
  0,
  ABS(omv.im_modifica),
  NULL,
  NULL,
  NULL,
  0
FROM accertamento o
INNER JOIN accertamento_modifica om
  ON om.cd_cds = o.cd_cds
  AND om.esercizio = o.esercizio
  AND om.pg_accertamento = o.pg_accertamento
  AND om.esercizio_originale = o.esercizio_originale
INNER JOIN accertamento_mod_voce omv
  ON omv.cd_cds = om.cd_cds
  AND omv.esercizio = om.esercizio
  AND omv.pg_modifica = om.pg_modifica
INNER JOIN elemento_voce ev
  ON ev.esercizio = o.esercizio
  AND ev.cd_elemento_voce = o.cd_elemento_voce
WHERE o.cd_tipo_documento_cont IN ('ACR_RES', 'ACR_PGIR_R', 'ACR_PGIRO')
  AND o.pg_accertamento > 0
  AND omv.im_modifica < 0
  AND om.pg_modifica > 0

UNION ALL

-- REVERSALI RESIDUI PROPRI
SELECT
  o.esercizio,
  o.esercizio_originale,
  osv.cd_centro_responsabilita,
  osv.cd_linea_attivita,
  o.ti_appartenenza,
  o.ti_gestione,
  o.cd_elemento_voce,
  ev.ds_elemento_voce,
  osv.cd_cds,
  osv.pg_accertamento,
  o.ds_accertamento,
  osv.pg_accertamento_scadenzario,
  o.cd_tipo_documento_cont,
  os.ds_scadenza,
  0,
  NULL,
  NULL,
  0,
  0,
  m.cd_cds,
  m.pg_reversale,
  m.ds_reversale,
  DECODE(NVL(os.im_scadenza, 0),
         0, 0,
         (osv.im_voce / os.im_scadenza) * mr.im_reversale_riga)
FROM accertamento o
INNER JOIN accertamento_scad_voce osv
  ON osv.cd_cds = o.cd_cds
  AND osv.esercizio = o.esercizio
  AND osv.esercizio_originale = o.esercizio_originale
  AND osv.pg_accertamento = o.pg_accertamento
INNER JOIN accertamento_scadenzario os
  ON os.cd_cds = osv.cd_cds
  AND os.esercizio = osv.esercizio
  AND os.esercizio_originale = osv.esercizio_originale
  AND os.pg_accertamento = osv.pg_accertamento
  AND os.pg_accertamento_scadenzario = osv.pg_accertamento_scadenzario
INNER JOIN reversale_riga mr
  ON mr.cd_cds = os.cd_cds
  AND mr.esercizio_accertamento = os.esercizio
  AND mr.esercizio_ori_accertamento = os.esercizio_originale
  AND mr.pg_accertamento = os.pg_accertamento
  AND mr.pg_accertamento_scadenzario = os.pg_accertamento_scadenzario
INNER JOIN reversale m
  ON m.cd_cds = mr.cd_cds
  AND m.esercizio = mr.esercizio
  AND m.pg_reversale = mr.pg_reversale
INNER JOIN elemento_voce ev
  ON ev.esercizio = o.esercizio
  AND ev.cd_elemento_voce = o.cd_elemento_voce
WHERE o.cd_tipo_documento_cont IN ('ACR_RES', 'ACR_PGIR_R', 'ACR_PGIRO')
  AND o.pg_accertamento > 0
  AND m.stato != 'A';
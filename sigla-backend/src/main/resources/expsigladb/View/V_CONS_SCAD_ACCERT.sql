--------------------------------------------------------
--  DDL for View V_CONS_SCAD_ACCERT
--------------------------------------------------------


  CREATE OR REPLACE FORCE EDITIONABLE VIEW "V_CONS_SCAD_ACCERT" ("CDS", "UO", "ESERCIZIO", "ESERCIZIO_ORIGINALE", "PG_ACC", "PG_ACC_SCAD", "VOCE_BILANCIO", "DATA_SCAD", "ACC_DESC", "DS_SCAD", "IM_SCAD", "IM_ASS_DOC_AMM", "IMP_ASS_DOC_CONT", "DEBITORE", "CD_CDS_ACCERTAMENTO", "DS_ELEMENTO_VOCE") AS
  SELECT DISTINCT
--
-- Version: 1.0
--
-- Vista per la consultazione Scadenzario accertamenti
--
--
-- Body
--
                   accertamento.cd_cds_origine,
                   accertamento.cd_uo_origine,
                   accertamento_scadenzario.esercizio,
                   accertamento_scadenzario.esercizio_originale,
                   accertamento_scadenzario.pg_accertamento,
                   accertamento_scadenzario.pg_accertamento_scadenzario,
                   accertamento.cd_elemento_voce,
                   accertamento_scadenzario.dt_scadenza_incasso,
                   accertamento.ds_accertamento,
                   accertamento_scadenzario.ds_scadenza,
                   accertamento_scadenzario.im_scadenza,
                   accertamento_scadenzario.im_associato_doc_amm,
                   accertamento_scadenzario.im_associato_doc_contabile,
                   terzo.denominazione_sede,
                   accertamento.cd_cds cd_cds_accertamento,
                   elemento_voce.ds_elemento_voce
              FROM accertamento_scadenzario,
                   accertamento,
                   terzo
                   , elemento_voce
             WHERE accertamento_scadenzario.esercizio_originale =
                                              accertamento.esercizio_originale
               AND accertamento_scadenzario.pg_accertamento =
                                                  accertamento.pg_accertamento
               AND accertamento_scadenzario.cd_cds 		= accertamento.cd_cds
               AND accertamento_scadenzario.esercizio = accertamento.esercizio
               AND terzo.cd_terzo = accertamento.cd_terzo
               AND (   accertamento.cd_tipo_documento_cont = 'ACR'
                    OR accertamento.cd_tipo_documento_cont = 'ACR_RES'
                   )
                and elemento_voce.esercizio=accertamento.esercizio
            and elemento_voce.TI_APPARTENENZA=accertamento.TI_APPARTENENZA
            and elemento_voce.TI_GESTIONE=accertamento.TI_GESTIONE
            and elemento_voce.CD_ELEMENTO_VOCE=accertamento.CD_ELEMENTO_VOCE
          ORDER BY esercizio,
                   accertamento_scadenzario.dt_scadenza_incasso,
                   accertamento_scadenzario.esercizio_originale,
                   accertamento_scadenzario.pg_accertamento,
                   accertamento_scadenzario.pg_accertamento_scadenzario;


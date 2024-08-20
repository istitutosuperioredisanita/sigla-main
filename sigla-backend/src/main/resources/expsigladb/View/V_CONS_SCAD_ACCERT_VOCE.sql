--------------------------------------------------------
--  DDL for View V_CONS_SCAD_ACCERT_VOCE
--------------------------------------------------------

CREATE OR REPLACE FORCE EDITIONABLE VIEW "V_CONS_SCAD_ACCERT_VOCE" ("CDS", "UO", "ESERCIZIO", "ESERCIZIO_ORIGINALE", "PG_ACC", "PG_ACC_SCAD", "VOCE_BILANCIO", "DATA_SCAD", "ACC_DESC", "DS_SCAD", "IM_SCAD", "IM_ASS_DOC_AMM", "IMP_ASS_DOC_CONT", "DEBITORE", "CD_CDS_ACCERTAMENTO", "DS_ELEMENTO_VOCE", "IM_VOCE", "PG_PROGETTO", "CD_PROGETTO", "DS_PROGETTO", "DT_INIZIO", "DT_FINE", "DT_PROROGA", "CD_TIPO_PROGETTO", "DS_TIPO_PROGETTO") AS
  SELECT
  --
  -- Date: 12/08/2024
  -- Version: 1.0
  --
  -- Vista per la consultazione Scadenzario accertamenti
  --
  --
  -- Body
  --
     v.CDS,
     v.UO,
     v.ESERCIZIO,
     v.ESERCIZIO_ORIGINALE,
     v.PG_ACC,
     v.PG_ACC_SCAD,
     v.VOCE_BILANCIO,
     v.DATA_SCAD,
     v.ACC_DESC,
     v.DS_SCAD,
     v.IM_SCAD,
     v.IM_ASS_DOC_AMM,
     v.IMP_ASS_DOC_CONT,
     v.DEBITORE,
     v.CD_CDS_ACCERTAMENTO,
     v.DS_ELEMENTO_VOCE,
     av.IM_VOCE,
     p.pg_progetto,
     p.cd_progetto,
     p.ds_progetto,
     po.dt_inizio,
     po.dt_fine,
     po.dt_proroga,
     tp.cd_tipo_progetto,
     tp.ds_tipo_progetto from V_CONS_SCAD_ACCERT v
          left outer join accertamento_scad_voce av
            on v.cd_cds_accertamento= av.cd_cds
              AND v.esercizio = av.esercizio
              and v.esercizio_originale =av.esercizio_originale
                         AND v.PG_ACC =av.pg_accertamento
                         AND v.PG_ACC_SCAD = av.PG_ACCERTAMENTO_SCADENZARIO
          left outer join linea_attivita l
               on  av.cd_linea_attivita=l.cd_linea_attivita
               and  av.cd_centro_responsabilita=l.cd_centro_responsabilita
           left outer join ass_linea_attivita_esercizio ass
             on ass.cd_linea_attivita=l.cd_linea_attivita
             and ass.cd_centro_responsabilita=l.cd_centro_responsabilita
             and ass.esercizio_fine>=v.esercizio
           left outer join progetto p
               on p.esercizio=v.esercizio
               and p.pg_progetto=NVL (ass.pg_progetto, l.pg_progetto)
               and p.tipo_fase = 'X'
            left outer join progetto_other_field po
               on p.pg_progetto_other_field=po.pg_progetto
            left outer join tipo_progetto tp
               on p.cd_tipo_progetto=tp.cd_tipo_progetto
            ORDER BY v.esercizio,
                        v.data_scad,
                        v.esercizio_originale,
                        v.pg_acc,
                        v.pg_acc_scad,
                        p.pg_progetto;


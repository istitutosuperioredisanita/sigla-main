--------------------------------------------------------
--  DDL for View V_CONS_SCAD_OBBL
--------------------------------------------------------

  CREATE OR REPLACE FORCE VIEW "V_CONS_SCAD_OBBL_VOCE" ("CDS", "UO", "ESERCIZIO", "ESERCIZIO_ORIGINALE", "PG_OBBL", "PG_OBBL_SCAD", "VOCE_BILANCIO", "DATA_SCAD", "DS_OBBL", "DS_SCAD", "IM_SCAD", "IM_ASS_DOC_AMM", "IMP_ASS_DOC_CONT", "CREDITORE", "DS_ELEMENTO_VOCE", "IM_VOCE", "PG_PROGETTO", "CD_PROGETTO", "DS_PROGETTO", "DT_INIZIO", "DT_FINE", "DT_PROROGA", "CD_TIPO_PROGETTO", "DS_TIPO_PROGETTO") AS
  SELECT
    --
    -- Date: 12/08/2024
    -- Version: 1.0
    --
    -- Vista per la consultazione Scadenzario Impegni Voce
    --
    --
    -- Body
    --
    v.CDS,
    v.UO,
    v.ESERCIZIO,
    v.ESERCIZIO_ORIGINALE,
    v.PG_OBBL,
    v.PG_OBBL_SCAD,
    v.VOCE_BILANCIO,
    v.DATA_SCAD,
    v.DS_OBBL,
    v.DS_SCAD,
    v.IM_SCAD,
    v.IM_ASS_DOC_AMM,
    v.IMP_ASS_DOC_CONT,
    v.CREDITORE,
    v.DS_ELEMENTO_VOCE,
    ov.im_voce,
    p.pg_progetto,
    p.cd_progetto,
    p.ds_progetto,
    po.dt_inizio,
    po.dt_fine,
    po.dt_proroga,
    tp.cd_tipo_progetto,
    tp.ds_tipo_progetto
    from v_cons_scad_obbl v
     left outer join obbligazione_scad_voce ov
         on   v.cds= ov.cd_cds
             AND v.esercizio = ov.esercizio
             and v.esercizio_originale =ov.esercizio_originale
                        AND v.PG_OBBL =ov.pg_obbligazione
                        AND v.PG_obbl_scad = ov.PG_obbligazione_SCADENZARIO
         left outer join linea_attivita l
         on  ov.cd_linea_attivita=l.cd_linea_attivita
         and  ov.cd_centro_responsabilita=l.cd_centro_responsabilita
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
         order by v.esercizio,v.DATA_SCAD,v.ESERCIZIO_ORIGINALE,v.PG_OBBL,v.PG_OBBL_SCAD,p.pg_progetto;



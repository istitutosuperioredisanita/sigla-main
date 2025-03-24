--------------------------------------------------------
--  DDL for View V_INVENTARIO_BENE_DET
--------------------------------------------------------

  CREATE OR REPLACE FORCE EDITIONABLE VIEW "V_INVENTARIO_BENE_DET" ("TIPORECORD", "ESERCIZIO_CARICO_BENE", "PG_INVENTARIO", "NR_INVENTARIO", "PROGRESSIVO", "ETICHETTA", "FL_MIGRATO", "TI_AMMORTAMENTO", "CD_CATEGORIA_GRUPPO", "VALORE_INIZIALE", "VALORE_AMMORTIZZATO", "IMPONIBILE_AMMORTAMENTO", "VARIAZIONE_PIU", "VARIAZIONE_MENO", "ESERCIZIO_BUONO_CARICO", "INCREMENTO_VALORE", "DECREMENTO_VALORE", "PG_BUONO_C_S", "QUOTA_AMMO_BENE_ALIENATO", "ESERCIZIO_AMMORTANENTO", "QUOTA_AMMORTAMENTO", "STORNO", "PIANO_AMM_BENE_MIGRATO") AS
  SELECT
--
-- Date: 22/03/2024
-- Version: 1.0
--
-- Vista CONSULTAZIONE Inventario beni
--
-- History:

-- Body:
--
           'VALORE' tipoRecord,
                      a.esercizio_carico_bene,
                      a.pg_inventario,
                      a.nr_inventario,
                      a.progressivo,
                      a.etichetta,
                       a.fl_migrato,
                       a.TI_AMMORTAMENTO,
                      a.cd_categoria_gruppo,
                      a.valore_iniziale,
                      a.valore_ammortizzato,
                      a.imponibile_ammortamento,
                      NVL(a.variazione_piu,0) variazione_piu,
                      NVL(variazione_meno,0) variazione_meno,
                      null esercizio_buono_carico,
                      0 incremento_valore,
                      0 decremento_valore,
                      null PG_BUONO_C_S,
                      0 quota_ammo_bene_alienato,
                      null esercizio_ammortanento,
                      0 quota_ammortamento,
                      0 storno,
                      a.piano_amm_bene_migrato
              from inventario_beni a
                  union all
              select 'INCREMENTO' tipoRecord,
                      a.esercizio_carico_bene,
                      a.pg_inventario,
                      a.nr_inventario,
                      a.progressivo,
                      a.etichetta,
                       a.fl_migrato,
                       a.TI_AMMORTAMENTO,
                      a.cd_categoria_gruppo,
                      0 valore_iniziale ,
                      0 valore_ammortizzato,
                      0 imponibile_ammortamento,
                      0 variazione_piu,
                      0 variazione_meno,
                      b.esercizio esercizio_buono_carico,
                      d.valore_unitario incremento_valore,
                      0 decremento_valore,
                      d.PG_BUONO_C_S,
                      0 quota_ammo_bene_alienato,
                      null esercizio_ammortanento,
                       0 quota_ammortamento,
                        0 storno,
                        a.piano_amm_bene_migrato
              from inventario_beni a
                      left outer join buono_carico_scarico_dett d
                          on d.pg_inventario=a.pg_inventario
                          and d.nr_inventario=a.nr_inventario
                      left outer join buono_carico_scarico b
                          on b.PG_INVENTARIO=d.PG_INVENTARIO
                          and b.TI_DOCUMENTO =d.TI_DOCUMENTO
                          and b.ESERCIZIO=d.ESERCIZIO
                          and b.PG_BUONO_C_S=d.PG_BUONO_C_S
                      left outer join tipo_carico_scarico t
                          on t.CD_TIPO_CARICO_SCARICO=b.CD_TIPO_CARICO_SCARICO
                          where t.fl_aumento_valore='Y'
                  union all
              select 'DECREMENTO' tipoRecord,
                      a.esercizio_carico_bene,
                      a.pg_inventario,
                      a.nr_inventario,
                      a.progressivo,
                      a.etichetta,
                       a.fl_migrato,
                       a.TI_AMMORTAMENTO,
                      a.cd_categoria_gruppo,
                      0 valore_iniziale ,
                      0 valore_ammortizzato,
                      0 imponibile_ammortamento,
                      0 variazione_piu,
                      0 variazione_meno,
                      b.esercizio esercizio_buono_carico,
                      0 incremento_valore,
                      DECODE( t.cd_tipo_carico_scarico,'3',a.valore_iniziale, d.valore_unitario) decremento_valore,
                      d.PG_BUONO_C_S  PG_BUONO_C_S,
                      DECODE( t.cd_tipo_carico_scarico,'3',a.valore_ammortizzato, 0)  quota_ammo_bene_alienato,
                      null esercizio_ammortanento,
                      0 quota_ammortamento,
                       0 storno,
                      a.piano_amm_bene_migrato
              from inventario_beni a
                      left outer join buono_carico_scarico_dett d
                          on d.pg_inventario=a.pg_inventario
                          and d.nr_inventario=a.nr_inventario
                      left outer join buono_carico_scarico b
                          on b.PG_INVENTARIO=d.PG_INVENTARIO
                          and b.TI_DOCUMENTO =d.TI_DOCUMENTO
                          and b.ESERCIZIO=d.ESERCIZIO
                          and b.PG_BUONO_C_S=d.PG_BUONO_C_S
                      left outer join tipo_carico_scarico t
                          on t.CD_TIPO_CARICO_SCARICO=b.CD_TIPO_CARICO_SCARICO
                          where t.ti_documento='S'
                  union all
              select DECODE(i.fl_storno,'N','AMMORTAMENTO','STORNO') tipoRecord,
                      a.esercizio_carico_bene,
                      a.pg_inventario,
                      a.nr_inventario,
                      a.progressivo,
                      a.etichetta,
                       a.fl_migrato,
                       a.TI_AMMORTAMENTO,
                      a.cd_categoria_gruppo,
                      0 valore_iniziale ,
                      0 valore_ammortizzato,
                      0 imponibile_ammortamento,
                      0 variazione_piu,
                      0 variazione_meno,
                      null esercizio_buono_carico,
                      0 incremento_valore,
                      0 decremento_valore,
                      null PG_BUONO_C_S,
                      0 quota_ammo_bene_alienato,
                      i.esercizio esercizio_ammortanento,
                      DECODE(i.fl_storno,'N',i.im_movimento_ammort,0) quota_ammortamento,
                      DECODE(i.fl_storno,'Y',i.im_movimento_ammort,0) storno,
                       a.piano_amm_bene_migrato
              from inventario_beni a
                      inner join ammortamento_bene_inv i on
                      a.pg_inventario=i.pg_inventario
                      and a.nr_inventario=i.nr_inventario;

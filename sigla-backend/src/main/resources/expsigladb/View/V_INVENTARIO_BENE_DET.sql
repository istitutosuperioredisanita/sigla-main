--------------------------------------------------------
--  DDL for View V_INVENTARIO_BENE_DET
--------------------------------------------------------

  CREATE OR REPLACE FORCE EDITIONABLE VIEW "V_INVENTARIO_BENE_DET" ("TIPORECORD", "ESERCIZIO_CARICO_BENE", "PG_INVENTARIO", "NR_INVENTARIO", "ETICHETTA", "CD_CATEGORIA_GRUPPO", "VALORE_INIZIALE", "VALORE_AMMORTIZZATO", "VARIAZIONE_PIU", "VARIAZIONE_MENO", "ESERCIZIO_BUONO_CARICO", "INCREMENTO_VALORE", "DECREMENTO_VALORE", "PG_BUONO_C_S", "ESERCIZIO_AMMORTANENTO", "QUOTA_AMMORTAMENTO") AS
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
            a.etichetta,
            a.cd_categoria_gruppo,
            a.valore_iniziale,
            a.valore_ammortizzato,
            NVL(a.variazione_piu,0)variazione_piu,
            NVL(variazione_meno,0)variazione_meno,
            null esercizio_buono_carico,
            0 incremento_valore,0 decremento_valore, null PG_BUONO_C_S,null esercizio_ammortanento, 0 quota_ammortamento
    from inventario_beni a
        union all
    select 'INCREMENTO' tipoRecord,
            a.esercizio_carico_bene,
            a.pg_inventario,
            a.nr_inventario,
            a.etichetta,
            a.cd_categoria_gruppo,
            0 valore_iniziale ,
            0 valore_ammortizzato,
            0 variazione_piu,
            0 variazione_meno,
            b.esercizio esercizio_buono_carico,
            d.valore_unitario incremento_valore,
            0 decremento_valore,
            d.PG_BUONO_C_S,
            null esercizio_ammortanento,
             0 quota_ammortamento
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
            a.etichetta,
            a.cd_categoria_gruppo,
            0 valore_iniziale ,
            0 valore_ammortizzato,
            0 variazione_piu,
            0 variazione_meno,
            b.esercizio esercizio_buono_carico,
            0 incremento_valore,
            d.valore_unitario decremento_valore,
            d.PG_BUONO_C_S  PG_BUONO_C_S,
            null esercizio_ammortanento,
            0 quota_ammortamento
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
    select 'AMMORTAMENTO' tipoRecord,
            a.esercizio_carico_bene,
            a.pg_inventario,
            a.nr_inventario,
            a.etichetta,
            a.cd_categoria_gruppo,
            0 valore_iniziale ,
            0 valore_ammortizzato,
            0 variazione_piu,
            0 variazione_meno,
            null esercizio_buono_carico,
            0 incremento_valore,
            0 decremento_valore,
            null PG_BUONO_C_S,
            i.esercizio esercizio_ammortanento,
            i.im_movimento_ammort quota_ammortamento
    from inventario_beni a
            inner join ammortamento_bene_inv i on
            a.pg_inventario=i.pg_inventario
            and a.nr_inventario=i.nr_inventario;

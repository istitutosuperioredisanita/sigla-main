--------------------------------------------------------
--  DDL for View V_AMMORTAMENTO_BENI_DET
--------------------------------------------------------
  CREATE OR REPLACE FORCE EDITIONABLE VIEW "V_AMMORTAMENTO_BENI_DET" ("TIPORECORD", "ESERCIZIO_CARICO_BENE", "PG_INVENTARIO", "NR_INVENTARIO", "PROGRESSIVO", "ETICHETTA", "FL_AMMORTAMENTO", "FL_TOTALMENTE_SCARICATO", "TI_AMMORTAMENTO", "CD_CATEGORIA_GRUPPO", "VALORE_INIZIALE", "VALORE_AMMORTIZZATO", "IMPONIBILE_AMMORTAMENTO", "VARIAZIONE_PIU", "VARIAZIONE_MENO", "ESERCIZIO_BUONO_CARICO", "INCREMENTO_VALORE", "DECREMENTO_VALORE", "ESERCIZIO_AMMORTANENTO", "STORNO", "ESERCIZIO_COMPETENZA", "CD_TIPO_AMMORTAMENTO", "DS_TIPO_AMMORTAMENTO", "PERC_PRIMO_ANNO", "PERC_SUCCESSIVI", "NUMERO_ANNO_AMMORTAMENTO") AS
  SELECT
            d.tiporecord,
            d.esercizio_carico_bene,
            d.pg_inventario,
            d.nr_inventario,
            d.progressivo,
            d.etichetta,
            d.fl_ammortamento,
            d.fl_totalmente_scaricato,
            d.ti_ammortamento,
            d.cd_categoria_gruppo,
            d.valore_iniziale,
            d.valore_ammortizzato,
            d.imponibile_ammortamento,
            d.variazione_piu,
            d.variazione_meno,
            d.esercizio_buono_carico,
            d.incremento_valore incremento_valore,
            d.decremento_valore,
            d.esercizio_ammortanento,
            d.storno,
            ass.esercizio_competenza,
            tamm.cd_tipo_ammortamento,
            tamm.ds_tipo_ammortamento,
            tamm.perc_primo_anno,
            tamm.perc_successivi,
            d.numero_anno_ammortamento
        from v_inventario_bene_det d
        left outer join ASS_TIPO_AMM_CAT_GRUP_INV ass on  ass.cd_categoria_gruppo=d.cd_categoria_gruppo AND
                                                          ass.ti_ammortamento=d.ti_ammortamento
        left outer join TIPO_AMMORTAMENTO tamm on tamm.CD_TIPO_AMMORTAMENTO  = ass.CD_TIPO_AMMORTAMENTO AND
                                                  tamm.TI_AMMORTAMENTO = ass.TI_AMMORTAMENTO
        where ass.dt_cancellazione is null;

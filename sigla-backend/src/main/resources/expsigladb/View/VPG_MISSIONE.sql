--------------------------------------------------------
--  DDL for View VPG_MISSIONE
--------------------------------------------------------

CREATE OR REPLACE FORCE  VIEW "VPG_MISSIONE" ("ID", "CHIAVE", "SEQUENZA", "DESCRIZIONE", "CD_CDS", "CD_UNITA_ORGANIZZATIVA", "ESERCIZIO", "PG_MISSIONE", "TI_RECORD_L1", "TI_RECORD_L2", "FL_RIMBORSO", "TI_PROVVISORIO_DEFINITIVO", "ES_FINANZIARIO", "DS_UNITA_ORGANIZZATIVA", "FL_ASSOCIATO_COMPENSO", "NOME", "COGNOME", "CD_TERZO", "VIA_SEDE", "NUMERO_CIVICO_SEDE", "CAP_COMUNE_SEDE", "DS_COMUNE", "CD_PROVINCIA", "TI_ANAGRAFICO", "MATRICOLA", "QUALIFICA", "TI_COMPETENZA_RESIDUO", "ESERCIZIO_ORI_OBBL_ACC", "PG_OBBL_ACC", "PG_OBBL_ACC_SCADENZARIO", "DT_SCADENZA", "PG_COMPENSO", "PG_MAN_REV", "IM_TOTALE_MISSIONE", "IM_DIARIA_LORDA", "IM_QUOTA_ESENTE", "IM_DIARIA_NETTO", "IM_SPESE", "IM_ANTICIPO", "IM_RIMBORSO", "IM_CR_ENTE", "IM_LORDO_PERCEPIENTE", "IM_NETTO_PECEPIENTE", "DS_MISSIONE", "DT_INIZIO_MISSIONE", "DT_FINE_MISSIONE", "DS_MODALITA_PAG", "INTESTAZIONE", "NUMERO_CONTO", "CIN", "ABI", "CAB", "DS_ABICAB", "VIA_BANCA", "CAP_BANCA", "DS_COMUNE_BANCA", "CD_PROVINCIA_BANCA", "CD_VOCE", "CD_CONTRIBUTO_RITENUTA", "TI_ENTE_PERCIPIENTE", "AMMONTARE", "ALIQUOTA", "IMPONIBILE", "DS_CONTRIBUTO_RITENUTA", "PG_RIGA", "DT_INIZIO_TAPPA", "DS_SPESA", "FL_SPESA_ANTICIPATA", "CD_DIVISA_SPESA", "IM_SPESA_DIVISA", "CAMBIO_SPESA", "IM_BASE_MAGGIORAZIONE", "PERCENTUALE_MAGGIORAZIONE", "IM_MAGGIORAZIONE", "IM_SPESA_EURO", "IM_TOTALE_SPESA", "TI_AUTO", "CHILOMETRI", "INDENNITA_CHILOMETRICA", "IM_SPESE_ANTICIPATE", "DT_FINE_TAPPA", "CD_DIVISA_TAPPA", "CAMBIO_TAPPA", "IM_DIARIA", "IM_DIARIA_LORDA_DET", "IM_QUOTA_ESENTE_DET", "IM_DIARIA_NETTO_DET", "FL_DIARIA_MANUALE", "DETRAZIONE_ALTRI_NETTO", "DETRAZIONE_CONIUGE_NETTO", "DETRAZIONE_FIGLI_NETTO", "DETRAZIONI_LA_NETTO", "DETRAZIONI_PERSONALI_NETTO", "IBAN", "DT_REGISTRAZIONE", "TOT_QUOTA_RIMBORSO", "IM_TABELL_RIMB", "QUOTA_RIMBORSO", "IM_SPESE_NO_TRACC", "IM_SPESE_TRACC", "FL_SPESA_TRACCIATA") AS
  SELECT
    --
    -- Data: 18/07/2006
    -- Version: 1.4
    --
    -- Vista per la stampa massiva di nota e prospetto liquidazione missione
    -- Protocollo VPG
    --
    -- History:
    --
    -- Data: 03/02/2003
    -- Version: 1.0
    -- Creazione
    --
    -- Data: 03/03/2003
    -- Version: 1.1
    -- Estrazione MISSIONE_DIARIA.IM_DIARIA (segnalazione 503)
    --
    -- Data: 04/03/2003
    -- Version: 1.2
    -- Gestione varchar2 con size > 200
    --
    -- Data: 20/01/2004
    -- Version: 1.3
    -- Estrazione CIN dalla BANCA (richiesta n. 697)
    --
    -- Data: 18/07/2006
    -- Version: 1.4
    -- Gestione Impegni/Accertamenti Residui:
    -- aggiornata la funzione per tener conto anche del campo Esercizio Originale Impegno/Accertamento
    --
    -- Corpo della SELECT:
    --
    ID,
    CHIAVE,
    SEQUENZA,
    DESCRIZIONE,
    ATTRIBUTO_1,         -- CD_CDS
    ATTRIBUTO_2,        -- CD_UNITA_ORGANIZZATIVA
    IMPORTO_1,            -- ESERCIZIO
    IMPORTO_2,            -- PG_MISSIONE
    TIPO,                -- ti_record_l1
    ATTRIBUTO_3,        -- ti_record_l2
    ATTRIBUTO_4,        -- fl_rimborso
    ATTRIBUTO_5,        -- TI_PROVVISORIO_DEFINITIVO
    IMPORTO_3,            -- es_finanziario
    ATTRIBUTO_LONG_1,    -- ds_unita_organizzativa
    ATTRIBUTO_7,        -- FL_ASSOCIATO_COMPENSO
    ATTRIBUTO_8,        -- NOME
    ATTRIBUTO_9,        -- COGNOME
    IMPORTO_4,            -- CD_TERZO
    ATTRIBUTO_10,        -- VIA_SEDE
    ATTRIBUTO_11,         -- NUMERO_CIVICO_SEDE
    ATTRIBUTO_12,       -- CAP_COMUNE_SEDE
    ATTRIBUTO_13,       -- DS_COMUNE
    ATTRIBUTO_14,       -- CD_PROVINCIA
    ATTRIBUTO_15,       -- TI_ANAGRAFICO
    ATTRIBUTO_16,       -- matricola
    ATTRIBUTO_17,        -- qualifica
    ATTRIBUTO_18,        -- ti_compentenza_residuo
    IMPORTO_39,             -- ESERCIZIO_ORI_OBBL_ACC
    IMPORTO_5,            -- PG_OBBLIGAZIONE
    IMPORTO_6,            -- PG_OBBLIGAZIONE_SCADENZARIO
    DATA_1,                -- DT_SCADENZA
    IMPORTO_7,            -- PG_COMPENSO
    IMPORTO_8,            -- PG_MANDATO
    IMPORTO_9,          -- IM_TOTALE_MISSIONE
    IMPORTO_10,         -- IM_DIARIA_LORDA
    IMPORTO_11,         -- IM_QUOTA_ESENTE
    IMPORTO_12,         -- IM_DIARIA_NETTO
    IMPORTO_13,         -- IM_SPESE
    IMPORTO_14,         -- IM_ANTICIPO
    IMPORTO_15,         -- IM_RIMBORSO
    IMPORTO_16,         -- IM_CR_ENTE
    IMPORTO_17,         -- IM_LORDO_PERCEPIENTE
    IMPORTO_18,         -- IM_NETTO_PECEPIENTE
    ATTRIBUTO_LONG_2,    -- DS_MISSIONE
    DATA_2,                -- DT_INIZIO_MISSIONE
    DATA_3,             -- DT_FINE_MISSIONE
    ATTRIBUTO_20,        -- DS_MODALITA_PAG
    ATTRIBUTO_21,        -- INTESTAZIONE
    ATTRIBUTO_22,        -- NUMERO_CONTO
    ATTRIBUTO_40,        -- CIN,
    ATTRIBUTO_23,        -- ABI
    ATTRIBUTO_24,        -- CAB
    ATTRIBUTO_25,        -- DS_ABICAB
    ATTRIBUTO_26,        -- via_banca
    ATTRIBUTO_27,        -- cap_banca
    ATTRIBUTO_28,        -- ds_comune_banca
    ATTRIBUTO_29,        -- cd_prov_banca
    ATTRIBUTO_30,        -- CD_VOCE
    ATTRIBUTO_31,        -- CD_CONTRIBUTO_RITENUTA
    ATTRIBUTO_32,        -- TI_ENTE_PERCIPIENTE
    IMPORTO_19,            -- AMMONTARE
    IMPORTO_DEC8_1,     -- ALIQUOTA
    IMPORTO_20,            -- IMPONIBILE
    ATTRIBUTO_33,        -- DS_CONTRIBUTO_RITENUTA
    IMPORTO_21,         -- PG_RIGA
    DATA_4,                -- DT_INIZIO_TAPPA
    ATTRIBUTO_34,       -- DS_SPESA
    ATTRIBUTO_35,       -- FL_SPESA_ANTICIPATA
    ATTRIBUTO_36,        -- CD_DIVISA_SPESA
    IMPORTO_22,         -- IM_SPESA_DIVISA
    IMPORTO_DEC8_2,     -- CAMBIO_SPESA
    IMPORTO_23,         -- IM_BASE_MAGGIORAZIONE
    IMPORTO_DEC8_3,     -- PERCENTUALE_MAGGIORAZIONE
    IMPORTO_24,         -- IM_MAGGIORAZIONE
    IMPORTO_25,         -- IM_SPESA_EURO
    IMPORTO_26,         -- IM_TOTALE_SPESA
    ATTRIBUTO_37,        -- TI_AUTO
    IMPORTO_27,            -- CHILOMETRI
    IMPORTO_28,            -- INDENNITA_CHILOMETRICA
    IMPORTO_29,            -- IM_SPESE_ANTICIPATE
    DATA_5,                -- DT_FINE_TAPPA
    ATTRIBUTO_38,        -- CD_DIVISA_TAPPA
    IMPORTO_DEC8_4,     -- CAMBIO_TAPPA
    IMPORTO_38,            -- IM_DIARIA
    IMPORTO_30,            -- IM_DIARIA_LORDA
    IMPORTO_31,            -- IM_QUOTA_ESENTE
    IMPORTO_32,            -- IM_DIARIA_NETTO
    ATTRIBUTO_39,        -- FL_DIARIA_MANUALE
    IMPORTO_33,            -- DETRAZIONE_ALTRI_NETTO
    IMPORTO_34,            -- DETRAZIONE_CONIUGE_NETTO
    IMPORTO_35,            -- DETRAZIONE_FIGLI_NETTO
    IMPORTO_36,            -- DETRAZIONI_LA_NETTO
    IMPORTO_37,            -- DETRAZIONI_PERSONALI_NETTO
    ATTRIBUTO_41,        -- IBAN
    DATA_6,                -- DT_REGISTRAZIONE
    IMPORTO_42,            -- IM_RIMBORSO_SPESE --TOT_QUOTA_RIMBORSO
    IMPORTO_43,            -- IM_TABELL_RIMB
    IMPORTO_44,            -- IM_RIMBORSO_DET   --QUOTA_RIMBORSO
    IMPORTO_45,            -- IM_SPESE_NO_TRACC
    IMPORTO_46,            -- IM_SPESE_TRACC
    ATTRIBUTO_42           -- FL_SPESA_TRACCIATA
FROM tmp_report_generico trp;

COMMENT ON TABLE "VPG_MISSIONE" IS 'Vista per la stampa massiva di nota e prospetto liquidazione missione -- Protocollo VPG';
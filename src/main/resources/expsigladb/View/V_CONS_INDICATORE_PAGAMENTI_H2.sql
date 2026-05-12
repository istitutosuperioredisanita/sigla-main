--------------------------------------------------------
--  DDL for View V_CONS_INDICATORE_PAGAMENTI
--------------------------------------------------------
CREATE OR REPLACE VIEW "V_CONS_INDICATORE_PAGAMENTI" (
    "ESERCIZIO",
    "TRIMESTRE",
    "TIPO_RIGA",
    "CD_TERZO",
    "TIPO_DOCUMENTO",
    "ESERCIZIO_DOCUMENTO",
    "UO_DOCUMENTO",
    "NUMERO_DOCUMENTO",
    "IMPORTO_DOCUMENTO",
    "DATA_SCADENZA",
    "DATA_TRASMISSIONE",
    "DATA_RICEZIONE",
    "DATA_REGISTRAZIONE",
    "DATA_LIQUIDAZIONE",
    "DIFFERENZA_GIORNI",
    "IMPORTO_PAGATO",
    "IMPORTO_PESATO",
    "INDICE_PAGAMENTI",
    "CD_CDS_OBBLIGAZIONE",
    "ESERCIZIO_OBBLIGAZIONE",
    "ESERCIZIO_ORI_OBBLIGAZIONE",
    "PG_OBBLIGAZIONE",
    "PG_OBBLIGAZIONE_SCADENZARIO"
) AS (
    SELECT
        CAST(YEAR(data_scadenza) AS INTEGER) AS esercizio,
        CAST(CEILING((MONTH(data_scadenza) + 1) / 3.0) AS INTEGER) AS trimestre,
        'DETAIL' AS tipo_riga,
        cd_terzo,
        tipo_documento,
        esercizio_documento,
        uo_documento,
        numero_documento,
        importo_documento,
        data_scadenza,
        data_trasmissione,
        data_ricezione,
        data_registrazione,
        data_liquidazione,
        differenza_giorni,
        importo_pagato,
        importo_pesato,
        NULL AS indice_pagamenti,
        cd_cds_obbligazione,
        esercizio_obbligazione,
        esercizio_ori_obbligazione,
        pg_obbligazione,
        PG_OBBLIGAZIONE_SCADENZARIO
    FROM V_INDICATORE_PAGAMENTI_DETAIL

    UNION ALL

    SELECT
        CAST(YEAR(data_scadenza) AS INTEGER) AS esercizio,
        CAST(CEILING((MONTH(data_scadenza) + 1) / 3.0) AS INTEGER) AS trimestre,
        'SUM_TRIMESTRE' AS tipo_riga,
        NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        SUM(importo_pagato),
        SUM(importo_pesato),
        SUM(importo_pesato) / SUM(importo_pagato),
        NULL, NULL, NULL, NULL, NULL
    FROM V_INDICATORE_PAGAMENTI_DETAIL
    GROUP BY CAST(YEAR(data_scadenza) AS INTEGER), CAST(CEILING((MONTH(data_scadenza) + 1) / 3.0) AS INTEGER)

    UNION ALL

    SELECT
        CAST(YEAR(data_scadenza) AS INTEGER) AS esercizio,
        CAST(CEILING((MONTH(data_scadenza) + 1) / 3.0) AS INTEGER) AS trimestre,
        'SUM_TRIMESTRE_UO' AS tipo_riga,
        NULL, NULL, NULL, UO_DOCUMENTO, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        SUM(importo_pagato),
        SUM(importo_pesato),
        SUM(importo_pesato) / SUM(importo_pagato),
        NULL, NULL, NULL, NULL, NULL
    FROM V_INDICATORE_PAGAMENTI_DETAIL
    GROUP BY CAST(YEAR(data_scadenza) AS INTEGER), CAST(CEILING((MONTH(data_scadenza) + 1) / 3.0) AS INTEGER), UO_DOCUMENTO

    UNION ALL

    SELECT
        CAST(YEAR(data_scadenza) AS INTEGER) AS esercizio,
        NULL AS trimestre,
        'SUM_ANNO' AS tipo_riga,
        NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        SUM(importo_pagato),
        SUM(importo_pesato),
        SUM(importo_pesato) / SUM(importo_pagato),
        NULL, NULL, NULL, NULL, NULL
    FROM V_INDICATORE_PAGAMENTI_DETAIL
    GROUP BY CAST(YEAR(data_scadenza) AS INTEGER)

    UNION ALL

    SELECT
        CAST(YEAR(data_scadenza) AS INTEGER) AS esercizio,
        NULL AS trimestre,
        'SUM_ANNO_UO' AS tipo_riga,
        NULL, NULL, NULL, UO_DOCUMENTO, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL,
        SUM(importo_pagato),
        SUM(importo_pesato),
        SUM(importo_pesato) / SUM(importo_pagato),
        NULL, NULL, NULL, NULL, NULL
    FROM V_INDICATORE_PAGAMENTI_DETAIL
    GROUP BY CAST(YEAR(data_scadenza) AS INTEGER), UO_DOCUMENTO
)
/
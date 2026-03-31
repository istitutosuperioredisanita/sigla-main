--------------------------------------------------------
--  DDL for View V_ANAGRAFICO_TERZO
--------------------------------------------------------

   CREATE OR REPLACE FORCE VIEW "V_DOC_PASSIVO" (
    "CD_CDS",
    "CD_UNITA_ORGANIZZATIVA",
    "ESERCIZIO",
    "CD_TIPO_DOCUMENTO_AMM",
    "DSP_TIPO_DOCUMENTO_AMM",
    "PG_DOCUMENTO_AMM",
    "CD_NUMERATORE",
    "PG_VER_REC",
    "CD_CDS_ORIGINE",
    "CD_UO_ORIGINE",
    "TI_FATTURA",
    "STATO_COFI",
    "STATO_PAGAMENTO_FONDO_ECO",
    "DT_PAGAMENTO_FONDO_ECO",
    "CD_CDS_OBBLIGAZIONE",
    "ESERCIZIO_OBBLIGAZIONE",
    "ESERCIZIO_ORI_OBBLIGAZIONE",
    "PG_OBBLIGAZIONE",
    "PG_OBBLIGAZIONE_SCADENZARIO",
    "DT_FATTURA_FORNITORE",
    "NR_FATTURA_FORNITORE",
    "CD_TERZO",
    "CD_TERZO_CESSIONARIO",
    "COGNOME",
    "NOME",
    "RAGIONE_SOCIALE",
    "PG_BANCA",
    "CD_MODALITA_PAG",
    "IM_IMPONIBILE_DOC_AMM",
    "IM_IVA_DOC_AMM",
    "IM_TOTALE_DOC_AMM",
    "PG_LETTERA",
    "TI_ENTRATA_SPESA",
    "TI_SOSPESO_RISCONTRO",
    "CD_SOSPESO",
    "FL_DA_ORDINI",
    "FL_SELEZIONE",
    "FL_FAI_REVERSALE") AS
  SELECT  a.cd_cds, a.cd_unita_organizzativa, a.esercizio, 'FATTURA_P', 'FATTURA_P',
          a.pg_fattura_passiva, 'GEN' cd_numeratore, a.pg_ver_rec, a.cd_cds_origine,
          a.cd_uo_origine, a.ti_fattura, b.stato_cofi,
          a.stato_pagamento_fondo_eco, a.dt_pagamento_fondo_eco,
          b.cd_cds_obbligazione, b.esercizio_obbligazione,
          b.esercizio_ori_obbligazione, b.pg_obbligazione,
          b.pg_obbligazione_scadenzario, a.dt_fattura_fornitore,
          a.nr_fattura_fornitore, a.cd_terzo, b.cd_terzo_cessionario,
          a.cognome, a.nome, a.ragione_sociale,
          DECODE (b.cd_terzo_cessionario,
                  NULL, b.pg_banca,
                  d.pg_banca_delegato
                 ),
          DECODE (b.cd_terzo_cessionario,
                  NULL, b.cd_modalita_pag,
                  SUBSTR (getmodpagcessionario (b.cd_terzo_cessionario,
                                                d.ti_pagamento
                                               ),
                          1,
                          10
                         )
                 ),
          DECODE (a.ti_fattura, 'C', (b.im_imponibile * -1), b.im_imponibile),
          DECODE (a.ti_fattura, 'C', (b.im_iva * -1), b.im_iva),
          DECODE (a.ti_fattura,
                  'C', ((b.im_imponibile + b.im_iva) * -1),
                  (b.im_imponibile + b.im_iva
                  )
                 ),
          a.pg_lettera, c.ti_entrata_spesa, c.ti_sospeso_riscontro,
          c.cd_sospeso, nvl(a.fl_da_ordini,'N'),
          SUBSTR (getflselezione ('FATTURA_P',
                                  a.stato_pagamento_fondo_eco,
                                  a.ti_fattura,
                                  a.pg_lettera,
                                  c.cd_sospeso,
                                  NULL,
                                  0,
                                  0,
                                  0,
                                  nvl(b.fl_attesa_nota, a.fl_congelata),
                                  a.stato_liquidazione
                                 ),
                  1,
                  1
                 ),
          SUBSTR (getflfaireversale (a.ti_fattura,
                                     a.ti_istituz_commerc,
                                     a.ti_bene_servizio,
                                     a.fl_san_marino_senza_iva,
                                     DECODE (a.fl_merce_intra_ue,
                                             'Y', 'Y',
                                             a.fl_intra_ue
                                            ),
                                     a.fl_split_payment,
                                     DECODE (a.ti_bene_servizio,
                                             'B', t.ti_bene_servizio,
                                             t.fl_servizi_non_residenti
                                            )
                                    ),
                  1,
                  1
                 )
     FROM fattura_passiva a
     LEFT JOIN fattura_passiva_riga b ON b.cd_cds = a.cd_cds AND b.cd_unita_organizzativa = a.cd_unita_organizzativa
      					AND b.esercizio = a.esercizio AND b.pg_fattura_passiva = a.pg_fattura_passiva
     LEFT OUTER JOIN lettera_pagam_estero c ON c.cd_cds = a.cd_cds AND c.cd_unita_organizzativa = a.cd_unita_organizzativa
      					AND c.esercizio = a.esercizio_lettera AND c.pg_lettera = a.pg_lettera
	 LEFT JOIN banca d ON d.cd_terzo = b.cd_terzo AND d.pg_banca = b.pg_banca
	 LEFT JOIN tipo_sezionale t ON t.cd_tipo_sezionale = a.cd_tipo_sezionale
    WHERE  b.dt_cancellazione IS NULL
      AND NOT EXISTS (
      	SELECT 1 FROM fattura_ordine fo
      	WHERE  fo.cd_cds = b.cd_cds
        AND fo.cd_unita_organizzativa = b.cd_unita_organizzativa
        AND fo.esercizio = b.esercizio
        AND fo.pg_fattura_passiva = b.pg_fattura_passiva
        AND fo.progressivo_riga = b.progressivo_riga
      )
   UNION ALL
   SELECT a.cd_cds, a.cd_unita_organizzativa, a.esercizio, 'FATTURA_A', 'FATTURA_A',
          a.pg_fattura_attiva, 'GEN' cd_numeratore, a.pg_ver_rec, a.cd_cds_origine,
          a.cd_uo_origine, a.ti_fattura, b.stato_cofi, 'N', TO_DATE (NULL),
          b.cd_cds_obbligazione, b.esercizio_obbligazione,
          b.esercizio_ori_obbligazione, b.pg_obbligazione,
          b.pg_obbligazione_scadenzario, NULL, NULL, a.cd_terzo,
          NULL, a.cognome, a.nome, a.ragione_sociale, a.pg_banca,
          a.cd_modalita_pag, b.im_imponibile, b.im_iva,
          (b.im_imponibile + b.im_iva), NULL, NULL, NULL, NULL, 'N',
          DECODE (a.fl_congelata, 'Y', 'N', 'Y'), 'N'
     FROM fattura_attiva a, fattura_attiva_riga b
    WHERE a.ti_fattura = 'C'
      AND b.cd_cds = a.cd_cds
      AND b.cd_unita_organizzativa = a.cd_unita_organizzativa
      AND b.esercizio = a.esercizio
      AND b.pg_fattura_attiva = a.pg_fattura_attiva
      AND b.cd_cds_obbligazione IS NOT NULL
      AND b.esercizio_obbligazione IS NOT NULL
      AND b.esercizio_ori_obbligazione IS NOT NULL
      AND b.pg_obbligazione IS NOT NULL
      AND b.pg_obbligazione_scadenzario IS NOT NULL
      AND b.dt_cancellazione IS NULL
   UNION ALL
   SELECT a.cd_cds, a.cd_unita_organizzativa, a.esercizio,
          a.cd_tipo_documento_amm, a.cd_tipo_documento_amm, a.pg_documento_generico, 'GEN' cd_numeratore,
          b.pg_ver_rec,
          b.cd_cds_origine, b.cd_uo_origine, NULL, a.stato_cofi,
          b.stato_pagamento_fondo_eco, b.dt_pagamento_fondo_eco,
          a.cd_cds_obbligazione, a.esercizio_obbligazione,
          a.esercizio_ori_obbligazione, a.pg_obbligazione,
          a.pg_obbligazione_scadenzario, TO_DATE (NULL), NULL, a.cd_terzo,
          a.cd_terzo_cessionario, a.cognome, a.nome, a.ragione_sociale,
          DECODE (a.cd_terzo_cessionario,
                  NULL, a.pg_banca,
                  d.pg_banca_delegato
                 ),
          DECODE (a.cd_terzo_cessionario,
                  NULL, a.cd_modalita_pag,
                  SUBSTR (getmodpagcessionario (a.cd_terzo_cessionario,
                                                d.ti_pagamento
                                               ),
                          1,
                          10
                         )
                 ),
          0, 0, a.im_riga, b.pg_lettera, c.ti_entrata_spesa,
          c.ti_sospeso_riscontro, c.cd_sospeso, 'N',
          SUBSTR (getflselezione ('DOC_GENERICO',
                                  b.stato_pagamento_fondo_eco,
                                  NULL,
                                  b.pg_lettera,
                                  c.cd_sospeso,
                                  NULL,
                                  0,
                                  0,
                                  0,
                                  NULL,
                                  b.stato_liquidazione
                                 ),
                  1,
                  1
                 ),
          'N'
     FROM documento_generico_riga a
     LEFT JOIN documento_generico b ON b.cd_cds = a.cd_cds AND b.cd_unita_organizzativa = a.cd_unita_organizzativa
      			AND b.esercizio = a.esercizio AND b.cd_tipo_documento_amm = a.cd_tipo_documento_amm
		        AND b.pg_documento_generico = a.pg_documento_generico
     LEFT OUTER JOIN lettera_pagam_estero c ON c.cd_cds = b.cd_cds AND c.cd_unita_organizzativa = b.cd_unita_organizzativa
      			AND c.esercizio = b.esercizio_lettera AND c.pg_lettera = b.pg_lettera
     LEFT JOIN banca d ON d.cd_terzo = a.cd_terzo AND d.pg_banca = a.pg_banca
    WHERE a.cd_cds_obbligazione IS NOT NULL
      AND a.esercizio_obbligazione IS NOT NULL
      AND a.esercizio_ori_obbligazione IS NOT NULL
      AND a.pg_obbligazione IS NOT NULL
      AND a.pg_obbligazione_scadenzario IS NOT NULL
      AND a.dt_cancellazione IS NULL
   UNION ALL
   SELECT a.cd_cds, a.cd_unita_organizzativa, a.esercizio, 'ANTICIPO', 'ANTICIPO',
          a.pg_anticipo, 'GEN' cd_numeratore, a.pg_ver_rec,
          a.cd_cds_origine, a.cd_uo_origine,
          NULL, a.stato_cofi, a.stato_pagamento_fondo_eco,
          a.dt_pagamento_fondo_eco, a.cd_cds_obbligazione,
          a.esercizio_obbligazione, a.esercizio_ori_obbligazione,
          a.pg_obbligazione, a.pg_obbligazione_scadenzario, NULL,
          NULL, a.cd_terzo, NULL, a.cognome, a.nome,
          a.ragione_sociale, a.pg_banca, a.cd_modalita_pag, 0, 0,
          a.im_anticipo, NULL, NULL, NULL, NULL, 'N',
          SUBSTR (getflselezione ('ANTICIPO',
                                  a.stato_pagamento_fondo_eco,
                                  NULL,
                                  0,
                                  NULL,
                                  NULL,
                                  0,
                                  0,
                                  NVL (b.im_rimborso, 0),
                                  NULL,
                                  NULL
                                 ),
                  1,
                  1
                 ),
          'N'
     FROM anticipo a
     LEFT OUTER JOIN rimborso b ON b.cd_cds_anticipo = a.cd_cds AND b.cd_uo_anticipo = a.cd_unita_organizzativa
		      AND b.esercizio_anticipo = a.esercizio AND b.pg_anticipo = a.pg_anticipo
    WHERE a.cd_cds_obbligazione IS NOT NULL
      AND a.esercizio_obbligazione IS NOT NULL
      AND a.esercizio_ori_obbligazione IS NOT NULL
      AND a.pg_obbligazione IS NOT NULL
      AND a.pg_obbligazione_scadenzario IS NOT NULL
      AND a.dt_cancellazione IS NULL
   UNION ALL
   SELECT a.cd_cds, a.cd_unita_organizzativa, a.esercizio, 'COMPENSO', 'COMPENSO',
          a.pg_compenso, 'GEN' cd_numeratore, a.pg_ver_rec,
          a.cd_cds_origine, a.cd_uo_origine,
          NULL, a.stato_cofi, a.stato_pagamento_fondo_eco,
          a.dt_pagamento_fondo_eco, a.cd_cds_obbligazione,
          a.esercizio_obbligazione, a.esercizio_ori_obbligazione,
          a.pg_obbligazione, a.pg_obbligazione_scadenzario, NULL,
          NULL, a.cd_terzo, NULL, a.cognome, a.nome,
          a.ragione_sociale, a.pg_banca, a.cd_modalita_pag, 0, 0,
          a.im_totale_compenso, NULL, NULL, NULL, NULL,
          'N', 'N', 'N'
     FROM compenso a
    WHERE a.cd_cds_obbligazione IS NOT NULL
      AND a.esercizio_obbligazione IS NOT NULL
      AND a.esercizio_ori_obbligazione IS NOT NULL
      AND a.pg_obbligazione IS NOT NULL
      AND a.pg_obbligazione_scadenzario IS NOT NULL
      AND a.dt_cancellazione IS NULL
      AND a.pg_compenso > 0
   UNION ALL
   SELECT a.cd_cds, a.cd_unita_organizzativa, a.esercizio, 'MISSIONE', 'MISSIONE',
          a.pg_missione, 'GEN' cd_numeratore, a.pg_ver_rec,
          a.cd_cds, a.cd_unita_organizzativa,
          NULL, a.stato_cofi, a.stato_pagamento_fondo_eco,
          a.dt_pagamento_fondo_eco, a.cd_cds_obbligazione,
          a.esercizio_obbligazione, a.esercizio_ori_obbligazione,
          a.pg_obbligazione, a.pg_obbligazione_scadenzario, NULL,
          NULL, a.cd_terzo, NULL, a.cognome, a.nome,
          a.ragione_sociale, a.pg_banca, a.cd_modalita_pag, 0, 0,
          a.im_totale_missione - NVL (b.im_anticipo, 0), NULL,
          NULL, NULL, NULL, 'N',
          SUBSTR (getflselezione ('MISSIONE',
                                  a.stato_pagamento_fondo_eco,
                                  NULL,
                                  0,
                                  NULL,
                                  a.fl_associato_compenso,
                                  a.im_totale_missione,
                                  NVL (b.im_anticipo, 0),
                                  0,
                                  NULL,
                                  a.stato_liquidazione
                                 ),
                  1,
                  1
                 ),
          'N'
     FROM missione a
     LEFT OUTER JOIN anticipo b ON b.esercizio = a.esercizio_anticipo AND b.cd_cds = a.cd_cds_anticipo
      				AND b.cd_unita_organizzativa = a.cd_uo_anticipo AND b.pg_anticipo = a.pg_anticipo
    WHERE a.cd_cds_obbligazione IS NOT NULL
      AND a.esercizio_obbligazione IS NOT NULL
      AND a.esercizio_ori_obbligazione IS NOT NULL
      AND a.pg_obbligazione IS NOT NULL
      AND a.pg_obbligazione_scadenzario IS NOT NULL
      AND a.dt_cancellazione IS NULL
   UNION ALL
   SELECT a.cd_cds, c.cd_unita_organizzativa, a.esercizio, 'ORDINE', 'ORDINE',
          a.numero, a.cd_numeratore, a.pg_ver_rec,
          a.cd_cds, c.cd_unita_organizzativa,
          NULL, 'C' stato_cofi, 'N' stato_pagamento_fondo_eco,
          NULL dt_pagamento_fondo_eco, a.cd_cds_obbl,
          a.esercizio_obbl, a.esercizio_orig_obbl,
          a.pg_obbligazione, a.pg_obbligazione_scad, NULL,
          NULL, b.cd_terzo, NULL, b.cognome, b.nome,
          b.ragione_sociale, b.pg_banca, b.cd_modalita_pag, a.im_imponibile, a.im_iva,
          a.im_totale_consegna, NULL,
          NULL, NULL, NULL,
          'N', 'N', 'N'
     FROM ordine_acq_consegna a, ordine_acq b, unita_operativa_ord c
    WHERE a.cd_cds_obbl IS NOT NULL
      AND a.esercizio_obbl IS NOT NULL
      AND a.esercizio_orig_obbl IS NOT NULL
      AND a.pg_obbligazione IS NOT NULL
      AND a.pg_obbligazione_scad IS NOT NULL
      AND a.cd_cds = b.cd_cds
      AND a.cd_unita_operativa = b.cd_unita_operativa
      and a.esercizio = b.esercizio
      and a.cd_numeratore = b.cd_numeratore
      and a.numero = b.numero
      and a.cd_unita_operativa = c.cd_unita_operativa
   UNION ALL
     SELECT a.cd_cds, a.cd_unita_organizzativa, a.esercizio,'FATTURA_P',
          (
             select decode(count(1), 1, 'FAT_ORDINE', 'FATTURA_P')
                 from FATTURA_ORDINE
                 where CD_CDS = b.CD_CDS
                   and CD_UNITA_ORGANIZZATIVA = b.CD_UNITA_ORGANIZZATIVA
                   and ESERCIZIO = b.ESERCIZIO
                   and PG_FATTURA_PASSIVA = b.PG_FATTURA_PASSIVA
                   and PROGRESSIVO_RIGA = b.PROGRESSIVO_RIGA
          ),
          a.pg_fattura_passiva, 'GEN' cd_numeratore, a.pg_ver_rec, a.cd_cds_origine,
          a.cd_uo_origine, a.ti_fattura, b.stato_cofi,
          a.stato_pagamento_fondo_eco, a.dt_pagamento_fondo_eco,
          b.cd_cds_obbligazione, b.esercizio_obbligazione,
          b.esercizio_ori_obbligazione, b.pg_obbligazione,
          b.pg_obbligazione_scadenzario, a.dt_fattura_fornitore,
          a.nr_fattura_fornitore, a.cd_terzo, b.cd_terzo_cessionario,
          a.cognome, a.nome, a.ragione_sociale,
          DECODE (b.cd_terzo_cessionario,
                  NULL, b.pg_banca,
                  d.pg_banca_delegato
                 ),
          DECODE (b.cd_terzo_cessionario,
                  NULL, b.cd_modalita_pag,
                  SUBSTR (getmodpagcessionario (b.cd_terzo_cessionario,
                                                d.ti_pagamento
                                               ),
                          1,
                          10
                         )
                 ),
          DECODE (a.ti_fattura, 'C', (b.im_imponibile * -1), b.im_imponibile),
          DECODE (a.ti_fattura, 'C', (b.im_iva * -1), b.im_iva),
          DECODE (a.ti_fattura,
                  'C', ((b.im_imponibile + b.im_iva) * -1),
                  (b.im_imponibile + b.im_iva
                  )
                 ),
          a.pg_lettera, c.ti_entrata_spesa, c.ti_sospeso_riscontro,
          c.cd_sospeso, nvl(a.fl_da_ordini,'N'),
          SUBSTR (getflselezione ('FATTURA_P',
                                  a.stato_pagamento_fondo_eco,
                                  a.ti_fattura,
                                  a.pg_lettera,
                                  c.cd_sospeso,
                                  NULL,
                                  0,
                                  0,
                                  0,
                                  nvl(b.fl_attesa_nota, a.fl_congelata),
                                  a.stato_liquidazione
                                 ),
                  1,
                  1
                 ),
          SUBSTR (getflfaireversale (a.ti_fattura,
                                     a.ti_istituz_commerc,
                                     a.ti_bene_servizio,
                                     a.fl_san_marino_senza_iva,
                                     DECODE (a.fl_merce_intra_ue,
                                             'Y', 'Y',
                                             a.fl_intra_ue
                                            ),
                                     a.fl_split_payment,
                                     DECODE (a.ti_bene_servizio,
                                             'B', t.ti_bene_servizio,
                                             t.fl_servizi_non_residenti
                                            )
                                    ),
                  1,
                  1
                 )
     FROM fattura_passiva a
     LEFT JOIN fattura_passiva_riga b ON b.cd_cds = a.cd_cds AND b.cd_unita_organizzativa = a.cd_unita_organizzativa
      					AND b.esercizio = a.esercizio AND b.pg_fattura_passiva = a.pg_fattura_passiva
     LEFT OUTER JOIN lettera_pagam_estero c ON c.cd_cds = a.cd_cds AND c.cd_unita_organizzativa = a.cd_unita_organizzativa
      					AND c.esercizio = a.esercizio_lettera AND c.pg_lettera = a.pg_lettera
	 LEFT JOIN fattura_ordine fa ON fa.cd_cds = b.cd_cds AND fa.cd_unita_organizzativa = b.cd_unita_organizzativa
				        AND fa.esercizio = b.esercizio AND fa.pg_fattura_passiva = b.pg_fattura_passiva
					    AND fa.progressivo_riga = b.progressivo_riga
	 LEFT JOIN banca d ON d.cd_terzo = b.cd_terzo AND d.pg_banca = b.pg_banca
	 LEFT JOIN tipo_sezionale t ON t.cd_tipo_sezionale = a.cd_tipo_sezionale
    WHERE b.dt_cancellazione IS NULL;
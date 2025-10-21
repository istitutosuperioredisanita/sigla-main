--------------------------------------------------------
--  DDL for View V_CONTRATTI_TOTALI_DET
--------------------------------------------------------
CREATE OR REPLACE FORCE VIEW v_contratti_totali_det (tipo,
                                                     esercizio_originale,
                                                     terzo,
                                                     cd_elemento_voce,
                                                     esercizio_contratto,
                                                     pg_contratto,
                                                     stato_contratto,
                                                     oggetto,
                                                     data_inizio,
                                                     data_fine,
                                                     linea,
                                                     cdr,
                                                     esercizio_obb_acr,
                                                     pg_obbligazione_accertamento,
                                                     esercizio_man_rev,
                                                     pg_man_rev,
                                                     es_doc_amm,
                                                     pg_doc_amm,
                                                     tipo_doc,
                                                     esercizio_contratto_padre,
                                                     pg_contratto_padre,
                                                     stato_contratto_padre,
                                                     desc_voce,
                                                     desc_terzo,
                                                     desc_gae,
                                                     cds,
                                                     tipo_contratto,
                                                     totale_entrate_contratto,
                                                     totale_spese_contratto,
                                                     totale_entrate,
                                                     totale_spese,
                                                     liquidato_entrate,
                                                     liquidato_spese,
                                                     totale_reversali,
                                                     totale_mandati,
                                                     totale_ordini,
                                                     totale_mandati_netto,
                                                     liquidato_spese_netto
                                                    )
AS
   SELECT   A.TIPO, A.ESERCIZIO_ORIGINALE, A.CD_TERZO, A.CD_ELEMENTO_VOCE,
            A.ESERCIZIO_CONTRATTO, A.PG_CONTRATTO, A.STATO_CONTRATTO,
            A.OGGETTO, A.DT_INIZIO_VALIDITA, A.DT_FINE_VALIDITA,
            A.CD_LINEA_ATTIVITA, A.CD_CENTRO_RESPONSABILITA,
            ESERCIZIO_OBB_ACR, PG_OBBLIGAZIONE_ACCERTAMENTO,
            ESERCIZIO_MAN_REV, PG_MAN_REV, ES_DOC_AMM, PG_DOC_AMM, TIPO_DOC,
            A.ESERCIZIO_CONTRATTO_PADRE, A.PG_CONTRATTO_PADRE,
            A.STATO_CONTRATTO_PADRE, DESC_VOCE, DESC_TERZO, DESC_GAE, A.CDS,
            A.CD_TIPO_CONTRATTO, NVL (A.IM_CONTRATTO_ATTIVO, 0),
            NVL (A.IM_CONTRATTO_PASSIVO, 0), SUM (A.TOTALE_ENTRATE),
            SUM (A.TOTALE_SPESE), ROUND (SUM (A.LIQUIDATO_ENTRATE), 2),
            ROUND (SUM (A.LIQUIDATO_SPESE), 2), SUM (A.TOTALE_REVERSALE),
            SUM (A.TOTALE_MANDATI), SUM (A.TOTALE_ORDINI),
            SUM (A.TOTALE_MANDATI_NETTO), SUM (A.LIQUIDATO_SPESE_NETTO)
       FROM (

             SELECT   'ETR' TIPO, ACCERTAMENTO.ESERCIZIO_ORIGINALE,
                      ACCERTAMENTO.CD_TERZO, ACCERTAMENTO.CD_ELEMENTO_VOCE,
                      ACCERTAMENTO.ESERCIZIO_CONTRATTO,
                      ACCERTAMENTO.PG_CONTRATTO, ACCERTAMENTO.STATO_CONTRATTO,
                      CONTRATTO.OGGETTO, CONTRATTO.DT_INIZIO_VALIDITA,
                      CONTRATTO.DT_FINE_VALIDITA,
                      ACCERTAMENTO_SCAD_VOCE.CD_LINEA_ATTIVITA,
                      ACCERTAMENTO_SCAD_VOCE.CD_CENTRO_RESPONSABILITA,
                      ACCERTAMENTO.ESERCIZIO ESERCIZIO_OBB_ACR,
                      ACCERTAMENTO.PG_ACCERTAMENTO
                                                 PG_OBBLIGAZIONE_ACCERTAMENTO,
                      NULL ESERCIZIO_MAN_REV, NULL PG_MAN_REV,
                      NULL ES_DOC_AMM, NULL PG_DOC_AMM, NULL TIPO_DOC,
                      CONTRATTO_PADRE.ESERCIZIO ESERCIZIO_CONTRATTO_PADRE,
                      CONTRATTO_PADRE.PG_CONTRATTO PG_CONTRATTO_PADRE,
                      CONTRATTO_PADRE.STATO STATO_CONTRATTO_PADRE,
                      ELEMENTO_VOCE.DS_ELEMENTO_VOCE DESC_VOCE,
                      TERZO.DENOMINAZIONE_SEDE DESC_TERZO,
                      LINEA_ATTIVITA.DS_LINEA_ATTIVITA DESC_GAE,
                      ACCERTAMENTO.CD_CDS_ORIGINE CDS,
                      CONTRATTO.CD_TIPO_CONTRATTO,
                      CONTRATTO.IM_CONTRATTO_ATTIVO,
                      CONTRATTO.IM_CONTRATTO_PASSIVO,
                      SUM (ACCERTAMENTO_SCAD_VOCE.IM_VOCE) TOTALE_ENTRATE,
                      0 TOTALE_SPESE, 0 LIQUIDATO_ENTRATE, 0 LIQUIDATO_SPESE,
                      0 TOTALE_REVERSALE, 0 TOTALE_MANDATI, 0 TOTALE_ORDINI,
                      0 TOTALE_MANDATI_NETTO, 0 LIQUIDATO_SPESE_NETTO
                 FROM CONTRATTO,
                      ACCERTAMENTO,
                      ACCERTAMENTO_SCADENZARIO,
                      ACCERTAMENTO_SCAD_VOCE,
                      TERZO,
                      ELEMENTO_VOCE,
                      LINEA_ATTIVITA
                LEFT OUTER JOIN CONTRATTO CONTRATTO_PADRE ON CONTRATTO_PADRE.ESERCIZIO = CONTRATTO.ESERCIZIO_PADRE AND CONTRATTO_PADRE.PG_CONTRATTO = CONTRATTO.PG_CONTRATTO_PADRE
                WHERE CONTRATTO.ESERCIZIO = ACCERTAMENTO.ESERCIZIO_CONTRATTO
                  AND CONTRATTO.STATO = ACCERTAMENTO.STATO_CONTRATTO
                  AND CONTRATTO.PG_CONTRATTO = ACCERTAMENTO.PG_CONTRATTO
                  AND ACCERTAMENTO.CD_CDS = ACCERTAMENTO_SCADENZARIO.CD_CDS
                  AND ACCERTAMENTO.ESERCIZIO =
                                            ACCERTAMENTO_SCADENZARIO.ESERCIZIO
                  AND (   ACCERTAMENTO.ESERCIZIO_ORIGINALE =
                                                        ACCERTAMENTO.ESERCIZIO
                       OR (    ACCERTAMENTO.CD_TIPO_DOCUMENTO_CONT = 'ACR_RES'
                           AND NOT EXISTS (
                                  SELECT 1
                                    FROM ACCERTAMENTO ACC
                                   WHERE ACCERTAMENTO.CD_CDS = ACC.CD_CDS
                                     AND ACCERTAMENTO.ESERCIZIO >
                                                                 ACC.ESERCIZIO
                                     AND ACCERTAMENTO.ESERCIZIO_ORIGINALE =
                                                       ACC.ESERCIZIO_ORIGINALE
                                     AND ACCERTAMENTO.PG_ACCERTAMENTO =
                                                           ACC.PG_ACCERTAMENTO
                                     AND ACCERTAMENTO.ESERCIZIO_CONTRATTO =
                                                       ACC.ESERCIZIO_CONTRATTO
                                     AND ACCERTAMENTO.STATO_CONTRATTO =
                                                           ACC.STATO_CONTRATTO
                                     AND ACCERTAMENTO.PG_CONTRATTO =
                                                              ACC.PG_CONTRATTO)
                          )
                      )
                  AND ACCERTAMENTO.ESERCIZIO_ORIGINALE =
                                  ACCERTAMENTO_SCADENZARIO.ESERCIZIO_ORIGINALE
                  AND ACCERTAMENTO.PG_ACCERTAMENTO =
                                      ACCERTAMENTO_SCADENZARIO.PG_ACCERTAMENTO
                  AND ACCERTAMENTO_SCADENZARIO.CD_CDS =
                                                 ACCERTAMENTO_SCAD_VOCE.CD_CDS
                  AND ACCERTAMENTO_SCADENZARIO.ESERCIZIO =
                                              ACCERTAMENTO_SCAD_VOCE.ESERCIZIO
                  AND ACCERTAMENTO_SCADENZARIO.ESERCIZIO_ORIGINALE =
                                    ACCERTAMENTO_SCAD_VOCE.ESERCIZIO_ORIGINALE
                  AND ACCERTAMENTO_SCADENZARIO.PG_ACCERTAMENTO =
                                        ACCERTAMENTO_SCAD_VOCE.PG_ACCERTAMENTO
                  AND ACCERTAMENTO_SCADENZARIO.PG_ACCERTAMENTO_SCADENZARIO =
                            ACCERTAMENTO_SCAD_VOCE.PG_ACCERTAMENTO_SCADENZARIO
                  AND ELEMENTO_VOCE.ESERCIZIO = ACCERTAMENTO.ESERCIZIO
                  AND ELEMENTO_VOCE.TI_APPARTENENZA =
                                                  ACCERTAMENTO.TI_APPARTENENZA
                  AND ELEMENTO_VOCE.TI_GESTIONE = ACCERTAMENTO.TI_GESTIONE
                  AND ELEMENTO_VOCE.CD_ELEMENTO_VOCE =
                                                 ACCERTAMENTO.CD_ELEMENTO_VOCE
                  AND TERZO.CD_TERZO = ACCERTAMENTO.CD_TERZO
                  AND LINEA_ATTIVITA.CD_CENTRO_RESPONSABILITA =
                               ACCERTAMENTO_SCAD_VOCE.CD_CENTRO_RESPONSABILITA
                  AND LINEA_ATTIVITA.CD_LINEA_ATTIVITA =
                                      ACCERTAMENTO_SCAD_VOCE.CD_LINEA_ATTIVITA
             GROUP BY 'ETR',
                      ACCERTAMENTO.ESERCIZIO_ORIGINALE,
                      ACCERTAMENTO.CD_TERZO,
                      ACCERTAMENTO.CD_ELEMENTO_VOCE,
                      ACCERTAMENTO.ESERCIZIO_CONTRATTO,
                      ACCERTAMENTO.PG_CONTRATTO,
                      ACCERTAMENTO.STATO_CONTRATTO,
                      CONTRATTO.OGGETTO,
                      CONTRATTO.DT_INIZIO_VALIDITA,
                      CONTRATTO.DT_FINE_VALIDITA,
                      ACCERTAMENTO_SCAD_VOCE.CD_LINEA_ATTIVITA,
                      ACCERTAMENTO_SCAD_VOCE.CD_CENTRO_RESPONSABILITA,
                      ACCERTAMENTO.ESERCIZIO,
                      ACCERTAMENTO.PG_ACCERTAMENTO,
                      NULL,
                      NULL,
                      NULL,
                      NULL,
                      NULL,
                      CONTRATTO_PADRE.ESERCIZIO,
                      CONTRATTO_PADRE.PG_CONTRATTO,
                      CONTRATTO_PADRE.STATO,
                      ELEMENTO_VOCE.DS_ELEMENTO_VOCE,
                      TERZO.DENOMINAZIONE_SEDE,
                      LINEA_ATTIVITA.DS_LINEA_ATTIVITA,
                      ACCERTAMENTO.CD_CDS_ORIGINE,
                      CONTRATTO.CD_TIPO_CONTRATTO,
                      CONTRATTO.IM_CONTRATTO_ATTIVO,
                      CONTRATTO.IM_CONTRATTO_PASSIVO
             UNION

             SELECT   'ETR' TIPO, ACCERTAMENTO.ESERCIZIO_ORIGINALE,
                      ACCERTAMENTO.CD_TERZO, ACCERTAMENTO.CD_ELEMENTO_VOCE,
                      ACCERTAMENTO.ESERCIZIO_CONTRATTO,
                      ACCERTAMENTO.PG_CONTRATTO, ACCERTAMENTO.STATO_CONTRATTO,
                      CONTRATTO.OGGETTO, CONTRATTO.DT_INIZIO_VALIDITA,
                      CONTRATTO.DT_FINE_VALIDITA,
                      ACCERTAMENTO_MOD_VOCE.CD_LINEA_ATTIVITA,
                      ACCERTAMENTO_MOD_VOCE.CD_CENTRO_RESPONSABILITA,
                      ACCERTAMENTO.ESERCIZIO ESERCIZIO_OBB_ACR,
                      ACCERTAMENTO.PG_ACCERTAMENTO
                                                 PG_OBBLIGAZIONE_ACCERTAMENTO,
                      NULL ESERCIZIO_MAN_REV, NULL PG_MAN_REV,
                      NULL ES_DOC_AMM, NULL PG_DOC_AMM, NULL TIPO_DOC,
                      CONTRATTO_PADRE.ESERCIZIO ESERCIZIO_CONTRATTO_PADRE,
                      CONTRATTO_PADRE.PG_CONTRATTO PG_CONTRATTO_PADRE,
                      CONTRATTO_PADRE.STATO STATO_CONTRATTO_PADRE,
                      ELEMENTO_VOCE.DS_ELEMENTO_VOCE DESC_VOCE,
                      TERZO.DENOMINAZIONE_SEDE DESC_TERZO,
                      LINEA_ATTIVITA.DS_LINEA_ATTIVITA DESC_GAE,
                      ACCERTAMENTO.CD_CDS_ORIGINE CDS,
                      CONTRATTO.CD_TIPO_CONTRATTO,
                      CONTRATTO.IM_CONTRATTO_ATTIVO,
                      CONTRATTO.IM_CONTRATTO_PASSIVO,
                      SUM (ACCERTAMENTO_MOD_VOCE.IM_MODIFICA) TOTALE_ENTRATE,
                      0 TOTALE_SPESE, 0 LIQUIDATO_ENTRATE, 0 LIQUIDATO_SPESE,
                      0 TOTALE_REVERSALE, 0 TOTALE_MANDATI, 0 TOTALE_ORDINI,
                      0 TOTALE_MANDATI_NETTO, 0 LIQUIDATO_SPESE_NETTO
                 FROM CONTRATTO,
                      ACCERTAMENTO,
                      ACCERTAMENTO_MODIFICA,
                      ACCERTAMENTO_MOD_VOCE,
                      TERZO,
                      ELEMENTO_VOCE,
                      LINEA_ATTIVITA
                LEFT OUTER JOIN CONTRATTO CONTRATTO_PADRE ON CONTRATTO_PADRE.ESERCIZIO = CONTRATTO.ESERCIZIO_PADRE AND CONTRATTO_PADRE.PG_CONTRATTO = CONTRATTO.PG_CONTRATTO_PADRE
                WHERE CONTRATTO.ESERCIZIO = ACCERTAMENTO.ESERCIZIO_CONTRATTO
                  AND CONTRATTO.STATO = ACCERTAMENTO.STATO_CONTRATTO
                  AND CONTRATTO.PG_CONTRATTO = ACCERTAMENTO.PG_CONTRATTO
                  AND ACCERTAMENTO.CD_CDS = ACCERTAMENTO_MODIFICA.CD_CDS
                  AND ACCERTAMENTO.ESERCIZIO = ACCERTAMENTO_MODIFICA.ESERCIZIO
                  AND ACCERTAMENTO.ESERCIZIO_ORIGINALE =
                                     ACCERTAMENTO_MODIFICA.ESERCIZIO_ORIGINALE
                  AND ACCERTAMENTO.PG_ACCERTAMENTO =
                                         ACCERTAMENTO_MODIFICA.PG_ACCERTAMENTO
                  AND ACCERTAMENTO_MODIFICA.CD_CDS =
                                                  ACCERTAMENTO_MOD_VOCE.CD_CDS
                  AND ACCERTAMENTO_MODIFICA.ESERCIZIO =
                                               ACCERTAMENTO_MOD_VOCE.ESERCIZIO
                  AND ACCERTAMENTO_MODIFICA.PG_MODIFICA =
                                             ACCERTAMENTO_MOD_VOCE.PG_MODIFICA
                  AND ELEMENTO_VOCE.ESERCIZIO = ACCERTAMENTO.ESERCIZIO
                  AND ELEMENTO_VOCE.TI_APPARTENENZA =
                                                  ACCERTAMENTO.TI_APPARTENENZA
                  AND ELEMENTO_VOCE.TI_GESTIONE = ACCERTAMENTO.TI_GESTIONE
                  AND ELEMENTO_VOCE.CD_ELEMENTO_VOCE =
                                                 ACCERTAMENTO.CD_ELEMENTO_VOCE
                  AND TERZO.CD_TERZO = ACCERTAMENTO.CD_TERZO
                  AND LINEA_ATTIVITA.CD_CENTRO_RESPONSABILITA =
                                ACCERTAMENTO_MOD_VOCE.CD_CENTRO_RESPONSABILITA
                  AND LINEA_ATTIVITA.CD_LINEA_ATTIVITA =
                                       ACCERTAMENTO_MOD_VOCE.CD_LINEA_ATTIVITA
                  AND EXISTS (
                         SELECT 1
                           FROM ACCERTAMENTO ACC
                          WHERE ACCERTAMENTO.CD_CDS = ACC.CD_CDS

                            AND ACCERTAMENTO.ESERCIZIO_ORIGINALE =
                                                       ACC.ESERCIZIO_ORIGINALE
                            AND ACCERTAMENTO.PG_ACCERTAMENTO =
                                                           ACC.PG_ACCERTAMENTO
                            AND ACCERTAMENTO.ESERCIZIO_CONTRATTO =
                                                       ACC.ESERCIZIO_CONTRATTO
                            AND ACCERTAMENTO.STATO_CONTRATTO =
                                                           ACC.STATO_CONTRATTO
                            AND ACCERTAMENTO.PG_CONTRATTO = ACC.PG_CONTRATTO
                            AND ACC.ESERCIZIO = ACC.ESERCIZIO_ORIGINALE)
             GROUP BY 'ETR',
                      ACCERTAMENTO.ESERCIZIO_ORIGINALE,
                      ACCERTAMENTO.CD_TERZO,
                      ACCERTAMENTO.CD_ELEMENTO_VOCE,
                      ACCERTAMENTO.ESERCIZIO_CONTRATTO,
                      ACCERTAMENTO.PG_CONTRATTO,
                      ACCERTAMENTO.STATO_CONTRATTO,
                      CONTRATTO.OGGETTO,
                      CONTRATTO.DT_INIZIO_VALIDITA,
                      CONTRATTO.DT_FINE_VALIDITA,
                      ACCERTAMENTO_MOD_VOCE.CD_LINEA_ATTIVITA,
                      ACCERTAMENTO_MOD_VOCE.CD_CENTRO_RESPONSABILITA,
                      ACCERTAMENTO.ESERCIZIO,
                      ACCERTAMENTO.PG_ACCERTAMENTO,
                      NULL,
                      NULL,
                      NULL,
                      NULL,
                      NULL,
                      CONTRATTO_PADRE.ESERCIZIO,
                      CONTRATTO_PADRE.PG_CONTRATTO,
                      CONTRATTO_PADRE.STATO,
                      ELEMENTO_VOCE.DS_ELEMENTO_VOCE,
                      TERZO.DENOMINAZIONE_SEDE,
                      LINEA_ATTIVITA.DS_LINEA_ATTIVITA,
                      ACCERTAMENTO.CD_CDS_ORIGINE,
                      CONTRATTO.CD_TIPO_CONTRATTO,
                      CONTRATTO.IM_CONTRATTO_ATTIVO,
                      CONTRATTO.IM_CONTRATTO_PASSIVO

             UNION

             SELECT   'ETR' TIPO, ACCERTAMENTO.ESERCIZIO_ORIGINALE,
                      ACCERTAMENTO.CD_TERZO, ACCERTAMENTO.CD_ELEMENTO_VOCE,
                      ACCERTAMENTO.ESERCIZIO_CONTRATTO,
                      ACCERTAMENTO.PG_CONTRATTO, ACCERTAMENTO.STATO_CONTRATTO,
                      CONTRATTO.OGGETTO, CONTRATTO.DT_INIZIO_VALIDITA,
                      CONTRATTO.DT_FINE_VALIDITA,
                      ACCERTAMENTO_SCAD_VOCE.CD_LINEA_ATTIVITA,
                      ACCERTAMENTO_SCAD_VOCE.CD_CENTRO_RESPONSABILITA,
                      RIGA.ESERCIZIO_ORI_ACCERTAMENTO ESERCIZIO_OBB_ACR,
                      RIGA.PG_ACCERTAMENTO PG_OBBLIGAZIONE_ACCERTAMENTO,
                      RIGA.ESERCIZIO ESERCIZIO_MAN_REV,
                      RIGA.PG_REVERSALE PG_MAN_REV,
                      RIGA.ESERCIZIO_DOC_AMM ES_DOC_AMM,
                      RIGA.PG_DOC_AMM PG_DOC_AMM,
                      TIPO_DOCUMENTO_AMM.DS_TIPO_DOCUMENTO_AMM TIPO_DOC,
                      CONTRATTO_PADRE.ESERCIZIO ESERCIZIO_CONTRATTO_PADRE,
                      CONTRATTO_PADRE.PG_CONTRATTO PG_CONTRATTO_PADRE,
                      CONTRATTO_PADRE.STATO STATO_CONTRATTO_PADRE,
                      ELEMENTO_VOCE.DS_ELEMENTO_VOCE DESC_VOCE,
                      TERZO.DENOMINAZIONE_SEDE DESC_TERZO,
                      LINEA_ATTIVITA.DS_LINEA_ATTIVITA DESC_GAE,
                      ACCERTAMENTO.CD_CDS_ORIGINE CDS,
                      CONTRATTO.CD_TIPO_CONTRATTO,
                      CONTRATTO.IM_CONTRATTO_ATTIVO,
                      CONTRATTO.IM_CONTRATTO_PASSIVO, 0 TOTALE_ENTRATE,
                      0 TOTALE_SPESE, 0 LIQUIDATO_ENTRATE, 0 LIQUIDATO_SPESE,
                      SUM
                         (DECODE (NVL (ACCERTAMENTO_SCADENZARIO.IM_SCADENZA,
                                       0),
                                  0, 0,
                                    (  ACCERTAMENTO_SCAD_VOCE.IM_VOCE
                                     / ACCERTAMENTO_SCADENZARIO.IM_SCADENZA
                                    )
                                  * RIGA.IM_REVERSALE_RIGA
                                 )
                         ) TOTALE_REVERSALE,
                      0 TOTALE_MANDATI, 0 TOTALE_ORDINI,
                      0 TOTALE_MANDATI_NETTO, 0 LIQUIDATO_SPESE_NETTO
                 FROM CONTRATTO,
                      ACCERTAMENTO,
                      ACCERTAMENTO_SCADENZARIO,
                      ACCERTAMENTO_SCAD_VOCE,
                      REVERSALE_RIGA RIGA,
                      TERZO,
                      ELEMENTO_VOCE,
                      LINEA_ATTIVITA,
                      TIPO_DOCUMENTO_AMM
                LEFT OUTER JOIN CONTRATTO CONTRATTO_PADRE ON CONTRATTO_PADRE.ESERCIZIO = CONTRATTO.ESERCIZIO_PADRE AND CONTRATTO_PADRE.PG_CONTRATTO = CONTRATTO.PG_CONTRATTO_PADRE
                WHERE CONTRATTO.ESERCIZIO = ACCERTAMENTO.ESERCIZIO_CONTRATTO
                  AND CONTRATTO.STATO = ACCERTAMENTO.STATO_CONTRATTO
                  AND CONTRATTO.PG_CONTRATTO = ACCERTAMENTO.PG_CONTRATTO
                  AND ACCERTAMENTO_SCADENZARIO.CD_CDS = RIGA.CD_CDS
                  AND ACCERTAMENTO_SCADENZARIO.ESERCIZIO =
                                                   RIGA.ESERCIZIO_ACCERTAMENTO
                  AND ACCERTAMENTO_SCADENZARIO.ESERCIZIO_ORIGINALE =
                                               RIGA.ESERCIZIO_ORI_ACCERTAMENTO
                  AND ACCERTAMENTO_SCADENZARIO.PG_ACCERTAMENTO =
                                                          RIGA.PG_ACCERTAMENTO
                  AND ACCERTAMENTO_SCADENZARIO.PG_ACCERTAMENTO_SCADENZARIO =
                                              RIGA.PG_ACCERTAMENTO_SCADENZARIO
                  AND RIGA.STATO <> 'A'
                  AND ACCERTAMENTO.CD_CDS = ACCERTAMENTO_SCADENZARIO.CD_CDS
                  AND ACCERTAMENTO.ESERCIZIO =
                                            ACCERTAMENTO_SCADENZARIO.ESERCIZIO
                  AND ACCERTAMENTO.ESERCIZIO_ORIGINALE =
                                  ACCERTAMENTO_SCADENZARIO.ESERCIZIO_ORIGINALE
                  AND ACCERTAMENTO.PG_ACCERTAMENTO =
                                      ACCERTAMENTO_SCADENZARIO.PG_ACCERTAMENTO
                  AND ACCERTAMENTO_SCADENZARIO.CD_CDS =
                                                 ACCERTAMENTO_SCAD_VOCE.CD_CDS
                  AND ACCERTAMENTO_SCADENZARIO.ESERCIZIO =
                                              ACCERTAMENTO_SCAD_VOCE.ESERCIZIO
                  AND ACCERTAMENTO_SCADENZARIO.ESERCIZIO_ORIGINALE =
                                    ACCERTAMENTO_SCAD_VOCE.ESERCIZIO_ORIGINALE
                  AND ACCERTAMENTO_SCADENZARIO.PG_ACCERTAMENTO =
                                        ACCERTAMENTO_SCAD_VOCE.PG_ACCERTAMENTO
                  AND ACCERTAMENTO_SCADENZARIO.PG_ACCERTAMENTO_SCADENZARIO =
                            ACCERTAMENTO_SCAD_VOCE.PG_ACCERTAMENTO_SCADENZARIO
                  AND ELEMENTO_VOCE.ESERCIZIO = ACCERTAMENTO.ESERCIZIO
                  AND ELEMENTO_VOCE.TI_APPARTENENZA =
                                                  ACCERTAMENTO.TI_APPARTENENZA
                  AND ELEMENTO_VOCE.TI_GESTIONE = ACCERTAMENTO.TI_GESTIONE
                  AND ELEMENTO_VOCE.CD_ELEMENTO_VOCE =
                                                 ACCERTAMENTO.CD_ELEMENTO_VOCE
                  AND TERZO.CD_TERZO = ACCERTAMENTO.CD_TERZO
                  AND LINEA_ATTIVITA.CD_CENTRO_RESPONSABILITA =
                               ACCERTAMENTO_SCAD_VOCE.CD_CENTRO_RESPONSABILITA
                  AND LINEA_ATTIVITA.CD_LINEA_ATTIVITA =
                                      ACCERTAMENTO_SCAD_VOCE.CD_LINEA_ATTIVITA
                  AND TIPO_DOCUMENTO_AMM.CD_TIPO_DOCUMENTO_AMM = RIGA.CD_TIPO_DOCUMENTO_AMM
             GROUP BY 'ETR',
                      ACCERTAMENTO.ESERCIZIO_ORIGINALE,
                      ACCERTAMENTO.CD_TERZO,
                      ACCERTAMENTO.CD_ELEMENTO_VOCE,
                      ACCERTAMENTO.ESERCIZIO_CONTRATTO,
                      ACCERTAMENTO.PG_CONTRATTO,
                      ACCERTAMENTO.STATO_CONTRATTO,
                      CONTRATTO.OGGETTO,
                      CONTRATTO.DT_INIZIO_VALIDITA,
                      CONTRATTO.DT_FINE_VALIDITA,
                      ACCERTAMENTO_SCAD_VOCE.CD_LINEA_ATTIVITA,
                      ACCERTAMENTO_SCAD_VOCE.CD_CENTRO_RESPONSABILITA,
                      RIGA.ESERCIZIO_ORI_ACCERTAMENTO,
                      RIGA.PG_ACCERTAMENTO,
                      RIGA.ESERCIZIO,
                      RIGA.PG_REVERSALE,
                      RIGA.ESERCIZIO_DOC_AMM,
                      RIGA.PG_DOC_AMM,
                      TIPO_DOCUMENTO_AMM.DS_TIPO_DOCUMENTO_AMM,
                      CONTRATTO_PADRE.ESERCIZIO,
                      CONTRATTO_PADRE.PG_CONTRATTO,
                      CONTRATTO_PADRE.STATO,
                      ELEMENTO_VOCE.DS_ELEMENTO_VOCE,
                      TERZO.DENOMINAZIONE_SEDE,
                      LINEA_ATTIVITA.DS_LINEA_ATTIVITA,
                      ACCERTAMENTO.CD_CDS_ORIGINE,
                      CONTRATTO.CD_TIPO_CONTRATTO,
                      CONTRATTO.IM_CONTRATTO_ATTIVO,
                      CONTRATTO.IM_CONTRATTO_PASSIVO
             UNION

             SELECT   'ETR' TIPO, ACCERTAMENTO.ESERCIZIO_ORIGINALE,
                      ACCERTAMENTO.CD_TERZO, ACCERTAMENTO.CD_ELEMENTO_VOCE,
                      ACCERTAMENTO.ESERCIZIO_CONTRATTO,
                      ACCERTAMENTO.PG_CONTRATTO, ACCERTAMENTO.STATO_CONTRATTO,
                      CONTRATTO.OGGETTO, CONTRATTO.DT_INIZIO_VALIDITA,
                      CONTRATTO.DT_FINE_VALIDITA,
                      ACCERTAMENTO_SCAD_VOCE.CD_LINEA_ATTIVITA,
                      ACCERTAMENTO_SCAD_VOCE.CD_CENTRO_RESPONSABILITA,
                      V.ESERCIZIO_ORI_ACCERTAMENTO ESERCIZIO_OBB_ACR,
                      V.PG_ACCERTAMENTO PG_OBBLIGAZIONE_ACCERTAMENTO,
                      NULL ESERCIZIO_MAN_REV, NULL PG_MAN_REV,
                      V.ESERCIZIO ES_DOC_AMM, V.PG_DOCUMENTO_AMM PG_DOC_AMM,
                      TIPO_DOCUMENTO_AMM.DS_TIPO_DOCUMENTO_AMM TIPO_DOC,
                      CONTRATTO_PADRE.ESERCIZIO ESERCIZIO_CONTRATTO_PADRE,
                      CONTRATTO_PADRE.PG_CONTRATTO PG_CONTRATTO_PADRE,
                      CONTRATTO_PADRE.STATO STATO_CONTRATTO_PADRE,
                      ELEMENTO_VOCE.DS_ELEMENTO_VOCE DESC_VOCE,
                      TERZO.DENOMINAZIONE_SEDE DESC_TERZO,
                      LINEA_ATTIVITA.DS_LINEA_ATTIVITA DESC_GAE,
                      ACCERTAMENTO.CD_CDS_ORIGINE CDS,
                      CONTRATTO.CD_TIPO_CONTRATTO,
                      CONTRATTO.IM_CONTRATTO_ATTIVO,
                      CONTRATTO.IM_CONTRATTO_PASSIVO, 0 TOTALE_ENTRATE,
                      0 TOTALE_SPESE,
                      SUM
                         (DECODE (NVL (ACCERTAMENTO_SCADENZARIO.IM_SCADENZA,
                                       0),
                                  0, 0,
                                    (  ACCERTAMENTO_SCAD_VOCE.IM_VOCE
                                     / ACCERTAMENTO_SCADENZARIO.IM_SCADENZA
                                    )
                                  * V.IM_TOTALE_DOC_AMM
                                 )
                         ) LIQUIDATO_ENTRATE,
                      0 LIQUIDATO_SPESE, 0 TOTALE_REVERSALE, 0 TOTALE_MANDATI,
                      0 TOTALE_ORDINI, 0 TOTALE_MANDATI_NETTO,
                      0 LIQUIDATO_SPESE_NETTO
                 FROM CONTRATTO,
                      ACCERTAMENTO,
                      ACCERTAMENTO_SCADENZARIO,
                      ACCERTAMENTO_SCAD_VOCE,
                      V_DOC_ATTIVO V,
                      TERZO,
                      ELEMENTO_VOCE,
                      LINEA_ATTIVITA,
                      TIPO_DOCUMENTO_AMM
                LEFT OUTER JOIN CONTRATTO CONTRATTO_PADRE ON CONTRATTO_PADRE.ESERCIZIO = CONTRATTO.ESERCIZIO_PADRE AND CONTRATTO_PADRE.PG_CONTRATTO = CONTRATTO.PG_CONTRATTO_PADRE
                WHERE CONTRATTO.ESERCIZIO = ACCERTAMENTO.ESERCIZIO_CONTRATTO
                  AND CONTRATTO.STATO = ACCERTAMENTO.STATO_CONTRATTO
                  AND CONTRATTO.PG_CONTRATTO = ACCERTAMENTO.PG_CONTRATTO
                  AND ACCERTAMENTO_SCADENZARIO.CD_CDS = V.CD_CDS_ACCERTAMENTO
                  AND ACCERTAMENTO_SCADENZARIO.ESERCIZIO =
                                                      V.ESERCIZIO_ACCERTAMENTO
                  AND ACCERTAMENTO_SCADENZARIO.ESERCIZIO_ORIGINALE =
                                                  V.ESERCIZIO_ORI_ACCERTAMENTO
                  AND ACCERTAMENTO_SCADENZARIO.PG_ACCERTAMENTO =
                                                             V.PG_ACCERTAMENTO
                  AND ACCERTAMENTO_SCADENZARIO.PG_ACCERTAMENTO_SCADENZARIO =
                                                 V.PG_ACCERTAMENTO_SCADENZARIO
                  AND ACCERTAMENTO.CD_CDS = ACCERTAMENTO_SCADENZARIO.CD_CDS
                  AND ACCERTAMENTO.ESERCIZIO =
                                            ACCERTAMENTO_SCADENZARIO.ESERCIZIO
                  AND ACCERTAMENTO.ESERCIZIO_ORIGINALE =
                                  ACCERTAMENTO_SCADENZARIO.ESERCIZIO_ORIGINALE
                  AND ACCERTAMENTO.PG_ACCERTAMENTO =
                                      ACCERTAMENTO_SCADENZARIO.PG_ACCERTAMENTO
                  AND ACCERTAMENTO_SCADENZARIO.CD_CDS =
                                                 ACCERTAMENTO_SCAD_VOCE.CD_CDS
                  AND ACCERTAMENTO_SCADENZARIO.ESERCIZIO =
                                              ACCERTAMENTO_SCAD_VOCE.ESERCIZIO
                  AND ACCERTAMENTO_SCADENZARIO.ESERCIZIO_ORIGINALE =
                                    ACCERTAMENTO_SCAD_VOCE.ESERCIZIO_ORIGINALE
                  AND ACCERTAMENTO_SCADENZARIO.PG_ACCERTAMENTO =
                                        ACCERTAMENTO_SCAD_VOCE.PG_ACCERTAMENTO
                  AND ACCERTAMENTO_SCADENZARIO.PG_ACCERTAMENTO_SCADENZARIO =
                            ACCERTAMENTO_SCAD_VOCE.PG_ACCERTAMENTO_SCADENZARIO
                  AND ELEMENTO_VOCE.ESERCIZIO = ACCERTAMENTO.ESERCIZIO
                  AND ELEMENTO_VOCE.TI_APPARTENENZA =
                                                  ACCERTAMENTO.TI_APPARTENENZA
                  AND ELEMENTO_VOCE.TI_GESTIONE = ACCERTAMENTO.TI_GESTIONE
                  AND ELEMENTO_VOCE.CD_ELEMENTO_VOCE =
                                                 ACCERTAMENTO.CD_ELEMENTO_VOCE
                  AND TERZO.CD_TERZO = ACCERTAMENTO.CD_TERZO
                  AND LINEA_ATTIVITA.CD_CENTRO_RESPONSABILITA =
                               ACCERTAMENTO_SCAD_VOCE.CD_CENTRO_RESPONSABILITA
                  AND LINEA_ATTIVITA.CD_LINEA_ATTIVITA =
                                      ACCERTAMENTO_SCAD_VOCE.CD_LINEA_ATTIVITA
                  AND TIPO_DOCUMENTO_AMM.CD_TIPO_DOCUMENTO_AMM = V.CD_TIPO_DOCUMENTO_AMM
             GROUP BY 'ETR',
                      ACCERTAMENTO.ESERCIZIO_ORIGINALE,
                      ACCERTAMENTO.CD_TERZO,
                      ACCERTAMENTO.CD_ELEMENTO_VOCE,
                      ACCERTAMENTO.ESERCIZIO_CONTRATTO,
                      ACCERTAMENTO.PG_CONTRATTO,
                      ACCERTAMENTO.STATO_CONTRATTO,
                      CONTRATTO.OGGETTO,
                      CONTRATTO.DT_INIZIO_VALIDITA,
                      CONTRATTO.DT_FINE_VALIDITA,
                      ACCERTAMENTO_SCAD_VOCE.CD_LINEA_ATTIVITA,
                      ACCERTAMENTO_SCAD_VOCE.CD_CENTRO_RESPONSABILITA,
                      V.ESERCIZIO_ORI_ACCERTAMENTO,
                      V.PG_ACCERTAMENTO,
                      NULL,
                      NULL,
                      V.ESERCIZIO,
                      V.PG_DOCUMENTO_AMM,
                      TIPO_DOCUMENTO_AMM.DS_TIPO_DOCUMENTO_AMM,
                      CONTRATTO_PADRE.ESERCIZIO,
                      CONTRATTO_PADRE.PG_CONTRATTO,
                      CONTRATTO_PADRE.STATO,
                      ELEMENTO_VOCE.DS_ELEMENTO_VOCE,
                      TERZO.DENOMINAZIONE_SEDE,
                      LINEA_ATTIVITA.DS_LINEA_ATTIVITA,
                      ACCERTAMENTO.CD_CDS_ORIGINE,
                      CONTRATTO.CD_TIPO_CONTRATTO,
                      CONTRATTO.IM_CONTRATTO_ATTIVO,
                      CONTRATTO.IM_CONTRATTO_PASSIVO
             UNION

             SELECT   'SPE' TIPO, OBBLIGAZIONE.ESERCIZIO_ORIGINALE,
                      OBBLIGAZIONE.CD_TERZO, OBBLIGAZIONE.CD_ELEMENTO_VOCE,
                      OBBLIGAZIONE.ESERCIZIO_CONTRATTO,
                      OBBLIGAZIONE.PG_CONTRATTO, OBBLIGAZIONE.STATO_CONTRATTO,
                      CONTRATTO.OGGETTO, CONTRATTO.DT_INIZIO_VALIDITA,
                      CONTRATTO.DT_FINE_VALIDITA,
                      OBBLIGAZIONE_SCAD_VOCE.CD_LINEA_ATTIVITA,
                      OBBLIGAZIONE_SCAD_VOCE.CD_CENTRO_RESPONSABILITA,
                      OBBLIGAZIONE.ESERCIZIO ESERCIZIO_OBB_ACR,
                      OBBLIGAZIONE.PG_OBBLIGAZIONE
                                                 PG_OBBLIGAZIONE_ACCERTAMENTO,
                      NULL ESERCIZIO_MAN_REV, NULL PG_MAN_REV,
                      NULL ES_DOC_AMM, NULL PG_DOC_AMM, NULL TIPO_DOC,
                      CONTRATTO_PADRE.ESERCIZIO ESERCIZIO_CONTRATTO_PADRE,
                      CONTRATTO_PADRE.PG_CONTRATTO PG_CONTRATTO_PADRE,
                      CONTRATTO_PADRE.STATO STATO_CONTRATTO_PADRE,
                      ELEMENTO_VOCE.DS_ELEMENTO_VOCE DESC_VOCE,
                      TERZO.DENOMINAZIONE_SEDE DESC_TERZO,
                      LINEA_ATTIVITA.DS_LINEA_ATTIVITA DESC_GAE,
                      OBBLIGAZIONE.CD_CDS CDS, CONTRATTO.CD_TIPO_CONTRATTO,
                      CONTRATTO.IM_CONTRATTO_ATTIVO,
                      CONTRATTO.IM_CONTRATTO_PASSIVO, 0 TOTALE_ENTRATE,
                      SUM (OBBLIGAZIONE_SCAD_VOCE.IM_VOCE) TOTALE_SPESE,
                      0 LIQUIDATO_ENTRATE, 0 LIQUIDATO_SPESE,
                      0 TOTALE_REVERSALE, 0 TOTALE_MANDATI, 0 TOTALE_ORDINI,
                      0 TOTALE_MANDATI_NETTO, 0 LIQUIDATO_SPESE_NETTO
                 FROM CONTRATTO,
                      OBBLIGAZIONE,
                      OBBLIGAZIONE_SCADENZARIO,
                      OBBLIGAZIONE_SCAD_VOCE,
                      TERZO,
                      ELEMENTO_VOCE,
                      LINEA_ATTIVITA
                LEFT OUTER JOIN CONTRATTO CONTRATTO_PADRE ON CONTRATTO_PADRE.ESERCIZIO = CONTRATTO.ESERCIZIO_PADRE AND CONTRATTO_PADRE.PG_CONTRATTO = CONTRATTO.PG_CONTRATTO_PADRE
                WHERE CONTRATTO.ESERCIZIO = OBBLIGAZIONE.ESERCIZIO_CONTRATTO
                  AND CONTRATTO.STATO = OBBLIGAZIONE.STATO_CONTRATTO
                  AND CONTRATTO.PG_CONTRATTO = OBBLIGAZIONE.PG_CONTRATTO
                  AND OBBLIGAZIONE.CD_CDS = OBBLIGAZIONE_SCADENZARIO.CD_CDS
                  AND OBBLIGAZIONE.ESERCIZIO =
                                            OBBLIGAZIONE_SCADENZARIO.ESERCIZIO
                  AND (   OBBLIGAZIONE.ESERCIZIO_ORIGINALE =
                                                        OBBLIGAZIONE.ESERCIZIO
                       OR OBBLIGAZIONE.CD_TIPO_DOCUMENTO_CONT = 'OBB_RESIM'
                       OR (    OBBLIGAZIONE.CD_TIPO_DOCUMENTO_CONT = 'OBB_RES'
                           AND NOT EXISTS (
                                  SELECT 1
                                    FROM OBBLIGAZIONE OBB
                                   WHERE OBBLIGAZIONE.CD_CDS = OBB.CD_CDS
                                     AND OBBLIGAZIONE.ESERCIZIO >
                                                                 OBB.ESERCIZIO
                                     AND OBBLIGAZIONE.ESERCIZIO_ORIGINALE =
                                                       OBB.ESERCIZIO_ORIGINALE
                                     AND OBBLIGAZIONE.PG_OBBLIGAZIONE =
                                                           OBB.PG_OBBLIGAZIONE
                                     AND OBBLIGAZIONE.ESERCIZIO_CONTRATTO =
                                                       OBB.ESERCIZIO_CONTRATTO
                                     AND OBBLIGAZIONE.STATO_CONTRATTO =
                                                           OBB.STATO_CONTRATTO
                                     AND OBBLIGAZIONE.PG_CONTRATTO =
                                                              OBB.PG_CONTRATTO)
                          )
                      )
                  AND OBBLIGAZIONE.ESERCIZIO_ORIGINALE =
                                  OBBLIGAZIONE_SCADENZARIO.ESERCIZIO_ORIGINALE
                  AND OBBLIGAZIONE.PG_OBBLIGAZIONE =
                                      OBBLIGAZIONE_SCADENZARIO.PG_OBBLIGAZIONE
                  AND OBBLIGAZIONE_SCADENZARIO.CD_CDS =
                                                 OBBLIGAZIONE_SCAD_VOCE.CD_CDS
                  AND OBBLIGAZIONE_SCADENZARIO.ESERCIZIO =
                                              OBBLIGAZIONE_SCAD_VOCE.ESERCIZIO
                  AND OBBLIGAZIONE_SCADENZARIO.ESERCIZIO_ORIGINALE =
                                    OBBLIGAZIONE_SCAD_VOCE.ESERCIZIO_ORIGINALE
                  AND OBBLIGAZIONE_SCADENZARIO.PG_OBBLIGAZIONE =
                                        OBBLIGAZIONE_SCAD_VOCE.PG_OBBLIGAZIONE
                  AND OBBLIGAZIONE_SCADENZARIO.PG_OBBLIGAZIONE_SCADENZARIO =
                            OBBLIGAZIONE_SCAD_VOCE.PG_OBBLIGAZIONE_SCADENZARIO
                  AND ELEMENTO_VOCE.ESERCIZIO = OBBLIGAZIONE.ESERCIZIO
                  AND ELEMENTO_VOCE.TI_APPARTENENZA =
                                                  OBBLIGAZIONE.TI_APPARTENENZA
                  AND ELEMENTO_VOCE.TI_GESTIONE = OBBLIGAZIONE.TI_GESTIONE
                  AND ELEMENTO_VOCE.CD_ELEMENTO_VOCE =
                                                 OBBLIGAZIONE.CD_ELEMENTO_VOCE
                  AND TERZO.CD_TERZO = OBBLIGAZIONE.CD_TERZO
                  AND LINEA_ATTIVITA.CD_CENTRO_RESPONSABILITA =
                               OBBLIGAZIONE_SCAD_VOCE.CD_CENTRO_RESPONSABILITA
                  AND LINEA_ATTIVITA.CD_LINEA_ATTIVITA =
                                      OBBLIGAZIONE_SCAD_VOCE.CD_LINEA_ATTIVITA
             GROUP BY 'SPE',
                      OBBLIGAZIONE.ESERCIZIO_ORIGINALE,
                      OBBLIGAZIONE.CD_TERZO,
                      OBBLIGAZIONE.CD_ELEMENTO_VOCE,
                      OBBLIGAZIONE.ESERCIZIO_CONTRATTO,
                      OBBLIGAZIONE.PG_CONTRATTO,
                      OBBLIGAZIONE.STATO_CONTRATTO,
                      CONTRATTO.OGGETTO,
                      CONTRATTO.DT_INIZIO_VALIDITA,
                      CONTRATTO.DT_FINE_VALIDITA,
                      OBBLIGAZIONE_SCAD_VOCE.CD_LINEA_ATTIVITA,
                      OBBLIGAZIONE_SCAD_VOCE.CD_CENTRO_RESPONSABILITA,
                      OBBLIGAZIONE.ESERCIZIO,
                      OBBLIGAZIONE.PG_OBBLIGAZIONE,
                      NULL,
                      NULL,
                      NULL,
                      NULL,
                      NULL,
                      CONTRATTO_PADRE.ESERCIZIO,
                      CONTRATTO_PADRE.PG_CONTRATTO,
                      CONTRATTO_PADRE.STATO,
                      ELEMENTO_VOCE.DS_ELEMENTO_VOCE,
                      TERZO.DENOMINAZIONE_SEDE,
                      LINEA_ATTIVITA.DS_LINEA_ATTIVITA,
                      OBBLIGAZIONE.CD_CDS,
                      CONTRATTO.CD_TIPO_CONTRATTO,
                      CONTRATTO.IM_CONTRATTO_ATTIVO,
                      CONTRATTO.IM_CONTRATTO_PASSIVO
             UNION

             SELECT   'SPE' TIPO, OBBLIGAZIONE.ESERCIZIO_ORIGINALE,
                      OBBLIGAZIONE.CD_TERZO, OBBLIGAZIONE.CD_ELEMENTO_VOCE,
                      OBBLIGAZIONE.ESERCIZIO_CONTRATTO,
                      OBBLIGAZIONE.PG_CONTRATTO, OBBLIGAZIONE.STATO_CONTRATTO,
                      CONTRATTO.OGGETTO, CONTRATTO.DT_INIZIO_VALIDITA,
                      CONTRATTO.DT_FINE_VALIDITA,
                      OBBLIGAZIONE_MOD_VOCE.CD_LINEA_ATTIVITA,
                      OBBLIGAZIONE_MOD_VOCE.CD_CENTRO_RESPONSABILITA,
                      OBBLIGAZIONE.ESERCIZIO ESERCIZIO_OBB_ACR,
                      OBBLIGAZIONE.PG_OBBLIGAZIONE
                                                 PG_OBBLIGAZIONE_ACCERTAMENTO,
                      NULL ESERCIZIO_MAN_REV, NULL PG_MAN_REV,
                      NULL ES_DOC_AMM, NULL PG_DOC_AMM, NULL TIPO_DOC,
                      CONTRATTO_PADRE.ESERCIZIO ESERCIZIO_CONTRATTO_PADRE,
                      CONTRATTO_PADRE.PG_CONTRATTO PG_CONTRATTO_PADRE,
                      CONTRATTO_PADRE.STATO STATO_CONTRATTO_PADRE,
                      ELEMENTO_VOCE.DS_ELEMENTO_VOCE DESC_VOCE,
                      TERZO.DENOMINAZIONE_SEDE DESC_TERZO,
                      LINEA_ATTIVITA.DS_LINEA_ATTIVITA DESC_GAE,
                      OBBLIGAZIONE.CD_CDS CDS, CONTRATTO.CD_TIPO_CONTRATTO,
                      CONTRATTO.IM_CONTRATTO_ATTIVO,
                      CONTRATTO.IM_CONTRATTO_PASSIVO, 0 TOTALE_ENTRATE,
                      SUM (OBBLIGAZIONE_MOD_VOCE.IM_MODIFICA) TOTALE_SPESE,
                      0 LIQUIDATO_ENTRATE, 0 LIQUIDATO_SPESE,
                      0 TOTALE_REVERSALE, 0 TOTALE_MANDATI, 0 TOTALE_ORDINI,
                      0 TOTALE_MANDATI_NETTO, 0 LIQUIDATO_SPESE_NETTO
                 FROM CONTRATTO,
                      OBBLIGAZIONE,
                      OBBLIGAZIONE_MODIFICA,
                      OBBLIGAZIONE_MOD_VOCE,
                      TERZO,
                      ELEMENTO_VOCE,
                      LINEA_ATTIVITA
                LEFT OUTER JOIN CONTRATTO CONTRATTO_PADRE ON CONTRATTO_PADRE.ESERCIZIO = CONTRATTO.ESERCIZIO_PADRE AND CONTRATTO_PADRE.PG_CONTRATTO = CONTRATTO.PG_CONTRATTO_PADRE
                WHERE CONTRATTO.ESERCIZIO = OBBLIGAZIONE.ESERCIZIO_CONTRATTO
                  AND CONTRATTO.STATO = OBBLIGAZIONE.STATO_CONTRATTO
                  AND CONTRATTO.PG_CONTRATTO = OBBLIGAZIONE.PG_CONTRATTO
                  AND OBBLIGAZIONE.CD_CDS = OBBLIGAZIONE_MODIFICA.CD_CDS
                  AND OBBLIGAZIONE.ESERCIZIO = OBBLIGAZIONE_MODIFICA.ESERCIZIO
                  AND OBBLIGAZIONE.ESERCIZIO_ORIGINALE =
                                     OBBLIGAZIONE_MODIFICA.ESERCIZIO_ORIGINALE
                  AND OBBLIGAZIONE.PG_OBBLIGAZIONE =
                                         OBBLIGAZIONE_MODIFICA.PG_OBBLIGAZIONE
                  AND OBBLIGAZIONE_MODIFICA.CD_CDS =
                                                  OBBLIGAZIONE_MOD_VOCE.CD_CDS
                  AND OBBLIGAZIONE_MODIFICA.ESERCIZIO =
                                               OBBLIGAZIONE_MOD_VOCE.ESERCIZIO
                  AND OBBLIGAZIONE_MODIFICA.PG_MODIFICA =
                                             OBBLIGAZIONE_MOD_VOCE.PG_MODIFICA
                  AND ELEMENTO_VOCE.ESERCIZIO = OBBLIGAZIONE.ESERCIZIO
                  AND ELEMENTO_VOCE.TI_APPARTENENZA =
                                                  OBBLIGAZIONE.TI_APPARTENENZA
                  AND ELEMENTO_VOCE.TI_GESTIONE = OBBLIGAZIONE.TI_GESTIONE
                  AND ELEMENTO_VOCE.CD_ELEMENTO_VOCE =
                                                 OBBLIGAZIONE.CD_ELEMENTO_VOCE
                  AND TERZO.CD_TERZO = OBBLIGAZIONE.CD_TERZO
                  AND LINEA_ATTIVITA.CD_CENTRO_RESPONSABILITA =
                                OBBLIGAZIONE_MOD_VOCE.CD_CENTRO_RESPONSABILITA
                  AND LINEA_ATTIVITA.CD_LINEA_ATTIVITA =
                                       OBBLIGAZIONE_MOD_VOCE.CD_LINEA_ATTIVITA

                  AND EXISTS (
                         SELECT 1
                           FROM OBBLIGAZIONE OBB
                          WHERE OBBLIGAZIONE.CD_CDS = OBB.CD_CDS

                            AND OBBLIGAZIONE.ESERCIZIO_ORIGINALE =
                                                       OBB.ESERCIZIO_ORIGINALE
                            AND OBBLIGAZIONE.PG_OBBLIGAZIONE =
                                                           OBB.PG_OBBLIGAZIONE
                            AND OBBLIGAZIONE.ESERCIZIO_CONTRATTO =
                                                       OBB.ESERCIZIO_CONTRATTO
                            AND OBBLIGAZIONE.STATO_CONTRATTO =
                                                           OBB.STATO_CONTRATTO
                            AND OBBLIGAZIONE.PG_CONTRATTO = OBB.PG_CONTRATTO
                            AND (   OBB.ESERCIZIO = OBB.ESERCIZIO_ORIGINALE
                                 OR OBB.CD_TIPO_DOCUMENTO_CONT = 'OBB_RESIM'
                                ))
             GROUP BY 'SPE',
                      OBBLIGAZIONE.ESERCIZIO_ORIGINALE,
                      OBBLIGAZIONE.CD_TERZO,
                      OBBLIGAZIONE.CD_ELEMENTO_VOCE,
                      OBBLIGAZIONE.ESERCIZIO_CONTRATTO,
                      OBBLIGAZIONE.PG_CONTRATTO,
                      OBBLIGAZIONE.STATO_CONTRATTO,
                      CONTRATTO.OGGETTO,
                      CONTRATTO.DT_INIZIO_VALIDITA,
                      CONTRATTO.DT_FINE_VALIDITA,
                      OBBLIGAZIONE_MOD_VOCE.CD_LINEA_ATTIVITA,
                      OBBLIGAZIONE_MOD_VOCE.CD_CENTRO_RESPONSABILITA,
                      OBBLIGAZIONE.ESERCIZIO,
                      OBBLIGAZIONE.PG_OBBLIGAZIONE,
                      NULL,
                      NULL,
                      NULL,
                      NULL,
                      NULL,
                      CONTRATTO_PADRE.ESERCIZIO,
                      CONTRATTO_PADRE.PG_CONTRATTO,
                      CONTRATTO_PADRE.STATO,
                      ELEMENTO_VOCE.DS_ELEMENTO_VOCE,
                      TERZO.DENOMINAZIONE_SEDE,
                      LINEA_ATTIVITA.DS_LINEA_ATTIVITA,
                      OBBLIGAZIONE.CD_CDS,
                      CONTRATTO.CD_TIPO_CONTRATTO,
                      CONTRATTO.IM_CONTRATTO_ATTIVO,
                      CONTRATTO.IM_CONTRATTO_PASSIVO

             UNION

             SELECT   'SPE' TIPO, OBBLIGAZIONE.ESERCIZIO_ORIGINALE,
                      OBBLIGAZIONE.CD_TERZO, OBBLIGAZIONE.CD_ELEMENTO_VOCE,
                      OBBLIGAZIONE.ESERCIZIO_CONTRATTO,
                      OBBLIGAZIONE.PG_CONTRATTO, OBBLIGAZIONE.STATO_CONTRATTO,
                      CONTRATTO.OGGETTO, CONTRATTO.DT_INIZIO_VALIDITA,
                      CONTRATTO.DT_FINE_VALIDITA,
                      OBBLIGAZIONE_SCAD_VOCE.CD_LINEA_ATTIVITA,
                      OBBLIGAZIONE_SCAD_VOCE.CD_CENTRO_RESPONSABILITA,
                      RIGA.ESERCIZIO_ORI_OBBLIGAZIONE ESERCIZIO_OBB_ACR,
                      RIGA.PG_OBBLIGAZIONE PG_OBBLIGAZIONE_ACCERTAMENTO,
                      RIGA.ESERCIZIO ESERCIZIO_MAN_REV,
                      RIGA.PG_MANDATO PG_MAN_REV,
                      RIGA.ESERCIZIO_DOC_AMM ES_DOC_AMM,
                      RIGA.PG_DOC_AMM PG_DOC_AMM,
                      TIPO_DOCUMENTO_AMM.DS_TIPO_DOCUMENTO_AMM TIPO_DOC,
                      CONTRATTO_PADRE.ESERCIZIO ESERCIZIO_CONTRATTO_PADRE,
                      CONTRATTO_PADRE.PG_CONTRATTO PG_CONTRATTO_PADRE,
                      CONTRATTO_PADRE.STATO STATO_CONTRATTO_PADRE,
                      ELEMENTO_VOCE.DS_ELEMENTO_VOCE DESC_VOCE,
                      TERZO.DENOMINAZIONE_SEDE DESC_TERZO,
                      LINEA_ATTIVITA.DS_LINEA_ATTIVITA DESC_GAE,
                      OBBLIGAZIONE.CD_CDS CDS, CONTRATTO.CD_TIPO_CONTRATTO,
                      CONTRATTO.IM_CONTRATTO_ATTIVO,
                      CONTRATTO.IM_CONTRATTO_PASSIVO, 0 TOTALE_ENTRATE,
                      0 TOTALE_SPESE, 0 LIQUIDATO_ENTRATE, 0 LIQUIDATO_SPESE,
                      0 TOTALE_REVERSALE,
                      SUM
                         (DECODE (NVL (OBBLIGAZIONE_SCADENZARIO.IM_SCADENZA,
                                       0),
                                  0, 0,
                                    (  OBBLIGAZIONE_SCAD_VOCE.IM_VOCE
                                     / OBBLIGAZIONE_SCADENZARIO.IM_SCADENZA
                                    )
                                  * RIGA.IM_MANDATO_RIGA
                                 )
                         ) TOTALE_MANDATI,
                      0 TOTALE_ORDINI,
                      SUM
                         (ROUND (  (  (  OBBLIGAZIONE_SCAD_VOCE.IM_VOCE
                                       / OBBLIGAZIONE_SCADENZARIO.IM_SCADENZA
                                      )
                                    * RIGA.IM_MANDATO_RIGA
                                   )
                                 * (  DECODE (V.IM_IMPONIBILE_DOC_AMM,
                                              0, V.IM_TOTALE_DOC_AMM,
                                              V.IM_IMPONIBILE_DOC_AMM
                                             )
                                    / V.IM_TOTALE_DOC_AMM
                                   ),
                                 2
                                )
                         ) TOTALE_MANDATI_NETTO,
                      0 LIQUIDATO_SPESE_NETTO
                 FROM CONTRATTO,
                      OBBLIGAZIONE,
                      OBBLIGAZIONE_SCADENZARIO,
                      OBBLIGAZIONE_SCAD_VOCE,
                      MANDATO_RIGA RIGA,
                      V_DOC_PASSIVO_OBBLIGAZIONE V,
                      TERZO,
                      ELEMENTO_VOCE,
                      LINEA_ATTIVITA,
                      TIPO_DOCUMENTO_AMM
                LEFT OUTER JOIN CONTRATTO CONTRATTO_PADRE ON CONTRATTO_PADRE.ESERCIZIO = CONTRATTO.ESERCIZIO_PADRE AND CONTRATTO_PADRE.PG_CONTRATTO = CONTRATTO.PG_CONTRATTO_PADRE
                WHERE OBBLIGAZIONE_SCADENZARIO.CD_CDS = RIGA.CD_CDS
                  AND OBBLIGAZIONE_SCADENZARIO.ESERCIZIO =
                                                   RIGA.ESERCIZIO_OBBLIGAZIONE
                  AND OBBLIGAZIONE_SCADENZARIO.ESERCIZIO_ORIGINALE =
                                               RIGA.ESERCIZIO_ORI_OBBLIGAZIONE
                  AND OBBLIGAZIONE_SCADENZARIO.PG_OBBLIGAZIONE =
                                                          RIGA.PG_OBBLIGAZIONE
                  AND OBBLIGAZIONE_SCADENZARIO.PG_OBBLIGAZIONE_SCADENZARIO =
                                              RIGA.PG_OBBLIGAZIONE_SCADENZARIO
                  AND OBBLIGAZIONE_SCADENZARIO.CD_CDS = V.CD_CDS_OBBLIGAZIONE
                  AND OBBLIGAZIONE_SCADENZARIO.ESERCIZIO =
                                                      V.ESERCIZIO_OBBLIGAZIONE
                  AND OBBLIGAZIONE_SCADENZARIO.ESERCIZIO_ORIGINALE =
                                                  V.ESERCIZIO_ORI_OBBLIGAZIONE
                  AND OBBLIGAZIONE_SCADENZARIO.PG_OBBLIGAZIONE =
                                                             V.PG_OBBLIGAZIONE
                  AND OBBLIGAZIONE_SCADENZARIO.PG_OBBLIGAZIONE_SCADENZARIO =
                                                 V.PG_OBBLIGAZIONE_SCADENZARIO
                  AND V.ESERCIZIO = RIGA.ESERCIZIO_DOC_AMM
                  AND V.CD_TIPO_DOCUMENTO_AMM = RIGA.CD_TIPO_DOCUMENTO_AMM
                  AND V.PG_DOCUMENTO_AMM = RIGA.PG_DOC_AMM
                  AND RIGA.STATO <> 'A'
                  AND IM_TOTALE_DOC_AMM > 0
                  AND CONTRATTO.ESERCIZIO = OBBLIGAZIONE.ESERCIZIO_CONTRATTO
                  AND CONTRATTO.STATO = OBBLIGAZIONE.STATO_CONTRATTO
                  AND CONTRATTO.PG_CONTRATTO = OBBLIGAZIONE.PG_CONTRATTO
                  AND OBBLIGAZIONE.CD_CDS = OBBLIGAZIONE_SCADENZARIO.CD_CDS
                  AND OBBLIGAZIONE.ESERCIZIO =
                                            OBBLIGAZIONE_SCADENZARIO.ESERCIZIO
                  AND OBBLIGAZIONE.ESERCIZIO_ORIGINALE =
                                  OBBLIGAZIONE_SCADENZARIO.ESERCIZIO_ORIGINALE
                  AND OBBLIGAZIONE.PG_OBBLIGAZIONE =
                                      OBBLIGAZIONE_SCADENZARIO.PG_OBBLIGAZIONE
                  AND OBBLIGAZIONE_SCADENZARIO.CD_CDS =
                                                 OBBLIGAZIONE_SCAD_VOCE.CD_CDS
                  AND OBBLIGAZIONE_SCADENZARIO.ESERCIZIO =
                                              OBBLIGAZIONE_SCAD_VOCE.ESERCIZIO
                  AND OBBLIGAZIONE_SCADENZARIO.ESERCIZIO_ORIGINALE =
                                    OBBLIGAZIONE_SCAD_VOCE.ESERCIZIO_ORIGINALE
                  AND OBBLIGAZIONE_SCADENZARIO.PG_OBBLIGAZIONE =
                                        OBBLIGAZIONE_SCAD_VOCE.PG_OBBLIGAZIONE
                  AND OBBLIGAZIONE_SCADENZARIO.PG_OBBLIGAZIONE_SCADENZARIO =
                            OBBLIGAZIONE_SCAD_VOCE.PG_OBBLIGAZIONE_SCADENZARIO
                  AND ELEMENTO_VOCE.ESERCIZIO = OBBLIGAZIONE.ESERCIZIO
                  AND ELEMENTO_VOCE.TI_APPARTENENZA =
                                                  OBBLIGAZIONE.TI_APPARTENENZA
                  AND ELEMENTO_VOCE.TI_GESTIONE = OBBLIGAZIONE.TI_GESTIONE
                  AND ELEMENTO_VOCE.CD_ELEMENTO_VOCE =
                                                 OBBLIGAZIONE.CD_ELEMENTO_VOCE
                  AND TERZO.CD_TERZO = OBBLIGAZIONE.CD_TERZO
                  AND LINEA_ATTIVITA.CD_CENTRO_RESPONSABILITA =
                               OBBLIGAZIONE_SCAD_VOCE.CD_CENTRO_RESPONSABILITA
                  AND LINEA_ATTIVITA.CD_LINEA_ATTIVITA =
                                      OBBLIGAZIONE_SCAD_VOCE.CD_LINEA_ATTIVITA
                  AND TIPO_DOCUMENTO_AMM.CD_TIPO_DOCUMENTO_AMM = RIGA.CD_TIPO_DOCUMENTO_AMM
             GROUP BY 'SPE',
                      OBBLIGAZIONE.ESERCIZIO_ORIGINALE,
                      OBBLIGAZIONE.CD_TERZO,
                      OBBLIGAZIONE.CD_ELEMENTO_VOCE,
                      OBBLIGAZIONE.ESERCIZIO_CONTRATTO,
                      OBBLIGAZIONE.PG_CONTRATTO,
                      OBBLIGAZIONE.STATO_CONTRATTO,
                      CONTRATTO.OGGETTO,
                      CONTRATTO.DT_INIZIO_VALIDITA,
                      CONTRATTO.DT_FINE_VALIDITA,
                      OBBLIGAZIONE_SCAD_VOCE.CD_LINEA_ATTIVITA,
                      OBBLIGAZIONE_SCAD_VOCE.CD_CENTRO_RESPONSABILITA,
                      RIGA.ESERCIZIO_ORI_OBBLIGAZIONE,
                      RIGA.PG_OBBLIGAZIONE,
                      RIGA.ESERCIZIO,
                      RIGA.PG_MANDATO,
                      RIGA.ESERCIZIO_DOC_AMM,
                      RIGA.PG_DOC_AMM,
                      TIPO_DOCUMENTO_AMM.DS_TIPO_DOCUMENTO_AMM,
                      CONTRATTO_PADRE.ESERCIZIO,
                      CONTRATTO_PADRE.PG_CONTRATTO,
                      CONTRATTO_PADRE.STATO,
                      ELEMENTO_VOCE.DS_ELEMENTO_VOCE,
                      TERZO.DENOMINAZIONE_SEDE,
                      LINEA_ATTIVITA.DS_LINEA_ATTIVITA,
                      OBBLIGAZIONE.CD_CDS,
                      CONTRATTO.CD_TIPO_CONTRATTO,
                      CONTRATTO.IM_CONTRATTO_ATTIVO,
                      CONTRATTO.IM_CONTRATTO_PASSIVO
             UNION

             SELECT   'SPE' TIPO, OBBLIGAZIONE.ESERCIZIO_ORIGINALE,
                      OBBLIGAZIONE.CD_TERZO, OBBLIGAZIONE.CD_ELEMENTO_VOCE,
                      OBBLIGAZIONE.ESERCIZIO_CONTRATTO,
                      OBBLIGAZIONE.PG_CONTRATTO, OBBLIGAZIONE.STATO_CONTRATTO,
                      CONTRATTO.OGGETTO, CONTRATTO.DT_INIZIO_VALIDITA,
                      CONTRATTO.DT_FINE_VALIDITA,
                      OBBLIGAZIONE_SCAD_VOCE.CD_LINEA_ATTIVITA,
                      OBBLIGAZIONE_SCAD_VOCE.CD_CENTRO_RESPONSABILITA,
                      V.ESERCIZIO_ORI_OBBLIGAZIONE ESERCIZIO_OBB_ACR,
                      V.PG_OBBLIGAZIONE PG_OBBLIGAZIONE_ACCERTAMENTO,
                      NULL ESERCIZIO_MAN_REV, NULL PG_MAN_REV,
                      V.ESERCIZIO ES_DOC_AMM, V.PG_DOCUMENTO_AMM PG_DOC_AMM,
                      TIPO_DOCUMENTO_AMM.DS_TIPO_DOCUMENTO_AMM TIPO_DOC,
                      CONTRATTO_PADRE.ESERCIZIO ESERCIZIO_CONTRATTO_PADRE,
                      CONTRATTO_PADRE.PG_CONTRATTO PG_CONTRATTO_PADRE,
                      CONTRATTO_PADRE.STATO STATO_CONTRATTO_PADRE,
                      ELEMENTO_VOCE.DS_ELEMENTO_VOCE DESC_VOCE,
                      TERZO.DENOMINAZIONE_SEDE DESC_TERZO,
                      LINEA_ATTIVITA.DS_LINEA_ATTIVITA DESC_GAE,
                      OBBLIGAZIONE.CD_CDS CDS, CONTRATTO.CD_TIPO_CONTRATTO,
                      CONTRATTO.IM_CONTRATTO_ATTIVO,
                      CONTRATTO.IM_CONTRATTO_PASSIVO, 0 TOTALE_ENTRATE,
                      0 TOTALE_SPESE, 0 LIQUIDATO_ENTRATE,
                      ROUND
                         (SUM
                             (DECODE
                                  (NVL (OBBLIGAZIONE_SCADENZARIO.IM_SCADENZA,
                                        0
                                       ),
                                   0, 0,
                                     (  OBBLIGAZIONE_SCAD_VOCE.IM_VOCE
                                      / OBBLIGAZIONE_SCADENZARIO.IM_SCADENZA
                                     )
                                   * V.IM_TOTALE_DOC_AMM
                                  )
                             ),
                          2
                         ) LIQUIDATO_SPESE,
                      0 TOTALE_REVERSALE, 0 TOTALE_MANDATI, 0 TOTALE_ORDINI,
                      0 TOTALE_MANDATI_NETTO,
                      ROUND
                         (SUM
                             (DECODE
                                  (NVL (OBBLIGAZIONE_SCADENZARIO.IM_SCADENZA,
                                        0
                                       ),
                                   0, 0,
                                     (  OBBLIGAZIONE_SCAD_VOCE.IM_VOCE
                                      / OBBLIGAZIONE_SCADENZARIO.IM_SCADENZA
                                     )
                                   * DECODE (V.IM_IMPONIBILE_DOC_AMM,
                                             0, V.IM_TOTALE_DOC_AMM,
                                             V.IM_IMPONIBILE_DOC_AMM
                                            )
                                  )
                             ),
                          2
                         ) LIQUIDATO_SPESE_NETTO
                 FROM CONTRATTO,
                      OBBLIGAZIONE,
                      OBBLIGAZIONE_SCADENZARIO,
                      OBBLIGAZIONE_SCAD_VOCE,
                      V_DOC_PASSIVO V,
                      TERZO,
                      ELEMENTO_VOCE,
                      LINEA_ATTIVITA,
                      TIPO_DOCUMENTO_AMM
                LEFT OUTER JOIN CONTRATTO CONTRATTO_PADRE ON CONTRATTO_PADRE.ESERCIZIO = CONTRATTO.ESERCIZIO_PADRE AND CONTRATTO_PADRE.PG_CONTRATTO = CONTRATTO.PG_CONTRATTO_PADRE
                WHERE CONTRATTO.ESERCIZIO = OBBLIGAZIONE.ESERCIZIO_CONTRATTO
                  AND CONTRATTO.STATO = OBBLIGAZIONE.STATO_CONTRATTO
                  AND CONTRATTO.PG_CONTRATTO = OBBLIGAZIONE.PG_CONTRATTO
                  AND OBBLIGAZIONE_SCADENZARIO.CD_CDS = V.CD_CDS_OBBLIGAZIONE
                  AND OBBLIGAZIONE_SCADENZARIO.ESERCIZIO =
                                                      V.ESERCIZIO_OBBLIGAZIONE
                  AND OBBLIGAZIONE_SCADENZARIO.ESERCIZIO_ORIGINALE =
                                                  V.ESERCIZIO_ORI_OBBLIGAZIONE
                  AND OBBLIGAZIONE_SCADENZARIO.PG_OBBLIGAZIONE =
                                                             V.PG_OBBLIGAZIONE
                  AND OBBLIGAZIONE_SCADENZARIO.PG_OBBLIGAZIONE_SCADENZARIO =
                                                 V.PG_OBBLIGAZIONE_SCADENZARIO
                  AND OBBLIGAZIONE.CD_CDS = OBBLIGAZIONE_SCADENZARIO.CD_CDS
                  AND OBBLIGAZIONE.ESERCIZIO =
                                            OBBLIGAZIONE_SCADENZARIO.ESERCIZIO
                  AND OBBLIGAZIONE.ESERCIZIO_ORIGINALE =
                                  OBBLIGAZIONE_SCADENZARIO.ESERCIZIO_ORIGINALE
                  AND OBBLIGAZIONE.PG_OBBLIGAZIONE =
                                      OBBLIGAZIONE_SCADENZARIO.PG_OBBLIGAZIONE
                  AND OBBLIGAZIONE_SCADENZARIO.CD_CDS =
                                                 OBBLIGAZIONE_SCAD_VOCE.CD_CDS
                  AND OBBLIGAZIONE_SCADENZARIO.ESERCIZIO =
                                              OBBLIGAZIONE_SCAD_VOCE.ESERCIZIO
                  AND OBBLIGAZIONE_SCADENZARIO.ESERCIZIO_ORIGINALE =
                                    OBBLIGAZIONE_SCAD_VOCE.ESERCIZIO_ORIGINALE
                  AND OBBLIGAZIONE_SCADENZARIO.PG_OBBLIGAZIONE =
                                        OBBLIGAZIONE_SCAD_VOCE.PG_OBBLIGAZIONE
                  AND OBBLIGAZIONE_SCADENZARIO.PG_OBBLIGAZIONE_SCADENZARIO =
                            OBBLIGAZIONE_SCAD_VOCE.PG_OBBLIGAZIONE_SCADENZARIO
                  AND OBBLIGAZIONE_SCADENZARIO.IM_SCADENZA != 0
                  AND ELEMENTO_VOCE.ESERCIZIO = OBBLIGAZIONE.ESERCIZIO
                  AND ELEMENTO_VOCE.TI_APPARTENENZA =
                                                  OBBLIGAZIONE.TI_APPARTENENZA
                  AND ELEMENTO_VOCE.TI_GESTIONE = OBBLIGAZIONE.TI_GESTIONE
                  AND ELEMENTO_VOCE.CD_ELEMENTO_VOCE =
                                                 OBBLIGAZIONE.CD_ELEMENTO_VOCE
                  AND TERZO.CD_TERZO = OBBLIGAZIONE.CD_TERZO
                  AND LINEA_ATTIVITA.CD_CENTRO_RESPONSABILITA =
                               OBBLIGAZIONE_SCAD_VOCE.CD_CENTRO_RESPONSABILITA
                  AND LINEA_ATTIVITA.CD_LINEA_ATTIVITA =
                                      OBBLIGAZIONE_SCAD_VOCE.CD_LINEA_ATTIVITA
                  AND TIPO_DOCUMENTO_AMM.CD_TIPO_DOCUMENTO_AMM = V.CD_TIPO_DOCUMENTO_AMM
             GROUP BY 'SPE',
                      OBBLIGAZIONE.ESERCIZIO_ORIGINALE,
                      OBBLIGAZIONE.CD_TERZO,
                      OBBLIGAZIONE.CD_ELEMENTO_VOCE,
                      OBBLIGAZIONE.ESERCIZIO_CONTRATTO,
                      OBBLIGAZIONE.PG_CONTRATTO,
                      OBBLIGAZIONE.STATO_CONTRATTO,
                      CONTRATTO.OGGETTO,
                      CONTRATTO.DT_INIZIO_VALIDITA,
                      CONTRATTO.DT_FINE_VALIDITA,
                      OBBLIGAZIONE_SCAD_VOCE.CD_LINEA_ATTIVITA,
                      OBBLIGAZIONE_SCAD_VOCE.CD_CENTRO_RESPONSABILITA,
                      V.ESERCIZIO_ORI_OBBLIGAZIONE,
                      V.PG_OBBLIGAZIONE,
                      NULL,
                      NULL,
                      V.ESERCIZIO,
                      V.PG_DOCUMENTO_AMM,
                      TIPO_DOCUMENTO_AMM.DS_TIPO_DOCUMENTO_AMM,
                      CONTRATTO_PADRE.ESERCIZIO,
                      CONTRATTO_PADRE.PG_CONTRATTO,
                      CONTRATTO_PADRE.STATO,
                      ELEMENTO_VOCE.DS_ELEMENTO_VOCE,
                      TERZO.DENOMINAZIONE_SEDE,
                      LINEA_ATTIVITA.DS_LINEA_ATTIVITA,
                      OBBLIGAZIONE.CD_CDS,
                      CONTRATTO.CD_TIPO_CONTRATTO,
                      CONTRATTO.IM_CONTRATTO_ATTIVO,
                      CONTRATTO.IM_CONTRATTO_PASSIVO
             UNION

             SELECT   'ORD' TIPO, ORDINE_ACQ.ESERCIZIO, ORDINE_ACQ.CD_TERZO,
                      OBBLIGAZIONE.CD_ELEMENTO_VOCE,
                      ORDINE_ACQ.ESERCIZIO_CONTRATTO, ORDINE_ACQ.PG_CONTRATTO,
                      ORDINE_ACQ.STATO_CONTRATTO, CONTRATTO.OGGETTO,
                      CONTRATTO.DT_INIZIO_VALIDITA,
                      CONTRATTO.DT_FINE_VALIDITA, NULL CD_LINEA_ATTIVITA,
                      NULL CD_CENTRO_RESPONSABILITA,
                      OBBLIGAZIONE.ESERCIZIO ESERCIZIO_OBB_ACR,
                      OBBLIGAZIONE.PG_OBBLIGAZIONE
                                                 PG_OBBLIGAZIONE_ACCERTAMENTO,
                      NULL ESERCIZIO_MAN_REV, NULL PG_MAN_REV,
                      NULL ES_DOC_AMM, NULL PG_DOC_AMM, NULL TIPO_DOC,
                      CONTRATTO_PADRE.ESERCIZIO ESERCIZIO_CONTRATTO_PADRE,
                      CONTRATTO_PADRE.PG_CONTRATTO PG_CONTRATTO_PADRE,
                      CONTRATTO_PADRE.STATO STATO_CONTRATTO_PADRE,
                      ELEMENTO_VOCE.DS_ELEMENTO_VOCE DESC_VOCE,
                      TERZO.DENOMINAZIONE_SEDE DESC_TERZO, NULL DESC_GAE,
                      ORDINE_ACQ.CD_CDS CDS, CONTRATTO.CD_TIPO_CONTRATTO,
                      CONTRATTO.IM_CONTRATTO_ATTIVO,
                      CONTRATTO.IM_CONTRATTO_PASSIVO, 0 TOTALE_ENTRATE,
                      0 TOTALE_SPESE, 0 LIQUIDATO_ENTRATE, 0 LIQUIDATO_SPESE,
                      0 TOTALE_REVERSALE, 0 TOTALE_MANDATI,
                      SUM
                         (ORDINE_ACQ_CONSEGNA.IM_TOTALE_CONSEGNA
                         ) TOTALE_ORDINI,
                      0 TOTALE_MANDATI_NETTO, 0 LIQUIDATO_SPESE_NETTO
                 FROM CONTRATTO,
                      ORDINE_ACQ,
                      ORDINE_ACQ_RIGA,
                      ORDINE_ACQ_CONSEGNA,
                      TERZO
                LEFT OUTER JOIN CONTRATTO CONTRATTO_PADRE ON CONTRATTO_PADRE.ESERCIZIO = CONTRATTO.ESERCIZIO_PADRE AND CONTRATTO_PADRE.PG_CONTRATTO = CONTRATTO.PG_CONTRATTO_PADRE
                LEFT OUTER JOIN OBBLIGAZIONE_SCADENZARIO ON OBBLIGAZIONE_SCADENZARIO.CD_CDS = ORDINE_ACQ_CONSEGNA.CD_CDS_OBBL AND
                				OBBLIGAZIONE_SCADENZARIO.ESERCIZIO = ORDINE_ACQ_CONSEGNA.ESERCIZIO_OBBL AND
                				OBBLIGAZIONE_SCADENZARIO.ESERCIZIO_ORIGINALE = ORDINE_ACQ_CONSEGNA.ESERCIZIO_ORIG_OBBL AND
                				OBBLIGAZIONE_SCADENZARIO.PG_OBBLIGAZIONE = ORDINE_ACQ_CONSEGNA.PG_OBBLIGAZIONE AND
                				OBBLIGAZIONE_SCADENZARIO.PG_OBBLIGAZIONE_SCADENZARIO = ORDINE_ACQ_CONSEGNA.PG_OBBLIGAZIONE_SCAD
                LEFT OUTER JOIN OBBLIGAZIONE ON OBBLIGAZIONE.CD_CDS = OBBLIGAZIONE_SCADENZARIO.CD_CDS AND
                				OBBLIGAZIONE.ESERCIZIO = OBBLIGAZIONE_SCADENZARIO.ESERCIZIO AND
                				OBBLIGAZIONE.ESERCIZIO_ORIGINALE = OBBLIGAZIONE_SCADENZARIO.ESERCIZIO_ORIGINALE AND
                				OBBLIGAZIONE.PG_OBBLIGAZIONE = OBBLIGAZIONE_SCADENZARIO.PG_OBBLIGAZIONE
                LEFT OUTER JOIN ELEMENTO_VOCE ON ELEMENTO_VOCE.ESERCIZIO = OBBLIGAZIONE.ESERCIZIO AND
                                ELEMENTO_VOCE.TI_APPARTENENZA = OBBLIGAZIONE.TI_APPARTENENZA AND
                                ELEMENTO_VOCE.TI_GESTIONE = OBBLIGAZIONE.TI_GESTIONE AND
                                ELEMENTO_VOCE.CD_ELEMENTO_VOCE = OBBLIGAZIONE.CD_ELEMENTO_VOCE
                WHERE CONTRATTO.ESERCIZIO = ORDINE_ACQ.ESERCIZIO_CONTRATTO
                  AND CONTRATTO.STATO = ORDINE_ACQ.STATO_CONTRATTO
                  AND CONTRATTO.PG_CONTRATTO = ORDINE_ACQ.PG_CONTRATTO
                  AND TERZO.CD_TERZO = ORDINE_ACQ.CD_TERZO
                  AND ORDINE_ACQ_RIGA.ESERCIZIO = ORDINE_ACQ.ESERCIZIO
                  AND ORDINE_ACQ_RIGA.CD_CDS = ORDINE_ACQ.CD_CDS
                  AND ORDINE_ACQ_RIGA.CD_UNITA_OPERATIVA =
                                                 ORDINE_ACQ.CD_UNITA_OPERATIVA
                  AND ORDINE_ACQ_RIGA.CD_NUMERATORE = ORDINE_ACQ.CD_NUMERATORE
                  AND ORDINE_ACQ_RIGA.NUMERO = ORDINE_ACQ.NUMERO
                  AND ORDINE_ACQ_RIGA.ESERCIZIO =
                                                 ORDINE_ACQ_CONSEGNA.ESERCIZIO
                  AND ORDINE_ACQ_RIGA.CD_CDS = ORDINE_ACQ_CONSEGNA.CD_CDS
                  AND ORDINE_ACQ_RIGA.CD_UNITA_OPERATIVA =
                                        ORDINE_ACQ_CONSEGNA.CD_UNITA_OPERATIVA
                  AND ORDINE_ACQ_RIGA.CD_NUMERATORE =
                                             ORDINE_ACQ_CONSEGNA.CD_NUMERATORE
                  AND ORDINE_ACQ_RIGA.NUMERO = ORDINE_ACQ_CONSEGNA.NUMERO
                  AND ORDINE_ACQ_RIGA.RIGA = ORDINE_ACQ_CONSEGNA.RIGA
                  AND ORDINE_ACQ.STATO != 'ANN'
             GROUP BY 'ORD',
                      ORDINE_ACQ.ESERCIZIO,
                      ORDINE_ACQ.CD_TERZO,
                      OBBLIGAZIONE.CD_ELEMENTO_VOCE,
                      ORDINE_ACQ.ESERCIZIO_CONTRATTO,
                      ORDINE_ACQ.PG_CONTRATTO,
                      ORDINE_ACQ.STATO_CONTRATTO,
                      CONTRATTO.OGGETTO,
                      CONTRATTO.DT_INIZIO_VALIDITA,
                      CONTRATTO.DT_FINE_VALIDITA,
                      NULL,
                      NULL,
                      OBBLIGAZIONE.ESERCIZIO,
                      OBBLIGAZIONE.PG_OBBLIGAZIONE,
                      NULL,
                      NULL,
                      NULL,
                      NULL,
                      NULL,
                      CONTRATTO_PADRE.ESERCIZIO,
                      CONTRATTO_PADRE.PG_CONTRATTO,
                      CONTRATTO_PADRE.STATO,
                      ELEMENTO_VOCE.DS_ELEMENTO_VOCE,
                      TERZO.DENOMINAZIONE_SEDE,
                      NULL,
                      ORDINE_ACQ.CD_CDS,
                      CONTRATTO.CD_TIPO_CONTRATTO,
                      CONTRATTO.IM_CONTRATTO_ATTIVO,
                      CONTRATTO.IM_CONTRATTO_PASSIVO) A
   GROUP BY A.TIPO,
            A.ESERCIZIO_ORIGINALE,
            A.CD_TERZO,
            A.CD_ELEMENTO_VOCE,
            A.ESERCIZIO_CONTRATTO,
            A.PG_CONTRATTO,
            A.STATO_CONTRATTO,
            A.OGGETTO,
            A.DT_INIZIO_VALIDITA,
            A.DT_FINE_VALIDITA,
            A.CD_LINEA_ATTIVITA,
            A.CD_CENTRO_RESPONSABILITA,
            A.ESERCIZIO_OBB_ACR,
            A.PG_OBBLIGAZIONE_ACCERTAMENTO,
            A.ESERCIZIO_MAN_REV,
            A.PG_MAN_REV,
            A.ES_DOC_AMM,
            A.PG_DOC_AMM,
            A.TIPO_DOC,
            A.ESERCIZIO_CONTRATTO_PADRE,
            A.PG_CONTRATTO_PADRE,
            A.STATO_CONTRATTO_PADRE,
            A.DESC_VOCE,
            A.DESC_TERZO,
            A.DESC_GAE,
            A.CDS,
            A.CD_TIPO_CONTRATTO,
            NVL (A.IM_CONTRATTO_ATTIVO, 0),
            NVL (A.IM_CONTRATTO_PASSIVO, 0);

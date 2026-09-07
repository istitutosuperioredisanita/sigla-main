/* Vista di consultazione completa dei progetti con dati anagrafici, finanziari e contabili. */

CREATE
OR REPLACE VIEW V_CONS_EST_PROGETTI AS

SELECT

    /* Identificativi del progetto */
    P.ESERCIZIO,
    P.PG_PROGETTO,
    P.CD_PROGETTO,

    /* Descrizione progetto */
    P.DS_PROGETTO,

    /* Area progettuale */
    PP.CD_PROGETTO                 AS AREA_PROGETTUALE,

    /* Unità organizzativa */
    P.CD_UNITA_ORGANIZZATIVA,

    P.TIPO_FASE,
    P.LIVELLO,

    /* Responsabile scientifico */
    P.CD_RESPONSABILE_TERZO,
    TR_RESP.DENOMINAZIONE_SEDE     AS RESPONSABILE_SCIENTIFICO,

    /* Date */
    POF.DT_INIZIO,
    POF.DT_FINE,
    POF.DT_PROROGA,

    /* Stato */
    POF.STATO,

    /* Classificazioni */
    P.CD_MISSIONE                  AS MISSIONE,
    P.CD_TIPO_PROGETTO             AS TIPO_PROGETTO,
    TF_OF.DESCRIZIONE              AS TIPO_FINANZIAMENTO,

    /* Enti finanziatori */
    FIN.ENTE_FINANZIATORE,

    /* Importi */
    POF.IM_FINANZIATO,
    POF.IM_COFINANZIATO,

    /* Piano economico */
    QUOTE.ESERCIZIO_PIANO,

    /* Indicatori economici */
    NVL(QUOTE.QUOTA_ASSEGNATA, 0)  AS QUOTA_ASSEGNATA,
    NVL(QUOTE.QUOTA_STANZIATA, 0)  AS QUOTA_STANZIATA,
    NVL(QUOTE.QUOTA_UTILIZZATA, 0) AS QUOTA_UTILIZZATA,
    NVL(QUOTE.QUOTA_PAGATA, 0)     AS QUOTA_PAGATA,

    /* Note */
    P.NOTE

FROM PROGETTO P

         /* Progetto padre */
         INNER JOIN PROGETTO PP
                    ON PP.ESERCIZIO = P.ESERCIZIO_PROGETTO_PADRE
                        AND PP.PG_PROGETTO = P.PG_PROGETTO_PADRE
                        AND PP.TIPO_FASE = P.TIPO_FASE_PROGETTO_PADRE

    /* Dati aggiuntivi */
         INNER JOIN PROGETTO_OTHER_FIELD POF
                    ON P.PG_PROGETTO_OTHER_FIELD = POF.PG_PROGETTO

    /* Responsabile scientifico */
         INNER JOIN TERZO TR_RESP
                    ON TR_RESP.CD_TERZO = P.CD_RESPONSABILE_TERZO

    /* Tipo finanziamento */
         INNER JOIN TIPO_FINANZIAMENTO TF_OF
                    ON TF_OF.ID = POF.ID_TIPO_FINANZIAMENTO

    /* Finanziatori aggregati */
         LEFT OUTER JOIN (SELECT PF.PG_PROGETTO,
                                 LISTAGG(
                                         T.DENOMINAZIONE_SEDE,
                                         '; '
                                 ) WITHIN GROUP (
                   ORDER BY T.DENOMINAZIONE_SEDE
               ) AS ENTE_FINANZIATORE
                          FROM PROGETTO_FINANZIATORE PF
                              INNER JOIN TERZO T
                          ON T.CD_TERZO = PF.CD_FINANZIATORE_TERZO
                          GROUP BY PF.PG_PROGETTO) FIN
                         ON FIN.PG_PROGETTO = P.PG_PROGETTO

    /* Quote economiche */
         INNER JOIN (SELECT ESERCIZIO,
                            PG_PROGETTO,
                            ESERCIZIO_PIANO,
                            SUM(IM_SPESA_FINANZIATO) AS QUOTA_ASSEGNATA,
                            SUM(STANZ)               AS QUOTA_STANZIATA,
                            SUM(UT)                  AS QUOTA_UTILIZZATA,
                            SUM(PAG)                 AS QUOTA_PAGATA
                     FROM (SELECT ESERCIZIO,
                                  PG_PROGETTO,
                                  CD_VOCE_PIANO,
                                  ESERCIZIO_PIANO,
                                  IM_SPESA_FINANZIATO,
                                  SUM(NVL(ASSESTATO_FIN, 0) + NVL(ASSESTATO_COFIN, 0)) AS STANZ,
                                  SUM(NVL(IMPACC_FIN, 0) + NVL(IMPACC_COFIN, 0))       AS UT,
                                  SUM(NVL(MANRIS_FIN, 0) + NVL(MANRIS_COFIN, 0))       AS PAG
                           FROM V_SALDI_PIANO_ECONOM_PROGCDR
                           GROUP BY ESERCIZIO,
                                    PG_PROGETTO,
                                    CD_VOCE_PIANO,
                                    ESERCIZIO_PIANO,
                                    IM_SPESA_FINANZIATO)
                     GROUP BY ESERCIZIO,
                              PG_PROGETTO,
                              ESERCIZIO_PIANO) QUOTE
                    ON QUOTE.ESERCIZIO = P.ESERCIZIO
                    AND QUOTE.PG_PROGETTO = P.PG_PROGETTO;
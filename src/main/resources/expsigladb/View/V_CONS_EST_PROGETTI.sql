/* Vista di consultazione completa dei progetti con dati anagrafici, finanziari e contabili. */

CREATE OR REPLACE VIEW V_CONS_EST_PROGETTI AS

WITH

/* Calcola la quota assegnata al progetto sommando gli importi
   finanziati e cofinanziati presenti nel piano economico. */
Q_PIANO AS (
    SELECT PG_PROGETTO,
           SUM(NVL(IM_SPESA_FINANZIATO, 0) + NVL(IM_SPESA_COFINANZIATO, 0)) AS QUOTA_ASSEGNATA
    FROM PROGETTO_PIANO_ECONOMICO
    GROUP BY PG_PROGETTO
),

/* Calcola i principali saldi contabili del progetto:
   - quota stanziata;
   - quota utilizzata (impegni);
   - quota pagata (mandati). */
Q_SALDI AS (
    SELECT
        PG_PROGETTO,
        SUM(
            NVL(STANZIAMENTO_FIN,0) + NVL(VARIAPIU_FIN,0) - NVL(VARIAMENO_FIN,0) +
            NVL(STANZIAMENTO_COFIN,0) + NVL(VARIAPIU_COFIN,0) - NVL(VARIAMENO_COFIN,0)
        ) AS QUOTA_STANZIATA,
        SUM(NVL(IMPACC_FIN,0) + NVL(IMPACC_COFIN,0)) AS QUOTA_UTILIZZATA,
        SUM(NVL(MANRIS_FIN,0)+ NVL(MANRIS_COFIN,0)) AS QUOTA_PAGATA
    FROM V_SALDI_VOCE_PROGETTO
    GROUP BY PG_PROGETTO
),

/* Recupera tutti gli enti finanziatori del progetto
   concatenandoli in un'unica stringa separata da '; '. */
Q_FINANZIATORI AS (
    SELECT PF.PG_PROGETTO,
           LISTAGG(T.DENOMINAZIONE_SEDE, '; ')
               WITHIN GROUP (ORDER BY T.DENOMINAZIONE_SEDE) AS ENTE_FINANZIATORE
    FROM PROGETTO_FINANZIATORE PF
    LEFT JOIN TERZO T
           ON PF.CD_FINANZIATORE_TERZO = T.CD_TERZO
    GROUP BY PF.PG_PROGETTO
)

SELECT
    /* Identificativi del progetto */
    P.PG_PROGETTO,
    P.ESERCIZIO,
    P.TIPO_FASE,
    P.LIVELLO,
    P.CD_RESPONSABILE_TERZO,
    P.CD_PROGETTO,

    /* Area progettuale (progetto padre) */
    PP.DS_PROGETTO AS AREA_PROGETTUALE,

    /* Unità organizzativa */
    P.CD_UNITA_ORGANIZZATIVA,

    /* Descrizione del progetto */
    P.DS_PROGETTO,

    /* Importi previsti */
    POF.IM_FINANZIATO,
    POF.IM_COFINANZIATO,

    /* Date del progetto */
    POF.DT_INIZIO,
    POF.DT_FINE,
    POF.DT_PROROGA,

    /* Stato del progetto */
    POF.STATO,

    /* Responsabile scientifico */
    TR.DENOMINAZIONE_SEDE AS RESPONSABILE_SCIENTIFICO,

    /* Ente/i finanziatore/i */
    F.ENTE_FINANZIATORE,

    /* Classificazioni del progetto */
    P.CD_MISSIONE AS MISSIONE,
    P.CD_TIPO_PROGETTO AS TIPO_PROGETTO,
    TF_OF.DESCRIZIONE AS TIPO_FINANZIAMENTO,

    /* Note */
    P.NOTE,

    /* Indicatori economici */
    NVL(QP.QUOTA_ASSEGNATA, 0) AS QUOTA_ASSEGNATA,
    NVL(QS.QUOTA_STANZIATA, 0) AS QUOTA_STANZIATA,
    NVL(QS.QUOTA_UTILIZZATA, 0) AS QUOTA_UTILIZZATA,
    NVL(QS.QUOTA_PAGATA, 0) AS QUOTA_PAGATA

FROM PROGETTO P

/* Quote assegnate dal piano economico */
         LEFT JOIN Q_PIANO QP
                   ON P.PG_PROGETTO = QP.PG_PROGETTO

/* Saldi contabili del progetto */
         LEFT JOIN Q_SALDI QS
                   ON P.PG_PROGETTO = QS.PG_PROGETTO

/* Elenco enti finanziatori */
         LEFT JOIN Q_FINANZIATORI F
                   ON P.PG_PROGETTO = F.PG_PROGETTO

/* Responsabile scientifico */
         LEFT JOIN TERZO TR
                   ON P.CD_RESPONSABILE_TERZO = TR.CD_TERZO

/* Progetto padre per recuperare l'area progettuale */
         LEFT JOIN PROGETTO PP
                   ON PP.ESERCIZIO = P.ESERCIZIO_PROGETTO_PADRE
                       AND PP.PG_PROGETTO = P.PG_PROGETTO_PADRE
                       AND PP.TIPO_FASE = P.TIPO_FASE_PROGETTO_PADRE

/* Informazioni aggiuntive del progetto */
         LEFT JOIN PROGETTO_OTHER_FIELD POF
                   ON P.PG_PROGETTO_OTHER_FIELD = POF.PG_PROGETTO

/* Tipologia di finanziamento */
         LEFT JOIN TIPO_FINANZIAMENTO TF_OF
                   ON POF.ID_TIPO_FINANZIAMENTO = TF_OF.ID;
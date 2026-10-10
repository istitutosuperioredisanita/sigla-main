--------------------------------------------------------
--  DDL for View V_CLASSIFICAZIONE_VOCI_ALL (versione H2)
--------------------------------------------------------
-- Estrae il codice di tutte le Classificazioni Ufficiali.
-- Simile a V_CLASSIFICAZIONE_VOCI con l'aggiunta che per ogni classificazione
-- viene riproposto anche il codice dei livelli precedenti.
--
-- Modifiche rispetto alla versione Oracle:
--  * outer join (+) sostituiti con LEFT JOIN ANSI
--  * DECODE sostituito con CASE (portabile)
--  * rimosso FORCE e i nomi di colonna tra apici

CREATE OR REPLACE VIEW V_CLASSIFICAZIONE_VOCI_ALL (
    ID_CLASSIFICAZIONE, ESERCIZIO, TI_GESTIONE, DS_CLASSIFICAZIONE,
    CD_LIVELLO1, CD_LIVELLO2, CD_LIVELLO3, CD_LIVELLO4, CD_LIVELLO5, CD_LIVELLO6, CD_LIVELLO7,
    ID_CLASS_PADRE, NR_LIVELLO, FL_MASTRINO, FL_CLASS_SAC, FL_SOLO_GESTIONE,
    FL_PIANO_RIPARTO, FL_ACCENTRATO, FL_DECENTRATO, FL_ESTERNA_DA_QUADRARE_SAC,
    CDR_ACCENTRATORE, TI_CLASSIFICAZIONE, FL_PREV_OBB_ANNO_SUC, IM_LIMITE_ASSESTATO,
    DUVA, UTUV, DACR, UTCR, PG_VER_REC, CD_CLASSIFICAZIONE,
    CD_LIV1, CD_LIV2, CD_LIV3, CD_LIV4, CD_LIV5, CD_LIV6, CD_LIV7,
    ID_LIV1, ID_LIV2, ID_LIV3, ID_LIV4, ID_LIV5, ID_LIV6, ID_LIV7,
    DS_LIV1, DS_LIV2, DS_LIV3, DS_LIV4, DS_LIV5, DS_LIV6, DS_LIV7
) AS
SELECT
    a.id_classificazione, a.esercizio, a.ti_gestione,
    a.ds_classificazione, a.cd_livello1, a.cd_livello2, a.cd_livello3,
    a.cd_livello4, a.cd_livello5, a.cd_livello6, a.cd_livello7,
    a.id_class_padre, a.nr_livello, a.fl_mastrino, a.fl_class_sac,
    a.fl_solo_gestione, a.fl_piano_riparto, a.fl_accentrato,
    a.fl_decentrato, a.fl_esterna_da_quadrare_sac, a.cdr_accentratore,
    a.ti_classificazione, a.fl_prev_obb_anno_suc, a.im_limite_assestato,
    a.duva, a.utuv, a.dacr, a.utcr, a.pg_ver_rec, a.cd_classificazione,

    -- Codici
    CASE a.nr_livello WHEN 1 THEN a.cd_classificazione WHEN 2 THEN b.cd_classificazione
                      WHEN 3 THEN c.cd_classificazione WHEN 4 THEN d.cd_classificazione
                      WHEN 5 THEN e.cd_classificazione WHEN 6 THEN f.cd_classificazione
                      WHEN 7 THEN g.cd_classificazione ELSE NULL END AS cd_liv1,
    CASE a.nr_livello WHEN 2 THEN a.cd_classificazione WHEN 3 THEN b.cd_classificazione
                      WHEN 4 THEN c.cd_classificazione WHEN 5 THEN d.cd_classificazione
                      WHEN 6 THEN e.cd_classificazione WHEN 7 THEN f.cd_classificazione
                      ELSE NULL END AS cd_liv2,
    CASE a.nr_livello WHEN 3 THEN a.cd_classificazione WHEN 4 THEN b.cd_classificazione
                      WHEN 5 THEN c.cd_classificazione WHEN 6 THEN d.cd_classificazione
                      WHEN 7 THEN e.cd_classificazione ELSE NULL END AS cd_liv3,
    CASE a.nr_livello WHEN 4 THEN a.cd_classificazione WHEN 5 THEN b.cd_classificazione
                      WHEN 6 THEN c.cd_classificazione WHEN 7 THEN d.cd_classificazione
                      ELSE NULL END AS cd_liv4,
    CASE a.nr_livello WHEN 5 THEN a.cd_classificazione WHEN 6 THEN b.cd_classificazione
                      WHEN 7 THEN c.cd_classificazione ELSE NULL END AS cd_liv5,
    CASE a.nr_livello WHEN 6 THEN a.cd_classificazione WHEN 7 THEN b.cd_classificazione
                      ELSE NULL END AS cd_liv6,
    CASE a.nr_livello WHEN 7 THEN a.cd_classificazione ELSE NULL END AS cd_liv7,

    -- Id
    CASE a.nr_livello WHEN 1 THEN a.id_classificazione WHEN 2 THEN b.id_classificazione
                      WHEN 3 THEN c.id_classificazione WHEN 4 THEN d.id_classificazione
                      WHEN 5 THEN e.id_classificazione WHEN 6 THEN f.id_classificazione
                      WHEN 7 THEN g.id_classificazione ELSE NULL END AS id_liv1,
    CASE a.nr_livello WHEN 2 THEN a.id_classificazione WHEN 3 THEN b.id_classificazione
                      WHEN 4 THEN c.id_classificazione WHEN 5 THEN d.id_classificazione
                      WHEN 6 THEN e.id_classificazione WHEN 7 THEN f.id_classificazione
                      ELSE NULL END AS id_liv2,
    CASE a.nr_livello WHEN 3 THEN a.id_classificazione WHEN 4 THEN b.id_classificazione
                      WHEN 5 THEN c.id_classificazione WHEN 6 THEN d.id_classificazione
                      WHEN 7 THEN e.id_classificazione ELSE NULL END AS id_liv3,
    CASE a.nr_livello WHEN 4 THEN a.id_classificazione WHEN 5 THEN b.id_classificazione
                      WHEN 6 THEN c.id_classificazione WHEN 7 THEN d.id_classificazione
                      ELSE NULL END AS id_liv4,
    CASE a.nr_livello WHEN 5 THEN a.id_classificazione WHEN 6 THEN b.id_classificazione
                      WHEN 7 THEN c.id_classificazione ELSE NULL END AS id_liv5,
    CASE a.nr_livello WHEN 6 THEN a.id_classificazione WHEN 7 THEN b.id_classificazione
                      ELSE NULL END AS id_liv6,
    CASE a.nr_livello WHEN 7 THEN a.id_classificazione ELSE NULL END AS id_liv7,

    -- Descrizioni
    CASE a.nr_livello WHEN 1 THEN a.ds_classificazione WHEN 2 THEN b.ds_classificazione
                      WHEN 3 THEN c.ds_classificazione WHEN 4 THEN d.ds_classificazione
                      WHEN 5 THEN e.ds_classificazione WHEN 6 THEN f.ds_classificazione
                      WHEN 7 THEN g.ds_classificazione ELSE NULL END AS ds_liv1,
    CASE a.nr_livello WHEN 2 THEN a.ds_classificazione WHEN 3 THEN b.ds_classificazione
                      WHEN 4 THEN c.ds_classificazione WHEN 5 THEN d.ds_classificazione
                      WHEN 6 THEN e.ds_classificazione WHEN 7 THEN f.ds_classificazione
                      ELSE NULL END AS ds_liv2,
    CASE a.nr_livello WHEN 3 THEN a.ds_classificazione WHEN 4 THEN b.ds_classificazione
                      WHEN 5 THEN c.ds_classificazione WHEN 6 THEN d.ds_classificazione
                      WHEN 7 THEN e.ds_classificazione ELSE NULL END AS ds_liv3,
    CASE a.nr_livello WHEN 4 THEN a.ds_classificazione WHEN 5 THEN b.ds_classificazione
                      WHEN 6 THEN c.ds_classificazione WHEN 7 THEN d.ds_classificazione
                      ELSE NULL END AS ds_liv4,
    CASE a.nr_livello WHEN 5 THEN a.ds_classificazione WHEN 6 THEN b.ds_classificazione
                      WHEN 7 THEN c.ds_classificazione ELSE NULL END AS ds_liv5,
    CASE a.nr_livello WHEN 6 THEN a.ds_classificazione WHEN 7 THEN b.ds_classificazione
                      ELSE NULL END AS ds_liv6,
    CASE a.nr_livello WHEN 7 THEN a.ds_classificazione ELSE NULL END AS ds_liv7
FROM v_classificazione_voci a
         LEFT JOIN v_classificazione_voci b ON a.id_class_padre = b.id_classificazione
         LEFT JOIN v_classificazione_voci c ON b.id_class_padre = c.id_classificazione
         LEFT JOIN v_classificazione_voci d ON c.id_class_padre = d.id_classificazione
         LEFT JOIN v_classificazione_voci e ON d.id_class_padre = e.id_classificazione
         LEFT JOIN v_classificazione_voci f ON e.id_class_padre = f.id_classificazione
         LEFT JOIN v_classificazione_voci g ON f.id_class_padre = g.id_classificazione;

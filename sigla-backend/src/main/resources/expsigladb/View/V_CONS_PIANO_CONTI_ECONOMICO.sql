--------------------------------------------------------
--  DDL for View V_CONS_PIANO_CONTI_ECONOMICO
--------------------------------------------------------

CREATE OR REPLACE FORCE VIEW "V_CONS_PIANO_CONTI_ECONOMICO" ("ESERCIZIO", "TIPO", "TIPOREC", "NR_LIVELLO", "CD_CLASSIFICAZIONE", "DS_CLASSIFICAZIONE") AS
  SELECT x.ESERCIZIO, x.tipo, x.tiporec, x.NR_LIVELLO, x.cd_classificazione, x.DS_CLASSIFICAZIONE
FROM (
SELECT DISTINCT a.ESERCIZIO, a.tipo, 'CLA' tiporec, a.NR_LIVELLO, a.cd_classificazione, a.DS_CLASSIFICAZIONE
FROM v_classificazione_voci_ep a
UNION ALL
SELECT DISTINCT b.ESERCIZIO, a.tipo, 'DET' tiporec, a.NR_LIVELLO+1, a.cd_classificazione||'.'||b.cd_voce_ep, b.DS_VOCE_EP
FROM v_classificazione_voci_ep a, voce_ep b
WHERE b.id_classificazione = a.id_classificazione
UNION ALL
SELECT DISTINCT b.ESERCIZIO, a.tipo, 'DET' tiporec, a.NR_LIVELLO+1, a.cd_classificazione||'.'||b.cd_voce_ep, b.DS_VOCE_EP
FROM v_classificazione_voci_ep a, voce_ep b
WHERE b.id_classificazione_acc = a.id_classificazione
UNION ALL
SELECT DISTINCT b.ESERCIZIO, a.tipo, 'DET' tiporec, a.NR_LIVELLO+1, a.cd_classificazione||'.'||b.CD_SIOPE, b.DESCRIZIONE
FROM v_classificazione_voci_ep a, CODICI_SIOPE b
WHERE b.id_classificazione_siope = a.id_classificazione
UNION ALL
SELECT DISTINCT b.ESERCIZIO, a.tipo, 'DET' tiporec, a.NR_LIVELLO+1, a.cd_classificazione||'.'||b.CD_SIOPE, b.DESCRIZIONE
FROM v_classificazione_voci_ep a, CODICI_SIOPE b
WHERE b.id_classificazione_siope_rend = a.id_classificazione) X
order by 1,2,5;
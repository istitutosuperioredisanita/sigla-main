 CREATE OR REPLACE FORCE EDITIONABLE VIEW "V_SALDI_PLURIENNALI_VOCE_PROGETTO" ("PG_PROGETTO", "ESERCIZIO_VOCE", "TI_APPARTENENZA", "TI_GESTIONE", "CD_ELEMENTO_VOCE", "ESERCIZIO_PIANO", "IMPORTO_PLURIENNALE") AS
  (SELECT    b.pg_progetto,
             a.esercizio_voce, a.ti_appartenenza, a.ti_gestione, a.cd_voce,
             a.anno,
             SUM (a.importo) importo_pluriennalE
   FROM obbligazione_pluriennale_voce a inner join v_linea_attivita_valida b on a.cd_centro_responsabilita = b.cd_centro_responsabilita
                 AND a.cd_linea_attivita = b.cd_linea_attivita and a.esercizio = b.esercizio
   where a.autorimod='Y'
   GROUP BY b.pg_progetto, a.esercizio_voce, a.ti_appartenenza, a.ti_gestione, a.cd_voce,a.anno);

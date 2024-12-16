 CREATE OR REPLACE FORCE EDITIONABLE VIEW "V_SALDI_PLURIENNALI_VOCE_PROGETTO" ("PG_PROGETTO", "ESERCIZIO_VOCE", "TI_APPARTENENZA", "TI_GESTIONE", "CD_ELEMENTO_VOCE", "ESERCIZIO_PIANO", "IMPORTO_PLURIENNALE") AS
  (SELECT a.pg_progetto,
                a.esercizio_voce, a.ti_appartenenza, a.ti_gestione, a.cd_voce cd_elemento_voce,
                a.anno,
                SUM (a.importo) importo_pluriennale
                from (
   SELECT    b.pg_progetto,
                a.esercizio_voce, a.ti_appartenenza, a.ti_gestione, a.cd_voce,
                a.anno,
                a.importo
      FROM obbligazione_pluriennale_voce a inner join v_linea_attivita_valida b on a.cd_centro_responsabilita = b.cd_centro_responsabilita
                    AND a.cd_linea_attivita = b.cd_linea_attivita and a.esercizio = b.esercizio
      where a.autorimod='Y'
      and not EXISTS ( select *
      from obbligazione_pluriennale p
      where p.CD_CDS =a.cd_cds
      and p.ESERCIZIO=a.esercizio
      and p.ESERCIZIO_ORIGINALE=a.ESERCIZIO_ORIGINALE
      and p.PG_OBBLIGAZIONE =a.PG_OBBLIGAZIONE
      and p.cd_cds_rif is not null
      and p.esercizio_rif is not null
      and NVL(p.pg_obbligazione_rif,-1) is not null)
      union all SELECT b.pg_progetto,
                a.esercizio_voce, a.ti_appartenenza, a.ti_gestione, a.cd_voce,
                a.anno,
                a.importo
      FROM obbligazione_pluriennale_voce a
       inner join  obbligazione_pluriennale p
      on  p.CD_CDS =a.cd_cds
      and p.ESERCIZIO=a.esercizio
      and p.ESERCIZIO_ORIGINALE=a.ESERCIZIO_ORIGINALE
      and p.PG_OBBLIGAZIONE =a.PG_OBBLIGAZIONE
      and p.anno=a.anno
      inner join v_linea_attivita_valida b on a.cd_centro_responsabilita = b.cd_centro_responsabilita
                    AND a.cd_linea_attivita = b.cd_linea_attivita and a.esercizio = b.esercizio
      where a.autorimod='Y'
      and DECODE( p.cd_cds_rif,-1,null) is not null
      and DECODE(p.esercizio_rif ,-1,null) is not null
      and DECODE(p.pg_obbligazione_rif,-1,null) is not null) a
       GROUP BY a.pg_progetto, a.esercizio_voce, a.ti_appartenenza, a.ti_gestione, a.cd_voce,a.anno);

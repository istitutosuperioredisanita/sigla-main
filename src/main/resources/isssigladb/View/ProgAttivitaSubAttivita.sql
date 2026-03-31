CREATE OR REPLACE FORCE VIEW PROGATTIVITASUBATTIVITA AS
        SELECT
                A.PG_PROGETTO AS IdAccordo,
      		A.CD_PROGETTO AS Fascicolo,
                B.DT_INIZIO AS Inizio,
                B.DT_FINE AS Scadenza,
                B.DT_PROROGA AS UltimaProroga,
                ADD_MONTHS(b.DT_FINE, 2) AS ScadenzaDifferita,
                ADD_MONTHS(b.DT_PROROGA, 2) AS UltimaProrogaDifferita,
                A.DS_PROGETTO AS Titolo,
                A.CD_RESPONSABILE_TERZO AS IdPersResponsabile,
                SUBSTR(C.CD_PRECEDENTE, 10) AS MatricolaResp,
                ana.NOME AS NomeResp,
                ana.COGNOME AS CognomeResp,
                1 AS IdAttivita,
                'ATTIVITA GENERICA SU PROG' AS DescrAttivita,
                1 AS IdSubAttivita,
                1 AS CodSubAttivita,
                'SUB ATTIVITA GENERICA SU PROG' AS DescrSubAttivita,
                ana.CODICE_FISCALE AS CodFiscaleResp,
                c.CD_ANAG AS CdAnagResp,
                cp.CD_CUP AS CUP
        FROM PROGETTO_SIP A
                INNER JOIN PROGETTO_OTHER_FIELD B ON A.PG_PROGETTO = B.PG_PROGETTO
                INNER JOIN TERZO C ON A.CD_RESPONSABILE_TERZO = C.CD_TERZO
                INNER JOIN ANAGRAFICO ana ON ana.CD_ANAG = c.CD_ANAG
                LEFT JOIN cupprogetti cp ON cp.ESERCIZIO = a.ESERCIZIO AND cp.PG_PROGETTO = a.PG_PROGETTO
        WHERE A.TIPO_FASE = 'G'
        ORDER BY 2;

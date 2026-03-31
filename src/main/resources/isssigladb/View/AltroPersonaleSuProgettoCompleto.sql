CREATE OR REPLACE FORCE VIEW ALTROPERSONALESUPROGETTOCOMPLETO AS
        SELECT pa.CD_ANAG AS IdPersona,
                p.IDACCORDO AS IdAccordo,
                SUBSTR(t.CD_PRECEDENTE, 10) AS MatricolaResp,
                ana.CODICE_FISCALE AS CodiceFiscale,
                t.DENOMINAZIONE_SEDE  AS NomeCognome,
                p.INIZIO as INIZIO,
                p.SCADENZA as SCADENZA,
                p.ULTIMAPROROGA as ULTIMAPROROGA,
                p.TITOLO as TITOLO,
                p.IDPERSRESPONSABILE,
                p.MATRICOLARESP AS MatricolaR,
               	ana.nome AS NomeResp,  -- TODO aggiornare campo
                ana.COGNOME  AS CognomeResp, -- TODO aggiornare campo
                p.IDATTIVITA as IDAttivita,
                p.DESCRATTIVITA as DESCRATTIVITA,
                p.IDSUBATTIVITA as IDSUBATTIVITA,
                p.CODSUBATTIVITA as CODSUBATTIVITA,
                p.DESCRSUBATTIVITA as DESCRSUBATTIVITA
        FROM PROGATTIVITASUBATTIVITA p
                JOIN PROGETTO_ANAGRAFICO pa ON p.IDACCORDO = pa.PG_PROGETTO
                JOIN terzo t ON pa.CD_ANAG = t.CD_ANAG
                JOIN ANAGRAFICO ana ON ana.CD_ANAG = t.CD_ANAG
        WHERE ana.CD_ANAG != p.CDANAGRESP
        ORDER BY 2;

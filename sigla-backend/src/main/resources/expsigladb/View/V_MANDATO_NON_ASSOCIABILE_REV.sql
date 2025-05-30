--------------------------------------------------------
--  DDL for View V_MANDATO_NON_ASSOCIABILE_REV
--------------------------------------------------------

  CREATE OR REPLACE FORCE VIEW "V_MANDATO_NON_ASSOCIABILE_REV" ("CD_CDS", "ESERCIZIO", "PG_MANDATO") AS 
  SELECT
--==============================================================================
--
-- Date: 26/05/2003
-- Version: 1.1
--
-- Vista estrazione mandati non associabili a reversali manualmente
--  Mandati di reintegro del fondo economale
--  Mandati di accreditamento e regolarizzazione
--  Mandati su fatture passive con recupero iva (reversale)
--
-- History:
-- Date: 03/02/2003
-- Version: 1.0
-- Creazione
--
-- Date: 26/05/2003
-- Version: 1.1
-- Aggiunto l'ulteriore controllo che il mandato non si a di accreditamento o regolarizzazione o
-- recupero iva
--
-- Date: 30/05/2025
-- Version: 1.2
-- Eliminato il controllo che il mandato sia legato ad una reversale iva
-- Body:
--
--==============================================================================
          m.cd_cds, m.esercizio, m.pg_mandato
     FROM mandato m
    WHERE
          -- Mandati di reintegro del fondo economale
          EXISTS (
             SELECT 1
               FROM fondo_spesa
              WHERE cd_cds_mandato = m.cd_cds
                AND esercizio_mandato = m.esercizio
                AND pg_mandato = m.pg_mandato)
       -- Mandati di accreditamento e regolarizzazione
       OR m.ti_mandato IN ('A', 'R');

   COMMENT ON TABLE "V_MANDATO_NON_ASSOCIABILE_REV"  IS 'Vista estrazione mandati non associabili a reversali manualmente';

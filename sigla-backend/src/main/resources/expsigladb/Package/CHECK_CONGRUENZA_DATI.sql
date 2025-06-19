create or replace PACKAGE "CHECK_CONGRUENZA_DATI" AS
-- =================================================================================================
--
-- ACCONTO - Gestione Acconto per le addizionali
--
-- Date: 17/06/2025
-- Version: 1.0
--
-- Dependency:
--
-- History:
--
--
--
-- Rilascio check congruenza dati.
--
-- =================================================================================================
--
-- Constants
--

--
-- Variabili globali
--


-- Dichiarazione di un cursore generico

--
-- Functions e Procedures
--

----------------------------------------------------------------------------------------------------
-- ROUTINE COMUNI
----------------------------------------------------------------------------------------------------

    TIPO_LOG_CONGR_DATI CONSTANT VARCHAR2(20):='CONGR_DATI';

  Procedure job_check_congruenza_dati(job number, pg_exec number, next_date date);

END CHECK_CONGRUENZA_DATI;
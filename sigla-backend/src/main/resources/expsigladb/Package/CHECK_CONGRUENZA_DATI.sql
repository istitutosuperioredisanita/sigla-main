CREATE OR REPLACE PACKAGE CHECK_CONGRUENZA_DATI AS
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
-- Rilascio check congruenza dati.
----------------------------------------------------------------------------------------------------
-- ROUTINE COMUNI
----------------------------------------------------------------------------------------------------
  TIPO_LOG_CONGR_DATI CONSTANT VARCHAR2(20):='CONGR_DATI';
  TIPO_LOG_QUADR_COGEINV CONSTANT VARCHAR2(20):='QUADR_COGEINV';

  Procedure job_check_congruenza_dati(job number, pg_exec number, next_date date);
  Procedure job_check_quadratura_coge_inv(job number, pg_exec number, next_date date, aEs number, aContoEp VARCHAR2);
  Procedure job_check_quadratura_coge_inv2(aEs number, aContoEp VARCHAR2);
END CHECK_CONGRUENZA_DATI;
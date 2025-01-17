--------------------------------------------------------
--  DDL for View V_PARTITARIO
--------------------------------------------------------
  CREATE OR REPLACE FORCE VIEW "V_PARTITARIO" (
    "PG_SCRITTURA",
    "DT_CONTABILIZZAZIONE",
    "DS_SCRITTURA",
    "ESERCIZIO",
    "PG_MOVIMENTO",
    "CD_UNITA_ORGANIZZATIVA",
    "CD_CDS",
    "CD_RIGA",
    "SEZIONE",
    "CD_VOCE_EP",
    "CD_TERZO",
    "TI_RIGA",
    "CD_CONTRIBUTO_RITENUTA",
    "ESERCIZIO_DOCUMENTO",
    "CD_TIPO_DOCUMENTO",
    "CD_CDS_DOCUMENTO",
    "CD_UO_DOCUMENTO",
    "PG_NUMERO_DOCUMENTO",
    "IM_MOVIMENTO_DARE",
    "IM_MOVIMENTO_AVERE",
    "DIFFERENZA"
  ) AS SELECT
--
-- Date: 11/09/2022
-- Version: 1.0
--
-- Vista di estrazione delle Partite di Economico Patrimoniale
--
-- History:
--
-- Date: 11/09/2022
-- Version: 1.0
-- Creazione
--
-- Body:
--
MOVIMENTO_COGE.PG_SCRITTURA,
SCRITTURA_PARTITA_DOPPIA.DT_CONTABILIZZAZIONE,
SCRITTURA_PARTITA_DOPPIA.DS_SCRITTURA,
MOVIMENTO_COGE.ESERCIZIO,
MOVIMENTO_COGE.PG_MOVIMENTO,
MOVIMENTO_COGE.CD_UNITA_ORGANIZZATIVA,
MOVIMENTO_COGE.CD_CDS,
'D' CD_RIGA,
MOVIMENTO_COGE.SEZIONE,
MOVIMENTO_COGE.CD_VOCE_EP,
MOVIMENTO_COGE.CD_TERZO,
MOVIMENTO_COGE.TI_RIGA,
NVL(MOVIMENTO_COGE.CD_CONTRIBUTO_RITENUTA, ' '),
MOVIMENTO_COGE.ESERCIZIO_DOCUMENTO,
MOVIMENTO_COGE.CD_TIPO_DOCUMENTO,
MOVIMENTO_COGE.CD_CDS_DOCUMENTO,
MOVIMENTO_COGE.CD_UO_DOCUMENTO,
MOVIMENTO_COGE.PG_NUMERO_DOCUMENTO,
Decode (SEZIONE,'D',IM_MOVIMENTO,null) IM_MOVIMENTO_DARE,
Decode (SEZIONE,'A',IM_MOVIMENTO,null) IM_MOVIMENTO_AVERE,
NULL DIFFERENZA
FROM MOVIMENTO_COGE, SCRITTURA_PARTITA_DOPPIA WHERE
( SCRITTURA_PARTITA_DOPPIA.ESERCIZIO=MOVIMENTO_COGE.ESERCIZIO ) AND
( SCRITTURA_PARTITA_DOPPIA.CD_CDS=MOVIMENTO_COGE.CD_CDS ) AND
( SCRITTURA_PARTITA_DOPPIA.PG_SCRITTURA=MOVIMENTO_COGE.PG_SCRITTURA ) AND
( SCRITTURA_PARTITA_DOPPIA.CD_UNITA_ORGANIZZATIVA=MOVIMENTO_COGE.CD_UNITA_ORGANIZZATIVA )
 UNION ALL (
SELECT
MAX(MOVIMENTO_COGE.PG_SCRITTURA),
MAX(SCRITTURA_PARTITA_DOPPIA.DT_CONTABILIZZAZIONE),
MAX(SCRITTURA_PARTITA_DOPPIA.DS_SCRITTURA),
MAX(MOVIMENTO_COGE.ESERCIZIO),
MAX(MOVIMENTO_COGE.PG_MOVIMENTO),
MAX(MOVIMENTO_COGE.CD_UNITA_ORGANIZZATIVA),
MAX(MOVIMENTO_COGE.CD_CDS),
'T' CD_RIGA,
NULL SEZIONE,
NULL CD_VOCE_EP,
MAX(MOVIMENTO_COGE.CD_TERZO) CD_TERZO,
MAX('SALDO') TI_RIGA,
NVL(MOVIMENTO_COGE.CD_CONTRIBUTO_RITENUTA, ' '),
MOVIMENTO_COGE.ESERCIZIO_DOCUMENTO,
MOVIMENTO_COGE.CD_TIPO_DOCUMENTO,
MOVIMENTO_COGE.CD_CDS_DOCUMENTO,
MOVIMENTO_COGE.CD_UO_DOCUMENTO,
MOVIMENTO_COGE.PG_NUMERO_DOCUMENTO,
NULL IM_MOVIMENTO_DARE,
NULL IM_MOVIMENTO_AVERE,
SUM(Decode (SEZIONE,'D',NVL(IM_MOVIMENTO,0),NVL(-IM_MOVIMENTO,0))) DIFFERENZA
FROM MOVIMENTO_COGE, SCRITTURA_PARTITA_DOPPIA WHERE
( SCRITTURA_PARTITA_DOPPIA.ESERCIZIO=MOVIMENTO_COGE.ESERCIZIO ) AND
( SCRITTURA_PARTITA_DOPPIA.CD_CDS=MOVIMENTO_COGE.CD_CDS ) AND
( SCRITTURA_PARTITA_DOPPIA.PG_SCRITTURA=MOVIMENTO_COGE.PG_SCRITTURA ) AND
( SCRITTURA_PARTITA_DOPPIA.CD_UNITA_ORGANIZZATIVA=MOVIMENTO_COGE.CD_UNITA_ORGANIZZATIVA ) AND
( SCRITTURA_PARTITA_DOPPIA.DT_CANCELLAZIONE IS NULL)
 GROUP BY MOVIMENTO_COGE.ESERCIZIO_DOCUMENTO,MOVIMENTO_COGE.CD_TIPO_DOCUMENTO, MOVIMENTO_COGE.CD_CDS_DOCUMENTO,MOVIMENTO_COGE.CD_UO_DOCUMENTO,MOVIMENTO_COGE.PG_NUMERO_DOCUMENTO, MOVIMENTO_COGE.CD_CONTRIBUTO_RITENUTA
 );

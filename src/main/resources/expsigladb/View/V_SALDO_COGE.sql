--------------------------------------------------------
--  DDL for View V_SALDO_COGE
--------------------------------------------------------
  CREATE OR REPLACE FORCE VIEW "V_SALDO_COGE" ("CD_CDS", "ESERCIZIO", "CD_UNITA_ORGANIZZATIVA", "CD_TERZO", "TI_ISTITUZ_COMMERC", "CD_VOCE_EP", "NATURA_VOCE", "RIEPILOGA_A", "TOT_DARE", "TOT_AVERE") AS
                                 SELECT MOVIMENTO_COGE.CD_CDS, MOVIMENTO_COGE.ESERCIZIO, MOVIMENTO_COGE.CD_UNITA_ORGANIZZATIVA,
                               MOVIMENTO_COGE.CD_TERZO, MOVIMENTO_COGE.TI_ISTITUZ_COMMERC,
                               MOVIMENTO_COGE.CD_VOCE_EP, VOCE_EP.NATURA_VOCE, VOCE_EP.RIEPILOGA_A,
                               SUM(CASE WHEN MOVIMENTO_COGE.SEZIONE='D' THEN MOVIMENTO_COGE.IM_MOVIMENTO ELSE 0 END ) TOT_DARE,
                               SUM(CASE WHEN MOVIMENTO_COGE.SEZIONE='A' THEN MOVIMENTO_COGE.IM_MOVIMENTO ELSE 0 END ) TOT_AVERE
                               FROM
                                      MOVIMENTO_COGE,
                                      SCRITTURA_PARTITA_DOPPIA,
                                      VOCE_EP WHERE
                               ( SCRITTURA_PARTITA_DOPPIA.ESERCIZIO=MOVIMENTO_COGE.ESERCIZIO ) AND
                               ( SCRITTURA_PARTITA_DOPPIA.CD_CDS=MOVIMENTO_COGE.CD_CDS ) AND
                               ( SCRITTURA_PARTITA_DOPPIA.PG_SCRITTURA=MOVIMENTO_COGE.PG_SCRITTURA ) AND
                               ( SCRITTURA_PARTITA_DOPPIA.CD_UNITA_ORGANIZZATIVA=MOVIMENTO_COGE.CD_UNITA_ORGANIZZATIVA ) AND
                               ( MOVIMENTO_COGE.ESERCIZIO=VOCE_EP.ESERCIZIO ) AND
                               ( MOVIMENTO_COGE.CD_VOCE_EP=VOCE_EP.CD_VOCE_EP ) AND
                                      ( SCRITTURA_PARTITA_DOPPIA.STATO = 'D' ) AND
                                      ( SCRITTURA_PARTITA_DOPPIA.ATTIVA = 'Y' ) AND
                                      ( MOVIMENTO_COGE.STATO = 'D' )
                               GROUP BY MOVIMENTO_COGE.CD_CDS, MOVIMENTO_COGE.ESERCIZIO, MOVIMENTO_COGE.CD_UNITA_ORGANIZZATIVA,
                               MOVIMENTO_COGE.CD_TERZO, MOVIMENTO_COGE.TI_ISTITUZ_COMMERC,
                               MOVIMENTO_COGE.CD_VOCE_EP, VOCE_EP.NATURA_VOCE, VOCE_EP.RIEPILOGA_A;

 COMMENT ON TABLE "V_SALDO_COGE"  IS 'Ritorna per ogni conto/terzo/uo il saldo dare/avere.';
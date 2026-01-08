CREATE OR REPLACE FUNCTION cnrctb550_copiacompenso(inCdsCompenso text,
       inUoCompenso text,
       inEsercizioCompenso bigint,
       inPgCompenso bigint,
       inCopiaCdsCompenso text,
       inCopiaUoCompenso text,
       inCopiaEsercizioCompenso bigint,
       inCopiaPgCompenso bigint)
  RETURNS void AS
$BODY$
BEGIN

END;

$BODY$
LANGUAGE plpgsql VOLATILE SECURITY DEFINER
  COST 100;
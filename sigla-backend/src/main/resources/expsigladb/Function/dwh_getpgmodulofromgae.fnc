CREATE OR REPLACE FUNCTION DWH_GETPGMODULOFROMGAE(IN_CDR IN VARCHAR2, IN_GAE VARCHAR2)
  RETURN NUMBER Is
   PG_MODULO_OUT  NUMBER;
Begin
    SELECT PG_PROGETTO
    INTO   PG_MODULO_OUT
    FROM   LINEA_ATTIVITA
    WHERE  CD_CENTRO_RESPONSABILITA = IN_CDR And
           CD_LINEA_ATTIVITA = IN_GAE;
    RETURN PG_MODULO_OUT;
END DWH_GETPGMODULOFROMGAE;
/



--------------------------------------------------------
--  DDL for View V_PROGETTO_PADRE_DET
--------------------------------------------------------

  CREATE OR REPLACE FORCE VIEW "V_PROGETTO_PADRE_DET"
    (
    ESERCIZIO,
    CD_PROGETTO,
    CD_PROGETTO_PADRE,
    CD_UNITA_ORGANIZZATIVA,
    DS_PROGETTO,
    IM_FINANZIATO,
    IM_COFINANZIATO,
    DT_INIZIO,
    DT_FINE,
    DT_PROROGA,
    STATO,
    TIPO_PROGETTO,
    DS_TIPO_PROGETTO,
    FINANZIATORE_TERZO,
    DENOMINAZIONE_SEDE,
    IM_FINANZIATO_FINANZIATORE
)
AS
SELECT
       V_PROGETTO_PADRE.ESERCIZIO,
       V_PROGETTO_PADRE.CD_PROGETTO,
       V_PROGETTO_PADRE.P_CD_PROGETTO,
       V_PROGETTO_PADRE.CD_UNITA_ORGANIZZATIVA,
       V_PROGETTO_PADRE.DS_PROGETTO,
       V_PROGETTO_PADRE.IM_FINANZIATO_OTHER_FIELD,
       V_PROGETTO_PADRE.IM_COFINANZIATO_OTHER_FIELD,
       V_PROGETTO_PADRE.DT_INIZIO_OTHER_FIELD,
       V_PROGETTO_PADRE.DT_FINE_OTHER_FIELD,
       V_PROGETTO_PADRE.DT_PROROGA_OTHER_FIELD,
       V_PROGETTO_PADRE.STATO_OTHER_FIELD,
       tp.cd_tipo_progetto tipo_progetto,
       tp.ds_tipo_progetto,
       f.cd_finanziatore_terzo,
       t.denominazione_sede,
       f.importo_finanziato
FROM 	V_PROGETTO_PADRE
            inner  join tipo_progetto tp    on V_PROGETTO_PADRE.cd_tipo_progetto=tp.cd_tipo_progetto
            left outer join PROGETTO_FINANZIATORE f on f.pg_progetto=V_PROGETTO_PADRE.pg_progetto
            left outer join TERZO t on    f.cd_finanziatore_terzo=t.cd_terzo
            left outer join anagrafico a  on a.cd_anag=t.cd_anag
WHERE
    ( V_PROGETTO_PADRE.LIVELLO = 2 ) AND
    ( V_PROGETTO_PADRE.TIPO_FASE = 'X' );


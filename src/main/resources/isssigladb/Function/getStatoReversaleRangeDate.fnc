create or replace FUNCTION getStatoReversaleRangeDate
--==================================================================================================
--
-- Date: 04/09/2023
-- Version: 1.0
--
-- Ritorna lo stato di una reversale nell'intevallo delle date fornito in input

-- History:
-- Date: 04/09/2023
-- Version: 1.0
-- Creazione funzione
--

-- Body:
--
--==================================================================================================
    (esercizio_in reversale.esercizio%TYPE,
        cd_cds_in reversale.cd_cds%TYPE,
        pg_reversale_in reversale.pg_reversale%TYPE,
        stato_cur reversale.stato%TYPE,
        data_da DATE,
        data_a DATE)
       RETURN reversale.stato%TYPE IS
       stato_ret reversale.stato%TYPE;
       data_inc sospeso.dt_registrazione%TYPE;
       dt_annullamento_storno siope_plus_esito.dt_ora_esito_operazione%type;
       checkIncasso BOOLEAN;
   Begin
       stato_ret:=stato_cur;
       checkIncasso:=TRUE;
     if ( stato_cur in ( 'A','P')) then
       begin
           begin
               select min( s.dt_registrazione) into data_inc from sospeso_det_etr a inner join sospeso s
               on a.CD_CDS=s.CD_CDS
               and a.ESERCIZIO=s.Esercizio
               and a.TI_ENTRATA_SPESA =s.TI_ENTRATA_SPESA
               and a.TI_SOSPESO_RISCONTRO=s.TI_SOSPESO_RISCONTRO
               and a.CD_SOSPESO=s.CD_SOSPESO
               where  a.esercizio=esercizio_in
               and a.cd_cds=cd_cds_in
               and a.pg_reversale=pg_reversale_in
               and dt_registrazione between data_da and data_a;
               Exception
                 When no_data_found then
                    data_inc:=null;
           End;
           if ( stato_cur in ( 'A')) then
               checkIncasso:=false;
                 begin
                       begin
                           select dt_ora_esito_operazione  into dt_annullamento_storno from siope_plus_esito
                           where  esercizio_reversale=esercizio_in
                           and cd_cds_reversale=cd_cds_in
                           and pg_reversale=pg_reversale_in
                           and esito_operazione='ANNULLATO'
                            and TRUNC(dt_ora_esito_operazione) between data_da and data_a;
                           Exception
                             When no_data_found then
                                dt_annullamento_storno:=null;
                       End;
                       if ( dt_annullamento_storno is null or ( dt_annullamento_storno!=null and dt_annullamento_storno>data_a)) then
                           checkIncasso:=true;
                       end if;
                 end;
           end if;--if ( stato_cur in ( 'A')) then

           if ( checkIncasso) then
                 stato_ret:='P';
                if ( data_inc is not null and data_inc>data_a) then
                       stato_ret:='E';
                   end if;
           end if;
       end;
     end if;--if ( stato_cur in ( 'A','P')) then

     return stato_ret;
   End;
/

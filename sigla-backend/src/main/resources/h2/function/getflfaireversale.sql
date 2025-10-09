DROP ALIAS IF EXISTS getFlFaiReversale;
CREATE ALIAS getFlFaiReversale AS $$
String execute(String aTiFattura, String aTiIstituzCommerc, String aTiBeneServizio, String aFlSanMarinoSenzaIva,
String aFlIntraUe, String aFlSplitPayment, String aflregistro) throws Exception {
   String aValoreRitorno = String.valueOf('N');
   if (String.valueOf('F').equals(aTiFattura) && String.valueOf('I').equals(aTiIstituzCommerc) && String.valueOf('B').equals(aTiBeneServizio) &&
       ((String.valueOf('Y').equals(aFlSanMarinoSenzaIva) || String.valueOf('Y').equals(aFlIntraUe)) && !String.valueOf('*').equals(aflregistro))) {
       aValoreRitorno = String.valueOf('Y');
       //Tipo sezionale FL_SERVIZI_NON_RESIDENTI = Si
   } else if (String.valueOf('F').equals(aTiFattura) && String.valueOf('I').equals(aTiIstituzCommerc) && String.valueOf('Y').equals(aFlSplitPayment)) {
       aValoreRitorno = String.valueOf('Y');
   } else if (String.valueOf('F').equals(aTiFattura) && String.valueOf('Y').equals(aflregistro)) {
       aValoreRitorno = String.valueOf('Y');
   }
   return aValoreRitorno;
}
$$;

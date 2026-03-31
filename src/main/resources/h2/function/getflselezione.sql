DROP ALIAS IF EXISTS getFlSelezione;
CREATE ALIAS getFlSelezione AS $$
String execute(String aTipoDocAmm, String aStatoPagamentoFondoEco, String aTiFattura,
java.math.BigDecimal aPgLettera, String aCdSospeso, String isMissioneAssCompenso,
java.math.BigDecimal aImportoMissione, java.math.BigDecimal aImportoAnticipo,
java.math.BigDecimal aImportoRimborso, String aFlCongelata, String aStatoLiquidazione) throws Exception {
    String isSelezionePerMandato = String.valueOf('Y');
    String isNotSelezionePerMandato = String.valueOf('N');

    // 1. MISSIONE
    if ("MISSIONE".equalsIgnoreCase(aTipoDocAmm)) {
        if (
            (aImportoMissione != null && aImportoAnticipo != null &&
             aImportoMissione.subtract(aImportoAnticipo).compareTo(java.math.BigDecimal.ZERO) < 0)
            || String.valueOf('Y').equalsIgnoreCase(isMissioneAssCompenso)
        ) {
            return isNotSelezionePerMandato;
        }
    }

    // 2. ANTICIPO
    else if ("ANTICIPO".equalsIgnoreCase(aTipoDocAmm)) {
        if (aImportoRimborso != null && aImportoRimborso.compareTo(java.math.BigDecimal.ZERO) > 0) {
            return isNotSelezionePerMandato;
        }
    }

    // 3. FATTURA_P o DOC_GENERICO
    else if ("FATTURA_P".equalsIgnoreCase(aTipoDocAmm) || "DOC_GENERICO".equalsIgnoreCase(aTipoDocAmm)) {
        if (aPgLettera != null && (aCdSospeso == null || aCdSospeso.trim().isEmpty())) {
            return isNotSelezionePerMandato;
        }

        if ("FATTURA_P".equalsIgnoreCase(aTipoDocAmm)) {
            if (!String.valueOf('F').equalsIgnoreCase(aTiFattura) || String.valueOf('Y').equalsIgnoreCase(aFlCongelata)) {
                return isNotSelezionePerMandato;
            }
        }
    }

    // 4. Stato Liquidazione non LIQ
    if (aStatoLiquidazione != null && !"LIQ".equalsIgnoreCase(aStatoLiquidazione)) {
        return isNotSelezionePerMandato;
    }

    // 5. Stato Pagamento Fondo Economale
    if (String.valueOf('N').equalsIgnoreCase(aStatoPagamentoFondoEco)) {
        return isSelezionePerMandato;
    } else {
        return isNotSelezionePerMandato;
    }
}
$$;
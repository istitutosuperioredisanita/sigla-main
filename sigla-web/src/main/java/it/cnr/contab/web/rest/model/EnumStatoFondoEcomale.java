package it.cnr.contab.web.rest.model;

import it.cnr.contab.docamm00.docs.bulk.Documento_genericoBulk;
import it.cnr.contab.util.ApplicationMessageFormatException;
import it.cnr.jada.DetailedRuntimeException;

public enum EnumStatoFondoEcomale {
    NON_USARE_FONDO_ECONOMALE(Documento_genericoBulk.NO_FONDO_ECO),
    USA_FONDO_ECONOMALE( Documento_genericoBulk.FONDO_ECO);

    public String statoFondoEconomale;

    public String getStatoFondoEconomale() {
        return statoFondoEconomale;
    }

    private EnumStatoFondoEcomale(String statoFondoEconomale) {
        this.statoFondoEconomale = statoFondoEconomale;
    }

    public static EnumStatoFondoEcomale getValueFrom(String statoFondoEconomale) {
        for (EnumStatoFondoEcomale enumStatoFondoEcomale : EnumStatoFondoEcomale.values()) {
            if (enumStatoFondoEcomale.statoFondoEconomale.equals(statoFondoEconomale))
                return enumStatoFondoEcomale;
        }
        throw new DetailedRuntimeException(
                new ApplicationMessageFormatException("Fondo Economale non trovato {0}!", statoFondoEconomale)
        );
    }
}

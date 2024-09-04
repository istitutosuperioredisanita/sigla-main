package it.cnr.contab.web.rest.model;

import it.cnr.contab.docamm00.docs.bulk.IDocumentoAmministrativoBulk;
import it.cnr.contab.util.ApplicationMessageFormatException;
import it.cnr.jada.DetailedRuntimeException;

public enum EnumStatoLiqDocumentoGen {

    LIQUIDABILE(IDocumentoAmministrativoBulk.LIQ),
    NON_LIQUIDABILE( IDocumentoAmministrativoBulk.NOLIQ),
    LIQUIDAZIONE_SOSPESA(IDocumentoAmministrativoBulk.SOSP);

    public String stato_liquidazione;

    public String getStato_liquidazione() {
        return stato_liquidazione;
    }

    private EnumStatoLiqDocumentoGen(String stato_liquidazione) {
        this.stato_liquidazione = stato_liquidazione;
    }

    public static EnumStatoLiqDocumentoGen getValueFrom(String stato_liquidazione) {
        for (EnumStatoLiqDocumentoGen enumStatoLiqDocumentoGen : EnumStatoLiqDocumentoGen.values()) {
            if (enumStatoLiqDocumentoGen.stato_liquidazione.equals(stato_liquidazione))
                return enumStatoLiqDocumentoGen;
        }
        throw new DetailedRuntimeException(
                new ApplicationMessageFormatException("Stato Liquidazione non trovato {0}!", stato_liquidazione)
        );
    }
}

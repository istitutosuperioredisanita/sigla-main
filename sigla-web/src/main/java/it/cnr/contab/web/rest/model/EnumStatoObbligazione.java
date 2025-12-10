package it.cnr.contab.web.rest.model;

import io.swagger.v3.oas.annotations.media.Schema;
import it.cnr.contab.doccont00.core.bulk.ObbligazioneBulk;
import it.cnr.contab.util.ApplicationMessageFormatException;
import it.cnr.jada.DetailedRuntimeException;

@Schema( description = "Il tipo di gestione Spesa/Entrata o Entrambe")
public enum EnumStatoObbligazione {
    PROVVISORIO(ObbligazioneBulk.STATO_OBB_PROVVISORIO),
    DEFINITIVO(ObbligazioneBulk.STATO_OBB_DEFINITIVO),
    STORNATO(ObbligazioneBulk.STATO_OBB_STORNATO);

    public String stato;

    public String getStato() {
        return this.stato;
    }
    private EnumStatoObbligazione(String stato) {
        this.stato = stato;
    }

    public static EnumStatoObbligazione getValueFrom(String stato) {
        for (EnumStatoObbligazione enumStatoObbligazione : EnumStatoObbligazione.values()) {
            if (enumStatoObbligazione.stato.equals(stato))
                return enumStatoObbligazione;
        }
        throw new DetailedRuntimeException(
                new ApplicationMessageFormatException("Stato Obbligazione non trovato {0}!", stato)
        );
    }


}

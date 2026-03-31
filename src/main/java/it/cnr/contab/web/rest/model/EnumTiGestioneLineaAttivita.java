package it.cnr.contab.web.rest.model;

import org.eclipse.microprofile.openapi.annotations.media.Schema;
import it.cnr.contab.config00.latt.bulk.WorkpackageBulk;
import it.cnr.contab.util.ApplicationMessageFormatException;
import it.cnr.jada.DetailedRuntimeException;

@Schema(name = "Il tipo di gestione Spesa/Entrata o Entrambe")
public enum EnumTiGestioneLineaAttivita {
    ENTRATA(WorkpackageBulk.TI_GESTIONE_ENTRATE),
    SPESA(WorkpackageBulk.TI_GESTIONE_SPESE),
    ENTRAMBE(WorkpackageBulk.TI_GESTIONE_ENTRAMBE);

    public String gestione;

    public String getGestione() {
        return this.gestione;
    }
    private EnumTiGestioneLineaAttivita(String gestione) {
        this.gestione = gestione;
    }

    public static EnumTiGestioneLineaAttivita getValueFrom(String gestione) {
        for (EnumTiGestioneLineaAttivita enumTiGestioneLineaAttivita : EnumTiGestioneLineaAttivita.values()) {
            if (enumTiGestioneLineaAttivita.gestione.equals(gestione))
                return enumTiGestioneLineaAttivita;
        }
        throw new DetailedRuntimeException(
                new ApplicationMessageFormatException("Tipo Gestione non trovato {0}!", gestione)
        );
    }

    public boolean isSpesa(){
        return SPESA.getGestione().equals(this.getGestione()) || ENTRAMBE.getGestione().equals(this.getGestione());
    }
    public boolean isEntrata(){
        return ENTRATA.getGestione().equals(this.getGestione()) || ENTRAMBE.getGestione().equals(this.getGestione());
    }

}

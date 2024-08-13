package it.cnr.contab.web.rest.model;

import io.swagger.annotations.ApiModel;
import it.cnr.contab.config00.latt.bulk.WorkpackageBulk;
@ApiModel( "Il tipo di gestione Spesa/Entrata o Entrambe")
public enum EnumTiGestioneLineaAttivita {
    ENTRATA(WorkpackageBulk.TI_GESTIONE_ENTRATE),
    SPESA(WorkpackageBulk.TI_GESTIONE_SPESE),
    ENTRAMBE(WorkpackageBulk.TI_GESTIONE_ENTRAMBE);

    public String gestione;

    private EnumTiGestioneLineaAttivita(String gestione) {
        this.gestione = gestione;
    }


}

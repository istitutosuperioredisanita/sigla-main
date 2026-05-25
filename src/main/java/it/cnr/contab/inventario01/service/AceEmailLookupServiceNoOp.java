package it.cnr.contab.inventario01.service;

import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.jada.comp.ComponentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AceEmailLookupServiceNoOp implements AceEmailLookupService {

    private static final Logger log =
            LoggerFactory.getLogger(AceEmailLookupServiceNoOp.class);

    @Override
    public String getEmailByTerzo(TerzoBulk terzo) throws ComponentException {
        log.warn(
                "ACE non disponibile: impossibile recuperare email per terzo={}",
                terzo != null ? terzo.getCd_terzo() : null
        );

        throw new ComponentException(
                "ACE non disponibile. Verificare che il profilo iss sia attivo e che ace-iss-client sia nel WAR."
        );
    }
}
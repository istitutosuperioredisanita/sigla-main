package it.cnr.contab.web.rest.local.config00;

import jakarta.ejb.Remote;

@Remote
public interface ContextRemote {
    Integer getLiquibasBootstrapEsercizio();
}

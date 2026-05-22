package it.cnr.contab.inventario01.service;

import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.jada.comp.ComponentException;

public interface AceEmailLookupService {

    String getEmailByTerzo(TerzoBulk terzo) throws ComponentException;
}
/*
 * Copyright (C) 2020  Consiglio Nazionale delle Ricerche
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package it.cnr.contab.inventario01.bulk;

import it.cnr.contab.inventario01.service.DocTraspRientCMISService;
import it.cnr.contab.util00.bulk.storage.AllegatoGenericoBulk;
import it.cnr.jada.bulk.ValidationException;
import it.cnr.jada.util.OrderedHashtable;
import it.cnr.si.spring.storage.annotation.StoragePolicy;
import it.cnr.si.spring.storage.annotation.StorageProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AllegatoDocTraspRientBulk extends AllegatoGenericoBulk {
    private static final long serialVersionUID = 1L;

    private Doc_trasporto_rientroBulk docTrasportoRientro;

    public static OrderedHashtable aspectNamesKeys = new OrderedHashtable();

    static {
        aspectNamesKeys.put(DocTraspRientCMISService.ASPECT_ALLEGATI_DOC, "Allegati al Documento");
        aspectNamesKeys.put(DocTraspRientCMISService.ASPECT_STAMPA_DOC, "Stampa Documento");
    }

    private String aspectName;

    public AllegatoDocTraspRientBulk() {
        super();
        setAspectName(DocTraspRientCMISService.ASPECT_ALLEGATI_DOC);
    }

    public AllegatoDocTraspRientBulk(String storageKey) {
        super(storageKey);
        setAspectName(DocTraspRientCMISService.ASPECT_ALLEGATI_DOC);
    }

    public Doc_trasporto_rientroBulk getDocTrasportoRientro() {
        return docTrasportoRientro;
    }

    public void setDocTrasportoRientro(Doc_trasporto_rientroBulk docTrasportoRientro) {
        this.docTrasportoRientro = docTrasportoRientro;
    }

    public String getAspectName() {
        return aspectName;
    }

    public void setAspectName(String aspectName) {
        this.aspectName = aspectName;
    }

    /**
     * Ritorna lo stato del documento per il documentale CMIS
     */
    @StoragePolicy(name="P:doc_trasporto_rientro:stato", property=@StorageProperty(name="doc_trasporto_rientro:stato"))
    public String getStato() {
        if (Optional.ofNullable(docTrasportoRientro)
                .map(doc -> doc.getStato())
                .filter(s -> !(s.isEmpty()))
                .isPresent()) {
            return Doc_trasporto_rientroBulk.STATO.get(docTrasportoRientro.getStato()).toString();
        }
        return null;
    }

    /**
     * Ritorna la lista degli aspect CMIS da applicare al documento
     */
    @StorageProperty(name="cmis:secondaryObjectTypeIds")
    public List<String> getAspect() {
        List<String> results = new ArrayList<String>();
        results.add(getAspectName());
        return results;
    }

    public static OrderedHashtable getAspectNamesKeys() {
        return aspectNamesKeys;
    }

    /**
     * Validazione dell'allegato
     */
    @Override
    public void validate() throws ValidationException {
        if (getAspectName() == null) {
            throw new ValidationException("Attenzione: selezionare la tipologia di File!");
        }
        super.validate();
    }

    /**
     * Verifica se l'allegato esiste già nel documentale
     */
    public boolean isAllegatoEsistente() {
        if (this.isToBeCreated())
            return false;
        return true;
    }
}
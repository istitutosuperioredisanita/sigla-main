package it.cnr.contab.ordmag.ordini.bulk;

import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.util00.bulk.storage.AllegatoGenericoBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.ValidationException;
import it.cnr.jada.util.OrderedHashtable;
import it.cnr.si.spring.storage.annotation.StoragePolicy;
import it.cnr.si.spring.storage.annotation.StorageProperty;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AllegatoEvasioneOrdineBulk extends AllegatoGenericoBulk {
    private static final long serialVersionUID = 1L;

    public static OrderedHashtable aspectNamesKeys = new OrderedHashtable();

    public static final String P_SIGLA_EVASIONE_ATTACHMENT_ALTRO = "P:sigla_evasione_attachment:altro";
    public static final String P_SIGLA_EVASIONE_ATTACHMENT_DDT = "P:sigla_evasione_attachment:ddt";

    static {
        aspectNamesKeys.put(P_SIGLA_EVASIONE_ATTACHMENT_DDT, "Documento di Trasporto");
        aspectNamesKeys.put(P_SIGLA_EVASIONE_ATTACHMENT_ALTRO, "Altro");
    }

    private String aspectName;

    public AllegatoEvasioneOrdineBulk() {
        super();
    }

    public AllegatoEvasioneOrdineBulk(String storageKey) {
        super(storageKey);
    }

    public String getAspectName() {
        return aspectName;
    }

    public void setAspectName(String aspectName) {
        this.aspectName = aspectName;
    }

    public static OrderedHashtable getAspectNamesKeys() {
        return aspectNamesKeys;
    }

    @StorageProperty(name = "cmis:secondaryObjectTypeIds")
    public List<String> getAspect() {
        List<String> results = new ArrayList<String>();
        results.add("P:cm:titled");

        if (getAspectName() != null && !getAspectName().isEmpty()) {
            results.add(getAspectName());
        }

        return results;
    }

    @Override
    @StoragePolicy(name = "P:cm:titled", property = @StorageProperty(name = "cm:description"))
    public String getDescrizione() {
        return super.getDescrizione();
    }

    @Override
    public void setDescrizione(String descrizione) {
        super.setDescrizione(descrizione);
    }

    @Override
    @StorageProperty(name = "cmis:name")
    public String getNome() {
        return super.getNome();
    }

    @Override
    public void setNome(String nome) {
        super.setNome(nome);
    }

    @StoragePolicy(
            name = "P:sigla_commons_aspect:utente_applicativo_sigla",
            property = @StorageProperty(name = "sigla_commons_aspect:utente_applicativo")
    )
    private String utenteSIGLA;

    public String getUtenteSIGLA() {
        return utenteSIGLA;
    }

    public void setUtenteSIGLA(String utenteSIGLA) {
        this.utenteSIGLA = utenteSIGLA;
    }

    @Override
    public void complete(UserContext userContext) {
        setUtenteSIGLA(CNRUserContext.getUser(userContext));
        super.complete(userContext);
    }

    public boolean isAllegatoEsistente() {
        return !this.isToBeCreated();
    }

    @Override
    public void validate() throws ValidationException {

        if (isToBeCreated()) {

            File file = getFile();
            String fileName = (file != null ? file.getName() : "sconosciuto");

            if (file == null) {
                throw new ValidationException("Attenzione: selezionare un file!");
            }

            if (!file.exists() || file.length() == 0) {
                throw new ValidationException(
                        "Attenzione: il file selezionato (" + fileName + ") è vuoto o non accessibile!"
                );
            }

            if (getAspectName() == null || getAspectName().isEmpty()) {
                throw new ValidationException("Attenzione: selezionare la tipologia di File!");
            }

            if (getDescrizione() == null || getDescrizione().trim().isEmpty()) {
                throw new ValidationException("Attenzione: inserire la descrizione del File!");
            }
        }

        super.validate();
    }
}
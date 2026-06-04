package it.cnr.contab.pdg00.bulk;

import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.util00.bulk.storage.AllegatoGenericoBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.ValidationException;
import it.cnr.jada.util.OrderedHashtable;
import it.cnr.si.spring.storage.annotation.StoragePolicy;
import it.cnr.si.spring.storage.annotation.StorageProperty;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

public class AllegatoAccrualBulk extends AllegatoGenericoBulk {

    private static final long serialVersionUID = 1L;

    public static final String P_SIGLA_ACCRUAL_ATTACHMENT_XBRL_ZIP =
            "P:sigla_accrual_attachment:accrual_xbrl_zip";

    public static final String P_SIGLA_ACCRUAL_ATTACHMENT_ALTRO =
            "P:sigla_accrual_attachment:altro";

    public static OrderedHashtable aspectNamesKeys = new OrderedHashtable();

    static {
        aspectNamesKeys.put(
                P_SIGLA_ACCRUAL_ATTACHMENT_XBRL_ZIP,
                "Accrual XBRL ZIP"
        );

        aspectNamesKeys.put(
                P_SIGLA_ACCRUAL_ATTACHMENT_ALTRO,
                "Altro"
        );
    }

    private String aspectName;

    @StoragePolicy(
            name = "P:sigla_commons_aspect:utente_applicativo_sigla",
            property = @StorageProperty(
                    name = "sigla_commons_aspect:utente_applicativo"
            )
    )
    private String utenteSIGLA;

    public AllegatoAccrualBulk() {
        super();
    }

    public AllegatoAccrualBulk(String storageKey) {
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
        List<String> results = new ArrayList<>();

        results.add("P:cm:titled");

        if (getAspectName() != null && !getAspectName().isEmpty()) {
            results.add(getAspectName());
        }

        return results;
    }

    @Override
    @StoragePolicy(
            name = "P:cm:titled",
            property = @StorageProperty(name = "cm:description")
    )
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

            String fileName = getNome();

            if (fileName == null || fileName.trim().isEmpty()) {
                fileName = file != null
                        ? file.getName()
                        : "sconosciuto";
            }

            if (file == null) {
                throw new ValidationException(
                        "Attenzione: selezionare un file!"
                );
            }

            if (!file.exists() || file.length() == 0) {
                throw new ValidationException(
                        "Attenzione: il file selezionato ("
                                + fileName
                                + ") è vuoto o non accessibile!"
                );
            }

            if (getAspectName() == null
                    || getAspectName().trim().isEmpty()) {

                throw new ValidationException(
                        "Attenzione: selezionare la tipologia di File!"
                );
            }

            if (getDescrizione() == null
                    || getDescrizione().trim().isEmpty()) {

                throw new ValidationException(
                        "Attenzione: inserire la descrizione del File!"
                );
            }

            if (P_SIGLA_ACCRUAL_ATTACHMENT_XBRL_ZIP.equals(getAspectName())) {
                validaFileAccrualXbrlZip(file, fileName);
            }
        }

        super.validate();
    }

    private void validaFileAccrualXbrlZip(
            File file,
            String fileName)
            throws ValidationException {

        if (!haEstensioneZip(fileName)) {
            throw new ValidationException(
                    "Attenzione: per la tipologia 'Accrual XBRL ZIP' il file deve avere estensione .zip."
            );
        }

        if (!contieneFileXbrl(file)) {
            throw new ValidationException(
                    "Attenzione: il file ZIP deve contenere almeno un file con estensione .xbrl."
            );
        }
    }

    private boolean haEstensioneZip(String fileName) {
        return fileName != null
                && fileName.trim()
                .toLowerCase()
                .endsWith(".zip");
    }

    private boolean contieneFileXbrl(File file) {

        if (file == null
                || !file.exists()
                || file.length() == 0) {
            return false;
        }

        try (ZipFile zipFile = new ZipFile(file)) {

            return zipFile.stream()
                    .anyMatch(entry ->
                            !entry.isDirectory()
                                    && entry.getName() != null
                                    && entry.getName()
                                    .toLowerCase()
                                    .endsWith(".xbrl"));

        } catch (IOException e) {
            return false;
        }
    }
}
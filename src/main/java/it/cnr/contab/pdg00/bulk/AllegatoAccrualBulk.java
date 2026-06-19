package it.cnr.contab.pdg00.bulk;

import it.cnr.contab.util00.bulk.storage.AllegatoGenericoBulk;
import it.cnr.jada.bulk.ValidationException;
import it.cnr.jada.util.OrderedHashtable;
import it.cnr.si.spring.storage.annotation.StoragePolicy;
import it.cnr.si.spring.storage.annotation.StorageProperty;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

    public boolean isAllegatoEsistente() {
        return !this.isToBeCreated();
    }

    // Lista immutabile dei MIME type validi per i file ZIP
    private static final Set<String> ZIP_MIME_TYPES = Set.of(
            "application/zip",
            "application/x-zip-compressed",
            "application/x-zip"
    );

    // Metodo di utilità privato per la verifica
    private boolean isZipMimeType(String mimeType) {
        return ZIP_MIME_TYPES.contains(mimeType.toLowerCase());
    }
    @Override
    public void validate() throws ValidationException {
        super.validate();
        if (P_SIGLA_ACCRUAL_ATTACHMENT_XBRL_ZIP.equals(getAspectName())) {
            if (isToBeCreated() || isToBeUpdated()) {
                File file = getFile();
                try {
                    String mimeType = Files.probeContentType(file.toPath());
                    Optional.ofNullable(mimeType)
                            .filter(this::isZipMimeType)
                            .orElseThrow(() -> new ValidationException("Il file deve essere di tipo zip."));


                } catch (IOException e) {
                    throw new ValidationException("Il file deve essere di tipo zip");
                }
                validaFileAccrualXbrlZip(file);
            }
        }


        super.validate();
    }

    private void validaFileAccrualXbrlZip(
            File file)
            throws ValidationException {


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
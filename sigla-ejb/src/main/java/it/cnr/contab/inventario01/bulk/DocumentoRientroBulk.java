package it.cnr.contab.inventario01.bulk;

import it.cnr.contab.service.SpringUtil;
import it.cnr.contab.spring.service.StorePath;
import it.cnr.contab.util.Utility;
import it.cnr.si.spring.storage.StorageDriver;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DocumentoRientroBulk extends Doc_trasporto_rientroBulk {

    private static final String DOC_RIENTRO_FILEFOLDER = "Doc. Rientro";

    public DocumentoRientroBulk() {
        super();
        setTiDocumento(RIENTRO);
    }

    public DocumentoRientroBulk(Long pg_inventario, String ti_documento,
                                Integer esercizio, Long pg_doc_trasporto_rientro) {
        super(pg_inventario, ti_documento, esercizio, pg_doc_trasporto_rientro);
        setTiDocumento(RIENTRO);
    }

    // ==================== IMPLEMENTAZIONE STORAGE PATH ====================
    @Override
    public List<String> getStorePath() {

        // ========== VALIDAZIONE ==========
        if (getEsercizio() == null || getPgDocTrasportoRientro() == null) {
            throw new IllegalStateException(
                    "Documento non ancora salvato: impossibile generare lo storage path"
            );
        }

        // ========== COSTRUZIONE PATH ==========
        return Collections.singletonList(
                Arrays.asList(
                        SpringUtil.getBean(StorePath.class).getPathComunicazioniDal(),
                        DOC_RIENTRO_FILEFOLDER,
                        String.valueOf(getEsercizio()),
                        "Documento Rientro " + getEsercizio().toString() +
                                Utility.lpad(getPgDocTrasportoRientro().toString(), 10, '0')
                ).stream().collect(Collectors.joining(StorageDriver.SUFFIX))
        );
    }
}
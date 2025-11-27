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

/**
 * Bulk per documenti di RIENTRO beni inventariali
 */
public class DocumentoRientroBulk extends Doc_trasporto_rientroBulk {

    private static final String DOC_RIENTRO_FILEFOLDER = "Doc. Rientro";

    // ========================================
    // COSTRUTTORI
    // ========================================

    public DocumentoRientroBulk() {
        super();
        setTiDocumento(RIENTRO);
    }

    public DocumentoRientroBulk(Long pg_inventario, String ti_documento,
                                Integer esercizio, Long pg_doc_trasporto_rientro) {
        super(pg_inventario, ti_documento, esercizio, pg_doc_trasporto_rientro);
        setTiDocumento(RIENTRO);
    }

    // ========================================
    // IMPLEMENTAZIONE STORAGE PATH
    // ========================================

    @Override
    public List<String> getStorePath() {

        return Collections.singletonList(Arrays.asList(
                SpringUtil.getBean(StorePath.class).getPathComunicazioniDal(),
                DOC_RIENTRO_FILEFOLDER,
                Optional.ofNullable(this.getEsercizio())
                        .map(esercizio -> String.valueOf(esercizio))
                        .orElse("0"),
                "Documento Rientro " + this.getEsercizio().toString() + Utility.lpad(this.getPgDocTrasportoRientro().toString(), 10, '0')
        ).stream().collect(
                Collectors.joining(StorageDriver.SUFFIX)
        ));
    }
}
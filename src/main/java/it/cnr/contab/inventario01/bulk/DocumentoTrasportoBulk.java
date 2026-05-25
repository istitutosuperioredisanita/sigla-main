package it.cnr.contab.inventario01.bulk;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.cnr.contab.service.SpringUtil;
import it.cnr.contab.spring.service.StorePath;
import it.cnr.contab.util.Utility;
import it.cnr.si.spring.storage.StorageDriver;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DocumentoTrasportoBulk extends Doc_trasporto_rientroBulk {

    public DocumentoTrasportoBulk(Long pg_inventario, String ti_documento,
                                  Integer esercizio, Long pg_doc_trasporto_rientro) {
        super(pg_inventario, ti_documento, esercizio, pg_doc_trasporto_rientro);
        setTiDocumento(TRASPORTO);
    }

    public DocumentoTrasportoBulk() {
        super();
        setTiDocumento(TRASPORTO);
    }

    private static final String DOC_TRASPORTO_FILEFOLDER = "Doc. Trasporto";

    /**
     * @JsonIgnore: evita che Jackson invochi questo metodo durante la
     * serializzazione, poiché richiede il contesto Spring non disponibile nei test.
     * Il campo non fa parte del body REST e non deve comparire nel JSON.
     */
    @Override
    @JsonIgnore
    public List<String> getStorePath() {

        if (getEsercizio() == null || getPgDocTrasportoRientro() == null) {
            throw new IllegalStateException(
                    "Documento non ancora salvato: impossibile generare lo storage path"
            );
        }

        return Collections.singletonList(
                Arrays.asList(
                        SpringUtil.getBean(StorePath.class).getPathComunicazioniDal(),
                        DOC_TRASPORTO_FILEFOLDER,
                        String.valueOf(getEsercizio()),
                        "Documento Trasporto " + getEsercizio().toString()
                                + Utility.lpad(getPgDocTrasportoRientro().toString(), 10, '0')
                ).stream().collect(Collectors.joining(StorageDriver.SUFFIX))
        );
    }
}
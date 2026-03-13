package it.cnr.contab.inventario01.bulk;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.cnr.contab.service.SpringUtil;
import it.cnr.contab.spring.service.StorePath;
import it.cnr.contab.util.Utility;
import it.cnr.contab.util00.bulk.storage.AllegatoGenericoBulk;
import it.cnr.jada.bulk.BulkList;
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

    private BulkList<AllegatoGenericoBulk> archivioAllegati = new BulkList<AllegatoGenericoBulk>();

    @Override
    public BulkList<AllegatoGenericoBulk> getArchivioAllegati() {
        return archivioAllegati;
    }

    @Override
    public void setArchivioAllegati(BulkList<AllegatoGenericoBulk> archivioAllegati) {
        this.archivioAllegati = archivioAllegati;
    }

    @Override
    public int addToArchivioAllegati(AllegatoGenericoBulk allegato) {
        if (allegato != null) {
            archivioAllegati.add(allegato);
        }
        return archivioAllegati.size() - 1;
    }

    public AllegatoGenericoBulk removeFromArchivioAllegati(int index) {
        return getArchivioAllegati().remove(index);
    }

    private final String DOC_TRASPORTO_FILEFOLDER = "Doc. Trasporto";

    /**
     * @JsonIgnore: evita che Jackson invochi questo metodo durante la
     * serializzazione, poiché richiede il contesto Spring non disponibile nei test.
     * Il campo non fa parte del body REST e non deve comparire nel JSON.
     */
    @Override
    @JsonIgnore
    public List<String> getStorePath() {
        return Collections.singletonList(Arrays.asList(
                SpringUtil.getBean(StorePath.class).getPathComunicazioniDal(),
                DOC_TRASPORTO_FILEFOLDER,
                Optional.ofNullable(this.getEsercizio())
                        .map(esercizio -> String.valueOf(esercizio))
                        .orElse("0"),
                "Documento Trasporto " + this.getEsercizio().toString()
                        + Utility.lpad(this.getPgDocTrasportoRientro().toString(), 10, '0')
        ).stream().collect(
                Collectors.joining(StorageDriver.SUFFIX)
        ));
    }
}
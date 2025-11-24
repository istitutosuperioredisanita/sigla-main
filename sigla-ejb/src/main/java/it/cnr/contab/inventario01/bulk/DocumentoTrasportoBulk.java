package it.cnr.contab.inventario01.bulk;

import it.cnr.contab.util00.bulk.storage.AllegatoGenericoBulk;
import it.cnr.contab.util00.bulk.storage.AllegatoParentBulk;
import it.cnr.contab.util00.bulk.storage.AllegatoStorePath;
import it.cnr.jada.bulk.BulkList;

import java.util.List;

public class DocumentoTrasportoBulk extends Doc_trasporto_rientroBulk implements AllegatoParentBulk, AllegatoStorePath {
    // ⚠️ UNICA LISTA ALLEGATI - Include TUTTO (DDT, verbali, stampa firmata, ecc.)

    public DocumentoTrasportoBulk(Long pg_inventario, String ti_documento,
                                  Integer esercizio, Long pg_doc_trasporto_rientro) {
        super(pg_inventario,ti_documento,esercizio,pg_doc_trasporto_rientro);
        setTiDocumento(TRASPORTO);
    }

    public DocumentoTrasportoBulk() {
        super();
        setTiDocumento(TRASPORTO);
    }
    private BulkList<AllegatoGenericoBulk> archivioAllegati = new BulkList<AllegatoGenericoBulk>();
    // Implementazione AllegatoParentBulk
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

    @Override
    public List<String> getStorePath() {
        return null;
    }
}

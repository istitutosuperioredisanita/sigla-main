package it.cnr.contab.inventario01.bulk;

import java.util.Optional;

public class DocumentoRientroDettBulk extends Doc_trasporto_rientro_dettBulk {

    private DocumentoRientroBulk documentoRientro;
    private DocumentoTrasportoDettBulk docTrasportoDettRif;

    public DocumentoRientroDettBulk() {
        super();
    }

    public DocumentoRientroDettBulk(Long pgInventario,
                                    String tiDocumento,
                                    Integer esercizio,
                                    Long pgDocTrasportoRientro,
                                    Long nrInventario,
                                    Integer progressivo) {

        super(pgInventario, tiDocumento, esercizio,
                pgDocTrasportoRientro, nrInventario, progressivo);

        setDoc_trasporto_rientro(
                new DocumentoRientroBulk(
                        pgInventario,
                        tiDocumento,
                        esercizio,
                        pgDocTrasportoRientro
                )
        );
    }

    public DocumentoRientroBulk getDocumentoRientro() {
        return documentoRientro;
    }

    public void setDocumentoRientro(DocumentoRientroBulk documentoRientro) {
        this.documentoRientro = documentoRientro;
    }

    @Override
    public Doc_trasporto_rientroBulk getDoc_trasporto_rientro() {
        return documentoRientro;
    }

    @Override
    public void setDoc_trasporto_rientro(Doc_trasporto_rientroBulk bulk) {
        this.documentoRientro = (DocumentoRientroBulk) bulk;
    }

    public DocumentoTrasportoDettBulk getDocTrasportoDettRif() {
        return docTrasportoDettRif;
    }

    public void setDocTrasportoDettRif(DocumentoTrasportoDettBulk doc) {
        this.docTrasportoDettRif = doc;
    }

    @Override
    public Doc_trasporto_rientro_dettBulk getDoc_trasporto_rientroDettRif() {
        return docTrasportoDettRif;
    }

    @Override
    public void setDoc_trasporto_rientroDettRif(Doc_trasporto_rientro_dettBulk bulk) {
        this.docTrasportoDettRif = (DocumentoTrasportoDettBulk) bulk;
    }


    public Long getPgInventarioRif() {
        return Optional.ofNullable(docTrasportoDettRif)
                .flatMap(d -> Optional.ofNullable(d.getDoc_trasporto_rientro()))
                .map(Doc_trasporto_rientroBulk::getPgInventario)
                .orElse(null);
    }

    public void setPgInventarioRif(Long pg) {
        Optional.ofNullable(docTrasportoDettRif)
                .map(Doc_trasporto_rientro_dettBulk::getDoc_trasporto_rientro)
                .ifPresent(t -> t.setPgInventario(pg));
    }

    public Long getNrInventarioRif() {
        return Optional.ofNullable(docTrasportoDettRif)
                .map(d -> d.getBene() != null
                        ? d.getBene().getNr_inventario()
                        : d.getNr_inventario())
                .orElse(null);
    }

    public void setNrInventarioRif(Long nr) {
        Optional.ofNullable(docTrasportoDettRif)
                .map(Doc_trasporto_rientro_dettBulk::getBene)
                .ifPresent(b -> b.setNr_inventario(nr));
    }

    public Integer getProgressivoRif() {
        return Optional.ofNullable(docTrasportoDettRif)
                .map(d -> {
                    if (d.getBene() != null && d.getBene().getProgressivo() != null)
                        return d.getBene().getProgressivo().intValue();
                    return d.getProgressivo();
                })
                .orElse(null);
    }

    public void setProgressivoRif(Integer prog) {
        Optional.ofNullable(docTrasportoDettRif)
                .map(Doc_trasporto_rientro_dettBulk::getBene)
                .ifPresent(b -> b.setProgressivo(
                        Optional.ofNullable(prog)
                                .map(Integer::longValue)
                                .orElse(null)
                ));
    }

    public String getTiDocumentoRif() {
        return Optional.ofNullable(docTrasportoDettRif)
                .map(Doc_trasporto_rientro_dettBulk::getTi_documento)
                .orElse(null);
    }
    public void setTiDocumentoRif(String v) {
        Optional.ofNullable(docTrasportoDettRif)
                .ifPresent(d -> d.setTi_documento(v));
    }

    public Integer getEsercizioRif() {
        return Optional.ofNullable(docTrasportoDettRif)
                .map(Doc_trasporto_rientro_dettBulk::getEsercizio)
                .orElse(null);
    }
    public void setEsercizioRif(Integer v) {
        Optional.ofNullable(docTrasportoDettRif)
                .ifPresent(d -> d.setEsercizio(v));
    }

    public Long getPgDocTrasportoRientroRif() {
        return Optional.ofNullable(docTrasportoDettRif)
                .map(Doc_trasporto_rientro_dettBulk::getPg_doc_trasporto_rientro)
                .orElse(null);
    }
    public void setPgDocTrasportoRientroRif(Long v) {
        Optional.ofNullable(docTrasportoDettRif)
                .ifPresent(d -> d.setPg_doc_trasporto_rientro(v));
    }

}
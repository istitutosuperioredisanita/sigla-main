package it.cnr.contab.inventario01.bulk;

import java.util.Optional;

public class DocumentoTrasportoDettBulk extends Doc_trasporto_rientro_dettBulk {

    private DocumentoTrasportoBulk documentoTrasporto;
    private DocumentoRientroDettBulk docRientroDettRif;

    public DocumentoTrasportoDettBulk() {
        super();
    }

    public DocumentoTrasportoDettBulk(Long pgInventario,
                                      String tiDocumento,
                                      Integer esercizio,
                                      Long pgDocTrasportoRientro,
                                      Long nrInventario,
                                      Integer progressivo) {

        super(pgInventario, tiDocumento, esercizio,
                pgDocTrasportoRientro, nrInventario, progressivo);

        setDoc_trasporto_rientro(
                new DocumentoTrasportoBulk(
                        pgInventario,
                        tiDocumento,
                        esercizio,
                        pgDocTrasportoRientro
                )
        );
    }

    public DocumentoTrasportoBulk getDocumentoTrasporto() {
        return documentoTrasporto;
    }

    public void setDocumentoTrasporto(DocumentoTrasportoBulk documentoTrasporto) {
        this.documentoTrasporto = documentoTrasporto;
    }

    @Override
    public Doc_trasporto_rientroBulk getDoc_trasporto_rientro() {
        return documentoTrasporto;
    }

    @Override
    public void setDoc_trasporto_rientro(Doc_trasporto_rientroBulk bulk) {
        this.documentoTrasporto = (DocumentoTrasportoBulk) bulk;
    }

    public DocumentoRientroDettBulk getDocRientroDettRif() {
        return docRientroDettRif;
    }

    public void setDocRientroDettRif(DocumentoRientroDettBulk docRientroDettRif) {
        this.docRientroDettRif = docRientroDettRif;
    }

    @Override
    public Doc_trasporto_rientro_dettBulk getDoc_trasporto_rientroDettRif() {
        return docRientroDettRif;
    }

    @Override
    public void setDoc_trasporto_rientroDettRif(Doc_trasporto_rientro_dettBulk bulk) {
        this.docRientroDettRif = (DocumentoRientroDettBulk) bulk;
    }


    public Long getPgInventarioRif() {
        return Optional.ofNullable(docRientroDettRif)
                .map(Doc_trasporto_rientro_dettBulk::getPg_inventario)
                .orElse(null);
    }

    public void setPgInventarioRif(Long pgInventario) {
        Optional.ofNullable(docRientroDettRif)
                .ifPresent(d -> d.setPg_inventario(pgInventario));
    }

    public Long getNrInventarioRif() {
        return Optional.ofNullable(docRientroDettRif)
                .map(d -> d.getBene() != null
                        ? d.getBene().getNr_inventario()
                        : d.getNr_inventario())
                .orElse(null);
    }

    public void setNrInventarioRif(Long nrInventario) {
        Optional.ofNullable(docRientroDettRif)
                .map(Doc_trasporto_rientro_dettBulk::getBene)
                .ifPresent(b -> b.setNr_inventario(nrInventario));
    }

    public Integer getProgressivoRif() {
        return Optional.ofNullable(docRientroDettRif)
                .map(d -> {
                    if (d.getBene() != null && d.getBene().getProgressivo() != null)
                        return d.getBene().getProgressivo().intValue();
                    return d.getProgressivo();
                })
                .orElse(null);
    }

    public void setProgressivoRif(Integer progressivoRif) {
        Optional.ofNullable(docRientroDettRif)
                .map(Doc_trasporto_rientro_dettBulk::getBene)
                .ifPresent(b -> b.setProgressivo(
                        Optional.ofNullable(progressivoRif)
                                .map(Integer::longValue)
                                .orElse(null)
                ));
    }

    public String getTiDocumentoRif() {
        return Optional.ofNullable(docRientroDettRif)
                .map(Doc_trasporto_rientro_dettBulk::getTi_documento)
                .orElse(null);
    }

    public void setTiDocumentoRif(String tiDocumentoRif) {
        Optional.ofNullable(docRientroDettRif)
                .ifPresent(d -> d.setTi_documento(tiDocumentoRif));
    }

    public Integer getEsercizioRif() {
        return Optional.ofNullable(docRientroDettRif)
                .map(Doc_trasporto_rientro_dettBulk::getEsercizio)
                .orElse(null);
    }

    public void setEsercizioRif(Integer esercizioRif) {
        Optional.ofNullable(docRientroDettRif)
                .ifPresent(d -> d.setEsercizio(esercizioRif));
    }

    public Long getPgDocTrasportoRientroRif() {
        return Optional.ofNullable(docRientroDettRif)
                .map(Doc_trasporto_rientro_dettBulk::getPg_doc_trasporto_rientro)
                .orElse(null);
    }

    public void setPgDocTrasportoRientroRif(Long pg) {
        Optional.ofNullable(docRientroDettRif)
                .ifPresent(d -> d.setPg_doc_trasporto_rientro(pg));
    }
}
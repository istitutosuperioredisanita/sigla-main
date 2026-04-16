package it.cnr.contab.web.rest.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DocTRWSResponse {

    private boolean ok;
    private String message;
    private Long pgInventario;
    private String tiDocumento;
    private Integer esercizio;
    private Long pgDocTrasportoRientro;

    public DocTRWSResponse() {
    }

    public DocTRWSResponse(boolean ok,
                           String message,
                           Long pgInventario,
                           String tiDocumento,
                           Integer esercizio,
                           Long pgDocTrasportoRientro) {
        this.ok = ok;
        this.message = message;
        this.pgInventario = pgInventario;
        this.tiDocumento = tiDocumento;
        this.esercizio = esercizio;
        this.pgDocTrasportoRientro = pgDocTrasportoRientro;
    }

    public static DocTRWSResponse of(boolean ok,
                                     String message,
                                     Doc_trasporto_rientroBulk documento) {
        return new DocTRWSResponse(
                ok,
                message,
                documento != null ? documento.getPgInventario() : null,
                documento != null ? documento.getTiDocumento() : null,
                documento != null ? documento.getEsercizio() : null,
                documento != null ? documento.getPgDocTrasportoRientro() : null
        );
    }

    public static DocTRWSResponse messageOnly(boolean ok, String message) {
        return new DocTRWSResponse(ok, message, null, null, null, null);
    }

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getPgInventario() {
        return pgInventario;
    }

    public void setPgInventario(Long pgInventario) {
        this.pgInventario = pgInventario;
    }

    public String getTiDocumento() {
        return tiDocumento;
    }

    public void setTiDocumento(String tiDocumento) {
        this.tiDocumento = tiDocumento;
    }

    public Integer getEsercizio() {
        return esercizio;
    }

    public void setEsercizio(Integer esercizio) {
        this.esercizio = esercizio;
    }

    public Long getPgDocTrasportoRientro() {
        return pgDocTrasportoRientro;
    }

    public void setPgDocTrasportoRientro(Long pgDocTrasportoRientro) {
        this.pgDocTrasportoRientro = pgDocTrasportoRientro;
    }
}
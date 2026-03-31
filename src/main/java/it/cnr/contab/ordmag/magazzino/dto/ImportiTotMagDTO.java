package it.cnr.contab.ordmag.magazzino.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

public class ImportiTotMagDTO implements Serializable {

    private BigDecimal qtyInizioAnnoTot; // somma di tutte le qty inizio anno
    private BigDecimal valoreInizioAnnoTot; // somma di tutti valori inizio anno
    private BigDecimal qtyCaricataTot; // somma di tutti i carichi
    private BigDecimal importoCaricoTot; // somma di tutti gli importi carico
    private BigDecimal qtyScaricataTot; // somma di tutti gli scarichi
    private BigDecimal importoScaricoTot;// somma di tutti gli importi scarico
    private BigDecimal giacenzaTot;// somma tutte giacenza
    private BigDecimal importoGiacenzaTot;// somma tutti gli importi giacenza


    public BigDecimal getQtyInizioAnnoTot() {
        if(this.qtyInizioAnnoTot==null)
            return new BigDecimal(0);
        return qtyInizioAnnoTot;

    }

    public void setQtyInizioAnnoTot(BigDecimal qtyInizioAnnoTot) {
        this.qtyInizioAnnoTot = qtyInizioAnnoTot;
    }

    public BigDecimal getValoreInizioAnnoTot() {
        if(this.valoreInizioAnnoTot==null)
            return new BigDecimal(0);
        return valoreInizioAnnoTot;

    }

    public void setValoreInizioAnnoTot(BigDecimal valoreInizioAnnoTot) {
        this.valoreInizioAnnoTot = valoreInizioAnnoTot;
    }

    public BigDecimal getQtyCaricataTot() {

        if(this.qtyCaricataTot==null)
            return new BigDecimal(0);
        return qtyCaricataTot;

    }

    public void setQtyCaricataTot(BigDecimal qtyCaricataTot) {
        this.qtyCaricataTot = qtyCaricataTot;
    }

    public BigDecimal getImportoCaricoTot() {

        if(this.importoCaricoTot==null)
            return new BigDecimal(0);
        return importoCaricoTot;
    }

    public void setImportoCaricoTot(BigDecimal importoCaricoTot) {
        this.importoCaricoTot = importoCaricoTot;
    }

    public BigDecimal getQtyScaricataTot() {

        if(this.qtyScaricataTot==null)
            return new BigDecimal(0);
        return qtyScaricataTot;

    }

    public void setQtyScaricataTot(BigDecimal qtyScaricataTot) {
        this.qtyScaricataTot = qtyScaricataTot;
    }

    public BigDecimal getImportoScaricoTot() {

        if(this.importoScaricoTot==null)
            return new BigDecimal(0);
        return importoScaricoTot;
    }

    public void setImportoScaricoTot(BigDecimal importoScaricoTot) {
        this.importoScaricoTot = importoScaricoTot;
    }

    public BigDecimal getGiacenzaTot() {
        if(this.giacenzaTot==null)
            return new BigDecimal(0);
        return giacenzaTot;

    }

    public void setGiacenzaTot(BigDecimal giacenzaTot) {
        this.giacenzaTot = giacenzaTot;
    }

    public BigDecimal getImportoGiacenzaTot() {
        if(this.importoGiacenzaTot==null)
            return new BigDecimal(0);
        return importoGiacenzaTot;

    }

    public void setImportoGiacenzaTot(BigDecimal importoGiacenzaTot) {
        this.importoGiacenzaTot = importoGiacenzaTot;
    }
}

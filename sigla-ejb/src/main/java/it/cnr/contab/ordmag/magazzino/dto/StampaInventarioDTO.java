package it.cnr.contab.ordmag.magazzino.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;

public class StampaInventarioDTO extends StampaInventarioDTOKey implements Serializable {

    private Timestamp dataMovimentoChiusuraLotto;
    private Timestamp dataCaricoLotto;
    private String desc_magazzino;
    private String descArticolo;
    private String descCatGrp;
    private String cod_categoria;
    private String cod_gruppo;
    private BigDecimal giacenza;
    private String um;
    private BigDecimal importoUnitario;
    private String cdMagRaggr;
    private String descMagRaggr;
    private String cod_categoriaGruppo;

    // VALORI PER SINGOLO LOTTO
    private BigDecimal qtyInizioAnnoLotto; // preso da lotto e aggiornato da lotto del movimento di chiusura
    private BigDecimal valoreInizioAnnoLotto; // preso da costo unitario lotto e aggiornato con costo unitario movimento di chiusura
    private BigDecimal qtyCaricataLotto; // preso dal movimento di carico (somma tutti i carichi)
    private BigDecimal importoCaricoLotto; // preso dal movimento di scarico (somma di importo unitario * qty caricata)
    private BigDecimal qtyScaricataLotto; // preso dal movimento di scarico (somma tutto gli scarichi)
    private BigDecimal importoScaricoLotto;// preso dal movimento di scarico (somma di importo unitario * qty scaricata)
    private BigDecimal importoGiacenzaLotto;// somma tutte giacenza*importo unitario


    // VALORI TOTALI PER ARTICOLO/ CAT GRUPPO/ RAGGR MAGAZZINO
    private BigDecimal mediaPonderataArticolo;
    private BigDecimal mediaPonderataCatGruppo;
    private BigDecimal mediaPonderataRaggrMag;
    private BigDecimal mediaPonderataTotale;

    // VALORI TOTALI PER ARTICOLO/ CAT GRUPPO
    private BigDecimal qtyInizioAnnoTotArt; // somma di tutte le qty inizio anno per Articolo
    private BigDecimal valoreInizioAnnoTotArt; // somma di tutti valori inizio anno per Articolo
    private BigDecimal qtyCaricataTotArt; // somma di tutti i carichi per Articolo
    private BigDecimal importoCaricoTotArt; // somma di tutti gli importi carico per Articolo
    private BigDecimal qtyScaricataTotArt; // somma di tutti gli scarichi per Articolo
    private BigDecimal importoScaricoTotArt;// somma di tutti gli importi scarico per Articolo
    private BigDecimal giacenzaTotArt;// somma tutte giacenza per Articolo
    private BigDecimal importoGiacenzaTotArt;// somma tutti gli importi giacenza per Articolo

    private BigDecimal qtyInizioAnnoTotCatGr; // somma di tutte le qty inizio anno per Cat. Gruppo
    private BigDecimal valoreInizioAnnoTotCatGr; // somma di tutti valori inizio anno per Cat. Gruppo
    private BigDecimal qtyCaricataTotCatGr; // somma di tutti i carichi per Cat. Gruppo
    private BigDecimal importoCaricoTotCatGr; // somma di tutti gli importi carico per Cat. Gruppo
    private BigDecimal qtyScaricataTotCatGr; // somma di tutti gli scarichi per Cat. Gruppo
    private BigDecimal importoScaricoTotCatGr;// somma di tutti gli importi scarico per Cat. Gruppo
    private BigDecimal giacenzaTotCatGr;// somma tutte giacenza per Cat. Gruppo
    private BigDecimal importoGiacenzaTotCatGr;// somma tutti gli importi giacenza per Cat. Gruppo

    private BigDecimal qtyInizioAnnoTotRaggrMag; // somma di tutte le qty inizio anno per Raggruppamento Magazzino
    private BigDecimal valoreInizioAnnoTotRaggrMag; // somma di tutti valori inizio anno per Raggruppamento Magazzino
    private BigDecimal qtyCaricataTotRaggrMag; // somma di tutti i carichi per Raggruppamento Magazzino
    private BigDecimal importoCaricoTotRaggrMag; // somma di tutti gli importi carico per Raggruppamento Magazzino
    private BigDecimal qtyScaricataTotRaggrMag; // somma di tutti gli scarichi per Raggruppamento Magazzino
    private BigDecimal importoScaricoTotRaggrMag;// somma di tutti gli importi scarico per Raggruppamento Magazzino
    private BigDecimal giacenzaTotRaggrMag;// somma tutte giacenza per Raggruppamento Magazzino
    private BigDecimal importoGiacenzaTotRaggrMag;// somma tutti gli importi giacenza per Raggruppamento Magazzino





    public Timestamp getDataMovimentoChiusuraLotto() {
        return dataMovimentoChiusuraLotto;
    }

    public void setDataMovimentoChiusuraLotto(Timestamp dataMovimentoChiusuraLotto) {
        this.dataMovimentoChiusuraLotto = dataMovimentoChiusuraLotto;
    }

    public Timestamp getDataCaricoLotto() {
        return dataCaricoLotto;
    }

    public void setDataCaricoLotto(Timestamp dataCaricoLotto) {
        this.dataCaricoLotto = dataCaricoLotto;
    }

    public String getDesc_magazzino() {
        return desc_magazzino;
    }

    public void setDesc_magazzino(String desc_magazzino) {
        this.desc_magazzino = desc_magazzino;
    }

    public String getDescArticolo() {
        return descArticolo;
    }

    public void setDescArticolo(String descArticolo) {
        this.descArticolo = descArticolo;
    }

    public String getDescCatGrp() {
        return descCatGrp;
    }

    public void setDescCatGrp(String descCatGrp) {
        this.descCatGrp = descCatGrp;
    }

    public String getCod_categoria() {
        return cod_categoria;
    }

    public void setCod_categoria(String cod_categoria) {
        this.cod_categoria = cod_categoria;
    }

    public String getCod_gruppo() {
        return cod_gruppo;
    }

    public void setCod_gruppo(String cod_gruppo) {
        this.cod_gruppo = cod_gruppo;
    }

    public BigDecimal getGiacenza() {
        if(this.giacenza==null)
            return new BigDecimal(0);
        return giacenza;
    }

    public void setGiacenza(BigDecimal giacenza) {
        this.giacenza = giacenza;
    }

    public String getUm() {
        return um;
    }

    public void setUm(String um) {
        this.um = um;
    }

    public BigDecimal getImportoUnitario() {
        return importoUnitario;
    }

    public void setImportoUnitario(BigDecimal importoUnitario) {
        this.importoUnitario = importoUnitario;
    }

    public String getCdMagRaggr() {
        return cdMagRaggr;
    }

    public void setCdMagRaggr(String cdMagRaggr) {
        this.cdMagRaggr = cdMagRaggr;
    }

    public String getDescMagRaggr() {
        return descMagRaggr;
    }

    public void setDescMagRaggr(String descMagRaggr) {
        this.descMagRaggr = descMagRaggr;
    }


    public BigDecimal getQtyInizioAnnoLotto() {
        if(this.qtyInizioAnnoLotto==null)
            return new BigDecimal(0);
        return qtyInizioAnnoLotto;
    }

    public void setQtyInizioAnnoLotto(BigDecimal qtyInizioAnnoLotto) {
        this.qtyInizioAnnoLotto = qtyInizioAnnoLotto;
    }

    public BigDecimal getValoreInizioAnnoLotto() {
        if(this.valoreInizioAnnoLotto==null)
            return new BigDecimal(0);
        return valoreInizioAnnoLotto;
    }

    public void setValoreInizioAnnoLotto(BigDecimal valoreInizioAnnoLotto) {
        this.valoreInizioAnnoLotto = valoreInizioAnnoLotto;
    }


    public BigDecimal getQtyCaricataLotto() {
        if(this.qtyCaricataLotto==null)
            return new BigDecimal(0);
        return qtyCaricataLotto;
    }

    public void setQtyCaricataLotto(BigDecimal qtyCaricataLotto) {
        this.qtyCaricataLotto = qtyCaricataLotto;
    }

    public BigDecimal getQtyScaricataLotto() {
        if(this.qtyScaricataLotto==null)
            return new BigDecimal(0);
        return qtyScaricataLotto;
    }

    public void setQtyScaricataLotto(BigDecimal qtyScaricataLotto) {
        this.qtyScaricataLotto = qtyScaricataLotto;
    }

    public BigDecimal getImportoCaricoLotto() {
        if(this.importoCaricoLotto==null)
            return new BigDecimal(0);
        return importoCaricoLotto;
    }

    public void setImportoCaricoLotto(BigDecimal importoCaricoLotto) {
        this.importoCaricoLotto = importoCaricoLotto;
    }



    public BigDecimal getImportoScaricoLotto() {
        if(this.importoScaricoLotto==null)
            return new BigDecimal(0);
        return importoScaricoLotto;
    }

    public void setImportoScaricoLotto(BigDecimal importoScaricoLotto) {
        this.importoScaricoLotto = importoScaricoLotto;
    }

    public BigDecimal getImportoGiacenzaLotto() {
        if(this.importoGiacenzaLotto==null)
            return new BigDecimal(0);
        return importoGiacenzaLotto;
    }

    public void setImportoGiacenzaLotto(BigDecimal importoGiacenzaLotto) {
        this.importoGiacenzaLotto = importoGiacenzaLotto;
    }

    public BigDecimal getMediaPonderataArticolo() {
        return mediaPonderataArticolo;
    }

    public void setMediaPonderataArticolo(BigDecimal mediaPonderataArticolo) {
        this.mediaPonderataArticolo = mediaPonderataArticolo;
    }

    public BigDecimal getMediaPonderataCatGruppo() {
        return mediaPonderataCatGruppo;
    }

    public void setMediaPonderataCatGruppo(BigDecimal mediaPonderataCatGruppo) {
        this.mediaPonderataCatGruppo = mediaPonderataCatGruppo;
    }

    public BigDecimal getMediaPonderataRaggrMag() {
        return mediaPonderataRaggrMag;
    }

    public void setMediaPonderataRaggrMag(BigDecimal mediaPonderataRaggrMag) {
        this.mediaPonderataRaggrMag = mediaPonderataRaggrMag;
    }

    public BigDecimal getMediaPonderataTotale() {
        return mediaPonderataTotale;
    }

    public void setMediaPonderataTotale(BigDecimal mediaPonderataTotale) {
        this.mediaPonderataTotale = mediaPonderataTotale;
    }


    public String getCod_categoriaGruppo(){
        return this.getCod_categoria()+"."+this.getCod_gruppo();
    }

    public void setCod_categoriaGruppo(String cod_categoriaGruppo) {
        this.cod_categoriaGruppo = cod_categoriaGruppo;
    }

    public BigDecimal getQtyInizioAnnoTotArt() {
        if(this.qtyInizioAnnoTotArt==null)
            return new BigDecimal(0);
        return qtyInizioAnnoTotArt;

    }

    public void setQtyInizioAnnoTotArt(BigDecimal qtyInizioAnnoTotArt) {
        this.qtyInizioAnnoTotArt = qtyInizioAnnoTotArt;
    }

    public BigDecimal getValoreInizioAnnoTotArt() {
        if(this.valoreInizioAnnoTotArt==null)
            return new BigDecimal(0);
        return valoreInizioAnnoTotArt;

    }

    public void setValoreInizioAnnoTotArt(BigDecimal valoreInizioAnnoTotArt) {
        this.valoreInizioAnnoTotArt = valoreInizioAnnoTotArt;
    }

    public BigDecimal getQtyCaricataTotArt() {
        if(this.qtyCaricataTotArt==null)
            return new BigDecimal(0);
        return qtyCaricataTotArt;

    }

    public void setQtyCaricataTotArt(BigDecimal qtyCaricataTotArt) {
        this.qtyCaricataTotArt = qtyCaricataTotArt;
    }

    public BigDecimal getImportoCaricoTotArt() {
        if(this.importoCaricoTotArt==null)
            return new BigDecimal(0);
        return importoCaricoTotArt;

    }

    public void setImportoCaricoTotArt(BigDecimal importoCaricoTotArt) {
        this.importoCaricoTotArt = importoCaricoTotArt;
    }

    public BigDecimal getQtyScaricataTotArt() {
        if(this.qtyScaricataTotArt==null)
            return new BigDecimal(0);
        return qtyScaricataTotArt;

    }

    public void setQtyScaricataTotArt(BigDecimal qtyScaricataTotArt) {
        this.qtyScaricataTotArt = qtyScaricataTotArt;
    }

    public BigDecimal getImportoScaricoTotArt() {
        if(this.importoScaricoTotArt==null)
            return new BigDecimal(0);
        return importoScaricoTotArt;

    }

    public void setImportoScaricoTotArt(BigDecimal importoScaricoTotArt) {
        this.importoScaricoTotArt = importoScaricoTotArt;
    }

    public BigDecimal getGiacenzaTotArt() {
        if(this.giacenzaTotArt==null)
            return new BigDecimal(0);
        return giacenzaTotArt;

    }

    public void setGiacenzaTotArt(BigDecimal giacenzaTotArt) {
        this.giacenzaTotArt = giacenzaTotArt;
    }

    public BigDecimal getImportoGiacenzaTotArt() {
        if(this.importoGiacenzaTotArt==null)
            return new BigDecimal(0);
        return importoGiacenzaTotArt;

    }

    public void setImportoGiacenzaTotArt(BigDecimal importoGiacenzaTotArt) {
        this.importoGiacenzaTotArt = importoGiacenzaTotArt;
    }

    public BigDecimal getQtyInizioAnnoTotCatGr() {
        if(this.qtyInizioAnnoTotCatGr==null)
            return new BigDecimal(0);
        return qtyInizioAnnoTotCatGr;

    }

    public void setQtyInizioAnnoTotCatGr(BigDecimal qtyInizioAnnoTotCatGr) {
        this.qtyInizioAnnoTotCatGr = qtyInizioAnnoTotCatGr;
    }

    public BigDecimal getValoreInizioAnnoTotCatGr() {
        if(this.valoreInizioAnnoTotCatGr==null)
            return new BigDecimal(0);
        return valoreInizioAnnoTotCatGr;

    }

    public void setValoreInizioAnnoTotCatGr(BigDecimal valoreInizioAnnoTotCatGr) {
        this.valoreInizioAnnoTotCatGr = valoreInizioAnnoTotCatGr;
    }

    public BigDecimal getQtyCaricataTotCatGr() {
        if(this.qtyCaricataTotCatGr==null)
            return new BigDecimal(0);
        return qtyCaricataTotCatGr;

    }

    public void setQtyCaricataTotCatGr(BigDecimal qtyCaricataTotCatGr) {
        this.qtyCaricataTotCatGr = qtyCaricataTotCatGr;
    }

    public BigDecimal getImportoCaricoTotCatGr() {
        if(this.importoCaricoTotCatGr==null)
            return new BigDecimal(0);
        return importoCaricoTotCatGr;

    }

    public void setImportoCaricoTotCatGr(BigDecimal importoCaricoTotCatGr) {
        this.importoCaricoTotCatGr = importoCaricoTotCatGr;
    }

    public BigDecimal getQtyScaricataTotCatGr() {
        if(this.qtyScaricataTotCatGr==null)
            return new BigDecimal(0);
        return qtyScaricataTotCatGr;

    }

    public void setQtyScaricataTotCatGr(BigDecimal qtyScaricataTotCatGr) {
        this.qtyScaricataTotCatGr = qtyScaricataTotCatGr;
    }

    public BigDecimal getImportoScaricoTotCatGr() {
        if(this.importoScaricoTotCatGr==null)
            return new BigDecimal(0);
        return importoScaricoTotCatGr;

    }

    public void setImportoScaricoTotCatGr(BigDecimal importoScaricoTotCatGr) {
        this.importoScaricoTotCatGr = importoScaricoTotCatGr;
    }

    public BigDecimal getGiacenzaTotCatGr() {
        if(this.giacenzaTotCatGr==null)
            return new BigDecimal(0);
        return giacenzaTotCatGr;

    }

    public void setGiacenzaTotCatGr(BigDecimal giacenzaTotCatGr) {
        this.giacenzaTotCatGr = giacenzaTotCatGr;
    }

    public BigDecimal getImportoGiacenzaTotCatGr() {
        if(this.importoGiacenzaTotCatGr==null)
            return new BigDecimal(0);
        return importoGiacenzaTotCatGr;

    }

    public void setImportoGiacenzaTotCatGr(BigDecimal importoGiacenzaTotCatGr) {
        this.importoGiacenzaTotCatGr = importoGiacenzaTotCatGr;
    }

    public BigDecimal getQtyInizioAnnoTotRaggrMag() {
        if(this.qtyInizioAnnoTotRaggrMag==null)
            return new BigDecimal(0);
        return qtyInizioAnnoTotRaggrMag;

    }

    public void setQtyInizioAnnoTotRaggrMag(BigDecimal qtyInizioAnnoTotRaggrMag) {
        this.qtyInizioAnnoTotRaggrMag = qtyInizioAnnoTotRaggrMag;
    }

    public BigDecimal getValoreInizioAnnoTotRaggrMag() {
        if(this.valoreInizioAnnoTotRaggrMag==null)
            return new BigDecimal(0);
        return valoreInizioAnnoTotRaggrMag;

    }

    public void setValoreInizioAnnoTotRaggrMag(BigDecimal valoreInizioAnnoTotRaggrMag) {
        this.valoreInizioAnnoTotRaggrMag = valoreInizioAnnoTotRaggrMag;
    }

    public BigDecimal getQtyCaricataTotRaggrMag() {
        if(this.qtyCaricataTotRaggrMag==null)
            return new BigDecimal(0);
        return qtyCaricataTotRaggrMag;

    }

    public void setQtyCaricataTotRaggrMag(BigDecimal qtyCaricataTotRaggrMag) {
        this.qtyCaricataTotRaggrMag = qtyCaricataTotRaggrMag;
    }

    public BigDecimal getImportoCaricoTotRaggrMag() {
        if(this.importoCaricoTotRaggrMag==null)
            return new BigDecimal(0);
        return importoCaricoTotRaggrMag;

    }

    public void setImportoCaricoTotRaggrMag(BigDecimal importoCaricoTotRaggrMag) {
        this.importoCaricoTotRaggrMag = importoCaricoTotRaggrMag;
    }

    public BigDecimal getQtyScaricataTotRaggrMag() {
        if(this.qtyScaricataTotRaggrMag==null)
            return new BigDecimal(0);
        return qtyScaricataTotRaggrMag;

    }

    public void setQtyScaricataTotRaggrMag(BigDecimal qtyScaricataTotRaggrMag) {
        this.qtyScaricataTotRaggrMag = qtyScaricataTotRaggrMag;
    }

    public BigDecimal getImportoScaricoTotRaggrMag() {
        if(this.importoScaricoTotRaggrMag==null)
            return new BigDecimal(0);
        return importoScaricoTotRaggrMag;

    }

    public void setImportoScaricoTotRaggrMag(BigDecimal importoScaricoTotRaggrMag) {
        this.importoScaricoTotRaggrMag = importoScaricoTotRaggrMag;
    }

    public BigDecimal getGiacenzaTotRaggrMag() {
        if(this.giacenzaTotRaggrMag==null)
            return new BigDecimal(0);
        return giacenzaTotRaggrMag;

    }

    public void setGiacenzaTotRaggrMag(BigDecimal giacenzaTotRaggrMag) {
        this.giacenzaTotRaggrMag = giacenzaTotRaggrMag;
    }

    public BigDecimal getImportoGiacenzaTotRaggrMag() {
        if(this.importoGiacenzaTotRaggrMag==null)
            return new BigDecimal(0);
        return importoGiacenzaTotRaggrMag;

    }

    public void setImportoGiacenzaTotRaggrMag(BigDecimal importoGiacenzaTotRaggrMag) {
        this.importoGiacenzaTotRaggrMag = importoGiacenzaTotRaggrMag;
    }
}

package it.cnr.contab.doccont00.consultazioni.bulk;

import it.cnr.contab.config00.sto.bulk.CdsBulk;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.Persistent;
import it.cnr.jada.util.OrderedHashtable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Dictionary;


public class FlussiDiCassaDtoBulk extends OggettoBulk implements Persistent {

    private final static Dictionary tipo_EstrazioneKeys;
    public final static String MANDATI = "MAN";
    public final static String REVERSALI = "REV";

    private final static Dictionary trimestreKeys;
    public final static String PRIMO = "1";
    public final static String SECONDO = "2";
    public final static String TERZO = "3";
    public final static String QUARTO = "4";

    private final static Dictionary livelloKeys;
    public final static String QUINTO = "5";

    private String cdCds;
    protected CdsBulk cds = new CdsBulk();
    private Integer esercizio;

    private Timestamp dtEmissioneDa;
    private Timestamp dtEmissioneA;
    private String tipoFlusso;
    private String trimestre;
    private String livello;

    private boolean estrazioneRendiconto=false;

    static {
        tipo_EstrazioneKeys = new OrderedHashtable();
        tipo_EstrazioneKeys.put(REVERSALI, "Entrate");
        tipo_EstrazioneKeys.put(MANDATI, "Uscite");

    }

    static {
        trimestreKeys = new OrderedHashtable();
        trimestreKeys.put(PRIMO, "Primo");
        trimestreKeys.put(SECONDO, "Secondo");
        trimestreKeys.put(TERZO, "Terzo");
        trimestreKeys.put(QUARTO, "Quarto");
    }

    static {
        livelloKeys = new OrderedHashtable();
        livelloKeys.put(SECONDO, "Secondo Livello");
        livelloKeys.put(TERZO, "Terzo Livello");
        livelloKeys.put(QUARTO, "Quarto Livello");
        livelloKeys.put(QUINTO, "Quinto Livello");
    }
    private boolean roFindCds;

    private String classificazione;
    private String dsClassificazione;

    private BigDecimal impPrimoTrimestre;
    private BigDecimal impSecondoTrimestre;
    private BigDecimal impTerzoTrimestre;
    private BigDecimal impQuartoTrimestre;

    private BigDecimal importoTotale;



    public String getCdCds() {
        return this.cds.getCd_unita_organizzativa();
    }

    public void setCdCds(String cdCds) {
        this.cds.setCd_unita_organizzativa(cdCds);
    }

    public CdsBulk getCds() {
        return cds;
    }

    public void setCds(CdsBulk cds) {
        this.cds = cds;
    }

    public Integer getEsercizio() {
        return esercizio;
    }

    public void setEsercizio(Integer esercizio) {
        this.esercizio = esercizio;
    }

    public Timestamp getDtEmissioneDa() {
        return dtEmissioneDa;
    }

    public void setDtEmissioneDa(Timestamp dtEmissioneDa) {
        this.dtEmissioneDa = dtEmissioneDa;
    }

    public Timestamp getDtEmissioneA() {
        return dtEmissioneA;
    }

    public void setDtEmissioneA(Timestamp dtEmissioneA) {
        this.dtEmissioneA = dtEmissioneA;
    }

    public String getTipoFlusso() {
        return tipoFlusso;
    }

    public void setTipoFlusso(String tipoFlusso) {
        this.tipoFlusso = tipoFlusso;
    }

    public String getClassificazione() {
        return classificazione;
    }

    public void setClassificazione(String classificazione) {
        this.classificazione = classificazione;
    }

    public String getDsClassificazione() {
        return dsClassificazione;
    }

    public void setDsClassificazione(String dsClassificazione) {
        this.dsClassificazione = dsClassificazione;
    }

    public BigDecimal getImpPrimoTrimestre() {
        return impPrimoTrimestre;
    }

    public void setImpPrimoTrimestre(BigDecimal impPrimoTrimestre) {
        this.impPrimoTrimestre = impPrimoTrimestre;
    }

    public BigDecimal getImpSecondoTrimestre() {
        return impSecondoTrimestre;
    }

    public void setImpSecondoTrimestre(BigDecimal impSecondoTrimestre) {
        this.impSecondoTrimestre = impSecondoTrimestre;
    }

    public BigDecimal getImpTerzoTrimestre() {
        return impTerzoTrimestre;
    }

    public void setImpTerzoTrimestre(BigDecimal impTerzoTrimestre) {
        this.impTerzoTrimestre = impTerzoTrimestre;
    }

    public BigDecimal getImpQuartoTrimestre() {
        return impQuartoTrimestre;
    }

    public void setImpQuartoTrimestre(BigDecimal impQuartoTrimestre) {
        this.impQuartoTrimestre = impQuartoTrimestre;
    }

    public String getTrimestre() {
        return trimestre;
    }

    public void setTrimestre(String trimestre) {
        this.trimestre = trimestre;
    }

    public String getLivello() {
        return livello;
    }

    public void setLivello(String livello) {
        this.livello = livello;
    }

    public boolean isROFindCds() {
        return roFindCds;
    }

    public void setROFindCds(boolean roFindCds) {
        this.roFindCds = roFindCds;
    }

    public java.util.Dictionary getTipo_EstrazioneKeys() {
        return tipo_EstrazioneKeys;
    }
    public java.util.Dictionary getTrimestreKeys() {
        return trimestreKeys;
    }
    public java.util.Dictionary getLivelloKeys() {
        return livelloKeys;
    }

    public boolean isEstrazioneRendiconto() {
        return estrazioneRendiconto;
    }

    public void setEstrazioneRendiconto(boolean estrazioneRendiconto) {
        this.estrazioneRendiconto = estrazioneRendiconto;
    }

    public boolean isRoFindCds() {
        return roFindCds;
    }

    public void setRoFindCds(boolean roFindCds) {
        this.roFindCds = roFindCds;
    }

    public BigDecimal getImportoTotale() {
        return importoTotale;
    }

    public void setImportoTotale(BigDecimal importoTotale) {
        this.importoTotale = importoTotale;
    }
}

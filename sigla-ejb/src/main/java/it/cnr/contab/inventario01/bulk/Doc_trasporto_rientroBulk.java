package it.cnr.contab.inventario01.bulk;

import com.fasterxml.jackson.annotation.*;
import it.cnr.contab.anagraf00.core.bulk.AnagraficoBulk;
import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.contab.anagraf00.core.bulk.V_persona_fisicaBulk;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;
import it.cnr.contab.inventario00.docs.bulk.Inventario_beniBulk;
import it.cnr.contab.inventario00.tabrif.bulk.Id_inventarioBulk;
import it.cnr.contab.inventario00.tabrif.bulk.Tipo_trasporto_rientroBulk;
import it.cnr.contab.util00.bulk.storage.AllegatoGenericoBulk;
import it.cnr.contab.util00.bulk.storage.AllegatoParentBulk;
import it.cnr.contab.util00.bulk.storage.AllegatoStorePath;
import it.cnr.jada.bulk.*;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.util.StrServ;

import java.util.Collection;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;

/**
 * Testata del documento di Trasporto o Rientro beni inventariali.
 * La classe gestisce le informazioni principali del movimento e la collezione degli allegati e dei dettagli.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "tiDocumento",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DocumentoTrasportoBulk.class, name = "T"),
        @JsonSubTypes.Type(value = DocumentoRientroBulk.class, name = "R")
})
public abstract class Doc_trasporto_rientroBulk extends Doc_trasporto_rientroBase
        implements AllegatoParentBulk, AllegatoStorePath {

    public static final String TRASPORTO        = "T";
    public static final String RIENTRO          = "R";
    public static final String STATO_INSERITO   = "INS";
    public static final String STATO_INVIATO    = "INV";
    public static final String STATO_DEFINITIVO = "DEF";
    public static final String STATO_ANNULLATO  = "ANN";

    public final static Dictionary STATO;

    static {
        STATO = new it.cnr.jada.util.OrderedHashtable();
        STATO.put(STATO_INSERITO,   "Inserito");
        STATO.put(STATO_INVIATO,    "Inviato In Firma");
        STATO.put(STATO_DEFINITIVO, "Definitivo");
        STATO.put(STATO_ANNULLATO,  "Annullato");
    }

    public static final String TIPO_RITIRO_INCARICATO = "I";
    public static final String TIPO_RITIRO_VETTORE    = "V";

    private static final java.util.Dictionary tipoRitiroKeys;

    static {
        tipoRitiroKeys = new it.cnr.jada.util.OrderedHashtable();
        tipoRitiroKeys.put(TIPO_RITIRO_INCARICATO, "Ritiro Incaricato");
        tipoRitiroKeys.put(TIPO_RITIRO_VETTORE,    "Ritiro Vettore");
    }

    private String local_transactionID;

    protected Id_inventarioBulk          inventario;
    protected Tipo_trasporto_rientroBulk tipoMovimento;
    private   TerzoBulk                  terzoIncRitiro;
    private   TerzoBulk                  terzoRespDip;
    private   TerzoBulk                  terzoSmartworking;

    private AnagraficoBulk anagIncRitiro   = new AnagraficoBulk();
    private AnagraficoBulk anagSmartworking = new AnagraficoBulk();

    private TerzoBulk              consegnatario;
    private TerzoBulk              delegato;
    private Unita_organizzativaBulk uo_consegnataria;

    private java.util.Collection         tipoMovimenti;
    private PrimaryKeyHashtable          accessoriContestualiHash;

    /**
     * Collezione dei dettagli/beni del documento.
     *
     * Il campo JSON è "dettagli" (allineato al body Postman).
     * Viene serializzato/deserializzato come List&lt;Map&lt;String,Object&gt;&gt;
     * per compatibilità con la classe astratta Doc_trasporto_rientro_dettBulk.
     * Il Resource converte ogni Map nella subclasse concreta corretta.
     *
     * Il modificatore {@code transient} esclude il campo dal mapping JADA/SQL;
     * la dettColl viene popolata dal Resource prima di invocare il component.
     */
    @JsonProperty("dettagli")
    private transient List<Map<String, Object>> restDettagli;

    private SimpleBulkList doc_trasporto_rientro_dettColl;

    private BulkList<AllegatoGenericoBulk> archivioAllegati = new BulkList<>();

    /**
     * Allegati binari trasportati via REST.
     *
     * Il campo JSON è "attachments" (allineato al body Postman).
     * Tipo List&lt;Map&gt;: nessuna dipendenza circolare verso il layer REST.
     * Il Resource converte ogni Map in AttachmentDocTrasportoRientro.
     *
     * Il modificatore {@code transient} garantisce che JADA ignori il campo
     * durante il mapping SQL/DB.
     */
    @JsonProperty("attachments")
    private transient List<Map<String, Object>> restAllegati;

    private V_persona_fisicaBulk personaFisicaResponsabile;

    private java.util.Collection condizioni;
    private String               cds_scrivania;
    private String               uo_scrivania;
    private Inventario_beniBulk  bene;

    // =========================================================================
    // COSTRUTTORI
    // =========================================================================

    public Doc_trasporto_rientroBulk() {
        super();
    }

    public Doc_trasporto_rientroBulk(Long pg_inventario, String ti_documento,
                                     Integer esercizio, Long pg_doc_trasporto_rientro) {
        super(pg_inventario, ti_documento, esercizio, pg_doc_trasporto_rientro);
        setInventario(new Id_inventarioBulk(pg_inventario));
    }

    // =========================================================================
    // restDettagli  (JSON "dettagli")
    // =========================================================================
    @JsonProperty("dettagli")
    public List<Map<String, Object>> getRestDettagli() {
        return restDettagli;
    }
    @JsonProperty("dettagli")
    public void setRestDettagli(List<Map<String, Object>> restDettagli) {
        this.restDettagli = restDettagli;
    }

    // =========================================================================
    // restAllegati  (JSON "attachments")
    // =========================================================================
    @JsonProperty("attachments")
    public List<Map<String, Object>> getRestAllegati() {
        return restAllegati;
    }
    @JsonProperty("attachments")
    public void setRestAllegati(List<Map<String, Object>> restAllegati) {
        this.restAllegati = restAllegati;
    }

    // =========================================================================
    // INVENTARIO
    // =========================================================================

    public Id_inventarioBulk getInventario() {
        return inventario;
    }

    public void setInventario(Id_inventarioBulk bulk) {
        inventario = bulk;
    }

    public void setPgInventario(Long pg_inventario) {
        if (this.getInventario() == null) {
            setInventario(new Id_inventarioBulk(pg_inventario));
        } else {
            this.getInventario().setPg_inventario(pg_inventario);
        }
    }
    public Long getPgInventario() {
        Id_inventarioBulk inv = this.getInventario();
        return inv == null ? null : inv.getPg_inventario();
    }

    // =========================================================================
    // TIPO MOVIMENTO
    // =========================================================================

    public Tipo_trasporto_rientroBulk getTipoMovimento() {
        return tipoMovimento;
    }

    public void setTipoMovimento(Tipo_trasporto_rientroBulk bulk) {
        tipoMovimento = bulk;
    }

    public void setCd_tipo_trasporto_rientro(String cd_tipo_trasporto_rientro) {
        if (this.getTipoMovimento() == null) {
            this.setTipoMovimento(new Tipo_trasporto_rientroBulk());
        }
        this.getTipoMovimento().setCdTipoTrasportoRientro(cd_tipo_trasporto_rientro);
    }

    public String getCd_tipo_trasporto_rientro() {
        Tipo_trasporto_rientroBulk tm = this.getTipoMovimento();
        return tm == null ? null : tm.getCdTipoTrasportoRientro();
    }

    // =========================================================================
    // TERZO INCARICATO
    // =========================================================================

    public AnagraficoBulk getAnagIncRitiro() {
        return anagIncRitiro;
    }

    public void setAnagIncRitiro(AnagraficoBulk anagIncRitiro) {
        this.anagIncRitiro = anagIncRitiro;
    }

    public TerzoBulk getTerzoIncRitiro() {
        return terzoIncRitiro;
    }

    public void setTerzoIncRitiro(TerzoBulk terzoIncRitiro) {
        this.terzoIncRitiro = terzoIncRitiro;
    }

    @Override
    public Integer getCdTerzoIncaricato() {
        if (terzoIncRitiro != null) return terzoIncRitiro.getCd_terzo();
        return super.getCdTerzoIncaricato();
    }

    @Override
    public void setCdTerzoIncaricato(Integer cdTerzoIncaricato) {
        super.setCdTerzoIncaricato(cdTerzoIncaricato);
        if (terzoIncRitiro != null && cdTerzoIncaricato != null)
            terzoIncRitiro.setCd_terzo(cdTerzoIncaricato);
    }

    public Integer getCdAnagIncaricato() {
        return anagIncRitiro != null ? anagIncRitiro.getCd_anag() : null;
    }

    public void setCdAnagIncaricato(Integer cdAnag) {
        if (this.anagIncRitiro == null && cdAnag != null)
            this.anagIncRitiro = new AnagraficoBulk();
        if (this.anagIncRitiro != null)
            this.anagIncRitiro.setCd_anag(cdAnag);
    }

    public String getDs_anag_incaricato() {
        if (anagIncRitiro != null && anagIncRitiro.getCognome() != null)
            return anagIncRitiro.getCognome() + " " +
                    (anagIncRitiro.getNome() != null ? anagIncRitiro.getNome() : "");
        return "";
    }

    // =========================================================================
    // TERZO RESPONSABILE
    // =========================================================================

    public TerzoBulk getTerzoRespDip() {
        return terzoRespDip;
    }

    public void setTerzoRespDip(TerzoBulk terzoRespDip) {
        this.terzoRespDip = terzoRespDip;
        if (terzoRespDip != null) setCdTerzoResponsabile(terzoRespDip.getCd_terzo());
        else                      setCdTerzoResponsabile(null);
    }

    @Override
    public void setCdTerzoResponsabile(Integer cdTerzoResponsabile) {
        super.setCdTerzoResponsabile(cdTerzoResponsabile);
        if (terzoRespDip != null && cdTerzoResponsabile != null)
            terzoRespDip.setCd_terzo(cdTerzoResponsabile);
    }

    // =========================================================================
    // SMARTWORKING
    // =========================================================================

    public AnagraficoBulk getAnagSmartworking() {
        return anagSmartworking;
    }

    public void setAnagSmartworking(AnagraficoBulk anagSmartworking) {
        this.anagSmartworking = anagSmartworking;
    }

    public Integer getCdAnagSmartworking() {
        return anagSmartworking != null ? anagSmartworking.getCd_anag() : null;
    }

    public void setCdAnagSmartworking(Integer cdAnag) {
        if (this.anagSmartworking == null && cdAnag != null)
            this.anagSmartworking = new AnagraficoBulk();
        if (this.anagSmartworking != null)
            this.anagSmartworking.setCd_anag(cdAnag);
    }

    public String getDs_anag_smartworking() {
        if (anagSmartworking != null && anagSmartworking.getCognome() != null)
            return anagSmartworking.getCognome() + " " +
                    (anagSmartworking.getNome() != null ? anagSmartworking.getNome() : "");
        return "";
    }

    public TerzoBulk getTerzoSmartworking() {
        return terzoSmartworking;
    }

    public void setTerzoSmartworking(TerzoBulk terzoSmartworking) {
        this.terzoSmartworking = terzoSmartworking;
    }

    public boolean isTerzoSmartworkingVisible() {
        return isSmartworking();
    }

    // =========================================================================
    // CONSEGNATARIO / DELEGATO / UO
    // =========================================================================

    public TerzoBulk getConsegnatario() { return consegnatario; }
    public void setConsegnatario(TerzoBulk consegnatario) { this.consegnatario = consegnatario; }

    public TerzoBulk getDelegato() { return delegato; }
    public void setDelegato(TerzoBulk delegato) { this.delegato = delegato; }

    public Unita_organizzativaBulk getUo_consegnataria() { return uo_consegnataria; }
    public void setUo_consegnataria(Unita_organizzativaBulk bulk) { uo_consegnataria = bulk; }

    // =========================================================================
    // PERSONA FISICA RESPONSABILE
    // =========================================================================

    public V_persona_fisicaBulk getPersonaFisicaResponsabile() {
        return personaFisicaResponsabile;
    }

    public void setPersonaFisicaResponsabile(V_persona_fisicaBulk personaFisicaResponsabile) {
        this.personaFisicaResponsabile = personaFisicaResponsabile;
        if (personaFisicaResponsabile != null && personaFisicaResponsabile.getCd_terzo() != null)
            setCdTerzoResponsabile(personaFisicaResponsabile.getCd_terzo());
    }

    // =========================================================================
    // VARIE
    // =========================================================================

    public Collection getCondizioni() { return condizioni; }
    public void setCondizioni(Collection condizioni) { this.condizioni = condizioni; }

    public String getCds_scrivania() { return cds_scrivania; }
    public void setCds_scrivania(String cds_scrivania) { this.cds_scrivania = cds_scrivania; }

    public String getUo_scrivania() { return uo_scrivania; }
    public void setUo_scrivania(String uo_scrivania) { this.uo_scrivania = uo_scrivania; }

    public Inventario_beniBulk getBene() { return bene; }
    public void setBene(Inventario_beniBulk bene) { this.bene = bene; }

    public java.util.Collection getTipoMovimenti() { return tipoMovimenti; }
    public void setTipoMovimenti(java.util.Collection collection) { tipoMovimenti = collection; }

    public PrimaryKeyHashtable getAccessoriContestualiHash() { return accessoriContestualiHash; }
    public void setAccessoriContestualiHash(PrimaryKeyHashtable hashtable) { accessoriContestualiHash = hashtable; }

    public String getLocal_transactionID() { return local_transactionID; }
    public void setLocal_transactionID(String local_transactionID) { this.local_transactionID = local_transactionID; }

    // =========================================================================
    // DETTAGLI COLL
    // =========================================================================

    public SimpleBulkList getDoc_trasporto_rientro_dettColl() {
        return doc_trasporto_rientro_dettColl;
    }

    public void setDoc_trasporto_rientro_dettColl(SimpleBulkList list) {
        doc_trasporto_rientro_dettColl = list;
    }

    public BulkCollection[] getBulkLists() {
        return new BulkCollection[]{ this.getDoc_trasporto_rientro_dettColl() };
    }

    public Doc_trasporto_rientro_dettBulk removeFromDoc_trasporto_rientro_dettColl(int indiceDiLinea) {
        return (Doc_trasporto_rientro_dettBulk) doc_trasporto_rientro_dettColl.remove(indiceDiLinea);
    }

    public int addToDoc_trasporto_rientro_dettColl(Doc_trasporto_rientro_dettBulk nuovo) {
        nuovo.setDoc_trasporto_rientro(this);
        getDoc_trasporto_rientro_dettColl().add(nuovo);

        nuovo.setBene(new Inventario_beniBulk());
        nuovo.getBene().setInventario(this.getInventario());
        nuovo.getBene().setPg_inventario(this.getPgInventario());
        nuovo.getBene().setFl_totalmente_scaricato(Boolean.FALSE);

        return getDoc_trasporto_rientro_dettColl().size() - 1;
    }

    // =========================================================================
    // ARCHIVIO ALLEGATI
    // =========================================================================

    @Override
    public BulkList<AllegatoGenericoBulk> getArchivioAllegati() { return archivioAllegati; }

    @Override
    public void setArchivioAllegati(BulkList<AllegatoGenericoBulk> archivioAllegati) {
        this.archivioAllegati = archivioAllegati;
    }

    @Override
    public int addToArchivioAllegati(AllegatoGenericoBulk allegato) {
        if (allegato != null) archivioAllegati.add(allegato);
        return archivioAllegati.size() - 1;
    }

    public AllegatoGenericoBulk removeFromArchivioAllegati(int index) {
        return getArchivioAllegati().remove(index);
    }

    // =========================================================================
    // STATO
    // =========================================================================

    public Dictionary getStatoKeys() { return STATO; }

    public Dictionary getStatoKeysForSearch() {
        it.cnr.jada.util.OrderedHashtable d = (it.cnr.jada.util.OrderedHashtable) getStatoKeys();
        if (d == null) return null;
        return (it.cnr.jada.util.OrderedHashtable) d.clone();
    }

    public Dictionary getStatoKeysForUpdate() {
        Dictionary stato = new it.cnr.jada.util.OrderedHashtable();
        if      (isInserito())       { stato.put(STATO_INSERITO, "Inserito");         stato.put(STATO_INVIATO, "Inviato In Firma"); }
        else if (isInviatoInFirma()) { stato.put(STATO_INVIATO, "Inviato In Firma");  stato.put(STATO_DEFINITIVO, "Definitivo");  stato.put(STATO_ANNULLATO, "Annullato"); }
        else if (isDefinitivo())     { stato.put(STATO_DEFINITIVO, "Definitivo");     stato.put(STATO_ANNULLATO, "Annullato"); }
        else if (isAnnullato())      { stato.put(STATO_ANNULLATO, "Annullato"); }
        else {
            stato.put(STATO_INSERITO, "Inserito"); stato.put(STATO_INVIATO, "Inviato In Firma");
            stato.put(STATO_DEFINITIVO, "Definitivo"); stato.put(STATO_ANNULLATO, "Annullato");
        }
        return stato;
    }

    private boolean hasStato(String s) { return s != null && s.equals(getStato()); }

    public boolean isInserito()        { return hasStato(STATO_INSERITO); }
    public boolean isInviatoInFirma()  { return hasStato(STATO_INVIATO); }
    public boolean isDefinitivo()      { return hasStato(STATO_DEFINITIVO); }
    public boolean isAnnullato()       { return hasStato(STATO_ANNULLATO); }

    public boolean isModificabile() {
        if (isDefinitivoCompletamente() || isInviatoInFirma()) return false;
        return isInserito();
    }

    public boolean isAnnullabile() { return !isAnnullato(); }

    // =========================================================================
    // TIPO RITIRO
    // =========================================================================

    public java.util.Dictionary getTipoRitiroKeys() { return tipoRitiroKeys; }

    public String getTipoRitiro() {
        if (Boolean.TRUE.equals(getFlIncaricato())) return TIPO_RITIRO_INCARICATO;
        if (Boolean.TRUE.equals(getFlVettore()))    return TIPO_RITIRO_VETTORE;
        return null;
    }

    public void setTipoRitiro(String tipoRitiro) {
        setFlIncaricato(TIPO_RITIRO_INCARICATO.equals(tipoRitiro));
        setFlVettore(TIPO_RITIRO_VETTORE.equals(tipoRitiro));
        if (TIPO_RITIRO_INCARICATO.equals(tipoRitiro))  setNominativoVettore(null);
        else if (TIPO_RITIRO_VETTORE.equals(tipoRitiro)) setTerzoIncRitiro(null);
    }

    public boolean hasTipoRitiroSelezionato() { return getTipoRitiro() != null; }
    public boolean isRitiroIncaricato()        { return TIPO_RITIRO_INCARICATO.equals(getTipoRitiro()); }
    public boolean isRitiroVettore()           { return TIPO_RITIRO_VETTORE.equals(getTipoRitiro()); }
    public boolean isNominativoVettoreVisible(){ return isRitiroVettore(); }
    public boolean isTrasporto()               { return TRASPORTO.equals(getTiDocumento()); }
    public boolean isRientro()                 { return RIENTRO.equals(getTiDocumento()); }

    public boolean hasDettagli() {
        return doc_trasporto_rientro_dettColl != null && doc_trasporto_rientro_dettColl.size() > 0;
    }

    public boolean isTemporaneo() {
        return getPgDocTrasportoRientro() == null
                || getPgDocTrasportoRientro().compareTo(0L) <= 0;
    }

    public boolean isNoteRitiroEnabled() {
        if (!hasTipoRitiroSelezionato()) return false;
        if (tipoMovimento != null) {
            String ds = tipoMovimento.getDsTipoTrasportoRientro();
            return "Sostituzione per".equals(ds) || "Altro".equals(ds);
        }
        return false;
    }

    public String getDocTrasportoRiferimento() {
        if (!RIENTRO.equals(this.getTiDocumento())) return null;
        if (this.doc_trasporto_rientro_dettColl == null || this.doc_trasporto_rientro_dettColl.isEmpty()) return null;
        Doc_trasporto_rientro_dettBulk primo =
                (Doc_trasporto_rientro_dettBulk) this.doc_trasporto_rientro_dettColl.get(0);
        if (primo.getPgDocTrasportoRientroRif() == null) return null;
        return primo.getEsercizioRif() + "/" + primo.getTiDocumentoRif() + "/" + primo.getPgDocTrasportoRientroRif();
    }

    // =========================================================================
    // FIRMA / FLUSSO HAPPYSIGN
    // =========================================================================

    public boolean isFirmabile()            { return isInviatoInFirma() && !isDefinitivoCompletamente(); }
    public boolean isDefinitivoCompletamente() { return "FIR".equals(getStatoFlusso()) || isDefinitivo(); }
    public boolean hasFlussoFirmaAttivo()   { return getIdFlussoHappysign() != null && !isDefinitivoCompletamente(); }
    public int getNumeroFirmatariRichiesti(){ return isRitiroIncaricato() ? 3 : 2; }

    public void inizializzaPerInvioFirma() {
        setStatoFlusso("INV");
        setDataInvioFirma(new java.sql.Timestamp(System.currentTimeMillis()));
    }

    public void aggiornaDopoFirmaCompletata() {
        setStato(STATO_DEFINITIVO);
        setStatoFlusso("FIR");
        setDataFirma(new java.sql.Timestamp(System.currentTimeMillis()));
        setNoteRifiuto(null);
    }

    public void aggiornaDopoRifiutoFirma(String motivoRifiuto) {
        setStato(STATO_INSERITO);
        setStatoFlusso("RIF");
        setNoteRifiuto(motivoRifiuto);
        setIdFlussoHappysign(null);
        setDataInvioFirma(null);
        setDataFirma(null);
    }

    public void resetFlussoFirma() {
        setIdFlussoHappysign(null);
        setStatoFlusso(null);
        setDataInvioFirma(null);
        setDataFirma(null);
        setNoteRifiuto(null);
    }

    public boolean isInviabileAllaFirma() {
        return isInviatoInFirma()
                && getIdFlussoHappysign() == null
                && getConsegnatario() != null
                && getCdTerzoResponsabile() != null
                && hasDettagli();
    }

    public boolean isRifiutatoInFirma()  { return "RIF".equals(getStatoFlusso()); }
    public boolean isInviatoAlFlusso()   { return "INV".equals(getStatoFlusso()) && getIdFlussoHappysign() != null; }

    // =========================================================================
    // VALIDAZIONE
    // =========================================================================

    @Override
    public void validate() throws ValidationException {
        validaCampoObbligatorio(getDsDocTrasportoRientro(), "la Descrizione del documento");
        validaCampoObbligatorio(getDataRegistrazione(),
                "la Data " + (TRASPORTO.equals(getTiDocumento()) ? "Trasporto" : "Rientro"));
        validaCampoObbligatorio(getTipoMovimento(), "il Tipo Movimento");
        super.validate();
    }

    private void validaCampoObbligatorio(Object valore, String nomeCampo) throws ValidationException {
        if (valore == null) throw new ValidationException("Indicare " + nomeCampo + ".");
        if (valore instanceof String && ((String) valore).trim().isEmpty())
            throw new ValidationException("Indicare " + nomeCampo + ".");
    }

    // =========================================================================
    // SMARTWORKING CHECK
    // =========================================================================

    public boolean isSmartworking() {
        return getTipoMovimento() != null
                && getTipoMovimento().getDsTipoTrasportoRientro() != null
                && getTipoMovimento().getDsTipoTrasportoRientro().equalsIgnoreCase("SMARTWORKING");
    }

    // =========================================================================
    // CMIS / RICERCA
    // =========================================================================

    public String constructCMISNomeFile() {
        return StrServ.lpad(this.getPgDocTrasportoRientro().toString(), 9, "0");
    }

    public String recuperoIdDocAsString() {
        return StrServ.replace(getTiDocumento(), ".", "")
                + getEsercizio()
                + StrServ.lpad(getPgInventario().toString(), 5)
                + StrServ.lpad(getPgDocTrasportoRientro().toString(), 5);
    }

    @Override
    public CompoundFindClause buildFindClauses(Boolean freeSearch) {
        CompoundFindClause clauses = getBulkInfo().buildFindClausesFrom(this, freeSearch);
        if (clauses == null) clauses = new it.cnr.jada.persistency.sql.CompoundFindClause();
        if (getTiDocumento() != null)
            clauses.addClause("AND", "tiDocumento",
                    it.cnr.jada.persistency.sql.SQLBuilder.EQUALS, getTiDocumento());
        return clauses;
    }

    public Long getPgDocTrasportoRientroVisualizzato() {
        Long pg = getPgDocTrasportoRientro();
        return (pg != null && pg.longValue() < 0) ? null : pg;
    }

    public void setPgDocTrasportoRientroVisualizzato(Long value) { }


}
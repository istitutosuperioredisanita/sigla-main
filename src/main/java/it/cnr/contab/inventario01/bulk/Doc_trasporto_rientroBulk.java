package it.cnr.contab.inventario01.bulk;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.contab.anagraf00.core.bulk.V_persona_fisicaBulk;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;
import it.cnr.contab.inventario00.docs.bulk.InventarioDocTRBulk;
import it.cnr.contab.inventario00.tabrif.bulk.Id_inventarioBulk;
import it.cnr.contab.inventario00.tabrif.bulk.Tipo_trasporto_rientroBulk;
import it.cnr.contab.util00.bulk.storage.AllegatoGenericoBulk;
import it.cnr.contab.util00.bulk.storage.AllegatoParentBulk;
import it.cnr.contab.util00.bulk.storage.AllegatoStorePath;
import it.cnr.jada.bulk.BulkCollection;
import it.cnr.jada.bulk.BulkList;
import it.cnr.jada.bulk.PrimaryKeyHashtable;
import it.cnr.jada.bulk.SimpleBulkList;
import it.cnr.jada.bulk.ValidationException;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;
import it.cnr.jada.util.OrderedHashtable;
import it.cnr.jada.util.StrServ;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Testata del documento di Trasporto/Rientro beni inventariali.
 *
 * Gestisce:
 * - stato documento;
 * - tipo ritiro;
 * - dati firma HappySign;
 * - dettagli beni;
 * - allegati;
 * - campi REST transitori.
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

    public static final String TRASPORTO = "T";
    public static final String RIENTRO = "R";

    public static final String STATO_INSERITO = "INS";
    public static final String STATO_INVIATO = "INV";
    public static final String STATO_DEFINITIVO = "DEF";
    public static final String STATO_ANNULLATO = "ANN";

    public static final String STATO_FLUSSO_INVIATO = "INV";
    public static final String STATO_FLUSSO_FIRMATO = "FIR";
    public static final String STATO_FLUSSO_RIFIUTATO = "RIF";

    public static final String TIPO_RITIRO_INCARICATO = "I";
    public static final String TIPO_RITIRO_VETTORE = "V";

    private static final String TIPO_MOVIMENTO_SMARTWORKING = "SMARTWORKING";

    public static final Dictionary STATO;
    private static final Dictionary TIPO_RITIRO_KEYS;

    static {
        STATO = new OrderedHashtable();
        STATO.put(STATO_INSERITO, "Inserito");
        STATO.put(STATO_INVIATO, "Inviato In Firma");
        STATO.put(STATO_DEFINITIVO, "Definitivo");
        STATO.put(STATO_ANNULLATO, "Annullato");

        TIPO_RITIRO_KEYS = new OrderedHashtable();
        TIPO_RITIRO_KEYS.put(TIPO_RITIRO_INCARICATO, "Ritiro Incaricato");
        TIPO_RITIRO_KEYS.put(TIPO_RITIRO_VETTORE, "Ritiro Vettore");
    }

    protected Id_inventarioBulk inventario;
    protected Tipo_trasporto_rientroBulk tipoMovimento;

    private TerzoBulk terzoIncRitiro;
    private TerzoBulk terzoRespDip;
    private TerzoBulk terzoSmartworking;

    private TerzoBulk consegnatario;
    private TerzoBulk delegato;
    private Unita_organizzativaBulk uo_consegnataria;

    private Collection tipoMovimenti;
    private PrimaryKeyHashtable accessoriContestualiHash;

    @JsonProperty("dettagli")
    private transient List<Map<String, Object>> restDettagli;

    @JsonProperty("attachments")
    private transient List<Map<String, Object>> restAllegati;

    private SimpleBulkList doc_trasporto_rientro_dettColl;
    private BulkList<AllegatoGenericoBulk> archivioAllegati = new BulkList<>();

    private V_persona_fisicaBulk personaFisicaResponsabile;

    private Collection condizioni;
    private String cds_scrivania;
    private String uo_scrivania;
    private InventarioDocTRBulk bene;

    public Doc_trasporto_rientroBulk() {
        super();
    }

    public Doc_trasporto_rientroBulk(
            Long pgInventario,
            String tiDocumento,
            Integer esercizio,
            Long pgDocTrasportoRientro) {

        super(pgInventario, tiDocumento, esercizio, pgDocTrasportoRientro);
        setInventario(new Id_inventarioBulk(pgInventario));
    }

    @JsonProperty("dettagli")
    public List<Map<String, Object>> getRestDettagli() {
        return restDettagli;
    }

    @JsonProperty("dettagli")
    public void setRestDettagli(List<Map<String, Object>> restDettagli) {
        this.restDettagli = restDettagli;
    }

    @JsonProperty("attachments")
    public List<Map<String, Object>> getRestAllegati() {
        return restAllegati;
    }

    @JsonProperty("attachments")
    public void setRestAllegati(List<Map<String, Object>> restAllegati) {
        this.restAllegati = restAllegati;
    }

    public Id_inventarioBulk getInventario() {
        return inventario;
    }

    public void setInventario(Id_inventarioBulk inventario) {
        this.inventario = inventario;
    }

    public Long getPgInventario() {
        return inventario == null ? null : inventario.getPg_inventario();
    }

    public void setPgInventario(Long pgInventario) {
        if (inventario == null) {
            inventario = new Id_inventarioBulk(pgInventario);
        } else {
            inventario.setPg_inventario(pgInventario);
        }
    }

    public Tipo_trasporto_rientroBulk getTipoMovimento() {
        return tipoMovimento;
    }

    public void setTipoMovimento(Tipo_trasporto_rientroBulk tipoMovimento) {
        this.tipoMovimento = tipoMovimento;
    }

    public String getCd_tipo_trasporto_rientro() {
        return tipoMovimento == null
                ? null
                : tipoMovimento.getCdTipoTrasportoRientro();
    }

    public void setCd_tipo_trasporto_rientro(String cdTipoTrasportoRientro) {
        if (tipoMovimento == null) {
            tipoMovimento = new Tipo_trasporto_rientroBulk();
        }

        tipoMovimento.setCdTipoTrasportoRientro(cdTipoTrasportoRientro);
    }

    public TerzoBulk getTerzoIncRitiro() {
        return terzoIncRitiro;
    }

    public void setTerzoIncRitiro(TerzoBulk terzoIncRitiro) {
        this.terzoIncRitiro = terzoIncRitiro;

        setCdTerzoIncaricato(
                terzoIncRitiro == null ? null : terzoIncRitiro.getCd_terzo()
        );
    }

    @Override
    public Integer getCdTerzoIncaricato() {
        return terzoIncRitiro != null
                ? terzoIncRitiro.getCd_terzo()
                : super.getCdTerzoIncaricato();
    }

    @Override
    public void setCdTerzoIncaricato(Integer cdTerzoIncaricato) {
        super.setCdTerzoIncaricato(cdTerzoIncaricato);

        if (terzoIncRitiro != null && cdTerzoIncaricato != null) {
            terzoIncRitiro.setCd_terzo(cdTerzoIncaricato);
        }
    }

    public TerzoBulk getTerzoRespDip() {
        return terzoRespDip;
    }

    public void setTerzoRespDip(TerzoBulk terzoRespDip) {
        this.terzoRespDip = terzoRespDip;

        setCdTerzoResponsabile(
                terzoRespDip == null ? null : terzoRespDip.getCd_terzo()
        );
    }

    @Override
    public void setCdTerzoResponsabile(Integer cdTerzoResponsabile) {
        super.setCdTerzoResponsabile(cdTerzoResponsabile);

        if (terzoRespDip != null && cdTerzoResponsabile != null) {
            terzoRespDip.setCd_terzo(cdTerzoResponsabile);
        }
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

    public TerzoBulk getConsegnatario() {
        return consegnatario;
    }

    public void setConsegnatario(TerzoBulk consegnatario) {
        this.consegnatario = consegnatario;
    }

    public TerzoBulk getDelegato() {
        return delegato;
    }

    public void setDelegato(TerzoBulk delegato) {
        this.delegato = delegato;
    }

    public Unita_organizzativaBulk getUo_consegnataria() {
        return uo_consegnataria;
    }

    public void setUo_consegnataria(Unita_organizzativaBulk uoConsegnataria) {
        this.uo_consegnataria = uoConsegnataria;
    }

    public V_persona_fisicaBulk getPersonaFisicaResponsabile() {
        return personaFisicaResponsabile;
    }

    public void setPersonaFisicaResponsabile(V_persona_fisicaBulk personaFisicaResponsabile) {
        this.personaFisicaResponsabile = personaFisicaResponsabile;

        if (personaFisicaResponsabile != null
                && personaFisicaResponsabile.getCd_terzo() != null) {
            setCdTerzoResponsabile(personaFisicaResponsabile.getCd_terzo());
        }
    }

    public Collection getCondizioni() {
        return condizioni;
    }

    public void setCondizioni(Collection condizioni) {
        this.condizioni = condizioni;
    }

    public String getCds_scrivania() {
        return cds_scrivania;
    }

    public void setCds_scrivania(String cdsScrivania) {
        this.cds_scrivania = cdsScrivania;
    }

    public String getUo_scrivania() {
        return uo_scrivania;
    }

    public void setUo_scrivania(String uoScrivania) {
        this.uo_scrivania = uoScrivania;
    }

    public InventarioDocTRBulk getBene() {
        return bene;
    }

    public void setBene(InventarioDocTRBulk bene) {
        this.bene = bene;
    }

    public Collection getTipoMovimenti() {
        return tipoMovimenti;
    }

    public void setTipoMovimenti(Collection tipoMovimenti) {
        this.tipoMovimenti = tipoMovimenti;
    }

    public PrimaryKeyHashtable getAccessoriContestualiHash() {
        return accessoriContestualiHash;
    }

    public void setAccessoriContestualiHash(PrimaryKeyHashtable accessoriContestualiHash) {
        this.accessoriContestualiHash = accessoriContestualiHash;
    }

    public SimpleBulkList getDoc_trasporto_rientro_dettColl() {
        return doc_trasporto_rientro_dettColl;
    }

    public void setDoc_trasporto_rientro_dettColl(SimpleBulkList dettagli) {
        this.doc_trasporto_rientro_dettColl = dettagli;
    }

    public BulkCollection[] getBulkLists() {
        return new BulkCollection[]{getDoc_trasporto_rientro_dettColl()};
    }

    public int addToDoc_trasporto_rientro_dettColl(Doc_trasporto_rientro_dettBulk nuovo) {
        if (doc_trasporto_rientro_dettColl == null) {
            doc_trasporto_rientro_dettColl = new SimpleBulkList();
        }

        nuovo.setDoc_trasporto_rientro(this);
        doc_trasporto_rientro_dettColl.add(nuovo);

        nuovo.setBene(new InventarioDocTRBulk());
        nuovo.getBene().setInventario(getInventario());
        nuovo.getBene().setPg_inventario(getPgInventario());
        nuovo.getBene().setFl_totalmente_scaricato(Boolean.FALSE);

        return doc_trasporto_rientro_dettColl.size() - 1;
    }

    public Doc_trasporto_rientro_dettBulk removeFromDoc_trasporto_rientro_dettColl(int index) {
        if (doc_trasporto_rientro_dettColl == null) {
            return null;
        }

        return (Doc_trasporto_rientro_dettBulk) doc_trasporto_rientro_dettColl.remove(index);
    }

    @Override
    public BulkList<AllegatoGenericoBulk> getArchivioAllegati() {
        if (archivioAllegati == null) {
            archivioAllegati = new BulkList<>();
        }

        return archivioAllegati;
    }

    @Override
    public void setArchivioAllegati(BulkList<AllegatoGenericoBulk> archivioAllegati) {
        this.archivioAllegati = archivioAllegati == null
                ? new BulkList<>()
                : archivioAllegati;
    }

    @Override
    public int addToArchivioAllegati(AllegatoGenericoBulk allegato) {
        if (allegato != null) {
            getArchivioAllegati().add(allegato);
        }

        return getArchivioAllegati().size() - 1;
    }

    public AllegatoGenericoBulk removeFromArchivioAllegati(int index) {
        return getArchivioAllegati().remove(index);
    }

    public Dictionary getStatoKeys() {
        return STATO;
    }

    public Dictionary getStatoKeysForSearch() {
        return cloneDictionary(STATO);
    }

    public Dictionary getStatoKeysForUpdate() {
        Dictionary stato = new OrderedHashtable();

        if (isInserito()) {
            stato.put(STATO_INSERITO, "Inserito");
            stato.put(STATO_INVIATO, "Inviato In Firma");
            return stato;
        }

        if (isInviatoInFirma()) {
            stato.put(STATO_INVIATO, "Inviato In Firma");
            stato.put(STATO_DEFINITIVO, "Definitivo");
            stato.put(STATO_ANNULLATO, "Annullato");
            return stato;
        }

        if (isDefinitivo()) {
            stato.put(STATO_DEFINITIVO, "Definitivo");
            stato.put(STATO_ANNULLATO, "Annullato");
            return stato;
        }

        if (isAnnullato()) {
            stato.put(STATO_ANNULLATO, "Annullato");
            return stato;
        }

        stato.put(STATO_INSERITO, "Inserito");
        stato.put(STATO_INVIATO, "Inviato In Firma");
        stato.put(STATO_DEFINITIVO, "Definitivo");
        stato.put(STATO_ANNULLATO, "Annullato");

        return stato;
    }

    private Dictionary cloneDictionary(Dictionary dictionary) {
        if (dictionary instanceof OrderedHashtable) {
            return (Dictionary) ((OrderedHashtable) dictionary).clone();
        }

        return dictionary;
    }

    private boolean hasStato(String stato) {
        return stato != null && stato.equals(getStato());
    }

    public boolean isInserito() {
        return hasStato(STATO_INSERITO);
    }

    public boolean isInviatoInFirma() {
        return hasStato(STATO_INVIATO);
    }

    public boolean isDefinitivo() {
        return hasStato(STATO_DEFINITIVO);
    }

    public boolean isAnnullato() {
        return hasStato(STATO_ANNULLATO);
    }

    public boolean isModificabile() {
        return isInserito()
                && !isAnnullato()
                && !isDefinitivo()
                && !isInAttesaDiFirma()
                && !isFirmatoDaCompletare();
    }

    public boolean isAnnullabile() {
        return !isAnnullato();
    }

    public Dictionary getTipoRitiroKeys() {
        return TIPO_RITIRO_KEYS;
    }

    public String getTipoRitiro() {
        if (Boolean.TRUE.equals(getFlIncaricato())) {
            return TIPO_RITIRO_INCARICATO;
        }

        if (Boolean.TRUE.equals(getFlVettore())) {
            return TIPO_RITIRO_VETTORE;
        }

        return null;
    }

    public void setTipoRitiro(String tipoRitiro) {
        setFlIncaricato(TIPO_RITIRO_INCARICATO.equals(tipoRitiro));
        setFlVettore(TIPO_RITIRO_VETTORE.equals(tipoRitiro));

        if (TIPO_RITIRO_INCARICATO.equals(tipoRitiro)) {
            setNominativoVettore(null);
        } else if (TIPO_RITIRO_VETTORE.equals(tipoRitiro)) {
            setTerzoIncRitiro(null);
        }
    }

    public boolean hasTipoRitiroSelezionato() {
        return getTipoRitiro() != null;
    }

    public boolean isRitiroIncaricato() {
        return TIPO_RITIRO_INCARICATO.equals(getTipoRitiro());
    }

    public boolean isRitiroVettore() {
        return TIPO_RITIRO_VETTORE.equals(getTipoRitiro());
    }

    public boolean isNominativoVettoreVisible() {
        return isRitiroVettore();
    }

    public boolean isTrasporto() {
        return TRASPORTO.equals(getTiDocumento());
    }

    public boolean isRientro() {
        return RIENTRO.equals(getTiDocumento());
    }

    public boolean hasChiaveDocumentoCompleta() {
        return getPgInventario() != null
                && getTiDocumento() != null
                && getEsercizio() != null
                && getPgDocTrasportoRientro() != null;
    }

    public boolean hasDettagli() {
        return doc_trasporto_rientro_dettColl != null
                && !doc_trasporto_rientro_dettColl.isEmpty();
    }

    public boolean isTemporaneo() {
        return getPgDocTrasportoRientro() == null
                || getPgDocTrasportoRientro() <= 0L;
    }

    public boolean isNoteRitiroEnabled() {
        if (!hasTipoRitiroSelezionato() || tipoMovimento == null) {
            return false;
        }

        String descrizione = tipoMovimento.getDsTipoTrasportoRientro();

        return "Sostituzione per".equals(descrizione)
                || "Altro".equals(descrizione);
    }

    public String getDocTrasportoRiferimento() {
        if (!isRientro() || !hasDettagli()) {
            return null;
        }

        Doc_trasporto_rientro_dettBulk primo =
                (Doc_trasporto_rientro_dettBulk) doc_trasporto_rientro_dettColl.get(0);

        if (primo == null || primo.getPgDocTrasportoRientroRif() == null) {
            return null;
        }

        return primo.getEsercizioRif()
                + "/"
                + primo.getTiDocumentoRif()
                + "/"
                + primo.getPgDocTrasportoRientroRif();
    }

    public boolean isInviabileAllaFirma() {
        return isInserito()
                && getUuidFlussoAutorizzativo() == null
                && getCdTerzoResponsabile() != null
                && hasDettagli()
                && (
                isRitiroVettore()
                        || isSmartworking()
                        || (isRitiroIncaricato() && getCdTerzoIncaricato() != null)
        );
    }

    public void inizializzaPerInvioFirma() {
        setStato(STATO_INVIATO);
        setStatoFlusso(STATO_FLUSSO_INVIATO);
        setDataInvioFirma(now());
        setDataFirma(null);
        setNoteRifiuto(null);
    }

    public void aggiornaDopoFirmaCompletata() {
        setStato(STATO_INSERITO);
        setStatoFlusso(STATO_FLUSSO_FIRMATO);
        setDataFirma(now());
        setNoteRifiuto(null);
    }

    public void aggiornaDopoRifiutoFirma(String motivoRifiuto) {
        setStato(STATO_INSERITO);
        setStatoFlusso(STATO_FLUSSO_RIFIUTATO);
        setNoteRifiuto(motivoRifiuto);
        setUuidFlussoAutorizzativo(null);
        setDataInvioFirma(null);
        setDataFirma(null);
    }

    public void resetFlussoFirma() {
        setUuidFlussoAutorizzativo(null);
        setStatoFlusso(null);
        setDataInvioFirma(null);
        setDataFirma(null);
        setNoteRifiuto(null);
    }

    public boolean isRifiutatoInFirma() {
        return STATO_FLUSSO_RIFIUTATO.equals(getStatoFlusso());
    }

    public boolean isInAttesaDiFirma() {
        return isInviatoInFirma()
                && getUuidFlussoAutorizzativo() != null
                && STATO_FLUSSO_INVIATO.equals(getStatoFlusso())
                && getDataFirma() == null;
    }

    public boolean isFirmatoDaCompletare() {
        return isInserito()
                && getUuidFlussoAutorizzativo() != null
                && STATO_FLUSSO_FIRMATO.equals(getStatoFlusso())
                && getDataFirma() != null
                && !isDefinitivo()
                && !isAnnullato();
    }

    public boolean hasFlussoFirmaAttivo() {
        return isInAttesaDiFirma();
    }

    public boolean isDefinitivoCompletamente() {
        return isDefinitivo();
    }

    public boolean hasDataFirmaValorizzata() {
        return getDataFirma() != null;
    }

    public boolean isAllegatiGestibiliDopoFirma() {
        return hasDataFirmaValorizzata()
                && !isDefinitivo()
                && !isAnnullato();
    }

    private Timestamp now() {
        return new Timestamp(System.currentTimeMillis());
    }

    @Override
    public void validate() throws ValidationException {
        validaCampoObbligatorio(
                getDsDocTrasportoRientro(),
                "la Descrizione del documento"
        );

        validaCampoObbligatorio(
                getDataRegistrazione(),
                "la Data " + (isTrasporto() ? "Trasporto" : "Rientro")
        );

        validaCampoObbligatorio(getTipoMovimento(), "il Tipo Movimento");

        super.validate();
    }

    private void validaCampoObbligatorio(Object valore, String nomeCampo)
            throws ValidationException {

        if (valore == null) {
            throw new ValidationException("Indicare " + nomeCampo + ".");
        }

        if (valore instanceof String && ((String) valore).trim().isEmpty()) {
            throw new ValidationException("Indicare " + nomeCampo + ".");
        }
    }

    public boolean isSmartworking() {
        return tipoMovimento != null
                && tipoMovimento.getDsTipoTrasportoRientro() != null
                && TIPO_MOVIMENTO_SMARTWORKING.equalsIgnoreCase(
                tipoMovimento.getDsTipoTrasportoRientro().trim()
        );
    }

    public String constructCMISNomeFile() {
        Long pg = getPgDocTrasportoRientro();

        if (pg == null) {
            return "";
        }

        return StrServ.lpad(pg.toString(), 9, "0");
    }

    public String recuperoIdDocAsString() {
        String tipoDocumento = getTiDocumento() == null
                ? ""
                : StrServ.replace(getTiDocumento(), ".", "");

        String esercizio = getEsercizio() == null
                ? ""
                : getEsercizio().toString();

        String pgInventario = getPgInventario() == null
                ? ""
                : StrServ.lpad(getPgInventario().toString(), 5);

        String pgDocumento = getPgDocTrasportoRientro() == null
                ? ""
                : StrServ.lpad(getPgDocTrasportoRientro().toString(), 5);

        return tipoDocumento + esercizio + pgInventario + pgDocumento;
    }

    @Override
    public CompoundFindClause buildFindClauses(Boolean freeSearch) {
        CompoundFindClause clauses =
                getBulkInfo().buildFindClausesFrom(this, freeSearch);

        if (clauses == null) {
            clauses = new CompoundFindClause();
        }

        if (getTiDocumento() != null) {
            clauses.addClause(
                    "AND",
                    "tiDocumento",
                    SQLBuilder.EQUALS,
                    getTiDocumento()
            );
        }

        return clauses;
    }

    public Long getPgDocTrasportoRientroVisualizzato() {
        Long pg = getPgDocTrasportoRientro();
        return pg != null && pg < 0L ? null : pg;
    }

    public void setPgDocTrasportoRientroVisualizzato(Long value) {
        // Campo solo visualizzato: nessuna assegnazione necessaria.
    }

    private boolean same(Object a, Object b) {
        return Objects.equals(a, b);
    }
}
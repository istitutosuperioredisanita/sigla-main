package it.cnr.contab.inventario01.bulk;

import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.contab.anagraf00.core.bulk.V_persona_fisicaBulk;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;
import it.cnr.contab.inventario00.docs.bulk.Inventario_beniBulk;
import it.cnr.contab.inventario00.tabrif.bulk.Id_inventarioBulk;
import it.cnr.contab.inventario00.tabrif.bulk.Tipo_trasporto_rientroBulk;
import it.cnr.contab.service.SpringUtil;
import it.cnr.contab.spring.service.StorePath;
import it.cnr.contab.util00.bulk.storage.AllegatoGenericoBulk;
import it.cnr.contab.util00.bulk.storage.AllegatoParentBulk;
import it.cnr.jada.bulk.*;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.PersistentHome;
import it.cnr.jada.util.StrServ;
import it.cnr.si.spring.storage.StorageDriver;

import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.stream.Collectors;

/**
 * Testata del documento di Trasporto o Rientro beni inventariali.
 * La classe gestisce le informazioni principali del movimento e la collezione degli allegati e dei dettagli.
 */
public class Doc_trasporto_rientroBulk extends Doc_trasporto_rientroBase implements AllegatoParentBulk {

    // ========================================
    // COSTANTI TIPO DOCUMENTO
    // ========================================

    public static final String TRASPORTO = "T";
    public static final String RIENTRO = "R";

    // ========================================
    // COSTANTI STATO DOCUMENTO
    // ========================================

    public static final String STATO_INSERITO = "INS";
    public static final String STATO_INVIATO = "INV";
    public static final String STATO_DEFINITIVO = "DEF";
    public static final String STATO_ANNULLATO = "ANN";

    public final static Dictionary STATO;

    static {
        STATO = new it.cnr.jada.util.OrderedHashtable();
        STATO.put(STATO_INSERITO, "Inserito");
        STATO.put(STATO_INVIATO, "Inviato In Firma");
        STATO.put(STATO_DEFINITIVO, "Definitivo");
        STATO.put(STATO_ANNULLATO, "Annullato");
    }

    // ========================================
    // COSTANTI TIPO RITIRO
    // ========================================

    public static final String TIPO_RITIRO_INCARICATO = "I";
    public static final String TIPO_RITIRO_VETTORE = "V";

    private static final java.util.Dictionary tipoRitiroKeys;

    static {
        tipoRitiroKeys = new it.cnr.jada.util.OrderedHashtable();
        tipoRitiroKeys.put(TIPO_RITIRO_INCARICATO, "Ritiro Incaricato");
        tipoRitiroKeys.put(TIPO_RITIRO_VETTORE, "Ritiro Vettore");
    }

    // ========================================
    // ATTRIBUTI (FK mappate e locali)
    // ========================================

    private String local_transactionID;

    // FK MAPPATE nel BulkPersistentInfo
    protected Id_inventarioBulk inventario;
    protected Tipo_trasporto_rientroBulk tipoMovimento;
    private TerzoBulk terzoIncRitiro;          // FK CD_TERZO_ASSEGNATARIO
    private TerzoBulk terzoRespDip;       // FK CD_TERZO_RESPONSABILE

    // Attributi NON mappati (transient - solo per uso applicativo)
    private TerzoBulk consegnatario;
    private TerzoBulk delegato;
    private Unita_organizzativaBulk uo_consegnataria;

    // Collections
    private java.util.Collection tipoMovimenti;
    private PrimaryKeyHashtable accessoriContestualiHash;
    private SimpleBulkList doc_trasporto_rientro_dettColl;

    // Gestione Allegati
    private BulkList<AllegatoGenericoBulk> archivioAllegati = new BulkList<AllegatoGenericoBulk>();

    // Firmatari
    private V_persona_fisicaBulk personaFisicaResponsabile;


    private java.util.Collection condizioni;   // Lista condizioni beni (non in DB)
    private String cds_scrivania;              // CDS di contesto (non in DB)
    private String uo_scrivania;               // UO di contesto (non in DB)
    private Inventario_beniBulk bene;          // Bene temporaneo (non in DB)


    // ========================================
    // COSTRUTTORI
    // ========================================

    public Doc_trasporto_rientroBulk() {
        super();
    }

    public Doc_trasporto_rientroBulk(Long pg_inventario, String ti_documento,
                                     Integer esercizio, Long pg_doc_trasporto_rientro) {
        super(pg_inventario, ti_documento, esercizio, pg_doc_trasporto_rientro);
        setInventario(new Id_inventarioBulk(pg_inventario));
    }

    // ========================================
    // GETTER/SETTER - FK MAPPATE (con auto-inizializzazione)
    // ========================================

    // =========== FK: INVENTARIO ===========
    public Id_inventarioBulk getInventario() {
        return inventario;
    }

    public void setInventario(Id_inventarioBulk bulk) {
        inventario = bulk;
    }

    public void setPgInventario(Long pg_inventario) {
        if (this.getInventario() == null) {
            this.setInventario(new Id_inventarioBulk());
        }
        this.getInventario().setPg_inventario(pg_inventario);
    }

    public Long getPgInventario() {
        Id_inventarioBulk inventario = this.getInventario();
        if (inventario == null)
            return null;
        return inventario.getPg_inventario();
    }

    // =========== FK: TIPO_MOVIMENTO ===========
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
        Tipo_trasporto_rientroBulk tipoMovimento = this.getTipoMovimento();
        if (tipoMovimento == null)
            return null;
        return tipoMovimento.getCdTipoTrasportoRientro();
    }

    // =========== FK: TERZO_ASSEGNATARIO (Incaricato Ritiro) ===========
    public TerzoBulk getTerzoIncRitiro() {
        return terzoIncRitiro;
    }

    public void setTerzoIncRitiro(TerzoBulk terzoIncRitiro) {
        this.terzoIncRitiro = terzoIncRitiro;
        if (terzoIncRitiro != null) {
            setCdTerzoAssegnatario(terzoIncRitiro.getCd_terzo());
        } else {
            setCdTerzoAssegnatario(null);
        }
    }

    // Metodo per accesso diretto alla FK (usato dal framework di persistenza)
    @Override
    public void setCdTerzoAssegnatario(Integer cdTerzoAssegnatario) {
        super.setCdTerzoAssegnatario(cdTerzoAssegnatario);
        if (terzoIncRitiro != null && cdTerzoAssegnatario != null) {
            terzoIncRitiro.setCd_terzo(cdTerzoAssegnatario);
        }
    }

    // =========== FK: TERZO_RESPONSABILE (Firmatario) ===========
    public TerzoBulk getTerzoRespDip() {
        return terzoRespDip;
    }

    public void setTerzoRespDip(TerzoBulk terzoRespDip) {
        this.terzoRespDip = terzoRespDip;
        if (terzoRespDip != null) {
            setCdTerzoResponsabile(terzoRespDip.getCd_terzo());
        } else {
            setCdTerzoResponsabile(null);
        }
    }

    // Metodo per accesso diretto alla FK (usato dal framework di persistenza)
    @Override
    public void setCdTerzoResponsabile(Integer cdTerzoResponsabile) {
        super.setCdTerzoResponsabile(cdTerzoResponsabile);
        if (terzoRespDip != null && cdTerzoResponsabile != null) {
            terzoRespDip.setCd_terzo(cdTerzoResponsabile);
        }
    }

    // ========================================
    // GETTER/SETTER - ATTRIBUTI TRANSIENT (non mappati)
    // ========================================

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

    public void setUo_consegnataria(Unita_organizzativaBulk bulk) {
        uo_consegnataria = bulk;
    }

    // Associa anche la FK del Terzo Responsabile quando si imposta la Persona Fisica
    public V_persona_fisicaBulk getPersonaFisicaResponsabile() {
        return personaFisicaResponsabile;
    }

    public void setPersonaFisicaResponsabile(V_persona_fisicaBulk personaFisicaResponsabile) {
        this.personaFisicaResponsabile = personaFisicaResponsabile;
        if (personaFisicaResponsabile != null &&
                personaFisicaResponsabile.getCd_terzo() != null) {
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

    public void setCds_scrivania(String cds_scrivania) {
        this.cds_scrivania = cds_scrivania;
    }

    public String getUo_scrivania() {
        return uo_scrivania;
    }

    public void setUo_scrivania(String uo_scrivania) {
        this.uo_scrivania = uo_scrivania;
    }

    public Inventario_beniBulk getBene() {
        return bene;
    }

    public void setBene(Inventario_beniBulk bene) {
        this.bene = bene;
    }

    // ========================================
    // GETTER/SETTER - COLLEZIONI
    // ========================================

    public java.util.Collection getTipoMovimenti() {
        return tipoMovimenti;
    }

    public void setTipoMovimenti(java.util.Collection collection) {
        tipoMovimenti = collection;
    }

    public PrimaryKeyHashtable getAccessoriContestualiHash() {
        return accessoriContestualiHash;
    }

    public void setAccessoriContestualiHash(PrimaryKeyHashtable hashtable) {
        accessoriContestualiHash = hashtable;
    }

    public SimpleBulkList getDoc_trasporto_rientro_dettColl() {
        return doc_trasporto_rientro_dettColl;
    }

    public void setDoc_trasporto_rientro_dettColl(SimpleBulkList list) {
        doc_trasporto_rientro_dettColl = list;
    }

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

    public String getLocal_transactionID() {
        return local_transactionID;
    }

    public void setLocal_transactionID(String local_transactionID) {
        this.local_transactionID = local_transactionID;
    }

    // ========================================
    // GESTIONE COLLEZIONI DETTAGLI
    // ========================================

    public BulkCollection[] getBulkLists() {
        return new BulkCollection[]{this.getDoc_trasporto_rientro_dettColl()};
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

    // ========================================
    // GESTIONE ACCESSORI CONTESTUALI
    // ========================================

    public Long addToAccessoriContestualiHash(
            Doc_trasporto_rientro_dettBulk bene_padre,
            Doc_trasporto_rientro_dettBulk bene_figlio,
            Long progressivo) {

        if (accessoriContestualiHash == null)
            accessoriContestualiHash = new PrimaryKeyHashtable();

        BulkList beni_associati = null;
        if (bene_padre.getChiaveHash() != null) {
            beni_associati = (BulkList) accessoriContestualiHash.get(bene_padre.getChiaveHash());
        }

        if (beni_associati == null) {
            beni_associati = new BulkList();
            bene_padre.setNr_inventario(progressivo);
            bene_padre.setProgressivo(new Integer("0"));
            bene_padre.setPg_inventario(getPgInventario());
            progressivo = new Long(progressivo.longValue() + 1);
        }

        bene_figlio.setNr_inventario(bene_padre.getNr_inventario());
        bene_figlio.setProgressivo(new Integer(Integer.toString(beni_associati.size() + 1)));
        bene_figlio.setPg_inventario(getPgInventario());

        beni_associati.add(bene_figlio);
        accessoriContestualiHash.put(bene_padre.getChiaveHash(), beni_associati);

        return progressivo;
    }

    public int removeFromAccessoriContestualiHash(Doc_trasporto_rientro_dettBulk bene_figlio) {
        if (accessoriContestualiHash != null) {
            for (java.util.Enumeration e = accessoriContestualiHash.keys(); e.hasMoreElements(); ) {
                String chiave_bene_padre = (String) e.nextElement();
                BulkList beni_accessori = (BulkList) accessoriContestualiHash.get(chiave_bene_padre);
                if (beni_accessori.containsByPrimaryKey(bene_figlio)) {
                    beni_accessori.removeByPrimaryKey(bene_figlio);
                    if (beni_accessori.isEmpty()) {
                        accessoriContestualiHash.remove(chiave_bene_padre);
                        if (accessoriContestualiHash.isEmpty()) {
                            setAccessoriContestualiHash(null);
                        }
                    }
                    break;
                }
            }
        }
        return (accessoriContestualiHash != null ? accessoriContestualiHash.size() : 0);
    }

    // ========================================
    // GESTIONE STATO DOCUMENTO
    // ========================================

    public Dictionary getStatoKeys() {
        return STATO;
    }

    public Dictionary getStatoKeysForSearch() {
        it.cnr.jada.util.OrderedHashtable d = (it.cnr.jada.util.OrderedHashtable) getStatoKeys();
        if (d == null) return null;
        return (it.cnr.jada.util.OrderedHashtable) d.clone();
    }

    public Dictionary getStatoKeysForUpdate() {
        Dictionary stato = new it.cnr.jada.util.OrderedHashtable();

        if (isInserito()) {
            stato.put(STATO_INSERITO, "Inserito");
            stato.put(STATO_INVIATO, "Inviato In Firma");
        } else if (isInviatoInFirma()) {
            stato.put(STATO_INVIATO, "Inviato In Firma");
            stato.put(STATO_DEFINITIVO, "Definitivo");
            stato.put(STATO_ANNULLATO, "Annullato");
        } else if (isDefinitivo()) {
            stato.put(STATO_DEFINITIVO, "Definitivo");
            stato.put(STATO_ANNULLATO, "Annullato");
        } else if (isAnnullato()) {
            stato.put(STATO_ANNULLATO, "Annullato");
        } else {
            stato.put(STATO_INSERITO, "Inserito");
            stato.put(STATO_INVIATO, "Inviato In Firma");
            stato.put(STATO_DEFINITIVO, "Definitivo");
            stato.put(STATO_ANNULLATO, "Annullato");
        }

        return stato;
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
        if (isDefinitivoCompletamente() || isInviatoInFirma()) {
            return false;
        }
        return isInserito();
    }

    public boolean isPossibileModificareDettagli() {
        return isInserito();
    }

    public boolean isConfermabile() {
        return isInserito() && hasDettagli();
    }

    public boolean isAnnullabile() {
        return !isAnnullato();
    }

    // ========================================
    // GESTIONE TIPO RITIRO
    // ========================================

    public java.util.Dictionary getTipoRitiroKeys() {
        return tipoRitiroKeys;
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

    public boolean isNominativoVettoreRequired() {
        return isRitiroVettore();
    }

    // ========================================
    // METODI UTILITY
    // ========================================

    public boolean isTrasporto() {
        return TRASPORTO.equals(getTiDocumento());
    }

    public boolean isRientro() {
        return RIENTRO.equals(getTiDocumento());
    }

    public boolean hasDettagli() {
        return doc_trasporto_rientro_dettColl != null && doc_trasporto_rientro_dettColl.size() > 0;
    }

    public boolean isTemporaneo() {
        return getPgDocTrasportoRientro() == null ||
                getPgDocTrasportoRientro().compareTo(new Long("0")) <= 0;
    }

    public boolean isNoteRitiroEnabled() {
        if (!hasTipoRitiroSelezionato()) {
            return false;
        }
        if (tipoMovimento != null) {
            String dsTipo = tipoMovimento.getDsTipoTrasportoRientro();
            return "Sostituzione per".equals(dsTipo) || "Altro".equals(dsTipo);
        }
        return false;
    }

    public String getDocTrasportoRiferimento() {
        if (!RIENTRO.equals(this.getTiDocumento())) return null;
        if (this.doc_trasporto_rientro_dettColl == null ||
                this.doc_trasporto_rientro_dettColl.isEmpty()) return null;

        Doc_trasporto_rientro_dettBulk primoDettaglio =
                (Doc_trasporto_rientro_dettBulk) this.doc_trasporto_rientro_dettColl.get(0);

        if (primoDettaglio.getPgDocTrasportoRientroRif() == null) return null;

        return primoDettaglio.getEsercizioRif() + "/" +
                primoDettaglio.getTiDocumentoRif() + "/" +
                primoDettaglio.getPgDocTrasportoRientroRif();
    }

    // ========================================
    // METODI FIRMA DIGITALE
    // ========================================

    public boolean isFirmabile() {
        return isInviatoInFirma() && !isDefinitivoCompletamente();
    }

    public boolean isDefinitivoCompletamente() {
        return "FIR".equals(getStatoFlusso()) || isDefinitivo();
    }

    public boolean hasFlussoFirmaAttivo() {
        return getIdFlussoHappysign() != null && !isDefinitivoCompletamente();
    }

    public int getNumeroFirmatariRichiesti() {
        return isRitiroIncaricato() ? 3 : 2;
    }

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
        return isInviatoInFirma() &&
                getIdFlussoHappysign() == null &&
                getConsegnatario() != null &&
                getCdTerzoResponsabile() != null &&
                hasDettagli();
    }

    public boolean isRifiutatoInFirma() {
        return "RIF".equals(getStatoFlusso());
    }

    public boolean isInviatoAlFlusso() {
        return "INV".equals(getStatoFlusso()) && getIdFlussoHappysign() != null;
    }

    public static String getStorePathDDT(String suffix, Integer esercizio, Long inventario,
                                         String tiDocumento, Long pgDocTrasportoRientro) {
        return Arrays.asList(
                SpringUtil.getBean(StorePath.class).getPathComunicazioniDal(),
                suffix,
                String.valueOf(esercizio),
                String.valueOf(inventario),
                tiDocumento,
                String.valueOf(pgDocTrasportoRientro)
        ).stream().collect(Collectors.joining(StorageDriver.SUFFIX));
    }

    // ========================================
    // VALIDAZIONE
    // ========================================

    private void validaCampoObbligatorio(Object valore, String nomeCampo) throws ValidationException {
        if (valore == null) {
            throw new ValidationException("Indicare " + nomeCampo + ".");
        }
        if (valore instanceof String && ((String) valore).trim().isEmpty()) {
            throw new ValidationException("Indicare " + nomeCampo + ".");
        }
    }

    @Override
    public void validate() throws ValidationException {
        validaCampoObbligatorio(getDsDocTrasportoRientro(), "la Descrizione del documento");
        validaCampoObbligatorio(getDataRegistrazione(),
                "la Data " + (TRASPORTO.equals(getTiDocumento()) ? "Trasporto" : "Rientro"));
        validaCampoObbligatorio(getTipoMovimento(), "il Tipo Movimento");
        validaCampoObbligatorio(getTipoRitiro(), "il Tipo Ritiro (Incaricato o Vettore)");

        if (!Arrays.asList(TIPO_RITIRO_INCARICATO, TIPO_RITIRO_VETTORE).contains(getTipoRitiro())) {
            throw new ValidationException("I valori possibili per Tipo Ritiro sono: Incaricato o Vettore.");
        }

        if (isRitiroIncaricato() &&
                (getTerzoIncRitiro() == null || getTerzoIncRitiro().getCd_anag() == null)) {
            throw new ValidationException(
                    "Per il ritiro tramite INCARICATO è necessario selezionare il Dipendente Incaricato.");
        }

        if (isRitiroVettore() &&
                (getNominativoVettore() == null || getNominativoVettore().trim().isEmpty())) {
            throw new ValidationException(
                    "Per il ritiro tramite VETTORE è necessario specificare il Nominativo del Vettore.");
        }

        super.validate();
    }

    public String constructCMISNomeFile() {
        StringBuffer nomeFile = new StringBuffer();
        nomeFile = nomeFile.append(StrServ.lpad(this.getPgDocTrasportoRientro().toString(), 9, "0"));
        return nomeFile.toString();
    }

    public String recuperoIdDocAsString() {
        return StrServ.replace(getTiDocumento(), ".", "")
                + getEsercizio()
                + StrServ.lpad(getPgInventario().toString(), 5)
                + StrServ.lpad(getPgDocTrasportoRientro().toString(), 5);
    }

    /**
     * Override per forzare il filtro TI_DOCUMENTO in tutte le ricerche.
     * Questo impedisce che vengano mostrati documenti del tipo sbagliato nella lista di ricerca.
     */
    @Override
    public CompoundFindClause buildFindClauses(Boolean freeSearch) {
        // Recupera le clausole standard dal BulkInfo
        CompoundFindClause clauses = getBulkInfo().buildFindClausesFrom(this, freeSearch);

        if (clauses == null) {
            clauses = new it.cnr.jada.persistency.sql.CompoundFindClause();
        }

        // CRITICO: Forza SEMPRE il filtro su TI_DOCUMENTO
        // Questo impedisce che documenti di TRASPORTO appaiano nella ricerca di RIENTRI e viceversa
        if (getTiDocumento() != null) {
            clauses.addClause(
                    "AND",
                    "tiDocumento",
                    it.cnr.jada.persistency.sql.SQLBuilder.EQUALS,
                    getTiDocumento()
            );
        }

        return clauses;
    }
}
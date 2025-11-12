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
import it.cnr.jada.util.StrServ;
import it.cnr.si.spring.storage.StorageDriver;
import it.cnr.si.spring.storage.annotation.StorageProperty;

import java.util.Arrays;
import java.util.Dictionary;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Testata documento Trasporto/Rientro.
 * <p>
 * PATTERN: Delega per pg_inventario
 * - pg_inventario è delegato a inventario (Id_inventarioBulk)
 * - ti_documento, esercizio, pg_doc_trasporto_rientro sono nella Key
 * <p>
 * GESTIONE STATO:
 * - Lo STATO del documento è gestito tramite il campo 'stato' ereditato dal Base
 * - Utilizza un Dictionary per le label degli stati
 * <p>
 * GESTIONE FIRMA DIGITALE:
 * - I firmatari sono gestiti direttamente come campi del documento
 * - Firmatari obbligatori: Consegnatario (sempre), Responsabile (sempre), Incaricato (se ritiro incaricato)
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
    public static final String STATO_FIRMATO = "FIR";
    public static final String STATO_ANNULLATO = "ANN";

    /**
     * Dictionary degli stati del documento con le relative label
     */
    public final static Dictionary STATO;

    static {
        STATO = new it.cnr.jada.util.OrderedHashtable();
        STATO.put(STATO_INSERITO, "Inserito");
        STATO.put(STATO_INVIATO, "Inviato In Firma");
        STATO.put(STATO_FIRMATO, "Firmato");
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
    // ATTRIBUTI ESISTENTI
    // ========================================

    private String local_transactionID;
    private Unita_organizzativaBulk uo_consegnataria;
    private TerzoBulk consegnatario;
    private TerzoBulk delegato;
    protected Tipo_trasporto_rientroBulk tipoMovimento;
    private java.util.Collection tipoMovimenti;
    private Id_inventarioBulk inventario;
    private List condizioni;
    private String cds_scrivania;
    private String uo_scrivania;
    private Integer nr_inventario;
    private PrimaryKeyHashtable accessoriContestualiHash;
    private SimpleBulkList doc_trasporto_rientro_dettColl;
    private TerzoBulk anagDipRitiro;
    private Inventario_beniBulk bene;
    private BulkList<AllegatoGenericoBulk> archivioAllegati = new BulkList<AllegatoGenericoBulk>();

    // ========================================
    // ATTRIBUTI PER LA FIRMA DIGITALE (SOLO OGGETTI COMPLETI)
    // ========================================

    /**
     * Responsabile struttura (firmatario obbligatorio) - OGGETTO COMPLETO
     * Il codice (cdTerzoResponsabile) è nel Base
     */
    private TerzoBulk terzoResponsabile;
    private V_persona_fisicaBulk personaFisicaResponsabile;

    // ========================================
    // GETTER E SETTER - FIRMA DIGITALE
    // ========================================

    /**
     * Alias per idFlussoHappysign (per compatibilità con codice esistente)
     */
    public String getIdFlusso() {
        return getIdFlussoHappysign();
    }

    public void setIdFlusso(String idFlusso) {
        setIdFlussoHappysign(idFlusso);
    }

    /**
     * Alias per noteRifiuto (per compatibilità con codice esistente)
     */
    public String getMotivoRifiuto() {
        return getNoteRifiuto();
    }

    public void setMotivoRifiuto(String motivoRifiuto) {
        setNoteRifiuto(motivoRifiuto);
    }

    // Responsabile struttura - OGGETTI COMPLETI
    public TerzoBulk getTerzoResponsabile() {
        return terzoResponsabile;
    }

    public void setTerzoResponsabile(TerzoBulk terzoResponsabile) {
        this.terzoResponsabile = terzoResponsabile;
        if (terzoResponsabile != null) {
            setCdTerzoResponsabile(terzoResponsabile.getCd_terzo());
        }
    }

    public V_persona_fisicaBulk getPersonaFisicaResponsabile() {
        return personaFisicaResponsabile;
    }

    public void setPersonaFisicaResponsabile(V_persona_fisicaBulk personaFisicaResponsabile) {
        this.personaFisicaResponsabile = personaFisicaResponsabile;
        if (personaFisicaResponsabile != null && personaFisicaResponsabile.getCd_terzo() != null) {
            setCdTerzoResponsabile(personaFisicaResponsabile.getCd_terzo());
        }
    }

    // ========================================
    // METODI UTILITY - FIRMA (SOLO ESSENZIALI)
    // ========================================

    /**
     * Verifica se il documento può essere firmato
     */
    public boolean isFirmabile() {
        return isInviatoInFirma() && !isFirmatoCompletamente();
    }

    /**
     * Verifica se il documento è già stato firmato completamente
     */
    public boolean isFirmatoCompletamente() {
        return "FIR".equals(getStatoFlusso()) || isFirmato();
    }

    /**
     * Verifica se il documento ha un flusso di firma attivo
     */
    public boolean hasFlussoFirmaAttivo() {
        return getIdFlussoHappysign() != null && !isFirmatoCompletamente();
    }

    /**
     * Restituisce il numero di firmatari richiesti per questo documento
     */
    public int getNumeroFirmatariRichiesti() {
        // Consegnatario + Responsabile = 2
        // + Incaricato se ritiro incaricato = 3
        return isRitiroIncaricato() ? 3 : 2;
    }

    // ========================================
    // GETTER E SETTER - ALLEGATI
    // ========================================

    /**
     * Restituisce la lista degli allegati del documento
     */
    public BulkList<AllegatoGenericoBulk> getArchivioAllegati() {
        return archivioAllegati;
    }

    /**
     * Imposta la lista degli allegati del documento
     */
    public void setArchivioAllegati(BulkList<AllegatoGenericoBulk> archivioAllegati) {
        this.archivioAllegati = archivioAllegati;
    }

    /**
     * Rimuove un allegato dalla lista degli allegati
     *
     * @param index l'indice dell'allegato da rimuovere
     * @return l'allegato rimosso
     */
    public AllegatoGenericoBulk removeFromArchivioAllegati(int index) {
        return getArchivioAllegati().remove(index);
    }

    /**
     * Aggiunge un allegato alla lista degli allegati
     * Se l'allegato è di tipo AllegatoDocTrasportoRientroBulk, imposta automaticamente
     * il riferimento al documento di trasporto/rientro
     *
     * @param allegato l'allegato da aggiungere
     * @return l'indice nella lista dove è stato aggiunto l'allegato
     */
    public int addToArchivioAllegati(AllegatoGenericoBulk allegato) {
        Optional.ofNullable(allegato)
                .filter(AllegatoDocTraspRientBulk.class::isInstance)
                .map(AllegatoDocTraspRientBulk.class::cast)
                .ifPresent(el -> el.setDocTrasportoRientro(this));
        archivioAllegati.add(allegato);
        return archivioAllegati.size() - 1;
    }

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
    // GETTER E SETTER - Oggetti Relazionati
    // ========================================

    public TerzoBulk getConsegnatario() {
        return consegnatario;
    }

    public void setConsegnatario(TerzoBulk bulk) {
        consegnatario = bulk;
    }

    public TerzoBulk getDelegato() {
        return delegato;
    }

    public void setDelegato(TerzoBulk bulk) {
        delegato = bulk;
    }

    public Id_inventarioBulk getInventario() {
        return inventario;
    }

    public void setInventario(Id_inventarioBulk bulk) {
        inventario = bulk;
    }

    public java.util.Collection getTipoMovimenti() {
        return tipoMovimenti;
    }

    public void setTipoMovimenti(java.util.Collection collection) {
        tipoMovimenti = collection;
    }

    public Tipo_trasporto_rientroBulk getTipoMovimento() {
        return tipoMovimento;
    }

    public void setTipoMovimento(Tipo_trasporto_rientroBulk bulk) {
        tipoMovimento = bulk;
    }

    public Unita_organizzativaBulk getUo_consegnataria() {
        return uo_consegnataria;
    }

    public void setUo_consegnataria(Unita_organizzativaBulk bulk) {
        uo_consegnataria = bulk;
    }

    // ========================================
    // GETTER E SETTER - Campi Delegati
    // ========================================

    public void setPgInventario(Long pg_inventario) {
        this.getInventario().setPg_inventario(pg_inventario);
    }

    public Long getPgInventario() {
        Id_inventarioBulk inventario = this.getInventario();
        if (inventario == null)
            return null;
        return inventario.getPg_inventario();
    }

    public void setCd_tipo_trasporto_rientro(String cd_tipo_trasporto_rientro) {
        this.getTipoMovimento().setCdTipoTrasportoRientro(cd_tipo_trasporto_rientro);
    }

    public String getCd_tipo_trasporto_rientro() {
        Tipo_trasporto_rientroBulk tipoMovimento = this.getTipoMovimento();
        if (tipoMovimento == null)
            return null;
        return tipoMovimento.getCdTipoTrasportoRientro();
    }

    // ========================================
    // GETTER E SETTER - Altri Attributi
    // ========================================

    public String getCds_scrivania() {
        return cds_scrivania;
    }

    public void setCds_scrivania(String string) {
        cds_scrivania = string;
    }

    public List getCondizioni() {
        return condizioni;
    }

    public void setCondizioni(List collection) {
        condizioni = collection;
    }

    public String getUo_scrivania() {
        return uo_scrivania;
    }

    public void setUo_scrivania(String string) {
        uo_scrivania = string;
    }

    public Integer getNr_inventario() {
        return nr_inventario;
    }

    public void setNr_inventario(Integer integer) {
        nr_inventario = integer;
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

    public String getLocal_transactionID() {
        return local_transactionID;
    }

    public void setLocal_transactionID(String local_transactionID) {
        this.local_transactionID = local_transactionID;
    }

    public TerzoBulk getAnagDipRitiro() {
        return anagDipRitiro;
    }

    public void setAnagDipRitiro(TerzoBulk anagDipRitiro) {
        this.anagDipRitiro = anagDipRitiro;
        if (anagDipRitiro != null) {
            setCdTerzoAssegnatario(anagDipRitiro.getCd_terzo());
            setDenominazioneSede(anagDipRitiro.getDenominazione_sede());
        } else {
            setCdTerzoAssegnatario(null);
        }
    }

    @Override
    public void setCdTerzoAssegnatario(Integer cdTerzoAssegnatario) {
        super.setCdTerzoAssegnatario(cdTerzoAssegnatario);
        if (anagDipRitiro != null && cdTerzoAssegnatario != null) {
            anagDipRitiro.setCd_terzo(cdTerzoAssegnatario);
        }
    }

    public void setDenominazioneSede(String denominazioneSede) {
        if (anagDipRitiro != null) {
            anagDipRitiro.setDenominazione_sede(denominazioneSede);
        }
    }

    public Inventario_beniBulk getBene() {
        return bene;
    }

    public void setBene(Inventario_beniBulk bene) {
        this.bene = bene;
    }

    // ========================================
    // GESTIONE COLLEZIONI
    // ========================================

    public BulkCollection[] getBulkLists() {
        return new BulkCollection[]{this.getDoc_trasporto_rientro_dettColl()};
    }

    public Doc_trasporto_rientro_dettBulk removeFromDoc_trasporto_rientro_dettColl(int indiceDiLinea) {
        Doc_trasporto_rientro_dettBulk element = (Doc_trasporto_rientro_dettBulk) doc_trasporto_rientro_dettColl.get(indiceDiLinea);
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

    /**
     * Restituisce il Dictionary completo degli stati
     * Utilizzato per visualizzare tutte le opzioni disponibili
     */
    public Dictionary getStatoKeys() {
        return STATO;
    }

    /**
     * Restituisce il Dictionary degli stati per la ricerca
     * Include tutti gli stati possibili
     */
    public Dictionary getStatoKeysForSearch() {
        it.cnr.jada.util.OrderedHashtable d = (it.cnr.jada.util.OrderedHashtable) getStatoKeys();
        if (d == null) return null;
        it.cnr.jada.util.OrderedHashtable clone = (it.cnr.jada.util.OrderedHashtable) d.clone();
        return clone;
    }

    /**
     * Restituisce il Dictionary degli stati per l'aggiornamento
     * Filtra gli stati in base allo stato corrente del documento
     */
    public Dictionary getStatoKeysForUpdate() {
        Dictionary stato = new it.cnr.jada.util.OrderedHashtable();

        if (isInserito()) {
            // Da INSERITO può passare a:
            stato.put(STATO_INSERITO, "Inserito");
            stato.put(STATO_INVIATO, "Inviato In Firma");
        } else if (isInviatoInFirma()) {
            // Da INVIATO IN FIRMA può passare a:
            stato.put(STATO_INVIATO, "Inviato In Firma");
            stato.put(STATO_FIRMATO, "Firmato");
            stato.put(STATO_ANNULLATO, "Annullato");
        } else if (isFirmato()) {
            // Da FIRMATO può solo essere annullato
            stato.put(STATO_FIRMATO, "Firmato");
            stato.put(STATO_ANNULLATO, "Annullato");
        } else if (isAnnullato()) {
            // ANNULLATO è uno stato finale
            stato.put(STATO_ANNULLATO, "Annullato");
        } else {
            // Default: mostra tutti gli stati
            stato.put(STATO_INSERITO, "Inserito");
            stato.put(STATO_INVIATO, "Inviato In Firma");
            stato.put(STATO_FIRMATO, "Firmato");
            stato.put(STATO_ANNULLATO, "Annullato");
        }

        return stato;
    }

    /**
     * Verifica se il documento è in un determinato stato
     */
    private boolean hasStato(String stato) {
        return stato != null && stato.equals(getStato());
    }

    /**
     * Verifica se il documento è in stato INSERITO
     */
    public boolean isInserito() {
        return hasStato(STATO_INSERITO);
    }

    /**
     * Verifica se il documento è in stato INVIATO IN FIRMA
     */
    public boolean isInviatoInFirma() {
        return hasStato(STATO_INVIATO);
    }

    /**
     * Verifica se il documento è in stato FIRMATO
     */
    public boolean isFirmato() {
        return hasStato(STATO_FIRMATO);
    }

    /**
     * Verifica se il documento è in stato ANNULLATO
     */
    public boolean isAnnullato() {
        return hasStato(STATO_ANNULLATO);
    }

    /**
     * Verifica se il documento è modificabile
     * Solo i documenti in stato INSERITO sono modificabili
     */
    public boolean isModificabile() {
        // Un documento firmato o INVIATO IN FIRMA non è modificabile
        if (isFirmatoCompletamente() || isInviatoInFirma()) {
            return false;
        }
        return isInserito();
    }

    /**
     * Verifica se è possibile modificare i dettagli del documento
     */
    public boolean isPossibileModificareDettagli() {
        return isInserito();
    }

    /**
     * Verifica se il documento può essere confermato/INVIATO IN FIRMA
     */
    public boolean isConfermabile() {
        return isInserito() && hasDettagli();
    }

    /**
     * Verifica se il documento può essere annullato
     */
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
        // Pulisci i campi quando cambi tipo ritiro
        if (TIPO_RITIRO_INCARICATO.equals(tipoRitiro)) {
            setNominativoVettore(null); // Pulisci nominativo vettore
        } else if (TIPO_RITIRO_VETTORE.equals(tipoRitiro)) {
            setAnagDipRitiro(null); // Pulisci dipendente incaricato
            setCdTerzoAssegnatario(null);
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
    // ANNOTAZIONI AZURE STORAGE
    // ========================================

    @StorageProperty(name = "Doc_Trasporto_Rientro:esercizio")
    public Integer getEsercizioPathAzure() {
        return getEsercizio();
    }

    @StorageProperty(name = "Doc_Trasporto_Rientro:inventario")
    public Long getPgInventarioPathAzure() {
        return getPgInventario();
    }

    @StorageProperty(name = "Doc_Trasporto_Rientro:tiDocumento")
    public String getTipoDocPathAzure() {
        if (TRASPORTO.equals(getTiDocumento()))
            return "Trasporto";
        else
            return "Rientro";
    }

    @StorageProperty(name = "Doc_Trasporto_Rientro:pgDocTrasportoRientro")
    public Long getPgDocTRPathAzure() {
        return getPgDocTrasportoRientro();
    }

    /**
     * Percorso su CMIS: cartella per DOC_T_R / esercizio / inventario / tiDocumento / pgDocTrasportoRientro
     **/
    public static String getStorePathDDT(String suffix, Integer esercizio, Long inventario,
                                         String tiDocumento, Long pgDocTrasportoRientro) {
        return Arrays.asList(
                SpringUtil.getBean(StorePath.class).getPathComunicazioniAl(),
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
        // Validazioni campi obbligatori
        validaCampoObbligatorio(getDsDocTrasportoRientro(), "la Descrizione del documento");
        validaCampoObbligatorio(getDataRegistrazione(),
                "la Data " + (TRASPORTO.equals(getTiDocumento()) ? "Trasporto" : "Rientro"));
        validaCampoObbligatorio(getTipoMovimento(), "il Tipo Movimento");
        validaCampoObbligatorio(getTipoRitiro(), "il Tipo Ritiro (Incaricato o Vettore)");

        // Validazione valori ammessi per Tipo Ritiro
        if (!Arrays.asList(TIPO_RITIRO_INCARICATO, TIPO_RITIRO_VETTORE).contains(getTipoRitiro())) {
            throw new ValidationException("I valori possibili per Tipo Ritiro sono: Incaricato o Vettore.");
        }

        // Validazione Dipendente Incaricato se tipo ritiro è INCARICATO
        if (isRitiroIncaricato() && (getAnagDipRitiro() == null || getAnagDipRitiro().getCd_anag() == null)) {
            throw new ValidationException(
                    "Per il ritiro tramite INCARICATO è necessario selezionare il Dipendente Incaricato.");
        }

        // Validazione Nominativo Vettore se tipo ritiro è VETTORE
        if (isRitiroVettore() &&
                (getNominativoVettore() == null || getNominativoVettore().trim().isEmpty())) {
            throw new ValidationException(
                    "Per il ritiro tramite VETTORE è necessario specificare il Nominativo del Vettore.");
        }

        super.validate();
    }

    /**
     * Determina se il campo Nominativo Vettore deve essere visibile
     * Visibile SOLO quando il tipo ritiro è VETTORE
     */
    public boolean isNominativoVettoreVisible() {
        return isRitiroVettore();
    }

    /**
     * Determina se il campo Nominativo Vettore è obbligatorio
     * Obbligatorio quando il tipo ritiro è VETTORE
     */
    public boolean isNominativoVettoreRequired() {
        return isRitiroVettore();
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

    // ========================================
// METODI PER LA GESTIONE DEL FLUSSO DI FIRMA
// ========================================

    /**
     * Inizializza il documento per l'invio al flusso di firma
     */
    public void inizializzaPerInvioFirma() {
        setStatoFlusso("INV");
        setDataInvioFirma(new java.sql.Timestamp(System.currentTimeMillis()));
    }

    /**
     * Aggiorna il documento dopo la firma completata
     */
    public void aggiornaDopoFirmaCompletata() {
        setStato(STATO_FIRMATO);
        setStatoFlusso("FIR");
        setDataFirma(new java.sql.Timestamp(System.currentTimeMillis()));
        setNoteRifiuto(null);
    }

    /**
     * Aggiorna il documento dopo il rifiuto della firma
     */
    public void aggiornaDopoRifiutoFirma(String motivoRifiuto) {
        setStato(STATO_INSERITO);
        setStatoFlusso("RIF");
        setNoteRifiuto(motivoRifiuto);
        setIdFlussoHappysign(null);
        setDataInvioFirma(null);
        setDataFirma(null);
    }

    /**
     * Resetta i dati del flusso di firma
     */
    public void resetFlussoFirma() {
        setIdFlussoHappysign(null);
        setStatoFlusso(null);
        setDataInvioFirma(null);
        setDataFirma(null);
        setNoteRifiuto(null);
    }

    /**
     * Verifica se il documento può essere inviato alla firma
     */
    public boolean isInviabileAllaFirma() {
        return isInviatoInFirma() &&
                getIdFlussoHappysign() == null &&
                getConsegnatario() != null &&
                getCdTerzoResponsabile() != null &&
                hasDettagli();
    }

    /**
     * Verifica se il documento è stato rifiutato in firma
     */
    public boolean isRifiutatoInFirma() {
        return "RIF".equals(getStatoFlusso());
    }

    /**
     * Verifica se il documento è stato inviato al flusso di firma
     */
    public boolean isInviatoAlFlusso() {
        return "INV".equals(getStatoFlusso()) && getIdFlussoHappysign() != null;
    }

}

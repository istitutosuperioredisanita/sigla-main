package it.cnr.contab.inventario01.bp;

import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.inventario01.ejb.BuonoCaricoScaricoComponentSession;
import it.cnr.contab.inventario01.ejb.DocTrasportoRientroComponentSession;
import it.cnr.contab.utenze00.bulk.UtenteBulk;
import it.cnr.contab.util00.bp.AllegatiCRUDBP;
import it.cnr.contab.util00.bulk.storage.AllegatoGenericoBulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.util.action.SimpleCRUDBP;

import java.rmi.RemoteException;

/**
 * Business Process base per la gestione dei documenti di Trasporto/Rientro.
 * Definisce il tipo di documento e verifica lo stato dell'esercizio e i ruoli utente.
 * Estende AllegatiCRUDBP per gestire gli allegati dei documenti.
 */
public abstract class CRUDTraspRientInventarioBP extends SimpleCRUDBP /*extends AllegatiCRUDBP<AllegatoGenericoBulk, Doc_trasporto_rientroBulk>*/ {

    public static final String TRASPORTO = "T";
    public static final String RIENTRO = "R";

    private String tipo;
    private boolean isAmministratore = false;
    private boolean isVisualizzazione = false; // True se l'esercizio COEP è chiuso

    // ========================================
    // COSTRUTTORI
    // ========================================


    public CRUDTraspRientInventarioBP() {
        super();
    }

    public CRUDTraspRientInventarioBP(String function) {
        super(function);
    }

    // ========================================
    // INIZIALIZZAZIONE
    // ========================================

    /**
     * Inizializza il Business Process.
     * Determina il tipo, imposta i column set e verifica lo stato dell'esercizio COEP e il ruolo.
     */
    @Override
    protected void init(it.cnr.jada.action.Config config, it.cnr.jada.action.ActionContext context)
            throws it.cnr.jada.action.BusinessProcessException {

        // 1. Determina tipo documento
        if (this instanceof CRUDTrasportoBeniInvBP) {
            setTipo(TRASPORTO);
        } else if (this instanceof CRUDRientroBeniInvBP) {
            setTipo(RIENTRO);
        }

        // 2. Imposta i set di ricerca/visualizzazione basati sul tipo
        setSearchResultColumnSet(getTipo());
        setFreeSearchSet(getTipo());

        // 3. Verifica stato esercizio e ruolo utente (Business Logic)
        try {
            BuonoCaricoScaricoComponentSession session =
                    (BuonoCaricoScaricoComponentSession) createComponentSession(
                            "CNRINVENTARIO01_EJB_BuonoCaricoScaricoComponentSession",
                            BuonoCaricoScaricoComponentSession.class);

            // True se l'esercizio COEP (contabile) è chiuso
            setVisualizzazione(session.isEsercizioCOEPChiuso(context.getUserContext()));

            // Verifica se l'utente è amministratore inventario
            setAmministratore(UtenteBulk.isAmministratoreInventario(context.getUserContext()));

        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }

        super.init(config, context);
        initVariabili(context, getTipo());

    }

    public void initVariabili(it.cnr.jada.action.ActionContext context, String Tipo) {

        if (this instanceof CRUDTrasportoBeniInvBP)
            setTipo(TRASPORTO);
        else
            setTipo(RIENTRO);
        setSearchResultColumnSet(getTipo());
        setFreeSearchSet(getTipo());
    }

    // ========================================
    // METODI ASTRATTI DA IMPLEMENTARE (richiesti da AllegatiCRUDBP)
    // ========================================

//    /**
//     * Metodo astratto che le sottoclassi devono implementare per definire
//     * il percorso di storage CMIS per gli allegati del documento.
//     *
//     * @param documento Il documento di trasporto/rientro
//     * @param create Se true, crea il percorso se non esiste
//     * @return Il percorso completo su CMIS
//     * @throws BusinessProcessException In caso di errori
//     */
//    protected abstract String getStorePath(Doc_trasporto_rientroBulk documento, boolean create)
//            throws BusinessProcessException;

    // ========================================
    // GETTER E SETTER
    // ========================================

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public boolean isAmministratore() {
        return isAmministratore;
    }

    public void setAmministratore(boolean isAmministratore) {
        this.isAmministratore = isAmministratore;
    }

    /** True se l'esercizio contabile è chiuso (modalità sola lettura). */
    public boolean isVisualizzazione() {
        return isVisualizzazione;
    }

    public void setVisualizzazione(boolean isVisualizzazione) {
        this.isVisualizzazione = isVisualizzazione;
    }

    /**
     * Un documento NON è modificabile se l'esercizio è chiuso (`isVisualizzazione = true`).
     */
    @Override
    public boolean isEditable() {
        return !isVisualizzazione() && super.isEditable();
    }

    // ========================================
    // VISIBILITÀ UI
    // ========================================

    public Doc_trasporto_rientroBulk getDoc() {
        return (Doc_trasporto_rientroBulk) getModel();
    }

    public boolean isDestinazioneVisible() {
        return getModel() != null && getDoc().hasTipoRitiroSelezionato();
    }

    public boolean isAssegnatarioVisible() {
        return getModel() != null && getDoc().isRitiroIncaricato();
    }

    public boolean isStatoVisible() {
        return getModel() != null && getDoc().getStato() != null && !isInserting();
    }

    public boolean isNoteVisible() {
        return getModel() != null && getDoc().isNoteRitiroEnabled();
    }

    public boolean isNoteAbilitate() {
        return getModel() != null && getDoc().getTipoMovimento() != null &&
                getDoc().getTipoMovimento().isAbilitaNote();
    }

    public boolean isTipoMovimentoReadOnly() {
        return isEditing() && getDoc() != null &&
                getDoc().getDoc_trasporto_rientro_dettColl() != null &&
                !getDoc().getDoc_trasporto_rientro_dettColl().isEmpty();
    }

    public boolean isTipoRitiroReadOnly() {
        return isTipoMovimentoReadOnly();
    }

    public boolean isAssegnatarioReadOnly() {
        return isTipoMovimentoReadOnly();
    }

    public boolean isQuantitaEnabled() {
        return isEditing() || isInserting();
    }

}
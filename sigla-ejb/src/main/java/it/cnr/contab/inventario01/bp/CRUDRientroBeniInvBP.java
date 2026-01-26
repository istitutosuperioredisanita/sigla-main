package it.cnr.contab.inventario01.bp;

import it.cnr.contab.inventario01.bulk.AllegatoDocumentoRientroBulk;
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.inventario01.bulk.DocumentoRientroBulk;
import it.cnr.contab.inventario01.bulk.DocumentoRientroDettBulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.bulk.BulkList;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.bulk.SimpleBulkList;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.util.RemoteIterator;

import java.rmi.RemoteException;
import java.util.BitSet;
import java.util.TreeMap;

/**
 * Business Process specializzato per la gestione del flusso di Rientro Beni.
 * Estende la logica comune di trasporto/rientro per gestire le specificità
 * dei documenti di rientro, i relativi tab e il recupero dei beni rientrabili.
 */
public class CRUDRientroBeniInvBP extends CRUDTraspRientInventarioBP<AllegatoDocumentoRientroBulk, DocumentoRientroBulk> {

    public CRUDRientroBeniInvBP() {
        super();
    }

    public CRUDRientroBeniInvBP(String function) {
        super(function);
    }

    /**
     * Restituisce il percorso di storage per gli allegati del documento di rientro.
     */
    @Override
    protected String getStorePath(DocumentoRientroBulk documentoRientroBulk, boolean create) throws BusinessProcessException {
        if (documentoRientroBulk == null) {
            throw new BusinessProcessException("Documento di rientro non presente");
        }
        return documentoRientroBulk.getStorePath().get(0);
    }

    /**
     * Restituisce la classe deputata alla gestione degli allegati per il rientro.
     */
    @Override
    protected Class getAllegatoClass() {
        return AllegatoDocumentoRientroBulk.class;
    }

    /**
     * Definisce i tab della pagina in base allo stato del documento (inserimento o modifica).
     */
    public String[][] getTabs() {
        TreeMap<Integer, String[]> hash = new TreeMap<>();
        int i = 0;
        hash.put(i++, new String[]{"tabRientroTestata", "Testata", "/inventario00/tab_testata_doc_r.jsp"});

        if (isInserting()) {
            hash.put(i++, new String[]{"tabRientroDettaglio", "Dettaglio", "/inventario00/tab_rientro_inv_dett.jsp"});
        } else {
            hash.put(i++, new String[]{"tabRientroDettaglio", "Dettaglio", "/inventario00/tab_rientro_inv_edit_dett.jsp"});
        }
        hash.put(i++, new String[]{"tabAllegati", "Allegati", "/util00/tab_allegati.jsp"});

        String[][] tabs = new String[i][3];
        for (int j = 0; j < i; j++) {
            tabs[j] = new String[]{hash.get(j)[0], hash.get(j)[1], hash.get(j)[2]};
        }
        return tabs;
    }

    /**
     * Nome del controller per la gestione dei dettagli in fase di inserimento.
     */
    @Override
    protected String getDettagliControllerName() {
        return "DettagliRientro";
    }

    /**
     * Nome del controller per la gestione dei dettagli in fase di modifica.
     */
    @Override
    protected String getEditDettagliControllerName() {
        return "EditDettagliRientro";
    }

    /**
     * Nome del tab principale della testata.
     */
    @Override
    public String getMainTabName() {
        return "tabRientroTestata";
    }

    /**
     * Label specifica per la data di registrazione nel contesto del rientro.
     */
    @Override
    public String getLabelData_registrazione() {
        return "Data Rientro";
    }

    /**
     * Inizializza le strutture per la selezione dei beni (non implementato).
     */
    @Override
    public void inizializzaSelezioneComponente(ActionContext context) throws ComponentException, RemoteException {
    }

    /**
     * Annulla le modifiche effettuate sui beni a livello di componente (non implementato).
     */
    @Override
    public void annullaModificaComponente(ActionContext context) throws ComponentException, RemoteException {
    }

    /**
     * Gestisce la modifica dei beni e degli accessori associati, aggiornando il modello in memoria.
     */
    @Override
    public void modificaBeniConAccessoriComponente(ActionContext context, OggettoBulk[] bulks,
                                                   BitSet oldSelection, BitSet newSelection)
            throws ComponentException, RemoteException, BusinessProcessException {
        Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) getModel();

        getComp().modificaBeniRientratiConAccessori(
                context.getUserContext(), doc, bulks, oldSelection, newSelection);

        BulkList dettagliAggiornati = getComp().getDetailsFor(
                context.getUserContext(), doc);
        doc.setDoc_trasporto_rientro_dettColl(dettagliAggiornati);

        setModel(context, doc);
    }

    /**
     * Restituisce la classe Bulk per i dettagli del documento di rientro.
     */
    @Override
    public Class getDocumentoClassDett() {
        return DocumentoRientroDettBulk.class;
    }

    /**
     * Recupera la lista dei beni che possono essere fatti rientrare (provenienti da documenti firmati).
     */
    public RemoteIterator getListaBeniDaFarRientrare(it.cnr.jada.UserContext userContext, SimpleBulkList beni_da_escludere, it.cnr.jada.persistency.sql.CompoundFindClause clauses) throws BusinessProcessException, RemoteException, ComponentException {
        return getComp().getListaBeniDaFarRientrare(userContext, getDoc(), beni_da_escludere, clauses);
    }

}
package it.cnr.contab.inventario01.bp;

import it.cnr.contab.inventario01.bulk.AllegatoDocumentoTrasportoBulk;
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.inventario01.bulk.DocumentoTrasportoBulk;
import it.cnr.contab.inventario01.bulk.DocumentoTrasportoDettBulk;
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
 * Business Process specializzato per la gestione del flusso di TRASPORTO Beni.
 * Gestisce la logica di testata, dettaglio e allegati specifica per i documenti di trasporto,
 * interfacciandosi con il componente per il recupero dei beni inventariati.
 */
public class CRUDTrasportoBeniInvBP extends CRUDTraspRientInventarioBP<AllegatoDocumentoTrasportoBulk, DocumentoTrasportoBulk> {

    public CRUDTrasportoBeniInvBP() {
        super();
    }

    public CRUDTrasportoBeniInvBP(String function) {
        super(function);
    }

    /**
     * Restituisce il percorso di storage CMIS per il documento di trasporto.
     */
    @Override
    protected String getStorePath(DocumentoTrasportoBulk documentoTrasportoBulk, boolean create) throws BusinessProcessException {
        if (documentoTrasportoBulk == null) {
            throw new BusinessProcessException("Documento di trasporto non presente");
        }
        return documentoTrasportoBulk.getStorePath().get(0);
    }

    /**
     * Restituisce la classe Bulk utilizzata per la gestione degli allegati del trasporto.
     */
    @Override
    protected Class getAllegatoClass() {
        return AllegatoDocumentoTrasportoBulk.class;
    }

    /**
     * Definisce i tab dell'interfaccia utente in base allo stato del documento.
     */
    public String[][] getTabs() {
        TreeMap<Integer, String[]> hash = new TreeMap<>();
        int i = 0;
        hash.put(i++, new String[]{"tabTrasportoTestata", "Testata", "/inventario00/tab_testata_doc_t.jsp"});

        if (isInserting()) {
            hash.put(i++, new String[]{"tabTrasportoDettaglio", "Dettaglio", "/inventario00/tab_trasporto_inv_dett.jsp"});
        } else {
            hash.put(i++, new String[]{"tabTrasportoDettaglio", "Dettaglio", "/inventario00/tab_trasporto_inv_edit_dett.jsp"});
        }
        hash.put(i++, new String[]{"tabAllegati", "Allegati", "/util00/tab_allegati.jsp"});

        String[][] tabs = new String[i][3];
        for (int j = 0; j < i; j++) {
            tabs[j] = new String[]{hash.get(j)[0], hash.get(j)[1], hash.get(j)[2]};
        }
        return tabs;
    }

    /**
     * Nome del controller associato ai dettagli in fase di inserimento.
     */
    @Override
    protected String getDettagliControllerName() {
        return "DettagliTrasporto";
    }

    /**
     * Nome del controller associato ai dettagli in fase di modifica.
     */
    @Override
    protected String getEditDettagliControllerName() {
        return "EditDettagliTrasporto";
    }

    /**
     * Restituisce il nome del tab principale per la testata.
     */
    @Override
    public String getMainTabName() {
        return "tabTrasportoTestata";
    }

    /**
     * Restituisce la label visualizzata per la data di registrazione.
     */
    @Override
    public String getLabelData_registrazione() {
        return "Data Trasporto";
    }

    /**
     * Inizializza la selezione dei componenti (hook per logiche future).
     */
    @Override
    public void inizializzaSelezioneComponente(ActionContext context) throws ComponentException, RemoteException {
    }

    /**
     * Annulla le modifiche effettuate a livello di componente (hook per logiche future).
     */
    @Override
    public void annullaModificaComponente(ActionContext context) throws ComponentException, RemoteException {
    }

    /**
     * Aggiorna il modello del documento a seguito della modifica dei beni e dei relativi accessori.
     */
    @Override
    public void modificaBeniConAccessoriComponente(ActionContext context, OggettoBulk[] bulks,
                                                   BitSet oldSelection, BitSet newSelection)
            throws ComponentException, RemoteException, BusinessProcessException {
        Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) getModel();

        getComp().modificaBeniTrasportatiConAccessori(
                context.getUserContext(), doc, bulks, oldSelection, newSelection);

        BulkList dettagliAggiornati = getComp().getDetailsFor(
                context.getUserContext(), doc);
        doc.setDoc_trasporto_rientro_dettColl(dettagliAggiornati);

        setModel(context, doc);
    }


    /**
     * Restituisce la classe Bulk specifica per il dettaglio del trasporto.
     */
    @Override
    public Class getDocumentoClassDett() {
        return DocumentoTrasportoDettBulk.class;
    }

    /**
     * Recupera l'elenco dei beni disponibili per il trasporto.
     */
    public RemoteIterator getListaBeniDaTrasportare(it.cnr.jada.UserContext userContext, SimpleBulkList beni_da_escludere, it.cnr.jada.persistency.sql.CompoundFindClause clauses) throws BusinessProcessException, RemoteException, ComponentException {
        return getComp().getListaBeniDaTrasportare(userContext, getDoc(), beni_da_escludere, clauses);
    }
}
package it.cnr.contab.inventario01.bp;

import it.cnr.contab.inventario01.bulk.AllegatoDocumentoTrasportoBulk;
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.inventario01.bulk.DocumentoTrasportoBulk;
import it.cnr.contab.inventario01.bulk.DocumentoTrasportoDettBulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.bulk.SimpleBulkList;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.util.RemoteIterator;

import java.rmi.RemoteException;
import java.util.BitSet;
import java.util.TreeMap;

/**
 * Business Process per la gestione dei Documenti di TRASPORTO.
 * <p>
 * Contiene SOLO le specifiche del Trasporto:
 * - Tipo documento = "T"
 * - Nomi controller specifici
 * - Chiamate component session specifiche per trasporto
 */
public class CRUDTrasportoBeniInvBP extends CRUDTraspRientInventarioBP<AllegatoDocumentoTrasportoBulk, DocumentoTrasportoBulk>{

    public CRUDTrasportoBeniInvBP() {
        super();
    }

    public CRUDTrasportoBeniInvBP(String function) {
        super(function);
    }

    @Override
    protected String getStorePath(DocumentoTrasportoBulk documentoTrasportoBulk, boolean create) throws BusinessProcessException {
        if (documentoTrasportoBulk == null) {
            throw new BusinessProcessException("Documento di trasporto non presente");
        }
        return documentoTrasportoBulk.getStorePath().get(0);
    }


    @Override
    protected Class getAllegatoClass() {
        return it.cnr.contab.inventario01.bulk.AllegatoDocumentoTrasportoBulk.class;
    }

    public String[][] getTabs() {
        TreeMap<Integer, String[]> hash = new TreeMap<>();
        int i=0;
        hash.put(i++, new String[]{ "tabTrasportoTestata","Testata","/inventario00/tab_testata_doc_t.jsp" });
        if ( isInserting())
            hash.put(i++, new String[]{ "tabTrasportoDettaglio","Dettaglio","/inventario00/tab_trasporto_inv_dett.jsp"});
        else
            hash.put(i++, new String[]{ "tabTrasportoDettaglio","Dettaglio","/inventario00/tab_trasporto_inv_edit_dett.jsp"});
        hash.put(i++, new String[]{"tabAllegati", "Allegati", "/util00/tab_allegati.jsp"});

        String[][] tabs = new String[i][3];
        for (int j = 0; j < i; j++)
            tabs[j]=new String[]{hash.get(j)[0],hash.get(j)[1],hash.get(j)[2]};
        return tabs;
    }
    // ==================== IMPLEMENTAZIONE METODI ASTRATTI ====================

    @Override
    protected String getDettagliControllerName() {
        return "DettagliTrasporto";
    }

    @Override
    protected String getEditDettagliControllerName() {
        return "EditDettagliTrasporto";
    }

    @Override
    public String getMainTabName() {
        return "tabTrasportoTestata";
    }

    @Override
    public String getLabelData_registrazione() {
        return "Data Trasporto";
    }

    @Override
    protected void inizializzaSelezioneComponente(ActionContext context)
            throws ComponentException, RemoteException {
    }

    @Override
    protected void annullaModificaComponente(ActionContext context)
            throws ComponentException, RemoteException {
    }


    @Override
    public void modificaBeniConAccessoriComponente(
            ActionContext context,
            OggettoBulk[] bulks,
            BitSet oldSelection,
            BitSet newSelection)
            throws ComponentException, RemoteException, BusinessProcessException {

        try {
            // ✅ RICEVI IL DOCUMENTO AGGIORNATO
            Doc_trasporto_rientroBulk docAggiornato = getComp().modificaBeniTrasportatiConAccessori(
                    context.getUserContext(),
                    getDoc(),
                    bulks,
                    oldSelection,
                    newSelection
            );

            // ✅ AGGIORNA IL MODEL NEL BP
            setModel(context, docAggiornato);

        } catch (ComponentException | RemoteException e) {
            throw handleException(e);
        }
    }

    @Override
    public Class getDocumentoClassDett() {
        return DocumentoTrasportoDettBulk.class;
    }

    // ==================== METODO SPECIFICO TRASPORTO ====================

    /**
     * Ottiene lista beni DA TRASPORTARE (qualsiasi bene inventariato)
     */
    public RemoteIterator getListaBeniDaTrasportare(
            it.cnr.jada.UserContext userContext,
            SimpleBulkList beni_da_escludere,
            it.cnr.jada.persistency.sql.CompoundFindClause clauses)
            throws BusinessProcessException, RemoteException, ComponentException {
        return getComp().getListaBeniDaTrasportare(userContext, getDoc(), beni_da_escludere, clauses);
    }




}

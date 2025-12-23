package it.cnr.contab.inventario01.bp;

import it.cnr.contab.inventario01.bulk.AllegatoDocumentoRientroBulk;
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.inventario01.bulk.DocumentoRientroBulk;
import it.cnr.contab.inventario01.bulk.DocumentoRientroDettBulk;
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
 * Business Process per la gestione dei Documenti di RIENTRO.
 * <p>
 * Contiene SOLO le specifiche del Rientro:
 * - Tipo documento = "R"
 * - Nomi controller specifici
 * - Chiamate component session specifiche per rientro
 * <p>
 * CARATTERISTICHE SPECIFICHE PER RIENTRO:
 * - I beni devono provenire da documenti di TRASPORTO FIRMATI
 * - Ogni bene può rientrare una sola volta
 * - Collegamento obbligatorio con documento di trasporto di riferimento
 */
public class CRUDRientroBeniInvBP extends CRUDTraspRientInventarioBP<AllegatoDocumentoRientroBulk, DocumentoRientroBulk> {

    public CRUDRientroBeniInvBP() {
        super();
    }

    public CRUDRientroBeniInvBP(String function) {
        super(function);
    }

    @Override
    protected String getStorePath(DocumentoRientroBulk documentoRientroBulk, boolean create)
            throws BusinessProcessException {

        if (documentoRientroBulk == null) {
            throw new BusinessProcessException("Documento di rientro non presente");
        }

        return documentoRientroBulk.getStorePath().get(0);
    }

    @Override
    protected Class getAllegatoClass() {
        return AllegatoDocumentoRientroBulk.class;
    }


    public String[][] getTabs() {
        TreeMap<Integer, String[]> hash = new TreeMap<>();
        int i=0;
        hash.put(i++, new String[]{ "tabRientroTestata","Testata","/inventario00/tab_testata_doc_r.jsp" });

        if ( isInserting())
            hash.put(i++, new String[]{ "tabRientroDettaglio","Dettaglio","/inventario00/tab_rientro_inv_dett.jsp"});
        else
            hash.put(i++, new String[]{ "tabRientroDettaglio","Dettaglio","/inventario00/tab_rientro_inv_edit_dett.jsp"});
        hash.put(i++, new String[]{"tabAllegati", "Allegati", "/util00/tab_allegati.jsp"});

        String[][] tabs = new String[i][3];
        for (int j = 0; j < i; j++)
            tabs[j]=new String[]{hash.get(j)[0],hash.get(j)[1],hash.get(j)[2]};
        return tabs;
    }

    // ==================== IMPLEMENTAZIONE METODI ASTRATTI ====================

    @Override
    protected String getDettagliControllerName() {
        return "DettagliRientro";
    }

    @Override
    protected String getEditDettagliControllerName() {
        return "EditDettagliRientro";
    }

    @Override
    protected String getMainTabName() {
        return "tabRientroTestata";
    }

    @Override
    public String getLabelData_registrazione() {
        return "Data Rientro";
    }

    @Override
    protected void inizializzaSelezioneComponente(ActionContext context)
            throws ComponentException, RemoteException {
        try {
            getComp().inizializzaBeniDaFarRientrare(context.getUserContext());
        } catch (BusinessProcessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void annullaModificaComponente(ActionContext context)
            throws ComponentException, RemoteException {
        try {
            getComp().annullaModificaRientroBeni(context.getUserContext());
        } catch (BusinessProcessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void selezionaTuttiBeniComponente(ActionContext context)
            throws ComponentException, RemoteException {
        try {
            getComp().rientraTuttiBeni(context.getUserContext(), getDoc(), getClauses());
        } catch (BusinessProcessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void modificaBeniConAccessoriComponente(
            ActionContext context,
            OggettoBulk[] bulks,
            BitSet oldSelection,
            BitSet newSelection) throws ComponentException, RemoteException {
        try {
            getComp().modificaBeniRientratiConAccessori(
                    context.getUserContext(),
                    getDoc(),
                    bulks,
                    oldSelection,
                    newSelection);
        } catch (BusinessProcessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Class getDocumentoClassDett() {
        return DocumentoRientroDettBulk.class;
    }


    // ==================== METODO SPECIFICO RIENTRO ====================

    /**
     * Ottiene lista beni DA FAR RIENTRARE (solo da doc. trasporto FIRMATI)
     */
    public RemoteIterator getListaBeniDaFarRientrare(
            it.cnr.jada.UserContext userContext,
            SimpleBulkList beni_da_escludere,
            it.cnr.jada.persistency.sql.CompoundFindClause clauses)
            throws BusinessProcessException, RemoteException, ComponentException {
        return getComp().getListaBeniDaFarRientrare(userContext, getDoc(), beni_da_escludere, clauses);
    }



}
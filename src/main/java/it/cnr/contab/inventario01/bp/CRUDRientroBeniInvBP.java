package it.cnr.contab.inventario01.bp;

import it.cnr.contab.inventario01.bulk.AllegatoDocumentoRientroBulk;
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.inventario01.bulk.DocumentoRientroBulk;
import it.cnr.contab.inventario01.bulk.DocumentoRientroDettBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.bulk.BulkList;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.bulk.SimpleBulkList;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.util.RemoteIterator;

import java.rmi.RemoteException;
import java.util.BitSet;

/** Business Process per il flusso di Rientro Beni. */
public class CRUDRientroBeniInvBP
        extends CRUDTraspRientInventarioBP<AllegatoDocumentoRientroBulk, DocumentoRientroBulk> {

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
        return new String[][]{
                {"tabRientroTestata", "Testata", "/inventario00/tab_testata_doc_r.jsp"},
                {"tabRientroDettaglio", "Dettaglio",
                        isInserting()
                                ? "/inventario00/tab_rientro_inv_dett.jsp"
                                : "/inventario00/tab_rientro_inv_edit_dett.jsp"},
                {"tabAllegati", "Allegati", "/util00/tab_allegati.jsp"}
        };
    }

    @Override
    protected String getDettagliControllerName() {
        return "DettagliRientro";
    }

    @Override
    protected String getEditDettagliControllerName() {
        return "EditDettagliRientro";
    }

    @Override
    public String getMainTabName() {
        return "tabRientroTestata";
    }

    @Override
    public String getLabelData_registrazione() {
        return "Data Rientro";
    }

    @Override
    public void inizializzaSelezioneComponente(ActionContext context)
            throws ComponentException, RemoteException {
    }

    @Override
    public void annullaModificaComponente(ActionContext context)
            throws ComponentException, RemoteException {
    }

    @Override
    public void modificaBeniConAccessoriComponente(ActionContext context, OggettoBulk[] bulks,
                                                   BitSet oldSelection, BitSet newSelection)
            throws ComponentException, RemoteException, BusinessProcessException {

        Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) getModel();

        getComp().modificaBeniRientratiConAccessori(
                context.getUserContext(), doc, bulks, oldSelection, newSelection);

        BulkList dettagliAggiornati = getComp().getDetailsFor(context.getUserContext(), doc);
        doc.setDoc_trasporto_rientro_dettColl(dettagliAggiornati);

        setModel(context, doc);
    }

    @Override
    public Class getDocumentoClassDett() {
        return DocumentoRientroDettBulk.class;
    }

    public RemoteIterator getListaBeniDaFarRientrare(UserContext userContext,
                                                     SimpleBulkList beniDaEscludere,
                                                     CompoundFindClause clauses)
            throws BusinessProcessException, RemoteException, ComponentException {
        return getComp().getListaBeniDaFarRientrare(userContext, getDoc(), beniDaEscludere, clauses);
    }
}
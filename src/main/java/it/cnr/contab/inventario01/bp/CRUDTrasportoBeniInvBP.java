package it.cnr.contab.inventario01.bp;

import it.cnr.contab.inventario01.bulk.AllegatoDocumentoTrasportoBulk;
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.inventario01.bulk.DocumentoTrasportoBulk;
import it.cnr.contab.inventario01.bulk.DocumentoTrasportoDettBulk;
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

/** Business Process per il flusso di Trasporto Beni. */
public class CRUDTrasportoBeniInvBP
        extends CRUDTraspRientInventarioBP<AllegatoDocumentoTrasportoBulk, DocumentoTrasportoBulk> {

    public CRUDTrasportoBeniInvBP() {
        super();
    }

    public CRUDTrasportoBeniInvBP(String function) {
        super(function);
    }

    @Override
    protected String getStorePath(DocumentoTrasportoBulk documentoTrasportoBulk, boolean create)
            throws BusinessProcessException {
        if (documentoTrasportoBulk == null) {
            throw new BusinessProcessException("Documento di trasporto non presente");
        }
        return documentoTrasportoBulk.getStorePath().get(0);
    }

    @Override
    protected Class getAllegatoClass() {
        return AllegatoDocumentoTrasportoBulk.class;
    }

    public String[][] getTabs() {
        return new String[][]{
                {"tabTrasportoTestata", "Testata", "/inventario00/tab_testata_doc_t.jsp"},
                {"tabTrasportoDettaglio", "Dettaglio",
                        isInserting()
                                ? "/inventario00/tab_trasporto_inv_dett.jsp"
                                : "/inventario00/tab_trasporto_inv_edit_dett.jsp"},
                {"tabAllegati", "Allegati", "/util00/tab_allegati.jsp"}
        };
    }

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

        getComp().modificaBeniTrasportatiConAccessori(
                context.getUserContext(), doc, bulks, oldSelection, newSelection);

        BulkList dettagliAggiornati = getComp().getDetailsFor(context.getUserContext(), doc);
        doc.setDoc_trasporto_rientro_dettColl(dettagliAggiornati);

        setModel(context, doc);
    }

    @Override
    public Class getDocumentoClassDett() {
        return DocumentoTrasportoDettBulk.class;
    }

    public RemoteIterator getListaBeniDaTrasportare(UserContext userContext,
                                                    SimpleBulkList beniDaEscludere,
                                                    CompoundFindClause clauses)
            throws BusinessProcessException, RemoteException, ComponentException {
        return getComp().getListaBeniDaTrasportare(userContext, getDoc(), beniDaEscludere, clauses);
    }
}
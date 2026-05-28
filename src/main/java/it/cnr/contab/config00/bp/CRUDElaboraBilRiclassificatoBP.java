package it.cnr.contab.config00.bp;

import it.cnr.contab.config00.ejb.PDCEconPatrComponentSession;
import it.cnr.contab.config00.pdcep.bulk.BilRiclassificatoBulk;
import it.cnr.contab.config00.pdcep.bulk.TipoBilancioBulk;
import it.cnr.contab.config00.pdcep.bulk.TipoBilancioKey;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.MessageToUser;
import it.cnr.jada.bulk.FillException;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.ejb.CRUDComponentSession;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;
import it.cnr.jada.persistency.sql.SimpleFindClause;
import it.cnr.jada.util.Config;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.action.FormBP;
import it.cnr.jada.util.action.SelectionIterator;
import it.cnr.jada.util.action.SelezionatoreSearchBP;
import it.cnr.jada.util.ejb.EJBCommonServices;
import it.cnr.jada.util.jsp.Button;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Optional;

public class CRUDElaboraBilRiclassificatoBP extends SelezionatoreSearchBP {

    public CRUDElaboraBilRiclassificatoBP() {
        super();
        table.setStatus(EDIT);
        table.setMultiSelection(true);
        table.setEditableOnFocus(true);
        table.setReadonly(false);
    }

    @Override
    protected void setFocusedElement(ActionContext actioncontext, Object obj) throws BusinessProcessException {
    }

    @Override
    protected void init(it.cnr.jada.action.Config config, ActionContext actioncontext) throws BusinessProcessException {
        super.init(config, actioncontext);
        CRUDComponentSession crudComponentSession = EJBCommonServices.createEJB("JADAEJB_CRUDComponentSession", CRUDComponentSession.class);
        try {
            BilRiclassificatoBulk bilRiclassificatoBulk = new BilRiclassificatoBulk();
            List<TipoBilancioBulk> tipoBilanci = crudComponentSession.find(actioncontext.getUserContext(), TipoBilancioBulk.class, "findAll");
            bilRiclassificatoBulk.setTipoBilanci(tipoBilanci);
            bilRiclassificatoBulk.setTipo_bilancio(tipoBilanci.stream().findFirst().orElse(null));
            setModel(actioncontext, bilRiclassificatoBulk);
            it.cnr.jada.util.RemoteIterator ri = search(actioncontext,null, getModel());
            this.setIterator(actioncontext, ri);
        } catch (ComponentException|RemoteException e) {
            throw handleException(e);
        }
    }

    public OggettoBulk[] fillModels(ActionContext actioncontext) throws FillException {
        OggettoBulk aoggettobulk[] = getPageContents();
        for(int i = 0; i < aoggettobulk.length; i++)
        {
            OggettoBulk oggettobulk = aoggettobulk[i];
            if (oggettobulk.fillFromActionContext(actioncontext, "mainTable.[" + (i + getFirstElementIndexOnCurrentPage()), EDIT, getFieldValidationMap()))
                setDirty(true);
        }
        return aoggettobulk;
    }

    @Override
    public List<Button> createToolbarList() {
        List<Button> toolbarList = super.createToolbarList();
        toolbarList.add(new Button(Config.getHandler().getProperties(getClass()), "CRUDToolbar.elabora"));
        toolbarList.add(new Button(Config.getHandler().getProperties(getClass()), "CRUDToolbar.refresh"));
        toolbarList.add(new Button(Config.getHandler().getProperties(getClass()), "CRUDToolbar.delete"));
        return toolbarList;
    }

    public void elaboraBilancio(ActionContext actioncontext) throws BusinessProcessException {
        PDCEconPatrComponentSession pdcEconPatrComponentSession = (PDCEconPatrComponentSession) EJBCommonServices.createEJB("CNRCONFIG00_EJB_PDCEconPatrComponentSession");
        try {
            pdcEconPatrComponentSession.generaBilancio(actioncontext.getUserContext(), ((BilRiclassificatoBulk)getModel()).getTipo_bilancio().getCdTipoBilancio(), ((BilRiclassificatoBulk)getModel()).getCdPianoGruppi());
            refresh(actioncontext);
            setMessage(FormBP.INFO_MESSAGE, "Operazione effettuata");
        } catch (ComponentException|RemoteException e) {
            throw handleException(e);
        }
    }

    public void aggiornaImportoFinale(ActionContext actioncontext) throws BusinessProcessException{
        CRUDComponentSession crudComponentSession = EJBCommonServices.createEJB("JADAEJB_CRUDComponentSession", CRUDComponentSession.class);
        BilRiclassificatoBulk bilRiclassificatoBulk = (BilRiclassificatoBulk)getFocusedElement(actioncontext);
        BigDecimal importoFinale = bilRiclassificatoBulk.getImportoFinale();
        try {
            bilRiclassificatoBulk = (BilRiclassificatoBulk) crudComponentSession.inizializzaBulkPerModifica(actioncontext.getUserContext(), bilRiclassificatoBulk);
            bilRiclassificatoBulk.setImportoFinale(importoFinale);
            bilRiclassificatoBulk.setRettifica(Optional.ofNullable(bilRiclassificatoBulk.getImportoFinale()).orElseThrow(() -> new ApplicationException("Valorizzare l'importo!")).subtract(bilRiclassificatoBulk.getSaldoConto()));
            bilRiclassificatoBulk.setToBeUpdated();
            crudComponentSession.modificaConBulk(actioncontext.getUserContext(), bilRiclassificatoBulk);
        } catch (ComponentException|RemoteException e) {
            throw handleException(e);
        }
    }

    public void aggiornaNote(ActionContext actioncontext) throws BusinessProcessException{
        CRUDComponentSession crudComponentSession = EJBCommonServices.createEJB("JADAEJB_CRUDComponentSession", CRUDComponentSession.class);
        BilRiclassificatoBulk bilRiclassificatoBulk = (BilRiclassificatoBulk)getFocusedElement(actioncontext);
        String note = bilRiclassificatoBulk.getNote();
        try {
            bilRiclassificatoBulk = (BilRiclassificatoBulk) crudComponentSession.inizializzaBulkPerModifica(actioncontext.getUserContext(), bilRiclassificatoBulk);
            bilRiclassificatoBulk.setNote(note);
            bilRiclassificatoBulk.setToBeUpdated();
            crudComponentSession.modificaConBulk(actioncontext.getUserContext(), bilRiclassificatoBulk);
        } catch (ComponentException|RemoteException e) {
            throw handleException(e);
        }
    }

    public void elimina(ActionContext actioncontext) throws BusinessProcessException{
        CRUDComponentSession crudComponentSession = EJBCommonServices.createEJB("JADAEJB_CRUDComponentSession", CRUDComponentSession.class);
        try {
            if (!selection.isEmpty()) {
                for (SelectionIterator i = selection.iterator(); i.hasNext();) {
                    OggettoBulk bulk = (OggettoBulk) getElementAt( actioncontext,i.nextIndex());
                    bulk.setToBeDeleted();
                    crudComponentSession.eliminaConBulk(actioncontext.getUserContext(),bulk);
                }
            } else {
                throw new MessageToUser( "E' necessario selezionare gli elementi da cancellare" );
            }
            refresh(actioncontext);
        } catch (ComponentException|RemoteException e) {
            throw handleException(e);
        }
    }

    @Override
    public RemoteIterator search(ActionContext actioncontext, CompoundFindClause compoundfindclause, OggettoBulk oggettobulk) throws BusinessProcessException {
        compoundfindclause = Optional.ofNullable(compoundfindclause).orElseGet(CompoundFindClause::new);
        compoundfindclause.addClause(FindClause.AND, "cdTipoBilancio", SQLBuilder.EQUALS, ((BilRiclassificatoBulk)getModel()).getTipo_bilancio().getCdTipoBilancio());
        compoundfindclause.addClause(FindClause.AND, "cdPianoGruppi", SQLBuilder.EQUALS, ((BilRiclassificatoBulk)getModel()).getCdPianoGruppi());
        return super.search(actioncontext, compoundfindclause, oggettobulk);
    }
}

package it.cnr.contab.config00.bp;

import it.cnr.contab.config00.bulk.Configurazione_cnrBulk;
import it.cnr.contab.config00.comp.Configurazione_cnrComponent;
import it.cnr.contab.config00.ejb.Configurazione_cnrComponentSession;
import it.cnr.contab.config00.latt.bulk.WorkpackageBulk;
import it.cnr.contab.cori00.docs.bulk.Liquid_gruppo_centroBulk;
import it.cnr.contab.cori00.ejb.Liquid_coriComponentSession;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.util.Utility;
import it.cnr.jada.UserContext;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.ActionPerformingError;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Config;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.bulk.ValidationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.ejb.RicercaComponentSession;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.LoggableStatement;
import it.cnr.jada.persistency.sql.SQLBuilder;
import it.cnr.jada.util.OrderConstants;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.action.BulkBP;
import it.cnr.jada.util.action.CRUDBP;
import it.cnr.jada.util.action.RemoteDetailCRUDController;
import it.cnr.jada.util.jsp.Button;

import javax.ejb.EJBException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

public class SelezionatoreStepFineAnnoBP extends BulkBP {
    private final RemoteDetailCRUDController detail = new RemoteDetailCRUDController(
            Configurazione_cnrBulk.PK_STEP_FINE_ANNO,
            Configurazione_cnrBulk.class,
            Configurazione_cnrBulk.PK_STEP_FINE_ANNO,
            "CNRCONFIG00_EJB_Configurazione_cnrComponentSession",
            this,
            Boolean.FALSE) {

        @Override
        protected RemoteIterator createRemoteIterator(ActionContext actioncontext) {
            try {
                return ((RicercaComponentSession)createComponentSession("CNRCONFIG00_EJB_Configurazione_cnrComponentSession", RicercaComponentSession.class))
                        .cerca(actioncontext.getUserContext(),
                                getBaseClause(actioncontext, getFilter()),
                                (OggettoBulk) getBulkInfo().getBulkClass().newInstance());
            } catch (ComponentException | RemoteException | IllegalAccessException | InstantiationException |
                     BusinessProcessException e) {
                throw new ActionPerformingError(e);
            }
        }

        @Override
        protected void removeDetails(ActionContext actioncontext, OggettoBulk[] aoggettobulk) throws BusinessProcessException {
            Arrays.stream(aoggettobulk)
                    .forEach(oggettoBulk -> oggettoBulk.setToBeDeleted());
            super.removeDetails(actioncontext, aoggettobulk);
        }

        public boolean isGrowable() {
            return true;
        }
        public boolean isShrinkable() {
            return true;
        }
    };


    public SelezionatoreStepFineAnnoBP(String s) {
        super(s);
    }

    public SelezionatoreStepFineAnnoBP() {
        super("Tr");
    }
    public RemoteDetailCRUDController getDetail() {
        return detail;
    }

    @Override
    public RemoteIterator find(ActionContext actioncontext, CompoundFindClause compoundfindclause, OggettoBulk oggettobulk, OggettoBulk oggettobulk1, String s) throws BusinessProcessException {
        return null;
    }

    private CompoundFindClause getBaseClause(ActionContext context, CompoundFindClause compoundFindClause) {
        CompoundFindClause baseClause = new CompoundFindClause();
        baseClause.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, CNRUserContext.getEsercizio(context.getUserContext()));
        baseClause.addClause(FindClause.AND, "cd_chiave_primaria", SQLBuilder.EQUALS, Configurazione_cnrBulk.PK_STEP_FINE_ANNO);
        Optional.ofNullable(compoundFindClause).ifPresent(compoundFindClause1 -> baseClause.addChild(compoundFindClause1));
        return baseClause;
    }

    @Override
    protected void init(Config config, ActionContext actioncontext) throws BusinessProcessException {
        super.init(config, actioncontext);
        setModel(actioncontext, new Configurazione_cnrBulk());
        resyncChildren(actioncontext);
        getDetail().setOrderBy(actioncontext, "cd_chiave_secondaria", OrderConstants.ORDER_ASC);
    }

    public void save(ActionContext actioncontext)
            throws ValidationException, BusinessProcessException {
        commitUserTransaction();
        if (getMessage() == null)
            setMessage(INFO_MESSAGE, "Salvataggio eseguito in modo corretto.");
        resyncChildren(actioncontext);
    }

    @Override
    protected String getFreeSearchSet() {
        return Configurazione_cnrBulk.PK_STEP_FINE_ANNO;
    }

    public boolean isSaveButtonEnabled() {
        return isDirty();
    }
    public boolean isSaveButtonHidden() {
        return Boolean.FALSE;
    }
    @Override
    protected Button[] createToolbar() {
        final Properties properties = it.cnr.jada.util.Config.getHandler().getProperties(CRUDBP.class);
        return Stream.of(
                        new Button(properties, "CRUDToolbar.save"),
                        new Button(it.cnr.jada.util.Config.getHandler().getProperties(getClass()), "CRUDToolbar.ribaltaProgetti")
                ).toArray(Button[]::new);
    }

    public String getFormName() {
        return Optional.ofNullable(detail.getModel())
                .filter(Configurazione_cnrBulk.class::isInstance)
                .map(Configurazione_cnrBulk.class::cast)
                .filter(configurazioneCnrBulk ->
                        Stream.of(
                                Configurazione_cnrBulk.StepFineAnno.STORNO_FATT_PAS,
                                Configurazione_cnrBulk.StepFineAnno.STORNO_FATT_ATT
                        ).map(Configurazione_cnrBulk.StepFineAnno::value)
                                .anyMatch(s -> s.equalsIgnoreCase(configurazioneCnrBulk.getCd_chiave_secondaria()))
                )
                .map(configurazioneCnrBulk -> "STEP_FINE_ANNO_ONLY_DATE")
                .orElse(Configurazione_cnrBulk.PK_STEP_FINE_ANNO);
    }

    public void ribaltaProgetti(ActionContext context) throws BusinessProcessException {
        try{
            Configurazione_cnrComponentSession sess = Utility.createConfigurazioneCnrComponentSession();
            sess.ribaltaProgetti(context.getUserContext(), CNRUserContext.getEsercizio(context.getUserContext()));
        }catch(EJBException | RemoteException | ComponentException e){
            throw handleException(e);
        }
    }
}

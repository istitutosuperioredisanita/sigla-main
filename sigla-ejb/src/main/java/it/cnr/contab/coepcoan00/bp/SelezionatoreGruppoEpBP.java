package it.cnr.contab.coepcoan00.bp;

import it.cnr.contab.config00.pdcep.bulk.GruppoEPBase;
import it.cnr.contab.config00.pdcep.bulk.GruppoEPBulk;
import it.cnr.contab.config00.pdcep.bulk.TipoBilancioBulk;
import it.cnr.jada.DetailedRuntimeException;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Config;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.ejb.CRUDComponentSession;
import it.cnr.jada.util.OrderConstants;
import it.cnr.jada.util.RemoteBulkTree;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.RemoteOrderable;
import it.cnr.jada.util.action.SelezionatoreListaAlberoBP;
import it.cnr.jada.util.action.SimpleNestedFormController;
import it.cnr.jada.util.ejb.EJBCommonServices;
import it.cnr.jada.util.jsp.Button;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

public class SelezionatoreGruppoEpBP extends SelezionatoreListaAlberoBP {
    private SimpleNestedFormController selezioneTipoBilancioController;
    private GruppoEPBulk selezione_tipo_bilancio;

    public SelezionatoreGruppoEpBP() {
    }

    public SelezionatoreGruppoEpBP(String s) {
        super(s);
        selezioneTipoBilancioController = new SimpleNestedFormController("selezione_tipo_bilancio", GruppoEPBulk.class, this);
    }

    @Override
    public Button[] createToolbar() {
        final Properties properties = it.cnr.jada.util.Config.getHandler().getProperties(getClass());
        return Stream.of(
                        new Button(properties, "Toolbar.expand"),
                        new Button(properties, "Toolbar.back"),
                        new Button(properties, "Toolbar.new"),
                        new Button(properties, "Toolbar.newChildren"),
                        new Button(properties, "Toolbar.edit"),
                        new Button(properties, "Toolbar.delete"),
                        new Button(properties, "Toolbar.associaConti"),
                        new Button(properties, "Toolbar.visualizzaConti")
                ).toArray(Button[]::new);
    }

    @Override
    protected void init(Config config, ActionContext actioncontext) throws BusinessProcessException {
        super.init(config, actioncontext);
        try {
            setPageSize(50);
            selezione_tipo_bilancio = new GruppoEPBulk();
            selezione_tipo_bilancio.setTipoBilanci(
                    createComponentSession()
                        .find(
                                actioncontext.getUserContext(),
                                TipoBilancioBulk.class,
                                "findAll",
                                actioncontext.getUserContext()
                        )
            );
            selezione_tipo_bilancio.setTipoBilancio(selezione_tipo_bilancio.getTipoBilanci().stream().findFirst().orElse(null));
            selezioneTipoBilancioController.setModel(actioncontext, selezione_tipo_bilancio);
            setBulkInfo(it.cnr.jada.bulk.BulkInfo.getBulkInfo(GruppoEPBulk.class));
            refreshRemoteBulkTree(actioncontext);
        } catch (RemoteException | ComponentException e) {
            throw handleException(e);
        }
    }

    public void refreshRemoteBulkTree(ActionContext actionContext) throws BusinessProcessException {
        try {
            if (getParentElement() != null) {
                RemoteIterator children = getRemoteBulkTree().getChildren(actionContext, getParentElement());
                children.refresh();
                setIterator(actionContext, children);
            } else {
                RemoteIterator roots = getGruppoEPTree().getChildren(
                        actionContext,
                        selezione_tipo_bilancio
                );
                orderRemoteIterator(roots);
                setRemoteBulkTree(actionContext, getGruppoEPTree(), roots);
            }
        } catch (RemoteException | ComponentException e) {
            throw handleException(e);
        }
    }

    private void orderRemoteIterator(RemoteIterator remoteIterator) {
        Stream.of("cdPianoGruppi","sequenza").forEach(s -> {
            try {
                ((RemoteOrderable) remoteIterator).setOrderBy(s, OrderConstants.ORDER_ASC);
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CRUDComponentSession createComponentSession() throws BusinessProcessException {
        return Optional.ofNullable(createComponentSession("JADAEJB_CRUDComponentSession"))
                .filter(CRUDComponentSession.class::isInstance)
                .map(CRUDComponentSession.class::cast)
                .orElseThrow(() -> new BusinessProcessException("Cannot create JADAEJB_CRUDComponentSession"));
    }

    public RemoteBulkTree getGruppoEPTree() throws it.cnr.jada.comp.ComponentException {
        return
                new RemoteBulkTree() {
                    public RemoteIterator getChildren(ActionContext context, OggettoBulk bulk) throws java.rmi.RemoteException {
                        try {
                            RemoteIterator remoteIterator = createComponentSession().cerca(
                                    context.getUserContext(),
                                    null,
                                    Optional.ofNullable(bulk).orElse(new GruppoEPBulk()),
                                    "findChildren"
                            );
                            orderRemoteIterator(remoteIterator);
                            return EJBCommonServices.openRemoteIterator(context, remoteIterator);
                        } catch (ComponentException | BusinessProcessException ex) {
                            throw new DetailedRuntimeException(ex);
                        }
                    }

                    public OggettoBulk getParent(ActionContext context, OggettoBulk bulk) throws java.rmi.RemoteException {
                        try {
                            return createComponentSession().find(
                                            context.getUserContext(),
                                            GruppoEPBulk.class,
                                            "findParents",
                                            bulk
                                    )
                                    .stream()
                                    .findFirst()
                                    .orElseThrow(() -> new DetailedRuntimeException("Cannot find parent"));
                        } catch (ComponentException | BusinessProcessException ex) {
                            throw new DetailedRuntimeException(ex);
                        }
                    }

                    public boolean isLeaf(ActionContext context, OggettoBulk bulk) throws java.rmi.RemoteException {
                        try {
                            List<OggettoBulk> children = createComponentSession().find(
                                    context.getUserContext(),
                                    GruppoEPBulk.class,
                                    "findChildren",
                                    context.getUserContext(),
                                    bulk
                            );
                            return children.isEmpty();
                        } catch (ComponentException | BusinessProcessException ex) {
                            throw new DetailedRuntimeException(ex);
                        }
                    }
                };
    }

    public SimpleNestedFormController getSelezioneTipoBilancioController() {
        return selezioneTipoBilancioController;
    }

    public void setSelezioneTipoBilancioController(SimpleNestedFormController selezioneTipoBilancioController) {
        this.selezioneTipoBilancioController = selezioneTipoBilancioController;
    }

    public GruppoEPBulk getSelezione_tipo_bilancio() {
        return selezione_tipo_bilancio;
    }

    public void setSelezione_tipo_bilancio(GruppoEPBulk selezione_tipo_bilancio) {
        this.selezione_tipo_bilancio = selezione_tipo_bilancio;
    }

    public boolean isNuovoRamoHidden() {
        return Optional.ofNullable(getParentElement()).isPresent();
    }

    public boolean isContiHidden() {
        return !Optional.ofNullable(getFocusedElement())
                .filter(GruppoEPBulk.class::isInstance)
                .map(GruppoEPBulk.class::cast)
                .filter(gruppoEPBulk -> !Optional.ofNullable(gruppoEPBulk.getFormula()).isPresent())
                .filter(GruppoEPBulk::getFlMastrino)
                .isPresent();
    }

    public boolean isNuovoRamoFiglioHidden() {
        return !Optional.ofNullable(getFocusedElement())
                .filter(GruppoEPBulk.class::isInstance)
                .map(GruppoEPBulk.class::cast)
                .filter(gruppoEPBulk -> !Optional.ofNullable(gruppoEPBulk.getFormula()).isPresent())
                .filter(gruppoEPBulk -> !gruppoEPBulk.getFlMastrino())
                .isPresent();
    }

    public boolean isCancellaRamoHidden() {
        return !Optional.ofNullable(getFocusedElement())
                .filter(GruppoEPBulk.class::isInstance)
                .map(GruppoEPBulk.class::cast)
                .filter(gruppoEPBulk -> isLeafElement())
                .isPresent();
    }
    public boolean isModificaRamoHidden() {
        return !Optional.ofNullable(getFocusedElement()).isPresent();
    }
}

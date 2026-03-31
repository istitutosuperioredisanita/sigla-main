package it.cnr.contab.doccont00.bp;

import it.cnr.contab.config00.latt.bulk.CostantiTi_gestione;
import it.cnr.contab.doccont00.core.bulk.Mandato_rigaBulk;
import it.cnr.contab.doccont00.core.bulk.Reversale_rigaBulk;
import it.cnr.contab.doccont00.ejb.CambiaVocePGiroComponentSession;
import it.cnr.contab.doccont00.tabrif.bulk.CambiaVocePGiroBulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Config;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.ejb.RicercaComponentSession;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.action.BulkBP;
import it.cnr.jada.util.action.SearchProvider;
import it.cnr.jada.util.action.SimpleDetailCRUDController;
import it.cnr.jada.util.jsp.Button;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

public class CambiaVocePGiroBP extends BulkBP implements SearchProvider {
    private String componentSessionName;

    private final SimpleDetailCRUDController dettagliSpesaCRUDController =
            new SimpleDetailCRUDController("dettaglioSpesaCRUDController", Mandato_rigaBulk.class,"dettagliSpesa",this);
    private final SimpleDetailCRUDController dettagliEntrataCRUDController =
            new SimpleDetailCRUDController("dettaglioEntrataCRUDController", Reversale_rigaBulk.class,"dettagliEntrata",this);

    public String getComponentSessionName() {
        return componentSessionName;
    }

    public void setComponentSessionName(String componentSessionName) {
        this.componentSessionName = componentSessionName;
    }

    public SimpleDetailCRUDController getDettagliSpesaCRUDController() {
        return dettagliSpesaCRUDController;
    }

    public SimpleDetailCRUDController getDettagliEntrataCRUDController() {
        return dettagliEntrataCRUDController;
    }

    public SimpleDetailCRUDController getDettagliCRUDController() {
        return Optional.ofNullable(getModel())
                .filter(CambiaVocePGiroBulk.class::isInstance)
                .map(CambiaVocePGiroBulk.class::cast)
                .map(CambiaVocePGiroBulk::getTiGestione)
                .map(s -> {
                   if (s.equalsIgnoreCase("E"))
                       return dettagliEntrataCRUDController;
                   return dettagliSpesaCRUDController;
                }).orElse(dettagliSpesaCRUDController);
    }

    @Override
    protected Button[] createToolbar() {
        final Properties properties = it.cnr.jada.util.Config.getHandler().getProperties(getClass());
        return Stream.concat(Arrays.asList(Optional.ofNullable(super.createToolbar()).orElse(new Button[0])).stream(),
                Arrays.asList(
                        new Button(properties, "Toolbar.elabora")
                ).stream()).toArray(Button[]::new);
    }

    public boolean isElaboraButtonEnabled() throws BusinessProcessException {
        final CambiaVocePGiroBulk cambiaVocePGiroBulk = Optional.ofNullable(getModel())
                .filter(CambiaVocePGiroBulk.class::isInstance)
                .map(CambiaVocePGiroBulk.class::cast)
                .orElseThrow(() -> new BusinessProcessException("Model not found!"));
        return Optional.ofNullable(cambiaVocePGiroBulk.getVoceIniziale())
                .filter(vVoceFPartitaGiroBulk -> Optional.ofNullable(vVoceFPartitaGiroBulk.getCd_elemento_voce()).isPresent())
                .isPresent() &&
                Optional.ofNullable(cambiaVocePGiroBulk.getVoceFinale())
                .filter(vVoceFPartitaGiroBulk -> Optional.ofNullable(vVoceFPartitaGiroBulk.getCd_elemento_voce()).isPresent())
                .isPresent();
    }

    @Override
    protected void init(Config config, ActionContext actioncontext) throws BusinessProcessException {
        super.init(config, actioncontext);
        setComponentSessionName(config.getInitParameter("componentSessionName"));
        final CambiaVocePGiroBulk cambiaVocePGiroBulk = new CambiaVocePGiroBulk();
        cambiaVocePGiroBulk.setTiGestione(CostantiTi_gestione.TI_GESTIONE_ENTRATE);
        setModel(actioncontext, cambiaVocePGiroBulk);
    }

    public CambiaVocePGiroComponentSession createComponentSession() throws BusinessProcessException {
        return (CambiaVocePGiroComponentSession)createComponentSession(componentSessionName, CambiaVocePGiroComponentSession.class);
    }

    @Override
    public RemoteIterator find(ActionContext actioncontext, CompoundFindClause compoundfindclause, OggettoBulk oggettobulk, OggettoBulk oggettobulk1, String property) throws BusinessProcessException {
        try {
            return it.cnr.jada.util.ejb.EJBCommonServices.openRemoteIterator(actioncontext,createComponentSession().cerca(actioncontext.getUserContext(),compoundfindclause,oggettobulk,oggettobulk1,property));
        } catch(Exception e) {
            throw new it.cnr.jada.action.BusinessProcessException(e);
        }
    }

    @Override
    public RemoteIterator search(ActionContext actionContext, CompoundFindClause compoundfindclause, OggettoBulk oggettobulk) throws BusinessProcessException {
        final CambiaVocePGiroBulk cambiaVocePGiroBulk = Optional.ofNullable(getModel())
                .filter(CambiaVocePGiroBulk.class::isInstance)
                .map(CambiaVocePGiroBulk.class::cast)
                .orElseThrow(() -> new BusinessProcessException("Model not found!"));
        try {
        if (cambiaVocePGiroBulk.getTiGestione().equalsIgnoreCase(CostantiTi_gestione.TI_GESTIONE_ENTRATE)) {
                return createComponentSession().cercaReversali(
                    actionContext.getUserContext(),
                    cambiaVocePGiroBulk,
                    cambiaVocePGiroBulk.getVoceIniziale(),
                    compoundfindclause
                );
            } else {
                return createComponentSession().cercaMandati(
                    actionContext.getUserContext(),
                    cambiaVocePGiroBulk,
                    cambiaVocePGiroBulk.getVoceIniziale(),
                    compoundfindclause
                );
            }
        } catch (ComponentException|RemoteException e) {
            throw handleException(e);
        }
    }
}

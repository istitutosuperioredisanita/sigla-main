package it.cnr.contab.doccont00.bp;

import it.cnr.contab.doccont00.consultazioni.action.ConsControlliPCCAction;
import it.cnr.contab.service.SpringUtil;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcess;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.util.Config;
import it.cnr.jada.util.action.ConsultazioniBP;
import it.cnr.jada.util.action.RicercaLiberaBP;
import it.cnr.jada.util.jsp.Button;
import it.cnr.si.spring.storage.StoreService;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Properties;
import java.util.Vector;
import java.util.stream.Stream;

public class CaricFlStipBP{



//    @Override
//    protected void init(it.cnr.jada.action.Config config, ActionContext context)
//            throws BusinessProcessException {
//        try {
//            setBulkInfo(it.cnr.jada.bulk.BulkInfo.getBulkInfo(Class.forName(config.getInitParameter("bulkClassName"))));
//            OggettoBulk model = (OggettoBulk) getBulkInfo().getBulkClass().newInstance();
//            setModel(context, model);
//        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
//            throw handleException(e);
//        }
//        storeService = SpringUtil.getBean("storeService", StoreService.class);
//        esercizio = CNRUserContext.getEsercizio(context.getUserContext());
//    }
//
//    public it.cnr.jada.ejb.CRUDComponentSession createComponentSession() throws javax.ejb.EJBException, RemoteException, BusinessProcessException {
//        return (it.cnr.jada.ejb.CRUDComponentSession) createComponentSession("JADAEJB_CRUDComponentSession", it.cnr.jada.ejb.CRUDComponentSession.class);
//    }
//
//    @Override
//    public Button[] createToolbar() {
//        final Properties properties = it.cnr.jada.util.Config.getHandler().getProperties(getClass());
//        return Stream.concat(Arrays.stream(super.createToolbar()),
//                Stream.of(
//                        new Button(properties, "CRUDToolbar.allegaticsv"),
//                )).toArray(Button[]::new);
//    }
}
package it.cnr.contab.pdg00.bp;


//ConsControlliPCCBP
public class CaricFlStipBP {



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
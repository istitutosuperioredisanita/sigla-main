package it.cnr.contab.pdg00.bp;

import it.cnr.contab.config00.ejb.EsercizioComponentSession;
import it.cnr.contab.pdg00.bulk.AccrualBulk;
import it.cnr.contab.pdg00.bulk.AllegatoAccrualBulk;
import it.cnr.contab.util00.bp.AllegatiCRUDBP;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Config;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.util.ejb.EJBCommonServices;
import it.cnr.si.spring.storage.StorageObject;
import it.cnr.si.spring.storage.config.StoragePropertyNames;

import java.util.List;
import java.util.Optional;

public class CRUDAccrualMefBP extends AllegatiCRUDBP<AllegatoAccrualBulk, AccrualBulk> {


    private boolean esercizioChiuso = false;

    public CRUDAccrualMefBP() {
        super("Tn");
    }

    public CRUDAccrualMefBP(String function) {
        super(function + "Tn");

    }

    @Override
    protected void init(Config config, ActionContext context)
            throws BusinessProcessException {

        try {
            esercizioChiuso = (((EsercizioComponentSession) EJBCommonServices
                    .createEJB(
                            "CNRCONFIG00_EJB_EsercizioComponentSession",
                            EsercizioComponentSession.class
                    ))
                    .isEsercizioChiuso(context.getUserContext()));

        } catch (Throwable e) {
            throw new BusinessProcessException(e);
        }
        super.init(config, context);

    }

    public String[][] getTabs() {
        return new String[][]{
                {
                        "tabModulo",
                        "Modulo",
                        "/pdg00/tab_accrual_mef_modulo.jsp"
                },
                {
                        "tabAllegati",
                        "Allegati",
                        "/util00/tab_allegati.jsp"
                }
        };
    }

    @Override
    protected void resetTabs(ActionContext actioncontext) {
        setTab("tab", "tabModulo");
    }
    /**
     * Stessa logica del BP Trasporto:
     * il path viene costruito dal Bulk e il BP restituisce solo il primo elemento.
     */
    @Override
    protected String getStorePath(AccrualBulk accrual, boolean create)
            throws BusinessProcessException {

        if (accrual == null) {
            throw new BusinessProcessException("Modulo Accrual MEF non presente");
        }

        return accrual.getStorePath().get(0);
    }

    @Override
    protected Class getAllegatoClass() {
        return AllegatoAccrualBulk.class;
    }

    @Override
    protected void completeAllegato(AllegatoAccrualBulk allegato, StorageObject storageObject) throws ApplicationException {
        Optional.ofNullable(storageObject.<List<String>>getPropertyValue(StoragePropertyNames.SECONDARY_OBJECT_TYPE_IDS.value()))
                .map(strings -> strings.stream())
                .ifPresent(stringStream -> {
                    stringStream
                            .filter(s -> AllegatoAccrualBulk.aspectNamesKeys.get(s) != null)
                            .findFirst()
                            .ifPresent(s -> allegato.setAspectName(s));
                });
        super.completeAllegato(allegato, storageObject);
    }

    @Override
    public boolean isNewButtonEnabled() {
        return !isEsercizioChiuso() && super.isNewButtonEnabled();
    }

    @Override
    public boolean isSaveButtonEnabled() {
        return !isEsercizioChiuso() && super.isSaveButtonEnabled();
    }

    @Override
    public boolean isDeleteButtonEnabled() {
        return !isEsercizioChiuso() && super.isDeleteButtonEnabled();
    }
    public boolean isEsercizioChiuso() {
        return esercizioChiuso;
    }


}
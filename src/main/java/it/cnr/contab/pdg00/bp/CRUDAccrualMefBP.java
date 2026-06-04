package it.cnr.contab.pdg00.bp;

import it.cnr.contab.config00.ejb.EsercizioComponentSession;
import it.cnr.contab.pdg00.bulk.AccrualBulk;
import it.cnr.contab.pdg00.bulk.AllegatoAccrualBulk;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.util00.bp.AllegatiCRUDBP;
import it.cnr.contab.util00.bulk.storage.AllegatoGenericoBulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Config;
import it.cnr.jada.action.HttpActionContext;
import it.cnr.jada.bulk.BulkList;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.bulk.ValidationException;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.util.ejb.EJBCommonServices;
import it.cnr.jada.util.upload.UploadedFile;
import it.cnr.si.spring.storage.StorageObject;

import java.util.List;
import java.util.Optional;

public class CRUDAccrualMefBP extends AllegatiCRUDBP<AllegatoAccrualBulk, AccrualBulk> {

    private boolean visualizzazione = false;
    private boolean esercizioChiuso = false;
    private boolean skipAllegatiReload = false;

    public CRUDAccrualMefBP() {
        super("Tn");
    }

    public CRUDAccrualMefBP(String function) {
        super(function + "Tn");
        setVisualizzazione("V".equals(function));
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

        resetTabs();

        if (isVisualizzazione() || isEsercizioChiuso()) {
            setStatus(VIEW);
        }

        if (isEsercizioChiuso()) {
            setErrorMessage(
                    "Esercizio chiuso: non è possibile effettuare operazioni sulla Contabilità Accrual - MEF."
            );
        }
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

    public void resetTabs() {
        setTab("tab", "tabModulo");
    }

    @Override
    public void resetForSearch(ActionContext context) throws BusinessProcessException {
        super.resetForSearch(context);
        resetTabs();
    }

    @Override
    public void setTab(String tabName, String tabValue) {
        if ("tab".equals(tabName) && "tabAllegati".equals(tabValue)) {
            AccrualBulk accrual = getAccrual();

            if (accrual != null && accrual.getCrudStatus() == OggettoBulk.UNDEFINED) {
                setErrorMessage("Non è possibile accedere agli allegati in fase di ricerca.");
                return;
            }
        }

        super.setTab(tabName, tabValue);
    }

    @Override
    public OggettoBulk initializeModelForInsert(ActionContext context, OggettoBulk bulk)
            throws BusinessProcessException {

        if (isEsercizioChiuso()) {
            throw new BusinessProcessException(
                    "Esercizio chiuso: non è possibile inserire un nuovo modulo Accrual MEF."
            );
        }

        AccrualBulk accrual = (AccrualBulk) super.initializeModelForInsert(context, bulk);

        Number esercizio = CNRUserContext.getEsercizio(context.getUserContext());

        if (esercizio != null) {
            accrual.setEsercizio(Long.valueOf(esercizio.longValue()));
        }

        if (accrual.getStato() == null || accrual.getStato().trim().isEmpty()) {
            accrual.setStato(AccrualBulk.STATO_INSERITO);
        }

        if (accrual.getArchivioAllegati() == null) {
            accrual.setArchivioAllegati(new BulkList<>());
        }

        return accrual;
    }

    @Override
    public OggettoBulk initializeModelForEdit(ActionContext context, OggettoBulk bulk)
            throws BusinessProcessException {

        bulk = super.initializeModelForEdit(context, bulk);

        AccrualBulk accrual = (AccrualBulk) bulk;

        if (accrual.getArchivioAllegati() == null) {
            accrual.setArchivioAllegati(new BulkList<>());
        }

        if (isEsercizioChiuso()) {
            setStatus(VIEW);
            setErrorMessage(
                    "Esercizio chiuso: il modulo Accrual MEF è disponibile solo in visualizzazione."
            );
        }

        return accrual;
    }

    @Override
    public OggettoBulk initializeModelForEditAllegati(
            ActionContext context,
            OggettoBulk bulk)
            throws BusinessProcessException {

        if (isSkipAllegatiReload()) {
            return bulk;
        }

        return super.initializeModelForEditAllegati(context, bulk);
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
    public void create(ActionContext context) throws BusinessProcessException {
        try {
            AccrualBulk accrual = getAccrual();

            if (accrual == null) {
                throw new BusinessProcessException("Modello Accrual MEF non disponibile.");
            }

            if (isEsercizioChiuso()) {
                throw new BusinessProcessException(
                        "Esercizio chiuso: non è possibile salvare il modulo Accrual MEF."
                );
            }

            preparaAllegati(context, accrual);

            setSkipAllegatiReload(true);

            try {
                super.create(context);
            } finally {
                setSkipAllegatiReload(false);
            }

        } catch (ValidationException e) {
            rollbackUserTransaction();
            throw handleException(e);

        } catch (Exception e) {
            rollbackUserTransaction();
            throw handleException(e);
        }
    }

    @Override
    public void update(ActionContext context) throws BusinessProcessException {
        try {
            AccrualBulk accrual = getAccrual();

            if (accrual == null) {
                throw new BusinessProcessException("Modello Accrual MEF non disponibile.");
            }

            if (isEsercizioChiuso()) {
                throw new BusinessProcessException(
                        "Esercizio chiuso: non è possibile modificare il modulo Accrual MEF."
                );
            }

            preparaAllegati(context, accrual);

            setSkipAllegatiReload(true);

            try {
                super.update(context);
            } finally {
                setSkipAllegatiReload(false);
            }

        } catch (ValidationException e) {
            rollbackUserTransaction();
            throw handleException(e);

        } catch (Exception e) {
            rollbackUserTransaction();
            throw handleException(e);
        }
    }

    private void preparaAllegati(ActionContext context, AccrualBulk accrual)
            throws ValidationException {

        if (accrual.getArchivioAllegati() == null) {
            accrual.setArchivioAllegati(new BulkList<>());
        }

        assegnaFileUploadato(context, accrual);
        validaAllegati(accrual);
    }

    private void assegnaFileUploadato(ActionContext context, AccrualBulk accrual) {
        if (!(context instanceof HttpActionContext)) {
            return;
        }

        UploadedFile uploadedFile =
                ((HttpActionContext) context)
                        .getMultipartParameter("main.ArchivioAllegati.file");

        if (uploadedFile == null
                || uploadedFile.getName() == null
                || uploadedFile.getName().isEmpty()) {
            return;
        }

        accrual.getArchivioAllegati().stream()
                .filter(a -> a.isToBeCreated() && a.getFile() == null)
                .findFirst()
                .ifPresent(a -> {
                    a.setFile(uploadedFile.getFile());
                    a.setContentType(uploadedFile.getContentType());
                    a.setNome(a.parseFilename(uploadedFile.getName()));
                });
    }

    private void validaAllegati(AccrualBulk accrual) throws ValidationException {
        if (accrual.getArchivioAllegati() == null) {
            return;
        }

        for (AllegatoGenericoBulk allegato : accrual.getArchivioAllegati()) {
            allegato.validate();
        }
    }

    @Override
    protected void completeAllegato(AllegatoAccrualBulk allegato, StorageObject storageObject)
            throws ApplicationException {

        Optional.ofNullable(storageObject.<String>getPropertyValue(
                        "sigla_commons_aspect:utente_applicativo"))
                .ifPresent(allegato::setUtenteSIGLA);

        List<String> secondaryTypes =
                storageObject.getPropertyValue("cmis:secondaryObjectTypeIds");

        if (secondaryTypes != null && !secondaryTypes.isEmpty()) {
            for (String secondaryType : secondaryTypes) {
                if (secondaryType == null) {
                    continue;
                }

                if ("P:cm:titled".equals(secondaryType)
                        || secondaryType.contains("sigla_commons_aspect")) {
                    continue;
                }

                if (AllegatoAccrualBulk.P_SIGLA_ACCRUAL_ATTACHMENT_XBRL_ZIP.equals(secondaryType)
                        || AllegatoAccrualBulk.P_SIGLA_ACCRUAL_ATTACHMENT_ALTRO.equals(secondaryType)) {

                    allegato.setAspectName(secondaryType);
                    break;
                }
            }
        }

        super.completeAllegato(allegato, storageObject);
    }

    @Override
    protected Boolean isPossibileCancellazione(AllegatoGenericoBulk allegato) {
        return !isEsercizioChiuso() && !isVisualizzazione();
    }

    @Override
    public boolean isEditable() {
        if (isEsercizioChiuso()) {
            return false;
        }

        return !isVisualizzazione() && super.isEditable();
    }

    @Override
    public boolean isNewButtonEnabled() {
        return !isEsercizioChiuso()
                && !isVisualizzazione()
                && super.isNewButtonEnabled();
    }

    @Override
    public boolean isSaveButtonEnabled() {
        return !isEsercizioChiuso()
                && !isVisualizzazione()
                && super.isSaveButtonEnabled();
    }

    @Override
    public boolean isDeleteButtonEnabled() {
        return !isEsercizioChiuso()
                && !isVisualizzazione()
                && super.isDeleteButtonEnabled();
    }

    public boolean isInputReadonly() {
        return isEsercizioChiuso() || isVisualizzazione();
    }

    public boolean isVisualizzazione() {
        return visualizzazione;
    }

    public void setVisualizzazione(boolean visualizzazione) {
        this.visualizzazione = visualizzazione;
    }

    public boolean isEsercizioChiuso() {
        return esercizioChiuso;
    }

    public void setEsercizioChiuso(boolean esercizioChiuso) {
        this.esercizioChiuso = esercizioChiuso;
    }

    public boolean isSkipAllegatiReload() {
        return skipAllegatiReload;
    }

    public void setSkipAllegatiReload(boolean skipAllegatiReload) {
        this.skipAllegatiReload = skipAllegatiReload;
    }

    public AccrualBulk getAccrual() {
        return (AccrualBulk) getModel();
    }
}
package it.cnr.contab.inventario01.actions;

import it.cnr.contab.inventario00.docs.bulk.Inventario_beniBulk;
import it.cnr.contab.inventario00.tabrif.bulk.Tipo_trasporto_rientroBulk;
import it.cnr.contab.inventario01.bp.CRUDTrasportoBeniInvBP;
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.inventario01.ejb.DocTrasportoRientroComponentSession;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.Forward;
import it.cnr.jada.action.HookForward;
import it.cnr.jada.bulk.SimpleBulkList;
import it.cnr.jada.bulk.ValidationException;
import it.cnr.jada.util.action.RicercaLiberaBP;
import it.cnr.jada.util.action.SelezionatoreListaBP;
import it.cnr.jada.util.ejb.EJBCommonServices;

import java.rmi.RemoteException;

/**
 * Action - SOLO COORDINAMENTO FLUSSO
 */
public class CRUDTrasportoDocAction extends it.cnr.jada.util.action.CRUDAction {

    // =======================================================
    // GESTIONE CAMBI TESTATA
    // =======================================================

    public CRUDTrasportoDocAction() {
        super();
    }

    public Forward doSelezionaTipoMovimento(ActionContext context) {
        try {
            CRUDTrasportoBeniInvBP bp = (CRUDTrasportoBeniInvBP) getBusinessProcess(context);
            Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bp.getModel();
            Tipo_trasporto_rientroBulk tipoOld = doc.getTipoMovimento();

            fillModel(context);

            if (hasBeniInDettaglio(doc)) {
                doc.setTipoMovimento(tipoOld);
                bp.setMessage("Impossibile cambiare Tipo Movimento: eliminare prima i beni.");
            }

            bp.setModel(context, doc);
            return context.findDefaultForward();
        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    public Forward doOnTipoRitiroChange(ActionContext context) {
        try {
            CRUDTrasportoBeniInvBP bp = (CRUDTrasportoBeniInvBP) getBusinessProcess(context);

            bp.getDettBeniController().removeAll(context);
            bp.getEditDettController().removeAll(context);
            Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bp.getModel();

            String oldValue = doc.getTipoRitiro();
            fillModel(context);
            String newValue = doc.getTipoRitiro();

            if (valoreUguale(oldValue, newValue)) {
                return context.findDefaultForward();
            }

            eliminaBeniSePresenti(context, bp, doc, "Tipo ritiro modificato. Beni precedenti rimossi.");

            if (Doc_trasporto_rientroBulk.TIPO_RITIRO_VETTORE.equals(newValue)) {
                doc.setAnagDipRitiro(null);
            }

            bp.setModel(context, doc);
            return context.findDefaultForward();
        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    public Forward doOnDipendenteChange(ActionContext context) {
        try {
            CRUDTrasportoBeniInvBP bp = (CRUDTrasportoBeniInvBP) getBusinessProcess(context);
            bp.getDettBeniController().removeAll(context);
            bp.getEditDettController().removeAll(context);

            Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bp.getModel();

            Integer oldValue = getCdAnag(doc.getAnagDipRitiro());
            fillModel(context);
            Integer newValue = getCdAnag(doc.getAnagDipRitiro());

            if (valoreUguale(oldValue, newValue)) {
                return context.findDefaultForward();
            }

            eliminaBeniSePresenti(context, bp, doc, "Dipendente modificato. Beni precedenti rimossi.");

            bp.setModel(context, doc);
            return context.findDefaultForward();
        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    public Forward doOnData_registrazioneChange(ActionContext context) {
        try {
            CRUDTrasportoBeniInvBP bp = (CRUDTrasportoBeniInvBP) getBusinessProcess(context);
            bp.getDettBeniController().removeAll(context);
            bp.getEditDettController().removeAll(context);

            fillModel(context);
            bp.setModel(context, (Doc_trasporto_rientroBulk) bp.getModel());

            return context.findDefaultForward();
        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    // =======================================================
    // AGGIUNTA BENI
    // =======================================================

    public Forward doAddToCRUDMain_DettBeniController(ActionContext context) {
        try {
            fillModel(context);

            CRUDTrasportoBeniInvBP bp = (CRUDTrasportoBeniInvBP) getBusinessProcess(context);
            Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bp.getModel();

            validaTestataDocumento(doc);
            bp.getDettBeniController().validate(context);
            SimpleBulkList selezionati = ((it.cnr.contab.inventario01.ejb.DocTrasportoRientroComponentSession) bp.createComponentSession()).
                    selezionati(context.getUserContext(), (Doc_trasporto_rientroBulk) bp.getModel());
            it.cnr.jada.util.RemoteIterator ri = getComp(bp).cercaBeniTrasportabili(
                    context.getUserContext(), doc, selezionati, null);
            ri = EJBCommonServices.openRemoteIterator(context, ri);

            if (ri.countElements() == 0) {
                bp.setMessage("Nessun Bene recuperato");
                EJBCommonServices.closeRemoteIterator(context, ri);
            } else {
                RicercaLiberaBP rlbp = (RicercaLiberaBP) context.createBusinessProcess("RicercaLibera");
                rlbp.setCanPerformSearchWithoutClauses(false);
                rlbp.setPrototype(new Inventario_beniBulk());
                context.addHookForward("searchResult", this, "doBringBackAddBeniTrasporto");
                context.addHookForward("filter", this, "doBringBackAddBeniTrasporto");
                return context.addBusinessProcess(rlbp);
            }

            return context.findDefaultForward();

        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    public Forward doBringBackAddBeniTrasporto(ActionContext context) {
        try {
            HookForward fwd = (HookForward) context.getCaller();
            CRUDTrasportoBeniInvBP bp = (CRUDTrasportoBeniInvBP) getBusinessProcess(context);
            Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bp.getModel();

            it.cnr.jada.persistency.sql.CompoundFindClause clauses =
                    (it.cnr.jada.persistency.sql.CompoundFindClause) fwd.getParameter("filter");
            it.cnr.jada.bulk.SimpleBulkList selezionati = null;

            bp.setClauses(clauses);
            context.addHookForward("filter", this, "doSelezionaBeniTrasporto");

            selezionati = getComp(bp).selezionati(context.getUserContext(),
                    (Doc_trasporto_rientroBulk) bp.getModel());

            it.cnr.jada.util.RemoteIterator ri = getComp(bp).cercaBeniTrasportabili(
                    context.getUserContext(), doc, selezionati, clauses);
            ri = EJBCommonServices.openRemoteIterator(context, ri);

            if (ri.countElements() == 0) {
                bp.setMessage("Nessun Bene associabile");
                EJBCommonServices.closeRemoteIterator(context, ri);
            } else {
//                // INIZIALIZZA LA SELEZIONE PRIMA DI CREARE IL SELEZIONATORE
//                bp.initializeSelection(context);

                SelezionatoreListaBP slbp = select(context, ri,
                        it.cnr.jada.bulk.BulkInfo.getBulkInfo(Inventario_beniBulk.class),
                        null, "doSelezionaBeniTrasporto", null, bp);
                slbp.setMultiSelection(true);
                return slbp;
            }

            return context.findDefaultForward();
        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    public Forward doSelezionaBeniTrasporto(ActionContext context) {
        try {
            CRUDTrasportoBeniInvBP bp = (CRUDTrasportoBeniInvBP) getBusinessProcess(context);
            bp.getDettBeniController().reset(context);

            // Pulisce il savepoint dopo la selezione
            bp.clearSelection(context);

            return context.findDefaultForward();
        } catch (Exception e) {
            return handleException(context, e);
        }
    }

    public Forward doSelectDettBeniController(ActionContext context) {
        try {
            CRUDTrasportoBeniInvBP bp = (CRUDTrasportoBeniInvBP) getBusinessProcess(context);
            bp.getDettBeniController().setSelection(context);
            return context.findDefaultForward();
        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    // =======================================================
    // WORKFLOW
    // =======================================================


    public Forward doPredisponiAllaFirma(ActionContext context) {
        try {
            CRUDTrasportoBeniInvBP bp = (CRUDTrasportoBeniInvBP) getBusinessProcess(context);
            bp.predisponiAllaFirma(context);
            return context.findDefaultForward();
        } catch (Exception e) {
            return handleException(context, e);
        }
    }

    // =======================================================
    // ELIMINAZIONE
    // =======================================================

    @Override
    public Forward doElimina(ActionContext context) throws RemoteException {
        try {
            fillModel(context);

            CRUDTrasportoBeniInvBP bp = (CRUDTrasportoBeniInvBP) getBusinessProcess(context);
            Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) bp.getModel();

            if (!bp.isEditing()) {
                bp.setMessage("Non è possibile annullare in questo momento.");
                return context.findDefaultForward();
            }

            doc.setStato(Doc_trasporto_rientroBulk.STATO_ANNULLATO);
            getComp(bp).modificaConBulk(context.getUserContext(), doc);

            bp.setMessage("Documento annullato correttamente.");
            bp.reset(context);
            return context.findDefaultForward();

        } catch (Throwable e) {
            return handleException(context, e);
        }
    }

    // =======================================================
    // HELPER
    // =======================================================

    private DocTrasportoRientroComponentSession getComp(CRUDTrasportoBeniInvBP bp) throws Exception {
        return (DocTrasportoRientroComponentSession) bp.createComponentSession(
                "CNRINVENTARIO01_EJB_DocTrasportoRientroComponentSession",
                DocTrasportoRientroComponentSession.class);
    }

    private <T> boolean valoreUguale(T oldValue, T newValue) {
        if (oldValue == null && newValue == null) return true;
        if (oldValue == null || newValue == null) return false;
        return oldValue.equals(newValue);
    }

    private Integer getCdAnag(it.cnr.contab.anagraf00.core.bulk.TerzoBulk terzo) {
        if (terzo == null) return null;
        it.cnr.contab.anagraf00.core.bulk.AnagraficoBulk anag = terzo.getAnagrafico();
        return (anag != null) ? anag.getCd_anag() : null;
    }

    private boolean hasBeniInDettaglio(Doc_trasporto_rientroBulk doc) {
        return doc.getDoc_trasporto_rientro_dettColl() != null &&
                !doc.getDoc_trasporto_rientro_dettColl().isEmpty();
    }

    private void eliminaBeniSePresenti(ActionContext context, CRUDTrasportoBeniInvBP bp,
                                       Doc_trasporto_rientroBulk doc, String messaggio) throws Exception {
        if (hasBeniInDettaglio(doc)) {
            getComp(bp).eliminaTuttiBeniDettaglio(context.getUserContext(), doc);
            doc.getDoc_trasporto_rientro_dettColl().clear();
            bp.setDirty(true);
            bp.setMessage(messaggio);
        }
    }

    private void validaTestataDocumento(Doc_trasporto_rientroBulk doc) throws ValidationException {
        if (doc.getTipoMovimento() == null) {
            throw new ValidationException("Attenzione: specificare un tipo di movimento nella testata");
        }

        if (doc.getDataRegistrazione() == null) {
            throw new ValidationException("Attenzione: specificare la data trasporto");
        }

        if (doc.getDsDocTrasportoRientro() == null || doc.getDsDocTrasportoRientro().trim().isEmpty()) {
            throw new ValidationException("Attenzione: indicare una Descrizione per il Documento di Trasporto");
        }
    }
}
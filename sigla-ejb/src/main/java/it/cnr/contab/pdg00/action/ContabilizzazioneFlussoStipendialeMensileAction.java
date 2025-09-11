/*
 * Copyright (C) 2019  Consiglio Nazionale delle Ricerche
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package it.cnr.contab.pdg00.action;

import it.cnr.contab.compensi00.bp.CRUDCompensoBP;
import it.cnr.contab.compensi00.docs.bulk.CompensoBulk;
import it.cnr.contab.docamm00.bp.CRUDDocumentoGenericoPassivoBP;
import it.cnr.contab.docamm00.docs.bulk.Documento_generico_passivoBulk;
import it.cnr.contab.doccont00.bp.CRUDMandatoBP;
import it.cnr.contab.doccont00.core.bulk.MandatoIBulk;
import it.cnr.contab.pdg00.bp.ContabilizzazioneFlussoStipendialeMensileBP;
import it.cnr.contab.pdg00.cdip.bulk.MeseTipoFlussoBulk;
import it.cnr.contab.pdg00.cdip.bulk.Stipendi_cofiBulk;
import it.cnr.contab.util00.bp.ModalBP;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Forward;
import it.cnr.jada.action.HookForward;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;
import it.cnr.jada.persistency.sql.SimpleFindClause;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.action.FormBP;
import it.cnr.jada.util.action.SelezionatoreListaBP;
import it.cnr.jada.util.ejb.EJBCommonServices;

import javax.validation.ValidationException;
import java.rmi.RemoteException;
import java.util.Optional;

public class ContabilizzazioneFlussoStipendialeMensileAction extends it.cnr.jada.util.action.SelezionatoreListaAction {
    public ContabilizzazioneFlussoStipendialeMensileAction() {
        super();
    }

    public Forward doApriCompenso(ActionContext context) throws BusinessProcessException {
        ContabilizzazioneFlussoStipendialeMensileBP bp = (ContabilizzazioneFlussoStipendialeMensileBP) context.getBusinessProcess();

        Stipendi_cofiBulk stipendi_cofi = (Stipendi_cofiBulk) bp.getFocusedElement();
        if (stipendi_cofi == null) {
            bp.setMessage("E' necessario selezionare un flusso stipendiale.");
            return context.findDefaultForward();
        }

        if (stipendi_cofi.getCd_cds_comp() == null ||
                stipendi_cofi.getCd_uo_comp() == null ||
                stipendi_cofi.getEsercizio_comp() == null ||
                stipendi_cofi.getPg_comp() == null) {
            bp.setMessage("E' necessario selezionare un flusso stipendiale con compenso associato.");
            return context.findDefaultForward();
        }

        CompensoBulk compenso = new CompensoBulk(
                stipendi_cofi.getCd_cds_comp(),
                stipendi_cofi.getCd_uo_comp(),
                stipendi_cofi.getEsercizio_comp(),
                stipendi_cofi.getPg_comp());

        CRUDCompensoBP compBP = CRUDCompensoBP.getBusinessProcessFor(context, compenso, "VRSWTh");

        compBP.edit(context, compenso);

        return context.addBusinessProcess(compBP);
    }

    public Forward doApriDocumentoGenerico(ActionContext context) throws BusinessProcessException {
        ContabilizzazioneFlussoStipendialeMensileBP bp = (ContabilizzazioneFlussoStipendialeMensileBP) context.getBusinessProcess();

        Stipendi_cofiBulk stipendi_cofi = (Stipendi_cofiBulk) bp.getFocusedElement();
        if (stipendi_cofi == null) {
            bp.setMessage("E' necessario selezionare un flusso stipendiale.");
            return context.findDefaultForward();
        }

        if (stipendi_cofi.getCd_cds_doc_gen() == null ||
                stipendi_cofi.getCd_uo_doc_gen() == null ||
                stipendi_cofi.getEsercizio_doc_gen() == null ||
                stipendi_cofi.getCd_tipo_doc_gen() == null ||
                stipendi_cofi.getPg_doc_gen() == null) {
            bp.setMessage("E' necessario selezionare un flusso stipendiale con documento generico associato.");
            return context.findDefaultForward();
        }

        CRUDDocumentoGenericoPassivoBP newbp = (CRUDDocumentoGenericoPassivoBP) context.createBusinessProcess("CRUDGenericoPassivoBP", new Object[]{"VRSWTh"});

        newbp.edit(context, new Documento_generico_passivoBulk(
                stipendi_cofi.getCd_cds_doc_gen(),
                stipendi_cofi.getCd_tipo_doc_gen(),
                stipendi_cofi.getCd_uo_doc_gen(),
                stipendi_cofi.getEsercizio_doc_gen(),
                stipendi_cofi.getPg_doc_gen()));

        return context.addBusinessProcess(newbp);
    }

    public Forward doRiportaSelezioneMandato(ActionContext actioncontext)
            throws RemoteException {
        try {
            HookForward caller = (HookForward)actioncontext.getCaller();
            Optional<MandatoIBulk> mandatoIBulk =
                    Optional.ofNullable( caller.getParameter("focusedElement")).
                            filter( MandatoIBulk.class::isInstance).
                            map(MandatoIBulk.class::cast);
            if (mandatoIBulk.isPresent()) {
                CRUDMandatoBP crudMandatoBP = (CRUDMandatoBP) actioncontext.createBusinessProcess("CRUDMandatoBP", new Object[]{"VRSWTh"});
                crudMandatoBP.edit(actioncontext, mandatoIBulk.get());
                return actioncontext.addBusinessProcess(crudMandatoBP);
            }
            return actioncontext.findDefaultForward();
        } catch (Exception exception) {
            return handleException(actioncontext, exception);
        }
    }


    public Forward doApriMandato(ActionContext context) throws BusinessProcessException {
        try {
            ContabilizzazioneFlussoStipendialeMensileBP bp = (ContabilizzazioneFlussoStipendialeMensileBP) context.getBusinessProcess();
            Stipendi_cofiBulk stipendi_cofi = (Stipendi_cofiBulk) bp.getFocusedElement();
            if (stipendi_cofi == null) {
                bp.setMessage("E' necessario selezionare un flusso stipendiale.");
                return context.findDefaultForward();
            }
            CRUDMandatoBP crudMandatoBP = (CRUDMandatoBP) context.createBusinessProcess("CRUDMandatoBP", new Object[]{"VRSWTh"});
            MandatoIBulk mandatoBulk = new MandatoIBulk();
            mandatoBulk.setStipendiCofiBulk(stipendi_cofi);
            SimpleFindClause findClause = new SimpleFindClause(
                    FindClause.AND,
                    "stipendiCofiBulk",
                    SQLBuilder.EQUALS,
                    stipendi_cofi
            );

            RemoteIterator remoteiterator = crudMandatoBP.find(context, new CompoundFindClause(findClause), new MandatoIBulk());
            if (remoteiterator != null && remoteiterator.countElements() != 0) {
                if (remoteiterator.countElements() == 1) {
                    OggettoBulk oggettobulk1 = (OggettoBulk)remoteiterator.nextElement();
                    EJBCommonServices.closeRemoteIterator(context, remoteiterator);
                    crudMandatoBP.edit(context, oggettobulk1);
                    return context.addBusinessProcess(crudMandatoBP);
                } else {
                    crudMandatoBP.setModel(context, mandatoBulk);
                    SelezionatoreListaBP selezionatorelistabp = (SelezionatoreListaBP)context.createBusinessProcess("Selezionatore");
                    selezionatorelistabp.setIterator(context, remoteiterator);
                    selezionatorelistabp.setBulkInfo(crudMandatoBP.getSearchBulkInfo());
                    selezionatorelistabp.setColumns(crudMandatoBP.getSearchResultColumns());
                    context.addHookForward("seleziona", this, "doRiportaSelezioneMandato");
                    return context.addBusinessProcess(selezionatorelistabp);
                }
            } else {
                EJBCommonServices.closeRemoteIterator(context, remoteiterator);
                bp.setMessage("La ricerca non ha fornito alcun risultato.");
                return context.findDefaultForward();
            }
        } catch (Throwable var6) {
            return this.handleException(context, var6);
        }
    }

    public Forward doBringBack(ActionContext context) throws BusinessProcessException {
        // Reimplementato per impedire che la selezione di un elemento provochi
        // la chiusura del bp (come da default in SelezionatoreListaAction
        return context.findDefaultForward();
    }

    /**
     * Avvia la la contabilizzazione dei flussi stipendiali mensili
     * sul mese selezionato.
     *
     * @param context L'ActionContext della richiesta
     * @return Il Forward alla pagina di risposta
     * @throws BusinessProcessException
     */
    public Forward doContabilizzaFlussoStipendialeMensile(ActionContext context) throws BusinessProcessException {
        ContabilizzazioneFlussoStipendialeMensileBP bp = (ContabilizzazioneFlussoStipendialeMensileBP) context.getBusinessProcess();
        bp.saveSelection(context);
        Stipendi_cofiBulk stipendi_cofi = (Stipendi_cofiBulk) bp.getFocusedElement();
        if (stipendi_cofi != null)
            try {
                bp.createComponentSession().contabilizzaFlussoStipendialeMensile(context.getUserContext(), stipendi_cofi.getMese().intValue());
                bp.refresh(context);
            } catch (Throwable e) {
                return handleException(context, e);
            }
        else
            setErrorMessage(context, "Per poter effettuare la contabilizzazione è necessario selezionare un flusso stipendiale non liquidato.");
        return context.findDefaultForward();
    }

	public Forward doNuovaRiga(ActionContext actioncontext) throws BusinessProcessException {
        ModalBP modalBP = (ModalBP) actioncontext.createBusinessProcess("ModalBP");
        modalBP.setCssCard("col-md-6");
        modalBP.setModel(
                actioncontext,
                new MeseTipoFlussoBulk()
        );
        actioncontext.addHookForward("model", this, "doConfirmNuovaRiga");
        return actioncontext.addBusinessProcess(modalBP);
	}

    public Forward doConfirmNuovaRiga(ActionContext context) throws BusinessProcessException {
        ContabilizzazioneFlussoStipendialeMensileBP bp = (ContabilizzazioneFlussoStipendialeMensileBP) context.getBusinessProcess();
        HookForward caller = (HookForward) context.getCaller();
        MeseTipoFlussoBulk meseTipoFlussoBulk = (MeseTipoFlussoBulk) caller.getParameter("model");
        bp.creaNuovaRiga(
                context,
                Optional.ofNullable(meseTipoFlussoBulk)
                        .flatMap(meseBulk1 -> Optional.ofNullable(meseBulk1.getMese()))
                        .orElseThrow(() -> new ValidationException("Indicare il mese!")),
                Optional.ofNullable(meseTipoFlussoBulk)
                        .flatMap(meseBulk1 -> Optional.ofNullable(meseBulk1.getTipoFlusso()))
                        .orElseThrow(() -> new ValidationException("Indicare il tipo di flusso!"))

        );
        bp.refresh(context);
        setMessage(context, FormBP.INFO_MESSAGE, "Operazione effettuata.");
        return context.findDefaultForward();
    }

    public Forward doEliminaRiga(ActionContext context) throws BusinessProcessException, RemoteException, ComponentException, PersistencyException {
        ContabilizzazioneFlussoStipendialeMensileBP bp = (ContabilizzazioneFlussoStipendialeMensileBP) context.getBusinessProcess();
        Stipendi_cofiBulk stipendiCofiBulk= (Stipendi_cofiBulk)bp.getModel();
        if (Optional.ofNullable(stipendiCofiBulk).isPresent()){
            try {
                if (stipendiCofiBulk.isLiquidato() ) {
                    setErrorMessage(context, "Non è possibile cancellare un flusso liquidato.");
                    return context.findDefaultForward();
                }
                bp.createFlussoStipendiComponentSession().cancellaFlussoNonLiquidato(context.getUserContext(), stipendiCofiBulk);
                bp.refresh(context);
                setMessage(context, FormBP.INFO_MESSAGE, "Operazione effettuata.");
            }catch (ComponentException e){
                setErrorMessage(context, e.getMessage());
            }

        }
        return context.findDefaultForward();
    }



}

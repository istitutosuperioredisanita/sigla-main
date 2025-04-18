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

package it.cnr.contab.docamm00.bp;

import it.cnr.contab.docamm00.docs.bulk.Documento_genericoBulk;
import it.cnr.contab.docamm00.docs.bulk.Documento_generico_rigaBulk;
import it.cnr.contab.docamm00.ejb.DocumentoGenericoComponentSession;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.HttpActionContext;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.bulk.ValidationException;
import it.cnr.jada.util.action.SimpleCRUDBP;

/**
 * Riga del documento generico attivo
 */

public class DocumentoGenericoAttivoRigaCRUDController extends it.cnr.jada.util.action.SimpleDetailCRUDController {
    public DocumentoGenericoAttivoRigaCRUDController(String name, Class modelClass, String listPropertyName, it.cnr.jada.util.action.FormController parent) {
        super(name, modelClass, listPropertyName, parent);
    }

    public boolean isGrowable() {

        Documento_genericoBulk doc = (Documento_genericoBulk) getParentModel();
        return super.isGrowable() && !((it.cnr.jada.util.action.CRUDBP) getParentController()).isSearching() &&
                !Documento_genericoBulk.STATO_PAGATO.equalsIgnoreCase(doc.getStato_cofi()) && !doc.isDocumentoStorno();

    }

    public boolean isShrinkable() {
        Documento_genericoBulk doc = (Documento_genericoBulk) getParentModel();
        return super.isShrinkable() && !((it.cnr.jada.util.action.CRUDBP) getParentController()).isSearching() &&
                !Documento_genericoBulk.STATO_PAGATO.equalsIgnoreCase(doc.getStato_cofi()) && !doc.isDocumentoStorno();
    }

    public void validate(ActionContext context, OggettoBulk model) throws ValidationException {
        if (context.getCurrentCommand().equals("doContabilizzaAccertamenti"))
            return;
        try {
            if ((Documento_generico_rigaBulk) model != null && ((Documento_generico_rigaBulk) model).getTerzo() == null)
                throw new ValidationException("Il campo anagrafica e' obbligatorio");
            if ((Documento_generico_rigaBulk) model != null && ((Documento_generico_rigaBulk) model).getDs_riga() == null)
                throw new ValidationException("Inserire una descrizione");
            if ((Documento_generico_rigaBulk) model != null && ((Documento_generico_rigaBulk) model).getModalita_pagamento_uo_cds() == null)
                throw new ValidationException("Inserire una modalità di pagamento");
//            if ((Documento_generico_rigaBulk) model != null && (((Documento_generico_rigaBulk) model).getIm_riga() == null || ((Documento_generico_rigaBulk) model).getIm_riga().compareTo(new java.math.BigDecimal(0)) == 0))
//                throw new ValidationException("Inserire un importo positivo");
            if ((Documento_generico_rigaBulk) model != null && ((Documento_generico_rigaBulk) model).getBanca_uo_cds() == null)
                throw new ValidationException("Inserire dei riferimenti bancari corretti");
            ((DocumentoGenericoComponentSession) (((SimpleCRUDBP) getParentController()).createComponentSession())).validaRiga(context.getUserContext(), (Documento_generico_rigaBulk) model);

        } catch (ValidationException e) {
            throw e;
        } catch (it.cnr.jada.comp.ApplicationException e) {
            throw new ValidationException(e.getMessage());
        } catch (Throwable e) {
            throw new it.cnr.jada.DetailedRuntimeException(e);
        }
    }

    public void validateForDelete(ActionContext context, OggettoBulk detail) throws ValidationException {
        try {
            Documento_generico_rigaBulk riga = (Documento_generico_rigaBulk) detail;
            if (riga.getTi_associato_manrev() != null && Documento_generico_rigaBulk.ASSOCIATO_A_MANDATO.equalsIgnoreCase(riga.getTi_associato_manrev()))
                throw new ValidationException("Impossibile eliminare il dettaglio \"" +
                        ((riga.getDs_riga() != null) ?
                                riga.getDs_riga() :
                                String.valueOf(riga.getProgressivo_riga().longValue())) +
                        "\" perchè associato a mandato.");
            if (riga.isRigaStornata())
                throw new ValidationException("Impossibile eliminare il dettaglio \"" +
                        ((riga.getDs_riga() != null) ?
                                riga.getDs_riga() :
                                String.valueOf(riga.getProgressivo_riga().longValue())) +
                        "\" perchè stornato.");

            ((DocumentoGenericoComponentSession) (((SimpleCRUDBP) getParentController()).createComponentSession())).eliminaRiga(context.getUserContext(), (Documento_generico_rigaBulk) detail);
        } catch (it.cnr.jada.comp.ApplicationException e) {
            throw new ValidationException(e.getMessage());
        } catch (ValidationException e) {
            throw e;
        } catch (Throwable e) {
            throw new it.cnr.jada.DetailedRuntimeException(e);
        }
    }

    @Override
    public void writeHTMLToolbar(
            javax.servlet.jsp.PageContext context,
            boolean reset,
            boolean find,
            boolean delete, boolean closedToolbar) throws java.io.IOException, javax.servlet.ServletException {

        super.writeHTMLToolbar(context, reset, find, delete, false);

        boolean isFromBootstrap = HttpActionContext.isFromBootstrap(context);
        it.cnr.jada.util.action.CRUDBP bp = (it.cnr.jada.util.action.CRUDBP) getParentController();
        Documento_genericoBulk doc = (Documento_genericoBulk) bp.getModel();
        String command = "javascript:submitForm('doRicercaAccertamento')";
        it.cnr.jada.util.jsp.JSPUtils.toolbarButton(
                context,
                isFromBootstrap ? "fa fa-fw fa-bolt" : "img/history16.gif",
                !(isInputReadonly() || getDetails().isEmpty() || ((CRUDDocumentoGenericoAttivoBP) getParentController()).isSearching()) && !doc.isDocumentoStorno() ? command : null,
                true,
                "Contabilizza",
                "btn-sm btn-outline-primary btn-title",
                isFromBootstrap);

        if (bp instanceof IDocumentoAmministrativoBP) {
            boolean enabled = !(bp.isSearching() || getDetails().isEmpty() || bp.isViewing());

            if (bp instanceof VoidableBP)
                enabled = enabled && !((VoidableBP) bp).isModelVoided();

            if (bp instanceof CRUDDocumentoGenericoAttivoBP)
                enabled = (enabled || ((CRUDDocumentoGenericoAttivoBP) bp).isManualModify()) && ((CRUDDocumentoGenericoAttivoBP) bp).isDetailDoubleable();
            else
                enabled = enabled && (((IDocumentoAmministrativoBP) bp).isDeleting() || ((IDocumentoAmministrativoBP) bp).isManualModify());

            Documento_generico_rigaBulk riga = (Documento_generico_rigaBulk) getModel();
            enabled = enabled && !(riga == null || riga.getTi_associato_manrev() != null && Documento_generico_rigaBulk.ASSOCIATO_A_MANDATO.equalsIgnoreCase(riga.getTi_associato_manrev()))
                    && !doc.isDocumentoStorno() && !riga.isRigaStornata();

            it.cnr.jada.util.jsp.JSPUtils.toolbarButton(
                    context,
                    isFromBootstrap ? "fa fa-fw fa-clipboard" : "img/bookmarks16.gif",
                    enabled ? "javascript:submitForm('doSdoppiaDettaglio');" : null,
                    true, "Sdoppia",
                    "btn-sm btn-outline-success btn-title",
                    HttpActionContext.isFromBootstrap(context));
            if (riga.isDocumentoStorno()) {
                String descrizione = null;
                if (riga.getDocumento_generico_riga_storno() != null) {
                    descrizione = String.format("Visualizza documento stornato n. %s/%s/%s/%s/%s",
                            riga.getEsercizio_storno(),
                            riga.getCd_cds_storno(),
                            riga.getCd_unita_organizzativa_storno(),
                            riga.getCd_tipo_documento_amm_storno(),
                            riga.getPg_documento_generico_storno()
                    );
                }
                if (riga.getFattura_attiva_riga_storno() != null) {
                    descrizione = String.format("Visualizza Fattura Attiva n. %s/%s/%s/%s",
                            riga.getEsercizio_storno_fa(),
                            riga.getCd_cds_storno_fa(),
                            riga.getCd_unita_organizzativa_storno_fa(),
                            riga.getPg_fattura_attiva_storno()
                    );
                }
                it.cnr.jada.util.jsp.JSPUtils.toolbarButton(
                        context,
                        isFromBootstrap ? "fa fa-fw fa-link" : "img/bookmarks16.gif",
                        "javascript:submitForm('doApriDocumentoStornato');",
                        true,
                        descrizione,
                        "btn-sm btn-outline-primary btn-title",
                        HttpActionContext.isFromBootstrap(context));
            }
        }
        super.closeButtonGROUPToolbar(context);
    }
}
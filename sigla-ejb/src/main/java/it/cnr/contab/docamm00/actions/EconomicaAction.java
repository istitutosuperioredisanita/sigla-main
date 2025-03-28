/*
 * Copyright (C) 2021  Consiglio Nazionale delle Ricerche
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

package it.cnr.contab.docamm00.actions;

import it.cnr.contab.coepcoan00.comp.ScritturaPartitaDoppiaNotEnabledException;
import it.cnr.contab.coepcoan00.comp.ScritturaPartitaDoppiaNotRequiredException;
import it.cnr.contab.coepcoan00.consultazioni.bp.ConsultazionePartitarioBP;
import it.cnr.contab.coepcoan00.core.bulk.*;
import it.cnr.contab.docamm00.bp.IDocAmmAnaliticaBP;
import it.cnr.contab.docamm00.bp.IDocAmmEconomicaBP;
import it.cnr.contab.docamm00.docs.bulk.IDocumentoAmministrativoBulk;
import it.cnr.contab.util.Utility;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Forward;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.action.CRUDAction;
import it.cnr.jada.util.action.FormBP;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class EconomicaAction extends CRUDAction {
    public Forward doGeneraScritturaEconomica(ActionContext actionContext) throws BusinessProcessException {
        IDocAmmEconomicaBP bp = Optional.ofNullable(actionContext.getBusinessProcess())
                .filter(IDocAmmEconomicaBP.class::isInstance)
                .map(IDocAmmEconomicaBP.class::cast)
                .orElseThrow(() -> new BusinessProcessException("Business process non compatibile!"));
        final IDocumentoCogeBulk documentoCogeBulk = Optional.ofNullable(bp.getEconomicaModel())
                .filter(IDocumentoCogeBulk.class::isInstance)
                .map(IDocumentoCogeBulk.class::cast)
                .orElseThrow(() -> new BusinessProcessException("Modello di business non compatibile!"));
        try {
            if (Optional.ofNullable(bp.getEconomicaModel()).filter(OggettoBulk::isToBeCreated).isPresent())
                throw new ApplicationException("Il documento risulta non salvato! Proposta scrittura prima nota non possibile.");

            if (Utility.createConfigurazioneCnrComponentSession().isAttivaAnalitica(actionContext.getUserContext())) {
                ResultScrittureContabili result = Utility.createProposeScritturaComponentSession().proposeScrittureContabili(
                        actionContext.getUserContext(),
                        documentoCogeBulk);

                documentoCogeBulk.setScrittura_partita_doppia(result.getScritturaPartitaDoppiaBulk());
                documentoCogeBulk.setScrittura_analitica(result.getScritturaAnaliticaBulk());
            } else {
                documentoCogeBulk.setScrittura_partita_doppia(Utility.createProposeScritturaComponentSession().proposeScritturaPartitaDoppia(
                        actionContext.getUserContext(),
                        documentoCogeBulk)
                );
            }
            Optional.of(documentoCogeBulk)
                    .filter(OggettoBulk.class::isInstance)
                    .map(OggettoBulk.class::cast)
                    .ifPresent(OggettoBulk::setToBeUpdated);
            bp.getMovimentiAvere().reset(actionContext);
            bp.getMovimentiDare().reset(actionContext);
            bp.setMessage(FormBP.INFO_MESSAGE, "Scrittura di economica generata correttamente.");
            bp.setDirty(true);
        } catch (ScritturaPartitaDoppiaNotRequiredException | ScritturaPartitaDoppiaNotEnabledException e) {
            bp.setMessage(FormBP.INFO_MESSAGE, e.getMessage());
        } catch (ComponentException | RemoteException e) {
            return handleException(actionContext, e);
        }
        return actionContext.findDefaultForward();
    }

    public Forward doGeneraScritturaAnalitica(ActionContext actionContext) throws BusinessProcessException {
        IDocAmmAnaliticaBP bp = Optional.ofNullable(actionContext.getBusinessProcess())
                .filter(IDocAmmAnaliticaBP.class::isInstance)
                .map(IDocAmmAnaliticaBP.class::cast)
                .orElseThrow(() -> new BusinessProcessException("Business process non compatibile!"));
        final IDocumentoCogeBulk documentoCogeBulk = Optional.ofNullable(bp.getAnaliticaModel())
                .filter(IDocumentoCogeBulk.class::isInstance)
                .map(IDocumentoCogeBulk.class::cast)
                .orElseThrow(() -> new BusinessProcessException("Modello di business non compatibile!"));
        try {
            if (Optional.ofNullable(bp.getAnaliticaModel()).filter(OggettoBulk::isToBeCreated).isPresent())
                throw new ApplicationException("Il documento risulta non salvato! Proposta scrittura analitica non possibile.");

            documentoCogeBulk.setScrittura_analitica(Utility.createProposeScritturaComponentSession().proposeScritturaAnalitica(
                    actionContext.getUserContext(),
                    documentoCogeBulk));

            Optional.of(documentoCogeBulk)
                    .filter(OggettoBulk.class::isInstance)
                    .map(OggettoBulk.class::cast)
                    .ifPresent(OggettoBulk::setToBeUpdated);
            bp.getMovimentiAnalitici().reset(actionContext);
            bp.setMessage(FormBP.INFO_MESSAGE, "Scrittura di analitica generata correttamente.");
            bp.setDirty(true);
        } catch (ScritturaPartitaDoppiaNotRequiredException | ScritturaPartitaDoppiaNotEnabledException e) {
            bp.setMessage(FormBP.INFO_MESSAGE, e.getMessage());
        } catch (ComponentException | RemoteException e) {
            return handleException(actionContext, e);
        }
        return actionContext.findDefaultForward();
    }

    public Forward doPartitario(ActionContext actionContext) throws BusinessProcessException {
        IDocAmmEconomicaBP bp = Optional.ofNullable(actionContext.getBusinessProcess())
                .filter(IDocAmmEconomicaBP.class::isInstance)
                .map(IDocAmmEconomicaBP.class::cast)
                .orElseThrow(() -> new BusinessProcessException("Business process non compatibile!"));
        final Optional<IDocumentoAmministrativoBulk> documentoAmministrativoBulk = Optional.ofNullable(bp.getEconomicaModel())
                .filter(IDocumentoAmministrativoBulk.class::isInstance)
                .map(IDocumentoAmministrativoBulk.class::cast);
        if (documentoAmministrativoBulk.isPresent()) {
            final Stream<Movimento_cogeBulk> movimentoCogeBulks = documentoAmministrativoBulk
                    .flatMap(documentoAmministrativoBulk1 -> Optional.ofNullable(documentoAmministrativoBulk1.getScrittura_partita_doppia()))
                    .flatMap(scrittura_partita_doppiaBulk -> Optional.ofNullable(scrittura_partita_doppiaBulk.getAllMovimentiColl()))
                    .orElse(Collections.emptyList())
                    .stream();
            List<IDocumentoCogeBulk> iDocumentoCogeBulks = Stream.concat(
                            movimentoCogeBulks.filter(movimento_cogeBulk -> Optional.ofNullable(movimento_cogeBulk.getDocumentoCoge()).isPresent()).map(Movimento_cogeBulk::getDocumentoCoge),
                            Stream.of(documentoAmministrativoBulk.get()))
                    .distinct()
                    .collect(Collectors.toList());

            ConsultazionePartitarioBP consBP = (ConsultazionePartitarioBP) actionContext.createBusinessProcess(
                    "ConsultazionePartitarioBP",
                    new Object[]{iDocumentoCogeBulks.stream().filter(Utility.distinctByKey(o -> o.primaryKeyHashCode())).collect(Collectors.toList()), "partitario"}
            );
            RemoteIterator ri = consBP.openIterator(actionContext);
            try {
                if (!Optional.ofNullable(ri).filter(remoteIterator -> {
                    try {
                        return remoteIterator.countElements() > 0;
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                }).isPresent()) {
                    it.cnr.jada.util.ejb.EJBCommonServices.closeRemoteIterator(actionContext, ri);
                    bp.setMessage(FormBP.WARNING_MESSAGE, "La ricerca non ha fornito alcun risultato.");
                    return actionContext.findDefaultForward();
                }
            } catch (Exception _ex) {
                handleException(actionContext, _ex);
            }
            actionContext.addBusinessProcess(consBP);
            return actionContext.findDefaultForward();
        }
        final Optional<IDocumentoCogeBulk> documentoCogeBulk = Optional.ofNullable(bp.getEconomicaModel())
                .filter(IDocumentoCogeBulk.class::isInstance)
                .map(IDocumentoCogeBulk.class::cast);
        if (documentoCogeBulk.isPresent()) {
            final List<IDocumentoCogeBulk> iDocumentoCogeBulks = documentoCogeBulk
                    .flatMap(documentoCogeBulk1 -> Optional.ofNullable(documentoCogeBulk1.getScrittura_partita_doppia()))
                    .flatMap(scrittura_partita_doppiaBulk -> Optional.ofNullable(scrittura_partita_doppiaBulk.getAllMovimentiColl()))
                    .orElse(Collections.emptyList())
                    .stream()
                    .filter(movimento_cogeBulk -> Optional.ofNullable(movimento_cogeBulk.getDocumentoCoge()).isPresent())
                    .map(Movimento_cogeBulk::getDocumentoCoge)
                    .distinct()
                    .collect(Collectors.toList());
            if (!iDocumentoCogeBulks.isEmpty()) {
                ConsultazionePartitarioBP consBP = (ConsultazionePartitarioBP) actionContext.createBusinessProcess(
                        "ConsultazionePartitarioBP",
                        new Object[]{iDocumentoCogeBulks, "partitario"}
                );
                RemoteIterator ri = consBP.openIterator(actionContext);
                try {
                    if (!Optional.ofNullable(ri).filter(remoteIterator -> {
                        try {
                            return remoteIterator.countElements() > 0;
                        } catch (RemoteException e) {
                            throw new RuntimeException(e);
                        }
                    }).isPresent()) {
                        it.cnr.jada.util.ejb.EJBCommonServices.closeRemoteIterator(actionContext, ri);
                        bp.setMessage(FormBP.WARNING_MESSAGE, "La ricerca non ha fornito alcun risultato.");
                        return actionContext.findDefaultForward();
                    }
                } catch (Exception _ex) {
                    handleException(actionContext, _ex);
                }
                actionContext.addBusinessProcess(consBP);
                return actionContext.findDefaultForward();
            }
        }
        bp.setMessage(FormBP.WARNING_MESSAGE, "Non ci sono elementi da visualizzare!");
        return actionContext.findDefaultForward();
    }
}

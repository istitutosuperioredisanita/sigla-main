/*
 * Copyright (C) 2020  Consiglio Nazionale delle Ricerche
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
package it.cnr.contab.util00.bp;

import it.cnr.contab.anagraf00.core.bulk.CambiaModalitaPagamentoBulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.ejb.RicercaComponentSession;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.util.RemoteIterator;

import java.util.Optional;

public class ModalitaPagamentoModalBP extends ModalBP{

    @Override
    public RemoteIterator find(ActionContext actioncontext, CompoundFindClause compoundfindclause, OggettoBulk oggettobulk, OggettoBulk oggettobulk1, String s) throws BusinessProcessException {
        final Optional<CambiaModalitaPagamentoBulk> cambiaModalitaPagamentoBulk = Optional.ofNullable(oggettobulk1)
                .filter(CambiaModalitaPagamentoBulk.class::isInstance)
                .map(CambiaModalitaPagamentoBulk.class::cast);
        if (cambiaModalitaPagamentoBulk.isPresent()) {
            try {
                return ((RicercaComponentSession)createComponentSession("JADAEJB_CRUDComponentSession")).cerca(
                        actioncontext.getUserContext(),
                        Optional.ofNullable(compoundfindclause).orElse(new CompoundFindClause()),
                        oggettobulk,
                        "selectBancaFor",
                        cambiaModalitaPagamentoBulk.get().getModalita_pagamento(),
                        cambiaModalitaPagamentoBulk.get().getTerzo().getCd_terzo());
            } catch (Exception exception) {
                throw new BusinessProcessException(exception);
            }
        }
        return super.find(actioncontext, compoundfindclause, oggettobulk, oggettobulk1, s);
    }
}

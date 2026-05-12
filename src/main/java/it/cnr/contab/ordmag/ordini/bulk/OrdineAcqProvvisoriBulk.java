/*
 * Copyright (C) 2022  Consiglio Nazionale delle Ricerche
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

package it.cnr.contab.ordmag.ordini.bulk;

import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;
import it.cnr.jada.DetailedRuntimeException;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.util.action.CRUDBP;

import java.util.Optional;

public class OrdineAcqProvvisoriBulk extends OrdineAcqTuttiBulk{
    @Override
    public OggettoBulk initializeForSearch(CRUDBP bp, ActionContext context) {
        final OrdineAcqBulk ordineAcqBulk = Optional.ofNullable(super.initializeForSearch(bp, context))
                .filter(OrdineAcqBulk.class::isInstance)
                .map(OrdineAcqBulk.class::cast)
                .orElseThrow(() -> new DetailedRuntimeException("Cannot instance Bulk!"));
        ordineAcqBulk.setStato(OrdineAcqBulk.STATO_INSERITO);
        return ordineAcqBulk;
    }
}

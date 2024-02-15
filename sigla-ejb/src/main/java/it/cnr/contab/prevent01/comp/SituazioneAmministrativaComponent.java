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

package it.cnr.contab.prevent01.comp;

import it.cnr.contab.prevent01.bulk.SituazioneAmministrativaBulk;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.CRUDComponent;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.comp.IPrintMgr;

public class SituazioneAmministrativaComponent extends CRUDComponent implements IPrintMgr {
/**
 * PdgAggregatoComponent constructor comment.
 */
	public SituazioneAmministrativaComponent() {
		super();
	}


	@Override
	public OggettoBulk inizializzaBulkPerStampa(UserContext userContext, OggettoBulk oggettoBulk) throws ComponentException {
		((SituazioneAmministrativaBulk) oggettoBulk).setEsercizio(CNRUserContext.getEsercizio(userContext));
		return oggettoBulk;
	}

	@Override
	public OggettoBulk stampaConBulk(UserContext userContext, OggettoBulk oggettoBulk) throws ComponentException {
		return oggettoBulk;
	}
}

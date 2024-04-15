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

package it.cnr.contab.prevent01.bp;

import it.cnr.contab.prevent01.bulk.Stampa_pdgp_bilancioBulk;
import it.cnr.contab.reports.bp.ParametricPrintBP;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.bulk.OggettoBulk;


public class SituazioneAmministrativaBP extends ParametricPrintBP {

    /**
     *
     */
    private static final long serialVersionUID = -3023075061400467210L;

    /**
     * StampaRendicontoFinanziarioBP constructor comment.
     */
    public SituazioneAmministrativaBP() {
        super();
    }

    /**
     * StampaRendicontoFinanziarioBP constructor comment.
     *
     * @param function java.lang.String
     */
    public SituazioneAmministrativaBP(String function) {
        super(function);
    }

    @Override
    public OggettoBulk initializeBulkForPrint(ActionContext context, OggettoBulk bulk) throws BusinessProcessException {
        try {
            OggettoBulk oggettoBulk = super.initializeBulkForPrint(context, bulk);
            ((Stampa_pdgp_bilancioBulk) oggettoBulk).setEsercizio(CNRUserContext.getEsercizio(context.getUserContext()));
            return oggettoBulk;
        }catch(Throwable e) {
            throw new BusinessProcessException(e);
        }
    }



}

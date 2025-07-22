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

/*
 * Created on Jan 19, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package it.cnr.contab.docamm00.consultazioni.bp;

import it.cnr.contab.config00.ejb.Configurazione_cnrComponentSession;
import it.cnr.contab.docamm00.consultazioni.bulk.V_confronta_costi_coge_cofiBulk;
import it.cnr.contab.doccont00.core.bulk.MandatoBulk;
import it.cnr.contab.doccont00.intcass.bulk.StatoTrasmissione;
import it.cnr.contab.doccont00.intcass.bulk.V_mandato_reversaleBulk;
import it.cnr.contab.utente00.ejb.UtenteComponentSession;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Config;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.ejb.CRUDComponentSession;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.action.CondizioneComplessaBulk;
import it.cnr.jada.util.action.ConsultazioniBP;
import it.cnr.jada.util.action.SelezionatoreListaBP;
import it.cnr.jada.util.ejb.EJBCommonServices;

import java.rmi.RemoteException;
import java.util.Optional;

/**
 * @author Matilde D'Urso
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class ConfCostiCogeCofiBP extends ConsultazioniBP {
	private static final long serialVersionUID = 1L;

	public ConfCostiCogeCofiBP() {
	}

	public ConfCostiCogeCofiBP(String s) {
		super(s);
	}

	protected CRUDComponentSession getComponentSession() {
		return (CRUDComponentSession) EJBCommonServices.createEJB("JADAEJB_CRUDComponentSession");
	}

	@Override
	protected void init(Config config, ActionContext actioncontext)
			throws BusinessProcessException {
		try {

			setBulkInfo(it.cnr.jada.bulk.BulkInfo.getBulkInfo(getClass().getClassLoader().loadClass(config.getInitParameter("bulkClassName"))));
			OggettoBulk model = (OggettoBulk) getBulkInfo().getBulkClass().newInstance();
			((V_confronta_costi_coge_cofiBulk) model).setTipo_visualizzazione(V_confronta_costi_coge_cofiBulk.VISUALIZZAZIONE_DETTAGLI);
			setModel(actioncontext, model);
			super.init(config, actioncontext);

		}  catch (InstantiationException e) {
			throw handleException(e);
		} catch (IllegalAccessException e) {
			throw handleException(e);
		} catch (ClassNotFoundException e) {
			throw handleException(e);
		}
	}
	public void openIterator(ActionContext actioncontext)
			throws BusinessProcessException {
		try {
			setIterator(actioncontext, search(actioncontext,new CompoundFindClause(),getModel())
			);
		} catch (RemoteException e) {
			throw new BusinessProcessException(e);
		}
	}
	public RemoteIterator search(
			ActionContext actioncontext,
			CompoundFindClause compoundfindclause,
			OggettoBulk oggettobulk)
			throws BusinessProcessException {
		V_confronta_costi_coge_cofiBulk v_confronta_costi_coge_cofiBulk = (V_confronta_costi_coge_cofiBulk) oggettobulk;
		try {
			return getComponentSession().cerca(
					actioncontext.getUserContext(),
					compoundfindclause,
					v_confronta_costi_coge_cofiBulk,
					"selectByClauseForTipoVisualizzazione");
		} catch (ComponentException|RemoteException e) {
			throw handleException(e);
		}
	}
	public void setColumnSet(ActionContext actioncontext, String tipoVisualizzazione) {
		String columnSetName = "visualizzazioneCompleta";
		if (tipoVisualizzazione.equalsIgnoreCase(V_confronta_costi_coge_cofiBulk.VISUALIZZAZIONE_TOTALI))
			columnSetName = "visualizzazioneTotali";

		setColumns(getBulkInfo().getColumnFieldPropertyDictionary(columnSetName));

	}
}

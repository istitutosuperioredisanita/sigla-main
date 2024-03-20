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

package it.cnr.contab.ordmag.magazzino.bp;

import it.cnr.contab.config00.bulk.Configurazione_cnrBulk;
import it.cnr.contab.docamm00.tabrif.bulk.Bene_servizioBulk;
import it.cnr.contab.ordmag.anag00.UnitaMisuraBulk;
import it.cnr.contab.ordmag.magazzino.bulk.*;
import it.cnr.contab.ordmag.magazzino.ejb.MovimentiMagComponentSession;
import it.cnr.contab.reports.bp.ParametricPrintBP;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Config;
import it.cnr.jada.bulk.BulkList;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.bulk.ValidationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.util.action.SimpleCRUDBP;
import it.cnr.jada.util.action.SimpleDetailCRUDController;
import it.cnr.jada.util.jsp.Button;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StampaChiusuraMagazzinoBP extends ParametricPrintBP {
	private static final long serialVersionUID = 1L;
	private boolean effettuatoCalcolo;
	private ChiusuraAnnoBulk chiusuraAnno;

	public StampaChiusuraMagazzinoBP() {
	}


	protected void init(it.cnr.jada.action.Config config,it.cnr.jada.action.ActionContext context) throws it.cnr.jada.action.BusinessProcessException {
		try {
			setBulkClassName(config.getInitParameter("bulkClassName"));
			setComponentSessioneName(config.getInitParameter("componentSessionName"));

		} catch(ClassNotFoundException e) {
			throw new RuntimeException("Non trovata la classe bulk");
		}
		super.init(config,context);
	}


	public StampaChiusuraMagazzinoBP(String function) {
		super(function);
	}

	public Button[] createToolbar() {
		Button[] baseToolbar = super.createToolbar();
		Button[] toolbar = new Button[3];
		int i = 0;
		for (Button button : baseToolbar) {
			toolbar[i++] = button;
		}
		toolbar[i++] = new Button(it.cnr.jada.util.Config.getHandler().getProperties(getClass()), "CRUDToolbar.calcolaRim");
		return toolbar;
	}

	public boolean isPrintButtonHidden(){
		return !effettuatoCalcolo;
	}
	public boolean isCalcoloButtonHidden()
	{
		return false;
	}

	public boolean isEffettuatoCalcolo() {
		return effettuatoCalcolo;
	}

	public void setEffettuatoCalcolo(boolean effettuatoCalcolo) {
		this.effettuatoCalcolo = effettuatoCalcolo;
	}

	public ChiusuraAnnoBulk getChiusuraAnno() {
		return chiusuraAnno;
	}

	public void setChiusuraAnno(ChiusuraAnnoBulk chiusuraAnno) {
		this.chiusuraAnno = chiusuraAnno;
	}


}
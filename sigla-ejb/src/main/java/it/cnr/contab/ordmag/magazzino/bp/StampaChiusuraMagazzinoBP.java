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

import it.cnr.contab.compensi00.docs.bulk.EstrazioneINPSBulk;
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
import it.cnr.jada.bulk.FillException;
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

	private ChiusuraAnnoBulk chiusuraAnno;
	private boolean isEsercizioChiusoPerAlmenoUnCds;

	public StampaChiusuraMagazzinoBP() {
	}


	protected void init(it.cnr.jada.action.Config config,it.cnr.jada.action.ActionContext context) throws it.cnr.jada.action.BusinessProcessException {
		try {
			super.init(config, context);

			setBulkClassName(config.getInitParameter("bulkClassName"));
			setComponentSessioneName(config.getInitParameter("componentSessionName"));

			if(this.getMapping().getConfig().getInitParameter("CHIUSURA_DEFINITIVA") == null) {
				this.getBulkInfo().setShortDescription("Chiusura Magazzino Provvisoria");
			}else{
				this.getBulkInfo().setShortDescription("Chiusura Magazzino Definitiva");
			}

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
		Button[] toolbar = null;
		if(this.getMapping().getConfig().getInitParameter("CHIUSURA_DEFINITIVA") == null) {
			toolbar=new Button[3];
		}else{
			toolbar=new Button[5];
		}
		int i = 0;
		for (Button button : baseToolbar) {
			toolbar[i++] = button;
		}
		if(this.getMapping().getConfig().getInitParameter("CHIUSURA_DEFINITIVA") == null) {
			toolbar[i++] = new Button(it.cnr.jada.util.Config.getHandler().getProperties(getClass()), "CRUDToolbar.calcolaRim");
		}else{
			toolbar[i++] = new Button(it.cnr.jada.util.Config.getHandler().getProperties(getClass()), "CRUDToolbar.calcolaRimDef");
			toolbar[i++] = new Button(it.cnr.jada.util.Config.getHandler().getProperties(getClass()), "CRUDToolbar.chiusuraDef");
			toolbar[i++] = new Button(it.cnr.jada.util.Config.getHandler().getProperties(getClass()), "CRUDToolbar.undochiusuraDef");

		}
		return toolbar;
	}

	public boolean isPrintButtonHidden(){
		// in fase provvisoria abilitato solo se esiste la chiusua anno, quindi almeno un calcolo effettuato
		if(this.getMapping().getConfig().getInitParameter("CHIUSURA_DEFINITIVA") == null){
			if(this.getChiusuraAnno() != null){
				return false;
			}
			// in fase definitiva abilitato se esiste la chiusura in stato diverso da PROVVISORIO
		}else{
			if(this.getChiusuraAnno() != null && !this.getChiusuraAnno().getStato().equals(ChiusuraAnnoBulk.STATO_CHIUSURA_PROVVISORIO)){
				return false;
			}
		}
		return true;
	}
	public boolean isCalcoloButtonHidden()
	{
		if(this.isEsercizioChiusoPerAlmenoUnCds){
			return true;
		// bottone calcolo provvisorio abilitato solo se calcolo ancora non effettuto oppure stato chiusura uguale PROVVISORIO
		}else if(this.getChiusuraAnno() == null || this.getChiusuraAnno().getStato().equals(ChiusuraAnnoBulk.STATO_CHIUSURA_PROVVISORIO)){
			return false;
		}
		else
			return true;
	}
	public boolean isCalcoloDefinitivoButtonHidden(){
		if(this.isEsercizioChiusoPerAlmenoUnCds) {
			return true;
		}
		else if(this.getChiusuraAnno() != null && this.getChiusuraAnno().getStato().equals(ChiusuraAnnoBulk.STATO_CHIUSURA_PROVVISORIO)){
			return false;
		}
		else
			return true;
	}
	public boolean isChiusuraDefinitivaButtonHidden()
	{
		if(this.isEsercizioChiusoPerAlmenoUnCds) {
			return true;
		}
		// bottone salvataggio definitivo abilitato solo se stato uguale PREDEFINITIVO
		else if(this.getChiusuraAnno() != null && this.getChiusuraAnno().getStato().equals(ChiusuraAnnoBulk.STATO_CHIUSURA_PREDEFINITIVO)){
			return false;
		}
		else
			return true;
	}
	public boolean isAnnullaChiusuraDefinitivaButtonHidden()
	{
		if(this.isEsercizioChiusoPerAlmenoUnCds) {
			return true;
		}
		// bottone di annulla definitivo abilitato solo se stato uguale DEFINITIVO
		else if(this.getChiusuraAnno() != null && this.getChiusuraAnno().getStato().equals(ChiusuraAnnoBulk.STATO_CHIUSURA_DEFINITIVO)){
			return false;
		}
		else
			return true;
	}



	public ChiusuraAnnoBulk getChiusuraAnno() {
		return chiusuraAnno;
	}

	public void setChiusuraAnno(ChiusuraAnnoBulk chiusuraAnno) {
		this.chiusuraAnno = chiusuraAnno;
	}

	public boolean isEsercizioChiusoPerAlmenoUnCds() {
		return isEsercizioChiusoPerAlmenoUnCds;
	}

	public void setEsercizioChiusoPerAlmenoUnCds(boolean esercizioChiusoPerAlmenoUnCds) {
		isEsercizioChiusoPerAlmenoUnCds = esercizioChiusoPerAlmenoUnCds;
	}
}
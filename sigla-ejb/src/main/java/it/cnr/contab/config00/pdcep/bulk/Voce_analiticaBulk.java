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

package it.cnr.contab.config00.pdcep.bulk;

import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.util.action.CRUDBP;

import java.util.*;

/**
 * Classe che eredita le caratteristiche della superclasse <code>OggettoBulk<code>.
 * Contiene tutte le variabili e i metodi che sono comuni alle sue sottoclassi.
 * Gestisce i dati relativi alla tabella Voce_ep.
 */
public class Voce_analiticaBulk extends Voce_analiticaBase {
	protected ContoBulk voce_ep = new ContoBulk();

	public Voce_analiticaBulk() {
		super();
	}

	public Voce_analiticaBulk(java.lang.String cd_voce_ana, java.lang.Integer esercizio) {
		super(cd_voce_ana,esercizio);
	}

	public ContoBulk getVoce_ep() {
		return voce_ep;
	}

	public void setVoce_ep(ContoBulk voce_ep) {
		this.voce_ep = voce_ep;
	}

	@java.lang.Override
	public java.lang.Integer getEsercizio_voce_ep() {
		return Optional.ofNullable(this.getVoce_ep())
				.map(ContoBulk::getEsercizio)
				.orElse(null);
	}

	@java.lang.Override
	public void setEsercizio_voce_ep(java.lang.Integer esercizio_voce_ep) {
		Optional.ofNullable(this.getVoce_ep()).ifPresent(el->el.setEsercizio(esercizio_voce_ep));
	}

	@java.lang.Override
	public java.lang.String getCd_voce_ep() {
		return Optional.ofNullable(this.getVoce_ep())
				.map(ContoBulk::getCd_voce_ep)
				.orElse(null);
	}

	@java.lang.Override
	public void setCd_voce_ep(java.lang.String cd_voce_ep) {
		Optional.ofNullable(this.getVoce_ep()).ifPresent(el->el.setCd_voce_ep(cd_voce_ep));
	}

	@Override
	public OggettoBulk initializeForInsert(CRUDBP crudbp, ActionContext actioncontext) {
		setFl_default(Boolean.FALSE);
		setEsercizio(CNRUserContext.getEsercizio(actioncontext.getUserContext()));
		return super.initializeForInsert(crudbp, actioncontext);
	}

	@Override
	public OggettoBulk initializeForSearch(CRUDBP crudbp, ActionContext actioncontext) {
		setEsercizio(CNRUserContext.getEsercizio(actioncontext.getUserContext()));
		return super.initializeForSearch(crudbp, actioncontext);
	}

	@Override
	public OggettoBulk initializeForFreeSearch(CRUDBP crudbp, ActionContext actioncontext) {
		setEsercizio(CNRUserContext.getEsercizio(actioncontext.getUserContext()));
		return super.initializeForFreeSearch(crudbp, actioncontext);
	}

	public boolean isROCdVoceAna() {
		return !this.isToBeCreated();
	}
}
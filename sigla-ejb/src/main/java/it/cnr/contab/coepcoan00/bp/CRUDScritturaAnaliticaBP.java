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

package it.cnr.contab.coepcoan00.bp;

import it.cnr.contab.coepcoan00.core.bulk.*;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;
import it.cnr.contab.utenze00.bulk.CNRUserInfo;
import it.cnr.contab.util.Utility;
import it.cnr.jada.action.*;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.util.action.*;

import java.rmi.RemoteException;
import java.util.Optional;

/**
 * Business Process che gestisce le attività di CRUD per l'entita' Scrittura Analitica
 */
public class CRUDScritturaAnaliticaBP extends it.cnr.jada.util.action.SimpleCRUDBP {
	public static final String[] TAB_ANALITICA = new String[]{"tabAnalitica", "Analitica", "/coepcoan00/tab_doc_analitica.jsp"};
	private Unita_organizzativaBulk uoScrivania;
	private Boolean isBloccoScrittureProposte;
    public boolean searchButtonHidden=Boolean.FALSE;
    public boolean freeSearchButtonHidden=Boolean.FALSE;

	private final SimpleDetailCRUDController movimenti = new SimpleDetailCRUDController("Movimenti",it.cnr.contab.coepcoan00.core.bulk.Movimento_coanBulk.class,"movimentiColl",this);
	/**
	 * CRUDScritturaAnaliticaBP constructor comment.
	 */
	public CRUDScritturaAnaliticaBP() {
		super();
		setTab("tab", "tabScritturaAnalitica");
	}
	/**
	 * CRUDScritturaAnaliticaBP constructor comment.
	 * @param function java.lang.String
	 */
	public CRUDScritturaAnaliticaBP(String function) {
		super(function);
		setTab("tab", "tabScritturaAnalitica");
	}
	/**
	 * @return it.cnr.jada.util.action.SimpleDetailCRUDController
	 */
	public final it.cnr.jada.util.action.SimpleDetailCRUDController getMovimenti() {
		return movimenti;
	}

	public boolean isScritturaReadonly() {
		return isBloccoScrittureProposte &&
				!OrigineScritturaEnum.PRIMA_NOTA_MANUALE.name().equals(((Scrittura_analiticaBulk) getModel()).getOrigine_scrittura());
	}

	@Override
	protected void basicEdit(ActionContext actioncontext, OggettoBulk oggettobulk, boolean flag) throws BusinessProcessException {
		super.basicEdit(actioncontext, oggettobulk, flag);
		if (isScritturaReadonly())
			setStatus(VIEW);
	}

	/* Metodo per riportare il fuoco sul tab iniziale */
	protected void resetTabs(ActionContext context) {
		setTab( "tab", "tabScritturaAnalitica");
	}

	@Override
	protected void initialize(ActionContext actioncontext) throws BusinessProcessException {
		super.initialize(actioncontext);
		try {
			uoScrivania = CNRUserInfo.getUnita_organizzativa(actioncontext);
			isBloccoScrittureProposte = Utility.createConfigurazioneCnrComponentSession().isBloccoScrittureProposte(actioncontext.getUserContext());
		} catch (ComponentException | RemoteException e) {
			throw handleException(e);
		}
	}

	public boolean isUoEnte(){
		return (uoScrivania.getCd_tipo_unita().compareTo(it.cnr.contab.config00.sto.bulk.Tipo_unita_organizzativaHome.TIPO_UO_ENTE)==0);
	}

	public boolean isFromDocumentoOrigine() {
		return Optional.ofNullable(getModel())
				.filter(Scrittura_analiticaBulk.class::isInstance)
				.map(Scrittura_analiticaBulk.class::cast)
				.map(scrittura_analiticaBulk -> Optional.ofNullable(scrittura_analiticaBulk.getEsercizio_documento_amm()).isPresent())
				.orElse(Boolean.FALSE);
	}

	public boolean isScritturaAnnullata() {
		return Optional.ofNullable(getModel())
				.filter(Scrittura_analiticaBulk.class::isInstance)
				.map(Scrittura_analiticaBulk.class::cast)
				.map(scrittura_analiticaBulk -> Optional.ofNullable(scrittura_analiticaBulk.getPg_scrittura_annullata()).isPresent())
				.orElse(Boolean.FALSE);
	}

    public void setSearchButtonHidden(boolean searchButtonHidden) {
        this.searchButtonHidden = searchButtonHidden;
    }

    public void setFreeSearchButtonHidden(boolean freeSearchButtonHidden) {
        this.freeSearchButtonHidden = freeSearchButtonHidden;
    }

    @Override
    public boolean isSearchButtonHidden() {
        return searchButtonHidden || super.isSearchButtonHidden();
    }

    @Override
    public boolean isFreeSearchButtonHidden() {
        return freeSearchButtonHidden || super.isFreeSearchButtonHidden();
    }
}

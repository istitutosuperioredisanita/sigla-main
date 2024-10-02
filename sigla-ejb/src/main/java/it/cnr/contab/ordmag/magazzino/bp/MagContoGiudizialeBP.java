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

package it.cnr.contab.ordmag.magazzino.bp;

import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;
import it.cnr.contab.doccont00.ejb.ConsConfrontoEntSpeComponentSession;
import it.cnr.contab.ordmag.magazzino.ejb.MagContoGiudizialeComponentSession;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.action.ConsultazioniBP;

import java.rmi.RemoteException;


public class MagContoGiudizialeBP extends ConsultazioniBP {

	public MagContoGiudizialeBP(String s) {
		super(s);
	}
	public MagContoGiudizialeBP() {
		super();
	}
	public MagContoGiudizialeComponentSession createMagContoGiudizialeComponentSession() throws javax.ejb.EJBException, RemoteException {
		return (MagContoGiudizialeComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRDOCCONT00_EJB_MagContoGiudizialeComponentSession", ConsConfrontoEntSpeComponentSession.class);
	}
	
	
	public RemoteIterator select(ActionContext context, CompoundFindClause compoundfindclause, OggettoBulk oggettobulk) throws BusinessProcessException {
		try {
			setFindclause(compoundfindclause);
			//CompoundFindClause clause = new CompoundFindClause(getBaseclause(), compoundfindclause);
			return createMagContoGiudizialeComponentSession().cerca(context.getUserContext(),null,oggettobulk);
		}catch(Throwable e) {
			throw new BusinessProcessException(e);
		}
	}



	public void openIterator(ActionContext context) throws BusinessProcessException {
		try	{
			OggettoBulk model = (OggettoBulk) getBulkInfo().getBulkClass().newInstance();
			setIterator(context,createMagContoGiudizialeComponentSession().cerca(context.getUserContext(),null,model));
		}catch(Throwable e) {
			throw new BusinessProcessException(e);
		}

	}

	/*
	protected void init(it.cnr.jada.action.Config config,ActionContext context) throws BusinessProcessException {
		try {
			Integer esercizio = CNRUserContext.getEsercizio(context.getUserContext());
			Parametri_cnrBulk parCnr = Utility.createParametriCnrComponentSession().getParametriCnr(context.getUserContext(), esercizio); 
			setFlNuovoPdg(parCnr.getFl_nuovo_pdg().booleanValue());

			String cds = CNRUserContext.getCd_cds(context.getUserContext());
			CompoundFindClause clauses = new CompoundFindClause();
			String uo_scrivania = CNRUserContext.getCd_unita_organizzativa(context.getUserContext())+"%";
		  
		    Unita_organizzativaBulk uo = new Unita_organizzativaBulk(uo_scrivania);
		   
			if(!isUoEnte(context) && !uo.isUoCds())	 {					
				clauses.addClause("AND", "esercizio", SQLBuilder.EQUALS, esercizio);
				clauses.addClause("AND", "cds",SQLBuilder.EQUALS, cds);
			}

			if(!isUoEnte(context) && uo.isUoCds())	 {					
				clauses.addClause("AND", "esercizio", SQLBuilder.EQUALS, esercizio);
				clauses.addClause("AND", "cds",SQLBuilder.EQUALS, cds);
			}
			   
			if (isUoEnte(context))
				clauses.addClause("AND", "esercizio", SQLBuilder.EQUALS, esercizio);
				
			setBaseclause(clauses);	
					
			if (getPathConsultazione()==null) {
				setPathConsultazione(this.LIV_BASE);					
				setLivelloConsultazione(this.LIV_BASE);
			} 
				
			super.init(config,context);
			initVariabili(context,null,getPathConsultazione()); 
		} catch (ComponentException e) {
			throw new BusinessProcessException(e);
		} catch (RemoteException e) {
			throw new BusinessProcessException(e);
		} 
	}

	   */
	   
	public boolean isUoEnte(ActionContext context){	
			Unita_organizzativaBulk uo = it.cnr.contab.utenze00.bulk.CNRUserInfo.getUnita_organizzativa(context);
			if (uo.getCd_tipo_unita().equals(it.cnr.contab.config00.sto.bulk.Tipo_unita_organizzativaHome.TIPO_UO_ENTE))
				return true;	
			return false; 
	}	

}
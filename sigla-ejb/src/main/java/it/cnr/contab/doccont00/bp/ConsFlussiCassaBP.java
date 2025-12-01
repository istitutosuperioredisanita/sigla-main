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

package it.cnr.contab.doccont00.bp;


import it.cnr.contab.config00.pdcfin.cla.bulk.Parametri_livelliBulk;
import it.cnr.contab.config00.sto.bulk.CdsBulk;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;
import it.cnr.contab.doccont00.consultazioni.bulk.FlussiDiCassaDtoBulk;
import it.cnr.contab.doccont00.ejb.ConsRiepilogoSiopeComponentSession;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.util.Utility;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.action.Config;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.bulk.ValidationException;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.persistency.sql.CompoundFindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.action.BulkBP;


import java.text.ParseException;


public class ConsFlussiCassaBP extends BulkBP {
	
	public Parametri_livelliBulk parametriLivelli;
	private String descrizioneClassificazione;
	private String livelloConsultazione;
	private String pathConsultazione;
	private CdsBulk cds_scrivania;
	
	public static final String LIV_BASE= "BASE";
	public static final String LIV_BASEDETT= "DETT";
	
		
		public ConsRiepilogoSiopeComponentSession createConsInviatoSiopeComponentSession() throws jakarta.ejb.EJBException,java.rmi.RemoteException {
			return (ConsRiepilogoSiopeComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRDOCCONT00_EJB_ConsRiepilogoSiopeComponentSession", ConsRiepilogoSiopeComponentSession.class);
		}
		
		public ConsRiepilogoSiopeComponentSession createComponentSession() throws jakarta.ejb.EJBException,java.rmi.RemoteException {
			return (ConsRiepilogoSiopeComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRDOCCONT00_EJB_ConsRiepilogoSiopeComponentSession", ConsRiepilogoSiopeComponentSession.class);
		}
		
		public it.cnr.jada.util.jsp.Button[] createToolbar() {
			it.cnr.jada.util.jsp.Button[] toolbar = new it.cnr.jada.util.jsp.Button[1];
			int i = 0;
			toolbar[i++] = new it.cnr.jada.util.jsp.Button(it.cnr.jada.util.Config.getHandler().getProperties(getClass()),"buttons.ricerca");
			return toolbar;
		}

		protected void init(Config config, ActionContext context) throws BusinessProcessException {

				FlussiDiCassaDtoBulk bulk = new FlussiDiCassaDtoBulk();
				CompoundFindClause clauses = new CompoundFindClause();
				Integer esercizio = CNRUserContext.getEsercizio(context.getUserContext());
			    String cds = CNRUserContext.getCd_cds(context.getUserContext());
			    bulk.setROFindCds(false);

				if(!isUoEnte(context))	 {
					clauses.addClause("AND", "esercizio", SQLBuilder.EQUALS, esercizio);
					clauses.addClause("AND", "cds",SQLBuilder.EQUALS, cds);
					bulk.setCds(new CdsBulk(cds));
					try {
						completeSearchTool(context,bulk,bulk.getBulkInfo().getFieldProperty("find_cds"));
					} catch (ValidationException e) {
						e.printStackTrace();
					}
					bulk.setROFindCds(true);
				}
			    
			    if (isUoEnte(context))
					clauses.addClause("AND", "esercizio", SQLBuilder.EQUALS, esercizio);
			    				
				setModel(context,bulk);
				bulk.setEsercizio(esercizio);

			super.init(config, context);
		}
		
		   
		   
		public boolean isUoEnte(ActionContext context){	
			Unita_organizzativaBulk uo = it.cnr.contab.utenze00.bulk.CNRUserInfo.getUnita_organizzativa(context);
			if (uo.getCd_tipo_unita().equals(it.cnr.contab.config00.sto.bulk.Tipo_unita_organizzativaHome.TIPO_UO_ENTE))
				return true;	
			return false; 
		}	
	 
		public void setTitle() {
			
			   String title=null;
			   		   title = "Consultazione Flussi di cassa";
			
				getBulkInfo().setShortDescription(title);
			}	

		public boolean isRicercaButtonEnabled()
		{
			return true;
		}
		

		public RemoteIterator find(ActionContext actionContext, CompoundFindClause clauses, OggettoBulk bulk, OggettoBulk context, String property) throws BusinessProcessException {
			try {
				return it.cnr.jada.util.ejb.EJBCommonServices.openRemoteIterator(actionContext,createComponentSession().cerca(actionContext.getUserContext(),clauses,bulk,context,property));
			} catch(Exception e) {
				throw new BusinessProcessException(e);
			}
		}

		public CdsBulk getCds_scrivania() {
			return cds_scrivania;
		}

		public void setCds_scrivania(CdsBulk cds_scrivania) {
			this.cds_scrivania = cds_scrivania;
		}

		public void validaRichiestaFlussi(FlussiDiCassaDtoBulk flussoCassa) throws ApplicationException {

			if(flussoCassa.getEsercizio() == null){
				throw new ApplicationException("Attenzione!Impostare esercizio");
			}
			if(flussoCassa.getTrimestre() == null){
				throw new ApplicationException("Attenzione!Impostare un Trimestre");
			}
			else{
				impostaDataEmissioneDaTrimestre(flussoCassa);
			}
			if(flussoCassa.getTipoFlusso() == null){
				throw new ApplicationException("Attenzione!Impostare una tipologia di Flusso");
			}
			if(flussoCassa.getLivello()== null){
				throw new ApplicationException("Attenzione!Impostare un Livello di estrazione");
			}

		}
		private void impostaDataEmissioneDaTrimestre(FlussiDiCassaDtoBulk flussoCassa) throws ApplicationException {
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");

			try {
				flussoCassa.setDtEmissioneDa(new java.sql.Timestamp(sdf.parse("01/01/"+flussoCassa.getEsercizio()).getTime()));

				if(flussoCassa.getTrimestre().equals(FlussiDiCassaDtoBulk.PRIMO)){
					// Fino al 31 Marzo
					flussoCassa.setDtEmissioneA(new java.sql.Timestamp(sdf.parse("31/03/"+flussoCassa.getEsercizio()).getTime()));

				}else if(flussoCassa.getTrimestre().equals(FlussiDiCassaDtoBulk.SECONDO)){
					// Fino al 30 Giugno
					flussoCassa.setDtEmissioneA(new java.sql.Timestamp(sdf.parse("30/06/"+flussoCassa.getEsercizio()).getTime()));
				}
				else if(flussoCassa.getTrimestre().equals(FlussiDiCassaDtoBulk.TERZO)){
					// Fino al 30 Settembre
					flussoCassa.setDtEmissioneA(new java.sql.Timestamp(sdf.parse("30/09/"+flussoCassa.getEsercizio()).getTime()));
				}
				else{
					// Fino al 31 Dicembre
					flussoCassa.setDtEmissioneA(new java.sql.Timestamp(sdf.parse("31/12/"+flussoCassa.getEsercizio()).getTime()));
				}


			} catch (ParseException e) {
				throw new ApplicationException("Attenzione! Riscontrato errore su impostazione della data emissione");
			}
		}

	}

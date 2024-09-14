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

package it.cnr.contab.web.rest.resource.anagraf00;

import it.cnr.contab.anagraf00.core.bulk.*;
import it.cnr.contab.anagraf00.ejb.TerzoComponentSession;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.web.rest.exception.RestException;
import it.cnr.contab.web.rest.local.anagraf00.TerzoLocal;
import it.cnr.contab.web.rest.model.AnagraficaInfoDTO;
import it.cnr.contab.web.rest.model.DettaglioModalitaPagDto;
import it.cnr.contab.web.rest.model.ModalitaPagamentoDto;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkList;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.ejb.CRUDComponentSession;
import it.cnr.jada.persistency.PersistencyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Stateless
public class TerzoResource implements TerzoLocal {
    private final Logger LOGGER = LoggerFactory.getLogger(TerzoResource.class);
	@Context SecurityContext securityContext;
	@EJB CRUDComponentSession crudComponentSession;
	@EJB TerzoComponentSession terzoComponentSession;

    public Response update(@Context HttpServletRequest request, TerzoBulk terzoBulk) throws Exception {
    	CNRUserContext userContext = (CNRUserContext) securityContext.getUserPrincipal();
		Optional.ofNullable(terzoBulk.getCd_terzo()).orElseThrow(() -> new RestException(Status.BAD_REQUEST, "Errore, indicare il codice terzo."));
    	
    	TerzoBulk terzoDB = getTerzo(userContext, terzoBulk.getCd_terzo());
		Optional.ofNullable(terzoDB).orElseThrow(() -> new RestException(Status.BAD_REQUEST, "Errore, il codice terzo indicato "+terzoBulk.getCd_terzo()+" non esiste"));
		Optional.ofNullable(terzoBulk.getCodiceDestinatarioFatt()).orElseGet(() -> Optional.ofNullable(terzoBulk.getPecForRest()).orElseThrow(() -> new RestException(Status.BAD_REQUEST, "Errore, indicare almeno la pec o il codice destinatario fattura.")));
    	
    	terzoDB = (TerzoBulk)terzoComponentSession.inizializzaBulkPerModifica(userContext, terzoDB);
    	terzoDB.setCodiceDestinatarioFatt(Optional.ofNullable(terzoBulk.getCodiceDestinatarioFatt()).orElse(terzoDB.getCodiceDestinatarioFatt()));
    	if (terzoBulk.getPecForRest() != null && !terzoBulk.getPecForRest().isEmpty()){
    		String newPec = terzoBulk.getPecForRest();
    		Boolean pecEsistente = false;
    		for (java.util.Iterator i = terzoDB.getPec().iterator(); i.hasNext();){
    			TelefonoBulk emailPec = (TelefonoBulk)i.next();
    			if (emailPec.getRiferimento().equalsIgnoreCase(newPec)){
    				pecEsistente = true;
    				emailPec.setFattElettronica(true);
    				emailPec.setToBeUpdated();
    			} else if (emailPec.getFattElettronica()){
    				emailPec.setFattElettronica(false);
    				emailPec.setToBeUpdated();
    			}
    		}    		
    		
    		if (!pecEsistente){
        		TelefonoBulk telefonoBulk = new TelefonoBulk();
        		telefonoBulk.setToBeCreated();
        		telefonoBulk.setRiferimento(terzoBulk.getPecForRest());
        		telefonoBulk.setDs_riferimento("INDIRIZZO PEC PER FATTURAZIONE ELETTRONICA");
        		telefonoBulk.setFattElettronica(true);
        		terzoDB.addToPec(telefonoBulk);
    		}
    	}
    	terzoDB.setToBeUpdated();
    	
    	terzoDB = (TerzoBulk)terzoComponentSession.modificaConBulk(userContext, terzoDB);
    	return Response.status(Status.OK).entity(terzoBulk).build();
    }



	@Override
	public Response get( Integer cd_terzo) throws Exception {
		CNRUserContext userContext = (CNRUserContext) securityContext.getUserPrincipal();
		TerzoBulk terzoDB = getTerzo(userContext, cd_terzo);
		return Response.status(Status.OK).entity(terzoDB).build();
	}



	@Override
	public Response getList( String codicefiscale) throws Exception {
		CNRUserContext userContext = (CNRUserContext) securityContext.getUserPrincipal();
		Optional.ofNullable(codicefiscale).orElseThrow(() -> new RestException(Status.BAD_REQUEST, "Errore, indicare il codice fiscale."));
		final List<AnagraficoBulk> anagraficoBulks = crudComponentSession.find(userContext, AnagraficoBulk.class, "findByCodiceFiscaleOrPartitaIVA", codicefiscale, null);
		final AnagraficoBulk anagraficoBulk = anagraficoBulks.stream().findAny().orElseThrow(() -> new RestException(Status.NOT_FOUND, "Errore, nessun anagrafico trovato per il codice fiscale indicato."));

		return Response.status(Response.Status.OK).entity(
				crudComponentSession.find(userContext, TerzoBulk.class, "findTerzi", anagraficoBulk)
		).build();

	}

	@Override
	public Response tipoRapporto(String codicefiscale) throws Exception {
		CNRUserContext userContext = (CNRUserContext) securityContext.getUserPrincipal();
		Optional.ofNullable(codicefiscale).orElseThrow(() -> new RestException(Status.BAD_REQUEST, "Errore, indicare il codice fiscale."));
		final List<AnagraficoBulk> anagraficoBulks = crudComponentSession.find(userContext, AnagraficoBulk.class, "findByCodiceFiscaleOrPartitaIVA", codicefiscale, null);
		return Response.status(Status.OK).entity(
			crudComponentSession.find(
					userContext,
					AnagraficoBulk.class,
					"findRapporti",
					anagraficoBulks.stream().findAny().orElseThrow(() -> new RestException(Status.BAD_REQUEST, "Errore, nessun anagrafico trovato per il codice fiscale indicato."))
			)
		).build();
	}
	public Response anagraficaInfo(String codicefiscale) throws Exception {
		CNRUserContext userContext = (CNRUserContext) securityContext.getUserPrincipal();
		Optional.ofNullable(codicefiscale).orElseThrow(() -> new RestException(Status.BAD_REQUEST, "Errore, indicare il codice fiscale."));
		final List<AnagraficoBulk> anagraficoBulks = crudComponentSession.find(userContext, AnagraficoBulk.class, "findByCodiceFiscaleOrPartitaIVA", codicefiscale, null);
		final AnagraficoBulk anagraficoBulk = anagraficoBulks.stream().findAny().orElseThrow(() -> new RestException(Status.NOT_FOUND, "Errore, nessun anagrafico trovato per il codice fiscale indicato."));
		return Response.status(Status.OK).entity(
				new AnagraficaInfoDTO(anagraficoBulk)
		).build();
	}
	public Response anagraficaInfoByCdTerzo(Integer cdTerzo) throws Exception {
		CNRUserContext userContext = (CNRUserContext) securityContext.getUserPrincipal();
		Optional.ofNullable(cdTerzo).orElseThrow(() -> new RestException(Status.BAD_REQUEST, "Errore, indicare il codice terzo."));

		TerzoBulk terzoDB = getTerzo(userContext, cdTerzo);
		Optional.ofNullable(terzoDB).orElseThrow(() -> new RestException(Status.BAD_REQUEST, "Errore, il codice terzo indicato "+cdTerzo+" non esiste"));

		final AnagraficoBulk anagraficoBulk = ( AnagraficoBulk) crudComponentSession.findByPrimaryKey(userContext, new AnagraficoBulk(terzoDB.getCd_anag()));
		Optional.ofNullable(anagraficoBulk).orElseThrow(() -> new RestException(Status.BAD_REQUEST, "Errore, nessun anagrafico trovato il codice terzo indicato "+cdTerzo));
		return Response.status(Status.OK).entity(
				new AnagraficaInfoDTO(anagraficoBulk)
		).build();
	}

	private List<DettaglioModalitaPagDto> getDettaglioMOdalitaPagDto(List<BancaBulk> banche){
		return ( List<DettaglioModalitaPagDto>)Optional.ofNullable( banche).
				orElse( new ArrayList<BancaBulk>()).stream().map(s->{
			return new DettaglioModalitaPagDto(s);
		}).collect(Collectors.toList());

	}
	private List<ModalitaPagamentoDto> getModalitaPagamentoDto( TerzoBulk terzo){

		return ( List<ModalitaPagamentoDto>) ( Optional.ofNullable(terzo)).map( s->s.getModalita_pagamento()).
				orElse(new BulkList<Modalita_pagamentoBulk>()).stream().map(modalitaPagamentoBulk -> {
					return new ModalitaPagamentoDto(modalitaPagamentoBulk,getDettaglioMOdalitaPagDto( terzo.getBanche(modalitaPagamentoBulk)));
				}).collect(Collectors.toList());
	}
	@Override
	public Response modalitaPagamentoByCdTerzo(Integer cdTerzo) throws Exception {
		Optional.ofNullable(cdTerzo).orElseThrow(() -> new RestException(Status.BAD_REQUEST, "Errore, indicare il codice terzo."));
		CNRUserContext userContext = (CNRUserContext) securityContext.getUserPrincipal();
		TerzoBulk terzoDB = getTerzo(userContext, cdTerzo);
		Optional.ofNullable(terzoDB).orElseThrow(() -> new RestException(Status.BAD_REQUEST, "Errore, il codice terzo indicato "+cdTerzo+" non esiste"));
		terzoDB= ( TerzoBulk) terzoComponentSession.inizializzaBulkPerModifica(userContext,terzoDB);
		List<ModalitaPagamentoDto> getModalitaPagamentoDto = getModalitaPagamentoDto( terzoDB);

		return Response.status(Status.OK).entity(
				getModalitaPagamentoDto( terzoDB)
		).build();
	}


	private TerzoBulk getTerzo(UserContext userContext, Integer cdTerzo) throws PersistencyException, ComponentException, RemoteException, EJBException {
		TerzoBulk terzoBulk = new TerzoBulk();
		terzoBulk.setCd_terzo(cdTerzo);
		terzoBulk = (TerzoBulk)crudComponentSession.findByPrimaryKey(userContext, terzoBulk);
		return terzoBulk;
	}

}
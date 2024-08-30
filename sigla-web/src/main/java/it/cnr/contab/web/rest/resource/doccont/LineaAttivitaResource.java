package it.cnr.contab.web.rest.resource.doccont;

import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.contab.anagraf00.core.bulk.TerzoKey;
import it.cnr.contab.config00.ejb.Linea_attivitaComponentSession;
import it.cnr.contab.config00.latt.bulk.*;
import it.cnr.contab.config00.pdcfin.bulk.FunzioneBulk;
import it.cnr.contab.config00.pdcfin.bulk.FunzioneKey;
import it.cnr.contab.config00.pdcfin.bulk.NaturaBulk;
import it.cnr.contab.config00.pdcfin.bulk.NaturaKey;
import it.cnr.contab.config00.sto.bulk.CdrBulk;
import it.cnr.contab.config00.sto.bulk.CdrKey;
import it.cnr.contab.prevent01.bulk.Pdg_missioneBulk;
import it.cnr.contab.prevent01.bulk.Pdg_missioneKey;
import it.cnr.contab.progettiric00.core.bulk.ProgettoBulk;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.web.rest.exception.RestException;
import it.cnr.contab.web.rest.local.config00.LineaAttivitaLocal;
import it.cnr.contab.web.rest.model.EnumTiGestioneLineaAttivita;
import it.cnr.contab.web.rest.model.LineaAttivitaDto;
import it.cnr.contab.web.rest.model.ProgettoDto;
import it.cnr.contab.web.rest.model.UpdateLineaAttivitaDto;
import it.cnr.jada.bulk.ROWrapper;
import it.cnr.jada.bulk.ValidationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.ejb.CRUDComponentSession;
import it.cnr.jada.persistency.PersistencyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.rmi.RemoteException;
import java.util.Optional;

@Stateless
public class LineaAttivitaResource implements LineaAttivitaLocal {

    private final Logger LOGGER = LoggerFactory.getLogger(LineaAttivitaResource.class);
    @Context
    SecurityContext securityContext;
    @EJB
    CRUDComponentSession crudComponentSession;
    @EJB
    Linea_attivitaComponentSession lineaAttivitaComponentSession;

    private void validateLineaAttivita( HttpServletRequest request, LineaAttivitaDto lineaAttivita){
        if ( !Optional.ofNullable(lineaAttivita.getTi_gestione()).isPresent())
            throw new RestException(Response.Status.BAD_REQUEST,String.format("La tipologia di Gae ( Spesa, Entrata, Entrambe) è obbligatoria"));
        if ( !Optional.ofNullable(lineaAttivita.getEsercizio_inizio()).isPresent())
            throw new RestException(Response.Status.BAD_REQUEST,String.format("L'eserizio di inizio è obbligatorio"));
        if ( !Optional.ofNullable(lineaAttivita.getEsercizio_fine()).isPresent())
            throw new RestException(Response.Status.BAD_REQUEST,String.format("L'eserizio di fine è obbligatorio"));
        if ( !Optional.ofNullable(lineaAttivita.getCd_linea_attivita()).isPresent())
            throw new RestException(Response.Status.BAD_REQUEST,String.format("Il codice linea attività è obbligatorio"));
        if ( !Optional.ofNullable(lineaAttivita.getCentro_responsabilitaKey()).isPresent()||
                !Optional.ofNullable(lineaAttivita.getCentro_responsabilitaKey().getCd_centro_responsabilita()).isPresent())
            throw new RestException(Response.Status.BAD_REQUEST,String.format("Il centro di responsabilità è obbligatorio"));
        if ( !Optional.ofNullable(lineaAttivita.getProgettoKey()).isPresent())
            throw new RestException(Response.Status.BAD_REQUEST,String.format("Il Progetto è obbligatorio"));
        if ( !Optional.ofNullable(lineaAttivita.getProgettoKey().getPg_progetto()).isPresent())
            throw new RestException(Response.Status.BAD_REQUEST,String.format("Il progressivo del Progetto è obbligatorio"));
        if ( !Optional.ofNullable(lineaAttivita.getProgettoKey().getEsercizio()).isPresent())
            throw new RestException(Response.Status.BAD_REQUEST,String.format("L'esercizio del Progetto è obbligatorio"));
        if ( lineaAttivita.getTi_gestione().isSpesa()) {
            if (!Optional.ofNullable(lineaAttivita.getCofogKey()).isPresent() ||
                    !Optional.ofNullable(lineaAttivita.getCofogKey().getCd_cofog()).isPresent())
                throw new RestException(Response.Status.BAD_REQUEST, String.format("Il cofog è obbligatorio"));
            if ( !Optional.ofNullable(lineaAttivita.getFunzioneKey()).isPresent()||
                    !Optional.ofNullable(lineaAttivita.getFunzioneKey().getCd_funzione()).isPresent())
                throw new RestException(Response.Status.BAD_REQUEST,String.format("La Funzione è obbligatoria"));
        }
        //Controllo per gae di tipo Solo entrata
        if (EnumTiGestioneLineaAttivita.ENTRATA.getGestione().equals(lineaAttivita.getTi_gestione().getGestione())) {
            if (Optional.ofNullable(lineaAttivita.getCofogKey()).isPresent() &&
                    Optional.ofNullable(lineaAttivita.getCofogKey().getCd_cofog()).isPresent())
                throw new RestException(Response.Status.BAD_REQUEST, String.format("Il cofog non è permesso per linee Attività di solo entrata"));
            if ( Optional.ofNullable(lineaAttivita.getFunzioneKey()).isPresent()&&
                    Optional.ofNullable(lineaAttivita.getFunzioneKey().getCd_funzione()).isPresent())
                throw new RestException(Response.Status.BAD_REQUEST,String.format("la Funzione non è permesso per linee Attività di solo entrata"));
        }
        if ( !Optional.ofNullable(lineaAttivita.getNaturaKey()).isPresent()||
                !Optional.ofNullable(lineaAttivita.getNaturaKey().getCd_natura()).isPresent())
            throw new RestException(Response.Status.BAD_REQUEST,String.format("La natura è obbligatoria"));
    }
    //da sosituire MapStruct
    protected LineaAttivitaDto workpackageBulkToLineAttivitaDto( WorkpackageBulk workpackageBulk){
        LineaAttivitaDto lineaAttivitaDto= new LineaAttivitaDto();
            lineaAttivitaDto.setEsercizio_inizio(workpackageBulk.getEsercizio_inizio());
            lineaAttivitaDto.setEsercizio_fine(workpackageBulk.getEsercizio_fine());
            lineaAttivitaDto.setCd_linea_attivita(workpackageBulk.getCd_linea_attivita());
            lineaAttivitaDto.setDenominazione(workpackageBulk.getDenominazione());
            lineaAttivitaDto.setDs_linea_attivita(workpackageBulk.getDs_linea_attivita());
            lineaAttivitaDto.setTi_gestione(EnumTiGestioneLineaAttivita.getValueFrom(workpackageBulk.getTi_gestione()));


            Optional.ofNullable(workpackageBulk.getGruppo_linea_attivita()).ifPresent(e-> {
                lineaAttivitaDto.setGruppoLineaAttivitaKey(new Gruppo_linea_attivitaKey(workpackageBulk.getGruppo_linea_attivita().getCd_gruppo_linea_attivita()));
            });
            Optional.ofNullable(workpackageBulk.getResponsabile()).ifPresent(e-> {
                lineaAttivitaDto.setResponsabileKey(new TerzoKey( workpackageBulk.getResponsabile().getCd_terzo()));
            });
            Optional.ofNullable(workpackageBulk.getFunzione()).ifPresent(e-> {
                lineaAttivitaDto.setFunzioneKey(new FunzioneKey(workpackageBulk.getFunzione().getCd_funzione()));
            });
            Optional.ofNullable(workpackageBulk.getNatura()).ifPresent(e-> {
                lineaAttivitaDto.setNaturaKey(new NaturaKey(workpackageBulk.getNatura().getCd_natura()));
            });
            Optional.ofNullable(workpackageBulk.getCentro_responsabilita()).ifPresent(e-> {
                lineaAttivitaDto.setCentro_responsabilitaKey(new CdrKey(workpackageBulk.getCd_centro_responsabilita()));
            });
            Optional.ofNullable(workpackageBulk.getProgetto2016()).ifPresent(e-> {
                lineaAttivitaDto.setProgettoKey(new ProgettoDto(e.getEsercizio(),e.getPg_progetto()));
            });
            Optional.ofNullable(workpackageBulk.getInsieme_la()).ifPresent(e-> {
                lineaAttivitaDto.setInsieme_laKey(new Insieme_laKey(
                                workpackageBulk.getInsieme_la().getCd_centro_responsabilita(),
                                workpackageBulk.getCd_insieme_la())
                );
            });
            Optional.ofNullable(workpackageBulk.getCofog()).ifPresent(e-> {
                lineaAttivitaDto.setCofogKey( new CofogKey( workpackageBulk.getCofog().getCd_cofog()));
            });
            Optional.ofNullable(workpackageBulk.getPdgMissione()).ifPresent(e-> {
                lineaAttivitaDto.setPdgMissioneKey(new Pdg_missioneKey(workpackageBulk.getPdgMissione().getCd_missione()));
            });
        return lineaAttivitaDto;

    }
    protected WorkpackageBulk lineaAttivitaDtoTodWorkpackageBulk(LineaAttivitaDto lineaAttivita, CNRUserContext userContext) throws PersistencyException, ValidationException, ComponentException, RemoteException {
        WorkpackageBulk workpackageBulk = new WorkpackageBulk();
        workpackageBulk.setFl_limite_ass_obblig(Boolean.TRUE);
        workpackageBulk = (WorkpackageBulk) lineaAttivitaComponentSession.inizializzaBulkPerInserimento(
                userContext,
                workpackageBulk);

        workpackageBulk.setCentro_responsabilita(new CdrBulk(lineaAttivita.getCentro_responsabilitaKey().getCd_centro_responsabilita()));
        workpackageBulk.setCd_linea_attivita(lineaAttivita.getCd_linea_attivita());
        workpackageBulk.setDenominazione(lineaAttivita.getDenominazione());
        workpackageBulk.setGruppo_linea_attivita(new Gruppo_linea_attivitaBulk(Optional.ofNullable(lineaAttivita.getGruppoLineaAttivitaKey()).map(Gruppo_linea_attivitaKey::getCd_gruppo_linea_attivita).orElse("")));

        workpackageBulk.setFunzione(new FunzioneBulk(lineaAttivita.getFunzioneKey().getCd_funzione()));
        workpackageBulk.setCofog( new CofogBulk(lineaAttivita.getCofogKey().getCd_cofog()));

        workpackageBulk.setNatura(new NaturaBulk(lineaAttivita.getNaturaKey().getCd_natura()));
        workpackageBulk.setDs_linea_attivita(lineaAttivita.getDs_linea_attivita());
        workpackageBulk.setEsercizio_fine(lineaAttivita.getEsercizio_fine());
        workpackageBulk.setEsercizio_inizio(lineaAttivita.getEsercizio_inizio());
        workpackageBulk.setTi_gestione(lineaAttivita.getTi_gestione().gestione);
        workpackageBulk.setProgetto2016(new ProgettoBulk(lineaAttivita.getProgettoKey().getEsercizio(),lineaAttivita.getProgettoKey().getPg_progetto(), ProgettoBulk.TIPO_FASE_NON_DEFINITA));

        workpackageBulk.setResponsabile(new TerzoBulk(lineaAttivita.getResponsabileKey().getCd_terzo()));
        //workpackageBulk.setFl_limite_ass_obblig(lineaAttivita.getFl_limite_ass_obblig());

        workpackageBulk.setPdgMissione( new Pdg_missioneBulk(lineaAttivita.getPdgMissioneKey().getCd_missione()));
        return workpackageBulk;
    }
    protected WorkpackageBulk mergeWorkpageBulkUpdateLineaAttiviti(UpdateLineaAttivitaDto updateLineaAttivitaDto, WorkpackageBulk workpackageBulk) throws PersistencyException, ValidationException, ComponentException, RemoteException {
       if ( !Optional.ofNullable(updateLineaAttivitaDto).isPresent())
           return workpackageBulk;
        Optional.ofNullable(updateLineaAttivitaDto.getEsercizio_fine()).ifPresent(e-> {
            workpackageBulk.setEsercizio_fine(e);
        });
        Optional.ofNullable(updateLineaAttivitaDto.getDs_linea_attivita()).ifPresent(e-> {
            workpackageBulk.setDs_linea_attivita(e);
        });
        Optional.ofNullable(updateLineaAttivitaDto.getDenominazione()).ifPresent(e-> {
            workpackageBulk.setDenominazione(e);
        });
        return workpackageBulk;
    }
    //fine da sosituire MapStruct
    protected WorkpackageBulk creaLineaAttivitaSigla( WorkpackageBulk workpackageBulk, CNRUserContext userContext) throws ComponentException, RemoteException,PersistencyException {

            workpackageBulk.setToBeCreated();
            return lineaAttivitaComponentSession.creaLineaAttivitaWs(userContext,workpackageBulk);
    }
    @Override
    public Response insert(HttpServletRequest request, LineaAttivitaDto lineaAttivita) throws Exception {

        CNRUserContext userContext = (CNRUserContext) securityContext.getUserPrincipal();
        validateLineaAttivita(request, lineaAttivita);
        WorkpackageBulk workpackageBulk= lineaAttivitaDtoTodWorkpackageBulk(lineaAttivita,userContext);
        try {
            return Response.status(Response.Status.CREATED).entity(workpackageBulkToLineAttivitaDto( creaLineaAttivitaSigla(workpackageBulk, userContext))).build();
        }catch (Throwable e){
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR,String.format(e.getMessage()));
        }
    }

    @Override
    public Response get(String cd_centro_responsabilita, String cd_linea_attivita) throws Exception {
        try{
            CNRUserContext userContext = (CNRUserContext) securityContext.getUserPrincipal();
            WorkpackageBulk w= ( WorkpackageBulk) lineaAttivitaComponentSession.findByPrimaryKey(userContext, new WorkpackageBulk(cd_centro_responsabilita,cd_linea_attivita));
            if ( !Optional.ofNullable(w).isPresent())
                throw new RestException(Response.Status.NOT_FOUND,String.format("Linea Attività non presente!"));
            return Response.status(Response.Status.OK).entity(workpackageBulkToLineAttivitaDto( w )).build();
        }catch (Throwable e){
            if ( e instanceof RestException)
                throw e;
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR,String.format(e.getMessage()));
        }
    }

    @Override
    public Response delete(String cd_centro_responsabilita, String cd_linea_attivita) throws Exception {
        try{
            CNRUserContext userContext = (CNRUserContext) securityContext.getUserPrincipal();
                Boolean result=lineaAttivitaComponentSession.deleteLineaAttivitaWs(userContext,cd_centro_responsabilita,cd_linea_attivita);
                if ( result)
                    return Response.status(Response.Status.OK).build();

                return Response.status(Response.Status.NO_CONTENT).build();

        }catch (Throwable e){
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR,String.format(e.getMessage()));
        }
    }

    @Override
    public Response update(String cd_centro_responsabilita, String cd_linea_attivita, UpdateLineaAttivitaDto updateLineaAttivitaDto) throws Exception {
        try{
            CNRUserContext userContext = (CNRUserContext) securityContext.getUserPrincipal();
            WorkpackageBulk workpackageBulk = ( WorkpackageBulk) lineaAttivitaComponentSession.findByPrimaryKey(userContext,new WorkpackageBulk(cd_centro_responsabilita,cd_linea_attivita));
            if ( !Optional.ofNullable(workpackageBulk).isPresent())
                throw new RestException(Response.Status.NOT_FOUND,String.format("Non esiste la Linea Attivita per centro Responsabilita ".
                        concat(cd_linea_attivita).
                        concat(" cd linea attivita").
                        concat("cd_linea_attivita")));


            Object result =  lineaAttivitaComponentSession.inizializzaBulkPerModifica(userContext,workpackageBulk);
            if ( result instanceof ROWrapper)
                throw new RestException(Response.Status.BAD_REQUEST,String.format(((ROWrapper)result).getMtu().getMessage()));
            workpackageBulk= ( WorkpackageBulk ) result;
            workpackageBulk=mergeWorkpageBulkUpdateLineaAttiviti( updateLineaAttivitaDto,workpackageBulk);
            workpackageBulk.setToBeUpdated();
            workpackageBulk = ( WorkpackageBulk) lineaAttivitaComponentSession.modificaLineaAttivitaWs(userContext,workpackageBulk);
            return Response.status(Response.Status.OK).entity(workpackageBulkToLineAttivitaDto(workpackageBulk)).build();
        }catch (Throwable e){
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR,String.format(e.getMessage()));
        }
    }

}

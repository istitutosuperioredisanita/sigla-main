package it.cnr.contab.web.rest.resource.config00;

import it.cnr.contab.doccont00.core.bulk.ObbligazioneBulk;
import it.cnr.contab.doccont00.ejb.ObbligazioneComponentSession;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.web.rest.exception.RestException;
import it.cnr.contab.web.rest.local.config00.ObbligazioneLocal;
import it.cnr.contab.web.rest.model.ObbligazioneDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@Stateless
public class ObbligazioneResource implements ObbligazioneLocal {
    private final Logger LOGGER = LoggerFactory.getLogger(LineaAttivitaResource.class);
    @Context
    SecurityContext securityContext;

    @EJB
    ObbligazioneComponentSession obbligazioneComponentSession;
    @Override
    public Response insert(HttpServletRequest request, ObbligazioneDto obbligazioneDto) throws Exception {
        throw new RestException(Response.Status.METHOD_NOT_ALLOWED,String.format("La Funzione non è implemetata"));
    }

    @Override
    public Response get(String cd_cds, Integer esercizio, Long pg_obbligazione, Integer esercizio_originale) throws Exception {
        try{
            CNRUserContext userContext = (CNRUserContext) securityContext.getUserPrincipal();
            ObbligazioneBulk obbligazioneBulk= ( ObbligazioneBulk) obbligazioneComponentSession.findObbligazione(userContext, new ObbligazioneBulk(cd_cds,esercizio,esercizio_originale,pg_obbligazione));
            obbligazioneBulk= ( ObbligazioneBulk)obbligazioneComponentSession.inizializzaBulkPerModifica(userContext,obbligazioneBulk);
            return Response.status(Response.Status.OK).entity(new ObbligazioneDto( )).build();
        }catch (Throwable e){
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR,String.format(e.getMessage()));
        }
    }

    @Override
    public Response delete(String cd_cds, Integer esercizio, Long pg_obbligazione, Integer esercizio_originale) throws Exception {
        throw new RestException(Response.Status.METHOD_NOT_ALLOWED,String.format("La Funzione non è implemetata"));
    }

    @Override
    public Response update(String cd_cds, Integer esercizio, Long pg_obbligazione, Integer esercizio_originale) throws Exception {
        throw new RestException(Response.Status.METHOD_NOT_ALLOWED,String.format("La Funzione non è implemetata"));
    }
}

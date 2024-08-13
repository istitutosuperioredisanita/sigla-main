package it.cnr.contab.web.rest.resource.config00;

import it.cnr.contab.client.docamm.LineaAttivita;
import it.cnr.contab.config00.ejb.Linea_attivitaComponentSession;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.web.rest.local.config00.LineaAttivitaLocal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public class LineaAttivitaResource implements LineaAttivitaLocal {

    private final Logger LOGGER = LoggerFactory.getLogger(LineaAttivitaResource.class);
    @Context
    SecurityContext securityContext;
    @EJB
    Linea_attivitaComponentSession lineaAttivitaComponentSession;

    @Override
    public Response insert(HttpServletRequest request, LineaAttivita lineaAttivita) throws Exception {
        CNRUserContext userContext = (CNRUserContext) securityContext.getUserPrincipal();
        //WorkpackageBulk workpackageBulk = new WorkpackageBulk(lineaAttivita.getCd_centro_responsabilita(),lineaAttivita.getCd_linea_attivita());
        //workpackageBulk.setCd_tipo_linea_attivita(lineaAttivita.getCd_tipo_linea_attivita());
        //workpackageBulk.setEsercizio_inizio(lineaAttivita.getEsercizio_inizio());
        //workpackageBulk.setEsercizio_fine(lineaAttivita.getEsercizio_fine());
        //lineaAttivitaComponentSession.creaConBulk(userContext,workpackageBulk);
        return Response.status(Response.Status.OK).entity(lineaAttivita).build();
    }

}

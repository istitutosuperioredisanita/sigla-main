package it.cnr.contab.web.rest.local.inventario01;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import it.cnr.contab.web.rest.config.SIGLARoles;
import it.cnr.contab.web.rest.config.SIGLASecurityContext;
import it.cnr.contab.web.rest.model.DocTrasportoRientroDTOBulk;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Local
@Path("/docTrasportoRientro")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
//@RolesAllowed(SIGLARoles.DOC_T_R) // Ruolo necessario per l’operazione
@Api("Documento Trasporto/Rientro")
public interface DocTrasportoRientroLocal {

    /**
     * Inserisce un documento di Trasporto (T) o Rientro (R).
     * // Accesso previsto solo a utenti con ruolo DOC_T_R
     */
    @POST
    @Valid
    @ApiOperation(
            value = "Inserisce un documento di Trasporto o Rientro",
            notes = "Crea un nuovo documento in stato INSERITO.",
            response = DocTrasportoRientroDTOBulk.class,
            authorizations = {
                    @Authorization("BASIC"),
                    @Authorization(SIGLASecurityContext.X_SIGLA_ESERCIZIO),
                    @Authorization(SIGLASecurityContext.X_SIGLA_CD_CDS),
                    @Authorization(SIGLASecurityContext.X_SIGLA_CD_UNITA_ORGANIZZATIVA),
                    @Authorization(SIGLASecurityContext.X_SIGLA_CD_CDR)
            }
    )
    Response insertDocTrasportoRientro(
            @Context HttpServletRequest request,
            @Valid DocTrasportoRientroDTOBulk dto
    ) throws Exception;
}
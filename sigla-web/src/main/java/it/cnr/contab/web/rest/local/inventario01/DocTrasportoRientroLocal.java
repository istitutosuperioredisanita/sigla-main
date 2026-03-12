package it.cnr.contab.web.rest.local.inventario01;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.web.rest.config.SIGLARoles;
import it.cnr.contab.web.rest.config.SIGLASecurityContext;

import javax.annotation.security.RolesAllowed;
import javax.ejb.Local;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

@Local
@Path("/docTrasportoRientro")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed(SIGLARoles.DOC_T_R)
@Api("Documento Trasporto/Rientro")
public interface DocTrasportoRientroLocal {

    /**
     * Crea un nuovo documento di Trasporto o Rientro in stato INSERITO.
     */
    @POST
    @RolesAllowed(SIGLARoles.DOC_T_R)
    @ApiOperation(
            value = "Inserisce un documento di Trasporto o Rientro",
            notes = "Crea un nuovo documento in stato INSERITO",
            response = Doc_trasporto_rientroBulk.class,
            authorizations = {
                    @Authorization("BASIC"),
                    @Authorization(SIGLASecurityContext.X_SIGLA_ESERCIZIO),
                    @Authorization(SIGLASecurityContext.X_SIGLA_CD_CDS),
                    @Authorization(SIGLASecurityContext.X_SIGLA_CD_UNITA_ORGANIZZATIVA),
                    @Authorization(SIGLASecurityContext.X_SIGLA_CD_CDR)
            }
    )
    Response saveDocTR(
            @Context HttpServletRequest request,
            @Valid Map<String, Object> body
    ) throws Exception;

    /**
     * Cerca un documento di Trasporto/Rientro per bene, tipo e stato.
     */
    @GET
    @RolesAllowed(SIGLARoles.DOC_T_R)
    @Path("/cerca")
    @ApiOperation(
            value = "Cerca documenti di Trasporto o Rientro",
            notes = "Restituisce il documento che contiene il bene specificato",
            authorizations = {
                    @Authorization("BASIC"),
                    @Authorization(SIGLASecurityContext.X_SIGLA_ESERCIZIO),
                    @Authorization(SIGLASecurityContext.X_SIGLA_CD_CDS),
                    @Authorization(SIGLASecurityContext.X_SIGLA_CD_UNITA_ORGANIZZATIVA)
            }
    )
    Response cercaDocTrasportoRientro(
            @Context HttpServletRequest request,
            @Valid @BeanParam Doc_trasporto_rientroBulk filtro
    ) throws Exception;
}
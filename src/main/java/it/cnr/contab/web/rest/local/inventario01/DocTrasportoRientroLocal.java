package it.cnr.contab.web.rest.local.inventario01;


import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.web.rest.config.SIGLARoles;
import it.cnr.contab.web.rest.config.SIGLASecurityContext;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Local;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.Map;

@Local
@Path("/docTrasportoRientro")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed(SIGLARoles.DOC_T_R)
@Tag(name = "Contratti Maggioli")
public interface DocTrasportoRientroLocal {

    /**
     * Crea un nuovo documento di Trasporto o Rientro in stato INSERITO.
     */

    @POST
    @Operation(summary = "Inserisce un documento di Trasporto o Rientro un contratto",
            description = "Crea un nuovo documento in stato INSERITO"
    )
    @SecurityRequirement(name = "BASIC")
    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_ESERCIZIO)
    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_CD_CDS)
    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_CD_UNITA_ORGANIZZATIVA)
    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_CD_CDR)
    @APIResponse(
            responseCode = "200",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Doc_trasporto_rientroBulk.class)
            )
    )
    Response saveDocTR(@Context HttpServletRequest request,@Valid Map<String, Object> body) throws Exception;

    /**
     * Cerca un documento di Trasporto/Rientro per bene, tipo e stato.
     */
    @GET
    @Path("/cerca")
    @Operation(summary = "Restitusce un documento di Trasporto o Rientro un contratto",
            description = "Restituisce documento in stato INSERITO"
    )
    @SecurityRequirement(name = "BASIC")
    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_ESERCIZIO)
    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_CD_CDS)
    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_CD_UNITA_ORGANIZZATIVA)
    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_CD_CDR)
    @APIResponse(
            responseCode = "200",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Doc_trasporto_rientroBulk.class)
            )
    )
    Response cercaDocTrasportoRientro(
            @Context HttpServletRequest request,
            @Valid @BeanParam Doc_trasporto_rientroBulk filtro
    ) throws Exception;
}
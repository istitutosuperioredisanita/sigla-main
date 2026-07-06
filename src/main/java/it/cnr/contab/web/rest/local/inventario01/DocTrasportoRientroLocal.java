package it.cnr.contab.web.rest.local.inventario01;

import it.cnr.contab.web.rest.config.SIGLARoles;
import it.cnr.contab.web.rest.config.SIGLASecurityContext;
import it.cnr.contab.web.rest.model.DocTRWSResponse;
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
@Tag(name = "Documento Trasporto/Rientro")
public interface DocTrasportoRientroLocal {

    @POST
    @RolesAllowed(SIGLARoles.DOC_T_R)
    @Operation(
            summary = "Inserisce un documento di Trasporto o Rientro",
            description = "Crea un nuovo documento in stato INSERITO"
    )
    @SecurityRequirement(name = "BASIC")
    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_ESERCIZIO)
    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_CD_CDS)
    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_CD_UNITA_ORGANIZZATIVA)
    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_CD_CDR)
    @APIResponse(
            responseCode = "201",
            description = "Esito salvataggio documento",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DocTRWSResponse.class)
            )
    )
    Response saveDocTR(
            @Context HttpServletRequest request,
            @Valid Map<String, Object> body
    ) throws Exception;


    @GET
    @RolesAllowed(SIGLARoles.DOC_T_R)
    @Operation(
            summary = "Cerca un documento di Trasporto o Rientro",
            description = "Restituisce i campi chiave del documento associato al bene specificato"
    )
    @SecurityRequirement(name = "BASIC")
    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_ESERCIZIO)
    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_CD_CDS)
    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_CD_UNITA_ORGANIZZATIVA)
    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_CD_CDR)
    @APIResponse(
            responseCode = "200",
            description = "Esito ricerca documento",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DocTRWSResponse.class)
            )
    )
    Response getBeneIntoDoc(
            @Context HttpServletRequest request,
            @Context jakarta.ws.rs.core.Request jaxrsRequest,
            @Context jakarta.ws.rs.core.UriInfo uriInfo,
            @QueryParam("tiDocumento") String tiDocumento,
            @QueryParam("stato") String stato,
            @QueryParam("esercizio") Integer esercizio,
            @QueryParam("nrInventario") String nrInventario
    ) throws Exception;


    @GET
    @Path("/{pgInventario}/{tiDocumento}/{esercizio}/{pgDocTrasportoRientro}")
    @RolesAllowed(SIGLARoles.DOC_T_R)
    @Operation(
            summary = "Ritorna un documento di Trasporto o Rientro",
            description = "Restituisce il documento identificato dalla chiave primaria completa")
    @SecurityRequirement(name = "BASIC")
    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_ESERCIZIO)
    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_CD_CDS)
    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_CD_UNITA_ORGANIZZATIVA)
    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_CD_CDR)
    @APIResponse(responseCode = "200", description = "Documento trovato",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DocTRWSResponse.class)))
    Response get(
            @Context HttpServletRequest request,
            @Context jakarta.ws.rs.core.Request jaxrsRequest, // <-- AGGIUNTO PER L'ETAG
            @Context jakarta.ws.rs.core.UriInfo uriInfo,
            @PathParam("pgInventario") Long pgInventario,
            @PathParam("tiDocumento") String tiDocumento,
            @PathParam("esercizio") Integer esercizio,
            @PathParam("pgDocTrasportoRientro") Long pgDocTrasportoRientro
    ) throws Exception;
}
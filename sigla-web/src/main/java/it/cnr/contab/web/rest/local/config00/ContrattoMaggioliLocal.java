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

package it.cnr.contab.web.rest.local.config00;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.cnr.contab.config00.contratto.bulk.ContrattoDatiSintesiBulk;
import it.cnr.contab.web.rest.config.SIGLARoles;
import it.cnr.contab.web.rest.config.SIGLASecurityContext;
import it.cnr.contab.web.rest.model.ContrattoDtoBulk;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Local;

import it.cnr.contab.web.rest.model.ObbligazioneDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Local
@Path("/contrattoMaggioli")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed(SIGLARoles.CONTRATTO)
@Tag(name = "Contratti")
public interface ContrattoMaggioliLocal extends DefaultContrattoLocal{



    /**
     * PUT  /restapi/contratto -> return Contratto
     */
    @POST
    @Valid
    @Operation(summary = "Inserisce un contratto",
            description = "Accesso consentito solo alle utenze abilitate al ruolo CONTRATTO",
            security = {
                    @SecurityRequirement(name = "BASIC"),
                    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_ESERCIZIO),
                    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_CD_CDS),
                    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_CD_UNITA_ORGANIZZATIVA),
                    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_CD_CDR)
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ContrattoDtoBulk.class)
                            )
                    )
            }
    )
    public Response insertContratto(@Context HttpServletRequest request, @Valid ContrattoDtoBulk contrattoMaggioliBulk ) throws Exception;

    @POST
    @Valid
    @Path("/v2.0")
    @Operation(summary = "Inserisce un contratto",
            description = "Accesso consentito solo alle utenze abilitate al ruolo CONTRATTO",
            security = {
                    @SecurityRequirement(name = "BASIC"),
                    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_ESERCIZIO),
                    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_CD_CDS),
                    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_CD_UNITA_ORGANIZZATIVA),
                    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_CD_CDR)
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ContrattoDtoBulk.class)
                            )
                    )
            }
    )
    Response insertContrattoV2(@Context HttpServletRequest request, @Valid ContrattoDtoBulk contrattoMaggioliBulk ) throws Exception;

    /**
     * GET  /restapi/contratto -> return Contratto
     */
    @GET
    @PermitAll
    @Operation(summary = "Recupera i dati dei contratti",
            description = "Accesso consentito solo alle utenze abilitate al ruolo CONTRATTO",
            security = {
                    @SecurityRequirement(name = "BASIC"),
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ContrattoDatiSintesiBulk.class)
                            )
                    )
            }    )
    Response recuperoDatiContratto(@Context HttpServletRequest request,
                                   @QueryParam("uo") String uo,
                                   @QueryParam("cdTerzo") Integer cdTerzo) throws Exception;
}

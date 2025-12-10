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
import it.cnr.contab.web.rest.config.SIGLARoles;
import it.cnr.contab.web.rest.config.SIGLASecurityContext;
import it.cnr.contab.web.rest.model.LineaAttivitaDto;
import it.cnr.contab.web.rest.model.UpdateLineaAttivitaDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Local;
import java.util.List;

@Local
@Path("/lineaattivita")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed(SIGLARoles.LINEA_ATTIVITA)
@Tag(name = "Linea Attivita")
public interface LineaAttivitaLocal {

	@POST
    @Operation(summary = "Inserisce Linea Attivita",
            description = "Accesso consentito solo alle utenze abilitate e con ruolo '" + SIGLARoles.LINEA_ATTIVITA +"'",
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
                        description = "Linea di Attività create",
                        content = @Content(
                                mediaType = "application/json",
                                schema = @Schema(implementation = List.class)
                        )
                )
            }
    )
    Response insert(@Context HttpServletRequest request, LineaAttivitaDto lineaAttivita) throws Exception;


    @GET
    @Path("/{cd_centro_responsabilita}/{cd_linea_attivita}")
    @Operation(
            summary = "Ritorna Linea Attivita",
            description = "Accesso consentito solo alle utenze abilitate e con ruolo '" + SIGLARoles.LINEA_ATTIVITA + "'",
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
                            description = "Linea Attività trovata",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = LineaAttivitaDto.class)
                            )
                    )
            }
    )
    Response get(
            @PathParam("cd_centro_responsabilita") String cd_centro_responsabilita,
            @PathParam("cd_linea_attivita") String cd_linea_attivita
    ) throws Exception;


    @DELETE
    @Path("/{cd_centro_responsabilita}/{cd_linea_attivita}")
    @Operation(summary = "Elimina una Linea Attivita",
            description = "Accesso consentito solo alle utenze abilitate e con ruolo '" + SIGLARoles.LINEA_ATTIVITA +"'",
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
                                    schema = @Schema(implementation = String.class)
                            )
                    )
            }
    )
    Response delete(@PathParam("cd_centro_responsabilita") String cd_centro_responsabilita,@PathParam("cd_linea_attivita") String cd_linea_attivita) throws Exception;

    @PATCH
    @Path("/{cd_centro_responsabilita}/{cd_linea_attivita}")
    @Operation(summary = "Modifica una Linea Attivita",
            description = "Accesso consentito solo alle utenze abilitate e con ruolo '" + SIGLARoles.LINEA_ATTIVITA +"'",
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
                                    schema = @Schema(implementation = LineaAttivitaDto.class)
                            )
                    )
            }
    )
    Response update(@PathParam("cd_centro_responsabilita") String cd_centro_responsabilita, @PathParam("cd_linea_attivita") String cd_linea_attivita, UpdateLineaAttivitaDto updateLineaAttivitaDto) throws Exception;
}

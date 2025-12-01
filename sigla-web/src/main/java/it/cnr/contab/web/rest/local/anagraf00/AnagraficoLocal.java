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

package it.cnr.contab.web.rest.local.anagraf00;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.cnr.contab.anagraf00.core.bulk.AnagraficoBulk;
import it.cnr.contab.web.rest.config.SIGLARoles;
import it.cnr.contab.web.rest.config.SIGLASecurityContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Local;

@Local
@Path("/anagrafico")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed(SIGLARoles.ANAGRAFICO)
@Tag(name = "Anagrafico")
public interface AnagraficoLocal {

	@PUT
    @Operation(summary = "Inserisce un anagrafico ed un terzo",
            description = "Accesso consentito solo alle utenze abilitate e con ruolo '" + SIGLARoles.ANAGRAFICO +"'",
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
                                    schema = @Schema(implementation = AnagraficoBulk.class)
                            )
                    )
            }
    )
    Response insert(@Context HttpServletRequest request, AnagraficoBulk anagraficoBulk) throws Exception;
}

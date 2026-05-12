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

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.contab.web.rest.config.SIGLARoles;
import it.cnr.contab.web.rest.model.AnagraficaInfoDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Local;

@Local
@Path("/terzo")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed(SIGLARoles.TERZO)
@Tag(name = "Terzo")
@SecurityRequirement(name = "BASIC")
public interface TerzoLocal {

	@POST
    @Operation(summary = "Aggiorna un terzo",
            description = "Accesso consentito solo alle utenze abilitate e con ruolo '" + SIGLARoles.TERZO +"'"
    )
    @APIResponse(
            responseCode = "200",
            description = "Terzo",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = TerzoBulk.class)
            )
    )
    Response update(@Context HttpServletRequest request, TerzoBulk terzoBulk) throws Exception;

    @GET
    @Path("/{cd_terzo}")
    @Operation(summary = "Ritorna il terzo dal codice ",
            description = "Accesso consentito solo alle utenze abilitate e con ruolo '" + SIGLARoles.TERZO +"'"

    )
    @APIResponse(
            responseCode = "200",
            description = "Terzo",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = TerzoBulk.class)
            )
    )
    Response get( @PathParam("cd_terzo") Integer cd_terzo) throws Exception;

    @GET
    @Path("/query")
    @Operation(summary = "Ritorna i terzo associati all'angrafico con il codice fiscale passato in input ",
            description = "Accesso consentito solo alle utenze abilitate e con ruolo '" + SIGLARoles.TERZO +"'"
    )
    @APIResponse(
            responseCode = "200",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = TerzoBulk.class, type = SchemaType.ARRAY)
            )
    )
    Response getList(@QueryParam("codicefiscale")  String codicefiscale) throws Exception;

	@GET
    @Path("/tiporapporto/{codicefiscale}")
    @Operation(summary = "Ritorna i rapporti associati al terzo",
            description = "Accesso consentito solo alle utenze abilitate e con ruolo '" + SIGLARoles.TERZO +"'"
    )
    @APIResponse(
            responseCode = "200",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = TerzoBulk.class, type = SchemaType.ARRAY)
            )
    )
    Response tipoRapporto(@PathParam("codicefiscale") String codicefiscale) throws Exception;

    @GET
    @Path("/info/{codicefiscale}")
    @Operation(summary = "Ritorna le informazioni anagrafiche associate al terzo",
            description = "Accesso consentito solo alle utenze abilitate e con ruolo '" + SIGLARoles.TERZO +"'"
    )
    @APIResponse(
            responseCode = "200",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = AnagraficaInfoDTO.class, type = SchemaType.ARRAY)
            )
    )
    Response anagraficaInfo(@PathParam("codicefiscale") String codicefiscale) throws Exception;

    @GET
    @Path("/infoByCdTerzo/{cd_terzo}")
    @Operation(summary = "Ritorna le informazioni anagrafiche associate al terzo",
            description = "Accesso consentito solo alle utenze abilitate e con ruolo '" + SIGLARoles.TERZO +"'"
    )
    @APIResponse(
            responseCode = "200",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = AnagraficaInfoDTO.class, type = SchemaType.ARRAY)
            )
    )
    Response anagraficaInfoByCdTerzo(@PathParam("cd_terzo") Integer cd_terzo) throws Exception;

}

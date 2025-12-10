/*
 * Copyright (C) 2022  Consiglio Nazionale delle Ricerche
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
import it.cnr.contab.web.rest.config.AllUserAllowedWithoutAbort;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import jakarta.ejb.Local;
import java.util.Map;

@Local
@Path("/tree")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@AllUserAllowedWithoutAbort
@Tag(name = "Albero delle funzioni")
public interface AlberoMainLocal {

    @DELETE
    @Operation(summary = "Elimina la cache e ricalcola l'albero delle funzioni abilitate",
            description = "Accesso consentito a tutte le utenze registrate",
            security = {
                    @SecurityRequirement(name = "BASIC")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Map.class)
                            )
                    )
            }
    )
    Response evictCacheTree(@Context HttpServletRequest request) throws Exception;

    /**
     * GET  /restapi/tree -> return Albero delle funzioni
     */
    @GET
    @Operation(summary = "Fornisce l'albero delle funzioni abilitate",
            description = "Accesso consentito a tutte le utenze registrate",
            security = {
                    @SecurityRequirement(name = "BASIC")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Map.class)
                            )
                    )
            }
    )
    Response tree(@Context HttpServletRequest request,
                  @QueryParam("esercizio") Integer esercizio,
                  @QueryParam("uo") String uo) throws Exception;
}

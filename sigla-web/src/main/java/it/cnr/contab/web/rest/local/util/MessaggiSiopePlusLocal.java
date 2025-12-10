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

package it.cnr.contab.web.rest.local.util;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.cnr.contab.web.rest.config.AccessoAllowed;
import it.cnr.contab.util.enumeration.AccessoEnum;
import it.cnr.contab.web.rest.model.AccountDTO;
import it.cnr.si.siopeplus.model.Esito;
import it.cnr.si.siopeplus.model.Risultato;

import jakarta.ejb.Local;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

@Local
@Path("/messaggi")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "SIOPE+")
public interface MessaggiSiopePlusLocal {

    @GET
    @Path("/siopeplus")
    @AccessoAllowed(value = AccessoEnum.XXXHTTPSESSIONXXXXXX)
    @Operation(summary = "Forza la lettura dei Messaggi SIOPE+ dalla piattaforma",
            description = "Accesso consentito solo alle utenze abilitate a XXXHTTPSESSIONXXXXXX",
            security = {
                    @SecurityRequirement(name = "BASIC"),
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Void.class)
                            )
                    )
            }

    )
    Response messaggiSiopeplus(@Context HttpServletRequest request) throws Exception;

    @GET
    @Path("/siopeplus/{esito}")
    @AccessoAllowed(value = AccessoEnum.XXXHTTPSESSIONXXXXXX)
    @Operation(summary = "Legge i messaggi SIOPE+ dalla piattaforma",
            description = "Accesso consentito solo alle utenze abilitate a XXXHTTPSESSIONXXXXXX",
            security = {
                    @SecurityRequirement(name = "BASIC"),
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
    Response esito(@Context HttpServletRequest request,
                   @PathParam("esito") Esito esito,
                   @QueryParam("dataDa") String dataDa,
                   @QueryParam("dataA") String dataA,
                   @QueryParam("download") Boolean download) throws Exception;


    @GET
    @Path("/siopeplus/{esito}/downloadxml")
    @AccessoAllowed(value = AccessoEnum.XXXHTTPSESSIONXXXXXX)
    @Operation(summary = "Legge i file associati ai messaggi SIOPE+ e li scarica nella folder locale ${localfolder}",
            description = "Accesso consentito solo alle utenze abilitate a XXXHTTPSESSIONXXXXXX",
            security = {
                    @SecurityRequirement(name = "BASIC"),
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
    Response downloadxml(@Context HttpServletRequest request,
                         @PathParam("esito") Esito esito,
                         @QueryParam("dataDa") String dataDa,
                         @QueryParam("dataA") String dataA,
                         @QueryParam("download") Boolean download,
                         @QueryParam("localfolder") String localFolder) throws Exception;

}

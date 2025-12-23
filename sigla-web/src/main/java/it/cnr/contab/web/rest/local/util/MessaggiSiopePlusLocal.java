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

import it.cnr.contab.util.enumeration.AccessoEnum;
import it.cnr.contab.web.rest.config.AccessoAllowed;
import it.cnr.si.siopeplus.model.Esito;
import jakarta.ejb.Local;
import jakarta.servlet.http.HttpServletRequest;
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
@Path("/messaggi")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "SIOPE+", description = "Servizi di utilità per il colloquio con la piattaforma SIOPE+")
public interface MessaggiSiopePlusLocal {

    @GET
    @Path("/siopeplus")
    @AccessoAllowed(value = AccessoEnum.XXXHTTPSESSIONXXXXXX)
    @Operation(summary = "Forza la lettura dei Messaggi SIOPE+ dalla piattaforma",
            description = "Accesso consentito solo alle utenze abilitate a XXXHTTPSESSIONXXXXXX"
    )
    @SecurityRequirement(name = "BASIC")
    Response messaggiSiopeplus(@Context HttpServletRequest request) throws Exception;

    @GET
    @Path("/siopeplus/{esito}")
    @AccessoAllowed(value = AccessoEnum.XXXHTTPSESSIONXXXXXX)
    @Operation(summary = "Legge i messaggi SIOPE+ dalla piattaforma",
            description = "Accesso consentito solo alle utenze abilitate a XXXHTTPSESSIONXXXXXX"
    )
    @APIResponse(
            responseCode = "200",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Map.class)
            )
    )
    @SecurityRequirement(name = "BASIC")
    Response esito(@Context HttpServletRequest request,
                   @PathParam("esito") Esito esito,
                   @QueryParam("dataDa") String dataDa,
                   @QueryParam("dataA") String dataA,
                   @QueryParam("download") Boolean download) throws Exception;


    @GET
    @Path("/siopeplus/{esito}/downloadxml")
    @AccessoAllowed(value = AccessoEnum.XXXHTTPSESSIONXXXXXX)
    @Operation(summary = "Legge i file associati ai messaggi SIOPE+ e li scarica nella folder locale ${localfolder}",
            description = "Accesso consentito solo alle utenze abilitate a XXXHTTPSESSIONXXXXXX"
    )
    @APIResponse(
            responseCode = "200",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Map.class)
            )
    )
    @SecurityRequirement(name = "BASIC")
    Response downloadxml(@Context HttpServletRequest request,
                         @PathParam("esito") Esito esito,
                         @QueryParam("dataDa") String dataDa,
                         @QueryParam("dataA") String dataA,
                         @QueryParam("download") Boolean download,
                         @QueryParam("localfolder") String localFolder) throws Exception;

}

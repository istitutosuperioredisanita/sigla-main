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

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import it.cnr.contab.web.rest.config.AllUserAllowedWithoutAbort;
import it.cnr.contab.web.rest.model.AccountDTO;
import it.cnr.contab.web.rest.model.PasswordDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import jakarta.ejb.Local;

@Local
@Path("/account")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@AllUserAllowedWithoutAbort
@Tag(name = "Account")
@SecurityRequirement(name = "BASIC")
public interface AccountLocal {

    @GET
    @Operation(summary = "Fornisce le informazioni dell'account",
            description = "Accesso consentito a tutte le utenze registrate"
    )
    @APIResponse(
            responseCode = "200",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AccountDTO.class)
            )
    )
    Response get(@Context HttpServletRequest request) throws Exception;

    @GET
    @Path("/{username}")
    @Operation(summary = "Fornisce le informazioni dell'account in base allo username",
            description = "Accesso consentito a tutte le utenze registrate"
    )
    @APIResponse(
            responseCode = "200",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AccountDTO.class)
            )
    )
    Response getUsername(@Context HttpServletRequest request, @PathParam("username") String username) throws Exception;

    @POST
    @Path("/change-password")
    @Operation(summary = "Cambia la passwod dell'utente collegato",
            description = "Accesso consentito a tutte le utenze registrate"
    )
    @APIResponse(
            responseCode = "200",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AccountDTO.class)
            )
    )
    Response changePassword(@Context HttpServletRequest request, PasswordDTO passwordDTO) throws Exception;

    AccountDTO getAccountDTO(HttpServletRequest request) throws Exception;
}

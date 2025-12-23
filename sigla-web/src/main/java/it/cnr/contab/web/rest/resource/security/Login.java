/*
 * Copyright (C) 2020  Consiglio Nazionale delle Ricerche
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

package it.cnr.contab.web.rest.resource.security;

import it.cnr.contab.web.rest.exception.UnprocessableEntityException;
import it.cnr.contab.web.rest.local.config00.AccountLocal;
import it.cnr.contab.web.rest.model.AccountDTO;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.EJB;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

@Path("login")
@Produces(MediaType.APPLICATION_JSON)
@PermitAll
@Tag(name = "Security")
public class Login {
    private final Logger LOGGER = LoggerFactory.getLogger(Login.class);
    @Context
    SecurityContext securityContext;

    @EJB
    private AccountLocal accountLocal;

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Operation(
            summary = "Effettua il login applicativo",
            description = "Restituisce le informazioni sull'utenza"
    )
    @RequestBody(
            content = @Content(
                    mediaType = MediaType.APPLICATION_FORM_URLENCODED,
                    schema = @Schema(implementation = LoginForm.class)
            )
    )
    @APIResponse(
            responseCode = "200",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AccountDTO.class)
            )
    )
    public Response postLogin(@Context HttpServletRequest request,
                              @BeanParam LoginForm loginForm) throws Exception {
        try {
            if (Optional.ofNullable(securityContext.getUserPrincipal()).isEmpty())
                request.login(loginForm.getUsername(), loginForm.getPassword());
            return Response.ok(accountLocal.getAccountDTO(request)).build();
        } catch (ServletException e) {
            LOGGER.trace("Login error for user:{} password:{}", loginForm.getUsername(), loginForm.getPassword(), e);
            return Response.status(Response.Status.UNAUTHORIZED) .build();
        } catch (UnprocessableEntityException e) {
            LOGGER.trace("Login error for user:{} password:{}", loginForm.getUsername(), loginForm.getPassword(), e);
            return Response.status(Response.Status.NOT_ACCEPTABLE) .build();
        }
    }

}

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

package it.cnr.contab.web.rest.local.docamm;

import it.cnr.contab.web.rest.config.SIGLARoles;
import it.cnr.contab.web.rest.model.TreeNode;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Local;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

@Local
@Path("/acquisti")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed(value = {SIGLARoles.SUPERVISORE})
@Tag(name = "Acquisti")
public interface AcquistiLocal {

    @GET
    @Path("/struttura/{esercizio}")
    @Operation(summary = "Recupera i dati relativi agli acquisti ragruppati per struttura",
            description = "Accesso consentito solo alle utenze abilitate al ruolo SUPERVISORE"
    )
    @SecurityRequirement(name = "BASIC")
    @APIResponse(
            responseCode = "200",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TreeNode.class)
            )
    )
    Response struttura(
            @Context HttpServletRequest request,
            @Parameter(description = "Esercizio", required = true)
            @PathParam("esercizio") Integer esercizio
    ) throws Exception;

}

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

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import it.cnr.contab.web.rest.config.AccessoAllowed;
import it.cnr.contab.util.enumeration.AccessoEnum;
import it.cnr.jada.util.ejb.EJBTracer;

import jakarta.ejb.Local;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Local
@Path("/tracers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Tracer")
@SecurityRequirement(name = "BASIC")
public interface IteratorTracersLocal {

    @GET
    @AccessoAllowed(value = AccessoEnum.XXXHTTPSESSIONXXXXXX)
    @Operation(summary = "Resituisce gli iteratori ancori aperti",
            description = "Accesso consentito solo alle utenze abilitate a XXXHTTPSESSIONXXXXXX"

    )
    @APIResponse(
            responseCode = "200",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = EJBTracer.IteratorTracer.class)
            )
    )
    Response map(@Context HttpServletRequest request) throws Exception;
}

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

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import it.cnr.contab.missioni00.docs.bulk.MissioneBulk;
import it.cnr.contab.web.rest.config.SIGLARoles;
import it.cnr.contab.web.rest.config.SIGLASecurityContext;
import it.cnr.contab.web.rest.model.MassimaleSpesaBulk;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Local;

@Local
@Path("/missioni")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed(SIGLARoles.MISSIONI)
@Tag(name = "Missioni. Gestione dei rimborsi delle trasferte.")
public interface MissioneLocal {
    /**
     * POST  /restapi/missioni/validaMassimaleSpesa -> return OK o KO
     */
    @POST
    @Path(value = "/validaMassimaleSpesa")
    @Operation(summary = "Verifica se la spesa è consentina in funzione dei massimali presenti",
            description = "Accesso consentito solo alle utenze abilitate al ruolo MISSIONI"
    )
    @SecurityRequirement(name = "BASIC")
    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_ESERCIZIO)
    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_CD_CDS)
    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_CD_UNITA_ORGANIZZATIVA)
    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_CD_CDR)
    @APIResponse(
            responseCode = "200",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = String.class)
            )
    )
    Response validaMassimaleSpesa(@Context HttpServletRequest request, MassimaleSpesaBulk massimaleSpesaBulk) throws Exception;

    /**
     * PUT  /restapi/missioni -> return Missione
     */
    @PUT
    @Operation(summary = "Inserisce una missione.",
            description = "Accesso consentito solo alle utenze abilitate al ruolo MISSIONI"
    )
    @SecurityRequirement(name = "BASIC")
    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_ESERCIZIO)
    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_CD_CDS)
    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_CD_UNITA_ORGANIZZATIVA)
    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_CD_CDR)
    @APIResponse(
            responseCode = "200",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MissioneBulk.class)
            )
    )
    Response insert(@Context HttpServletRequest request, MissioneBulk missioneBulk) throws Exception;

    /**
     * DELETE  /restapi/missioni -> return OK
     */
    @DELETE
    @Path("{id}")
    @Operation(summary = "Elimina una missione.",
            description = "Accesso consentito solo alle utenze abilitate al ruolo MISSIONI"
    )
    @SecurityRequirement(name = "BASIC")
    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_ESERCIZIO)
    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_CD_CDS)
    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_CD_UNITA_ORGANIZZATIVA)
    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_CD_CDR)
    @APIResponse(
            responseCode = "200",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = String.class)
            )
    )
    Response delete(@Context HttpServletRequest request, @PathParam("id") long idRimborsoMissione) throws Exception;
}

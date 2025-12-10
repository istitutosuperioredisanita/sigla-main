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
import it.cnr.contab.web.rest.config.SIGLASecurityContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import jakarta.ejb.Local;
import java.util.List;

@Local
@Path("/fatture-attive")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Fatturazione Attiva Elettronica")
public interface PECFattureAttiveLocal {

    @GET
    @Path("/reinvia-pec")
    @AccessoAllowed(value= AccessoEnum.XXXHTTPSESSIONXXXXXX)
    @Operation(summary = "Reinvia tramite PEC l'xml della fattura attiva a SDI",
            description = "Accesso consentito solo alle utenze abilitate con accesso XXXHTTPSESSIONXXXXXX",
            security = {
                    @SecurityRequirement(name = "BASIC")
            }
    )
    Response reinviaPEC(@Context HttpServletRequest request,
                        @QueryParam("esercizio") Integer esercizio,
                        @QueryParam("pgFatturaAttiva") Long pgFatturaAttiva) throws Exception;

    @GET
    @Path("/aggiorna-nome-file")
    @AccessoAllowed(value= AccessoEnum.XXXHTTPSESSIONXXXXXX)
    @Operation(summary = "Aggiorna il nome del file su tutte le fatture attive con stato INV",
            description = "Accesso consentito solo alle utenze abilitate con accesso AMMFATTURDOCSFATATTV",
            security = {
                    @SecurityRequirement(name = "BASIC")
            }

    )
    Response aggiornaNomeFile(@Context HttpServletRequest request) throws Exception;

    @GET
    @Path("/aggiorna-metadati")
    @AccessoAllowed(value= AccessoEnum.XXXHTTPSESSIONXXXXXX)
    @Operation(summary = "Aggiorna i metadati della fattura attiva sul documentale",
            description = "Accesso consentito solo alle utenze abilitate con accesso AMMFATTURDOCSFATATTV",
            security = {
                    @SecurityRequirement(name = "BASIC")
            }
    )
    Response aggiornaMetadati(@Context HttpServletRequest request,
                              @QueryParam("esercizio") Integer esercizio,
                              @QueryParam("cdCds") String cdCds,
                              @QueryParam("pgFatturaAttiva") Long pgFatturaAttiva) throws Exception;

    @GET
    @Path("/reinvia-notifica-ko")
    @AccessoAllowed(value= AccessoEnum.XXXHTTPSESSIONXXXXXX)
    @Operation(summary = "Reinvia la notifica di esito negativo a tutte le utenza configurate a ricevere la notifica e all'utenza che ha creato la fattura",
            description = "Accesso consentito solo alle utenze abilitate con accesso AMMFATTURDOCSFATATTV",
            security = {
                    @SecurityRequirement(name = "BASIC")
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = List.class)
                            )
                    )
            }
    )
    Response reinviaNotifica(@Context HttpServletRequest request,
                             @QueryParam("esercizio") Integer esercizio,
                             @QueryParam("cdCds") String cdCds,
                             @QueryParam("cdUnitaOrganizzativa")String cdUnitaOrganizzativa,
                             @QueryParam("pgFatturaAttiva") Long pgFatturaAttiva) throws Exception;
}

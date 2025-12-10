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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.cnr.contab.client.docamm.FatturaAttiva;
import it.cnr.contab.web.rest.config.AccessoAllowed;
import it.cnr.contab.util.enumeration.AccessoEnum;
import it.cnr.contab.web.rest.config.SIGLASecurityContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

import jakarta.ejb.Local;

@Local
@Path("/fatturaattiva")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Fatturazione Attiva")
public interface FatturaAttivaLocal {
	/**
     * GET  /restapi/fatturaattiva/ricerca -> return Fattura attiva
     */
    @GET
    @AccessoAllowed(value=AccessoEnum.AMMFATTURDOCSFATATTV)
    @Operation(summary = "Recupera i dati della Fattura Attiva",
            description = "Accesso consentito solo alle utenze abilitate con accesso AMMFATTURDOCSFATATTV",
            security = {
                    @SecurityRequirement(name = "BASIC"),
                    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_ESERCIZIO),
                    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_CD_CDS),
                    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_CD_UNITA_ORGANIZZATIVA),
                    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_CD_CDR)
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = FatturaAttiva.class)
                            )
                    )
            }
    )
    Response ricercaFattura(@Context HttpServletRequest request,
                            @QueryParam("esercizio") Integer esercizio,
                            @QueryParam ("pg") Long pg) throws Exception;

	/**
	 * POST  /restapi/fatturaattiva-> return Fattura attiva
	 */
	@POST
	@AccessoAllowed(value=AccessoEnum.AMMFATTURDOCSFATATTM)
    @Operation(summary = "Inserisce una o più Fatture Attive",
            description = "Accesso consentito solo alle utenze abilitate con accesso AMMFATTURDOCSFATATTM",
            security = {
                    @SecurityRequirement(name = "BASIC"),
                    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_ESERCIZIO),
                    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_CD_CDS),
                    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_CD_UNITA_ORGANIZZATIVA),
                    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_CD_CDR)
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
    Response inserisciFatture(@Context HttpServletRequest request, List<FatturaAttiva> fatture) throws Exception;


    /**
     * GET  /restapi/fatturaattiva/ricerca -> return Fattura attiva
     */
    @POST
    @Path("/inserisciDatiPerStampa")
    @AccessoAllowed(value=AccessoEnum.AMMFATTURDOCSFATATTM)
    @Operation(summary = "Inserisce i dati per la stampa IVA",
            description = "Accesso consentito solo alle utenze abilitate con accesso AMMFATTURDOCSFATATTM",
            security = {
                    @SecurityRequirement(name = "BASIC"),
                    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_ESERCIZIO),
                    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_CD_CDS),
                    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_CD_UNITA_ORGANIZZATIVA),
                    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_CD_CDR)
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = Long.class)
                            )
                    )
            }

    )
    Response inserisciDatiPerStampa(@Context HttpServletRequest request,
                                    @QueryParam ("esercizio") Integer esercizio,
                                    @QueryParam ("pg") Long pg) throws Exception;

    /**
     * GET  /restapi/fatturaattiva/ricerca -> return Fattura attiva
     */
    @GET
    @Path("/print")
    @AccessoAllowed(value=AccessoEnum.AMMFATTURDOCSFATATTV)
    @Produces("application/pdf")
    @Operation(summary = "Fornisce la stampa della Fattura Attiva in pdf",
            description = "Accesso consentito solo alle utenze abilitate con accesso AMMFATTURDOCSFATATTV",
            security = {
                    @SecurityRequirement(name = "BASIC"),
                    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_ESERCIZIO),
                    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_CD_CDS),
                    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_CD_UNITA_ORGANIZZATIVA),
                    @SecurityRequirement(name = SIGLASecurityContext.X_SIGLA_CD_CDR)
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = byte[].class)
                            )
                    )
            }
    )
    Response stampaFattura(@Context HttpServletRequest request,
                           @QueryParam ("pgStampa") Long pgStampa) throws Exception;

}

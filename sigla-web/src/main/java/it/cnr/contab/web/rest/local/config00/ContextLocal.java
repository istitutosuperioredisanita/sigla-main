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

import it.cnr.contab.messaggio00.bulk.MessaggioBulk;
import it.cnr.contab.web.rest.config.AllUserAllowedWithoutAbort;
import it.cnr.contab.web.rest.model.PreferitiDTOBulk;
import it.cnr.contab.web.rest.model.UtenteIndirizziMailDTO;
import jakarta.ejb.Local;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.springframework.data.util.Pair;

import java.util.List;

@Local
@Path("/context")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@AllUserAllowedWithoutAbort
@Tag(name = "Servizi di contesto applicativo")
@SecurityRequirement(name = "BASIC")
public interface ContextLocal {

    @GET
    @Path("/esercizi")
    @Operation(
            summary = "Ritorna la lista degli esercizi possibili",
            description = "Accesso consentito a tutte le utenze registrate"
    )
    @APIResponse(
            responseCode = "200",
            description = "Lista degli esercizi possibili",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = Integer.class, type = SchemaType.ARRAY)
            )
    )
    Response esercizi(@Context HttpServletRequest request, @QueryParam("cds") String cds) throws Exception;

    @GET
    @Path("/uo")
    @Operation(
            summary = "Ritorna la lista delle Unità Organizzative abilitate",
            description = "Accesso consentito a tutte le utenze registrate"
    )
    @APIResponse(
            responseCode = "200",
            description = "Lista delle UO abilitate",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = Pair.class, type = SchemaType.ARRAY)
            )
    )
    Response findUnitaOrganizzativeAbilitate(@Context HttpServletRequest request, @QueryParam("cds") String cds) throws Exception;

    @GET
    @Path("/cds")
    @Operation(
            summary = "Ritorna la lista dei CdS abilitati",
            description = "Accesso consentito a tutte le utenze registrate"
    )
    @APIResponse(
            responseCode = "200",
            description = "Lista dei CdS abilitati",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = Pair.class, type = SchemaType.ARRAY)
            )
    )
    Response findCdSAbilitati(@Context HttpServletRequest request, @QueryParam("uo") String uo) throws Exception;

    @GET
    @Path("/cdr")
    @Operation(
            summary = "Ritorna la lista dei CdR",
            description = "Accesso consentito a tutte le utenze registrate"
    )
    @APIResponse(
            responseCode = "200",
            description = "Lista dei CdR",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = Pair.class, type = SchemaType.ARRAY)
            )
    )
    Response findCdR(@Context HttpServletRequest request, @QueryParam("uo") String uo) throws Exception;

    @GET
    @Path("/preferiti")
    @Operation(
            summary = "Ritorna la lista dei Preferiti per Utente",
            description = "Accesso consentito a tutte le utenze registrate"
    )
    @APIResponse(
            responseCode = "200",
            description = "Lista dei preferiti",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = PreferitiDTOBulk.class, type = SchemaType.ARRAY)
            )
    )
    Response findPreferiti(@Context HttpServletRequest request) throws Exception;

    @GET
    @Path("/messaggi")
    @Operation(
            summary = "Ritorna la lista dei Messaggi per Utente",
            description = "Accesso consentito a tutte le utenze registrate"
    )
    @APIResponse(
            responseCode = "200",
            description = "Lista dei messaggi",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = MessaggioBulk.class, type = SchemaType.ARRAY)
            )
    )
    Response findMessaggi(@Context HttpServletRequest request) throws Exception;

    @POST
    @Path("/messaggi")
    @Operation(
            summary = "Cancella la lista dei Messaggi per Utente",
            description = "Accesso consentito a tutte le utenze registrate"
    )
    @APIResponse(
            responseCode = "200",
            description = "Messaggi eliminati",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = MessaggioBulk.class, type = SchemaType.ARRAY)
            )
    )
    Response deleteMessaggi(@Context HttpServletRequest request, List<MessaggioBulk> messaggi) throws Exception;

    @GET
    @Path("/indirizzi-mail")
    @Operation(
            summary = "Ritorna la lista degli indirizzi email per Utente",
            description = "Accesso consentito a tutte le utenze registrate"
    )
    @APIResponse(
            responseCode = "200",
            description = "Lista degli indirizzi email",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = UtenteIndirizziMailDTO.class, type = SchemaType.ARRAY)
            )
    )
    Response indirizziMail(@Context HttpServletRequest request) throws Exception;

    @POST
    @Path("/indirizzi-mail")
    @Operation(
            summary = "Inserisce la lista degli indirizzi email per Utente",
            description = "Accesso consentito a tutte le utenze registrate"
    )
    @APIResponse(
            responseCode = "200",
            description = "Indirizzi email inseriti",
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON,
                    schema = @Schema(implementation = UtenteIndirizziMailDTO.class, type = SchemaType.ARRAY)
            )
    )
    Response inserisciIndirizziMail(@Context HttpServletRequest request, List<UtenteIndirizziMailDTO> utente_indirizzi_mailBulks) throws Exception;


    @DELETE
    @Path("/indirizzi-mail/{indirizzi:.+}/delete")
    @Operation(
            summary = "Elimina la lista degli indirizzi email per Utente",
            description = "Accesso consentito a tutte le utenze registrate"
    )
    @APIResponse(
            responseCode = "200",
            description = "Indirizzi email eliminati",
            content = @Content(mediaType = "application/json")
    )
    Response eliminaIndirizziMail(@Context HttpServletRequest request, @PathParam("indirizzi") String indirizzi) throws Exception;
}

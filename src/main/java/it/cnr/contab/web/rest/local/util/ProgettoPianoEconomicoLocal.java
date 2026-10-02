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

import it.cnr.contab.web.rest.config.AccessoAllowed;
import it.cnr.contab.util.enumeration.AccessoEnum;

import it.cnr.contab.web.rest.config.SIGLARoles;
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

import java.util.Map;

@Local
@Path("/progetto")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Piano Economico", description = "Servizi di utilità per la verifica del piano economico di un progetto")
public interface ProgettoPianoEconomicoLocal {

    @GET
    @Path("/check-piano-economico")
    @AccessoAllowed(value= AccessoEnum.XXXHTTPSESSIONXXXXXX)
    Response checkPdgPianoEconomico(@Context HttpServletRequest request,
                                    @QueryParam("tipoVariazione") String tipoVariazione,
                                    @QueryParam("esercizio") Integer esercizio,
                                    @QueryParam("pgVariazioneMin") Long pgVariazioneMin,
                                    @QueryParam("pgVariazioneMax") Long pgVariazioneMax) throws Exception;

    @GET
    @Path("/fondi-funzionamento/uo/{esercizio}")
    @Operation(summary = "Analisi Fondi Funzionamento i dati mostrano la quota di funzionamento, comprensiva della quota per la sicurezza, assegnata ed utilizzata per ogni UO nell'anno.",
            description = "Accesso consentito solo alle utenze abilitate al ruolo PROGETTI oppure SUPERVISORE"
    )
    @SecurityRequirement(name = "BASIC")
    @APIResponse(
            responseCode = "200",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Map.class)
            )
    )
    @RolesAllowed(value = {SIGLARoles.PROGETTI, SIGLARoles.SUPERVISORE})
    Response fondiFunzionamentoUO(
            @Context HttpServletRequest request,
            @Parameter(description = "Esercizio", required = true)
            @PathParam("esercizio") Integer esercizio,
            @Parameter(description = "CdS - Centro di spesa")
            @QueryParam("cds") String cds
    ) throws Exception;

    @GET
    @Path("/fondi-funzionamento/tipo-finanziamento/{esercizio}")
    @Operation(summary = "Analisi Fondi Funzionamento i dati mostrano la quota di funzionamento, comprensiva della quota per la sicurezza, assegnata ed utilizzata per ogni tipo finanziamento nell'anno.",
            description = "Accesso consentito solo alle utenze abilitate al ruolo PROGETTI oppure SUPERVISORE"
    )
    @SecurityRequirement(name = "BASIC")
    @APIResponse(
            responseCode = "200",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Map.class)
            )
    )
    @RolesAllowed(value = {SIGLARoles.PROGETTI, SIGLARoles.SUPERVISORE})
    Response fondiFunzionamentoTipoFinanziamento(
            @Context HttpServletRequest request,
            @Parameter(description = "Esercizio", required = true)
            @PathParam("esercizio") Integer esercizio,
            @Parameter(description = "Unità Organizzativa")
            @QueryParam("uo") String uo
    ) throws Exception;

    @GET
    @Path("/fondi-funzionamento/tipo-progetto/{esercizio}")
    @Operation(summary = "Analisi Fondi Funzionamento i dati mostrano la quota di funzionamento, comprensiva della quota per la sicurezza, assegnata ed utilizzata per ogni tipo progetto nell'anno.",
            description = "Accesso consentito solo alle utenze abilitate al ruolo PROGETTI oppure SUPERVISORE"
    )
    @SecurityRequirement(name = "BASIC")
    @APIResponse(
            responseCode = "200",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Map.class)
            )
    )
    @RolesAllowed(value = {SIGLARoles.PROGETTI, SIGLARoles.SUPERVISORE})
    Response fondiFunzionamentoTipoProgetto(
            @Context HttpServletRequest request,
            @Parameter(description = "Esercizio", required = true)
            @PathParam("esercizio") Integer esercizio,
            @Parameter(description = "Unità Organizzativa")
            @QueryParam("uo") String uo
    ) throws Exception;


    @GET
    @Path("/fondi-funzionamento/ente-finanziatore/{esercizio}")
    @Operation(summary = "Analisi Fondi Funzionamento i dati mostrano la quota di funzionamento, comprensiva della quota per la sicurezza, assegnata ed utilizzata per ogni ente finanziatore nell'anno.",
            description = "Accesso consentito solo alle utenze abilitate al ruolo PROGETTI oppure SUPERVISORE"
    )
    @SecurityRequirement(name = "BASIC")
    @APIResponse(
            responseCode = "200",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Map.class)
            )
    )
    @RolesAllowed(value = {SIGLARoles.PROGETTI, SIGLARoles.SUPERVISORE})
    Response fondiFunzionamentoEnteFinanziatore(
            @Context HttpServletRequest request,
            @Parameter(description = "Esercizio", required = true)
            @PathParam("esercizio") Integer esercizio,
            @Parameter(description = "Unità Organizzativa")
            @QueryParam("uo") String uo
    ) throws Exception;

    @GET
    @Path("/fondi-funzionamento/uo/{esercizio}/{uo}")
    @Operation(summary = "Analisi Fondi Funzionamento i dati mostrano la quota di funzionamento, comprensiva della quota per la sicurezza, assegnata ed utilizzata per ogni progetto nell'anno della UO indicata.",
            description = "Accesso consentito solo alle utenze abilitate al ruolo PROGETTI oppure SUPERVISORE"
    )
    @SecurityRequirement(name = "BASIC")
    @APIResponse(
            responseCode = "200",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Map.class)
            )
    )
    @RolesAllowed(value = {SIGLARoles.PROGETTI, SIGLARoles.SUPERVISORE})
    Response fondiFunzionamentoDettaglioUO(
            @Context HttpServletRequest request,
            @Parameter(description = "Esercizio", required = true)
            @PathParam("esercizio") Integer esercizio,
            @Parameter(description = "Unità Organizzativa", required = true)
            @PathParam("uo") String uo
    ) throws Exception;

    @GET
    @Path("/fondi-funzionamento/tipo-finanziamento/{esercizio}/{tipo}")
    @Operation(summary = "Analisi Fondi Funzionamento i dati mostrano la quota di funzionamento, comprensiva della quota per la sicurezza, assegnata ed utilizzata per ogni progetto nell'anno del tipo di finanziamento indicato.",
            description = "Accesso consentito solo alle utenze abilitate al ruolo PROGETTI oppure SUPERVISORE"
    )
    @SecurityRequirement(name = "BASIC")
    @APIResponse(
            responseCode = "200",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Map.class)
            )
    )
    @RolesAllowed(value = {SIGLARoles.PROGETTI, SIGLARoles.SUPERVISORE})
    Response fondiFunzionamentoDettaglioTipoFinanziamento(
            @Context HttpServletRequest request,
            @Parameter(description = "Esercizio", required = true)
            @PathParam("esercizio") Integer esercizio,
            @Parameter(description = "Tipo Finanziamento", required = true)
            @PathParam("tipo") String tipo,
            @Parameter(description = "Unità Organizzativa")
            @QueryParam("uo") String uo
    ) throws Exception;

    @GET
    @Path("/fondi-funzionamento/tipo-progetto/{esercizio}/{tipo}")
    @Operation(summary = "Analisi Fondi Funzionamento i dati mostrano la quota di funzionamento, comprensiva della quota per la sicurezza, assegnata ed utilizzata per ogni progetto nell'anno del tipo di progetto indicato.",
            description = "Accesso consentito solo alle utenze abilitate al ruolo PROGETTI oppure SUPERVISORE"
    )
    @SecurityRequirement(name = "BASIC")
    @APIResponse(
            responseCode = "200",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Map.class)
            )
    )
    @RolesAllowed(value = {SIGLARoles.PROGETTI, SIGLARoles.SUPERVISORE})
    Response fondiFunzionamentoDettaglioTipoProgetto(
            @Context HttpServletRequest request,
            @Parameter(description = "Esercizio", required = true)
            @PathParam("esercizio") Integer esercizio,
            @Parameter(description = "Tipo Progetto", required = true)
            @PathParam("tipo") String tipo,
            @Parameter(description = "Unità Organizzativa")
            @QueryParam("uo") String uo
    ) throws Exception;

    @GET
    @Path("/fondi-funzionamento/ente-finanziatore/{esercizio}/{cdTerzo}")
    @Operation(summary = "Analisi Fondi Funzionamento i dati mostrano la quota di funzionamento, comprensiva della quota per la sicurezza, assegnata ed utilizzata per ogni progetto nell'anno del terzo finanziatore indicato.",
            description = "Accesso consentito solo alle utenze abilitate al ruolo PROGETTI oppure SUPERVISORE"
    )
    @SecurityRequirement(name = "BASIC")
    @APIResponse(
            responseCode = "200",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Map.class)
            )
    )
    @RolesAllowed(value = {SIGLARoles.PROGETTI, SIGLARoles.SUPERVISORE})
    Response fondiFunzionamentoDettaglioEnteFinanziatore(
            @Context HttpServletRequest request,
            @Parameter(description = "Esercizio", required = true)
            @PathParam("esercizio") Integer esercizio,
            @Parameter(description = "Ente FInanziatore", required = true)
            @PathParam("cdTerzo") String cdTerzo,
            @Parameter(description = "Unità Organizzativa")
            @QueryParam("uo") String uo
    ) throws Exception;

    @GET
    @Path("/fondi-funzionamento/{esercizio}/codice/{cdProgetto}")
    @Operation(summary = "Analisi Fondi Funzionamento i dati mostrano la quota di funzionamento, comprensiva della quota per la sicurezza, assegnata ed utilizzata nell'anno riferita al progetto indicato.",
            description = "Accesso consentito solo alle utenze abilitate al ruolo PROGETTI oppure SUPERVISORE"
    )
    @SecurityRequirement(name = "BASIC")
    @APIResponse(
            responseCode = "200",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Map.class)
            )
    )
    @RolesAllowed(value = {SIGLARoles.PROGETTI, SIGLARoles.SUPERVISORE})
    Response fondiFunzionamentoProgetto(
            @Context HttpServletRequest request,
            @Parameter(description = "Esercizio", required = true)
            @PathParam("esercizio") Integer esercizio,
            @Parameter(description = "Codice progetto", required = true)
            @PathParam("cdProgetto") String cdProgetto,
            @Parameter(description = "Lista degli elementi voce separati da ',' ")
            @QueryParam("elementiVoce") String elementiVoce
    ) throws Exception;

    @GET
    @Path("/piano-economico/{esercizio}/codice/{cdProgetto}")
    @Operation(summary = "Piano economico nell'anno riferito al progetto indicato.",
            description = "Accesso consentito solo alle utenze abilitate al ruolo PROGETTI oppure SUPERVISORE"
    )
    @SecurityRequirement(name = "BASIC")
    @APIResponse(
            responseCode = "200",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Map.class)
            )
    )
    @RolesAllowed(value = {SIGLARoles.PROGETTI, SIGLARoles.SUPERVISORE})
    Response pianoEconomicoProgetto(
            @Context HttpServletRequest request,
            @Parameter(description = "Esercizio", required = true)
            @PathParam("esercizio") Integer esercizio,
            @Parameter(description = "Codice progetto", required = true)
            @PathParam("cdProgetto") String cdProgetto
    ) throws Exception;

}

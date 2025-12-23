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

import jakarta.ejb.Local;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

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
}

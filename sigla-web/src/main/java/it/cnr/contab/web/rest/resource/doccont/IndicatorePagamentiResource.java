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

package it.cnr.contab.web.rest.resource.doccont;

import it.cnr.contab.doccont00.core.bulk.ConsIndicatorePagamentiBulk;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.web.rest.exception.RestException;
import it.cnr.contab.web.rest.local.doccont.IndicatorePagamentiLocal;
import it.cnr.jada.ejb.CRUDComponentSession;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Stateless
public class IndicatorePagamentiResource implements IndicatorePagamentiLocal {
    private final Logger LOGGER = LoggerFactory.getLogger(IndicatorePagamentiResource.class);
    @Context
    SecurityContext securityContext;

    @EJB
    CRUDComponentSession crudComponentSession;

    @Override
    public Response riepilogo(@Context HttpServletRequest request, Integer esercizio, String uo) throws Exception {
        LOGGER.debug("REST request per riepilogo indicatore pagamenti.");
        CNRUserContext userContext = (CNRUserContext) securityContext.getUserPrincipal();
        final ConsIndicatorePagamentiBulk consIndicatorePagamentiBulk = new ConsIndicatorePagamentiBulk();
        Optional.ofNullable(esercizio).orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Errore, esercizio obbligatorio."));

        try {
            List<ConsIndicatorePagamentiBulk> dati =
                    crudComponentSession.find(userContext, ConsIndicatorePagamentiBulk.class, "findRiepilogo", userContext, consIndicatorePagamentiBulk, esercizio, uo);
            LOGGER.debug("Fine REST per riepilogo indicatore pagamenti.");
            return Response.ok(dati
                    .stream()
                    .collect(Collectors.toMap(
                            ConsIndicatorePagamentiBulk::getHashTrimestre,
                            cipk -> cipk.getIndicePagamenti().setScale(2, RoundingMode.UP))
                    )).header("Keep-Alive", "timeout=86400").build();
        } catch (Exception _ex) {
            LOGGER.error("REST request per riepilogo indicatore pagamenti. ERROR: ", _ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Collections.singletonMap("ERROR", _ex)).build();
        }
    }

}
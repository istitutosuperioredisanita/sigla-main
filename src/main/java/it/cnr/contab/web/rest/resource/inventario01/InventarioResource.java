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

package it.cnr.contab.web.rest.resource.inventario01;

import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;
import it.cnr.contab.docamm00.tabrif.bulk.Categoria_gruppo_inventBulk;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.web.rest.exception.RestException;
import it.cnr.contab.web.rest.local.inventario01.InventarioLocal;
import it.cnr.jada.ejb.CRUDComponentSession;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Stateless
public class InventarioResource implements InventarioLocal {
    private final Logger LOGGER = LoggerFactory.getLogger(InventarioResource.class);
    @Context
    SecurityContext securityContext;

    @EJB
    CRUDComponentSession crudComponentSession;

    record UnitaKey(String codice, String descrizione) {}
    record TotaliBeni(long numeroTotaleBeni, BigDecimal valoreTotaleBeni) {}

    record UnitaTotali(String codice, String descrizione, long numeroTotaleBeni, BigDecimal valoreTotaleBeni) {
        static UnitaTotali of(UnitaKey k, TotaliBeni t) {
            return new UnitaTotali(k.codice(), k.descrizione(), t.numeroTotaleBeni(), t.valoreTotaleBeni());
        }
    }

    @Override
    public Response struttura(@Context HttpServletRequest request, Integer esercizio, String cds) throws Exception {
        LOGGER.debug("REST request per acquisti per struttura.");
        CNRUserContext userContext = (CNRUserContext) securityContext.getUserPrincipal();
        Optional.ofNullable(esercizio).orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Errore, esercizio obbligatorio."));

        try {
            List<Unita_organizzativaBulk> dati = crudComponentSession.find(
                    userContext, Unita_organizzativaBulk.class,
                    "findRegistroInventario", userContext, esercizio, cds);
            LOGGER.debug("Fine REST per acquisti per struttura.");

            List<UnitaTotali> risultato;

            if (cds != null) {
                risultato = dati.stream()
                        .map(u -> UnitaTotali.of(
                                new UnitaKey(u.getCd_unita_organizzativa(), u.getDs_unita_organizzativa()),
                                new TotaliBeni(
                                        Optional.ofNullable(u.getNumeroBeni()).orElse(0L),
                                        Optional.ofNullable(u.getTotaleBeni()).orElse(BigDecimal.ZERO))))
                        .toList();
            } else {
                Map<UnitaKey, TotaliBeni> raggruppati = dati.stream()
                        .filter(u -> Optional.ofNullable(u.getUnita_padre()).isPresent())
                        .collect(Collectors.groupingBy(
                                (Unita_organizzativaBulk u) -> new UnitaKey(
                                        u.getUnita_padre().getCd_unita_organizzativa(),
                                        u.getUnita_padre().getDs_unita_organizzativa()),
                                Collectors.teeing(
                                        Collectors.summingLong((Unita_organizzativaBulk u) ->
                                                Optional.ofNullable(u.getNumeroBeni()).orElse(0L)),
                                        Collectors.reducing(
                                                BigDecimal.ZERO,
                                                (Unita_organizzativaBulk u) ->
                                                        Optional.ofNullable(u.getTotaleBeni()).orElse(BigDecimal.ZERO),
                                                BigDecimal::add),
                                        TotaliBeni::new)));

                risultato = raggruppati.entrySet().stream()
                        .map(e -> UnitaTotali.of(e.getKey(), e.getValue()))
                        .toList();
            }

            return Response.ok(risultato).build();
        } catch (Exception _ex) {
            LOGGER.error("REST request per acquisti per struttura. ERROR: ", _ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Collections.singletonMap("ERROR", _ex)).build();
        }
    }

    @Override
    public Response categoria(@Context HttpServletRequest request, Integer esercizio, String cdUnitaOrganizzativa) throws Exception {
        LOGGER.debug("REST request per acquisti per categoria gruppo.");
        CNRUserContext userContext = (CNRUserContext) securityContext.getUserPrincipal();
        Optional.ofNullable(esercizio).orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Errore, esercizio obbligatorio."));

        try {
            List<Categoria_gruppo_inventBulk> dati = crudComponentSession.find(
                    userContext, Categoria_gruppo_inventBulk.class,
                    "findRegistroInventario", userContext, esercizio, cdUnitaOrganizzativa);
            LOGGER.debug("Fine REST per acquisti per categoria gruppo.");

            List<UnitaTotali>  risultato = dati.stream()
                    .map(u -> UnitaTotali.of(
                            new UnitaKey(u.getCd_categoria_gruppo(), u.getDs_categoria_gruppo()),
                            new TotaliBeni(
                                    Optional.ofNullable(u.getNumeroBeni()).orElse(0L),
                                    Optional.ofNullable(u.getTotaleBeni()).orElse(BigDecimal.ZERO))))
                    .toList();

            return Response.ok(risultato).build();
        } catch (Exception _ex) {
            LOGGER.error("REST request per acquisti per categoria gruppo. ERROR: ", _ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Collections.singletonMap("ERROR", _ex)).build();
        }
    }
}

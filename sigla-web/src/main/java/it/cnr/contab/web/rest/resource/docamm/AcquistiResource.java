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

package it.cnr.contab.web.rest.resource.docamm;

import it.cnr.contab.doccont00.consultazioni.bulk.VControlliPCCBulk;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.web.rest.exception.RestException;
import it.cnr.contab.web.rest.local.docamm.AcquistiLocal;
import it.cnr.contab.web.rest.model.TreeNode;
import it.cnr.jada.ejb.CRUDComponentSession;
import it.cnr.si.service.dto.anagrafica.letture.PersonaEntitaOrganizzativaWebDto;
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
public class AcquistiResource implements AcquistiLocal {
    private final Logger LOGGER = LoggerFactory.getLogger(AcquistiResource.class);
    @Context
    SecurityContext securityContext;

    @EJB
    CRUDComponentSession crudComponentSession;

    @Override
    public Response struttura(@Context HttpServletRequest request, Integer esercizio) throws Exception {
        LOGGER.debug("REST request per acquisti per struttura.");
        CNRUserContext userContext = (CNRUserContext) securityContext.getUserPrincipal();
        final VControlliPCCBulk vControlliPCCBulk = new VControlliPCCBulk();
        Optional.ofNullable(esercizio).orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Errore, esercizio obbligatorio."));

        try {
            List<VControlliPCCBulk> dati =
                    crudComponentSession.find(userContext, VControlliPCCBulk.class, "findRiepilogoPerStruttura", userContext, vControlliPCCBulk, esercizio);
            LOGGER.debug("Fine REST per acquisti per struttura.");
            return Response.ok(transformToTree(dati)).build();
        } catch (Exception _ex) {
            LOGGER.error("REST request per acquisti per struttura. ERROR: ", _ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Collections.singletonMap("ERROR", _ex)).build();
        }
    }

    @Override
    public Response stato(@Context HttpServletRequest request, String codice) throws Exception {
        LOGGER.debug("REST request per acquisti per stato.");
        CNRUserContext userContext = (CNRUserContext) securityContext.getUserPrincipal();
        final VControlliPCCBulk vControlliPCCBulk = new VControlliPCCBulk();
        try {
            List<VControlliPCCBulk> dati =
                    crudComponentSession.find(userContext, VControlliPCCBulk.class, "findRiepilogoPerStato", userContext, vControlliPCCBulk, codice);
            LOGGER.debug("Fine REST per acquisti per stato.");
            return Response.ok(dati.stream().sorted(Comparator.comparing(VControlliPCCBulk::getRiepilogo_stato_esercizio)).collect(Collectors.toList())).build();
        } catch (Exception _ex) {
            LOGGER.error("REST request per acquisti per stato. ERROR: ", _ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Collections.singletonMap("ERROR", _ex)).build();
        }
    }
    public TreeNode transformToTree(List<VControlliPCCBulk> riepilogoList) {
        TreeNode root = new TreeNode("Ente", "Ente", "Ente");

        if (riepilogoList == null || riepilogoList.isEmpty()) {
            return root;
        }

        // Raggruppa per CDS
        Map<String, List<VControlliPCCBulk>> groupedByCds = riepilogoList.stream()
                .collect(Collectors.groupingBy(VControlliPCCBulk::getRiepilogo_cds_codice));

        List<TreeNode> cdsNodes = new ArrayList<>();

        for (Map.Entry<String, List<VControlliPCCBulk>> cdsEntry : groupedByCds.entrySet()) {
            String cdsCode = cdsEntry.getKey();
            List<VControlliPCCBulk> cdsItems = cdsEntry.getValue();

            // Prendi la descrizione dal primo elemento
            String cdsDescription = cdsItems.getFirst().getRiepilogo_cds_descrizione();
            TreeNode cdsNode = new TreeNode(cdsCode + " - " + cdsDescription, cdsCode, cdsDescription);

            // Raggruppa le UO all'interno del CDS
            Map<String, List<VControlliPCCBulk>> groupedByUo = cdsItems.stream()
                    .collect(Collectors.groupingBy(VControlliPCCBulk::getRiepilogo_uo_codice));

            List<TreeNode> uoNodes = new ArrayList<>();

            for (Map.Entry<String, List<VControlliPCCBulk>> uoEntry : groupedByUo.entrySet()) {
                String uoCode = uoEntry.getKey();
                List<VControlliPCCBulk> uoItems = uoEntry.getValue();

                String uoDescription = uoItems.getFirst().getRiepilogo_uo_descrizione();

                // Somma i totali per la UO (foglie)
                BigDecimal uoTotal = uoItems.stream()
                        .map(VControlliPCCBulk::getRiepilogo_totale)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                // Somma i totali per la UO (foglie)
                BigDecimal uoTotalImporto = uoItems.stream()
                        .map(VControlliPCCBulk::getRiepilogo_totale_importo)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                TreeNode uoNode = new TreeNode(uoCode + " - " + uoDescription, uoCode, uoDescription, uoTotalImporto, uoTotal);
                uoNodes.add(uoNode);
            }

            cdsNode.setChildren(uoNodes);
            cdsNodes.add(cdsNode);
        }

        root.setChildren(cdsNodes);

        // Calcola ricorsivamente tutti i totali dei nodi padre
        root.calculateTotalValue();
        root.calculateTotalSecondaryValue();

        return root;
    }
}
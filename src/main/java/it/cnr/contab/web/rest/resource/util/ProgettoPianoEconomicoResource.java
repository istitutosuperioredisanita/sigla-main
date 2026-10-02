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

package it.cnr.contab.web.rest.resource.util;

import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.contab.config00.pdcfin.bulk.Elemento_voceBulk;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;
import it.cnr.contab.doccont00.ejb.SaldoComponentSession;
import it.cnr.contab.pdg00.bulk.Pdg_variazioneBulk;
import it.cnr.contab.progettiric00.core.bulk.Ass_progetto_piaeco_voceBulk;
import it.cnr.contab.progettiric00.core.bulk.ProgettoBulk;
import it.cnr.contab.progettiric00.core.bulk.TipoFinanziamentoBulk;
import it.cnr.contab.progettiric00.core.bulk.V_saldi_voce_progettoBulk;
import it.cnr.contab.progettiric00.tabrif.bulk.Tipo_progettoBulk;
import it.cnr.contab.varstanz00.bulk.Var_stanz_resBulk;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.web.rest.exception.RestException;
import it.cnr.contab.web.rest.local.util.ProgettoPianoEconomicoLocal;
import it.cnr.contab.web.rest.model.*;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.ejb.CRUDComponentSession;
import jakarta.ejb.EJB;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.servlet.http.HttpServletRequest;

import java.util.*;
import java.util.stream.Collectors;

@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class ProgettoPianoEconomicoResource implements ProgettoPianoEconomicoLocal {
    private transient static final Logger logger = LoggerFactory.getLogger(ProgettoPianoEconomicoResource.class);
    @EJB
    private SaldoComponentSession saldoComponentSession;
    @EJB CRUDComponentSession crudComponentSession;
    @Context
    SecurityContext securityContext;

    @Override
    public Response checkPdgPianoEconomico(HttpServletRequest request, String tipoVariazione, Integer esercizio, Long pgVariazioneMin, Long pgVariazioneMax) throws Exception {
        CNRUserContext userContext = (CNRUserContext) securityContext.getUserPrincipal();
        if (tipoVariazione==null || esercizio==null || pgVariazioneMin==null)
            return Response.serverError().entity("Servizio non eseguibile. Parametri di lancio non corretti.").build();

        pgVariazioneMin = Optional.ofNullable(pgVariazioneMin).orElse(Long.valueOf(1));
        pgVariazioneMax = Optional.ofNullable(pgVariazioneMax).orElse(Long.valueOf(pgVariazioneMin));

        StringJoiner anomalie = new StringJoiner("   ********   ");
        if ("COM".equals(tipoVariazione)) {
            for (int pgVariazione = pgVariazioneMin.intValue(); pgVariazione <= pgVariazioneMax; pgVariazione++) {
                try {
                    OggettoBulk pdgVariazione = (OggettoBulk) crudComponentSession.findByPrimaryKey(userContext, new Pdg_variazioneBulk(esercizio, Long.valueOf(pgVariazione)));
                    if (Optional.ofNullable(pdgVariazione).filter(Pdg_variazioneBulk.class::isInstance).map(Pdg_variazioneBulk.class::cast)
                            .filter(el->!el.isAnnullata() && !el.isPropostaProvvisoria()).isPresent()) {
                        userContext.setCd_unita_organizzativa(((Pdg_variazioneBulk) pdgVariazione).getCentro_responsabilita().getCd_unita_organizzativa());
                        saldoComponentSession.checkPdgPianoEconomico(userContext, (Pdg_variazioneBulk) pdgVariazione);
                    }
                } catch (Exception e) {
                    anomalie.add("Variazione di competenza " + esercizio + "/" + pgVariazione + " - Errore: "+e.toString());
                }
            }
        } else if ("RES".equals(tipoVariazione)) {
            for (int pgVariazione = pgVariazioneMin.intValue(); pgVariazione <= pgVariazioneMax; pgVariazione++) {
                try {
                    OggettoBulk variazioneResidua = (OggettoBulk) crudComponentSession.findByPrimaryKey(userContext, new Var_stanz_resBulk(esercizio, Long.valueOf(pgVariazione)));
                    if (Optional.ofNullable(variazioneResidua).filter(Var_stanz_resBulk.class::isInstance).map(Var_stanz_resBulk.class::cast)
                            .filter(el->!el.isAnnullata() && !el.isPropostaProvvisoria()).isPresent()) {
                        userContext.setCd_unita_organizzativa(((Var_stanz_resBulk) variazioneResidua).getCd_cds() + ".000");
                        saldoComponentSession.checkPdgPianoEconomico(userContext, (Var_stanz_resBulk) variazioneResidua);
                    }
                } catch (Exception e) {
                    anomalie.add("Variazione residua " + esercizio + "/" + pgVariazione + " - Errore: "+e.toString());
                }
            }
        }
        if (anomalie.length()==0)
            return Response.ok().entity("Controllo terminato con successo.").build();
        else
            return Response.ok().entity(anomalie.toString()).build();
    }

    @Override
    public Response fondiFunzionamentoUO(@Context HttpServletRequest request, Integer esercizio, String cds, String voce) throws Exception {
        logger.debug("REST request per fondi di funzionamento per uo.");
        CNRUserContext userContext = (CNRUserContext) securityContext.getUserPrincipal();
        Optional.ofNullable(esercizio).orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Errore, esercizio obbligatorio."));
        try {
            List<Unita_organizzativaBulk> dati =
                    crudComponentSession.find(userContext, Unita_organizzativaBulk.class, "findFondiFunzionamento", userContext, esercizio, cds, voce);
            logger.debug("Fine REST per fondi di funzionamento per uo.");
            return Response.ok(
                    dati
                            .stream()
                            .map(FondiFunzionamentoUODTO::new)
                            .collect(Collectors.toList())
            ).build();
        } catch (Exception _ex) {
            logger.error("REST request per fondi di funzionamento per uo. ERROR: ", _ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Collections.singletonMap("ERROR", _ex)).build();
        }
    }

    @Override
    public Response fondiFunzionamentoTipoFinanziamento(@Context HttpServletRequest request, Integer esercizio, String uo) throws Exception {
        logger.debug("REST request per fondi di funzionamento per tipo.");
        CNRUserContext userContext = (CNRUserContext) securityContext.getUserPrincipal();
        Optional.ofNullable(esercizio).orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Errore, esercizio obbligatorio."));
        try {
            List<TipoFinanziamentoBulk> dati =
                    crudComponentSession.find(userContext, TipoFinanziamentoBulk.class, "findFondiFunzionamento", userContext, esercizio, uo);
            logger.debug("Fine REST per fondi di funzionamento per tipo.");
            return Response.ok(
                    dati
                            .stream()
                            .map(FondiFunzionamentoTipoFinanziamentoDTO::new)
                            .collect(Collectors.toList())
            ).build();
        } catch (Exception _ex) {
            logger.error("REST request per fondi di funzionamentoper tipo. ERROR: ", _ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Collections.singletonMap("ERROR", _ex)).build();
        }
    }

    @Override
    public Response fondiFunzionamentoTipoProgetto(@Context HttpServletRequest request, Integer esercizio, String uo) throws Exception {
        logger.debug("REST request per fondi di funzionamento per tipo progetto.");
        CNRUserContext userContext = (CNRUserContext) securityContext.getUserPrincipal();
        Optional.ofNullable(esercizio).orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Errore, esercizio obbligatorio."));
        try {
            List<Tipo_progettoBulk> dati =
                    crudComponentSession.find(userContext, Tipo_progettoBulk.class, "findFondiFunzionamento", userContext, esercizio, uo);
            logger.debug("Fine REST per fondi di funzionamento per tipo.");
            return Response.ok(
                    dati
                            .stream()
                            .map(FondiFunzionamentoTipoProgettoDTO::new)
                            .collect(Collectors.toList())
            ).build();
        } catch (Exception _ex) {
            logger.error("REST request per fondi di funzionamentoper tipo. ERROR: ", _ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Collections.singletonMap("ERROR", _ex)).build();
        }
    }

    @Override
    public Response fondiFunzionamentoElementoVoce(@Context HttpServletRequest request, Integer esercizio, String uo) throws Exception {
        logger.debug("REST request per fondi di funzionamento per elemento voce.");
        CNRUserContext userContext = (CNRUserContext) securityContext.getUserPrincipal();
        Optional.ofNullable(esercizio).orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Errore, esercizio obbligatorio."));
        try {
            List<Elemento_voceBulk> dati =
                    crudComponentSession.find(userContext, Elemento_voceBulk.class, "findFondiFunzionamento", userContext, esercizio, uo);
            logger.debug("Fine REST per fondi di funzionamento per tipo.");
            return Response.ok(
                    dati
                            .stream()
                            .map(FondiFunzionamentoElementoVoceDTO::new)
                            .collect(Collectors.toList())
            ).build();
        } catch (Exception _ex) {
            logger.error("REST request per fondi di funzionamentoper tipo. ERROR: ", _ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Collections.singletonMap("ERROR", _ex)).build();
        }
    }

    @Override
    public Response fondiFunzionamentoEnteFinanziatore(@Context HttpServletRequest request, Integer esercizio, String uo) throws Exception {
        logger.debug("REST request per fondi di funzionamento per ente finanziatore.");
        CNRUserContext userContext = (CNRUserContext) securityContext.getUserPrincipal();
        Optional.ofNullable(esercizio).orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Errore, esercizio obbligatorio."));
        try {
            List<TerzoBulk> dati =
                    crudComponentSession.find(userContext, TerzoBulk.class, "findFondiFunzionamentoEnteFinanziatore", userContext, esercizio, uo);
            logger.debug("Fine REST per fondi di funzionamento per ente finanziatore.");
            return Response.ok(
                    dati
                            .stream()
                            .map(FondiFunzionamentoEnteFinanziatoreDTO::new)
                            .collect(Collectors.toList())
            ).build();
        } catch (Exception _ex) {
            logger.error("REST request per fondi di funzionamentoper ente finanziatore. ERROR: ", _ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Collections.singletonMap("ERROR", _ex)).build();
        }
    }

    @Override
    public Response fondiFunzionamentoDettaglioUO(@Context HttpServletRequest request, Integer esercizio, String uo, String voce) throws Exception {
        logger.debug("REST request per fondi di funzionamento per UO.");
        CNRUserContext userContext = (CNRUserContext) securityContext.getUserPrincipal();
        Optional.ofNullable(esercizio).orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Errore, esercizio obbligatorio."));
        Optional.ofNullable(uo).orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Errore, Unità Organizzativa obbligatorio."));
        try {
            List<ProgettoBulk> dati =
                    crudComponentSession.find(userContext, ProgettoBulk.class, "findFondiFunzionamentoUO", userContext, esercizio, uo, voce);
            logger.debug("Fine REST per fondi di funzionamento per UO.");
            return Response.ok(
                    dati
                            .stream()
                            .map(FondiFunzionamentoProgettoDTO::new)
                            .collect(Collectors.toList())
            ).build();
        } catch (Exception _ex) {
            logger.error("REST request per fondi di funzionamento per UO. ERROR: ", _ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Collections.singletonMap("ERROR", _ex)).build();
        }
    }

    @Override
    public Response fondiFunzionamentoDettaglioTipoFinanziamento(@Context HttpServletRequest request, Integer esercizio, String tipo, String uo) throws Exception {
        logger.debug("REST request per fondi di funzionamento per UO.");
        CNRUserContext userContext = (CNRUserContext) securityContext.getUserPrincipal();
        Optional.ofNullable(esercizio).orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Errore, esercizio obbligatorio."));
        Optional.ofNullable(tipo).orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Errore, Unità Organizzativa obbligatorio."));
        try {
            List<ProgettoBulk> dati =
                    crudComponentSession.find(userContext, ProgettoBulk.class, "findFondiFunzionamentoTipoFinanziamento", userContext, esercizio, tipo, uo);
            logger.debug("Fine REST per fondi di funzionamento per UO.");
            return Response.ok(
                    dati
                            .stream()
                            .map(FondiFunzionamentoProgettoDTO::new)
                            .collect(Collectors.toList())
            ).build();
        } catch (Exception _ex) {
            logger.error("REST request per fondi di funzionamento per UO. ERROR: ", _ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Collections.singletonMap("ERROR", _ex)).build();
        }
    }

    @Override
    public Response fondiFunzionamentoDettaglioTipoProgetto(@Context HttpServletRequest request, Integer esercizio, String tipo, String uo) throws Exception {
        logger.debug("REST request per fondi di funzionamento per UO.");
        CNRUserContext userContext = (CNRUserContext) securityContext.getUserPrincipal();
        Optional.ofNullable(esercizio).orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Errore, esercizio obbligatorio."));
        Optional.ofNullable(tipo).orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Errore, Unità Organizzativa obbligatorio."));
        try {
            List<ProgettoBulk> dati =
                    crudComponentSession.find(userContext, ProgettoBulk.class, "findFondiFunzionamentoTipoProgetto", userContext, esercizio, tipo, uo);
            logger.debug("Fine REST per fondi di funzionamento per UO.");
            return Response.ok(
                    dati
                            .stream()
                            .map(FondiFunzionamentoProgettoDTO::new)
                            .collect(Collectors.toList())
            ).build();
        } catch (Exception _ex) {
            logger.error("REST request per fondi di funzionamento per UO. ERROR: ", _ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Collections.singletonMap("ERROR", _ex)).build();
        }
    }

    @Override
    public Response fondiFunzionamentoDettaglioElementoVoce(@Context HttpServletRequest request, Integer esercizio, String codice, String uo) throws Exception {
        logger.debug("REST request per fondi di funzionamento per UO.");
        CNRUserContext userContext = (CNRUserContext) securityContext.getUserPrincipal();
        Optional.ofNullable(esercizio).orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Errore, esercizio obbligatorio."));
        Optional.ofNullable(codice).orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Errore, Unità Organizzativa obbligatorio."));
        try {
            List<ProgettoBulk> dati =
                    crudComponentSession.find(userContext, ProgettoBulk.class, "findFondiFunzionamentoElementoVoce", userContext, esercizio, codice, uo);
            logger.debug("Fine REST per fondi di funzionamento per UO.");
            return Response.ok(
                    dati
                            .stream()
                            .map(FondiFunzionamentoProgettoDTO::new)
                            .collect(Collectors.toList())
            ).build();
        } catch (Exception _ex) {
            logger.error("REST request per fondi di funzionamento per UO. ERROR: ", _ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Collections.singletonMap("ERROR", _ex)).build();
        }
    }

    @Override
    public Response fondiFunzionamentoDettaglioEnteFinanziatore(@Context HttpServletRequest request, Integer esercizio, String cdTerzo, String uo) throws Exception {
        logger.debug("REST request per fondi di funzionamento per UO.");
        CNRUserContext userContext = (CNRUserContext) securityContext.getUserPrincipal();
        Optional.ofNullable(esercizio).orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Errore, esercizio obbligatorio."));
        Optional.ofNullable(cdTerzo).orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Errore, Ente Finanziatore obbligatorio."));
        try {
            List<ProgettoBulk> dati =
                    crudComponentSession.find(userContext, ProgettoBulk.class, "findFondiFunzionamentoEnteFinanziatore", userContext, esercizio, cdTerzo, uo);
            logger.debug("Fine REST per fondi di funzionamento per UO.");
            return Response.ok(
                    dati
                            .stream()
                            .map(FondiFunzionamentoProgettoDTO::new)
                            .collect(Collectors.toList())
            ).build();
        } catch (Exception _ex) {
            logger.error("REST request per fondi di funzionamento per UO. ERROR: ", _ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Collections.singletonMap("ERROR", _ex)).build();
        }
    }

    @Override
    public Response fondiFunzionamentoProgetto(@Context HttpServletRequest request, Integer esercizio, String cdProgetto, String elementiVoce) throws Exception {
        logger.debug("REST request per fondi di funzionamento per progetto.");
        CNRUserContext userContext = (CNRUserContext) securityContext.getUserPrincipal();
        Optional.ofNullable(esercizio).orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Errore, esercizio obbligatorio."));
        Optional.ofNullable(cdProgetto).orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Errore, Codice progetto obbligatorio."));
        try {
            List<V_saldi_voce_progettoBulk> dati =
                    crudComponentSession.find(userContext, V_saldi_voce_progettoBulk.class, "findByCodiceProgetto", userContext, esercizio, cdProgetto, elementiVoce);
            logger.debug("Fine REST per fondi di funzionamento per progetto.");
            return Response.ok(
                    dati
                            .stream()
                            .map(VociBilancioProgettoDTO::new)
                            .collect(Collectors.toList())
            ).build();
        } catch (Exception _ex) {
            logger.error("REST request per fondi di funzionamento per progetto.ERROR: ", _ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Collections.singletonMap("ERROR", _ex)).build();
        }
    }

    public record VocePianoEconomico(String codice, String descrizione) {}
    public record GruppoVoce(String codice, String descrizione, List<String> elementiVoce) {}

    @Override
    public Response pianoEconomicoProgetto(@Context HttpServletRequest request, Integer esercizio, String cdProgetto) throws Exception {
        logger.debug("REST request per piano economico del progetto.");
        CNRUserContext userContext = (CNRUserContext) securityContext.getUserPrincipal();
        Optional.ofNullable(esercizio).orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Errore, esercizio obbligatorio."));
        Optional.ofNullable(cdProgetto).orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Errore, Codice progetto obbligatorio."));
        try {
            List<Ass_progetto_piaeco_voceBulk> dati =
                    crudComponentSession.find(userContext, Ass_progetto_piaeco_voceBulk.class, "findByCodiceProgetto", userContext, esercizio, cdProgetto);
            logger.debug("Fine REST per fondi di funzionamento per progetto.");
            Map<VocePianoEconomico, List<String>> mappa = dati.stream()
                    .collect(Collectors.groupingBy(
                            a -> new VocePianoEconomico(
                                    a.getProgetto_piano_economico().getVoce_piano_economico().getCd_voce_piano(),
                                    a.getProgetto_piano_economico().getVoce_piano_economico().getDs_voce_piano()
                            ),
                            Collectors.mapping(Ass_progetto_piaeco_voceBulk::getCd_elemento_voce, Collectors.toList())
                    ));
            return Response.ok(mappa.entrySet().stream()
                    .map(entry -> new GruppoVoce(
                            entry.getKey().codice(),
                            entry.getKey().descrizione(),
                            entry.getValue()
                    ))
                    .toList()).build();
        } catch (Exception _ex) {
            logger.error("REST request per fondi di funzionamento per progetto.ERROR: ", _ex);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Collections.singletonMap("ERROR", _ex)).build();
        }
    }
}

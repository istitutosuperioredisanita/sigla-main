package it.cnr.contab.web.rest.resource.config00;

import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.contab.config00.ejb.Linea_attivitaComponentSession;
import it.cnr.contab.config00.latt.bulk.CofogBulk;
import it.cnr.contab.config00.latt.bulk.Gruppo_linea_attivitaBulk;
import it.cnr.contab.config00.latt.bulk.WorkpackageBulk;
import it.cnr.contab.config00.pdcfin.bulk.FunzioneBulk;
import it.cnr.contab.config00.pdcfin.bulk.NaturaBulk;
import it.cnr.contab.prevent01.bulk.Pdg_missioneBulk;
import it.cnr.contab.prevent01.bulk.Pdg_programmaBulk;
import it.cnr.contab.progettiric00.core.bulk.ProgettoBulk;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.web.rest.local.config00.LineaAttivitaLocal;
import it.cnr.contab.web.rest.model.LineaAttivitaDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public class LineaAttivitaResource implements LineaAttivitaLocal {

    private final Logger LOGGER = LoggerFactory.getLogger(LineaAttivitaResource.class);
    @Context
    SecurityContext securityContext;
    @EJB
    Linea_attivitaComponentSession lineaAttivitaComponentSession;

    @Override
    public Response insert(HttpServletRequest request, LineaAttivitaDto lineaAttivita) throws Exception {
        CNRUserContext userContext = (CNRUserContext) securityContext.getUserPrincipal();
        WorkpackageBulk workpackageBulk = new WorkpackageBulk(lineaAttivita.getCentro_responsabilitaKey().getCd_centro_responsabilita(),lineaAttivita.getCd_linea_attivita());
        workpackageBulk.setCd_tipo_linea_attivita(lineaAttivita.getTipoLineaAttivitaKey().getCd_tipo_linea_attivita());
        workpackageBulk.setDenominazione(lineaAttivita.getDenominazione());
        workpackageBulk.setGruppo_linea_attivita(new Gruppo_linea_attivitaBulk(lineaAttivita.getGruppoLineaAttivitaKey().getCd_gruppo_linea_attivita()));
        workpackageBulk.setFunzione(new FunzioneBulk(lineaAttivita.getFunzioneKey().getCd_funzione()));
        workpackageBulk.setNatura(new NaturaBulk(lineaAttivita.getNaturaKey().getCd_natura()));
        workpackageBulk.setDs_linea_attivita(lineaAttivita.getDs_linea_attivita());
        workpackageBulk.setEsercizio_fine(lineaAttivita.getEsercizio_fine());
        workpackageBulk.setEsercizio_inizio(lineaAttivita.getEsercizio_inizio());
        workpackageBulk.setTi_gestione(lineaAttivita.getTi_gestione().gestione);
        workpackageBulk.setProgetto(new ProgettoBulk(lineaAttivita.getProgettoKey().getEsercizio(),
                    lineaAttivita.getProgettoKey().getPg_progetto(),
                    lineaAttivita.getProgettoKey().getTipo_fase()));
        workpackageBulk.setResponsabile(new TerzoBulk(lineaAttivita.getResponsabileKey().getCd_terzo()));
        workpackageBulk.setFl_limite_ass_obblig(lineaAttivita.getFl_limite_ass_obblig());
        workpackageBulk.setCofog( new CofogBulk(lineaAttivita.getCofogKey().getCd_cofog()));
        workpackageBulk.setPdgProgramma( new Pdg_programmaBulk(lineaAttivita.getPdgProgrammaKey().getCd_programma()));
        workpackageBulk.setPdgMissione( new Pdg_missioneBulk(lineaAttivita.getPdgMissioneKey().getCd_missione()));

        lineaAttivitaComponentSession.creaLineaAttivitaWs(userContext,workpackageBulk);
        return Response.status(Response.Status.OK).entity(lineaAttivita).build();
    }

}

package it.cnr.contab.web.rest.resource.config00;

import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.contab.config00.ejb.Linea_attivitaComponentSession;
import it.cnr.contab.config00.latt.bulk.*;
import it.cnr.contab.config00.pdcfin.bulk.FunzioneBulk;
import it.cnr.contab.config00.pdcfin.bulk.NaturaBulk;
import it.cnr.contab.config00.sto.bulk.CdrBulk;
import it.cnr.contab.prevent01.bulk.Pdg_missioneBulk;
import it.cnr.contab.prevent01.bulk.Pdg_programmaBulk;
import it.cnr.contab.progettiric00.core.bulk.ProgettoBulk;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.web.rest.local.config00.LineaAttivitaLocal;
import it.cnr.contab.web.rest.model.LineaAttivitaDto;
import it.cnr.jada.ejb.CRUDComponentSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.Optional;
@Stateless
public class LineaAttivitaResource implements LineaAttivitaLocal {

    private final Logger LOGGER = LoggerFactory.getLogger(LineaAttivitaResource.class);
    @Context
    SecurityContext securityContext;
    @EJB
    CRUDComponentSession crudComponentSession;
    @EJB
    Linea_attivitaComponentSession lineaAttivitaComponentSession;

    @Override
    public Response insert(HttpServletRequest request, LineaAttivitaDto lineaAttivita) throws Exception {
        CNRUserContext userContext = (CNRUserContext) securityContext.getUserPrincipal();
        WorkpackageBulk workpackageBulk = new WorkpackageBulk();
        CdrBulk cdrBulk=( CdrBulk) crudComponentSession.findByPrimaryKey(userContext,
                new CdrBulk(lineaAttivita.getCentro_responsabilitaKey().getCd_centro_responsabilita()));
        workpackageBulk.setCentro_responsabilita(cdrBulk);
        workpackageBulk.setCd_linea_attivita(lineaAttivita.getCd_linea_attivita());
        workpackageBulk.setDenominazione(lineaAttivita.getDenominazione());
        workpackageBulk.setGruppo_linea_attivita(new Gruppo_linea_attivitaBulk(Optional.ofNullable(lineaAttivita.getGruppoLineaAttivitaKey()).map(Gruppo_linea_attivitaKey::getCd_gruppo_linea_attivita).orElse("")));
        workpackageBulk.setFunzione(new FunzioneBulk(lineaAttivita.getFunzioneKey().getCd_funzione()));
        workpackageBulk.setNatura(new NaturaBulk(lineaAttivita.getNaturaKey().getCd_natura()));
        workpackageBulk.setDs_linea_attivita(lineaAttivita.getDs_linea_attivita());
        workpackageBulk.setEsercizio_fine(lineaAttivita.getEsercizio_fine());
        workpackageBulk.setEsercizio_inizio(lineaAttivita.getEsercizio_inizio());
        workpackageBulk.setTi_gestione(lineaAttivita.getTi_gestione().gestione);
         ProgettoBulk progetto = ( ProgettoBulk) crudComponentSession.findByPrimaryKey(userContext,
                new ProgettoBulk(lineaAttivita.getProgettoKey().getEsercizio(),
                        lineaAttivita.getProgettoKey().getPg_progetto(), ProgettoBulk.TIPO_FASE_NON_DEFINITA));

         progetto.setProgettopadre(( ProgettoBulk) crudComponentSession.findByPrimaryKey(userContext,
                 new ProgettoBulk(progetto.getEsercizio_progetto_padre(),
                         progetto.getPg_progetto_padre(), progetto.getTipo_fase_progetto_padre())));

        workpackageBulk.setPdgProgramma((Pdg_programmaBulk)crudComponentSession.findByPrimaryKey(userContext, progetto.getProgettopadre().getPdgProgramma()));

        workpackageBulk.setProgetto2016(progetto);
        workpackageBulk.setResponsabile(new TerzoBulk(lineaAttivita.getResponsabileKey().getCd_terzo()));
        workpackageBulk.setFl_limite_ass_obblig(lineaAttivita.getFl_limite_ass_obblig());
        workpackageBulk.setCofog( new CofogBulk(lineaAttivita.getCofogKey().getCd_cofog()));
        workpackageBulk.setPdgMissione( new Pdg_missioneBulk(lineaAttivita.getPdgMissioneKey().getCd_missione()));
        workpackageBulk.setToBeCreated();
        workpackageBulk=lineaAttivitaComponentSession.creaLineaAttivitaWs(userContext,workpackageBulk);
        return Response.status(Response.Status.OK).entity(lineaAttivita).build();
    }

}

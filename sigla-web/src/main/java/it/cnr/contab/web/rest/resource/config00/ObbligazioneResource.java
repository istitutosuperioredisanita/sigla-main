package it.cnr.contab.web.rest.resource.config00;

import it.cnr.contab.config00.latt.bulk.WorkpackageBulk;
import it.cnr.contab.config00.pdcfin.bulk.Elemento_voceBulk;
import it.cnr.contab.config00.pdcfin.bulk.NaturaBulk;
import it.cnr.contab.config00.sto.bulk.CdrBulk;
import it.cnr.contab.config00.sto.bulk.CdsBulk;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;
import it.cnr.contab.doccont00.core.bulk.*;
import it.cnr.contab.doccont00.ejb.ObbligazioneComponentSession;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.web.rest.exception.RestException;
import it.cnr.contab.web.rest.local.config00.ObbligazioneLocal;
import it.cnr.contab.web.rest.model.ObbligazioneDto;
import it.cnr.contab.web.rest.model.ObbligazioneScadVoceDto;
import it.cnr.contab.web.rest.model.ObbligazioneScadenzarioDto;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.ejb.CRUDComponentSession;
import it.cnr.jada.persistency.PersistencyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.Optional;

@Stateless
public class ObbligazioneResource implements ObbligazioneLocal {
    private final Logger LOGGER = LoggerFactory.getLogger(LineaAttivitaResource.class);
    @Context
    SecurityContext securityContext;
    @EJB
    CRUDComponentSession crudComponentSession;
    @EJB
    ObbligazioneComponentSession obbligazioneComponentSession;

    private void validaObbligazioneDto (CNRUserContext userContext, ObbligazioneDto obbligazioneDto) {
        Optional.ofNullable(obbligazioneDto.getEsercizio()).orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Errore, Esercizio obbligatorio!"));
        Optional.ofNullable(obbligazioneDto.getCdsKey().getCd_unita_organizzativa()).orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Errore, Cds  obbligatorio!"));
        Optional.ofNullable(obbligazioneDto.getUnitaOrganizzativaKey().getCd_unita_organizzativa()).orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Errore, UO  obbligatoria!"));

        Optional.ofNullable(obbligazioneDto.getEsercizio()).filter(x -> userContext.getEsercizio().equals(x)).
                orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Esercizio del contesto diverso da quello dell'Obbligazione!"));
        Optional.ofNullable(obbligazioneDto.getCdsKey().getCd_unita_organizzativa()).filter(x -> userContext.getCd_cds().equals(x)).
                orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "CdS del contesto diverso da quello dell'Obbligazione!"));
        Optional.ofNullable(obbligazioneDto.getUnitaOrganizzativaKey().getCd_unita_organizzativa()).filter(x -> userContext.getCd_unita_organizzativa().equals(x)).
                orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Unità Organizzativa del contesto diversa da quello dell'Obbligazione!"));




    }
    //inizio MapStruct
    private ObbligazioneBulk obbligazioneDtoTodObbligazioneBulk(ObbligazioneDto obbligazioneDto, CNRUserContext userContext) throws ComponentException, RemoteException {
        ObbligazioneBulk obbligazioneBulk = new ObbligazioneBulk();
        obbligazioneBulk.setEsercizio( obbligazioneDto.getEsercizio());
        obbligazioneBulk.setEsercizio_originale(obbligazioneDto.getEsercizio());
        obbligazioneBulk.setCds(( CdsBulk) crudComponentSession.findByPrimaryKey( userContext,new CdsBulk(obbligazioneDto.getCdsKey().getCd_unita_organizzativa())));
        obbligazioneBulk.setUnita_organizzativa(new Unita_organizzativaBulk(obbligazioneDto.getUnitaOrganizzativaKey().getCd_unita_organizzativa()));
        obbligazioneBulk.setDt_registrazione(it.cnr.jada.util.ejb.EJBCommonServices.getServerDate());

        obbligazioneBulk.setDs_obbligazione(obbligazioneDto.getDs_obbligazione());

        obbligazioneBulk.setCd_cds_origine(obbligazioneBulk.getCd_cds());
        obbligazioneBulk.setCd_uo_origine(obbligazioneBulk.getUnita_organizzativa().getCd_unita_organizzativa());
        obbligazioneBulk.setCd_tipo_documento_cont(Numerazione_doc_contBulk.TIPO_OBB);
        obbligazioneBulk.setFl_pgiro(new Boolean(false));
        obbligazioneBulk.setRiportato("N");
        obbligazioneBulk.setFromDocAmm(Boolean.FALSE);
        obbligazioneBulk.setFl_calcolo_automatico(Boolean.FALSE);
        obbligazioneBulk.setFl_spese_costi_altrui(Boolean.FALSE);

        Elemento_voceBulk voce = new it.cnr.contab.config00.pdcfin.bulk.Elemento_voceBulk();
        voce.setEsercizio(obbligazioneDto.getElemento_voceKey().getEsercizio());
        voce.setTi_appartenenza(obbligazioneDto.getElemento_voceKey().getTi_appartenenza());
        voce.setTi_gestione(obbligazioneDto.getElemento_voceKey().getTi_gestione());
        voce.setCd_elemento_voce(obbligazioneDto.getElemento_voceKey().getCd_elemento_voce());
        obbligazioneBulk.setElemento_voce((Elemento_voceBulk)crudComponentSession.findByPrimaryKey(userContext,voce));

        obbligazioneBulk = obbligazioneComponentSession.listaCapitoliPerCdsVoce(userContext, obbligazioneBulk);
        obbligazioneBulk.setCapitoliDiSpesaCdsSelezionatiColl(obbligazioneBulk.getCapitoliDiSpesaCdsColl());

        obbligazioneBulk.setFl_gara_in_corso(obbligazioneDto.getFl_gara_in_corso());
        obbligazioneBulk.setIm_obbligazione(obbligazioneDto.getIm_obbligazione());
        obbligazioneBulk.setIm_costi_anticipati(new java.math.BigDecimal(0));

        obbligazioneBulk.setCd_terzo(obbligazioneDto.getTerzoDto().getCd_terzo());
        obbligazioneBulk.setEsercizio_competenza(obbligazioneDto.getEsercizio());
        obbligazioneBulk.setStato_obbligazione(ObbligazioneBulk.STATO_OBB_DEFINITIVO);
        obbligazioneBulk.setCrudStatus(OggettoBulk.TO_BE_CREATED);

        for (ObbligazioneScadenzarioDto scadenza : obbligazioneDto.getScadenze()) {
            Obbligazione_scadenzarioBulk obb_scadenza = new Obbligazione_scadenzarioBulk();
            obb_scadenza.setToBeCreated();
            obbligazioneBulk.addToObbligazione_scadenzarioColl(obb_scadenza);
            obb_scadenza.setIm_scadenza(scadenza.getIm_scadenza());
            obb_scadenza.setDt_scadenza(new Timestamp(scadenza.getDt_scadenza().getTime()));
            obb_scadenza.setDs_scadenza(scadenza.getDs_scadenza());
            //obb_scadenza.setIm_associato_doc_amm(BigDecimal.ZERO);
            //obb_scadenza.setIm_associato_doc_contabile(BigDecimal.ZERO);

           // BulkList<Obbligazione_scad_voceBulk> listScadVoce= new BulkList<Obbligazione_scad_voceBulk>();
            for ( ObbligazioneScadVoceDto scad_voce :scadenza.getObbligazioneScadVoce()) {
                Obbligazione_scad_voceBulk obb_scad_voce = new Obbligazione_scad_voceBulk();
                obb_scad_voce.setObbligazione_scadenzario(obb_scadenza);
                obb_scad_voce.setToBeCreated();
                obb_scad_voce.setIm_voce(scad_voce.getIm_voce());
                obb_scad_voce.setCd_voce(voce.getCd_voce());
                obb_scad_voce.setTi_gestione(voce.getTi_gestione());
                obb_scad_voce.setTi_appartenenza(voce.getTi_appartenenza());


                WorkpackageBulk gaePrelevamentoFondi = (WorkpackageBulk) crudComponentSession.findByPrimaryKey(userContext, new WorkpackageBulk(
                        scad_voce.getLineaAttivitaKeyDto().getCentro_responsabilitaKey().getCd_centro_responsabilita(),
                        scad_voce.getLineaAttivitaKeyDto().getCd_linea_attivita()));
                gaePrelevamentoFondi.setCentro_responsabilita((CdrBulk) crudComponentSession.findByPrimaryKey(userContext,gaePrelevamentoFondi.getCentro_responsabilita()));
                gaePrelevamentoFondi.setNatura((NaturaBulk) crudComponentSession.findByPrimaryKey(userContext,gaePrelevamentoFondi.getNatura()));

                obb_scad_voce.setLinea_attivita(gaePrelevamentoFondi);
                //Controllo da riattivare
               // if (obb_scad_voce.getLinea_attivita().getTi_gestione().compareTo(obbligazioneBulk.getTi_gestione()) != 0)
                //    throw new RestException(Response.Status.BAD_REQUEST, "Tipo G.a.e. non coerente con la voce");
                Linea_attivitaBulk nuovaLatt = new Linea_attivitaBulk();
                nuovaLatt.setLinea_att(obb_scad_voce.getLinea_attivita());
                nuovaLatt.setPrcImputazioneFin(new BigDecimal(100));
                nuovaLatt.setObbligazione(obbligazioneBulk);
                obb_scadenza.getObbligazione_scad_voceColl().add((obb_scad_voce));
                obbligazioneBulk.getNuoveLineeAttivitaColl().add(nuovaLatt);
            }


        }


        return obbligazioneBulk;
    }
    protected ObbligazioneBulk creaObbligazioneSigla( ObbligazioneBulk obbligazioneBulk, CNRUserContext userContext) throws ComponentException, RemoteException, PersistencyException {

        obbligazioneBulk.setToBeCreated();
        return ( ObbligazioneBulk) obbligazioneComponentSession.creaConBulk(userContext,obbligazioneBulk);
    }

    @Override
    public Response insert(HttpServletRequest request, ObbligazioneDto obbligazioneDto) throws Exception {

        CNRUserContext userContext = (CNRUserContext) securityContext.getUserPrincipal();
        validaObbligazioneDto(userContext, obbligazioneDto);
        ObbligazioneBulk obbligazioneBulk= obbligazioneDtoTodObbligazioneBulk(obbligazioneDto,userContext);
        try {
            ObbligazioneBulk obbligazioneBulk1=creaObbligazioneSigla( obbligazioneBulk,userContext);
            return Response.status(Response.Status.CREATED).entity(obbligazioneDto).build();
        }catch (Throwable e){
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR,String.format(e.getMessage()));
        }

    }

    @Override
    public Response get(String cd_cds, Integer esercizio, Long pg_obbligazione, Integer esercizio_originale) throws Exception {
        try{
            CNRUserContext userContext = (CNRUserContext) securityContext.getUserPrincipal();
            ObbligazioneBulk obbligazioneBulk= ( ObbligazioneBulk) obbligazioneComponentSession.findObbligazione(userContext, new ObbligazioneBulk(cd_cds,esercizio,esercizio_originale,pg_obbligazione));
            obbligazioneBulk= ( ObbligazioneBulk)obbligazioneComponentSession.inizializzaBulkPerModifica(userContext,obbligazioneBulk);
            return Response.status(Response.Status.OK).entity(new ObbligazioneDto( )).build();
        }catch (Throwable e){
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR,String.format(e.getMessage()));
        }
    }

    @Override
    public Response delete(String cd_cds, Integer esercizio, Long pg_obbligazione, Integer esercizio_originale) throws Exception {
        throw new RestException(Response.Status.METHOD_NOT_ALLOWED,String.format("La Funzione non è implemetata"));
    }

    @Override
    public Response update(String cd_cds, Integer esercizio, Long pg_obbligazione, Integer esercizio_originale) throws Exception {
        throw new RestException(Response.Status.METHOD_NOT_ALLOWED,String.format("La Funzione non è implemetata"));
    }
}

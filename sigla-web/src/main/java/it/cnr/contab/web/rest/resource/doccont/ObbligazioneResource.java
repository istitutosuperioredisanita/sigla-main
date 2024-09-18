package it.cnr.contab.web.rest.resource.doccont;

import it.cnr.contab.anagraf00.core.bulk.TerzoKey;
import it.cnr.contab.config00.contratto.bulk.ContrattoKey;
import it.cnr.contab.config00.ejb.Unita_organizzativaComponentSession;
import it.cnr.contab.config00.latt.bulk.WorkpackageBulk;
import it.cnr.contab.config00.pdcfin.bulk.Elemento_voceBulk;
import it.cnr.contab.config00.pdcfin.bulk.Elemento_voceKey;
import it.cnr.contab.config00.pdcfin.bulk.NaturaBulk;
import it.cnr.contab.config00.sto.bulk.*;
import it.cnr.contab.doccont00.core.bulk.*;
import it.cnr.contab.doccont00.ejb.ObbligazioneComponentSession;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.web.rest.exception.RestException;
import it.cnr.contab.web.rest.local.config00.ObbligazioneLocal;
import it.cnr.contab.web.rest.model.*;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Stateless
public class ObbligazioneResource implements ObbligazioneLocal {
    private final Logger LOGGER = LoggerFactory.getLogger(ObbligazioneResource.class);
    @Context
    SecurityContext securityContext;
    @EJB
    CRUDComponentSession crudComponentSession;
    @EJB
    ObbligazioneComponentSession obbligazioneComponentSession;
    @EJB
    Unita_organizzativaComponentSession unita_organizzativaComponentSession;

    private void validaContestoObbligazione(CNRUserContext userContext,
                                                Integer esericizioObbligazione,
                                                String cdCdsObbligazione,
                                                String cdUoObbligazione) throws ComponentException, RemoteException {

        Unita_organizzativa_enteBulk uoEnte = (Unita_organizzativa_enteBulk) unita_organizzativaComponentSession.getUoEnte( userContext);

        Optional.ofNullable(userContext.getEsercizio()).orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Errore, Esercizio del Contesto obbligatorio!"));
        Optional.ofNullable(userContext.getCd_cds()).orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Errore, Cds del Contesto obbligatorio !"));
        Optional.ofNullable(userContext.getCd_unita_organizzativa()).orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Errore, Uo del Contesto obbligatorio!"));

        Optional.ofNullable(esericizioObbligazione).orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Errore, Esercizio obbligatorio!"));
        Optional.ofNullable(cdCdsObbligazione).orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Errore, Cds  obbligatorio!"));
        Optional.ofNullable(cdUoObbligazione).orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Errore, UO  obbligatoria!"));

        Unita_organizzativaBulk unitaCds =  (Unita_organizzativaBulk) unita_organizzativaComponentSession.findUOByCodice(userContext, userContext.getCd_unita_organizzativa());
        Optional.ofNullable(unitaCds).orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Errore, Unità Operativa non presente!"));
        Optional.ofNullable(unitaCds).filter(x->x.getUnita_padre().getFl_cds()&& x.getCd_unita_padre().equalsIgnoreCase(userContext.getCd_cds())).
                orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "UO selezionata non è del Cds"));

        Optional.ofNullable(esericizioObbligazione).filter(x -> userContext.getEsercizio().equals(x)).
                orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Esercizio del contesto diverso da quello dell'Obbligazione!"));
        Optional.ofNullable(cdCdsObbligazione).filter(x -> userContext.getCd_cds().equals(x)).
                orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "CdS del contesto diverso da quello dell'Obbligazione!"));
        Optional.ofNullable(cdUoObbligazione).filter(x -> userContext.getCd_unita_organizzativa().equals(x)).
                orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Unità Organizzativa del contesto diversa da quello dell'Obbligazione!"));


        Optional.ofNullable(cdUoObbligazione).filter(x -> userContext.getCd_unita_organizzativa().equals(x)).
                orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Unità Organizzativa del contesto diversa da quello dell'Obbligazione!"));

        Optional.ofNullable(uoEnte).filter(x -> ( !userContext.getCd_unita_organizzativa().equals(x.getCd_unita_organizzativa()))).
                orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Funzione non consentita per la Uo Ente"));
    }
    private void validaScadenzeVoce(CNRUserContext userContext, List<ObbligazioneScadVoceDto> scadenzeVoce, Integer numScadenza){
        Integer numScadenzaVoce= 0;

        if ( !Optional.ofNullable(scadenzeVoce).isPresent())
            throw new RestException(Response.Status.BAD_REQUEST, "Mancano le informazioni delle voci per le scadenze!");
        for ( ObbligazioneScadVoceDto scadenzaVoce : scadenzeVoce){
                    numScadenzaVoce++;
                    if ( !Optional.ofNullable(scadenzaVoce.getLineaAttivitaKeyDto()).isPresent())
                        throw new RestException(Response.Status.BAD_REQUEST, "Manca l'informazione della linea Attività della scadenza voce "+numScadenzaVoce +" della scadenza "+ numScadenza);
                    if ( !Optional.ofNullable(scadenzaVoce.getLineaAttivitaKeyDto()).map( e->e.getCentro_responsabilitaKey()).isPresent())
                        throw new RestException(Response.Status.BAD_REQUEST, "Mancano l'informazione del Centro Reponsabilia della  linea Attività della scadenza voce "+numScadenzaVoce +" della scadenza "+ numScadenza);
                    if ( !Optional.ofNullable(scadenzaVoce.getLineaAttivitaKeyDto()).map( e->e.getCd_linea_attivita()).isPresent())
                        throw new RestException(Response.Status.BAD_REQUEST, "Mancano l'informazione del codice della linea Attività della scadenza voce "+numScadenzaVoce +" della scadenza "+ numScadenza);
                    if ( !Optional.ofNullable(scadenzaVoce.getIm_voce()).isPresent())
                        throw new RestException(Response.Status.BAD_REQUEST, "Manca l'importo della scadenza Voce "+numScadenzaVoce+" della scadenza "+ numScadenza);
                    }

    }
    private void validaScadenze(CNRUserContext userContext, List<ObbligazioneScadenzarioDto> scadenze){
        Integer numScadenza= 0;
        if ( !Optional.ofNullable(scadenze).isPresent())
            throw new RestException(Response.Status.BAD_REQUEST, "Mancano le scadenze!");
        for ( ObbligazioneScadenzarioDto scadenza : scadenze){
                    numScadenza++;
                    if ( !Optional.ofNullable(scadenza.getDs_scadenza()).isPresent())
                        throw new RestException(Response.Status.BAD_REQUEST, "Mancano la descrizione della scadenza "+numScadenza);
                    if ( !Optional.ofNullable(scadenza.getIm_scadenza()).isPresent())
                        throw new RestException(Response.Status.BAD_REQUEST, "Manca l'importo della scadenza "+numScadenza);
                    if ( !Optional.ofNullable(scadenza.getDt_scadenza()).isPresent())
                        throw new RestException(Response.Status.BAD_REQUEST, "Mancano la data della scadenza "+numScadenza);
                    Timestamp today=it.cnr.jada.util.ejb.EJBCommonServices.getServerDate();
                    if ( //  data obbligazione != data scadenza && data_obbligazione >= data_scadenza
                            !(today.after(scadenza.getDt_scadenza()) && today.before(scadenza.getDt_scadenza())) &&
                                    today.after(scadenza.getDt_scadenza()))
                        throw new RestException(Response.Status.BAD_REQUEST, "La data della scadenza "+ numScadenza+ " è antecedente ad oggi");
                    validaScadenzeVoce(userContext,scadenza.getObbligazioneScadVoce(), numScadenza);

                };

    }
    private void validaObbligazioneDto (CNRUserContext userContext, ObbligazioneDto obbligazioneDto) throws ComponentException, RemoteException {
        validaContestoObbligazione(userContext,obbligazioneDto.getEsercizio(),
                            obbligazioneDto.getCdsKey().getCd_unita_organizzativa(),
                            obbligazioneDto.getUnitaOrganizzativaKey().getCd_unita_organizzativa());


        if ( !Optional.ofNullable(obbligazioneDto.getEsercizio_originale()).isPresent())
            throw new RestException(Response.Status.BAD_REQUEST,String.format("L'esercizio orginale è obbligatorio"));

        if ( obbligazioneDto.getEsercizio().compareTo(obbligazioneDto.getEsercizio_originale())!=0)
            throw new RestException(Response.Status.BAD_REQUEST,String.format("E' possibile creare solo Obbligazione in comptenza.L'esercizio originale deve essere uguale all'esercizio"));

        if ( Optional.ofNullable(obbligazioneDto.getPg_obbligazione()).isPresent())
            throw new RestException(Response.Status.BAD_REQUEST,String.format("Il pg obbligazione deve essere null in creazione"));

        if ( !Optional.ofNullable(obbligazioneDto.getDs_obbligazione()).isPresent())
            throw new RestException(Response.Status.BAD_REQUEST,String.format("La descrizione è Obbligatoria"));

        if ( !Optional.ofNullable(obbligazioneDto.getTerzoKey()).isPresent())
            throw new RestException(Response.Status.BAD_REQUEST,String.format("Il terzo è Obbligatorio"));

        if ( !Optional.ofNullable(obbligazioneDto.getElemento_voceKey()).isPresent())
            throw new RestException(Response.Status.BAD_REQUEST,String.format("La voce è Obbligatoria"));
        if ( !Optional.ofNullable(obbligazioneDto.getElemento_voceKey().getCd_elemento_voce()).isPresent())
            throw new RestException(Response.Status.BAD_REQUEST,String.format("Il codice voce è Obbligatoria"));
        if ( !Optional.ofNullable(obbligazioneDto.getElemento_voceKey().getTi_appartenenza()).isPresent())
            throw new RestException(Response.Status.BAD_REQUEST,String.format("L'informazione appartenenza della voce è Obbligatoria"));
        if ( !Optional.ofNullable(obbligazioneDto.getElemento_voceKey().getTi_gestione()).isPresent())
            throw new RestException(Response.Status.BAD_REQUEST,String.format("L'informazione gestione della voce è Obbligatoria"));
        if ( !Optional.ofNullable(obbligazioneDto.getIm_obbligazione()).isPresent())
            throw new RestException(Response.Status.BAD_REQUEST,String.format("L'importo dell'Obbligazione è Obbligatorio"));
        if ( obbligazioneDto.getIm_obbligazione().compareTo(BigDecimal.ZERO)<0)
            throw new RestException(Response.Status.BAD_REQUEST,String.format("L'importo dell'Obbligazione deve essere maggiore di Zero"));
        if ( !Optional.ofNullable(obbligazioneDto.getStatoObbligazione()).isPresent())
            throw new RestException(Response.Status.BAD_REQUEST,String.format("Lo stato dell'Obbligazione deve essere maggiore di Zero"));
        Optional.ofNullable(obbligazioneDto.getStatoObbligazione().getStato()).
                filter(x -> Stream.of(EnumStatoObbligazione.PROVVISORIO.stato,EnumStatoObbligazione.DEFINITIVO.stato)
                        .anyMatch(y -> y.equals(x)))
                .orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, String.format("Lo stato dell'obbligazione in creazione deve essere o provvisiorio o definitivo")));

        validaScadenze( userContext,obbligazioneDto.getScadenze());


    }
    //inizio MapStruct
    private ObbligazioneBulk obbligazioneDtoTodObbligazioneBulk(ObbligazioneDto obbligazioneDto, CNRUserContext userContext) throws ComponentException, RemoteException {
        ObbligazioneOrdBulk obbligazioneBulk = new ObbligazioneOrdBulk();
        obbligazioneBulk.setEsercizio( obbligazioneDto.getEsercizio());
        obbligazioneBulk.setEsercizio_originale(obbligazioneDto.getEsercizio_originale());
        obbligazioneBulk.setCds(( CdsBulk) crudComponentSession.findByPrimaryKey( userContext,new CdsBulk(obbligazioneDto.getCdsKey().getCd_unita_organizzativa())));
        obbligazioneBulk.setUnita_organizzativa(( Unita_organizzativaBulk) crudComponentSession.findByPrimaryKey(userContext,new Unita_organizzativaBulk(obbligazioneDto.getUnitaOrganizzativaKey().getCd_unita_organizzativa())));
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

        obbligazioneBulk = (ObbligazioneOrdBulk) obbligazioneComponentSession.listaCapitoliPerCdsVoce(userContext, obbligazioneBulk);
        obbligazioneBulk.setCapitoliDiSpesaCdsSelezionatiColl(obbligazioneBulk.getCapitoliDiSpesaCdsColl());

        obbligazioneBulk.setFl_gara_in_corso(obbligazioneDto.getFl_gara_in_corso());
        obbligazioneBulk.setIm_obbligazione(obbligazioneDto.getIm_obbligazione());
        obbligazioneBulk.setIm_costi_anticipati(new java.math.BigDecimal(0));

        obbligazioneBulk.setCd_terzo(obbligazioneDto.getTerzoKey().getCd_terzo());
        obbligazioneBulk.setEsercizio_competenza(obbligazioneDto.getEsercizio());
        obbligazioneBulk.setStato_obbligazione(obbligazioneDto.getStatoObbligazione().stato);
        obbligazioneBulk.setCrudStatus(OggettoBulk.TO_BE_CREATED);

        for (ObbligazioneScadenzarioDto scadenza : obbligazioneDto.getScadenze()) {
            Obbligazione_scadenzarioBulk obb_scadenza = new Obbligazione_scadenzarioBulk();
            obb_scadenza.setToBeCreated();
            obbligazioneBulk.addToObbligazione_scadenzarioColl(obb_scadenza);
            obb_scadenza.setIm_scadenza(scadenza.getIm_scadenza());

            obb_scadenza.setDt_scadenza(Optional.ofNullable(scadenza.getDt_scadenza())
                    .map(esercizio -> new Timestamp(scadenza.getDt_scadenza().getTime()))
                    .orElse(it.cnr.jada.util.ejb.EJBCommonServices.getServerDate()));

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
                if ( !Optional.ofNullable(gaePrelevamentoFondi).isPresent())
                    throw new RestException(Response.Status.BAD_REQUEST,String.format("La Linea Attivita centro di responsabilità "+
                            scad_voce.getLineaAttivitaKeyDto().getCentro_responsabilitaKey().getCd_centro_responsabilita()+
                            " e codice "+scad_voce.getLineaAttivitaKeyDto().getCd_linea_attivita()+ " non è presente!!"));
                gaePrelevamentoFondi.setCentro_responsabilita((CdrBulk) crudComponentSession.findByPrimaryKey(userContext,gaePrelevamentoFondi.getCentro_responsabilita()));
                gaePrelevamentoFondi.setNatura((NaturaBulk) crudComponentSession.findByPrimaryKey(userContext,gaePrelevamentoFondi.getNatura()));


                obb_scad_voce.setLinea_attivita(gaePrelevamentoFondi);
                Optional.ofNullable(obb_scad_voce.getLinea_attivita().getTi_gestione()).
                        filter(x -> Stream.of(WorkpackageBulk.TI_GESTIONE_ENTRAMBE,WorkpackageBulk.TI_GESTIONE_SPESE)
                                .anyMatch(y -> y.equals(x)))
                        .orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, String.format("La Linea Attivita centro di responsabilità " +
                                scad_voce.getLineaAttivitaKeyDto().getCentro_responsabilitaKey().getCd_centro_responsabilita()+
                                " e codice "+scad_voce.getLineaAttivitaKeyDto().getCd_linea_attivita()+ " non è utilizzabile in spesa!!")));


                obb_scadenza.getObbligazione_scad_voceColl().add((obb_scad_voce));
                Linea_attivitaBulk nuovaLatt = new Linea_attivitaBulk();
                nuovaLatt.setLinea_att(obb_scad_voce.getLinea_attivita());
                if (obb_scad_voce.getObbligazione_scadenzario().getIm_scadenza().compareTo(new BigDecimal(0)) != 0)
                    nuovaLatt.setPrcImputazioneFin(obb_scad_voce.getIm_voce().multiply(new BigDecimal(100)).divide(obb_scad_voce.getObbligazione_scadenzario().getIm_scadenza(), 2, BigDecimal.ROUND_HALF_UP));


                nuovaLatt.setObbligazione(obbligazioneBulk);

                obbligazioneBulk.getNuoveLineeAttivitaColl().add(nuovaLatt);
            }
        }
        return obbligazioneBulk;
    }

    private List<ObbligazioneScadVoceDto> getScadenzeVoce(List<Obbligazione_scad_voceBulk> scadenzeVoce){
        List<ObbligazioneScadVoceDto> scadenzeVoceDto = new ArrayList<ObbligazioneScadVoceDto>();
        for (Obbligazione_scad_voceBulk scadenzaVoce: scadenzeVoce) {
            ObbligazioneScadVoceDto obbligazioneScadVoceDto = new ObbligazioneScadVoceDto();
            obbligazioneScadVoceDto.setIm_voce(scadenzaVoce.getIm_voce());
            obbligazioneScadVoceDto.setLineaAttivitaKeyDto(new LineaAttivitaKeyDto( scadenzaVoce.getCd_centro_responsabilita(),scadenzaVoce.getCd_linea_attivita()));
            obbligazioneScadVoceDto.setPercentuale(scadenzaVoce.getPrc());
            scadenzeVoceDto.add(obbligazioneScadVoceDto);
        }
        return scadenzeVoceDto;
    }
    private List<ObbligazioneScadenzarioDto> getScadenze(List<Obbligazione_scadenzarioBulk> scadenze){
        List<ObbligazioneScadenzarioDto> scadenzeDto = new ArrayList<ObbligazioneScadenzarioDto>();
        for (Obbligazione_scadenzarioBulk scadenza: scadenze) {
            ObbligazioneScadenzarioDto obbligazioneScadenzarioDto = new ObbligazioneScadenzarioDto();
            obbligazioneScadenzarioDto.setDs_scadenza(scadenza.getDs_scadenza());
            obbligazioneScadenzarioDto.setIm_scadenza(scadenza.getIm_scadenza());
            obbligazioneScadenzarioDto.setIm_associato_doc_amm(scadenza.getIm_associato_doc_amm());
            obbligazioneScadenzarioDto.setIm_associato_doc_contabile(scadenza.getIm_associato_doc_contabile());
            obbligazioneScadenzarioDto.setObbligazioneScadVoce(getScadenzeVoce(scadenza.getObbligazione_scad_voceColl()));
            obbligazioneScadenzarioDto.setPg_obbligazione_scadenzario(scadenza.getPg_obbligazione_scadenzario());
            scadenzeDto.add(obbligazioneScadenzarioDto);
        }
        return scadenzeDto;
    }
    private ObbligazioneDto obbligazioneBulkToObbligazioneDto(ObbligazioneBulk obbligazioneBulk) throws ComponentException, RemoteException {
        ObbligazioneDto obbligazioneDto = new ObbligazioneDto();
        obbligazioneDto.setCdsKey( new CdsKey(obbligazioneBulk.getCds().getCd_unita_organizzativa()));
        obbligazioneDto.setEsercizio( obbligazioneBulk.getEsercizio());
        obbligazioneDto.setEsercizio_originale(obbligazioneBulk.getEsercizio_originale());
        obbligazioneDto.setPg_obbligazione(obbligazioneBulk.getPg_obbligazione());
        obbligazioneDto.setDs_obbligazione(obbligazioneBulk.getDs_obbligazione());
        obbligazioneDto.setIm_obbligazione(obbligazioneBulk.getIm_obbligazione());
        obbligazioneDto.setStatoObbligazione(EnumStatoObbligazione.getValueFrom(obbligazioneBulk.getStato_obbligazione()));
        obbligazioneDto.setUnitaOrganizzativaKey(new Unita_organizzativaKey(obbligazioneBulk.getUnita_organizzativa().getCd_unita_organizzativa()));
        obbligazioneDto.setFl_gara_in_corso(obbligazioneBulk.getFl_gara_in_corso());
        Optional.ofNullable(obbligazioneBulk.getContratto()).ifPresent(e-> {
            obbligazioneDto.setContrattoKey(new ContrattoKey(e.getEsercizio(),e.getStato(),e.getPg_contratto()));
        });
        obbligazioneDto.setTerzoKey(new TerzoKey( obbligazioneBulk.getCreditore().getCd_terzo()));

        obbligazioneDto.setElemento_voceKey(new Elemento_voceKey(obbligazioneBulk.getElemento_voce().getCd_elemento_voce(),
                    obbligazioneBulk.getElemento_voce().getEsercizio(),
                obbligazioneBulk.getElemento_voce().getTi_appartenenza(),
                obbligazioneBulk.getElemento_voce().getTi_gestione()));
        obbligazioneDto.setScadenze(getScadenze(obbligazioneBulk.getObbligazione_scadenzarioColl() ) );
        return obbligazioneDto;

    }

    protected ObbligazioneBulk creaObbligazioneSigla( ObbligazioneBulk obbligazioneBulk, CNRUserContext userContext) throws ComponentException, RemoteException, PersistencyException {
        obbligazioneBulk = (ObbligazioneBulk)  obbligazioneComponentSession.inizializzaBulkPerInserimento(userContext,obbligazioneBulk);
        obbligazioneBulk.setToBeCreated();
        return ( ObbligazioneBulk) obbligazioneComponentSession.creaObbligazioneWs(userContext,obbligazioneBulk);
    }

    @Override
    public Response insert(HttpServletRequest request, ObbligazioneDto obbligazioneDto) throws Exception {
        CNRUserContext userContext = (CNRUserContext) securityContext.getUserPrincipal();
        validaObbligazioneDto(userContext, obbligazioneDto);
        try {

            ObbligazioneBulk obbligazioneBulk= obbligazioneDtoTodObbligazioneBulk(obbligazioneDto,userContext);

            return Response.status(Response.Status.CREATED).entity(obbligazioneBulkToObbligazioneDto( creaObbligazioneSigla( obbligazioneBulk,userContext))).build();
        }catch (Throwable e){
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR,String.format(e.getMessage()));
        }

    }

    @Override
    public Response get(String cd_cds, Integer esercizio, Long pg_obbligazione, Integer esercizio_originale) throws Exception {
        try{
            CNRUserContext userContext = (CNRUserContext) securityContext.getUserPrincipal();
            validaContestoObbligazione( userContext,esercizio,cd_cds,userContext.getCd_unita_organizzativa());
            ObbligazioneBulk obbligazioneBulk= ( ObbligazioneBulk) obbligazioneComponentSession.findObbligazione(userContext, new ObbligazioneBulk(cd_cds,esercizio,esercizio_originale,pg_obbligazione));
            if ( !Optional.ofNullable(obbligazioneBulk).isPresent())
                throw new RestException(Response.Status.NOT_FOUND,String.format("Obbligazione non presente!"));
            obbligazioneBulk= ( ObbligazioneBulk)obbligazioneComponentSession.inizializzaBulkPerModifica(userContext,obbligazioneBulk);
            return Response.status(Response.Status.OK).entity(obbligazioneBulkToObbligazioneDto( obbligazioneBulk )).build();
        }catch (Throwable e){
            if ( e instanceof RestException)
                throw e;
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR,String.format(e.getMessage()));
        }
    }
    @Override
    public Response delete(String cd_cds, Integer esercizio, Long pg_obbligazione, Integer esercizio_originale) throws Exception {
        try{
            CNRUserContext userContext = (CNRUserContext) securityContext.getUserPrincipal();
            validaContestoObbligazione( userContext,esercizio,cd_cds,userContext.getCd_unita_organizzativa());
            ObbligazioneBulk obbligazioneBulk= ( ObbligazioneBulk) obbligazioneComponentSession.findObbligazione(userContext, new ObbligazioneBulk(cd_cds,esercizio,esercizio_originale,pg_obbligazione));
            if ( !Optional.ofNullable(obbligazioneBulk).isPresent())
                return Response.status(Response.Status.NO_CONTENT).build();

            Boolean result=obbligazioneComponentSession.deleteObbligazioneWs(userContext,obbligazioneBulk);
            if ( result)
                return Response.status(Response.Status.OK).build();

            return Response.status(Response.Status.NO_CONTENT).build();

        }catch (Throwable e){
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR,String.format(e.getMessage()));
        }
    }

    @Override
    public Response update(String cd_cds, Integer esercizio, Long pg_obbligazione, Integer esercizio_originale) throws Exception {
        throw new RestException(Response.Status.METHOD_NOT_ALLOWED,String.format("La Funzione non è implemetata"));
    }
}

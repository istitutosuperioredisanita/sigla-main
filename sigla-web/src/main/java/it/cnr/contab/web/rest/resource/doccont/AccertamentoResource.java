package it.cnr.contab.web.rest.resource.doccont;

import it.cnr.contab.anagraf00.core.bulk.TerzoKey;
import it.cnr.contab.config00.contratto.bulk.ContrattoKey;
import it.cnr.contab.config00.ejb.Unita_organizzativaComponentSession;
import it.cnr.contab.config00.latt.bulk.WorkpackageBulk;
import it.cnr.contab.config00.pdcfin.bulk.Elemento_voceBulk;
import it.cnr.contab.config00.pdcfin.bulk.Elemento_voceKey;
import it.cnr.contab.config00.pdcfin.bulk.NaturaBulk;
import it.cnr.contab.config00.pdcfin.bulk.V_voce_f_partita_giroBulk;
import it.cnr.contab.config00.sto.bulk.*;
import it.cnr.contab.doccont00.core.bulk.*;
import it.cnr.contab.doccont00.ejb.AccertamentoComponentSession;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.web.rest.exception.RestException;
import it.cnr.contab.web.rest.local.config00.AccertamentoLocal;
import it.cnr.contab.web.rest.model.AccertamentoDto;
import it.cnr.contab.web.rest.model.AccertamentoScadVoceDto;
import it.cnr.contab.web.rest.model.AccertamentoScadenzarioDto;
import it.cnr.contab.web.rest.model.LineaAttivitaKeyDto;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.ejb.CRUDComponentSession;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.util.RemoteIterator;
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
public class AccertamentoResource implements AccertamentoLocal {
    private final Logger LOGGER = LoggerFactory.getLogger(AccertamentoResource.class);
    @Context
    SecurityContext securityContext;
    @EJB
    CRUDComponentSession crudComponentSession;
    @EJB
    AccertamentoComponentSession accertamentoComponentSession;
    @EJB
    Unita_organizzativaComponentSession unita_organizzativaComponentSession;

    private void validaContestoAccertamento(CNRUserContext userContext,
                                                Integer esercizio,
                                                String cdCds,
                                                String cdUo) throws ComponentException, RemoteException {

        Unita_organizzativa_enteBulk uoEnte = (Unita_organizzativa_enteBulk) unita_organizzativaComponentSession.getUoEnte( userContext);

        Optional.ofNullable(userContext.getEsercizio()).orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Errore, Esercizio del Contesto obbligatorio!"));
        Optional.ofNullable(userContext.getCd_cds()).orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Errore, Cds del Contesto obbligatorio !"));
        Optional.ofNullable(userContext.getCd_unita_organizzativa()).orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Errore, Uo del Contesto obbligatorio!"));

        Optional.ofNullable(esercizio).orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Errore, Esercizio obbligatorio!"));
        Optional.ofNullable(cdCds).orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Errore, Cds  obbligatorio!"));
        Optional.ofNullable(cdUo).orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Errore, UO  obbligatoria!"));

        Unita_organizzativaBulk unitaCds =  (Unita_organizzativaBulk) unita_organizzativaComponentSession.findUOByCodice(userContext, userContext.getCd_unita_organizzativa());
        Optional.ofNullable(unitaCds).orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Errore, Unità Operativa non presente!"));
        Optional.ofNullable(unitaCds).filter(x->x.getUnita_padre().getFl_cds()&& x.getCd_unita_padre().equalsIgnoreCase(userContext.getCd_cds())).
                orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "UO selezionata non è del Cds"));

        Optional.ofNullable(esercizio).filter(x -> userContext.getEsercizio().equals(x)).
                orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Esercizio del contesto diverso da quello dell'Accertamento!"));
        Optional.ofNullable(cdCds).filter(x -> userContext.getCd_cds().equals(x)).
                orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "CdS del contesto diverso da quello dell'Accertamento!"));
        Optional.ofNullable(cdUo).filter(x -> userContext.getCd_unita_organizzativa().equals(x)).
                orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Unità Organizzativa del contesto diversa da quello dell'Accertamento!"));

        Optional.ofNullable(uoEnte).filter(x -> ( !userContext.getCd_unita_organizzativa().equals(x.getCd_unita_organizzativa()))).
                orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Funzione non consentita per la Uo Ente"));


    }
    private void validaScadenzeVoce(CNRUserContext userContext, List<AccertamentoScadVoceDto> scadenzeVoce, Integer numScadenza){
        Integer numScadenzaVoce= 0;

        if ( !Optional.ofNullable(scadenzeVoce).isPresent())
            throw new RestException(Response.Status.BAD_REQUEST, "Mancano le informazioni delle voci per le scadenze!");
        for ( AccertamentoScadVoceDto scadenzaVoce : scadenzeVoce){
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
    private void validaScadenze(CNRUserContext userContext, List<AccertamentoScadenzarioDto> scadenze){
        Integer numScadenza= 0;
        if ( !Optional.ofNullable(scadenze).isPresent())
            throw new RestException(Response.Status.BAD_REQUEST, "Mancano le scadenze!");
        for ( AccertamentoScadenzarioDto scadenza : scadenze){
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
                    validaScadenzeVoce(userContext,scadenza.getAccertamentoScadVoce(), numScadenza);

                };

    }
    private void validaAccertamentoDto (CNRUserContext userContext, AccertamentoDto accertamentoDto) throws ComponentException, RemoteException {
        validaContestoAccertamento(userContext,accertamentoDto.getEsercizio(),
                accertamentoDto.getCdsOrgineKey().getCd_unita_organizzativa(),
                accertamentoDto.getUnitaOrganizzativaOrigineKey().getCd_unita_organizzativa());


        if ( !Optional.ofNullable(accertamentoDto.getEsercizio_originale()).isPresent())
            throw new RestException(Response.Status.BAD_REQUEST,String.format("L'esercizio orginale è obbligatorio"));

        if ( accertamentoDto.getEsercizio().compareTo(accertamentoDto.getEsercizio_originale())!=0)
            throw new RestException(Response.Status.BAD_REQUEST,String.format("E' possibile creare solo Accertamenti in competenza.L'esercizio originale deve essere uguale all'esercizio"));

        if ( Optional.ofNullable(accertamentoDto.getPg_accertamento()).isPresent())
            throw new RestException(Response.Status.BAD_REQUEST,String.format("Il pg accertamento deve essere null in creazione"));

        if ( !Optional.ofNullable(accertamentoDto.getDs_accertamento()).isPresent())
            throw new RestException(Response.Status.BAD_REQUEST,String.format("La descrizione è Obbligatoria"));

        if ( !Optional.ofNullable(accertamentoDto.getTerzoKey()).isPresent())
            throw new RestException(Response.Status.BAD_REQUEST,String.format("Il terzo è Obbligatorio"));

        if ( !Optional.ofNullable(accertamentoDto.getElemento_voceKey()).isPresent())
            throw new RestException(Response.Status.BAD_REQUEST,String.format("La voce è Obbligatoria"));
        if ( !Optional.ofNullable(accertamentoDto.getElemento_voceKey().getCd_elemento_voce()).isPresent())
            throw new RestException(Response.Status.BAD_REQUEST,String.format("Il codice voce è Obbligatoria"));
        if ( !Optional.ofNullable(accertamentoDto.getElemento_voceKey().getTi_appartenenza()).isPresent())
            throw new RestException(Response.Status.BAD_REQUEST,String.format("L'informazione appartenenza della voce è Obbligatoria"));
        if ( !Optional.ofNullable(accertamentoDto.getElemento_voceKey().getTi_gestione()).isPresent())
            throw new RestException(Response.Status.BAD_REQUEST,String.format("L'informazione gestione della voce è Obbligatoria"));
        if ( !Optional.ofNullable(accertamentoDto.getIm_accertamento()).isPresent())
            throw new RestException(Response.Status.BAD_REQUEST,String.format("L'importo dell'Accertamento è Obbligatorio"));
        if ( accertamentoDto.getIm_accertamento().compareTo(BigDecimal.ZERO)<0)
            throw new RestException(Response.Status.BAD_REQUEST,String.format("L'importo dell'Accertamento deve essere maggiore di Zero"));

        validaScadenze( userContext,accertamentoDto.getScadenze());


    }
    //inizio MapStruct
    private AccertamentoBulk accertamentoDtoToAccertamentoBulk(AccertamentoDto accertamentoDto, CNRUserContext userContext) throws ComponentException, RemoteException {

        AccertamentoBulk accertamentoBulk = new AccertamentoBulk();
        accertamentoBulk.setEsercizio( accertamentoDto.getEsercizio());
        accertamentoBulk.setEsercizio_originale(accertamentoDto.getEsercizio_originale());
        accertamentoBulk.setCds((CdsBulk) crudComponentSession.findByPrimaryKey( userContext,new CdsBulk(accertamentoDto.getCdsKey().getCd_unita_organizzativa())));
        accertamentoBulk.setUnita_organizzativa((Unita_organizzativaBulk) crudComponentSession.findByPrimaryKey(userContext,new Unita_organizzativaBulk(accertamentoDto.getUnitaOrganizzativaKey().getCd_unita_organizzativa())));
        accertamentoBulk.setDt_registrazione(it.cnr.jada.util.ejb.EJBCommonServices.getServerDate());
        accertamentoBulk.setCd_cds_origine(accertamentoDto.getCdsOrgineKey().getCd_unita_organizzativa());
        accertamentoBulk.setCd_uo_origine(accertamentoDto.getUnitaOrganizzativaOrigineKey().getCd_unita_organizzativa());

        accertamentoBulk.setDs_accertamento(accertamentoDto.getDs_accertamento());

        accertamentoBulk.setCd_cds_origine(accertamentoBulk.getCd_cds());
        accertamentoBulk.setCd_uo_origine(accertamentoBulk.getUnita_organizzativa().getCd_unita_organizzativa());
        accertamentoBulk.setCd_tipo_documento_cont(Numerazione_doc_contBulk.TIPO_ACR);
        accertamentoBulk.setFl_pgiro(new Boolean(false));
        accertamentoBulk.setRiportato("N");
        accertamentoBulk.setFromDocAmm(Boolean.FALSE);
        accertamentoBulk.setFl_calcolo_automatico(Boolean.FALSE);


        Elemento_voceBulk voce = new it.cnr.contab.config00.pdcfin.bulk.Elemento_voceBulk();
        voce.setEsercizio(accertamentoDto.getElemento_voceKey().getEsercizio());
        voce.setTi_appartenenza(accertamentoDto.getElemento_voceKey().getTi_appartenenza());
        voce.setTi_gestione(accertamentoDto.getElemento_voceKey().getTi_gestione());
        voce.setCd_elemento_voce(accertamentoDto.getElemento_voceKey().getCd_elemento_voce());
        voce = (Elemento_voceBulk)crudComponentSession.findByPrimaryKey(userContext,voce);
        if (voce.getFl_solo_residuo())
            throw new RestException(Response.Status.BAD_REQUEST,String.format("La Voce può essere usata solo per Accrtamenti Residui "));

        accertamentoBulk.setCapitolo( (V_voce_f_partita_giroBulk) crudComponentSession.findByPrimaryKey(userContext,
                new V_voce_f_partita_giroBulk(voce.getCd_voce(),
                        voce.getEsercizio(),
                        voce.getTi_appartenenza(),
                        voce.getTi_gestione())));
        //accertamentoBulk.setCapitoliDiEntrataCdsSelezionatiColl();

        accertamentoBulk.setTi_appartenenza(voce.getTi_appartenenza());
        accertamentoBulk.setTi_gestione(voce.getTi_gestione());
        accertamentoBulk.setCd_elemento_voce(voce.getCd_elemento_voce());
        accertamentoBulk.setCd_voce(voce.getCd_voce());

        accertamentoBulk.setIm_accertamento(accertamentoDto.getIm_accertamento());


        accertamentoBulk.setCd_terzo(accertamentoDto.getTerzoKey().getCd_terzo());
        accertamentoBulk.setEsercizio_competenza(accertamentoDto.getEsercizio());

        accertamentoBulk.setCrudStatus(OggettoBulk.TO_BE_CREATED);

        for (AccertamentoScadenzarioDto scadenza : accertamentoDto.getScadenze()) {
            Accertamento_scadenzarioBulk acr_scadenza = new Accertamento_scadenzarioBulk();
            acr_scadenza.setToBeCreated();
            accertamentoBulk.addToAccertamento_scadenzarioColl(acr_scadenza);
            acr_scadenza.setIm_scadenza(scadenza.getIm_scadenza());

            acr_scadenza.setDt_scadenza_incasso(Optional.ofNullable(scadenza.getDt_scadenza())
                    .map(esercizio -> new Timestamp(scadenza.getDt_scadenza().getTime()))
                    .orElse(it.cnr.jada.util.ejb.EJBCommonServices.getServerDate()));
            acr_scadenza.setDt_scadenza_incasso(new Timestamp(scadenza.getDt_scadenza().getTime()));
            acr_scadenza.setDs_scadenza(scadenza.getDs_scadenza());
            //obb_scadenza.setIm_associato_doc_amm(BigDecimal.ZERO);
            //obb_scadenza.setIm_associato_doc_contabile(BigDecimal.ZERO);

            // BulkList<Obbligazione_scad_voceBulk> listScadVoce= new BulkList<Obbligazione_scad_voceBulk>();
            for ( AccertamentoScadVoceDto scad_voce :scadenza.getAccertamentoScadVoce()) {
                Accertamento_scad_voceBulk acr_scad_voce = new Accertamento_scad_voceBulk();
                acr_scad_voce.setAccertamento_scadenzario(acr_scadenza);
                acr_scad_voce.setToBeCreated();
                acr_scad_voce.setIm_voce(scad_voce.getIm_voce());



                WorkpackageBulk gaePrelevamentoFondi = (WorkpackageBulk) crudComponentSession.findByPrimaryKey(userContext, new WorkpackageBulk(
                        scad_voce.getLineaAttivitaKeyDto().getCentro_responsabilitaKey().getCd_centro_responsabilita(),
                        scad_voce.getLineaAttivitaKeyDto().getCd_linea_attivita()));
                if ( !Optional.ofNullable(gaePrelevamentoFondi).isPresent())
                    throw new RestException(Response.Status.BAD_REQUEST,String.format("La Linea Attivita centro di responsabilità "+
                            scad_voce.getLineaAttivitaKeyDto().getCentro_responsabilitaKey().getCd_centro_responsabilita()+
                            " e codice "+scad_voce.getLineaAttivitaKeyDto().getCd_linea_attivita()+ " non è presente!!"));
                gaePrelevamentoFondi.setCentro_responsabilita((CdrBulk) crudComponentSession.findByPrimaryKey(userContext,gaePrelevamentoFondi.getCentro_responsabilita()));
                gaePrelevamentoFondi.setNatura((NaturaBulk) crudComponentSession.findByPrimaryKey(userContext,gaePrelevamentoFondi.getNatura()));


                acr_scad_voce.setLinea_attivita(gaePrelevamentoFondi);
                Optional.ofNullable(acr_scad_voce.getLinea_attivita().getTi_gestione()).
                        filter(x -> Stream.of(WorkpackageBulk.TI_GESTIONE_ENTRAMBE,WorkpackageBulk.TI_GESTIONE_ENTRATE)
                                .anyMatch(y -> y.equals(x)))
                        .orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, String.format("La Linea Attivita centro di responsabilità " +
                                scad_voce.getLineaAttivitaKeyDto().getCentro_responsabilitaKey().getCd_centro_responsabilita()+
                                " e codice "+scad_voce.getLineaAttivitaKeyDto().getCd_linea_attivita()+ " non è utilizzabile in entrata!!")));


                acr_scadenza.getAccertamento_scad_voceColl().add((acr_scad_voce));
                Linea_attivitaBulk nuovaLatt = new Linea_attivitaBulk();
                nuovaLatt.setLinea_att(acr_scad_voce.getLinea_attivita());
                if (acr_scad_voce.getAccertamento_scadenzario().getIm_scadenza().compareTo(new BigDecimal(0)) != 0)
                    nuovaLatt.setPrcImputazioneFin(acr_scad_voce.getIm_voce().multiply(new BigDecimal(100)).divide(acr_scad_voce.getAccertamento_scadenzario().getIm_scadenza(), 2, BigDecimal.ROUND_HALF_UP));


                nuovaLatt.setAccertamento(accertamentoBulk);

                accertamentoBulk.getNuoveLineeAttivitaColl().add(nuovaLatt);
            }
        }
        return accertamentoBulk;
    }

    private List<AccertamentoScadVoceDto> getScadenzeVoce(List<Accertamento_scad_voceBulk> scadenzeVoce){
        List<AccertamentoScadVoceDto> scadenzeVoceDto = new ArrayList<AccertamentoScadVoceDto>();
        for (Accertamento_scad_voceBulk scadenzaVoce: scadenzeVoce) {
            AccertamentoScadVoceDto accertamentoScadVoceDto = new AccertamentoScadVoceDto();
            accertamentoScadVoceDto.setIm_voce(scadenzaVoce.getIm_voce());
            accertamentoScadVoceDto.setLineaAttivitaKeyDto(new LineaAttivitaKeyDto( scadenzaVoce.getCd_centro_responsabilita(),scadenzaVoce.getCd_linea_attivita()));
            accertamentoScadVoceDto.setPercentuale(scadenzaVoce.getPrc());
            scadenzeVoceDto.add(accertamentoScadVoceDto);
        }
        return scadenzeVoceDto;
    }
    private List<AccertamentoScadenzarioDto> getScadenze(List<Accertamento_scadenzarioBulk> scadenze){
        List<AccertamentoScadenzarioDto> scadenzeDto = new ArrayList<AccertamentoScadenzarioDto>();
        for (Accertamento_scadenzarioBulk scadenza: scadenze) {
            AccertamentoScadenzarioDto accertamentoScadenzarioDto = new AccertamentoScadenzarioDto();
            accertamentoScadenzarioDto.setDs_scadenza(scadenza.getDs_scadenza());
            accertamentoScadenzarioDto.setIm_scadenza(scadenza.getIm_scadenza());
            accertamentoScadenzarioDto.setIm_associato_doc_amm(scadenza.getIm_associato_doc_amm());
            accertamentoScadenzarioDto.setIm_associato_doc_contabile(scadenza.getIm_associato_doc_contabile());
            accertamentoScadenzarioDto.setAccertamentoScadVoce(getScadenzeVoce(scadenza.getAccertamento_scad_voceColl()));
            accertamentoScadenzarioDto.setPg_accertamento_scadenzario(scadenza.getPg_accertamento_scadenzario());
            scadenzeDto.add(accertamentoScadenzarioDto);
        }
        return scadenzeDto;
    }
    private AccertamentoDto accertamentoBulkToAccertamentoDto(AccertamentoBulk accertamentoBulk) throws ComponentException, RemoteException {

        AccertamentoDto accertamentoDto = new AccertamentoDto();
        accertamentoDto.setCdsKey( new CdsKey(accertamentoBulk.getCds().getCd_unita_organizzativa()));
        accertamentoDto.setEsercizio( accertamentoBulk.getEsercizio());
        accertamentoDto.setEsercizio_originale(accertamentoBulk.getEsercizio_originale());
        accertamentoDto.setPg_accertamento(accertamentoBulk.getPg_accertamento());
        accertamentoDto.setDs_accertamento(accertamentoBulk.getDs_accertamento());
        accertamentoDto.setIm_accertamento(accertamentoBulk.getIm_accertamento());
        accertamentoDto.setCdsOrgineKey(new CdsKey(accertamentoBulk.getCd_cds_origine()));
        accertamentoDto.setUnitaOrganizzativaOrigineKey(new CdsKey(accertamentoBulk.getCd_uo_origine()));

        accertamentoDto.setUnitaOrganizzativaKey(new Unita_organizzativaKey(accertamentoBulk.getUnita_organizzativa().getCd_unita_organizzativa()));

        Optional.ofNullable(accertamentoBulk.getContratto()).ifPresent(e-> {
            accertamentoDto.setContrattoKey(new ContrattoKey(e.getEsercizio(),e.getStato(),e.getPg_contratto()));
        });
        accertamentoDto.setTerzoKey(new TerzoKey( accertamentoBulk.getDebitore().getCd_terzo()));

        accertamentoDto.setElemento_voceKey(new Elemento_voceKey(accertamentoBulk.getCapitolo().getCd_elemento_voce(),
                accertamentoBulk.getCapitolo().getEsercizio(),
                accertamentoBulk.getCapitolo().getTi_appartenenza(),
                accertamentoBulk.getCapitolo().getTi_gestione()));
        accertamentoDto.setScadenze(getScadenze(accertamentoBulk.getAccertamento_scadenzarioColl() ) );
        return accertamentoDto;

    }

    protected AccertamentoBulk creaAccertamentoSigla( AccertamentoBulk accertamentoBulk, CNRUserContext userContext) throws ComponentException, RemoteException, PersistencyException {
        accertamentoBulk = (AccertamentoBulk)  accertamentoComponentSession.inizializzaBulkPerInserimento(userContext,accertamentoBulk);
        accertamentoBulk.setToBeCreated();
       return ( AccertamentoBulk) accertamentoComponentSession.creaAccertamentoWs(userContext,accertamentoBulk);

    }

    @Override
    public Response insert(HttpServletRequest request, AccertamentoDto accertamentoDto) throws Exception {
        CNRUserContext userContext = (CNRUserContext) securityContext.getUserPrincipal();
        validaAccertamentoDto(userContext, accertamentoDto);
        try {

            AccertamentoBulk accertamentoBulk= accertamentoDtoToAccertamentoBulk(accertamentoDto,userContext);

            return Response.status(Response.Status.CREATED).entity(accertamentoBulkToAccertamentoDto( creaAccertamentoSigla( accertamentoBulk,userContext))).build();
        }catch (Throwable e){
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR,String.format(e.getMessage()));
        }


    }
    private AccertamentoBulk getAccertamento(CNRUserContext userContext,String cd_cds, Integer esercizio, Long pg_accertamento, Integer esercizio_originale) throws Exception {
        AccertamentoBulk accertamentoBulk =new AccertamentoBulk(cd_cds,esercizio,esercizio_originale,pg_accertamento);
        accertamentoBulk.setCd_cds_origine( userContext.getCd_cds());
        accertamentoBulk.setCd_uo_origine( userContext.getCd_unita_organizzativa());
        RemoteIterator iterator =accertamentoComponentSession.cerca(userContext, null, accertamentoBulk);
        if (iterator == null ||iterator.countElements() != 1)
            return null;
        return  ( AccertamentoBulk)iterator.nextElement();
    }

    @Override
    public Response get(String cd_cds, Integer esercizio, Long pg_accertamento, Integer esercizio_originale) throws Exception {
        try{
            CNRUserContext userContext = (CNRUserContext) securityContext.getUserPrincipal();
            validaContestoAccertamento( userContext,esercizio, userContext.getCd_cds(), userContext.getCd_unita_organizzativa());
            AccertamentoBulk accertamentoBulk =getAccertamento( userContext,cd_cds,esercizio,pg_accertamento,esercizio_originale);
            Optional.ofNullable(accertamentoBulk).orElseThrow(() ->new RestException(Response.Status.NOT_FOUND,String.format("Accertamento non presente!")));
            return Response.status(Response.Status.OK).entity(accertamentoBulkToAccertamentoDto( accertamentoBulk )).build();
        }catch (Throwable e){
            if ( e instanceof RestException)
                throw e;
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR,String.format(e.getMessage()));
        }
    }
    @Override
    public Response delete(String cd_cds, Integer esercizio, Long pg_accertamento, Integer esercizio_originale) throws Exception {
        try{
            CNRUserContext userContext = (CNRUserContext) securityContext.getUserPrincipal();
            validaContestoAccertamento( userContext,esercizio, userContext.getCd_cds(), userContext.getCd_unita_organizzativa());
            AccertamentoBulk accertamentoBulk =getAccertamento( userContext,cd_cds,esercizio,pg_accertamento,esercizio_originale);
            if ( !Optional.ofNullable(accertamentoBulk).isPresent())
                    return Response.status(Response.Status.NO_CONTENT).build();
           Boolean result= accertamentoComponentSession.deleteAccertamentoWs(userContext,accertamentoBulk);
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

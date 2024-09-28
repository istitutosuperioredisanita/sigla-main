package it.cnr.contab.web.rest.resource.doccont;

import it.cnr.contab.anagraf00.core.bulk.BancaBulk;
import it.cnr.contab.anagraf00.core.bulk.Modalita_pagamentoBulk;
import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.contab.anagraf00.core.bulk.TerzoKey;
import it.cnr.contab.anagraf00.ejb.TerzoComponentSession;
import it.cnr.contab.anagraf00.tabrif.bulk.Rif_modalita_pagamentoBulk;
import it.cnr.contab.anagraf00.tabrif.bulk.Rif_modalita_pagamentoKey;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;
import it.cnr.contab.docamm00.docs.bulk.*;
import it.cnr.contab.docamm00.tabrif.bulk.Tipo_documento_genericoBulk;
import it.cnr.contab.doccont00.core.bulk.Accertamento_scadenzarioBulk;
import it.cnr.contab.doccont00.core.bulk.Accertamento_scadenzarioKey;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.web.rest.exception.RestException;
import it.cnr.contab.web.rest.local.config00.DocumentoGenericoAttivoLocal;
import it.cnr.contab.web.rest.model.*;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.ComponentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Stateless
public class DocumentoGenAttivoResource extends AbstractDocumentoGenericoResource<DocumentoGenericoAttivoDto> implements DocumentoGenericoAttivoLocal {
    private final Logger LOGGER = LoggerFactory.getLogger(DocumentoGenAttivoResource.class);
    @EJB
    TerzoComponentSession terzoComponentSession;
    @Override
    public Response insert(HttpServletRequest request, DocumentoGenericoAttivoDto documentoGenericoAttivoDto) throws Exception {
        return insertDocumentoGenerico( request,documentoGenericoAttivoDto);
    }
    @Override
    String getCdTipoDocumentoAmm() {
        return Documento_genericoBulk.GENERICO_E;
    }

    @Override
    protected void addInfoContToDocRigheDto(Documento_generico_rigaBulk rigaBulk, DocumentoGenericoRigaDto rigaDto, UserContext userContext){

                DocumentoGenericoAttivoRigaDto rigaAttivoDto= (DocumentoGenericoAttivoRigaDto) rigaDto;
                TerzoPagamentoIncasso terzoCreditore = new TerzoPagamentoIncasso();

            rigaAttivoDto.setTerzoDebitore(new TerzoKey( rigaBulk.getCd_terzo()));
            TerzoPagamentoIncasso terzoUo = new TerzoPagamentoIncasso();
            terzoUo.setTerzoKey(new TerzoKey( rigaBulk.getCd_terzo_uo_cds()));
            terzoUo.setPg_banca(rigaBulk.getPg_banca_uo_cds());
            terzoUo.setRifModalitaPagamentoKey( new Rif_modalita_pagamentoKey(rigaBulk.getModalita_pagamento_uo_cds().getCd_modalita_pag()));
            rigaAttivoDto.setTerzoUo(terzoUo);


            rigaAttivoDto.setAccertamentoScadenzarioKey( new Accertamento_scadenzarioKey(rigaBulk.getAccertamento_scadenziario().getCd_cds(),
                                            rigaBulk.getAccertamento_scadenziario().getEsercizio(),
                                            rigaBulk.getAccertamento_scadenziario().getEsercizio_originale(),
                                            rigaBulk.getAccertamento_scadenziario().getPg_accertamento(),
                        rigaBulk.getAccertamento_scadenziario().getPg_accertamento_scadenzario()));
    }

    @Override
    protected DocumentoGenericoAttivoDto completeDocumentoGenDto( Documento_genericoBulk bulk,DocumentoGenericoAttivoDto documentoGenericoDto, UserContext userContext) {
        //documentoGenericoDto.set( EnumStatoFondoEcomale.getValueFrom(bulk.getStato_pagamento_fondo_eco()));
       //documentoGenericoDto.setStatoLiquidazione( EnumStatoLiqDocumentoGen.getValueFrom(bulk.getStato_liquidazione()));
        //Optional.ofNullable(bulk.getCausale()).ifPresent(causale-> {
        //    documentoGenericoDto.setCausale(EnumCausaleDocumentoGen.getValueFrom(causale));
        //});


        return documentoGenericoDto;
    }
    private Tipo_documento_genericoBulk getTipoDocumentoGenerico( CNRUserContext userContext,DocumentoGenericoAttivoDto documentoGenericoDto) throws ComponentException, RemoteException {
        return  documentoGenericoComponentSession.findTipoDocumentoGenerico(userContext,"GEN","A");
    }

    @Override
    protected Documento_genericoBulk initializeDocumentoGenerico(CNRUserContext userContext, DocumentoGenericoAttivoDto documentoGenericoDto) throws ComponentException, RemoteException {
        Documento_genericoBulk documentoGenericoBulk = new Documento_genericoBulk();
        documentoGenericoBulk.setEsercizio(documentoGenericoDto.getEsercizio());
        documentoGenericoBulk.setCd_cds( documentoGenericoDto.getCdsKey().getCd_unita_organizzativa());
        documentoGenericoBulk.setCd_cds_origine(documentoGenericoBulk.getCd_cds());
        documentoGenericoBulk.setCd_uo_origine(documentoGenericoDto.getUnitaOrganizzativaKey().getCd_unita_organizzativa());
        documentoGenericoBulk.setCd_unita_organizzativa(documentoGenericoDto.getUnitaOrganizzativaKey().getCd_unita_organizzativa());
        documentoGenericoBulk.setTi_entrate_spese(Documento_genericoBulk.ENTRATE);
        documentoGenericoBulk.setTipo_documento(new Tipo_documento_ammBulk( Numerazione_doc_ammBulk.TIPO_DOC_GENERICO_E));
        documentoGenericoBulk.setTipoDocumentoGenerico( getTipoDocumentoGenerico(userContext,documentoGenericoDto));
        documentoGenericoBulk.setTipoDocumentoGenerico(getTipoDocumentoGenerico(userContext,documentoGenericoDto));
        documentoGenericoBulk = ( Documento_genericoBulk) documentoGenericoComponentSession.inizializzaBulkPerInserimento(userContext,documentoGenericoBulk);

        return documentoGenericoBulk;
    }

    private void setTerzo(CNRUserContext userContext,Documento_generico_rigaBulk rigaBulk,Integer cd_terzo,Integer riga) throws ComponentException, RemoteException {
        TerzoBulk terzoBulk =( TerzoBulk) crudComponentSession.findByPrimaryKey(
                userContext,new TerzoBulk(cd_terzo));
        if ( !Optional.ofNullable(terzoBulk).isPresent())
            new RestException(Response.Status.BAD_REQUEST, "Il terzo  "+cd_terzo +
                    " per la riga "+ riga + " non è presente in Sigla");
        impostaTerzo(userContext,terzoBulk,rigaBulk);
    }
    @Override
    protected void addDocumentoGenericoRighe(CNRUserContext userContext, Documento_genericoBulk documentoGenericoBulk, DocumentoGenericoAttivoDto documentoGenericoDto) throws Exception {
        List<Documento_generico_rigaBulk> righe= new ArrayList<Documento_generico_rigaBulk>();
        if (!Optional.ofNullable(documentoGenericoDto.getRighe()).isPresent())
            return;
        int riga=0;
        for ( DocumentoGenericoAttivoRigaDto rigaDto:documentoGenericoDto.getRighe()){
            riga++;
            Documento_generico_rigaBulk rigaBulk= initializeDocumentoGenericoRiga( userContext,documentoGenericoBulk,rigaDto,riga);
            setTerzo( userContext,rigaBulk,rigaDto.getTerzoDebitore().getCd_terzo(),riga);
            // imposta terzo Uo

            rigaBulk.setAccertamento_scadenziario( new Accertamento_scadenzarioBulk(rigaDto.getAccertamentoScadenzarioKey().getCd_cds(),
                    rigaDto.getAccertamentoScadenzarioKey().getEsercizio(),
                    rigaDto.getAccertamentoScadenzarioKey().getEsercizio_originale(),
                    rigaDto.getAccertamentoScadenzarioKey().getPg_accertamento(),
                    rigaDto.getAccertamentoScadenzarioKey().getPg_accertamento_scadenzario()));

            rigaBulk.setTi_associato_manrev(EnumAssMandRevDocGenRiga.NO_ASSOCIATO_A_MAND_REV.getAssMandRev());
            rigaBulk.setToBeCreated();
        }
    }




    @Override
    public Response get(String cd_cds, String cd_unita_organizzativa, Integer esercizio,Long pg_documento_generico) throws Exception {

        Documento_generico_attivoBulk documentoGenericoAttivoBulk = new Documento_generico_attivoBulk(cd_cds,
                getCdTipoDocumentoAmm(),
                cd_unita_organizzativa,
                esercizio,
                pg_documento_generico
        );
        return getDocumentoGenerico( documentoGenericoAttivoBulk);


    }

    @Override
    public Response delete(String cd_cds, String cd_unita_organizzativa, Integer esercizio,  Long pg_documento_generico) throws Exception {
        return deleteDocumentoGenerico(cd_cds,cd_unita_organizzativa,esercizio,pg_documento_generico);
    }

    @Override
    public Response update(String cd_cds, String cd_unita_organizzativa, Integer esercizio,  Long pg_documento_generico) throws Exception {
        throw new RestException(Response.Status.METHOD_NOT_ALLOWED,String.format("La Funzione non è implemetata"));
    }

    private List<BancaBulk> getBancheRifPag(Rif_modalita_pagamentoBulk rifModalitaPagamentoBulk, List<BancaBulk> banche){

        return Optional.ofNullable(banche).orElse(  Collections.emptyList()).stream().
                filter(banca->rifModalitaPagamentoBulk.getTi_pagamento().equals(banca.getTi_pagamento())).
                collect(Collectors.toList());

    }
    private BancaBulk getBancaDefaultForCdsFrom( Rif_modalita_pagamentoBulk rifModalitaPagamentoBulk, List<BancaBulk> banche){
        List<BancaBulk> bancheRifPagamento= getBancheRifPag(rifModalitaPagamentoBulk,banche );
        if ( Optional.ofNullable(bancheRifPagamento).isPresent()){
                for (BancaBulk banca : bancheRifPagamento) {
                    if (banca.getFl_cc_cds() != null &&
                            banca.getFl_cc_cds().booleanValue())
                        return banca;
                }
                return bancheRifPagamento.stream().findFirst().get();
        }
        return null;
    }
    @Override
    public Response terzoUnitaOrganizzativa(Integer esercizio,String cd_unita_organizzativa) throws Exception {
        Optional.ofNullable(cd_unita_organizzativa).orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Errore, indicare l'unita organizzativa."));
        Optional.ofNullable(esercizio).orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Errore, indicare l'esercizio."));
        CNRUserContext userContext = (CNRUserContext) securityContext.getUserPrincipal();

        TerzoBulk terzoDB=terzoComponentSession.cercaTerzoPerUnitaOrganizzativa(userContext, new Unita_organizzativaBulk(cd_unita_organizzativa));
        Optional.ofNullable(terzoDB).orElseThrow(() -> new RestException(Response.Status.BAD_REQUEST, "Errore, il terzo per l'unita Organizzativa "+cd_unita_organizzativa+" non esiste"));
        terzoDB= ( TerzoBulk) terzoComponentSession.inizializzaBulkPerModifica(userContext,terzoDB);
        //gestione Banche//
        List<ModalitaPagamentoDto> modPag = new ArrayList<ModalitaPagamentoDto>();

        for (Modalita_pagamentoBulk modalitaPagamentoBulk: terzoDB.getModalita_pagamento()){

            if (!Rif_modalita_pagamentoBulk.BANCARIO.equals(modalitaPagamentoBulk.getRif_modalita_pagamento().getTi_pagamento())) {
                DettaglioModalitaPagDto dettaglioModalitaPagDto =
                        new DettaglioModalitaPagDto(getBancaDefaultForCdsFrom(modalitaPagamentoBulk.getRif_modalita_pagamento(), terzoDB.getBanche()));
                List<DettaglioModalitaPagDto> dettaglioModalitaPagDtos = new ArrayList<DettaglioModalitaPagDto>();
                dettaglioModalitaPagDtos.add(dettaglioModalitaPagDto);
                modPag.add(new ModalitaPagamentoDto(modalitaPagamentoBulk,
                        dettaglioModalitaPagDtos));
            }else {

                Documento_genericoBulk documentoGenericoToSearchBanca = new Documento_genericoBulk();
                documentoGenericoToSearchBanca.setTi_entrate_spese( Documento_genericoBulk.ENTRATE);
                documentoGenericoToSearchBanca.setEsercizio(esercizio);
                Documento_generico_rigaBulk dettaglioToSearchBanca  =new Documento_generico_rigaBulk();
                dettaglioToSearchBanca.setEsercizio(esercizio);
                dettaglioToSearchBanca.setModalita_pagamento_uo_cds(modalitaPagamentoBulk.getRif_modalita_pagamento());
                dettaglioToSearchBanca.setDocumento_generico( documentoGenericoToSearchBanca);
                documentoGenericoToSearchBanca.getDocumento_generico_dettColl().add(dettaglioToSearchBanca);

                BancaBulk banca = documentoGenericoComponentSession.setContoEnteIn(userContext, dettaglioToSearchBanca, getBancheRifPag( modalitaPagamentoBulk.getRif_modalita_pagamento(), terzoDB.getBanche()));

                if (banca == null)
                    banca =getBancaDefaultForCdsFrom(modalitaPagamentoBulk.getRif_modalita_pagamento(), terzoDB.getBanche());
                DettaglioModalitaPagDto dettaglioModalitaPagDto =
                        new DettaglioModalitaPagDto(banca);
                List<DettaglioModalitaPagDto> dettaglioModalitaPagDtos = new ArrayList<DettaglioModalitaPagDto>();
                dettaglioModalitaPagDtos.add(dettaglioModalitaPagDto);
                modPag.add(new ModalitaPagamentoDto(modalitaPagamentoBulk,
                        dettaglioModalitaPagDtos));
            }
        }


        return Response.status(Response.Status.OK).entity(
                new TerzoUnitaOrganizzativaDto( terzoDB,modPag)
        ).build();


    }
}

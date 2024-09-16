package it.cnr.contab.web.rest.resource.doccont;

import it.cnr.contab.anagraf00.core.bulk.BancaBulk;
import it.cnr.contab.anagraf00.core.bulk.Modalita_pagamentoBulk;
import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.contab.anagraf00.core.bulk.TerzoKey;
import it.cnr.contab.anagraf00.tabrif.bulk.Rif_modalita_pagamentoBulk;
import it.cnr.contab.docamm00.docs.bulk.*;
import it.cnr.contab.docamm00.ejb.DocumentoGenericoComponentSession;
import it.cnr.contab.doccont00.core.bulk.Accertamento_scadenzarioBulk;
import it.cnr.contab.doccont00.core.bulk.Accertamento_scadenzarioKey;
import it.cnr.contab.web.rest.exception.RestException;
import it.cnr.contab.web.rest.local.config00.DocumentoGenericoAttivoLocal;
import it.cnr.contab.web.rest.model.*;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.ejb.CRUDComponentSession;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Stateless
public class DocumentoGenAttivoResource extends AbstractDocumentoGenericoResource<DocumentoGenericoAttivoDto> implements DocumentoGenericoAttivoLocal {

    @Context
    SecurityContext securityContext;
    @EJB
    CRUDComponentSession crudComponentSession;
    @EJB
    DocumentoGenericoComponentSession documentoGenericoComponentSessione;
    @Override
    public Response insert(HttpServletRequest request, DocumentoGenericoPassivoDto documentoGenericoPassivoDto) throws Exception {
        return null;
    }

    @Override
    String getCdTipoDocumentoAmm() {
        return Documento_genericoBulk.GENERICO_E;
    }

    @Override
    protected void addInfoContToDocRigheDto(Documento_generico_rigaBulk rigaBulk, DocumentoGenericoRigaDto rigaDto, UserContext userContext){

                DocumentoGenericoAttivoRigaDto rigaPassDto= (DocumentoGenericoAttivoRigaDto) rigaDto;
                TerzoPagamentoIncasso terzoCreditore = new TerzoPagamentoIncasso();
                terzoCreditore.setTerzoKey(new TerzoKey( rigaBulk.getCd_terzo()));
                terzoCreditore.setPg_banca(rigaBulk.getPg_banca());
                terzoCreditore.setRifModalitaPagamentoKey( rigaBulk.getModalita_pagamento());
                rigaPassDto.setTerzoCreditore(terzoCreditore);
        rigaPassDto.setAccertamentoScadenzarioKey( new Accertamento_scadenzarioKey(rigaBulk.getAccertamento_scadenziario().getCd_cds(),
                                            rigaBulk.getAccertamento_scadenziario().getEsercizio(),
                                            rigaBulk.getAccertamento_scadenziario().getEsercizio_originale(),
                                            rigaBulk.getAccertamento_scadenziario().getPg_accertamento(),
                        rigaBulk.getAccertamento_scadenziario().getPg_accertamento_scadenzario()));
    }

    @Override
    protected DocumentoGenericoAttivoDto completeDocumentoGenDto( Documento_genericoBulk bulk,DocumentoGenericoAttivoDto documentoGenericoDto, UserContext userContext) {
        //documentoGenericoDto.set( EnumStatoFondoEcomale.getValueFrom(bulk.getStato_pagamento_fondo_eco()));
        documentoGenericoDto.setStatoLiquidazione( EnumStatoLiqDocumentoGen.getValueFrom(bulk.getStato_liquidazione()));
        //Optional.ofNullable(bulk.getCausale()).ifPresent(causale-> {
        //    documentoGenericoDto.setCausale(EnumCausaleDocumentoGen.getValueFrom(causale));
        //});
        return documentoGenericoDto;
    }

    @Override
    protected Documento_genericoBulk initializeDocumentoGenerico(UserContext userContext, DocumentoGenericoAttivoDto documentoGenericoDto) throws ComponentException, RemoteException {
        Documento_genericoBulk documentoGenericoBulk = new Documento_genericoBulk();
        documentoGenericoBulk.setTi_entrate_spese(Documento_genericoBulk.ENTRATE);
        documentoGenericoBulk.setTipo_documento(new Tipo_documento_ammBulk( Numerazione_doc_ammBulk.TIPO_DOC_GENERICO_E));
        documentoGenericoComponentSession.inizializzaBulkPerInserimento(userContext,documentoGenericoBulk);
        return documentoGenericoBulk;
    }

    @Override
    protected void addDocumentoGenericoRighe(UserContext userContext, Documento_genericoBulk documentoGenericoBulk, DocumentoGenericoAttivoDto documentoGenericoDto) throws Exception {
        List<Documento_generico_rigaBulk> righe= new ArrayList<Documento_generico_rigaBulk>();
        if (!Optional.ofNullable(documentoGenericoDto.getRighe()).isPresent())
            return;
        int riga=0;
        for ( DocumentoGenericoAttivoRigaDto rigaDto:documentoGenericoDto.getRighe()){
            riga++;
            Documento_generico_rigaBulk rigaBulk= new Documento_generico_rigaBulk();
            documentoGenericoBulk.addToDocumento_generico_dettColl( rigaBulk);
            rigaBulk.setDocumento_generico(documentoGenericoBulk);
            rigaBulk.setStato_cofi(EnumStatoDocumentoGenerico.CONTABILIZZATO.getStato());
            rigaBulk.setIm_riga(rigaDto.getIm_riga());
            rigaBulk.setIm_riga_divisa(rigaBulk.getIm_riga());
            rigaBulk.setDs_riga(rigaDto.getDs_riga());
            rigaBulk.setDt_a_competenza_coge(rigaDto.getDt_a_competenza_coge());
            rigaBulk.setDt_da_competenza_coge(rigaDto.getDt_da_competenza_coge());
            rigaBulk.setTerzo(new TerzoBulk(rigaDto.getTerzoCreditore().getTerzoKey().getCd_terzo()));
            rigaBulk.setBanca((BancaBulk)crudComponentSession.findByPrimaryKey(userContext,
                    new BancaBulk(rigaDto.getTerzoCreditore().getTerzoKey().getCd_terzo(),rigaDto.getTerzoCreditore().getPg_banca())));

            if ( !Optional.ofNullable(rigaBulk.getBanca()).isPresent())
                new RestException(Response.Status.BAD_REQUEST, "Identificativo Banca "+ rigaDto.getTerzoCreditore().getPg_banca() +
                        " per la riga "+ riga + " non è presente per il terzo "+rigaBulk.getTerzo().getCd_terzo());
            rigaBulk.setModalita_pagamento((Rif_modalita_pagamentoBulk)crudComponentSession.findByPrimaryKey(userContext,
                    new Rif_modalita_pagamentoBulk(rigaDto.getTerzoCreditore().getRifModalitaPagamentoKey().getCd_modalita_pag())));
            if ( !Optional.ofNullable(rigaBulk.getModalita_pagamento()).isPresent())
                throw new RestException(Response.Status.BAD_REQUEST, "Modalità di pagamento  "+ rigaDto.getTerzoCreditore().getRifModalitaPagamentoKey().getCd_modalita_pag() +
                        " riga "+ riga + " non presente in Sigla");

            Modalita_pagamentoBulk modalitaPagamentTerzo = ( Modalita_pagamentoBulk) crudComponentSession.findByPrimaryKey(userContext,
                    new Modalita_pagamentoBulk(rigaDto.getTerzoCreditore().getRifModalitaPagamentoKey().getCd_modalita_pag(),
                            rigaDto.getTerzoCreditore().getTerzoKey().getCd_terzo()));

            if ( !Optional.ofNullable(modalitaPagamentTerzo).isPresent())
                throw new RestException(Response.Status.BAD_REQUEST, "La modalità pagamento  "+ rigaBulk.getModalita_pagamento().getCd_modalita_pag() +
                        " per la riga "+ riga + " non associata al terzo "+rigaBulk.getTerzo().getCd_terzo());

            //controlla se la modalita di pagamento è coerente con quella presente sulla banca
            if ( rigaBulk.getBanca().getTi_pagamento().compareTo(rigaBulk.getModalita_pagamento().getTi_pagamento())!=0)
                throw new RestException(Response.Status.BAD_REQUEST, "La modalità pagamento  "+ rigaBulk.getModalita_pagamento().getCd_modalita_pag() +
                        " per la riga "+ riga + " non associata al terzo "+rigaBulk.getTerzo().getCd_terzo());



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
        return null;
    }
}

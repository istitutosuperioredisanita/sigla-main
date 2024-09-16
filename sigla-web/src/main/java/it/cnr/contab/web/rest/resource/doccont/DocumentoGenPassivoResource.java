package it.cnr.contab.web.rest.resource.doccont;

import it.cnr.contab.anagraf00.core.bulk.BancaBulk;
import it.cnr.contab.anagraf00.core.bulk.Modalita_pagamentoBulk;
import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.contab.anagraf00.core.bulk.TerzoKey;
import it.cnr.contab.anagraf00.tabrif.bulk.Rif_modalita_pagamentoBulk;
import it.cnr.contab.anagraf00.tabrif.bulk.Rif_modalita_pagamentoKey;
import it.cnr.contab.docamm00.docs.bulk.*;
import it.cnr.contab.doccont00.core.bulk.Obbligazione_scadenzarioBulk;
import it.cnr.contab.doccont00.core.bulk.Obbligazione_scadenzarioKey;
import it.cnr.contab.web.rest.exception.RestException;
import it.cnr.contab.web.rest.local.config00.DocumentoGenericoPassivoLocal;
import it.cnr.contab.web.rest.model.*;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.ComponentException;

import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Stateless
public class DocumentoGenPassivoResource extends AbstractDocumentoGenericoResource<DocumentoGenericoPassivoDto> implements DocumentoGenericoPassivoLocal {
    @Override
    public Response insert(HttpServletRequest request, DocumentoGenericoPassivoDto documentoGenericoPassivoDto) throws Exception {
        return insertDocumentoGenerico( request,documentoGenericoPassivoDto);
    }
    @Override
    String getCdTipoDocumentoAmm() {
        return Documento_genericoBulk.GENERICO_S;
    }

    @Override
    protected void addInfoContToDocRigheDto(Documento_generico_rigaBulk rigaBulk, DocumentoGenericoRigaDto rigaDto, UserContext userContext){

                DocumentoGenericoPassRigaDto rigaPassDto= (DocumentoGenericoPassRigaDto) rigaDto;
                TerzoPagamentoIncasso terzoDebitore = new TerzoPagamentoIncasso();
                    terzoDebitore.setTerzoKey(new TerzoKey( rigaBulk.getCd_terzo()));
                    terzoDebitore.setPg_banca(rigaBulk.getPg_banca());
                    terzoDebitore.setRifModalitaPagamentoKey( new Rif_modalita_pagamentoKey(rigaBulk.getModalita_pagamento().getCd_modalita_pag()));
                    rigaPassDto.setTerzoDebitore(terzoDebitore);

                rigaPassDto.setObbligazioneScadenzarioKey( new Obbligazione_scadenzarioKey(rigaBulk.getObbligazione_scadenziario().getCd_cds(),
                                            rigaBulk.getObbligazione_scadenziario().getEsercizio(),
                                            rigaBulk.getObbligazione_scadenziario().getEsercizio_originale(),
                                            rigaBulk.getObbligazione_scadenziario().getPg_obbligazione(),
                        rigaBulk.getObbligazione_scadenziario().getPg_obbligazione_scadenzario()));
    }

    @Override
    protected DocumentoGenericoPassivoDto completeDocumentoGenDto( Documento_genericoBulk bulk,DocumentoGenericoPassivoDto documentoGenericoDto, UserContext userContext) {
        documentoGenericoDto.setEnumStatoFondoEcomale( EnumStatoFondoEcomale.getValueFrom(bulk.getStato_pagamento_fondo_eco()));
        documentoGenericoDto.setStato_liquidazione( EnumStatoLiqDocumentoGen.getValueFrom(bulk.getStato_liquidazione()));
         Optional.ofNullable(bulk.getCausale()).ifPresent(causale-> {
            documentoGenericoDto.setCausale(EnumCausaleDocumentoGen.getValueFrom(causale));
        });
            return documentoGenericoDto;
    }

    @Override
    protected Documento_genericoBulk initializeDocumentoGenerico(UserContext userContext, DocumentoGenericoPassivoDto documentoGenericoDto) throws ComponentException, RemoteException {
        Documento_genericoBulk documentoGenericoBulk = new Documento_genericoBulk();
        documentoGenericoBulk.setTi_entrate_spese(Documento_genericoBulk.SPESE);
        documentoGenericoBulk.setTipo_documento(new Tipo_documento_ammBulk( Numerazione_doc_ammBulk.TIPO_DOC_GENERICO_S));
        documentoGenericoBulk.setCd_uo_origine(documentoGenericoDto.getUnitaOrganizzativaKey().getCd_unita_organizzativa());
        return ( Documento_genericoBulk) documentoGenericoComponentSession.inizializzaBulkPerInserimento(userContext,documentoGenericoBulk);


    }

    @Override
    protected void  addDocumentoGenericoRighe(UserContext userContext, Documento_genericoBulk documentoGenericoBulk,DocumentoGenericoPassivoDto documentoGenericoDto) throws Exception {
        List<Documento_generico_rigaBulk> righe= new ArrayList<Documento_generico_rigaBulk>();
       if (!Optional.ofNullable(documentoGenericoDto.getRighe()).isPresent())
           return;
       int riga=0;
       for ( DocumentoGenericoPassRigaDto rigaDto:documentoGenericoDto.getRighe()){
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
           rigaBulk.setTerzo(new TerzoBulk(rigaDto.getTerzoDebitore().getTerzoKey().getCd_terzo()));
           rigaBulk.setBanca((BancaBulk)crudComponentSession.findByPrimaryKey(userContext,
                   new BancaBulk(rigaDto.getTerzoDebitore().getTerzoKey().getCd_terzo(),rigaDto.getTerzoDebitore().getPg_banca())));

           if ( !Optional.ofNullable(rigaBulk.getBanca()).isPresent())
               new RestException(Response.Status.BAD_REQUEST, "Identificativo Banca "+ rigaDto.getTerzoDebitore().getPg_banca() +
                        " per la riga "+ riga + " non è presente per il terzo "+rigaBulk.getTerzo().getCd_terzo());
           rigaBulk.setModalita_pagamento((Rif_modalita_pagamentoBulk)crudComponentSession.findByPrimaryKey(userContext,
                                new Rif_modalita_pagamentoBulk(rigaDto.getTerzoDebitore().getRifModalitaPagamentoKey().getCd_modalita_pag())));
           if ( !Optional.ofNullable(rigaBulk.getModalita_pagamento()).isPresent())
               throw new RestException(Response.Status.BAD_REQUEST, "Modalità di pagamento  "+ rigaDto.getTerzoDebitore().getRifModalitaPagamentoKey().getCd_modalita_pag() +
                       " riga "+ riga + " non presente in Sigla");

           Modalita_pagamentoBulk modalitaPagamentTerzo = ( Modalita_pagamentoBulk) crudComponentSession.findByPrimaryKey(userContext,
                   new Modalita_pagamentoBulk(rigaDto.getTerzoDebitore().getRifModalitaPagamentoKey().getCd_modalita_pag(),
                           rigaDto.getTerzoDebitore().getTerzoKey().getCd_terzo()));

           if ( !Optional.ofNullable(modalitaPagamentTerzo).isPresent())
               throw new RestException(Response.Status.BAD_REQUEST, "La modalità pagamento  "+ rigaBulk.getModalita_pagamento().getCd_modalita_pag() +
                       " per la riga "+ riga + " non associata al terzo "+rigaBulk.getTerzo().getCd_terzo());

           //controlla se la modalita di pagamento è coerente con quella presente sulla banca
           if ( rigaBulk.getBanca().getTi_pagamento().compareTo(rigaBulk.getModalita_pagamento().getTi_pagamento())!=0)
               throw new RestException(Response.Status.BAD_REQUEST, "La modalità pagamento  "+ rigaBulk.getModalita_pagamento().getCd_modalita_pag() +
                       " per la riga "+ riga + " non associata al terzo "+rigaBulk.getTerzo().getCd_terzo());



            rigaBulk.setObbligazione_scadenziario( new Obbligazione_scadenzarioBulk(rigaDto.getObbligazioneScadenzarioKey().getCd_cds(),
                    rigaDto.getObbligazioneScadenzarioKey().getEsercizio(),
                    rigaDto.getObbligazioneScadenzarioKey().getEsercizio_originale(),
                    rigaDto.getObbligazioneScadenzarioKey().getPg_obbligazione(),
                    rigaDto.getObbligazioneScadenzarioKey().getPg_obbligazione_scadenzario()));

           rigaBulk.setTi_associato_manrev(EnumAssMandRevDocGenRiga.NO_ASSOCIATO_A_MAND_REV.getAssMandRev());
           rigaBulk.setToBeCreated();
       }
    }


    @Override
    public Response get(String cd_cds, String cd_unita_organizzativa, Integer esercizio,Long pg_documento_generico) throws Exception {

        Documento_generico_passivoBulk documentoGenericoPassivoBulk =new Documento_generico_passivoBulk(cd_cds,
                            Documento_genericoBulk.GENERICO_S,
                            cd_unita_organizzativa,
                            esercizio,
                            pg_documento_generico);
        return getDocumentoGenerico( documentoGenericoPassivoBulk);


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

package it.cnr.contab.web.rest.resource.doccont;

import it.cnr.contab.anagraf00.core.bulk.TerzoKey;
import it.cnr.contab.docamm00.docs.bulk.*;
import it.cnr.contab.docamm00.ejb.DocumentoGenericoComponentSession;
import it.cnr.contab.doccont00.core.bulk.Accertamento_scadenzarioBulk;
import it.cnr.contab.doccont00.core.bulk.Accertamento_scadenzarioKey;
import it.cnr.contab.utenze00.bp.CNRUserContext;
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
                rigaPassDto.setTerzoDebitore(terzoCreditore);
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
    protected Documento_genericoBulk initializeDocumentoGenerico(CNRUserContext userContext, DocumentoGenericoAttivoDto documentoGenericoDto) throws ComponentException, RemoteException {
        Documento_genericoBulk documentoGenericoBulk = new Documento_genericoBulk();
        documentoGenericoBulk.setTi_entrate_spese(Documento_genericoBulk.ENTRATE);
        documentoGenericoBulk.setTipo_documento(new Tipo_documento_ammBulk( Numerazione_doc_ammBulk.TIPO_DOC_GENERICO_E));
        documentoGenericoComponentSession.inizializzaBulkPerInserimento(userContext,documentoGenericoBulk);
        return documentoGenericoBulk;
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

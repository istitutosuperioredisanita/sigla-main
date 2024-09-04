package it.cnr.contab.web.rest.resource.doccont;

import it.cnr.contab.anagraf00.core.bulk.TerzoKey;
import it.cnr.contab.docamm00.docs.bulk.*;
import it.cnr.contab.docamm00.ejb.DocumentoGenericoComponentSession;
import it.cnr.contab.doccont00.core.bulk.Accertamento_scadenzarioKey;
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
        return;
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
        return null;
    }

    @Override
    public Response update(String cd_cds, String cd_unita_organizzativa, Integer esercizio,  Long pg_documento_generico) throws Exception {
        return null;
    }
}

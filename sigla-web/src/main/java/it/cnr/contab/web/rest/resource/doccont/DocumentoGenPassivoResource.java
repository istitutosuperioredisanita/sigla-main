package it.cnr.contab.web.rest.resource.doccont;

import it.cnr.contab.docamm00.docs.bulk.Documento_genericoBulk;
import it.cnr.contab.docamm00.docs.bulk.Documento_generico_rigaBulk;
import it.cnr.contab.docamm00.ejb.DocumentoGenericoComponentSession;
import it.cnr.contab.doccont00.core.bulk.Obbligazione_scadenzarioKey;
import it.cnr.contab.web.rest.local.config00.DocumentoGenericoPassivoLocal;
import it.cnr.contab.web.rest.model.DocumentoGenericoPassRigaDto;
import it.cnr.contab.web.rest.model.DocumentoGenericoPassivoDto;
import it.cnr.contab.web.rest.model.DocumentoGenericoRigaDto;
import it.cnr.jada.UserContext;
import it.cnr.jada.ejb.CRUDComponentSession;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@Stateless
public class DocumentoGenPassivoResource extends AbstractDocumentoGenericoResource<DocumentoGenericoPassivoDto> implements DocumentoGenericoPassivoLocal {


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
        return Documento_genericoBulk.GENERICO_S;
    }

    @Override
    protected void addInfoContToDocRigheDto(Documento_generico_rigaBulk rigaBulk, DocumentoGenericoRigaDto rigaDto, UserContext userContext){

                DocumentoGenericoPassRigaDto rigaPassDto= (DocumentoGenericoPassRigaDto) rigaDto;
        rigaPassDto.setObbligazioneScadenzarioKey( new Obbligazione_scadenzarioKey(rigaBulk.getObbligazione_scadenziario().getCd_cds(),
                                            rigaBulk.getObbligazione_scadenziario().getEsercizio(),
                                            rigaBulk.getObbligazione_scadenziario().getEsercizio_originale(),
                                            rigaBulk.getObbligazione_scadenziario().getPg_obbligazione(),
                        rigaBulk.getObbligazione_scadenziario().getPg_obbligazione_scadenzario()));
    }


    @Override
    public Response get(String cd_cds, String cd_unita_organizzativa, Integer esercizio,Long pg_documento_generico) throws Exception {
        return getDocumentoGenerico( cd_cds,cd_unita_organizzativa,esercizio,pg_documento_generico);


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

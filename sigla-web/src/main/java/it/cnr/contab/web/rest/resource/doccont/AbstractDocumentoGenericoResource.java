package it.cnr.contab.web.rest.resource.doccont;

import it.cnr.contab.anagraf00.core.bulk.BancaKey;
import it.cnr.contab.anagraf00.core.bulk.TerzoKey;
import it.cnr.contab.config00.sto.bulk.CdsKey;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaKey;
import it.cnr.contab.docamm00.docs.bulk.Documento_genericoBulk;
import it.cnr.contab.docamm00.docs.bulk.Documento_generico_rigaBulk;
import it.cnr.contab.docamm00.ejb.DocumentoGenericoComponentSession;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.web.rest.exception.RestException;
import it.cnr.contab.web.rest.model.DocumentoGenericoDto;
import it.cnr.contab.web.rest.model.DocumentoGenericoRigaDto;
import it.cnr.jada.UserContext;
import it.cnr.jada.ejb.CRUDComponentSession;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.lang.reflect.ParameterizedType;
import java.util.Optional;
@Stateless
abstract public class AbstractDocumentoGenericoResource<T extends DocumentoGenericoDto>  {

    @Context
    SecurityContext securityContext;
    @EJB
    CRUDComponentSession crudComponentSession;
    @EJB
    DocumentoGenericoComponentSession documentoGenericoComponentSession;


    public Response insertDocumentoGenerico(HttpServletRequest request, T documentoGenericoPassivoDto) throws Exception {
        return null;
    }

    abstract String getCdTipoDocumentoAmm();

    @SuppressWarnings("unchecked")
    public Class reflectClassType() {
        return ((Class) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0]);
    }


    abstract protected void addInfoContToDocRigheDto(Documento_generico_rigaBulk rigaBulk,  DocumentoGenericoRigaDto  rigaDto,UserContext userContext);
    protected void addRigheToDocumentoGenDto(Documento_genericoBulk bulk, DocumentoGenericoDto documentoGenericoDto,UserContext userContext) throws InstantiationException, IllegalAccessException {
        if (Optional.ofNullable(bulk.getDocumento_generico_dettColl()).isPresent()){
            for (Documento_generico_rigaBulk riga:bulk.getDocumento_generico_dettColl()){
                DocumentoGenericoRigaDto rigaDto = (DocumentoGenericoRigaDto) documentoGenericoDto.reflectClassType().newInstance();

                rigaDto.setProgressivo_riga(riga.getProgressivo_riga());
                rigaDto.setDs_riga(riga.getDs_riga());
                rigaDto.setIm_riga(riga.getIm_riga());
                rigaDto.setDt_a_competenza_coge(riga.getDt_a_competenza_coge());
                rigaDto.setDt_da_competenza_coge(riga.getDt_da_competenza_coge());
                rigaDto.setDt_cancellazione(riga.getDt_cancellazione());
                rigaDto.setTerzoKey(new TerzoKey( riga.getCd_terzo()));
                rigaDto.setBancaKey(new BancaKey( riga.getCd_terzo(),riga.getPg_banca()));
                addInfoContToDocRigheDto( riga,rigaDto,userContext);
                documentoGenericoDto.getRighe().add(rigaDto);
            }
        }
    }
    protected T documentoGenBulkToDocumentoGenDto(Documento_genericoBulk bulk, UserContext userContext) throws InstantiationException, IllegalAccessException {
        if ( !Optional.ofNullable(bulk).isPresent())
            return null;
        DocumentoGenericoDto documentoGenericoDto = ( DocumentoGenericoDto) reflectClassType().newInstance();
        documentoGenericoDto.setCdsKey( new CdsKey(bulk.getCd_cds()));
        documentoGenericoDto.setUnitaOrganizzativaKey(new Unita_organizzativaKey(bulk.getCd_unita_organizzativa()));
        documentoGenericoDto.setDs_documento_generico(bulk.getDs_documento_generico());
        documentoGenericoDto.setPg_documento_generico(bulk.getPg_documento_generico());
        documentoGenericoDto.setData_registrazione(bulk.getData_registrazione());
        documentoGenericoDto.setDt_scadenza(bulk.getDt_scadenza());
        documentoGenericoDto.setDt_a_competenza_coge(bulk.getDt_a_competenza_coge());
        documentoGenericoDto.setDt_da_competenza_coge(bulk.getDt_da_competenza_coge());
        addRigheToDocumentoGenDto( bulk,documentoGenericoDto,userContext);


        return ( T) documentoGenericoDto;
    }


    public Response getDocumentoGenerico( Documento_genericoBulk documentoGenericoBulk) throws Exception {
        try{
            CNRUserContext userContext = (CNRUserContext) securityContext.getUserPrincipal();
            Documento_genericoBulk documento_genericoBulk= ( Documento_genericoBulk) documentoGenericoComponentSession.findByPrimaryKey(userContext,
                    documentoGenericoBulk);
            documento_genericoBulk= ( Documento_genericoBulk)documentoGenericoComponentSession.inizializzaBulkPerModifica(userContext,documento_genericoBulk);
            if ( !Optional.ofNullable(documento_genericoBulk).isPresent())
                throw new RestException(Response.Status.NOT_FOUND,String.format("Documento Generico non presente!"));
            //obbligazioneBulk= ( ObbligazioneBulk)obbligazioneComponentSession.inizializzaBulkPerModifica(userContext,obbligazioneBulk);
            //return Response.status(Response.Status.OK).entity(obbligazioneBulkToObbligazioneDto( obbligazioneBulk )).build();
            return Response.status(Response.Status.OK).entity(documentoGenBulkToDocumentoGenDto( documento_genericoBulk,userContext)).build();
        }catch (Throwable e){
            if ( e instanceof RestException)
                throw e;
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR,String.format(e.getMessage()));
        }
    }


    public Response deleteDocumentoGenerico(String cd_cds, String cd_unita_organizzativa, Integer esercizio,  Long pg_documento_generico) throws Exception {
        try{

            CNRUserContext userContext = (CNRUserContext) securityContext.getUserPrincipal();
            Boolean result=documentoGenericoComponentSession.deleteDocumentoGenericoWs
                    (userContext,cd_cds,getCdTipoDocumentoAmm(),cd_unita_organizzativa,esercizio,pg_documento_generico);
            if ( result)
                return Response.status(Response.Status.OK).build();

            return Response.status(Response.Status.NO_CONTENT).build();

        }catch (Throwable e){
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR,String.format(e.getMessage()));
        }
    }


    public Response updateDocumentoGenerico(String cd_cds, String cd_unita_organizzativa, Integer esercizio,  Long pg_documento_generico) throws Exception {
        return null;
    }
}

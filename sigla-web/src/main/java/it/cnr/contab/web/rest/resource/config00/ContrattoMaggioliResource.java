package it.cnr.contab.web.rest.resource.config00;

import it.cnr.contab.config00.contratto.bulk.Ass_contratto_uoBulk;
import it.cnr.contab.config00.contratto.bulk.ContrattoBulk;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.web.rest.exception.RestException;
import it.cnr.contab.web.rest.local.config00.ContrattoMaggioliLocal;
import it.cnr.contab.web.rest.model.ContrattoDtoBulk;
import it.cnr.contab.web.rest.model.EnumNaturaContabileContratto;
import it.cnr.contab.web.rest.model.EnumTipoDettaglioContratto;
import it.cnr.contab.web.rest.model.EnumTypeAttachmentContratti;
import it.cnr.jada.DetailedRuntimeException;
import it.cnr.jada.bulk.BulkList;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.bulk.ValidationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.Optional;

@Stateless
public class ContrattoMaggioliResource  extends AbstractContrattoResource implements ContrattoMaggioliLocal {
    private final Logger _log = LoggerFactory.getLogger(ContrattoMaggioliResource.class);

    @Override
    protected ContrattoBulk creaContrattoSigla(ContrattoDtoBulk contrattoBulk, CNRUserContext userContext, ApiVersion apiVersion) throws PersistencyException, ValidationException, ComponentException, RemoteException {
        // verificare che in associazioneUo ci sia l'unita operativa 000.000 altrimenti inserirla
        ContrattoBulk contrattoToSave= super.creaContrattoSigla(contrattoBulk, userContext,apiVersion);
        if ( ApiVersion.V2.equals(apiVersion)) {
            contrattoToSave.setCodfisPivaAggiudicatarioExt(contrattoBulk.getCodFisPivaAggiudicatari().get(0));
        }
        if (! Optional.ofNullable((( BulkList<Ass_contratto_uoBulk>) contrattoToSave.getAssociazioneUO()).
                stream().
                filter(el->"000.000".equalsIgnoreCase(el.getCd_unita_organizzativa())).findFirst().orElse(null)).isPresent()){
                Ass_contratto_uoBulk ass000000 =new Ass_contratto_uoBulk();
                    ass000000.setUnita_organizzativa(new Unita_organizzativaBulk());
                    ass000000.setCd_unita_organizzativa("000.000");
                    ass000000.setContratto(contrattoToSave);
                    ass000000.setEsercizio(contrattoToSave.getEsercizio());
                    ass000000.setStato_contratto(contrattoToSave.getStato());
                    ass000000.setCrudStatus( OggettoBulk.TO_BE_CREATED);
                contrattoToSave.addToAssociazioneUO(ass000000);
        }
        return contrattoToSave;
    }

    @Override
    public void validateContratto(ContrattoDtoBulk contrattoBulk, CNRUserContext userContext,ApiVersion apiVersion) throws RemoteException, ComponentException{
        //Check valore tipoDettaglioContratto
        super.validateContratto(contrattoBulk,userContext,apiVersion);
        if (Optional.ofNullable(contrattoBulk.getTipoDettaglioContratto()).isPresent()){
            if ( !(contrattoBulk.getTipoDettaglioContratto().equals(EnumTipoDettaglioContratto.DETTAGLIO_CONTRATTO_ARTICOLI)) &&
                    (!contrattoBulk.getTipoDettaglioContratto().equals(EnumTipoDettaglioContratto.DETTAGLIO_CONTRATTO_CATGRP)))
                throw new RestException(Response.Status.BAD_REQUEST, String.format("Per Il Tipo Dettaglio Contratto sono previsti i seguenti valori:{ vuoto,"
                        + EnumTipoDettaglioContratto.DETTAGLIO_CONTRATTO_ARTICOLI+","+EnumTipoDettaglioContratto.DETTAGLIO_CONTRATTO_CATGRP)+"}");
        }
        if (CollectionUtils.isEmpty(contrattoBulk.getAttachments()))
            throw new RestException(Response.Status.BAD_REQUEST,String.format("Deve essere presente tra gli allegati il documento del contratto"));

        contrattoBulk.getAttachments().stream().filter(a->a.getTypeAttachment().equals(EnumTypeAttachmentContratti.CONTRATTO_FLUSSO)).findFirst().
                orElseThrow(
                        () -> new DetailedRuntimeException("Il file del contratto è obbligatorio"));
        if ( ApiVersion.V2.equals(apiVersion)) {
            if (!Optional.ofNullable(contrattoBulk.getNaturaContabileContratto()).isPresent())
                throw new RestException(Response.Status.BAD_REQUEST, String.format("La natura Contabile non può essere vuota. Sono Previsti i seguenti valori:{ "
                        + EnumNaturaContabileContratto.NATURA_CONTABILE_PASSIVO + "," + EnumNaturaContabileContratto.NATURA_CONTABILE_SENZA_FLUSSI_FINANZIARI) + "}");

            if ( EnumNaturaContabileContratto.NATURA_CONTABILE_SENZA_FLUSSI_FINANZIARI.equals(contrattoBulk.getNaturaContabileContratto())){
                if ( !Optional.ofNullable(contrattoBulk.getCdCigPadre()).isPresent())
                    throw new RestException(Response.Status.BAD_REQUEST, String.format("Per Contratti senza flussi finanziari è necessario indicare il cig del contratto Padre "));
            }

            if (Optional.ofNullable(contrattoBulk.getCodFisPivaAggiudicatari()).orElse(Collections.emptyList()).size() == 0)
                throw new RestException(Response.Status.BAD_REQUEST, String.format("Manca l'informazione degli aggiudicatari del contratto"));
            if (Optional.ofNullable(contrattoBulk.getCodFisPivaAggiudicatari()).orElse(Collections.emptyList()).size() > 1)
                throw new RestException(Response.Status.BAD_REQUEST, String.format("La gestione dei multiaggiudicari ancora non è implementata"));
        }
    }

    protected ContrattoBulk innerCreaContrattoBulk( CNRUserContext userContext ,ContrattoBulk contratto) throws ComponentException, RemoteException {
        return (ContrattoBulk) contrattoComponentSession.creaContrattoDaFlussoAcquisti(userContext, contratto,true);
    }


    @Override
    public Response insertContrattoV2(HttpServletRequest request, ContrattoDtoBulk contrattoMaggioliBulk) throws Exception {
        return insertContratto(request,contrattoMaggioliBulk,ApiVersion.V2);
    }

    @Override
    public Response recuperoDatiContratto(HttpServletRequest request, String uo, Integer cdTerzo) throws Exception {
        _log.info("recuperoDatiContratto->Maggioli" );
       return super.recuperoDatiContratto(request,uo,cdTerzo);
    }
}

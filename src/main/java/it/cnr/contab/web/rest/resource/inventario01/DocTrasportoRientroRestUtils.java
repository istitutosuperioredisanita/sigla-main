package it.cnr.contab.web.rest.resource.inventario01;

import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientro_dettBulk;
import it.cnr.contab.web.rest.model.DocTRWSResponse.LinkDTO;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class DocTrasportoRientroRestUtils {

    private DocTrasportoRientroRestUtils() {
    }

    // ETAG
    public static EntityTag buildETag(Doc_trasporto_rientroBulk doc) {
        return new EntityTag(Integer.toHexString(buildETagRaw(doc).hashCode()));
    }

    public static EntityTag buildETag(List<Doc_trasporto_rientroBulk> documenti) {
        String raw = documenti.stream()
                .map(DocTrasportoRientroRestUtils::buildETagRaw)
                .collect(Collectors.joining("|"));
        return new EntityTag(Integer.toHexString(raw.hashCode()));
    }

    private static String buildETagRaw(Doc_trasporto_rientroBulk doc) {
        return String.join("-",
                String.valueOf(doc.getPgInventario()),
                String.valueOf(doc.getTiDocumento()),
                String.valueOf(doc.getEsercizio()),
                String.valueOf(doc.getPgDocTrasportoRientro()),
                String.valueOf(doc.getPg_ver_rec())
        );
    }

    /**
     * Valuta se la richiesta HTTP contiene un ETag valido che corrisponde allo stato attuale.
     * Ritorna una Response 304 Not Modified se il client è aggiornato, altrimenti null.
     */
    public static Response evaluateNotModified(Request jaxrsRequest, EntityTag eTag) {
        Response.ResponseBuilder notModifiedBuilder = jaxrsRequest.evaluatePreconditions(eTag);
        if (notModifiedBuilder != null) {
            return notModifiedBuilder.tag(eTag).build();
        }
        return null;
    }

    // HATEOAS
    public static List<LinkDTO> costruisciLinks(UriInfo uriInfo, Doc_trasporto_rientroBulk doc) {
        List<LinkDTO> links = new ArrayList<>();

        String basePath = uriInfo.getBaseUriBuilder()
                .path("docTrasportoRientro")
                .build()
                .toString();

        String selfHref = basePath + "/"
                + doc.getPgInventario() + "/"
                + doc.getTiDocumento() + "/"
                + doc.getEsercizio() + "/"
                + doc.getPgDocTrasportoRientro();

        links.add(new LinkDTO("self", selfHref, "GET"));

        if (doc.getDoc_trasporto_rientro_dettColl() != null
                && !doc.getDoc_trasporto_rientro_dettColl().isEmpty()) {

            Doc_trasporto_rientro_dettBulk primoDettaglio =
                    (Doc_trasporto_rientro_dettBulk) doc.getDoc_trasporto_rientro_dettColl().get(0);

            String ricercaBeneHref = uriInfo.getBaseUriBuilder()
                    .path("docTrasportoRientro")
                    .queryParam("tiDocumento", doc.getTiDocumento())
                    .queryParam("esercizio", doc.getEsercizio())
                    .queryParam("nrInventario", primoDettaglio.getNr_inventario())
                    .build()
                    .toString();

            links.add(new LinkDTO("ricerca-per-bene", ricercaBeneHref, "GET"));
        }

        return links;
    }
}
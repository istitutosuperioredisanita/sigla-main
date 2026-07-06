package it.cnr.contab.web.rest.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import it.cnr.contab.inventario01.bulk.*;
import it.cnr.contab.util00.bulk.storage.AllegatoGenericoBulk;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DocTRWSResponse(
        boolean ok,
        String message,
        DocumentoDTO documento,
        List<DocumentoDTO> documenti
) {

    public static DocTRWSResponse of(boolean ok, String message, Doc_trasporto_rientroBulk documento) {
        return of(ok, message, documento, null);
    }

    public static DocTRWSResponse of(
            boolean ok,
            String message,
            Doc_trasporto_rientroBulk documento,
            List<LinkDTO> links) {

        return new DocTRWSResponse(
                ok,
                message,
                documento != null ? DocumentoDTO.from(documento, links) : null,
                null
        );
    }

    public static DocTRWSResponse ofList(boolean ok, String message, List<DocumentoDTO> documenti) {
        return new DocTRWSResponse(ok, message, null, documenti);
    }

    public static DocTRWSResponse messageOnly(boolean ok, String message) {
        return new DocTRWSResponse(ok, message, null, null);
    }

    // =====================================================================
    // LINK HATEOAS
    // =====================================================================

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record LinkDTO(String rel, String href, String method) {
    }

    // =====================================================================
    // DOCUMENTO
    // =====================================================================

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record DocumentoDTO(

            Long pgInventario,
            String tiDocumento,
            Integer esercizio,
            Long pgDocTrasportoRientro,
            String dsDocTrasportoRientro,

            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
            Date dataRegistrazione,

            String cdTipoTrasportoRientro,
            String stato,
            String statoFlusso,
            String destinazione,
            String indirizzo,
            Boolean flIncaricato,
            Boolean flVettore,
            Integer cdTerzoIncaricato,
            String nominativoVettore,
            String noteRitiro,
            String note,
            String uuidFlussoAutorizzativo,

            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
            Timestamp dataInvioFirma,

            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss.SSS")
            Timestamp dataFirma,

            String noteRifiuto,
            Integer cdTerzoResponsabile,
            String utenteRemotoRequest,
            Long pgVerRec,
            List<DettaglioDTO> dettagli,
            List<AllegatoDTO> allegati,
            List<LinkDTO> links
    ) {

        public static DocumentoDTO from(Doc_trasporto_rientroBulk doc) {
            return from(doc, null);
        }

        public static DocumentoDTO from(Doc_trasporto_rientroBulk doc, List<LinkDTO> links) {

            var dettagli = doc.getDoc_trasporto_rientro_dettColl() == null
                    ? List.<DettaglioDTO>of()
                    : doc.getDoc_trasporto_rientro_dettColl()
                    .stream()
                    .map(o -> DettaglioDTO.from((Doc_trasporto_rientro_dettBulk) o))
                    .toList();

            var allegati = doc.getArchivioAllegati() == null
                    ? List.<AllegatoDTO>of()
                    : doc.getArchivioAllegati()
                    .stream()
                    .map(AllegatoDTO::from)
                    .toList();

            return new DocumentoDTO(
                    doc.getPgInventario(),
                    doc.getTiDocumento(),
                    doc.getEsercizio(),
                    doc.getPgDocTrasportoRientro(),
                    doc.getDsDocTrasportoRientro(),
                    doc.getDataRegistrazione(),
                    doc.getCdTipoTrasportoRientro(),
                    doc.getStato(),
                    doc.getStatoFlusso(),
                    doc.getDestinazione(),
                    doc.getIndirizzo(),
                    doc.getFlIncaricato(),
                    doc.getFlVettore(),
                    doc.getCdTerzoIncaricato(),
                    doc.getNominativoVettore(),
                    doc.getNoteRitiro(),
                    doc.getNote(),
                    doc.getUuidFlussoAutorizzativo(),
                    doc.getDataInvioFirma(),
                    doc.getDataFirma(),
                    doc.getNoteRifiuto(),
                    doc.getCdTerzoResponsabile(),
                    doc.getUtenteRemotoRequest(),
                    doc.getPg_ver_rec(),
                    dettagli,
                    allegati,
                    links
            );
        }
    }

    // =====================================================================
    // DETTAGLIO
    // =====================================================================

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record DettaglioDTO(

            Long pgInventario,
            String tiDocumento,
            Integer esercizio,
            Long pgDocTrasportoRientro,
            Long nrInventario,
            Integer progressivo,
            Integer cdTerzoAssegnatario,
            Long pgInventarioRif,
            String tiDocumentoRif,
            Integer esercizioRif,
            Long pgDocTrasportoRientroRif,
            Long nrInventarioRif,
            Integer progressivoRif
    ) {

        public static DettaglioDTO from(
                Doc_trasporto_rientro_dettBulk d) {

            return new DettaglioDTO(
                    d.getPgInventario(),
                    d.getTiDocumento(),
                    d.getEsercizio(),
                    d.getPgDocTrasportoRientro(),
                    d.getNr_inventario(),
                    d.getProgressivo(),
                    d.getCdTerzoAssegnatario(),
                    d.getPgInventarioRif(),
                    d.getTiDocumentoRif(),
                    d.getEsercizioRif(),
                    d.getPgDocTrasportoRientroRif(),
                    d.getNrInventarioRif(),
                    d.getProgressivoRif()
            );
        }
    }

    // =====================================================================
    // ALLEGATO
    // =====================================================================

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record AllegatoDTO(
            String nome,
            String descrizione,
            String aspectName,
            String contentType
    ) {

        public static AllegatoDTO from(
                AllegatoGenericoBulk allegato) {

            String aspectName = switch (allegato) {
                case AllegatoDocTraspRientroBulk a -> a.getAspectName();
                default -> null;
            };

            return new AllegatoDTO(
                    allegato.getNome(),
                    allegato.getDescrizione(),
                    aspectName,
                    allegato.getContentType()
            );
        }
    }
}
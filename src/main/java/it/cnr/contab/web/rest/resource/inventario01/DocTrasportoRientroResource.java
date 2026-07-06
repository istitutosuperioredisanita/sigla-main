package it.cnr.contab.web.rest.resource.inventario01;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.cnr.contab.inventario00.docs.bulk.InventarioDocTRBulk;
import it.cnr.contab.inventario00.tabrif.bulk.Id_inventarioBulk;
import it.cnr.contab.inventario01.bulk.*;
import it.cnr.contab.inventario01.ejb.DocTrasportoRientroComponentSession;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.web.rest.exception.RestException;
import it.cnr.contab.web.rest.local.inventario01.DocTrasportoRientroLocal;
import it.cnr.contab.web.rest.model.DocTRWSResponse;
import it.cnr.jada.bulk.SimpleBulkList;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static it.cnr.jada.bulk.OggettoBulk.isNullOrEmpty;

/**
 * Resource REST per la gestione dei documenti di Trasporto e Rientro dei beni inventariali.
 * Espone le operazioni per:
 * - creazione di documenti di Trasporto o Rientro (stato INSERITO, senza allegati)
 * - gestione degli allegati per documenti in stato FIRMATO
 * - ricerca dei documenti associati ad un bene inventariale
 * FLUSSO STATI:
 * INS (inserito) → INV (inviato in firma) → INV+FIR (firmato) → DEF (definitivo)
 * Gli allegati sono ammessi via REST SOLO quando il documento è in stato FIRMATO (statoFlusso=FIR).
 * In fase di creazione (stato INS) non è consentito allegare documenti.
 * La logica applicativa principale viene delegata al componente
 * {@link DocTrasportoRientroComponentSession}.
 */
@Stateless
public class DocTrasportoRientroResource implements DocTrasportoRientroLocal {

    private final Logger log = LoggerFactory.getLogger(DocTrasportoRientroResource.class);

    @Context
    SecurityContext securityContext;

    @EJB
    DocTrasportoRientroComponentSession componenteDocTR;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final int MIN_PDF_FILE_SIZE_BYTES = 512;

    /**
     * Salva un documento di Trasporto o Rientro in stato INSERITO.
     * Il metodo esegue:
     * - estrazione del contesto utente
     * - deserializzazione del body JSON
     * - validazione della testata
     * - validazione dei dettagli
     * - validazione assenza allegati (non ammessi in stato INSERITO)
     * - salvataggio tramite componente EJB
     *
     * @param request richiesta HTTP
     * @param body    body JSON della richiesta
     * @return Response HTTP con documento salvato
     * @throws Exception errore durante il processo di salvataggio
     */
    @Override
    public Response saveDocTR(@Context HttpServletRequest request,
                              Map<String, Object> body) throws Exception {

        CNRUserContext ctx = (CNRUserContext) securityContext.getUserPrincipal();
        Doc_trasporto_rientroBulk bulk = deserializzaBulk(body);
        validaRichiestaHttp(bulk, ctx);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rawDettagli =
                (List<Map<String, Object>>) body.get("dettagli");

        List<Doc_trasporto_rientro_dettBulk> dettagli =
                deserializzaDettagli(bulk, rawDettagli);

        validaDettagliHttp(bulk, dettagli);
        validaAssenzeAllegatiHttp(body);

        bulk.setDoc_trasporto_rientro_dettColl(
                new SimpleBulkList<>(dettagli));

        try {
            Doc_trasporto_rientroBulk docSalvato =
                    componenteDocTR.saveDocFromWS(ctx, bulk);

            String message =
                    Doc_trasporto_rientroBulk.TRASPORTO.equals(docSalvato.getTiDocumento())
                            ? "Documento di Trasporto salvato con successo"
                            : "Documento di Rientro salvato con successo";

            return Response.status(Response.Status.CREATED)
                    .entity(DocTRWSResponse.of(true, message, docSalvato))
                    .build();

        } catch (Exception e) {
            log.error("Errore durante saveDocTR", e);
            return buildErrorResponse(
                    Response.Status.INTERNAL_SERVER_ERROR,
                    e,
                    "Documento non salvato"
            );
        }
    }

    /**
     * Ricerca di un bene inventariale all'interno di un Documento di Trasporto o Rientro*
     *
     * @param request      richiesta HTTP
     * @param jaxrsRequest richiesta JAX-RS
     * @param uriInfo      informazioni URI della richiesta
     * @param tiDocumento  tipo documento (T=Trasporto, R=Rientro)
     * @param stato        stato del documento
     * @param esercizio    esercizio contabile
     * @param nrInventario numero inventario del bene
     * @return documento trovato con dettagli e allegati
     * @throws Exception errore durante la ricerca
     */
    @Override
    public Response getBeneIntoDoc(
            @Context HttpServletRequest request,
            @Context Request jaxrsRequest,
            @Context UriInfo uriInfo,
            String tiDocumento,
            String stato,
            Integer esercizio,
            String nrInventario)
            throws Exception {

        CNRUserContext ctx = (CNRUserContext) securityContext.getUserPrincipal();
        validaParametriRicercaDocumento(tiDocumento, nrInventario);

        Integer esercizioEffettivo = esercizio != null ? esercizio : ctx.getEsercizio();

        try {
            List<Doc_trasporto_rientroBulk> documenti = componenteDocTR.cercaDocumentiPerBene(
                    ctx, tiDocumento, stato, nrInventario, esercizioEffettivo);

            if (documenti == null || documenti.isEmpty()) {
                return Response.ok(DocTRWSResponse.messageOnly(false, "Nessun documento trovato")).build();
            }

            EntityTag eTag = DocTrasportoRientroRestUtils.buildETag(documenti);
            Response notModifiedResp = DocTrasportoRientroRestUtils.evaluateNotModified(jaxrsRequest, eTag);
            if (notModifiedResp != null) {
                return notModifiedResp;
            }

            List<DocTRWSResponse.DocumentoDTO> documentiDTO = documenti.stream()
                    .map(doc -> DocTRWSResponse.DocumentoDTO.from(doc, DocTrasportoRientroRestUtils.costruisciLinks(uriInfo, doc)))
                    .toList();

            String message = documenti.size() == 1
                    ? (documenti.get(0).isTrasporto() ? "Documento di Trasporto trovato" : "Documento di Rientro trovato")
                    : "Trovati " + documenti.size() + " documenti";

            return Response.ok()
                    .tag(eTag)
                    .entity(DocTRWSResponse.ofList(true, message, documentiDTO))
                    .build();

        } catch (Exception e) {
            log.error(
                    "Errore durante getBeneIntoDoc (tipo={}, stato={}, esercizio={}, nrInventario={})",
                    tiDocumento, stato, esercizioEffettivo, nrInventario, e
            );
            return buildErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, e, "Errore durante la ricerca del documento");
        }
    }

    @Override
    public Response get(
            @Context HttpServletRequest request,
            @Context Request jaxrsRequest,
            @Context UriInfo uriInfo,
            Long pgInventario,
            String tiDocumento,
            Integer esercizio,
            Long pgDocTrasportoRientro)
            throws Exception {

        try {
            CNRUserContext ctx = (CNRUserContext) securityContext.getUserPrincipal();

            if (pgInventario == null) {
                throw new RestException(Response.Status.BAD_REQUEST, "pgInventario obbligatorio");
            }
            if (esercizio == null) {
                throw new RestException(Response.Status.BAD_REQUEST, "esercizio obbligatorio");
            }
            if (pgDocTrasportoRientro == null) {
                throw new RestException(Response.Status.BAD_REQUEST, "pgDocTrasportoRientro obbligatorio");
            }

            Doc_trasporto_rientroBulk chiave =
                    costruisciChiavePerRicerca(tiDocumento, pgInventario, esercizio, pgDocTrasportoRientro);

            Doc_trasporto_rientroBulk documento = componenteDocTR.findDocTrasportoRientro(ctx, chiave);

            if (Optional.ofNullable(documento).isEmpty()) {
                throw new RestException(Response.Status.NOT_FOUND, "Documento Trasporto/Rientro non presente!");
            }

            EntityTag eTag = DocTrasportoRientroRestUtils.buildETag(documento);
            Response notModifiedResp = DocTrasportoRientroRestUtils.evaluateNotModified(jaxrsRequest, eTag);
            if (notModifiedResp != null) {
                return notModifiedResp;
            }

            String message = documento.isTrasporto()
                    ? "Documento di Trasporto trovato"
                    : "Documento di Rientro trovato";

            return Response.status(Response.Status.OK)
                    .tag(eTag)
                    .entity(DocTRWSResponse.of(
                            true,
                            message,
                            documento,
                            DocTrasportoRientroRestUtils.costruisciLinks(uriInfo, documento) // DELEGA HATEOAS
                    ))
                    .build();

        } catch (Throwable e) {
            if (e instanceof RestException) {
                throw (RestException) e;
            }
            log.error(
                    "Errore durante get (pgInventario={}, tiDocumento={}, esercizio={}, pgDocTrasportoRientro={})",
                    pgInventario, tiDocumento, esercizio, pgDocTrasportoRientro, e
            );
            throw new RestException(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }


    /**
     * Costruisce l'istanza concreta (DocumentoTrasportoBulk/DocumentoRientroBulk)
     * usata come chiave di ricerca, coerente col tipo documento indicato.
     */
    private Doc_trasporto_rientroBulk costruisciChiavePerRicerca(
            String tiDocumento, Long pgInventario, Integer esercizio, Long pgDocTrasportoRientro) {

        if (isNullOrEmpty(tiDocumento)) {
            throw new RestException(
                    Response.Status.BAD_REQUEST,
                    "tiDocumento obbligatorio (valori ammessi: T o R)"
            );
        }

        Doc_trasporto_rientroBulk chiave;

        if (Doc_trasporto_rientroBulk.TRASPORTO.equals(tiDocumento)) {
            chiave = new DocumentoTrasportoBulk();
        } else if (Doc_trasporto_rientroBulk.RIENTRO.equals(tiDocumento)) {
            chiave = new DocumentoRientroBulk();
        } else {
            throw new RestException(
                    Response.Status.BAD_REQUEST,
                    "tiDocumento non valido '" + tiDocumento
                            + "': valori ammessi T (Trasporto) o R (Rientro)"
            );
        }

        chiave.setPgInventario(pgInventario);
        chiave.setTiDocumento(tiDocumento);
        chiave.setEsercizio(esercizio);
        chiave.setPgDocTrasportoRientro(pgDocTrasportoRientro);

        return chiave;
    }

    /**
     * Deserializza la testata del documento dal body JSON.
     */
    private Doc_trasporto_rientroBulk deserializzaBulk(Map<String, Object> body) {
        try {
            String json = MAPPER.writeValueAsString(body);
            return MAPPER.readValue(json, Doc_trasporto_rientroBulk.class);
        } catch (JsonProcessingException e) {
            throw new RestException(
                    Response.Status.BAD_REQUEST,
                    "Body testata non valido: " + e.getOriginalMessage()
            );
        }
    }

    /**
     * Converte i dettagli ricevuti dal body JSON in oggetti Bulk tipizzati.
     */
    private List<Doc_trasporto_rientro_dettBulk> deserializzaDettagli(
            Doc_trasporto_rientroBulk bulk,
            List<Map<String, Object>> rawDettagli) {

        if (rawDettagli == null || rawDettagli.isEmpty()) {
            return Collections.emptyList();
        }

        List<Doc_trasporto_rientro_dettBulk> result = new ArrayList<>();
        boolean isRientro = Doc_trasporto_rientroBulk.RIENTRO.equals(bulk.getTiDocumento());

        for (Map<String, Object> map : rawDettagli) {

            Long pgInventario = toLong(map.get("pgInventario"));
            Long nrInventario = toLong(map.get("nrInventario"));
            Integer progressivo = toInt(map.get("progressivo"));
            int progSafe = progressivo != null ? progressivo : 0;

            Doc_trasporto_rientro_dettBulk dett;

            if (isRientro) {

                DocumentoRientroDettBulk rDett = new DocumentoRientroDettBulk();

                DocumentoTrasportoDettBulk rif = new DocumentoTrasportoDettBulk();
                rif.setDoc_trasporto_rientro(new DocumentoTrasportoBulk());
                rif.setBene(new InventarioDocTRBulk());
                rDett.setDocTrasportoDettRif(rif);

                InventarioDocTRBulk bene = new InventarioDocTRBulk();
                bene.setInventario(new Id_inventarioBulk());
                bene.setNr_inventario(nrInventario);
                bene.setPg_inventario(pgInventario);
                bene.setProgressivo((long) progSafe);
                rDett.setBene(bene);

                rDett.setPgInventarioRif(toLong(map.get("pgInventarioRif")));
                rDett.setNrInventarioRif(toLong(map.get("nrInventarioRif")));
                rDett.setProgressivoRif(toInt(map.get("progressivoRif")));
                rDett.setTiDocumentoRif((String) map.get("tiDocumentoRif"));
                rDett.setEsercizioRif(toInt(map.get("esercizioRif")));
                rDett.setPgDocTrasportoRientroRif(toLong(map.get("pgDocTrasportoRientroRif")));
                rDett.setCdTerzoAssegnatario(toInt(map.get("cdTerzoAssegnatario")));

                dett = rDett;

            } else {

                DocumentoTrasportoDettBulk tDett = new DocumentoTrasportoDettBulk();

                InventarioDocTRBulk bene = new InventarioDocTRBulk();
                bene.setInventario(new Id_inventarioBulk());
                bene.setNr_inventario(nrInventario);
                bene.setPg_inventario(pgInventario);
                bene.setProgressivo((long) progSafe);

                tDett.setBene(bene);
                tDett.setCdTerzoAssegnatario(toInt(map.get("cdTerzoAssegnatario")));

                dett = tDett;
            }

            dett.setDoc_trasporto_rientro(bulk);
            result.add(dett);
        }

        return result;
    }


    /**
     * Valida i dati della testata del documento ricevuto via REST.
     * Ammesso solo lo stato INSERITO.
     */
    private void validaRichiestaHttp(Doc_trasporto_rientroBulk bulk, CNRUserContext ctx) {


        if (isNullOrEmpty(bulk.getUtenteRemotoRequest())) {
            throw new RestException(
                    Response.Status.BAD_REQUEST,
                    "Email obbligatoria dell'utente remoto che effettua la richiesta (utenteRemotoRequest)"
            );
        }

        if (isNullOrEmpty(bulk.getTiDocumento())) {
            throw new RestException(
                    Response.Status.BAD_REQUEST,
                    "tiDocumento obbligatorio (valori ammessi: 'T' per Trasporto, 'R' per Rientro)"
            );
        }

        if (!Doc_trasporto_rientroBulk.TRASPORTO.equals(bulk.getTiDocumento())
                && !Doc_trasporto_rientroBulk.RIENTRO.equals(bulk.getTiDocumento())) {
            throw new RestException(
                    Response.Status.BAD_REQUEST,
                    "tiDocumento non valido '" + bulk.getTiDocumento()
                            + "': deve essere 'T' (Trasporto) o 'R' (Rientro)"
            );
        }

        if (bulk.getEsercizio() == null) {
            throw new RestException(Response.Status.BAD_REQUEST, "esercizio obbligatorio");
        }

        if (!ctx.getEsercizio().equals(bulk.getEsercizio())) {
            throw new RestException(
                    Response.Status.BAD_REQUEST,
                    "esercizio nel body (" + bulk.getEsercizio()
                            + ") diverso dal contesto utente (" + ctx.getEsercizio() + ")"
            );
        }

        if (bulk.getPgInventario() == null) {
            throw new RestException(
                    Response.Status.BAD_REQUEST,
                    "pgInventario obbligatorio nella testata"
            );
        }

        if (isNullOrEmpty(bulk.getCdTipoTrasportoRientro())) {
            throw new RestException(
                    Response.Status.BAD_REQUEST,
                    "Attenzione: specificare un Tipo di Movimento (cdTipoTrasportoRientro obbligatorio)"
            );
        }

        if (isNullOrEmpty(bulk.getDsDocTrasportoRientro())) {
            throw new RestException(
                    Response.Status.BAD_REQUEST,
                    "Attenzione: indicare una Descrizione (dsDocTrasportoRientro obbligatorio)"
            );
        }

        if (!isNullOrEmpty(bulk.getStato()) && Doc_trasporto_rientroBulk.STATO.get(bulk.getStato()) == null) {
            throw new RestException(
                    Response.Status.BAD_REQUEST,
                    "stato non valido '" + bulk.getStato()
                            + "': valori ammessi: " + Doc_trasporto_rientroBulk.STATO.toString()
            );
        }

        if (Boolean.TRUE.equals(bulk.getFlIncaricato()) && bulk.getCdTerzoIncaricato() == null) {
            throw new RestException(
                    Response.Status.BAD_REQUEST,
                    "cdTerzoIncaricato obbligatorio quando flIncaricato=true"
            );
        }

        if (Boolean.TRUE.equals(bulk.getFlVettore()) && isNullOrEmpty(bulk.getNominativoVettore())) {
            throw new RestException(
                    Response.Status.BAD_REQUEST,
                    "nominativoVettore obbligatorio quando flVettore=true"
            );
        }

        if (bulk.isSmartworking()
                && (bulk.getTerzoSmartworking() == null || bulk.getTerzoSmartworking().getCd_terzo() == null)) {
            throw new RestException(
                    Response.Status.BAD_REQUEST,
                    "cdTerzoSmartworking obbligatorio quando il documento è di tipo Smartworking"
            );
        }

        if (Boolean.TRUE.equals(bulk.getFlIncaricato()) && Boolean.TRUE.equals(bulk.getFlVettore())) {
            throw new RestException(
                    Response.Status.BAD_REQUEST,
                    "flIncaricato e flVettore non possono essere entrambi true contemporaneamente"
            );
        }

        if (!Doc_trasporto_rientroBulk.STATO_INSERITO.equals(bulk.getStato())) {
            throw new RestException(
                    Response.Status.BAD_REQUEST,
                    "Operazione consentita solo per documenti in stato INSERITO. Stato attuale: "
                            + bulk.getStato()
            );
        }
    }

    /**
     * Valida la lista dei dettagli dei beni associati al documento.
     */
    private void validaDettagliHttp(Doc_trasporto_rientroBulk bulk,
                                    List<Doc_trasporto_rientro_dettBulk> dettagli) {

        if (dettagli == null || dettagli.isEmpty()) {
            throw new RestException(
                    Response.Status.BAD_REQUEST,
                    "Attenzione: è necessario specificare almeno un bene (dettagli obbligatori)"
            );
        }

        boolean isRientro = Doc_trasporto_rientroBulk.RIENTRO.equals(bulk.getTiDocumento());
        Set<String> chiaviBeniViste = new LinkedHashSet<>();

        for (int i = 0; i < dettagli.size(); i++) {

            Doc_trasporto_rientro_dettBulk d = dettagli.get(i);
            String pfx = "Dettaglio[" + i + "]: ";

            if (d.getPg_inventario() == null) {
                throw new RestException(Response.Status.BAD_REQUEST, pfx + "pgInventario mancante");
            }

            if (d.getNr_inventario() == null) {
                throw new RestException(Response.Status.BAD_REQUEST, pfx + "nrInventario mancante");
            }

            if (d.getProgressivo() == null) {
                throw new RestException(Response.Status.BAD_REQUEST, pfx + "progressivo mancante");
            }

            if (!bulk.getPgInventario().equals(d.getPg_inventario())) {
                throw new RestException(
                        Response.Status.BAD_REQUEST,
                        pfx + "pgInventario (" + d.getPg_inventario()
                                + ") non coincide con pgInventario della testata ("
                                + bulk.getPgInventario() + ")"
                );
            }

            String chiaveBene = d.getNr_inventario() + "_" + d.getProgressivo();

            if (!chiaviBeniViste.add(chiaveBene)) {
                throw new RestException(
                        Response.Status.BAD_REQUEST,
                        pfx + "bene duplicato nella stessa richiesta (nrInventario="
                                + d.getNr_inventario() + ", progressivo=" + d.getProgressivo() + ")"
                );
            }

            if (isRientro) {

                DocumentoRientroDettBulk rDett = (DocumentoRientroDettBulk) d;

                if (rDett.getPgDocTrasportoRientroRif() == null) {
                    throw new RestException(
                            Response.Status.BAD_REQUEST,
                            pfx + "pgDocTrasportoRientroRif obbligatorio per documenti di Rientro"
                    );
                }

                if (rDett.getNrInventarioRif() == null) {
                    throw new RestException(
                            Response.Status.BAD_REQUEST,
                            pfx + "nrInventarioRif obbligatorio per documenti di Rientro"
                    );
                }

                if (rDett.getProgressivoRif() == null) {
                    throw new RestException(
                            Response.Status.BAD_REQUEST,
                            pfx + "progressivoRif obbligatorio per documenti di Rientro"
                    );
                }

                if (rDett.getPgInventarioRif() == null) {
                    throw new RestException(
                            Response.Status.BAD_REQUEST,
                            pfx + "pgInventarioRif obbligatorio per documenti di Rientro"
                    );
                }

                if (!bulk.getPgInventario().equals(rDett.getPgInventarioRif())) {
                    throw new RestException(
                            Response.Status.BAD_REQUEST,
                            pfx + "pgInventarioRif (" + rDett.getPgInventarioRif()
                                    + ") deve coincidere con pgInventario della testata ("
                                    + bulk.getPgInventario() + ")"
                    );
                }

                if (!d.getNr_inventario().equals(rDett.getNrInventarioRif())) {
                    throw new RestException(
                            Response.Status.BAD_REQUEST,
                            pfx + "nrInventario (" + d.getNr_inventario()
                                    + ") deve coincidere con nrInventarioRif (" + rDett.getNrInventarioRif() + ")"
                    );
                }

                if (!d.getProgressivo().equals(rDett.getProgressivoRif())) {
                    throw new RestException(
                            Response.Status.BAD_REQUEST,
                            pfx + "progressivo (" + d.getProgressivo()
                                    + ") deve coincidere con progressivoRif (" + rDett.getProgressivoRif() + ")"
                    );
                }
            }
        }
    }

    /**
     * Verifica che non siano presenti allegati nel body della richiesta.
     * <p>
     * In fase di inserimento (stato INSERITO) non è consentito allegare documenti.
     * Gli allegati sono ammessi via REST solo per documenti in stato FIRMATO (statoFlusso=FIR),
     * tramite endpoint dedicato.
     */
    private void validaAssenzeAllegatiHttp(Map<String, Object> body) {

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rawAllegati =
                (List<Map<String, Object>>) body.get("attachments");

        if (rawAllegati != null && !rawAllegati.isEmpty()) {
            throw new RestException(
                    Response.Status.BAD_REQUEST,
                    "Non è consentito allegare documenti in fase di inserimento. " +
                            "Gli allegati possono essere aggiunti solo quando il documento " +
                            "è in stato FIRMATO (statoFlusso=FIR)"
            );
        }
    }

    private void validaParametriRicercaDocumento(
            String tiDocumento,
            String nrInventario) {

        if (isNullOrEmpty(nrInventario)) {
            throw new RestException(
                    Response.Status.BAD_REQUEST,
                    "nrInventario obbligatorio"
            );
        }

        long nrInventarioValue;

        try {
            nrInventarioValue = Long.parseLong(nrInventario);
        } catch (NumberFormatException e) {
            throw new RestException(
                    Response.Status.BAD_REQUEST,
                    "nrInventario deve essere numerico"
            );
        }

        if (nrInventarioValue <= 0L) {
            throw new RestException(
                    Response.Status.BAD_REQUEST,
                    "nrInventario deve essere un numero positivo maggiore di zero"
            );
        }

        if (isNullOrEmpty(tiDocumento)) {
            throw new RestException(
                    Response.Status.BAD_REQUEST,
                    "tiDocumento obbligatorio (valori ammessi: T o R)"
            );
        }

        if (!Doc_trasporto_rientroBulk.TRASPORTO.equals(tiDocumento)
                && !Doc_trasporto_rientroBulk.RIENTRO.equals(tiDocumento)) {
            throw new RestException(
                    Response.Status.BAD_REQUEST,
                    "tiDocumento non valido '" + tiDocumento
                            + "': valori ammessi T (Trasporto) o R (Rientro)"
            );
        }

    }

    // =========================================================================
    // UTILITY
    // =========================================================================

    /**
     * Converte oggetto generico in Long.
     */
    private static Long toLong(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).longValue();
        try {
            return Long.parseLong(o.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Converte oggetto generico in Integer.
     */
    private static Integer toInt(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).intValue();
        try {
            return Integer.parseInt(o.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String extractErrorMessage(Throwable t, String defaultMessage) {
        Throwable current = t;
        String message = defaultMessage;

        while (current != null) {
            if (current.getMessage() != null && !current.getMessage().isBlank()) {
                message = current.getMessage();
            }
            current = current.getCause();
        }

        return message;
    }

    private Response buildErrorResponse(Response.Status status, Throwable t, String defaultMessage) {
        return Response.status(status)
                .entity(DocTRWSResponse.messageOnly(false, extractErrorMessage(t, defaultMessage)))
                .build();
    }
}
package it.cnr.contab.web.rest.resource.inventario01;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.cnr.contab.inventario00.docs.bulk.InventarioDocTRBulk;
import it.cnr.contab.inventario00.tabrif.bulk.Id_inventarioBulk;
import it.cnr.contab.inventario01.bulk.*;
import it.cnr.contab.inventario01.ejb.DocTrasportoRientroComponentSession;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.util00.bulk.storage.AllegatoGenericoBulk;
import it.cnr.contab.web.rest.exception.RestException;
import it.cnr.contab.web.rest.local.inventario01.DocTrasportoRientroLocal;
import it.cnr.contab.web.rest.model.AttachmentDocTrasportoRientro;
import it.cnr.contab.web.rest.model.DocTRWSResponse;
import it.cnr.jada.bulk.BulkList;
import it.cnr.jada.bulk.SimpleBulkList;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

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

    // =========================================================================
    // SALVA DOCUMENTO (stato INS, senza allegati)
    // =========================================================================

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
    public Response saveDocTR(@Context HttpServletRequest request, Map<String, Object> body) throws Exception {

        CNRUserContext ctx = (CNRUserContext) securityContext.getUserPrincipal();
        Doc_trasporto_rientroBulk bulk = deserializzaBulk(body);
        validaRichiestaHttp(bulk, ctx);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rawDettagli = (List<Map<String, Object>>) body.get("dettagli");
        List<Doc_trasporto_rientro_dettBulk> dettagli = deserializzaDettagli(bulk, rawDettagli);

        validaDettagliHttp(bulk, dettagli);
        validaAssenzeAllegatiHttp(body);

        bulk.setDoc_trasporto_rientro_dettColl(new SimpleBulkList<>(dettagli));

        try {
            Doc_trasporto_rientroBulk docSalvato = componenteDocTR.saveDocFromWS(ctx, bulk);

            String message = Doc_trasporto_rientroBulk.TRASPORTO.equals(docSalvato.getTiDocumento())
                    ? "Documento di Trasporto salvato con successo"
                    : "Documento di Rientro salvato con successo";

            return Response.status(Response.Status.CREATED)
                    .entity(DocTRWSResponse.of(true, message, docSalvato))
                    .build();

        } catch (Exception e) {
            log.error("Errore durante saveDocTR", e);
            return buildErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, e, "Documento non salvato");
        }
    }

    // =========================================================================
    // RICERCA DOCUMENTO
    // =========================================================================

    /**
     * Ricerca documento di Trasporto o Rientro associato ad un bene inventariale.
     *
     * @param request     richiesta HTTP
     * @param tiDocumento tipo documento (T/R)
     * @param stato       stato documento
     * @param esercizio   esercizio contabile
     * @param nrInventario numero inventario del bene
     * @return documento trovato
     * @throws Exception errore durante la ricerca
     */
    @Override
    public Response cercaDocTrasportoRientro(@Context HttpServletRequest request,
                                             String tiDocumento,
                                             String stato,
                                             Integer esercizio,
                                             Long nrInventario) throws Exception {

        CNRUserContext ctx = (CNRUserContext) securityContext.getUserPrincipal();

        if (nrInventario == null) {
            throw new RestException(Response.Status.BAD_REQUEST, "nrInventario obbligatorio");
        }

        Integer esercizioEffettivo = esercizio != null ? esercizio : ctx.getEsercizio();

        try {
            Doc_trasporto_rientroBulk trovato = componenteDocTR.cercaDocumentoPerBene(
                    ctx,
                    tiDocumento,
                    stato,
                    nrInventario,
                    esercizioEffettivo
            );

            if (trovato == null) {
                return Response.ok(
                        DocTRWSResponse.messageOnly(false, "Documento non trovato")
                ).build();
            }

            String message = Doc_trasporto_rientroBulk.TRASPORTO.equals(trovato.getTiDocumento())
                    ? "Documento di Trasporto trovato"
                    : "Documento di Rientro trovato";

            return Response.ok(
                    DocTRWSResponse.of(true, message, trovato)
            ).build();

        } catch (Exception e) {
            log.error("Errore durante cercaDocTrasportoRientro", e);
            return buildErrorResponse(Response.Status.INTERNAL_SERVER_ERROR, e, "Errore durante la ricerca del documento");
        }
    }

    // =========================================================================
    // DESERIALIZZAZIONE
    // =========================================================================

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

            Long pgInventario  = toLong(map.get("pgInventario"));
            Long nrInventario  = toLong(map.get("nrInventario"));
            Integer progressivo = toInt(map.get("progressivo"));
            int progSafe        = progressivo != null ? progressivo : 0;

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
     * Deserializza gli allegati presenti nel body della richiesta.
     * Usato solo per la gestione allegati su documenti in stato FIRMATO.
     */
    @SuppressWarnings("unchecked")
    private List<AttachmentDocTrasportoRientro> deserializzaAllegati(Map<String, Object> body) {

        List<Map<String, Object>> rawAllegati =
                (List<Map<String, Object>>) body.get("attachments");

        if (rawAllegati == null || rawAllegati.isEmpty()) {
            return Collections.emptyList();
        }

        List<AttachmentDocTrasportoRientro> allegati = new ArrayList<>();

        for (int i = 0; i < rawAllegati.size(); i++) {
            try {
                allegati.add(MAPPER.convertValue(rawAllegati.get(i), AttachmentDocTrasportoRientro.class));
            } catch (IllegalArgumentException e) {
                throw new RestException(
                        Response.Status.BAD_REQUEST,
                        "Allegato[" + i + "] non valido: " + e.getMessage()
                );
            }
        }

        return allegati;
    }

    // =========================================================================
    // VALIDAZIONE
    // =========================================================================

    /**
     * Valida i dati della testata del documento ricevuto via REST.
     * Ammesso solo lo stato INSERITO.
     */
    private void validaRichiestaHttp(Doc_trasporto_rientroBulk bulk, CNRUserContext ctx) {

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

//    /**
//     * Valida gli allegati per documenti in stato FIRMATO.
//     * <p>
//     * Regole:
//     * - nomeFile obbligatorio
//     * - bytes obbligatori e di dimensione minima
//     * - typeAttachment obbligatorio e valido per il tipo documento (T/R)
//     * - descrizione obbligatoria
//     * - nessun nomeFile duplicato
//     * - al massimo un allegato FIRMATO
//     *
//     * @param bulk     testata del documento
//     * @param allegati lista allegati da validare
//     */
//    private void validaAllegatiPerDocumentoFirmato(Doc_trasporto_rientroBulk bulk,
//                                                   List<AttachmentDocTrasportoRientro> allegati) {
//
//        String aspectFirmato = bulk.isTrasporto()
//                ? AllegatoDocumentoTrasportoBulk.P_SIGLA_DOCTRASPORTO_ATTACHMENT_FIRMATO
//                : AllegatoDocumentoRientroBulk.P_SIGLA_DOCRIENTRO_ATTACHMENT_FIRMATO;
//
//        String aspectAltro = bulk.isTrasporto()
//                ? AllegatoDocumentoTrasportoBulk.P_SIGLA_DOCTRASPORTO_ATTACHMENT_ALTRO
//                : AllegatoDocumentoRientroBulk.P_SIGLA_DOCRIENTRO_ATTACHMENT_ALTRO;
//
//        Set<String> aspectValidi = new HashSet<>();
//        aspectValidi.add(aspectFirmato);
//        aspectValidi.add(aspectAltro);
//
//        Set<String> nomiVisti = new HashSet<>();
//        int countFirmato = 0;
//
//        if (allegati == null || allegati.isEmpty()) {
//            throw new RestException(
//                    Response.Status.BAD_REQUEST,
//                    "Per un documento in stato FIRMATO è obbligatorio fornire almeno un allegato."
//            );
//        }
//
//        for (int i = 0; i < allegati.size(); i++) {
//
//            AttachmentDocTrasportoRientro att = allegati.get(i);
//            String pfx = "Allegato[" + i + "]: ";
//            String nomeFile = att.getNomeFile();
//
//            if (isNullOrEmpty(nomeFile)) {
//                throw new RestException(Response.Status.BAD_REQUEST, pfx + "nomeFile mancante");
//            }
//
//            if (att.getBytes() == null) {
//                throw new RestException(
//                        Response.Status.BAD_REQUEST,
//                        nomeFile + ": file non fornito"
//                );
//            }
//
//            if (att.getBytes().length < MIN_PDF_FILE_SIZE_BYTES) {
//                throw new RestException(
//                        Response.Status.BAD_REQUEST,
//                        nomeFile + ": file vuoto o non accessibile (dimensione: "
//                                + att.getBytes().length + " bytes)"
//                );
//            }
//
//            if (isNullOrEmpty(att.getTypeAttachment())) {
//                throw new RestException(
//                        Response.Status.BAD_REQUEST,
//                        nomeFile + ": typeAttachment mancante"
//                );
//            }
//
//            if (!aspectValidi.contains(att.getTypeAttachment())) {
//                throw new RestException(
//                        Response.Status.BAD_REQUEST,
//                        nomeFile + ": typeAttachment non valido per il tipo documento"
//                );
//            }
//
//            if (isNullOrEmpty(att.getDescrizione())) {
//                throw new RestException(
//                        Response.Status.BAD_REQUEST,
//                        nomeFile + ": descrizione mancante"
//                );
//            }
//
//            if (!nomiVisti.add(nomeFile.toLowerCase())) {
//                throw new RestException(
//                        Response.Status.BAD_REQUEST,
//                        nomeFile + ": nomeFile duplicato nella richiesta"
//                );
//            }
//
//            if (aspectFirmato.equals(att.getTypeAttachment())) {
//                countFirmato++;
//            }
//        }
//
//        if (countFirmato > 1) {
//            throw new RestException(
//                    Response.Status.BAD_REQUEST,
//                    "È consentito allegare al massimo un documento FIRMATO (trovati: " + countFirmato + ")"
//            );
//        }
//    }
//
//    // =========================================================================
//    // ARCHIVIAZIONE ALLEGATI (solo per documenti in stato FIRMATO)
//    // =========================================================================
//
//    /**
//     * Archivia gli allegati del documento nel repository documentale.
//     * Utilizzato solo per documenti in stato FIRMATO (statoFlusso=FIR).
//     *
//     * @param ctx        contesto utente
//     * @param docSalvato documento già persistito
//     * @param allegati   lista allegati da archiviare
//     */
//    private void archiviaAllegati(CNRUserContext ctx,
//                                  Doc_trasporto_rientroBulk docSalvato,
//                                  List<AttachmentDocTrasportoRientro> allegati) {
//
//        List<File> tmpFiles = new ArrayList<>();
//
//        try {
//            BulkList<AllegatoGenericoBulk> archivio = new BulkList<>();
//
//            for (AttachmentDocTrasportoRientro att : allegati) {
//                File tmp = creaFileTemporaneo(att.getNomeFile(), att.getBytes());
//                tmpFiles.add(tmp);
//
//                AllegatoDocTraspRientroBulk a = costruisciAllegato(att, docSalvato, tmp);
//                a.setToBeCreated();
//                archivio.add(a);
//            }
//
//            docSalvato.setArchivioAllegati(archivio);
//
//            try {
//                componenteDocTR.archiviaAllegatiDocTR(ctx, docSalvato);
//            } catch (Exception e) {
//                log.error("Errore durante archiviaAllegatiDocTR", e);
//                throw new RestException(
//                        Response.Status.INTERNAL_SERVER_ERROR,
//                        "Documento salvato ma errore nell'archiviazione allegati: " + e.getMessage()
//                );
//            }
//
//        } finally {
//            for (File f : tmpFiles) {
//                try {
//                    Files.deleteIfExists(f.toPath());
//                } catch (IOException ignored) {
//                }
//            }
//        }
//    }

    /**
     * Costruisce l'oggetto Bulk rappresentante un allegato del documento.
     */
    private AllegatoDocTraspRientroBulk costruisciAllegato(AttachmentDocTrasportoRientro att,
                                                           Doc_trasporto_rientroBulk doc,
                                                           File tmp) {

        boolean isTrasporto = doc instanceof DocumentoTrasportoBulk;

        AllegatoDocTraspRientroBulk a = isTrasporto
                ? new AllegatoDocumentoTrasportoBulk()
                : new AllegatoDocumentoRientroBulk();

        a.setAspectName(att.getTypeAttachment());
        a.setNome(att.getNomeFile());
        a.setTitolo(att.getNomeFile());
        a.setDescrizione(!isNullOrEmpty(att.getDescrizione())
                ? att.getDescrizione()
                : att.getNomeFile());

        if (att.getMimeTypes() != null) {
            a.setContentType(att.getMimeTypes().mimetype());
        }

        a.setFile(tmp);

        return a;
    }

    /**
     * Crea un file temporaneo a partire dai byte di un allegato.
     */
    private File creaFileTemporaneo(String nome, byte[] bytes) {

        try {
            String pref = nome.contains(".") ? nome.substring(0, nome.lastIndexOf(".")) : nome;
            String suff = nome.contains(".") ? nome.substring(nome.lastIndexOf(".")) : ".tmp";

            if (pref.length() < 3) {
                pref += "___";
            }

            File tmp = File.createTempFile(pref, suff);
            tmp.deleteOnExit();

            try (FileOutputStream fos = new FileOutputStream(tmp)) {
                fos.write(bytes);
            }

            return tmp;

        } catch (IOException e) {
            throw new RestException(
                    Response.Status.INTERNAL_SERVER_ERROR,
                    "Errore creazione file temporaneo per: " + nome
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
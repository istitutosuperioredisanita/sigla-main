package it.cnr.test.contab.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.cnr.test.h2.utenze.action.ActionDeployments;
import org.junit.jupiter.api.*;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RestServiceDocTRTest  extends ActionDeployments {

    private static final String ENDPOINT  = "/restapi/docTrasportoRientro";
    private static final String CD_CDS    = "000";
    private static final String CD_UO     = "000.000";
    private static final int    ESERCIZIO = 2026;

    /** 19/02/2025 00:00:00 UTC */
    private static final long DATE_INS = 1739919600000L;
    /** 02/03/2025 00:00:00 UTC */
    private static final long DATE_SW  = 1740873600000L;

    private static final String PDF_BYTES = "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YW5zcGFyZW5jeS9DUy9EZXZpY2VSR0IvSSB0cnVlPj4vQ29udGVudHMgMiAwIFI+PgplbmRvYmoKCjQgMCBvYmoKPDwvVHlwZS9QYWdlcwovUmVzb3VyY2VzIDExIDAgUgovTWVkaWFCb3hbIDAgMCA1OTUgODQyIF0KL0tpZHNbIDEgMCBSIF0KL0NvdW50IDE+PgplbmRvYmoKCjEyIDAgb2JqCjw8L1R5cGUvQ2F0YWxvZy9QYWdlcyA0IDAgUgovT3BlbkFjdGlvblsxIDAgUiAvWFlaIG51bGwgbnVsbCAwXQovTGFuZyhlbi1VUykKPj4KZW5kb2JqCgoxMyAwIG9iago8PC9DcmVhdG9yPEZFRkYwMDU3MDA3MjAwNjkwMDc0MDA2NTAwNzI+Ci9Qcm9kdWNlcjxGRUZGMDA0QzAwNjkwMDYyMDA3MjAwNjUwMDRGMDA2NjAwNjYwMDY5MDA2MzAwNjUwMDIwMDAzNTAwMkUwMDMzPgovQ3JlYXRpb25EYXRlKEQ6MjAxNzA3MTcxMTEwMDcrMDInMDAnKT4+CmVuZG9iagoKeHJlZgowIDE0CjAwMDAwMDAwMDAgNjU1MzUgZiAKMDAwMDAwNjIxNSAwMDAwMCBuIAowMDAwMDAwMDE5IDAwMDAwIG4gCjAwMDAwMDAyMDkgMDAwMDAgbiAKMDAwMDAwNjM1OCAwMDAwMCBuIAowMDAwMDAwMjI5IDAwMDAwIG4gCjAwMDAwMDUzOTMgMDAwMDAgbiAKMDAwMDAwNTQxNCAwMDAwMCBuIAowMDAwMDA1NjA5IDAwMDAwIG4gCjAwMDAwMDU5MzcgMDAwMDAgbiAKMDAwMDAwNjEyOCAwMDAwMCBuIAowMDAwMDA2MTYwIDAwMDAwIG4gCjAwMDAwMDY0NTcgMDAwMDAgbiAKMDAwMDAwNjU1NCAwMDAwMCBuIAp0cmFpbGVyCjw8L1NpemUgMTQvUm9vdCAxMiAwIFIKL0luZm8gMTMgMCBSCi9JRCBbIDxFMzNCMjk4QTdBMUM2NDc1ODU1NDMyODQyMzEwNUM5Rj4KPEUzM0IyOThBN0ExQzY0NzU4NTU0MzI4NDIzMTA1QzlGPiBdCi9Eb2NDaGVja3N1bSAvNTY2MzRFRTYzQUIxNDcxRTBENUMzRThFODBDRTVGRUYKPj4Kc3RhcnR4cmVmCjY3MjkKJSVFT0YK";

    private static final String TYPE_TRASPORTO_ALTRO   = "P:sigla_doctrasporto_attachment:altro";
    private static final String TYPE_TRASPORTO_FIRMATO = "P:sigla_doctrasporto_attachment:firmato";
    private static final String TYPE_RIENTRO_ALTRO     = "P:sigla_doctrientro_attachment:altro";
    private static final String TYPE_DOCTRASPORTO_ATTACHMENT_FIRMATO = "P:sigla_doctrasporto_attachment:doctrasporto_firmato";

    private final ObjectMapper mapper = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

    // -------------------------------------------------------------------------
    // test01 – Trasporto INS vettore
    // -------------------------------------------------------------------------
    @Test
    @Disabled
    @Order(1)
    public void test01_trasportoInsVettore() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("pgInventario",          1);
        body.put("tiDocumento",           "T");
        body.put("stato",                 "INS");
        body.put("esercizio",             ESERCIZIO);
        body.put("dsDocTrasportoRientro", "Trasporto INS Vettore");
        body.put("cdTipoTrasportoRientro","1");
        body.put("dataRegistrazione",     DATE_INS);
        body.put("destinazione",          "Destinazione INS Vettore");
        body.put("indirizzo",             "Indirizzo INS Vettore");
        body.put("flVettore",             true);
        body.put("flIncaricato",          false);
        body.put("nominativoVettore",     "Vettore Trasporto INS");
        body.put("cdTerzoResponsabile",   100);
        body.put("dettagli", Arrays.asList(
                dettaglio(1, 1321L, 0, 101),
                dettaglio(1, 1322L, 0, 101),
                dettaglio(1, 1320L, 0, 101)
        ));
        body.put("attachments", Arrays.asList(
                allegato("trasporto_ins_vettore_1.pdf", TYPE_TRASPORTO_ALTRO),
                allegato("trasporto_ins_vettore_2.pdf", TYPE_TRASPORTO_ALTRO)
        ));

        int status = post(body);
        Assertions.assertEquals(201, status);
    }

    // -------------------------------------------------------------------------
    // test02 – Trasporto INS incaricato
    // -------------------------------------------------------------------------
    @Test
    @Disabled
    @Order(2)
    public void test02_trasportoInsIncaricato() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("pgInventario",          1);
        body.put("tiDocumento",           "T");
        body.put("stato",                 "INS");
        body.put("esercizio",             ESERCIZIO);
        body.put("dsDocTrasportoRientro", "Trasporto INS Incaricato");
        body.put("cdTipoTrasportoRientro","1");
        body.put("dataRegistrazione",     DATE_INS);
        body.put("destinazione",          "Destinazione INS Incaricato");
        body.put("indirizzo",             "Indirizzo INS Incaricato");
        body.put("flIncaricato",          true);
        body.put("flVettore",             false);
        body.put("cdTerzoIncaricato",     100);
        body.put("cdTerzoResponsabile",   101);
        body.put("dettagli", Arrays.asList(
                dettaglio(1, 1323L, 0, 101),
                dettaglio(1, 1324L, 0, 101),
                dettaglio(1, 1325L, 0, 101)
        ));
        body.put("attachments", Arrays.asList(
                allegato("trasporto_ins_incaricato_1.pdf", TYPE_TRASPORTO_ALTRO),
                allegato("trasporto_ins_incaricato_2.pdf", TYPE_TRASPORTO_ALTRO)
        ));

        int status = post(body);
        Assertions.assertEquals( 201, status);
    }

    // -------------------------------------------------------------------------
    // test03 – Trasporto INS smartworking OK
    // -------------------------------------------------------------------------
    @Test
    @Disabled
    @Order(3)
    public void test03_trasportoInsSmartworkingOk() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("pgInventario",          1);
        body.put("tiDocumento",           "T");
        body.put("stato",                 "INS");
        body.put("esercizio",             ESERCIZIO);
        body.put("dsDocTrasportoRientro", "SW OK");
        body.put("cdTipoTrasportoRientro","9");
        body.put("dataRegistrazione",     DATE_SW);
        body.put("destinazione",          "Sede ISS");
        body.put("indirizzo",             "Roma");
        //body.put("cdAnagSmartworking",    "4924");
        body.put("cdTerzoResponsabile",   100);
        body.put("dettagli", Arrays.asList(
                dettaglio(1, 1326L, 0, 101),
                dettaglio(1, 1327L, 0, 101)
        ));
        body.put("attachments", Arrays.asList(
                allegato("sw_1.pdf", TYPE_TRASPORTO_ALTRO),
                allegato("sw_2.pdf", TYPE_TRASPORTO_ALTRO)
        ));

        int status = post(body);
        Assertions.assertEquals( 201, status);
    }

    // -------------------------------------------------------------------------
    // test04 – Trasporto INS smartworking ERROR (inventari inesistenti)
    // -------------------------------------------------------------------------
    @Test
    @Disabled
    @Order(4)
    public void test04_trasportoInsSmartworkingError() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("pgInventario",          1);
        body.put("tiDocumento",           "T");
        body.put("stato",                 "INS");
        body.put("esercizio",             ESERCIZIO);
        body.put("dsDocTrasportoRientro", "SW ERROR");
        body.put("cdTipoTrasportoRientro","9");
        body.put("dataRegistrazione",     DATE_SW);
        body.put("destinazione",          "Sede ISS");
        body.put("indirizzo",             "Roma");
        body.put("cdAnagSmartworking",    "4924");
        body.put("cdTerzoResponsabile",   100);
        body.put("dettagli", Arrays.asList(
                dettaglio(1, 1320L, 0, 101),
                dettaglio(1, 1321L, 0, 101)
        ));
        body.put("attachments", Arrays.asList(
                allegato("sw_err_1.pdf", TYPE_TRASPORTO_ALTRO),
                allegato("sw_err_2.pdf", TYPE_TRASPORTO_ALTRO)
        ));

        int status = post(body);
        Assertions.assertTrue( status >= 400 && status < 500);

    }

    // -------------------------------------------------------------------------
    // test05 – Trasporto DEF
    // -------------------------------------------------------------------------
    @Test
    @Disabled
    @Order(5)
    public void test05_trasportoDef() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("pgInventario",          1);
        body.put("tiDocumento",           "T");
        body.put("stato",                 "INS");
        body.put("esercizio",             ESERCIZIO);
        body.put("dsDocTrasportoRientro", "Trasporto DEF");
        body.put("cdTipoTrasportoRientro","1");
        body.put("dataRegistrazione",     DATE_INS);
        body.put("destinazione",          "Destinazione DEF");
        body.put("indirizzo",             "Indirizzo DEF");
        body.put("flVettore",             true);
        body.put("flIncaricato",          false);
        body.put("nominativoVettore",     "Vettore Trasporto DEF");
        body.put("cdTerzoResponsabile",   100);
        body.put("dettagli", Arrays.asList(
                dettaglio(1, 1328L, 0, 101),
                dettaglio(1, 1329L, 0, 101)
        ));
        body.put("attachments", Arrays.asList(
                allegato("trasporto_def_altro.pdf",   TYPE_TRASPORTO_ALTRO),
                allegato("trasporto_def_firmato.pdf", TYPE_DOCTRASPORTO_ATTACHMENT_FIRMATO)
        ));

        int status = post(body);
        Assertions.assertEquals( 201, status);
    }

    // -------------------------------------------------------------------------
    // test06 – Rientro INS (riferimento al Trasporto DEF di test05)
    // -------------------------------------------------------------------------
    @Test
    @Disabled
    @Order(6)
    public void test06_rientroIns() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("pgInventario",          1);
        body.put("tiDocumento",           "R");
        body.put("stato",                 "INS");
        body.put("esercizio",             ESERCIZIO);
        body.put("dsDocTrasportoRientro", "Rientro INS");
        body.put("cdTipoTrasportoRientro","2");
        body.put("dataRegistrazione",     DATE_INS);
        body.put("destinazione",          "Destinazione Rientro INS");
        body.put("indirizzo",             "Indirizzo Rientro INS");
        body.put("flIncaricato",          false);
        body.put("flVettore",             true);
        body.put("nominativoVettore",     "Vettore Rientro INS");
        body.put("cdTerzoResponsabile",   100);
        body.put("dettagli", Arrays.asList(
                dettaglioRientro(1, 1328L, 0, 101, "T", ESERCIZIO, 1, 3, 1328L, 0),
                dettaglioRientro(1, 1329L, 0, 101, "T", ESERCIZIO, 1, 3, 1329L, 0)
        ));
        body.put("attachments", Arrays.asList(
                allegato("rientro_ins_1.pdf", TYPE_RIENTRO_ALTRO),
                allegato("rientro_ins_2.pdf", TYPE_RIENTRO_ALTRO)
        ));

        int status = post(body);
        Assertions.assertEquals( 201, status);
    }

    // =========================================================================
    // Helper methods
    // =========================================================================

    /**
     * Costruisce un dettaglio senza riferimento (Trasporto).
     */
    private Map<String, Object> dettaglio(int pgInventario, long nrInventario,
                                          int progressivo, int cdTerzoAssegnatario) {
        Map<String, Object> d = new LinkedHashMap<>();
        d.put("pgInventario",       pgInventario);
        d.put("nrInventario",       nrInventario);
        d.put("progressivo",        progressivo);
        d.put("cdTerzoAssegnatario",cdTerzoAssegnatario);
        return d;
    }

    /**
     * Costruisce un dettaglio con riferimento al documento di trasporto (Rientro).
     */
    private Map<String, Object> dettaglioRientro(int pgInventario, long nrInventario,
                                                 int progressivo, int cdTerzoAssegnatario,
                                                 String tiDocumentoRif, int esercizioRif,
                                                 int pgInventarioRif, int pgDocTrasportoRientroRif,
                                                 long nrInventarioRif, int progressivoRif) {
        Map<String, Object> d = dettaglio(pgInventario, nrInventario, progressivo, cdTerzoAssegnatario);
        d.put("tiDocumentoRif",            tiDocumentoRif);
        d.put("esercizioRif",              esercizioRif);
        d.put("pgInventarioRif",           pgInventarioRif);
        d.put("pgDocTrasportoRientroRif",  pgDocTrasportoRientroRif);
        d.put("nrInventarioRif",           nrInventarioRif);
        d.put("progressivoRif",            progressivoRif);
        return d;
    }

    /**
     * Costruisce un allegato con il PDF minimo condiviso.
     */
    private Map<String, Object> allegato(String nomeFile, String typeAttachment) {
        Map<String, Object> a = new LinkedHashMap<>();
        a.put("nomeFile",        nomeFile);
        a.put("mimeTypes",       "PDF");
        a.put("typeAttachment",  typeAttachment);
        a.put("bytes",           PDF_BYTES);
        a.put("descrizione",     "TEST");
        return a;
    }

    // =========================================================================
    // HTTP helper
    // =========================================================================

    /**
     * Esegue una POST verso {@code ENDPOINT} con Basic Auth e restituisce l'HTTP status code.
     */
    private int post(Map<String, Object> body) throws Exception {
        String json = mapper.writeValueAsString(body);

        URL url = new URL(deploymentURL.toString().concat( ENDPOINT)
                .concat("?cdCds=" ).concat(CD_CDS)
                .concat("&cdUo=" ).concat(CD_UO)
                .concat("&esercizio=").concat(String.valueOf(ESERCIZIO)));

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Accept",       "application/json");

        // Basic Auth: ente / 2023enteiss
        String credentials = Base64.getEncoder()
                .encodeToString("ENTETEST:PASSTEST".getBytes(StandardCharsets.UTF_8));
        conn.setRequestProperty("Authorization", "Basic " + credentials);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }

        return conn.getResponseCode();
    }
}

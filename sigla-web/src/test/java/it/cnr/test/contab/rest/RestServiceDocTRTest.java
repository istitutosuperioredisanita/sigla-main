package it.cnr.test.contab.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;




public class RestServiceDocTRTest {

    private static final String BASE_URL  = "http://localhost:8080";
    private static final String ENDPOINT  = "/SIGLA/restapi/docTrasportoRientro";
    private static final String CD_CDS    = "000";
    private static final String CD_UO     = "000.000";
    private static final int    ESERCIZIO = 2026;

    /** 19/02/2025 00:00:00 UTC */
    private static final long DATE_INS = 1739919600000L;
    /** 02/03/2025 00:00:00 UTC */
    private static final long DATE_SW  = 1740873600000L;

    private static final String PDF_BYTES =
            "JVBERi0xLjAKMSAwIG9iago8PCAvVHlwZSAvQ2F0YWxvZyAvUGFnZXMgMiAwIFIgPj4KZW5kb2JqCjIgMCBvYmoK"
                    + "PDwgL1R5cGUgL1BhZ2VzIC9LaWRzIFszIDAgUl0gL0NvdW50IDEgPj4KZW5kb2JqCjMgMCBvYmoKPDwgL1R5cGUg"
                    + "L1BhZ2UgL1BhcmVudCAyIDAgUiAvTWVkaWFCb3ggWzAgMCA2MTIgNzkyXSA+PgplbmRvYmoKeHJlZgowIDQKMDAw"
                    + "MDAwMDAwMCA2NTUzNSBmCjAwMDAwMDAwMDkgMDAwMDAgbgowMDAwMDAwMDU4IDAwMDAwIG4KMDAwMDAwMDExNSAw"
                    + "MDAwMCBuCnRyYWlsZXIKPDwgL1NpemUgNCAvUm9vdCAxIDAgUiA+PgpzdGFydHhyZWYKMTk0CiUlRU9G";

    private static final String TYPE_TRASPORTO_ALTRO   = "P:sigla_doctrasporto_attachment:altro";
    private static final String TYPE_TRASPORTO_FIRMATO = "P:sigla_doctrasporto_attachment:firmato";
    private static final String TYPE_RIENTRO_ALTRO     = "P:sigla_doctrientro_attachment:altro";

    private final ObjectMapper mapper = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

    // -------------------------------------------------------------------------
    // test01 – Trasporto INS vettore
    // -------------------------------------------------------------------------
    @Test
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
        body.put("cdTerzoResponsabile",   3942);
        body.put("dettagli", Arrays.asList(
                dettaglio(1, 1321L, 0, 4008),
                dettaglio(1, 1322L, 0, 4008),
                dettaglio(1, 1320L, 0, 4008)
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
        body.put("cdTerzoIncaricato",     5969);
        body.put("cdTerzoResponsabile",   3942);
        body.put("dettagli", Arrays.asList(
                dettaglio(1, 1324L, 0, 4008),
                dettaglio(1, 1325L, 0, 4008),
                dettaglio(1, 1326L, 0, 4008)
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
        body.put("cdAnagSmartworking",    "4924");
        body.put("cdTerzoResponsabile",   3942);
        body.put("dettagli", Arrays.asList(
                dettaglio(1, 3824L, 0, 5091),
                dettaglio(1, 3689L, 0, 5091)
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
        body.put("cdTerzoResponsabile",   3942);
        body.put("dettagli", Arrays.asList(
                dettaglio(1, 9999L, 0, 5091),
                dettaglio(1, 9998L, 0, 5091)
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
    public void test05_trasportoDef() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("pgInventario",          1);
        body.put("tiDocumento",           "T");
        body.put("stato",                 "DEF");
        body.put("esercizio",             ESERCIZIO);
        body.put("dsDocTrasportoRientro", "Trasporto DEF");
        body.put("cdTipoTrasportoRientro","1");
        body.put("dataRegistrazione",     DATE_INS);
        body.put("destinazione",          "Destinazione DEF");
        body.put("indirizzo",             "Indirizzo DEF");
        body.put("flVettore",             true);
        body.put("flIncaricato",          false);
        body.put("nominativoVettore",     "Vettore Trasporto DEF");
        body.put("cdTerzoResponsabile",   3942);
        body.put("dettagli", Arrays.asList(
                dettaglio(1, 1637L, 0, 4008),
                dettaglio(1, 1638L, 0, 4008)
        ));
        body.put("attachments", Arrays.asList(
                allegato("trasporto_def_altro.pdf",   TYPE_TRASPORTO_ALTRO),
                allegato("trasporto_def_firmato.pdf", TYPE_TRASPORTO_FIRMATO)
        ));

        int status = post(body);
        Assertions.assertEquals( 201, status);
    }

    // -------------------------------------------------------------------------
    // test06 – Rientro INS (riferimento al Trasporto DEF di test05)
    // -------------------------------------------------------------------------
    @Test
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
        body.put("cdTerzoResponsabile",   3942);
        body.put("dettagli", Arrays.asList(
                dettaglioRientro(1, 1637L, 0, 4008, "T", ESERCIZIO, 1, 3, 1637L, 0),
                dettaglioRientro(1, 1638L, 0, 4008, "T", ESERCIZIO, 1, 3, 1638L, 0)
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

        URL url = new URL(BASE_URL + ENDPOINT
                + "?cdCds=" + CD_CDS
                + "&cdUo="  + CD_UO
                + "&esercizio=" + ESERCIZIO);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        conn.setRequestProperty("Accept",       "application/json");

        // Basic Auth: ente / 2023enteiss
        String credentials = Base64.getEncoder()
                .encodeToString("ente:2023enteiss".getBytes(StandardCharsets.UTF_8));
        conn.setRequestProperty("Authorization", "Basic " + credentials);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }

        return conn.getResponseCode();
    }
}
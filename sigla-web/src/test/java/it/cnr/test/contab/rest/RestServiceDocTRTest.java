package it.cnr.test.contab.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import it.cnr.contab.inventario01.bulk.AllegatoDocumentoRientroBulk;
import it.cnr.contab.inventario01.bulk.AllegatoDocumentoTrasportoBulk;
import it.cnr.contab.web.rest.config.SIGLASecurityContext;
import it.cnr.contab.web.rest.model.AttachmentDocTrasportoRientro;
import it.cnr.contab.web.rest.model.DocTrasportoRientroDTOBulk;
import it.cnr.contab.web.rest.model.DocTrasportoRientroDettDTOBulk;
import it.cnr.si.spring.storage.MimeTypes;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class RestServiceDocTRTest {

    private static final String BASE_URL  = "http://localhost:8080/SIGLA/restapi";
    private static final String USERNAME  = "ente";
    private static final String PASSWORD  = "2023enteiss";
    private static final String CD_CDS    = "000";
    private static final String CD_UO     = "000.000";
    private static final int    ESERCIZIO = 2026;
    private static final String ENDPOINT  = "docTrasportoRientro";

    private static Long pgTrasportoDefCreato = null;

    // =========================================================================
    // TEST A — TRASPORTO INS (Replica DB)
    // =========================================================================
    @Test
    public void A_testTrasportoIns() throws Exception {

        Timestamp dataReg = toTimestamp("20260219");

        DocTrasportoRientroDTOBulk dto = new DocTrasportoRientroDTOBulk();

        dto.setTiDocumento("T");
        dto.setStato("INS");
        dto.setEsercizio(ESERCIZIO);

        dto.setDsDocTrasportoRientro("fdsfsdf");
        dto.setCdTipoTrasportoRientro("1");
        dto.setDataRegistrazione(dataReg);

        dto.setDestinazione("dsf");
        dto.setIndirizzo("dsfd");

        dto.setFlIncaricato(Boolean.FALSE);
        dto.setFlVettore(Boolean.TRUE);
        dto.setNominativoVettore("sdfsdfdf");

        dto.setCdTerzoResponsabile(3942L);

        List<DocTrasportoRientroDettDTOBulk> dettagli = new ArrayList<>();
        dettagli.add(buildDettaglioTrasporto(1321L, 0, 4008L));
        dettagli.add(buildDettaglioTrasporto(1322L, 0, 4008L));
        dettagli.add(buildDettaglioTrasporto(1320L, 0, 4008L));

        dto.setDettagli(dettagli);
        dto.setAttachments(buildAllegatiTrasportoIns());

        eseguiChiamataRest(dto, "Trasporto INS (Replica DB)");
    }

    // =========================================================================
    // TEST B — TRASPORTO DEF (Documento Master)
    // =========================================================================
    @Test
    public void B_testTrasportoDef() throws Exception {

        Timestamp dataReg = toTimestamp("20260219");

        DocTrasportoRientroDTOBulk dto = new DocTrasportoRientroDTOBulk();

        dto.setTiDocumento("T");
        dto.setStato("DEF");
        dto.setEsercizio(ESERCIZIO);

        dto.setDsDocTrasportoRientro("qqqq");
        dto.setCdTipoTrasportoRientro("1");
        dto.setDataRegistrazione(dataReg);

        dto.setDestinazione("qqq");
        dto.setIndirizzo("qqq");

        dto.setFlIncaricato(Boolean.FALSE);
        dto.setFlVettore(Boolean.TRUE);
        dto.setNominativoVettore("qqq");

        dto.setCdTerzoResponsabile(3942L);

        List<DocTrasportoRientroDettDTOBulk> dettagli = new ArrayList<>();
        dettagli.add(buildDettaglioTrasporto(1637L, 0, 4008L));
        dettagli.add(buildDettaglioTrasporto(1638L, 0, 4008L));

        dto.setDettagli(dettagli);
        dto.setAttachments(buildAllegatiTrasportoDef());

        DocTrasportoRientroDTOBulk risposta =
                eseguiChiamataRest(dto, "Trasporto DEF");

        if (risposta != null) {
            pgTrasportoDefCreato = risposta.getPgDocTrasportoRientro();
            System.out.println(">>> PG Trasporto DEF: " + pgTrasportoDefCreato);
        }
    }

    // =========================================================================
    // TEST C — RIENTRO INS (Replica DB)
    // =========================================================================
    @Test
    public void C_testRientroIns() throws Exception {

        Timestamp dataReg = toTimestamp("20260219");

        DocTrasportoRientroDTOBulk dto = new DocTrasportoRientroDTOBulk();

        dto.setTiDocumento("R");
        dto.setStato("INS");
        dto.setEsercizio(ESERCIZIO);

        dto.setDsDocTrasportoRientro("werwerwe");
        dto.setCdTipoTrasportoRientro("2");
        dto.setDataRegistrazione(dataReg);

        dto.setDestinazione("erwerwer");
        dto.setIndirizzo("werwerwerw");

        dto.setFlIncaricato(Boolean.FALSE);
        dto.setFlVettore(Boolean.TRUE);
        dto.setNominativoVettore("rwerwer");

        dto.setNote("rewrwerwe");

        dto.setCdTerzoResponsabile(3942L);

        List<DocTrasportoRientroDettDTOBulk> dettagli = new ArrayList<>();
        dettagli.add(buildDettaglioRientro(1637L, 0, 4008L));
        dettagli.add(buildDettaglioRientro(1638L, 0, 4008L));

        dto.setDettagli(dettagli);
        dto.setAttachments(buildAllegatiRientroIns());

        eseguiChiamataRest(dto, "Rientro INS (Replica DB)");
    }

    // =========================================================================
    // TEST D — RIENTRO DEF
    // =========================================================================
    @Test
    public void D_testRientroDef() throws Exception {

        Timestamp dataReg = toTimestamp("20260219");

        DocTrasportoRientroDTOBulk dto = new DocTrasportoRientroDTOBulk();

        dto.setTiDocumento("R");
        dto.setStato("DEF");
        dto.setEsercizio(ESERCIZIO);

        dto.setDsDocTrasportoRientro("Rientro TEST");
        dto.setCdTipoTrasportoRientro("2");
        dto.setDataRegistrazione(dataReg);

        dto.setFlVettore(Boolean.TRUE);
        dto.setNominativoVettore("Vettore Rientro");

        dto.setCdTerzoResponsabile(3942L);

        List<DocTrasportoRientroDettDTOBulk> dettagli = new ArrayList<>();
        dettagli.add(buildDettaglioRientro(1637L, 0, 4008L));
        dettagli.add(buildDettaglioRientro(1638L, 0, 4008L));

        dto.setDettagli(dettagli);
        dto.setAttachments(buildAllegatiRientroDef());

        eseguiChiamataRest(dto, "Rientro DEF");
    }

    // =========================================================================
    // FACTORY DETTAGLI
    // =========================================================================
    private DocTrasportoRientroDettDTOBulk buildDettaglioTrasporto(
            Long nrInventario, int progressivo, Long cdTerzo) {

        DocTrasportoRientroDettDTOBulk det = new DocTrasportoRientroDettDTOBulk();
        det.setPgInventario(1L);
        det.setNrInventario(nrInventario);
        det.setProgressivo(progressivo);
        det.setCdTerzoAssegnatario(cdTerzo);

        return det;
    }

    private DocTrasportoRientroDettDTOBulk buildDettaglioRientro(
            Long nrInventario, int progressivo, Long cdTerzo) {

        DocTrasportoRientroDettDTOBulk det =
                buildDettaglioTrasporto(nrInventario, progressivo, cdTerzo);

        det.setTiDocumentoRif("T");
        det.setEsercizioRif(ESERCIZIO);
        det.setPgInventarioRif(1L);
        det.setPgDocTrasportoRientroRif(pgTrasportoDefCreato);
        det.setNrInventarioRif(nrInventario);
        det.setProgressivoRif(progressivo);

        return det;
    }

    // =========================================================================
    // ALLEGATI
    // =========================================================================
    private List<AttachmentDocTrasportoRientro> buildAllegatiTrasportoIns() {

        List<AttachmentDocTrasportoRientro> lista = new ArrayList<>();

        lista.add(buildAllegato(
                "trasporto_ins.pdf",
                AllegatoDocumentoTrasportoBulk.P_SIGLA_DOCTRASPORTO_ATTACHMENT_ALTRO
        ));

        return lista;
    }

    private List<AttachmentDocTrasportoRientro> buildAllegatiTrasportoDef() {

        List<AttachmentDocTrasportoRientro> lista = new ArrayList<>();

        lista.add(buildAllegato(
                "trasporto_firmato.pdf",
                AllegatoDocumentoTrasportoBulk.P_SIGLA_DOCTRASPORTO_ATTACHMENT_FIRMATO
        ));

        return lista;
    }

    private List<AttachmentDocTrasportoRientro> buildAllegatiRientroIns() {

        List<AttachmentDocTrasportoRientro> lista = new ArrayList<>();

        lista.add(buildAllegato(
                "rientro_ins.pdf",
                AllegatoDocumentoRientroBulk.P_SIGLA_DOCTRASPORTO_ATTACHMENT_ALTRO
        ));

        return lista;
    }

    private List<AttachmentDocTrasportoRientro> buildAllegatiRientroDef() {

        List<AttachmentDocTrasportoRientro> lista = new ArrayList<>();

        lista.add(buildAllegato(
                "rientro_firmato.pdf",
                AllegatoDocumentoRientroBulk.P_SIGLA_DOCTRASPORTO_ATTACHMENT_FIRMATO
        ));

        return lista;
    }

    private AttachmentDocTrasportoRientro buildAllegato(String nome, String tipo) {

        AttachmentDocTrasportoRientro att = new AttachmentDocTrasportoRientro();
        att.setNomeFile(nome);
        att.setMimeTypes(MimeTypes.PDF);
        att.setTypeAttachment(tipo);
        att.setBytes(("PDF-FITTIZIO-" + nome).getBytes());

        return att;
    }

    // =========================================================================
    // HELPER
    // =========================================================================
    private Timestamp toTimestamp(String data) throws Exception {
        return new Timestamp(new SimpleDateFormat("yyyyMMdd").parse(data).getTime());
    }

    private DocTrasportoRientroDTOBulk eseguiChiamataRest(
            DocTrasportoRientroDTOBulk dto, String descrizione) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true);

        String json = mapper.writeValueAsString(dto);

        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(USERNAME, PASSWORD));

        HttpClient client = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(provider)
                .build();

        HttpPost request = new HttpPost(BASE_URL + "/" + ENDPOINT);
        request.setHeader("Content-Type", "application/json;charset=UTF-8");

        request.setHeader(SIGLASecurityContext.X_SIGLA_CD_CDS, CD_CDS);
        request.setHeader(SIGLASecurityContext.X_SIGLA_CD_UNITA_ORGANIZZATIVA, CD_UO);
        request.setHeader(SIGLASecurityContext.X_SIGLA_ESERCIZIO, String.valueOf(ESERCIZIO));

        request.setEntity(new StringEntity(json, "UTF-8"));

        HttpResponse response = client.execute(request);
        int statusCode = response.getStatusLine().getStatusCode();

        System.out.println("\n=== " + descrizione + " ===");
        System.out.println("HTTP Status: " + statusCode);

        if (response.getEntity() != null) {
            String body = IOUtils.toString(response.getEntity().getContent(), "UTF-8");
            System.out.println(body);

            if (statusCode == 201) {
                return mapper.readValue(body, DocTrasportoRientroDTOBulk.class);
            }
        }

        Assert.assertEquals(201, statusCode);
        return null;
    }
}

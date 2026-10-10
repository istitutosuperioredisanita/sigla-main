package it.cnr.test.h2.rest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;
import it.cnr.contab.web.rest.model.FondiFunzionamentoUODTO;
import it.cnr.test.h2.utenze.action.ActionDeployments;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DashboardRestTest extends ActionDeployments {

    public static final String USERNAME = "ENTETEST";
    public static final String PASSWORD = "PASSTEST";

    @Test
    @Order(1)
    public void testFondiFunzionamentoUO() throws Exception {

        Unita_organizzativaBulk uoAttesa = new Unita_organizzativaBulk("000.000");
        uoAttesa.setDs_unita_organizzativa("Struttura Amministrativa Centrale");
        uoAttesa.setImportoFinanziato(new BigDecimal("1000000.00"));
        uoAttesa.setImportoUtilizzato(BigDecimal.ZERO);
        FondiFunzionamentoUODTO expected = new FondiFunzionamentoUODTO(uoAttesa);

        JsonNode root = getJson("/restapi/progetto/fondi-funzionamento/uo/2025");
        Assertions.assertTrue(root.isArray(), "La risposta deve essere un array");
        Assertions.assertFalse(root.isEmpty(), "La risposta non deve essere vuota");

        JsonNode actual = root.get(0);
        Assertions.assertEquals(expected.getCodice(), actual.get("codice").asText());
        Assertions.assertEquals(expected.getDescrizione(), actual.get("descrizione").asText());
        Assertions.assertEquals(0, expected.getImportoFinanziato()
                .compareTo(actual.get("importoFinanziato").decimalValue()));
        Assertions.assertEquals(0, expected.getImportoUtilizzato()
                .compareTo(actual.get("importoUtilizzato").decimalValue()));
    }

    @Test
    @Order(2)
    public void testFondiFunzionamentoElementoVoce() throws Exception {
        JsonNode root = getJson("/restapi/progetto/fondi-funzionamento/elemento-voce/2025");

        Assertions.assertTrue(root.isArray(), "La risposta deve essere un array");
        Assertions.assertEquals(3, root.size());

        assertVoce(root, "22011", "Attrezzature sanitarie",
                new BigDecimal("1500000.00"), new BigDecimal("10000.00"));
        assertVoce(root, "22010", "Attrezzature scientifiche",
                new BigDecimal("1000000.00"), new BigDecimal("10000.00"));
        assertVoce(root, "13017", "Altri beni e materiali di consumo",
                new BigDecimal("500000.00"), BigDecimal.ZERO);
    }

    private JsonNode getJson(String path) throws Exception {
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(USERNAME, PASSWORD));
        HttpClient client = HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();

        HttpGet method = new HttpGet(deploymentURL.toString().concat(path));
        method.addHeader("Accept-Language", Locale.getDefault().toString());
        method.setHeader("Content-Type", "application/json;charset=UTF-8");

        HttpResponse response = client.execute(method);
        String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        Assertions.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode(), body);
        return new ObjectMapper().readTree(body);
    }

    private void assertVoce(JsonNode root, String codice, String descrizione,
                            BigDecimal finanziato, BigDecimal utilizzato) {
        JsonNode voce = null;
        for (JsonNode n : root) {
            if (codice.equals(n.path("codice").asText())) {
                voce = n;
                break;
            }
        }
        Assertions.assertNotNull(voce, "Voce non trovata: " + codice);
        Assertions.assertEquals(descrizione, voce.get("descrizione").asText(), "descrizione " + codice);
        Assertions.assertEquals(0, finanziato.compareTo(voce.get("importoFinanziato").decimalValue()),
                "importoFinanziato " + codice);
        Assertions.assertEquals(0, utilizzato.compareTo(voce.get("importoUtilizzato").decimalValue()),
                "importoUtilizzato " + codice);
    }
}

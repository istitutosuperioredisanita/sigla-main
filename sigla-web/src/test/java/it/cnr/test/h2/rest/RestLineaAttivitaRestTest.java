package it.cnr.test.h2.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.cnr.contab.anagraf00.core.bulk.TerzoKey;
import it.cnr.contab.config00.latt.bulk.CofogKey;
import it.cnr.contab.config00.pdcfin.bulk.FunzioneKey;
import it.cnr.contab.config00.pdcfin.bulk.NaturaKey;
import it.cnr.contab.config00.sto.bulk.CdrKey;
import it.cnr.contab.prevent01.bulk.Pdg_missioneKey;
import it.cnr.contab.web.rest.config.SIGLASecurityContext;
import it.cnr.contab.web.rest.model.EnumTiGestioneLineaAttivita;
import it.cnr.contab.web.rest.model.LineaAttivitaDto;
import it.cnr.contab.web.rest.model.ProgettoDto;
import it.cnr.jada.comp.ComponentException;
import it.cnr.test.h2.utenze.action.ActionDeployments;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.beans.factory.annotation.Value;

import java.util.Locale;

public class RestLineaAttivitaRestTest extends ActionDeployments {

    @Value("${test.value}")
    public String value;

    @Test
    @RunAsClient
    @OperateOnDeployment(TEST_H2)
    @InSequence(1)
    public void testLineAttivitaCreazione()throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        LineaAttivitaDto c = new LineaAttivitaDto();
        c.setEsercizio_inizio(2024);
        c.setEsercizio_fine( 2024);
        c.setCd_linea_attivita("TESTGA");
        c.setDs_linea_attivita("Test Creazione Automatica");
        c.setDenominazione("Test Creazione Automatica");
        c.setCentro_responsabilitaKey(new CdrKey("000.000"));
        c.setProgettoKey(new ProgettoDto(2024,1639));
        c.setPdgMissioneKey(new Pdg_missioneKey("MISS01"));
        c.setCofogKey(new CofogKey("01.4"));
        c.setResponsabileKey( new TerzoKey(3942));
        c.setTi_gestione(EnumTiGestioneLineaAttivita.SPESA);
        c.setNaturaKey( new NaturaKey("2"));
        c.setFunzioneKey(new FunzioneKey("01"));
        String myJson = null;
        try {
            myJson = mapper.writeValueAsString(c);
        } catch (Exception ex) {
            throw new ComponentException("Errore nella generazione del file JSON per l'esecuzione della stampa ( errore joson).",ex);
        }
        HttpEntity e = new StringEntity(myJson.toString());
        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("ENTE", "2023ENTEISS");
        provider.setCredentials(AuthScope.ANY, credentials);
        HttpClient client=HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();
        HttpPost method = new HttpPost(deploymentURL.toString().concat("/restapi/lineaattivita"));
        method.addHeader("Accept-Language", Locale.getDefault().toString());
        method.setHeader("Content-Type", "application/json;charset=UTF-8");
        method.setHeader(SIGLASecurityContext.X_SIGLA_ESERCIZIO,"2024");
        method.setHeader(SIGLASecurityContext.X_SIGLA_CD_CDS,"999");
        method.setHeader(SIGLASecurityContext.X_SIGLA_CD_UNITA_ORGANIZZATIVA, "999.999");
        method.setHeader(SIGLASecurityContext.X_SIGLA_CD_CDR, "000.000.000");
        method.setEntity(e);
        HttpResponse response = client.execute(method);//Replace HttpPost with HttpGet if you need to perform a GET to login
        int statusCode = response.getStatusLine().getStatusCode();


        System.out.println("Response Code :"+ statusCode);

    }

    @Test
    @RunAsClient
    @OperateOnDeployment(TEST_H2)
    @InSequence(2)
    public void jsonTest()throws Exception {

        System.out.println(value);
        /*
        final String regex = "[0-9A-Z]{3}\\.[0-9A-Z]{3}";
        final Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        String unita="00.aA1";
        Boolean b = pattern.matcher(unita).matches();
        Assert.assertTrue(b);


        String text = "2009-10-20";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        Date parsedDate = dateFormat.parse("20211122");
        Timestamp tm=new java.sql.Timestamp(parsedDate.getTime());
        ContrattoDtoBulk c = new ContrattoDtoBulk();
        c.setEsercizio(2021);
        List<AttachmentContratto> l = new ArrayList<AttachmentContratto>();
        AttachmentContratto a = new AttachmentContratto();
        a.setNomeFile("contratto.pdf");
        a.setMimeTypes(MimeTypes.PDF);


         //file = new File(classLoader.getResource("contratto.pdf"));
        InputStream is = this.getClass().getResourceAsStream("/contratto.pdf");
        byte[] bytes = IOUtils.toByteArray(is);
        byte[] encoded= Base64.getEncoder().encode(bytes);

        a.setTypeAttachment(EnumTypeAttachmentContratti.CONTRATTO_FLUSSO);
        a.setBytes(encoded);
        l.add(a);
        c.setAttachments(l);

        StampaInventarioDTO dto = new StampaInventarioDTO();
        dto.setDescCatGrp("ciao");
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory jsonFactory = new JsonFactory();
        String myJson = null;
        try {
            myJson = mapper.writeValueAsString(c);
        } catch (Exception ex) {
            throw new ComponentException("Errore nella generazione del file JSON per l'esecuzione della stampa ( errore joson).",ex);
        }
        System.out.println(myJson);
    */
    }

}

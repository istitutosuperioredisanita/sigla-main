package it.cnr.test.h2.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.cnr.contab.web.rest.config.SIGLASecurityContext;
import it.cnr.contab.web.rest.model.*;
import it.cnr.jada.comp.ComponentException;
import it.cnr.si.spring.storage.MimeTypes;
import it.cnr.test.h2.utenze.action.ActionDeployments;
import org.apache.commons.io.IOUtils;
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

import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

public class RestServiceContrattiTest extends ActionDeployments {

    @Value("${test.value}")
    public String value;

    @Test
    @RunAsClient
    @OperateOnDeployment(TEST_H2)
    @InSequence(1)
    public void testContrattiMaggioli()throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        ContrattoDtoBulk c = new ContrattoDtoBulk();
        c.setEsercizio(2025);
        c.setCodiceFlussoAcquisti( "PLUTO30");
        c.setCd_unita_organizzativa("000.001");
        c.setCodfisPivaRupExt("RGLNLR52E69Z600O");
        c.setCodfisPivaAggiudicatarioExt("05923561004");
        //c.setCodfisPivaFirmatarioExt("ZNCMRT79E49H501E");

        //c.setTipoDettaglioContratto(EnumTipoDettaglioContratto.DETTAGLIO_CONTRATTO_ARTICOLI);
        c.setTipoDettaglioContratto(EnumTipoDettaglioContratto.DETTAGLIO_CONTRATTO_CATGRP);
        DettaglioContrattoDtoBulk dettaglioContrattoDtoBulk= new DettaglioContrattoDtoBulk();
        dettaglioContrattoDtoBulk.setCdCategoriaGruppo("0.0.3");
        //dettaglioContrattoDtoBulk.setCdBeneServizio("AA00107");
        //dettaglioContrattoDtoBulk.setPrezzoUnitario(new BigDecimal(2920));
        //dettaglioContrattoDtoBulk.setCdCategoriaGruppo("8.0");
        //dettaglioContrattoDtoBulk.setQuantitaMax(new BigDecimal(10));
        //dettaglioContrattoDtoBulk.setQuantitaMax(new BigDecimal(1));
        c.addDettaglioContratto(dettaglioContrattoDtoBulk);
        //dettaglioContrattoDtoBulk= new DettaglioContrattoDtoBulk();
        //dettaglioContrattoDtoBulk.setCdBeneServizio("AA00385");
        //dettaglioContrattoDtoBulk.setCdCategoriaGruppo("0.4");
       // dettaglioContrattoDtoBulk.setPrezzoUnitario(new BigDecimal(110));
       // dettaglioContrattoDtoBulk.setQuantitaMax(new BigDecimal(100));
       // dettaglioContrattoDtoBulk.setQuantitaMax(new BigDecimal(2));
        //c.addDettaglioContratto(dettaglioContrattoDtoBulk);




        c.setDs_atto("DECISIONE A CONTRARRE");
        c.setOggetto("Oggetto Test Contratto test");

        c.setIm_contratto_passivo(new BigDecimal("1000"));
        c.setIm_contratto_passivo_netto(new BigDecimal("1000"));
        c.setCd_tipo_norma_perla("30D");
        //c.setCd_tipo_atto("DEL");
        //c.setCd_tipo_contratto
        //c.setCd_proc_amm();
            //c.setCd_organo();
        c.setCdCigExt("983e989");
        c.setCdCupExt("test");
        c.setCd_proc_amm("PA");
        c.setCd_tipo_norma_perla("19");
        c.setCd_tipo_contratto("UTENZ");
        c.setCd_organo("DRUE");


        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

        c.setDt_stipula(new java.sql.Timestamp(dateFormat.parse("20231001").getTime()));
        c.setDt_inizio_validita(new java.sql.Timestamp(dateFormat.parse("20231122").getTime()));

        c.setDt_fine_validita(new java.sql.Timestamp(dateFormat.parse("20241122").getTime()));
        c.setDt_registrazione(new java.sql.Timestamp(dateFormat.parse("20231223").getTime()));


        c.setCdCigExt("845fktest");
        c.setCdCupExt("983e938");
        //Tipologia
        //Tipo_norma_perlaBulk tipoNormaPerla da aggiungere al contrarroDtoBulk ( tipoNormaPerla)
        //Procedura amministrativa da aggiungere al contrattoDtoBulk (Procedure_amministrativeBulk)
        List<AttachmentContratto> l = new ArrayList<AttachmentContratto>();
        AttachmentContratto a = new AttachmentContratto();
        a.setNomeFile("contratto.pdf");
        a.setMimeTypes(MimeTypes.PDF);

        InputStream is = this.getClass().getResourceAsStream("/contratto.pdf");
        byte[] bytes = IOUtils.toByteArray(is);
        byte[] encoded= Base64.getEncoder().encode(bytes);
        try (FileOutputStream fos = new FileOutputStream("E:\\sigla\\contratto.txt")) {
            fos.write(encoded);
            //fos.close(); There is no more need for this line since you had created the instance of "fos" inside the try. And this will automatically close the OutputStream
        }

        a.setTypeAttachment(EnumTypeAttachmentContratti.CONTRATTO_FLUSSO);
        a.setBytes(encoded);
        l.add(a);
        c.setAttachments(l);
        String myJson = null;
        try {
            myJson = mapper.writeValueAsString(c);
        } catch (Exception ex) {
            throw new ComponentException("Errore nella generazione del file JSON per l'esecuzione della stampa ( errore joson).",ex);
        }
        try (FileOutputStream fos = new FileOutputStream("E:\\sigla\\jsonContratto.json")) {
            fos.write(myJson.getBytes());
            //fos.close(); There is no more need for this line since you had created the instance of "fos" inside the try. And this will automatically close the OutputStream
        }
        HttpEntity e = new StringEntity(myJson.toString());
        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("MAGGIOLI", "MAGGIOLI");
        provider.setCredentials(AuthScope.ANY, credentials);
        HttpClient client=HttpClientBuilder.create().setDefaultCredentialsProvider(provider).build();
        HttpPost method = new HttpPost(deploymentURL.toString().concat("/restapi/contrattoMaggioli"));
        method.addHeader("Accept-Language", Locale.getDefault().toString());
        method.setHeader("Content-Type", "application/json;charset=UTF-8");
        method.setHeader(SIGLASecurityContext.X_SIGLA_CD_CDS,"999");
        method.setHeader(SIGLASecurityContext.X_SIGLA_CD_UNITA_ORGANIZZATIVA, "999.999");
        method.setEntity(e);
        HttpResponse response = client.execute(method);//Replace HttpPost with HttpGet if you need to perform a GET to login
        int statusCode = response.getStatusLine().getStatusCode();
        //JsonFactory jsonFactory = new JsonFactory();
        //try {
        //    myJson = mapper.writeValueAsString(response.getEntity());
        //} catch (Exception ex) {
        //    throw new ComponentException("Errore nella generazione del file JSON per l'esecuzione della stampa ( errore joson).",ex);
        //}


        System.out.println("Response Code :"+ statusCode);
        System.out.println("myJson :"+ myJson);

    }







    }

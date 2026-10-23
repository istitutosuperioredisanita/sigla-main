package it.cnr.contab.inventario01.service;

import it.cnr.contab.service.SpringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class DocTraspRientFirmaTestService {

    private static final Logger log =
            LoggerFactory.getLogger(DocTraspRientFirmaTestService.class);

    public static final String PROP_ENABLED =
            "doc.trasp.rient.happysign.test.enabled";
    public static final String PROP_SIGNER_CF =
            "doc.trasp.rient.happysign.test.signerFiscalCode";
    public static final String PROP_SIGNER_COUNT =
            "doc.trasp.rient.happysign.test.forceSignerCount";
    public static final String PROP_EMAIL_CREATORE =
            "doc.trasp.rient.happysign.test.emailCreatore";

    private DocTraspRientFirmaTestService() {
    }

    public static boolean isAttiva() {
        return Boolean.parseBoolean(
                env().getProperty(PROP_ENABLED, "false")
        );
    }

    public static List<String> applicaFirmatari(
            List<String> codiciFiscaliReali) {

        if (!isAttiva()) {
            return codiciFiscaliReali;
        }

        Environment env = env();
        String cfTest = env.getProperty(PROP_SIGNER_CF);

        if (cfTest == null || cfTest.isBlank()) {
            log.warn("{}=true ma {} non valorizzato: uso i firmatari reali", PROP_ENABLED, PROP_SIGNER_CF);
            return codiciFiscaliReali;
        }

        int n = Math.max(1, env.getProperty(PROP_SIGNER_COUNT, Integer.class, codiciFiscaliReali.size()));

        List<String> test = new ArrayList<>();
        String codiceFiscale = cfTest.trim().toUpperCase(Locale.ROOT);

        for (int i = 0; i < n; i++) {
            test.add(codiceFiscale);
        }

        log.warn("MODALITA' TEST firma T/R attiva: {} firmatari sostituiti", test.size());

        return test;
    }

    public static String getEmailCreatore() {
        return isAttiva() ? env().getProperty(PROP_EMAIL_CREATORE) : null;
    }

    private static Environment env() {
        return SpringUtil.getBean(Environment.class);
    }
}
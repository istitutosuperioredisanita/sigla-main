package it.cnr.contab.inventario01.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Modalità test firma T/R. */
@Service
public class DocTraspRientFirmaTestService {

    @Value("${doc.trasp.rient.happysign.test.enabled:false}")
    private boolean attiva;

    @Value("${doc.trasp.rient.happysign.test.signerFiscalCode:}")
    private String signerFiscalCode;

    @Value("${doc.trasp.rient.happysign.test.forceSignerCount:}")
    private Integer forceSignerCount;

    @Value("${doc.trasp.rient.happysign.test.emailCreatore:}")
    private String emailUC;

    public List<String> applicaFirmatari(List<String> codiciFiscaliReali) {
        if (!attiva || signerFiscalCode == null || signerFiscalCode.isBlank())
            return codiciFiscaliReali;

        int n = Math.max(1, forceSignerCount != null ? forceSignerCount : codiciFiscaliReali.size());
        return new ArrayList<>(Collections.nCopies(n, signerFiscalCode.trim().toUpperCase(Locale.ROOT)));
    }

    public String getEmailUC() {
        return attiva && emailUC != null && !emailUC.isBlank() ? emailUC.trim() : null;
    }
}

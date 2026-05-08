package it.cnr.contab.inventario01.service;

import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.inventario01.dto.StartWorkflowDocTraspRientDto;
import it.iss.si.dto.happysign.base.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service applicativo per invio documenti Trasporto/Rientro a HappySign.
 */
@Service
public class DocTraspRientHappySignService {

    private static final String TEST_SIGNER = "davide.mirra@iss.it";

    @Autowired
    private UtilHappySignDocTraspRient utilHappySignDocTraspRient;

    @Autowired
    private DocTraspRientTerzoService terzoService;

    public String inviaDocumentoAdHappySign(Doc_trasporto_rientroBulk doc,
                                            byte[] pdfBytes) throws Exception {

        if (doc == null) {
            throw new IllegalArgumentException("Documento non presente");
        }

        if (pdfBytes == null || pdfBytes.length == 0) {
            throw new IllegalArgumentException("PDF documento non presente");
        }

        StartWorkflowDocTraspRientDto dto = new StartWorkflowDocTraspRientDto();
        dto.setDocumento(doc);

        List<String> signers = new ArrayList<>();

        /*
         * PARTE UFFICIALE - DA RIPRISTINARE DOPO IL TEST
         *
         * String emailResponsabile = terzoService.getEmailTerzo(doc.getCdTerzoResponsabile());
         * if (emailResponsabile != null) {
         *     signers.add(emailResponsabile);
         * }
         *
         * if (doc.isRitiroIncaricato() && doc.getCdTerzoIncaricato() != null) {
         *     String emailIncaricato = terzoService.getEmailTerzo(doc.getCdTerzoIncaricato());
         *     if (emailIncaricato != null) {
         *         signers.add(emailIncaricato);
         *     }
         * }
         *
         * if (doc.getConsegnatario() != null && doc.getConsegnatario().getCd_terzo() != null) {
         *     String emailConsegnatario = terzoService.getEmailTerzo(doc.getConsegnatario().getCd_terzo());
         *     if (emailConsegnatario != null) {
         *         signers.add(emailConsegnatario);
         *     }
         * }
         */

        /*
         * TEST TEMPORANEO:
         * per ora tutti i flussi HappySign vengono inviati sempre
         * allo stesso firmatario.
         */
        signers.add(TEST_SIGNER);

        if (signers.isEmpty()) {
            throw new IllegalStateException("Nessun firmatario presente per il documento");
        }

        dto.setSigners(utilHappySignDocTraspRient.getNoDoubleSigners(signers));
        dto.setApprovers(new ArrayList<>());

        utilHappySignDocTraspRient.setTemplateFirme(dto);

        String fileName = "DocTrasportoRientro_"
                + doc.getEsercizio()
                + "_"
                + doc.getTiDocumento()
                + "_"
                + doc.getPgDocTrasportoRientro()
                + ".pdf";

        File fileToSign = utilHappySignDocTraspRient.toHappySignFile(fileName, pdfBytes);
        dto.setFileToSign(fileToSign);

        return utilHappySignDocTraspRient.send(
                dto.getTemplateName(),
                dto.getSigners(),
                dto.getApprovers(),
                dto.getFileToSign()
        );
    }
}
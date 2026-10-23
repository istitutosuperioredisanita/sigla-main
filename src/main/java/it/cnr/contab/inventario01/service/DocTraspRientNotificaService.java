package it.cnr.contab.inventario01.service;

import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.inventario01.util.DocTraspRientUtil;
import it.cnr.jada.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Email al creatore del documento T/R quando viene firmato o rifiutato su HappySign.
 * Indirizzo e invio passano dal component (flowService): dal thread dello scheduler
 * non sono disponibili home e mail session.
 */
@Service
@Profile("iss")
public class DocTraspRientNotificaService {

    private static final Logger log =
            LoggerFactory.getLogger(DocTraspRientNotificaService.class);

    @Autowired
    private DocTraspRientFlowService flowService;

    // TEST: destinatario usato se l'email del creatore non è recuperabile (vuoto = nessun invio)
    @Value("${doc.trasp.rient.happysign.test.emailCreatore:}")
    private String emailCreatoreTest;

    public void notificaFirmato(UserContext userContext, Doc_trasporto_rientroBulk doc) {
        String tipo = DocTraspRientUtil.descrizioneTipo(doc);
        String subject = "Documento di " + tipo + " n. " + doc.getPgDocTrasportoRientro() + " firmato";
        String body = "<p>Il documento di " + tipo + " n. <b>" + doc.getPgDocTrasportoRientro()
                + "</b> (esercizio " + doc.getEsercizio()
                + ") è stato <b>firmato</b> e risulta ora definitivo.</p>"
                + "<p>Il PDF firmato è disponibile tra gli allegati del documento.</p>";

        inviaMail(userContext, doc, subject, body);
    }

    public void notificaRifiutato(UserContext userContext, Doc_trasporto_rientroBulk doc,
                                  String motivoRifiuto) {
        String tipo = DocTraspRientUtil.descrizioneTipo(doc);
        String subject = "Documento di " + tipo + " n. " + doc.getPgDocTrasportoRientro() + " rifiutato in firma";
        String motivo = (motivoRifiuto != null && !motivoRifiuto.isBlank()) ? motivoRifiuto : "non specificato";
        String body = "<p>Il documento di " + tipo + " n. <b>" + doc.getPgDocTrasportoRientro()
                + "</b> (esercizio " + doc.getEsercizio()
                + ") è stato <b>rifiutato</b> in fase di firma.</p>"
                + "<p><b>Motivo del rifiuto:</b><br>" + escapeHtml(motivo) + "</p>"
                + "<p>Il documento torna in stato INSERITO e può essere corretto e reinviato in firma.</p>";

        inviaMail(userContext, doc, subject, body);
    }

    /** Non solleva mai eccezioni: la notifica non deve far fallire l'aggiornamento del documento. */
    private void inviaMail(UserContext userContext, Doc_trasporto_rientroBulk doc,
                           String subject, String body) {
        try {
            String email = null;
            try {
                email = flowService.getEmailCreatore(userContext, doc);
            } catch (Exception e) {
                log.warn("Errore recupero email creatore per pg={}: {}", doc.getPgDocTrasportoRientro(), e.getMessage());
            }
            if ((email == null || email.isBlank()) && !emailCreatoreTest.isBlank()) {
                log.warn("MODALITA' TEST: email creatore non recuperata per pg={}, invio a {}",
                        doc.getPgDocTrasportoRientro(), emailCreatoreTest);
                email = emailCreatoreTest;
            }
            if (email == null || email.isBlank()) {
                log.warn("Nessun destinatario per la notifica del documento pg={}", doc.getPgDocTrasportoRientro());
                return;
            }
            flowService.inviaMail(userContext, subject, body, List.of(email.trim()));
            log.info("Notifica '{}' inviata a {}", subject, email.trim());
        } catch (Exception e) {
            log.error("Errore invio notifica per documento pg={}", doc.getPgDocTrasportoRientro(), e);
        }
    }

    private String escapeHtml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n", "<br>");
    }
}

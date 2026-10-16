package it.cnr.contab.inventario01.service;

import it.cnr.contab.inventario01.bulk.Doc_trasporto_rientroBulk;
import it.cnr.jada.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Notifiche email per i documenti di Trasporto/Rientro firmati o rifiutati da HappySign.
 * Destinatario: il creatore del documento (utcr). L'indirizzo viene ricostruito dal component
 * (cd_utente_uid + dominio email di configurazione PK_COSTANTI / SK_DOMINIO_EMAIL), chiamato
 * via flowService perché il thread dello scheduler non ha il contesto EJB per i lookup java:module
 * e non può usare le home.
 * <p>
 * L'invio vero e proprio avviene tramite flowService (quindi in un component EJB): dal thread
 * dello scheduler il lookup java:comp/env/mail/MailSession non è disponibile e SendMail
 * ripiegherebbe sulla session di default di WildFly (localhost, senza mail.from).
 * Se l'indirizzo non è risolvibile la notifica non viene inviata (nessun fallback, nessuna eccezione).
 */
@Service
@Profile("iss")
public class DocTraspRientNotificaService {

    private static final Logger log =
            LoggerFactory.getLogger(DocTraspRientNotificaService.class);

    @Autowired
    private DocTraspRientFlowService flowService;

    /** Notifica al creatore che il documento è stato firmato. */
    public void notificaFirmato(UserContext userContext, Doc_trasporto_rientroBulk doc) {
        String tipo = descrizioneTipo(doc);
        String subject = "Documento " + tipo + " n. " + doc.getPgDocTrasportoRientro()
                + " firmato";
        String body = "<p>Il documento " + tipo + " n. <b>" + doc.getPgDocTrasportoRientro()
                + "</b> (esercizio " + doc.getEsercizio()
                + ") è stato <b>firmato</b> e risulta ora definitivo.</p>"
                + "<p>Il PDF firmato è disponibile tra gli allegati del documento.</p>";

        inviaMail(userContext, doc, subject, body);
    }

    /** Notifica al creatore che il documento è stato rifiutato in firma, con il motivo. */
    public void notificaRifiutato(UserContext userContext, Doc_trasporto_rientroBulk doc,
                                  String motivoRifiuto) {
        String tipo = descrizioneTipo(doc);
        String subject = "Documento " + tipo + " n. " + doc.getPgDocTrasportoRientro()
                + " rifiutato in firma";
        String motivo = (motivoRifiuto != null && !motivoRifiuto.isBlank())
                ? motivoRifiuto
                : "non specificato";
        String body = "<p>Il documento " + tipo + " n. <b>" + doc.getPgDocTrasportoRientro()
                + "</b> (esercizio " + doc.getEsercizio()
                + ") è stato <b>rifiutato</b> in fase di firma.</p>"
                + "<p><b>Motivo del rifiuto:</b><br>" + escapeHtml(motivo) + "</p>"
                + "<p>Il documento torna in stato INSERITO e può essere corretto e reinviato in firma.</p>";

        inviaMail(userContext, doc, subject, body);
    }

    private void inviaMail(UserContext userContext, Doc_trasporto_rientroBulk doc,
                           String subject, String body) {
        try {
            List<String> destinatari = risolviDestinatari(userContext, doc);
            if (destinatari.isEmpty()) {
                log.warn("Nessun destinatario per la notifica del documento pg={}",
                        doc.getPgDocTrasportoRientro());
                return;
            }
            // Invio tramite component (contesto EJB): qui SendMail trova la mail session corretta
            flowService.inviaMail(userContext, subject, body, destinatari);
            log.info("Notifica '{}' inviata a {}", subject, destinatari);
        } catch (Exception e) {
            // La notifica non deve mai far fallire l'aggiornamento del documento
            log.error("Errore invio notifica per documento pg={}",
                    doc.getPgDocTrasportoRientro(), e);
        }
    }

    private List<String> risolviDestinatari(UserContext userContext, Doc_trasporto_rientroBulk doc)
            throws Exception {

         List<String> destinatari = new ArrayList<>();
         String email = flowService.getEmailCreatore(userContext, doc);
         if (email != null && !email.isBlank()) {
             destinatari.add(email.trim());
         }
         return destinatari;
    }

    private String descrizioneTipo(Doc_trasporto_rientroBulk doc) {
        return Doc_trasporto_rientroBulk.TRASPORTO.equals(doc.getTiDocumento())
                ? "di Trasporto"
                : "di Rientro";
    }

    private String escapeHtml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n", "<br>");
    }
}
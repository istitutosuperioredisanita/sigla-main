package it.cnr.contab.inventario01.config;

import it.cnr.contab.inventario01.service.DocTraspRientCronService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Scheduler per la verifica dello stato di firma digitale (HappySign)
 * dei documenti di trasporto e rientro.
 * Configurazione tramite application.yml: ${cron.happysign.active} e ${cron.happysign.cronExpression}.
 */
@Configuration
@EnableScheduling
public class DocTraspRientSchedulerConfig {

    private static final Logger log = LoggerFactory.getLogger(DocTraspRientSchedulerConfig.class);

    @Value("${cron.happysign.active}")
    private boolean cronHappySignActive;

    // Richiesto = false per non bloccare l'avvio se il service non è presente in tutti i profili.
    @Autowired(required = false)
    private DocTraspRientCronService docTRCronService;

    /**
     * Job schedulato per interrogare HappySign e aggiornare lo stato dei documenti INVIATO.
     */
    @Scheduled(cron = "${cron.happysign.cronExpression}")
    public void cronVerificaFirmeDocumentiTrasportoRientro() {
        if (!cronHappySignActive) {
            log.trace("Scheduler HappySign NON attivo. Saltato.");
            return;
        }

        if (docTRCronService == null) {
            log.warn("DocTraspRientCronService non disponibile. Saltato.");
            return;
        }

        try {
            log.info("--- INIZIO Verifica firme HappySign documenti trasporto/rientro ---");

            // Delega l'esecuzione al service
          //  docTRCronService.verificaFirmeDocumentiTrasportoRientro();

            log.info("--- FINE Verifica firme HappySign documenti trasporto/rientro ---");

        } catch (Exception e) {
            log.error("❌ ERRORE durante la verifica firme documenti trasporto/rientro", e);
        }
    }
}
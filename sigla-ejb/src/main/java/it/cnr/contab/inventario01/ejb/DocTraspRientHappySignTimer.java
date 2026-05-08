package it.cnr.contab.inventario01.ejb;

import it.cnr.contab.inventario01.service.DocTraspRientCronService;
import it.cnr.contab.service.SpringUtil;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Timer EJB per polling HappySign dei documenti di Trasporto/Rientro.
 *
 * Verifica ogni minuto lo stato dei documenti inviati a HappySign:
 * - se firmati: aggiorna STATO = DEF, STATO_FLUSSO = FIR
 * - se rifiutati: aggiorna STATO = INS, STATO_FLUSSO = RIF, NOTE_RIFIUTO
 *
 * L'utente tecnico e la scrivania batch vengono letti da properties:
 * - doctr.batch.user
 * - doctr.batch.cd_cds
 * - doctr.batch.cd_uo
 * - doctr.batch.cd_cdr
 */
@Singleton
@Startup
public class DocTraspRientHappySignTimer {

    private static final Logger log = LoggerFactory.getLogger(DocTraspRientHappySignTimer.class);

    private static final String PROPERTY_BATCH_USER = "doctr.batch.user";
    private static final String PROPERTY_BATCH_CD_CDS = "doctr.batch.cd_cds";
    private static final String PROPERTY_BATCH_CD_UO = "doctr.batch.cd_uo";
    private static final String PROPERTY_BATCH_CD_CDR = "doctr.batch.cd_cdr";

    /**
     * Esecuzione ogni minuto.
     */
    @Schedule(second = "0", minute = "*", hour = "*", persistent = false)
    public void verificaFirmeHappySign() {
        try {
            DocTraspRientCronService service =
                    SpringUtil.getBean(DocTraspRientCronService.class);

            CNRUserContext userContext = creaUserContextBatch();

            log.info(
                    "Avvio verifica firme HappySign Doc T/R - user={}, esercizio={}, cds={}, uo={}, cdr={}",
                    userContext.getUser(),
                    userContext.getEsercizio(),
                    userContext.getCd_cds(),
                    userContext.getCd_unita_organizzativa(),
                    userContext.getCd_cdr()
            );

            service.verificaFirmeDocumentiTrasportoRientro(userContext);

            log.info("Fine verifica firme HappySign Doc T/R");

        } catch (Exception e) {
            log.error("Errore timer HappySign documenti Trasporto/Rientro", e);
        }
    }

    private CNRUserContext creaUserContextBatch() {
        Environment environment = SpringUtil.getBean(Environment.class);

        String batchUser = getRequiredProperty(environment, PROPERTY_BATCH_USER);
        String batchCdCds = getRequiredProperty(environment, PROPERTY_BATCH_CD_CDS);
        String batchCdUo = getRequiredProperty(environment, PROPERTY_BATCH_CD_UO);
        String batchCdCdr = getRequiredProperty(environment, PROPERTY_BATCH_CD_CDR);

        Integer esercizio = LocalDate.now().getYear();
        String sessionId = "BATCH-HAPPYSIGN-" + batchUser + "-" + UUID.randomUUID();

        return new CNRUserContext(
                batchUser,
                sessionId,
                esercizio,
                batchCdUo,
                batchCdCds,
                batchCdCdr
        );
    }

    private String getRequiredProperty(Environment environment, String propertyName) {
        if (environment == null) {
            throw new IllegalStateException(
                    "Environment Spring non disponibile. Impossibile leggere la property: " + propertyName
            );
        }

        String value = environment.getProperty(propertyName);

        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException(
                    "Property obbligatoria non configurata: " + propertyName
            );
        }

        return value.trim();
    }
}
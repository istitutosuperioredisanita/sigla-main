package it.cnr.contab.inventario01.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Factory Spring per selezionare l'implementazione HappySign.
 *
 * Non importa classi di happysign-client, quindi può stare nel codice comune.
 * Se la classe reale ISS è presente nel classpath, la crea via reflection.
 * Altrimenti ritorna HappysignServiceNoOp.
 */
public class HappysignDocServiceFactoryBean
        implements FactoryBean<HappysignDocService>, ApplicationContextAware {

    private static final Logger log =
            LoggerFactory.getLogger(HappysignDocServiceFactoryBean.class);

    private static final String REAL_SERVICE_CLASS =
            "it.cnr.contab.inventario01.service.DocTraspRientHappySignService";

    private ApplicationContext applicationContext;

    private HappysignDocService instance;

    @Override
    public HappysignDocService getObject() {
        if (instance == null) {
            instance = creaServizio();
        }

        return instance;
    }

    @Override
    public Class<?> getObjectType() {
        return HappysignDocService.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    private HappysignDocService creaServizio() {
        try {
            Class<?> clazz = Class.forName(REAL_SERVICE_CLASS);

            Object service = clazz.getDeclaredConstructor().newInstance();

            if (applicationContext != null) {
                applicationContext
                        .getAutowireCapableBeanFactory()
                        .autowireBean(service);

                applicationContext
                        .getAutowireCapableBeanFactory()
                        .initializeBean(service, "docTraspRientHappySignServiceReal");
            }

            log.info("HappySign ISS attivo: uso {}", REAL_SERVICE_CLASS);

            return (HappysignDocService) service;

        } catch (ClassNotFoundException e) {
            log.info("HappySign ISS non attivo: uso HappysignServiceNoOp");
            return new HappysignDocServiceNoOp();

        } catch (NoClassDefFoundError e) {
            log.warn(
                    "Classe HappySign reale presente ma dipendenze mancanti: uso HappysignServiceNoOp. Dettaglio: {}",
                    e.getMessage()
            );
            return new HappysignDocServiceNoOp();

        } catch (Exception e) {
            log.warn(
                    "Errore inizializzazione HappySign reale: uso HappysignServiceNoOp. Dettaglio: {}",
                    e.getMessage(),
                    e
            );

            return new HappysignDocServiceNoOp();
        }
    }
}
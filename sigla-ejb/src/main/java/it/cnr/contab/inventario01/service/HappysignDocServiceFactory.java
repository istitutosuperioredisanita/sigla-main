package it.cnr.contab.inventario01.service;

import it.cnr.contab.service.SpringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory centrale per l'integrazione HappySign dei documenti Trasporto/Rientro.
 *
 * Non importa classi it.iss.si.* e quindi può essere usata anche nella build standard.
 */
public final class HappysignDocServiceFactory {

    private static final Logger log =
            LoggerFactory.getLogger(HappysignDocServiceFactory.class);

    private static final String HAPPYSIGN_CLIENT_CLASS =
            "it.iss.si.service.HappySignService";

    private static final String SPRING_BEAN_NAME =
            "docTraspRientHappySignService";

    private static final HappysignDocService NO_OP =
            new HappysignDocServiceNoOp();

    private HappysignDocServiceFactory() {
    }

    public static HappysignDocService get() {
        if (!isHappySignClientAvailable()) {
            log.warn("happysign-client non presente nel classpath: uso HappysignServiceNoOp");
            return NO_OP;
        }

        try {
            return SpringUtil.getBean(SPRING_BEAN_NAME, HappysignDocService.class);
        } catch (Throwable e) {
            log.error(
                    "Bean Spring '{}' non disponibile: uso HappysignServiceNoOp",
                    SPRING_BEAN_NAME,
                    e
            );
            return NO_OP;
        }
    }

    private static boolean isHappySignClientAvailable() {
        try {
            Class.forName(HAPPYSIGN_CLIENT_CLASS);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
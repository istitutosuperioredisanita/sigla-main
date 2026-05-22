package it.cnr.contab.inventario01.service;

import it.cnr.contab.service.SpringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ExternalServiceFactory {

    private static final Logger log =
            LoggerFactory.getLogger(ExternalServiceFactory.class);

    private ExternalServiceFactory() {
    }

    public static <T> T get(
            String requiredClassName,
            String springBeanName,
            Class<T> serviceInterface,
            T noOpInstance) {

        if (!isClassAvailable(requiredClassName)) {
            log.warn("{} non presente nel classpath: uso NoOp per {}",
                    requiredClassName,
                    serviceInterface.getSimpleName());
            return noOpInstance;
        }

        try {
            return SpringUtil.getBean(springBeanName, serviceInterface);
        } catch (Throwable e) {
            log.error(
                    "Bean Spring '{}' non disponibile: uso NoOp per {}",
                    springBeanName,
                    serviceInterface.getSimpleName(),
                    e
            );
            return noOpInstance;
        }
    }

    private static boolean isClassAvailable(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
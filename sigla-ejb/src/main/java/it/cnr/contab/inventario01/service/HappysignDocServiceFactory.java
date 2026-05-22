package it.cnr.contab.inventario01.service;

public final class HappysignDocServiceFactory {

    private static final String HAPPYSIGN_CLIENT_CLASS =
            "it.iss.si.service.HappySignService";

    private static final String SPRING_BEAN_NAME =
            "docTraspRientHappySignService";

    private static final HappysignDocService NO_OP =
            new HappysignDocServiceNoOp();

    private HappysignDocServiceFactory() {
    }

    public static HappysignDocService get() {
        return ExternalServiceFactory.get(
                HAPPYSIGN_CLIENT_CLASS,
                SPRING_BEAN_NAME,
                HappysignDocService.class,
                NO_OP
        );
    }
}
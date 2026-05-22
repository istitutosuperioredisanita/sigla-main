package it.cnr.contab.inventario01.service;

public final class AceEmailLookupServiceFactory {

    private static final String ACE_CLIENT_CLASS =
            "it.iss.si.service.AceService";

    private static final String SPRING_BEAN_NAME =
            "docTraspRientAceEmailLookupService";

    private static final AceEmailLookupService NO_OP =
            new AceEmailLookupServiceNoOp();

    private AceEmailLookupServiceFactory() {
    }

    public static AceEmailLookupService get() {
        return ExternalServiceFactory.get(
                ACE_CLIENT_CLASS,
                SPRING_BEAN_NAME,
                AceEmailLookupService.class,
                NO_OP
        );
    }
}
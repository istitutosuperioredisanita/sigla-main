//package it.cnr.contab.inventario01.service;
//
//import it.iss.si.dto.anagrafica.Contatto;
//import it.iss.si.dto.anagrafica.EmployeeDetails;
//import it.iss.si.service.AceService;
//import org.springframework.context.annotation.Profile;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//@Profile("iss")
//public class DocTraspRientAceEmailLookupService {
//
//    private static final String TIPO_CONTATTO_MAIL = "Mail";
//
//    private final AceService aceService;
//
//    public DocTraspRientAceEmailLookupService(AceService aceService) {
//        this.aceService = aceService;
//    }
//
//    /**
//     * Recupera l'email istituzionale da ACE partendo dal codice fiscale.
//     */
//    public String getEmailByCodiceFiscale(String codiceFiscale) {
//
//        if (codiceFiscale == null || codiceFiscale.trim().isEmpty()) {
//            throw new IllegalArgumentException("Codice fiscale non valorizzato");
//        }
//
//        String cf = codiceFiscale.trim().toUpperCase();
//
//        EmployeeDetails persona = aceService.getPersonaByCodiceFiscale(cf);
//
//        if (persona == null) {
//            throw new IllegalStateException(
//                    "Nessuna persona trovata su ACE per codice fiscale: " + cf
//            );
//        }
//
//        String email = estraiEmailDaPersonaAce(persona);
//
//        if (email == null || email.trim().isEmpty()) {
//            throw new IllegalStateException(
//                    "ACE non ha restituito alcuna email per il codice fiscale " + cf
//            );
//        }
//
//        return email.trim().toLowerCase();
//    }
//
//    private String estraiEmailDaPersonaAce(EmployeeDetails persona) {
//
//        if (persona == null || persona.getContatti() == null) {
//            return null;
//        }
//
//        List<Contatto> contatti = persona.getContatti();
//
//        for (Contatto contatto : contatti) {
//            if (contatto == null) {
//                continue;
//            }
//
//            String tipoContatto = contatto.getTipoContatto();
//            String valore = contatto.getValore();
//
//            if (isTipoEmail(tipoContatto)
//                    && valore != null
//                    && !valore.trim().isEmpty()) {
//                return valore.trim();
//            }
//        }
//
//        return null;
//    }
//
//    private boolean isTipoEmail(String tipoContatto) {
//        return TIPO_CONTATTO_MAIL.equalsIgnoreCase(tipoContatto)
//                || "MAIL".equalsIgnoreCase(tipoContatto)
//                || "EMAIL".equalsIgnoreCase(tipoContatto);
//    }
//}
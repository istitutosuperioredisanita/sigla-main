package it.cnr.contab.inventario01.service;

import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.jada.comp.ComponentException;
import it.iss.si.dto.anagrafica.Contatto;
import it.iss.si.dto.anagrafica.EmployeeDetails;
import it.iss.si.service.AceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DocTraspRientAceEmailLookupService implements AceEmailLookupService {

    private static final Logger log =
            LoggerFactory.getLogger(DocTraspRientAceEmailLookupService.class);

    private static final String TIPO_CONTATTO_MAIL = "Mail";

    private final AceService aceService;

    public DocTraspRientAceEmailLookupService(AceService aceService) {
        this.aceService = aceService;
    }

    @Override
    public String getEmailByTerzo(TerzoBulk terzo) throws ComponentException {
        try {
            if (terzo == null) {
                throw new ComponentException("Terzo firmatario non valorizzato.");
            }

            String codiceFiscale = getCodiceFiscale(terzo);

            EmployeeDetails persona =
                    aceService.getPersonaByCodiceFiscale(codiceFiscale);

            String email = estraiEmailDaPersonaAce(persona);

            if (email == null || email.trim().isEmpty()) {
                throw new ComponentException(
                        "ACE non ha restituito alcuna email per il codice fiscale " + codiceFiscale
                );
            }

            log.info(
                    "Email firmatario recuperata da ACE: terzo={}, codiceFiscale={}, email={}",
                    terzo.getCd_terzo(),
                    codiceFiscale,
                    email
            );

            return email.trim().toLowerCase();

        } catch (ComponentException e) {
            throw e;

        } catch (Exception e) {
            throw new ComponentException(
                    "Errore recupero email firmatario da ACE: " + e.getMessage(),
                    e
            );
        }
    }

    private String getCodiceFiscale(TerzoBulk terzo) throws ComponentException {
        if (terzo == null) {
            throw new ComponentException("Terzo firmatario non valorizzato.");
        }

        if (terzo.getAnagrafico() == null) {
            throw new ComponentException(
                    "Anagrafico non valorizzato per il terzo " + terzo.getCd_terzo()
            );
        }

        String codiceFiscale = terzo.getAnagrafico().getCodice_fiscale();

        if (codiceFiscale == null || codiceFiscale.trim().isEmpty()) {
            throw new ComponentException(
                    "Codice fiscale non valorizzato per il terzo " + terzo.getCd_terzo()
            );
        }

        return codiceFiscale.trim().toUpperCase();
    }

    private String estraiEmailDaPersonaAce(EmployeeDetails persona) {
        if (persona == null || persona.getContatti() == null) {
            return null;
        }

        for (Contatto contatto : persona.getContatti()) {
            if (contatto == null) {
                continue;
            }

            String tipoContatto = contatto.getTipoContatto();
            String valore = contatto.getValore();

            if (TIPO_CONTATTO_MAIL.equalsIgnoreCase(tipoContatto)
                    && valore != null
                    && !valore.trim().isEmpty()) {
                return valore.trim();
            }
        }

        return null;
    }
}
package it.cnr.contab.doccont00.dto;

import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.CRUDValidationException;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class EsitoCori {
    public final Set<String> codiciNonEsistenti = new LinkedHashSet<>();
    public final Set<String> codiciSenzaGruppo = new LinkedHashSet<>();
    public final Set<String> codiciSenzaCapitolo = new LinkedHashSet<>();
    public final Set<String> codiciSenzaTerzo = new LinkedHashSet<>();
    public final Set<String> codiciTerziModalitaPagamento = new LinkedHashSet<>(); // OK terzo+MP

    public boolean isOk() {
        return codiciNonEsistenti.isEmpty()
                && codiciSenzaGruppo.isEmpty()
                && codiciSenzaCapitolo.isEmpty()
                && codiciSenzaTerzo.isEmpty();
    }

    public String buildErrorMessage() {
        Map<String, Set<String>> checks = new LinkedHashMap<>();
        checks.put("I codici di ritenuta non sono presenti", codiciNonEsistenti);
        checks.put("I codici di ritenuta non hanno un gruppo", codiciSenzaGruppo);
        checks.put("I codici di ritenuta non hanno una voce di bilancio associata", codiciSenzaCapitolo);
        checks.put("I codici di ritenuta non non hanno un terzo associato", codiciSenzaTerzo);

        String details = checks.entrySet().stream()
                .filter(e -> !e.getValue().isEmpty())
                .map(e -> "- " + e.getKey() + ": " + e.getValue())
                .collect(Collectors.joining("\n"));

        return "CONFIGURAZIONE E CHECK INCOMPLETI\n" + details;
    }

    /**
     * Lancia ApplicationException con messaggio formattato se NON ok
     */
    public void gestioneNoOk() throws ApplicationException {
        if (!isOk()) {
            throw new CRUDValidationException(buildErrorMessage());
        }
    }
}

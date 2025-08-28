package it.cnr.contab.doccont00.dto;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * DTO per validazioni stipendi_cofi_cori
 */
public class EsitoCori {
    public final Set<String> codiciNonEsistenti          = new LinkedHashSet<>();
    public final Set<String> codiciSenzaGruppo           = new LinkedHashSet<>();
    public final Set<String> codiciSenzaCapitolo         = new LinkedHashSet<>();
    public final Set<String> codiciSenzaTerzo            = new LinkedHashSet<>();
    public final Set<String> codiciTerziModalitaPagamento= new LinkedHashSet<>(); // elenco “OK” terzo+MP
    public boolean isOk() {
        return codiciNonEsistenti.isEmpty()
                && codiciSenzaGruppo.isEmpty()
                && codiciSenzaCapitolo.isEmpty()
                && codiciSenzaTerzo.isEmpty();
    }
}
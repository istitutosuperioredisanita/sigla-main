package it.cnr.contab.coepcoan00.core.bulk;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Sezione {
    D("Dare"), A("Avere");
    private final String label;

    private Sezione(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static Dictionary<String, String> KEYS() {
        return Stream.of(Sezione.values())
            .collect(Collectors.toMap(
                    Sezione::name,
                    Sezione::label,
                    (oldValue, newValue) -> oldValue, Hashtable::new)
            );
    }
}

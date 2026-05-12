package it.cnr.contab.util00.bulk;

import it.cnr.contab.util.Utility;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.util.OrderedHashtable;
import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MeseBulk extends OggettoBulk {

    private Integer mese;

    public Integer getMese() {
        return mese;
    }

    public void setMese(Integer mese) {
        this.mese = mese;
    }

    public static final Map<Integer, String> meseKeys =
            Calendar
                .getInstance()
                .getDisplayNames(Calendar.MONTH, Calendar.LONG, Locale.ITALIAN)
                .entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(
                        s -> s.getValue() + 1, s -> StringUtils.capitalize(s.getKey()),
                        (u, v) -> {
                            throw new IllegalStateException(
                                    String.format("Cannot have 2 values (%s, %s) for the same key", u, v)
                            );
                        }, HashMap::new
            ));

}

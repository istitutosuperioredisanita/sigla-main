package it.cnr.contab.docamm00.docs.bulk;

import java.util.Arrays;

public enum TipoContoDocAttivoEnum {
    ACC("ACC", "Documento in conto acconto"), ANT("ANT", "Documento in conto anticipo");

    private final String value;
    private final String label;

    public final static java.util.Dictionary ti_tipoContoDocAttivoEnumKeys = new it.cnr.jada.util.OrderedHashtable();
    static {
        Arrays.asList(TipoContoDocAttivoEnum.values()).stream().forEach(tipoContoDocAttivoEnum -> {
            ti_tipoContoDocAttivoEnumKeys.put(tipoContoDocAttivoEnum.value(), tipoContoDocAttivoEnum.label());
        });
    }

    TipoContoDocAttivoEnum(String value, String label) {
        this.value = value;
        this.label = label;
    }

    public String value() {
        return value;
    }

    public String label() {
        return label;
    }
}

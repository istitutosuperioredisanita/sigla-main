package it.cnr.contab.pdg00.cdip.bulk;

import it.cnr.contab.util00.bulk.storage.AllegatoParentIBulk;
import java.util.Dictionary;

public class CaricFlStipBulk extends AllegatoParentIBulk {
    private String tipo_rapporto;

    public String getTipo_rapporto() { return tipo_rapporto; }
    public void setTipo_rapporto(String tipo_rapporto) { this.tipo_rapporto = tipo_rapporto; }

    public static final String DIPENDENTE = "D";
    public static final String COLLABORATORE_COORD_E_CONT = "C";

    public static final Dictionary tipo_rapportoKeys;
    static {
        tipo_rapportoKeys = new it.cnr.jada.util.OrderedHashtable();
        tipo_rapportoKeys.put(DIPENDENTE, "Dipendente");
        tipo_rapportoKeys.put(COLLABORATORE_COORD_E_CONT, "Collaboratore");
    }
    public Dictionary getTipo_rapportoKeys() { return tipo_rapportoKeys; }
}

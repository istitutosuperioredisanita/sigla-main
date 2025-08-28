package it.cnr.contab.pdg00.cdip.bulk;

import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.Persistent;

import java.util.Dictionary;
import java.util.Optional;

public class CaricFlStipBulk extends OggettoBulk {

    public CaricFlStipBulk() {
    }

    private String tipo_rapporto;

    public String getTipo_rapporto() {
        return tipo_rapporto;
    }

    public void setTipo_rapporto(String tipo_rapporto) {
        this.tipo_rapporto = tipo_rapporto;
    }


    public final static String DIPENDENTE = "D";
    public final static String COLLABORATORE_COORD_E_CONT = "C";

    public final static Dictionary tipo_rapportoKeys;

    static {
        tipo_rapportoKeys = new it.cnr.jada.util.OrderedHashtable();
        tipo_rapportoKeys.put(DIPENDENTE, "Dipendente");
        tipo_rapportoKeys.put(COLLABORATORE_COORD_E_CONT, "Collaboratore");
    }

    public Dictionary getTipo_rapportoKeys() {
        return tipo_rapportoKeys;
    }
}

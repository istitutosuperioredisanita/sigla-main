package it.cnr.contab.pdg00.cdip.bulk;

import it.cnr.contab.anagraf00.tabrif.bulk.Tipo_rapportoBulk;
import it.cnr.contab.util00.bulk.MeseBulk;
import org.apache.commons.lang.StringUtils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class MeseTipoFlussoBulk extends MeseBulk {
    private String tipoFlusso;

    public static final Map<String, String> tipoFlussoKeys = new HashMap<>();

    static {
        tipoFlussoKeys.put(Tipo_rapportoBulk.DIPENDENTE,"Dipendenti");
        tipoFlussoKeys.put(Tipo_rapportoBulk.COLLABORATORE_COORD_E_CONT,"Collaboratori");
    }

    public String getTipoFlusso() {
        return tipoFlusso;
    }

    public void setTipoFlusso(String tipoFlusso) {
        this.tipoFlusso = tipoFlusso;
    }

    public final Map<String, String> getTipoFlussoKeys() {
        return tipoFlussoKeys;
    }
}

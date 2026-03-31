package it.cnr.contab.doccont00.tabrif.bulk;

import it.cnr.contab.config00.latt.bulk.CostantiTi_gestione;
import it.cnr.contab.config00.pdcfin.bulk.V_voce_f_partita_giroBulk;
import it.cnr.contab.doccont00.core.bulk.Mandato_rigaBulk;
import it.cnr.contab.doccont00.core.bulk.Reversale_rigaBulk;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.bulk.SimpleBulkList;

import java.util.HashMap;
import java.util.Map;

public class CambiaVocePGiroBulk extends OggettoBulk {
    public final static Map<String, String> tiGestioneKeys = new HashMap<String, String>() {{
        put(CostantiTi_gestione.TI_GESTIONE_SPESE, "Spesa");
        put(CostantiTi_gestione.TI_GESTIONE_ENTRATE, "Entrata");
    }};

    private String tiGestione;
    private V_voce_f_partita_giroBulk voceIniziale;
    private V_voce_f_partita_giroBulk voceFinale;

    private SimpleBulkList<Reversale_rigaBulk> dettagliEntrata = new SimpleBulkList();
    private SimpleBulkList<Mandato_rigaBulk> dettagliSpesa = new SimpleBulkList();

    public CambiaVocePGiroBulk() {
    }

    public String getTiGestione() {
        return tiGestione;
    }

    public void setTiGestione(String tiGestione) {
        this.tiGestione = tiGestione;
    }

    public V_voce_f_partita_giroBulk getVoceIniziale() {
        return voceIniziale;
    }

    public void setVoceIniziale(V_voce_f_partita_giroBulk voceIniziale) {
        this.voceIniziale = voceIniziale;
    }

    public V_voce_f_partita_giroBulk getVoceFinale() {
        return voceFinale;
    }

    public void setVoceFinale(V_voce_f_partita_giroBulk voceFinale) {
        this.voceFinale = voceFinale;
    }

    public SimpleBulkList<Reversale_rigaBulk> getDettagliEntrata() {
        return dettagliEntrata;
    }

    public void setDettagliEntrata(SimpleBulkList<Reversale_rigaBulk> dettagliEntrata) {
        this.dettagliEntrata = dettagliEntrata;
    }

    public SimpleBulkList<Mandato_rigaBulk> getDettagliSpesa() {
        return dettagliSpesa;
    }

    public void setDettagliSpesa(SimpleBulkList<Mandato_rigaBulk> dettagliSpesa) {
        this.dettagliSpesa = dettagliSpesa;
    }

    public Reversale_rigaBulk removeFromDettagliEntrata(int indiceDiLinea ) {
        return dettagliEntrata.remove(indiceDiLinea);
    }

    public int addToDettagliEntrata(Reversale_rigaBulk reversaleRigaBulk)    {
        getDettagliEntrata().add(reversaleRigaBulk);
        return getDettagliEntrata().size()-1;
    }
    public Mandato_rigaBulk removeFromDettagliSpesa(int indiceDiLinea ) {
        return dettagliSpesa.remove(indiceDiLinea);
    }

    public int addToDettagliSpesa(Mandato_rigaBulk mandatoRigaBulk)    {
        getDettagliSpesa().add(mandatoRigaBulk);
        return getDettagliSpesa().size()-1;
    }

}

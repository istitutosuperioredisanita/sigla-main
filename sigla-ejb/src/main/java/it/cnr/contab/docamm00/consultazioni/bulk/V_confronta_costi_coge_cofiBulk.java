/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 16/06/2025
 */
package it.cnr.contab.docamm00.consultazioni.bulk;

import it.cnr.jada.action.ActionContext;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.util.action.CRUDBP;

import java.util.Dictionary;

public class V_confronta_costi_coge_cofiBulk extends V_confronta_costi_coge_cofiBase {
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: V_CONFRONTA_COSTI_COGE_COFI
	 **/

	public final static String VISUALIZZAZIONE_DETTAGLI = "D";
	public final static String VISUALIZZAZIONE_TOTALI="T";
	public final static String VISUALIZZAZIONE_COMPLETA="C";


	public V_confronta_costi_coge_cofiBulk() {
		super();
	}

	public V_confronta_costi_coge_cofiBulk(Integer esercizio,String voceCoge,String voceCofi) {
		super(esercizio,voceCoge,voceCofi);
	}

	public final static Dictionary tipo_visualizzazioneKeys;
	private java.lang.String tipo_visualizzazione;

	static {
		tipo_visualizzazioneKeys = new it.cnr.jada.util.OrderedHashtable();
		tipo_visualizzazioneKeys.put(VISUALIZZAZIONE_DETTAGLI, "Dettagli");
		tipo_visualizzazioneKeys.put(VISUALIZZAZIONE_TOTALI, "Totali");
		tipo_visualizzazioneKeys.put(VISUALIZZAZIONE_COMPLETA, "TUTTI");


	}

	public String getTipo_visualizzazione() {
		return tipo_visualizzazione;
	}

	public void setTipo_visualizzazione(String tipo_visualizzazione) {
		this.tipo_visualizzazione = tipo_visualizzazione;
	}


	@Override
	public OggettoBulk initializeForSearch(CRUDBP crudbp, ActionContext actioncontext) {
		this.setTipo_visualizzazione(VISUALIZZAZIONE_DETTAGLI);
		return super.initializeForSearch(crudbp, actioncontext);
	}
}
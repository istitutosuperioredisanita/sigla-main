/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 24/03/2026
 */
package it.cnr.contab.config00.pdcep.bulk;

import it.cnr.contab.coepcoan00.core.bulk.Sezione;

import java.util.Dictionary;
import java.util.List;

public class BilRiclassificatoBulk extends BilRiclassificatoBase {
	public static Dictionary<String, String> tiSezioneKeys = Sezione.KEYS();
	/**
	 * [VOCE_EP ]
	 **/
	private ContoBulk voceEp =  new ContoBulk();

	private List<TipoBilancioBulk> tipoBilanci;

	private TipoBilancioBulk tipo_bilancio;
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: BIL_RICLASSIFICATO
	 **/
	public BilRiclassificatoBulk() {
		super();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: BIL_RICLASSIFICATO
	 **/
	public BilRiclassificatoBulk(Integer esercizio, String cdUnitaOrganizzativa, String cdTipoBilancio, String cdVoceEp) {
		super(esercizio, cdUnitaOrganizzativa, cdTipoBilancio, cdVoceEp);
		setVoceEp( new ContoBulk(cdVoceEp, esercizio));
	}
	public ContoBulk getVoceEp() {
		return voceEp;
	}
	public void setVoceEp(ContoBulk voceEp)  {
		this.voceEp=voceEp;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Esercizio]
	 **/
	public Integer getEsercizio() {
		Voce_epBulk voceEp = this.getVoceEp();
		if (voceEp == null)
			return null;
		return getVoceEp().getEsercizio();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Esercizio]
	 **/
	public void setEsercizio(Integer esercizio)  {
		this.getVoceEp().setEsercizio(esercizio);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice Identificativo completo del conto del piano.]
	 **/
	public String getCdVoceEp() {
		Voce_epBulk voceEp = this.getVoceEp();
		if (voceEp == null)
			return null;
		return getVoceEp().getCd_voce_ep();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice Identificativo completo del conto del piano.]
	 **/
	public void setCdVoceEp(String cdVoceEp)  {
		this.getVoceEp().setCd_voce_ep(cdVoceEp);
	}

	public List<TipoBilancioBulk> getTipoBilanci() {
		return tipoBilanci;
	}

	public void setTipoBilanci(List<TipoBilancioBulk> tipoBilanci) {
		this.tipoBilanci = tipoBilanci;
	}

	public TipoBilancioBulk getTipo_bilancio() {
		return tipo_bilancio;
	}

	public void setTipo_bilancio(TipoBilancioBulk tipo_bilancio) {
		this.tipo_bilancio = tipo_bilancio;
	}
}

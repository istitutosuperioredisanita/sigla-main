/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 10/01/2025
 */
package it.cnr.contab.config00.bulk;
import it.cnr.contab.coepcoan00.core.bulk.Sezione;
import it.cnr.contab.config00.pdcep.bulk.Voce_epBulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.util.action.CRUDBP;

import java.util.Dictionary;
import java.util.Optional;

public class AssCausaleVoceEPBulk extends AssCausaleVoceEPBase {
	/**
	 * [CAUSALE_CONTABILE ]
	 **/
	private CausaleContabileBulk causaleContabile =  new CausaleContabileBulk();
	/**
	 * [VOCE_EP ]
	 **/
	private Voce_epBulk voceEp =  new Voce_epBulk();
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: ASS_CAUSALE_VOCE_EP
	 **/
	public AssCausaleVoceEPBulk() {
		super();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: ASS_CAUSALE_VOCE_EP
	 **/
	public static Dictionary<String, String> tiSezioneKeys = Sezione.KEYS();
	public AssCausaleVoceEPBulk(String cdCausale, Integer esercizio, String cdVoceEp) {
		super(cdCausale, esercizio, cdVoceEp);
		setCausaleContabile( new CausaleContabileBulk(cdCausale) );
		setVoceEp( new Voce_epBulk(cdVoceEp, esercizio) );
	}
	public CausaleContabileBulk getCausaleContabile() {
		return causaleContabile;
	}
	public void setCausaleContabile(CausaleContabileBulk causaleContabile)  {
		this.causaleContabile=causaleContabile;
	}
	public Voce_epBulk getVoceEp() {
		return voceEp;
	}
	public void setVoceEp(Voce_epBulk voceEp)  {
		this.voceEp = voceEp;
		Optional.ofNullable(voceEp)
			.filter(voceEpBulk -> !voceEpBulk.isContoSezioneBifase())
			.ifPresent(voceEpBulk -> setTiSezione(voceEpBulk.getTi_sezione()));
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice causale]
	 **/
	public String getCdCausale() {
		CausaleContabileBulk causaleContabile = this.getCausaleContabile();
		if (causaleContabile == null)
			return null;
		return getCausaleContabile().getCdCausale();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice causale]
	 **/
	public void setCdCausale(String cdCausale)  {
		this.getCausaleContabile().setCdCausale(cdCausale);
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

	public boolean isSezioneEnabled() {
		return !Optional.ofNullable(getVoceEp())
				.map(Voce_epBulk::isContoSezioneBifase)
				.orElse(Boolean.FALSE);
	}
}
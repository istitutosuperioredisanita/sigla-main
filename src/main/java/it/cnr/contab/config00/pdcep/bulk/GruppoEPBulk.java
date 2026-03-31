/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 27/03/2024
 */
package it.cnr.contab.config00.pdcep.bulk;

import java.util.Collection;
import java.util.Optional;

public class GruppoEPBulk extends GruppoEPBase {

	private TipoBilancioBulk tipoBilancio;
	private java.util.Collection<TipoBilancioBulk> tipoBilanci;
	public final static java.util.Dictionary ti_segnoKeys = AssociazioneContoGruppoBulk.ti_segnoKeys;
	public final static java.util.Dictionary ti_pianoGruppiKeys = AssociazioneContoGruppoBulk.ti_pianoGruppiKeys;
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: CNR_GRUPPO_EP
	 **/
	public GruppoEPBulk() {
		super();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: CNR_GRUPPO_EP
	 **/
	public GruppoEPBulk(String rowid) {
		super(rowid);
	}

	public TipoBilancioBulk getTipoBilancio() {
		return tipoBilancio;
	}

	public void setTipoBilancio(TipoBilancioBulk tipoBilancio) {
		this.tipoBilancio = tipoBilancio;
	}

	@Override
	public void setCdTipoBilancio(String cd_tipo_bilancio) {
		Optional.ofNullable(tipoBilancio).ifPresent(
				tipoBilancioBulk -> tipoBilancioBulk.setCdTipoBilancio(cd_tipo_bilancio)
		);
	}

	@Override
	public String getCdTipoBilancio() {
		return Optional.ofNullable(tipoBilancio)
				.map(TipoBilancioKey::getCdTipoBilancio)
				.orElse(null);
	}

	public Collection<TipoBilancioBulk> getTipoBilanci() {
		return tipoBilanci;
	}

	public void setTipoBilanci(Collection<TipoBilancioBulk> tipoBilanci) {
		this.tipoBilanci = tipoBilanci;
	}
}
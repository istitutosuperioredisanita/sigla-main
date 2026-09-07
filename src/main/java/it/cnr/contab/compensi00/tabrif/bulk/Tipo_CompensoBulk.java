/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 27/05/2026
 */
package it.cnr.contab.compensi00.tabrif.bulk;

import it.cnr.contab.compensi00.docs.bulk.CompensoBulk;
import it.cnr.jada.action.BusinessProcessException;

import java.util.Optional;

public class Tipo_CompensoBulk extends Tipo_CompensoBase {
	Tipo_trattamentoBulk tipo_trattamento=new Tipo_trattamentoBulk();

	private java.util.List<Tipo_CompensoBulk> intervalli;

	public java.util.List getIntervalli() {
		return intervalli;
	}

	/**
	 * Insert the method's description here.
	 * Creation date: (04/06/2002 14.35.49)
	 * @param newIntervalli java.util.List
	 */
	public void setIntervalli(java.util.List newIntervalli) {
		intervalli = newIntervalli;
	}

	public Tipo_trattamentoBulk getTipo_trattamento() {
		return tipo_trattamento;
	}

	public void setTipo_trattamento(Tipo_trattamentoBulk tipo_trattamento) {
		this.tipo_trattamento = tipo_trattamento;
	}

	@Override
	public String getCdTrattamento() {
		if (Optional.ofNullable(tipo_trattamento).isPresent())
			return tipo_trattamento.getCd_trattamento();
		return null;
	}

	@Override
	public void setCdTrattamento(String cdTrattamento) {
		tipo_trattamento.setCd_trattamento(cdTrattamento);

	}

	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: TIPO_COMPENSO
	 **/
	public Tipo_CompensoBulk() {
		super();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: TIPO_COMPENSO
	 **/
	public Tipo_CompensoBulk(String cdTrattamento, String cdTiCompenso, java.sql.Timestamp dtInizioValidita) {
		super(cdTrattamento, cdTiCompenso, dtInizioValidita);
	}
	public boolean isROCheck() {

		return Optional.ofNullable(this.getDtFineValidita())
						.map(el-> {
                            try {
                                return !(el.compareTo(CompensoBulk.getDataOdierna())>0);
                            } catch (BusinessProcessException e) {
                                throw new RuntimeException(e);
                            }
                        })
						.orElse(Boolean.FALSE);
	}
	public boolean isROTipoTrattamento() {
		if ( Optional.ofNullable(getDtInizioValidita()).isPresent())
			return Boolean.FALSE;
		return Boolean.TRUE;
	}
}
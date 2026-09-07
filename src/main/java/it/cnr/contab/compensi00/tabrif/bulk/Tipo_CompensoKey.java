/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 27/05/2026
 */
package it.cnr.contab.compensi00.tabrif.bulk;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.KeyedPersistent;
public class Tipo_CompensoKey extends OggettoBulk implements KeyedPersistent {
	private String cdTrattamento;
	private String cdTiCompenso;
	private java.sql.Timestamp dtInizioValidita;
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: TIPO_COMPENSO
	 **/
	public Tipo_CompensoKey() {
		super();
	}
	public Tipo_CompensoKey(String cdTrattamento, String cdTiCompenso, java.sql.Timestamp dtInizioValidita) {
		super();
		this.cdTrattamento=cdTrattamento;
		this.cdTiCompenso=cdTiCompenso;
		this.dtInizioValidita=dtInizioValidita;
	}
	public boolean equalsByPrimaryKey(Object o) {
		if (this== o) return true;
		if (!(o instanceof Tipo_CompensoKey)) return false;
		Tipo_CompensoKey k = (Tipo_CompensoKey) o;
		if (!compareKey(getCdTrattamento(), k.getCdTrattamento())) return false;
		if (!compareKey(getCdTiCompenso(), k.getCdTiCompenso())) return false;
		if (!compareKey(getDtInizioValidita(), k.getDtInizioValidita())) return false;
		return true;
	}
	public int primaryKeyHashCode() {
		int i = 0;
		i = i + calculateKeyHashCode(getCdTrattamento());
		i = i + calculateKeyHashCode(getCdTiCompenso());
		i = i + calculateKeyHashCode(getDtInizioValidita());
		return i;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice tipo trattamento prelevato dalla tabella TIPO_TRATTAMENTO. Per ogni cd_ti_trattamento e definito un corrispondente cd_ti_compenso]
	 **/
	public void setCdTrattamento(String cdTrattamento)  {
		this.cdTrattamento=cdTrattamento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice tipo trattamento prelevato dalla tabella TIPO_TRATTAMENTO. Per ogni cd_ti_trattamento e definito un corrispondente cd_ti_compenso]
	 **/
	public String getCdTrattamento() {
		return cdTrattamento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice tipo compenso. Per ogni cd_ti_trattamento e definito un corrispondente cd_ti_compenso]
	 **/
	public void setCdTiCompenso(String cdTiCompenso)  {
		this.cdTiCompenso=cdTiCompenso;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice tipo compenso. Per ogni cd_ti_trattamento e definito un corrispondente cd_ti_compenso]
	 **/
	public String getCdTiCompenso() {
		return cdTiCompenso;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Data inizio validita]
	 **/
	public void setDtInizioValidita(java.sql.Timestamp dtInizioValidita)  {
		this.dtInizioValidita=dtInizioValidita;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Data inizio validita]
	 **/
	public java.sql.Timestamp getDtInizioValidita() {
		return dtInizioValidita;
	}
}
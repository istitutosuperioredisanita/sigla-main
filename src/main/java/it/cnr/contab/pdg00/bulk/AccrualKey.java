/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 04/06/2026
 */
package it.cnr.contab.pdg00.bulk;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.KeyedPersistent;
public class AccrualKey extends OggettoBulk implements KeyedPersistent {
	private Long esercizio;
	private String stato;
	private String esito;
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: ACCRUAL
	 **/
	public AccrualKey() {
		super();
	}
	public AccrualKey(Long esercizio, String stato, String esito) {
		super();
		this.esercizio=esercizio;
		this.stato=stato;
		this.esito=esito;
	}
	public boolean equalsByPrimaryKey(Object o) {
		if (this== o) return true;
		if (!(o instanceof AccrualKey)) return false;
		AccrualKey k = (AccrualKey) o;
		if (!compareKey(getEsercizio(), k.getEsercizio())) return false;
		if (!compareKey(getStato(), k.getStato())) return false;
		if (!compareKey(getEsito(), k.getEsito())) return false;
		return true;
	}
	public int primaryKeyHashCode() {
		int i = 0;
		i = i + calculateKeyHashCode(getEsercizio());
		i = i + calculateKeyHashCode(getStato());
		i = i + calculateKeyHashCode(getEsito());
		return i;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Esercizio di riferimento]
	 **/
	public void setEsercizio(Long esercizio)  {
		this.esercizio=esercizio;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Esercizio di riferimento]
	 **/
	public Long getEsercizio() {
		return esercizio;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Stato del flusso Accrual MEF. Dominio: INS = Inserito, PRE = Predisposto, INV = Inviato]
	 **/
	public void setStato(String stato)  {
		this.stato=stato;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Stato del flusso Accrual MEF. Dominio: INS = Inserito, PRE = Predisposto, INV = Inviato]
	 **/
	public String getStato() {
		return stato;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Esito del flusso Accrual MEF]
	 **/
	public void setEsito(String esito)  {
		this.esito=esito;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Esito del flusso Accrual MEF]
	 **/
	public String getEsito() {
		return esito;
	}
}
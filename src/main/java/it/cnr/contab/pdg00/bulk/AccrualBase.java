/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 04/06/2026
 */
package it.cnr.contab.pdg00.bulk;
import it.cnr.jada.persistency.Keyed;
public class AccrualBase extends AccrualKey implements Keyed {
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: ACCRUAL
	 **/
	private String stato;
	private String esito;

	public AccrualBase() {
		super();
	}
	public AccrualBase(Integer esercizio, String stato, String esito) {
		super(esercizio);
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
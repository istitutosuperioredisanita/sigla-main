/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 10/10/2025
 */
package it.cnr.contab.inventario00.docs.bulk;
import it.cnr.jada.persistency.Keyed;
public class Numeratore_doc_t_rBase extends Numeratore_doc_t_rKey implements Keyed {
//    INIZIALE DECIMAL(38,0) NOT NULL
	private Long iniziale;
 
//    CORRENTE DECIMAL(38,0) NOT NULL
	private Long corrente;
 
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: NUMERATORE_DOC_T_R
	 **/
	public Numeratore_doc_t_rBase() {
		super();
	}
	public Numeratore_doc_t_rBase(Long pgInventario, String tiTrasportoRientro, Integer esercizio) {
		super(pgInventario, tiTrasportoRientro, esercizio);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Numero iniziale della numerazione]
	 **/
	public Long getIniziale() {
		return iniziale;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Numero iniziale della numerazione]
	 **/
	public void setIniziale(Long iniziale)  {
		this.iniziale=iniziale;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Numero corrente della numerazione]
	 **/
	public Long getCorrente() {
		return corrente;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Numero corrente della numerazione]
	 **/
	public void setCorrente(Long corrente)  {
		this.corrente=corrente;
	}
}
/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 27/02/2024
 */
package it.cnr.contab.ordmag.magazzino.bulk;
import it.cnr.jada.persistency.Keyed;

import java.math.BigDecimal;
import java.util.Date;

public class ChiusuraAnnoBase extends ChiusuraAnnoKey implements Keyed {
//    STATO DECIMAL(5,0) NOT NULL
	private String stato;

	private Date dataCalcolo;

	private BigDecimal pg_job;
	private String stato_job;

 
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: CHIUSURA_ANNO
	 **/
	public ChiusuraAnnoBase() {
		super();
	}
	public ChiusuraAnnoBase(Integer pgChiusura, Integer anno, String tipoChiusura) {
		super(pgChiusura, anno, tipoChiusura);
	}

	public String getStato() {
		return stato;
	}

	public void setStato(String stato) {
		this.stato = stato;
	}

	public Date getDataCalcolo() {
		return dataCalcolo;
	}

	public void setDataCalcolo(Date dataCalcolo) {
		this.dataCalcolo = dataCalcolo;
	}

	public BigDecimal getPg_job() {
		return pg_job;
	}

	public void setPg_job(BigDecimal pg_job) {
		this.pg_job = pg_job;
	}

	public String getStato_job() {
		return stato_job;
	}

	public void setStato_job(String stato_job) {
		this.stato_job = stato_job;
	}
}
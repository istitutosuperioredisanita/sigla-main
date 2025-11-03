/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 31/10/2025
 */
package it.cnr.contab.inventario01.bulk;

import it.cnr.jada.persistency.Keyed;

public class Doc_trasporto_rientro_dettBase extends Doc_trasporto_rientro_dettKey implements Keyed {

	// Campi di riferimento (dopo la PK)
	private String tiDocumentoRif;
	private Integer esercizioRif;
	private Long pgDocTrasportoRientroRif;

	// Altri campi
	private String intervallo;
	private Long quantita;
	private java.sql.Timestamp dataEffettivaMovimentazione;

	/**
	 * Table name: DOC_TRASPORTO_RIENTRO_DETT
	 **/
	public Doc_trasporto_rientro_dettBase() {
		super();
	}

	public Doc_trasporto_rientro_dettBase(Long pgInventario, String tiDocumento, Integer esercizio,
										  Long pgDocTrasportoRientro, Long nrInventario, Integer progressivo) {
		super(pgInventario, tiDocumento, esercizio, pgDocTrasportoRientro, nrInventario, progressivo);
	}

	// ===============================
	// CAMPI DI RIFERIMENTO
	// ===============================
	public String getTiDocumentoRif() { return tiDocumentoRif; }
	public void setTiDocumentoRif(String tiDocumentoRif) { this.tiDocumentoRif = tiDocumentoRif; }

	public Integer getEsercizioRif() { return esercizioRif; }
	public void setEsercizioRif(Integer esercizioRif) { this.esercizioRif = esercizioRif; }

	public Long getPgDocTrasportoRientroRif() { return pgDocTrasportoRientroRif; }
	public void setPgDocTrasportoRientroRif(Long pgDocTrasportoRientroRif) { this.pgDocTrasportoRientroRif = pgDocTrasportoRientroRif; }

	// ===============================
	// ALTRI CAMPI
	// ===============================
	public String getIntervallo() { return intervallo; }
	public void setIntervallo(String intervallo) { this.intervallo = intervallo; }

	public Long getQuantita() { return quantita; }
	public void setQuantita(Long quantita) { this.quantita = quantita; }

	public java.sql.Timestamp getDataEffettivaMovimentazione() { return dataEffettivaMovimentazione; }
	public void setDataEffettivaMovimentazione(java.sql.Timestamp dataEffettivaMovimentazione) { this.dataEffettivaMovimentazione = dataEffettivaMovimentazione; }

	public java.sql.Timestamp getDacr() { return dacr; }
	public void setDacr(java.sql.Timestamp dacr) { this.dacr = dacr; }

	public String getUtcr() { return utcr; }
	public void setUtcr(String utcr) { this.utcr = utcr; }

	public java.sql.Timestamp getDuva() { return duva; }
	public void setDuva(java.sql.Timestamp duva) { this.duva = duva; }

	public String getUtuv() { return utuv; }
	public void setUtuv(String utuv) { this.utuv = utuv; }
}

/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 31/10/2025
 */
package it.cnr.contab.inventario01.bulk;

import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.KeyedPersistent;

public class Doc_trasporto_rientro_dettKey extends OggettoBulk implements KeyedPersistent {

	private Long pgInventario;
	private String tiDocumento;
	private Integer esercizio;
	private Long pgDocTrasportoRientro;
	private Long nrInventario;
	private Integer progressivo;

	/**
	 * Table name: DOC_TRASPORTO_RIENTRO_DETT
	 **/
	public Doc_trasporto_rientro_dettKey() {
		super();
	}

	public Doc_trasporto_rientro_dettKey(Long pgInventario, String tiDocumento, Integer esercizio,
										 Long pgDocTrasportoRientro, Long nrInventario, Integer progressivo) {
		super();
		this.pgInventario = pgInventario;
		this.tiDocumento = tiDocumento;
		this.esercizio = esercizio;
		this.pgDocTrasportoRientro = pgDocTrasportoRientro;
		this.nrInventario = nrInventario;
		this.progressivo = progressivo;
	}

	public boolean equalsByPrimaryKey(Object o) {
		if (this == o) return true;
		if (!(o instanceof Doc_trasporto_rientro_dettKey)) return false;
		Doc_trasporto_rientro_dettKey k = (Doc_trasporto_rientro_dettKey) o;
		if (!compareKey(getPgInventario(), k.getPgInventario())) return false;
		if (!compareKey(getTiDocumento(), k.getTiDocumento())) return false;
		if (!compareKey(getEsercizio(), k.getEsercizio())) return false;
		if (!compareKey(getPgDocTrasportoRientro(), k.getPgDocTrasportoRientro())) return false;
		if (!compareKey(getNrInventario(), k.getNrInventario())) return false;
		if (!compareKey(getProgressivo(), k.getProgressivo())) return false;
		return true;
	}

	public int primaryKeyHashCode() {
		int i = 0;
		i += calculateKeyHashCode(getPgInventario());
		i += calculateKeyHashCode(getTiDocumento());
		i += calculateKeyHashCode(getEsercizio());
		i += calculateKeyHashCode(getPgDocTrasportoRientro());
		i += calculateKeyHashCode(getNrInventario());
		i += calculateKeyHashCode(getProgressivo());
		return i;
	}

	public void setPgInventario(Long pgInventario) { this.pgInventario = pgInventario; }
	public Long getPgInventario() { return pgInventario; }

	public void setTiDocumento(String tiDocumento) { this.tiDocumento = tiDocumento; }
	public String getTiDocumento() { return tiDocumento; }

	public void setEsercizio(Integer esercizio) { this.esercizio = esercizio; }
	public Integer getEsercizio() { return esercizio; }

	public void setPgDocTrasportoRientro(Long pgDocTrasportoRientro) { this.pgDocTrasportoRientro = pgDocTrasportoRientro; }
	public Long getPgDocTrasportoRientro() { return pgDocTrasportoRientro; }

	public void setNrInventario(Long nrInventario) { this.nrInventario = nrInventario; }
	public Long getNrInventario() { return nrInventario; }

	public void setProgressivo(Integer progressivo) { this.progressivo = progressivo; }
	public Integer getProgressivo() { return progressivo; }
}

/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 20/11/2024
 */
package it.cnr.contab.inventario00.docs.bulk;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.KeyedPersistent;
public class Ammortamento_bene_invKey extends OggettoBulk implements KeyedPersistent {
	private Long pgInventario;
	private Long nrInventario;
	private Long progressivo;
	private Integer esercizio;
	private Integer pgRiga;
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: AMMORTAMENTO_BENE_INV
	 **/
	public Ammortamento_bene_invKey() {
		super();
	}
	public Ammortamento_bene_invKey(Long pgInventario, Long nrInventario, Long progressivo, Integer esercizio, Integer pgRiga) {
		super();
		this.pgInventario=pgInventario;
		this.nrInventario=nrInventario;
		this.progressivo=progressivo;
		this.esercizio=esercizio;
		this.pgRiga=pgRiga;
	}
	public boolean equalsByPrimaryKey(Object o) {
		if (this== o) return true;
		if (!(o instanceof Ammortamento_bene_invKey)) return false;
		Ammortamento_bene_invKey k = (Ammortamento_bene_invKey) o;
		if (!compareKey(getPgInventario(), k.getPgInventario())) return false;
		if (!compareKey(getNrInventario(), k.getNrInventario())) return false;
		if (!compareKey(getProgressivo(), k.getProgressivo())) return false;
		if (!compareKey(getEsercizio(), k.getEsercizio())) return false;
		if (!compareKey(getPgRiga(), k.getPgRiga())) return false;
		return true;
	}
	public int primaryKeyHashCode() {
		int i = 0;
		i = i + calculateKeyHashCode(getPgInventario());
		i = i + calculateKeyHashCode(getNrInventario());
		i = i + calculateKeyHashCode(getProgressivo());
		i = i + calculateKeyHashCode(getEsercizio());
		i = i + calculateKeyHashCode(getPgRiga());
		return i;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice del'inventario]
	 **/
	public void setPgInventario(Long pgInventario)  {
		this.pgInventario=pgInventario;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice del'inventario]
	 **/
	public Long getPgInventario() {
		return pgInventario;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Numero che identifica il bene all'interno di un inventario]
	 **/
	public void setNrInventario(Long nrInventario)  {
		this.nrInventario=nrInventario;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Numero che identifica il bene all'interno di un inventario]
	 **/
	public Long getNrInventario() {
		return nrInventario;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Numero che identifica il bene accesorio all'interno di un inventario e una volta che risulti noto il bene primario]
	 **/
	public void setProgressivo(Long progressivo)  {
		this.progressivo=progressivo;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Numero che identifica il bene accesorio all'interno di un inventario e una volta che risulti noto il bene primario]
	 **/
	public Long getProgressivo() {
		return progressivo;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Esercizio di riferimento]
	 **/
	public void setEsercizio(Integer esercizio)  {
		this.esercizio=esercizio;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Esercizio di riferimento]
	 **/
	public Integer getEsercizio() {
		return esercizio;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Progressivo aggiunto in chiave per inserire anche più quote di storno per lo stesso anno]
	 **/
	public void setPgRiga(Integer pgRiga)  {
		this.pgRiga=pgRiga;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Progressivo aggiunto in chiave per inserire anche più quote di storno per lo stesso anno]
	 **/
	public Integer getPgRiga() {
		return pgRiga;
	}
}
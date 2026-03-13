/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 10/10/2025
 */
package it.cnr.contab.inventario00.tabrif.bulk;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.KeyedPersistent;
public class Tipo_trasporto_rientroKey extends OggettoBulk implements KeyedPersistent {
	private String cdTipoTrasportoRientro;
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: TIPO_TRASPORTO_RIENTRO
	 **/
	public Tipo_trasporto_rientroKey() {
		super();
	}
	public Tipo_trasporto_rientroKey(String cdTipoTrasportoRientro) {
		super();
		this.cdTipoTrasportoRientro=cdTipoTrasportoRientro;
	}
	public boolean equalsByPrimaryKey(Object o) {
		if (this== o) return true;
		if (!(o instanceof Tipo_trasporto_rientroKey)) return false;
		Tipo_trasporto_rientroKey k = (Tipo_trasporto_rientroKey) o;
		if (!compareKey(getCdTipoTrasportoRientro(), k.getCdTipoTrasportoRientro())) return false;
		return true;
	}
	public int primaryKeyHashCode() {
		int i = 0;
		i = i + calculateKeyHashCode(getCdTipoTrasportoRientro());
		return i;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice del movimento di trasporto/rientro di inventario]
	 **/
	public void setCdTipoTrasportoRientro(String cdTipoTrasportoRientro)  {
		this.cdTipoTrasportoRientro=cdTipoTrasportoRientro;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice del movimento di trasporto/rientro di inventario]
	 **/
	public String getCdTipoTrasportoRientro() {
		return cdTipoTrasportoRientro;
	}
}
/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 25/01/2023
 */
package it.cnr.contab.config00.consultazioni.bulk;
import it.cnr.contab.ordmag.ordini.bulk.EvasioneOrdineKey;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.KeyedPersistent;
public class VControlloDispUnitaOrgKey extends OggettoBulk implements KeyedPersistent {
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: V_CONTROLLO_DISP_UNITA_ORGANIZZATIVA
	 **/
	//    CD_UNITA_ORGANIZZATIVA VARCHAR(30)
	private String cdUnitaOrganizzativa;

	public VControlloDispUnitaOrgKey() {
		super();
	}

	public VControlloDispUnitaOrgKey(String cdUnitaOrganizzativa) {
		this.cdUnitaOrganizzativa = cdUnitaOrganizzativa;
	}

	public boolean equalsByPrimaryKey(Object o) {
		if (this== o) return true;
		if (!(o instanceof VControlloDispUnitaOrgKey)) return false;
		VControlloDispUnitaOrgKey k = (VControlloDispUnitaOrgKey) o;
		if (!compareKey(getCdUnitaOrganizzativa(), k.getCdUnitaOrganizzativa())) return false;
		return true;
	}

	public int primaryKeyHashCode() {
		int i = 0;
		i = i + calculateKeyHashCode(getCdUnitaOrganizzativa());
		return i;
	}


	public String getCdUnitaOrganizzativa() {
		return cdUnitaOrganizzativa;
	}

	public void setCdUnitaOrganizzativa(String cdUnitaOrganizzativa) {
		this.cdUnitaOrganizzativa = cdUnitaOrganizzativa;
	}
}
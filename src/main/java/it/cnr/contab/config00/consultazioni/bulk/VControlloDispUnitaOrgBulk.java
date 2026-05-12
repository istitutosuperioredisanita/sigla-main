/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 25/01/2023
 */
package it.cnr.contab.config00.consultazioni.bulk;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;
import it.cnr.contab.ordmag.anag00.UnitaOperativaOrdBulk;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.util.action.CRUDBP;
public class VControlloDispUnitaOrgBulk extends VControlloDispUnitaOrgBase {

	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: V_CONTROLLO_DISP_UNITA_ORGANIZZATIVA
	 **/
	public VControlloDispUnitaOrgBulk() {
		super();
	}


	private Unita_organizzativaBulk unitaOrganizzativa = new Unita_organizzativaBulk();


	public Unita_organizzativaBulk getUnitaOrganizzativa() {
		return unitaOrganizzativa;
	}

	public void setUnitaOrganizzativa(Unita_organizzativaBulk unitaOrganizzativa) {
		this.unitaOrganizzativa = unitaOrganizzativa;
	}

	public String getCdUnitaOrganizzativa() {
		Unita_organizzativaBulk uo = this.getUnitaOrganizzativa();
		if (uo == null)
			return null;
		return getUnitaOrganizzativa().getCd_unita_organizzativa();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cdUnitaOrganizzativa]
	 **/
	public void setCdUnitaOrganizzativa(String cdUnitaOrganizzativa)  {
		this.getUnitaOrganizzativa().setCd_unita_organizzativa(cdUnitaOrganizzativa);
	}



	public String getDs_unita_organizzativa() {
		Unita_organizzativaBulk uo = this.getUnitaOrganizzativa();
		if (uo == null)
			return null;
		return getUnitaOrganizzativa().getDs_unita_organizzativa();
	}

	public void setDs_unita_organizzativa(String ds_unita_organizzativa) {
		getUnitaOrganizzativa().setDs_unita_organizzativa(ds_unita_organizzativa);
	}


}
/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 16/06/2025
 */
package it.cnr.contab.docamm00.consultazioni.bulk;
import it.cnr.jada.persistency.Keyed;
public class V_confronta_costi_coge_cofiBase extends V_confronta_costi_coge_cofiKey implements Keyed {

//    IMPORTO_COGE DECIMAL(0,-127)
	private java.math.BigDecimal importoCoge;
 
//    IMPORTO_COFI DECIMAL(0,-127)
	private java.math.BigDecimal importoCofi;


	private String dsVoceCoge;
	private String dsVoceCofi;
 
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: V_CONFRONTA_COSTI_COGE_COFI
	 **/
	public V_confronta_costi_coge_cofiBase() {
		super();
	}

	public V_confronta_costi_coge_cofiBase(Integer esercizio,String voceCoge,String voceCofi) {
		super(esercizio,voceCoge,voceCofi);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [importoCoge]
	 **/
	public java.math.BigDecimal getImportoCoge() {
		return importoCoge;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [importoCoge]
	 **/
	public void setImportoCoge(java.math.BigDecimal importoCoge)  {
		this.importoCoge=importoCoge;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [importoCofi]
	 **/
	public java.math.BigDecimal getImportoCofi() {
		return importoCofi;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [importoCofi]
	 **/
	public void setImportoCofi(java.math.BigDecimal importoCofi)  {
		this.importoCofi=importoCofi;
	}

	public String getDsVoceCoge() {
		return dsVoceCoge;
	}

	public void setDsVoceCoge(String dsVoceCoge) {
		this.dsVoceCoge = dsVoceCoge;
	}

	public String getDsVoceCofi() {
		return dsVoceCofi;
	}

	public void setDsVoceCofi(String dsVoceCofi) {
		this.dsVoceCofi = dsVoceCofi;
	}
}
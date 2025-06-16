/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 16/06/2025
 */
package it.cnr.contab.docamm00.consultazioni.bulk;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.KeyedPersistent;
public class V_confronta_costi_coge_cofiKey extends OggettoBulk implements KeyedPersistent {
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: V_CONFRONTA_COSTI_COGE_COFI
	 **/
	//    ESERCIZIO DECIMAL(5,0)
	private Integer esercizio;

	//    VOCE_COGE VARCHAR(45)
	private String voceCoge;

	//    VOCE_COFI VARCHAR(20)
	private String voceCofi;
	public V_confronta_costi_coge_cofiKey() {
		super();
	}
	public V_confronta_costi_coge_cofiKey(Integer esercizio,String voceCoge,String voceCofi) {
		super();
		this.setEsercizio(esercizio);
		this.setVoceCoge(voceCoge);
		this.setVoceCofi(voceCofi);

	}
	public boolean equalsByPrimaryKey(Object o) {
		if (this== o) return true;
		if (!(o instanceof V_confronta_costi_coge_cofiKey)) return false;
		V_confronta_costi_coge_cofiKey k = (V_confronta_costi_coge_cofiKey) o;
		return true;
	}
	public int primaryKeyHashCode() {
		int i = 0;
		return i;
	}

	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [esercizio]
	 **/
	public Integer getEsercizio() {
		return esercizio;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [esercizio]
	 **/
	public void setEsercizio(Integer esercizio)  {
		this.esercizio=esercizio;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [voceCoge]
	 **/
	public String getVoceCoge() {
		return voceCoge;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [voceCoge]
	 **/
	public void setVoceCoge(String voceCoge)  {
		this.voceCoge=voceCoge;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [voceCofi]
	 **/
	public String getVoceCofi() {
		return voceCofi;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [voceCofi]
	 **/
	public void setVoceCofi(String voceCofi)  {
		this.voceCofi=voceCofi;
	}
}
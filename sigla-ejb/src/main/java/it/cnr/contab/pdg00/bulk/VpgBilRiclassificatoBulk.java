/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 12/04/2024
 */
package it.cnr.contab.pdg00.bulk;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.Persistent;

public class VpgBilRiclassificatoBulk extends OggettoBulk implements Persistent {
	//    ID DECIMAL(0,-127) NOT NULL
	private java.math.BigDecimal id;

	//    CHIAVE VARCHAR(100) NOT NULL
	private java.lang.String chiave;

	//    TIPO CHAR(1) NOT NULL
	private java.lang.String tipo;

	//    SEQUENZA DECIMAL(0,-127) NOT NULL
	private java.math.BigDecimal sequenza;

	//    ORDINE DECIMAL(20,3)
	private java.math.BigDecimal ordine;

	//    CONTO_RICLASS VARCHAR(200)
	private java.lang.String contoRiclass;

	//    I_LIVELLO VARCHAR(200)
	private java.lang.String i_livello;

	//    II_LIVELLO VARCHAR(200)
	private java.lang.String ii_livello;

	//    III_LIVELLO VARCHAR(200)
	private java.lang.String iii_livello;

	//    IV_LIVELLO VARCHAR(200)
	private java.lang.String iv_livello;

	//    DESCRIZIONE VARCHAR(200)
	private java.lang.String descrizione;

	//    PARZIALE_I_ANNO DECIMAL(20,3)
	private java.math.BigDecimal parzialeIAnno;

	//    TOTALE_I_ANNO DECIMAL(20,3)
	private java.math.BigDecimal totaleIAnno;

	//    PARZIALE_II_ANNO DECIMAL(20,3)
	private java.math.BigDecimal parzialeIiAnno;

	//    TOTALE_II_ANNO DECIMAL(20,3)
	private java.math.BigDecimal totaleIiAnno;

	//    SN_TOTALE VARCHAR(200)
	private java.lang.String snTotale;
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: PRT_VPG_BIL_RICLASSIFICATO
	 **/
	public VpgBilRiclassificatoBulk() {
		super();
	}
	/*
	* Created by BulkGenerator 2.0 [07/12/2009]
	* Restituisce il valore di: [id]
	**/
	public java.math.BigDecimal getId() {
		return id;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [id]
	 **/
	public void setId(java.math.BigDecimal id)  {
		this.id=id;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [chiave]
	 **/
	public java.lang.String getChiave() {
		return chiave;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [chiave]
	 **/
	public void setChiave(java.lang.String chiave)  {
		this.chiave=chiave;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [tipo]
	 **/
	public java.lang.String getTipo() {
		return tipo;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [tipo]
	 **/
	public void setTipo(java.lang.String tipo)  {
		this.tipo=tipo;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [sequenza]
	 **/
	public java.math.BigDecimal getSequenza() {
		return sequenza;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [sequenza]
	 **/
	public void setSequenza(java.math.BigDecimal sequenza)  {
		this.sequenza=sequenza;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [ordine]
	 **/
	public java.math.BigDecimal getOrdine() {
		return ordine;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [ordine]
	 **/
	public void setOrdine(java.math.BigDecimal ordine)  {
		this.ordine=ordine;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [contoRiclass]
	 **/
	public java.lang.String getContoRiclass() {
		return contoRiclass;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [contoRiclass]
	 **/
	public void setContoRiclass(java.lang.String contoRiclass)  {
		this.contoRiclass=contoRiclass;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [iLivello]
	 **/
	public java.lang.String getI_livello() {
		return i_livello;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [iLivello]
	 **/
	public void setI_livello(java.lang.String iLivello)  {
		this.i_livello =iLivello;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [iiLivello]
	 **/
	public java.lang.String getIi_livello() {
		return ii_livello;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [iiLivello]
	 **/
	public void setIi_livello(java.lang.String ii_livello)  {
		this.ii_livello = ii_livello;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [iiiLivello]
	 **/
	public java.lang.String getIii_livello() {
		return iii_livello;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [iiiLivello]
	 **/
	public void setIii_livello(java.lang.String iii_livello)  {
		this.iii_livello = iii_livello;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [ivLivello]
	 **/
	public java.lang.String getIv_livello() {
		return iv_livello;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [ivLivello]
	 **/
	public void setIv_livello(java.lang.String iv_livello)  {
		this.iv_livello = iv_livello;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [descrizione]
	 **/
	public java.lang.String getDescrizione() {
		return descrizione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [descrizione]
	 **/
	public void setDescrizione(java.lang.String descrizione)  {
		this.descrizione=descrizione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [parzialeIAnno]
	 **/
	public java.math.BigDecimal getParzialeIAnno() {
		return parzialeIAnno;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [parzialeIAnno]
	 **/
	public void setParzialeIAnno(java.math.BigDecimal parzialeIAnno)  {
		this.parzialeIAnno=parzialeIAnno;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [totaleIAnno]
	 **/
	public java.math.BigDecimal getTotaleIAnno() {
		return totaleIAnno;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [totaleIAnno]
	 **/
	public void setTotaleIAnno(java.math.BigDecimal totaleIAnno)  {
		this.totaleIAnno=totaleIAnno;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [parzialeIiAnno]
	 **/
	public java.math.BigDecimal getParzialeIiAnno() {
		return parzialeIiAnno;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [parzialeIiAnno]
	 **/
	public void setParzialeIiAnno(java.math.BigDecimal parzialeIiAnno)  {
		this.parzialeIiAnno=parzialeIiAnno;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [totaleIiAnno]
	 **/
	public java.math.BigDecimal getTotaleIiAnno() {
		return totaleIiAnno;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [totaleIiAnno]
	 **/
	public void setTotaleIiAnno(java.math.BigDecimal totaleIiAnno)  {
		this.totaleIiAnno=totaleIiAnno;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [snTotale]
	 **/
	public java.lang.String getSnTotale() {
		return snTotale;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [snTotale]
	 **/
	public void setSnTotale(java.lang.String snTotale)  {
		this.snTotale=snTotale;
	}
}
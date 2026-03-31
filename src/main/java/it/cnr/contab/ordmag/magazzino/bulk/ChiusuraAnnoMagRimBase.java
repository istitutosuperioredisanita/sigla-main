/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 27/02/2024
 */
package it.cnr.contab.ordmag.magazzino.bulk;
import it.cnr.jada.persistency.Keyed;

import java.math.BigDecimal;

public class ChiusuraAnnoMagRimBase extends ChiusuraAnnoMagRimKey implements Keyed {
//    PG_CHIUSURA DECIMAL(38,0) NOT NULL
	private Integer pgChiusura;
 
//    TIPO_CHIUSURA CHAR(1) NOT NULL
	private String tipoChiusura;
 
//    CD_CDS_RAGGR_MAG VARCHAR(30) NOT NULL
	private String cdCdsRaggrMag;
 
//    CD_RAGGR_MAG VARCHAR(3) NOT NULL
	private String cdRaggrMag;
 
//    CD_CDS_MAG VARCHAR(30) NOT NULL
	private String cdCdsMag;
 
//    CD_MAGAZZINO VARCHAR(10) NOT NULL
	private String cdMagazzino;
 
//    CD_CATEGORIA_GRUPPO VARCHAR(10) NOT NULL
	private String cdCategoriaGruppo;
 
//    CD_BENE_SERVIZIO VARCHAR(15) NOT NULL
	private String cdBeneServizio;

	private String unitaMisura;

//    GIACENZA DECIMAL(12,5) NOT NULL
	private java.math.BigDecimal giacenza;
 
//    CARICO_INIZIALE DECIMAL(12,5) NOT NULL
	private java.math.BigDecimal caricoIniziale;
 
//    CARICO_ANNO DECIMAL(12,5) NOT NULL
	private java.math.BigDecimal caricoAnno;
 
//    SCARICO_ANNO DECIMAL(12,5) NOT NULL
	private java.math.BigDecimal scaricoAnno;
 
//    IMPORTO_UNITARIO DECIMAL(21,6) NOT NULL
	private java.math.BigDecimal importoUnitarioChi;
	//    IMPORTO_UNITARIO DECIMAL(21,6) NOT NULL
	private java.math.BigDecimal importoUnitarioLotto;
	//    IMPORTO_UNITARIO DECIMAL(21,6) NOT NULL
	private java.math.BigDecimal importoCmppArt;
 

 
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: CHIUSURA_ANNO_MAG_RIM
	 **/
	public ChiusuraAnnoMagRimBase() {
		super();
	}
	public ChiusuraAnnoMagRimBase(String cdCdsLotto, String cdMagazzinoLotto, Integer esercizioLotto, String cdNumeratoreLotto, Integer pgLotto,Integer anno) {
		super(cdCdsLotto, cdMagazzinoLotto, esercizioLotto, cdNumeratoreLotto, pgLotto,anno);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Progressivo record chiusura]
	 **/
	public Integer getPgChiusura() {
		return pgChiusura;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Progressivo record chiusura]
	 **/
	public void setPgChiusura(Integer pgChiusura)  {
		this.pgChiusura=pgChiusura;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [M=Magazzino; I=Inventario]
	 **/
	public String getTipoChiusura() {
		return tipoChiusura;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [M=Magazzino; I=Inventario]
	 **/
	public void setTipoChiusura(String tipoChiusura)  {
		this.tipoChiusura=tipoChiusura;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cds raggruppamento magazzino]
	 **/
	public String getCdCdsRaggrMag() {
		return cdCdsRaggrMag;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cds raggruppamento magazzino]
	 **/
	public void setCdCdsRaggrMag(String cdCdsRaggrMag)  {
		this.cdCdsRaggrMag=cdCdsRaggrMag;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [codice raggruppamento magazzino]
	 **/
	public String getCdRaggrMag() {
		return cdRaggrMag;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [codice raggruppamento magazzino]
	 **/
	public void setCdRaggrMag(String cdRaggrMag)  {
		this.cdRaggrMag=cdRaggrMag;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cds magazzino]
	 **/
	public String getCdCdsMag() {
		return cdCdsMag;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cds magazzino]
	 **/
	public void setCdCdsMag(String cdCdsMag)  {
		this.cdCdsMag=cdCdsMag;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [codice magazzino]
	 **/
	public String getCdMagazzino() {
		return cdMagazzino;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [codice magazzino]
	 **/
	public void setCdMagazzino(String cdMagazzino)  {
		this.cdMagazzino=cdMagazzino;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice categoria/gruppo]
	 **/
	public String getCdCategoriaGruppo() {
		return cdCategoriaGruppo;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice categoria/gruppo]
	 **/
	public void setCdCategoriaGruppo(String cdCategoriaGruppo)  {
		this.cdCategoriaGruppo=cdCategoriaGruppo;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice identificativo del bene o servizio]
	 **/
	public String getCdBeneServizio() {
		return cdBeneServizio;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice identificativo del bene o servizio]
	 **/
	public void setCdBeneServizio(String cdBeneServizio)  {
		this.cdBeneServizio=cdBeneServizio;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Qta rimanente nel lotto in unita" minima.]
	 **/
	public java.math.BigDecimal getGiacenza() {
		return giacenza;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Qta rimanente nel lotto in unita" minima.]
	 **/
	public void setGiacenza(java.math.BigDecimal giacenza)  {
		this.giacenza=giacenza;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Qta caricata inizio anno]
	 **/
	public java.math.BigDecimal getCaricoIniziale() {
		return caricoIniziale;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Qta caricata inizio anno]
	 **/
	public void setCaricoIniziale(java.math.BigDecimal caricoIniziale)  {
		this.caricoIniziale=caricoIniziale;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Qta caricata nell'anno]
	 **/
	public java.math.BigDecimal getCaricoAnno() {
		return caricoAnno;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Qta caricata nell'anno]
	 **/
	public void setCaricoAnno(java.math.BigDecimal caricoAnno)  {
		this.caricoAnno=caricoAnno;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Qta scaricata nell'anno]
	 **/
	public java.math.BigDecimal getScaricoAnno() {
		return scaricoAnno;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Qta scaricata nell'anno]
	 **/
	public void setScaricoAnno(java.math.BigDecimal scaricoAnno)  {
		this.scaricoAnno=scaricoAnno;
	}

	public BigDecimal getImportoUnitarioChi() {
		return importoUnitarioChi;
	}

	public void setImportoUnitarioChi(BigDecimal importoUnitarioChi) {
		this.importoUnitarioChi = importoUnitarioChi;
	}

	public BigDecimal getImportoUnitarioLotto() {
		return importoUnitarioLotto;
	}

	public void setImportoUnitarioLotto(BigDecimal importoUnitarioLotto) {
		this.importoUnitarioLotto = importoUnitarioLotto;
	}

	public BigDecimal getImportoCmppArt() {
		return importoCmppArt;
	}

	public void setImportoCmppArt(BigDecimal importoCmppArt) {
		this.importoCmppArt = importoCmppArt;
	}

	public String getUnitaMisura() {
		return unitaMisura;
	}

	public void setUnitaMisura(String unitaMisura) {
		this.unitaMisura = unitaMisura;
	}
}
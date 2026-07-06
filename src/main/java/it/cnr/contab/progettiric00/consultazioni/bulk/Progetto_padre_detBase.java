/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 02/07/2026
 */
package it.cnr.contab.progettiric00.consultazioni.bulk;
import it.cnr.jada.persistency.Keyed;
public class Progetto_padre_detBase extends Progetto_padre_detKey implements Keyed {

	private java.lang.Integer esercizio;

	//    CD_PROGETTO VARCHAR(30)
	private java.lang.String cdProgetto;
 
//    CD_PROGETTO_PADRE VARCHAR(30)
	private java.lang.String cdProgettoPadre;
 
//    CD_UNITA_ORGANIZZATIVA VARCHAR(30)
	private java.lang.String cdUnitaOrganizzativa;
 
//    DS_PROGETTO VARCHAR(400)
	private java.lang.String dsProgetto;
 
//    IM_FINANZIATO DECIMAL(15,2)
	private java.math.BigDecimal imFinanziato;
 
//    IM_COFINANZIATO DECIMAL(15,2)
	private java.math.BigDecimal imCofinanziato;
 
//    DT_INIZIO TIMESTAMP(7)
	private java.sql.Timestamp dtInizio;
 
//    DT_FINE TIMESTAMP(7)
	private java.sql.Timestamp dtFine;
 
//    DT_PROROGA TIMESTAMP(7)
	private java.sql.Timestamp dtProroga;
 
//    STATO CHAR(3)
	private java.lang.String stato;
 
//    TIPO_PROGETTO VARCHAR(10) NOT NULL
	private java.lang.String tipoProgetto;
 
//    DS_TIPO_PROGETTO VARCHAR(100) NOT NULL
	private java.lang.String dsTipoProgetto;
 
//    FINANZIATORE_TERZO DECIMAL(38,0)
	private java.lang.Long finanziatoreTerzo;
 
//    DENOMINAZIONE_SEDE VARCHAR(200)
	private java.lang.String denominazioneSede;
 
//    IM_FINANZIATO_FINANZIATORE DECIMAL(15,2)
	private java.math.BigDecimal imFinanziatoFinanziatore;
 
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: V_PROGETTO_PADRE_DET
	 **/
	public Progetto_padre_detBase() {
		super();
	}

	public java.lang.Integer getEsercizio() {
		return esercizio;
	}

	public void setEsercizio(java.lang.Integer esercizio)  {
		this.esercizio=esercizio;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cdProgetto]
	 **/
	public java.lang.String getCdProgetto() {
		return cdProgetto;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cdProgetto]
	 **/
	public void setCdProgetto(java.lang.String cdProgetto)  {
		this.cdProgetto=cdProgetto;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cdProgettoPadre]
	 **/
	public java.lang.String getCdProgettoPadre() {
		return cdProgettoPadre;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cdProgettoPadre]
	 **/
	public void setCdProgettoPadre(java.lang.String cdProgettoPadre)  {
		this.cdProgettoPadre=cdProgettoPadre;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cdUnitaOrganizzativa]
	 **/
	public java.lang.String getCdUnitaOrganizzativa() {
		return cdUnitaOrganizzativa;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cdUnitaOrganizzativa]
	 **/
	public void setCdUnitaOrganizzativa(java.lang.String cdUnitaOrganizzativa)  {
		this.cdUnitaOrganizzativa=cdUnitaOrganizzativa;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [dsProgetto]
	 **/
	public java.lang.String getDsProgetto() {
		return dsProgetto;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [dsProgetto]
	 **/
	public void setDsProgetto(java.lang.String dsProgetto)  {
		this.dsProgetto=dsProgetto;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [imFinanziato]
	 **/
	public java.math.BigDecimal getImFinanziato() {
		return imFinanziato;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [imFinanziato]
	 **/
	public void setImFinanziato(java.math.BigDecimal imFinanziato)  {
		this.imFinanziato=imFinanziato;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [imCofinanziato]
	 **/
	public java.math.BigDecimal getImCofinanziato() {
		return imCofinanziato;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [imCofinanziato]
	 **/
	public void setImCofinanziato(java.math.BigDecimal imCofinanziato)  {
		this.imCofinanziato=imCofinanziato;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [dtInizio]
	 **/
	public java.sql.Timestamp getDtInizio() {
		return dtInizio;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [dtInizio]
	 **/
	public void setDtInizio(java.sql.Timestamp dtInizio)  {
		this.dtInizio=dtInizio;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [dtFine]
	 **/
	public java.sql.Timestamp getDtFine() {
		return dtFine;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [dtFine]
	 **/
	public void setDtFine(java.sql.Timestamp dtFine)  {
		this.dtFine=dtFine;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [dtProroga]
	 **/
	public java.sql.Timestamp getDtProroga() {
		return dtProroga;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [dtProroga]
	 **/
	public void setDtProroga(java.sql.Timestamp dtProroga)  {
		this.dtProroga=dtProroga;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [stato]
	 **/
	public java.lang.String getStato() {
		return stato;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [stato]
	 **/
	public void setStato(java.lang.String stato)  {
		this.stato=stato;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [tipoProgetto]
	 **/
	public java.lang.String getTipoProgetto() {
		return tipoProgetto;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [tipoProgetto]
	 **/
	public void setTipoProgetto(java.lang.String tipoProgetto)  {
		this.tipoProgetto=tipoProgetto;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [dsTipoProgetto]
	 **/
	public java.lang.String getDsTipoProgetto() {
		return dsTipoProgetto;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [dsTipoProgetto]
	 **/
	public void setDsTipoProgetto(java.lang.String dsTipoProgetto)  {
		this.dsTipoProgetto=dsTipoProgetto;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [finanziatoreTerzo]
	 **/
	public java.lang.Long getFinanziatoreTerzo() {
		return finanziatoreTerzo;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [finanziatoreTerzo]
	 **/
	public void setFinanziatoreTerzo(java.lang.Long finanziatoreTerzo)  {
		this.finanziatoreTerzo=finanziatoreTerzo;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [denominazioneSede]
	 **/
	public java.lang.String getDenominazioneSede() {
		return denominazioneSede;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [denominazioneSede]
	 **/
	public void setDenominazioneSede(java.lang.String denominazioneSede)  {
		this.denominazioneSede=denominazioneSede;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [imFinanziatoFinanziatore]
	 **/
	public java.math.BigDecimal getImFinanziatoFinanziatore() {
		return imFinanziatoFinanziatore;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [imFinanziatoFinanziatore]
	 **/
	public void setImFinanziatoFinanziatore(java.math.BigDecimal imFinanziatoFinanziatore)  {
		this.imFinanziatoFinanziatore=imFinanziatoFinanziatore;
	}
}
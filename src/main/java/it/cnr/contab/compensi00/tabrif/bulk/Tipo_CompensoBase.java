/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 27/05/2026
 */
package it.cnr.contab.compensi00.tabrif.bulk;
import it.cnr.jada.persistency.Keyed;
public class Tipo_CompensoBase extends Tipo_CompensoKey implements Keyed {
//    DS_TI_COMPENSO VARCHAR(100) NOT NULL
	private String dsTiCompenso;
 
//    FL_REGISTRA_FATTURA CHAR(1) NOT NULL
	private Boolean flRegistraFattura;
 
//    FL_DIARIA CHAR(1) NOT NULL
	private Boolean flDiaria;
 
//    DT_FINE_VALIDITA TIMESTAMP(7) NOT NULL
	private java.sql.Timestamp dtFineValidita;
 
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: TIPO_COMPENSO
	 **/
	public Tipo_CompensoBase() {
		super();
	}
	public Tipo_CompensoBase(String cdTrattamento, String cdTiCompenso, java.sql.Timestamp dtInizioValidita) {
		super(cdTrattamento, cdTiCompenso, dtInizioValidita);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Descrizione tipo compenso. Proposta come uguale a quella del corrispondente record in TIPO_TRATTAMENTO ma modificabile dall'utente.]
	 **/
	public String getDsTiCompenso() {
		return dsTiCompenso;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Descrizione tipo compenso. Proposta come uguale a quella del corrispondente record in TIPO_TRATTAMENTO ma modificabile dall'utente.]
	 **/
	public void setDsTiCompenso(String dsTiCompenso)  {
		this.dsTiCompenso=dsTiCompenso;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Indicatore di compenso abilitato ad essere associato ad una fattura.]
	 **/
	public Boolean getFlRegistraFattura() {
		return flRegistraFattura;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Indicatore di compenso abilitato ad essere associato ad una fattura.]
	 **/
	public void setFlRegistraFattura(Boolean flRegistraFattura)  {
		this.flRegistraFattura=flRegistraFattura;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Indicatore di tipologia di compenso utilizzabile nel calcolo della diaria delle missioni.]
	 **/
	public Boolean getFlDiaria() {
		return flDiaria;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Indicatore di tipologia di compenso utilizzabile nel calcolo della diaria delle missioni.]
	 **/
	public void setFlDiaria(Boolean flDiaria)  {
		this.flDiaria=flDiaria;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Data fine validita]
	 **/
	public java.sql.Timestamp getDtFineValidita() {
		return dtFineValidita;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Data fine validita]
	 **/
	public void setDtFineValidita(java.sql.Timestamp dtFineValidita)  {
		this.dtFineValidita=dtFineValidita;
	}
}
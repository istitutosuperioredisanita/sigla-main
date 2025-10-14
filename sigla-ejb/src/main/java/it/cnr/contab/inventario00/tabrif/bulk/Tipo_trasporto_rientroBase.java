/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 10/10/2025
 */
package it.cnr.contab.inventario00.tabrif.bulk;
import it.cnr.jada.persistency.Keyed;
public class Tipo_trasporto_rientroBase extends Tipo_trasporto_rientroKey implements Keyed {
//    DS_TIPO_TRASPORTO_RIENTRO VARCHAR(100) NOT NULL
	private String dsTipoTrasportoRientro;

//    TI_DOCUMENTO CHAR(1) NOT NULL
	private String tiDocumento;

//    DT_CANCELLAZIONE TIMESTAMP(7)
	private java.sql.Timestamp dtCancellazione;

//    FL_ABILITA_NOTE CHAR(1) NOT NULL
	private Boolean flAbilitaNote;

	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: TIPO_TRASPORTO_RIENTRO
	 **/
	public Tipo_trasporto_rientroBase() {
		super();
	}
	public Tipo_trasporto_rientroBase(String cdTipoTrasportoRientro) {
		super(cdTipoTrasportoRientro);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Descrizione del movimento di trasporto/rientro di inventario]
	 **/
	public String getDsTipoTrasportoRientro() {
		return dsTipoTrasportoRientro;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Descrizione del movimento di trasporto/rientro di inventario]
	 **/
	public void setDsTipoTrasportoRientro(String dsTipoTrasportoRientro)  {
		this.dsTipoTrasportoRientro=dsTipoTrasportoRientro;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Flag per stabilire se il movimento risulta essere un trasporto o un rientro di inventario (T=Trasporto, R=Rientro)]
	 **/
	public String getTiDocumento() {
		return tiDocumento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Flag per stabilire se il movimento risulta essere un trasporto o un rientro di inventario (T=Trasporto, R=Rientro)]
	 **/
	public void setTiDocumento(String tiDocumento)  {
		this.tiDocumento=tiDocumento;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Data di fine validita per il movimento in esame]
	 **/
	public java.sql.Timestamp getDtCancellazione() {
		return dtCancellazione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Data di fine validita per il movimento in esame]
	 **/
	public void setDtCancellazione(java.sql.Timestamp dtCancellazione)  {
		this.dtCancellazione=dtCancellazione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Flag che indica se per questa tipologia di documento e' possibile inserire delle note. Dominio: Y = Abilita campo NOTE nel documento, N = Non abilita campo NOTE]
	 **/
	public Boolean getFlAbilitaNote() {
		return flAbilitaNote;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Flag che indica se per questa tipologia di documento e' possibile inserire delle note. Dominio: Y = Abilita campo NOTE nel documento, N = Non abilita campo NOTE]
	 **/
	public void setFlAbilitaNote(Boolean flAbilitaNote)  {
		this.flAbilitaNote=flAbilitaNote;
	}
}
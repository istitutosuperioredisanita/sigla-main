/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 27/03/2024
 */
package it.cnr.contab.config00.pdcep.bulk;
import it.cnr.jada.persistency.Keyed;
public class GruppoEPBase extends GruppoEPKey implements Keyed {
//    CD_PIANO_GRUPPI VARCHAR(5) NOT NULL
	private String cdPianoGruppi;
 
//    CD_GRUPPO_EP VARCHAR(40) NOT NULL
	private String cdGruppoEp;
 
//    CD_GRUPPO_PADRE VARCHAR(40)
	private String cdGruppoPadre;
 
//    SEQUENZA DECIMAL(38,0) NOT NULL
	private Long sequenza;
 
//    NOME VARCHAR(100)
	private String nome;
 
//    DS_GRUPPO_EP VARCHAR(200)
	private String dsGruppoEp;
 
//    SEGNO CHAR(1) NOT NULL
	private String segno;
 
//    FL_MASTRINO CHAR(1) NOT NULL
	private Boolean flMastrino;
 
//    CD_PIANO_PADRE VARCHAR(5)
	private String cdPianoPadre;
 
//    FORMULA VARCHAR(2000)
	private String formula;
 
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: CNR_GRUPPO_EP
	 **/
	public GruppoEPBase() {
		super();
	}
	public GruppoEPBase(String rowid) {
		super(rowid);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cdPianoGruppi]
	 **/
	public String getCdPianoGruppi() {
		return cdPianoGruppi;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cdPianoGruppi]
	 **/
	public void setCdPianoGruppi(String cdPianoGruppi)  {
		this.cdPianoGruppi=cdPianoGruppi;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cdGruppoEp]
	 **/
	public String getCdGruppoEp() {
		return cdGruppoEp;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cdGruppoEp]
	 **/
	public void setCdGruppoEp(String cdGruppoEp)  {
		this.cdGruppoEp=cdGruppoEp;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cdGruppoPadre]
	 **/
	public String getCdGruppoPadre() {
		return cdGruppoPadre;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cdGruppoPadre]
	 **/
	public void setCdGruppoPadre(String cdGruppoPadre)  {
		this.cdGruppoPadre=cdGruppoPadre;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [sequenza]
	 **/
	public Long getSequenza() {
		return sequenza;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [sequenza]
	 **/
	public void setSequenza(Long sequenza)  {
		this.sequenza=sequenza;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [nome]
	 **/
	public String getNome() {
		return nome;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [nome]
	 **/
	public void setNome(String nome)  {
		this.nome=nome;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [dsGruppoEp]
	 **/
	public String getDsGruppoEp() {
		return dsGruppoEp;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [dsGruppoEp]
	 **/
	public void setDsGruppoEp(String dsGruppoEp)  {
		this.dsGruppoEp=dsGruppoEp;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [segno]
	 **/
	public String getSegno() {
		return segno;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [segno]
	 **/
	public void setSegno(String segno)  {
		this.segno=segno;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [flMastrino]
	 **/
	public Boolean getFlMastrino() {
		return flMastrino;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [flMastrino]
	 **/
	public void setFlMastrino(Boolean flMastrino)  {
		this.flMastrino=flMastrino;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cdPianoPadre]
	 **/
	public String getCdPianoPadre() {
		return cdPianoPadre;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cdPianoPadre]
	 **/
	public void setCdPianoPadre(String cdPianoPadre)  {
		this.cdPianoPadre=cdPianoPadre;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [formula]
	 **/
	public String getFormula() {
		return formula;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [formula]
	 **/
	public void setFormula(String formula)  {
		this.formula=formula;
	}
}
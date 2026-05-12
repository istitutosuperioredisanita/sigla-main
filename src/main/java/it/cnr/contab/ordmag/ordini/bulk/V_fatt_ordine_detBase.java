/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 18/08/2025
 */
package it.cnr.contab.ordmag.ordini.bulk;

import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.Persistent;

public class V_fatt_ordine_detBase extends OggettoBulk implements Persistent {

	//    CD_CDS VARCHAR(30) NOT NULL
	private String cdCds;

	//    CD_UNITA_ORGANIZZATIVA VARCHAR(30) NOT NULL
	private String cdUnitaOrganizzativa;

	//    ESERCIZIO DECIMAL(5,0) NOT NULL
	private Integer esercizio;

	//    PG_FATTURA_PASSIVA DECIMAL(38,0) NOT NULL
	private Long pgFatturaPassiva;

	//    PROGRESSIVO_RIGA DECIMAL(38,0) NOT NULL
	private Long progressivoRiga;
	//    CD_CDS_ORDINE VARCHAR(30) NOT NULL
	private String cdCdsOrdine;

	//    UOP_ORDINE VARCHAR(30) NOT NULL
	private String uopOrdine;

	//    ESERCIZIO_ORDINE DECIMAL(5,0) NOT NULL
	private Integer esercizioOrdine;

	//    CD_NUM_ORDINE VARCHAR(3) NOT NULL
	private String cdNumOrdine;

	//    NUM_ORDINE DECIMAL(38,0) NOT NULL
	private Integer numOrdine;

	//    RIGA_ORDINE aggiornaDatiInventario2(userContext,fattura_passiva);DECIMAL(38,0) NOT NULL
	private Integer rigaOrdine;

	//    CONS_ORDINE DECIMAL(38,0) NOT NULL
	private Integer consOrdine;
//    CDS_EVASIONE VARCHAR(30)
	private String cdsEvasione;
 
//    MAG_EVAZIONE VARCHAR(10)
	private String magEvazione;
 
//    ESE_EVASIONE DECIMAL(5,0)
	private Integer eseEvasione;
 
//    NUMMAG_EVASIONE VARCHAR(10)
	private String nummagEvasione;
 
//    NUM_EVASIONE DECIMAL(38,0)
	private Long numEvasione;
 
//    RIGA_EVASIONE DECIMAL(38,0)
	private Long rigaEvasione;
 
//    DATA_BOLLA TIMESTAMP(7)
	private java.sql.Timestamp dataBolla;
 
//    NUMERO_BOLLA VARCHAR(30)
	private String numeroBolla;
 
//    ID_TRANSITO DECIMAL(38,0)
	private Long idTransito;

	private java.lang.String ti_documento;
	private java.lang.Integer eser_buono_car_sca;
	private java.lang.Long pg_buono_c_s;

//    PG_INVENTARIO DECIMAL(38,0)
	private Long pgInventario;
 
//    NR_INVENTARIO DECIMAL(38,0)
	private Long nrInventario;
 
//    PROGRESSIVO DECIMAL(38,0)
	private Long progressivo;
 
//    PG_RIGA_ASS DECIMAL(0,-127)
	private java.math.BigDecimal pgRigaAss;
 
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: V_FATT_ORDINE_DET
	 **/

	public V_fatt_ordine_detBase() {
		super();
	}

	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cdCds]
	 **/
	public String getCdCds() {
		return cdCds;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cdCds]
	 **/
	public void setCdCds(String cdCds)  {
		this.cdCds=cdCds;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cdUnitaOrganizzativa]
	 **/
	public String getCdUnitaOrganizzativa() {
		return cdUnitaOrganizzativa;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cdUnitaOrganizzativa]
	 **/
	public void setCdUnitaOrganizzativa(String cdUnitaOrganizzativa)  {
		this.cdUnitaOrganizzativa=cdUnitaOrganizzativa;
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
	 * Restituisce il valore di: [pgFatturaPassiva]
	 **/
	public Long getPgFatturaPassiva() {
		return pgFatturaPassiva;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [pgFatturaPassiva]
	 **/
	public void setPgFatturaPassiva(Long pgFatturaPassiva)  {
		this.pgFatturaPassiva=pgFatturaPassiva;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [progressivoRiga]
	 **/
	public Long getProgressivoRiga() {
		return progressivoRiga;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [progressivoRiga]
	 **/
	public void setProgressivoRiga(Long progressivoRiga)  {
		this.progressivoRiga=progressivoRiga;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cdCdsOrdine]
	 **/
	public String getCdCdsOrdine() {
		return cdCdsOrdine;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cdCdsOrdine]
	 **/
	public void setCdCdsOrdine(String cdCdsOrdine)  {
		this.cdCdsOrdine=cdCdsOrdine;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [uopOrdine]
	 **/
	public String getUopOrdine() {
		return uopOrdine;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [uopOrdine]
	 **/
	public void setUopOrdine(String uopOrdine)  {
		this.uopOrdine=uopOrdine;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [esercizioOrdine]
	 **/
	public Integer getEsercizioOrdine() {
		return esercizioOrdine;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [esercizioOrdine]
	 **/
	public void setEsercizioOrdine(Integer esercizioOrdine)  {
		this.esercizioOrdine=esercizioOrdine;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cdNumOrdine]
	 **/
	public String getCdNumOrdine() {
		return cdNumOrdine;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cdNumOrdine]
	 **/
	public void setCdNumOrdine(String cdNumOrdine)  {
		this.cdNumOrdine=cdNumOrdine;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [numOrdine]
	 **/
	public Integer getNumOrdine() {
		return numOrdine;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [numOrdine]
	 **/
	public void setNumOrdine(Integer numOrdine)  {
		this.numOrdine=numOrdine;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [rigaOrdine]
	 **/
	public Integer getRigaOrdine() {
		return rigaOrdine;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [rigaOrdine]
	 **/
	public void setRigaOrdine(Integer rigaOrdine)  {
		this.rigaOrdine=rigaOrdine;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [consOrdine]
	 **/
	public Integer getConsOrdine() {
		return consOrdine;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [consOrdine]
	 **/
	public void setConsOrdine(Integer consOrdine)  {
		this.consOrdine=consOrdine;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cdsEvasione]
	 **/
	public String getCdsEvasione() {
		return cdsEvasione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cdsEvasione]
	 **/
	public void setCdsEvasione(String cdsEvasione)  {
		this.cdsEvasione=cdsEvasione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [magEvazione]
	 **/
	public String getMagEvazione() {
		return magEvazione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [magEvazione]
	 **/
	public void setMagEvazione(String magEvazione)  {
		this.magEvazione=magEvazione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [eseEvasione]
	 **/
	public Integer getEseEvasione() {
		return eseEvasione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [eseEvasione]
	 **/
	public void setEseEvasione(Integer eseEvasione)  {
		this.eseEvasione=eseEvasione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [nummagEvasione]
	 **/
	public String getNummagEvasione() {
		return nummagEvasione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [nummagEvasione]
	 **/
	public void setNummagEvasione(String nummagEvasione)  {
		this.nummagEvasione=nummagEvasione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [numEvasione]
	 **/
	public Long getNumEvasione() {
		return numEvasione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [numEvasione]
	 **/
	public void setNumEvasione(Long numEvasione)  {
		this.numEvasione=numEvasione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [rigaEvasione]
	 **/
	public Long getRigaEvasione() {
		return rigaEvasione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [rigaEvasione]
	 **/
	public void setRigaEvasione(Long rigaEvasione)  {
		this.rigaEvasione=rigaEvasione;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [dataBolla]
	 **/
	public java.sql.Timestamp getDataBolla() {
		return dataBolla;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [dataBolla]
	 **/
	public void setDataBolla(java.sql.Timestamp dataBolla)  {
		this.dataBolla=dataBolla;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [numeroBolla]
	 **/
	public String getNumeroBolla() {
		return numeroBolla;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [numeroBolla]
	 **/
	public void setNumeroBolla(String numeroBolla)  {
		this.numeroBolla=numeroBolla;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [idTransito]
	 **/
	public Long getIdTransito() {
		return idTransito;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [idTransito]
	 **/
	public void setIdTransito(Long idTransito)  {
		this.idTransito=idTransito;
	}

	public String getTi_documento() {
		return ti_documento;
	}

	public void setTi_documento(String ti_documento) {
		this.ti_documento = ti_documento;
	}

	public Integer getEser_buono_car_sca() {
		return eser_buono_car_sca;
	}

	public void setEser_buono_car_sca(Integer eser_buono_car_sca) {
		this.eser_buono_car_sca = eser_buono_car_sca;
	}

	public Long getPg_buono_c_s() {
		return pg_buono_c_s;
	}

	public void setPg_buono_c_s(Long pg_buono_c_s) {
		this.pg_buono_c_s = pg_buono_c_s;
	}

	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [pgInventario]
	 **/
	public Long getPgInventario() {
		return pgInventario;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [pgInventario]
	 **/
	public void setPgInventario(Long pgInventario)  {
		this.pgInventario=pgInventario;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [nrInventario]
	 **/
	public Long getNrInventario() {
		return nrInventario;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [nrInventario]
	 **/
	public void setNrInventario(Long nrInventario)  {
		this.nrInventario=nrInventario;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [progressivo]
	 **/
	public Long getProgressivo() {
		return progressivo;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [progressivo]
	 **/
	public void setProgressivo(Long progressivo)  {
		this.progressivo=progressivo;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [pgRigaAss]
	 **/
	public java.math.BigDecimal getPgRigaAss() {
		return pgRigaAss;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [pgRigaAss]
	 **/
	public void setPgRigaAss(java.math.BigDecimal pgRigaAss)  {
		this.pgRigaAss=pgRigaAss;
	}
}
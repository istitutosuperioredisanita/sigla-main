/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 19/03/2024
 */
package it.cnr.contab.ordmag.magazzino.bulk;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.KeyedPersistent;
public class ChiusuraAnnoCatGrpVoceEpKey extends OggettoBulk implements KeyedPersistent {
	private Integer pgChiusura;
	private Integer anno;
	private String tipoChiusura;
	private String cdCategoriaGruppo;
	private Integer esercizio;
	private String cdVoceEp;
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: CHIUSURA_ANNO_CATGRP_VOCE_EP
	 **/
	public ChiusuraAnnoCatGrpVoceEpKey() {
		super();
	}
	public ChiusuraAnnoCatGrpVoceEpKey(Integer pgChiusura, Integer anno, String tipoChiusura, String cdCategoriaGruppo, Integer esercizio, String cdVoceEp) {
		super();
		this.pgChiusura=pgChiusura;
		this.anno=anno;
		this.tipoChiusura=tipoChiusura;
		this.cdCategoriaGruppo=cdCategoriaGruppo;
		this.esercizio=esercizio;
		this.cdVoceEp=cdVoceEp;
	}
	public boolean equalsByPrimaryKey(Object o) {
		if (this== o) return true;
		if (!(o instanceof ChiusuraAnnoCatGrpVoceEpKey)) return false;
		ChiusuraAnnoCatGrpVoceEpKey k = (ChiusuraAnnoCatGrpVoceEpKey) o;
		if (!compareKey(getPgChiusura(), k.getPgChiusura())) return false;
		if (!compareKey(getAnno(), k.getAnno())) return false;
		if (!compareKey(getTipoChiusura(), k.getTipoChiusura())) return false;
		if (!compareKey(getCdCategoriaGruppo(), k.getCdCategoriaGruppo())) return false;
		if (!compareKey(getEsercizio(), k.getEsercizio())) return false;
		if (!compareKey(getCdVoceEp(), k.getCdVoceEp())) return false;
		return true;
	}
	public int primaryKeyHashCode() {
		int i = 0;
		i = i + calculateKeyHashCode(getPgChiusura());
		i = i + calculateKeyHashCode(getAnno());
		i = i + calculateKeyHashCode(getTipoChiusura());
		i = i + calculateKeyHashCode(getCdCategoriaGruppo());
		i = i + calculateKeyHashCode(getEsercizio());
		i = i + calculateKeyHashCode(getCdVoceEp());
		return i;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Progressivo record chiusura]
	 **/
	public void setPgChiusura(Integer pgChiusura)  {
		this.pgChiusura=pgChiusura;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Progressivo record chiusura]
	 **/
	public Integer getPgChiusura() {
		return pgChiusura;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Anno di chiusura]
	 **/
	public void setAnno(Integer anno)  {
		this.anno=anno;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Anno di chiusura]
	 **/
	public Integer getAnno() {
		return anno;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [M=Magazzino; I=Inventario]
	 **/
	public void setTipoChiusura(String tipoChiusura)  {
		this.tipoChiusura=tipoChiusura;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [M=Magazzino; I=Inventario]
	 **/
	public String getTipoChiusura() {
		return tipoChiusura;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice categoria/gruppo]
	 **/
	public void setCdCategoriaGruppo(String cdCategoriaGruppo)  {
		this.cdCategoriaGruppo=cdCategoriaGruppo;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice categoria/gruppo]
	 **/
	public String getCdCategoriaGruppo() {
		return cdCategoriaGruppo;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Esercizio.]
	 **/
	public void setEsercizio(Integer esercizio)  {
		this.esercizio=esercizio;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Esercizio.]
	 **/
	public Integer getEsercizio() {
		return esercizio;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Codice Identificativo completo del conto del piano.]
	 **/
	public void setCdVoceEp(String cdVoceEp)  {
		this.cdVoceEp=cdVoceEp;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Codice Identificativo completo del conto del piano.]
	 **/
	public String getCdVoceEp() {
		return cdVoceEp;
	}
}
/*
 * Copyright (C) 2020  Consiglio Nazionale delle Ricerche
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 21/09/2017
 */
package it.cnr.contab.ordmag.magazzino.bulk;

import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.persistency.Persistent;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Optional;

public class V_dettaglio_lotti_magBulk extends OggettoBulk implements Persistent {
	private static final long serialVersionUID = 1L;
	private String tipoRecord;
	private String cdCds;
	private String cdMagazzino;
	private Integer esercizio;
	private String cdNumeratoreMag;
	private Integer pgLotto;
	private String cdCdsMag;
	private String dsMagazzino;
	private String cdMagazzinoMag;
	private String cdBeneServizio;
	private String ds_bene_servizio;
	private String cd_categoria_padre;
	private String cd_proprio;
	private String unita_misura;
	private BigDecimal giacenzaAttuale;
	private BigDecimal quantitaCarico;
	private java.sql.Timestamp dtCarico;
	private BigDecimal quantitaScarico;
	private java.sql.Timestamp dtScarico;
	private BigDecimal quantitaApertura;
	private java.sql.Timestamp dtMovimentoChiusura;
	private Integer annoRiferimentoMovimento;
	private BigDecimal qtaInizioAnno;
	private BigDecimal qtaCaricoAnno;
	private BigDecimal qtaScaricoAnno;
	private BigDecimal qtaGiacenzaAnno;

	public String getTipoRecord() {
		return tipoRecord;
	}

	public void setTipoRecord(String tipoRecord) {
		this.tipoRecord = tipoRecord;
	}

	public String getCdCds() {
		return cdCds;
	}

	public void setCdCds(String cdCds) {
		this.cdCds = cdCds;
	}

	public String getCdMagazzino() {
		return cdMagazzino;
	}

	public void setCdMagazzino(String cdMagazzino) {
		this.cdMagazzino = cdMagazzino;
	}

	public Integer getEsercizio() {
		return esercizio;
	}

	public void setEsercizio(Integer esercizio) {
		this.esercizio = esercizio;
	}

	public String getCdNumeratoreMag() {
		return cdNumeratoreMag;
	}

	public void setCdNumeratoreMag(String cdNumeratoreMag) {
		this.cdNumeratoreMag = cdNumeratoreMag;
	}

	public Integer getPgLotto() {
		return pgLotto;
	}

	public void setPgLotto(Integer pgLotto) {
		this.pgLotto = pgLotto;
	}

	public String getCdCdsMag() {
		return cdCdsMag;
	}

	public void setCdCdsMag(String cdCdsMag) {
		this.cdCdsMag = cdCdsMag;
	}

	public String getDsMagazzino() {
		return dsMagazzino;
	}
	public void setDsMagazzino(String dsMagazzino) {
		this.dsMagazzino = dsMagazzino;
	}
	public String getCdMagazzinoMag() {
		return cdMagazzinoMag;
	}

	public void setCdMagazzinoMag(String cdMagazzinoMag) {
		this.cdMagazzinoMag = cdMagazzinoMag;
	}

	public String getCdBeneServizio() {
		return cdBeneServizio;
	}

	public void setCdBeneServizio(String cdBeneServizio) {
		this.cdBeneServizio = cdBeneServizio;
	}

	public String getDs_bene_servizio() {
		return ds_bene_servizio;
	}

	public void setDs_bene_servizio(String ds_bene_servizio) {
		this.ds_bene_servizio = ds_bene_servizio;
	}

	public String getCd_categoria_padre() {
		return cd_categoria_padre;
	}

	public void setCd_categoria_padre(String cd_categoria_padre) {
		this.cd_categoria_padre = cd_categoria_padre;
	}

	public String getCd_proprio() {
		return cd_proprio;
	}

	public void setCd_proprio(String cd_proprio) {
		this.cd_proprio = cd_proprio;
	}

	public String getUnita_misura() {
		return unita_misura;
	}

	public void setUnita_misura(String unita_misura) {
		this.unita_misura = unita_misura;
	}

	public BigDecimal getGiacenzaAttuale() {
		return giacenzaAttuale;
	}

	public void setGiacenzaAttuale(BigDecimal giacenzaAttuale) {
		this.giacenzaAttuale = giacenzaAttuale;
	}

	public BigDecimal getQuantitaCarico() {
		return quantitaCarico;
	}

	public void setQuantitaCarico(BigDecimal quantitaCarico) {
		this.quantitaCarico = quantitaCarico;
	}

	public Timestamp getDtCarico() {
		return dtCarico;
	}

	public void setDtCarico(Timestamp dtCarico) {
		this.dtCarico = dtCarico;
	}

	public BigDecimal getQuantitaScarico() {
		return quantitaScarico;
	}

	public void setQuantitaScarico(BigDecimal quantitaScarico) {
		this.quantitaScarico = quantitaScarico;
	}

	public Timestamp getDtScarico() {
		return dtScarico;
	}

	public void setDtScarico(Timestamp dtScarico) {
		this.dtScarico = dtScarico;
	}

	public BigDecimal getQtaInizioAnno() {
		return Optional.ofNullable(qtaInizioAnno).orElse(BigDecimal.ZERO);
	}

	public void setQtaInizioAnno(BigDecimal qtaInizioAnno) {
		this.qtaInizioAnno = qtaInizioAnno;
	}

	public BigDecimal getQtaCaricoAnno() {
		return Optional.ofNullable(qtaCaricoAnno).orElse(BigDecimal.ZERO);
	}

	public void setQtaCaricoAnno(BigDecimal qtaCaricoAnno) {
		this.qtaCaricoAnno=qtaCaricoAnno;
	}

	public BigDecimal getQtaScaricoAnno() {
		return Optional.ofNullable(qtaScaricoAnno).orElse(BigDecimal.ZERO);
	}

	public void setQtaScaricoAnno(BigDecimal qtaScaricoAnno) {
		this.qtaScaricoAnno = qtaScaricoAnno;
	}

	public BigDecimal getQtaGiacenzaAnno() {
		return (getQtaInizioAnno().add(getQtaCaricoAnno())).subtract(getQtaScaricoAnno());
	}

	public void setQtaGiacenzaAnno(BigDecimal qtaGiacenzaAnno) {
		this.qtaGiacenzaAnno = qtaGiacenzaAnno;
	}

	public BigDecimal getQuantitaApertura() {
		return quantitaApertura;
	}

	public void setQuantitaApertura(BigDecimal quantitaApertura) {
		this.quantitaApertura = quantitaApertura;
	}

	public Timestamp getDtMovimentoChiusura() {
		return dtMovimentoChiusura;
	}

	public void setDtMovimentoChiusura(Timestamp dtMovimentoChiusura) {
		this.dtMovimentoChiusura = dtMovimentoChiusura;
	}

	public Integer getAnnoRiferimentoMovimento() {
		return annoRiferimentoMovimento;
	}

	public void setAnnoRiferimentoMovimento(Integer annoRiferimentoMovimento) {
		this.annoRiferimentoMovimento = annoRiferimentoMovimento;
	}
}
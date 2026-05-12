/*
 * Copyright (C) 2019  Consiglio Nazionale delle Ricerche
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
 * Created on Apr 6, 2006
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package it.cnr.contab.doccont00.core.bulk;

import it.cnr.contab.anagraf00.core.bulk.BancaBulk;
import it.cnr.contab.anagraf00.core.bulk.Modalita_pagamentoBulk;
import it.cnr.jada.bulk.BulkList;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class V_doc_passivo_obbligazione_wizardBulk extends V_doc_passivo_obbligazioneBulk {
	protected Mandato_rigaBulk mandatoRiga;

	protected String descrizioneRigaMandatoWizard;

	protected String cdElementoVoce;
	protected BigDecimal imponibileRigaMandatoWizard;
	protected BigDecimal impostaRigaMandatoWizard;
	protected BigDecimal importoRigaMandatoWizard;
	protected BancaBulk bancaRigaDocumentoWizard;
	protected Modalita_pagamentoBulk modalitaPagamentoRigaDocumentoWizard;
	protected List<Modalita_pagamentoBulk> modalitaPagamentoOptions = new BulkList<>();

	protected List<BancaBulk> bancaOptions = new BulkList<>();

	public V_doc_passivo_obbligazione_wizardBulk() {
		super();
	}

	public Mandato_rigaBulk getMandatoRiga() {
		return mandatoRiga;
	}

	public void setMandatoRiga(Mandato_rigaBulk mandatoRiga) {
		this.mandatoRiga = mandatoRiga;
	}

	public String getDescrizioneRigaMandatoWizard() {
		return descrizioneRigaMandatoWizard;
	}

	public void setDescrizioneRigaMandatoWizard(String descrizioneRigaMandatoWizard) {
		this.descrizioneRigaMandatoWizard = descrizioneRigaMandatoWizard;
	}

	public String getCdElementoVoce() {
		return cdElementoVoce;
	}

	public void setCdElementoVoce(String cdElementoVoce) {
		this.cdElementoVoce = cdElementoVoce;
	}

	//Ritorna il valore dell'imponibile indicato dall'utente per la creazione del mandato
	public BigDecimal getImponibileRigaMandatoWizard() {
		return imponibileRigaMandatoWizard;
	}

	public void setImponibileRigaMandatoWizard(BigDecimal imponibileRigaMandatoWizard) {
		this.imponibileRigaMandatoWizard = imponibileRigaMandatoWizard;
	}

	//Ritorna il valore dell'imposta indicata dall'utente per la creazione del mandato
	public BigDecimal getImpostaRigaMandatoWizard() {
		return impostaRigaMandatoWizard;
	}

	public void setImpostaRigaMandatoWizard(BigDecimal impostaRigaMandatoWizard) {
		this.impostaRigaMandatoWizard = impostaRigaMandatoWizard;
	}

	//Ritorna l'oggetto BancaBulk indicata dall'utente per la creazione del mandato
	public BancaBulk getBancaRigaDocumentoWizard() {
		return bancaRigaDocumentoWizard;
	}

	public void setBancaRigaDocumentoWizard(BancaBulk bancaRigaDocumentoWizard) {
		this.bancaRigaDocumentoWizard = bancaRigaDocumentoWizard;
	}

	//Ritorna la modalità di pagamento indicata dall'utente per la creazione del mandato
	public Modalita_pagamentoBulk getModalitaPagamentoRigaDocumentoWizard() {
		return modalitaPagamentoRigaDocumentoWizard;
	}

	public void setModalitaPagamentoRigaDocumentoWizard(Modalita_pagamentoBulk modalitaPagamentoRigaDocumentoWizard) {
		this.modalitaPagamentoRigaDocumentoWizard = modalitaPagamentoRigaDocumentoWizard;
	}

	public List<Modalita_pagamentoBulk> getModalitaPagamentoOptions() {
		return modalitaPagamentoOptions;
	}

	public void setModalitaPagamentoOptions(List<Modalita_pagamentoBulk> modalitaPagamentoOptions) {
		this.modalitaPagamentoOptions = modalitaPagamentoOptions;
	}

	public List<BancaBulk> getBancaOptions() {
		return bancaOptions;
	}

	public void setBancaOptions(List<BancaBulk> bancaOptions) {
		this.bancaOptions = bancaOptions;
	}

	//Ritorna il valore dell'importo indicato dall'utente per la creazione del mandato
	public BigDecimal getImportoRigaMandatoWizard() {
		return importoRigaMandatoWizard;
	}

	public void setImportoRigaMandatoWizard(BigDecimal importoRigaMandatoWizard) {
		this.importoRigaMandatoWizard = importoRigaMandatoWizard;
	}

	public Modalita_pagamentoBulk getModalitaPagamentoRigaDocumento() {
		if (Optional.ofNullable(this.getModalitaPagamentoRigaDocumentoWizard()).isPresent())
			return this.getModalitaPagamentoRigaDocumentoWizard();
		if (Optional.ofNullable(this.getCd_terzo_cessionario()).isPresent())
			return this.getModalitaPagamentoOptions().stream()
					.filter(el->el.getRif_modalita_pagamento().getFl_per_cessione())
					.filter(el->this.getCd_terzo_cessionario().equals(el.getCd_terzo_delegato()))
					.findAny()
					.orElse(null);
		else
			return this.getModalitaPagamentoOptions().stream()
					.filter(el->!el.getRif_modalita_pagamento().getFl_per_cessione())
					.filter(el->el.getCd_modalita_pag().equals(this.getCd_modalita_pag()))
					.findAny()
					.orElse(null);
	}

	//Ritorna il codice della modalità di pagamento da utilizzare per la creazione del mandato
	public String getCdModalitaPagamentoRigaDocumento() {
		return Optional.ofNullable(this.getModalitaPagamentoRigaDocumento()).map(Modalita_pagamentoBulk::getCd_modalita_pag).orElse(this.getCd_modalita_pag());
	}


	//Ritorna l'oggetto BancaBulk indicata dall'utente per la creazione del mandato
	public BancaBulk getBancaRigaDocumento() {
		if (Optional.ofNullable(this.getBancaRigaDocumentoWizard()).isPresent())
			return this.getBancaRigaDocumentoWizard();
		if (Optional.ofNullable(this.getCd_terzo_cessionario()).isPresent())
			return this.getBancaOptions().stream()
					.filter(el->this.getCd_terzo_cessionario().equals(el.getCd_terzo_delegato()))
					.filter(el->this.getPg_banca().equals(el.getPg_banca_delegato()))
					.findAny()
					.orElse(null);
		else
			return this.getBancaOptions().stream()
					.filter(el->this.getPg_banca().equals(el.getPg_banca()))
					.findAny()
					.orElse(null);
	}

	//Ritorna il progressivo banca da utilizzare per la creazione del mandato
	public Long getPgBancaRigaDocumento() {
		return Optional.ofNullable(this.getBancaRigaDocumento()).map(BancaBulk::getPg_banca).orElse(this.getPg_banca());
	}

	//Ritorna il valore dell'imponibile da utilizzare per la creazione del mandato
	public BigDecimal getImponibileRigaMandato() {
		if (Optional.ofNullable(this.getImponibileRigaMandatoWizard()).isPresent())
			return this.getImponibileRigaMandatoWizard();
		if (this.isFatturaPassiva())
			return Optional.ofNullable(this.getIm_imponibile_doc_amm()).orElse(BigDecimal.ZERO);
		return BigDecimal.ZERO;
	}

	//Ritorna il valore dell'imponibile da utilizzare per la creazione del mandato
	public BigDecimal getImpostaRigaMandato() {
		if (Optional.ofNullable(this.getImpostaRigaMandatoWizard()).isPresent())
			return this.getImpostaRigaMandatoWizard();
		if (this.isFatturaPassiva())
			return Optional.ofNullable(this.getIm_iva_doc_amm()).orElse(BigDecimal.ZERO);
		return BigDecimal.ZERO;
	}

	public BigDecimal getImportoRigaMandato() {
		if (Optional.ofNullable(this.getImportoRigaMandatoWizard()).isPresent())
			return this.getImportoRigaMandatoWizard();
		if (this.isFatturaPassiva())
			return Optional.ofNullable(this.getImponibileRigaMandatoWizard()).orElse(BigDecimal.ZERO).add(Optional.ofNullable(this.getImpostaRigaMandatoWizard()).orElse(BigDecimal.ZERO));
		return Optional.ofNullable(this.getIm_totale_doc_amm()).orElse(BigDecimal.ZERO);
	}
}

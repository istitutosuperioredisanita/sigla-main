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

package it.cnr.contab.docamm00.docs.bulk;

public class Nota_di_credito_attiva_riga_ecoBulk extends Fattura_attiva_riga_ecoBulk {
	private Nota_di_credito_attiva_rigaBulk notaDiCreditoAttivaRiga;

	public Nota_di_credito_attiva_riga_ecoBulk() {
		super();
	}

	public Nota_di_credito_attiva_riga_ecoBulk(String cd_cds, String cd_unita_organizzativa, Integer esercizio, Long pg_fattura_attiva, Long progressivo_riga, Long progressivo_riga_eco) {
		super(cd_cds,cd_unita_organizzativa,esercizio,pg_fattura_attiva,progressivo_riga,progressivo_riga_eco);
		setNotaDiCreditoAttivaRiga(new Nota_di_credito_attiva_rigaBulk(cd_cds,cd_unita_organizzativa,esercizio,pg_fattura_attiva,progressivo_riga));
	}

	@Override
	public Fattura_attiva_rigaBulk getFattura_attiva_riga() {
		return this.getNotaDiCreditoAttivaRiga();
	}

	@Override
	public void setFattura_attiva_riga(Fattura_attiva_rigaBulk fattura_attiva_riga) {
		setNotaDiCreditoAttivaRiga((Nota_di_credito_attiva_rigaBulk) fattura_attiva_riga);
	}

	public String getCd_cds() {
		Nota_di_credito_attiva_rigaBulk notaDiCreditoAttivaRiga = this.getNotaDiCreditoAttivaRiga();
		if (notaDiCreditoAttivaRiga == null)
			return null;
		return notaDiCreditoAttivaRiga.getCd_cds();
	}

	public String getCd_unita_organizzativa() {
		Nota_di_credito_attiva_rigaBulk notaDiCreditoAttivaRiga = this.getNotaDiCreditoAttivaRiga();
		if (notaDiCreditoAttivaRiga == null)
			return null;
		return notaDiCreditoAttivaRiga.getCd_unita_organizzativa();
	}

	public Integer getEsercizio() {
		Nota_di_credito_attiva_rigaBulk notaDiCreditoAttivaRiga = this.getNotaDiCreditoAttivaRiga();
		if (notaDiCreditoAttivaRiga == null)
			return null;
		return notaDiCreditoAttivaRiga.getEsercizio();
	}

	public Nota_di_credito_attiva_rigaBulk getNotaDiCreditoAttivaRiga() {
	return notaDiCreditoAttivaRiga;
}

	public void setNotaDiCreditoAttivaRiga(Nota_di_credito_attiva_rigaBulk notaDiCreditoAttivaRiga) {
		this.notaDiCreditoAttivaRiga = notaDiCreditoAttivaRiga;
	}
}

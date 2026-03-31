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

package it.cnr.contab.coepcoan00.core.bulk;

import it.cnr.contab.config00.latt.bulk.WorkpackageBulk;
import it.cnr.contab.config00.pdcep.bulk.Voce_analiticaBulk;
import it.cnr.contab.docamm00.docs.bulk.IDocumentoAmministrativoRigaBulk;

import java.math.BigDecimal;

public interface IDocumentoDetailAnaCogeBulk {
	IDocumentoDetailEcoCogeBulk getFather();

	Voce_analiticaBulk getVoce_analitica();

	Integer getEsercizio_voce_ana();

	String getCd_voce_ana();

	WorkpackageBulk getLinea_attivita();

	String getCd_linea_attivita();

	String getCd_centro_responsabilita();

	BigDecimal getImporto();
}

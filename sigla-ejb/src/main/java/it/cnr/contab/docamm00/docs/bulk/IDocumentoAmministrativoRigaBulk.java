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

import it.cnr.contab.anagraf00.core.bulk.BancaBulk;
import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.contab.anagraf00.tabrif.bulk.Rif_modalita_pagamentoBulk;
import it.cnr.contab.coepcoan00.core.bulk.IDocumentoCogeBulk;
import it.cnr.contab.coepcoan00.core.bulk.IDocumentoDetailEcoCogeBulk;
import it.cnr.contab.config00.pdcep.bulk.ContoBulk;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Insert the type's description here.
 * Creation date: (12/13/2001 4:02:04 PM)
 * @author: Roberto Peli
 */
public interface IDocumentoAmministrativoRigaBulk extends IDocumentoDetailEcoCogeBulk  {
	public enum tipo {
		GEN_CORA_E, GEN_CORV_E
	}


	public final static String NON_RIPORTATO = "N";
	public final static String RIPORTATO = "R";

	/**
	 * Insert the method's description here.
	 * Creation date: (12/14/2001 2:38:27 PM)
	 * @return it.cnr.contab.docamm00.docs.bulk.IDocumentoAmministrativoBulk
	 */
	IDocumentoAmministrativoRigaBulk getAssociatedDetail();
	/**
	 * Insert the method's description here.
	 * Creation date: (12/14/2001 2:38:27 PM)
	 * @return it.cnr.contab.docamm00.docs.bulk.IDocumentoAmministrativoBulk
	 */
	IDocumentoAmministrativoBulk getFather();
	java.math.BigDecimal getIm_diponibile_nc();
	/**
	 * Insert the method's description here.
	 * Creation date: (12/14/2001 2:38:27 PM)
	 * @return it.cnr.contab.docamm00.docs.bulk.IDocumentoAmministrativoBulk
	 */
	java.math.BigDecimal getIm_imponibile();
	/**
	 * Insert the method's description here.
	 * Creation date: (12/14/2001 2:38:27 PM)
	 * @return it.cnr.contab.docamm00.docs.bulk.IDocumentoAmministrativoBulk
	 */
	java.math.BigDecimal getIm_iva();
	/**
	 * Insert the method's description here.
	 * Creation date: (12/14/2001 2:38:27 PM)
	 * @return it.cnr.contab.docamm00.docs.bulk.IDocumentoAmministrativoBulk
	 */
	IDocumentoAmministrativoRigaBulk getOriginalDetail();
	/**
	 * Insert the method's description here.
	 * Creation date: (12/14/2001 2:38:27 PM)
	 * @return it.cnr.contab.docamm00.docs.bulk.IDocumentoAmministrativoBulk
	 */
	it.cnr.contab.doccont00.core.bulk.IScadenzaDocumentoContabileBulk getScadenzaDocumentoContabile();
	/**
	 * Insert the method's description here.
	 * Creation date: (12/14/2001 2:38:27 PM)
	 * @return it.cnr.contab.docamm00.docs.bulk.IDocumentoAmministrativoBulk
	 */
	it.cnr.contab.docamm00.tabrif.bulk.Voce_ivaBulk getVoce_iva();

	boolean isDirectlyLinkedToDC();

	boolean isRiportata();

	void setIm_diponibile_nc(java.math.BigDecimal im_diponibile_nc);

	java.lang.Integer getCd_terzo();

	TerzoBulk getTerzo();

	java.sql.Timestamp getDt_da_competenza_coge();

	java.sql.Timestamp getDt_a_competenza_coge();

	BancaBulk getBanca();

	void setBanca(BancaBulk bancaBulk);

	Rif_modalita_pagamentoBulk getModalita_pagamento();

	void setModalita_pagamento(Rif_modalita_pagamentoBulk modalita_pagamento);

	String getDs_riga();

	default BigDecimal getIm_riga() {
		return Optional.ofNullable(getIm_imponibile()).orElse(BigDecimal.ZERO).add(
				Optional.ofNullable(getIm_iva()).orElse(BigDecimal.ZERO)
		);
	}

	@Override
	default BigDecimal getImportoCostoEco() {
		return this.getIm_riga();
	}
}

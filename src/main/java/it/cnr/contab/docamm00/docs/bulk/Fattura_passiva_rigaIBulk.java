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
import it.cnr.contab.anagraf00.tabrif.bulk.Rif_termini_pagamentoBulk;
import it.cnr.contab.docamm00.tabrif.bulk.Voce_ivaBulk;
import it.cnr.contab.doccont00.core.bulk.Mandato_rigaIBulk;
import it.cnr.jada.bulk.BulkList;

/**
 * Insert the type's description here.
 * Creation date: (10/25/2001 11:52:17 AM)
 * @author: Roberto Peli
 */
public class Fattura_passiva_rigaIBulk extends Fattura_passiva_rigaBulk {

	private Fattura_passiva_IBulk fattura_passivaIBulk;

	private java.math.BigDecimal saldo = new java.math.BigDecimal(0);
	private java.math.BigDecimal im_totale_storni = new java.math.BigDecimal(0);
	private java.math.BigDecimal im_totale_addebiti = new java.math.BigDecimal(0);
	private java.math.BigDecimal im_riga_sdoppia;

    /*
     * lista righe mandati associati, utilizzato per l'integrazione con i brevetti
     */
    private BulkList<Mandato_rigaIBulk> mandatiRighe = new BulkList();
	/**
	 * Fattura_passiva_rigaIBulk constructor comment.
	 */
	public Fattura_passiva_rigaIBulk() {
		super();
	}
	/**
	 * Fattura_passiva_rigaIBulk constructor comment.
	 * @param cd_cds java.lang.String
	 * @param cd_unita_organizzativa java.lang.String
	 * @param esercizio java.lang.Integer
	 * @param pg_fattura_passiva java.lang.Long
	 * @param progressivo_riga java.lang.Long
	 */
	public Fattura_passiva_rigaIBulk(String cd_cds, String cd_unita_organizzativa, Integer esercizio, Long pg_fattura_passiva, Long progressivo_riga) {
		super(cd_cds, cd_unita_organizzativa, esercizio, pg_fattura_passiva, progressivo_riga);
		setFattura_passiva( new Fattura_passiva_IBulk(cd_cds, cd_unita_organizzativa,  esercizio,  pg_fattura_passiva));
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (10/25/2001 12:03:34 PM)
	 * @return it.cnr.contab.docamm00.docs.bulk.Fattura_passiva_IBulk
	 */
	public Fattura_passivaBulk getFattura_passiva() {

		return getFattura_passivaI();
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (10/25/2001 12:03:34 PM)
	 * @return it.cnr.contab.docamm00.docs.bulk.Fattura_passiva_IBulk
	 */
	public Fattura_passiva_IBulk getFattura_passivaI() {
		return fattura_passivaIBulk;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (12/17/2001 2:14:22 PM)
	 * @return java.math.BigDecimal
	 */
	public java.math.BigDecimal getIm_totale_addebiti() {
		return im_totale_addebiti;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (11/6/2001 3:20:41 PM)
	 * @return java.math.BigDecimal
	 */
	public java.math.BigDecimal getIm_totale_storni() {

		return im_totale_storni;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (2/13/2002 1:28:11 PM)
	 * @return it.cnr.contab.docamm00.docs.bulk.IDocumentoAmministrativoBulk
	 */
	public IDocumentoAmministrativoRigaBulk getOriginalDetail() {
		return null;
	}

	/**
	 * Insert the method's description here.
	 * Creation date: (11/6/2001 3:38:46 PM)
	 * @return java.math.BigDecimal
	 */
	public java.math.BigDecimal getSaldo() {

		if (getIm_iva() == null) setIm_iva(new java.math.BigDecimal(0));

		java.math.BigDecimal totRiga = getIm_imponibile().add(getIm_iva());
		return totRiga.subtract(getIm_totale_storni()).add(getIm_totale_addebiti());
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (11/22/2001 11:59:14 AM)
	 * @return boolean
	 */
	public boolean hasAddebiti() {

		return	getIm_totale_addebiti() != null &&
				getIm_totale_addebiti().compareTo(new java.math.BigDecimal(0).setScale(2, java.math.BigDecimal.ROUND_HALF_UP)) > 0;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (11/22/2001 11:59:14 AM)
	 * @return boolean
	 */
	public boolean hasStorni() {

		return	getIm_totale_storni() != null &&
				getIm_totale_storni().compareTo(new java.math.BigDecimal(0).setScale(2, java.math.BigDecimal.ROUND_HALF_UP)) > 0;
	}

	public void setFattura_passiva(Fattura_passivaBulk fattura_passiva) {

		setFattura_passivaI((Fattura_passiva_IBulk)fattura_passiva);
	}

	public void setFattura_passivaI(Fattura_passiva_IBulk newFattura_passivaIBulk) {
		fattura_passivaIBulk = newFattura_passivaIBulk;
	}

	public void setIm_totale_addebiti(java.math.BigDecimal newIm_totale_addebiti) {
		im_totale_addebiti = newIm_totale_addebiti;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (11/6/2001 3:20:41 PM)
	 * @param newIm_totale_storni java.math.BigDecimal
	 */
	public void setIm_totale_storni(java.math.BigDecimal newIm_totale_storni) {
		im_totale_storni = newIm_totale_storni;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (11/6/2001 3:38:46 PM)
	 * @param newSaldo java.math.BigDecimal
	 */
	public void setSaldo(java.math.BigDecimal newSaldo) {
		saldo = newSaldo;
	}
	public java.math.BigDecimal getIm_riga_sdoppia() {
		return im_riga_sdoppia;
	}
	public void setMandatiRighe(BulkList<Mandato_rigaIBulk> mandatiRighe) {
		this.mandatiRighe = mandatiRighe;
	}
	public void setIm_riga_sdoppia(java.math.BigDecimal im_riga_sdoppia) {
		this.im_riga_sdoppia = im_riga_sdoppia;
	}

	public BulkList<Mandato_rigaIBulk> getMandatiRighe() {
		return mandatiRighe;
	}

	public static Fattura_passiva_rigaIBulk copyByRigaDocumento(Fattura_passiva_rigaBulk origine) {
		Fattura_passiva_rigaIBulk nuovoDettaglio = new Fattura_passiva_rigaIBulk();
		if (origine.getFornitore() != null)
			nuovoDettaglio.setFornitore((TerzoBulk) origine.getFornitore().clone());
		if (origine.getCessionario() != null)
			nuovoDettaglio.setCessionario((TerzoBulk) origine.getCessionario().clone());
		if (origine.getTermini_pagamento() != null)
			nuovoDettaglio.setTermini_pagamento((Rif_termini_pagamentoBulk) origine.getTermini_pagamento().clone());
		if (origine.getModalita_pagamento() != null)
			nuovoDettaglio.setModalita_pagamento((Rif_modalita_pagamentoBulk) origine.getModalita_pagamento().clone());
		if (origine.getBanca() != null)
			nuovoDettaglio.setBanca((BancaBulk) origine.getBanca().clone());
		if (origine.getVoce_iva() != null)
			nuovoDettaglio.setVoce_iva((Voce_ivaBulk)origine.getVoce_iva().clone());
		nuovoDettaglio.setStato_cofi(origine.getStato_cofi());
		nuovoDettaglio.setDt_da_competenza_coge(origine.getDt_da_competenza_coge());
		nuovoDettaglio.setDt_a_competenza_coge(origine.getDt_a_competenza_coge());
		nuovoDettaglio.setDs_riga_fattura(origine.getDs_riga_fattura());
		nuovoDettaglio.setTi_associato_manrev(origine.getTi_associato_manrev());
		nuovoDettaglio.setBene_servizio(origine.getBene_servizio());
		nuovoDettaglio.setInventariato(origine.isInventariato());
		nuovoDettaglio.setTrovato(origine.getTrovato());
		nuovoDettaglio.setFl_iva_forzata(Boolean.FALSE);
		nuovoDettaglio.setTi_istituz_commerc(origine.getTi_istituz_commerc());
		nuovoDettaglio.setToBeCreated();
		return nuovoDettaglio;
	}
}

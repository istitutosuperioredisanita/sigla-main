/*
 * Copyright (C) 2019  Consiglio Nazionale delle Ricerche
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package it.cnr.contab.docamm00.tabrif.bulk;

import java.rmi.RemoteException;
import java.util.Dictionary;

import it.cnr.contab.anagraf00.core.bulk.AnagraficoBulk;
import it.cnr.contab.inventario00.docs.bulk.Inventario_beniBulk;
import it.cnr.contab.inventario00.tabrif.bulk.Condizione_beneBulk;
import it.cnr.contab.inventario00.tabrif.bulk.Ubicazione_beneBulk;
import it.cnr.contab.util.Utility;
import it.cnr.contab.util.enumeration.TipoIVA;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.util.OrderedHashtable;

/**
 * Dettaglio documento Trasporto/Rientro.
 *
 * CAMPO ANAGRAFICA:
 * - Contiene l'anagrafica di chi ha effettuato il trasporto
 * - Se ritiro delegato (incaricato/vettore) → anagrafica del dipendente
 * - Se ritiro normale → anagrafica dell'utente loggato
 * - Serve per AUDIT/TRACCIABILITÀ
 */
public class Doc_trasporto_rientro_dettBulk extends Doc_trasporto_rientro_dettBase {

	public final static java.lang.String STATO_INSERITO = "INS";
	public final static java.lang.String STATO_DEFINITIVO = "DEF";
	public final static java.lang.String STATO_ANNULLATO = "ANN";
	public final static java.lang.String STATO_PREDISPOSTO_FIRMA = "PAF";

	private final static Dictionary statoTrasportoKeys;

	static {
		statoTrasportoKeys = new OrderedHashtable();
		statoTrasportoKeys.put(STATO_INSERITO, "Inserito");
		statoTrasportoKeys.put(STATO_DEFINITIVO, "Definitivo");
		statoTrasportoKeys.put(STATO_ANNULLATO, "Annullato");
		statoTrasportoKeys.put(STATO_PREDISPOSTO_FIRMA, "Predisposto alla firma");
	}

	private Inventario_beniBulk bene;
	private int gruppi;
	private Doc_trasporto_rientroBulk doc_trasporto_rientro;
	private Boolean fl_accessorio_contestuale = new Boolean(false);
	protected Boolean fl_bene_accessorio;
	private Categoria_gruppo_voceBulk cat_voce;

	/**
	 * ⭐ Anagrafica del trasportatore effettivo.
	 * - Se ritiro delegato → dipendente selezionato nella testata
	 * - Se ritiro normale → utente loggato
	 * Campo per audit/tracciabilità.
	 */
	private AnagraficoBulk anagDipRitiro;

	private Doc_trasporto_rientro_dettBulk docTrasportoRientroDettRif;

	public Doc_trasporto_rientro_dettBulk() {
		super();
	}

	public Doc_trasporto_rientro_dettBulk(java.lang.Long pg_inventario, java.lang.String ti_documento,
										  java.lang.Integer esercizio, java.lang.Long pg_doc_trasporto_rientro,
										  java.lang.Long nr_inventario, java.lang.Integer progressivo) {
		super(pg_inventario, ti_documento, esercizio, pg_doc_trasporto_rientro, nr_inventario, progressivo);
		setDoc_trasporto_rientro(new Doc_trasporto_rientroBulk(
				pg_inventario, ti_documento, esercizio, pg_doc_trasporto_rientro));
		setBene(new Inventario_beniBulk(nr_inventario, pg_inventario, new Long(progressivo.longValue())));
	}

	// ========================================
	// GETTER E SETTER - Anagrafica
	// ========================================

	public AnagraficoBulk getAnagDipRitiro() {
		return anagDipRitiro;
	}

	public void setAnagDipRitiro(AnagraficoBulk anagDipRitiro) {
		this.anagDipRitiro = anagDipRitiro;
	}

	public String getCd_unita_organizzativa() {
		if (anagDipRitiro != null && anagDipRitiro.getCd_unita_organizzativa() != null) {
			return anagDipRitiro.getCd_unita_organizzativa();
		}
		return null;
	}

	// ========================================
	// GETTER E SETTER - Attributi principali
	// ========================================

	public Inventario_beniBulk getBene() {
		return bene;
	}

	public void setBene(Inventario_beniBulk bulk) {
		bene = bulk;
	}

	public Doc_trasporto_rientroBulk getDoc_trasporto_rientro() {
		return doc_trasporto_rientro;
	}

	public void setDoc_trasporto_rientro(Doc_trasporto_rientroBulk bulk) {
		doc_trasporto_rientro = bulk;
	}

	public Doc_trasporto_rientro_dettBulk getDocTrasportoRientroDettRif() {
		return docTrasportoRientroDettRif;
	}

	public void setDocTrasportoRientroDettRif(Doc_trasporto_rientro_dettBulk docTrasportoRientroDettRif) {
		this.docTrasportoRientroDettRif = docTrasportoRientroDettRif;
	}

	// ========================================
	// METODI UTILITY
	// ========================================

	public final java.util.Dictionary getStatoTrasportoKeys() {
		return statoTrasportoKeys;
	}

	public boolean isInserito() {
		return STATO_INSERITO.equals(getStatoTrasporto());
	}

	public boolean isDefinitivo() {
		return STATO_DEFINITIVO.equals(getStatoTrasporto());
	}

	public boolean isAnnullato() {
		return STATO_ANNULLATO.equals(getStatoTrasporto());
	}

	public boolean isPredispostoAllaFirma() {
		return STATO_PREDISPOSTO_FIRMA.equals(getStatoTrasporto());
	}

	public String getCod_bene() {
		if (getNr_inventario() == null || getProgressivo() == null)
			return "";
		java.text.DecimalFormat formato = new java.text.DecimalFormat("000");
		return getNr_inventario().toString() + "-" + formato.format(getProgressivo());
	}

	public Condizione_beneBulk getCondizioneBene() {
		return bene.getCondizioneBene();
	}

	public String getChiaveHash() {
		if (getNr_inventario() == null || getProgressivo() == null)
			return null;
		return getNr_inventario().toString() + "." + getProgressivo().toString() + "." + getEtichetta();
	}

	public OggettoBulk initialize(it.cnr.jada.util.action.CRUDBP bp, it.cnr.jada.action.ActionContext context) {
		bene = new Inventario_beniBulk();
		bene.setTi_commerciale_istituzionale(TipoIVA.ISTITUZIONALE.value());
		return this;
	}

	public OggettoBulk initializeForInsert(it.cnr.jada.util.action.CRUDBP bp, it.cnr.jada.action.ActionContext context) {
		bene = new Inventario_beniBulk();
		bene.setTi_commerciale_istituzionale(TipoIVA.ISTITUZIONALE.value());

		try {
			if (Utility.createConfigurazioneCnrComponentSession()
					.isGestioneEtichettaInventarioBeneAttivo(context.getUserContext())) {
				setQuantita(1L);
			}
		} catch (RemoteException | ComponentException e) {
			// Log error
		}

		return this;
	}

	public boolean isAccessorioContestuale() {
		if (fl_accessorio_contestuale != null) {
			return fl_accessorio_contestuale.booleanValue();
		} else
			return false;
	}

	public boolean isAssociatoConAccessorioContestuale() {
		Doc_trasporto_rientroBulk doc = (Doc_trasporto_rientroBulk) getDoc_trasporto_rientro();
		if (getChiaveHash() == null || doc.getAccessoriContestualiHash() == null)
			return false;
		return (doc.getAccessoriContestualiHash().containsKey(getChiaveHash()));
	}

	public boolean isROcategoriaBene() {
		if (isAccessorioContestuale())
			return true;
		return false;
	}

	public boolean isROcollocazione() {
		if (getBene().getCategoria_Bene() != null)
			return true;
		return false;
	}

	public boolean isROfl_accessorio() {
		return isAccessorioContestuale();
	}

	public boolean isROEtichetta() {
		return isBeneAccessorio();
	}

	public String getEtichetta() {
		if (isBeneAccessorio() || isAccessorioContestuale()) {
			if (getBene() != null &&
					getBene().getBene_principale() != null &&
					getBene().getBene_principale().getEtichetta() != null) {
				return getBene().getBene_principale().getEtichetta();
			}
			return "";
		}
		return getBene() != null && getBene().getEtichetta() != null ? getBene().getEtichetta() : "";
	}

	public Boolean getFl_accessorio_contestuale() {
		return fl_accessorio_contestuale;
	}

	public void setFl_accessorio_contestuale(Boolean boolean1) {
		fl_accessorio_contestuale = boolean1;
	}

	public boolean isBeneAccessorio() {
		if (fl_bene_accessorio != null)
			return fl_bene_accessorio.booleanValue();
		return false;
	}

	public Boolean getFl_bene_accessorio() {
		return fl_bene_accessorio;
	}

	public void setFl_bene_accessorio(Boolean boolean1) {
		fl_bene_accessorio = boolean1;
	}

	public boolean isTotalmenteScaricato() {
		if (getBene() != null && getBene().getFl_totalmente_scaricato() != null)
			return getBene().getFl_totalmente_scaricato().booleanValue();
		return false;
	}

	public boolean isBeneInTransito() {
		if (getBene() != null && getBene().getId_transito_beni_ordini() != null)
			return true;
		return false;
	}

	public boolean isValidoPerTrasporto() {
		boolean statoValido = (getStatoTrasporto() == null || STATO_ANNULLATO.equals(getStatoTrasporto()));
		return !isTotalmenteScaricato() && !isBeneInTransito() && statoValido;
	}

	public boolean isValidoPerRientro() {
		return getDocTrasportoRientroDettRif() != null &&
				getDocTrasportoRientroDettRif().getPgInventario() != null &&
				getDocTrasportoRientroDettRif().getPgDocTrasportoRientro() != null;
	}

	// ========================================
	// METODI DELEGATI
	// ========================================

	public void setPg_inventario(java.lang.Long pg_inventario) {
		this.getDoc_trasporto_rientro().setPg_inventario(pg_inventario);
	}

	public java.lang.Long getPg_inventario() {
		return this.getDoc_trasporto_rientro().getPg_inventario();
	}

	public java.lang.Long getNr_inventario() {
		return this.getBene().getNr_inventario();
	}

	public void setNr_inventario(java.lang.Long nr_inventario) {
		this.getBene().setNr_inventario(nr_inventario);
	}

	public Integer getProgressivo() {
		return new Integer(this.getBene().getProgressivo().intValue());
	}

	public void setProgressivo(Integer progressivo) {
		this.getBene().setProgressivo(new Long(progressivo.longValue()));
	}

	public void setTi_documento(java.lang.String ti_documento) {
		this.getDoc_trasporto_rientro().setTiDocumento(ti_documento);
	}

	public java.lang.String getTi_documento() {
		return this.getDoc_trasporto_rientro().getTiDocumento();
	}

	public void setEsercizio(java.lang.Integer esercizio) {
		this.getDoc_trasporto_rientro().setEsercizio(esercizio);
	}

	public java.lang.Integer getEsercizio() {
		return this.getDoc_trasporto_rientro().getEsercizio();
	}

	public void setPg_doc_trasporto_rientro(java.lang.Long pg_doc_trasporto_rientro) {
		this.getDoc_trasporto_rientro().setPgDocTrasportoRientro(pg_doc_trasporto_rientro);
	}

	public java.lang.Long getPg_doc_trasporto_rientro() {
		return this.getDoc_trasporto_rientro().getPgDocTrasportoRientro();
	}

	public int getGruppi() {
		return gruppi;
	}

	public void setGruppi(int gruppi) {
		this.gruppi = gruppi;
	}

	public it.cnr.contab.anagraf00.core.bulk.TerzoBulk getAssegnatario() {
		return bene.getAssegnatario();
	}

	public String getCollocazione() {
		return bene.getCollocazione();
	}

	public Categoria_gruppo_inventBulk getCategoria_Bene() {
		return bene.getCategoria_Bene();
	}

	public String getDs_bene() {
		return bene.getDs_bene();
	}

	public Ubicazione_beneBulk getUbicazione() {
		return bene.getUbicazione();
	}

	public Categoria_gruppo_voceBulk getCat_voce() {
		return cat_voce;
	}

	public void setCat_voce(Categoria_gruppo_voceBulk cat_voce) {
		this.cat_voce = cat_voce;
	}
}
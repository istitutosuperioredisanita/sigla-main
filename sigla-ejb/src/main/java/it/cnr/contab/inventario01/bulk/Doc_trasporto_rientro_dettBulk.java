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

package it.cnr.contab.inventario01.bulk;

import java.rmi.RemoteException;

import it.cnr.contab.docamm00.tabrif.bulk.Categoria_gruppo_voceBulk;
import it.cnr.contab.inventario00.docs.bulk.Inventario_beniBulk;
import it.cnr.contab.inventario00.tabrif.bulk.Id_inventarioBulk;
import it.cnr.contab.util.Utility;
import it.cnr.contab.util.enumeration.TipoIVA;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ComponentException;

/**
 * Dettaglio documento Trasporto/Rientro.
 *
 * PATTERN: Delega completa alle entità correlate
 * - I campi PK (pg_inventario, ti_documento, esercizio, pg_doc_trasporto_rientro)
 *   sono delegati a doc_trasporto_rientro
 * - I campi PK (nr_inventario, progressivo) sono delegati a bene
 *
 * RELAZIONI DATABASE:
 * - FK verso DOC_TRASPORTO_RIENTRO (testata) → OBBLIGATORIA, ON DELETE CASCADE
 * - FK verso DOC_TRASPORTO_RIENTRO_DETT (riferimento) → per i rientri
 */
public class Doc_trasporto_rientro_dettBulk extends Doc_trasporto_rientro_dettBase {

	// ========================================
	// ATTRIBUTI
	// ========================================

	/**
	 * Bene associato al dettaglio.
	 * Contiene nr_inventario e progressivo (parte della PK).
	 */
	private Inventario_beniBulk bene;

	/**
	 * TESTATA del documento (FOREIGN KEY OBBLIGATORIA).
	 * Contiene pg_inventario, ti_documento, esercizio, pg_doc_trasporto_rientro (parte della PK).
	 * Relazione master-detail con ON DELETE CASCADE.
	 */
	private Doc_trasporto_rientroBulk doc_trasporto_rientro;

	/**
	 * RIFERIMENTO al documento di trasporto originale (per i RIENTRI).
	 * Foreign key self-reference.
	 */
	private Doc_trasporto_rientro_dettBulk docTrasportoRientroDettRif;

	// Altri attributi
	private int gruppi;
	private Boolean fl_accessorio_contestuale = Boolean.FALSE;
	protected Boolean fl_bene_accessorio;
	private Categoria_gruppo_voceBulk cat_voce;

	// ========================================
	// COSTRUTTORI
	// ========================================

	public Doc_trasporto_rientro_dettBulk() {
		super();
		// NON inizializzare nulla qui - lascia che JADA usi la classe base
	}

	public Doc_trasporto_rientro_dettBulk(Long pg_inventario, String ti_documento,
										  Integer esercizio, Long pg_doc_trasporto_rientro,
										  Long nr_inventario, Integer progressivo) {
		super(pg_inventario, ti_documento, esercizio, pg_doc_trasporto_rientro,
				nr_inventario, progressivo);

		// Inizializza SOLO quando costruisci manualmente
		setDoc_trasporto_rientro(new Doc_trasporto_rientroBulk(
				pg_inventario, ti_documento, esercizio, pg_doc_trasporto_rientro));
		setBene(new Inventario_beniBulk(nr_inventario, pg_inventario,
				Long.valueOf(progressivo)));
	}

	// ========================================
	// GETTER E SETTER - Oggetti Relazionati
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

	public void setDocTrasportoRientroDettRif(Doc_trasporto_rientro_dettBulk docTrRif) {
		this.docTrasportoRientroDettRif = docTrRif;
	}

	// ========================================
	// METODI UTILITY - Codifiche
	// ========================================

	/**
	 * Restituisce il codice del bene formato come "NR_INVENTARIO-PROGRESSIVO".
	 * Il progressivo viene formattato con 3 cifre (es: 001, 002, etc.)
	 */
	public String getCod_bene() {
		if (getNrInventario() == null || getProgressivo() == null) {
			return "";
		}
		java.text.DecimalFormat formato = new java.text.DecimalFormat("000");
		return getNrInventario().toString() + "-" + formato.format(getProgressivo());
	}

	/**
	 * Restituisce una chiave hash univoca per il dettaglio.
	 * Formato: "NR_INVENTARIO.PROGRESSIVO.ETICHETTA"
	 */
	public String getChiaveHash() {
		if (getNrInventario() == null || getProgressivo() == null) {
			return null;
		}
		return getNrInventario().toString() + "." + getProgressivo().toString() + "." + getEtichetta();
	}

	// ========================================
	// METODI UTILITY - Validazioni
	// ========================================

	public boolean isValidoPerRientro() {
		return docTrasportoRientroDettRif != null &&
				docTrasportoRientroDettRif.getPgInventarioRif() != null &&
				docTrasportoRientroDettRif.getPgDocTrasportoRientroRif() != null;
	}

	/**
	 * Verifica se il bene è totalmente scaricato.
	 */
	public boolean isTotalmenteScaricato() {
		return bene != null && Boolean.TRUE.equals(bene.getFl_totalmente_scaricato());
	}

	/**
	 * Verifica se il bene è in transito.
	 */
	public boolean isBeneInTransito() {
		return bene != null && bene.getId_transito_beni_ordini() != null;
	}

	// ========================================
	// METODI DI INIZIALIZZAZIONE
	// ========================================

	@Override
	public OggettoBulk initialize(it.cnr.jada.util.action.CRUDBP bp, it.cnr.jada.action.ActionContext context) {
		bene = new Inventario_beniBulk();
		bene.setTi_commerciale_istituzionale(TipoIVA.ISTITUZIONALE.value());
		return this;
	}

	@Override
	public OggettoBulk initializeForInsert(it.cnr.jada.util.action.CRUDBP bp, it.cnr.jada.action.ActionContext context) {
		bene = new Inventario_beniBulk();
		bene.setTi_commerciale_istituzionale(TipoIVA.ISTITUZIONALE.value());

		try {
			if (Utility.createConfigurazioneCnrComponentSession()
					.isGestioneEtichettaInventarioBeneAttivo(context.getUserContext())) {
				setQuantita(1L);
			}
		} catch (RemoteException | ComponentException e) {
			// Log error se necessario
		}

		return this;
	}

	// ========================================
	// ACCESSORI E ETICHETTE
	// ========================================

	public boolean isAccessorioContestuale() {
		return Boolean.TRUE.equals(fl_accessorio_contestuale);
	}

	public boolean isAssociatoConAccessorioContestuale() {
		Doc_trasporto_rientroBulk doc = getDoc_trasporto_rientro();
		if (getChiaveHash() == null || doc == null || doc.getAccessoriContestualiHash() == null) {
			return false;
		}
		return doc.getAccessoriContestualiHash().containsKey(getChiaveHash());
	}

	public boolean isROcategoriaBene() {
		return isAccessorioContestuale();
	}

	public boolean isROcollocazione() {
		return bene != null && bene.getCategoria_Bene() != null;
	}

	public boolean isROfl_accessorio() {
		return isAccessorioContestuale();
	}

	public boolean isROEtichetta() {
		return isBeneAccessorio();
	}

	/**
	 * Restituisce l'etichetta del bene.
	 * Se il bene è accessorio, restituisce l'etichetta del bene principale.
	 */
	public String getEtichetta() {
		if (isBeneAccessorio() || isAccessorioContestuale()) {
			if (bene != null && bene.getBene_principale() != null &&
					bene.getBene_principale().getEtichetta() != null) {
				return bene.getBene_principale().getEtichetta();
			}
			return "";
		}
		return bene != null && bene.getEtichetta() != null ? bene.getEtichetta() : "";
	}

	public Boolean getFl_accessorio_contestuale() {
		return fl_accessorio_contestuale;
	}

	public void setFl_accessorio_contestuale(Boolean value) {
		fl_accessorio_contestuale = value;
	}

	public boolean isBeneAccessorio() {
		return Boolean.TRUE.equals(fl_bene_accessorio);
	}

	public Boolean getFl_bene_accessorio() {
		return fl_bene_accessorio;
	}

	public void setFl_bene_accessorio(Boolean value) {
		fl_bene_accessorio = value;
	}

	// ========================================
	// ALTRI GETTER/SETTER
	// ========================================

	public int getGruppi() {
		return gruppi;
	}

	public void setGruppi(int gruppi) {
		this.gruppi = gruppi;
	}

	public Categoria_gruppo_voceBulk getCat_voce() {
		return cat_voce;
	}

	public void setCat_voce(Categoria_gruppo_voceBulk cat_voce) {
		this.cat_voce = cat_voce;
	}



}
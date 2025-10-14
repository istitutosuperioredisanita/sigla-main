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

package it.cnr.contab.docamm00.tabrif.bulk;

import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.contab.anagraf00.core.bulk.AnagraficoBulk;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;
import it.cnr.contab.inventario00.docs.bulk.Inventario_beniBulk;
import it.cnr.contab.inventario00.tabrif.bulk.Condizione_beneBulk;
import it.cnr.contab.inventario00.tabrif.bulk.Id_inventarioBulk;
import it.cnr.contab.inventario00.tabrif.bulk.Tipo_trasporto_rientroBulk;
import it.cnr.contab.inventario00.tabrif.bulk.Ubicazione_beneBulk;
import it.cnr.contab.util.enumeration.TipoIVA;
import it.cnr.jada.bulk.BulkCollection;
import it.cnr.jada.bulk.BulkList;
import it.cnr.jada.bulk.PrimaryKeyHashtable;

/**
 * Documento di Trasporto/Rientro beni inventariali.
 *
 * LOGICA FILTRO UO ⭐
 *
 * CASO 1: FL_INCARICATO = N e FL_VETTORE = N
 *   → Usa UO dell'utente loggato
 *   → Campo dipendente DISABILITATO
 *   → Campo noteRitiro DISABILITATO
 *
 * CASO 2: FL_INCARICATO = Y o FL_VETTORE = Y
 *   → Campo dipendente ABILITATO (obbligatorio)
 *   → Usa UO del DIPENDENTE selezionato
 *   → Campo noteRitiro ABILITATO
 */
public class Doc_trasporto_rientroBulk extends Doc_trasporto_rientroBase {

	public final static String TRASPORTO = "T";
	public final static String RIENTRO = "R";

	private String local_transactionID;
	private Unita_organizzativaBulk uo_consegnataria;
	private TerzoBulk consegnatario;
	private TerzoBulk delegato;

	/**
	 * DIPENDENTE (Incaricato/Vettore)
	 * Campo CONDIZIONALE:
	 * - Abilitato SOLO se flIncaricato = Y o flVettore = Y
	 * - Se valorizzato, l'UO dei beni viene presa dal dipendente
	 * - Se non valorizzato, l'UO viene presa dall'utente loggato
	 */
	private AnagraficoBulk anagDipRitiro;

	protected Tipo_trasporto_rientroBulk tipoMovimento;
	private java.util.Collection tipoMovimenti;
	private Id_inventarioBulk inventario;
	private java.util.Collection condizioni;
	private String cds_scrivania;
	private String uo_scrivania;
	private Integer nr_inventario;
	private Integer cd_barre;
	private PrimaryKeyHashtable accessoriContestualiHash;
	private it.cnr.jada.bulk.SimpleBulkList doc_trasporto_rientro_dettColl;

	// ========================================
	// COSTRUTTORI
	// ========================================

	public Doc_trasporto_rientroBulk() {
		super();
	}

	public Doc_trasporto_rientroBulk(java.lang.Long pg_inventario, java.lang.String ti_documento,
									 java.lang.Integer esercizio, java.lang.Long pg_doc_trasporto_rientro) {
		super(pg_inventario, ti_documento, esercizio, pg_doc_trasporto_rientro);
		setInventario(new Id_inventarioBulk(pg_inventario));
	}

	// ========================================
	// GETTER E SETTER - Dipendente
	// ========================================

	/**
	 * Restituisce il dipendente (incaricato/vettore).
	 * Questo campo è abilitato SOLO se flIncaricato = Y o flVettore = Y.
	 * Se valorizzato, l'UO per filtrare i beni viene presa dal dipendente.
	 *
	 * @return AnagraficoBulk del dipendente
	 */
	public AnagraficoBulk getAnagDipRitiro() {
		return anagDipRitiro;
	}

	/**
	 * Imposta il dipendente (incaricato/vettore).
	 *
	 * @param anagDipRitiro AnagraficoBulk del dipendente
	 */
	public void setAnagDipRitiro(AnagraficoBulk anagDipRitiro) {
		this.anagDipRitiro = anagDipRitiro;
	}

	/**
	 * Restituisce il codice UO da utilizzare per filtrare i beni.
	 *
	 * LOGICA:
	 * 1. Se flIncaricato = Y o flVettore = Y E dipendente è valorizzato
	 *    → restituisce l'UO del DIPENDENTE
	 * 2. Altrimenti
	 *    → restituisce NULL (sarà l'UO dell'utente loggato)
	 *
	 * @return codice UO da usare per filtro beni
	 */
	public String getCd_unita_organizzativa_filtro() {
		// Se è attivo il ritiro delegato (incaricato o vettore)
		if (isRitiroDelegato() && anagDipRitiro != null) {
			return anagDipRitiro.getCd_unita_organizzativa();
		}
		// Altrimenti si usa l'UO dell'utente loggato (gestito nel component)
		return null;
	}

	/**
	 * Verifica se il ritiro è delegato (incaricato o vettore).
	 *
	 * @return true se flIncaricato = Y o flVettore = Y
	 */
	public boolean isRitiroDelegato() {
		return (getFlIncaricato() != null && getFlIncaricato().booleanValue()) ||
				(getFlVettore() != null && getFlVettore().booleanValue());
	}

	/**
	 * Verifica se il campo dipendente è abilitato.
	 * Il dipendente è abilitato SOLO se flIncaricato = Y o flVettore = Y.
	 *
	 * @return true se il campo dipendente deve essere abilitato
	 */
	public boolean isDipendenteEnabled() {
		return isRitiroDelegato();
	}

	/**
	 * Verifica se il campo noteRitiro è abilitato.
	 * Le note sono abilitate SOLO se flIncaricato = Y o flVettore = Y.
	 *
	 * @return true se il campo noteRitiro deve essere abilitato
	 */
	public boolean isNoteRitiroEnabled() {
		return isRitiroDelegato();
	}

	/**
	 * Verifica se il dipendente è obbligatorio.
	 * Se il ritiro è delegato, il dipendente diventa obbligatorio.
	 *
	 * @return true se il dipendente è obbligatorio
	 */
	public boolean isDipendenteRequired() {
		return isRitiroDelegato();
	}

	// ========================================
	// GETTER E SETTER - Campi Standard
	// ========================================

	public TerzoBulk getConsegnatario() {
		return consegnatario;
	}

	public TerzoBulk getDelegato() {
		return delegato;
	}

	public Id_inventarioBulk getInventario() {
		return inventario;
	}

	public java.util.Collection getTipoMovimenti() {
		return tipoMovimenti;
	}

	public Tipo_trasporto_rientroBulk getTipoMovimento() {
		return tipoMovimento;
	}

	public Unita_organizzativaBulk getUo_consegnataria() {
		return uo_consegnataria;
	}

	public void setConsegnatario(TerzoBulk bulk) {
		consegnatario = bulk;
	}

	public void setDelegato(TerzoBulk bulk) {
		delegato = bulk;
	}

	public void setInventario(Id_inventarioBulk bulk) {
		inventario = bulk;
	}

	public void setTipoMovimenti(java.util.Collection collection) {
		tipoMovimenti = collection;
	}

	public void setTipoMovimento(Tipo_trasporto_rientroBulk bulk) {
		tipoMovimento = bulk;
	}

	public void setUo_consegnataria(Unita_organizzativaBulk bulk) {
		uo_consegnataria = bulk;
	}

	public void setPg_inventario(java.lang.Long pg_inventario) {
		this.getInventario().setPg_inventario(pg_inventario);
	}

	public void setCd_tipo_trasporto_rientro(java.lang.String cd_tipo_trasporto_rientro) {
		this.getTipoMovimento().setCdTipoTrasportoRientro(cd_tipo_trasporto_rientro);
	}

	public java.lang.Long getPg_inventario() {
		Id_inventarioBulk inventario = this.getInventario();
		if (inventario == null)
			return null;
		return inventario.getPg_inventario();
	}

	public java.lang.String getCd_tipo_trasporto_rientro() {
		Tipo_trasporto_rientroBulk tipoMovimento = this.getTipoMovimento();
		if (tipoMovimento == null)
			return null;
		return tipoMovimento.getCdTipoTrasportoRientro();
	}

	public String getCds_scrivania() {
		return cds_scrivania;
	}

	public java.util.Collection getCondizioni() {
		return condizioni;
	}

	public String getUo_scrivania() {
		return uo_scrivania;
	}

	public void setCds_scrivania(String string) {
		cds_scrivania = string;
	}

	public void setCondizioni(java.util.Collection collection) {
		condizioni = collection;
	}

	public void setUo_scrivania(String string) {
		uo_scrivania = string;
	}

	public Integer getNr_inventario() {
		return nr_inventario;
	}

	public void setNr_inventario(Integer integer) {
		nr_inventario = integer;
	}

	public Integer getCd_barre() {
		return cd_barre;
	}

	public void setCd_barre(Integer cd_barre) {
		this.cd_barre = cd_barre;
	}

	public String getLocal_transactionID() {
		return local_transactionID;
	}

	public void setLocal_transactionID(String local_transactionID) {
		this.local_transactionID = local_transactionID;
	}

	// ========================================
	// GESTIONE ACCESSORI CONTESTUALI
	// ========================================

	public PrimaryKeyHashtable getAccessoriContestualiHash() {
		return accessoriContestualiHash;
	}

	public void setAccessoriContestualiHash(PrimaryKeyHashtable hashtable) {
		accessoriContestualiHash = hashtable;
	}

	public Long addToAccessoriContestualiHash(
			Doc_trasporto_rientro_dettBulk bene_padre,
			Doc_trasporto_rientro_dettBulk bene_figlio,
			Long progressivo) {

		if (accessoriContestualiHash == null)
			accessoriContestualiHash = new PrimaryKeyHashtable();

		BulkList beni_associati = null;
		if (bene_padre.getChiaveHash() != null) {
			beni_associati = (BulkList) accessoriContestualiHash.get(bene_padre.getChiaveHash());
		}

		if (beni_associati == null) {
			beni_associati = new BulkList();
			bene_padre.setNr_inventario(progressivo);
			bene_padre.setProgressivo(new Integer("0"));
			bene_padre.setPg_inventario(getPg_inventario());
			progressivo = new Long(progressivo.longValue() + 1);
		}

		bene_figlio.setNr_inventario(bene_padre.getNr_inventario());
		bene_figlio.setProgressivo(new Integer(Integer.toString(beni_associati.size() + 1)));
		bene_figlio.setPg_inventario(getPg_inventario());
		beni_associati.add(bene_figlio);
		accessoriContestualiHash.put(bene_padre.getChiaveHash(), beni_associati);

		return progressivo;
	}

	public int removeFromAccessoriContestualiHash(Doc_trasporto_rientro_dettBulk bene_figlio) {

		if (accessoriContestualiHash != null) {
			for (java.util.Enumeration e = accessoriContestualiHash.keys(); e.hasMoreElements();) {
				String chiave_bene_padre = (String) e.nextElement();
				BulkList beni_accessori = (BulkList) accessoriContestualiHash.get(chiave_bene_padre);
				if (beni_accessori.containsByPrimaryKey(bene_figlio)) {
					beni_accessori.removeByPrimaryKey(bene_figlio);
					if (beni_accessori.isEmpty()) {
						accessoriContestualiHash.remove(chiave_bene_padre);
						if (accessoriContestualiHash.isEmpty()) {
							setAccessoriContestualiHash(null);
						}
					}
					break;
				}
			}
		}
		return (accessoriContestualiHash != null ? accessoriContestualiHash.size() : 0);
	}

	// ========================================
	// GESTIONE DETTAGLI
	// ========================================

	public it.cnr.jada.bulk.SimpleBulkList getDoc_trasporto_rientro_dettColl() {
		return doc_trasporto_rientro_dettColl;
	}

	public void setDoc_trasporto_rientro_dettColl(it.cnr.jada.bulk.SimpleBulkList list) {
		doc_trasporto_rientro_dettColl = list;
	}

	public BulkCollection[] getBulkLists() {
		return new it.cnr.jada.bulk.BulkCollection[] { this.getDoc_trasporto_rientro_dettColl() };
	}

	public Doc_trasporto_rientro_dettBulk removeFromDoc_trasporto_rientro_dettColl(int indiceDiLinea) {
		Doc_trasporto_rientro_dettBulk element =
				(Doc_trasporto_rientro_dettBulk) doc_trasporto_rientro_dettColl.get(indiceDiLinea);
		return (Doc_trasporto_rientro_dettBulk) doc_trasporto_rientro_dettColl.remove(indiceDiLinea);
	}

	public int addToDoc_trasporto_rientro_dettColl(Doc_trasporto_rientro_dettBulk nuovo) {
		nuovo.setDoc_trasporto_rientro(this);
		getDoc_trasporto_rientro_dettColl().add(nuovo);
		nuovo.setBene(new Inventario_beniBulk());
		nuovo.getBene().setInventario(this.getInventario());
		nuovo.getBene().setPg_inventario(this.getPg_inventario());
		nuovo.getBene().setTi_commerciale_istituzionale(TipoIVA.ISTITUZIONALE.value());
		nuovo.getBene().setFl_totalmente_scaricato(java.lang.Boolean.FALSE);
		nuovo.getBene().setCategoria_Bene(new Categoria_gruppo_inventBulk());
		nuovo.getBene().setUbicazione(new Ubicazione_beneBulk());
		nuovo.getBene().setAssegnatario(new TerzoBulk());
		if (nuovo.getBene().getCondizioneBene() == null) {
			nuovo.getBene().setCondizioneBene(new Condizione_beneBulk());
			nuovo.getBene().setCd_condizione_bene("4");
		}

		return getDoc_trasporto_rientro_dettColl().size() - 1;
	}

	// ========================================
	// METODI UTILITY
	// ========================================

	public boolean hasDettagli() {
		return getDoc_trasporto_rientro_dettColl().size() > 0;
	}

	public boolean includesBene(Inventario_beniBulk bene) {
		return (getDettaglioPerBene(bene) != null);
	}

	public Doc_trasporto_rientro_dettBulk getDettaglioPerBene(Inventario_beniBulk bene) {
		java.util.List beni = new it.cnr.jada.util.Collect(getDoc_trasporto_rientro_dettColl(), "bene");
		for (int i = 0; i < beni.size(); i++) {
			Inventario_beniBulk unBene = (Inventario_beniBulk) beni.get(i);
			if (unBene.getNumeroBeneCompleto().equals(bene.getNumeroBeneCompleto()))
				return (Doc_trasporto_rientro_dettBulk) getDoc_trasporto_rientro_dettColl().get(i);
		}
		return null;
	}

	public void sostituisciDettagli(Doc_trasporto_rientro_dettBulk dettaglio1, BulkList dettagliSostituti) {
		if (getDoc_trasporto_rientro_dettColl().contains(dettaglio1)) {
			int indice = getDoc_trasporto_rientro_dettColl().indexOf(dettaglio1);
			getDoc_trasporto_rientro_dettColl().remove(indice);
			for (java.util.Iterator i = dettagliSostituti.iterator(); i.hasNext();) {
				Doc_trasporto_rientro_dettBulk nuovoDettaglio = (Doc_trasporto_rientro_dettBulk) i.next();
				nuovoDettaglio.setDoc_trasporto_rientro(this);
				getDoc_trasporto_rientro_dettColl().add(indice++, nuovoDettaglio);
			}
		}
	}

	public boolean isTemporaneo() {
		return getPgDocTrasportoRientro() == null ||
				getPgDocTrasportoRientro().compareTo(new Long("0")) <= 0;
	}
}
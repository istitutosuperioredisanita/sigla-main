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

import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;
import it.cnr.contab.inventario00.docs.bulk.Inventario_beniBulk;
import it.cnr.contab.inventario00.tabrif.bulk.*;
import it.cnr.contab.service.SpringUtil;
import it.cnr.contab.spring.service.StorePath;
import it.cnr.contab.util00.bulk.storage.AllegatoGenericoBulk;
import it.cnr.contab.util00.bulk.storage.AllegatoParentBulk;
import it.cnr.jada.bulk.*;
import it.cnr.si.spring.storage.StorageDriver;
import it.cnr.si.spring.storage.annotation.StorageProperty;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Testata documento Trasporto/Rientro.
 *
 * PATTERN: Delega per pg_inventario
 * - pg_inventario è delegato a inventario (Id_inventarioBulk)
 * - ti_documento, esercizio, pg_doc_trasporto_rientro sono nella Key
 *
 * GESTIONE STATO:
 * - Lo STATO del documento è gestito tramite il campo 'stato' ereditato dal Base
 */
public class Doc_trasporto_rientroBulk extends Doc_trasporto_rientroBase /*implements AllegatoParentBulk*/ {

	// ========================================
	// COSTANTI TIPO DOCUMENTO
	// ========================================

	public static final String TRASPORTO = "T";
	public static final String RIENTRO = "R";

	// ========================================
	// COSTANTI STATO DOCUMENTO
	// ========================================

	public static final String STATO_INSERITO = "INS";
	public static final String STATO_PREDISPOSTO_FIRMA = "PAF";
	public static final String STATO_FIRMATO = "FIR";
	public static final String STATO_ANNULLATO = "ANN";

	// ========================================
	// COSTANTI TIPO RITIRO
	// ========================================

	public static final String TIPO_RITIRO_INCARICATO = "I";
	public static final String TIPO_RITIRO_VETTORE = "V";

	private static final java.util.Dictionary tipoRitiroKeys;

	static {
		tipoRitiroKeys = new it.cnr.jada.util.OrderedHashtable();
		tipoRitiroKeys.put(TIPO_RITIRO_INCARICATO, "Ritiro Incaricato");
		tipoRitiroKeys.put(TIPO_RITIRO_VETTORE, "Ritiro Vettore");
	}

	// ========================================
	// ATTRIBUTI
	// ========================================

	/**
	 * Inventario di riferimento (FK).
	 * Contiene pg_inventario (parte della PK).
	 */
	private Id_inventarioBulk inventario;

	private String cds_scrivania;
	private String uo_scrivania;
	private Tipo_trasporto_rientroBulk tipoMovimento;
	private SimpleBulkList doc_trasporto_rientro_dettColl;
	private List condizioni;
	private java.util.Collection tipoMovimenti;
	private String local_transactionID;
	private TerzoBulk anagDipRitiro;
	private TerzoBulk consegnatario;
	private TerzoBulk delegato;
	private Unita_organizzativaBulk uo_consegnataria;
	private PrimaryKeyHashtable accessoriContestualiHash;
	private Integer nr_inventario;
	private Inventario_beniBulk bene;

//	// Archivio allegati per implementare AllegatoParentBulk
//	private BulkList<AllegatoGenericoBulk> archivioAllegatiDDT = new BulkList<AllegatoGenericoBulk>();

	// ========================================
	// COSTRUTTORI
	// ========================================

	public Doc_trasporto_rientroBulk() {
		super();
	}

	public Doc_trasporto_rientroBulk(Long pg_inventario, String ti_documento,
									 Integer esercizio, Long pg_doc_trasporto_rientro) {
		super(pg_inventario, ti_documento, esercizio, pg_doc_trasporto_rientro);
		// Inizializza l'inventario (FK)
		setInventario(new Id_inventarioBulk(pg_inventario));
	}

	// ========================================
	// GETTER/SETTER CHIAVE PRIMARIA - Snake_case per JADA ORM
	// ========================================
	// ========================================
	// GETTER E SETTER - Oggetti Relazionati
	// ========================================

	public Id_inventarioBulk getInventario() {
		return inventario;
	}

	public void setInventario(Id_inventarioBulk inventario) {
		this.inventario = inventario;
	}

	public Tipo_trasporto_rientroBulk getTipoMovimento() {
		return tipoMovimento;
	}

	public void setTipoMovimento(Tipo_trasporto_rientroBulk tipoMovimento) {
		this.tipoMovimento = tipoMovimento;
		if (tipoMovimento != null) {
			setCdTipoTrasportoRientro(tipoMovimento.getCdTipoTrasportoRientro());
		}
	}

	public SimpleBulkList getDoc_trasporto_rientro_dettColl() {
		return doc_trasporto_rientro_dettColl;
	}

	public void setDoc_trasporto_rientro_dettColl(SimpleBulkList doc_trasporto_rientro_dettColl) {
		this.doc_trasporto_rientro_dettColl = doc_trasporto_rientro_dettColl;
	}

	public BulkCollection[] getBulkLists() {
		return new it.cnr.jada.bulk.BulkCollection[] { this.getDoc_trasporto_rientro_dettColl() };
	}
	// ========================================
	// GESTIONE STATO DOCUMENTO
	// ========================================

	private boolean hasStato(String stato) {
		return stato != null && stato.equals(getStato());
	}

	public boolean isInserito() {
		return hasStato(STATO_INSERITO);
	}

	public boolean isPredispostoAllaFirma() {
		return hasStato(STATO_PREDISPOSTO_FIRMA);
	}

	public boolean isFirmato() {
		return hasStato(STATO_FIRMATO);
	}

	public boolean isAnnullato() {
		return hasStato(STATO_ANNULLATO);
	}

	public boolean isModificabile() {
		return isInserito();
	}

	public boolean isPossibileModificareDettagli() {
		return isInserito();
	}

	public boolean isConfermabile() {
		return isInserito() && hasDettagli();
	}

	public boolean isAnnullabile() {
		return !isAnnullato();
	}

	// ========================================
	// GESTIONE TIPO RITIRO
	// ========================================

	public java.util.Dictionary getTipoRitiroKeys() {
		return tipoRitiroKeys;
	}

	public String getTipoRitiro() {
		if (Boolean.TRUE.equals(getFlIncaricato())) {
			return TIPO_RITIRO_INCARICATO;
		}
		if (Boolean.TRUE.equals(getFlVettore())) {
			return TIPO_RITIRO_VETTORE;
		}
		return null;
	}

	public void setTipoRitiro(String tipoRitiro) {
		setFlIncaricato(TIPO_RITIRO_INCARICATO.equals(tipoRitiro));
		setFlVettore(TIPO_RITIRO_VETTORE.equals(tipoRitiro));
	}

	public boolean hasTipoRitiroSelezionato() {
		return getTipoRitiro() != null;
	}

	public boolean isRitiroIncaricato() {
		return TIPO_RITIRO_INCARICATO.equals(getTipoRitiro());
	}

	public boolean isRitiroVettore() {
		return TIPO_RITIRO_VETTORE.equals(getTipoRitiro());
	}

	// ========================================
	// METODI UTILITY
	// ========================================

	public boolean isTrasporto() {
		return TRASPORTO.equals(getTiDocumento());
	}

	public boolean isRientro() {
		return RIENTRO.equals(getTiDocumento());
	}

	public boolean hasDettagli() {
		return doc_trasporto_rientro_dettColl != null && doc_trasporto_rientro_dettColl.size() > 0;
	}

	public boolean isTemporaneo() {
		return getPgDocTrasportoRientro() == null ||
				getPgDocTrasportoRientro().compareTo(new Long("0")) <= 0;
	}

	// ========================================
	// GETTER E SETTER - Altri attributi
	// ========================================

	public String getCds_scrivania() {
		return cds_scrivania;
	}

	public void setCds_scrivania(String cds_scrivania) {
		this.cds_scrivania = cds_scrivania;
	}

	public String getUo_scrivania() {
		return uo_scrivania;
	}

	public void setUo_scrivania(String uo_scrivania) {
		this.uo_scrivania = uo_scrivania;
	}

	public List getCondizioni() {
		return condizioni;
	}

	public void setCondizioni(List condizioni) {
		this.condizioni = condizioni;
	}

	public java.util.Collection getTipoMovimenti() {
		return tipoMovimenti;
	}

	public void setTipoMovimenti(java.util.Collection tipoMovimenti) {
		this.tipoMovimenti = tipoMovimenti;
	}

	public String getLocal_transactionID() {
		return local_transactionID;
	}

	public void setLocal_transactionID(String local_transactionID) {
		this.local_transactionID = local_transactionID;
	}

	public TerzoBulk getAnagDipRitiro() {
		return anagDipRitiro;
	}

	public void setAnagDipRitiro(TerzoBulk anagDipRitiro) {
		this.anagDipRitiro = anagDipRitiro;
		if (anagDipRitiro != null) {
			setCdTerzoAssegnatario(anagDipRitiro.getCd_terzo());
			setDenominazioneSede(anagDipRitiro.getDenominazione_sede());
		} else {
			setCdTerzoAssegnatario(null);
		}
	}

	@Override
	public void setCdTerzoAssegnatario(Integer cdTerzoAssegnatario) {
		super.setCdTerzoAssegnatario(cdTerzoAssegnatario);
		if (anagDipRitiro != null && cdTerzoAssegnatario != null) {
			anagDipRitiro.setCd_terzo(cdTerzoAssegnatario);
		}
	}

	public void setDenominazioneSede(String denominazioneSede) {
		if (anagDipRitiro != null) {
			anagDipRitiro.setDenominazione_sede(denominazioneSede);
		}
	}

	public TerzoBulk getConsegnatario() {
		return consegnatario;
	}

	public void setConsegnatario(TerzoBulk consegnatario) {
		this.consegnatario = consegnatario;
	}

	public TerzoBulk getDelegato() {
		return delegato;
	}

	public void setDelegato(TerzoBulk delegato) {
		this.delegato = delegato;
	}

	public Unita_organizzativaBulk getUo_consegnataria() {
		return uo_consegnataria;
	}

	public void setUo_consegnataria(Unita_organizzativaBulk uo_consegnataria) {
		this.uo_consegnataria = uo_consegnataria;
	}

	public PrimaryKeyHashtable getAccessoriContestualiHash() {
		return accessoriContestualiHash;
	}

	public void setAccessoriContestualiHash(PrimaryKeyHashtable accessoriContestualiHash) {
		this.accessoriContestualiHash = accessoriContestualiHash;
	}

	public Integer getNr_inventario() {
		return nr_inventario;
	}

	public void setNr_inventario(Integer nr_inventario) {
		this.nr_inventario = nr_inventario;
	}

	public Inventario_beniBulk getBene() {
		return bene;
	}

	public void setBene(Inventario_beniBulk bene) {
		this.bene = bene;
	}

	// ========================================
	// ANNOTAZIONI AZURE STORAGE
	// ========================================

	@StorageProperty(name = "Doc_Trasporto_Rientro:esercizio")
	public Integer getEsercizioPathAzure() {
		return getEsercizio();
	}

	@StorageProperty(name = "Doc_Trasporto_Rientro:inventario")
	public Long getPgInventarioPathAzure() {
		return getPgInventario();
	}

	@StorageProperty(name = "Doc_Trasporto_Rientro:tiDocumento")
	public String getTipoDocPathAzure() {
		if (TRASPORTO.equals(getTiDocumento()))
			return "Trasporto";
		else
			return "Rientro";
	}

	@StorageProperty(name = "Doc_Trasporto_Rientro:pgDocTrasportoRientro")
	public Long getPgDocTRPathAzure() {
		return getPgDocTrasportoRientro();
	}

	/** Percorso su CMIS: cartella per DOC_T_R / esercizio / inventario / tiDocumento / pgDocTrasportoRientro **/
	public static String getStorePathDDT(String suffix, Integer esercizio, Long inventario,
										 String tiDocumento, Long pgDocTrasportoRientro) {
		return Arrays.asList(
				SpringUtil.getBean(StorePath.class).getPathComunicazioniAl(),
				suffix,
				String.valueOf(esercizio),
				String.valueOf(inventario),
				tiDocumento,
				String.valueOf(pgDocTrasportoRientro)
		).stream().collect(Collectors.joining(StorageDriver.SUFFIX));
	}

	// ========================================
	// VALIDAZIONE
	// ========================================

	private void validaCampoObbligatorio(Object valore, String nomeCampo) throws ValidationException {
		if (valore == null) {
			throw new ValidationException("Indicare " + nomeCampo + ".");
		}
		if (valore instanceof String && ((String) valore).trim().isEmpty()) {
			throw new ValidationException("Indicare " + nomeCampo + ".");
		}
	}

	@Override
	public void validate() throws ValidationException {
		// Validazioni campi obbligatori
		validaCampoObbligatorio(getDsDocTrasportoRientro(), "la Descrizione del documento");
		validaCampoObbligatorio(getDataRegistrazione(),
				"la Data " + (TRASPORTO.equals(getTiDocumento()) ? "Trasporto" : "Rientro"));
		validaCampoObbligatorio(getTipoMovimento(), "il Tipo Movimento");
		validaCampoObbligatorio(getTipoRitiro(), "il Tipo Ritiro (Incaricato o Vettore)");

		// Validazione valori ammessi per Tipo Ritiro
		if (!Arrays.asList(TIPO_RITIRO_INCARICATO, TIPO_RITIRO_VETTORE).contains(getTipoRitiro())) {
			throw new ValidationException("I valori possibili per Tipo Ritiro sono: Incaricato o Vettore.");
		}

		// Validazione Dipendente Incaricato se tipo ritiro è INCARICATO
		if (isRitiroIncaricato() && (getAnagDipRitiro() == null || getAnagDipRitiro().getCd_anag() == null)) {
			throw new ValidationException(
					"Per il ritiro tramite INCARICATO è necessario selezionare il Dipendente Incaricato.");
		}

		super.validate();
	}

//	// ========================================
//	// IMPLEMENTAZIONE AllegatoParentBulk
//	// ========================================
//
//	@Override
//	public BulkList<AllegatoGenericoBulk> getArchivioAllegati() {
//		return archivioAllegatiDDT;
//	}
//
//	@Override
//	public void setArchivioAllegati(BulkList<AllegatoGenericoBulk> archivioAllegatiDDT) {
//		this.archivioAllegatiDDT = archivioAllegatiDDT;
//	}
//
//	@Override
//	public int addToArchivioAllegati(AllegatoGenericoBulk allegatoDDT) {
//		if (allegatoDDT != null) {
//			archivioAllegatiDDT.add(allegatoDDT);
//		}
//		return archivioAllegatiDDT.size() - 1;
//	}
//
//	@Override
//	public AllegatoGenericoBulk removeFromArchivioAllegati(int index) {
//		return getArchivioAllegati().remove(index);
//	}

	public boolean isNoteRitiroEnabled() {
		if (!hasTipoRitiroSelezionato()) {
			return false;
		}
		if (tipoMovimento != null) {
			String dsTipo = tipoMovimento.getDsTipoTrasportoRientro();
			return "Sostituzione per".equals(dsTipo) || "Altro".equals(dsTipo);
		}
		return false;
	}

}
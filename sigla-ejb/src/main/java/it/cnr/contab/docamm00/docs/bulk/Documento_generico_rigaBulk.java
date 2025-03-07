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

import it.cnr.contab.anagraf00.core.bulk.AnagraficoBulk;
import it.cnr.contab.anagraf00.core.bulk.BancaBulk;
import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.contab.anagraf00.tabrif.bulk.Rif_modalita_pagamentoBulk;
import it.cnr.contab.anagraf00.tabrif.bulk.Rif_termini_pagamentoBulk;
import it.cnr.jada.DetailedRuntimeException;
import it.cnr.jada.bulk.BulkList;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.bulk.ValidationException;

import javax.persistence.Transient;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Dictionary;
import java.util.Optional;
import java.util.regex.Pattern;

public class Documento_generico_rigaBulk extends Documento_generico_rigaBase implements IDocumentoAmministrativoRigaBulk, Voidable {
    private Documento_genericoBulk documento_generico;

    private BulkList riferimenti_bancari= new BulkList();

    protected BancaBulk banca;
    protected TerzoBulk terzo;
    protected BancaBulk banca_uo_cds;
    protected TerzoBulk terzo_uo_cds;
    protected TerzoBulk cessionario;
    private java.math.BigDecimal im_riga_iniziale;
    private java.math.BigDecimal im_riga_sdoppia;
    protected Rif_termini_pagamentoBulk termini_pagamento;
    protected Rif_modalita_pagamentoBulk modalita_pagamento;
    protected Rif_termini_pagamentoBulk termini_pagamento_uo_cds;
    protected Rif_modalita_pagamentoBulk modalita_pagamento_uo_cds;
    public final static String ASSOCIATO_A_MANDATO= "T";
    public final static String NON_ASSOCIATO_A_MANDATO= "N";

    public final static java.util.Dictionary STATO_MANDATO;
	public final static java.util.Dictionary STATI_RIPORTO;
    

    private java.util.Collection banche;
    private java.util.Collection modalita;
    private java.util.Collection termini;
    private java.util.Collection banche_uo_cds;
    private java.util.Collection modalita_uo_cds;
    private java.util.Collection termini_uo_cds;

    protected AnagraficoBulk anagrafico;

    public final static String STATO_INIZIALE= "I";
    public final static String STATO_PAGATO= "P";
    public final static String STATO_CONTABILIZZATO= "C";
    public final static String STATO_ANNULLATO= "A";

    public final static String NON_REGISTRATO_IN_COGE= "N";
    public final static String REGISTRATO_IN_COGE= "C";
    public final static String DA_RIREGISTRARE_IN_COGE= "R";
    public final static String DA_NON_REGISTRARE_IN_COGE= "X";

    private it.cnr.contab.doccont00.core.bulk.Obbligazione_scadenzarioBulk obbligazione_scadenziario;
    private it.cnr.contab.doccont00.core.bulk.Accertamento_scadenzarioBulk accertamento_scadenziario;
	private java.lang.String riportata = NON_RIPORTATO;
	private boolean inventariato = false;

	private Documento_generico_rigaBulk documento_generico_riga_storno;
	private Fattura_attiva_rigaIBulk fattura_attiva_riga_storno;
	private Fattura_passiva_rigaIBulk fattura_passiva_riga_storno;

	@Transient
	private BigDecimal importoStornato=BigDecimal.ZERO;
	@Transient
	private Boolean rigaStornata;
	static{
		STATO_MANDATO = new it.cnr.jada.util.OrderedHashtable();
		STATO_MANDATO.put(NON_ASSOCIATO_A_MANDATO,"Man/rev non associato");
		STATO_MANDATO.put(ASSOCIATO_A_MANDATO,"Man/rev associato");

		STATI_RIPORTO = new it.cnr.jada.util.OrderedHashtable();
		STATI_RIPORTO.put(NON_RIPORTATO,"Non riportata");
		STATI_RIPORTO.put(RIPORTATO,"Riportata");
    }
	public static Dictionary<String, String> tiDocumentoAmmKeys;

	public Documento_generico_rigaBulk() {
		super();
	}
	public Documento_generico_rigaBulk(java.lang.String cd_cds,java.lang.String cd_tipo_documento_amm,java.lang.String cd_unita_organizzativa,java.lang.Integer esercizio,java.lang.Long pg_documento_generico,java.lang.Long progressivo_riga) {
		super(cd_cds,cd_tipo_documento_amm,cd_unita_organizzativa,esercizio,pg_documento_generico,progressivo_riga);
		setDocumento_generico(new Documento_genericoBulk(cd_cds,cd_tipo_documento_amm,cd_unita_organizzativa,esercizio,pg_documento_generico));
	}
	//aggiunge un banca alla lista dei riferimenti bancari
	public int addToRiferimenti_bancari( Documento_generico_rigaBulk os )
	{
		riferimenti_bancari.add(os);
		os.setDocumento_generico(this.getDocumento_generico());

		return riferimenti_bancari.size()-1;
	}
	/**
	 * Effettua il calcolo del importo in euro
	 */
	public void calcolaCampiDiRiga() {

		//setIm_riga(getIm_riga_divisa().setScale(2, java.math.BigDecimal.ROUND_HALF_UP));
		java.math.BigDecimal change = getDocumento_generico().getCambio();
		if (getIm_riga_divisa() != null
			&& getIm_riga_divisa() != new java.math.BigDecimal(0))
			if (getIm_riga_divisa() != null)
				setIm_riga(
					(getDocumento_generico().getChangeOperation()
						== Documento_genericoBulk.MOLTIPLICA)
						? getIm_riga_divisa().multiply(change)
						: getIm_riga_divisa()
							.divide(change, java.math.BigDecimal.ROUND_HALF_UP)
							.setScale(2, java.math.BigDecimal.ROUND_HALF_UP));
			else {
				setIm_riga(java.math.BigDecimal.valueOf(0));
				setIm_riga_divisa(java.math.BigDecimal.valueOf(0));
			}
	}
	public boolean checkIfRiportata() {

		if (getDocumento_generico().isGenericoAttivo())
			return	!isPagata() &&
					(getAccertamento_scadenziario() != null &&
						!getAccertamento_scadenziario().getEsercizio().equals(getEsercizio()));
		else
			return	!isPagata() &&
					(getObbligazione_scadenziario() != null &&
						!getObbligazione_scadenziario().getEsercizio().equals(getEsercizio()));
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (07/01/2002 12.41.55)
	 * @return it.cnr.contab.doccont00.core.bulk.Accertamento_scadenzarioBulk
	 */
	public it.cnr.contab.doccont00.core.bulk.Accertamento_scadenzarioBulk getAccertamento_scadenziario() {
		return accertamento_scadenziario;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (28/09/2001 16.43.41)
	 * @return it.cnr.contab.anagraf00.core.bulk.AnagraficoBulk
	 */
	public it.cnr.contab.anagraf00.core.bulk.AnagraficoBulk getAnagrafico() {
		return anagrafico;
	}
	/*
	* Getter dell'attributo cd_cds
	*/
	public IDocumentoAmministrativoRigaBulk getAssociatedDetail() {

		return null;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (28/09/2001 14.44.27)
	 * @return it.cnr.contab.anagraf00.core.bulk.BancaBulk
	 */
	public it.cnr.contab.anagraf00.core.bulk.BancaBulk getBanca() {
		return banca;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (07/03/2002 11.35.58)
	 * @return it.cnr.contab.anagraf00.core.bulk.BancaBulk
	 */
	public it.cnr.contab.anagraf00.core.bulk.BancaBulk getBanca_uo_cds() {
		return banca_uo_cds;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (28/09/2001 14.44.27)
	 * @return java.util.Collection
	 */
	public java.util.Collection getBanche() {
		return banche;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (07/03/2002 11.35.58)
	 * @return java.util.Collection
	 */
	public java.util.Collection getBanche_uo_cds() {
		return banche_uo_cds;
	}
	/*
	* Getter dell'attributo cd_cds
	*/
	public java.lang.String getCd_cds() {
		Documento_genericoBulk generico= this.getDocumento_generico();
		if (generico == null)
			return null;
		return generico.getCd_cds();
	}
	public java.lang.String getCd_modalita_pag() {
		it.cnr.contab.anagraf00.tabrif.bulk.Rif_modalita_pagamentoBulk modalita_pagamento = this.getModalita_pagamento();
		if (modalita_pagamento == null)
			return null;
		return modalita_pagamento.getCd_modalita_pag();
	}
	public java.lang.String getCd_termini_pag() {
		it.cnr.contab.anagraf00.tabrif.bulk.Rif_termini_pagamentoBulk termini_pagamento = this.getTermini_pagamento();
		if (termini_pagamento == null)
			return null;
		return termini_pagamento.getCd_termini_pag();
	}
		/*
	 * Getter dell'attributo cd_tipo_documento_amm
	 */
	public java.lang.String getCd_tipo_documento_amm() {
		Documento_genericoBulk generico= this.getDocumento_generico();
		if (generico == null)
			return null;
		return generico.getCd_tipo_documento_amm();
	}
	/*
	 * Getter dell'attributo cd_unita_organizzativa
	 */
	public java.lang.String getCd_unita_organizzativa() {
		Documento_genericoBulk generico= this.getDocumento_generico();
		if (generico == null)
			return null;
		return generico.getCd_unita_organizzativa();
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (25/03/2002 12.10.19)
	 * @return it.cnr.contab.anagraf00.core.bulk.TerzoBulk
	 */
	public it.cnr.contab.anagraf00.core.bulk.TerzoBulk getCessionario() {
		return cessionario;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (05/10/2001 11.56.28)
	 * @return it.cnr.contab.docamm00.docs.bulk.Documento_genericoBulk
	 */
	public Documento_genericoBulk getDocumento_generico() {
		return documento_generico;
	}
	/*
	 * Getter dell'attributo esercizio
	 */
	public java.lang.Integer getEsercizio() {
		Documento_genericoBulk generico= this.getDocumento_generico();
		if (generico == null)
			return null;
		return generico.getEsercizio();
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (12/14/2001 2:38:43 PM)
	 * @return it.cnr.contab.docamm00.docs.bulk.IDocumentoAmministrativoBulk
	 */
	public IDocumentoAmministrativoBulk getFather() {

		return getDocumento_generico();
	}
	public java.math.BigDecimal getIm_diponibile_nc(){
		return null;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (12/14/2001 2:38:27 PM)
	 * @return it.cnr.contab.docamm00.docs.bulk.IDocumentoAmministrativoBulk
	 */
	public java.math.BigDecimal getIm_imponibile(){
		return getIm_riga();
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (12/14/2001 2:38:27 PM)
	 * @return it.cnr.contab.docamm00.docs.bulk.IDocumentoAmministrativoBulk
	 */
	public java.math.BigDecimal getIm_iva(){
		return null;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (28/03/2002 15.54.56)
	 * @return java.math.BigDecimal
	 */
	public java.math.BigDecimal getIm_riga_iniziale() {
		return im_riga_iniziale;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (28/09/2001 14.44.27)
	 * @return java.util.Collection
	 */
	public java.util.Collection getModalita() {
		return modalita;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (28/09/2001 14.44.27)
	 * @return it.cnr.contab.anagraf00.tabrif.bulk.Rif_modalita_pagamentoBulk
	 */
	public it.cnr.contab.anagraf00.tabrif.bulk.Rif_modalita_pagamentoBulk getModalita_pagamento() {
		return modalita_pagamento;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (07/03/2002 11.35.58)
	 * @return it.cnr.contab.anagraf00.tabrif.bulk.Rif_modalita_pagamentoBulk
	 */
	public it.cnr.contab.anagraf00.tabrif.bulk.Rif_modalita_pagamentoBulk getModalita_pagamento_uo_cds() {
		return modalita_pagamento_uo_cds;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (07/03/2002 11.35.58)
	 * @return java.util.Collection
	 */
	public java.util.Collection getModalita_uo_cds() {
		return modalita_uo_cds;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (03/12/2001 12.37.59)
	 * @return it.cnr.contab.doccont00.core.bulk.Obbligazione_scadenzarioBulk
	 */
	public it.cnr.contab.doccont00.core.bulk.Obbligazione_scadenzarioBulk getObbligazione_scadenziario() {
		return obbligazione_scadenziario;
	}
	public IDocumentoAmministrativoRigaBulk getOriginalDetail(){
	return null;
	}
		public java.lang.Long getPg_banca() {
		it.cnr.contab.anagraf00.core.bulk.BancaBulk banca = this.getBanca();
		if (banca == null)
			return null;
		return banca.getPg_banca();
	}
	/*
	 * Getter dell'attributo pg_documento_generico
	 */
	public java.lang.Long getPg_documento_generico() {
		Documento_genericoBulk generico= this.getDocumento_generico();
		if (generico == null)
			return null;
		return generico.getPg_documento_generico();
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (28/09/2001 14.44.27)
	 * @return it.cnr.jada.bulk.BulkList
	 */
	public it.cnr.jada.bulk.BulkList getRiferimenti_bancari() {
		return riferimenti_bancari;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (30/05/2003 16.17.42)
	 * @return java.lang.String
	 */
	public java.lang.String getRiportata() {
		return riportata;
	}
	public it.cnr.contab.doccont00.core.bulk.IScadenzaDocumentoContabileBulk getScadenzaDocumentoContabile() {

		if (this.getDocumento_generico().isGenericoAttivo())
			return (it.cnr.contab.doccont00.core.bulk.IScadenzaDocumentoContabileBulk)getAccertamento_scadenziario();
		else
			return (it.cnr.contab.doccont00.core.bulk.IScadenzaDocumentoContabileBulk)getObbligazione_scadenziario();
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (28/09/2001 14.44.27)
	 * @return java.util.Collection
	 */
	public java.util.Collection getTermini() {
		return termini;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (28/09/2001 14.44.27)
	 * @return it.cnr.contab.anagraf00.tabrif.bulk.Rif_termini_pagamentoBulk
	 */
	public it.cnr.contab.anagraf00.tabrif.bulk.Rif_termini_pagamentoBulk getTermini_pagamento() {
		return termini_pagamento;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (07/03/2002 11.35.58)
	 * @return it.cnr.contab.anagraf00.tabrif.bulk.Rif_termini_pagamentoBulk
	 */
	public it.cnr.contab.anagraf00.tabrif.bulk.Rif_termini_pagamentoBulk getTermini_pagamento_uo_cds() {
		return termini_pagamento_uo_cds;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (07/03/2002 11.35.58)
	 * @return java.util.Collection
	 */
	public java.util.Collection getTermini_uo_cds() {
		return termini_uo_cds;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (27/09/2001 14.45.52)
	 * @return it.cnr.contab.anagraf00.core.bulk.TerzoBulk
	 */
	public it.cnr.contab.anagraf00.core.bulk.TerzoBulk getTerzo() {
		return terzo;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (07/03/2002 11.35.58)
	 * @return it.cnr.contab.anagraf00.core.bulk.TerzoBulk
	 */
	public it.cnr.contab.anagraf00.core.bulk.TerzoBulk getTerzo_uo_cds() {
		return terzo_uo_cds;
	}
	/*
	 * Getter dell'attributo ti_associato_manrev
	 */
	public java.util.Dictionary getTi_associato_manrevKeys() {
		return STATO_MANDATO;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (28/09/2001 16.43.41)
	 * @param newAnagrafico it.cnr.contab.anagraf00.core.bulk.AnagraficoBulk
	 */
	public it.cnr.contab.docamm00.tabrif.bulk.Voce_ivaBulk getVoce_iva(){
		return null;
	}
		public boolean isAbledToInsertBank() {
				return false;
	}
		public boolean isAbledToInsertBankUOCDS() {
			if (isAnnullato()||isPagata())
				return true;
			else
				return false;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (13/05/2002 10.48.32)
	 * @return boolean
	 */
	public boolean isAnnullato() {
		return STATO_ANNULLATO.equalsIgnoreCase(getStato_cofi());
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (2/7/2002 6:05:19 PM)
	 * @return it.cnr.contab.docamm00.docs.bulk.IDocumentoAmministrativoBulk
	 */
	public boolean isDirectlyLinkedToDC() {
		return false;
	}
	public boolean isPagata() {

		//Se si necessita di modifica, verificare che la condizione in 'checkIfRiportata' è ancora valida!
		return STATO_PAGATO.equals(getStato_cofi());
	}
	public boolean isRiportata() {

		return getRiportata().equals(RIPORTATO);
	}
	public boolean isROCd_terzo() {

		return getTerzo() == null ||
				getTerzo().getCrudStatus() == OggettoBulk.NORMAL;
	}
	public boolean isROModalita_pagamento() {
		if (Optional.ofNullable(getDocumento_generico())
				.map(Documento_genericoBulk::isGenericoAttivo)
				.orElse(Boolean.FALSE))  {
			if (isPagata() || isAnnullato()) {
                return true;
            }
			return false;
		}
		return Optional.ofNullable(getDocumento_generico())
				.map(Documento_genericoBulk::isDoc1210Associato)
				.orElse(Boolean.FALSE);
	}
	public boolean isROTerzo() {
		return Optional.ofNullable(getTerzo())
				.map(terzoBulk -> {
					return !((terzoBulk.getCrudStatus() == OggettoBulk.UNDEFINED
							|| terzoBulk.getCrudStatus() == OggettoBulk.NORMAL)
							&& getStato_cofi().equals(STATO_INIZIALE));
				}).orElse(Boolean.FALSE);
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (13/05/2002 10.48.32)
	 */
	public boolean isVoidable() {

		return isRiportata() ||
				(STATO_CONTABILIZZATO.equals(getStato_cofi()) &&
					ASSOCIATO_A_MANDATO.equals(getTi_associato_manrev())) ||
				(Documento_genericoBulk.STATO_CONTABILIZZATO.equals(getStato_cofi()) &&
					Documento_genericoBulk.PARZIALMENTE_ASSOCIATO_A_MANDATO.equals(getDocumento_generico().getTi_associato_manrev())) ||
				!Documento_genericoBulk.NON_REGISTRATO_IN_COGE.equalsIgnoreCase(getDocumento_generico().getStato_coge()) ||
				!Documento_genericoBulk.NON_CONTABILIZZATO_IN_COAN.equalsIgnoreCase(getDocumento_generico().getStato_coan());
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (07/01/2002 12.41.55)
	 * @param newAccertamento_scadenziario it.cnr.contab.doccont00.core.bulk.Accertamento_scadenzarioBulk
	 */
	public void setAccertamento_scadenziario(it.cnr.contab.doccont00.core.bulk.Accertamento_scadenzarioBulk newAccertamento_scadenziario) {
		accertamento_scadenziario = newAccertamento_scadenziario;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (28/09/2001 16.43.41)
	 * @param newAnagrafico it.cnr.contab.anagraf00.core.bulk.AnagraficoBulk
	 */
	public void setAnagrafico(it.cnr.contab.anagraf00.core.bulk.AnagraficoBulk newAnagrafico) {
		anagrafico = newAnagrafico;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (13/05/2002 10.43.32)
	 * @return boolean
	 */
	public void setAnnullato(java.sql.Timestamp date) {
		setStato_cofi(STATO_ANNULLATO);
		setDt_cancellazione(date);
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (28/09/2001 14.44.27)
	 * @param newBanca it.cnr.contab.anagraf00.core.bulk.BancaBulk
	 */
	public void setBanca(it.cnr.contab.anagraf00.core.bulk.BancaBulk newBanca) {
		banca = newBanca;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (07/03/2002 11.35.58)
	 * @param newBanca_uo_cds it.cnr.contab.anagraf00.core.bulk.BancaBulk
	 */
	public void setBanca_uo_cds(it.cnr.contab.anagraf00.core.bulk.BancaBulk newBanca_uo_cds) {
		banca_uo_cds = newBanca_uo_cds;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (28/09/2001 14.44.27)
	 * @param newBanche java.util.Collection
	 */
	public void setBanche(java.util.Collection newBanche) {
		banche = newBanche;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (07/03/2002 11.35.58)
	 * @param newBanche_uo_cds java.util.Collection
	 */
	public void setBanche_uo_cds(java.util.Collection newBanche_uo_cds) {
		banche_uo_cds = newBanche_uo_cds;
	}
	public void setCd_termini_pag(java.lang.String cd_termini_pag) {
		if (cd_termini_pag==null)
			super.setCd_termini_pag(null);
		else
			this.getTermini_pagamento().setCd_termini_pag(cd_termini_pag);
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (25/03/2002 12.10.19)
	 * @param newCessionario it.cnr.contab.anagraf00.core.bulk.TerzoBulk
	 */
	public void setCessionario(it.cnr.contab.anagraf00.core.bulk.TerzoBulk newCessionario) {
		cessionario = newCessionario;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (05/10/2001 11.56.28)
	 * @param newDocumento_generico it.cnr.contab.docamm00.docs.bulk.Documento_genericoBulk
	 */
	public void setDocumento_generico(Documento_genericoBulk newDocumento_generico) {
		documento_generico = newDocumento_generico;
	}
	public void setIm_diponibile_nc(java.math.BigDecimal im_diponibile_nc){
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (28/03/2002 15.54.56)
	 * @param newIm_riga_iniziale java.math.BigDecimal
	 */
	public void setIm_riga_iniziale(java.math.BigDecimal newIm_riga_iniziale) {
		im_riga_iniziale = newIm_riga_iniziale;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (28/09/2001 14.44.27)
	 * @param newModalita java.util.Collection
	 */
	public void setModalita(java.util.Collection newModalita) {
		modalita = newModalita;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (28/09/2001 14.44.27)
	 * @param newModalita_pagamento it.cnr.contab.anagraf00.tabrif.bulk.Rif_modalita_pagamentoBulk
	 */
	public void setModalita_pagamento(it.cnr.contab.anagraf00.tabrif.bulk.Rif_modalita_pagamentoBulk newModalita_pagamento) {
		modalita_pagamento = newModalita_pagamento;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (07/03/2002 11.35.58)
	 * @param newModalita_pagamento_uo_cds it.cnr.contab.anagraf00.tabrif.bulk.Rif_modalita_pagamentoBulk
	 */
	public void setModalita_pagamento_uo_cds(it.cnr.contab.anagraf00.tabrif.bulk.Rif_modalita_pagamentoBulk newModalita_pagamento_uo_cds) {
		modalita_pagamento_uo_cds = newModalita_pagamento_uo_cds;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (07/03/2002 11.35.58)
	 * @param newModalita_uo_cds java.util.Collection
	 */
	public void setModalita_uo_cds(java.util.Collection newModalita_uo_cds) {
		modalita_uo_cds = newModalita_uo_cds;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (03/12/2001 12.37.59)
	 * @param newObbligazione_scadenziario it.cnr.contab.doccont00.core.bulk.Obbligazione_scadenzarioBulk
	 */
	public void setObbligazione_scadenziario(it.cnr.contab.doccont00.core.bulk.Obbligazione_scadenzarioBulk newObbligazione_scadenziario) {
		obbligazione_scadenziario = newObbligazione_scadenziario;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (28/09/2001 14.44.27)
	 * @param newRiferimenti_bancari it.cnr.jada.bulk.BulkList
	 */
	public void setRiferimenti_bancari(it.cnr.jada.bulk.BulkList newRiferimenti_bancari) {
		riferimenti_bancari = newRiferimenti_bancari;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (30/05/2003 16.17.42)
	 * @param newRiportata java.lang.String
	 */
	public void setRiportata(java.lang.String newRiportata) {
		riportata = newRiportata;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (28/09/2001 14.44.27)
	 * @param newTermini java.util.Collection
	 */
	public void setTermini(java.util.Collection newTermini) {
		termini = newTermini;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (28/09/2001 14.44.27)
	 * @param newTermini_pagamento it.cnr.contab.anagraf00.tabrif.bulk.Rif_termini_pagamentoBulk
	 */
	public void setTermini_pagamento(it.cnr.contab.anagraf00.tabrif.bulk.Rif_termini_pagamentoBulk newTermini_pagamento) {
		termini_pagamento = newTermini_pagamento;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (07/03/2002 11.35.58)
	 * @param newTermini_pagamento_uo_cds it.cnr.contab.anagraf00.tabrif.bulk.Rif_termini_pagamentoBulk
	 */
	public void setTermini_pagamento_uo_cds(it.cnr.contab.anagraf00.tabrif.bulk.Rif_termini_pagamentoBulk newTermini_pagamento_uo_cds) {
		termini_pagamento_uo_cds = newTermini_pagamento_uo_cds;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (07/03/2002 11.35.58)
	 * @param newTermini_uo_cds java.util.Collection
	 */
	public void setTermini_uo_cds(java.util.Collection newTermini_uo_cds) {
		termini_uo_cds = newTermini_uo_cds;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (27/09/2001 14.45.52)
	 * @param newTerzo it.cnr.contab.anagraf00.core.bulk.TerzoBulk
	 */
	public void setTerzo(it.cnr.contab.anagraf00.core.bulk.TerzoBulk newTerzo) {
		terzo = newTerzo;
	}
	/**
	 * Insert the method's description here.
	 * Creation date: (07/03/2002 11.35.58)
	 * @param newTerzo_uo_cds it.cnr.contab.anagraf00.core.bulk.TerzoBulk
	 */
	public void setTerzo_uo_cds(it.cnr.contab.anagraf00.core.bulk.TerzoBulk newTerzo_uo_cds) {
		terzo_uo_cds = newTerzo_uo_cds;
	}
	public void validaDateCompetenza()
		throws ValidationException {

		String dsRiga = (getDs_riga() != null) ?
											"per il dettaglio \"" + getDs_riga() + "\"":
											"per il/i dettagli selezionato/i";
		if (getDt_da_competenza_coge() == null)
			throw new ValidationException("Inserire la data di \"competenza da\" " + dsRiga + ".");
		if (getDt_a_competenza_coge() == null)
			throw new ValidationException("Inserire la data di \"competenza a\" " + dsRiga + ".");

		Calendar competenzaDa = getDocumento_generico().getDateCalendar(getDt_da_competenza_coge());
		Calendar competenzaA = getDocumento_generico().getDateCalendar(getDt_a_competenza_coge());
		Calendar competenzaDaTestata = getDocumento_generico().getDateCalendar(getDocumento_generico().getDt_da_competenza_coge());
		Calendar competenzaATestata = getDocumento_generico().getDateCalendar(getDocumento_generico().getDt_a_competenza_coge());

		if (competenzaA.before(competenzaDa))
			throw new ValidationException("Inserire correttamente le date di competenza " + dsRiga + ".");

		if (competenzaDa.before(competenzaDaTestata))
			throw new ValidationException("La data di \"competenza Da\" deve essere successiva o uguale alla data di \"competenza da\" della testata " + dsRiga + ".");
		if (competenzaA.after(competenzaATestata))
			throw new ValidationException("La data di \"competenza A\" deve essere inferiore o uguale alla data di \"competenza a\" della testata " + dsRiga + ".");
	}
	/**
	 * Ritorna il valore da attribuire alla riga. La quota residua
	 * viene utilizzata per creare un'altra riga di dettaglio
	 *
	 * @return Returns the im_riga_sdoppia.
	 */
	public java.math.BigDecimal getIm_riga_sdoppia() {
		return im_riga_sdoppia;
	}
	/**
	 * Setta il valore nuovo da attribuire alla riga. La quota residua
	 * viene utilizzata per creare un'altra riga di dettaglio
	 *
	 * @param im_riga_sdoppia The im_riga_sdoppia to set.
	 */
	public void setIm_riga_sdoppia(java.math.BigDecimal im_riga_sdoppia) {
		this.im_riga_sdoppia = im_riga_sdoppia;
	}
	public boolean isInventariato() {
		return inventariato;
	}
	public void setInventariato(boolean inventariato) {
		this.inventariato = inventariato;
	}
	public void setCd_cds(String cd_cds) {
		if (this.getDocumento_generico()!=null)
		this.getDocumento_generico().setCd_cds(cd_cds);
	}
	public void setEsercizio(Integer esercizio) {
		if (this.getDocumento_generico()!=null)
		this.getDocumento_generico().setEsercizio(esercizio);
	}
	public void setCd_unita_organizzativa(String cd_unita_organizzativa) {
		if (this.getDocumento_generico()!=null)
		this.getDocumento_generico().setCd_unita_organizzativa(cd_unita_organizzativa);
	}

	public void setCd_tipo_documento_amm(String cd_tipo_documento_amm) {
		if (this.getDocumento_generico()!=null)
		this.getDocumento_generico().setCd_tipo_documento_amm(cd_tipo_documento_amm);
	}

	public void setPg_documento_generico(Long pg_documento_generico) {
		if (this.getDocumento_generico()!=null)
		this.getDocumento_generico().setPg_documento_generico(pg_documento_generico);
	}
	public void setCd_terzo(java.lang.Integer cd_terzo) {
		this.getTerzo().setCd_terzo(cd_terzo);
	}


	@Override
	public java.lang.Integer getCd_terzo() {
		it.cnr.contab.anagraf00.core.bulk.TerzoBulk terzo = this.getTerzo();
		if (terzo == null)
			return null;
		return terzo.getCd_terzo();
	}

	public static Documento_generico_rigaBulk copyByRigaDocumento(Documento_generico_rigaBulk origine) {
		Documento_generico_rigaBulk nuovoDettaglio = new Documento_generico_rigaBulk();
		if (origine.getTerzo() != null)
			nuovoDettaglio.setTerzo((TerzoBulk) origine.getTerzo().clone());
		if (origine.getTerzo_uo_cds() != null)
			nuovoDettaglio.setTerzo_uo_cds((TerzoBulk) origine.getTerzo_uo_cds().clone());
		if (origine.getAnagrafico() != null)
			nuovoDettaglio.setAnagrafico((AnagraficoBulk) origine.getAnagrafico().clone());
		if (origine.getCessionario() != null)
			nuovoDettaglio.setCessionario((TerzoBulk) origine.getCessionario().clone());
		if (origine.getTermini_pagamento() != null)
			nuovoDettaglio.setTermini_pagamento((Rif_termini_pagamentoBulk) origine.getTermini_pagamento().clone());
		if (origine.getTermini_pagamento_uo_cds() != null)
			nuovoDettaglio.setTermini_pagamento_uo_cds((Rif_termini_pagamentoBulk) origine.getTermini_pagamento_uo_cds().clone());
		nuovoDettaglio.setModalita(origine.getModalita());
		nuovoDettaglio.setModalita_uo_cds(origine.getModalita_uo_cds());
		if (origine.getModalita_pagamento() != null)
			nuovoDettaglio.setModalita_pagamento((Rif_modalita_pagamentoBulk) origine.getModalita_pagamento().clone());
		if (origine.getModalita_pagamento_uo_cds() != null)
			nuovoDettaglio.setModalita_pagamento_uo_cds((Rif_modalita_pagamentoBulk) origine.getModalita_pagamento_uo_cds().clone());
		if (origine.getBanca() != null)
			nuovoDettaglio.setBanca((BancaBulk) origine.getBanca().clone());
		if (origine.getBanca_uo_cds() != null)
			nuovoDettaglio.setBanca_uo_cds((BancaBulk) origine.getBanca_uo_cds().clone());
		nuovoDettaglio.setRagione_sociale(origine.getRagione_sociale());
		nuovoDettaglio.setEsercizio(origine.getEsercizio());
		nuovoDettaglio.setCd_cds(origine.getCd_cds());
		nuovoDettaglio.setCd_unita_organizzativa(origine.getCd_unita_organizzativa());
		nuovoDettaglio.setCd_tipo_documento_amm(origine.getCd_tipo_documento_amm());
		nuovoDettaglio.setStato_cofi(origine.getStato_cofi());
		nuovoDettaglio.setDt_da_competenza_coge(origine.getDt_da_competenza_coge());
		nuovoDettaglio.setDt_a_competenza_coge(origine.getDt_a_competenza_coge());
		nuovoDettaglio.setDs_riga(origine.getDs_riga());
		nuovoDettaglio.setCd_modalita_pag(origine.getCd_modalita_pag());
		nuovoDettaglio.setCd_modalita_pag_uo_cds(origine.getCd_modalita_pag_uo_cds());
		nuovoDettaglio.setNome(origine.getNome());
		nuovoDettaglio.setCognome(origine.getCognome());
		nuovoDettaglio.setCodice_fiscale(origine.getCodice_fiscale());
		nuovoDettaglio.setPartita_iva(origine.getPartita_iva());
		nuovoDettaglio.setIm_riga(origine.getIm_riga());
		nuovoDettaglio.setIm_riga_divisa(origine.getIm_riga());
		nuovoDettaglio.setPg_banca(origine.getPg_banca());
		nuovoDettaglio.setPg_banca_uo_cds(origine.getPg_banca_uo_cds());
		nuovoDettaglio.setTi_associato_manrev(origine.getTi_associato_manrev());
		nuovoDettaglio.setToBeCreated();
		return nuovoDettaglio;
	}

	@Override
	public void validate(OggettoBulk oggettobulk) throws ValidationException {
		super.validate(oggettobulk);
		try {
			Optional.ofNullable(getCodice_identificativo_ente_pagopa())
				.ifPresent(s -> {
					final String regex = "[0-9]{11}";
					if (!Pattern.compile(regex, Pattern.MULTILINE)
							.matcher(s)
							.find()) {
						throw new DetailedRuntimeException("L'identificativo ente può contenere solo numeri e la sua lunghezza deve essere di 11 caratteri!");
					}
				});
			Optional.ofNullable(getNumero_avviso_pagopa())
					.ifPresent(s -> {
						final String regex = "[0-9]{18}";
						if (!Pattern.compile(regex, Pattern.MULTILINE)
								.matcher(s)
								.find()) {
							throw new DetailedRuntimeException("Il numero dell'avviso può contenere solo numeri e la sua lunghezza deve essere di 18 caratteri!");
						}
					});
		} catch (DetailedRuntimeException _ex) {
			throw new ValidationException(_ex.getMessage());
		}
	}

	public Documento_generico_rigaBulk getDocumento_generico_riga_storno() {
		return documento_generico_riga_storno;
	}

	public void setDocumento_generico_riga_storno(Documento_generico_rigaBulk documento_generico_riga_storno) {
		this.documento_generico_riga_storno = documento_generico_riga_storno;
	}

	@Override
	public void setEsercizio_storno(Integer esercizio_storno) {
		Optional.ofNullable(getDocumento_generico_riga_storno())
				.ifPresent(
						documentoGenericoRigaBulk -> documentoGenericoRigaBulk.setEsercizio(esercizio_storno)
				);
	}

	@Override
	public Integer getEsercizio_storno() {
		return Optional.ofNullable(getDocumento_generico_riga_storno())
				.map(Documento_generico_rigaBulk::getEsercizio)
				.orElse(null);
	}

	@Override
	public void setCd_cds_storno(String cd_cds_storno) {
		Optional.ofNullable(getDocumento_generico_riga_storno())
				.ifPresent(
						documentoGenericoRigaBulk -> documentoGenericoRigaBulk.setCd_cds(cd_cds_storno)
				);
	}

	@Override
	public String getCd_cds_storno() {
		return Optional.ofNullable(getDocumento_generico_riga_storno())
				.map(Documento_generico_rigaBulk::getCd_cds)
				.orElse(null);
	}

	@Override
	public void setCd_unita_organizzativa_storno(String cd_unita_organizzativa_storno) {
		Optional.ofNullable(getDocumento_generico_riga_storno())
				.ifPresent(
						documentoGenericoRigaBulk -> documentoGenericoRigaBulk.setCd_unita_organizzativa(cd_unita_organizzativa_storno)
				);
	}

	@Override
	public String getCd_unita_organizzativa_storno() {
		return Optional.ofNullable(getDocumento_generico_riga_storno())
				.map(Documento_generico_rigaBulk::getCd_unita_organizzativa)
				.orElse(null);
	}

	@Override
	public void setCd_tipo_documento_amm_storno(String cd_tipo_documento_amm_storno) {
		Optional.ofNullable(getDocumento_generico_riga_storno())
				.ifPresent(
						documentoGenericoRigaBulk -> documentoGenericoRigaBulk.setCd_tipo_documento_amm(cd_tipo_documento_amm_storno)
				);
	}

	@Override
	public String getCd_tipo_documento_amm_storno() {
		return Optional.ofNullable(getDocumento_generico_riga_storno())
				.map(Documento_generico_rigaBulk::getCd_tipo_documento_amm)
				.orElse(null);
	}

	@Override
	public void setPg_documento_generico_storno(Long pg_documento_generico_storno) {
		Optional.ofNullable(getDocumento_generico_riga_storno())
				.ifPresent(
						documentoGenericoRigaBulk -> documentoGenericoRigaBulk.setPg_documento_generico(pg_documento_generico_storno)
				);
	}

	@Override
	public Long getPg_documento_generico_storno() {
		return Optional.ofNullable(getDocumento_generico_riga_storno())
				.map(Documento_generico_rigaBulk::getPg_documento_generico)
				.orElse(null);
	}

	@Override
	public void setProgressivo_riga_storno(Long progressivo_riga_storno) {
		Optional.ofNullable(getDocumento_generico_riga_storno())
				.ifPresent(
						documentoGenericoRigaBulk -> documentoGenericoRigaBulk.setProgressivo_riga(progressivo_riga_storno)
				);
	}

	@Override
	public Long getProgressivo_riga_storno() {
		return Optional.ofNullable(getDocumento_generico_riga_storno())
				.map(Documento_generico_rigaBulk::getProgressivo_riga)
				.orElse(null);
	}

	public boolean isDocumentoStorno() {
		return Optional.ofNullable(getDocumento_generico())
				.flatMap(documentoGenericoBulk -> Optional.ofNullable(documentoGenericoBulk.getFl_storno()))
				.orElse(Boolean.FALSE);
	}

	public BigDecimal getImportoStornato() {
		return importoStornato;
	}

	public void setImportoStornato(BigDecimal importoStornato) {
		this.importoStornato = importoStornato;
	}

	public Boolean isRigaStornata() {
		return Optional.ofNullable(rigaStornata).orElse(Boolean.FALSE);
	}

	public void setRigaStornata(Boolean rigaStornata) {
		this.rigaStornata = rigaStornata;
	}

	public Fattura_attiva_rigaIBulk getFattura_attiva_riga_storno() {
		return fattura_attiva_riga_storno;
	}

	public void setFattura_attiva_riga_storno(Fattura_attiva_rigaIBulk fattura_attiva_riga_storno) {
		this.fattura_attiva_riga_storno = fattura_attiva_riga_storno;
	}

	public Fattura_passiva_rigaIBulk getFattura_passiva_riga_storno() {
		return fattura_passiva_riga_storno;
	}

	public void setFattura_passiva_riga_storno(Fattura_passiva_rigaIBulk fattura_passiva_riga_storno) {
		this.fattura_passiva_riga_storno = fattura_passiva_riga_storno;
	}

	@Override
	public Integer getEsercizio_storno_fa() {
		return Optional.ofNullable(getFattura_attiva_riga_storno())
				.map(Fattura_attiva_rigaIBulk::getEsercizio)
				.orElse(null);
	}

	@Override
	public void setEsercizio_storno_fa(Integer esercizio_storno_fa) {
		Optional.ofNullable(getFattura_attiva_riga_storno())
				.ifPresent(
						fatturaAttivaRigaIBulk -> fatturaAttivaRigaIBulk.setEsercizio(esercizio_storno_fa)
				);
	}

	@Override
	public Long getProgressivo_riga_storno_fa() {
		return Optional.ofNullable(getFattura_attiva_riga_storno())
				.map(Fattura_attiva_rigaIBulk::getProgressivo_riga)
				.orElse(null);
	}

	@Override
	public void setProgressivo_riga_storno_fa(Long progressivo_riga_storno_fa) {
		Optional.ofNullable(getFattura_attiva_riga_storno())
				.ifPresent(
						fatturaAttivaRigaIBulk -> fatturaAttivaRigaIBulk.setProgressivo_riga(progressivo_riga_storno_fa)
				);
	}

	@Override
	public String getCd_unita_organizzativa_storno_fa() {
		return Optional.ofNullable(getFattura_attiva_riga_storno())
				.map(Fattura_attiva_rigaIBulk::getCd_unita_organizzativa)
				.orElse(null);
	}

	@Override
	public void setCd_unita_organizzativa_storno_fa(String cd_unita_organizzativa_storno_fa) {
		Optional.ofNullable(getFattura_attiva_riga_storno())
				.ifPresent(
						fatturaAttivaRigaIBulk -> fatturaAttivaRigaIBulk.setCd_unita_organizzativa(cd_unita_organizzativa_storno_fa)
				);
	}

	@Override
	public String getCd_cds_storno_fa() {
		return Optional.ofNullable(getFattura_attiva_riga_storno())
				.map(Fattura_attiva_rigaIBulk::getCd_cds)
				.orElse(null);
	}

	@Override
	public void setCd_cds_storno_fa(String cd_cds_storno_fa) {
		Optional.ofNullable(getFattura_attiva_riga_storno())
				.ifPresent(
						fatturaAttivaRigaIBulk -> fatturaAttivaRigaIBulk.setCd_cds(cd_cds_storno_fa)
				);
	}

	@Override
	public Long getPg_fattura_attiva_storno() {
		return Optional.ofNullable(getFattura_attiva_riga_storno())
				.map(Fattura_attiva_rigaIBulk::getPg_fattura_attiva)
				.orElse(null);
	}

	@Override
	public void setPg_fattura_attiva_storno(Long pg_fattura_attiva_storno) {
		Optional.ofNullable(getFattura_attiva_riga_storno())
				.ifPresent(
						fatturaAttivaRigaIBulk -> fatturaAttivaRigaIBulk.setPg_fattura_attiva(pg_fattura_attiva_storno)
				);
	}

	@Override
	public Integer getEsercizio_storno_fp() {
		return Optional.ofNullable(getFattura_passiva_riga_storno())
				.map(Fattura_passiva_rigaIBulk::getEsercizio)
				.orElse(null);
	}

	@Override
	public void setEsercizio_storno_fp(Integer esercizio_storno_fp) {
		Optional.ofNullable(getFattura_passiva_riga_storno())
				.ifPresent(
						fatturaPassivaRigaIBulk -> fatturaPassivaRigaIBulk.setEsercizio(esercizio_storno_fp)
				);
	}

	@Override
	public Long getProgressivo_riga_storno_fp() {
		return Optional.ofNullable(getFattura_passiva_riga_storno())
				.map(Fattura_passiva_rigaIBulk::getProgressivo_riga)
				.orElse(null);
	}

	@Override
	public void setProgressivo_riga_storno_fp(Long progressivo_riga_storno_fp) {
		Optional.ofNullable(getFattura_passiva_riga_storno())
				.ifPresent(
						fatturaPassivaRigaIBulk -> fatturaPassivaRigaIBulk.setProgressivo_riga(progressivo_riga_storno_fp)
				);
	}

	@Override
	public String getCd_unita_organizzativa_storno_fp() {
		return Optional.ofNullable(getFattura_passiva_riga_storno())
				.map(Fattura_passiva_rigaIBulk::getCd_unita_organizzativa)
				.orElse(null);
	}

	@Override
	public void setCd_unita_organizzativa_storno_fp(String cd_unita_organizzativa_storno_fp) {
		Optional.ofNullable(getFattura_passiva_riga_storno())
				.ifPresent(
						fatturaPassivaRigaIBulk -> fatturaPassivaRigaIBulk.setCd_unita_organizzativa(cd_unita_organizzativa_storno_fp)
				);
	}

	@Override
	public String getCd_cds_storno_fp() {
		return Optional.ofNullable(getFattura_passiva_riga_storno())
				.map(Fattura_passiva_rigaIBulk::getCd_cds)
				.orElse(null);
	}

	@Override
	public void setCd_cds_storno_fp(String cd_cds_storno_fp) {
		Optional.ofNullable(getFattura_passiva_riga_storno())
				.ifPresent(
						fatturaPassivaRigaIBulk -> fatturaPassivaRigaIBulk.setCd_cds(cd_cds_storno_fp)
				);
	}

	@Override
	public Long getPg_fattura_passiva_storno() {
		return Optional.ofNullable(getFattura_passiva_riga_storno())
				.map(Fattura_passiva_rigaIBulk::getPg_fattura_passiva)
				.orElse(null);
	}

	@Override
	public void setPg_fattura_passiva_storno(Long pg_fattura_passiva_storno) {
		Optional.ofNullable(getFattura_passiva_riga_storno())
				.ifPresent(
						fatturaPassivaRigaIBulk -> fatturaPassivaRigaIBulk.setPg_fattura_passiva(pg_fattura_passiva_storno)
				);
	}

	public Optional<IDocumentoAmministrativoRigaBulk> getRigaStorno() {
		Optional<IDocumentoAmministrativoRigaBulk> documentoGenericoRigaStorno =
				Optional.ofNullable(getDocumento_generico_riga_storno());
		Optional<IDocumentoAmministrativoRigaBulk> fattura_attiva_riga_storno =
				Optional.ofNullable(getFattura_attiva_riga_storno());
		Optional<IDocumentoAmministrativoRigaBulk> fattura_passiva_riga_storno =
				Optional.ofNullable(getFattura_passiva_riga_storno());
		return Optional.ofNullable(documentoGenericoRigaStorno
					.orElse(fattura_attiva_riga_storno
							.orElse(fattura_passiva_riga_storno
									.orElse(null))));
	}
}

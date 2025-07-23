/*
 * Copyright (C) 2020  Consiglio Nazionale delle Ricerche
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
 * Created by BulkGenerator 2.0 [07/12/2009]
 * Date 28/06/2017
 */
package it.cnr.contab.ordmag.ordini.bulk;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import it.cnr.contab.anagraf00.core.bulk.BancaBulk;
import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.contab.anagraf00.tabrif.bulk.Rif_modalita_pagamentoBulk;
import it.cnr.contab.coepcoan00.core.bulk.IDocumentoDetailAnaCogeBulk;
import it.cnr.contab.config00.contratto.bulk.Dettaglio_contrattoBulk;
import it.cnr.contab.config00.pdcep.bulk.ContoBulk;
import it.cnr.contab.docamm00.docs.bulk.Documento_generico_riga_ecoBulk;
import it.cnr.contab.docamm00.docs.bulk.IDocumentoAmministrativoBulk;
import it.cnr.contab.docamm00.docs.bulk.IDocumentoAmministrativoRigaBulk;
import it.cnr.contab.docamm00.docs.bulk.Voidable;
import it.cnr.contab.docamm00.tabrif.bulk.Bene_servizioBulk;
import it.cnr.contab.docamm00.tabrif.bulk.Voce_ivaBulk;
import it.cnr.contab.doccont00.core.bulk.IScadenzaDocumentoContabileBulk;
import it.cnr.contab.doccont00.core.bulk.Obbligazione_scadenzarioBulk;
import it.cnr.contab.ordmag.anag00.LuogoConsegnaMagBulk;
import it.cnr.contab.ordmag.anag00.MagazzinoBulk;
import it.cnr.contab.ordmag.anag00.UnitaMisuraBulk;
import it.cnr.contab.ordmag.anag00.UnitaOperativaOrdBulk;
import it.cnr.contab.util00.bulk.storage.AllegatoGenericoBulk;
import it.cnr.contab.util00.bulk.storage.AllegatoParentBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.bulk.BulkCollection;
import it.cnr.jada.bulk.BulkList;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.util.StrServ;
import it.cnr.jada.util.action.CRUDBP;
public class OrdineAcqRigaBulk extends OrdineAcqRigaBase implements IDocumentoAmministrativoRigaBulk, Voidable, AllegatoParentBulk {
	protected BulkList<OrdineAcqConsegnaBulk> righeConsegnaColl= new BulkList<OrdineAcqConsegnaBulk>();
	private BulkList<OrdineAcqRigaEcoBulk> righeEconomica = new BulkList<>();

	private java.lang.String dspTipoConsegna;
	private java.lang.String dspStato;
	private StatoContabilizzazione dspStatoContabilizzazione;
	private Dettaglio_contrattoBulk dettaglioContratto;

	private java.lang.String tipoConsegnaDefault;

	private java.sql.Timestamp dspDtPrevConsegna;

	public String getDspStato() {
		return dspStato;
	}

	public void setDspStato(String dspStato) {
		this.dspStato = dspStato;
	}

	private java.math.BigDecimal dspQuantita;

	private LuogoConsegnaMagBulk dspLuogoConsegna;

	private Obbligazione_scadenzarioBulk dspObbligazioneScadenzario;

	private MagazzinoBulk dspMagazzino;

	private ContoBulk dspConto;

	public ContoBulk getDspConto() {
		return dspConto;
	}

	public void setDspConto(ContoBulk dspConto) {
		this.dspConto = dspConto;
	}

	private UnitaOperativaOrdBulk dspUopDest;
	
	private Boolean consegneModificate = false;
	public final static String STATO_INSERITA= "INS";
    public final static String STATO_ANNULLATA= "ANN";
	/**
	 * [ORDINE_ACQ Testata Ordine d'Acquisto]
	 **/
	private OrdineAcqBulk ordineAcq =  new OrdineAcqBulk();
	/**
	 * [BENE_SERVIZIO Rappresenta la classificazione di beni e servizi il cui dettaglio è esposto in sede di registrazione delle righe fattura passiva.

Da questa gestione sono ricavati gli elementi per la gestione di magazziono e di inventario dalla registrazione di fatture passive]
	 **/
	private Bene_servizioBulk beneServizio =  new Bene_servizioBulk();
	/**
	 * [VOCE_IVA La tabella iva contiene i codici e le aliquote dell'iva, commerciale o istituzionale, registrata nei dettagli della fattura attiva e passiva. Questa entità si riferisce alla normativa vigente sull'iva.]
	 **/
	private Voce_ivaBulk voceIva =  new Voce_ivaBulk();
	/**
	 * [UNITA_MISURA Rappresenta l'anagrafica delle unità di misura.]
	 **/
	private UnitaMisuraBulk unitaMisura =  new UnitaMisuraBulk();
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: ORDINE_ACQ_RIGA
	 **/
	private BulkList<AllegatoGenericoBulk> dettaglioAllegati = new BulkList<AllegatoGenericoBulk>();

	private ContoBulk voce_ep = new ContoBulk();

	public OrdineAcqRigaBulk() {
		super();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Table name: ORDINE_ACQ_RIGA
	 **/
	public OrdineAcqRigaBulk(java.lang.String cdCds, java.lang.String cdUnitaOperativa, java.lang.Integer esercizio, java.lang.String cdNumeratore, java.lang.Integer numero, java.lang.Integer riga) {
		super(cdCds, cdUnitaOperativa, esercizio, cdNumeratore, numero, riga);
		setOrdineAcq( new OrdineAcqBulk(cdCds,cdUnitaOperativa,esercizio,cdNumeratore,numero) );
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Testata Ordine d'Acquisto]
	 **/
	public OrdineAcqBulk getOrdineAcq() {
		return ordineAcq;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Testata Ordine d'Acquisto]
	 **/
	public void setOrdineAcq(OrdineAcqBulk ordineAcq)  {
		this.ordineAcq=ordineAcq;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Rappresenta la classificazione di beni e servizi il cui dettaglio è esposto in sede di registrazione delle righe fattura passiva.

Da questa gestione sono ricavati gli elementi per la gestione di magazziono e di inventario dalla registrazione di fatture passive]
	 **/
	public Bene_servizioBulk getBeneServizio() {
		return beneServizio;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Rappresenta la classificazione di beni e servizi il cui dettaglio è esposto in sede di registrazione delle righe fattura passiva.

Da questa gestione sono ricavati gli elementi per la gestione di magazziono e di inventario dalla registrazione di fatture passive]
	 **/
	public void setBeneServizio(Bene_servizioBulk beneServizio)  {
		this.beneServizio=beneServizio;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [La tabella iva contiene i codici e le aliquote dell'iva, commerciale o istituzionale, registrata nei dettagli della fattura attiva e passiva. Questa entità si riferisce alla normativa vigente sull'iva.]
	 **/
	public Voce_ivaBulk getVoceIva() {
		return voceIva;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [La tabella iva contiene i codici e le aliquote dell'iva, commerciale o istituzionale, registrata nei dettagli della fattura attiva e passiva. Questa entità si riferisce alla normativa vigente sull'iva.]
	 **/
	public void setVoceIva(Voce_ivaBulk voceIva)  {
		this.voceIva=voceIva;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [Rappresenta l'anagrafica delle unità di misura.]
	 **/
	public UnitaMisuraBulk getUnitaMisura() {
		return unitaMisura;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [Rappresenta l'anagrafica delle unità di misura.]
	 **/
	public void setUnitaMisura(UnitaMisuraBulk unitaMisura)  {
		this.unitaMisura=unitaMisura;
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cdCds]
	 **/
	public java.lang.String getCdCds() {
		OrdineAcqBulk ordineAcq = this.getOrdineAcq();
		if (ordineAcq == null)
			return null;
		return getOrdineAcq().getCdCds();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cdCds]
	 **/
	public void setCdCds(java.lang.String cdCds)  {
		this.getOrdineAcq().setCdCds(cdCds);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cdUnitaOperativa]
	 **/
	public java.lang.String getCdUnitaOperativa() {
		OrdineAcqBulk ordineAcq = this.getOrdineAcq();
		if (ordineAcq == null)
			return null;
		return getOrdineAcq().getCdUnitaOperativa();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cdUnitaOperativa]
	 **/
	public void setCdUnitaOperativa(java.lang.String cdUnitaOperativa)  {
		this.getOrdineAcq().setCdUnitaOperativa(cdUnitaOperativa);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [esercizio]
	 **/
	public java.lang.Integer getEsercizio() {
		OrdineAcqBulk ordineAcq = this.getOrdineAcq();
		if (ordineAcq == null)
			return null;
		return getOrdineAcq().getEsercizio();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [esercizio]
	 **/
	public void setEsercizio(java.lang.Integer esercizio)  {
		this.getOrdineAcq().setEsercizio(esercizio);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cdNumeratore]
	 **/
	public java.lang.String getCdNumeratore() {
		OrdineAcqBulk ordineAcq = this.getOrdineAcq();
		if (ordineAcq == null)
			return null;
		return getOrdineAcq().getCdNumeratore();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cdNumeratore]
	 **/
	public void setCdNumeratore(java.lang.String cdNumeratore)  {
		this.getOrdineAcq().setCdNumeratore(cdNumeratore);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [numero]
	 **/
	public java.lang.Integer getNumero() {
		OrdineAcqBulk ordineAcq = this.getOrdineAcq();
		if (ordineAcq == null)
			return null;
		return getOrdineAcq().getNumero();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [numero]
	 **/
	public void setNumero(java.lang.Integer numero)  {
		this.getOrdineAcq().setNumero(numero);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cdBeneServizio]
	 **/
	public java.lang.String getCdBeneServizio() {
		Bene_servizioBulk beneServizio = this.getBeneServizio();
		if (beneServizio == null)
			return null;
		return getBeneServizio().getCd_bene_servizio();
	}
	public java.lang.Long getIdDettaglioContratto() {
		Dettaglio_contrattoBulk dettaglio_contrattoBulk = this.getDettaglioContratto();
		if (dettaglio_contrattoBulk == null)
			return null;
		return getDettaglioContratto().getId();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cdBeneServizio]
	 **/
	public void setCdBeneServizio(java.lang.String cdBeneServizio)  {
		this.getBeneServizio().setCd_bene_servizio(cdBeneServizio);
	}
	public void setIdDettaglioContratto(java.lang.Long idDettaglioContratto)  {
		this.getDettaglioContratto().setId(idDettaglioContratto);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cdVoceIva]
	 **/
	public java.lang.String getCdVoceIva() {
		Voce_ivaBulk voceIva = this.getVoceIva();
		if (voceIva == null)
			return null;
		return getVoceIva().getCd_voce_iva();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cdVoceIva]
	 **/
	public void setCdVoceIva(java.lang.String cdVoceIva)  {
		this.getVoceIva().setCd_voce_iva(cdVoceIva);
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Restituisce il valore di: [cdUnitaMisura]
	 **/
	public java.lang.String getCdUnitaMisura() {
		UnitaMisuraBulk unitaMisura = this.getUnitaMisura();
		if (unitaMisura == null)
			return null;
		return getUnitaMisura().getCdUnitaMisura();
	}
	/**
	 * Created by BulkGenerator 2.0 [07/12/2009]
	 * Setta il valore di: [cdUnitaMisura]
	 **/
	public void setCdUnitaMisura(java.lang.String cdUnitaMisura)  {
		this.getUnitaMisura().setCdUnitaMisura(cdUnitaMisura);
	}
	public java.lang.String getDspTipoConsegna() {
		return dspTipoConsegna;
	}
	public void setDspTipoConsegna(java.lang.String dspTipoConsegna) {
		this.dspTipoConsegna = dspTipoConsegna;
	}
	public java.sql.Timestamp getDspDtPrevConsegna() {
		return dspDtPrevConsegna;
	}
	public void setDspDtPrevConsegna(java.sql.Timestamp dspDtPrevConsegna) {
		this.dspDtPrevConsegna = dspDtPrevConsegna;
	}
	public java.math.BigDecimal getDspQuantita() {
		return dspQuantita;
	}
	public void setDspQuantita(java.math.BigDecimal dspQuantita) {
		this.dspQuantita = dspQuantita;
	}
	public LuogoConsegnaMagBulk getDspLuogoConsegna() {
		return dspLuogoConsegna;
	}
	public void setDspLuogoConsegna(LuogoConsegnaMagBulk dspLuogoConsegna) {
		this.dspLuogoConsegna = dspLuogoConsegna;
	}
	public MagazzinoBulk getDspMagazzino() {
		return dspMagazzino;
	}
	public void setDspMagazzino(MagazzinoBulk dspMagazzino) {
		this.dspMagazzino = dspMagazzino;
	}
	public UnitaOperativaOrdBulk getDspUopDest() {
		return dspUopDest;
	}
	public void setDspUopDest(UnitaOperativaOrdBulk dspUopDest) {
		this.dspUopDest = dspUopDest;
	}
	public boolean isROPrezzoUnitario(){
		if (getDettaglioContratto() == null || getDettaglioContratto().getPrezzoUnitario() == null){
			return false;
		}
		return true;
	}
	public Boolean isROCoefConv(){
		if (getUnitaMisura() != null && getUnitaMisura().getCdUnitaMisura() != null && 
				getBeneServizio() != null && getBeneServizio().getUnitaMisura() != null && getBeneServizio().getCdUnitaMisura() != null && 
				!getUnitaMisura().getCdUnitaMisura().equals(getBeneServizio().getCdUnitaMisura()) && (getDettaglioContratto() == null || getDettaglioContratto().getCdUnitaMisura() == null)){
			return false;
		}
		return true;
	}
	public Dictionary getTipoConsegnaKeys() {
		return OrdineAcqConsegnaBulk.TIPO_CONSEGNA;
	}
	public Dictionary getStatoKeys() {
		return OrdineAcqConsegnaBulk.STATO;
	}
	public OggettoBulk initializeForInsert(CRUDBP bp, ActionContext context)
	{
		return inizializzaPerInserimento(context.getUserContext());
	}
	public OggettoBulk inizializzaPerInserimento(UserContext userContext) {
		setStato(STATO_INSERITA);
		setImImponibile(BigDecimal.ZERO);
		setImImponibileDivisa(BigDecimal.ZERO);
		setImIva(BigDecimal.ZERO);
		setImIvaD(BigDecimal.ZERO);
		setImIvaNd(BigDecimal.ZERO);
		setImIvaDivisa(BigDecimal.ZERO);
		setImTotaleRiga(BigDecimal.ZERO);
		setDspDtPrevConsegna(OrdineAcqConsegnaBulk.recuperoDataDefaultPrevistaConsegna(userContext));
		return this;
	}
	public BulkList<OrdineAcqConsegnaBulk> getRigheConsegnaColl() {
		return righeConsegnaColl;
	}
	public void setRigheConsegnaColl(BulkList<OrdineAcqConsegnaBulk> righeConsegnaColl) {
		this.righeConsegnaColl = righeConsegnaColl;
	}
	public OrdineAcqConsegnaBulk removeFromRigheConsegnaColl(int index) 
	{
		// Gestisce la selezione del bottone cancella repertorio
		OrdineAcqConsegnaBulk consegna = (OrdineAcqConsegnaBulk)righeConsegnaColl.remove(index);
		consegna.setToBeDeleted();
		return consegna;
	}
	public int addToRigheConsegnaColl( OrdineAcqConsegnaBulk nuovoRigo ) 
	{


//		nuovoRigo.setTi_associato_manrev(nuovoRigo.NON_ASSOCIATO_A_MANDATO);
//		nuovoRigo.setTerzo(new TerzoBulk());
//		if (getTi_entrate_spese()==ENTRATE){
//			nuovoRigo.setTerzo_uo_cds(getTerzo_uo_cds());		
//		}
		nuovoRigo.setOrdineAcqRiga(this);

//		try {
//			java.sql.Timestamp ts = it.cnr.jada.util.ejb.EJBCommonServices.getServerTimestamp();
//			nuovoRigo.setDt_da_competenza_coge((getDt_da_competenza_coge() == null)?ts : getDt_da_competenza_coge());
//			nuovoRigo.setDt_a_competenza_coge((getDt_a_competenza_coge() == null)?ts : getDt_a_competenza_coge());
//		} catch (javax.ejb.EJBException e) {
//			throw new it.cnr.jada.DetailedRuntimeException(e);
//		}	
		nuovoRigo.setStato(OrdineAcqRigaBulk.STATO_INSERITA);
		int max = 0;
		for (Iterator i = righeConsegnaColl.iterator(); i.hasNext();) {
			int prog = ((OrdineAcqConsegnaBulk)i.next()).getConsegna();
			if (prog > max) max = prog;
		}
		nuovoRigo.setConsegna(new Integer(max+1));
		righeConsegnaColl.add(nuovoRigo);
		return righeConsegnaColl.size()-1;
	}
	public BigDecimal getQuantitaConsegneColl()
	{
		BigDecimal quantita = BigDecimal.ZERO;
		for (Iterator i = righeConsegnaColl.iterator(); i.hasNext();) {
			OrdineAcqConsegnaBulk cons = ((OrdineAcqConsegnaBulk)i.next());
			if (!cons.isConsegna0()){
				BigDecimal qtaCons = cons.getQuantitaEvasa() == null ? cons.getQuantita() : cons.getQuantitaEvasa();
				quantita = quantita.add(qtaCons);
			}
		}
		return quantita;
	}
	public BulkCollection[] getBulkLists() {

		// Metti solo le liste di oggetti che devono essere resi persistenti

		return new it.cnr.jada.bulk.BulkCollection[] { 
				righeConsegnaColl,
				righeEconomica,
				dettaglioAllegati
		};
	}
	public List getChildren() {
		return getRigheConsegnaColl();
	}
	public Boolean getConsegneModificate() {
		return consegneModificate;
	}
	public void setConsegneModificate(Boolean consegneModificate) {
		this.consegneModificate = consegneModificate;
	}
	public Obbligazione_scadenzarioBulk getDspObbligazioneScadenzario() {
		return dspObbligazioneScadenzario;
	}
	public void setDspObbligazioneScadenzario(Obbligazione_scadenzarioBulk dspObbligazioneScadenzario) {
		this.dspObbligazioneScadenzario = dspObbligazioneScadenzario;
	}
	@Override
	public Timestamp getDt_cancellazione() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public boolean isAnnullato() {
		return STATO_ANNULLATA.equalsIgnoreCase(getStato());
	}
	@Override
	public boolean isVoidable() {
		return isConsegnaEvasa();
	}
	public boolean isConsegnaEvasa() {
		for (Iterator i = righeConsegnaColl.iterator(); i.hasNext();) {
			String stato = ((OrdineAcqConsegnaBulk)i.next()).getStato();
			if (stato != null && stato.equals(OrdineAcqConsegnaBulk.STATO_EVASA)){
				return true;
			}
		}

		return false;
	}
	@Override
	public void setAnnullato(Timestamp date) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setDt_cancellazione(Timestamp date) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public IDocumentoAmministrativoRigaBulk getAssociatedDetail() {
		return null;
	}
	@Override
	public IDocumentoAmministrativoBulk getFather() {
		return getOrdineAcq();
	}

	@Override
	public BigDecimal getIm_diponibile_nc() {
		return null;
	}
	@Override
	public BigDecimal getIm_imponibile() {
		return getImImponibile();
	}

	@Override
	public BigDecimal getIm_iva() {
		return getImIva();
	}
	@Override
	public IDocumentoAmministrativoRigaBulk getOriginalDetail() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public IScadenzaDocumentoContabileBulk getScadenzaDocumentoContabile() {
		return (IScadenzaDocumentoContabileBulk)getDspObbligazioneScadenzario();
	}
	@Override
	public Voce_ivaBulk getVoce_iva() {
		return getVoceIva();
	}
	@Override
	public boolean isDirectlyLinkedToDC() {
		return false;
	}
	@Override
	public boolean isRiportata() {
		return false;
	}
	@Override
	public void setIm_diponibile_nc(BigDecimal im_diponibile_nc) {
		
	}
	public java.lang.String getTipoConsegnaDefault() {
		return tipoConsegnaDefault;
	}
	public void setTipoConsegnaDefault(java.lang.String tipoConsegnaDefault) {
		this.tipoConsegnaDefault = tipoConsegnaDefault;
	}
	public String getRigaOrdineString() {
		return getOrdineAcq().getOrdineString()
				.concat("/")
				.concat(String.valueOf(this.getRiga()));
	}

	@Override
	public Integer getCd_terzo() {
		return this.getOrdineAcq().getCdTerzo();
	}

	@Override
	public Timestamp getDt_da_competenza_coge() {
		// TODO: 14/06/21 Da implementare 
		return null;
	}

	@Override
	public Timestamp getDt_a_competenza_coge() {
		// TODO: 14/06/21 Da implementare
		return null;
	}

	@Override
	public BancaBulk getBanca() {
		return getOrdineAcq().getBanca();
	}

	@Override
	public void setBanca(BancaBulk bancaBulk) {
		getOrdineAcq().setBanca(bancaBulk);
	}

	@Override
	public Rif_modalita_pagamentoBulk getModalita_pagamento() {
		return getOrdineAcq().getModalitaPagamento();
	}

	@Override
	public void setModalita_pagamento(Rif_modalita_pagamentoBulk modalita_pagamento) {
		getOrdineAcq().setModalitaPagamento(modalita_pagamento);
	}

	@Override
	public String getDs_riga() {
		return getNotaRiga();
	}

	public Dettaglio_contrattoBulk getDettaglioContratto() {
		return dettaglioContratto;
	}

	public void setDettaglioContratto(Dettaglio_contrattoBulk dettaglioContratto) {
		this.dettaglioContratto = dettaglioContratto;
	}

	@Override
	public TerzoBulk getTerzo() {
		return this.getOrdineAcq().getFornitore();
	}

	public String constructCMISNomeFile() {
		StringBuffer nomeFile = new StringBuffer();
		nomeFile = nomeFile.append(StrServ.lpad(this.getRiga().toString(), 9, "0"));
		return nomeFile.toString();
	}

	@Override
	public int addToArchivioAllegati(AllegatoGenericoBulk allegato) {
		dettaglioAllegati.add(allegato);
		return dettaglioAllegati.size()-1;
	}

	@Override
	public AllegatoGenericoBulk removeFromArchivioAllegati(int index) {
		AllegatoGenericoBulk allegato = dettaglioAllegati.remove(index);
		allegato.setToBeDeleted();
		return allegato;
	}

	@Override
	public BulkList<AllegatoGenericoBulk> getArchivioAllegati() {
		return dettaglioAllegati;
	}

	@Override
	public void setArchivioAllegati(BulkList<AllegatoGenericoBulk> archivioAllegati) {
		this.dettaglioAllegati = archivioAllegati;
	}

	public StatoContabilizzazione getDspStatoContabilizzazione() {
		return dspStatoContabilizzazione;
	}

	public void setDspStatoContabilizzazione(StatoContabilizzazione dspStatoContabilizzazione) {
		this.dspStatoContabilizzazione = dspStatoContabilizzazione;
	}

	public static final Map<StatoContabilizzazione, String> statoContabilizzazioneKeys = Arrays.stream(
			StatoContabilizzazione.values()
	).collect(Collectors.toMap(
			s -> s, s -> s.getLabel(),
			(u, v) -> {
				throw new IllegalStateException(
						String.format("Cannot have 2 values (%s, %s) for the same key", u, v)
				);
			}, Hashtable::new
	));

	public enum StatoContabilizzazione {
		NON_CONTABILIZZATA("No"),
		CONTABILIZZATA("Si"),
		PARZIALMENTE_CONTABILIZZATA("Parzialmente");
		private final String label;

		StatoContabilizzazione(String label) {
			this.label = label;
		}

		public String getLabel() {
			return label;
		}
	}

	public BulkList<OrdineAcqRigaEcoBulk> getRigheEconomica() {
		return righeEconomica;
	}

	public void setRigheEconomica(BulkList<OrdineAcqRigaEcoBulk> righeEconomica) {
		this.righeEconomica = righeEconomica;
	}

	public OrdineAcqRigaEcoBulk removeFromRigheEconomica(int index)	{
		// Gestisce la selezione del bottone cancella
		OrdineAcqRigaEcoBulk rigaEco = righeEconomica.remove(index);
		rigaEco.setToBeDeleted();
		return rigaEco;
	}
	public int addToRigheEconomica( OrdineAcqRigaEcoBulk nuovoRigo )	{
		nuovoRigo.setOrdineAcqRiga(this);
		righeEconomica.add(nuovoRigo);
		return righeEconomica.size()-1;
	}

	@Override
	public ContoBulk getVoce_ep() {
		return this.getDspConto();
	}

	public void setVoce_ep(ContoBulk voce_ep) {
		setDspConto(voce_ep);
	}

	@Override
	public java.lang.Integer getEsercizio_voce_ep() {
		return Optional.ofNullable(this.getVoce_ep())
				.map(ContoBulk::getEsercizio)
				.orElse(null);
	}

	@Override
	public void setEsercizio_voce_ep(java.lang.Integer esercizio_voce_ep) {
		Optional.ofNullable(this.getVoce_ep()).ifPresent(el->el.setEsercizio(esercizio_voce_ep));
	}

	@Override
	public java.lang.String getCd_voce_ep() {
		return Optional.ofNullable(this.getVoce_ep())
				.map(ContoBulk::getCd_voce_ep)
				.orElse(null);
	}

	@Override
	public void setCd_voce_ep(java.lang.String cd_voce_ep) {
		Optional.ofNullable(this.getVoce_ep()).ifPresent(el->el.setCd_voce_ep(cd_voce_ep));
	}

	@Override
	public List<IDocumentoDetailAnaCogeBulk> getChildrenAna() {
		return this.getRigheEconomica().stream()
				.filter(Objects::nonNull)
				.map(IDocumentoDetailAnaCogeBulk.class::cast)
				.collect(Collectors.toList());
	}

	@Override
	public void clearChildrenAna() {
		this.setRigheEconomica(new BulkList<>());
	}

	/*
	 * Ritorna l'importo della riga da imputare ad un conto di costo.
	 * Il valore viene utilizzato come quota da ripartire a livello analitico.
	 */
	@Override
	public BigDecimal getImCostoEco() {
		return this.getImTotaleRiga();
	}

	@Override
	public BigDecimal getImCostoEcoRipartito() {
		return this.getChildrenAna().stream()
				.map(el->Optional.ofNullable(el.getImporto()).orElse(BigDecimal.ZERO))
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	@Override
	public BigDecimal getImCostoEcoDaRipartire() {
		return Optional.ofNullable(this.getImCostoEco()).orElse(BigDecimal.ZERO)
				.subtract(Optional.ofNullable(this.getImCostoEcoRipartito()).orElse(BigDecimal.ZERO));
	}

	public List<OrdineAcqRigaEcoBulk> getResultRigheEconomica() {
		List<OrdineAcqRigaEcoBulk> result = new ArrayList<>();
		this.getRigheConsegnaColl().stream()
				.flatMap(el->el.getRigheEconomica().stream())
				.forEach(el->{
					OrdineAcqRigaEcoBulk myRigaEco = result.stream()
							.filter(el2->el2.getCd_linea_attivita().equals(el.getCd_linea_attivita()))
							.filter(el2->el2.getCd_centro_responsabilita().equals(el.getCd_centro_responsabilita()))
							.filter(el2->el2.getCd_voce_ana().equals(el.getCd_voce_ana()))
							.findAny().orElseGet(()->{
								OrdineAcqRigaEcoBulk rigaEco = new OrdineAcqRigaEcoBulk();
								rigaEco.setOrdineAcqRiga(this);
								rigaEco.setLinea_attivita(el.getLinea_attivita());
								rigaEco.setVoce_analitica(el.getVoce_analitica());
								rigaEco.setProgressivo_riga_eco((long) result.size()+1);
								rigaEco.setImporto(BigDecimal.ZERO);
								result.add(rigaEco);
								return rigaEco;
							});
					myRigaEco.setImporto(myRigaEco.getImporto().add(el.getImporto()));
				});
		return result;
	}

	public java.math.BigDecimal getImImponibileConsegne() {
		return Optional.ofNullable(this.getRigheConsegnaColl())
				.map(Collection::stream)
				.orElse(Stream.empty())
				.map(OrdineAcqConsegnaBulk::getImImponibile)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public java.math.BigDecimal getImIvaConsegne() {
		return Optional.ofNullable(this.getRigheConsegnaColl())
				.map(Collection::stream)
				.orElse(Stream.empty())
				.map(OrdineAcqConsegnaBulk::getImIva)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public java.math.BigDecimal getImIvaDConsegne() {
		return Optional.ofNullable(this.getRigheConsegnaColl())
				.map(Collection::stream)
				.orElse(Stream.empty())
				.map(OrdineAcqConsegnaBulk::getImIvaD)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public java.math.BigDecimal getImTotaleConsegne() {
		return Optional.ofNullable(this.getRigheConsegnaColl())
				.map(Collection::stream)
				.orElse(Stream.empty())
				.map(OrdineAcqConsegnaBulk::getImTotaleConsegna)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public boolean isImportiConsegneModificati() {
		return this.getImImponibileConsegne().compareTo(this.getIm_imponibile())!=0 ||
				this.getImIvaConsegne().compareTo(this.getIm_iva())!=0 ||
				this.getImIvaDConsegne().compareTo(this.getImIvaD())!=0 ||
				this.getImTotaleConsegne().compareTo(this.getImTotaleRiga())!=0;
	}

	public BigDecimal getImCostoEcoConsegne() {
		return Optional.ofNullable(this.getRigheConsegnaColl())
				.map(Collection::stream)
				.orElseGet(Stream::empty)
				.map(OrdineAcqConsegnaBulk::getImCostoEco)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public BigDecimal getImCostoEcoRipartitoConsegne() {
		return Optional.ofNullable(this.getRigheConsegnaColl())
				.map(Collection::stream)
				.orElseGet(Stream::empty)
				.map(OrdineAcqConsegnaBulk::getImCostoEcoRipartito)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public BigDecimal getImCostoEcoDaRipartireConsegne() {
		return Optional.ofNullable(this.getRigheConsegnaColl())
				.map(Collection::stream)
				.orElseGet(Stream::empty)
				.map(OrdineAcqConsegnaBulk::getImCostoEcoDaRipartire)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

}
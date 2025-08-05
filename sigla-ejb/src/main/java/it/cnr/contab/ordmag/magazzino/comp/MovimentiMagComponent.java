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

package it.cnr.contab.ordmag.magazzino.comp;

import it.cnr.contab.config00.sto.bulk.EnteBulk;
import it.cnr.contab.docamm00.tabrif.bulk.*;
import it.cnr.contab.inventario00.docs.bulk.Transito_beni_ordiniBulk;
import it.cnr.contab.ordmag.anag00.*;
import it.cnr.contab.ordmag.magazzino.bulk.*;
import it.cnr.contab.ordmag.magazzino.dto.ValoriChiusuraMagRim;
import it.cnr.contab.ordmag.ordini.bulk.*;

import it.cnr.contab.ordmag.ordini.dto.ImportoOrdine;

import it.cnr.contab.ordmag.ordini.dto.ParametriCalcoloImportoOrdine;
import it.cnr.contab.ordmag.ordini.ejb.OrdineAcqComponentSession;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.util.Utility;
import it.cnr.jada.DetailedRuntimeException;
import it.cnr.jada.UserContext;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.bulk.*;
import it.cnr.jada.comp.*;
import it.cnr.jada.persistency.IntrospectionException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.sql.*;
import it.cnr.jada.util.DateUtils;
import it.cnr.jada.util.RemoteIterator;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class MovimentiMagComponent extends CalcolaImportiMagComponent implements ICRUDMgr, IPrintMgr,Cloneable, Serializable {
	private static final long serialVersionUID = 1L;

	public final static String TIPO_TOTALE_COMPLETO = "C";
    public final static String TIPO_TOTALE_PARZIALE = "P";

    private final static String statmentCreazioneMovimentoChiusuraMagazzino ="( " +
			" PG_MOVIMENTO, "+
			" DT_MOVIMENTO, "+
			" CD_CDS_TIPO_MOVIMENTO, "+
			" CD_TIPO_MOVIMENTO, "+
			" DATA_BOLLA, "+
			" NUMERO_BOLLA, "+
			" DT_RIFERIMENTO, "+
			" CD_TERZO, "+
			" CD_UNITA_MISURA, "+
			" QUANTITA, "+
			" COEFF_CONV, "+
			" CD_UOP, "+
			" DT_SCADENZA, "+
			" LOTTO_FORNITORE, "+
			" CD_CDS_LOTTO, "+
			" CD_MAGAZZINO_LOTTO, "+
			" ESERCIZIO_LOTTO, "+
			" CD_NUMERATORE_LOTTO, "+
			" PG_LOTTO, "+
			" SCONTO1, "+
			" SCONTO2, "+
			" SCONTO3, "+
			" CD_CDS_BOLLA_SCA, "+
			" CD_MAGAZZINO_BOLLA_SCA, "+
			" ESERCIZIO_BOLLA_SCA, "+
			" CD_NUMERATORE_BOLLA_SCA, "+
			" PG_BOLLA_SCA, "+
			" IMPORTO, "+
			" IMPORTO_CEFF, "+
			" IMPORTO_CMP, "+
			" IMPORTO_FIFO, "+
			" IMPORTO_LIFO, "+
			" IMPORTO_CMPP, "+
			" IMPORTO_CMPIST, "+
			" CD_DIVISA, "+
			" CAMBIO, "+
			" PREZZO_UNITARIO, "+
			" STATO, "+
			" DT_CANCELLAZIONE, "+
			" DACR, "+
			" UTCR, "+
			" DUVA, "+
			" UTUV, "+
			" PG_VER_REC, "+
			" PG_MOVIMENTO_RIF, "+
			" PG_MOVIMENTO_ANN, "+
			" CD_VOCE_IVA, "+
			" IM_IVA ) "+
			" SELECT "+
			" CNRSEQ00_MOVIMENTI_MAG.NEXTVAL, "+
			" SYSDATE, "+
			" c.CD_CDS_LOTTO, "+
			" ?, "+
			" null, "+
			" null, "+
			" ?, "+
			" l.CD_TERZO, " +
			" c.UNITA_MISURA, " +
			" c.GIACENZA, " +
			" 1, "+
			" m.CD_UNITA_OPERATIVA," +
			" null, "+
			" null, "+
			" c.CD_CDS_LOTTO, "+
			" c.CD_MAGAZZINO_LOTTO,  " +
			" c.ESERCIZIO_LOTTO, " +
			" c.CD_NUMERATORE_LOTTO, " +
			" c.PG_LOTTO, " +
			" null, "+
			" null, "+
			" null, "+
			" null, "+
			" null, "+
			" null, "+
			" null, "+
			" null, "+
			" null, "+
			" null, "+
			" null, "+
			" null, "+
			" null, "+
			" null, "+
			" null, "+
			" l.CD_DIVISA, " +
			" l.CAMBIO, " +
			" c.IMPORTO_CMPP_ART, " +
			" ?, "+
			" null, "+
			" SYSDATE, "+
			" ?, "+
			" SYSDATE, "+
			" ?, "+
			" 1, "+
			" null, "+
			" null, "+
			" l.CD_VOCE_IVA, " +
			" null " +
			" FROM  " +
			" DUAL CNRSEQ00_MOVIMENTI_MAG, "+
			" CHIUSURA_ANNO_MAG_RIM c " +
			"        inner join MAGAZZINO m on c.CD_MAGAZZINO=m.CD_MAGAZZINO and c.CD_CDS_MAG=m.CD_CDS " +
			"        inner join LOTTO_MAG l on c.CD_CDS_LOTTO = l.cd_cds and c.CD_MAGAZZINO_LOTTO=l.cd_magazzino " +
			"								and c.ESERCIZIO_LOTTO=l.esercizio and c.CD_NUMERATORE_LOTTO=l.cd_numeratore_mag " +
			"                               and c.PG_LOTTO=l.pg_lotto " +
			"  WHERE  " +
			"  ( c.PG_CHIUSURA = ? ) AND  " +
			"  ( c.ANNO = ? ) AND " +
			"  ( c.TIPO_CHIUSURA = ? ) ";

    private final static String statmentUpdateLottoPerMovimentoChiusura= "" +
			" MERGE into LOTTO_MAG c USING MOVIMENTI_MAG m "+
				" ON (m.ESERCIZIO_LOTTO=c.ESERCIZIO and m.PG_LOTTO=c.PG_LOTTO and m.CD_MAGAZZINO_LOTTO = c.CD_MAGAZZINO and "+
				"	  m.CD_CDS_LOTTO = c.CD_CDS and m.CD_NUMERATORE_LOTTO=c.CD_NUMERATORE_MAG) "+
			    " WHEN MATCHED THEN UPDATE SET c.QUANTITA_INIZIO_ANNO = m.QUANTITA " +
			    " WHERE m.CD_TIPO_MOVIMENTO = 'CHI' and  m.DT_RIFERIMENTO= ? " ;

	private final static String statmentUpdateLottoPerAnnullaMovimentoChiusura= "" +
			" MERGE into LOTTO_MAG c USING MOVIMENTI_MAG m "+
			" ON (m.ESERCIZIO_LOTTO=c.ESERCIZIO and m.PG_LOTTO=c.PG_LOTTO and m.CD_MAGAZZINO_LOTTO = c.CD_MAGAZZINO and "+
			"	  m.CD_CDS_LOTTO = c.CD_CDS and m.CD_NUMERATORE_LOTTO=c.CD_NUMERATORE_MAG) "+
			" WHEN MATCHED THEN UPDATE SET c.QUANTITA_INIZIO_ANNO = 0 " +
			" WHERE m.CD_TIPO_MOVIMENTO = 'CHI' and  m.DT_RIFERIMENTO= ? " ;


    private MovimentiMagBulk createMovimentoMagazzino(UserContext userContext,
													  UnitaOperativaOrdBulk unitaOperativa, MagazzinoBulk magazzino, TipoMovimentoMagBulk tipoMovimento,
													  Bene_servizioBulk beneServizio, BigDecimal quantitaBeneServizio, Timestamp dataRiferimento,
													  DivisaBulk divisa, BigDecimal cambio, UnitaMisuraBulk unitaMisura, BigDecimal coeffConv,
													  Voce_ivaBulk voceIva) throws PersistencyException,ComponentException {
		MovimentiMagHome homeMag = (MovimentiMagHome)getHome(userContext, MovimentiMagBulk.class);

		MovimentiMagBulk movimentoMag = new MovimentiMagBulk();
		movimentoMag.setBeneServizioUt(beneServizio);
		movimentoMag.setQuantita(quantitaBeneServizio);
		movimentoMag.setDtMovimento(new Timestamp(System.currentTimeMillis()));
		movimentoMag.setDtRiferimento(dataRiferimento);
		movimentoMag.setMagazzinoUt(magazzino);
		movimentoMag.setUnitaOperativaOrd(unitaOperativa);
		TipoMovimentoMagHome home = (TipoMovimentoMagHome) getHome(userContext, TipoMovimentoMagBulk.class);
		tipoMovimento = (TipoMovimentoMagBulk)home.findByPrimaryKey(tipoMovimento);
		movimentoMag.setTipoMovimentoMag(tipoMovimento);
		movimentoMag.setPgMovimento(homeMag.recuperoProgressivoMovimento(userContext));
		movimentoMag.setDivisa(divisa);
		movimentoMag.setCambio(cambio);
		movimentoMag.setUnitaMisura(unitaMisura);
		movimentoMag.setCoeffConv(coeffConv);
		movimentoMag.setVoceIva(voceIva);
		movimentoMag.setStato(MovimentiMagBulk.STATO_INSERITO);
		
		movimentoMag.setToBeCreated();
    	return movimentoMag;
    }

    private MovimentiMagBulk createMovimentoMagazzino(UserContext userContext, CaricoMagazzinoRigaBulk movimento, Bene_servizioBulk bene, DivisaBulk divisaDefault,Voce_ivaBulk voceIva) throws PersistencyException,ComponentException {
    	MagazzinoBulk magazzino = (MagazzinoBulk)findByPrimaryKey(userContext, movimento.getMovimentiMagazzinoBulk().getMagazzinoAbilitato());
		MovimentiMagBulk movimentoMag = createMovimentoMagazzino(userContext, 
							null, 
							magazzino,
							movimento.getMovimentiMagazzinoBulk().getTipoMovimentoMag(),
							bene,
							movimento.getQuantita(),
							movimento.getMovimentiMagazzinoBulk().getDataCompetenza(),
							divisaDefault,
							BigDecimal.ONE,
							movimento.getUnitaMisura(),
							movimento.getCoefConv(),
							voceIva);

		movimentoMag.setDataBolla(movimento.getDataBolla());
		movimentoMag.setNumeroBolla(movimento.getNumeroBolla());

		movimentoMag.setPrezzoUnitario(movimento.getPrezzoUnitario());
		movimentoMag.setImIva(movimento.getImpIva());
		
		movimentoMag.setLottoFornitore(movimento.getLottoFornitore());
		movimentoMag.setDtScadenza(movimento.getDtScadenza());
		movimentoMag.setTerzo(movimento.getTerzo());

    	return movimentoMag;
    }

    private MovimentiMagBulk createMovimentoMagazzino(UserContext userContext, OrdineAcqConsegnaBulk consegna, 
    		EvasioneOrdineRigaBulk evasioneOrdineRiga, Bene_servizioBulk bene) throws PersistencyException,ComponentException {
    	MagazzinoBulk magazzino = (MagazzinoBulk)findByPrimaryKey(userContext, evasioneOrdineRiga.getEvasioneOrdine().getNumerazioneMag().getMagazzino());
		MovimentiMagBulk movimentoMag = createMovimentoMagazzino(userContext, 
							consegna.getUnitaOperativaOrd(), 
							magazzino,
							magazzino.getTipoMovimentoMag(consegna.getTipoConsegna()),
							consegna.getOrdineAcqRiga().getBeneServizio(),
							evasioneOrdineRiga.getQuantitaEvasaBolla(),
							evasioneOrdineRiga.getEvasioneOrdine().getDataConsegna(),
							consegna.getOrdineAcqRiga().getOrdineAcq().getDivisa(),
							consegna.getOrdineAcqRiga().getOrdineAcq().getCambio(),
							Optional.ofNullable(evasioneOrdineRiga.getUnitaMisuraEvasaBolla()).orElse(consegna.getOrdineAcqRiga().getUnitaMisura()),
							Optional.ofNullable(evasioneOrdineRiga.getCoefConvEvasaBolla()).orElse(consegna.getOrdineAcqRiga().getCoefConv()),
							consegna.getOrdineAcqRiga().getVoceIva());

		movimentoMag.setDataBolla(evasioneOrdineRiga.getEvasioneOrdine().getDataBolla());
		movimentoMag.setNumeroBolla(evasioneOrdineRiga.getEvasioneOrdine().getNumeroBolla());
		movimentoMag.setOrdineAcqConsegnaUt(consegna);

		try {
			ImportoOrdine importo =  recuperoPrezzoUnitario(userContext, consegna);
			BigDecimal prezzoUnitario = importo.getPrezzoCompIva();
			BigDecimal prezzoIvaUnitario = importo.getTotaleIva();

			//Rapporto il prezzo unitario al Coefficiente di Conversione usato nell'evasione se diverso da quello indicato sulla riga
			if (!movimentoMag.getCoeffConv().equals(consegna.getOrdineAcqRiga().getCoefConv())) {
				prezzoUnitario = prezzoUnitario.multiply(movimentoMag.getCoeffConv()).divide(consegna.getOrdineAcqRiga().getCoefConv(), 6, RoundingMode.HALF_UP);
				prezzoIvaUnitario = prezzoIvaUnitario.multiply(movimentoMag.getCoeffConv()).divide(consegna.getOrdineAcqRiga().getCoefConv(), 6, RoundingMode.HALF_UP);;
			}
			// TODO IMPOSTARE IMPORTO IVA - V.T.

			movimentoMag.setPrezzoUnitario(prezzoUnitario);
			movimentoMag.setImIva(prezzoIvaUnitario);
		} catch (RemoteException e) {
			throw new ComponentException(e);
		}
		
		movimentoMag.setSconto1(consegna.getOrdineAcqRiga().getSconto1());
		movimentoMag.setSconto2(consegna.getOrdineAcqRiga().getSconto2());
		movimentoMag.setSconto3(consegna.getOrdineAcqRiga().getSconto3());
		movimentoMag.setLottoFornitore(consegna.getLottoFornitore());
		movimentoMag.setDtScadenza(consegna.getDtScadenza());
		movimentoMag.setTerzo(consegna.getOrdineAcqRiga().getOrdineAcq().getFornitore());

    	return movimentoMag;
    }

    private LottoMagBulk createLottoMagazzino(UserContext userContext, MovimentiMagBulk movimentoCaricoMag) throws ComponentException, PersistencyException {
		LottoMagBulk lotto = new LottoMagBulk();
		lotto.setBeneServizio(movimentoCaricoMag.getBeneServizioUt());
		lotto.setDivisa(movimentoCaricoMag.getDivisa());
		lotto.setCambio(movimentoCaricoMag.getCambio());
		lotto.setDtScadenza(movimentoCaricoMag.getDtScadenza()!=null?movimentoCaricoMag.getDtScadenza():lotto.getDtScadenza());
		lotto.setEsercizio(CNRUserContext.getEsercizio(userContext));
		lotto.setMagazzino(movimentoCaricoMag.getMagazzinoUt());
		lotto.setOrdineAcqConsegna(movimentoCaricoMag.getOrdineAcqConsegnaUt()!=null?movimentoCaricoMag.getOrdineAcqConsegnaUt():lotto.getOrdineAcqConsegna());
		lotto.setDivisa(Optional.ofNullable(movimentoCaricoMag.getDivisa()).orElse(lotto.getDivisa()));
		lotto.setTerzo(Optional.ofNullable(movimentoCaricoMag.getTerzo()).orElse(lotto.getTerzo()));
		lotto.setStato(LottoMagBulk.STATO_INSERITO);
		lotto.setVoceIva(movimentoCaricoMag.getVoceIva());

		lotto.setToBeCreated();
    	return lotto;
    }



    /**
     * Effetta operazione di carico e/o scarico magazzino a fronte di ordine
     * Ritorna la riga di scarico se create, oggetto vuoto in caso di solo carico
     * 
     * @param userContext
     * @param consegna
     * @param evasioneOrdineRiga
     * @return riga di scarico se creata (MovimentiMagBulk)
     * @throws ComponentException
     * @throws PersistencyException
     * @throws ApplicationException
     */
    public MovimentiMagBulk caricoDaOrdine(UserContext userContext, OrdineAcqConsegnaBulk consegna, EvasioneOrdineRigaBulk evasioneOrdineRiga) throws ComponentException, PersistencyException, ApplicationException {
    	MovimentiMagBulk movimentoScaricoMag = null;

		Bene_servizioHome beneHome = (Bene_servizioHome)getHome(userContext, Bene_servizioBulk.class);
    	Bene_servizioBulk bene = (Bene_servizioBulk)beneHome.findByPrimaryKey(consegna.getOrdineAcqRiga().getBeneServizio());

    	if (bene.getFlScadenza() != null && bene.getFlScadenza() && consegna.getDtScadenza() == null)
			throw new ApplicationException("Indicare la data di scadenza per la consegna "+consegna.getConsegnaOrdineString());
		
		//creo il movimento di carico di magazzino
		MovimentiMagBulk movimentoCaricoMag = createMovimentoMagazzino(userContext, consegna, evasioneOrdineRiga, bene);

		//creo il movimento di scarico di magazzino
		if (!consegna.isConsegnaMagazzino()){
			//creo il movimento di scarico
//			TipoMovimentoMagAzBulk tipoMovimentoAz = new TipoMovimentoMagAzBulk(movimentoCaricoMag.getTipoMovimentoMag().getCdCds(), movimentoCaricoMag.getTipoMovimentoMag().getCdTipoMovimento());
//			TipoMovimentoMagAzHome home = (TipoMovimentoMagAzHome)getHome(userContext,TipoMovimentoMagAzBulk.class);
//			tipoMovimentoAz = (TipoMovimentoMagAzBulk)home.findByPrimaryKey(tipoMovimentoAz);
			
			Optional.ofNullable(movimentoCaricoMag.getTipoMovimentoMag())
				.map(TipoMovimentoMagBulk::getTipoMovimentoMagRif)
				.orElseThrow(()->new ApplicationException("Attenzione! Errore nell'individuazione del tipo movimento di riferimento per l'operazione di scarico."));
				
			movimentoScaricoMag = createMovimentoMagazzino(userContext, consegna, evasioneOrdineRiga, bene);
			movimentoScaricoMag.setTipoMovimentoMag(movimentoCaricoMag.getTipoMovimentoMag().getTipoMovimentoMagRif());
		}

    	movimentoCaricoMag = creaCarico(userContext, movimentoCaricoMag);
		try {
			Utility.createTransitoBeniOrdiniComponentSession().gestioneTransitoInventario(userContext, movimentoCaricoMag);
		} catch (RemoteException e) {
			throw new ComponentException(e);
		}
		if (movimentoScaricoMag != null) {
			//associo il lotto di magazzino al movimento di scarico
			movimentoScaricoMag.setLottoMag(movimentoCaricoMag.getLottoMag());
			//creo il legame del movimento di scarico con il movimento di carico
			movimentoScaricoMag.setMovimentoRif(movimentoCaricoMag);
			movimentoScaricoMag.setToBeCreated();
			movimentoScaricoMag = (MovimentiMagBulk)super.creaConBulk(userContext, movimentoScaricoMag);

			//creo il legame del movimento di carico con il movimento di scarico
			movimentoCaricoMag.setMovimentoRif(movimentoScaricoMag);
			movimentoCaricoMag.setToBeUpdated();
			modificaConBulk(userContext, movimentoCaricoMag);
			
			//codice di controllo: il lotto creato deve avere le movimentazioni a zero essendo carico/scarico contestuale
	    	LottoMagHome lottoHome = (LottoMagHome)getHome(userContext,LottoMagBulk.class);
			LottoMagBulk lotto = (LottoMagBulk)lottoHome.findByPrimaryKey(movimentoCaricoMag.getLottoMag());
			if (lotto.getGiacenza().compareTo(BigDecimal.ZERO)!=0 || lotto.getQuantitaValore().compareTo(BigDecimal.ZERO)!=0) {
				lotto.setGiacenza(BigDecimal.ZERO);
				lotto.setQuantitaValore(BigDecimal.ZERO);
				lotto.setToBeUpdated();
				lottoHome.update(lotto, userContext);
			}
		}
		
		return movimentoCaricoMag;
	}

	public MovimentiMagBulk creaMovimentoRettificaValoreOrdine(UserContext userContext, FatturaOrdineBulk fatturaOrdineBulk) throws ComponentException {
		LottoMagBulk lotto = null;
		try {
			lotto = ((LottoMagHome)getHome(userContext, LottoMagBulk.class)).findCaricoDaOrdine(fatturaOrdineBulk.getOrdineAcqConsegna());
		} catch (IntrospectionException | PersistencyException e) {
			throw handleException(e);
		}

		MovimentiMagBulk movimentoMag = new MovimentiMagBulk();
		movimentoMag.setLottoMag(lotto);

		PersistentHome evHome = getHome(userContext, MagazzinoBulk.class);
		MagazzinoBulk magazzinoBulk = new MagazzinoBulk(lotto.getCdCdsMag(), lotto.getCdMagazzino());
		try {
			magazzinoBulk = (MagazzinoBulk)evHome.findByPrimaryKey(magazzinoBulk);
		} catch (PersistencyException e) {
			throw handleException(e);
		}

		Bene_servizioHome homeBene = (Bene_servizioHome)getHome(userContext, Bene_servizioBulk.class);
		Bene_servizioBulk bene = null;
		try {
			bene = (Bene_servizioBulk)homeBene.findByPrimaryKey(lotto.getBeneServizio());
		} catch (PersistencyException e) {
			throw handleException(e);
		}
		MovimentiMagHome homeMag = (MovimentiMagHome)getHome(userContext, MovimentiMagBulk.class);

		movimentoMag.setBeneServizioUt(lotto.getBeneServizio());
		movimentoMag.setQuantita(BigDecimal.ONE);
		movimentoMag.setDtMovimento(new Timestamp(System.currentTimeMillis()));
		movimentoMag.setDtRiferimento(lotto.getDtCarico());
		movimentoMag.setMagazzinoUt(magazzinoBulk);
		movimentoMag.setUnitaOperativaOrd(fatturaOrdineBulk.getOrdineAcqConsegna().getUnitaOperativaOrd());

		try {
			movimentoMag.setPgMovimento(homeMag.recuperoProgressivoMovimento(userContext));
		} catch (PersistencyException e) {
			throw handleException(e);
		}
		movimentoMag.setDivisa(lotto.getDivisa());
		movimentoMag.setCambio(lotto.getCambio());
		movimentoMag.setUnitaMisura(bene.getUnitaMisura());
		movimentoMag.setCoeffConv(BigDecimal.ONE);
		movimentoMag.setVoceIva(lotto.getVoceIva());
		movimentoMag.setStato(MovimentiMagBulk.STATO_INSERITO);

		movimentoMag.setToBeCreated();

		OrdineAcqConsegnaHome homeConsegna = (OrdineAcqConsegnaHome)getHome(userContext, OrdineAcqConsegnaBulk.class);
		OrdineAcqConsegnaBulk consegna = null;
		try {
			consegna = (OrdineAcqConsegnaBulk)homeConsegna.findByPrimaryKey(fatturaOrdineBulk.getOrdineAcqConsegna());
		} catch (PersistencyException e) {
			throw handleException(e);
		}

		movimentoMag.setOrdineAcqConsegnaUt(consegna);

		try {
			ImportoOrdine importo = recuperoPrezzoUnitario(userContext, consegna);
			BigDecimal prezzoUnitarioOrdine = importo.getPrezzoCompIva();
			BigDecimal prezzoIvaUnitarioOrdine = importo.getTotaleIva();

			ImportoOrdine importoRet=  recuperoPrezzoUnitarioRettificato(userContext, fatturaOrdineBulk);
			BigDecimal prezzoUnitarioOrdineRettificato =importoRet.getPrezzoCompIva();
			BigDecimal prezzoUnitarioIvaOrdineRettificato =importoRet.getTotaleIva();

			BigDecimal diffOrdineRettificato = prezzoUnitarioOrdineRettificato.subtract(prezzoUnitarioOrdine);
			BigDecimal diffIvaOrdineRettificato = prezzoUnitarioIvaOrdineRettificato.subtract(prezzoIvaUnitarioOrdine);

			movimentoMag.setTipoMovimentoMag(diffOrdineRettificato.compareTo(BigDecimal.ZERO) > 0 ? magazzinoBulk.getTipoMovimentoMagRvPos() : magazzinoBulk.getTipoMovimentoMagRvNeg());
			// TODO IMPOSTARE IMPORTO IVA - V.T.
			movimentoMag.setPrezzoUnitario(diffOrdineRettificato.abs());
			movimentoMag.setPrezzoUnitario(diffIvaOrdineRettificato.abs());
		} catch (RemoteException | PersistencyException e) {
			throw new ComponentException(e);
		}

		movimentoMag.setTerzo(lotto.getTerzo());
		movimentoMag.setToBeCreated();
		return (MovimentiMagBulk) creaConBulk(userContext, movimentoMag);
	}

	public void creaMovimentoChiusura(UserContext userContext, Integer pgChiusura, Integer anno, String tipoChiusura, java.sql.Timestamp dataRiferimentoMovimento) throws ComponentException, SQLException {

    	try{
			Connection c =  getConnection( userContext);

			MovimentiMagHome homeMag = (MovimentiMagHome)getHome(userContext, MovimentiMagBulk.class);

			LoggableStatement psInsert =new LoggableStatement(c,
					"INSERT  INTO " + it.cnr.jada.util.ejb.EJBCommonServices.getDefaultSchema() + "MOVIMENTI_MAG " +
					statmentCreazioneMovimentoChiusuraMagazzino	 ,
					true,this.getClass());
			try
			{
				//ps.setLong( 1, homeMag.recuperoProgressivoMovimento(userContext));
				psInsert.setString( 1, TipoMovimentoMagBulk.TIPO_MOVIMENTO_CHIUSURA);
				psInsert.setTimestamp( 2, dataRiferimentoMovimento);
				psInsert.setString( 3,MovimentiMagBulk.STATO_INSERITO);
				psInsert.setString( 4, userContext.getUser());
				psInsert.setString( 5, userContext.getUser());
				psInsert.setInt(6, pgChiusura);
				psInsert.setInt(7, anno);
				psInsert.setString(8, tipoChiusura);
				psInsert.executeUpdate();

				LoggableStatement psUpdate = new LoggableStatement(c,
											statmentUpdateLottoPerMovimentoChiusura,
											true, this.getClass());
				try {

					psUpdate.setTimestamp(1, dataRiferimentoMovimento);
					psUpdate.executeQuery();

				} catch (SQLException e) {
					throw new PersistencyException(e);
				}
				finally
				{
					try {
							if (psInsert != null)
								psInsert.close();
							if (psUpdate != null)
								psUpdate.close();
						}
						catch (java.sql.SQLException e) {
						}
				}
			}
			finally
			{
				try{
					if (psInsert != null)
						psInsert.close();

				}catch( java.sql.SQLException e ){};
			}

		}
		catch (Exception e )
		{
			throw handleException( e );
		}

	}

	public void eliminaMovimentoChiusura(UserContext userContext, Integer pgChiusura, Integer anno, String tipoChiusura, Timestamp dataRiferimentoMovimento) throws RemoteException, ComponentException {
		try {
			Connection c =  getConnection( userContext);

			// annullo la quantita di inizio anno impostata dai movimenti di chiusura
			LoggableStatement psUpdate = new LoggableStatement(c,
					statmentUpdateLottoPerAnnullaMovimentoChiusura,
					true, this.getClass());
			try {

				psUpdate.setTimestamp(1, dataRiferimentoMovimento);
				psUpdate.executeQuery();

				// aggiorno la quantita inziio anno ai lotti con un precedente movimenti di chiusura
				LoggableStatement psUpdate2 = new LoggableStatement(c,
						statmentUpdateLottoPerMovimentoChiusura,
						true, this.getClass());
				try {

					java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
					java.sql.Timestamp dataRifMovChi = (new java.sql.Timestamp(sdf.parse("01/01/"+anno).getTime()));


					psUpdate2.setTimestamp(1, dataRifMovChi);
					psUpdate2.executeQuery();

					LoggableStatement psDel = new LoggableStatement(
							c, "DELETE FROM "
							+ it.cnr.jada.util.ejb.EJBCommonServices
							.getDefaultSchema() + "MOVIMENTI_MAG "
							+ "WHERE CD_TIPO_MOVIMENTO = 'CHI'  AND DT_RIFERIMENTO = ? ", true, this.getClass());

					try {
						psDel.setTimestamp(1, dataRiferimentoMovimento);
						psDel.executeUpdate();
					} catch (SQLException e) {
						throw new PersistencyException(e);
					}finally
					{
						try {
							if (psDel != null)
								psDel.close();
							if (psUpdate2 != null)
								psUpdate2.close();
							if (psUpdate != null)
								psUpdate.close();
						}
						catch (java.sql.SQLException e) {
						}
					}

				} catch (SQLException e) {
					throw new PersistencyException(e);
				} catch (ParseException e) {
					e.printStackTrace();
				} finally
				{
					try {

						if (psUpdate2 != null)
							psUpdate2.close();
						if (psUpdate != null)
							psUpdate.close();
					}
					catch (java.sql.SQLException e) {
					}
				}
			}
			catch (java.sql.SQLException e) {

			} finally {
				try {

					if (psUpdate != null)
						psUpdate.close();
				} catch (java.sql.SQLException e) {
				}
			}

		} catch (SQLException | PersistencyException e) {
			throw handleException(e);
		}
	}
	private MovimentiMagBulk creaCarico(UserContext userContext, MovimentiMagBulk movimentoCaricoMag)
			throws ComponentException, PersistencyException {
		//creo il lotto di magazzino a partire dal movimento di carico
		LottoMagBulk lotto = (LottoMagBulk)super.creaConBulk(userContext, createLottoMagazzino(userContext, movimentoCaricoMag));

		//associo il lotto di magazzino al movimento di carico
		movimentoCaricoMag.setLottoMag(lotto);
		creaConBulk(userContext, movimentoCaricoMag);
		return movimentoCaricoMag;
	}
	private void impostaImportiCorrettiPerCarico(UserContext userContext,CaricoMagazzinoBulk carico) throws ComponentException, RemoteException, PersistencyException {


		for(CaricoMagazzinoRigaBulk riga : carico.getCaricoMagazzinoRigaColl()){
			ParametriCalcoloImportoOrdine parametri = new ParametriCalcoloImportoOrdine();
			parametri.setCoefacq(riga.getCoefConv());
			parametri.setPrezzo(riga.getPrezzoUnitario());
			parametri.setVoceIva(riga.getVoceIva());
			parametri.setQtaOrd(riga.getQuantita());

			DivisaBulk divisaDefault = ((DivisaHome)getHome(userContext, DivisaBulk.class)).getDivisaDefault(userContext);
			parametri.setDivisa(divisaDefault);
			parametri.setDivisaRisultato(divisaDefault);

			ImportoOrdine importi = calcoloImportoPerMagazzino(parametri);

			riga.setPrezzoUnitario(importi.getPrezzoCompIva());
			riga.setImpIva(importi.getTotaleIva());

		}
	}

    public CaricoMagazzinoBulk caricaMagazzino(UserContext userContext, CaricoMagazzinoBulk caricoMagazzino) throws ComponentException, PersistencyException, RemoteException {
		DivisaBulk divisaDefault = ((DivisaHome)getHome(userContext, DivisaBulk.class)).getDivisaDefault(userContext);


		// impostare qui i paramentri per il calcolo importI
		impostaImportiCorrettiPerCarico(userContext,caricoMagazzino);

		controlloDatiObbligatoriMovimentiMagazzino(userContext, caricoMagazzino, divisaDefault);
		if (caricoMagazzino.getCaricoMagazzinoRigaColl()==null||caricoMagazzino.getCaricoMagazzinoRigaColl().isEmpty())
			throw new ApplicationException("Errore nel movimento di magazzino! Manca l'indicazione dei beni/servizi da movimentare.");

		LottoMagHome lottoHome = (LottoMagHome)getHome(userContext, LottoMagBulk.class);
		
		List<MovimentiMagBulk> listaMovimenti = new ArrayList<>();
		try {
			caricoMagazzino.getCaricoMagazzinoRigaColl().stream()
			.forEach(caricoMagazzinoRiga->{
				caricoMagazzinoRiga.setAnomalia(null);
				try {
					controlloDatiObbligatoriCaricoManualeRiga(userContext, caricoMagazzinoRiga);
				} catch (ApplicationException e) {
					// TODO Auto-generated catch block
					throw new DetailedRuntimeException(e);
				}

				try {
					
					Bene_servizioHome beneHome = (Bene_servizioHome)getHome(userContext, Bene_servizioBulk.class);
			    	Bene_servizioBulk bene = (Bene_servizioBulk)beneHome.findByPrimaryKey(caricoMagazzinoRiga.getBeneServizio());

					Voce_ivaHome voce_ivaHome = (Voce_ivaHome) getHome(userContext, Voce_ivaBulk.class);
					Voce_ivaBulk voce_iva = null;
					try {
						voce_iva = (Voce_ivaBulk) voce_ivaHome.findByPrimaryKey(caricoMagazzinoRiga.getVoceIva());
					} catch (PersistencyException e) {
						throw new ComponentException(e);
					}

					//creo il movimento di carico di magazzino
					MovimentiMagBulk movimentoCaricoMag = createMovimentoMagazzino(userContext, caricoMagazzinoRiga, bene, divisaDefault,voce_iva);
					listaMovimenti.add(movimentoCaricoMag);
			    	creaCarico(userContext, movimentoCaricoMag);
				} catch (PersistencyException | ComponentException ex ) {
					throw new DetailedRuntimeException(ex);
				}
			});
		} catch (DetailedRuntimeException ex) {
			throw handleException(ex.getDetail());
		}
		
		return caricoMagazzino;
    }

    public void annullaMovimento(UserContext userContext, MovimentiMagBulk movimentoDaAnnullare) throws ComponentException, PersistencyException, RemoteException, ApplicationException {
    	controlliAnnullamento(userContext, movimentoDaAnnullare);
		movimentoDaAnnullare = (MovimentiMagBulk)findByPrimaryKey(userContext, movimentoDaAnnullare);
		if (movimentoDaAnnullare.isStatoAnnullato())
			return;
    	MovimentiMagBulk movimentoDiStorno = preparaMovimentoDiAnnullamento(userContext, movimentoDaAnnullare);
    	if (movimentoDaAnnullare.getPgMovimentoRif() != null){
    		MovimentiMagBulk movimentoRifDaAnnullare = (MovimentiMagBulk)findByPrimaryKey(userContext, new MovimentiMagBulk(movimentoDaAnnullare.getPgMovimentoRif()));
    		MovimentiMagBulk movimentoRifDiStorno = preparaMovimentoDiAnnullamento(userContext, movimentoRifDaAnnullare);
    		if (movimentoDaAnnullare.getTipoMovimentoMag().isMovimentoDiCarico()){
    			movimentoRifDiStorno = effettuaAnnullamento(userContext, movimentoRifDaAnnullare, movimentoRifDiStorno);
    			movimentoDiStorno = effettuaAnnullamento(userContext, movimentoDaAnnullare, movimentoDiStorno);
    		} else {
    			movimentoDiStorno = effettuaAnnullamento(userContext, movimentoDaAnnullare, movimentoDiStorno);
    			movimentoRifDiStorno = effettuaAnnullamento(userContext, movimentoRifDaAnnullare, movimentoRifDiStorno);
    		}
    		movimentoDiStorno.setMovimentoRif(movimentoRifDiStorno);
    		movimentoRifDiStorno.setMovimentoRif(movimentoDiStorno);
    		modificaConBulk(userContext, movimentoDiStorno);
    		modificaConBulk(userContext, movimentoRifDiStorno);
    	} else {
    		effettuaAnnullamento(userContext, movimentoDaAnnullare, movimentoDiStorno);
    	}
    }

	private MovimentiMagBulk effettuaAnnullamento(UserContext userContext, MovimentiMagBulk movimentoDaAnnullare,
			MovimentiMagBulk movimentoDiStorno) throws ComponentException, PersistencyException, ApplicationException, RemoteException {
		movimentoDiStorno = (MovimentiMagBulk)super.creaConBulk(userContext, movimentoDiStorno);
		movimentoDaAnnullare.setStato(MovimentiMagBulk.STATO_ANNULLATO);
		movimentoDaAnnullare.setDtCancellazione(movimentoDiStorno.getDtMovimento());
		movimentoDaAnnullare.setPgMovimentoAnn(movimentoDiStorno.getPgMovimento());
		if (movimentoDaAnnullare.getTipoMovimentoMag().isCaricoDaOrdine()){
			aggiornaOrdineCaricoDaAnnullare(userContext, movimentoDaAnnullare);
		}
		movimentoDaAnnullare.setToBeUpdated();
		modificaConBulk(userContext, movimentoDaAnnullare);
		if (movimentoDaAnnullare.getBollaScaricoMag() != null && movimentoDaAnnullare.getBollaScaricoMag().getPgBollaSca() != null ){
			annullaRigaBollaDiScarico(userContext, movimentoDaAnnullare);
		}
		/**
		 * Cerco la riga di evasione legata al movimento per effettuare l'annullamento
		 */
		final EvasioneOrdineRigaHome evasioneOrdineRigaHome = Optional.ofNullable(getHome(userContext, EvasioneOrdineRigaBulk.class))
				.filter(EvasioneOrdineRigaHome.class::isInstance)
				.map(EvasioneOrdineRigaHome.class::cast)
				.orElseThrow(() -> new ComponentException("EvasioneOrdineRigaHome not found!"));
		final List<EvasioneOrdineRigaBulk> evasioneOrdineRigaBulks =
				evasioneOrdineRigaHome.findByMovimentiMag(movimentoDaAnnullare)
						.stream()
						.map(evasioneOrdineRigaBulk -> {
							evasioneOrdineRigaBulk.setStato(OrdineAcqConsegnaBulk.STATO_ANNULLATA);
							evasioneOrdineRigaBulk.setToBeUpdated();
							return evasioneOrdineRigaBulk;
						}).collect(Collectors.toList());
		if (!evasioneOrdineRigaBulks.isEmpty())
			super.modificaConBulk(userContext, evasioneOrdineRigaBulks.toArray(new EvasioneOrdineRigaBulk[evasioneOrdineRigaBulks.size()]));

		return movimentoDiStorno;
	}
	private void aggiornaOrdineCaricoDaAnnullare(UserContext userContext, MovimentiMagBulk movimentoDaAnnullare)
			throws ComponentException, PersistencyException, ApplicationException, RemoteException {
		OrdineAcqComponentSession ordineComponent = Utility.createOrdineAcqComponentSession();
		OrdineAcqConsegnaBulk consegnaDaRipristinare = movimentoDaAnnullare.getLottoMag().getOrdineAcqConsegna();
		OrdineAcqRigaBulk rigaDaRipristinare = consegnaDaRipristinare.getOrdineAcqRiga();
		OrdineAcqConsegnaHome home = (OrdineAcqConsegnaHome)getHome(userContext, OrdineAcqConsegnaBulk.class);
		OrdineAcqConsegnaBulk consegna = (OrdineAcqConsegnaBulk)home.findByPrimaryKey(userContext, consegnaDaRipristinare);
		OrdineAcqBulk ordine = (OrdineAcqBulk)ordineComponent.inizializzaBulkPerModifica(userContext, consegna.getOrdineAcqRiga().getOrdineAcq());

		OrdineAcqRigaBulk ordineRiga =
				Optional.ofNullable(ordine.getRigheOrdineColl())
						.filter(list -> !list.isEmpty())
						.map(list->list.get(list.indexOfByPrimaryKey(rigaDaRipristinare)))
						.orElseThrow(()->new DetailedRuntimeException("Errore nell'individuazione della riga "+rigaDaRipristinare.getRigaOrdineString()+"."));

		OrdineAcqConsegnaBulk ordineConsegnaComp =
				Optional.ofNullable(ordineRiga.getRigheConsegnaColl())
						.filter(list -> !list.isEmpty())
						.map(list->list.get(list.indexOfByPrimaryKey(consegna)))
						.orElseThrow(()->new DetailedRuntimeException("Errore nell'individuazione della consegna "+consegna.getConsegnaOrdineString()+"."));

		if(consegna.getStatoFatt().equals(OrdineAcqConsegnaBulk.STATO_FATT_ASSOCIATA_TOTALMENTE))
		{
			throw new ApplicationException("Non è possibile annullare il movimento, la consegna è associata totalmente ad una fattura");
		}
		consegna.setStato(OrdineAcqConsegnaBulk.STATO_INSERITA);
		ordine.setToBeUpdated();
		ordineRiga.setToBeUpdated();
		consegna.setToBeUpdated();
		int i = 0;
		//rimuovo la vecchia consegna
		ordineRiga.getRigheConsegnaColl().removeByPrimaryKey(ordineConsegnaComp);
		//inserisco la nuova consegna
		ordineRiga.getRigheConsegnaColl().add(consegna);
		ordineComponent.modificaConBulk(userContext, ordine);
	}

	private void annullaRigaBollaDiScarico(UserContext userContext, MovimentiMagBulk movimentoDaAnnullare)
			throws ComponentException, PersistencyException, ApplicationException {

    	verificaBeneDaAnnullare(userContext, movimentoDaAnnullare);
		MovimentiMagHome movimentiHome = (MovimentiMagHome)getHome(userContext, MovimentiMagBulk.class);
		try {
			List listaRigaBolle = movimentiHome.findRigheBollaDiScarico(movimentoDaAnnullare);
			if (listaRigaBolle != null){
				for (Object obj : listaRigaBolle){
					BollaScaricoRigaMagBulk riga = (BollaScaricoRigaMagBulk)obj;
					riga.setStato(MovimentiMagBulk.STATO_ANNULLATO);
					riga.setToBeUpdated();
					super.modificaConBulk(userContext, riga);
				}
			}
		} catch (IntrospectionException e) {
			throw new ComponentException(e);
		}
		
	}


	private void verificaBeneDaAnnullare(UserContext userContext, MovimentiMagBulk movimentoDaAnnullare) throws ComponentException, PersistencyException, ApplicationException{

		MovimentiMagHome movimentiHome = (MovimentiMagHome)getHome(userContext, MovimentiMagBulk.class);
		try {
			List listaBeniInTrasito = movimentiHome.findBeniInTransito(movimentoDaAnnullare);
			if (listaBeniInTrasito != null){

				// ciclo per verificare che non ci siano beni già inventariati
				for (Object obj : listaBeniInTrasito) {
					Transito_beni_ordiniBulk beneTrans = (Transito_beni_ordiniBulk) obj;
					if (!beneTrans.getStato().equals(Transito_beni_ordiniBulk.STATO_ANNULLATO)) {
						if (beneTrans.getStato().equals(Transito_beni_ordiniBulk.STATO_TRASFERITO)) {

							throw new ApplicationException("Non è possibile annullare il movimento, risulta un bene già inventariato");
						}
					}
				}
				// se non ci sono beni inventariati annullo i beni in transito
				for (Object obj : listaBeniInTrasito) {
					Transito_beni_ordiniBulk beneTrans = (Transito_beni_ordiniBulk) obj;
					beneTrans.setStato(Transito_beni_ordiniBulk.STATO_ANNULLATO);
					beneTrans.setToBeUpdated();
					super.modificaConBulk(userContext, beneTrans);
				}
			}
		} catch (IntrospectionException e) {
			throw new ComponentException(e);
		}
	}
	
	private void controlliAnnullamento(UserContext userContext, MovimentiMagBulk movimentoDaAnnullare)
			throws ComponentException, PersistencyException, ApplicationException {
		if (movimentoDaAnnullare.getTipoMovimentoMag().isMovimentoDiCarico()){
			MovimentiMagHome movimentiHome = (MovimentiMagHome)getHome(userContext, MovimentiMagBulk.class);
	    	try {
				List lista = movimentiHome.recuperoMovimentiDaLotto(userContext,movimentoDaAnnullare);
				if (lista != null && !lista.isEmpty()){
					for (Object obj : lista){
						MovimentiMagBulk mag = (MovimentiMagBulk)obj;

						if(mag.getPgMovimento().compareTo(movimentoDaAnnullare.getPgMovimento()) != 0 &&
								((movimentoDaAnnullare.getPgMovimentoRif() != null && mag.getPgMovimento().compareTo(movimentoDaAnnullare.getPgMovimentoRif()) != 0)
																||
								(movimentoDaAnnullare.getPgMovimentoRif() == null && mag.getTipoMovimentoMag().isMovimentoDiScarico()))){
							throw new ApplicationException("Impossibile annullare il movimento. Esiste un altro movimento con progressivo "+mag.getPgMovimento() + " per il lotto del movimento per cui si sta chiedendo l'annullamento");
						}
					}
				}
	    	} catch (IntrospectionException e) {
				throw new ComponentException(e);
			}
		}
	}

	private MovimentiMagBulk preparaMovimentoDiAnnullamento(UserContext userContext, MovimentiMagBulk movimentoDaAnnullare) throws ComponentException {
		TipoMovimentoMagBulk tipoMovimento = (TipoMovimentoMagBulk)findByPrimaryKey(userContext, movimentoDaAnnullare.getTipoMovimentoMag());
		if (tipoMovimento.getTipoMovimentoMagStorno() != null && tipoMovimento.getTipoMovimentoMagStorno().getCdTipoMovimento() != null){

			MovimentiMagBulk movimentoDiStorno = (MovimentiMagBulk)movimentoDaAnnullare.clone();

			movimentoDiStorno.setDtMovimento(new Timestamp(System.currentTimeMillis()));
			movimentoDiStorno.setPgMovimento(null);
			movimentoDiStorno.setMovimentoRif(new MovimentiMagBulk());
			movimentoDiStorno.setTipoMovimentoMag(tipoMovimento.getTipoMovimentoMagStorno());
			movimentoDiStorno.setStato(MovimentiMagBulk.STATO_ANNULLATO);
			movimentoDiStorno.setCrudStatus(OggettoBulk.TO_BE_CREATED);
			return movimentoDiStorno;
		} else {
			throw new ApplicationException("Il tipo movimento "+movimentoDaAnnullare.getCdTipoMovimento() + " non ha impostato il tipo movimento di annullamento");
		}

	}

	private void controlloDatiObbligatoriMovimentiMagazzino(UserContext userContext, MovimentiMagazzinoBulk movimentiMagazzino, DivisaBulk divisaDefault) throws ApplicationException{
		if (divisaDefault==null || divisaDefault.getCd_divisa()==null)
			throw new ApplicationException("Impossibile caricare la valuta di default! Prima di poter effettuare lo scarico, immettere tale valore.");
		if (movimentiMagazzino.getUnitaOperativaAbilitata()==null || movimentiMagazzino.getUnitaOperativaAbilitata().getCdUnitaOperativa()==null)
			throw new ApplicationException("Errore nel movimento di magazzino! Manca l'indicazione della Unità Operativa.");
		if (movimentiMagazzino.getMagazzinoAbilitato()==null || movimentiMagazzino.getMagazzinoAbilitato().getCdMagazzino()==null)
			throw new ApplicationException("Errore nel movimento di magazzino! Manca l'indicazione del Magazzino.");
		if (movimentiMagazzino.getTipoMovimentoMag()==null || movimentiMagazzino.getTipoMovimentoMag().getCdTipoMovimento()==null)
			throw new ApplicationException("Errore nel movimento di magazzino! Manca l'indicazione del Tipo Movimento.");
		if (movimentiMagazzino.getDataCompetenza()==null){
			throw new ApplicationException("Errore nel movimento di magazzino! Manca l'indicazione della Data Competenza.");
		}else {
			java.util.Calendar gc = java.util.Calendar.getInstance();
			gc.setTime(movimentiMagazzino.getDataCompetenza());
			int annoCompetenza = gc.get(java.util.Calendar.YEAR);
			int esScrivania = it.cnr.contab.utenze00.bp.CNRUserContext.getEsercizio(userContext).intValue();

			if (annoCompetenza != esScrivania) {
				throw new ApplicationException("La \"Data di competenza\" deve ricadere nell'esercizio selezionato!");
			}
		}
	}

	private void controlloDatiObbligatoriCaricoManualeRiga(UserContext userContext, CaricoMagazzinoRigaBulk riga) throws ApplicationException {
		controlloDatiObbligatoriMovimentiMagazzinoRiga(userContext, riga);
		if (riga.getVoceIva()==null || riga.getVoceIva().getCd_voce_iva()==null)
			throw new ApplicationException("Errore nel movimento di magazzino! Manca l'indicazione dell'iva.");
		if (riga.getPrezzoUnitario()==null )
			throw new ApplicationException("Errore nel movimento di magazzino! Manca l'indicazione del prezzo unitario.");
	}
	private void controlloDatiObbligatoriMovimentiMagazzinoRiga(UserContext userContext, MovimentiMagazzinoRigaBulk riga) throws ApplicationException {
		if (riga.getBeneServizio()==null || riga.getBeneServizio().getCd_bene_servizio()==null)
			throw new ApplicationException("Errore nel movimento di magazzino! Manca l'indicazione del bene/servizio.");
		if (riga.getQuantita()==null )
			throw new ApplicationException("Errore nel movimento di magazzino! Manca l'indicazione della quantita.");
		if (riga.getUnitaMisura()==null || riga.getUnitaMisura().getCdUnitaMisura()==null)
			throw new ApplicationException("Errore nel movimento di magazzino! Manca l'indicazione dell'unità di misura.");
		if (riga.getCoefConv()==null)
			throw new ApplicationException("Errore nel movimento di magazzino! Manca l'indicazione del coefficiente di conversione.");
	}


    public List<BollaScaricoMagBulk> generaBolleScarico(UserContext userContext, List<MovimentiMagBulk> listaMovimenti)
    				throws ComponentException, PersistencyException, ApplicationException {
    	try {
	    	if (!listaMovimenti.isEmpty()){
	    		List<BollaScaricoMagBulk> listaBolleScarico = new ArrayList<>();
	
				listaMovimenti.stream()
						.collect(Collectors.groupingBy(MovimentiMagBulk::getCdUop,
								Collectors.groupingBy(MovimentiMagBulk::getCdCdsMag,
										Collectors.groupingBy(MovimentiMagBulk::getCdMagazzino,
												Collectors.groupingBy(MovimentiMagBulk::getDtRiferimento)))))
								.entrySet().stream().forEach(unitaOperativaSet->{
									unitaOperativaSet.getValue().entrySet().stream().forEach(cdsMagSet->{
										cdsMagSet.getValue().entrySet().stream().forEach(codmagSet->{
											codmagSet.getValue().entrySet().stream().forEach(dtRiferimentoSet->{
	   	    	    			BollaScaricoMagBulk bollaScarico = new BollaScaricoMagBulk();
	   							try {
		   	    					bollaScarico.setDtBollaSca(dtRiferimentoSet.getKey());
		   	    					bollaScarico.setMagazzino(new MagazzinoBulk(cdsMagSet.getKey(), codmagSet.getKey()));
		   	    					bollaScarico.setStato(OrdineAcqBulk.STATO_INSERITO);
		   	    					bollaScarico.setUnitaOperativaOrd(new UnitaOperativaOrdBulk(unitaOperativaSet.getKey()));
		   	    					bollaScarico.setToBeCreated();
	
		   	    					dtRiferimentoSet.getValue().stream().forEach(movimento->{
		   	   	    	    			BollaScaricoRigaMagBulk riga = new BollaScaricoRigaMagBulk();
		   	   	    	    			riga.setMovimentiMag(movimento);
		   	   	    	    			riga.setToBeCreated();
		   	   	    	    			bollaScarico.addToRigheColl(riga);
		   	    					});
			   	    	    		listaBolleScarico.add((BollaScaricoMagBulk)super.creaConBulk(userContext, bollaScarico));
	   							} catch (ComponentException ex) {
	   								throw new DetailedRuntimeException(ex);
	   							}
	   						});
										});
	   					});
					});
				listaBolleScarico.stream().forEach(bollaScarico->{
					try {
						this.aggiornaMovimentiConBollaScarico(userContext, bollaScarico);	
					} catch (ComponentException ex) {
						throw new DetailedRuntimeException(ex);
					}
				});
				
				return listaBolleScarico;
	    	}
	    	return null;
		} catch (DetailedRuntimeException ex) {
			throw handleException(ex.getDetail());
		}
    }

	private ImportoOrdine recuperoPrezzoUnitarioRettificato(UserContext userContext, FatturaOrdineBulk fatturaOrdineBulk) throws RemoteException, ComponentException, PersistencyException{
		OrdineAcqComponentSession ordineComponent = Utility.createOrdineAcqComponentSession();

		FatturaOrdineBulk fatturaOrdineBulk1 =  ordineComponent.calcolaImportoOrdine(userContext, fatturaOrdineBulk, true);
		ImportoOrdine importo = new ImportoOrdine();
		importo.setImponibile(fatturaOrdineBulk1.getImImponibile());
		importo.setImportoIvaInd(fatturaOrdineBulk1.getImIvaNd());
		return importo;
	}

	/*private BigDecimal getPrezzoUnitario(ImportoOrdine importo) {
		return importo.getImponibile().add(Utility.nvl(importo.getImportoIvaInd()).add(Utility.nvl(importo.getArrAliIva())));
	}*/

	private ImportoOrdine recuperoPrezzoUnitario(UserContext userContext, OrdineAcqConsegnaBulk cons) throws RemoteException, ComponentException{
    	OrdineAcqComponentSession ordineComponent = Utility.createOrdineAcqComponentSession();
        ParametriCalcoloImportoOrdine parametri = new ParametriCalcoloImportoOrdine();
    	OrdineAcqRigaBulk riga = cons.getOrdineAcqRiga();
        OrdineAcqBulk ordine = riga.getOrdineAcq();
		try {
			getHomeCache(userContext).fetchAll(userContext);
		} catch (PersistencyException e) {
			handleException(e);
		}
		parametri.setCambio(ordine.getCambio());
    	parametri.setDivisa(ordine.getDivisa());
    	parametri.setDivisaRisultato(getEuro(userContext));
    	parametri.setPercProrata(ordine.getPercProrata());
    	parametri.setCoefacq(riga.getCoefConv());
    	parametri.setPrezzo(riga.getPrezzoUnitario());
    	parametri.setSconto1(riga.getSconto1());
    	parametri.setSconto2(riga.getSconto2());
    	parametri.setSconto3(riga.getSconto3());
    	parametri.setVoceIva(riga.getVoceIva());
    	parametri.setQtaOrd(cons.getQuantita());
    	parametri.setArrAliIva(cons.getArrAliIva());
    	ImportoOrdine importo = ordineComponent.calcoloImportoOrdinePerMagazzino(userContext,parametri);
    	return importo;
    }
    
    private DivisaBulk getEuro(UserContext userContext) throws ComponentException {
    	try {
    		DivisaBulk divisaDefault = ((DivisaHome)getHome(userContext, DivisaBulk.class)).getDivisaDefault(userContext);
    		Optional.ofNullable(divisaDefault)
    				.map(DivisaBulk::getCd_divisa)
    				.orElseThrow(()->new ApplicationException("Impossibile caricare la valuta di default! Prima di poter inserire un ordine, immettere tale valore."));
    		return divisaDefault;
    	} catch (javax.ejb.EJBException|PersistencyException e) {
    		handleException(e);
    	}
    	return null;
    }

    public java.util.Collection<LottoMagBulk> findLottiMagazzino(UserContext userContext, MovimentiMagazzinoRigaBulk movimentiMagazzinoRigaBulk) throws ComponentException, PersistencyException {
		LottoMagHome lottoHome = (LottoMagHome)getHome(userContext, LottoMagBulk.class);
		return lottoHome.findLottiMagazzinoByClause(movimentiMagazzinoRigaBulk);
    }

    public ScaricoMagazzinoBulk scaricaMagazzino(UserContext userContext, ScaricoMagazzinoBulk scaricoMagazzino) throws ComponentException, PersistencyException {
    	List<BollaScaricoMagBulk> bolleList = new ArrayList<>();
		DivisaBulk divisaDefault = ((DivisaHome)getHome(userContext, DivisaBulk.class)).getDivisaDefault(userContext);
		controlloDatiObbligatoriMovimentiMagazzino(userContext, scaricoMagazzino, divisaDefault);
		if (scaricoMagazzino.getScaricoMagazzinoRigaColl()==null||scaricoMagazzino.getScaricoMagazzinoRigaColl().isEmpty())
			throw new ApplicationException("Errore nello scarico magazzino! Manca l'indicazione dei beni/servizi da movimentare.");

		LottoMagHome lottoHome = (LottoMagHome)getHome(userContext, LottoMagBulk.class);
		
		List<MovimentiMagBulk> listaMovimenti = new ArrayList<>();

		try {
			scaricoMagazzino.getScaricoMagazzinoRigaColl().stream()
			.forEach(scaricoMagazzinoRiga->{
				scaricoMagazzinoRiga.setAnomalia(null);
				List<String> errorList = new ArrayList<>();
				try {
					controlloDatiObbligatoriMovimentiMagazzinoRiga(userContext, scaricoMagazzinoRiga);
				} catch (ApplicationException e) {
					throw new DetailedRuntimeException(e);
				}

				try {
					//richiamo per ogni riga i lotti e faccio i lock 
					Map<LottoMagBulk, BigDecimal> mapLotti = new HashMap<LottoMagBulk, BigDecimal>();
					if (scaricoMagazzinoRiga.isImputazioneScaricoSuBeneEnable()) {
						java.util.Collection<LottoMagBulk> lottiList = findLottiMagazzino(userContext, scaricoMagazzinoRiga);
						BigDecimal qtResidua = scaricoMagazzinoRiga.getQtScaricoConvertita();
						scaricoMagazzinoRiga.setScaricoMagazzinoRigaLottoColl(new BulkList<ScaricoMagazzinoRigaLottoBulk>());
						for (Iterator<LottoMagBulk> iterator = lottiList.iterator(); iterator.hasNext();) {
							LottoMagBulk lottoMagazzino = iterator.next();
							//Aggiorno i lotti sull'oggetto ScaricoMagazzinoRiga
							ScaricoMagazzinoRigaLottoBulk rigaLotto = new ScaricoMagazzinoRigaLottoBulk();
							rigaLotto.setLottoMagazzino(lottoMagazzino);
							rigaLotto.setQtScarico(BigDecimal.ZERO);
							scaricoMagazzinoRiga.getScaricoMagazzinoRigaLottoColl().add(rigaLotto);

							if (qtResidua.compareTo(BigDecimal.ZERO)>0) {
								LottoMagBulk lottoMagazzinoDB = (LottoMagBulk)lottoHome.findAndLock(lottoMagazzino);

								//metto sull'oggetto ScaricoMagazzinoRiga il lotto bloccato
								rigaLotto.setLottoMagazzino(lottoMagazzinoDB);

								BigDecimal qtScarico = lottoMagazzinoDB.getGiacenza().compareTo(qtResidua)>0?qtResidua:lottoMagazzinoDB.getGiacenza();
								qtResidua = qtResidua.subtract(qtScarico);
								mapLotti.put(lottoMagazzinoDB, qtScarico);
							}
						}
					} else {
						scaricoMagazzinoRiga.getScaricoMagazzinoRigaLottoColl().stream()
							.filter(rigaLotto->rigaLotto.getQtScarico().compareTo(BigDecimal.ZERO)>0)
							.forEach(rigaLotto->{
								try{
									LottoMagBulk lottoMagazzinoDB = (LottoMagBulk)lottoHome.findAndLock(rigaLotto.getLottoMagazzino());
									mapLotti.put(lottoMagazzinoDB, rigaLotto.getQtScaricoConvertita());

									//metto sull'oggetto ScaricoMagazzinoRiga il lotto bloccato
									rigaLotto.setLottoMagazzino(lottoMagazzinoDB);
								} catch (OutdatedResourceException|BusyResourceException|PersistencyException ex) {
									throw new DetailedRuntimeException(ex);
								}
							});
					}
					
					//Verifico che i lotti hanno giacenza sufficiente
					mapLotti.entrySet().stream()
						.filter(set->set.getKey().getGiacenza().compareTo(set.getValue())<0)
						.forEach(set->{
							errorList.add("Per il lotto "+
									set.getKey().getEsercizio()+"/"+set.getKey().getCdNumeratoreMag()+"/"+set.getKey().getPgLotto()+
									" alla data "+new java.text.SimpleDateFormat("dd/MM/yyyy").format(scaricoMagazzino.getDataCompetenza())+
									" la giacenza ("+new it.cnr.contab.util.Importo5CifreFormat().format(set.getKey().getGiacenza())+
									") è inferiore alla quantità da scaricare ("+
									new it.cnr.contab.util.Importo5CifreFormat().format(set.getValue())+")");
						});
					
					//Verifico che la quantità da scaricare sia pari a quella richiesta
					BigDecimal totGiacenzaLotti = mapLotti.entrySet().stream()
													.map(Entry::getValue)
													.reduce(BigDecimal.ZERO, BigDecimal::add);
					if (totGiacenzaLotti.compareTo(scaricoMagazzinoRiga.getQtScaricoConvertita())<0)
						errorList.add("Alla data "+new java.text.SimpleDateFormat("dd/MM/yyyy").format(scaricoMagazzino.getDataCompetenza())+
								" la giacenza totale dei lotti ("+new it.cnr.contab.util.Importo5CifreFormat().format(totGiacenzaLotti)+
								") è inferiore alla quantità richiesta da scaricare ("+
								new it.cnr.contab.util.Importo5CifreFormat().format(scaricoMagazzinoRiga.getQtScaricoConvertita())+")");

					if (errorList.isEmpty()) {
						//Effettuo le movimentazioni di magazzino
						mapLotti.keySet().stream()
							.forEach(lottoMagazzino->{
								try {
									BigDecimal qtScarico = mapLotti.get(lottoMagazzino);
									//creo il movimento di carico di magazzino
									MovimentiMagBulk movimentoMag = createMovimentoMagazzino(userContext, 
															scaricoMagazzinoRiga.getUnitaOperativaRicevente(), 
															scaricoMagazzino.getMagazzinoAbilitato(),
															scaricoMagazzino.getTipoMovimentoMag(),
															scaricoMagazzinoRiga.getBeneServizio(),
															//scaricoMagazzinoRiga.getQuantita(),
															qtScarico,
															scaricoMagazzino.getDataCompetenza(),
															divisaDefault,
															BigDecimal.ONE,
															scaricoMagazzinoRiga.getUnitaMisura(),
															scaricoMagazzinoRiga.getCoefConv(),
															lottoMagazzino.getVoceIva());
									
									movimentoMag.setLottoMag(lottoMagazzino);
									movimentoMag.setPrezzoUnitario(lottoMagazzino.getCostoUnitario());
									movimentoMag.setImIva(lottoMagazzino.getImIva());
									listaMovimenti.add((MovimentiMagBulk)super.creaConBulk(userContext, movimentoMag));
								} catch (ComponentException|PersistencyException ex) {
									throw new DetailedRuntimeException(ex);
								}
							});
					} else
						scaricoMagazzinoRiga.setAnomalia(
								errorList.stream().collect(Collectors.joining(", ")));
				} catch (OutdatedResourceException|BusyResourceException|PersistencyException|DetailedRuntimeException|ComponentException ex) {
					throw new DetailedRuntimeException(ex);
				}
			});
		} catch (DetailedRuntimeException ex ) {
			throw handleException(ex.getDetail());
		}
		
		if (!listaMovimenti.isEmpty())
			bolleList = generaBolleScarico(userContext, listaMovimenti);

		scaricoMagazzino.setBolleScaricoColl(bolleList); 
		return scaricoMagazzino;
    }

	public MovimentiMagazzinoBulk initializeMovimentiMagazzino(UserContext usercontext, MovimentiMagazzinoBulk movimentoMagazzinoBulk) throws PersistencyException, ComponentException {
		movimentoMagazzinoBulk = (MovimentiMagazzinoBulk)initializeAbilitazioneMovimentiMagazzino(usercontext, movimentoMagazzinoBulk);		
		movimentoMagazzinoBulk.setDataMovimento(DateUtils.truncate(it.cnr.jada.util.ejb.EJBCommonServices.getServerTimestamp()));
		movimentoMagazzinoBulk.setDataCompetenza(DateUtils.truncate(it.cnr.jada.util.ejb.EJBCommonServices.getServerTimestamp()));
		return movimentoMagazzinoBulk;
	}

	public AbilitazioneMagazzinoBulk initializeAbilitazioneMovimentiMagazzino(UserContext usercontext, AbilitazioneMagazzinoBulk abilitazioneMagazzinoBulk) throws PersistencyException, ComponentException {
		AbilitazioneMagazzinoHome abilitazioneMagazzinoHome = (AbilitazioneMagazzinoHome)getHome(usercontext, abilitazioneMagazzinoBulk.getClass());
		UnitaOperativaOrdHome unitaOperativaHome = (UnitaOperativaOrdHome)getHome(usercontext, UnitaOperativaOrdBulk.class);
		
		SQLBuilder sqlUop = abilitazioneMagazzinoHome.selectUnitaOperativaAbilitataByClause(usercontext, abilitazioneMagazzinoBulk, 
				unitaOperativaHome, new UnitaOperativaOrdBulk(), new CompoundFindClause());
		List<UnitaOperativaOrdBulk> listUop=unitaOperativaHome.fetchAll(sqlUop);
		abilitazioneMagazzinoBulk.setUnitaOperativaAbilitata(Optional.ofNullable(listUop)
														.map(e->{
															if (e.stream().count()>1) {
																return null;
															};
															return e.stream().findFirst().orElse(null);
														}).orElse(null));
					

		MagazzinoHome magazzinoHome = (MagazzinoHome)getHome(usercontext, MagazzinoBulk.class);
		SQLBuilder sqlMagazzino = abilitazioneMagazzinoHome.selectMagazzinoAbilitatoByClause(usercontext, abilitazioneMagazzinoBulk, 
				magazzinoHome, new MagazzinoBulk(), new CompoundFindClause());
		List<MagazzinoBulk> listMagazzino=magazzinoHome.fetchAll(sqlMagazzino);
		abilitazioneMagazzinoBulk.setMagazzinoAbilitato(Optional.ofNullable(listMagazzino)
				.map(e->{
					if (e.stream().count()>1) {
						return null;
					};
					return e.stream().findFirst().orElse(null);
				}).orElse(null));
		
		return abilitazioneMagazzinoBulk;
	}

    public RemoteIterator preparaQueryBolleScaricoDaVisualizzare(UserContext userContext, List<BollaScaricoMagBulk> bolle) throws ComponentException{
		BollaScaricoMagHome homeBolla= (BollaScaricoMagHome)getHome(userContext, BollaScaricoMagBulk.class);
		return iterator(userContext,
				homeBolla.selectBolleGenerate(bolle),
				BollaScaricoMagBulk.class,
				"default");
    }
    
    public RemoteIterator ricercaMovimenti(UserContext userContext, ParametriSelezioneMovimentiBulk parametri) throws ComponentException
    {
    	MovimentiMagHome movimentiHome = (MovimentiMagHome)getHome(userContext, MovimentiMagBulk.class);
    	SQLBuilder sql = movimentiHome.createSQLBuilder();
    	sql.addColumn("BENE_SERVIZIO.DS_BENE_SERVIZIO");
    	sql.addSQLClause(FindClause.AND, "MOVIMENTI_MAG.stato", SQLBuilder.NOT_EQUALS, MovimentiMagBulk.STATO_ANNULLATO);
		if (parametri.getMagazzinoAbilitato() != null ){
			sql.addSQLClause(FindClause.AND, "LOTTO_MAG.cd_Magazzino", SQLBuilder.EQUALS, parametri.getMagazzinoAbilitato().getCdMagazzino());
		}
    	if (parametri.getDaDataMovimento() != null ){
    		sql.addSQLClause("AND","DT_MOVIMENTO",SQLBuilder.GREATER_EQUALS,parametri.getDaDataMovimento());
    	} 
    	if (parametri.getaDataMovimento() != null ){
    		Calendar cal = Calendar.getInstance();
    		cal.setTime(parametri.getaDataMovimento());
    		cal.add(Calendar.DAY_OF_WEEK, 1);
    		Timestamp aDataMovimento= new Timestamp(cal.getTime().getTime());
    		sql.addSQLClause("AND","DT_MOVIMENTO",SQLBuilder.LESS,aDataMovimento);
    	} 
    	if (parametri.getDaDataCompetenza() != null ){
    		sql.addSQLClause("AND","DT_RIFERIMENTO",SQLBuilder.GREATER_EQUALS,parametri.getDaDataCompetenza());
    	} 
    	if (parametri.getaDataCompetenza() != null ){
    		sql.addSQLClause("AND","DT_RIFERIMENTO",SQLBuilder.LESS_EQUALS,parametri.getaDataCompetenza());
    	} 
    	if (parametri.getTerzo() != null && parametri.getTerzo().getCd_terzo() != null){
    		sql.addSQLClause("AND","MOVIMENTI_MAG.CD_TERZO",SQLBuilder.EQUALS,parametri.getTerzo().getCd_terzo());
    	} 
    	if (parametri.getTipoMovimentoMag() != null && parametri.getTipoMovimentoMag().getCdTipoMovimento() != null){
     	}
        sql.generateJoin(MovimentiMagBulk.class, LottoMagBulk.class, "lottoMag", "LOTTO_MAG");

        sql.generateJoin(LottoMagBulk.class, Bene_servizioBulk.class, "beneServizio", "BENE_SERVIZIO");
    	
    	if (parametri.getDaBeneServizio() != null && parametri.getDaBeneServizio().getCd_bene_servizio() != null){
    		sql.addSQLClause("AND","BENE_SERVIZIO.CD_BENE_SERVIZIO",SQLBuilder.GREATER_EQUALS,parametri.getDaBeneServizio().getCd_bene_servizio());
    	} 
    	if (parametri.getaBeneServizio() != null && parametri.getaBeneServizio().getCd_bene_servizio() != null){
    		sql.addSQLClause("AND","BENE_SERVIZIO.CD_BENE_SERVIZIO",SQLBuilder.LESS_EQUALS,parametri.getaBeneServizio().getCd_bene_servizio());
    	} 
    	if (parametri.getDaUnitaOperativaRicevente() != null && parametri.getDaUnitaOperativaRicevente().getCdUnitaOperativa() != null){
    		sql.addSQLClause("AND","CD_UOP",SQLBuilder.GREATER_EQUALS,parametri.getDaUnitaOperativaRicevente().getCdUnitaOperativa());
    	} 
    	if (parametri.getaUnitaOperativaRicevente() != null && parametri.getaUnitaOperativaRicevente().getCdUnitaOperativa() != null){
    		sql.addSQLClause("AND","CD_UOP",SQLBuilder.LESS_EQUALS,parametri.getaUnitaOperativaRicevente().getCdUnitaOperativa());
    	} 
    	if (parametri.getDataBolla() != null ){
    		sql.addSQLClause("AND","DATA_BOLLA",SQLBuilder.GREATER_EQUALS,parametri.getDataBolla());
    	} 
    	if (parametri.getNumeroBolla() != null ){
    		sql.addSQLClause("AND","NUMERO_BOLLA",SQLBuilder.GREATER_EQUALS,parametri.getNumeroBolla());
    	} 
    	if (parametri.getLottoFornitore() != null ){
    		sql.addSQLClause("AND","LOTTO_FORNITORE",SQLBuilder.GREATER_EQUALS,parametri.getLottoFornitore());
    	} 
    	if (parametri.getUnitaOperativaOrdine() != null && parametri.getUnitaOperativaOrdine().getCdUnitaOperativa() != null){
    		sql.addSQLClause("AND","LOTTO_MAG.CD_UNITA_OPERATIVA",SQLBuilder.EQUALS,parametri.getUnitaOperativaOrdine().getCdUnitaOperativa());
    	} 
    	if (parametri.getNumerazioneOrd() != null && parametri.getNumerazioneOrd().getCdNumeratore() != null){
    		sql.addSQLClause("AND","LOTTO_MAG.CD_NUMERATORE_ORDINE",SQLBuilder.EQUALS,parametri.getNumerazioneOrd().getCdNumeratore());
    	} 
    	if (parametri.getDaNumeroOrdine() != null){
    		sql.addSQLClause("AND","LOTTO_MAG.NUMERO_ORDINE",SQLBuilder.GREATER_EQUALS,parametri.getDaNumeroOrdine());
    	} 
    	if (parametri.getaNumeroOrdine() != null){
    		sql.addSQLClause("AND","LOTTO_MAG.NUMERO_ORDINE",SQLBuilder.LESS_EQUALS,parametri.getaNumeroOrdine());
    	}

		if (parametri.getOrdineAcqConsegnaBulk()!= null) {
			sql.addSQLClause(FindClause.AND,"LOTTO_MAG.CD_CDS_ORDINE",SQLBuilder.EQUALS,parametri.getOrdineAcqConsegnaBulk().getCdCds());
			sql.addSQLClause(FindClause.AND,"LOTTO_MAG.CD_UNITA_OPERATIVA",SQLBuilder.EQUALS,parametri.getOrdineAcqConsegnaBulk().getCdUnitaOperativa());
			sql.addSQLClause(FindClause.AND,"LOTTO_MAG.ESERCIZIO_ORDINE",SQLBuilder.EQUALS,parametri.getOrdineAcqConsegnaBulk().getEsercizio());
			sql.addSQLClause(FindClause.AND,"LOTTO_MAG.CD_NUMERATORE_ORDINE",SQLBuilder.EQUALS,parametri.getOrdineAcqConsegnaBulk().getCdNumeratore());
			sql.addSQLClause(FindClause.AND,"LOTTO_MAG.NUMERO_ORDINE",SQLBuilder.EQUALS,parametri.getOrdineAcqConsegnaBulk().getNumero());
			sql.addSQLClause(FindClause.AND,"LOTTO_MAG.RIGA_ORDINE",SQLBuilder.EQUALS,parametri.getOrdineAcqConsegnaBulk().getRiga());
			sql.addSQLClause(FindClause.AND,"LOTTO_MAG.CONSEGNA",SQLBuilder.EQUALS,parametri.getOrdineAcqConsegnaBulk().getConsegna());
		}

		if (parametri.getDaDataOrdine() != null || parametri.getaDataOrdine() != null || parametri.getDaDataOrdineDef() != null || parametri.getaDataOrdineDef() != null){
            sql.generateJoin(LottoMagBulk.class, OrdineAcqConsegnaBulk.class, "ordineAcqConsegna", "ORDINE_ACQ_CONSEGNA");
            sql.generateJoin(OrdineAcqConsegnaBulk.class, OrdineAcqRigaBulk.class, "ordineAcqRiga", "ORDINE_ACQ_RIGA");
            sql.generateJoin(OrdineAcqRigaBulk.class, OrdineAcqBulk.class, "ordineAcq", "ORDINE_ACQ");
            if (parametri.getDaDataOrdine() != null){
        		sql.addSQLClause("AND","ORDINE_ACQ.DATA_ORDINE",SQLBuilder.GREATER_EQUALS,parametri.getDaDataOrdine());
            }
            if (parametri.getaDataOrdine() != null){
        		sql.addSQLClause("AND","ORDINE_ACQ.DATA_ORDINE",SQLBuilder.LESS_EQUALS,parametri.getaDataOrdine());
            }
            if (parametri.getDaDataOrdineDef() != null){
        		sql.addSQLClause("AND","ORDINE_ACQ.DATA_ORDINE_DEF",SQLBuilder.GREATER_EQUALS,parametri.getDaDataOrdineDef());
            }
            if (parametri.getaDataOrdineDef() != null){
        		sql.addSQLClause("AND","ORDINE_ACQ.DATA_ORDINE_DEF",SQLBuilder.LESS_EQUALS,parametri.getaDataOrdineDef());
            }
    	}

    	if (parametri.getTipoMovimento() != null){
			SQLBuilder sqlCdsExists = getHome(userContext, TipoMovimentoMagBulk.class).createSQLBuilder();
			sqlCdsExists.resetColumns();
			sqlCdsExists.addColumn("1");
			sqlCdsExists.addSQLJoin("TIPO_MOVIMENTO_MAG.CD_CDS", "MOVIMENTI_MAG.CD_CDS_TIPO_MOVIMENTO");
			sqlCdsExists.addSQLJoin("TIPO_MOVIMENTO_MAG.CD_TIPO_MOVIMENTO", "MOVIMENTI_MAG.CD_TIPO_MOVIMENTO");
			sqlCdsExists.addSQLClause("AND","TIPO_MOVIMENTO_MAG.TIPO", SQLBuilder.EQUALS, parametri.getTipoMovimento());
			sql.addSQLExistsClause("AND", sqlCdsExists);
		}


    	sql.addOrderBy("DT_MOVIMENTO");
    	sql.addOrderBy("DT_RIFERIMENTO");
    	
    	return  iterator(userContext,sql,MovimentiMagBulk.class,null);
    }

    private void aggiornaMovimentiConBollaScarico(UserContext userContext, BollaScaricoMagBulk bollaScaricoMag) throws ComponentException{
    	try {
	    	BollaScaricoMagHome bollaHome = (BollaScaricoMagHome)getHome(userContext, BollaScaricoMagBulk.class);
	    	MovimentiMagHome movHome = (MovimentiMagHome)getHome(userContext, MovimentiMagBulk.class);
	    	Optional.ofNullable(bollaHome.findBollaScaricoRigaMagList(bollaScaricoMag))
	    	.filter(list->!list.isEmpty())
	    	.ifPresent(list->{
	    		list.stream()
	    		.forEach(bollaRiga->{
	    			try{
	    				MovimentiMagBulk movimentoMag = (MovimentiMagBulk)movHome.findByPrimaryKey(bollaRiga.getMovimentiMag());
	    				movimentoMag.setBollaScaricoMag(bollaRiga.getBollaScaricoMag());
	    				movimentoMag.setToBeUpdated();
	    				modificaConBulk(userContext, movimentoMag);
	    			} catch (ComponentException|PersistencyException ex) {
	    				throw new DetailedRuntimeException(ex);
	    			}
			    });
		    });
		} catch (DetailedRuntimeException ex) {
			throw handleException(ex.getDetail());
    	} catch(PersistencyException|IntrospectionException e) {
    		throw new ComponentException(e);
    	}
    }

	/**
	 * inizializzaBulkPerStampa method comment.
	 */
	public it.cnr.jada.bulk.OggettoBulk inizializzaBulkPerStampa(UserContext userContext, OggettoBulk bulk) throws it.cnr.jada.comp.ComponentException {
		try{
			EnteBulk ente = (EnteBulk) getHome(userContext, EnteBulk.class).findAll().get(0);

//			if (bulk instanceof Stampa_registro_accertamentiBulk)
//				inizializzaBulkPerStampa(userContext, (Stampa_registro_accertamentiBulk)bulk);
//			else if (bulk instanceof Stampa_registro_annotazione_entrate_pgiroBulk)
//				inizializzaBulkPerStampa(userContext, (Stampa_registro_annotazione_entrate_pgiroBulk)bulk);
//			else if (bulk instanceof Stampa_scadenzario_accertamentiBulk)
//				inizializzaBulkPerStampa(userContext, (Stampa_scadenzario_accertamentiBulk)bulk);
		//	inizializzaBulkPerStampa(userContext, bulk);
			return bulk;
		} catch (it.cnr.jada.persistency.PersistencyException pe){
			throw new ComponentException(pe);
		}
	}

	@Override
	public OggettoBulk stampaConBulk(UserContext userContext, OggettoBulk oggettoBulk) throws ComponentException {
		//if (oggettoBulk instanceof Stampa_registro_accertamentiBulk)
		//	validateBulkForPrint(userContext, (Stampa_registro_accertamentiBulk)bulk);
		//else if (bulk instanceof Stampa_registro_annotazione_entrate_pgiroBulk)
		//	validateBulkForPrint(userContext, (Stampa_registro_annotazione_entrate_pgiroBulk)bulk);
		//else if (bulk instanceof Stampa_scadenzario_accertamentiBulk)
		//	validateBulkForPrint(userContext, (Stampa_scadenzario_accertamentiBulk)bulk);

		return oggettoBulk;
	}

}
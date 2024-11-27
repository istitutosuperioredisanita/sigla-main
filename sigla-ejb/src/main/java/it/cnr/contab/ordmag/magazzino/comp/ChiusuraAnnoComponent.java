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

import it.cnr.contab.config00.pdcep.bulk.Voce_epBulk;
import it.cnr.contab.docamm00.tabrif.bulk.Bene_servizioBulk;
import it.cnr.contab.docamm00.tabrif.bulk.Categoria_gruppo_inventBulk;
import it.cnr.contab.ordmag.anag00.MagazzinoBulk;
import it.cnr.contab.ordmag.anag00.RaggrMagazzinoBulk;
import it.cnr.contab.ordmag.anag00.UnitaMisuraBulk;
import it.cnr.contab.ordmag.magazzino.bulk.*;
import it.cnr.contab.ordmag.magazzino.dto.ValoriChiusuraCatGrVoceEP;
import it.cnr.contab.ordmag.magazzino.dto.ValoriLottoPerAnno;
import it.cnr.contab.ordmag.magazzino.ejb.MovimentiMagComponentSession;
import it.cnr.contab.util.Utility;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BusyResourceException;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.CRUDComponent;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.comp.ICRUDMgr;
import it.cnr.jada.comp.IPrintMgr;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.sql.LoggableStatement;

import java.io.Serializable;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

public class ChiusuraAnnoComponent extends CRUDComponent implements ICRUDMgr, IPrintMgr,Cloneable, Serializable {

	public ChiusuraAnnoBulk verificaChiusuraAnno(UserContext userContext,Integer esercizio,String tipoChiusura) throws ComponentException, PersistencyException {
		ChiusuraAnnoHome homeChiusuraAnno = (ChiusuraAnnoHome) getHome(userContext, ChiusuraAnnoBulk.class);
		return homeChiusuraAnno.getChiusuraAnno(esercizio,tipoChiusura);
	}


	public ChiusuraAnnoBulk calcolaRimanenzeAnno(UserContext userContext,Integer esercizio, Date dataFinePeriodo,String statoChiusura) throws RemoteException, ComponentException, PersistencyException, BusyResourceException {

		ChiusuraAnnoHome homeChiusuraAnno = (ChiusuraAnnoHome) getHome(userContext, ChiusuraAnnoBulk.class);
		ChiusuraAnnoBulk chiusuraAnno = verificaChiusuraAnno(userContext,esercizio,ChiusuraAnnoBulk.TIPO_CHIUSURA_MAGAZZINO);

		boolean nuovoCalcoloRimanenze=false;

		if(chiusuraAnno == null){
			chiusuraAnno = new ChiusuraAnnoBulk();
			chiusuraAnno.setPgChiusura(homeChiusuraAnno.recuperaNuovoProgressivo(userContext,chiusuraAnno));
			chiusuraAnno.setTipoChiusura(ChiusuraAnnoBulk.TIPO_CHIUSURA_MAGAZZINO);
			chiusuraAnno.setAnno(esercizio);
			chiusuraAnno.setStato(statoChiusura);
			chiusuraAnno.setDataCalcolo(dataFinePeriodo);
			chiusuraAnno.setCrudStatus(OggettoBulk.TO_BE_CREATED);
			nuovoCalcoloRimanenze=true;

		}else{
			// cancella i calcoli dei dettagli lotto legati alla chiusura
			eliminaRigheChiusuraMagRim(userContext,chiusuraAnno.getPgChiusura(), chiusuraAnno.getAnno(), chiusuraAnno.getTipoChiusura());
			// cancella i calcoli delle catgruppo/voce legati alla chiusura
			eliminaRigheChiusuraCatGruppoVoceEP(userContext,chiusuraAnno.getPgChiusura(), chiusuraAnno.getAnno(),chiusuraAnno.getTipoChiusura());

			chiusuraAnno.setStato(statoChiusura);
			chiusuraAnno.setDataCalcolo(dataFinePeriodo);
			chiusuraAnno.setCrudStatus(OggettoBulk.TO_BE_UPDATED);
		}
		chiusuraAnno.setToBeUpdated();
		makeBulkPersistent(userContext, chiusuraAnno);

		if(nuovoCalcoloRimanenze){
			creaConBulk(userContext,chiusuraAnno);
		}else {
			modificaConBulk(userContext, chiusuraAnno);
		}
		
		List<ValoriLottoPerAnno> qtyLottoAnnoList = getQuantitaAnnoPerLotto(userContext,esercizio,dataFinePeriodo);

		salvaDettagliChiusura(userContext,chiusuraAnno,qtyLottoAnnoList);

		aggiornaChiusuraMagRimConCmpp(userContext,esercizio);

		salvaImportiPerCatGruppoVoceEP(userContext,esercizio,ChiusuraAnnoBulk.TIPO_CHIUSURA_MAGAZZINO);

		return chiusuraAnno;
	}
	private void  salvaDettagliChiusura(UserContext userContext,ChiusuraAnnoBulk chiusura ,List<ValoriLottoPerAnno> qtyLottoAnnoList) throws ComponentException, PersistencyException {
		for(ValoriLottoPerAnno valoreLotto : qtyLottoAnnoList){
			getChiusuraAnnoMagRim(userContext,chiusura,valoreLotto);
		}
	}

	public List<ValoriLottoPerAnno> getQuantitaAnnoPerLotto(UserContext userContext,Integer esercizio, Date dataFinePeriodo) throws PersistencyException, ComponentException {
		Valori_magazzinoHome homeMag = (Valori_magazzinoHome) getHome(userContext, Valori_magazzinoBulk.class);
		List<ValoriLottoPerAnno> list = homeMag.getQuantitaAnnoPerLotto(esercizio, dataFinePeriodo);

		return list;
	}

	private void salvaImportiPerCatGruppoVoceEP(UserContext userContext,Integer esercizio,String tipoChiusura) throws ComponentException, PersistencyException {
		ChiusuraAnnoCatGrpVoceEpHome chiusuraAnnoCatGrpVoceEpHome = (ChiusuraAnnoCatGrpVoceEpHome) getHome(userContext, ChiusuraAnnoCatGrpVoceEpBulk.class);

		List<ValoriChiusuraCatGrVoceEP> list = chiusuraAnnoCatGrpVoceEpHome.getImportoChisuraAnnoCatGruppoVoceEP(esercizio,tipoChiusura);

		for (ValoriChiusuraCatGrVoceEP valore : list){
			ChiusuraAnnoCatGrpVoceEpBulk chiusuraCatVoce = new ChiusuraAnnoCatGrpVoceEpBulk();

			Categoria_gruppo_inventBulk catGruppo = new Categoria_gruppo_inventBulk();
			catGruppo.setNodoPadre(new Categoria_gruppo_inventBulk());
			catGruppo.setCd_categoria_gruppo(valore.getCdCategoriaGruppo());

			chiusuraCatVoce.setCategoriaGruppoInvent(catGruppo);

			Voce_epBulk voceEp = new Voce_epBulk();
			voceEp.setCd_voce_ep(valore.getCdVoceEp());
			voceEp.setEsercizio(valore.getEsercizio());

			chiusuraCatVoce.setVoceEp(voceEp);

			ChiusuraAnnoBulk chiusuraAnno = new ChiusuraAnnoBulk();
			chiusuraAnno.setAnno(valore.getEsercizio());
			chiusuraAnno.setPgChiusura(valore.getPgChiusura());
			chiusuraAnno.setTipoChiusura(valore.getTipoChiusura());

			chiusuraCatVoce.setChiusuraAnno(chiusuraAnno);

			chiusuraCatVoce.setImpTotale(valore.getImpTotCatGrVoceEP().setScale(6, BigDecimal.ROUND_HALF_UP));

			chiusuraCatVoce.setToBeUpdated();
			chiusuraCatVoce.setCrudStatus(OggettoBulk.TO_BE_CREATED);

			makeBulkPersistent(userContext, chiusuraCatVoce);
			creaConBulk(userContext,chiusuraCatVoce);
		}
	}

	private void getChiusuraAnnoMagRim(UserContext userContext,ChiusuraAnnoBulk chiusura,ValoriLottoPerAnno valoreLotto) throws ComponentException, PersistencyException {

		ChiusuraAnnoMagRimBulk chiusuraRiga = new ChiusuraAnnoMagRimBulk(valoreLotto.getCdCdsLotto(),
				valoreLotto.getCdMagazzinoLotto(),
				valoreLotto.getEsercizioLotto(),
				valoreLotto.getCdNumeratoreMagLotto(),
				valoreLotto.getPgLotto(),
				chiusura.getAnno());

		Bene_servizioBulk bene = new Bene_servizioBulk();
		bene.setCd_bene_servizio(valoreLotto.getCdBeneServizio());
		bene.setDs_bene_servizio(valoreLotto.getBeneServizioDesc());
		bene.setUnitaMisura(new UnitaMisuraBulk());
		bene.setUnita_misura(valoreLotto.getUnitaMisura());

		Categoria_gruppo_inventBulk catGruppo = new Categoria_gruppo_inventBulk();
		catGruppo.setNodoPadre(new Categoria_gruppo_inventBulk());
		catGruppo.setCd_categoria_gruppo(valoreLotto.getCdCatGruppo());
		catGruppo.setCd_categoria_padre(valoreLotto.getCdCatPadre());
		catGruppo.setCd_proprio(valoreLotto.getCdGruppo());

		bene.setCategoria_gruppo(catGruppo);

		MagazzinoBulk magazzino = new MagazzinoBulk();
		magazzino.setCdMagazzino(valoreLotto.getCdMagazzinoLotto());
		magazzino.setCdCds(valoreLotto.getCdCdsLotto());

		RaggrMagazzinoBulk raggruppamentoMag = new RaggrMagazzinoBulk();
		raggruppamentoMag.setCdRaggrMagazzino(valoreLotto.getCdRaggrMag());
		raggruppamentoMag.setCdCds(valoreLotto.getCdsRaggrMag());

		magazzino.setRaggrMagazzinoRim(raggruppamentoMag);


		chiusuraRiga.setChiusuraAnno(chiusura);
		chiusuraRiga.setBeneServizio(bene);
		chiusuraRiga.setCategoriaGruppoInvent(catGruppo);
		chiusuraRiga.setMagazzino(magazzino);
		chiusuraRiga.setRaggrMagazzino(raggruppamentoMag);

		chiusuraRiga.setUnitaMisura(valoreLotto.getUnitaMisura());
		chiusuraRiga.setCaricoAnno(valoreLotto.getQtyCaricoAnno());
		chiusuraRiga.setScaricoAnno(valoreLotto.getQtyScaricoAnno());
		chiusuraRiga.setImportoUnitarioChi(valoreLotto.getValoreUnitarioChi());
		chiusuraRiga.setImportoUnitarioLotto(valoreLotto.getValoreUnitarioLotto());
		chiusuraRiga.setCaricoIniziale(valoreLotto.getQtyInizioAnno());
		chiusuraRiga.setGiacenza(valoreLotto.getGiacenzaCalcolata());
		// viene impostato da update successivamente
		chiusuraRiga.setImportoCmppArt(new BigDecimal(0));

		chiusuraRiga.setToBeUpdated();
		chiusuraRiga.setCrudStatus(OggettoBulk.TO_BE_CREATED);

		makeBulkPersistent(userContext, chiusuraRiga);
		creaConBulk(userContext,chiusuraRiga);

	}
	@Override
	public OggettoBulk inizializzaBulkPerStampa(UserContext userContext, OggettoBulk oggettoBulk) throws ComponentException {
		return oggettoBulk;
	}

	@Override
	public OggettoBulk stampaConBulk(UserContext userContext, OggettoBulk oggettoBulk) throws ComponentException {
		return oggettoBulk;
	}
	protected void eliminaRigheChiusuraMagRim(UserContext userContext, Integer pgChiusura, Integer anno, String tipoChiusura)
			throws ComponentException {
		try {
			LoggableStatement ps = new LoggableStatement(
					getConnection(userContext), "DELETE FROM "
					+ it.cnr.jada.util.ejb.EJBCommonServices
					.getDefaultSchema() + "CHIUSURA_ANNO_MAG_RIM "
					+ "WHERE PG_CHIUSURA = ?  AND ANNO = ? AND TIPO_CHIUSURA = ?", true, this.getClass());

			try {
				ps.setObject(1, pgChiusura);
				ps.setObject(2, anno);
				ps.setObject(3, tipoChiusura);

				ps.executeUpdate();
			} finally {
				try {
					ps.close();
				} catch (java.sql.SQLException e) {
				}
			}

		} catch (SQLException e) {
			throw handleException(e);
		}
	}
	protected void eliminaRigheChiusuraCatGruppoVoceEP(UserContext userContext, Integer pgChiusura, Integer anno, String tipoChiusura)
			throws ComponentException {
		try {
			LoggableStatement ps = new LoggableStatement(
					getConnection(userContext), "DELETE FROM "
					+ it.cnr.jada.util.ejb.EJBCommonServices
					.getDefaultSchema() + "CHIUSURA_ANNO_CATGRP_VOCE_EP "
					+ "WHERE PG_CHIUSURA = ?  AND ANNO = ? AND TIPO_CHIUSURA = ? ", true, this.getClass());

			try {
				ps.setObject(1, pgChiusura);
				ps.setObject(2, anno);
				ps.setObject(3, tipoChiusura);

				ps.executeUpdate();
			} finally {
				try {
					ps.close();
				} catch (java.sql.SQLException e) {
				}
			}

		} catch (SQLException e) {
			throw handleException(e);
		}
	}
	protected void eliminaChiusuraAnno(UserContext userContext, Integer pgChiusura, Integer anno, String tipoChiusura)
			throws ComponentException {
		try {
			LoggableStatement ps = new LoggableStatement(
					getConnection(userContext), "DELETE FROM "
					+ it.cnr.jada.util.ejb.EJBCommonServices
					.getDefaultSchema() + "CHIUSURA_ANNO "
					+ "WHERE PG_CHIUSURA = ?  AND ANNO = ? AND TIPO_CHIUSURA = ? ", true, this.getClass());

			try {
				ps.setObject(1, pgChiusura);
				ps.setObject(2, anno);
				ps.setObject(3, tipoChiusura);

				ps.executeUpdate();
			} finally {
				try {
					ps.close();
				} catch (java.sql.SQLException e) {
				}
			}

		} catch (SQLException e) {
			throw handleException(e);
		}
	}
	void aggiornaChiusuraMagRimConCmpp(UserContext userContext,Integer esercizio) throws ComponentException, PersistencyException {
		ChiusuraAnnoMagRimHome chiusuraAnnoMagRimHome  = ( ChiusuraAnnoMagRimHome)getHome(userContext,ChiusuraAnnoMagRimBulk.class);
		chiusuraAnnoMagRimHome.getCmppPerArticolo(userContext,esercizio);

	}
	public ChiusuraAnnoBulk salvaChiusuraDefinitiva(UserContext userContext,Integer esercizio,String tipoChiusura,Date dataFinePeriodo) throws RemoteException, ComponentException, PersistencyException, ParseException {

		ChiusuraAnnoBulk chiusuraAnnoBulk = verificaChiusuraAnno(userContext, esercizio, tipoChiusura);

			MovimentiMagComponentSession movimentiMagComponent = Utility.createMovimentiMagComponentSession();

		int annoChiusura = chiusuraAnnoBulk.getAnno().intValue()+1;
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
		java.sql.Timestamp dataRifMovChi = (new java.sql.Timestamp(sdf.parse("01/01/"+annoChiusura).getTime()));

		movimentiMagComponent.creaMovimentoChiusura(userContext,chiusuraAnnoBulk.getPgChiusura(), chiusuraAnnoBulk.getAnno(), chiusuraAnnoBulk.getTipoChiusura(),dataRifMovChi);

		chiusuraAnnoBulk.setStato(ChiusuraAnnoBulk.STATO_CHIUSURA_DEFINITIVO);
		chiusuraAnnoBulk.setDataCalcolo(dataFinePeriodo);
		chiusuraAnnoBulk.setCrudStatus(OggettoBulk.TO_BE_UPDATED);
		chiusuraAnnoBulk.setToBeUpdated();

		makeBulkPersistent(userContext, chiusuraAnnoBulk);
		modificaConBulk(userContext, chiusuraAnnoBulk);
		return chiusuraAnnoBulk;
	}
	public void annullaChiusuraDefinitiva(UserContext userContext,Integer esercizio,String tipoChiusura) throws RemoteException, ComponentException, PersistencyException, ParseException {

		ChiusuraAnnoBulk chiusuraAnnoBulk = verificaChiusuraAnno(userContext, esercizio, tipoChiusura);

		MovimentiMagComponentSession movimentiMagComponent = Utility.createMovimentiMagComponentSession();

		int annoChiusura = chiusuraAnnoBulk.getAnno().intValue()+1;
		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
		java.sql.Timestamp dataRifMovChi = (new java.sql.Timestamp(sdf.parse("01/01/"+annoChiusura).getTime()));

		movimentiMagComponent.eliminaMovimentoChiusura(userContext,chiusuraAnnoBulk.getPgChiusura(), chiusuraAnnoBulk.getAnno(), chiusuraAnnoBulk.getTipoChiusura(),dataRifMovChi);

		// cancella i calcoli dei dettagli lotto legati alla chiusura
		eliminaRigheChiusuraMagRim(userContext,chiusuraAnnoBulk.getPgChiusura(), chiusuraAnnoBulk.getAnno(), chiusuraAnnoBulk.getTipoChiusura());
		// cancella i calcoli delle catgruppo/voce legati alla chiusura
		eliminaRigheChiusuraCatGruppoVoceEP(userContext,chiusuraAnnoBulk.getPgChiusura(), chiusuraAnnoBulk.getAnno(),chiusuraAnnoBulk.getTipoChiusura());
		// cancella chiusura anno
		eliminaChiusuraAnno(userContext,chiusuraAnnoBulk.getPgChiusura(), chiusuraAnnoBulk.getAnno(),chiusuraAnnoBulk.getTipoChiusura());


	}
}
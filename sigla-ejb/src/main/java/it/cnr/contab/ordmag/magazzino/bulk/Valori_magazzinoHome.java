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

package it.cnr.contab.ordmag.magazzino.bulk;


import com.fasterxml.jackson.databind.ObjectMapper;
import it.cnr.contab.docamm00.tabrif.bulk.Bene_servizioBulk;
import it.cnr.contab.ordmag.anag00.TipoMovimentoMagBulk;
import it.cnr.contab.ordmag.magazzino.dto.StampaInventarioDTO;
import it.cnr.contab.ordmag.magazzino.dto.StampaInventarioDTOKey;
import it.cnr.contab.reports.bulk.Print_spoolerBulk;
import it.cnr.contab.reports.bulk.Print_spooler_paramBulk;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.bulk.BulkList;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.PersistentCache;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.SQLBuilder;
import it.cnr.jada.util.Orderable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


public class Valori_magazzinoHome extends BulkHome {

	private transient static final Logger _log = LoggerFactory.getLogger(Valori_magazzinoHome.class);

	public Valori_magazzinoHome(Connection conn) {
		super(Valori_magazzinoBulk.class, conn);
	}
	public Valori_magazzinoHome(Connection conn, PersistentCache persistentCache) {
		super(Valori_magazzinoBulk.class, conn, persistentCache);
	}



	public Map<String,BigDecimal> calcolaMediaPonderataAdArticolo(UserContext uc,Integer esercizio,Date dataFine,Date dataInizio,Date dataMovimentoChiusura,String codRaggrMag,String codMag,String catGruppo){

		Map<StampaInventarioDTOKey,StampaInventarioDTO> lottiMap = getLottiMagazzino(uc,esercizio,dataInizio,dataFine,dataMovimentoChiusura,codRaggrMag,codMag,catGruppo);

		Map<String,BigDecimal>  articoliCMPMap = calcolaMediaPonderataAdArticolo(lottiMap);

		return articoliCMPMap;

	}
	public Map<String,BigDecimal> calcolaMediaPonderataCategoriaGruppo(UserContext uc,Integer esercizio,Date dataFine,Date dataInizio,Date dataMovimentoChiusura,String codRaggrMag,String codMag,String catGruppo){

		Map<StampaInventarioDTOKey,StampaInventarioDTO> lottiMap = getLottiMagazzino(uc,esercizio,dataInizio,dataFine,dataMovimentoChiusura,codRaggrMag,codMag,catGruppo);

		Map<String,BigDecimal>  catGruppoCMPMap = calcolaMediaPonderataCategoriaGruppo(lottiMap);

		return catGruppoCMPMap;
	}
	public Map<String,BigDecimal> calcolaMediaPonderataRaggruppamentoMagazzino(UserContext uc,Integer esercizio,Date dataFine,Date dataInizio,Date dataMovimentoChiusura,String codRaggrMag,String codMag,String catGruppo){

		Map<StampaInventarioDTOKey,StampaInventarioDTO> lottiMap = getLottiMagazzino(uc,esercizio,dataInizio,dataFine,dataMovimentoChiusura,codRaggrMag,codMag,catGruppo);

		Map<String,BigDecimal>  raggrMagCMPMap = calcolaMediaPonderataRaggruppamentoMagazzino(lottiMap);

		return raggrMagCMPMap;
	}





	public Map<StampaInventarioDTOKey,StampaInventarioDTO> getLottiMagazzino(UserContext uc,Integer esercizio,Date dataInizio,Date dataFine,Date dataMovimentoChiusura,String codRaggrMag,String codMag,String catGruppo){


		Map<StampaInventarioDTOKey,StampaInventarioDTO> lottiMap= new HashMap<StampaInventarioDTOKey,StampaInventarioDTO>();

		LottoMagHome lottoMagHome  = ( LottoMagHome)getHomeCache().getHome(LottoMagBulk.class);
		MovimentiMagHome movimentoMagHome = (MovimentiMagHome)getHomeCache().getHome(MovimentiMagBulk.class);

		try {
			// ESTRAZIONE DEI LOTTI CON GIACENZA E CON DATA DI CARICO COMPRESA NEL RANGE FORNITO IN INPUT
			List<LottoMagBulk> lotti=lottoMagHome.getLottiCompresiTra(uc,dataInizio,dataFine,esercizio,codRaggrMag,null,catGruppo);
			impostaLottiInMappaLotti(lotti,lottiMap);

			// ESTRAZIONE DEI MOVIMENTI DI CHIUSURA
			List<MovimentiMagBulk> movimenti= movimentoMagHome.getMovimentiDiChiusura(uc,dataMovimentoChiusura);
			aggiornaMappaLottiConDatiMovimentoChiusura(movimenti,lottiMap);

			// QUERY ESTRAZIONE MOVIMENTI ANTECEDENTI ALLA DATA DI RIFERIMENTO PER IL CALCOLO DEL VALORE DI CARICO E SCARICO
			movimenti = movimentoMagHome.getMovimentiCompresiTra(uc,dataInizio,dataFine,esercizio,codRaggrMag,catGruppo);
			aggiornaMappaLottiConCarichiScarichiPeriodo(movimenti,lottiMap);

			// QUERY ESTRAZIONE MOVIMENTI SUCCESSIVI ALLA DATA DI RIFERIMENTO PER IL CORRETTO CALCOLO DELLE GIACENZE
			movimenti=movimentoMagHome.getMovimentiSuccessiviAData(uc,dataFine,esercizio,codRaggrMag,null,catGruppo);
			aggiornaMappaLottiConGiacenzeCorrette(movimenti,lottiMap);

		} catch (PersistencyException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return lottiMap;
	}

	public Map<String,BigDecimal> calcolaMediaPonderataAdArticolo(Map<StampaInventarioDTOKey,StampaInventarioDTO> lottiMap){

		Map<String,ArrayList<StampaInventarioDTO>> articoliMap = getMappaArticoliDaMappaLotti(lottiMap);

		Map<String,BigDecimal> cmpArticoliMap = new HashMap<String,BigDecimal>();


		for (Map.Entry<String,ArrayList<StampaInventarioDTO>> articoloMap : articoliMap.entrySet()) {


			ArrayList<StampaInventarioDTO> lottiArticoloArray = articoloMap.getValue();

			BigDecimal articoloCtr = new BigDecimal(0);
			BigDecimal importoTotaleArticolo = new BigDecimal(0);

			for(StampaInventarioDTO lottoArticolo:lottiArticoloArray){

				articoloCtr.add(new BigDecimal(1));
				importoTotaleArticolo.add(lottoArticolo.getImportoUnitario().multiply(lottoArticolo.getGiacenza()));
			}

			if(cmpArticoliMap.get(articoloMap.getKey()) == null){
				cmpArticoliMap.put(articoloMap.getKey(),new BigDecimal(0));
			}
			// calcolo media ponderata (importo totale articoli / totale articoli)
			BigDecimal cmpArticolo =importoTotaleArticolo.divide(articoloCtr);
			cmpArticoliMap.put(articoloMap.getKey(),cmpArticolo);

		}
		return cmpArticoliMap;
	}

	public Map<String,BigDecimal> calcolaMediaPonderataCategoriaGruppo(Map<StampaInventarioDTOKey,StampaInventarioDTO> lottiMap){

		Map<String,ArrayList<StampaInventarioDTO>> catGruppoMap = getMappaCatGruppoDaMappaLotti(lottiMap);

		Map<String,BigDecimal> cmpCatGruppoMap = new HashMap<String,BigDecimal>();


		for (Map.Entry<String,ArrayList<StampaInventarioDTO>> catGruppo : catGruppoMap.entrySet()) {

			ArrayList<StampaInventarioDTO> lottiCatGruppoArray = catGruppo.getValue();

			BigDecimal catGruppoCtr = new BigDecimal(0);
			BigDecimal importoTotaleCatGruppo = new BigDecimal(0);

			for(StampaInventarioDTO lottoCat:lottiCatGruppoArray){

				catGruppoCtr.add(new BigDecimal(1));
				importoTotaleCatGruppo.add(lottoCat.getImportoUnitario().multiply(lottoCat.getGiacenza()));
			}

			if(cmpCatGruppoMap.get(catGruppo.getKey())==null){
				cmpCatGruppoMap.put(catGruppo.getKey(),new BigDecimal(0));
			}
			// calcolo media ponderata (importo totale categoria gruppo / totale articoli)
			BigDecimal cmpCatGruppo = importoTotaleCatGruppo.divide(catGruppoCtr);
			cmpCatGruppoMap.put(catGruppo.getKey(),cmpCatGruppo);

		}
		return cmpCatGruppoMap;
	}

	public Map<String,BigDecimal>  calcolaMediaPonderataRaggruppamentoMagazzino(Map<StampaInventarioDTOKey,StampaInventarioDTO> lottiMap){

		Map<String,ArrayList<StampaInventarioDTO>> raggrMagMap = getMappaRaggrMagDaMappaLotti(lottiMap);

		Map<String,BigDecimal> cmpRaggrMagMap = new HashMap<String,BigDecimal>();


		for (Map.Entry<String,ArrayList<StampaInventarioDTO>> raggrMag : raggrMagMap.entrySet()) {

			ArrayList<StampaInventarioDTO> lottiRaggrMagArray = raggrMag.getValue();

			BigDecimal raggrMagCtr = new BigDecimal(0);
			BigDecimal importoTotaleRaggrMag = new BigDecimal(0);

			for(StampaInventarioDTO lottoRaggrMag:lottiRaggrMagArray){

				raggrMagCtr.add(new BigDecimal(1));
				importoTotaleRaggrMag.add(lottoRaggrMag.getImportoUnitario().multiply(lottoRaggrMag.getGiacenza()));
			}

			if(cmpRaggrMagMap.get(raggrMag.getKey())==null){
				cmpRaggrMagMap.put(raggrMag.getKey(),new BigDecimal(0));
			}
			// calcolo media ponderata (importo totale categoria gruppo / totale articoli)
			BigDecimal cmpRaggrMag = importoTotaleRaggrMag.divide(raggrMagCtr);
			cmpRaggrMagMap.put(raggrMag.getKey(),cmpRaggrMag);

		}
		return cmpRaggrMagMap;
	}

	// MAP COD_ARTICOLO / ARRAY LOTTI CHE LO CONTENGONO
	private Map<String,ArrayList<StampaInventarioDTO>> getMappaArticoliDaMappaLotti(Map<StampaInventarioDTOKey,StampaInventarioDTO> lottiMap){
		Map<String,ArrayList<StampaInventarioDTO>> articoliMap = new HashMap<String,ArrayList<StampaInventarioDTO>>();

		for (StampaInventarioDTO lotto : lottiMap.values()) {

			ArrayList<StampaInventarioDTO> articoloArray = articoliMap.get(lotto.getCod_articolo());

			if(articoloArray == null){
				articoloArray = new ArrayList<StampaInventarioDTO>();
				articoloArray.add(lotto);
				articoliMap.put(lotto.getCod_articolo(),articoloArray);
			}else {
				articoloArray.add(lotto);
				articoliMap.put(lotto.getCod_articolo(),articoloArray);
			}
		}
		return articoliMap;
	}

	// MAP COD_CATEGORIA_GRUPPO / ARRAY LOTTI CHE LO CONTENGONO
	private Map<String,ArrayList<StampaInventarioDTO>> getMappaCatGruppoDaMappaLotti(Map<StampaInventarioDTOKey,StampaInventarioDTO> lottiMap){
		Map<String,ArrayList<StampaInventarioDTO>> catGruppoMap = new HashMap<String,ArrayList<StampaInventarioDTO>>();

		for (StampaInventarioDTO lotto : lottiMap.values()) {

			ArrayList<StampaInventarioDTO> catGruppoArray = catGruppoMap.get(lotto.getCod_categoria()+"."+lotto.getCod_gruppo());

			if(catGruppoArray == null){
				catGruppoArray = new ArrayList<StampaInventarioDTO>();
				catGruppoArray.add(lotto);
				catGruppoMap.put(lotto.getCod_categoria()+"."+lotto.getCod_gruppo(),catGruppoArray);
			}else {
				catGruppoArray.add(lotto);
				catGruppoMap.put(lotto.getCod_categoria()+"."+lotto.getCod_gruppo(),catGruppoArray);
			}
		}
		return catGruppoMap;
	}
	// MAP COD_RAGGR_MAG / ARRAY DI LOTTI CHE LO CONTENGONO
	private Map<String,ArrayList<StampaInventarioDTO>>  getMappaRaggrMagDaMappaLotti(Map<StampaInventarioDTOKey,StampaInventarioDTO> lottiMap){
		Map<String,ArrayList<StampaInventarioDTO>> raggrMagMap = new HashMap<String,ArrayList<StampaInventarioDTO>>();

		for (StampaInventarioDTO lotto : lottiMap.values()) {

			ArrayList<StampaInventarioDTO>  raggrMagArray = raggrMagMap.get(lotto.getCdMagRaggr());

			if(raggrMagArray == null){

				raggrMagArray = new ArrayList<StampaInventarioDTO>();
				raggrMagArray.add(lotto);
				raggrMagMap.put(lotto.getCdMagRaggr(),raggrMagArray);

			}else {
				raggrMagArray.add(lotto);
				raggrMagMap.put(lotto.getCdMagRaggr(),raggrMagArray);
			}
		}
		return raggrMagMap;
	}

	private void impostaLottiInMappaLotti(List<LottoMagBulk> lotti,Map<StampaInventarioDTOKey,StampaInventarioDTO> lottiMap){
		for ( LottoMagBulk m:lotti){

			StampaInventarioDTO inv  =new StampaInventarioDTO();
			inv.setCd_magazzino(m.getCdMagazzino());
			inv.setDesc_magazzino(m.getMagazzino().getDsMagazzino());
			inv.setCdMagRaggr(m.getMagazzino().getCdRaggrMagazzinoRim());
			inv.setDescMagRaggr(m.getMagazzino().getRaggrMagazzinoRim().getDsRaggrMagazzino());
			inv.setCod_articolo(m.getCdBeneServizio());
			inv.setGiacenza(m.getGiacenza());
			inv.setAnnoLotto(m.getEsercizio());
			inv.setTipoLotto(m.getCdNumeratoreMag());
			inv.setNumeroLotto(m.getPgLotto());
			inv.setCategoriaGruppo(m.getBeneServizio().getCd_categoria_gruppo());
			inv.setDescArticolo(m.getBeneServizio().getDs_bene_servizio());
			inv.setCod_categoria(m.getBeneServizio().getCategoria_gruppo().getCd_categoria_padre());
			inv.setCod_gruppo(m.getBeneServizio().getCategoria_gruppo().getCd_proprio());
			inv.setUm(m.getBeneServizio().getUnitaMisura().getCdUnitaMisura());
			inv.setDescCatGrp(m.getBeneServizio().getCategoria_gruppo().getDs_categoria_gruppo());
			inv.setImportoUnitario(m.getCostoUnitario());
			inv.setCdCds(m.getCdCds());
			inv.setQtyInizioAnno(m.getQuantitaInizioAnno());
			inv.setValoreInizioAnno(m.getCostoUnitario());
			inv.setDataCaricoLotto(m.getDtCarico());

			StampaInventarioDTOKey invKeyDaLotto = new StampaInventarioDTOKey(inv.getCdCds(),inv.getAnnoLotto(),inv.getNumeroLotto(),inv.getCd_magazzino(),inv.getCategoriaGruppo(),inv.getCod_articolo(),inv.getTipoLotto());
			lottiMap.put(invKeyDaLotto,inv);

		}
	}
	private void aggiornaMappaLottiConDatiMovimentoChiusura(List<MovimentiMagBulk>  movimenti,Map<StampaInventarioDTOKey,StampaInventarioDTO> lottiMap){

		// PER TUTTI I MOVIMENTI DI CHIUSURA
		for(MovimentiMagBulk movimento : movimenti){

			StampaInventarioDTOKey invKeyDaMovimento = new StampaInventarioDTOKey(movimento.getLottoMag().getCdCds(),movimento.getLottoMag().getEsercizio(),movimento.getLottoMag().getPgLotto(),movimento.getLottoMag().getCdMagazzino(),
					movimento.getLottoMag().getBeneServizio().getCd_categoria_gruppo(),movimento.getLottoMag().getCdBeneServizio(),movimento.getLottoMag().getCdNumeratoreMag());

			StampaInventarioDTO invDto = lottiMap.get(invKeyDaMovimento);

			if(invDto==null){
				invDto  =new StampaInventarioDTO();
				invDto.setCd_magazzino(movimento.getLottoMag().getCdMagazzino());
				invDto.setDesc_magazzino(movimento.getLottoMag().getMagazzino().getDsMagazzino());
				invDto.setCdMagRaggr(movimento.getLottoMag().getMagazzino().getCdRaggrMagazzinoRim());
				invDto.setDescMagRaggr(movimento.getLottoMag().getMagazzino().getRaggrMagazzinoRim().getDsRaggrMagazzino());
				invDto.setCod_articolo(movimento.getLottoMag().getCdBeneServizio());
				invDto.setGiacenza(new BigDecimal(0));
				invDto.setAnnoLotto(movimento.getLottoMag().getEsercizio());
				invDto.setTipoLotto(movimento.getLottoMag().getCdNumeratoreMag());
				invDto.setNumeroLotto(movimento.getLottoMag().getPgLotto());
				invDto.setCategoriaGruppo(movimento.getLottoMag().getBeneServizio().getCd_categoria_gruppo());
				invDto.setDescArticolo(movimento.getLottoMag().getBeneServizio().getDs_bene_servizio());
				invDto.setCod_categoria(movimento.getLottoMag().getBeneServizio().getCategoria_gruppo().getCd_categoria_padre());
				invDto.setCod_gruppo(movimento.getLottoMag().getBeneServizio().getCategoria_gruppo().getCd_proprio());
				invDto.setUm(movimento.getLottoMag().getBeneServizio().getUnitaMisura().getCdUnitaMisura());
				invDto.setDescCatGrp(movimento.getLottoMag().getBeneServizio().getCategoria_gruppo().getDs_categoria_gruppo());
				invDto.setImportoUnitario(movimento.getLottoMag().getCostoUnitario());
				invDto.setCdCds(movimento.getCdCdsLotto());
				invDto.setDataCaricoLotto(movimento.getLottoMag().getDtCarico());
			}
			// salvo la quantità inizio anno presa dal lotto del movimento di chiusura
			invDto.setQtyInizioAnno(movimento.getLottoMag().getQuantitaInizioAnno());
			invDto.setValoreInizioAnno(movimento.getPrezzoUnitario());

			lottiMap.put(invKeyDaMovimento,invDto);

		}
	}

	private void aggiornaMappaLottiConCarichiScarichiPeriodo(List<MovimentiMagBulk>  movimenti,Map<StampaInventarioDTOKey,StampaInventarioDTO> lottiMap){

		for(MovimentiMagBulk movimento : movimenti){
			StampaInventarioDTOKey invKey = new StampaInventarioDTOKey(movimento.getLottoMag().getCdCds(),movimento.getLottoMag().getEsercizio(),movimento.getLottoMag().getPgLotto(),movimento.getLottoMag().getCdMagazzino(),
					movimento.getLottoMag().getBeneServizio().getCd_categoria_gruppo(),movimento.getLottoMag().getCdBeneServizio(),movimento.getLottoMag().getCdNumeratoreMag());
			StampaInventarioDTO invDto = lottiMap.get(invKey);
			if(invDto==null){
				StampaInventarioDTO inv  =new StampaInventarioDTO();
				inv.setCd_magazzino(movimento.getLottoMag().getCdMagazzino());
				inv.setDesc_magazzino(movimento.getLottoMag().getMagazzino().getDsMagazzino());
				inv.setCdMagRaggr(movimento.getLottoMag().getMagazzino().getCdRaggrMagazzinoRim());
				inv.setDescMagRaggr(movimento.getLottoMag().getMagazzino().getRaggrMagazzinoRim().getDsRaggrMagazzino());
				inv.setCod_articolo(movimento.getLottoMag().getCdBeneServizio());
				inv.setGiacenza(new BigDecimal(0));
				inv.setAnnoLotto(movimento.getLottoMag().getEsercizio());
				inv.setTipoLotto(movimento.getLottoMag().getCdNumeratoreMag());
				inv.setNumeroLotto(movimento.getLottoMag().getPgLotto());
				inv.setCategoriaGruppo(movimento.getLottoMag().getBeneServizio().getCd_categoria_gruppo());
				inv.setDescArticolo(movimento.getLottoMag().getBeneServizio().getDs_bene_servizio());
				inv.setCod_categoria(movimento.getLottoMag().getBeneServizio().getCategoria_gruppo().getCd_categoria_padre());
				inv.setCod_gruppo(movimento.getLottoMag().getBeneServizio().getCategoria_gruppo().getCd_proprio());
				inv.setUm(movimento.getLottoMag().getBeneServizio().getUnitaMisura().getCdUnitaMisura());
				inv.setDescCatGrp(movimento.getLottoMag().getBeneServizio().getCategoria_gruppo().getDs_categoria_gruppo());
				inv.setImportoUnitario(movimento.getLottoMag().getCostoUnitario());
				inv.setCdCds(movimento.getCdCdsLotto());
				lottiMap.put(invKey,inv);
				invDto = lottiMap.get(invKey);
			}
			if(movimento.getTipoMovimentoMag().getModAggQtaMagazzino().equals(TipoMovimentoMagBulk.AZIONE_SOTTRAE)){
				invDto.setQtyScaricata(movimento.getQuantita());
				invDto.setImportoUnitarioScarico(movimento.getPrezzoUnitario());
			}
			if(movimento.getTipoMovimentoMag().getModAggQtaMagazzino().equals(TipoMovimentoMagBulk.AZIONE_SOMMA)){
				invDto.setQtyCaricata(movimento.getQuantita());
				invDto.setImportoUnitarioCarico(movimento.getPrezzoUnitario());
			}
			lottiMap.put(invKey,invDto);
		}
	}

	private void aggiornaMappaLottiConGiacenzeCorrette(List<MovimentiMagBulk>  movimenti,Map<StampaInventarioDTOKey,StampaInventarioDTO> lottiMap){

		for(MovimentiMagBulk movimento : movimenti){
			StampaInventarioDTOKey invKey = new StampaInventarioDTOKey(movimento.getLottoMag().getCdCds(),movimento.getLottoMag().getEsercizio(),movimento.getLottoMag().getPgLotto(),movimento.getLottoMag().getCdMagazzino(),
					movimento.getLottoMag().getBeneServizio().getCd_categoria_gruppo(),movimento.getLottoMag().getCdBeneServizio(),movimento.getLottoMag().getCdNumeratoreMag());
			StampaInventarioDTO invDto = lottiMap.get(invKey);
			if(invDto==null){
				StampaInventarioDTO inv  =new StampaInventarioDTO();
				inv.setCd_magazzino(movimento.getLottoMag().getCdMagazzino());
				inv.setDesc_magazzino(movimento.getLottoMag().getMagazzino().getDsMagazzino());
				inv.setCdMagRaggr(movimento.getLottoMag().getMagazzino().getCdRaggrMagazzinoRim());
				inv.setDescMagRaggr(movimento.getLottoMag().getMagazzino().getRaggrMagazzinoRim().getDsRaggrMagazzino());
				inv.setCod_articolo(movimento.getLottoMag().getCdBeneServizio());
				inv.setGiacenza(new BigDecimal(0));
				inv.setAnnoLotto(movimento.getLottoMag().getEsercizio());
				inv.setTipoLotto(movimento.getLottoMag().getCdNumeratoreMag());
				inv.setNumeroLotto(movimento.getLottoMag().getPgLotto());
				inv.setCategoriaGruppo(movimento.getLottoMag().getBeneServizio().getCd_categoria_gruppo());
				inv.setDescArticolo(movimento.getLottoMag().getBeneServizio().getDs_bene_servizio());
				inv.setCod_categoria(movimento.getLottoMag().getBeneServizio().getCategoria_gruppo().getCd_categoria_padre());
				inv.setCod_gruppo(movimento.getLottoMag().getBeneServizio().getCategoria_gruppo().getCd_proprio());
				inv.setUm(movimento.getLottoMag().getBeneServizio().getUnitaMisura().getCdUnitaMisura());
				inv.setDescCatGrp(movimento.getLottoMag().getBeneServizio().getCategoria_gruppo().getDs_categoria_gruppo());
				inv.setImportoUnitario(movimento.getLottoMag().getCostoUnitario());
				inv.setCdCds(movimento.getCdCdsLotto());
				lottiMap.put(invKey,inv);
				invDto = lottiMap.get(invKey);
			}
			if(movimento.getTipoMovimentoMag().getModAggQtaMagazzino().equals(TipoMovimentoMagBulk.AZIONE_SOTTRAE)){
				invDto.setGiacenza(invDto.getGiacenza().add(movimento.getQuantitaEffettiva()));
			}
			if(movimento.getTipoMovimentoMag().getModAggQtaMagazzino().equals(TipoMovimentoMagBulk.AZIONE_SOMMA)){
				invDto.setGiacenza(invDto.getGiacenza().subtract(movimento.getQuantitaEffettiva()));
			}

			lottiMap.put(invKey,invDto);
		}
	}
}

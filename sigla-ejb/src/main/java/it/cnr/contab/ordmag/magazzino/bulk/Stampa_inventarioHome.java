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

import it.cnr.contab.util.Utility;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkHome;
import it.cnr.jada.bulk.BulkList;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.sql.FindClause;
import it.cnr.jada.persistency.sql.LoggableStatement;
import it.cnr.jada.persistency.sql.SQLBuilder;
import it.cnr.jada.util.DateUtils;
import it.cnr.si.service.dto.anagrafica.letture.PersonaEntitaOrganizzativaWebDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Insert the type's description here.
 * Creation date: (23/01/2003 16.03.39)
 * @author: Roberto Fantino
 */
public class Stampa_inventarioHome extends BulkHome {
	/**
	 * Stampa_consumiHome constructor comment.
	 * @param clazz java.lang.Class
	 * @param conn java.sql.Connection
	 */
	private transient static final Logger _log = LoggerFactory.getLogger(Stampa_inventarioHome.class);
	public Stampa_inventarioHome(Class clazz, java.sql.Connection conn) {
		super(clazz, conn);
	}
	/**
	 * Stampa_consumiHome constructor comment.
	 * @param clazz java.lang.Class
	 * @param conn java.sql.Connection
	 * @param persistentCache it.cnr.jada.persistency.PersistentCache
	 */
	public Stampa_inventarioHome(Class clazz, java.sql.Connection conn, it.cnr.jada.persistency.PersistentCache persistentCache) {
		super(clazz, conn, persistentCache);
	}
	/**
	 * Stampa_consumiHome constructor comment.
	 * @param conn java.sql.Connection
	 */
	public Stampa_inventarioHome(java.sql.Connection conn) {
		super(Stampa_consumiBulk.class, conn);
	}
	/**
	 * Stampa_consumiHome constructor comment.
	 * @param conn java.sql.Connection
	 * @param persistentCache it.cnr.jada.persistency.PersistentCache
	 */
	public Stampa_inventarioHome(java.sql.Connection conn, it.cnr.jada.persistency.PersistentCache persistentCache) {
		super(Stampa_inventarioBulk.class, conn, persistentCache);
	}

	private static final String DATA_INVENTARIO="dataInventario";
	private static final String COD_MAGAZZINO = "cdMagazzino";
	private static final String CATEGORIA_GRUPPO = "cdCatGrp";
	private static final String ORDINAMENTO = "ordinamento";




	public String createJsonForPrint(Object object) throws ComponentException {
		ObjectMapper mapper = new ObjectMapper();
		String myJson = null;
		try {
			myJson = mapper.writeValueAsString(object);
		} catch (Exception ex) {
			throw new ComponentException("Errore nella generazione del file JSON per l'esecuzione della stampa ( errore joson).",ex);
		}
		return myJson;
	}

	public String getJsonDataSource(UserContext uc, Print_spoolerBulk print_spoolerBulk)  {
		BulkList<Print_spooler_paramBulk> params= print_spoolerBulk.getParams();
		Print_spooler_paramBulk dataInventario=params.stream().
				filter(e->e.getNomeParam().equals(DATA_INVENTARIO)).findFirst().get();

		Print_spooler_paramBulk codMagazzinoParam=params.stream().
				filter(e->e.getNomeParam().equals(COD_MAGAZZINO)).findFirst().get();

		Print_spooler_paramBulk catGruppoParam=params.stream().
				filter(e->e.getNomeParam().equals(CATEGORIA_GRUPPO)).findFirst().get();

		Print_spooler_paramBulk ordinamentoParam=params.stream().
				filter(e->e.getNomeParam().equals(ORDINAMENTO)).findFirst().get();

		Date dt = null;
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd");
		try {
			dt =dateFormatter.parse(dataInventario.getValoreParam());

		} catch (ParseException e) {
			e.printStackTrace();
		}
		String codMag = null;
		if(codMagazzinoParam != null){
			codMag = codMagazzinoParam.getValoreParam();
		}
		String catGruppo = null;
		if(catGruppoParam != null){
			catGruppo = catGruppoParam.getValoreParam();
		}

		//LottoMagHome lottoMagHome  = ( LottoMagHome)getHomeCache().getHome(LottoMagBulk.class,null,"stampa_inventario");
		LottoMagHome lottoMagHome  = ( LottoMagHome)getHomeCache().getHome(LottoMagBulk.class);
		SQLBuilder sql = lottoMagHome.createSQLBuilder();
		sql.addColumn("BENE_SERVIZIO.DS_BENE_SERVIZIO");
		//sql.addTableToHeader("BENE_SERVIZIO");
		sql.generateJoin(LottoMagBulk.class, Bene_servizioBulk.class, "beneServizio", "BENE_SERVIZIO");
		//sql.addSQLJoin("BENE_SERVIZIO.cd_bene_servizio","LOTTO_MAG.cd_bene_servizio");
		sql.addTableToHeader("MAGAZZINO","m");
		sql.addSQLJoin("m.cd_cds","LOTTO_MAG.cd_cds_mag");
		sql.addSQLJoin("m.cd_magazzino","LOTTO_MAG.cd_magazzino_mag");
		sql.addTableToHeader("Categoria_Gruppo_Invent","c");
		sql.addSQLJoin("c.cd_categoria_gruppo","BENE_SERVIZIO.cd_categoria_gruppo(+)");
		sql.addSQLClause(FindClause.AND,"LOTTO_MAG.DT_CARICO",SQLBuilder.LESS_EQUALS, new Timestamp(dt.getTime()));
		sql.addSQLClause(FindClause.AND,"LOTTO_MAG.GIACENZA",SQLBuilder.GREATER, 0);
		// codice magazzino uguale a quello in input
		sql.addSQLClause(FindClause.AND,"LOTTO_MAG.CD_MAGAZZINO_MAG",SQLBuilder.EQUALS, codMag);
		if(catGruppo != null && !catGruppo.equals(Valori_magazzinoBulk.TUTTI)){
			sql.addTableToHeader("CATEGORIA_GRUPPO_INVENT","CATEGORIA_GRUPPO_INVENT");
			sql.addSQLJoin("CATEGORIA_GRUPPO_INVENT.CD_CATEGORIA_GRUPPO","BENE_SERVIZIO.CD_CATEGORIA_GRUPPO");
			//categoria gruppo uguale a quella in input
			sql.addSQLClause(FindClause.AND,"CATEGORIA_GRUPPO_INVENT.CD_CATEGORIA_GRUPPO",SQLBuilder.EQUALS, catGruppo);
		}
		List<StampaInventarioDTO> inventario=null;
		Map<StampaInventarioDTOKey,StampaInventarioDTO> stampaInvMap= new HashMap<StampaInventarioDTOKey,StampaInventarioDTO>();

		try {
			List<LottoMagBulk> lotti=lottoMagHome.fetchAll(sql);
			getHomeCache().fetchAll(uc);

			for ( LottoMagBulk m:lotti){

				StampaInventarioDTO inv  =new StampaInventarioDTO();
				inv.setCd_magazzino(m.getCdMagazzino());
				inv.setDesc_magazzino(m.getMagazzino().getDsMagazzino());
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

				StampaInventarioDTOKey invKey = new StampaInventarioDTOKey(inv.getCdCds(),inv.getAnnoLotto(),inv.getNumeroLotto(),inv.getCd_magazzino(),inv.getCategoriaGruppo(),inv.getCod_articolo(),inv.getTipoLotto());
				stampaInvMap.put(invKey,inv);
			}

			MovimentiMagHome movimentoMagHome = (MovimentiMagHome)getHomeCache().getHome(MovimentiMagBulk.class);

			sql = movimentoMagHome.createSQLBuilder();
			sql.generateJoin(MovimentiMagBulk.class, LottoMagBulk.class, "lottoMag", "LOTTO_MAG");
			sql.generateJoin(MovimentiMagBulk.class, TipoMovimentoMagBulk.class, "tipoMovimentoMag","TIPO_MOVIMENTO_MAG");
			sql.addTableToHeader("BENE_SERVIZIO","BENE_SERVIZIO");
			sql.addSQLJoin("BENE_SERVIZIO.CD_BENE_SERVIZIO","LOTTO_MAG.CD_BENE_SERVIZIO");
			// tipo movimento != CHIUSURE (CH)
			sql.addSQLClause(FindClause.AND,"TIPO_MOVIMENTO_MAG.TIPO",SQLBuilder.NOT_EQUALS, TipoMovimentoMagBulk.CHIUSURE);
			// stato movimento = STATO_INSERITO (INS)
			sql.addSQLClause(FindClause.AND,"MOVIMENTI_MAG.STATO",SQLBuilder.EQUALS, MovimentiMagBulk.STATO_INSERITO);
			// data riferimento maggiore/uguale della data in input
			sql.addSQLClause(FindClause.AND,"MOVIMENTI_MAG.DT_RIFERIMENTO",SQLBuilder.GREATER, new Timestamp(dt.getTime()));
			// codice magazzino uguale a quello in input
			sql.addSQLClause(FindClause.AND,"LOTTO_MAG.CD_MAGAZZINO_MAG",SQLBuilder.EQUALS, codMag);
			// data carico lotto minore/uguale della data in input
			sql.addSQLClause(FindClause.AND,"LOTTO_MAG.DT_CARICO",SQLBuilder.LESS_EQUALS, new Timestamp(dt.getTime()));
			if(catGruppo != null && !catGruppo.equals(Valori_magazzinoBulk.TUTTI)){
				sql.addTableToHeader("CATEGORIA_GRUPPO_INVENT","CATEGORIA_GRUPPO_INVENT");
				sql.addSQLJoin("CATEGORIA_GRUPPO_INVENT.CD_CATEGORIA_GRUPPO","BENE_SERVIZIO.CD_CATEGORIA_GRUPPO");
				//categoria gruppo uguale a quella in input
				sql.addSQLClause(FindClause.AND,"CATEGORIA_GRUPPO_INVENT.CD_CATEGORIA_GRUPPO",SQLBuilder.EQUALS, catGruppo);
			}

			List<MovimentiMagBulk> movimenti=movimentoMagHome.fetchAll(sql);
			getHomeCache().fetchAll(uc);

			for(MovimentiMagBulk movimento : movimenti){
				StampaInventarioDTOKey invKey = new StampaInventarioDTOKey(movimento.getLottoMag().getCdCds(),movimento.getLottoMag().getEsercizio(),movimento.getLottoMag().getPgLotto(),movimento.getLottoMag().getCdMagazzino(),
						                                                   movimento.getLottoMag().getBeneServizio().getCd_categoria_gruppo(),movimento.getLottoMag().getCdBeneServizio(),movimento.getLottoMag().getCdNumeratoreMag());
				StampaInventarioDTO invDto = stampaInvMap.get(invKey);
				if(invDto==null){
					StampaInventarioDTO inv  =new StampaInventarioDTO();
					inv.setCd_magazzino(movimento.getLottoMag().getCdMagazzino());
					inv.setDesc_magazzino(movimento.getLottoMag().getMagazzino().getDsMagazzino());
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
					stampaInvMap.put(invKey,inv);
					invDto = stampaInvMap.get(invKey);
				}
				if(movimento.getTipoMovimentoMag().getModAggQtaMagazzino().equals(TipoMovimentoMagBulk.AZIONE_SOTTRAE)){
					invDto.setGiacenza(invDto.getGiacenza().add(movimento.getQuantitaEffettiva()));
				}
				if(movimento.getTipoMovimentoMag().getModAggQtaMagazzino().equals(TipoMovimentoMagBulk.AZIONE_SOMMA)){
					invDto.setGiacenza(invDto.getGiacenza().subtract(movimento.getQuantitaEffettiva()));
				}
				stampaInvMap.put(invKey,invDto);
			}
			inventario = stampaInvMap.values().stream().collect(Collectors.toList());

			if(ordinamentoParam!=null){

				if(ordinamentoParam.getValoreParam().equals(Stampa_inventarioBulk.ORD_CODICE)){
					inventario = inventario.stream().sorted(Comparator.comparing(StampaInventarioDTO::getCod_articolo)).collect(Collectors.toList());
				}
				if(ordinamentoParam.getValoreParam().equals(Stampa_inventarioBulk.ORD_DENOMINAZIONE)){
					inventario = inventario.stream().sorted(Comparator.comparing(StampaInventarioDTO::getDescArticolo)).collect(Collectors.toList());
				}
			}
		} catch (PersistencyException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		String json=null;
		try {
			json=createJsonForPrint( inventario);
		} catch (ComponentException e) {
			e.printStackTrace();
		}
		return json;
	}
}

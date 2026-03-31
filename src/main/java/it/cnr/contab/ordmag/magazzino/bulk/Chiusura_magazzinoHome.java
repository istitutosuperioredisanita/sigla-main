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
import it.cnr.contab.ordmag.magazzino.dto.ImportiTotMagDTO;
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
import org.apache.pdfbox.pdmodel.font.encoding.MacExpertEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
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
public class Chiusura_magazzinoHome extends Valori_magazzinoHome {

	private transient static final Logger _log = LoggerFactory.getLogger(Chiusura_magazzinoHome.class);

	public Chiusura_magazzinoHome(Connection conn) {
		super(conn);
	}

	public Chiusura_magazzinoHome(Connection conn, PersistentCache persistentCache) {
		super(conn, persistentCache);
	}

	private static final String DATA_FINE_PERIODO="dataInventario";
	private static final String DATA_INIZIO_PERIODO="dataInventarioInizio";
	private static final String DATA_MOVIMENTO_CHIUSURA="dataChiusuraMovimento";
	private static final String CATEGORIA_GRUPPO = "cdCatGrp";
	private static final String ORDINAMENTO = "ordinamento";
	private static final String RAGGR_MAG = "cdRaggrMag";
	private static final String ESERCIZIO = "esercizio";
	private static final String TIPO_OPERAZIONE = "tipoOperazione";
	private static final String TIPO_VALORIZZAZIONE = "tipoValorizzazione";




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
		Print_spooler_paramBulk dataFinePeriodo=params.stream().
				filter(e->e.getNomeParam().equals(DATA_FINE_PERIODO)).findFirst().get();

		Print_spooler_paramBulk dataInizioPeriodo=params.stream().
				filter(e->e.getNomeParam().equals(DATA_INIZIO_PERIODO)).findFirst().get();

		Print_spooler_paramBulk dataMovimentoChiusuraParam=params.stream().
				filter(e->e.getNomeParam().equals(DATA_MOVIMENTO_CHIUSURA)).findFirst().get();

		Print_spooler_paramBulk codRaggMagazzinoParam=params.stream().
				filter(e->e.getNomeParam().equals(RAGGR_MAG)).findFirst().get();

		Print_spooler_paramBulk catGruppoParam=params.stream().
				filter(e->e.getNomeParam().equals(CATEGORIA_GRUPPO)).findFirst().get();

		Print_spooler_paramBulk ordinamentoParam=params.stream().
				filter(e->e.getNomeParam().equals(ORDINAMENTO)).findFirst().get();

		Print_spooler_paramBulk esercizioParam=params.stream().
				filter(e->e.getNomeParam().equals(ESERCIZIO)).findFirst().get();

		Print_spooler_paramBulk tipoOperazioneParam=params.stream().
				filter(e->e.getNomeParam().equals(TIPO_OPERAZIONE)).findFirst().get();

		Print_spooler_paramBulk tipoValorizzazioneParam=params.stream().
				filter(e->e.getNomeParam().equals(TIPO_VALORIZZAZIONE)).findFirst().get();



		Integer esercizio=null;
		if(esercizioParam !=null){
			esercizio = Integer.valueOf(esercizioParam.getValoreParam());
		}

		Date dataFine = null;
		Date dataInizio = null;
		Date dataMovimentoChiusura=null;
		SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd");
		try {
			dataFine =dateFormatter.parse(dataFinePeriodo.getValoreParam());
			dataInizio =dateFormatter.parse(dataInizioPeriodo.getValoreParam());
			dataMovimentoChiusura=dateFormatter.parse(dataMovimentoChiusuraParam.getValoreParam());

		} catch (ParseException e) {
			e.printStackTrace();
		}
		String codRaggrMag = null;
		if(codRaggMagazzinoParam != null){
			codRaggrMag = codRaggMagazzinoParam.getValoreParam();
		}
		String catGruppo = null;
		if(catGruppoParam != null){
			catGruppo = catGruppoParam.getValoreParam();
		}

		List<StampaInventarioDTO> inventario=null;

		try{

			ChiusuraAnnoMagRimHome chiusuraAnnoMagRimHome  = ( ChiusuraAnnoMagRimHome)getHomeCache().getHome(ChiusuraAnnoMagRimBulk.class);
			List<ChiusuraAnnoMagRimBulk> ChiusuraAnnoMagRimList = chiusuraAnnoMagRimHome.getCalcoliChiusuraAnno(uc,esercizio,dataFine,dataInizio,codRaggrMag,catGruppo);



			if(ordinamentoParam!=null){

				if(ordinamentoParam.getValoreParam().equals(Stampa_inventarioBulk.ORD_CODICE)){
					inventario = inventario.stream().sorted(Comparator.comparing(StampaInventarioDTO::getCod_articolo)).collect(Collectors.toList());
				}
				if(ordinamentoParam.getValoreParam().equals(Stampa_inventarioBulk.ORD_DENOMINAZIONE)){
					inventario = inventario.stream().sorted(Comparator.comparing(StampaInventarioDTO::getDescArticolo)).collect(Collectors.toList());
				}
			}
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

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

/*
 * Created on Sep 28, 2005
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package it.cnr.contab.util;

import it.cnr.contab.anagraf00.ejb.TerzoComponentSession;
import it.cnr.contab.bollo00.ejb.AttoBolloComponentSession;
import it.cnr.contab.bollo00.ejb.TipoAttoBolloComponentSession;
import it.cnr.contab.client.docamm.FatturaAttiva;
import it.cnr.contab.coepcoan00.ejb.*;
import it.cnr.contab.compensi00.ejb.CompensoComponentSession;
import it.cnr.contab.config00.ejb.*;
import it.cnr.contab.docamm00.ejb.AutoFatturaComponentSession;
import it.cnr.contab.docamm00.ejb.DocumentoGenericoComponentSession;
import it.cnr.contab.docamm00.ejb.FatturaAttivaSingolaComponentSession;
import it.cnr.contab.docamm00.ejb.FatturaPassivaComponentSession;
import it.cnr.contab.doccont00.comp.AsyncConsSostitutivaComponentSession;
import it.cnr.contab.doccont00.comp.AsyncPluriennaliComponentSession;
import it.cnr.contab.doccont00.ejb.*;
import it.cnr.contab.gestiva00.ejb.LiquidIvaInterfComponentSession;
import it.cnr.contab.incarichi00.ejb.IncarichiEstrazioneFpComponentSession;
import it.cnr.contab.incarichi00.ejb.IncarichiProceduraComponentSession;
import it.cnr.contab.incarichi00.ejb.IncarichiRepertorioComponentSession;
import it.cnr.contab.incarichi00.ejb.RepertorioLimitiComponentSession;
import it.cnr.contab.inventario00.ejb.*;
import it.cnr.contab.ordmag.ejb.NumeratoriOrdMagComponentSession;
import it.cnr.contab.ordmag.magazzino.ejb.ChiusuraAnnoComponentSession;
import it.cnr.contab.ordmag.magazzino.ejb.MovimentiMagComponentSession;
import it.cnr.contab.ordmag.magazzino.ejb.TransitoBeniOrdiniComponentSession;
import it.cnr.contab.ordmag.ordini.ejb.OrdineAcqComponentSession;
import it.cnr.contab.pdg00.ejb.PdGVariazioniComponentSession;
import it.cnr.contab.pdg01.ejb.CRUDPdgVariazioneGestionaleComponentSession;
import it.cnr.contab.pdg01.ejb.CRUDPdgVariazioneRigaGestComponentSession;
import it.cnr.contab.prevent01.ejb.PdgAggregatoModuloComponentSession;
import it.cnr.contab.prevent01.ejb.PdgContrSpeseComponentSession;
import it.cnr.contab.progettiric00.ejb.ProgettoRicercaComponentSession;
import it.cnr.contab.progettiric00.ejb.RimodulaProgettoRicercaComponentSession;
import it.cnr.contab.progettiric00.ejb.geco.ProgettoGecoComponentSession;
import it.cnr.contab.utente00.ejb.RuoloComponentSession;
import it.cnr.contab.utente00.ejb.UtenteComponentSession;
import it.cnr.contab.varstanz00.ejb.VariazioniStanziamentoResiduoComponentSession;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.ejb.AdminSession;
import it.cnr.jada.ejb.CRUDComponentSession;
import it.cnr.jada.util.ejb.EJBCommonServices;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;

import javax.ejb.EJBException;
import javax.servlet.ServletException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author mspasiano
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public final class Utility {
	private transient static final Logger logger = LoggerFactory.getLogger(Utility.class);

	public static final String MANIFEST_PATH = "/META-INF/MANIFEST.MF";
	public static final String APPLICATION_TITLE = "SIGLA - Sistema Informativo per la Gestione delle Linee di Attività";
	public static final java.math.BigDecimal ZERO = new java.math.BigDecimal(0);
	public static final java.math.BigDecimal SCOSTAMENTO_MAX = new java.math.BigDecimal(0.05);
    public static final String CSS_CLASS_W_10 = "w-10";
    public static String TIPO_GESTIONE_SPESA = "S";
	public static String TIPO_GESTIONE_ENTRATA = "E";
	public static final BigDecimal CENTO = new BigDecimal(100);
	/**
	 * Restituisce true se i due oggetti sono uguali o sono entrambi null
	 * false altrimenti
	 */
	public static boolean equalsNull(Object object1, Object object2){
		if (object1 == null && object2 == null)
			return true;
		else if ((object1 == null && object2 != null)||(object1 != null && object2 == null))
			return false;
		else if (object1 != null && object2 != null)
			return object1.equals(object2);
		return false;
	}
	/**
	 * Restituisce true se i due oggetti sono uguali o sono entrambi null
	 * false altrimenti
	 */
	public static boolean equalsBulkNull(OggettoBulk object1, OggettoBulk object2){
		if (object1 == null && object2 == null)
			return true;
		else if ((object1 == null && object2 != null)||(object1 != null && object2 == null))
			return false;
		else if (object1 != null && object2 != null)
			return object1.equalsByPrimaryKey(object2);
		return false;
	}

	public static BigDecimal nvl(BigDecimal imp, BigDecimal otherImp){
		if (imp != null)
			return imp;
		return otherImp;
	}

	public static BigDecimal nvl(BigDecimal imp){
		if (imp != null)
			return imp;
		return ZERO;
	}

	public static <T extends Comparable<T>> boolean isBetween(T value, T start, T end) {
		return value.compareTo(start) >= 0 && value.compareTo(end) <= 0;
	}

	public static String getSiglaVersion(){
		String version  = "01.001.000";
		InputStream is = FatturaAttiva.class.getResourceAsStream(Utility.MANIFEST_PATH);
		if (is != null) {
			try {
				Manifest manifest = new Manifest(is);
				Attributes attributes = manifest.getMainAttributes();

				version = attributes.getValue("Implementation-Version");
			} catch (IOException e) {
				logger.warn("IOException", e);
			}
		}
		return version;
	}

	/**
	 * Restituisce una Stringa ottenuta sostituendo
	 * nella stringa sorgente alla stringa pattern la stringa replace,
	 * se la stringa pattern non è presente restituisce la stringa sorgente
	 */
	public static String replace(String source, String pattern, String replace)
	{
		if (source!=null){
			final int len = pattern.length();
			StringBuffer sb = new StringBuffer();
			int found = -1;
			int start = 0;

			while( (found = source.indexOf(pattern, start) ) != -1) {
				sb.append(source.substring(start, found));
				sb.append(replace);
				start = found + len;
			}

			sb.append(source.substring(start));
			return sb.toString();
		}
		else
			return null;
	}


	public static String lpad(double d, int size, char pad) {
		return lpad(Double.toString(d), size, pad);
	}

	public static String lpad(long l, int size, char pad) {
		return lpad(Long.toString(l), size, pad);
	}

	public static String lpad(String s, int size, char pad) {
		StringBuilder builder = new StringBuilder();
		while (builder.length() + s.length() < size) {
			builder.append(pad);
		}
		builder.append(s);
		return builder.toString();
	}
	public static BigDecimal round2Decimali(BigDecimal importo) {
		return round(importo,2);
	}

	public static BigDecimal round6Decimali(BigDecimal importo) {
		return round(importo,6);
	}

	public static BigDecimal round5Decimali(BigDecimal importo) {
		return round(importo,5);
	}

	public static BigDecimal roundIntero(BigDecimal importo) {
		return round(importo,0);
	}

	public static BigDecimal round(BigDecimal importo, int scale) {
		importo = nvl(importo);
		return importo.setScale(scale,RoundingMode.HALF_UP);
	}

	public static BigDecimal trunc(BigDecimal importo, int scale) {
		importo = nvl(importo);
		return importo.setScale(scale,RoundingMode.DOWN);
	}

	public static BigDecimal ceil(BigDecimal importo, int scale) {
		importo = nvl(importo);
		return importo.setScale(scale,RoundingMode.UP);
	}


	public static BigDecimal trunc2Decimali(BigDecimal importo) {
		return trunc(nvl(importo),2);
	}

	public static BigDecimal truncIntero(BigDecimal importo) {
		return trunc(nvl(importo),0);
	}

	/**
	 * Metodo che effetua una divisione, il cui risultato viene fornito con 2
	 * cifre decimali ed arrotondamento per difetto o per eccesso
	 *
	 * @param dividendo
	 *            Dividendo
	 * @param divisore
	 *            Divisore
	 * @return il risultato della divisione
	 */

	public static BigDecimal divide(BigDecimal dividendo, BigDecimal divisore) {
		return dividendo.divide(divisore,2, RoundingMode.HALF_UP);
	}

	public static BigDecimal divide(BigDecimal dividendo, Integer divisore) {
		return dividendo.divide(new BigDecimal(divisore),2,RoundingMode.HALF_UP);
	}

	public static BigDecimal divide(BigDecimal dividendo, BigDecimal divisore, Integer arrotondamento) {
		return dividendo.divide(divisore,arrotondamento,RoundingMode.HALF_UP);
	}


	public static String NumberToText(int n) {
		// metodo wrapper
		if (n == 0) {
			return "zero";
		} else {
			return NumberToTextRicorsiva(n);
		}
	}

	public static Collection<? extends List<?>> splitListBySize(List<?> intList, int size) {
		if (!intList.isEmpty() && size > 0) {
			final AtomicInteger counter = new AtomicInteger(0);
			return intList.stream().collect(Collectors.groupingBy(it -> counter.getAndIncrement() / size)).values();
		}
		return null;
	}

	public static void main(String[] args) {
		System.out.println(NumberToText(new BigDecimal("16754")));
	}

	public static String NumberToText(BigDecimal importo) {
		int parteIntera = importo.intValue();
		String parteDecimale = importo.remainder(BigDecimal.ONE).abs().toPlainString();
		if (parteDecimale.length() > 1)
			parteDecimale = parteDecimale.substring(2);
		if (parteIntera == 0) {
			return "zero/" + parteDecimale;
		} else {
			return NumberToTextRicorsiva(parteIntera) + "/" + parteDecimale;
		}
	}
	public static String NumberToTextPadDecimal(BigDecimal importo) {
		int parteIntera = importo.intValue();
		String parteDecimale = importo.remainder(BigDecimal.ONE).abs().toPlainString();
		if (parteDecimale.length() > 1)
			parteDecimale = parteDecimale.substring(2);
		if (parteIntera == 0) {
			return "zero/" + parteDecimale;
		} else {
			return NumberToTextRicorsiva(parteIntera) + "/" + StringUtils.rightPad(parteDecimale, 2, "0");
		}
	}

	public static Timestamp addDays(Timestamp timestamp, int days) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(timestamp);
		cal.add(Calendar.DAY_OF_YEAR, days);
		return new Timestamp(cal.getTime().getTime());
	}

	public static synchronized void loadPersistentInfos() throws ServletException{
		ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(true);
		provider.addIncludeFilter(new AssignableTypeFilter(OggettoBulk.class));
		AdminSession adminSession = (AdminSession) EJBCommonServices.createEJB("JADAEJB_AdminSession");
		Set<BeanDefinition> components = provider.findCandidateComponents("it/cnr");
		logger.info("Load PersistentInfo start, total number of classes is {}", components.size());
		for (BeanDefinition component : components){
			try {
				Class<?> clazz = Class.forName(component.getBeanClassName());
				logger.debug("Load PersistentInfo for class: {}",clazz.getName());
				adminSession.loadPersistentInfos(clazz);
				if(clazz.getName().endsWith("Bulk")) {
					logger.debug("Load BulkInfo for class: {}",clazz.getName());
					adminSession.loadBulkInfos(clazz);
				}
			} catch (Exception e) {
				logger.error("Cannot load persistentInfo for class : {}", component.getBeanClassName(), e);
			}
		}
		logger.info("Load PersistentInfo finish");
	}

	private static String NumberToTextRicorsiva(int n) {
		if (n < 0) {
			return "meno " + NumberToTextRicorsiva(-n);
		} else if (n == 0){
			return "";
		} else if (n <= 19){
			return new String[] { "uno", "due", "tre", "quattro", "cinque",
					"sei", "sette", "otto", "nove", "dieci",
					"undici", "dodici", "tredici",
					"quattordici", "quindici", "sedici",
					"diciassette", "diciotto", "diciannove" }[n-1];
		} else if (n <= 99) {
			String[] vettore =
					{ "venti", "trenta", "quaranta", "cinquanta", "sessanta",
							"settanta", "ottanta", "novanta" };
			String letter = vettore[n / 10 - 2];
			int t = n % 10; // t è la prima cifra di n
			// se è 1 o 8 va tolta la vocale finale di letter
			if (t == 1 || t == 8 ) {
				letter = letter.substring(0, letter.length() - 1);
			}
			return letter + NumberToTextRicorsiva(n % 10);
		} else if (n <= 199){
			return "cento" + NumberToTextRicorsiva(n % 100);
		} else if (n <= 999){
			int m = n % 100;
			m /= 10; // divisione intera per 10 della variabile
			String letter = "cent";
			if (m != 8){
				letter = letter + "o";
			}
			return NumberToTextRicorsiva(n / 100) + letter +
					NumberToTextRicorsiva(n % 100);
		}
		else if (n <= 1999){
			return "mille" + NumberToTextRicorsiva(n % 1000);
		}  else if (n <= 999999){
			return NumberToTextRicorsiva(n / 1000) + "mila" +
					NumberToTextRicorsiva(n % 1000);
		}
		else if (n <= 1999999){
			return "unmilione" + NumberToTextRicorsiva(n % 1000000);
		} else if (n <= 999999999){
			return NumberToTextRicorsiva(n / 1000000) + "milioni" +
					NumberToTextRicorsiva(n % 1000000);
		} else if (n <= 1999999999){
			return "unmiliardo" + NumberToTextRicorsiva(n % 1000000000);
		} else {
			return NumberToTextRicorsiva(n / 1000000000) + "miliardi" +
					NumberToTextRicorsiva(n % 1000000000);
		}
	}

	public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
		Map<Object, Boolean> map = new ConcurrentHashMap<>();
		return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}

	public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue( Map<K, V> map ) {
		Map<K, V> result = new LinkedHashMap<>();
		Stream<Map.Entry<K, V>> st = map.entrySet().stream();

		st.sorted( Map.Entry.comparingByValue() )
				.forEachOrdered( e -> result.put(e.getKey(), e.getValue()) );

		return result;
	}

	public static boolean isInteger(BigDecimal num) {
		return num.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0;
	}

	public static Map<OggettoBulk,Map<OggettoBulk,BigDecimal>> spalmaImporti(Map<OggettoBulk,BigDecimal> source, Map<OggettoBulk,BigDecimal> destination) throws ApplicationException {
		Map<OggettoBulk,Map<OggettoBulk,BigDecimal>> result = new HashMap<>();
		BigDecimal imTotSourceDett = source.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
		for (OggettoBulk destinationDett : destination.keySet()) {
			Map<OggettoBulk,BigDecimal> result2 = new HashMap<>();
			BigDecimal imDestinationDett = destination.get(destinationDett);

			for (OggettoBulk sourceDett : source.keySet()) {
				BigDecimal imSourceDett = source.get(sourceDett);
				BigDecimal imRipartitoSourceDett = result.values().stream()
						.map(el->el.get(sourceDett))
						.reduce(BigDecimal.ZERO, BigDecimal::add);
				BigDecimal imDaRipartireSourceDett = imSourceDett.subtract(imRipartitoSourceDett);
				BigDecimal imResult = imSourceDett.multiply(imDestinationDett)
						.divide(imTotSourceDett, 2, RoundingMode.HALF_UP);
				if (imResult.compareTo(imDaRipartireSourceDett)<0)
					result2.put(sourceDett,imResult);
				else
					result2.put(sourceDett,imDaRipartireSourceDett);
			}

			BigDecimal imRipartitoDestinationDett = result2.values()
					.stream()
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			BigDecimal diff = imDestinationDett.subtract(imRipartitoDestinationDett);

			if (diff.compareTo(BigDecimal.ZERO) < 0) {
				for (OggettoBulk obj : result2.keySet()) {
					BigDecimal importoRipartito = result2.get(obj);
					if (importoRipartito.compareTo(diff.abs()) >= 0) {
						result2.replace(obj,importoRipartito.subtract(diff.abs()));
						break;
					} else {
						diff = diff.subtract(importoRipartito);
						result2.replace(obj,BigDecimal.ZERO);
					}
				}
			} else if (diff.compareTo(BigDecimal.ZERO) > 0) {
				for (OggettoBulk obj : result2.keySet()) {
					BigDecimal importoRipartito = result2.get(obj);
					result2.replace(obj,importoRipartito.add(diff));
					break;
				}
			}
			result.put(destinationDett,result2);
		}
		//Verifica quadratura
		for (OggettoBulk sourceDett : source.keySet()) {
			BigDecimal imSourceDett = source.get(sourceDett);
			BigDecimal imRipartitoSourceDett = result.values().stream()
					.map(el -> el.get(sourceDett))
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			if (imSourceDett.compareTo(imRipartitoSourceDett) != 0)
				throw new it.cnr.jada.comp.ApplicationException("Operazione di spalma importi sulle consegne non andata a buon fine. Contattare il servizio assistenza!");
		}
		for (OggettoBulk destinationDett : destination.keySet()) {
			BigDecimal imDestinationDett = destination.get(destinationDett);
			BigDecimal imRipartitoDestinationDett = result.get(destinationDett)
					.values().stream()
					.reduce(BigDecimal.ZERO, BigDecimal::add);
			if (imDestinationDett.compareTo(imRipartitoDestinationDett) != 0)
				throw new it.cnr.jada.comp.ApplicationException("Operazione di spalma importi sulle consegne non andata a buon fine. Contattare il servizio assistenza!");
		}
		return result;
	}

	public static CRUDComponentSession createCRUDComponentSession() throws EJBException, RemoteException {
		return Optional.ofNullable(EJBCommonServices.createEJB("JADAEJB_CRUDComponentSession"))
				.filter(CRUDComponentSession.class::isInstance)
				.map(CRUDComponentSession.class::cast)
				.orElseThrow(() -> new RemoteException("Cannot find EJB with jndiName: JADAEJB_CRUDComponentSession"));
	}

	public static Parametri_cnrComponentSession createParametriCnrComponentSession()throws EJBException, RemoteException {
		return (Parametri_cnrComponentSession)EJBCommonServices.createEJB("CNRCONFIG00_EJB_Parametri_cnrComponentSession", Parametri_cnrComponentSession.class);
	}
	public static Classificazione_vociComponentSession createClassificazioneVociComponentSession() throws javax.ejb.EJBException,java.rmi.RemoteException {
		return (Classificazione_vociComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRCONFIG00_EJB_Classificazione_vociComponentSession",Classificazione_vociComponentSession.class);
	}
	/**
	 * Crea la ComponentSession da usare per effettuare le operazioni di lettura della Configurazione CNR
	 *
	 * @return Configurazione_cnrComponentSession l'istanza di <code>Configurazione_cnrComponentSession</code> che serve per leggere i parametri di configurazione del CNR
	 */
	public static Configurazione_cnrComponentSession createConfigurazioneCnrComponentSession() throws EJBException, RemoteException{
		return (Configurazione_cnrComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRCONFIG00_EJB_Configurazione_cnrComponentSession",Configurazione_cnrComponentSession.class);
	}
	/**
	 * Crea la
	 * da usare per effettuare le operazioni di CRUD
	 */
	public static SaldoComponentSession createSaldoComponentSession() throws EJBException{
		return (SaldoComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRDOCCONT00_EJB_SaldoComponentSession",SaldoComponentSession.class);
	}
	/**
	 * Crea la CdrComponentSession da usare per effettuare operazioni
	 */
	public static it.cnr.contab.config00.ejb.CDRComponentSession createCdrComponentSession() throws javax.ejb.EJBException{
		return (it.cnr.contab.config00.ejb.CDRComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRCONFIG00_EJB_CDRComponentSession", it.cnr.contab.config00.ejb.CDRComponentSession.class);
	}
	/**
	 * Crea la CdrComponentSession da usare per effettuare operazioni
	 */
	public static PdgAggregatoModuloComponentSession createPdgAggregatoModuloComponentSession() throws javax.ejb.EJBException {
		return (PdgAggregatoModuloComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRPREVENT01_EJB_PdgAggregatoModuloComponentSession", PdgAggregatoModuloComponentSession.class);
	}

	/**
	 * Crea la PdgContrSpeseComponentSession da usare per effettuare operazioni
	 */
	public static PdgContrSpeseComponentSession createPdgContrSpeseComponentSession() throws javax.ejb.EJBException{
		return (PdgContrSpeseComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRPREVENT01_EJB_PdgContrSpeseComponentSession", PdgContrSpeseComponentSession.class);
	}
	/**
	 * Crea la Local CdrComponentSession da usare per effettuare operazioni
	 */
	public static PdGVariazioniComponentSession createPdGVariazioniComponentSession() throws javax.ejb.EJBException{
		return (PdGVariazioniComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRPDG00_EJB_PdGVariazioniComponentSession", PdGVariazioniComponentSession.class);
	}
	/**
	 * Crea la Local MandatoComponentSession da usare per effettuare operazioni
	 */
	public static MandatoComponentSession createMandatoComponentSession() throws javax.ejb.EJBException{
		return (MandatoComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRDOCCONT00_EJB_MandatoComponentSession", MandatoComponentSession.class);
	}
	/**
	 * Crea la Local ReversaleComponentSession da usare per effettuare operazioni
	 */
	public static ReversaleComponentSession createReversaleComponentSession() throws javax.ejb.EJBException{
		return (ReversaleComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRDOCCONT00_EJB_ReversaleComponentSession", ReversaleComponentSession.class);
	}
	/**
	 * Crea la Local Unita_organizzativaComponentSession da usare per effettuare operazioni
	 */
	public static Unita_organizzativaComponentSession createUnita_organizzativaComponentSession() throws javax.ejb.EJBException{
		return (Unita_organizzativaComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRCONFIG00_EJB_Unita_organizzativaComponentSession", Unita_organizzativaComponentSession.class);
	}
	public static UtenteComponentSession createUtenteComponentSession() throws javax.ejb.EJBException{
		return (UtenteComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRUTENZE00_EJB_UtenteComponentSession", UtenteComponentSession.class);
	}
	/**
	 * Crea la Local TerzoComponentSession da usare per effettuare operazioni
	 */
	public static TerzoComponentSession createTerzoComponentSession() throws javax.ejb.EJBException{
		return (TerzoComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRANAGRAF00_EJB_TerzoComponentSession", TerzoComponentSession.class);
	}
	/**
	 * Crea la RepertorioLimitiComponentSession da usare per effettuare le operazioni di CRUD
	 */
	public static RepertorioLimitiComponentSession createRepertorioLimitiComponentSession() throws EJBException{
		return (RepertorioLimitiComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRINCARICHI00_EJB_RepertorioLimitiComponentSession",RepertorioLimitiComponentSession.class);
	}
	/**
	 * Crea la IncarichiRepertorioComponentSession da usare per effettuare le operazioni di CRUD
	 */
	public static IncarichiRepertorioComponentSession createIncarichiRepertorioComponentSession() throws EJBException{
		return (IncarichiRepertorioComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRINCARICHI00_EJB_IncarichiRepertorioComponentSession",IncarichiRepertorioComponentSession.class);
	}
	/**
	 * Crea la IncarichiProceduraComponentSession da usare per effettuare le operazioni di CRUD
	 */
	public static IncarichiProceduraComponentSession createIncarichiProceduraComponentSession() throws EJBException{
		return (IncarichiProceduraComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRINCARICHI00_EJB_IncarichiProceduraComponentSession",IncarichiProceduraComponentSession.class);
	}
	/**
	 * Crea la Remote ProgettoGecoComponentSession da usare per effettuare operazioni
	 */
	public static ProgettoGecoComponentSession createProgettoGecoComponentSession() throws javax.ejb.EJBException{
		return (ProgettoGecoComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRPROGETTIRIC00_EJB_GECO_ProgettoGecoComponentSession");
	}
	public static Parametri_cdsComponentSession createParametriCdsComponentSession()throws EJBException, RemoteException {
		return (Parametri_cdsComponentSession)EJBCommonServices.createEJB("CNRCONFIG00_EJB_Parametri_cdsComponentSession", Parametri_cdsComponentSession.class);
	}
	public static it.cnr.contab.anagraf00.ejb.AbiCabComponentSession createAbiCabComponentSession() throws javax.ejb.EJBException,java.rmi.RemoteException {
		return (it.cnr.contab.anagraf00.ejb.AbiCabComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRANAGRAF00_EJB_AbiCabComponentSession", it.cnr.contab.anagraf00.ejb.AbiCabComponentSession.class);
	}
	public static Parametri_enteComponentSession createParametriEnteComponentSession()throws EJBException, RemoteException {
		return (Parametri_enteComponentSession)EJBCommonServices.createEJB("CNRCONFIG00_EJB_Parametri_enteComponentSession", Parametri_cnrComponentSession.class);
	}
	public static FatturaAttivaSingolaComponentSession createFatturaAttivaSingolaComponentSession()throws EJBException, RemoteException {
		return (FatturaAttivaSingolaComponentSession)EJBCommonServices.createEJB("CNRDOCAMM00_EJB_FatturaAttivaSingolaComponentSession", FatturaAttivaSingolaComponentSession.class);
	}
	public static FatturaPassivaComponentSession createFatturaPassivaComponentSession()throws EJBException, RemoteException {
		return (FatturaPassivaComponentSession)EJBCommonServices.createEJB("CNRDOCAMM00_EJB_FatturaPassivaComponentSession", FatturaPassivaComponentSession.class);
	}
	/**
	 * Crea la Local ReversaleComponentSession da usare per effettuare operazioni
	 */
	public static DistintaCassiereComponentSession createDistintaCassiereComponentSession() throws javax.ejb.EJBException{
		return (DistintaCassiereComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRDOCCONT00_EJB_DistintaCassiereComponentSession", DistintaCassiereComponentSession.class);
	}
	public static LiquidIvaInterfComponentSession createLiquidIvaInterfComponentSession() throws javax.ejb.EJBException{
		return (LiquidIvaInterfComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRGESTIVA00_EJB_LiquidIvaInterfComponentSession", LiquidIvaInterfComponentSession.class);
	}
	public static ProgettoRicercaComponentSession createProgettoRicercaComponentSession() throws javax.ejb.EJBException{
		return (ProgettoRicercaComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRPROGETTIRIC00_EJB_ProgettoRicercaComponentSession", ProgettoRicercaComponentSession.class);
	}
	public static VariazioniStanziamentoResiduoComponentSession createVariazioniStanziamentoResiduoComponentSession() throws javax.ejb.EJBException{
		return (VariazioniStanziamentoResiduoComponentSession) it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRVARSTANZ00_EJB_VariazioniStanziamentoResiduoComponentSession", VariazioniStanziamentoResiduoComponentSession.class);
	}
	public static MovimentiMagComponentSession createMovimentiMagComponentSession() throws javax.ejb.EJBException{
		return (MovimentiMagComponentSession) it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRORDMAG00_EJB_MovimentiMagComponentSession", MovimentiMagComponentSession.class);
	}
	public static OrdineAcqComponentSession createOrdineAcqComponentSession() throws javax.ejb.EJBException{
		return (OrdineAcqComponentSession) it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRORDMAG00_EJB_OrdineAcqComponentSession", OrdineAcqComponentSession.class);
	}
	public static TransitoBeniOrdiniComponentSession createTransitoBeniOrdiniComponentSession() throws javax.ejb.EJBException{
		return (TransitoBeniOrdiniComponentSession) it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRINVENTARIO00_EJB_TransitoBeniOrdiniComponentSession", TransitoBeniOrdiniComponentSession.class);
	}
	public static NumeratoriOrdMagComponentSession createNumeratoriOrdMagComponentSession() throws javax.ejb.EJBException{
		return (NumeratoriOrdMagComponentSession) it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRORDMAG_EJB_NumeratoriOrdMagComponentSession", NumeratoriOrdMagComponentSession.class);
	}
	public static AttoBolloComponentSession createAttoBolloComponentSession() throws javax.ejb.EJBException{
		return (AttoBolloComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRBOLLO00_EJB_AttoBolloComponentSession", AttoBolloComponentSession.class);
	}
	public static TipoAttoBolloComponentSession createTipoAttoBolloComponentSession() throws javax.ejb.EJBException{
		return (TipoAttoBolloComponentSession) it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRBOLLO00_EJB_TipoAttoBolloComponentSession", TipoAttoBolloComponentSession.class);
	}
	public static RuoloComponentSession getRuoloComponentSession() throws javax.ejb.EJBException, java.rmi.RemoteException {
		return (RuoloComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRUTENZE00_EJB_RuoloComponentSession",RuoloComponentSession.class);
	}
	public static CRUDConfigAssEvoldEvnewComponentSession createCRUDConfigAssEvoldEvnewComponentSession() throws javax.ejb.EJBException, java.rmi.RemoteException {
		return (CRUDConfigAssEvoldEvnewComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRCONFIG00_EJB_CRUDConfigAssEvoldEvnewComponentSession",CRUDConfigAssEvoldEvnewComponentSession.class);
	}
	public static RimodulaProgettoRicercaComponentSession createRimodulaProgettoRicercaComponentSession() throws javax.ejb.EJBException{
		return (RimodulaProgettoRicercaComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRPROGETTIRIC00_EJB_RimodulaProgettoRicercaComponentSession", RimodulaProgettoRicercaComponentSession.class);
	}
	public static IncarichiEstrazioneFpComponentSession createIncarichiEstrazioneFpComponentSession() throws javax.ejb.EJBException{
		return (IncarichiEstrazioneFpComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRINCARICHI00_EJB_IncarichiEstrazioneFpComponentSession", IncarichiEstrazioneFpComponentSession.class);
	}
	public static ScritturaPartitaDoppiaComponentSession createScritturaPartitaDoppiaComponentSession() throws javax.ejb.EJBException{
		return (ScritturaPartitaDoppiaComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRCOEPCOAN00_EJB_ScritturaPartitaDoppiaComponentSession", ScritturaPartitaDoppiaComponentSession.class);
	}
	public static ScritturaAnaliticaComponentSession createScritturaAnaliticaComponentSession() throws javax.ejb.EJBException{
		return (ScritturaAnaliticaComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRCOEPCOAN00_EJB_ScritturaAnaliticaComponentSession", ScritturaAnaliticaComponentSession.class);
	}
	public static ProposeScritturaComponentSession createProposeScritturaComponentSession() throws javax.ejb.EJBException{
		return (ProposeScritturaComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRCOEPCOAN00_EJB_ProposeScritturaComponentSession", ProposeScritturaComponentSession.class);
	}
	public static CompensoComponentSession createCompensoComponentSession() throws javax.ejb.EJBException{
		return (CompensoComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRCOMPENSI00_EJB_CompensoComponentSession", CompensoComponentSession.class);
	}
	public static CRUDPdgVariazioneGestionaleComponentSession createCRUDPdgVariazioneGestionaleComponentSession() throws javax.ejb.EJBException{
		return (CRUDPdgVariazioneGestionaleComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRPDG01_EJB_CRUDPdgVariazioneGestionaleComponentSession", CRUDPdgVariazioneGestionaleComponentSession.class);
	}
	public static CRUDPdgVariazioneRigaGestComponentSession createCRUDPdgVariazioneRigaGestComponentSession() throws javax.ejb.EJBException{
		return (CRUDPdgVariazioneRigaGestComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRPDG01_EJB_CRUDPdgVariazioneRigaGestComponentSession", CRUDPdgVariazioneRigaGestComponentSession.class);
	}
	public static DocumentoGenericoComponentSession createDocumentoGenericoComponentSession() throws javax.ejb.EJBException{
		return (DocumentoGenericoComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRDOCAMM00_EJB_DocumentoGenericoComponentSession", DocumentoGenericoComponentSession.class);
	}
	public static ObbligazioneComponentSession createObbligazioneComponentSession() throws javax.ejb.EJBException{
		return (ObbligazioneComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRDOCCONT00_EJB_ObbligazioneComponentSession", ObbligazioneComponentSession.class);
	}
	public static AccertamentoPGiroComponentSession createAccertamentoPGiroComponentSession() throws javax.ejb.EJBException{
		return (AccertamentoPGiroComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRDOCCONT00_EJB_AccertamentoPGiroComponentSession", AccertamentoPGiroComponentSession.class);
	}
	public static ObbligazionePGiroComponentSession createObbligazionePGiroComponentSession() throws javax.ejb.EJBException{
		return (ObbligazionePGiroComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRDOCCONT00_EJB_ObbligazionePGiroComponentSession", ObbligazionePGiroComponentSession.class);
	}
	public static ScritturaPartitaDoppiaFromDocumentoComponentSession createScritturaPartitaDoppiaFromDocumentoComponentSession() throws javax.ejb.EJBException{
		return (ScritturaPartitaDoppiaFromDocumentoComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRCOEPCOAN00_EJB_ScritturaPartitaDoppiaFromDocumentoComponentSession", ScritturaPartitaDoppiaFromDocumentoComponentSession.class);
	}
	public static AsyncScritturaPartitaDoppiaFromDocumentoComponentSession createAsyncScritturaPartitaDoppiaFromDocumentoComponentSession() throws javax.ejb.EJBException{
		return (AsyncScritturaPartitaDoppiaFromDocumentoComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRCOEPCOAN00_EJB_AsyncScritturaPartitaDoppiaFromDocumentoComponentSession", ScritturaPartitaDoppiaFromDocumentoComponentSession.class);
	}
	public static AutoFatturaComponentSession createAutoFatturaComponentSession() throws javax.ejb.EJBException{
		return (AutoFatturaComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRDOCAMM00_EJB_AutoFatturaComponentSession", AutoFatturaComponentSession.class);
	}
	public static AsyncPluriennaliComponentSession createAsyncPluriennaliComponentSession() throws javax.ejb.EJBException{
		return (AsyncPluriennaliComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRCOEPCOAN00_EJB_AsyncPluriennaliComponentSession", AsyncPluriennaliComponentSession.class);
	}

	public static ObbligazionePluriennaleComponentSession createObbligazionePluriennaleComponentSession() throws javax.ejb.EJBException{
		return (ObbligazionePluriennaleComponentSession) it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRDOCCONT00_EJB_ObbligazionePluriennaleComponentSession", ObbligazionePluriennaleComponentSession.class);
	}
	public static AccertamentoPluriennaleComponentSession createAccertamentoPluriennaleComponentSession() throws javax.ejb.EJBException{
		return (AccertamentoPluriennaleComponentSession) it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRDOCCONT00_EJB_AccertamentoPluriennaleComponentSession", AccertamentoPluriennaleComponentSession.class);
	}
	public static AsyncScritturaPartitaDoppiaChiusuraComponentSession createAsyncScritturaPartitaDoppiaChiusuraComponentSession() throws javax.ejb.EJBException{
		return (AsyncScritturaPartitaDoppiaChiusuraComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRCOEPCOAN00_EJB_AsyncScritturaPartitaDoppiaChiusuraComponentSession", AsyncScritturaPartitaDoppiaChiusuraComponentSession.class);
	}
	public static ScritturaPartitaDoppiaChiusuraComponentSession createScritturaPartitaDoppiaChiusuraComponentSession() throws javax.ejb.EJBException{
		return (ScritturaPartitaDoppiaChiusuraComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRCOEPCOAN00_EJB_ScritturaPartitaDoppiaChiusuraComponentSession", ScritturaPartitaDoppiaChiusuraComponentSession.class);
	}
	public static AmmortamentoBeneComponentSession createAmmortamentoBeneComponentSession() throws javax.ejb.EJBException{
		return (AmmortamentoBeneComponentSession) it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRINVENTARIO00_EJB_AmmortamentoBeneComponentSession", AmmortamentoBeneComponentSession.class);
	}
	public static Inventario_beniComponentSession createInventario_beniComponentSession() throws javax.ejb.EJBException{
		return (Inventario_beniComponentSession) it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRINVENTARIO00_EJB_Inventario_beniComponentSession", Inventario_beniComponentSession.class);
	}
	public static V_AmmortamentoBeniComponentSession createV_AmmortamentoBeniComponentSession() throws javax.ejb.EJBException{
		return (V_AmmortamentoBeniComponentSession) it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRINVENTARIO00_EJB_V_AmmortamentoBeniComponentSession", V_AmmortamentoBeniComponentSession.class);
	}
	public static Tipo_ammortamentoComponentSession createTipo_ammortamentoComponentSession() throws javax.ejb.EJBException{
		return (Tipo_ammortamentoComponentSession) it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRINVENTARIO00_EJB_Tipo_ammortamentoComponentSession", Tipo_ammortamentoComponentSession.class);
	}
	public static V_InventarioBeneDetComponentSession createV_InventarioBeneDetComponentSession() throws javax.ejb.EJBException{
		return (V_InventarioBeneDetComponentSession) it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRINVENTARIO00_EJB_V_InventarioBeneDetComponentSession", V_InventarioBeneDetComponentSession.class);
	}
	public static V_AmmortamentoBeniDetComponentSession createV_AmmortamentoBeniDetComponentSession() throws javax.ejb.EJBException{
		return (V_AmmortamentoBeniDetComponentSession) it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRINVENTARIO00_EJB_V_AmmortamentoBeniDetComponentSession", V_AmmortamentoBeniDetComponentSession.class);
	}
	public static AsyncAmmortamentoBeneComponentSession createAsyncAmmortamentoBeneComponentSession() throws javax.ejb.EJBException{
		return (AsyncAmmortamentoBeneComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRINVENTARIO00_EJB_AsyncAmmortamentoBeneComponentSession", AsyncAmmortamentoBeneComponentSession.class);
	}
	public static ChiusuraAnnoComponentSession createChiusuraAnnoComponentSession() throws javax.ejb.EJBException{
		return (ChiusuraAnnoComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRORDMAG00_EJB_ChiusuraAnnoComponentSession", ChiusuraAnnoComponentSession.class);
	}
	public static AsyncConsSostitutivaComponentSession createAsyncConsSostitutivaComponentSession() throws javax.ejb.EJBException{
		return (AsyncConsSostitutivaComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRCOEPCOAN00_EJB_ConsSostitutivaComponentSession", AsyncConsSostitutivaComponentSession.class);
	}
    public static PDCContoAnaliticoComponentSession createPDCContoAnaliticoComponentSession() throws javax.ejb.EJBException{
		return (PDCContoAnaliticoComponentSession)it.cnr.jada.util.ejb.EJBCommonServices.createEJB("CNRCONFIG00_EJB_PDCContoAnaliticoComponentSession", PDCContoAnaliticoComponentSession.class);
	}
}

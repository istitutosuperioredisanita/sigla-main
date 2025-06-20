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

package it.cnr.contab.docamm00.comp;

import it.cnr.contab.anagraf00.core.bulk.AnagraficoBulk;
import it.cnr.contab.anagraf00.core.bulk.TerzoBulk;
import it.cnr.contab.anagraf00.core.bulk.TerzoHome;
import it.cnr.contab.anagraf00.core.bulk.TerzoKey;
import it.cnr.contab.anagraf00.tabrif.bulk.Rif_modalita_pagamentoBulk;
import it.cnr.contab.anagraf00.tabter.bulk.ComuneBulk;
import it.cnr.contab.anagraf00.tabter.bulk.NazioneBulk;
import it.cnr.contab.config00.bulk.Configurazione_cnrBulk;
import it.cnr.contab.config00.contratto.bulk.ContrattoBulk;
import it.cnr.contab.config00.sto.bulk.Unita_organizzativaBulk;
import it.cnr.contab.docamm00.docs.bulk.*;
import it.cnr.contab.docamm00.tabrif.bulk.Bene_servizioBulk;
import it.cnr.contab.docamm00.tabrif.bulk.TariffarioBulk;
import it.cnr.contab.docamm00.tabrif.bulk.Voce_ivaBulk;
import it.cnr.contab.doccont00.core.bulk.AccertamentoBulk;
import it.cnr.contab.doccont00.core.bulk.Accertamento_scadenzarioBulk;
import it.cnr.contab.utenze00.bulk.UtenteBulk;
import it.cnr.contab.util.RemoveAccent;
import it.cnr.contab.util.Utility;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BulkList;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.CRUDComponent;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.sql.SQLBuilder;
import it.cnr.jada.util.SendMail;
import it.gov.agenziaentrate.ivaservizi.docs.xsd.fatture.v1.*;
import it.gov.fatturapa.sdi.ws.trasmissione.v1_0.types.FileSdIBaseType;
import org.springframework.util.StringUtils;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.ejb.EJBException;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;

@XmlRootElement(name = "fileSdIBase_Type")
public class DocAmmFatturazioneElettronicaComponent extends CRUDComponent{
	private static SimpleDateFormat DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd");
	private static Hashtable<String, TipoDocumentoType> tipoDocumento = new Hashtable<String, TipoDocumentoType>();
	private static Hashtable<Integer, String> conversioneNumeriIdUnivocoNomeFile = new Hashtable<Integer, String>();
	private static String CODICE_DESTINATARIO_ESTERO = "XXXXXXX";
	private static String CODICE_DESTINATARIO_CON_PEC_O_NON_REGISTRATO = "0000000";
	private static BigDecimal BASE_ID_UNIVOCO = new BigDecimal(62);
	static{
		tipoDocumento.put(Fattura_attivaBulk.TIPO_FATTURA_ATTIVA, TipoDocumentoType.TD_01);
		tipoDocumento.put(Fattura_attivaBulk.TIPO_NOTA_DI_CREDITO, TipoDocumentoType.TD_04);
		tipoDocumento.put(Fattura_attivaBulk.TIPO_NOTA_DI_DEBITO, TipoDocumentoType.TD_05);

		conversioneNumeriIdUnivocoNomeFile.put(0  ,"0");
		conversioneNumeriIdUnivocoNomeFile.put(1  ,"1");
		conversioneNumeriIdUnivocoNomeFile.put(2  ,"2");
		conversioneNumeriIdUnivocoNomeFile.put(3  ,"3");
		conversioneNumeriIdUnivocoNomeFile.put(4  ,"4");
		conversioneNumeriIdUnivocoNomeFile.put(5  ,"5");
		conversioneNumeriIdUnivocoNomeFile.put(6  ,"6");
		conversioneNumeriIdUnivocoNomeFile.put(7  ,"7");
		conversioneNumeriIdUnivocoNomeFile.put(8  ,"8");
		conversioneNumeriIdUnivocoNomeFile.put(9  ,"9");
		conversioneNumeriIdUnivocoNomeFile.put(10 ,"a");
		conversioneNumeriIdUnivocoNomeFile.put(11 ,"b");
		conversioneNumeriIdUnivocoNomeFile.put(12 ,"c");
		conversioneNumeriIdUnivocoNomeFile.put(13 ,"d");
		conversioneNumeriIdUnivocoNomeFile.put(14 ,"e");
		conversioneNumeriIdUnivocoNomeFile.put(15 ,"f");
		conversioneNumeriIdUnivocoNomeFile.put(16 ,"g");
		conversioneNumeriIdUnivocoNomeFile.put(17 ,"h");
		conversioneNumeriIdUnivocoNomeFile.put(18 ,"i");
		conversioneNumeriIdUnivocoNomeFile.put(19 ,"j");
		conversioneNumeriIdUnivocoNomeFile.put(20 ,"k");
		conversioneNumeriIdUnivocoNomeFile.put(21 ,"l");
		conversioneNumeriIdUnivocoNomeFile.put(22 ,"m");
		conversioneNumeriIdUnivocoNomeFile.put(23 ,"n");
		conversioneNumeriIdUnivocoNomeFile.put(24 ,"o");
		conversioneNumeriIdUnivocoNomeFile.put(25 ,"p");
		conversioneNumeriIdUnivocoNomeFile.put(26 ,"q");
		conversioneNumeriIdUnivocoNomeFile.put(27 ,"r");
		conversioneNumeriIdUnivocoNomeFile.put(28 ,"s");
		conversioneNumeriIdUnivocoNomeFile.put(29 ,"t");
		conversioneNumeriIdUnivocoNomeFile.put(30 ,"u");
		conversioneNumeriIdUnivocoNomeFile.put(31 ,"v");
		conversioneNumeriIdUnivocoNomeFile.put(32 ,"w");
		conversioneNumeriIdUnivocoNomeFile.put(33 ,"x");
		conversioneNumeriIdUnivocoNomeFile.put(34 ,"y");
		conversioneNumeriIdUnivocoNomeFile.put(35 ,"z");
		conversioneNumeriIdUnivocoNomeFile.put(36 ,"A");
		conversioneNumeriIdUnivocoNomeFile.put(37 ,"B");
		conversioneNumeriIdUnivocoNomeFile.put(38 ,"C");
		conversioneNumeriIdUnivocoNomeFile.put(39 ,"D");
		conversioneNumeriIdUnivocoNomeFile.put(40 ,"E");
		conversioneNumeriIdUnivocoNomeFile.put(41 ,"F");
		conversioneNumeriIdUnivocoNomeFile.put(42 ,"G");
		conversioneNumeriIdUnivocoNomeFile.put(43 ,"H");
		conversioneNumeriIdUnivocoNomeFile.put(44 ,"I");
		conversioneNumeriIdUnivocoNomeFile.put(45 ,"J");
		conversioneNumeriIdUnivocoNomeFile.put(46 ,"K");
		conversioneNumeriIdUnivocoNomeFile.put(47 ,"L");
		conversioneNumeriIdUnivocoNomeFile.put(48 ,"M");
		conversioneNumeriIdUnivocoNomeFile.put(49 ,"N");
		conversioneNumeriIdUnivocoNomeFile.put(50 ,"O");
		conversioneNumeriIdUnivocoNomeFile.put(51 ,"P");
		conversioneNumeriIdUnivocoNomeFile.put(52 ,"Q");
		conversioneNumeriIdUnivocoNomeFile.put(53 ,"R");
		conversioneNumeriIdUnivocoNomeFile.put(54 ,"S");
		conversioneNumeriIdUnivocoNomeFile.put(55 ,"T");
		conversioneNumeriIdUnivocoNomeFile.put(56 ,"U");
		conversioneNumeriIdUnivocoNomeFile.put(57 ,"V");
		conversioneNumeriIdUnivocoNomeFile.put(58 ,"W");
		conversioneNumeriIdUnivocoNomeFile.put(59 ,"X");
		conversioneNumeriIdUnivocoNomeFile.put(60 ,"Y");
		conversioneNumeriIdUnivocoNomeFile.put(61 ,"Z");
	}
	
	
	public class RiepilogoPerAliquotaIVA {
		private BigDecimal aliquota;
		private BigDecimal imponibile;
		private BigDecimal imposta;
		private String naturaIvaNonImponibile;
		private String riferimentoNormativaNonImponibile;
		public BigDecimal getAliquota() {
			return aliquota;
		}
		public void setAliquota(BigDecimal aliquota) {
			this.aliquota = aliquota;
		}
		public BigDecimal getImponibile() {
			return imponibile;
		}
		public void setImponibile(BigDecimal imponibile) {
			this.imponibile = imponibile;
		}
		public BigDecimal getImposta() {
			return imposta;
		}
		public void setImposta(BigDecimal imposta) {
			this.imposta = imposta;
		}
		public String getNaturaIvaNonImponibile() {
			return naturaIvaNonImponibile;
		}
		public void setNaturaIvaNonImponibile(String naturaIvaNonImponibile) {
			this.naturaIvaNonImponibile = naturaIvaNonImponibile;
		}
		public String getRiferimentoNormativaNonImponibile() {
			return riferimentoNormativaNonImponibile;
		}
		public void setRiferimentoNormativaNonImponibile(
				String riferimentoNormativaNonImponibile) {
			this.riferimentoNormativaNonImponibile = riferimentoNormativaNonImponibile;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result
					+ ((aliquota == null) ? 0 : aliquota.hashCode());
			result = prime
					* result
					+ ((naturaIvaNonImponibile == null) ? 0
							: naturaIvaNonImponibile.hashCode());
			result = prime
					* result
					+ ((riferimentoNormativaNonImponibile == null) ? 0
							: riferimentoNormativaNonImponibile.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			RiepilogoPerAliquotaIVA other = (RiepilogoPerAliquotaIVA) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (aliquota == null) {
				if (other.aliquota != null)
					return false;
			} else if (!aliquota.equals(other.aliquota))
				return false;
			if (naturaIvaNonImponibile == null) {
				if (other.naturaIvaNonImponibile != null)
					return false;
			} else if (!naturaIvaNonImponibile
					.equals(other.naturaIvaNonImponibile))
				return false;
			if (riferimentoNormativaNonImponibile == null) {
				if (other.riferimentoNormativaNonImponibile != null)
					return false;
			} else if (!riferimentoNormativaNonImponibile
					.equals(other.riferimentoNormativaNonImponibile))
				return false;
			return true;
		}
		private DocAmmFatturazioneElettronicaComponent getOuterType() {
			return DocAmmFatturazioneElettronicaComponent.this;
		}
		
	}
	/* 
	 * Conversione da base 10 a base b.
	Si procede nel modo seguente: dividere il numero da convertire per la base b fino a quando l'ultimo quoziente e' minore della base stessa (b), dopodiche' il numero convertito si ottiene prendendo l'ultimo quoziente e tutti i resti delle divisioni, 
	procedendo dall'ultimo resto al primo e scrivendoli da sinistra verso destra.

	Esempio: Convertire il numero 12 da Base 10 a Base 2
	12 : 2 = 6 con resto=0
	6 : 2 = 3 con resto =0
	3 : 2 = 1 con resto =1
	1 : 2 = 0 con resto =1
	quindi: (1100)2

	Esempio: Convertire il numero 120 da Base 10 a Base 8
	120 : 8 = 15 con resto = 0
	15 : 8 = 1 con resto = 7
	1 : 8 = 0 con resto 1
	quindi: (170)8

	Esempio: Convertire il numero 520 da Base 10 a Base 16
	520 : 16 = 32 con resto = 8
	32 : 16 = 2 con resto = 0
	2 : 16 = 0 con resto 2
	quindi: (208)16
	 */

	private String recuperoCodiceUnivocoFile(IDocumentoAmministrativoElettronicoBulk docammElettronico) throws ComponentException{
		StringBuffer result = new StringBuffer();
		if (docammElettronico.getProgrUnivocoAnno() > 99999){
			throw new ApplicationException("Impossibile Procedere! Il documento elettronico "+docammElettronico.getTipoDocumentoElettronico()+"/"+docammElettronico.getEsercizio()+"/"+docammElettronico.getPg_docamm()+" ha un progressivo univoco per anno troppo grande");
		} else {
			String stringaUnivoca = docammElettronico.getEsercizio().toString() + Utility.lpad(docammElettronico.getProgrUnivocoAnno().toString(),5,'0');
			BigDecimal idUnivocoBigD = new BigDecimal(stringaUnivoca);
			Hashtable<Integer, Integer> risultati = new Hashtable<Integer, Integer>(); 
			BigDecimal base = new BigDecimal(62);
			for (int i = 4; i > 0; i--) {
				BigDecimal[] risultatoInteroEResto = idUnivocoBigD.divideAndRemainder(base);
				risultati.put(i, risultatoInteroEResto[1].intValue());
				idUnivocoBigD = risultatoInteroEResto[0];
			}
			risultati.put(0, idUnivocoBigD.intValue());
			for (int i = 0; i < 5; i++) {
				Integer resto = risultati.get(i);
				result.append(conversioneNumeriIdUnivocoNomeFile.get(resto));
			}
		}
		return result.toString();
	}
	
	public String recuperoNomeFileXml(UserContext userContext, IDocumentoAmministrativoElettronicoBulk docammElettronico) throws RemoteException,  ComponentException {
		try {
			return recuperoNomeFileSenzaEstensione(userContext, docammElettronico) +".xml";
		} catch(Exception e) {
			throw handleException(e);
		}
	}

	public String recuperoNomeFileSenzaEstensione(UserContext userContext, IDocumentoAmministrativoElettronicoBulk docammElettronico) throws RemoteException,  ComponentException {
		try {
			String inizioNomeFile = recuperoInizioNomeFile(userContext);
			return inizioNomeFile+"_"+recuperoCodiceUnivocoFile(docammElettronico);
		} catch(Exception e) {
			throw handleException(e);
		}
	}

	public String recuperoInizioNomeFile(UserContext userContext) throws RemoteException,ComponentException {
		try {
			TerzoBulk terzoCnr = ((TerzoHome)getHome( userContext, TerzoBulk.class)).findTerzoEnte();

			String codiceFiscaleEnte = impostaCodiceFiscale(userContext, terzoCnr);
			String idPaese = impostaCodicePaese(userContext, terzoCnr);
			String inizioNomeFile = idPaese+codiceFiscaleEnte;
			return inizioNomeFile;
		} catch(Exception e) {
			throw handleException(e);
		}
	}

	public DataHandler creaFileHandler(UserContext userContext, File file) throws ComponentException, PersistencyException, IOException{
		try {
			return new DataHandler(new FileDataSource(file));
		} catch(Exception e) {
			throw handleException(e);
		}
	}

	public FileSdIBaseType preparaMessaggio(UserContext userContext, DataHandler dataHandler) throws ComponentException, PersistencyException, IOException{
		try {
			it.gov.fatturapa.sdi.ws.trasmissione.v1_0.types.ObjectFactory factory = new it.gov.fatturapa.sdi.ws.trasmissione.v1_0.types.ObjectFactory();
			FileSdIBaseType fileSdi = factory.createFileSdIBaseType();
			fileSdi.setNomeFile(dataHandler.getName());
			fileSdi.setFile(dataHandler);
			return fileSdi;
		} catch(Exception e) {
			throw handleException(e);
		}
	}

	public Configurazione_cnrBulk getAuthenticatorPecSdi(UserContext userContext) throws ComponentException {
		Configurazione_cnrBulk email;
		try {
			email = Utility.createConfigurazioneCnrComponentSession().getConfigurazione(userContext, new Integer(0),null,Configurazione_cnrBulk.PK_EMAIL_PEC, Configurazione_cnrBulk.SK_SDI);
			if (email != null)
				return email;
				throw new ApplicationException("Confiurazione PEC non trovata, contattare il servizio di HelpDesk!");
		} catch (RemoteException e) {
			throw new ApplicationException(e);
		} catch (EJBException e) {
			throw new ApplicationException(e);
		}
	}

	private String getMailAddress(UserContext userContext, IDocumentoAmministrativoElettronicoBulk docamm) throws ComponentException {
		UtenteBulk utente = new UtenteBulk(docamm.getUtcr());
		try {
			utente = (UtenteBulk) getHome(userContext, UtenteBulk.class).findByPrimaryKey(utente);
			if (utente != null){
				String dominioMail=Utility.createConfigurazioneCnrComponentSession().getVal02(userContext, it.cnr.contab.utenze00.bp.CNRUserContext.getEsercizio(userContext), null, Configurazione_cnrBulk.PK_FATTURAZIONE_ELETTRONICA, Configurazione_cnrBulk.SK_MAIL_REFERENTE_TECNICO);
				if (utente.getCd_utente_uid() != null){
					return utente.getCd_utente_uid().concat(Optional.ofNullable(dominioMail).orElse(""));
				}
			}
			String eMailReferente = Utility.createConfigurazioneCnrComponentSession().getVal01(userContext, it.cnr.contab.utenze00.bp.CNRUserContext.getEsercizio(userContext), null, Configurazione_cnrBulk.PK_FATTURAZIONE_ELETTRONICA, Configurazione_cnrBulk.SK_MAIL_REFERENTE_TECNICO);
			return eMailReferente;
		} catch (PersistencyException | RemoteException e) {
			throw new ComponentException(e);
		}
	}

	public FatturaElettronicaType preparaFattura(UserContext userContext, IDocumentoAmministrativoElettronicoBulk docammElettronico)throws ComponentException {
		try {
			ObjectFactory factory = new ObjectFactory();
			FatturaElettronicaType fatturaType = factory.createFatturaElettronicaType();
			fatturaType.setSistemaEmittente(Utility.APPLICATION_TITLE.substring(0, 5));
			fatturaType.setVersione(docammElettronico.getCodiceUnivocoUfficioIpa()==null ? FormatoTrasmissioneType.FPR_12 : FormatoTrasmissioneType.FPA_12);
			FatturaElettronicaHeaderType fatturaHeaderType = factory.createFatturaElettronicaHeaderType();

			TerzoBulk terzoCnr = ((TerzoHome)getHome( userContext, TerzoBulk.class)).findTerzoEnte();

			Unita_organizzativaBulk uoOrigine = (Unita_organizzativaBulk)getHome(userContext, Unita_organizzativaBulk.class).findByPrimaryKey(new Unita_organizzativaBulk(docammElettronico.getCd_uo_origine()));
			TerzoBulk terzoUo = Utility.createTerzoComponentSession().cercaTerzoPerUnitaOrganizzativa(userContext, uoOrigine);

			TerzoBulk cliente = null;
			if (docammElettronico instanceof Fattura_attivaBulk)
				cliente = (TerzoBulk) getHome(userContext, TerzoBulk.class).findByPrimaryKey(new TerzoKey(((Fattura_attivaBulk)docammElettronico).getCd_terzo()));
			else if (docammElettronico instanceof AutofatturaBulk) {
				AutofatturaBulk autofattura = (AutofatturaBulk)docammElettronico;
				if (autofattura.getFlTerzoEnte())
					cliente = terzoUo;
				else {
					Integer cdTerzo = Optional.ofNullable(autofattura.getFattura_passiva()).map(Fattura_passivaBase::getCd_terzo).orElse(null);
					if (cdTerzo != null)
						cliente = (TerzoBulk) getHome(userContext, TerzoBulk.class).findByPrimaryKey(new TerzoKey(cdTerzo));
				}
			}

			if (cliente.getAnagrafico()==null || cliente.getAnagrafico().getNome()==null) {
				AnagraficoBulk anagraficoDB = (AnagraficoBulk) getHome(userContext, AnagraficoBulk.class).findByPrimaryKey(new AnagraficoBulk(cliente.getCd_anag()));
				cliente.setAnagrafico(anagraficoDB);
			}

			TerzoBulk terzoCedentePrestatore = null;
			TerzoBulk terzoCessionarioCommittente = null;
			if (docammElettronico instanceof Fattura_attivaBulk) {
				terzoCedentePrestatore = terzoCnr;
				terzoCessionarioCommittente = cliente;
			} else if (docammElettronico instanceof AutofatturaBulk) {
				terzoCedentePrestatore = cliente;
				terzoCessionarioCommittente = terzoCnr;
			}

			if (terzoCnr != null){
				DatiTrasmissioneType datiTrasmissione = factory.createDatiTrasmissioneType();

				datiTrasmissione.setIdTrasmittente(impostaIdTrasmittente(userContext, factory, terzoCnr));

				datiTrasmissione.setProgressivoInvio(docammElettronico.getEsercizio().toString() + Utility.lpad(docammElettronico.getProgrUnivocoAnno().toString(),6,'0'));

				if (docammElettronico.getCodiceUnivocoUfficioIpa() != null){
					datiTrasmissione.setCodiceDestinatario(docammElettronico.getCodiceUnivocoUfficioIpa());
				} else {
					if (docammElettronico.getCodiceDestinatarioFatt() != null){
						datiTrasmissione.setCodiceDestinatario(docammElettronico.getCodiceDestinatarioFatt());
					} else {
						if (docammElettronico.isFatturaEstera()){
							datiTrasmissione.setCodiceDestinatario(CODICE_DESTINATARIO_ESTERO);
						} else {
							datiTrasmissione.setCodiceDestinatario(CODICE_DESTINATARIO_CON_PEC_O_NON_REGISTRATO);
						}
						datiTrasmissione.setPECDestinatario(docammElettronico.getPecFatturaElettronica());
					}
				}

				ContattiTrasmittenteType contattiTrasmittenteType = factory.createContattiTrasmittenteType();
				contattiTrasmittenteType.setEmail(getMailAddress(userContext, docammElettronico));
				String telefonoReferente = Utility.createConfigurazioneCnrComponentSession().getVal01(userContext, it.cnr.contab.utenze00.bp.CNRUserContext.getEsercizio(userContext), null, Configurazione_cnrBulk.PK_FATTURAZIONE_ELETTRONICA, Configurazione_cnrBulk.SK_TELEFONO_REFERENTE_TECNICO);
				contattiTrasmittenteType.setTelefono(telefonoReferente);
				datiTrasmissione.setContattiTrasmittente(contattiTrasmittenteType);

				datiTrasmissione.setFormatoTrasmissione(docammElettronico.getCodiceUnivocoUfficioIpa()==null ? FormatoTrasmissioneType.FPR_12 : FormatoTrasmissioneType.FPA_12);

				fatturaHeaderType.setDatiTrasmissione(datiTrasmissione);

				CedentePrestatoreType cedentePrestatoreType = factory.createCedentePrestatoreType();

				DatiAnagraficiCedenteType anagraficiCedenteType = factory.createDatiAnagraficiCedenteType();

				if (terzoCedentePrestatore.getAnagrafico() == null || terzoCedentePrestatore.getAnagrafico().getPartita_iva() == null)
					throw new ApplicationException("Impossibile Procedere! Manca la Partita Iva per il codice Anagrafica: "+terzoCedentePrestatore.getCd_anag()+" del terzo: "+ terzoCedentePrestatore.getCd_terzo());
				anagraficiCedenteType.setIdFiscaleIVA(impostaIdFiscale(userContext, factory, terzoCedentePrestatore, null));
				if (docammElettronico instanceof Fattura_attivaBulk) {
					if (terzoCedentePrestatore.getAnagrafico().getCodice_fiscale() == null)
						throw new ApplicationException("Impossibile Procedere! Manca il Codice Fiscale per il codice Anagrafica: " + terzoCedentePrestatore.getCd_anag() + " del terzo: " + terzoCedentePrestatore.getCd_terzo());

					anagraficiCedenteType.setCodiceFiscale(terzoCedentePrestatore.getAnagrafico().getCodice_fiscale());
				}
				anagraficiCedenteType.setAnagrafica(impostaAnagrafica(factory, terzoCedentePrestatore));

				anagraficiCedenteType.setRegimeFiscale(RegimeFiscaleType.RF_01);
				cedentePrestatoreType.setDatiAnagrafici(anagraficiCedenteType);
				cedentePrestatoreType.setSede(impostaIndirizzo(userContext, factory, docammElettronico instanceof Fattura_attivaBulk?terzoUo:terzoCedentePrestatore, docammElettronico));

				fatturaHeaderType.setCedentePrestatore(cedentePrestatoreType);

				CessionarioCommittenteType clienteType = factory.createCessionarioCommittenteType();

				DatiAnagraficiCessionarioType datiAnagraficiClienteType = factory.createDatiAnagraficiCessionarioType();

				datiAnagraficiClienteType.setAnagrafica(impostaAnagrafica(factory, terzoCessionarioCommittente));

				if (docammElettronico instanceof Fattura_attivaBulk) {
					Fattura_attivaBulk fattura = (Fattura_attivaBulk) docammElettronico;
					if (fattura.getFl_intra_ue() && terzoCessionarioCommittente.getAnagrafico().isPersonaGiuridica() && terzoCessionarioCommittente.getCodiceUnivocoUfficioIpa() == null && terzoCessionarioCommittente.getAnagrafico().getPartita_iva() == null) {
						throw new ApplicationException("Impossibile Procedere! E' necessario indicare la partita IVA");
					}
					if ((!fattura.isFatturaEstera() || !terzoCessionarioCommittente.getAnagrafico().isPersonaGiuridica()) && (fattura.getCodice_fiscale() == null && !terzoCessionarioCommittente.getAnagrafico().isGruppoIVA())) {
						throw new ApplicationException("Impossibile Procedere! Manca il Codice Fiscale per la Fattura: " + fattura.getEsercizio() + "-" + fattura.getPg_fattura_attiva());
					}
					if (!fattura.isFatturaEstera()) {
						datiAnagraficiClienteType.setCodiceFiscale(fattura.getCodice_fiscale());
					}

					if (terzoCessionarioCommittente.getAnagrafico() != null && terzoCessionarioCommittente.getAnagrafico().getPartita_iva() != null && (!terzoCessionarioCommittente.getAnagrafico().isEntePubblico() || terzoCessionarioCommittente.getAnagrafico().isPartitaIvaVerificata() || (fattura.getCodice_fiscale() != null && terzoCessionarioCommittente.getAnagrafico().getPartita_iva().compareTo(fattura.getCodice_fiscale())!=0) )){
						datiAnagraficiClienteType.setIdFiscaleIVA(impostaIdFiscale(userContext, factory, terzoCessionarioCommittente, fattura));
					} else if (!StringUtils.hasLength(datiAnagraficiClienteType.getCodiceFiscale())){
						datiAnagraficiClienteType.setIdFiscaleIVA(impostaIdFiscale(userContext, factory, terzoCessionarioCommittente, fattura));
					} else if (docammElettronico.isFatturaEstera() && terzoCessionarioCommittente.getAnagrafico().isPersonaFisica()){
						IdFiscaleType idFiscale= factory.createIdFiscaleType();
						idFiscale.setIdCodice("99999999999");
						idFiscale.setIdPaese(impostaCodicePaese(userContext, terzoCessionarioCommittente));
						datiAnagraficiClienteType.setIdFiscaleIVA(idFiscale);
					}

					clienteType.setSede(impostaIndirizzo(userContext, factory, terzoCessionarioCommittente, fattura));
				} else if (docammElettronico instanceof AutofatturaBulk) {
					datiAnagraficiClienteType.setCodiceFiscale(terzoCessionarioCommittente.getCodice_fiscale_anagrafico());
					datiAnagraficiClienteType.setIdFiscaleIVA(impostaIdFiscale(userContext, factory, terzoCessionarioCommittente, null));

					clienteType.setSede(impostaIndirizzo(userContext, factory, terzoCessionarioCommittente, docammElettronico));
				}

				if (!StringUtils.hasLength(datiAnagraficiClienteType.getCodiceFiscale()) && !StringUtils.hasLength(datiAnagraficiClienteType.getIdFiscaleIVA().getIdCodice())){
					datiAnagraficiClienteType.getIdFiscaleIVA().setIdCodice("99999999999");
				}

				clienteType.setDatiAnagrafici(datiAnagraficiClienteType);

				fatturaHeaderType.setCessionarioCommittente(clienteType);

				fatturaType.setFatturaElettronicaHeader(fatturaHeaderType);

				FatturaElettronicaBodyType fatturaBodyType = factory.createFatturaElettronicaBodyType();

				DatiGeneraliType datiGenerali = factory.createDatiGeneraliType();

				DatiGeneraliDocumentoType datiGeneraliDocumento = factory.createDatiGeneraliDocumentoType();
				datiGeneraliDocumento.setDivisa("EUR");
				datiGeneraliDocumento.setNumero(docammElettronico.recuperoIdFatturaAsString());

				if (docammElettronico instanceof AutofatturaBulk) {
					AutofatturaBulk autofattura = (AutofatturaBulk)docammElettronico;
					Fattura_passivaBulk fatturaPassiva = (Fattura_passivaBulk)getHome(userContext, Fattura_passiva_IBulk.class).findByPrimaryKey(autofattura.getFattura_passiva());

					datiGeneraliDocumento.setData(convertDateToXmlGregorian(fatturaPassiva.getDt_registrazione()));

					List detailFattura = new BulkList(Utility.createFatturaPassivaComponentSession().findDettagli(userContext, fatturaPassiva));

					if (fatturaPassiva.isSanMarinoConIVA()) {
						datiGeneraliDocumento.setTipoDocumento(TipoDocumentoType.TD_28);
					} else if (fatturaPassiva.isEstera() || fatturaPassiva.isSanMarinoSenzaIVA()) {
						if (fatturaPassiva.isFatturaDiServizi())
							datiGeneraliDocumento.setTipoDocumento(TipoDocumentoType.TD_17);
						else if (fatturaPassiva.isFatturaDiBeni()) {
							if (!fatturaPassiva.getFl_extra_ue())
								datiGeneraliDocumento.setTipoDocumento(TipoDocumentoType.TD_18);
							else if (fatturaPassiva.getFl_merce_intra_ue())
								datiGeneraliDocumento.setTipoDocumento(TipoDocumentoType.TD_19);
						}
					} else if (fatturaPassiva.isCommerciale()) {
						datiGeneraliDocumento.setTipoDocumento(TipoDocumentoType.TD_16);
					}

					if (datiGeneraliDocumento.getTipoDocumento()==null)
						throw new ApplicationException("Impossibile Procedere! Non è stato possibile individuare il tipo documento da imputare nel file XML per il documento elettronico "+autofattura.getTipoDocumentoElettronico()+"-"+autofattura.getEsercizio()+"-"+autofattura.getPg_docamm());

					datiGeneraliDocumento.setImportoTotaleDocumento(autofattura.getFattura_passiva().getIm_totale_imponibile().add(autofattura.getFattura_passiva().getIm_totale_iva()).setScale(2));
					//datiGeneraliDocumento.getCausale().add("");
					datiGenerali.setDatiGeneraliDocumento(datiGeneraliDocumento);

					DatiBeniServiziType datiBeniServizi = factory.createDatiBeniServiziType();
					List<DettaglioLineeType> listaDettagli = new ArrayList<>();
					List<RiepilogoPerAliquotaIVA> listaRiepilogo = new ArrayList<>();
					List<Integer> listaTutteLinee = new ArrayList<>();

					for (Iterator<Fattura_passiva_rigaBulk> i = detailFattura.iterator(); i.hasNext(); ) {
						Fattura_passiva_rigaBulk riga = i.next();
						DettaglioLineeType rigaFattura = factory.createDettaglioLineeType();

						rigaFattura.setNumeroLinea(riga.getProgressivo_riga().intValue());
						listaTutteLinee.add(riga.getProgressivo_riga().intValue());

						if (riga.getDs_riga_fattura() != null) {
							rigaFattura.setDescrizione(RemoveAccent.convert(riga.getDs_riga_fattura().replaceAll("\\u20AC", "E")));
						} else {
							rigaFattura.setDescrizione("Descrizione");
						}
						rigaFattura.setQuantita(riga.getQuantita().setScale(2));
						rigaFattura.setDataInizioPeriodo(convertDateToXmlGregorian(riga.getDt_da_competenza_coge()));
						rigaFattura.setDataFinePeriodo(convertDateToXmlGregorian(riga.getDt_a_competenza_coge()));
						rigaFattura.setPrezzoUnitario(riga.getPrezzo_unitario().multiply(autofattura.getFattura_passiva().getCambio()).setScale(2,java.math.BigDecimal.ROUND_HALF_UP));
						rigaFattura.setPrezzoTotale(rigaFattura.getPrezzoUnitario().multiply(rigaFattura.getQuantita()).setScale(2));
						if (riga.getVoce_iva() != null && riga.getVoce_iva().getCd_voce_iva() != null && riga.getVoce_iva().getPercentuale() == null)
							riga.setVoce_iva((Voce_ivaBulk) findByPrimaryKey(userContext, riga.getVoce_iva()));
						rigaFattura.setAliquotaIVA(Utility.nvl(riga.getVoce_iva().getPercentuale()).setScale(2));
						rigaFattura.setNatura(impostaDatiNatura(riga.getVoce_iva().getNaturaOperNonImpSdi()));
						listaDettagli.add(rigaFattura);
						impostaDatiPerRiepilogoDatiIva(listaRiepilogo, riga);
					}
					datiBeniServizi.getDettaglioLinee().addAll(listaDettagli);

					fatturaBodyType.setDatiGenerali(datiGenerali);

					List<DatiRiepilogoType> listaRiepilogoType = new ArrayList<>();
					for (Iterator<RiepilogoPerAliquotaIVA> i = listaRiepilogo.iterator(); i.hasNext(); ) {
						RiepilogoPerAliquotaIVA riepilogo = i.next();
						DatiRiepilogoType datiRiepilogo = factory.createDatiRiepilogoType();
						datiRiepilogo.setAliquotaIVA(riepilogo.getAliquota());
						if (autofattura.getFl_liquidazione_differita()) {
							datiRiepilogo.setEsigibilitaIVA(autofattura.getEsercizio() > 2014 ? EsigibilitaIVAType.S : EsigibilitaIVAType.D);
						} else {
							datiRiepilogo.setEsigibilitaIVA(EsigibilitaIVAType.I);
						}
						datiRiepilogo.setImponibileImporto(riepilogo.getImponibile().setScale(2));
						datiRiepilogo.setImposta(riepilogo.getImposta().setScale(2));
						impostaDatiNonImponibile(autofattura, riepilogo, datiRiepilogo);
						listaRiepilogoType.add(datiRiepilogo);
					}
					datiBeniServizi.getDatiRiepilogo().addAll(listaRiepilogoType);

					fatturaBodyType.setDatiBeniServizi(datiBeniServizi);

					DatiPagamentoType datiPagamento = factory.createDatiPagamentoType();
					datiPagamento.setCondizioniPagamento(CondizioniPagamentoType.TP_02);

					List<DettaglioPagamentoType> listaDettagliPagamento = new ArrayList<>();
					DettaglioPagamentoType dettaglioPagamento = factory.createDettaglioPagamentoType();
					dettaglioPagamento.setModalitaPagamento(ModalitaPagamentoType.MP_01);

					if (autofattura.getFl_liquidazione_differita() && autofattura.getEsercizio() > 2014) {
						dettaglioPagamento.setImportoPagamento(autofattura.getFattura_passiva().getIm_totale_imponibile().setScale(2));
					} else {
						dettaglioPagamento.setImportoPagamento(autofattura.getFattura_passiva().getIm_totale_imponibile().add(autofattura.getFattura_passiva().getIm_totale_iva()).setScale(2));
					}

					dettaglioPagamento.setCodicePagamento(autofattura.recuperoIdFatturaAsString());
					listaDettagliPagamento.add(dettaglioPagamento);

					datiPagamento.getDettaglioPagamento().addAll(listaDettagliPagamento);

					List<DatiPagamentoType> listaDatiPagamento = new ArrayList<>();
					listaDatiPagamento.add(datiPagamento);
					fatturaBodyType.getDatiPagamento().addAll(listaDatiPagamento);

					//Aggiungo il legame dell'autofattura con la fattura passiva
					DatiDocumentiCorrelatiType datiFattureCollegate = factory.createDatiDocumentiCorrelatiType();
					datiFattureCollegate.getRiferimentoNumeroLinea().addAll(listaTutteLinee);
					datiFattureCollegate.setData(convertDateToXmlGregorian(fatturaPassiva.getDt_fattura_fornitore()));
					datiFattureCollegate.setIdDocumento(substring(fatturaPassiva.getIdentificativoSdi()!=null?fatturaPassiva.getIdentificativoSdi().toString()
							:fatturaPassiva.getNr_fattura_fornitore(), 20));
					datiGenerali.getDatiFattureCollegate().add(datiFattureCollegate);

					List<FatturaElettronicaBodyType> listaFattureBody = new ArrayList<>();
					listaFattureBody.add(fatturaBodyType);
					fatturaType.getFatturaElettronicaBody().addAll(listaFattureBody);
				} else if (docammElettronico instanceof Fattura_attivaBulk) {
					Fattura_attivaBulk fattura = (Fattura_attivaBulk)docammElettronico;

					datiGeneraliDocumento.setData(convertDateToXmlGregorian(it.cnr.jada.util.ejb.EJBCommonServices.getServerDate()));
					datiGeneraliDocumento.setTipoDocumento(tipoDocumento.get(docammElettronico.getTi_fattura()));

					List list = Utility.createFatturaAttivaSingolaComponentSession().findDettagli(userContext, fattura);

					if (list != null) {
						BulkList dettaglio = new BulkList(list);

						if (dettaglio == null || dettaglio.isEmpty()) {
							throw new ApplicationException("Impossibile Procedere! La Fattura: " + fattura.getEsercizio() + "-" + fattura.getPg_fattura_attiva() + " non ha dettagli");
						}
						Boolean esisteBollo = gestioneBollo(userContext, factory, datiGeneraliDocumento, fattura, dettaglio);

						datiGeneraliDocumento.setImportoTotaleDocumento(fattura.getIm_totale_fattura().setScale(2));
						String descrizione = "";
						if (fattura.getDs_fattura_attiva() != null) {
							descrizione = fattura.getDs_fattura_attiva();
						}
						List<String> listaCausali = new ArrayList<String>();
						if (fattura.getRiferimento_ordine() != null && fattura.getDt_ordine() == null) {
							if (!descrizione.equals("")) {
								descrizione += "  ";
							}
							descrizione += "Riferimento ordine:" + fattura.getRiferimento_ordine();
						}

						int lunghezzaStringaCausale = 200;
						int numeroCaratteriFinali = 0;
						for (int i = 0; i <= descrizione.length() / lunghezzaStringaCausale; i++) {
							int numeroCaratteri = i * lunghezzaStringaCausale;
							int numeroCaratteriOccorrenti = (numeroCaratteri + lunghezzaStringaCausale);
							if (numeroCaratteriOccorrenti > descrizione.length()) {
								numeroCaratteriFinali = numeroCaratteri + (descrizione.length() - numeroCaratteri);
							} else {
								numeroCaratteriFinali = numeroCaratteri + lunghezzaStringaCausale;
							}
							if (numeroCaratteri < numeroCaratteriFinali) {
								String causale = descrizione.substring(numeroCaratteri, numeroCaratteriFinali);
								listaCausali.add(RemoveAccent.convert(causale));
							}
						}

						datiGeneraliDocumento.getCausale().addAll(listaCausali);

						datiGenerali.setDatiGeneraliDocumento(datiGeneraliDocumento);

						DatiBeniServiziType datiBeniServizi = factory.createDatiBeniServiziType();
						List<DettaglioLineeType> listaDettagli = new ArrayList<DettaglioLineeType>();
						List<RiepilogoPerAliquotaIVA> listaRiepilogo = new ArrayList<DocAmmFatturazioneElettronicaComponent.RiepilogoPerAliquotaIVA>();
						HashMap<ContrattoBulk, List<Integer>> mappaContratti = new HashMap<ContrattoBulk, List<Integer>>();
						HashMap<Fattura_attivaBulk, List<Integer>> dettagliNoteSenzaContratto = new HashMap<Fattura_attivaBulk, List<Integer>>();
						HashMap<Fattura_attivaBulk, HashMap<ContrattoBulk, List<Integer>>> mappaDocumentiCollegati = new HashMap<Fattura_attivaBulk, HashMap<ContrattoBulk, List<Integer>>>();
						List<Integer> listaTutteLinee = new ArrayList<>();
						for (Iterator<Fattura_attiva_rigaBulk> i = dettaglio.iterator(); i.hasNext(); ) {
							Fattura_attiva_rigaBulk riga = (Fattura_attiva_rigaBulk) i.next();
							listaTutteLinee.add(riga.getProgressivo_riga().intValue());
							DettaglioLineeType rigaFattura = factory.createDettaglioLineeType();
							rigaFattura.setNumeroLinea(riga.getProgressivo_riga().intValue());
							if (riga.getDs_riga_fattura() != null) {
								rigaFattura.setDescrizione(RemoveAccent.convert(riga.getDs_riga_fattura().replaceAll("\\u20AC", "E")));
							} else {
								rigaFattura.setDescrizione("Descrizione");
							}
							rigaFattura.setQuantita(riga.getQuantita().setScale(2));
							if (riga.getTariffario() != null && riga.getTariffario().getCd_tariffario() != null) {
								if (riga.getTariffario().getUnita_misura() == null)
									riga.setTariffario((TariffarioBulk) findByPrimaryKey(userContext, riga.getTariffario()));
								rigaFattura.setUnitaMisura(riga.getTariffario().getUnita_misura());
							}
							rigaFattura.setDataInizioPeriodo(convertDateToXmlGregorian(riga.getDt_da_competenza_coge()));
							rigaFattura.setDataFinePeriodo(convertDateToXmlGregorian(riga.getDt_a_competenza_coge()));
							rigaFattura.setPrezzoUnitario(riga.getPrezzo_unitario().setScale(2));
							rigaFattura.setPrezzoTotale(rigaFattura.getPrezzoUnitario().multiply(rigaFattura.getQuantita()).setScale(2));
							if (riga.getVoce_iva() != null && riga.getVoce_iva().getCd_voce_iva() != null && riga.getVoce_iva().getPercentuale() == null)
								riga.setVoce_iva((Voce_ivaBulk) findByPrimaryKey(userContext, riga.getVoce_iva()));
							rigaFattura.setAliquotaIVA(Utility.nvl(riga.getVoce_iva().getPercentuale()).setScale(2));
							rigaFattura.setNatura(impostaDatiNatura(riga.getVoce_iva().getNaturaOperNonImpSdi()));
							listaDettagli.add(rigaFattura);
							if (fattura.getTi_fattura().equals(Fattura_attivaBulk.TIPO_NOTA_DI_CREDITO)) {
								impostaDatiPerNoteCredito(userContext, mappaDocumentiCollegati, riga, dettagliNoteSenzaContratto);
							}
							preparaDatiContratto(userContext, mappaContratti, riga);
							impostaDatiPerRiepilogoDatiIva(listaRiepilogo, riga);
						}
						datiBeniServizi.getDettaglioLinee().addAll(listaDettagli);

						impostaDatiContratto(factory, datiGenerali, mappaContratti);
						impostaDatiDocumentiCollegati(factory, datiGenerali, mappaDocumentiCollegati, dettagliNoteSenzaContratto);

						if (!fattura.isFatturaEstera() && fattura.getRiferimento_ordine() != null && fattura.getDt_ordine() != null) {
							DatiDocumentiCorrelatiType datiOrdineAcquisto = factory.createDatiDocumentiCorrelatiType();
							datiOrdineAcquisto.getRiferimentoNumeroLinea().addAll(listaTutteLinee);
							datiOrdineAcquisto.setData(convertDateToXmlGregorian(fattura.getDt_ordine()));
							datiOrdineAcquisto.setIdDocumento(substring(fattura.getRiferimento_ordine(), 20));
							String soggettoOrdine = null;
							if (!fattura.getFl_ordine_elettronico()) {
								soggettoOrdine = "#NO#";
							} else {
								if (fattura.getCodiceUnivocoUfficioIpa() != null) {
									String cuu = null;
									if (fattura.getCodiceUnivocoUfficioOrdine() != null) {
										cuu = fattura.getCodiceUnivocoUfficioOrdine();
									} else {
										cuu = fattura.getCodiceUnivocoUfficioIpa();
									}
									soggettoOrdine = "#" + cuu + "#";
								} else {
									if (fattura.getPartita_iva() != null) {
										soggettoOrdine = "#9906:";
									} else {
										soggettoOrdine = "#9907:";
									}
									if (fattura.getCodiceDestinatarioFatt() != null) {
										soggettoOrdine += fattura.getCodiceDestinatarioFatt();
									} else if (fattura.getPecFatturaElettronica() != null) {
										soggettoOrdine += fattura.getPecFatturaElettronica();
									} else {
										soggettoOrdine += datiTrasmissione.getCodiceDestinatario();
									}
									soggettoOrdine += "#";
								}
							}
							datiOrdineAcquisto.setCodiceCommessaConvenzione(soggettoOrdine);
							datiGenerali.getDatiOrdineAcquisto().add(datiOrdineAcquisto);
						}


						fatturaBodyType.setDatiGenerali(datiGenerali);

						List<DatiRiepilogoType> listaRiepilogoType = new ArrayList<DatiRiepilogoType>();
						for (Iterator<RiepilogoPerAliquotaIVA> i = listaRiepilogo.iterator(); i.hasNext(); ) {
							RiepilogoPerAliquotaIVA riepilogo = (RiepilogoPerAliquotaIVA) i.next();
							DatiRiepilogoType datiRiepilogo = factory.createDatiRiepilogoType();
							datiRiepilogo.setAliquotaIVA(riepilogo.getAliquota());
							if (fattura.getFl_liquidazione_differita()) {
								datiRiepilogo.setEsigibilitaIVA(fattura.getEsercizio() > 2014 ? EsigibilitaIVAType.S : EsigibilitaIVAType.D);
							} else {
								datiRiepilogo.setEsigibilitaIVA(EsigibilitaIVAType.I);
							}
							datiRiepilogo.setImponibileImporto(riepilogo.getImponibile().setScale(2));
							datiRiepilogo.setImposta(riepilogo.getImposta().setScale(2));
							impostaDatiNonImponibile(fattura, riepilogo, datiRiepilogo);
							listaRiepilogoType.add(datiRiepilogo);
						}
						datiBeniServizi.getDatiRiepilogo().addAll(listaRiepilogoType);

						fatturaBodyType.setDatiBeniServizi(datiBeniServizi);

						DatiPagamentoType datiPagamento = factory.createDatiPagamentoType();
						datiPagamento.setCondizioniPagamento(CondizioniPagamentoType.TP_02);

						List<DettaglioPagamentoType> listaDettagliPagamento = new ArrayList<DettaglioPagamentoType>();
						if (fattura.getTi_fattura().equals(Fattura_attivaBulk.TIPO_NOTA_DI_CREDITO)) {
							for (Iterator<Fattura_attivaBulk> y = mappaDocumentiCollegati.keySet().iterator(); y.hasNext(); ) {
								Fattura_attivaBulk fatturaCollegata = y.next();
								listaDettagliPagamento.add(impostaDatiPagamento(userContext, terzoCnr, factory, fatturaCollegata));
							}
						} else {
							DettaglioPagamentoType dettaglioPagamento = impostaDatiPagamento(userContext, terzoCnr, factory, fattura);
							dettaglioPagamento.setCodicePagamento(fattura.recuperoIdFatturaAsString());
							listaDettagliPagamento.add(dettaglioPagamento);
						}
						datiPagamento.getDettaglioPagamento().addAll(listaDettagliPagamento);

						List<DatiPagamentoType> listaDatiPagamento = new ArrayList<DatiPagamentoType>();
						listaDatiPagamento.add(datiPagamento);
						fatturaBodyType.getDatiPagamento().addAll(listaDatiPagamento);


						List<FatturaElettronicaBodyType> listaFattureBody = new ArrayList<FatturaElettronicaBodyType>();
						listaFattureBody.add(fatturaBodyType);
						fatturaType.getFatturaElettronicaBody().addAll(listaFattureBody);
					}
				}
			}
			
			return fatturaType;
		} catch(Exception e) {
			throw handleException(e);
		}
		
	}
	
	public JAXBElement<FatturaElettronicaType> creaFatturaElettronicaType(UserContext userContext, IDocumentoAmministrativoElettronicoBulk docammElettronico) throws ComponentException {
		try {

			ObjectFactory factory = new ObjectFactory();
			return factory.createFatturaElettronica(preparaFattura(userContext, docammElettronico));
		} catch(Exception e) {
			throw handleException(e);
		}
	}

	private Boolean gestioneBollo(UserContext userContext,
			ObjectFactory factory,
			DatiGeneraliDocumentoType datiGeneraliDocumento, Fattura_attivaBulk fatturaAttiva, BulkList dettaglio)
			throws ComponentException, ApplicationException {
		
		try {
			Utility.createFatturaAttivaSingolaComponentSession().controlliGestioneBolloVirtuale(userContext, fatturaAttiva, dettaglio);
		} catch (RemoteException | EJBException e) {
			throw new ComponentException(e);
		}
		Boolean esisteBollo = false;
		for (Iterator<Fattura_attiva_rigaBulk> i= dettaglio.iterator(); i.hasNext();) {
			Fattura_attiva_rigaBulk riga= (Fattura_attiva_rigaBulk) i.next();
			if (isRigaFatturaConBollo(userContext, riga)){
				if (esisteBollo){
					throw new ApplicationException("Impossibile Procedere! Esistono più righe con tipo Bollo indicate in fattura"); 
				}
				DatiBolloType datiBollo = factory.createDatiBolloType();
				datiBollo.setBolloVirtuale(BolloVirtualeType.SI);
				datiBollo.setImportoBollo(riga.getIm_imponibile().setScale(2));
				datiGeneraliDocumento.setDatiBollo(datiBollo);
				esisteBollo = true;
			}
		}
		return esisteBollo;
	}

	private Boolean isRigaFatturaConBollo(UserContext userContext, Fattura_attiva_rigaBulk riga) throws ComponentException {
		Bene_servizioBulk beneServizio;
		try {
			beneServizio = ((Bene_servizioBulk)getHome(userContext, Bene_servizioBulk.class).findByPrimaryKey(riga.getBene_servizio()));
		} catch (Exception e) {
			throw handleException(e);
		}
		if (beneServizio != null && beneServizio.getFl_bollo()){
			return true;
		}
		return false;
	}
	private void impostaDatiNonImponibile(IDocumentoAmministrativoElettronicoBulk docammElettronico, RiepilogoPerAliquotaIVA riepilogo, DatiRiepilogoType datiRiepilogo) throws ApplicationException{
		if (datiRiepilogo.getAliquotaIVA().compareTo(BigDecimal.ZERO) == 0){
			String msg = "Impossibile Procedere! Per il documento elettronico: "+docammElettronico.getTipoDocumentoElettronico()+"-"+docammElettronico.getEsercizio()+"-"+docammElettronico.getPg_docamm()+" esiste almeno una riga con aliquota a 0 senza l'indicazione";
			if (riepilogo.getNaturaIvaNonImponibile() == null || riepilogo.getNaturaIvaNonImponibile().equals("")){
				throw new ApplicationException(msg+" della Natura per invio SDI"); 
			}
			datiRiepilogo.setNatura(impostaDatiNatura(riepilogo.getNaturaIvaNonImponibile()));
			if (riepilogo.getRiferimentoNormativaNonImponibile() == null || riepilogo.getRiferimentoNormativaNonImponibile().equals("")){
				throw new ApplicationException(msg+" del riferimento normativo per invio SDI"); 
			}
			datiRiepilogo.setRiferimentoNormativo(riepilogo.getRiferimentoNormativaNonImponibile());
		}
	}

	private void impostaDatiContratto(ObjectFactory factory, DatiGeneraliType datiGenerali, HashMap<ContrattoBulk, List<Integer>> mappaContratti)
			throws DatatypeConfigurationException {
		if (!mappaContratti.isEmpty()){
		    List<DatiDocumentiCorrelatiType> listaDatiContratto = new ArrayList<DatiDocumentiCorrelatiType>();
			for(Iterator<ContrattoBulk> y = mappaContratti.keySet().iterator(); y.hasNext();) {
				ContrattoBulk contratto = y.next();
				List<Integer> listaLinee = mappaContratti.get(contratto);
		        DatiDocumentiCorrelatiType datiContratto = factory.createDatiDocumentiCorrelatiType();
				if (mappaContratti.size() > 1){
					datiContratto.getRiferimentoNumeroLinea().addAll(listaLinee);
				}

		        datiContratto.setIdDocumento(contratto.getEsercizio() + "/" + contratto.getPg_contratto().toString());
		        impostaDatiCigCup(contratto, datiContratto);
		        datiContratto.setData(convertDateToXmlGregorian(contratto.getDt_stipula()));
		        listaDatiContratto.add(datiContratto);
			}
		    
		    datiGenerali.getDatiContratto().addAll(listaDatiContratto);
		}
	}

	private void impostaDatiCigCup(ContrattoBulk contratto,
			DatiDocumentiCorrelatiType datiContratto) {
		if (contratto.getCdCigFatturaAttiva() != null){
		    datiContratto.setCodiceCIG(contratto.getCdCigFatturaAttiva());
		}
		if (contratto.getCup() != null && contratto.getCup().getCdCup() != null){
		    datiContratto.setCodiceCUP(contratto.getCup().getCdCup());
		}
	}

	private void impostaDatiDocumentiCollegati(ObjectFactory factory, DatiGeneraliType datiGenerali, HashMap<Fattura_attivaBulk, HashMap<ContrattoBulk, List<Integer>>> mappaDocumentiCollegati, HashMap<Fattura_attivaBulk,List<Integer>> dettagliNoteSenzaContratto)
			throws DatatypeConfigurationException {
		if (!mappaDocumentiCollegati.isEmpty()){
		    List<DatiDocumentiCorrelatiType> listaDatiDocumentiCollegati = new ArrayList<DatiDocumentiCorrelatiType>();
			for(Iterator<Fattura_attivaBulk> y = mappaDocumentiCollegati.keySet().iterator(); y.hasNext();) {
				Fattura_attivaBulk fattura = y.next();
				HashMap<ContrattoBulk, List<Integer>> mappaContratti = mappaDocumentiCollegati.get(fattura);
				for(Iterator<ContrattoBulk> z = mappaContratti.keySet().iterator(); z.hasNext();) {
					ContrattoBulk contratto = z.next();
					List<Integer> listaLinee = mappaContratti.get(contratto);
			        DatiDocumentiCorrelatiType datiDocumentiCollegati = factory.createDatiDocumentiCorrelatiType();
					if (mappaContratti.size() > 1){
						datiDocumentiCollegati.getRiferimentoNumeroLinea().addAll(listaLinee);
					}

			        datiDocumentiCollegati.setIdDocumento(fattura.recuperoIdFatturaAsString());
			        impostaDatiCigCup(contratto, datiDocumentiCollegati);
			        datiDocumentiCollegati.setData(convertDateToXmlGregorian(fattura.getDt_emissione()));
			        listaDatiDocumentiCollegati.add(datiDocumentiCollegati);
				}
			}
			for(Iterator<Fattura_attivaBulk> z = dettagliNoteSenzaContratto.keySet().iterator(); z.hasNext();) {
				Fattura_attivaBulk fatturaSenzaContratto = z.next();
				List<Integer> listaLinee = dettagliNoteSenzaContratto.get(fatturaSenzaContratto);
		        DatiDocumentiCorrelatiType datiDocumentiCollegati = factory.createDatiDocumentiCorrelatiType();
				if (dettagliNoteSenzaContratto.size() > 1){
					datiDocumentiCollegati.getRiferimentoNumeroLinea().addAll(listaLinee);
				}

		        datiDocumentiCollegati.setIdDocumento(fatturaSenzaContratto.recuperoIdFatturaAsString());
		        datiDocumentiCollegati.setData(convertDateToXmlGregorian(fatturaSenzaContratto.getDt_emissione()));
		        listaDatiDocumentiCollegati.add(datiDocumentiCollegati);
			}
		    
		    datiGenerali.getDatiFattureCollegate().addAll(listaDatiDocumentiCollegati);
		}
	}

	private void preparaDatiContratto(UserContext userContext, 
			HashMap<ContrattoBulk, List<Integer>> mappaContratti,
			Fattura_attiva_rigaBulk riga) throws PersistencyException, ComponentException {
		preparaDatiContratto(userContext, mappaContratti, riga, riga.getProgressivo_riga().intValue());
	}

	private void preparaDatiContratto(UserContext userContext, 
			HashMap<ContrattoBulk, List<Integer>> mappaContratti,
			Fattura_attiva_rigaBulk riga, Integer progressivoRiga) throws PersistencyException, ComponentException {
		ContrattoBulk contrattoBulk = recuperoContrattoCollegato(userContext, riga);
		caricaDatiContratto(mappaContratti, progressivoRiga, contrattoBulk);
	}

	private void caricaDatiContratto(
			HashMap<ContrattoBulk, List<Integer>> mappaContratti,
			Integer progressivoRiga, ContrattoBulk contrattoBulk) {
		if (contrattoBulk != null && contrattoBulk.getEsercizio() != null){
			if (mappaContratti.containsKey(contrattoBulk)){
				mappaContratti.get(contrattoBulk).add(progressivoRiga);
			} else {
				List<Integer> lista = new ArrayList<Integer>();
				lista.add(progressivoRiga);
				mappaContratti.put(contrattoBulk, lista);
			}
		}
	}

	private ContrattoBulk recuperoContrattoCollegato(UserContext userContext, Fattura_attiva_rigaBulk riga) throws PersistencyException, ComponentException {
		if (Optional.ofNullable(riga.getAccertamento_scadenzario()).isPresent()) {
			Accertamento_scadenzarioBulk accSca= ((Accertamento_scadenzarioBulk)getHome(userContext, Accertamento_scadenzarioBulk.class).findByPrimaryKey(riga.getAccertamento_scadenzario()));
			AccertamentoBulk accert = ((AccertamentoBulk)getHome(userContext, AccertamentoBulk.class).findByPrimaryKey(accSca.getAccertamento()));
			if (accert.getContratto() != null && accert.getContratto().getEsercizio() != null){
				ContrattoBulk contrattoBulk= ((ContrattoBulk)getHome(userContext, ContrattoBulk.class).findByPrimaryKey(accert.getContratto()));
				return contrattoBulk;
			}
		}
		return null;
	}
		
	private DettaglioPagamentoType impostaDatiPagamento(UserContext userContext, TerzoBulk terzoCnr, ObjectFactory factory,
			Fattura_attivaBulk fattura)  throws ComponentException, RemoteException, EJBException, PersistencyException {
		DettaglioPagamentoType dettaglioPagamento = factory.createDettaglioPagamentoType();
		Rif_modalita_pagamentoBulk modPag = ((Rif_modalita_pagamentoBulk)getHome(userContext, Rif_modalita_pagamentoBulk.class).findByPrimaryKey(fattura.getModalita_pagamento_uo()));
		
		if (modPag.getTipoPagamentoSdi() == null){
			throw new ApplicationException("Impossibile Procedere! Per la modalità di Pagamento: "+fattura.getModalita_pagamento_uo().getCd_ds_modalita_pagamento()+" non è stato indicato il Tipo Pagamento per SDI"); 
		}
		dettaglioPagamento.setModalitaPagamento(ModalitaPagamentoType.fromValue(fattura.getModalita_pagamento_uo().getTipoPagamentoSdi()));				
			
		if (fattura.getFl_liquidazione_differita() && fattura.getEsercizio() > 2014) {
			dettaglioPagamento.setImportoPagamento(fattura.getIm_totale_imponibile().setScale(2));
		} else {
			dettaglioPagamento.setImportoPagamento(fattura.getIm_totale_fattura().setScale(2));
		}
			
		if (fattura.getBanca_uo() != null && fattura.getBanca_uo().getAbi() != null){
		    dettaglioPagamento.setIstitutoFinanziario(fattura.getBanca_uo().getAbi_cab().getDs_abicab());
		    dettaglioPagamento.setIBAN(fattura.getBanca_uo().getCodice_iban());
		    dettaglioPagamento.setABI(fattura.getBanca_uo().getAbi());
		    dettaglioPagamento.setCAB(fattura.getBanca_uo().getCab());
		    dettaglioPagamento.setBIC(fattura.getBanca_uo().getCodice_swift());
		} else if (fattura.getModalita_pagamento_uo().isModalitaBancaItalia()){
			String conto = Utility.createConfigurazioneCnrComponentSession().getVal01(userContext, it.cnr.contab.utenze00.bp.CNRUserContext.getEsercizio(userContext), null, Configurazione_cnrBulk.PK_CONTO_CORRENTE_BANCA_ITALIA, Configurazione_cnrBulk.SK_CODICE);
			if (conto == null){
				throw new ApplicationException("Impossibile Procedere! Nell'archivio CONFIGURAZIONE_CNR non è inserito il valore relativo al numero di conto in Banca d'Italia"); 
			}
			try {
				dettaglioPagamento.setIBAN(impostaCodicePaese(userContext, terzoCnr)+Utility.lpad(conto,25,'0'));
			} catch (PersistencyException e) {
				throw new ComponentException(e);
			}
		}

		return dettaglioPagamento;
	}
	
	private NaturaType impostaDatiNatura(String natura){
		if (natura != null){
			return NaturaType.fromValue(natura);
		}
		return null;
	}

	private String impostaCodiceFiscale(UserContext userContext, TerzoBulk terzo) throws ComponentException{
		if (terzo.getAnagrafico() == null || terzo.getAnagrafico().getCodice_fiscale() == null)
			throw new ApplicationException("Impossibile Procedere! Manca il Codice Fiscale per il codice Anagrafica: "+terzo.getCd_anag()+" del terzo: "+ terzo.getCd_terzo()); 
		return terzo.getAnagrafico().getCodice_fiscale();
	}

	private String impostaCodicePaese(UserContext userContext, TerzoBulk terzo) throws ComponentException, PersistencyException{
		return impostaCodicePaese(userContext, terzo.getComune_sede());
	}

	private String impostaCodicePaese(UserContext userContext, ComuneBulk comune) throws ComponentException, PersistencyException{
		comune= ((ComuneBulk)getHome(userContext, ComuneBulk.class).findByPrimaryKey(comune));
		NazioneBulk nazione=null;
		if (comune.getNazione().getCd_iso() == null ){
			nazione= ((NazioneBulk)getHome(userContext, NazioneBulk.class).findByPrimaryKey(comune.getNazione()));
		} else {
			nazione = comune.getNazione();
		}
		return nazione.getCd_iso();
	}
	
	private IdFiscaleType impostaIdTrasmittente(UserContext userContext, ObjectFactory factory, TerzoBulk terzo) throws ComponentException, PersistencyException{
		IdFiscaleType idTrasmittente = factory.createIdFiscaleType();
		idTrasmittente.setIdCodice(impostaCodiceFiscale(userContext, terzo));
		idTrasmittente.setIdPaese(impostaCodicePaese(userContext, terzo));
		return idTrasmittente;
	}

	private IdFiscaleType impostaIdFiscale(UserContext userContext, ObjectFactory factory, TerzoBulk terzo, Fattura_attivaBulk fattura) throws ComponentException, PersistencyException{
		IdFiscaleType idFiscale= factory.createIdFiscaleType();
		idFiscale.setIdCodice(fattura == null ? terzo.getAnagrafico().getPartita_iva() : fattura.getPartita_iva());
		idFiscale.setIdPaese(impostaCodicePaese(userContext, terzo));
		return idFiscale;
	}

	private AnagraficaType impostaAnagrafica(ObjectFactory factory, TerzoBulk terzo){
		AnagraficaType anagrafica = factory.createAnagraficaType();
		anagrafica.setDenominazione(substring80(RemoveAccent.convert(terzo.getDenominazione_sede())));
		return anagrafica;
	}
	
	private String substring80(String rit) {
		return substring(rit, 80 );
	}

	private String substring60(String rit) {
		return substring(rit, 60 );
	}

	private String substring(String rit, int caratteri) {
		return rit.length() > caratteri ? rit.substring(0,caratteri) : rit;
	}

	private IndirizzoType impostaIndirizzo(UserContext userContext, ObjectFactory factory, TerzoBulk terzo, IDocumentoAmministrativoElettronicoBulk fattura) throws ComponentException, PersistencyException{
		IndirizzoType indirizzoCedente = factory.createIndirizzoType();
		ComuneBulk comune = (ComuneBulk)findByPrimaryKey(userContext, terzo.getComune_sede());
		
		indirizzoCedente.setComune(substring60(comune.getDs_comune()));
		
		if ((fattura == null || !fattura.isFatturaEstera()) && terzo.getCap_comune_sede() == null){
			throw new ApplicationException("Impossibile Procedere! Manca il CAP per il terzo: "+terzo.getCd_terzo()); 
		} 
		if (terzo.getCap_comune_sede() != null && (fattura == null || !fattura.isFatturaEstera())){
			indirizzoCedente.setCAP(terzo.getCap_comune_sede().length() > 5 ? terzo.getCap_comune_sede().substring(0,5) : terzo.getCap_comune_sede());
		} else {
			indirizzoCedente.setCAP("00000");
		}

		indirizzoCedente.setProvincia(comune.getCd_provincia());
		indirizzoCedente.setNazione(impostaCodicePaese(userContext, comune));
		indirizzoCedente.setIndirizzo(substring60(RemoveAccent.convert(terzo.getVia_sede())));
		indirizzoCedente.setNumeroCivico(terzo.getNumero_civico_sede());
		return indirizzoCedente;
	}

	private IndirizzoType impostaIndirizzo(UserContext userContext, ObjectFactory factory, TerzoBulk terzo) throws ComponentException, PersistencyException{
		return impostaIndirizzo(userContext, factory, terzo, null);
	}

	private java.sql.Timestamp getDataOdierna() {
		return it.cnr.jada.util.ejb.EJBCommonServices.getServerDate();
	}

	private XMLGregorianCalendar getDataOdiernaForXml() throws DatatypeConfigurationException {
		return convertDateToXmlGregorian(getDataOdierna());
	}

	public XMLGregorianCalendar convertDateToXmlGregorian(Date d) throws DatatypeConfigurationException{
		
		if (d==null)
			return null;		
				
		return DatatypeFactory.newInstance().newXMLGregorianCalendar(DATEFORMAT.format(d));
						
	}

	private void impostaDatiPerRiepilogoDatiIva(List<RiepilogoPerAliquotaIVA> listaRiepilogo, IDocumentoAmministrativoRigaBulk riga) {
		RiepilogoPerAliquotaIVA riepilogo = new RiepilogoPerAliquotaIVA();
		riepilogo.setAliquota(Utility.nvl(riga.getVoce_iva().getPercentuale()).setScale(2));
		riepilogo.setNaturaIvaNonImponibile(riga.getVoce_iva().getNaturaOperNonImpSdi());
		riepilogo.setRiferimentoNormativaNonImponibile(riga.getVoce_iva().getRifNormOperNonImpSdi());
		riepilogo.setImponibile(riga.getIm_imponibile());
		riepilogo.setImposta(riga.getIm_iva());
		if (listaRiepilogo.contains(riepilogo)){
	        for (Iterator<RiepilogoPerAliquotaIVA> i= listaRiepilogo.iterator(); i.hasNext();) {
	        	RiepilogoPerAliquotaIVA riepilogoLista= (RiepilogoPerAliquotaIVA) i.next();
	        	if (riepilogoLista.equals(riepilogo)){
	        		riepilogoLista.setImponibile(riepilogoLista.getImponibile().add(riepilogo.getImponibile()));
	        		riepilogoLista.setImposta(riepilogoLista.getImposta().add(riepilogo.getImposta()));
	        		return;
	        	}
	        }
		}
		listaRiepilogo.add(riepilogo);
	}

	private void impostaDatiPerNoteCredito(UserContext userContext, HashMap<Fattura_attivaBulk, HashMap<ContrattoBulk, List<Integer>>> mappaDocumentiCollegati, Fattura_attiva_rigaBulk riga, HashMap<Fattura_attivaBulk,List<Integer>> dettagliSenzaContratto) throws PersistencyException, ComponentException, RemoteException, EJBException {
		Fattura_attiva_rigaIBulk rigaFattura = new Fattura_attiva_rigaIBulk(riga.getCd_cds_assncna_fin(), riga.getCd_uo_assncna_fin(), riga.getEsercizio_assncna_fin(), riga.getPg_fattura_assncna_fin(), riga.getPg_riga_assncna_fin());
		rigaFattura = (Fattura_attiva_rigaIBulk)Utility.createFatturaAttivaSingolaComponentSession().findByPrimaryKey(userContext, rigaFattura);
		Fattura_attivaBulk fattura = rigaFattura.getFattura_attiva();

		if (fattura != null){
			fattura = ((Fattura_attivaBulk)getHome(userContext, Fattura_attivaBulk.class).findByPrimaryKey(fattura));
			if (mappaDocumentiCollegati.containsKey(fattura)){
				HashMap<ContrattoBulk, List<Integer>> mappaContratti = mappaDocumentiCollegati.get(fattura);
				impostaDatiNoteCreditoCollegate(userContext, riga, dettagliSenzaContratto,rigaFattura, fattura, mappaContratti);
			} else {
				HashMap<ContrattoBulk, List<Integer>> mappaContratti = new HashMap<ContrattoBulk, List<Integer>>();
				impostaDatiNoteCreditoCollegate(userContext, riga, dettagliSenzaContratto,rigaFattura, fattura, mappaContratti);
				mappaDocumentiCollegati.put(fattura, mappaContratti);
			}
		}
	}

	private void impostaDatiNoteCreditoCollegate(UserContext userContext, 
			Fattura_attiva_rigaBulk riga,
			HashMap<Fattura_attivaBulk, List<Integer>> dettagliSenzaContratto,
			Fattura_attiva_rigaBulk rigaFattura, Fattura_attivaBulk fattura,
			HashMap<ContrattoBulk, List<Integer>> mappaContratti) throws PersistencyException, ComponentException {
		ContrattoBulk contrattoBulk = recuperoContrattoCollegato(userContext, rigaFattura);
		if (contrattoBulk == null || contrattoBulk.getEsercizio() == null){
			if (dettagliSenzaContratto.containsKey(fattura)){
				dettagliSenzaContratto.get(fattura).add(riga.getProgressivo_riga().intValue());
			} else {
				List<Integer> lista = new ArrayList<Integer>(Arrays.asList(riga.getProgressivo_riga().intValue()));
				dettagliSenzaContratto.put(fattura, lista);
			}
		} else {
			caricaDatiContratto(mappaContratti, riga.getProgressivo_riga().intValue(), contrattoBulk);
		}
	}
	public void aggiornaMetadati(UserContext userContext, Integer esercizio, String cdCds, Long pgFatturaAttiva)throws ComponentException {
		try {
			Fattura_attiva_IHome home = (Fattura_attiva_IHome) getHome(userContext, Fattura_attiva_IBulk.class);
			List<Fattura_attivaBulk> fatture = aggiornaMetadatiDocumenti(esercizio, cdCds, pgFatturaAttiva, home);

			Nota_di_credito_attivaHome home2 = (Nota_di_credito_attivaHome) getHome(userContext, Nota_di_credito_attivaBulk.class);
			List<Fattura_attivaBulk> note = aggiornaMetadatiDocumenti(esercizio, cdCds, pgFatturaAttiva, home2);

		} catch(Exception e) {
			throw handleException(e);
		}
	}

	private List<Fattura_attivaBulk> aggiornaMetadatiDocumenti(Integer esercizio, String cdCds, Long pgFatturaAttiva, Fattura_attivaHome home) throws PersistencyException {
		SQLBuilder sql = home.createSQLBuilder();

		if (cdCds != null){
			sql.addSQLClause("AND", "CD_CDS_ORIGINE", sql.EQUALS, cdCds);
		}
		sql.addSQLClause("AND", "ESERCIZIO", sql.EQUALS, esercizio);
		if (pgFatturaAttiva != null){
			sql.addSQLClause("AND", "PG_FATTURA_ATTIVA", sql.EQUALS, pgFatturaAttiva);
		}

		List<Fattura_attivaBulk> fatture = home.fetchAll(sql);

		for (Fattura_attivaBulk fattura_attivaBulk : fatture) {
			home.aggiornaMetadatiFattura(fattura_attivaBulk);
		}
		return fatture;
	}

	public IDocumentoAmministrativoElettronicoBulk castDocumentoElettronico(UserContext userContext, VDocammElettroniciAttiviBulk docamm) throws ComponentException {
		try {
			if (docamm.isFatturaAttiva()) {
				Fattura_attiva_IBulk fatturaAttivaIBulk = new Fattura_attiva_IBulk();
				fatturaAttivaIBulk.setEsercizio(docamm.getEsercizio());
				fatturaAttivaIBulk.setCd_cds(docamm.getCd_cds());
				fatturaAttivaIBulk.setCd_unita_organizzativa(docamm.getCd_unita_organizzativa());
				fatturaAttivaIBulk.setPg_fattura_attiva(docamm.getPg_docamm());
				return (Fattura_attiva_IBulk)Utility.createFatturaAttivaSingolaComponentSession().inizializzaBulkPerModifica(userContext, fatturaAttivaIBulk);
			} else {
				AutofatturaBulk autofattura = ((VDocammElettroniciAttiviHome) getHome(userContext, VDocammElettroniciAttiviBulk.class)).findAutofattura(docamm);

				//Carico il terzo documento elettronico
				if (autofattura.getFlTerzoEnte()) {
					Unita_organizzativaBulk uoOrigine = (Unita_organizzativaBulk) getHome(userContext, Unita_organizzativaBulk.class).findByPrimaryKey(new Unita_organizzativaBulk(autofattura.getCd_uo_origine()));
					autofattura.setTerzoDocumentoElettronico(Utility.createTerzoComponentSession().cercaTerzoPerUnitaOrganizzativa(userContext, uoOrigine));
				} else {
					Integer cdTerzo = Optional.ofNullable(autofattura.getFattura_passiva()).map(Fattura_passivaBase::getCd_terzo).orElse(null);
					if (cdTerzo != null)
						autofattura.setTerzoDocumentoElettronico((TerzoBulk) getHome(userContext, TerzoBulk.class).findByPrimaryKey(new TerzoKey(cdTerzo)));
				}
				return autofattura;
			}
		} catch(Exception e) {
			throw handleException(e);
		}
    }

    public IDocumentoAmministrativoElettronicoBulk aggiornaDocammElettronicoPredispostoAllaFirma(UserContext userContext, IDocumentoAmministrativoElettronicoBulk docammElettronico) throws ComponentException, RemoteException {
		try {
			docammElettronico.setStatoInvioSdi(VDocammElettroniciAttiviBulk.FATT_ELETT_PREDISPOSTA_FIRMA);
			((OggettoBulk)docammElettronico).setToBeUpdated();
			updateBulk(userContext, (OggettoBulk) docammElettronico);
			return docammElettronico;
		} catch(Exception e) {
			throw handleException(e);
		}
	}

	public IDocumentoAmministrativoElettronicoBulk aggiornaFatturaConsegnaSDI(UserContext userContext, IDocumentoAmministrativoElettronicoBulk docammElettronico, Date dataConsegnaSdi) throws ComponentException, RemoteException {
		try {
			docammElettronico.setStatoInvioSdi(VDocammElettroniciAttiviBulk.FATT_ELETT_CONSEGNATA_SDI);
			docammElettronico.setDtRicezioneSdi(dataConsegnaSdi!=null?new Timestamp(dataConsegnaSdi.getTime()):null);
			((OggettoBulk)docammElettronico).setToBeUpdated();
			updateBulk(userContext, (OggettoBulk) docammElettronico);
			return docammElettronico;
		} catch(Exception e) {
			throw handleException(e);
		}
	}

	public IDocumentoAmministrativoElettronicoBulk aggiornaFatturaMancataConsegnaInvioSDI(UserContext userContext, IDocumentoAmministrativoElettronicoBulk docammElettronico, String codiceSdi, String noteInvioSdi) throws ComponentException, java.rmi.RemoteException {
		try {
			docammElettronico.setStatoInvioSdi(VDocammElettroniciAttiviBulk.FATT_ELETT_MANCATA_CONSEGNA);
			docammElettronico.setCodiceInvioSdi(codiceSdi);
			docammElettronico.setNoteInvioSdi(impostaNoteSdi(noteInvioSdi));
			((OggettoBulk)docammElettronico).setToBeUpdated();
			updateBulk(userContext, (OggettoBulk)docammElettronico);
			if (docammElettronico.getCodiceUnivocoUfficioIpa() != null){
				gestioneEmailUtenteAvvisoFatturaElettronica(userContext, docammElettronico);
			}
			return docammElettronico;
		} catch(Exception e) {
			throw handleException(e);
		}
	}

	private String impostaNoteSdi(String noteSdi) {
		return noteSdi == null || noteSdi.length() <= 500 ? noteSdi : noteSdi.substring(0, 499);
	}

	private void gestioneEmailUtenteAvvisoFatturaElettronica(UserContext userContext, IDocumentoAmministrativoElettronicoBulk docammElettronico) throws ComponentException {
		String msg = "Si comunica che occorre inviare al cliente il documento  "+docammElettronico.getTipoDocumentoElettronico()+"-"+docammElettronico.getCd_uo_origine()+"-"+docammElettronico.getEsercizio()+"-"+docammElettronico.getPg_docamm()+".";
		String subject = "IMPORTANTE: Necessario inviare al cliente la fattura";

		if (docammElettronico.isFatturaEstera() || (docammElettronico.getCodiceDestinatarioFatt() == null &&
				docammElettronico.getPecFatturaElettronica() == null)){
			String eMail = recuperoEmailUtente(userContext, docammElettronico);
			if (eMail != null){
				List<String> list = new ArrayList<>();
				list.add(eMail);
				if (docammElettronico.isFatturaEstera()){
					SendMail.sendMail(subject, msg, list);
				} else {
					if (docammElettronico.getPartita_iva() == null){
						String msgPersonaFisica = msg+" Bisogna inoltre avvisarlo che può visualizzare la fattura elettronica nella sua area riservata del sito WEB dell'agenzia delle entrate.";
						SendMail.sendMail(subject, msgPersonaFisica, list);
					} else {
						msg = "Comunicare al cliente che la fattura elettronica "+docammElettronico.getTipoDocumentoElettronico()+"-"+docammElettronico.getCd_uo_origine()+"-"+docammElettronico.getEsercizio()+"-"+docammElettronico.getPg_docamm()+" si trova nella sua area riservata del sito WEB dell'agenzia delle entrate.";
						subject = "Necessario comunicare al cliente trasmissione fattura elettronica";
						SendMail.sendMail(subject, msg, list);
					}
				}
			}
		}
	}

	public String recuperoEmailUtente(UserContext aUC, IDocumentoAmministrativoElettronicoBulk docammElettronico) throws ComponentException {
		UtenteBulk utente = new UtenteBulk(docammElettronico.getUtcr());
		try {
			utente = (UtenteBulk) getHome(aUC, UtenteBulk.class).findByPrimaryKey(utente);
			if (utente != null){
				if (utente.getCd_utente_uid() != null){
					return utente.getCd_utente_uid()+"@cnr.it";
				}

				String msg = "Si comunica che occorre inviare al cliente il documento  "+docammElettronico.getTipoDocumentoElettronico()+"-"+docammElettronico.getCd_uo_origine()+"-"+docammElettronico.getEsercizio()+"-"+docammElettronico.getPg_docamm()+".";
				String subject = "Email utente non trovata IMPORTANTE: Necessario inviare al cliente la fattura";
				SendMail.sendErrorMail(subject, msg);
			}
		} catch (PersistencyException e) {
			throw new ComponentException(e);
		}
		return null;
	}
}

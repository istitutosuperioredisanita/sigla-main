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

package it.cnr.contab.doccont00.comp;

import it.cnr.contab.doccont00.core.bulk.ArchiviaConsSostitutivaBulk;
import it.cnr.contab.doccont00.ejb.DistintaCassiereComponentSession;
import it.cnr.contab.doccont00.intcass.bulk.Distinta_cassiereBulk;
import it.cnr.contab.doccont00.service.DocumentiContabiliService;
import it.cnr.contab.logs.bulk.Batch_log_rigaBulk;
import it.cnr.contab.logs.bulk.Batch_log_tstaBulk;
import it.cnr.contab.logs.ejb.BatchControlComponentSession;
import it.cnr.contab.service.SpringUtil;
import it.cnr.contab.util.SIGLAStoragePropertyNames;
import it.cnr.contab.util.Utility;
import it.cnr.contab.util00.bulk.storage.AllegatoParentIBulk;
import it.cnr.jada.DetailedRuntimeException;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.util.SendMail;
import it.cnr.jada.util.ejb.EJBCommonServices;
import it.cnr.si.spring.storage.StorageObject;
import it.cnr.si.spring.storage.StoreService;
import it.cnr.si.spring.storage.config.StoragePropertyNames;
import jakarta.ejb.Asynchronous;
import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;

import jakarta.ejb.Stateless;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Stateless(name = "CNRCOEPCOAN00_EJB_ConsSostitutivaComponentSession")
public class AsyncConsSostitutivaComponentSessionBean extends it.cnr.jada.ejb.CRUDComponentSessionBean implements AsyncConsSostitutivaComponentSession {
	private final static org.slf4j.Logger logger = LoggerFactory.getLogger(AsyncConsSostitutivaComponentSessionBean.class);



	@Asynchronous
	@Override
	public void asyncConsSostitutiva(UserContext userContext, Integer esercizio) throws ComponentException, PersistencyException, RemoteException {
		String subjectError = "Errore Conservazione Sostitutiva";
		Boolean hasError = Boolean.FALSE;
		try{
			ArchiviaConsSostitutivaBulk archiviaConsSostitutivaBulk = new ArchiviaConsSostitutivaBulk();
			archiviaConsSostitutivaBulk.setEsercizio(esercizio);
			DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").withZone(ZoneId.systemDefault());
			Batch_log_tstaBulk log = new Batch_log_tstaBulk();
			log.setDs_log("Batch Preparazione Pacchetto Doc. Sostitutiva");
			log.setCd_log_tipo(Batch_log_tstaBulk.LOG_TIPO_CONS_SOSTITUIVA);
			log.setNote("Batch Preparazione Pacchetto Doc. Sostitutiva. Esercizio: " + archiviaConsSostitutivaBulk.getEsercizio() +
						" Start: " + formatterTime.format(EJBCommonServices.getServerTimestamp().toInstant()));
			log.setToBeCreated();

			BatchControlComponentSession batchControlComponentSession = (BatchControlComponentSession) EJBCommonServices
					.createEJB("BLOGS_EJB_BatchControlComponentSession");
			try {
				log = (Batch_log_tstaBulk) batchControlComponentSession.creaConBulkRequiresNew(userContext, log);
			} catch (ComponentException | RemoteException ex) {
				SendMail.sendErrorMail(subjectError, "Errore durante l'inserimento della riga di testata di Batch_log " + ex.getMessage());
				throw new ComponentException(ex);
			}
			final Batch_log_tstaBulk logDB = log;
			List<Batch_log_rigaBulk> listLogRighe = new ArrayList<>();
			Integer righeElabrate=0;
			try {

				List<String> listInsert = new ArrayList<>();
				List<String> listError = new ArrayList<>();
				try {
					Integer righeDistintaTes =distinte(userContext,archiviaConsSostitutivaBulk,Distinta_cassiereBulk.Tesoreria.BANCA_TESORIERE,logDB,listLogRighe,listInsert,listError);
					hasError=( hasError||listError.size()>0);
					righeElabrate=righeElabrate+righeDistintaTes;
				} catch (ComponentException | RemoteException  ex) {
					SendMail.sendErrorMail(subjectError, "Errore durante la lettura delle Distinte "+ Distinta_cassiereBulk.Tesoreria.BANCA_TESORIERE.value()+" dell'esercizio " + esercizio + " - Errore: " + ex.getMessage());
					throw new DetailedRuntimeException(ex);
				}
				try {
					Integer righeDistintaIta =distinte(userContext,archiviaConsSostitutivaBulk,Distinta_cassiereBulk.Tesoreria.BANCA_ITALIA,logDB,listLogRighe,listInsert,listError);
					hasError=( hasError||listError.size()>0);
					righeElabrate=righeElabrate+righeDistintaIta;
				} catch (ComponentException | RemoteException  ex) {
					SendMail.sendErrorMail(subjectError, "Errore durante la lettura delle Distinte "+ Distinta_cassiereBulk.Tesoreria.BANCA_ITALIA.value()+" dell'esercizio " + esercizio + " - Errore: " + ex.getMessage());
					throw new DetailedRuntimeException(ex);
				}
				Batch_log_rigaBulk log_riga = new Batch_log_rigaBulk();
				log_riga.setPg_esecuzione(logDB.getPg_esecuzione());
				log_riga.setPg_riga(BigDecimal.valueOf(listLogRighe.size() + 1));
				log_riga.setTi_messaggio("I");
				log_riga.setMessaggio("Esercizio: " + esercizio + " - Righe elaborate: " + righeElabrate+ " - Righe processate: " + listInsert.size() + " - Errori: " + listError.size());
				log_riga.setNote("Termine operazione creazione automatica Documentazione Sostitutiva Esercizio: " + archiviaConsSostitutivaBulk.getEsercizio() + ".");
				log_riga.setToBeCreated();
				try {
					listLogRighe.add((Batch_log_rigaBulk) batchControlComponentSession.creaConBulkRequiresNew(userContext, log_riga));
				} catch (ComponentException | RemoteException ex) {
					SendMail.sendErrorMail(subjectError, "Errore durante l'inserimento della riga di chiusura di Batch_log_riga " + ex.getMessage());
					throw new DetailedRuntimeException(ex);
				}

			} catch (Exception ex) {
				Batch_log_rigaBulk log_riga = new Batch_log_rigaBulk();
				log_riga.setPg_esecuzione(logDB.getPg_esecuzione());
				log_riga.setPg_riga(BigDecimal.valueOf(listLogRighe.size() + 1));
				log_riga.setTi_messaggio("E");
				log_riga.setMessaggio("Creazione automatica Documentazione Sostitutiva in errore. Errore: " + ex.getMessage());
				log_riga.setNote("Termine operazione creazione automatica Documentazione Sostitutiva." + formatterTime.format(EJBCommonServices.getServerTimestamp().toInstant()));
				log_riga.setToBeCreated();
				try {
					listLogRighe.add((Batch_log_rigaBulk) batchControlComponentSession.creaConBulkRequiresNew(userContext, log_riga));
				} catch (ComponentException | RemoteException ex2) {
					SendMail.sendErrorMail("Errore creazione automatica Documentazione Sostitutiva", "Errore durante l'inserimento della riga di chiusura di Batch_log_riga " + ex2.getMessage());
					throw new DetailedRuntimeException(ex);
				}
			}

			log.setNote(log.getNote()+" - End: "+ formatterTime.format(EJBCommonServices.getServerTimestamp().toInstant()));
			// aggiorna che ha errori
			if (hasError) {
				logDB.setFl_errori(true);
			}
			log.setToBeUpdated();
			try {
				batchControlComponentSession.modificaConBulkRequiresNew(userContext, log);
			} catch (ComponentException | RemoteException ex) {
				SendMail.sendErrorMail(subjectError, "Errore durante l'aggiornamento della riga di testata di Batch_log " + ex.getMessage());
				throw new ComponentException(ex);
			}
		} catch (DetailedRuntimeException ex) {
			logger.error("Creazione automatica Documentazione Sostitutiva errore. Errore: " + ex.getMessage());
		} catch (Exception ex) {
			logger.error("Creazione automatica Documentazione Sostitutiva in errore. Errore: " + ex.getMessage());
			SendMail.sendErrorMail(subjectError, "Creazione automatica Documentazione Sostitutiva in errore. Errore: " + ex.getMessage());
			throw ex;
		}
	}

	private List<StorageObject> getStorageObjectDistinte(UserContext userContext, Distinta_cassiereBulk distinta,DocumentiContabiliService documentiContabiliService) throws ComponentException {
		final String storePath = distinta.getStorePath();
		final String baseIdentificativoFlusso = distinta.getBaseIdentificativoFlusso();
		List<StorageObject> toSendCons= new ArrayList<>();
		try {
			return documentiContabiliService.getChildren(documentiContabiliService.getStorageObjectByPath(storePath).getKey())
					.stream()
					.filter(storageObject1 -> storageObject1.<String>getPropertyValue(StoragePropertyNames.NAME.value())
							.startsWith(baseIdentificativoFlusso)).
					filter(storageObject1 -> documentiContabiliService.hasAspect(storageObject1,SIGLAStoragePropertyNames.CNR_SIGNEDDOCUMENT.value())).
					collect(Collectors.toList());

		}catch(Exception e){
			throw new ComponentException(" Eccezione nel recupero del Documento Flusso " );
		}
	}


	private Integer distinte (UserContext userContext,ArchiviaConsSostitutivaBulk archiviaConsSostitutivaBulk,
						   Distinta_cassiereBulk.Tesoreria tipoDistinta,
						   Batch_log_tstaBulk logDB,
						   List<Batch_log_rigaBulk> listLogRighe,
						   List<String> listInsertAll,
						   List<String> listErrorAll
						   ) throws ComponentException, RemoteException {
		String subjectError = "Errore Conservazione Sostitutiva";
		List<String> listInsert= new ArrayList<>();
		List<String> listError= new ArrayList<>();
		DocumentiContabiliService documentiContabiliService = SpringUtil.getBean("documentiContabiliService", DocumentiContabiliService.class);
		List<Distinta_cassiereBulk> distinteCassiere;
		BatchControlComponentSession batchControlComponentSession = (BatchControlComponentSession) EJBCommonServices
				.createEJB("BLOGS_EJB_BatchControlComponentSession");
		DistintaCassiereComponentSession distintaCassiereComponentSession = Utility.createDistintaCassiereComponentSession();
		Distinta_cassiereBulk distintaCassiereBulk = new Distinta_cassiereBulk();
		distintaCassiereBulk.setEsercizio(archiviaConsSostitutivaBulk.getEsercizio());
		distintaCassiereBulk.setCd_tesoreria( tipoDistinta.value());
		List<Distinta_cassiereBulk> distinte= distintaCassiereComponentSession.findDistinteToConservazione(userContext,archiviaConsSostitutivaBulk.getEsercizio(),null,tipoDistinta);

		ZipOutputStream zos = null;
		ByteArrayOutputStream baos=null;
		baos=new ByteArrayOutputStream();
		zos = new ZipOutputStream(baos);
		zos.setLevel(Deflater.DEFAULT_COMPRESSION);
		for ( Distinta_cassiereBulk distinta:distinte){
			try{
				List<StorageObject> storageObjectsDistinte = getStorageObjectDistinte( userContext,distinta,documentiContabiliService);
				if ( Optional.ofNullable(storageObjectsDistinte).isPresent()){
					if ( storageObjectsDistinte.size()==0)
						throw new  ComponentException(" Non esite il flusso firmato " );
					for ( StorageObject so:storageObjectsDistinte){
						InputStream is = documentiContabiliService.getResource(so);
						addToZip(so,zos,is);
					}
				}
				listInsert.add("X");
				Batch_log_rigaBulk log_riga = new Batch_log_rigaBulk();
				log_riga.setPg_esecuzione(logDB.getPg_esecuzione());
				log_riga.setPg_riga(BigDecimal.valueOf(listLogRighe.size() + 1));
				log_riga.setTi_messaggio("I");
				log_riga.setMessaggio("Distinta Elaborata :"
						+ "-Cds:" + distinta.getCd_cds()
						+ "-Esercizio:" + distinta.getEsercizio()
						+ "-Unità Organizzativa.:" + distinta.getCd_unita_organizzativa()
						+ "-Numero Distinta Prov.:" + distinta.getPg_distinta()
						+ "-Numero Distinta Def.:" + distinta.getPg_distinta_def());
				log_riga.setTrace(log_riga.getMessaggio());
				log_riga.setToBeCreated();
				try {
					listLogRighe.add((Batch_log_rigaBulk) batchControlComponentSession.creaConBulkRequiresNew(userContext, log_riga));
				} catch (ComponentException | RemoteException ex) {
					SendMail.sendErrorMail(subjectError, "Errore durante l'inserimento dell'errore in Batch_log_rigaBulk " + ex.getMessage());
					throw new DetailedRuntimeException(ex);
				}

			}catch (Exception e){
				listError.add("X");
				Batch_log_rigaBulk log_riga = new Batch_log_rigaBulk();
				log_riga.setPg_esecuzione(logDB.getPg_esecuzione());
				log_riga.setPg_riga(BigDecimal.valueOf(listLogRighe.size() + 1));
				log_riga.setTi_messaggio("E");
				log_riga.setMessaggio("Distinta Elaborata :"
						+ "-Cds:" + distinta.getCd_cds()
						+ "-Esercizio:" + distinta.getEsercizio()
						+ "-Unità Organizzativa.:" + distinta.getCd_unita_organizzativa()
						+ "-Numero Distinta Prov.:" + distinta.getPg_distinta()
						+ "-Numero Distinta Def.:" + distinta.getPg_distinta_def());
				log_riga.setTrace(log_riga.getMessaggio());
				log_riga.setNote(e.getMessage().substring(0, Math.min(e.getMessage().length(), 3999)));
				log_riga.setToBeCreated();
				try {
					listLogRighe.add((Batch_log_rigaBulk) batchControlComponentSession.creaConBulkRequiresNew(userContext, log_riga));
				} catch (ComponentException | RemoteException ex) {
					SendMail.sendErrorMail(subjectError, "Errore durante l'inserimento dell'errore in Batch_log_rigaBulk " + ex.getMessage());
					throw new DetailedRuntimeException(e);
				}
			}
		}
		try {
			zos.closeEntry();
			baos.flush();
			zos.flush();
			zos.close();
			baos.close();
			saveFileZip( baos,archiviaConsSostitutivaBulk,tipoDistinta.value());
		}catch (IOException e){
			Batch_log_rigaBulk log_riga = new Batch_log_rigaBulk();
			log_riga.setPg_esecuzione(logDB.getPg_esecuzione());
			log_riga.setPg_riga(BigDecimal.valueOf(listLogRighe.size() + 1));
			log_riga.setTi_messaggio("E");
			log_riga.setMessaggio("Errore Scrittura file Zip");
			log_riga.setTrace(log_riga.getMessaggio());
			log_riga.setNote(e.getMessage().substring(0, Math.min(e.getMessage().length(), 3999)));
			log_riga.setToBeCreated();
			try {
				listLogRighe.add((Batch_log_rigaBulk) batchControlComponentSession.creaConBulkRequiresNew(userContext, log_riga));
			} catch (ComponentException | RemoteException ex) {
				SendMail.sendErrorMail(subjectError, "Errore durante l'inserimento dell'errore in Batch_log_rigaBulk " + ex.getMessage());
				throw new DetailedRuntimeException(e);
			}
		}

		Batch_log_rigaBulk log_riga = new Batch_log_rigaBulk();
		log_riga.setPg_esecuzione(logDB.getPg_esecuzione());
		log_riga.setPg_riga(BigDecimal.valueOf(listLogRighe.size() + 1));
		log_riga.setTi_messaggio("I");
		log_riga.setMessaggio("Esercizio: " + archiviaConsSostitutivaBulk.getEsercizio() + "Distinte " +tipoDistinta.value()+ " - Righe elaborate: " + distinte.size() + " - Righe processate: " + listInsert.size() + " - Errori: " + listError.size());
		log_riga.setNote("Termine operazione creazione automatica Doc. Sost. Distinte tipo :"+tipoDistinta.value() +" Esercizio "+ archiviaConsSostitutivaBulk.getEsercizio() + ".");
		log_riga.setToBeCreated();
		try {
			listLogRighe.add((Batch_log_rigaBulk) batchControlComponentSession.creaConBulkRequiresNew(userContext, log_riga));
		} catch (ComponentException | RemoteException ex) {
			SendMail.sendErrorMail(subjectError, "Errore durante l'inserimento della riga di chiusura di Batch_log_riga " + ex.getMessage());
			throw new DetailedRuntimeException(ex);
		}
		listInsertAll.addAll(listInsert);
		listErrorAll.addAll(listError);
		return distinte.size();

	}

	private void addToZip(StorageObject so, ZipOutputStream zos,InputStream inputStream) {
		final StoreService storeService = SpringUtil.getBean("storeService",	StoreService .class);
		try {
			ZipEntry zipEntryChild = new ZipEntry(so.<String>getPropertyValue(StoragePropertyNames.NAME.value()));
			zos.putNextEntry(zipEntryChild);
			IOUtils.copyLarge(inputStream, zos);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	private void saveFileZip(ByteArrayOutputStream baos, ArchiviaConsSostitutivaBulk archiviaConsSostitutivaBulk,String tipoDocumento){
		final StoreService storeService = SpringUtil.getBean("storeService",	StoreService .class);
		final StorageObject storageObject = storeService.storeSimpleDocument(
				new ByteArrayInputStream(baos.toByteArray()),
				"application/zip",
                    storeService.getStorageObjectByPath(AllegatoParentIBulk.getStorePath("Consercazione Sostituva", archiviaConsSostitutivaBulk.getEsercizio()), true).getPath(),
				Stream.of(
								new AbstractMap.SimpleEntry<>(StoragePropertyNames.NAME.value(), "ConsSostitutiva-".concat(tipoDocumento).concat(archiviaConsSostitutivaBulk.getEsercizio().toString()).concat(".zip")),
								new AbstractMap.SimpleEntry<>(StoragePropertyNames.SECONDARY_OBJECT_TYPE_IDS.value(), Arrays.asList("P:cm:titled")),
								new AbstractMap.SimpleEntry<>(StoragePropertyNames.TITLE.value(),"ConsSostitutiva Sost.".concat( tipoDocumento) .concat(" ").concat(archiviaConsSostitutivaBulk.getEsercizio().toString())),
								new AbstractMap.SimpleEntry<>(StoragePropertyNames.OBJECT_TYPE_ID.value(), "cmis:document"))
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
            );
	}
}

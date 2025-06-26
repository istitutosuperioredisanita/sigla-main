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
import it.cnr.contab.service.SpringUtil;
import it.cnr.contab.util.Utility;
import it.cnr.contab.util00.bulk.storage.AllegatoParentIBulk;
import it.cnr.jada.DetailedRuntimeException;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.util.SendMail;
import it.cnr.si.spring.storage.StorageObject;
import it.cnr.si.spring.storage.StoreService;
import it.cnr.si.spring.storage.config.StoragePropertyNames;
import org.apache.commons.io.IOUtils;
import org.slf4j.LoggerFactory;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Stateless(name = "CNRCOEPCOAN00_EJB_ConsSostitutivaComponentSession")
public class AsyncConsSostitutivaComponentSessionBean extends it.cnr.jada.ejb.CRUDComponentSessionBean implements AsyncConsSostitutivaComponentSession {
	private final static org.slf4j.Logger logger = LoggerFactory.getLogger(AsyncConsSostitutivaComponentSessionBean.class);

    public static it.cnr.jada.ejb.CRUDComponentSessionBean newInstance() throws javax.ejb.EJBException {
        return new AsyncConsSostitutivaComponentSessionBean();
    }
	DocumentiContabiliService documentiContabiliService = SpringUtil.getBean("documentiContabiliService", DocumentiContabiliService.class);
	StoreService storeService = SpringUtil.getBean("storeService",	StoreService .class);

	@Asynchronous
	@Override
	public void asyncConsSostitutiva(UserContext param0, Integer esercizio) throws ComponentException, PersistencyException, RemoteException {
		String subjectError = "Errore Conservazione Sostitutiva";
		try {
			ArchiviaConsSostitutivaBulk archiviaConsSostitutivaBulk = new ArchiviaConsSostitutivaBulk();
			archiviaConsSostitutivaBulk.setEsercizio(esercizio);
			distinte(param0,archiviaConsSostitutivaBulk,Distinta_cassiereBulk.Tesoreria.BANCA_TESORIERE);
		} catch (DetailedRuntimeException ex) {
			logger.error("Creazione pacchetto documentazione sostitutiva in errore. Errore: " + ex.getMessage());
		} catch (Exception ex) {
			logger.error("Creazione pacchetto documentazione sostitutiva in errore. Errore: " + ex.getMessage());
			SendMail.sendErrorMail(subjectError, "Creazione automatica Obbligazioni Pluriennali in errore. Errore: " + ex.getMessage());
			throw ex;
		}
			/*
			DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").withZone(ZoneId.systemDefault());
			Batch_log_tstaBulk log = new Batch_log_tstaBulk();
			log.setDs_log("Batch Preparazione Pacchetto Doc. Sostitutiva");
			log.setCd_log_tipo(Batch_log_tstaBulk.LOG_TIPO_CONS_SOSTITUIVA);
			log.setNote("Batch di creazione Obb. Pluriennali Java. Esercizio: " + esercizio +
						" Start: " + formatterTime.format(EJBCommonServices.getServerTimestamp().toInstant()));
			log.setToBeCreated();

			BatchControlComponentSession batchControlComponentSession = (BatchControlComponentSession) EJBCommonServices
					.createEJB("BLOGS_EJB_BatchControlComponentSession");
			try {
				log = (Batch_log_tstaBulk) batchControlComponentSession.creaConBulkRequiresNew(param0, log);
			} catch (ComponentException | RemoteException ex) {
				SendMail.sendErrorMail(subjectError, "Errore durante l'inserimento della riga di testata di Batch_log " + ex.getMessage());
				throw new ComponentException(ex);
			}

			final Batch_log_tstaBulk logDB = log;
			List<Batch_log_rigaBulk> listLogRighe = new ArrayList<>();

			try {
				List<String> listCdCds = new ArrayList<>();
				List<Obbligazione_pluriennaleBulk> allObbPluriennali;
				List<String> listInsert = new ArrayList<>();
				List<String> listError = new ArrayList<>();

				try {
					allObbPluriennali = session.findObbligazioniPluriennali(param0, esercizio);
				} catch (ComponentException | RemoteException  ex) {
					SendMail.sendErrorMail(subjectError, "Errore durante la lettura delle Obbligazioni Pluriennali dell'esercizio " + esercizio + " - Errore: " + ex.getMessage());
					throw new DetailedRuntimeException(ex);
				}
				allObbPluriennali.stream()
						.forEach(obbligazione -> {
					try {

						logger.info("Obbligazione in elaborazione: ".
								concat(obbligazione.getAnno().toString()).concat("/").
								concat(obbligazione.getCdCds()).concat("/").
								concat(obbligazione.getEsercizio().toString()).concat("/").
								concat(obbligazione.getEsercizioOriginale().toString()).concat("/").
								concat(obbligazione.getPgObbligazione().toString()));
						ObbligazioneBulk obbligazioneNew = session.createObbligazioneNew(param0, obbligazione,esercizio,gaeIniziale);

						Batch_log_rigaBulk log_riga = new Batch_log_rigaBulk();
						log_riga.setPg_esecuzione(logDB.getPg_esecuzione());
						log_riga.setPg_riga(BigDecimal.valueOf(listLogRighe.size() + 1));
						log_riga.setTi_messaggio("I");
						log_riga.setMessaggio("Obbligazione Elaborata :"
								+ "-Cds:" + obbligazione.getCdCds()
								+ "-Esercizio:" + obbligazione.getEsercizio()
								+ "-Esercizio Orig.:" + obbligazione.getEsercizioOriginale()
								+ "-Numero Obbl.:" + obbligazione.getPgObbligazione()
								+" - Creata Obbligazione : "
								+ "-Cds:" + obbligazioneNew.getCds().getCd_ds_cds()
								+ "-Esercizio:" + obbligazioneNew.getEsercizio()
								+ "-Esercizio Orig.:" + obbligazioneNew.getEsercizio_originale()
								+ "-Numero Obbl.:" + obbligazioneNew.getPg_obbligazione());
						log_riga.setTrace(log_riga.getMessaggio());
						log_riga.setToBeCreated();
						try {
							listLogRighe.add((Batch_log_rigaBulk) batchControlComponentSession.creaConBulkRequiresNew(param0, log_riga));
						} catch (ComponentException | RemoteException ex) {
							SendMail.sendErrorMail(subjectError, "Errore durante l'inserimento dell'errore in Batch_log_rigaBulk " + ex.getMessage());
							throw new DetailedRuntimeException(ex);
						}



						listInsert.add("X");
					} catch (Throwable e) {
						listError.add("X");
						Batch_log_rigaBulk log_riga = new Batch_log_rigaBulk();
						log_riga.setPg_esecuzione(logDB.getPg_esecuzione());
						log_riga.setPg_riga(BigDecimal.valueOf(listLogRighe.size() + 1));
						log_riga.setTi_messaggio("E");
						log_riga.setMessaggio("Obbligazione Pluriennale Anno:" + obbligazione.getAnno()
										+ "-Cds:" + obbligazione.getCdCds()
										+ "-Esercizio:" + obbligazione.getEsercizio()
										+ "-Esercizio Orig.:" + obbligazione.getEsercizioOriginale()
										+ "-Numero Obbl.:" + obbligazione.getPgObbligazione());
						log_riga.setTrace(log_riga.getMessaggio());
						log_riga.setNote(e.getMessage().substring(0, Math.min(e.getMessage().length(), 3999)));
						log_riga.setToBeCreated();
						try {
							listLogRighe.add((Batch_log_rigaBulk) batchControlComponentSession.creaConBulkRequiresNew(param0, log_riga));
						} catch (ComponentException | RemoteException ex) {
							SendMail.sendErrorMail(subjectError, "Errore durante l'inserimento dell'errore in Batch_log_rigaBulk " + ex.getMessage());
							throw new DetailedRuntimeException(e);
						}
					}
				});

				Batch_log_rigaBulk log_riga = new Batch_log_rigaBulk();
				log_riga.setPg_esecuzione(logDB.getPg_esecuzione());
				log_riga.setPg_riga(BigDecimal.valueOf(listLogRighe.size() + 1));
				log_riga.setTi_messaggio("I");
				log_riga.setMessaggio("Esercizio: " + esercizio + " - Righe elaborate: " + allObbPluriennali.size() + " - Righe processate: " + listInsert.size() + " - Errori: " + listError.size());
				log_riga.setNote("Termine operazione creazione automatica Obbligazioni Pluriennali Esercizio: " + esercizio + ".");
				log_riga.setToBeCreated();
				try {
					listLogRighe.add((Batch_log_rigaBulk) batchControlComponentSession.creaConBulkRequiresNew(param0, log_riga));
				} catch (ComponentException | RemoteException ex) {
					SendMail.sendErrorMail(subjectError, "Errore durante l'inserimento della riga di chiusura di Batch_log_riga " + ex.getMessage());
					throw new DetailedRuntimeException(ex);
				}
			} catch (Exception ex) {
				Batch_log_rigaBulk log_riga = new Batch_log_rigaBulk();
				log_riga.setPg_esecuzione(logDB.getPg_esecuzione());
				log_riga.setPg_riga(BigDecimal.valueOf(listLogRighe.size() + 1));
				log_riga.setTi_messaggio("E");
				log_riga.setMessaggio("Creazione automatica Obbligazioni Pluriennali in errore. Errore: " + ex.getMessage());
				log_riga.setNote("Termine operazione creazione automatica Obbligazioni Pluriennali." + formatterTime.format(EJBCommonServices.getServerTimestamp().toInstant()));
				log_riga.setToBeCreated();
				try {
					listLogRighe.add((Batch_log_rigaBulk) batchControlComponentSession.creaConBulkRequiresNew(param0, log_riga));
				} catch (ComponentException | RemoteException ex2) {
					SendMail.sendErrorMail("Errore creazione automatica Obbligazioni Pluriennali", "Errore durante l'inserimento della riga di chiusura di Batch_log_riga " + ex2.getMessage());
					throw new DetailedRuntimeException(ex);
				}
			}

			log.setNote(log.getNote()+" - End: "+ formatterTime.format(EJBCommonServices.getServerTimestamp().toInstant()));
			log.setToBeUpdated();
			try {
				batchControlComponentSession.modificaConBulkRequiresNew(param0, log);
			} catch (ComponentException | RemoteException ex) {
				SendMail.sendErrorMail(subjectError, "Errore durante l'aggiornamento della riga di testata di Batch_log " + ex.getMessage());
				throw new ComponentException(ex);
			}
		} catch (DetailedRuntimeException ex) {
			logger.error("Creazione automatica Obbligazioni Pluriennali in errore. Errore: " + ex.getMessage());
		} catch (Exception ex) {
			logger.error("Creazione automatica Obbligazioni Pluriennali in errore. Errore: " + ex.getMessage());
			SendMail.sendErrorMail(subjectError, "Creazione automatica Obbligazioni Pluriennali in errore. Errore: " + ex.getMessage());
			throw ex;
		}*/
	}


	private List<StorageObject> getStorageObjectDistinte(UserContext userContext, Distinta_cassiereBulk distinta) throws ApplicationException {
		final String storePath = distinta.getStorePath();
		final String baseIdentificativoFlusso = distinta.getBaseIdentificativoFlusso();

			return documentiContabiliService.getChildren(documentiContabiliService.getStorageObjectByPath(storePath).getKey())
					.stream()
					.filter(storageObject1 -> storageObject1.<String>getPropertyValue(StoragePropertyNames.NAME.value())
							.startsWith(baseIdentificativoFlusso)).collect(Collectors.toList());


	}

	private void distinte (UserContext userContext,ArchiviaConsSostitutivaBulk archiviaConsSostitutivaBulk, Distinta_cassiereBulk.Tesoreria tipoDistinta) throws ComponentException, RemoteException {

		List<Distinta_cassiereBulk> distinteCassiere;
		DistintaCassiereComponentSession distintaCassiereComponentSession = Utility.createDistintaCassiereComponentSession();
		Distinta_cassiereBulk distintaCassiereBulk = new Distinta_cassiereBulk();
		distintaCassiereBulk.setEsercizio(archiviaConsSostitutivaBulk.getEsercizio());
		distintaCassiereBulk.setCd_tesoreria( tipoDistinta.value());
		List<Distinta_cassiereBulk> distinte= distintaCassiereComponentSession.findDistinteToConservazione(userContext,archiviaConsSostitutivaBulk.getEsercizio(),null,tipoDistinta);
		String zipFilePath = "E:\\ZipFile\\";
		ZipOutputStream zos = null;
		ByteArrayOutputStream baos=null;
		baos=new ByteArrayOutputStream();
		zos = new ZipOutputStream(baos);
		zos.setLevel(Deflater.DEFAULT_COMPRESSION);

		for ( Distinta_cassiereBulk distinta:distinte){
			logger.info(distinta.toString());
			try{
				List<StorageObject> storageObjectsDistinte = getStorageObjectDistinte( userContext,distinta);
				if ( Optional.ofNullable(storageObjectsDistinte).isPresent()){
					for ( StorageObject so:storageObjectsDistinte){
						addToZip(so,zos);
					}
				}

			}catch (Exception e){
				logger.info(e.getMessage());
			}
		}
		try {
			zos.closeEntry();
			baos.flush();
			zos.flush();
			zos.close();
			baos.close();
			saveFileZip( baos,archiviaConsSostitutivaBulk);


		}catch (IOException e){
			logger.info("Errore chiusura file zip "+ e.getMessage());
		}

	}

	private void addToZip(StorageObject so, ZipOutputStream zos) {

		try {
			ZipEntry zipEntryChild = new ZipEntry(so.<String>getPropertyValue(StoragePropertyNames.NAME.value()));
			zos.putNextEntry(zipEntryChild);
			IOUtils.copyLarge(documentiContabiliService.getResource(so), zos);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	private void saveFileZip(ByteArrayOutputStream baos, ArchiviaConsSostitutivaBulk archiviaConsSostitutivaBulk){
		final StorageObject storageObject = storeService.storeSimpleDocument(
				new ByteArrayInputStream(baos.toByteArray()),
				"application/zip",
                    storeService.getStorageObjectByPath(AllegatoParentIBulk.getStorePath("Consercazione Sostituva", archiviaConsSostitutivaBulk.getEsercizio()), true).getPath(),
				Stream.of(
								new AbstractMap.SimpleEntry<>(StoragePropertyNames.NAME.value(), "ConsSostitutiva".concat(archiviaConsSostitutivaBulk.getEsercizio().toString()).concat(".zip")),
								new AbstractMap.SimpleEntry<>(StoragePropertyNames.SECONDARY_OBJECT_TYPE_IDS.value(), Arrays.asList("P:cm:titled")),
								new AbstractMap.SimpleEntry<>(StoragePropertyNames.TITLE.value(),"ConsSostitutiva Anno".concat(archiviaConsSostitutivaBulk.getEsercizio().toString())),
								new AbstractMap.SimpleEntry<>(StoragePropertyNames.OBJECT_TYPE_ID.value(), "cmis:document"))
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
            );
	}
}

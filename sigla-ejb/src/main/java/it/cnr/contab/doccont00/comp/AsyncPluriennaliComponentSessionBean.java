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

import it.cnr.contab.config00.latt.bulk.WorkpackageBulk;
import it.cnr.contab.doccont00.core.bulk.Accertamento_pluriennaleBulk;
import it.cnr.contab.doccont00.core.bulk.ObbligazioneBulk;
import it.cnr.contab.doccont00.core.bulk.Obbligazione_pluriennaleBulk;
import it.cnr.contab.doccont00.ejb.AccertamentoPluriennaleComponentSession;
import it.cnr.contab.doccont00.ejb.ObbligazionePluriennaleComponentSession;
import it.cnr.contab.logs.bulk.Batch_log_rigaBulk;
import it.cnr.contab.logs.bulk.Batch_log_tstaBulk;
import it.cnr.contab.logs.ejb.BatchControlComponentSession;
import it.cnr.contab.util.Utility;
import it.cnr.jada.DetailedRuntimeException;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.util.SendMail;
import it.cnr.jada.util.ejb.EJBCommonServices;
import org.slf4j.LoggerFactory;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Stateless(name = "CNRCOEPCOAN00_EJB_AsyncPluriennaliComponentSession")
public class AsyncPluriennaliComponentSessionBean extends it.cnr.jada.ejb.CRUDComponentSessionBean implements AsyncPluriennaliComponentSession {
	private final static org.slf4j.Logger logger = LoggerFactory.getLogger(AsyncPluriennaliComponentSessionBean.class);

    public static it.cnr.jada.ejb.CRUDComponentSessionBean newInstance() throws javax.ejb.EJBException {
        return new AsyncPluriennaliComponentSessionBean();
    }
	@Asynchronous
	public void asyncCreatePluriennali(UserContext uc, Integer esercizio, WorkpackageBulk gaeIniziale) throws ComponentException {
		logger.info("Inizio Creazioni Obbligazione da Pluriennali".concat("esercizio:").concat(esercizio.toString()));
		createObbligazioniPluriennali( uc,esercizio,gaeIniziale);
		logger.info("Inizio Creazioni Accertamenti da Pluriennali".concat("esercizio:").concat(esercizio.toString()));
		createAcceratmentiPluriennali( uc,esercizio,gaeIniziale);
	}


	private void createObbligazioniPluriennali(UserContext param0, Integer esercizio, WorkpackageBulk gaeIniziale) throws ComponentException {
		String subjectError = "Errore caricamento scritture patrimoniali";
		try {
			ObbligazionePluriennaleComponentSession session = Utility.createObbligazionePluriennaleComponentSession();

			DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").withZone(ZoneId.systemDefault());
			Batch_log_tstaBulk log = new Batch_log_tstaBulk();
			log.setDs_log("Creazione Obbligazioni Pluriennali");
			log.setCd_log_tipo(Batch_log_tstaBulk.LOG_TIPO_OBBL_PLURIENNALE);
			log.setNote("Batch di creazione Obb. Pluriennali Java. Esercizio: " + esercizio + " - Centro Responsabilita : "
						+ gaeIniziale.getCd_centro_responsabilita() +" linea attivita: " + gaeIniziale.getCd_linea_attivita()+
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
		}
	}
	private void createAcceratmentiPluriennali(UserContext param0, Integer esercizio, WorkpackageBulk gaeIniziale) throws ComponentException {
		String subjectError = "Errore caricamento scritture patrimoniali";
		try {
			AccertamentoPluriennaleComponentSession session = Utility.createAccertamentoPluriennaleComponentSession();

			DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").withZone(ZoneId.systemDefault());
			Batch_log_tstaBulk log = new Batch_log_tstaBulk();
			log.setDs_log("Creazione Accertamenti Pluriennali");
			log.setCd_log_tipo(Batch_log_tstaBulk.LOG_TIPO_ACC_PLURIENNALE);
			log.setNote("Batch di creazione Acc. Pluriennali Java. Esercizio: " + esercizio + " - Centro Responsabilita : "
					+ gaeIniziale.getCd_centro_responsabilita() +" linea attivita: " + gaeIniziale.getCd_linea_attivita()+
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

				List<Accertamento_pluriennaleBulk> allAccPluriennali=new ArrayList<>();
				List<String> listInsert = new ArrayList<>();
				List<String> listError = new ArrayList<>();


				try {
					allAccPluriennali = session.findAccertamentiPluriennali(param0, esercizio);
				} catch (ComponentException | RemoteException  ex) {
					SendMail.sendErrorMail(subjectError, "Errore durante la lettura degli Accertamenti pluriennali dell'esercizio " + esercizio +" - Errore: " + ex.getMessage());
					throw new DetailedRuntimeException(ex);
				}

				allAccPluriennali.stream()
						.forEach(accertamento -> {
					try {
						logger.info("Accertamenti in elaborazione: ".
								concat(accertamento.getAnno().toString()).concat("/").
								concat(accertamento.getCdCds()).concat("/").
								concat(accertamento.getEsercizio().toString()).concat("/").
								concat(accertamento.getEsercizioOriginale().toString()).concat("/").
								concat(accertamento.getPgAccertamento().toString()));
						session.createAccertamentoNew(param0, esercizio,accertamento);
						listInsert.add("X");
					} catch (Throwable e) {
						listError.add("X");
						Batch_log_rigaBulk log_riga = new Batch_log_rigaBulk();
						log_riga.setPg_esecuzione(logDB.getPg_esecuzione());
						log_riga.setPg_riga(BigDecimal.valueOf(listLogRighe.size() + 1));
						log_riga.setTi_messaggio("E");
						log_riga.setMessaggio("Accertamento Pluriennale Anno:" + accertamento.getAnno()
								+ "-Cds:" + accertamento.getCdCds()
								+ "-Esercizio:" + accertamento.getEsercizio()
								+ "-Esercizio Orig.:" + accertamento.getEsercizioOriginale()
								+ "-Numero Obbl.:" + accertamento.getPgAccertamento());
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
				log_riga.setMessaggio("Esercizio: " + esercizio + " - Righe elaborate: " + allAccPluriennali.size() + " - Righe processate: " + listInsert.size() + " - Errori: " + listError.size());
				log_riga.setNote("Termine operazione creazione automatica Accertamenti Pluriennali: " + esercizio + ".");
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
				log_riga.setMessaggio("Creazione automatica Accertamenti Pluriennali. Errore: " + ex.getMessage());
				log_riga.setNote("Termine creazione automatica Accertamenti Pluriennali." + formatterTime.format(EJBCommonServices.getServerTimestamp().toInstant()));
				log_riga.setToBeCreated();
				try {
					listLogRighe.add((Batch_log_rigaBulk) batchControlComponentSession.creaConBulkRequiresNew(param0, log_riga));
				} catch (ComponentException | RemoteException ex2) {
					SendMail.sendErrorMail("Errore creazione automatica Accertamenti Pluriennali", "Errore durante l'inserimento della riga di chiusura di Batch_log_riga " + ex2.getMessage());
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
			logger.error("Creazione automatica Accertamenti Pluriennali. Errore: " + ex.getMessage());
		} catch (Exception ex) {
			logger.error("Creazione automatica Accertamenti Pluriennali in errore. Errore: " + ex.getMessage());
			SendMail.sendErrorMail(subjectError, "Creazione automatica Accertamenti Pluriennali in errore. Errore: " + ex.getMessage());
			throw ex;
		}
	}
}

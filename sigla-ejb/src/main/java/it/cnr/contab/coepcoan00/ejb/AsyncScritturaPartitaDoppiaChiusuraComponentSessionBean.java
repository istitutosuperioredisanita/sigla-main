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

package it.cnr.contab.coepcoan00.ejb;

import it.cnr.contab.logs.bulk.Batch_log_rigaBulk;
import it.cnr.contab.logs.bulk.Batch_log_tstaBulk;
import it.cnr.contab.logs.ejb.BatchControlComponentSession;
import it.cnr.contab.util.Utility;
import it.cnr.jada.DetailedRuntimeException;
import it.cnr.jada.UserContext;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.util.SendMail;
import it.cnr.jada.util.ejb.EJBCommonServices;
import jakarta.ejb.Asynchronous;
import org.slf4j.LoggerFactory;

import jakarta.ejb.Stateless;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Stateless(name = "CNRCOEPCOAN00_EJB_AsyncScritturaPartitaDoppiaChiusuraComponentSession")
public class AsyncScritturaPartitaDoppiaChiusuraComponentSessionBean extends it.cnr.jada.ejb.CRUDComponentSessionBean implements AsyncScritturaPartitaDoppiaChiusuraComponentSession {
	private final static org.slf4j.Logger logger = LoggerFactory.getLogger(AsyncScritturaPartitaDoppiaChiusuraComponentSessionBean.class);



	@Asynchronous
	public void asyncMakeScrittureChiusura(UserContext userContext, Integer pEsercizio, boolean pIsAnnullamento, boolean pIsDefinitiva) throws ComponentException {
		String subjectError = "Errore caricamento scritture chiusura bilancio";
		try {
			ScritturaPartitaDoppiaChiusuraComponentSession session = Utility.createScritturaPartitaDoppiaChiusuraComponentSession();

			DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").withZone(ZoneId.systemDefault());
			Batch_log_tstaBulk log = new Batch_log_tstaBulk();
			log.setDs_log("Registrazione Scritture Chiusura Bilancio Java");
			log.setCd_log_tipo(Batch_log_tstaBulk.LOG_TIPO_CONTAB_COGECOAN00);
			log.setNote("Batch di registrazione scritture chiusura bilancio Java. Esercizio: " + pEsercizio + " - Definitiva: " + pIsDefinitiva + " - " + formatterTime.format(it.cnr.jada.util.ejb.EJBCommonServices.getServerTimestamp().toInstant()));
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

			try {
				session.makeScrittureChiusura(userContext, pEsercizio, pIsAnnullamento, pIsDefinitiva);
			} catch (Throwable e) {
				Batch_log_rigaBulk log_riga = new Batch_log_rigaBulk();
				log_riga.setPg_esecuzione(logDB.getPg_esecuzione());
				log_riga.setPg_riga(BigDecimal.valueOf(listLogRighe.size() + 1));
				log_riga.setTi_messaggio("E");
				log_riga.setMessaggio("Esercizio:" + pEsercizio);
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
			log_riga.setMessaggio("Esercizio: " + pEsercizio + " - Elaborazione terminata");
			log_riga.setNote("Termine operazione caricamento automatico scritture chiusura bilancio Esercizio: " + pEsercizio + ".");
			log_riga.setToBeCreated();
			try {
				listLogRighe.add((Batch_log_rigaBulk) batchControlComponentSession.creaConBulkRequiresNew(userContext, log_riga));
			} catch (ComponentException | RemoteException ex) {
				SendMail.sendErrorMail(subjectError, "Errore durante l'inserimento della riga di chiusura di Batch_log_riga " + ex.getMessage());
				throw new DetailedRuntimeException(ex);
			}

			//log.setNote(log.getNote()+" - End: "+ formatterTime.format(it.cnr.jada.util.ejb.EJBCommonServices.getServerTimestamp().toInstant()));
			log.setToBeUpdated();
			try {
				batchControlComponentSession.modificaConBulkRequiresNew(userContext, log);
			} catch (ComponentException | RemoteException ex) {
				SendMail.sendErrorMail(subjectError, "Errore durante l'aggiornamento della riga di testata di Batch_log " + ex.getMessage());
				throw new ComponentException(ex);
			}
		} catch (DetailedRuntimeException ex) {
			logger.error("Caricamento automatico scritture prima nota in errore. Errore: " + ex.getMessage());
		} catch (Exception ex) {
			logger.error("Caricamento automatico scritture prima nota in errore. Errore: " + ex.getMessage());
			SendMail.sendErrorMail(subjectError, "Caricamento automatico scritture prima nota in errore. Errore: " + ex.getMessage());
			throw ex;
		}
	}
}

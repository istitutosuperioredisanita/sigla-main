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

import it.cnr.contab.coepcoan00.core.bulk.IDocumentoCogeBulk;
import it.cnr.contab.doccont00.core.bulk.MandatoBulk;
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
import org.slf4j.LoggerFactory;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Stateless(name = "CNRCOEPCOAN00_EJB_AsyncScritturaPartitaDoppiaFromDocumentoComponentSession")
public class AsyncScritturaPartitaDoppiaFromDocumentoComponentSessionBean extends it.cnr.jada.ejb.CRUDComponentSessionBean implements AsyncScritturaPartitaDoppiaFromDocumentoComponentSession {
	private final static org.slf4j.Logger logger = LoggerFactory.getLogger(AsyncScritturaPartitaDoppiaFromDocumentoComponentSessionBean.class);

    public static it.cnr.jada.ejb.CRUDComponentSessionBean newInstance() throws javax.ejb.EJBException {
        return new AsyncScritturaPartitaDoppiaFromDocumentoComponentSessionBean();
    }

	@Asynchronous
	public void asyncLoadScritturePatrimoniali(UserContext param0, Integer pEsercizio, String pCdCds) throws ComponentException {
		String subjectError = "Errore caricamento scritture patrimoniali";
		try {
			ScritturaPartitaDoppiaFromDocumentoComponentSession session = Utility.createScritturaPartitaDoppiaFromDocumentoComponentSession();

			DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").withZone(ZoneId.systemDefault());
			Batch_log_tstaBulk log = new Batch_log_tstaBulk();
			log.setDs_log("Registrazione Coge/Coan Java");
			log.setCd_log_tipo(Batch_log_tstaBulk.LOG_TIPO_CONTAB_COGECOAN00);
			log.setNote("Batch di registrazione economica Java. Esercizio: " + pEsercizio + " - CDS: *  Start: " + formatterTime.format(it.cnr.jada.util.ejb.EJBCommonServices.getServerTimestamp().toInstant()));
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
				List<IDocumentoCogeBulk> allDocuments;
				try {
					allDocuments = session.getAllDocumentiCogeDaContabilizzare(param0, pEsercizio, pCdCds.equals("*")?null:pCdCds);
				} catch (ComponentException | RemoteException | PersistencyException ex) {
					SendMail.sendErrorMail(subjectError, "Errore durante la lettura dei documenti dell'esercizio " + pEsercizio + (pCdCds.equals("*")?"":" del Cds:"+pCdCds)+" - Errore: " + ex.getMessage());
					throw new DetailedRuntimeException(ex);
				}

				List<String> listInsert = new ArrayList<>();
				List<String> listError = new ArrayList<>();

				boolean loadAnalitica = Utility.createConfigurazioneCnrComponentSession().isAttivaAnalitica(param0);

				allDocuments.stream()
						.filter(el-> Optional.ofNullable(el.getDtGenerazioneScrittura()).isPresent())
						.filter(el-> MandatoBulk.STATO_COGE_N.equals(el.getStato_coge()) || MandatoBulk.STATO_COGE_R.equals(el.getStato_coge())
						|| MandatoBulk.STATO_COGE_N.equals(el.getStato_coan()) || MandatoBulk.STATO_COGE_R.equals(el.getStato_coan()))
						.sorted(Comparator.comparing(IDocumentoCogeBulk::getDtGenerazioneScrittura)
								.thenComparing(el->el.getTipoDocumentoEnum().getOrdineCostruzione())
								.thenComparing(IDocumentoCogeBulk::getPg_doc))
						.forEach(documentoCoge -> {
					try {
						System.out.println("Data: "+documentoCoge.getDtGenerazioneScrittura()+" - Tipo: "+
								documentoCoge.getTipoDocumentoEnum().getValue() +" - Numero: "+
								documentoCoge.getPg_doc());
                        logger.info("Documento in elaborazione: {}/{}/{}/{}", documentoCoge.getEsercizio(), documentoCoge.getCd_uo(), documentoCoge.getCd_tipo_doc(), documentoCoge.getPg_doc());
						session.createScritturaRequiresNew(param0, documentoCoge, loadAnalitica);
						listInsert.add("X");
					} catch (Throwable e) {
						listError.add("X");
						Batch_log_rigaBulk log_riga = new Batch_log_rigaBulk();
						log_riga.setPg_esecuzione(logDB.getPg_esecuzione());
						log_riga.setPg_riga(BigDecimal.valueOf(listLogRighe.size() + 1));
						log_riga.setTi_messaggio("E");
						log_riga.setMessaggio("Esercizio:" + documentoCoge.getEsercizio() + "-CdUo:" + documentoCoge.getCd_uo() + "-CdTipoDoc:" + documentoCoge.getCd_tipo_doc() + "-PgDoc:" + documentoCoge.getPg_doc());
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
				log_riga.setMessaggio("Esercizio: " + pEsercizio + " - Righe elaborate: " + allDocuments.size() + " - Righe processate: " + listInsert.size() + " - Errori: " + listError.size());
				log_riga.setNote("Termine operazione caricamento automatico scritture prima nota Esercizio: " + pEsercizio + ".");
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
				log_riga.setMessaggio("Caricamento automatico scritture prima nota in errore. Errore: " + ex.getMessage());
				log_riga.setNote("Termine operazione caricamento automatico scritture prima nota." + formatterTime.format(it.cnr.jada.util.ejb.EJBCommonServices.getServerTimestamp().toInstant()));
				log_riga.setToBeCreated();
				try {
					listLogRighe.add((Batch_log_rigaBulk) batchControlComponentSession.creaConBulkRequiresNew(param0, log_riga));
				} catch (ComponentException | RemoteException ex2) {
					SendMail.sendErrorMail("Errore caricamento scritture patrimoniali", "Errore durante l'inserimento della riga di chiusura di Batch_log_riga " + ex2.getMessage());
					throw new DetailedRuntimeException(ex);
				}
			}

			log.setNote(log.getNote()+" - End: "+ formatterTime.format(it.cnr.jada.util.ejb.EJBCommonServices.getServerTimestamp().toInstant()));
			log.setToBeUpdated();
			try {
				batchControlComponentSession.modificaConBulkRequiresNew(param0, log);
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

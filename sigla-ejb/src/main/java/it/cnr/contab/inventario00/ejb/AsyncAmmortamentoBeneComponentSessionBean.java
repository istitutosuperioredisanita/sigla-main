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

package it.cnr.contab.inventario00.ejb;

import it.cnr.contab.inventario00.docs.bulk.*;

import it.cnr.contab.logs.bulk.Batch_log_rigaBulk;
import it.cnr.contab.logs.bulk.Batch_log_tstaBulk;
import it.cnr.contab.logs.ejb.BatchControlComponentSession;

import it.cnr.contab.ordmag.magazzino.bulk.ChiusuraAnnoBulk;
import it.cnr.contab.ordmag.magazzino.bulk.ChiusuraAnnoHome;
import it.cnr.contab.ordmag.magazzino.ejb.ChiusuraAnnoComponentSession;
import it.cnr.contab.util.Utility;
import it.cnr.jada.DetailedRuntimeException;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.BusyResourceException;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.persistency.PersistencyException;

import it.cnr.jada.util.ejb.EJBCommonServices;
import org.slf4j.LoggerFactory;

import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.rmi.RemoteException;

import java.text.ParseException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Stateless(name = "CNRINVENTARIO00_EJB_AsyncAmmortamentoBeneComponentSession")
public class AsyncAmmortamentoBeneComponentSessionBean extends it.cnr.jada.ejb.CRUDComponentSessionBean implements AsyncAmmortamentoBeneComponentSession {
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(AsyncAmmortamentoBeneComponentSessionBean.class);

    public static it.cnr.jada.ejb.CRUDComponentSessionBean newInstance() throws javax.ejb.EJBException {
        return new AsyncAmmortamentoBeneComponentSessionBean();
    }

    private final BigDecimal MENO_UNO = new BigDecimal(-1);
    private final BigDecimal CENTO = new BigDecimal(100);

    private final String CD_CDS_UBICAZIONE = "999";
    private final String CD_UO_UBICAZIONE = "999.000";


    private final String subjectError = "Errore Ammortamento beni";

    @Asynchronous
    public void asyncAmmortamentoBeni(UserContext uc, Integer esercizio,String statoChiusuraInventario,boolean gestisciDatiChiusura,boolean gestisciRipristinoInventario, boolean gestisciAggiornamentoInventario) throws ComponentException, PersistencyException, RemoteException, BusyResourceException {
        logger.info("Inizio Ammortamento Beni ".concat("esercizio:").concat(esercizio.toString()));
        ammortamentoBeni(uc, esercizio,statoChiusuraInventario,gestisciDatiChiusura,gestisciRipristinoInventario,gestisciAggiornamentoInventario);
        logger.info("Fine Ammortamento Beni ".concat("esercizio:").concat(esercizio.toString()));
    }

    private void ammortamentoBeni(UserContext uc, Integer esercizio,String statoChiusuraInventario,boolean gestisciDatiChiusura,boolean gestisciRipristinoInventario, boolean gestisciAggiornamentoInventario) throws ComponentException, RemoteException, BusyResourceException, PersistencyException {

        AmmortamentoBeneComponentSession ammortamentoBeneComponent = Utility.createAmmortamentoBeneComponentSession();
        V_AmmortamentoBeniDetComponentSession v_ammortamentoBeneDetComponent = Utility.createV_AmmortamentoBeniDetComponentSession();
        ChiusuraAnnoComponentSession chiusuraAnnoComponent = Utility.createChiusuraAnnoComponentSession();
        boolean logDettaglio=false;
        ChiusuraAnnoBulk chiusuraAnno = null;
        Batch_log_tstaBulk log=null;


        try {
            DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").withZone(ZoneId.systemDefault());
            log = new Batch_log_tstaBulk();

            log.setDs_log("Ammortamento Beni Inventario");
            log.setCd_log_tipo(Batch_log_tstaBulk.LOG_TIPO_AMMORT_BENE);
            log.setNote("Batch di Ammortamento Beni Inventario. Esercizio: " + esercizio + " - Start: " + formatterTime.format(EJBCommonServices.getServerTimestamp().toInstant()));
            log.setToBeCreated();

            BatchControlComponentSession batchControlComponentSession = (BatchControlComponentSession) EJBCommonServices
                    .createEJB("BLOGS_EJB_BatchControlComponentSession");
            try {
                log = (Batch_log_tstaBulk) batchControlComponentSession.creaConBulkRequiresNew(uc, log);
            } catch (ComponentException | RemoteException ex) {
                logger.info( "Errore durante l'inserimento della riga di testata di Batch_log " + ex.getMessage());
                throw new ComponentException(ex);
            }
            final Batch_log_tstaBulk logDB = log;
            List<Batch_log_rigaBulk> listLogRighe = new ArrayList<>();
            Integer countBeniAmm = 0;


            // GESTIONE CHIUSURA INVENTARIO
            if(gestisciDatiChiusura){
                chiusuraAnno = chiusuraAnnoComponent.verificaChiusuraAnno(uc,esercizio,ChiusuraAnnoBulk.TIPO_CHIUSURA_INVENTARIO);
                chiusuraAnno = aggiornaStatoInChiusuraAnno(uc,chiusuraAnnoComponent,chiusuraAnno,Batch_log_tstaBulk.STATO_JOB_RUNNING,statoChiusuraInventario,esercizio,logDB.getPg_job());
                //chiusuraAnno = eliminaECreaNuovaChiusuraInventario(uc,chiusuraAnnoComponent,esercizio,statoChiusuraInventario, logDB.getPg_job());
            }

            try {
                Batch_log_rigaBulk log_riga = new Batch_log_rigaBulk();
                log_riga.setPg_esecuzione(logDB.getPg_esecuzione());
                log_riga.setPg_riga(BigDecimal.valueOf(listLogRighe.size() + 1));
                log_riga.setTi_messaggio("I");
                log_riga.setMessaggio("Cancellazione Ammortamento Beni Inventario esercizio : "+esercizio);
                log_riga.setTrace(log_riga.getMessaggio());
                log_riga.setToBeCreated();
                try {
                    eliminaAmmortamentiPrecedentiDellEsercizio(uc, ammortamentoBeneComponent,  esercizio,gestisciRipristinoInventario);
                }catch (ComponentException | RemoteException ex) {
                    logger.info( "Errore durante l'eliminazione dell'ammortamento " + ex.getMessage());
                    if(gestisciDatiChiusura){
                        aggiornaStatoInChiusuraAnno(uc,chiusuraAnnoComponent,chiusuraAnno,Batch_log_tstaBulk.STATO_JOB_ERROR,statoChiusuraInventario,esercizio,logDB.getPg_job());
                    }
                    throw new DetailedRuntimeException(ex);
                }


                try {
                    listLogRighe.add((Batch_log_rigaBulk) batchControlComponentSession.creaConBulkRequiresNew(uc, log_riga));
                } catch (ComponentException | RemoteException ex) {
                    logger.info( "Errore durante l'inserimento dell'errore in Batch_log_rigaBulk " + ex.getMessage());
                    if(gestisciDatiChiusura){
                        aggiornaStatoInChiusuraAnno(uc,chiusuraAnnoComponent,chiusuraAnno,Batch_log_tstaBulk.STATO_JOB_ERROR,statoChiusuraInventario,esercizio,logDB.getPg_job());
                    }
                    throw new DetailedRuntimeException(ex);
                }

                List<V_ammortamento_beni_detBulk> beniList = getBeniDaElaborare(uc, v_ammortamentoBeneDetComponent, esercizio);


                if (beniList == null || beniList.isEmpty()) {
                    logger.info("Non risultano beni per ammortamento esercizio: " + esercizio);

                    Batch_log_rigaBulk log_rigaErr = new Batch_log_rigaBulk();
                    log_rigaErr.setPg_esecuzione(logDB.getPg_esecuzione());
                    log_rigaErr.setPg_riga(BigDecimal.valueOf(listLogRighe.size() + 1));
                    log_rigaErr.setTi_messaggio("E");
                    log_rigaErr.setMessaggio("Ammortamento Beni Inventario esercizio : " + esercizio + "- Non risultano beni da elaboarare");
                    log_rigaErr.setNote("Termine operazione Ammortamento Beni Inventario." + formatterTime.format(EJBCommonServices.getServerTimestamp().toInstant()));
                    log_rigaErr.setToBeCreated();
                    try {
                        listLogRighe.add((Batch_log_rigaBulk) batchControlComponentSession.creaConBulkRequiresNew(uc, log_rigaErr));
                    } catch (ComponentException | RemoteException ex2) {
                        logger.info("Errore durante l'inserimento della riga di chiusura di Batch_log_riga " + ex2.getMessage());
                        if(gestisciDatiChiusura){
                            aggiornaStatoInChiusuraAnno(uc,chiusuraAnnoComponent,chiusuraAnno,Batch_log_tstaBulk.STATO_JOB_ERROR,statoChiusuraInventario,esercizio,logDB.getPg_job());
                        }
                        throw new DetailedRuntimeException();
                    }
                }

                for (V_ammortamento_beni_detBulk bene : beniList) {
                    try {
                        // CASO DA GESTIRE
                        if (bene.getTiAmmortamento() != null) {

                            if (bene.getValoreAmmortizzatoCalcolato().compareTo(bene.getImponibileAmmortamentoCalcolato()) > 0 && !bene.getFlTotalmenteScaricato()) {

                            } else if (((bene.getValoreIniziale().add(bene.getVariazionePiu())).add(bene.getVariazioneMeno().multiply(MENO_UNO))).compareTo(BigDecimal.ZERO) == 0
                                    &&
                                    bene.getImponibileAmmortamentoCalcolato().compareTo(BigDecimal.ZERO) > 0 && !bene.getFlTotalmenteScaricato()) {
                            } else {

                                BigDecimal percAmmortamento = bene.getNumeroAnnoAmmortamento().compareTo(BigDecimal.ZERO) == 0 ? Utility.nvl(bene.getPercPrimoAnno()) : Utility.nvl(bene.getPercSuccessivi());

                                if (percAmmortamento.compareTo(BigDecimal.ZERO) > 0) {

                                    BigDecimal rataAmmortamento = BigDecimal.ZERO;
                                    //Controllare se il bene può essere ammortizzato dell'importo della rata calcolata precedentemente.
                                    //Si posssono verificare i seguenti casi:
                                    //   a) la rata di ammortamento calcolata con la percentuale stabilita è troppo alta
                                    //           VALORE_AMMORTIZZATO + rata_ammortamento > IMPONIBILE_AMMORTAMENTO
                                    //       In questo caso il bene viene ammortizzato di un valore pari allo scarto cosi calcolato:
                                    //           IMPONIBILE_AMMORTAMENTO - VALORE_AMMORTIZZATO
                                    //   b) la rata di ammortamento calcolata con la percentuale stabilita è consistente in quanto risulta:
                                    //           VALORE_AMMORTIZZATO + rata_ammortamento <= IMPONIBILE_AMMORTAMENTO (valore da ammortizzare),
                                    //       in questo caso il bene viene ammortizzato con un importo pari alla rata di ammortamento in esame.
                                    // Per tutti i casi in cui il bene deve subire una rata di ammortamento si deve aggiornare il :
                                    //   1) VALORE_AMMORTIZZATO del bene in esame
                                    //   2) inserire un movimento di ammortamento relativo alla rata.

                                    if (bene.getImponibileAmmortamentoCalcolato().compareTo(BigDecimal.ZERO) > 0 && !bene.getFlTotalmenteScaricato() &&
                                            Utility.nvl(bene.getValoreAmmortizzatoCalcolato()).compareTo(bene.getImponibileAmmortamentoCalcolato()) < 0) {

                                        rataAmmortamento = ((bene.getImponibileAmmortamentoCalcolato().multiply(percAmmortamento)).divide(CENTO));

                                        if ((rataAmmortamento.add(Utility.nvl(bene.getValoreAmmortizzatoCalcolato()))).compareTo(bene.getImponibileAmmortamentoCalcolato()) > 0) {
                                            rataAmmortamento = bene.getImponibileAmmortamentoCalcolato().add(Utility.nvl(bene.getValoreAmmortizzatoCalcolato()).multiply(MENO_UNO));
                                        }

                                        Ammortamento_bene_invBulk amm = creaAmmortamentoBene(uc, ammortamentoBeneComponent, bene, esercizio, rataAmmortamento, percAmmortamento);
                                        if (amm == null) {
                                            logger.info("Errore durante la creazione dell'ammortamento esercizio :" + esercizio + " pg_inventario: " + bene.getPgInventario() +
                                                    " nr_inventario: " + bene.getNrInventario() + " progressivo: " + bene.getProgressivo());
                                        } else {
                                            try {

                                                ammortamentoBeneComponent.creaConBulk(uc, amm);
                                                countBeniAmm++;
                                            } catch (ComponentException | RemoteException e) {
                                                logger.info("Errore durante l'inserimento dell'amomortamento esercizio :" + esercizio + " pg_inventario: " + bene.getPgInventario() +
                                                        " nr_inventario: " + bene.getNrInventario() + " progressivo: " + bene.getProgressivo() + " - Error:" + e.getMessage());
                                            }

                                        }
                                    }
                                } else {
                                    //logger.info( "Attenzione percentuale di ammortamento a 0 per il bene pg_inventario: " + bene.getPgInventario() +
                                    //       " nr_inventario: " + bene.getNrInventario() + " progressivo: " + bene.getProgressivo());

                                    Batch_log_rigaBulk log_rigaErr = new Batch_log_rigaBulk();
                                    log_rigaErr.setPg_esecuzione(logDB.getPg_esecuzione());
                                    log_rigaErr.setPg_riga(BigDecimal.valueOf(listLogRighe.size() + 1));
                                    log_rigaErr.setTi_messaggio("W");
                                    log_rigaErr.setMessaggio("Attenzione percentuale di ammortamento a 0 per il bene pg_inventario: " + bene.getPgInventario() +
                                            " nr_inventario: " + bene.getNrInventario() + " progressivo: " + bene.getProgressivo());
                                    log_rigaErr.setNote("");
                                    log_rigaErr.setToBeCreated();
                                    try {
                                        listLogRighe.add((Batch_log_rigaBulk) batchControlComponentSession.creaConBulkRequiresNew(uc, log_rigaErr));
                                    } catch (ComponentException | RemoteException ex2) {
                                        logger.info("Errore durante l'inserimento della riga di chiusura di Batch_log_riga " + ex2.getMessage());
                                        if(gestisciDatiChiusura){
                                            aggiornaStatoInChiusuraAnno(uc,chiusuraAnnoComponent,chiusuraAnno,Batch_log_tstaBulk.STATO_JOB_ERROR,statoChiusuraInventario,esercizio,logDB.getPg_job());
                                        }
                                        throw new DetailedRuntimeException();
                                    }
                                }
                                if (logDettaglio){
                                    try {
                                        log_riga.setMessaggio("Ammortamento bene :"
                                                + "-Numero Inventario:" + bene.getNrInventario()
                                                + "-Pg Inventario:" + bene.getPgInventario()
                                                + "-Progressivo:" + bene.getProgressivo());
                                        log_riga.setPg_riga(BigDecimal.valueOf(listLogRighe.size() + 1));
                                        log_riga.setTrace(log_riga.getMessaggio());
                                        log_riga.setToBeCreated();
                                        listLogRighe.add(log_riga);
                                        listLogRighe.add((Batch_log_rigaBulk) batchControlComponentSession.creaConBulkRequiresNew(uc, log_riga));
                                    } catch (ComponentException | RemoteException ex) {
                                        logger.info("Errore durante l'inserimento dell'errore in Batch_log_rigaBulk " + ex.getMessage());
                                        if(gestisciDatiChiusura){
                                            aggiornaStatoInChiusuraAnno(uc,chiusuraAnnoComponent,chiusuraAnno,Batch_log_tstaBulk.STATO_JOB_ERROR,statoChiusuraInventario,esercizio,logDB.getPg_job());
                                        }
                                        throw new DetailedRuntimeException(ex);
                                    }
                              }
                            }
                        }
                        else{
                          //  logger.info( "Attenzione nessun tipo ammortamento per il bene pg_inventario: " + bene.getPgInventario() +
                          //               " nr_inventario: " + bene.getNrInventario() + " progressivo: " + bene.getProgressivo());

                            Batch_log_rigaBulk log_rigaErr = new Batch_log_rigaBulk();
                            log_rigaErr.setPg_esecuzione(logDB.getPg_esecuzione());
                            log_rigaErr.setPg_riga(BigDecimal.valueOf(listLogRighe.size() + 1));
                            log_rigaErr.setTi_messaggio("W");
                            log_rigaErr.setMessaggio("Attenzione nessun tipo ammortamento per il bene pg_inventario: " + bene.getPgInventario() +
                                    " nr_inventario: " + bene.getNrInventario() + " progressivo: " + bene.getProgressivo());
                            log_rigaErr.setNote("");
                            log_rigaErr.setToBeCreated();
                            try {
                                listLogRighe.add((Batch_log_rigaBulk) batchControlComponentSession.creaConBulkRequiresNew(uc, log_rigaErr));
                            } catch (ComponentException | RemoteException ex2) {
                                logger.info("Errore durante l'inserimento della riga di chiusura di Batch_log_riga " + ex2.getMessage());
                                if(gestisciDatiChiusura){
                                    aggiornaStatoInChiusuraAnno(uc,chiusuraAnnoComponent,chiusuraAnno,Batch_log_tstaBulk.STATO_JOB_ERROR,statoChiusuraInventario,esercizio,logDB.getPg_job());
                                }
                                throw new DetailedRuntimeException();
                            }
                        }
                    } catch (DetailedRuntimeException ex) {
                        if(gestisciDatiChiusura){
                            aggiornaStatoInChiusuraAnno(uc,chiusuraAnnoComponent,chiusuraAnno,Batch_log_tstaBulk.STATO_JOB_ERROR,statoChiusuraInventario,esercizio,logDB.getPg_job());
                        }
                        throw new DetailedRuntimeException("Errore durante Ammortamento Bene esercizio:" + esercizio +"Error:" + ex.getMessage());
                    }
                }

                try {
                    log_riga.setMessaggio("Ammortamento effettuato su "+countBeniAmm+" beni");
                    log_riga.setTrace(log_riga.getMessaggio());
                    log_riga.setPg_riga(BigDecimal.valueOf(listLogRighe.size() + 1));
                    log_riga.setToBeCreated();

                    listLogRighe.add((Batch_log_rigaBulk) batchControlComponentSession.creaConBulkRequiresNew(uc, log_riga));
                } catch (ComponentException | RemoteException ex) {
                    logger.info( "Errore durante l'inserimento dell'errore in Batch_log_rigaBulk " + ex.getMessage());
                    if(gestisciDatiChiusura){
                        aggiornaStatoInChiusuraAnno(uc,chiusuraAnnoComponent,chiusuraAnno,Batch_log_tstaBulk.STATO_JOB_ERROR,statoChiusuraInventario,esercizio,logDB.getPg_job());
                    }
                    throw new DetailedRuntimeException(ex);
                }

                if(gestisciAggiornamentoInventario) {
                    try {
                        log_riga.setMessaggio("Aggiornamento Valore Ammortizzato su Inventario per " + countBeniAmm + " beni");
                        log_riga.setTrace(log_riga.getMessaggio());
                        log_riga.setPg_riga(BigDecimal.valueOf(listLogRighe.size() + 1));
                        log_riga.setToBeCreated();

                        try {
                            ammortamentoBeneComponent.aggiornamentoInventarioBeneConAmmortamento(uc, esercizio, Ammortamento_bene_invBulk.INCREMENTA_VALORE_AMMORTIZZATO);
                        } catch (RemoteException e) {
                            logger.info("Errore durante l'aggiornamento Inventario a seguito dell'ammortamento esercizio: " + esercizio);
                        }

                        listLogRighe.add((Batch_log_rigaBulk) batchControlComponentSession.creaConBulkRequiresNew(uc, log_riga));
                    } catch (ComponentException | RemoteException ex) {
                        logger.info("Errore durante l'inserimento dell'errore in Batch_log_rigaBulk " + ex.getMessage());
                        if(gestisciDatiChiusura){
                            aggiornaStatoInChiusuraAnno(uc,chiusuraAnnoComponent,chiusuraAnno,Batch_log_tstaBulk.STATO_JOB_ERROR,statoChiusuraInventario,esercizio,logDB.getPg_job());
                        }
                        throw new DetailedRuntimeException(ex);
                    }
                }
                // GESTIONE CHIUSURA INVENTARIO
                if(gestisciDatiChiusura){
                    inserisciDatiChiusuraInventario(uc,chiusuraAnnoComponent,chiusuraAnno, statoChiusuraInventario, esercizio,logDB.getPg_job());
                }

            } catch (Exception ex) {
                Batch_log_rigaBulk log_riga = new Batch_log_rigaBulk();
                log_riga.setPg_esecuzione(logDB.getPg_esecuzione());
                log_riga.setPg_riga(BigDecimal.valueOf(listLogRighe.size() + 1));
                log_riga.setTi_messaggio("E");
                log_riga.setMessaggio("Ammortamento Beni Inventario in errore. Errore: " + ex.getMessage());
                log_riga.setNote("Termine operazione Ammortamento Beni Inventario." + formatterTime.format(EJBCommonServices.getServerTimestamp().toInstant()));
                log_riga.setToBeCreated();
                try {
                    listLogRighe.add((Batch_log_rigaBulk) batchControlComponentSession.creaConBulkRequiresNew(uc, log_riga));
                } catch (ComponentException | RemoteException ex2) {
                    logger.info( "Errore durante l'inserimento della riga di chiusura di Batch_log_riga " + ex2.getMessage());
                    if(gestisciDatiChiusura){
                        aggiornaStatoInChiusuraAnno(uc,chiusuraAnnoComponent,chiusuraAnno,Batch_log_tstaBulk.STATO_JOB_ERROR,statoChiusuraInventario,esercizio,logDB.getPg_job());
                    }
                    throw new DetailedRuntimeException(ex);
                }

            }
            log.setNote(log.getNote()+" - End: "+ formatterTime.format(EJBCommonServices.getServerTimestamp().toInstant())+" - Ammortizzati "+countBeniAmm+ " beni.");
            log.setToBeUpdated();
            try {
                batchControlComponentSession.modificaConBulkRequiresNew(uc, log);
            } catch (ComponentException | RemoteException ex) {
                logger.info( "Errore durante l'aggiornameto della riga di testata di Batch_log " + ex.getMessage());
                if(gestisciDatiChiusura){
                    aggiornaStatoInChiusuraAnno(uc,chiusuraAnnoComponent,chiusuraAnno,Batch_log_tstaBulk.STATO_JOB_ERROR,statoChiusuraInventario,esercizio,logDB.getPg_job());
                }
                throw new ComponentException(ex);
            }
        }catch (Exception ex) {
            if(gestisciDatiChiusura){
                aggiornaStatoInChiusuraAnno(uc,chiusuraAnnoComponent,chiusuraAnno,Batch_log_tstaBulk.STATO_JOB_ERROR,statoChiusuraInventario,esercizio,log.getPg_job());
            }
            throw new DetailedRuntimeException(ex);
        }

    }


    private void eliminaAmmortamentiPrecedentiDellEsercizio(UserContext uc,  AmmortamentoBeneComponentSession ammortamentoBeneComponent, Integer esercizio,boolean gestisciRipristinoInventario) throws RemoteException, InvocationTargetException, ComponentException, PersistencyException {

        if(ammortamentoBeneComponent.isExistAmmortamentoEsercizio(uc, esercizio) )
        {
            if(gestisciRipristinoInventario) {
                ammortamentoBeneComponent.aggiornamentoInventarioBeneConAmmortamento(uc, esercizio, Ammortamento_bene_invBulk.DECREMENTA_VALORE_AMMORTIZZATO);
            }
            try {
                ammortamentoBeneComponent.cancellaAmmortamentiEsercizio(uc, esercizio);
            } catch (RemoteException | ComponentException ex) {
                logger.info( "Errore durante l'eliminazione dell'ammortamento esercizio : " + esercizio + " - Errore: " + ex.getMessage());
                throw new DetailedRuntimeException( "Errore durante l'eliminazione dell'ammortamento esercizio : " + esercizio + " - Errore: " + ex.getMessage());
            }
        }
    }

    private List<V_ammortamento_beni_detBulk> getBeniDaElaborare(UserContext uc,V_AmmortamentoBeniDetComponentSession v_ammortamentoBeniDetComponent,Integer esercizio){
        try {
            return v_ammortamentoBeniDetComponent.getDatiAmmortamentoBeni(uc,esercizio);
        }catch (RemoteException | ComponentException ex) {
            logger.info( "Errore durante la lettura dei beni da ammortizzare " + esercizio + " - Errore: " + ex.getMessage());
            throw new DetailedRuntimeException("Errore durante la lettura dei beni da ammortizzare " + esercizio + " - Errore: " + ex.getMessage());
        }
    }


    private Ammortamento_bene_invBulk creaAmmortamentoBene(UserContext uc, AmmortamentoBeneComponentSession ammortamentoComponent,V_ammortamento_beni_detBulk bene,Integer esercizio,BigDecimal rataAmm, BigDecimal percAmm) throws RemoteException {
        Ammortamento_bene_invBulk ammortamento = null;

        Integer numeroAnnoAmm = ammortamentoComponent.getNumeroAnnoAmmortamento( uc, bene.getPgInventario(),bene.getNrInventario(),bene.getProgressivo());
        Integer progRigaAmm = ammortamentoComponent.getProgressivoRigaAmmortamento(uc, bene.getPgInventario(),bene.getNrInventario(),bene.getProgressivo(),esercizio);

        ammortamento = new Ammortamento_bene_invBulk();

        ammortamento.setInventarioBeni( new Inventario_beniBulk(bene.getNrInventario(),bene.getPgInventario(),bene.getProgressivo()));
        ammortamento.setPgInventario(bene.getPgInventario());
        ammortamento.setNrInventario(bene.getNrInventario());
        ammortamento.setProgressivo(bene.getProgressivo());
        ammortamento.setEsercizio(esercizio);
        ammortamento.setCdTipoAmmortamento(bene.getCdTipoAmmortamento());
        ammortamento.setTiAmmortamento(bene.getTiAmmortamento());
        ammortamento.setCdCategoriaGruppo(bene.getCdCategoriaGruppo());
        ammortamento.setEsercizioCompetenza(bene.getEsercizioCompetenza());
        ammortamento.setImponibileAmmortamento(bene.getImponibileAmmortamentoCalcolato().setScale(2, java.math.BigDecimal.ROUND_HALF_UP));
        ammortamento.setImMovimentoAmmort(rataAmm.setScale(2, java.math.BigDecimal.ROUND_HALF_UP));
        ammortamento.setPercAmmortamento(percAmm);
        ammortamento.setNumeroAnni((bene.getNumeroAnnoAmmortamento().add(BigDecimal.ONE).intValue()));
        ammortamento.setNumeroAnno(numeroAnnoAmm);
        ammortamento.setPercPrimoAnno(bene.getPercPrimoAnno());
        ammortamento.setPercSuccessivi(bene.getPercSuccessivi());
        ammortamento.setCdCdsUbicazione(CD_CDS_UBICAZIONE);
        ammortamento.setCdUoUbicazione(CD_UO_UBICAZIONE);
        ammortamento.setPgRiga(progRigaAmm);
        ammortamento.setFlStorno(false);
        ammortamento.setToBeCreated();

        return  ammortamento;
    }

    ChiusuraAnnoBulk eliminaECreaNuovaChiusuraInventario(UserContext uc,ChiusuraAnnoComponentSession chiusuraAnnoComponent,Integer esercizio,String statoChiusuraInventario, BigDecimal pgJob ) throws ComponentException, PersistencyException, RemoteException, ParseException, BusyResourceException {
        ChiusuraAnnoBulk chiusuraAnno = null;
        if(chiusuraAnnoComponent.verificaChiusuraAnno(uc,esercizio,ChiusuraAnnoBulk.TIPO_CHIUSURA_INVENTARIO)!=null) {
            chiusuraAnno = chiusuraAnnoComponent.eliminaDatiChiusuraInventarioECreaNuovaChiusuraRequestNew(uc, esercizio, ChiusuraAnnoBulk.TIPO_CHIUSURA_INVENTARIO,statoChiusuraInventario,pgJob);
        }
        else{
            chiusuraAnno = new ChiusuraAnnoBulk();
            chiusuraAnno.setPgChiusura(chiusuraAnnoComponent.getNuovoProgressivoChiusura(uc,chiusuraAnno));
            chiusuraAnno.setTipoChiusura(ChiusuraAnnoBulk.TIPO_CHIUSURA_INVENTARIO);
            chiusuraAnno.setAnno(esercizio);
            chiusuraAnno.setStato(statoChiusuraInventario);
            chiusuraAnno.setDataCalcolo(it.cnr.jada.util.ejb.EJBCommonServices.getServerTimestamp());
            chiusuraAnno.setPg_job(pgJob);
            chiusuraAnno.setStato_job(Batch_log_tstaBulk.STATO_JOB_RUNNING);
            chiusuraAnno.setCrudStatus(OggettoBulk.TO_BE_CREATED);

            chiusuraAnnoComponent.creaConBulkRequiresNew(uc,chiusuraAnno);
        }
        return chiusuraAnno;
    }
    ChiusuraAnnoBulk aggiornaStatoInChiusuraAnno(UserContext uc,ChiusuraAnnoComponentSession chiusuraAnnoComponent,ChiusuraAnnoBulk chiusuraAnno,String statoJob,String statoChiusura,Integer esercizio,BigDecimal pgJob) throws ComponentException, RemoteException, BusyResourceException, PersistencyException {
        if(chiusuraAnno!=null) {
            chiusuraAnno = chiusuraAnnoComponent.findByPrimaryKey(uc,chiusuraAnno);
            chiusuraAnno.setStato_job(statoJob);
            chiusuraAnno.setDataCalcolo(it.cnr.jada.util.ejb.EJBCommonServices.getServerTimestamp());
            chiusuraAnno.setPg_job(pgJob);
            chiusuraAnno.setCrudStatus(OggettoBulk.TO_BE_UPDATED);
            chiusuraAnno = (ChiusuraAnnoBulk) chiusuraAnnoComponent.modificaConBulkRequiresNew(uc, chiusuraAnno);
        }
        else{
            chiusuraAnno = new ChiusuraAnnoBulk();
            chiusuraAnno.setPgChiusura(chiusuraAnnoComponent.getNuovoProgressivoChiusura(uc,chiusuraAnno));
            chiusuraAnno.setTipoChiusura(ChiusuraAnnoBulk.TIPO_CHIUSURA_INVENTARIO);
            chiusuraAnno.setAnno(esercizio);
            chiusuraAnno.setStato(statoChiusura);
            chiusuraAnno.setDataCalcolo(it.cnr.jada.util.ejb.EJBCommonServices.getServerTimestamp());
            chiusuraAnno.setPg_job(pgJob);
            chiusuraAnno.setStato_job(statoJob);
            chiusuraAnno.setCrudStatus(OggettoBulk.TO_BE_CREATED);
            chiusuraAnnoComponent.creaConBulkRequiresNew(uc,chiusuraAnno);
        }
        return chiusuraAnno;
    }
    void inserisciDatiChiusuraInventario(UserContext uc,ChiusuraAnnoComponentSession chiusuraAnnoComponent,ChiusuraAnnoBulk chiusuraAnno,String statoChiusuraInventario,Integer esercizio,BigDecimal pg_job) throws ComponentException, RemoteException, BusyResourceException, PersistencyException {
        chiusuraAnnoComponent.inserisciDettagliChiusuraInventario(uc,chiusuraAnno.getAnno(),chiusuraAnno.getPgChiusura());
        chiusuraAnnoComponent.inserisciImportiPerCatGruppoVoceEPInventario(uc,chiusuraAnno.getAnno(),chiusuraAnno.getPgChiusura());
        aggiornaStatoInChiusuraAnno(uc,chiusuraAnnoComponent,chiusuraAnno,Batch_log_tstaBulk.STATO_JOB_COMPLETE,statoChiusuraInventario,esercizio,pg_job);
    }
}

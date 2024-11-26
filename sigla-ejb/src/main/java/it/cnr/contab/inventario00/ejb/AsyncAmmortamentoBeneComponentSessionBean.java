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

import it.cnr.contab.inventario00.docs.bulk.Ammortamento_bene_invBulk;
import it.cnr.contab.inventario00.docs.bulk.Inventario_beniBulk;
import it.cnr.contab.inventario00.docs.bulk.V_ammortamento_beniBulk;
import it.cnr.contab.inventario00.dto.NormalizzatoreAmmortamentoDto;
import it.cnr.contab.inventario00.tabrif.bulk.Tipo_ammortamentoBulk;
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
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Stateless(name = "CNRINVENTARIO00_EJB_AsyncAmmortamentoBeneComponentSession")
public class AsyncAmmortamentoBeneComponentSessionBean extends it.cnr.jada.ejb.CRUDComponentSessionBean implements AsyncAmmortamentoBeneComponentSession {
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(AsyncAmmortamentoBeneComponentSessionBean.class);

    public static it.cnr.jada.ejb.CRUDComponentSessionBean newInstance() throws javax.ejb.EJBException {
        return new AsyncAmmortamentoBeneComponentSessionBean();
    }

    private final BigDecimal zero=new BigDecimal(0);
    private final BigDecimal menoUno = new BigDecimal(-1);
    private final BigDecimal cento = new BigDecimal(100);

    private final String subjectError = "Errore Ammortamento beni";

    @Asynchronous
    public void asyncAmmortamentoBeni(UserContext uc, Integer esercizio) throws ComponentException, PersistencyException, RemoteException {
        logger.info("Inizio Ammortamento Beni ".concat("esercizio:").concat(esercizio.toString()));
        ammortamentoBeni(uc, esercizio);
        logger.info("Fine Ammortamento Beni ".concat("esercizio:").concat(esercizio.toString()));
    }

    private void ammortamentoBeni(UserContext uc, Integer esercizio) throws ComponentException {

        BatchControlComponentSession batchControlComponentSession = (BatchControlComponentSession) EJBCommonServices
                .createEJB("BLOGS_EJB_BatchControlComponentSession");

        DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").withZone(ZoneId.systemDefault());
        Batch_log_tstaBulk log = new Batch_log_tstaBulk();
        log.setDs_log("Ammortamento Beni");
        log.setCd_log_tipo(Batch_log_tstaBulk.LOG_TIPO_AMMORT_BENE);
        log.setNote("Batch di Ammortamento Beni . Esercizio: " + esercizio + " Start: " + formatterTime.format(EJBCommonServices.getServerTimestamp().toInstant()));
        log.setToBeCreated();

        try {
            log = (Batch_log_tstaBulk) batchControlComponentSession.creaConBulkRequiresNew(uc, log);
        } catch (ComponentException | RemoteException ex) {
            SendMail.sendErrorMail(subjectError, "Errore durante l'inserimento della riga di testata di Batch_log " + ex.getMessage());
            throw new ComponentException(ex);
        }
        List<Batch_log_rigaBulk> listLogRighe = new ArrayList<>();
        Batch_log_rigaBulk log_riga = null;

// ANNULLAMENTO AMMORTAMENTO ESERCIZIO CORRENTE INIZIO
        try {
            log_riga = getRigaLog(log.getPg_esecuzione(), listLogRighe.size() + 1, "I", "Batch di Ammortamento Beni",
                    "Eliminazione Ammortamento Esercizio :" + esercizio + "Start:" + formatterTime.format(EJBCommonServices.getServerTimestamp().toInstant()));
            try {
                listLogRighe.add((Batch_log_rigaBulk) batchControlComponentSession.creaConBulkRequiresNew(uc, log_riga));
            } catch (ComponentException | RemoteException ex) {
                SendMail.sendErrorMail(subjectError, "Errore durante l'inserimento dell'errore in Batch_log_rigaBulk " + ex.getMessage());
                throw new DetailedRuntimeException(ex);
            }
// Elimina eventuali ammortamenti presenti per l'esercizio , ripristina INVENTARIO_BENI e cancella AMMORTAMENTO_BENI
            Integer beniElaborati = eliminaAmmortamentiPrecedentiDellEsercizio(uc, esercizio, subjectError);

            log_riga = getRigaLog(log.getPg_esecuzione(), listLogRighe.size() + 1, "I", "Batch di Ammortamento Beni",
                    "Eliminazione Ammortamento Esercizio :" + esercizio + "End:" + formatterTime.format(EJBCommonServices.getServerTimestamp().toInstant()) + "Beni elaborati: " + beniElaborati);
            try {
                listLogRighe.add((Batch_log_rigaBulk) batchControlComponentSession.creaConBulkRequiresNew(uc, log_riga));
            } catch (ComponentException | RemoteException ex) {
                SendMail.sendErrorMail(subjectError, "Errore durante l'inserimento dell'errore in Batch_log_rigaBulk " + ex.getMessage());
                throw new DetailedRuntimeException(ex);
            }

        } catch (DetailedRuntimeException ex) {
            log_riga = getRigaLog(log.getPg_esecuzione(), listLogRighe.size() + 1, "E", "Batch di Ammortamento Beni",
                    "Errore durante Eliminazione Ammortamento Esercizio :" + esercizio + "Errore: " + ex.getMessage());
            try {
                listLogRighe.add((Batch_log_rigaBulk) batchControlComponentSession.creaConBulkRequiresNew(uc, log_riga));
            } catch (ComponentException | RemoteException ex1) {
                SendMail.sendErrorMail(subjectError, "Errore durante l'inserimento dell'errore in Batch_log_rigaBulk " + ex1.getMessage());
                throw new DetailedRuntimeException(ex1);
            }
        }
// ANNULLAMENTO AMMORTAMENTO ESERCIZIO CORRENTE FINE

// AMMORTAMENTO INIZIO
        try {
            log_riga = getRigaLog(log.getPg_esecuzione(), listLogRighe.size() + 1, "I", "Batch di Ammortamento Beni",
                    "Elaborazione Beni da Ammortizzare Esercizio :" + esercizio + "Start:" + formatterTime.format(EJBCommonServices.getServerTimestamp().toInstant()));
            try {
                listLogRighe.add((Batch_log_rigaBulk) batchControlComponentSession.creaConBulkRequiresNew(uc, log_riga));
            } catch (ComponentException | RemoteException ex) {
                SendMail.sendErrorMail(subjectError, "Errore durante l'inserimento dell'errore in Batch_log_rigaBulk " + ex.getMessage());
                throw new DetailedRuntimeException(ex);
            }

            List<V_ammortamento_beniBulk> beniDaElaborareList = null;
            // TIPO AMMORTAMENTO
            try {
                log_riga = getRigaLog(log.getPg_esecuzione(), listLogRighe.size() + 1, "I", "Batch di Ammortamento Beni",
                        "Verifica tipo ammortamento :" + esercizio + "Start:" + formatterTime.format(EJBCommonServices.getServerTimestamp().toInstant()));
                try {
                    listLogRighe.add((Batch_log_rigaBulk) batchControlComponentSession.creaConBulkRequiresNew(uc, log_riga));
                } catch (ComponentException | RemoteException ex) {
                    SendMail.sendErrorMail(subjectError, "Errore durante l'inserimento dell'errore in Batch_log_rigaBulk " + ex.getMessage());
                    throw new DetailedRuntimeException(ex);
                }
// preleva per ogni bene da ammortizzare (V_AMMORTAMENTO_BENI) tutti quelli che hanno un tipo ammortamento associato
                beniDaElaborareList = getAllBeniDaElaborare(uc, esercizio, subjectError);

                if (beniDaElaborareList == null || beniDaElaborareList.isEmpty()) {
                    SendMail.sendErrorMail(subjectError, "Errore non risultano beni da ammortizzare esercizio: " + esercizio);
                    throw new DetailedRuntimeException("Errore non risultano beni da ammortizzare esercizio: " + esercizio);
                }
                log_riga = getRigaLog(log.getPg_esecuzione(), listLogRighe.size() + 1, "I", "Batch di Ammortamento Beni",
                        "Verifica tipo ammortamento :" + esercizio + " beni da elaborare:" + beniDaElaborareList.size() + "End:" + formatterTime.format(EJBCommonServices.getServerTimestamp().toInstant()));
                try {
                    listLogRighe.add((Batch_log_rigaBulk) batchControlComponentSession.creaConBulkRequiresNew(uc, log_riga));
                } catch (ComponentException | RemoteException ex) {
                    SendMail.sendErrorMail(subjectError, "Errore durante l'inserimento dell'errore in Batch_log_rigaBulk " + ex.getMessage());
                    throw new DetailedRuntimeException(ex);
                }
            } catch (DetailedRuntimeException ex) {
                log_riga = getRigaLog(log.getPg_esecuzione(), listLogRighe.size() + 1, "E", "Batch di Ammortamento Beni",
                        "Errore durante Verifica tipo ammortamento :" + esercizio + "Errore :" + ex.getMessage());
                try {
                    listLogRighe.add((Batch_log_rigaBulk) batchControlComponentSession.creaConBulkRequiresNew(uc, log_riga));
                } catch (ComponentException | RemoteException ex1) {
                    SendMail.sendErrorMail(subjectError, "Errore durante l'inserimento dell'errore in Batch_log_rigaBulk " + ex1.getMessage());
                    throw new DetailedRuntimeException(ex1);
                }
            }

            // NORMALIZZATORE
            try {
                log_riga = getRigaLog(log.getPg_esecuzione(), listLogRighe.size() + 1, "I", "Batch di Ammortamento Beni",
                        "Normalizzazione Beni :" + esercizio + "Start:" + formatterTime.format(EJBCommonServices.getServerTimestamp().toInstant()));

                try {
                    listLogRighe.add((Batch_log_rigaBulk) batchControlComponentSession.creaConBulkRequiresNew(uc, log_riga));
                } catch (ComponentException | RemoteException ex) {
                    SendMail.sendErrorMail(subjectError, "Errore durante l'inserimento dell'errore in Batch_log_rigaBulk " + ex.getMessage());
                    throw new DetailedRuntimeException(ex);
                }

// normalizza importi in base a movimenti futuri alla fine dell'esercizio
                beniDaElaborareList = getAllBeniNormalizzati(uc, beniDaElaborareList, esercizio, subjectError);

                if (beniDaElaborareList == null || beniDaElaborareList.isEmpty()) {
                    SendMail.sendErrorMail(subjectError, "Errore non risultano beni dopo normalizzazione esercizio: " + esercizio);
                    throw new DetailedRuntimeException("Errore non risultano beni dopo normalizzazione esercizio: " + esercizio);
                }
                log_riga = getRigaLog(log.getPg_esecuzione(), listLogRighe.size() + 1, "I", "Batch di Ammortamento Beni",
                        "Normalizzazione Beni :" + esercizio + " beni da elaborare:" + beniDaElaborareList.size() + "End:" + formatterTime.format(EJBCommonServices.getServerTimestamp().toInstant()));
                try {
                    listLogRighe.add((Batch_log_rigaBulk) batchControlComponentSession.creaConBulkRequiresNew(uc, log_riga));
                } catch (ComponentException | RemoteException ex) {
                    SendMail.sendErrorMail(subjectError, "Errore durante l'inserimento dell'errore in Batch_log_rigaBulk " + ex.getMessage());
                    throw new DetailedRuntimeException(ex);
                }
            } catch (DetailedRuntimeException ex) {

                log_riga = getRigaLog(log.getPg_esecuzione(), listLogRighe.size() + 1, "E", "Batch di Ammortamento Beni",
                        "Errore durante Normalizzazione beni Esercizio :" + esercizio + "Errore:" + ex.getMessage());
                try {
                    listLogRighe.add((Batch_log_rigaBulk) batchControlComponentSession.creaConBulkRequiresNew(uc, log_riga));
                } catch (ComponentException | RemoteException ex1) {
                    SendMail.sendErrorMail(subjectError, "Errore durante l'inserimento dell'errore in Batch_log_rigaBulk " + ex1.getMessage());
                    throw new DetailedRuntimeException(ex1);
                }
            }

            // AMMORTAMENTO

            try {
                log_riga = getRigaLog(log.getPg_esecuzione(), listLogRighe.size() + 1, "I", "Batch di Ammortamento Beni",
                        "Ammortamento dei Beni :" + esercizio + "Start:" + formatterTime.format(EJBCommonServices.getServerTimestamp().toInstant()));

                try {
                    listLogRighe.add((Batch_log_rigaBulk) batchControlComponentSession.creaConBulkRequiresNew(uc, log_riga));
                } catch (ComponentException | RemoteException ex) {
                    SendMail.sendErrorMail(subjectError, "Errore durante l'inserimento dell'errore in Batch_log_rigaBulk " + ex.getMessage());
                    throw new DetailedRuntimeException(ex);
                }
                for (V_ammortamento_beniBulk bene : beniDaElaborareList) {

                    try {
                        log_riga = getRigaLog(log.getPg_esecuzione(), listLogRighe.size() + 1, "I", "Batch di Ammortamento Beni",
                                "Ammortamento Bene esercizio:" + esercizio +
                                        " pg_inventario:" + bene.getPgInventario() + " nr_inventario : " + bene.getNrInventario() + " progressivo:" + bene.getProgressivo()
                                        + "Start:" + formatterTime.format(EJBCommonServices.getServerTimestamp().toInstant()));
                        try {
                            listLogRighe.add((Batch_log_rigaBulk) batchControlComponentSession.creaConBulkRequiresNew(uc, log_riga));
                        } catch (ComponentException | RemoteException ex) {
                            SendMail.sendErrorMail(subjectError, "Errore durante l'inserimento dell'errore in Batch_log_rigaBulk " + ex.getMessage());
                            throw new DetailedRuntimeException(ex);
                        }

                        if(bene.getValoreAmmortizzato().compareTo( bene.getImponibileAmmortamento())>0 && !bene.getFlTotalmenteScaricato()){
                            log_riga = getRigaLog(log.getPg_esecuzione(), listLogRighe.size() + 1, "W", "Batch di Ammortamento Beni",
                                    "Ammortamento Bene- Il valore ammortizzato è maggiore dell''imponibile ammortamento esercizio:" + esercizio +
                                            " pg_inventario:" + bene.getPgInventario() + " nr_inventario : " + bene.getNrInventario() + " progressivo:" + bene.getProgressivo());
                            try {
                                listLogRighe.add((Batch_log_rigaBulk) batchControlComponentSession.creaConBulkRequiresNew(uc, log_riga));
                            } catch (ComponentException | RemoteException ex) {
                                SendMail.sendErrorMail(subjectError, "Errore durante l'inserimento dell'errore in Batch_log_rigaBulk " + ex.getMessage());
                                throw new DetailedRuntimeException(ex);
                            }
                            SendMail.sendErrorMail(subjectError, "Ammortamento Bene- Il valore ammortizzato è maggiore dell'imponibile ammortamento - esercizio:" + esercizio +
                                    " pg_inventario:" + bene.getPgInventario() + " nr_inventario : " + bene.getNrInventario() + " progressivo:" + bene.getProgressivo());

                            // se valore inziale + variazione più - variazione meno = 0 e imponibile ammortamento > 0 e bene non completamente scaricato
                        }else if( ((bene.getValoreIniziale().add(bene.getVariazionePiu())).add(bene.getVariazioneMeno().multiply(menoUno))).compareTo(zero) ==0
                                                                                            &&
                                bene.getImponibileAmmortamento().compareTo(zero) > 0 && !bene.getFlTotalmenteScaricato()) {

                                log_riga = getRigaLog(log.getPg_esecuzione(), listLogRighe.size() + 1, "W", "Batch di Ammortamento Beni",
                                        "Ammortamento Bene- L'assestato del bene vale 0 e risulta non totalmente scaricato e con imponibile ammortamento > 0 - esercizio:" + esercizio +
                                               " pg_inventario:" + bene.getPgInventario() + " nr_inventario : " + bene.getNrInventario() + " progressivo:" + bene.getProgressivo());

                            try {
                                listLogRighe.add((Batch_log_rigaBulk) batchControlComponentSession.creaConBulkRequiresNew(uc, log_riga));
                            } catch (ComponentException | RemoteException ex) {
                                SendMail.sendErrorMail(subjectError, "Errore durante l'inserimento dell'errore in Batch_log_rigaBulk " + ex.getMessage());
                                throw new DetailedRuntimeException(ex);
                            }
                            SendMail.sendErrorMail(subjectError, "Ammortamento Bene- L'assestato del bene vale 0 e risulta non totalmente scaricato e con imponibile ammortamento > 0 - esercizio:" + esercizio +
                                    " pg_inventario:" + bene.getPgInventario() + " nr_inventario : " + bene.getNrInventario() + " progressivo:" + bene.getProgressivo());
                        }
                        else{
// Gestione ammortamento del bene (ricoda percentuale)

                            ammortizzaBene(uc,bene,esercizio);
                        }

                        try {
                            log_riga = getRigaLog(log.getPg_esecuzione(), listLogRighe.size() + 1, "I", "Batch di Ammortamento Beni",
                                    "Ammortamento Bene esercizio:" + esercizio +
                                            " pg_inventario:" + bene.getPgInventario() + " nr_inventario : " + bene.getNrInventario() + " progressivo:" + bene.getProgressivo()
                                            + "End:" + formatterTime.format(EJBCommonServices.getServerTimestamp().toInstant()));
                            try {
                                listLogRighe.add((Batch_log_rigaBulk) batchControlComponentSession.creaConBulkRequiresNew(uc, log_riga));
                            } catch (ComponentException | RemoteException ex) {
                                SendMail.sendErrorMail(subjectError, "Errore durante l'inserimento dell'errore in Batch_log_rigaBulk " + ex.getMessage());
                                throw new DetailedRuntimeException(ex);
                            }
                        } catch (DetailedRuntimeException ex) {
                            log_riga = getRigaLog(log.getPg_esecuzione(), listLogRighe.size() + 1, "E", "Batch di Ammortamento Beni",
                                    "Errore durante Ammortamento Bene esercizio:" + esercizio +
                                            " pg_inventario:" + bene.getPgInventario() + " nr_inventario : " + bene.getNrInventario() + " progressivo:" + bene.getProgressivo() + "Errore :" + ex.getMessage());
                            try {
                                listLogRighe.add((Batch_log_rigaBulk) batchControlComponentSession.creaConBulkRequiresNew(uc, log_riga));
                            } catch (ComponentException | RemoteException ex1) {
                                SendMail.sendErrorMail(subjectError, "Errore durante l'inserimento dell'errore in Batch_log_rigaBulk " + ex1.getMessage());
                                throw new DetailedRuntimeException(ex1);
                            }
                        }


                    } catch (DetailedRuntimeException ex) {
                        log_riga = getRigaLog(log.getPg_esecuzione(), listLogRighe.size() + 1, "E", "Batch di Ammortamento Beni",
                                "Errore durante Ammortamento Bene esercizio:" + esercizio +
                                        " pg_inventario:" + bene.getPgInventario() + " nr_inventario : " + bene.getNrInventario() + " progressivo:" + bene.getProgressivo() + "Errore :" + ex.getMessage());

                        try {
                            listLogRighe.add((Batch_log_rigaBulk) batchControlComponentSession.creaConBulkRequiresNew(uc, log_riga));
                        } catch (ComponentException | RemoteException ex1) {
                            SendMail.sendErrorMail(subjectError, "Errore durante l'inserimento dell'errore in Batch_log_rigaBulk " + ex1.getMessage());
                            throw new DetailedRuntimeException(ex1);
                        }
                    }
                }

            } catch (DetailedRuntimeException ex) {
                log_riga = getRigaLog(log.getPg_esecuzione(), listLogRighe.size() + 1, "E", "Batch di Ammortamento Beni",
                        "Errore durante Ammortamento dei Beni Esercizio :" + esercizio + "Errore :" + ex.getMessage());
                try {
                    listLogRighe.add((Batch_log_rigaBulk) batchControlComponentSession.creaConBulkRequiresNew(uc, log_riga));
                } catch (ComponentException | RemoteException ex1) {
                    SendMail.sendErrorMail(subjectError, "Errore durante l'inserimento dell'errore in Batch_log_rigaBulk " + ex1.getMessage());
                    throw new DetailedRuntimeException(ex1);
                }
            }

            log_riga = getRigaLog(log.getPg_esecuzione(), listLogRighe.size() + 1, "I", "Batch di Ammortamento Beni",
                    "Elaborazione Beni da Ammortizzare Esercizio :" + esercizio + "End:" + formatterTime.format(EJBCommonServices.getServerTimestamp().toInstant()) + "Beni elaborati: " + beniDaElaborareList.size());

            try {
                listLogRighe.add((Batch_log_rigaBulk) batchControlComponentSession.creaConBulkRequiresNew(uc, log_riga));
            } catch (ComponentException | RemoteException ex) {
                SendMail.sendErrorMail(subjectError, "Errore durante l'inserimento dell'errore in Batch_log_rigaBulk " + ex.getMessage());
                throw new DetailedRuntimeException(ex);
            }

        } catch (DetailedRuntimeException ex) {
            log_riga = getRigaLog(log.getPg_esecuzione(), listLogRighe.size() + 1, "E", "Batch di Ammortamento Beni",
                    "Errore durante Elaborazione Beni da Ammortizzare Esercizio : Esercizio :" + esercizio + "Errore:" + ex.getMessage());
            try {
                listLogRighe.add((Batch_log_rigaBulk) batchControlComponentSession.creaConBulkRequiresNew(uc, log_riga));
            } catch (ComponentException | RemoteException ex1) {
                SendMail.sendErrorMail(subjectError, "Errore durante l'inserimento dell'errore in Batch_log_rigaBulk " + ex1.getMessage());
                throw new DetailedRuntimeException(ex1);
            }
        }
// AMMORTAMENTO FINE
        log_riga = getRigaLog(log.getPg_esecuzione(), listLogRighe.size() + 1, "I", "Batch di Ammortamento Beni",
                "Batch di Ammortamento Beni . Esercizio:" + esercizio + "End:" + formatterTime.format(EJBCommonServices.getServerTimestamp().toInstant()));
        try {
            listLogRighe.add((Batch_log_rigaBulk) batchControlComponentSession.creaConBulkRequiresNew(uc, log_riga));
        } catch (ComponentException | RemoteException ex) {
            SendMail.sendErrorMail(subjectError, "Errore durante l'inserimento dell'errore in Batch_log_rigaBulk " + ex.getMessage());
            throw new DetailedRuntimeException(ex);
        }
    }


    private Integer eliminaAmmortamentiPrecedentiDellEsercizio(UserContext uc, Integer esercizio, String subjectError) {

        AmmortamentoBeneComponentSession ammortamentoBeneComponent = Utility.createAmmortamentoBeneComponentSession();
        List<Ammortamento_bene_invBulk> listaAmmortamenti = null;

        try {
            listaAmmortamenti = ammortamentoBeneComponent.findAllAmmortamenti(uc, esercizio);

        } catch (RemoteException | InvocationTargetException ex) {
            SendMail.sendErrorMail(subjectError, "Errore durante la lettura degli ammortamenti dell'esercizio " + esercizio + " - Errore: " + ex.getMessage());
            throw new DetailedRuntimeException(ex);
        }
        listaAmmortamenti.stream()
                .forEach(ammortamento_bene_invBulk -> {

                    ripristinaInventarioBeni(uc, ammortamento_bene_invBulk.getPgInventario(),ammortamento_bene_invBulk.getNrInventario(),ammortamento_bene_invBulk.getProgressivo(),
                                             ammortamento_bene_invBulk.getImMovimentoAmmort().multiply(menoUno));

                    try {
                        ammortamento_bene_invBulk.setToBeDeleted();
                        ammortamentoBeneComponent.eliminaConBulk(uc, ammortamento_bene_invBulk);
                    } catch (RemoteException | ComponentException ex) {
                        SendMail.sendErrorMail(subjectError, "Errore durante l'eliminazione dell'ammortamento esercizio :" + esercizio + " pg_inventario: " + ammortamento_bene_invBulk.getPgInventario() +
                                " nr_inventario: " + ammortamento_bene_invBulk.getNrInventario() + " progressivo: " + ammortamento_bene_invBulk.getProgressivo() + "- Errore: " + ex.getMessage());
                        throw new DetailedRuntimeException(ex);
                    }
                });

        return listaAmmortamenti.size();
    }

    private void ripristinaInventarioBeni(UserContext uc, Long pgInventario,Long nrInventario, Long progressivo,BigDecimal aggiornamentoImporto) {

        Inventario_beniComponentSession inventarioBeniComponent = Utility.createInventario_beniComponentSession();

        try {
            Inventario_beniBulk inventario_beniBulk = inventarioBeniComponent.getBeneInventario(uc, pgInventario,nrInventario,progressivo);
            inventario_beniBulk.setValore_ammortizzato(
                    inventario_beniBulk.getValore_ammortizzato().add(aggiornamentoImporto));

            inventario_beniBulk.setToBeUpdated();

            inventarioBeniComponent.modificaConBulk(uc, inventario_beniBulk);

        } catch (RemoteException | ComponentException ex) {
            SendMail.sendErrorMail(subjectError, "Errore durante il ripristino del bene pg_inventario: " + pgInventario +
                    " nr_inventario: " + nrInventario + " progressivo: " + progressivo + "- Errore: " + ex.getMessage());
            throw new DetailedRuntimeException(ex);
        }

    }

    private List<V_ammortamento_beniBulk> getAllBeniDaElaborare(UserContext uc, Integer esercizio, String subjectError) {
        V_AmmortamentoBeniComponentSession v_ammortamentoBeneComponent = Utility.createV_AmmortamentoBeniComponentSession();

        List<V_ammortamento_beniBulk> listaV_AmmortamentiDB = null;
        List<V_ammortamento_beniBulk> listaV_AmmortamentiWork = new ArrayList<V_ammortamento_beniBulk>();


        try {
            listaV_AmmortamentiDB = v_ammortamentoBeneComponent.findAllBeniDaAmmortizare(uc, esercizio);

        } catch (RemoteException | ComponentException ex) {
            SendMail.sendErrorMail(subjectError, "Errore durante la lettura dei beni da ammortizzare " + esercizio + " - Errore: " + ex.getMessage());
            throw new DetailedRuntimeException(ex);
        }
        for (V_ammortamento_beniBulk v_ammortamentoBene : listaV_AmmortamentiDB) {

            Tipo_ammortamentoBulk tipoAmmortamento = getTipoAmmortamento(uc, v_ammortamentoBene, esercizio, subjectError);

            if (tipoAmmortamento == null) {
                SendMail.sendErrorMail(subjectError, "Errore durante la lettura del Tipo Ammortamento : tipoAmmortamento:" + v_ammortamentoBene.getTiAmmortamento() +
                        " categoria gruppo:" + v_ammortamentoBene.getCdCategoriaGruppo() + " esercizio : " + esercizio);
                //throw new DetailedRuntimeException("Errore durante la lettura del Tipo Ammortamento : tipoAmmortamento:"+v_ammortamentoBene.getTiAmmortamento() +
                //		" categoria gruppo:"+v_ammortamentoBene.getCdCategoriaGruppo()+" esercizio : "+esercizio);
            } else {
                v_ammortamentoBene.setEsercizioCompetenza(esercizio);
                v_ammortamentoBene.setCdTipoAmmortamento(tipoAmmortamento.getCd_tipo_ammortamento());
                v_ammortamentoBene.setTiAmmortamentoBene(tipoAmmortamento.getTi_ammortamento());
                v_ammortamentoBene.setDtCancellazione(tipoAmmortamento.getDt_cancellazione());
                v_ammortamentoBene.setPercPrimoAnno(tipoAmmortamento.getPerc_primo_anno());
                v_ammortamentoBene.setPercSuccessivi(tipoAmmortamento.getPerc_successivi());
                v_ammortamentoBene.setNumeroAnni(tipoAmmortamento.getNumero_anni());

                listaV_AmmortamentiWork.add(v_ammortamentoBene);
            }
        }
        return listaV_AmmortamentiWork;
    }

    private Tipo_ammortamentoBulk getTipoAmmortamento(UserContext uc, V_ammortamento_beniBulk v_ammortamentoBene, Integer esercizio, String subjectError) {
        Tipo_ammortamentoComponentSession tipo_ammortamentoComponent = Utility.createTipo_ammortamentoComponentSession();
        List<Tipo_ammortamentoBulk> listaTipoAmmortamento = null;

        try {
            listaTipoAmmortamento = tipo_ammortamentoComponent.findTipoAmmortamento(uc, v_ammortamentoBene.getTiAmmortamento(), v_ammortamentoBene.getCdCategoriaGruppo(), esercizio);

        } catch (RemoteException | ComponentException ex) {
            SendMail.sendErrorMail(subjectError, "Errore durante la lettura del Tipo Ammortamento : tipoAmmortamento:" + v_ammortamentoBene.getTiAmmortamento() +
                    " categoria gruppo:" + v_ammortamentoBene.getCdCategoriaGruppo() + " esercizio : " + esercizio + " - Errore: " + ex.getMessage());
            throw new DetailedRuntimeException(ex);
        }
        if (listaTipoAmmortamento == null || listaTipoAmmortamento.isEmpty()) {
            SendMail.sendErrorMail(subjectError, "Tipo Ammortamento NON TROVATO per: tipoAmmortamento:" + v_ammortamentoBene.getTiAmmortamento() +
                    " categoria gruppo:" + v_ammortamentoBene.getCdCategoriaGruppo() + " esercizio : " + esercizio);
        } else if (listaTipoAmmortamento.size() > 1) {
            SendMail.sendErrorMail(subjectError, "Tipo Ammortamento MULTIPLO per : tipoAmmortamento:" + v_ammortamentoBene.getTiAmmortamento() +
                    " categoria gruppo:" + v_ammortamentoBene.getCdCategoriaGruppo() + " esercizio : " + esercizio);
        } else {
            return listaTipoAmmortamento.get(0);
        }
        return null;
    }

    private List<V_ammortamento_beniBulk> getAllBeniNormalizzati(UserContext uc, List<V_ammortamento_beniBulk> beniDaElaborare, Integer esercizio, String subjectError) {
        List<V_ammortamento_beniBulk> beniDaElabNorm = new ArrayList<V_ammortamento_beniBulk>();

        V_InventarioBeneDetComponentSession v_inventarioBeneDetComponent = Utility.createV_InventarioBeneDetComponentSession();
        NormalizzatoreAmmortamentoDto normBene = null;

        for (V_ammortamento_beniBulk beneDaElab : beniDaElaborare) {
            try {
                normBene = v_inventarioBeneDetComponent.findNormalizzatoreBene(uc, esercizio, beneDaElab.getPgInventario(), beneDaElab.getNrInventario(), beneDaElab.getProgressivo());
            } catch (ComponentException | RemoteException e) {
                SendMail.sendErrorMail(subjectError, "Errore durante la lettura per la 'normalizzazione' del bene : esercizio:" + esercizio +
                        " pg_inventario:" + beneDaElab.getPgInventario() + " nr_inventario : " + beneDaElab.getNrInventario() + " progressivo:" + beneDaElab.getProgressivo() + " - Errore: " + e.getMessage());
            }

            if (normBene == null) {
                SendMail.sendErrorMail(subjectError, "Errore in fase di 'normalizzazione' non è stato trovato il bene : esercizio:" + esercizio +
                        " pg_inventario:" + beneDaElab.getPgInventario() + " nr_inventario : " + beneDaElab.getNrInventario() + " progressivo:" + beneDaElab.getProgressivo());
            }


            // se nell'oggetto normalizzatore risultano variazioni che sono state fatte oltre la fine dell'esercizio
            if ((normBene.getIncrementiSuccessivi().compareTo(zero) > 0) || (normBene.getDecrementiSuccessivi().compareTo(zero) > 0) ||
                    (normBene.getQuotaStorniSuccessivi().compareTo(zero) > 0)) {

                beneDaElab.setFlTotalmenteScaricato(false);

                BigDecimal importoTotIncrDecr = normBene.getIncrementiSuccessivi().add(normBene.getDecrementiSuccessivi().multiply(menoUno));

                beneDaElab.setImponibileAmmortamento(beneDaElab.getImponibileAmmortamento().add(importoTotIncrDecr.multiply(menoUno)));
                beneDaElab.setVariazionePiu(beneDaElab.getVariazionePiu().add(normBene.getIncrementiSuccessivi().multiply(menoUno)));
                beneDaElab.setVariazioneMeno(beneDaElab.getVariazioneMeno().add(normBene.getDecrementiSuccessivi().multiply(menoUno)));
                beneDaElab.setValoreAmmortizzato(beneDaElab.getValoreAmmortizzato().add(normBene.getQuotaStorniSuccessivi()));
            }

            beniDaElabNorm.add(beneDaElab);
        }
        return beniDaElabNorm;
    }

    private void ammortizzaBene(UserContext uc,V_ammortamento_beniBulk bene,Integer esercizio){

        BigDecimal percAmmortamento  = bene.getEsercizioCaricoBene()==esercizio ? bene.getPercPrimoAnno() : bene.getPercSuccessivi();
        BigDecimal rataAmmortamento = null;


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

        if(bene.getImponibileAmmortamento().compareTo(zero) > 0 && !bene.getFlTotalmenteScaricato() &&
                bene.getValoreAmmortizzato().compareTo(bene.getImponibileAmmortamento()) < 0){
            rataAmmortamento=((bene.getImponibileAmmortamento().multiply(percAmmortamento)).divide(cento));
        }

        if((rataAmmortamento.add(bene.getValoreAmmortizzato())).compareTo(bene.getImponibileAmmortamento()) > 0){
            rataAmmortamento = bene.getImponibileAmmortamento().add(bene.getValoreAmmortizzato().multiply(menoUno));
        }

        // aggiorna il valore ammortizzato
        ripristinaInventarioBeni(uc,bene.getPgInventario(),bene.getNrInventario(),bene.getProgressivo(),rataAmmortamento);

        // inserisce riga ammortamento
        inserisciAmmortamentoBene(uc,bene,esercizio,rataAmmortamento,percAmmortamento);

    }

    private void inserisciAmmortamentoBene(UserContext uc,V_ammortamento_beniBulk bene,Integer esercizio,BigDecimal rataAmm, BigDecimal percAmm){
        AmmortamentoBeneComponentSession ammortamentoComponent = Utility.createAmmortamentoBeneComponentSession();

        try {
            Integer numeroAnnoAmm = ammortamentoComponent.getNumeroAnnoAmmortamento( uc, bene.getPgInventario(),bene.getNrInventario(),bene.getProgressivo());
            Integer progRigaAmm = ammortamentoComponent.getProgressivoRigaAmmortamento(uc, bene.getPgInventario(),bene.getNrInventario(),bene.getProgressivo(),esercizio);

            Ammortamento_bene_invBulk ammortamento = new Ammortamento_bene_invBulk();
            ammortamento.setPgInventario(bene.getPgInventario());
            ammortamento.setNrInventario(bene.getNrInventario());
            ammortamento.setProgressivo(bene.getProgressivo());
            ammortamento.setEsercizio(esercizio);
            ammortamento.setCdTipoAmmortamento(bene.getCdTipoAmmortamento());
            ammortamento.setTiAmmortamento(bene.getTiAmmortamento());
            ammortamento.setCdCategoriaGruppo(bene.getCdCategoriaGruppo());
            ammortamento.setEsercizioCompetenza(bene.getEsercizioCompetenza());
            ammortamento.setImponibileAmmortamento(bene.getImponibileAmmortamento());
            ammortamento.setImMovimentoAmmort(rataAmm);
            ammortamento.setPercAmmortamento(percAmm);
            ammortamento.setNumeroAnni(bene.getNumeroAnni());
            ammortamento.setNumeroAnno(numeroAnnoAmm);
            ammortamento.setPercPrimoAnno(bene.getPercPrimoAnno());
            ammortamento.setPercSuccessivi(bene.getPercSuccessivi());
            ammortamento.setCdCdsUbicazione(bene.getCdCds());
            ammortamento.setCdUoUbicazione(bene.getCdUnitaOrganizzativa());
            ammortamento.setPgRiga(progRigaAmm);
            ammortamento.setFlStorno(false);
            ammortamento.setToBeCreated();

            ammortamentoComponent.creaConBulk(uc,ammortamento);


        } catch (RemoteException | ComponentException ex) {
            SendMail.sendErrorMail(subjectError, "Errore durante l'inserimento dell'ammortamento del bene -  pg_inventario: " + bene.getPgInventario() +
                    " nr_inventario: " + bene.getNrInventario() + " progressivo: " + bene.getProgressivo() + "- Errore: " + ex.getMessage());
            throw new DetailedRuntimeException(ex);
        }
    }

    private Batch_log_rigaBulk getRigaLog(BigDecimal progEsecuzione, Integer progRiga, String tipoMess, String messaggio, String note) {
        Batch_log_rigaBulk log_riga = new Batch_log_rigaBulk();
        log_riga.setPg_esecuzione(progEsecuzione);
        log_riga.setPg_riga(BigDecimal.valueOf(progRiga));
        log_riga.setTi_messaggio(tipoMess);
        log_riga.setMessaggio(messaggio);
        log_riga.setTrace(log_riga.getMessaggio());
        log_riga.setNote(note);
        log_riga.setToBeCreated();

        return log_riga;
    }
}

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
import it.cnr.contab.inventario00.dto.TipoAmmCatGruppoDto;
import it.cnr.contab.inventario00.tabrif.bulk.Tipo_ammortamentoBulk;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Stateless(name = "CNRINVENTARIO00_EJB_AsyncAmmortamentoBeneComponentSession")
public class AsyncAmmortamentoBeneComponentSessionBean extends it.cnr.jada.ejb.CRUDComponentSessionBean implements AsyncAmmortamentoBeneComponentSession {
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(AsyncAmmortamentoBeneComponentSessionBean.class);

    public static it.cnr.jada.ejb.CRUDComponentSessionBean newInstance() throws javax.ejb.EJBException {
        return new AsyncAmmortamentoBeneComponentSessionBean();
    }

    private final BigDecimal zero=new BigDecimal(0);
    private final BigDecimal menoUno = new BigDecimal(-1);
    private final BigDecimal cento = new BigDecimal(100);
    private final String SEPARATORE="-";

    private final String subjectError = "Errore Ammortamento beni";

 //   @Asynchronous
    public void asyncAmmortamentoBeni(UserContext uc, Integer esercizio) throws ComponentException, PersistencyException, RemoteException {
        logger.info("Inizio Ammortamento Beni ".concat("esercizio:").concat(esercizio.toString()));
        ammortamentoBeni(uc, esercizio);
        logger.info("Fine Ammortamento Beni ".concat("esercizio:").concat(esercizio.toString()));
    }

    private void ammortamentoBeni(UserContext uc, Integer esercizio) throws ComponentException {

        DateTimeFormatter formatterTime = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss").withZone(ZoneId.systemDefault());


        AmmortamentoBeneComponentSession ammortamentoBeneComponent = Utility.createAmmortamentoBeneComponentSession();
        Inventario_beniComponentSession inventarioBeniComponent = Utility.createInventario_beniComponentSession();
        V_AmmortamentoBeniComponentSession v_ammortamentoBeneComponent = Utility.createV_AmmortamentoBeniComponentSession();
        V_InventarioBeneDetComponentSession v_inventarioBeneDetComponent = Utility.createV_InventarioBeneDetComponentSession();
        Tipo_ammortamentoComponentSession tipo_ammortamentoComponent = Utility.createTipo_ammortamentoComponentSession();

// ANNULLAMENTO AMMORTAMENTO ESERCIZIO CORRENTE INIZIO
        try {
            logger.info("Eliminazione Ammortamento Esercizio :" + esercizio + "Start:" + formatterTime.format(EJBCommonServices.getServerTimestamp().toInstant()));
            // Elimina eventuali ammortamenti presenti per l'esercizio , ripristina INVENTARIO_BENI e cancella AMMORTAMENTO_BENI
            Integer beniElaborati = eliminaAmmortamentiPrecedentiDellEsercizio(uc,ammortamentoBeneComponent,inventarioBeniComponent, esercizio, subjectError);
            logger.info("Eliminazione Ammortamento Esercizio :" + esercizio + "Eliminati: "+beniElaborati+" beni - Fine:" + formatterTime.format(EJBCommonServices.getServerTimestamp().toInstant()));
        } catch (DetailedRuntimeException ex) {
            logger.info("Errore durante Eliminazione Ammortamento Esercizio :" + esercizio + "Errore: " + ex.getMessage());
            throw new DetailedRuntimeException("Errore durante Eliminazione Ammortamento Esercizio :" + esercizio + "Errore: " + ex.getMessage());
        }
// ANNULLAMENTO AMMORTAMENTO ESERCIZIO CORRENTE FINE

// AMMORTAMENTO INIZIO
        try {
            logger.info("Elaborazione Beni da Ammortizzare Esercizio :" + esercizio + "Start:" + formatterTime.format(EJBCommonServices.getServerTimestamp().toInstant()));
            List<V_ammortamento_beniBulk> beniDaElaborareList = null;
            // TIPO AMMORTAMENTO
            try {
                logger.info("Verifica tipo ammortamento :" + esercizio + "Start:" + formatterTime.format(EJBCommonServices.getServerTimestamp().toInstant()));
                // preleva per ogni bene da ammortizzare (V_AMMORTAMENTO_BENI) tutti quelli che hanno un tipo ammortamento associato
                beniDaElaborareList = getAllBeniDaElaborare(uc, v_ammortamentoBeneComponent, tipo_ammortamentoComponent,esercizio, subjectError);

                if (beniDaElaborareList == null || beniDaElaborareList.isEmpty()) {
                    logger.info("Errore non risultano beni da ammortizzare esercizio: " + esercizio);
                    throw new DetailedRuntimeException("Errore non risultano beni da ammortizzare esercizio: " + esercizio);
                }
                logger.info("Verifica tipo ammortamento :" + esercizio +" beni da elaborare:" + beniDaElaborareList.size() + "End:" + formatterTime.format(EJBCommonServices.getServerTimestamp().toInstant()));

            } catch (DetailedRuntimeException ex) {
                logger.info("Errore durante Verifica tipo ammortamento :" + esercizio + "Errore :" + ex.getMessage());
                throw new DetailedRuntimeException("Errore durante Verifica tipo ammortamento :" + esercizio + "Errore :" + ex.getMessage());
            }

            // NORMALIZZATORE
            try {
                logger.info("Normalizza movimenti successivi all'esercizio:" + esercizio + "Start:" + formatterTime.format(EJBCommonServices.getServerTimestamp().toInstant()));
                // normalizza importi in base a movimenti futuri alla fine dell'esercizio
                beniDaElaborareList = getAllBeniNormalizzati(uc,v_inventarioBeneDetComponent, beniDaElaborareList, esercizio, subjectError);

                if (beniDaElaborareList == null || beniDaElaborareList.isEmpty()) {
                    logger.info("Errore non risultano beni dopo normalizzazione esercizio: " + esercizio);
                    throw new DetailedRuntimeException("Errore non risultano beni dopo normalizzazione esercizio: " + esercizio);

                }
                logger.info("Normalizza movimenti successivi all'esercizio:" + esercizio + " beni da elaborare:" + beniDaElaborareList.size() + "End:" + formatterTime.format(EJBCommonServices.getServerTimestamp().toInstant()));

            } catch (DetailedRuntimeException ex) {

                logger.info("Errore durante Normalizzazione beni Esercizio :" + esercizio + "Errore:" + ex.getMessage());
                throw new DetailedRuntimeException("Errore durante Normalizzazione beni Esercizio :" + esercizio + "Errore:" + ex.getMessage());
            }
            // AMMORTAMENTO

            try {
                logger.info("Ammortamento dei Beni :" + esercizio + "Start:" + formatterTime.format(EJBCommonServices.getServerTimestamp().toInstant()));

                for (V_ammortamento_beniBulk bene : beniDaElaborareList) {
                    try {
                        if(bene.getValoreAmmortizzato().compareTo( bene.getImponibileAmmortamento())>0 && !bene.getFlTotalmenteScaricato()){
                            logger.info("il valore ammortizzato è maggiore dell''imponibile ammortamento esercizio:" + esercizio +
                            " pg_inventario:" + bene.getPgInventario() + " nr_inventario : " + bene.getNrInventario() + " progressivo:" + bene.getProgressivo());

                            // se valore inziale + variazione più - variazione meno = 0 e imponibile ammortamento > 0 e bene non completamente scaricato
                        }else if( ((bene.getValoreIniziale().add(bene.getVariazionePiu())).add(bene.getVariazioneMeno().multiply(menoUno))).compareTo(zero) ==0
                                                                                            &&
                                bene.getImponibileAmmortamento().compareTo(zero) > 0 && !bene.getFlTotalmenteScaricato()) {

                                logger.info("L'assestato del bene vale 0 e risulta non totalmente scaricato e con imponibile ammortamento > 0 - esercizio:" + esercizio +
                                        " pg_inventario:" + bene.getPgInventario() + " nr_inventario : " + bene.getNrInventario() + " progressivo:" + bene.getProgressivo());
                        }
                        else{
                            // Gestione ammortamento del bene
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


                            Inventario_beniBulk inv=null;

                            // aggiorna il valore ammortizzato
                            inv= aggiornaInventarioBeni(uc,inventarioBeniComponent,bene.getPgInventario(),bene.getNrInventario(),bene.getProgressivo(),rataAmmortamento);

                            if(inv == null){
                                logger.info("Errore durante l'aggiornamento del bene esercizio :" + esercizio + " pg_inventario: " + bene.getPgInventario() +
                                        " nr_inventario: " + bene.getNrInventario() + " progressivo: " + bene.getProgressivo() );
                                throw new DetailedRuntimeException("Errore durante l'aggiornamento del bene esercizio :" + esercizio + " pg_inventario: " + bene.getPgInventario() +
                                        " nr_inventario: " + bene.getNrInventario() + " progressivo: " + bene.getProgressivo() );
                            }
                            try {
                                inventarioBeniComponent.aggiornamentoInventarioBeneConAmmortamento(uc,inv);
                            }
                            catch (RemoteException e) {
                                logger.info("Errore durante l'aggiornamento del valore ammortizzato sul bene esercizio :" + esercizio + " pg_inventario: " + bene.getPgInventario()+
                                        " nr_inventario: " + bene.getNrInventario() + " progressivo: " + bene.getProgressivo()+" - Error:"+e.getMessage());
                                throw new DetailedRuntimeException("Errore durante l'aggiornamento del valore ammortizzato sul bene esercizio :" + esercizio + " pg_inventario: " + bene.getPgInventario()+
                                        " nr_inventario: " + bene.getNrInventario() + " progressivo: " + bene.getProgressivo()+" - Error:"+e.getMessage());

                            }
                            Ammortamento_bene_invBulk amm = null;
                            // inserisce riga ammortamento
                            amm = creaAmmortamentoBene(uc,ammortamentoBeneComponent,bene,esercizio,rataAmmortamento,percAmmortamento);
                            if(amm == null){
                                logger.info("Errore durante la creazione dell'ammortamento esercizio:" + esercizio + " pg_inventario: " + bene.getPgInventario() +
                                        " nr_inventario: " + bene.getNrInventario() + " progressivo: " + bene.getProgressivo() );
                                throw new DetailedRuntimeException("Errore durante la creazione dell'ammortamento esercizio :" + esercizio + " pg_inventario: " + bene.getPgInventario() +
                                        " nr_inventario: " + bene.getNrInventario() + " progressivo: " + bene.getProgressivo() );
                            }
                            try {
                                ammortamentoBeneComponent.inserisciAmmortamentoBene(uc,amm);
                            }
                            catch (RemoteException e) {
                                logger.info("Errore durante l'inserimento dell'amomortamento esercizio :" + esercizio + " pg_inventario: " + bene.getPgInventario()+
                                        " nr_inventario: " + bene.getNrInventario() + " progressivo: " + bene.getProgressivo()+" - Error:"+e.getMessage());
                                throw new DetailedRuntimeException("Errore durante l'inserimento dell'amomortamento esercizio :" + esercizio + " pg_inventario: " + bene.getPgInventario()+
                                        " nr_inventario: " + bene.getNrInventario() + " progressivo: " + bene.getProgressivo()+" - Error:"+e.getMessage());
                            }
                        }

                        logger.info("Ammortamento Bene esercizio:" + esercizio +
                                            " pg_inventario:" + bene.getPgInventario() + " nr_inventario : " + bene.getNrInventario() + " progressivo:" + bene.getProgressivo()
                                            + "End:" + formatterTime.format(EJBCommonServices.getServerTimestamp().toInstant()));
                    } catch (DetailedRuntimeException ex) {
                        logger.info("Errore durante Ammortamento Bene esercizio:" + esercizio +
                                " pg_inventario:" + bene.getPgInventario() + " nr_inventario : " + bene.getNrInventario() + " progressivo:" + bene.getProgressivo()
                                + "Error:" + ex.getMessage());
                        throw new DetailedRuntimeException("Errore durante Ammortamento Bene esercizio:" + esercizio +
                                " pg_inventario:" + bene.getPgInventario() + " nr_inventario : " + bene.getNrInventario() + " progressivo:" + bene.getProgressivo()
                                + "Error:" + ex.getMessage());
                    }
                }

            } catch (DetailedRuntimeException ex) {
                logger.info("Errore durante Ammortamento dei Beni esercizio:" + esercizio +
                           "Error:" + ex.getMessage());
                throw new DetailedRuntimeException("Errore durante Ammortamento dei Beni esercizio:" + esercizio +
                        "Error:" + ex.getMessage());
             }
            logger.info("Elaborazione Beni da Ammortizzare Esercizio :" + esercizio + "End:" + formatterTime.format(EJBCommonServices.getServerTimestamp().toInstant()));


        } catch (DetailedRuntimeException ex) {
            logger.info("Errore durante Elaborazione Beni da Ammortizzare Esercizio:" + esercizio + " Errore:" + ex.getMessage());
            throw new DetailedRuntimeException("Errore durante Elaborazione Beni da Ammortizzare Esercizio:" + esercizio + " Errore:" + ex.getMessage());

        }
// AMMORTAMENTO FINE
        logger.info("Batch di Ammortamento Beni . Esercizio:" + esercizio + "End:" + formatterTime.format(EJBCommonServices.getServerTimestamp().toInstant()));
    }


    private Integer eliminaAmmortamentiPrecedentiDellEsercizio(UserContext uc,  AmmortamentoBeneComponentSession ammortamentoBeneComponent,
                                                                                Inventario_beniComponentSession inventarioBeniComponent,Integer esercizio, String subjectError) {
        List<Ammortamento_bene_invBulk> listaAmmortamenti = null;
        try {
            listaAmmortamenti = ammortamentoBeneComponent.findAllAmmortamenti(uc, esercizio);

        } catch (RemoteException | InvocationTargetException ex) {
            logger.info("Errore durante la lettura degli ammortamenti dell'esercizio " + esercizio + " - Errore: " + ex.getMessage());
            throw new DetailedRuntimeException("Errore durante la lettura degli ammortamenti dell'esercizio " + esercizio + " - Errore: " + ex.getMessage());
        }
        if(listaAmmortamenti!=null)
        {
            List<Inventario_beniBulk> invList = new ArrayList<Inventario_beniBulk>();
            listaAmmortamenti.stream()
                    .forEach(ammortamento_bene_invBulk -> {

                        Inventario_beniBulk inv = aggiornaInventarioBeni(uc,inventarioBeniComponent, ammortamento_bene_invBulk.getPgInventario(),ammortamento_bene_invBulk.getNrInventario(),ammortamento_bene_invBulk.getProgressivo(),
                                ammortamento_bene_invBulk.getImMovimentoAmmort().multiply(menoUno));

                        if(inv == null){
                            logger.info("Errore durante l'eliminazione dell'ammortamento esercizio :" + esercizio + " pg_inventario: " + ammortamento_bene_invBulk.getPgInventario() +
                                    " nr_inventario: " + ammortamento_bene_invBulk.getNrInventario() + " progressivo: " + ammortamento_bene_invBulk.getProgressivo() );
                            throw new DetailedRuntimeException("Errore durante l'eliminazione dell'ammortamento esercizio :" + esercizio + " pg_inventario: " + ammortamento_bene_invBulk.getPgInventario() +
                                    " nr_inventario: " + ammortamento_bene_invBulk.getNrInventario() + " progressivo: " + ammortamento_bene_invBulk.getProgressivo() );
                        }
                        try {
                            inventarioBeniComponent.aggiornamentoInventarioBeneConAmmortamento(uc,inv);
                        }
                        catch (RemoteException | ComponentException e) {
                            logger.info("Errore durante il ripristino del valore ammortizzato sul bene " + e.getMessage());
                            throw new DetailedRuntimeException("Errore durante il ripristinodel valore ammortizzato sul bene " + e.getMessage());
                        }
                        try {
                            ammortamentoBeneComponent.cancellaiAmmortamentoBene(uc, ammortamento_bene_invBulk);
                        } catch (RemoteException | ComponentException ex) {
                            logger.info("Errore durante l'eliminazione dell'ammortamento esercizio :" + esercizio + " pg_inventario: " + ammortamento_bene_invBulk.getPgInventario() +
                                    " nr_inventario: " + ammortamento_bene_invBulk.getNrInventario() + " progressivo: " + ammortamento_bene_invBulk.getProgressivo() + "- Errore: " + ex.getMessage());
                            throw new DetailedRuntimeException("Errore durante l'eliminazione dell'ammortamento esercizio :" + esercizio + " pg_inventario: " + ammortamento_bene_invBulk.getPgInventario() +
                                    " nr_inventario: " + ammortamento_bene_invBulk.getNrInventario() + " progressivo: " + ammortamento_bene_invBulk.getProgressivo() + "- Errore: " + ex.getMessage());
                        }
                    });
            return listaAmmortamenti.size();
        }
        return 0;

    }

    private Inventario_beniBulk aggiornaInventarioBeni(UserContext uc,Inventario_beniComponentSession inventarioBeniComponent, Long pgInventario,Long nrInventario, Long progressivo,BigDecimal aggiornamentoImporto) {

        Inventario_beniBulk inventario_beniBulk = null;
        try {
            inventario_beniBulk = inventarioBeniComponent.getBeneInventario(uc, pgInventario,nrInventario,progressivo);
            inventario_beniBulk.setValore_ammortizzato(
                    inventario_beniBulk.getValore_ammortizzato().add(aggiornamentoImporto));


        } catch (RemoteException ex) {
            logger.info("Errore durante l'aggiornamento del bene pg_inventario: " + pgInventario +
                    " nr_inventario: " + nrInventario + " progressivo: " + progressivo + "- Errore: " + ex.getMessage());
            throw new DetailedRuntimeException("Errore durante l'aggiornamento del bene pg_inventario: " + pgInventario +
                    " nr_inventario: " + nrInventario + " progressivo: " + progressivo + "- Errore: " + ex.getMessage());
        }
        return inventario_beniBulk;
    }

    private List<V_ammortamento_beniBulk> getAllBeniDaElaborare(UserContext uc,V_AmmortamentoBeniComponentSession v_ammortamentoBeneComponent,Tipo_ammortamentoComponentSession tipo_ammortamentoComponent, Integer esercizio, String subjectError) {
        List<V_ammortamento_beniBulk> listaV_AmmortamentiDB = null;
        List<V_ammortamento_beniBulk> listaV_AmmortamentiWork = new ArrayList<V_ammortamento_beniBulk>();
        try {
            listaV_AmmortamentiDB = v_ammortamentoBeneComponent.findAllBeniDaAmmortizare(uc, esercizio);

        } catch (RemoteException | ComponentException ex) {
            logger.info("Errore durante la lettura dei beni da ammortizzare " + esercizio + " - Errore: " + ex.getMessage());
            throw new DetailedRuntimeException("Errore durante la lettura dei beni da ammortizzare " + esercizio + " - Errore: " + ex.getMessage());
        }
        HashMap<String, TipoAmmCatGruppoDto> tipoAmmortHM = null;
        try{
            tipoAmmortHM = getAllTipoAmmortamento(uc,tipo_ammortamentoComponent,esercizio, subjectError);
        }catch (Exception ex) {
            logger.info("Errore durante la lettura dell'associativa catGruppo-TipoAmm  - Errore: " + ex.getMessage());
            throw new DetailedRuntimeException("Errore durante la lettura dell'associativa catGruppo-TipoAmm  - Errore: " + ex.getMessage());
        }

        for (V_ammortamento_beniBulk v_ammortamentoBene : listaV_AmmortamentiDB) {

            TipoAmmCatGruppoDto tipoAmmBene = null;

            String keyHM = v_ammortamentoBene.getCdCategoriaGruppo()+SEPARATORE+v_ammortamentoBene.getCdTipoAmmortamento();
            if(v_ammortamentoBene.getTiAmmortamento()!=null){
                keyHM=keyHM+SEPARATORE+v_ammortamentoBene.getTiAmmortamento();
                tipoAmmBene=tipoAmmortHM.get(keyHM);
            }else{
                for (Map.Entry<String, TipoAmmCatGruppoDto> tipoAmmCatGruppo : tipoAmmortHM.entrySet()) {
                    String key = tipoAmmCatGruppo.getKey();
                    if(key.startsWith(keyHM)){
                        tipoAmmBene=tipoAmmortHM.get(key);
                    }
                }
            }

            if (tipoAmmBene == null) {
                logger.info("Errore durante la lettura del Tipo Ammortamento : tipoAmmortamento:" + v_ammortamentoBene.getTiAmmortamento() +
                        " categoria gruppo:" + v_ammortamentoBene.getCdCategoriaGruppo() + " esercizio : " + esercizio);
                throw new DetailedRuntimeException("Errore durante la lettura del Tipo Ammortamento : tipoAmmortamento:" + v_ammortamentoBene.getTiAmmortamento() +
                        " categoria gruppo:" + v_ammortamentoBene.getCdCategoriaGruppo() + " esercizio : " + esercizio);

            } else {
                v_ammortamentoBene.setEsercizioCompetenza(esercizio);
                v_ammortamentoBene.setCdTipoAmmortamento(tipoAmmBene.getCdTipoAmm());
                v_ammortamentoBene.setTiAmmortamentoBene(tipoAmmBene.getTipoAmm());
                v_ammortamentoBene.setDtCancellazione(tipoAmmBene.getDataCanc());
                v_ammortamentoBene.setPercPrimoAnno(tipoAmmBene.getPerPrimoAnno());
                v_ammortamentoBene.setPercSuccessivi(tipoAmmBene.getPerAnniSucc());
                v_ammortamentoBene.setNumeroAnni(tipoAmmBene.getNumAnni());

                listaV_AmmortamentiWork.add(v_ammortamentoBene);
            }
        }
        return listaV_AmmortamentiWork;
    }

    private HashMap<String, TipoAmmCatGruppoDto>  getAllTipoAmmortamento(UserContext uc, Tipo_ammortamentoComponentSession tipo_ammortamentoComponent,Integer esercizio, String subjectError){

        HashMap<String, TipoAmmCatGruppoDto> tipoAmmHM = null;
        List<TipoAmmCatGruppoDto> listTipoAmmDto = null;

        try {
            listTipoAmmDto =tipo_ammortamentoComponent.findTipoAmmortamento(uc,esercizio);

        }catch (RemoteException | ComponentException ex) {
            logger.info("Errore durante la lettura dei Tipi Ammortamento / Cat Gruppo - Errore: " + ex.getMessage());
            throw new DetailedRuntimeException("Errore durante la lettura dei Tipi Ammortamento / Cat Gruppo - Errore: " + ex.getMessage());
        }
        if (listTipoAmmDto == null || listTipoAmmDto.isEmpty()) {

            logger.info("Tipi Ammortamento non trovati -  esercizio : " + esercizio);
            throw new DetailedRuntimeException("Tipi Ammortamento non trovati -  esercizio : " + esercizio);

        }else {
            tipoAmmHM=new HashMap<String, TipoAmmCatGruppoDto>();
            for (TipoAmmCatGruppoDto tipoAmm : listTipoAmmDto) {

                String keyHM = tipoAmm.getCdCatGruppo() + SEPARATORE + tipoAmm.getCdTipoAmm() + SEPARATORE + tipoAmm.getTipoAmm();

                if (tipoAmmHM.get(keyHM) != null) {
                    logger.info("Tipo Ammortamento MULTIPLO per : tipoAmmortamento:" + tipoAmm.getTipoAmm() +
                            "cdTipoAmmortamento:" + tipoAmm.getCdTipoAmm() + " categoria gruppo:" + tipoAmm.getCdCatGruppo() + " esercizio : " + esercizio);
                    throw new DetailedRuntimeException("Tipo Ammortamento MULTIPLO per : tipoAmmortamento:" + tipoAmm.getTipoAmm() +
                            "cdTipoAmmortamento:" + tipoAmm.getCdTipoAmm() + " categoria gruppo:" + tipoAmm.getCdCatGruppo() + " esercizio : " + esercizio);

                }
                tipoAmmHM.put(keyHM, tipoAmm);
            }
        }
        return tipoAmmHM;
    }

    private HashMap<String,NormalizzatoreAmmortamentoDto>  getAllBeniNormalizzatiPerAmm(UserContext uc,V_InventarioBeneDetComponentSession v_inventarioBeneDetComponent,Integer esercizio, String subjectError){
        HashMap<String,NormalizzatoreAmmortamentoDto> normHM = null;
        List<NormalizzatoreAmmortamentoDto> normList = null;

        try{

            normList=v_inventarioBeneDetComponent.findNormalizzatoreBeniPerAmm(uc,esercizio);
        }catch (RemoteException | ComponentException ex){
            logger.info("Errore durante la lettura dei beni da normalizzare - Errore: " + ex.getMessage());
            throw new DetailedRuntimeException("Errore durante la lettura dei beni da normalizzare - Errore: " + ex.getMessage());

        }

        if(normList == null || normList.isEmpty()){
            logger.info("Nessun bene trovato dal normalizzatore -  esercizio : " + esercizio);
            throw new DetailedRuntimeException("Nessun bene trovato dal normalizzatore -  esercizio : " + esercizio);
        }else {
            normHM = new HashMap<String,NormalizzatoreAmmortamentoDto>();

            for(NormalizzatoreAmmortamentoDto norm : normList){
                String keyHM = norm.getNrInventario()+SEPARATORE+norm.getPgInventario()+SEPARATORE+norm.getProgressivo();

                if (normHM.get(keyHM) != null) {
                    SendMail.sendErrorMail(subjectError, "bene MULTIPLO in fase di normalizzatore  : nr_inventario:" + norm.getNrInventario() +
                            "pg_inventario:" + norm.getPgInventario() + " progressivo :" + norm.getProgressivo() );
                    throw new DetailedRuntimeException();
                }
                normHM.put(keyHM, norm);
            }
        }
        return normHM;
    }
    private Tipo_ammortamentoBulk getTipoAmmortamento(UserContext uc,Tipo_ammortamentoComponentSession tipo_ammortamentoComponent, V_ammortamento_beniBulk v_ammortamentoBene, Integer esercizio, String subjectError) {

        List<Tipo_ammortamentoBulk> listaTipoAmmortamento = null;

        try {
            listaTipoAmmortamento = tipo_ammortamentoComponent.findTipoAmmortamento(uc, v_ammortamentoBene.getTiAmmortamento(), v_ammortamentoBene.getCdCategoriaGruppo(), esercizio);

        } catch (RemoteException | ComponentException ex) {
            logger.info("Errore durante la lettura del Tipo Ammortamento : tipoAmmortamento:" + v_ammortamentoBene.getTiAmmortamento() +
                    " categoria gruppo:" + v_ammortamentoBene.getCdCategoriaGruppo() + " esercizio : " + esercizio + " - Errore: " + ex.getMessage());
            throw new DetailedRuntimeException("Errore durante la lettura del Tipo Ammortamento : tipoAmmortamento:" + v_ammortamentoBene.getTiAmmortamento() +
                    " categoria gruppo:" + v_ammortamentoBene.getCdCategoriaGruppo() + " esercizio : " + esercizio + " - Errore: " + ex.getMessage());
         }
        if (listaTipoAmmortamento == null || listaTipoAmmortamento.isEmpty()) {
            logger.info("Tipo Ammortamento NON TROVATO per: tipoAmmortamento:" + v_ammortamentoBene.getTiAmmortamento() +
                    " categoria gruppo:" + v_ammortamentoBene.getCdCategoriaGruppo() + " esercizio : " + esercizio);
            throw new DetailedRuntimeException("Tipo Ammortamento NON TROVATO per: tipoAmmortamento:" + v_ammortamentoBene.getTiAmmortamento() +
                    " categoria gruppo:" + v_ammortamentoBene.getCdCategoriaGruppo() + " esercizio : " + esercizio);
           } else if (listaTipoAmmortamento.size() > 1) {
            logger.info("Tipo Ammortamento MULTIPLO per : tipoAmmortamento:" + v_ammortamentoBene.getTiAmmortamento() +
                    " categoria gruppo:" + v_ammortamentoBene.getCdCategoriaGruppo() + " esercizio : " + esercizio);
            throw new DetailedRuntimeException("Tipo Ammortamento MULTIPLO per : tipoAmmortamento:" + v_ammortamentoBene.getTiAmmortamento() +
                    " categoria gruppo:" + v_ammortamentoBene.getCdCategoriaGruppo() + " esercizio : " + esercizio);
        } else {
            return listaTipoAmmortamento.get(0);
        }
    }

    private List<V_ammortamento_beniBulk> getAllBeniNormalizzati(UserContext uc, V_InventarioBeneDetComponentSession v_inventarioBeneDetComponent,List<V_ammortamento_beniBulk> beniDaElaborare, Integer esercizio, String subjectError) {
        List<V_ammortamento_beniBulk> beniDaElabNorm = new ArrayList<V_ammortamento_beniBulk>();

        HashMap<String, NormalizzatoreAmmortamentoDto> normHM = null;
        try{
            normHM = getAllBeniNormalizzatiPerAmm(uc,v_inventarioBeneDetComponent,esercizio, subjectError);
        }catch (Exception ex) {
            logger.info("Errore durante la lettura del normalizzatore - Errore: " + ex.getMessage());
            throw new DetailedRuntimeException("Errore durante la lettura del normalizzatore - Errore: " + ex.getMessage());
        }


        NormalizzatoreAmmortamentoDto normBene = null;

        for (V_ammortamento_beniBulk beneDaElab : beniDaElaborare) {
            String keyHM = beneDaElab.getNrInventario()+SEPARATORE+beneDaElab.getPgInventario()+SEPARATORE+beneDaElab.getProgressivo();
            normBene=normHM.get(keyHM);

            if (normBene == null) {
                logger.info("Errore in fase di 'normalizzazione' non è stato trovato il bene : esercizio:" + esercizio +
                        " pg_inventario:" + beneDaElab.getPgInventario() + " nr_inventario : " + beneDaElab.getNrInventario() + " progressivo:" + beneDaElab.getProgressivo());
                throw new DetailedRuntimeException("Errore in fase di 'normalizzazione' non è stato trovato il bene : esercizio:" + esercizio +
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

    private Ammortamento_bene_invBulk creaAmmortamentoBene(UserContext uc, AmmortamentoBeneComponentSession ammortamentoComponent,V_ammortamento_beniBulk bene,Integer esercizio,BigDecimal rataAmm, BigDecimal percAmm){
        Ammortamento_bene_invBulk ammortamento = null;

        try {
            Integer numeroAnnoAmm = ammortamentoComponent.getNumeroAnnoAmmortamento( uc, bene.getPgInventario(),bene.getNrInventario(),bene.getProgressivo());
            Integer progRigaAmm = ammortamentoComponent.getProgressivoRigaAmmortamento(uc, bene.getPgInventario(),bene.getNrInventario(),bene.getProgressivo(),esercizio);

            ammortamento = new Ammortamento_bene_invBulk();
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
      } catch (RemoteException  ex) {
            logger.info("Errore durante l'inserimento dell'ammortamento del bene -  pg_inventario: " + bene.getPgInventario() +
                    " nr_inventario: " + bene.getNrInventario() + " progressivo: " + bene.getProgressivo() + "- Errore: " + ex.getMessage());
            throw new DetailedRuntimeException("Errore durante l'inserimento dell'ammortamento del bene -  pg_inventario: " + bene.getPgInventario() +
                    " nr_inventario: " + bene.getNrInventario() + " progressivo: " + bene.getProgressivo() + "- Errore: " + ex.getMessage());

        }
        return  ammortamento;
    }

}

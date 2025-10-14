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

package it.cnr.contab.inventario01.bp;

import it.cnr.contab.docamm00.tabrif.bulk.Doc_trasporto_rientroBulk;
import it.cnr.contab.docamm00.tabrif.bulk.Doc_trasporto_rientro_dettBulk;
import it.cnr.contab.inventario01.ejb.DocTrasportoRientroComponentSession;
import it.cnr.contab.reports.bp.OfflineReportPrintBP;
import it.cnr.contab.reports.bulk.Print_spooler_paramBulk;
import it.cnr.contab.utenze00.bulk.UtenteBulk;
import it.cnr.jada.DetailedRuntimeException;
import it.cnr.jada.UserContext;
import it.cnr.jada.action.ActionContext;
import it.cnr.jada.action.BusinessProcessException;
import it.cnr.jada.bulk.OggettoBulk;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.util.action.AbstractPrintBP;
import it.cnr.jada.util.action.SimpleCRUDBP;

import java.rmi.RemoteException;
import java.util.Optional;

/**
 * Business Process per la gestione dei documenti di Trasporto e Rientro Inventario
 *
 * @author Generated
 */
public class CRUDTraspRientInventarioBP extends SimpleCRUDBP {

    // Costanti per i tipi di documento
    public static final String TRASPORTO = "TRASPORTO";
    public static final String RIENTRO = "RIENTRO";

    // Variabili di stato
    private String tipo;
    private boolean first = true;
    private boolean isAmministratore = false;
    private boolean isVisualizzazione = false;

    /**
     * Costruttore di default
     */
    public CRUDTraspRientInventarioBP() {
        super();
    }

    /**
     * Costruttore con funzione
     */
    public CRUDTraspRientInventarioBP(String function) {
        super(function);
    }

    /**
     * Inizializzazione del Business Process
     */
    protected void init(it.cnr.jada.action.Config config, it.cnr.jada.action.ActionContext context)
            throws it.cnr.jada.action.BusinessProcessException {

        Integer esercizio = it.cnr.contab.utenze00.bp.CNRUserContext.getEsercizio(context.getUserContext());

        // Determina il tipo di documento in base all'istanza specifica
        if (this instanceof CRUDTrasportoBeniInvBP)
            setTipo(TRASPORTO);
        else
            setTipo(RIENTRO);

        setSearchResultColumnSet(getTipo());
        setFreeSearchSet(getTipo());

        try {
            DocTrasportoRientroComponentSession session = (DocTrasportoRientroComponentSession)
                    createComponentSession("CNRINVENTARIO01_EJB_DocTrasportoRientroComponentSession",
                            DocTrasportoRientroComponentSession.class);
            setVisualizzazione(session.isEsercizioCOEPChiuso(context.getUserContext()));
            setAmministratore(UtenteBulk.isAmministratoreInventario(context.getUserContext()));
        } catch (ComponentException e) {
            throw handleException(e);
        } catch (RemoteException e) {
            throw handleException(e);
        }

        super.init(config, context);
        initVariabili(context, getTipo());
    }

    /**
     * Inizializza le variabili del BP
     */
    public void initVariabili(it.cnr.jada.action.ActionContext context, String tipo) {
        if (this instanceof CRUDTrasportoBeniInvBP)
            setTipo(TRASPORTO);
        else
            setTipo(RIENTRO);

        setSearchResultColumnSet(getTipo());
        setFreeSearchSet(getTipo());
    }

    /**
     * Inizializza il model per la modifica
     */
    public OggettoBulk initializeModelForEdit(ActionContext context, OggettoBulk bulk)
            throws BusinessProcessException {
        try {
            DocTrasportoRientroComponentSession session = (DocTrasportoRientroComponentSession)
                    createComponentSession("CNRINVENTARIO01_EJB_DocTrasportoRientroComponentSession",
                            DocTrasportoRientroComponentSession.class);
            setAmministratore(UtenteBulk.isAmministratoreInventario(context.getUserContext()));
        } catch (ComponentException e1) {
            throw handleException(e1);
        } catch (RemoteException e1) {
            throw handleException(e1);
        }
        bulk = super.initializeModelForEdit(context, bulk);
        return bulk;
    }

    // ========================================
    // GETTER E SETTER
    // ========================================

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    public boolean isAmministratore() {
        return isAmministratore;
    }

    public void setAmministratore(boolean isAmministratore) {
        this.isAmministratore = isAmministratore;
    }

    public boolean isVisualizzazione() {
        return isVisualizzazione;
    }

    public void setVisualizzazione(boolean isVisualizzazione) {
        this.isVisualizzazione = isVisualizzazione;
    }

    /**
     * Verifica se il documento è editabile
     * Non è editabile se siamo in modalità visualizzazione
     */
    @Override
    public boolean isEditable() {
        return !isVisualizzazione() && super.isEditable();
    }

    // ========================================
    // METODI DI BUSINESS LOGIC
    // ========================================
//
//    /**
//     * Verifica se un dettaglio è associato ad altri documenti
//     */
//    public boolean isAssociata(UserContext uc, Doc_trasporto_rientro_dettBulk dett)
//            throws BusinessProcessException, ComponentException, RemoteException {
//
//        if (dett == null || dett.getBene() == null || dett.getBene().getProgressivo() == null)
//            return false;
//        else {
//            DocTrasportoRientroComponentSession session = (DocTrasportoRientroComponentSession)
//                    createComponentSession("CNRINVENTARIO01_EJB_DocTrasportoRientroComponentSession",
//                            DocTrasportoRientroComponentSession.class);
//            //TODO da verificare e in caso implementare
//            return session.verifica_associazioni(uc, dett);
//        }
//    }
//
//    /**
//     * Verifica se la testata del documento è associata ad altri documenti
//     */
//    public boolean isAssociataTestata(UserContext uc, Doc_trasporto_rientroBulk doc)
//            throws BusinessProcessException, ComponentException, RemoteException {
//
//        if (doc == null)
//            return false;
//        else {
//            DocTrasportoRientroComponentSession session = (DocTrasportoRientroComponentSession)
//                    createComponentSession("CNRINVENTARIO01_EJB_DocTrasportoRientroComponentSession",
//                            DocTrasportoRientroComponentSession.class);
//            //TODO da verificare e in caso implementare
//            //return session.verifica_associazioni(uc, doc);
//        }
//        return false;
//    }

//    /**
//     * Verifica se il dettaglio non è l'ultimo movimento del bene
//     */
//    public boolean isNonUltimo(UserContext uc, Doc_trasporto_rientro_dettBulk dett)
//            throws BusinessProcessException, ComponentException, RemoteException {
//
//        if (dett == null || dett.getBene() == null || dett.getBene().getProgressivo() == null)
//            return false;
//        else {
//            DocTrasportoRientroComponentSession session = (DocTrasportoRientroComponentSession)
//                    createComponentSession("CNRINVENTARIO01_EJB_DocTrasportoRientroComponentSession",
//                            DocTrasportoRientroComponentSession.class);
//            //TODO da verificare e in caso implementare
//            //return session.isNonUltimo(uc, dett);
//        }
//        return false;
//    }

//    /**
//     * Verifica se il documento è stato contabilizzato (se applicabile)
//     */
//    public boolean isContabilizzato(UserContext uc, Doc_trasporto_rientroBulk doc)
//            throws BusinessProcessException, ComponentException, RemoteException {
//
//        if (doc == null)
//            return false;
//        else {
//            DocTrasportoRientroComponentSession session = (DocTrasportoRientroComponentSession)
//                    createComponentSession("CNRINVENTARIO01_EJB_DocTrasportoRientroComponentSession",
//                            DocTrasportoRientroComponentSession.class);
//            //TODO da verificare e in caso implementare
//            //return session.isContabilizzato(uc, doc);
//        }
//        return false;
//    }

    /**
     * Verifica se il bene è valido per il trasporto
     */
    public boolean isValidoPerTrasporto(UserContext uc, Doc_trasporto_rientro_dettBulk dett)
            throws BusinessProcessException, ComponentException, RemoteException {

        if (dett == null)
            return false;

        return dett.isValidoPerTrasporto();
    }

    /**
     * Verifica se il bene è valido per il rientro
     */
    public boolean isValidoPerRientro(UserContext uc, Doc_trasporto_rientro_dettBulk dett)
            throws BusinessProcessException, ComponentException, RemoteException {

        if (dett == null)
            return false;

        return dett.isValidoPerRientro();
    }

    // ========================================
    // GESTIONE STAMPA
    // ========================================

    /**
     * Determina se il bottone di stampa deve essere nascosto
     * Nascosto se il documento non ha ancora un progressivo
     */
    @Override
    public boolean isPrintButtonHidden() {
        return !Optional.ofNullable(getModel())
                .filter(Doc_trasporto_rientroBulk.class::isInstance)
                .map(Doc_trasporto_rientroBulk.class::cast)
                .flatMap(doc -> Optional.ofNullable(doc.getPgDocTrasportoRientro()))
                .isPresent();
    }


    //TODO creazione report
//    /**
//     * Inizializza il BP di stampa con i parametri del report
//     */
//    @Override
//    protected void initializePrintBP(ActionContext actioncontext, AbstractPrintBP abstractprintbp) {
//        OfflineReportPrintBP printbp = (OfflineReportPrintBP) abstractprintbp;
//        printbp.setReportName("/cnrdocamm/docamm/doc_trasporto_rientro.jasper");
//
//        final Doc_trasporto_rientroBulk doc = Optional.ofNullable(getModel())
//                .filter(Doc_trasporto_rientroBulk.class::isInstance)
//                .map(Doc_trasporto_rientroBulk.class::cast)
//                .orElseThrow(() -> new DetailedRuntimeException("Modello vuoto!"));
//
//        Print_spooler_paramBulk param;
//
//        // Parametro: esercizio
//        param = new Print_spooler_paramBulk();
//        param.setNomeParam("esercizio");
//        param.setValoreParam(
//                Optional.ofNullable(doc.getEsercizio())
//                        .map(integer -> String.valueOf(integer))
//                        .orElse(null)
//        );
//        param.setParamType(Integer.class.getCanonicalName());
//        printbp.addToPrintSpoolerParam(param);
//
//        // Parametro: pg_inventario
//        param = new Print_spooler_paramBulk();
//        param.setNomeParam("pg_inventario");
//        param.setValoreParam(
//                Optional.ofNullable(doc.getPg_inventario())
//                        .map(integer -> String.valueOf(integer))
//                        .orElse(null)
//        );
//        param.setParamType(Integer.class.getCanonicalName());
//        printbp.addToPrintSpoolerParam(param);
//
//        // Parametro: ti_documento
//        param = new Print_spooler_paramBulk();
//        param.setNomeParam("ti_documento");
//        param.setValoreParam(
//                Optional.ofNullable(doc.getTiDocumento())
//                        .orElse(null)
//        );
//        param.setParamType(String.class.getCanonicalName());
//        printbp.addToPrintSpoolerParam(param);
//
//        // Parametro: pg_doc_trasporto_rientro
//        param = new Print_spooler_paramBulk();
//        param.setNomeParam("pg_doc_trasporto_rientro");
//        param.setValoreParam(
//                Optional.ofNullable(doc.getPgDocTrasportoRientro())
//                        .map(integer -> String.valueOf(integer))
//                        .orElse(null)
//        );
//        param.setParamType(Integer.class.getCanonicalName());
//        printbp.addToPrintSpoolerParam(param);
//    }
}
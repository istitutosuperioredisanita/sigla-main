/*
 * Copyright (C) 2020  Consiglio Nazionale delle Ricerche
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

package it.cnr.contab.ordmag.ordini.comp;

import it.cnr.contab.anagraf00.core.bulk.*;
import it.cnr.contab.config00.bulk.CigBulk;
import it.cnr.contab.config00.bulk.Configurazione_cnrBulk;
import it.cnr.contab.config00.bulk.Parametri_cdsBulk;
import it.cnr.contab.config00.bulk.Parametri_cdsHome;
import it.cnr.contab.config00.contratto.bulk.ContrattoBulk;
import it.cnr.contab.config00.contratto.bulk.ContrattoHome;
import it.cnr.contab.config00.contratto.bulk.Dettaglio_contrattoBulk;
import it.cnr.contab.config00.contratto.bulk.Procedure_amministrativeBulk;
import it.cnr.contab.config00.latt.bulk.WorkpackageBulk;
import it.cnr.contab.config00.pdcep.bulk.ContoBulk;
import it.cnr.contab.config00.pdcep.bulk.ContoHome;
import it.cnr.contab.config00.pdcep.bulk.Voce_analiticaBulk;
import it.cnr.contab.config00.pdcep.bulk.Voce_analiticaHome;
import it.cnr.contab.config00.pdcfin.bulk.Elemento_voceBulk;
import it.cnr.contab.config00.sto.bulk.*;
import it.cnr.contab.docamm00.docs.bulk.Fattura_passiva_rigaBulk;
import it.cnr.contab.docamm00.docs.bulk.Fattura_passiva_rigaHome;
import it.cnr.contab.docamm00.docs.bulk.Filtro_ricerca_obbligazioniVBulk;
import it.cnr.contab.docamm00.docs.bulk.ObbligazioniTable;
import it.cnr.contab.docamm00.ejb.CategoriaGruppoInventComponentSession;
import it.cnr.contab.docamm00.tabrif.bulk.*;
import it.cnr.contab.doccont00.comp.DocumentoContabileComponentSession;
import it.cnr.contab.doccont00.core.DatiFinanziariScadenzeDTO;
import it.cnr.contab.doccont00.core.bulk.*;
import it.cnr.contab.doccont00.ejb.ObbligazioneAbstractComponentSession;
import it.cnr.contab.doccont00.ejb.ObbligazioneComponentSession;
import it.cnr.contab.doccont00.tabrif.bulk.CupBulk;
import it.cnr.contab.ordmag.anag00.*;
import it.cnr.contab.ordmag.ejb.NumeratoriOrdMagComponentSession;
import it.cnr.contab.ordmag.magazzino.comp.CalcolaImportiMagComponent;
import it.cnr.contab.ordmag.ordini.bp.ParametriSelezioneOrdiniAcqBP;
import it.cnr.contab.ordmag.ordini.bulk.*;

import it.cnr.contab.ordmag.ordini.dto.ImportoOrdine;
import it.cnr.contab.ordmag.ordini.dto.ParametriCalcoloImportoOrdine;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.contab.utenze00.bulk.UtenteBulk;
import it.cnr.contab.util.ApplicationMessageFormatException;
import it.cnr.contab.util.EuroFormat;
import it.cnr.contab.util.ICancellatoLogicamente;
import it.cnr.contab.util.Utility;
import it.cnr.jada.DetailedRuntimeException;
import it.cnr.jada.UserContext;
import it.cnr.jada.bulk.*;
import it.cnr.jada.comp.ApplicationException;
import it.cnr.jada.comp.ComponentException;
import it.cnr.jada.comp.ICRUDMgr;
import it.cnr.jada.persistency.IntrospectionException;
import it.cnr.jada.persistency.PersistencyException;
import it.cnr.jada.persistency.sql.*;
import it.cnr.jada.util.RemoteIterator;
import it.cnr.jada.util.ejb.EJBCommonServices;

import javax.ejb.EJBException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.rmi.RemoteException;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OrdineAcqComponent
        extends CalcolaImportiMagComponent
        implements ICRUDMgr, Cloneable, Serializable {

    private final static int INSERIMENTO = 1;
    private final static int MODIFICA = 2;

    public OrdineAcqComponent() {
        /*Default constructor*/
    }

    private void assegnaProgressivo(UserContext userContext, OrdineAcqBulk ordine) throws ComponentException {
        try {
            // Assegno un nuovo progressivo al documento
            NumeratoriOrdMagComponentSession progressiviSession = EJBCommonServices.createEJB("CNRORDMAG_EJB_NumeratoriOrdMagComponentSession", NumeratoriOrdMagComponentSession.class);
            NumerazioneOrdBulk numerazione = new NumerazioneOrdBulk(ordine.getCdUnitaOperativa(), ordine.getEsercizio(), ordine.getCdNumeratore());
            ordine.setNumero(progressiviSession.getNextPG(userContext, numerazione));
        } catch (Throwable t) {
            throw handleException(ordine, t);
        }
    }

    public OggettoBulk creaConBulk(UserContext userContext, OggettoBulk bulk) throws ComponentException {
        return creaConBulk(userContext, bulk, null);
    }

    ////^^@@
    ///**
    //  *  Creazione di un nuovo documento
    //  *	 Validazioni superate
    //  *    PreCondition:
    //  *      Viene richiesto il salvataggio di un nuovo documento
    //  *    PostCondition:
    //  *      Salva.
    //  *  Validazioni non superate
    //  *    PreCondition:
    //  *      Viene richiesto il salvataggio di un nuovo documento ma le validazioni
    //  *      non vengono superate
    //  *    PostCondition:
    //  *      Informa l'utente della causa per la quale non è possibile salvare
    // */
    ////^^@@
    public it.cnr.jada.bulk.OggettoBulk creaConBulk(it.cnr.jada.UserContext userContext, it.cnr.jada.bulk.OggettoBulk bulk, it.cnr.contab.doccont00.core.bulk.OptionRequestParameter status)
            throws it.cnr.jada.comp.ComponentException {
        OrdineAcqBulk ordine = (OrdineAcqBulk) bulk;
        //assegna un progressivo al documento all'atto della creazione.
        validaOrdine(userContext, ordine);
        calcolaImportoOrdine(userContext, ordine);

        manageDocumentiContabiliCancellati(userContext, ordine, status);

        verificaCoperturaContratto(userContext, ordine, INSERIMENTO);
        assegnaProgressivo(userContext, ordine);
        aggiornaObbligazioni(userContext, ordine, status);
        ordine = (OrdineAcqBulk) super.creaConBulk(userContext, ordine);
        return ordine;
    }

    public OrdineAcqBulk calcolaImportoOrdine(it.cnr.jada.UserContext userContext, OrdineAcqBulk ordine) throws it.cnr.jada.comp.ComponentException {
        //Se definitivo non posso cambiare nulla...
        if (ordine.isOrdineDefinitivo())
            return ordine;

        if (ordine.getCambio() == null || ordine.getDivisa() == null || ordine.getDivisa().getCd_divisa() == null)
            throw new it.cnr.jada.comp.ApplicationException("Campi di testata ordine necessari per il calcolo dell'importo non valorizzati.");

        ParametriCalcoloImportoOrdine parametriTestata = new ParametriCalcoloImportoOrdine();
        parametriTestata.setCambio(ordine.getCambio());
        parametriTestata.setDivisa(ordine.getDivisa());
        parametriTestata.setDivisaRisultato(getEuro(userContext));
        parametriTestata.setPercProrata(ordine.getPercProrata());
        ordine.setImImponibile(BigDecimal.ZERO);
        ordine.setImIva(BigDecimal.ZERO);
        ordine.setImIvaD(BigDecimal.ZERO);
        ordine.setImTotaleOrdine(BigDecimal.ZERO);

        for (OrdineAcqRigaBulk riga : ordine.getRigheOrdineColl()) {
            if (riga == null)
                throw new ApplicationException("Dettaglio vuoto.");
            if (riga.getCoefConv() == null || riga.getPrezzoUnitario() == null || riga.getVoceIva() == null || riga.getVoceIva().getCd_voce_iva() == null)
                throw new ApplicationException("Campi di dettaglio ordine necessari per il calcolo dell'importo non valorizzati.");
            ParametriCalcoloImportoOrdine parametriRiga = (ParametriCalcoloImportoOrdine) parametriTestata.clone();
            parametriRiga.setCoefacq(riga.getCoefConv());
            parametriRiga.setPrezzo(riga.getPrezzoUnitario());
            parametriRiga.setSconto1(riga.getSconto1());
            parametriRiga.setSconto2(riga.getSconto2());
            parametriRiga.setSconto3(riga.getSconto3());
            parametriRiga.setVoceIva(riga.getVoceIva());
            riga.setImImponibile(BigDecimal.ZERO);
            riga.setImImponibileDivisa(BigDecimal.ZERO);
            riga.setImIva(BigDecimal.ZERO);
            riga.setImIvaDivisa(BigDecimal.ZERO);
            riga.setImIvaD(BigDecimal.ZERO);
            riga.setImIvaNd(BigDecimal.ZERO);
            riga.setImTotaleRiga(BigDecimal.ZERO);

            if (riga.getRigheConsegnaColl() == null || riga.getRigheConsegnaColl().isEmpty()) {
                if (riga.getDspQuantita() != null)
                    gestioneSalvataggioRigaConsegnaSingola(riga);
                else
                    throw new ApplicationException("Campi di dettaglio ordine necessari per il calcolo dell'importo non valorizzati.");
            }

            for (OrdineAcqConsegnaBulk cons : riga.getRigheConsegnaColl()) {
                if (!cons.isConsegna0()) {
                    ParametriCalcoloImportoOrdine parametriCons = (ParametriCalcoloImportoOrdine) parametriRiga.clone();

                    if (cons.getQuantita() == null)
                        throw new ApplicationException("Campi di consegna ordine necessari per il calcolo dell'importo non valorizzati.");

                    parametriCons.setQtaOrd(cons.getQuantita());
                    parametriCons.setArrAliIva(cons.getArrAliIva());

                    if (cons.getFatturaOrdineBulk() != null) {
                        impostoVoceIvaRett(userContext, cons.getFatturaOrdineBulk(), parametriCons);
                        parametriCons.setPrezzoRet(cons.getFatturaOrdineBulk().getPrezzoUnitarioRett());
                        parametriCons.setSconto1Ret(cons.getFatturaOrdineBulk().getSconto1Rett());
                        parametriCons.setSconto2Ret(cons.getFatturaOrdineBulk().getSconto2Rett());
                        parametriCons.setSconto3Ret(cons.getFatturaOrdineBulk().getSconto3Rett());
                    }

                    ImportoOrdine importo = calcoloImporto(parametriCons);
                    cons.setImImponibile(importo.getImponibile());
                    cons.setImImponibileDivisa(importo.getImponibile());
                    cons.setImIva(importo.getImportoIva());
                    cons.setImIvaDivisa(importo.getImportoIva());
                    cons.setImIvaD(importo.getImportoIvaDetraibile());
                    cons.setImIvaNd(importo.getImportoIvaInd());
                    cons.setImTotaleConsegna(importo.getTotale());
                    cons.setToBeUpdated();
                    riga.setImImponibile(riga.getImImponibile().add(cons.getImImponibile()));
                    riga.setImImponibileDivisa(riga.getImImponibileDivisa().add(cons.getImImponibileDivisa()));
                    riga.setImIva(riga.getImIva().add(cons.getImIva()));
                    riga.setImIvaDivisa(riga.getImIvaDivisa().add(cons.getImIvaDivisa()));
                    riga.setImIvaD(riga.getImIvaD().add(cons.getImIvaD()));
                    riga.setImIvaNd(riga.getImIvaNd().add(cons.getImIvaNd()));
                    riga.setImTotaleRiga(riga.getImTotaleRiga().add(cons.getImTotaleConsegna()));
                    riga.setToBeUpdated();
                } else {
                    cons.setImImponibile(BigDecimal.ZERO);
                    cons.setImImponibileDivisa(BigDecimal.ZERO);
                    cons.setImIva(BigDecimal.ZERO);
                    cons.setImIvaDivisa(BigDecimal.ZERO);
                    cons.setImIvaD(BigDecimal.ZERO);
                    cons.setImIvaNd(BigDecimal.ZERO);
                    cons.setImTotaleConsegna(BigDecimal.ZERO);
                    cons.setToBeUpdated();
                }
                ordine.sostituisciConsegnaFromObbligazioniHash(cons);
            }
        }
        impostaTotaliOrdine(ordine);
        return ordine;
    }

    public FatturaOrdineBulk calcolaImportoOrdine(it.cnr.jada.UserContext userContext, FatturaOrdineBulk fatturaOrdine) throws it.cnr.jada.comp.ComponentException {
        return calcolaImportoOrdine(userContext, fatturaOrdine, false);
    }

    public FatturaOrdineBulk calcolaImportoOrdine(it.cnr.jada.UserContext userContext, FatturaOrdineBulk fatturaOrdine, Boolean magazzino) throws it.cnr.jada.comp.ComponentException {
        OrdineAcqBulk ordine = new OrdineAcqBulk(fatturaOrdine.getCdCdsOrdine(), fatturaOrdine.getCdUnitaOperativa(), fatturaOrdine.getEsercizioOrdine(), fatturaOrdine.getCdNumeratore(), fatturaOrdine.getNumero());
        OrdineAcqHome ordineAcqHome = (OrdineAcqHome) getHome(userContext, OrdineAcqBulk.class);
        try {
            ordine = (OrdineAcqBulk) ordineAcqHome.findByPrimaryKey(ordine);
        } catch (PersistencyException e) {
            throw new ComponentException(e);
        }
        OrdineAcqRigaBulk riga = new OrdineAcqRigaBulk(fatturaOrdine.getCdCdsOrdine(), fatturaOrdine.getCdUnitaOperativa(), fatturaOrdine.getEsercizioOrdine(), fatturaOrdine.getCdNumeratore(), fatturaOrdine.getNumero(), fatturaOrdine.getRiga());
        OrdineAcqRigaHome ordineAcqRigaHome = (OrdineAcqRigaHome) getHome(userContext, OrdineAcqRigaBulk.class);
        try {
            riga = (OrdineAcqRigaBulk) ordineAcqRigaHome.findByPrimaryKey(riga);
        } catch (PersistencyException e) {
            throw new ComponentException(e);
        }
        ParametriCalcoloImportoOrdine parametri = new ParametriCalcoloImportoOrdine();
        parametri.setCambio(ordine.getCambio());
        parametri.setDivisa(ordine.getDivisa());
        parametri.setDivisaRisultato(getEuro(userContext));
        parametri.setPercProrata(ordine.getPercProrata());

        parametri.setCoefacq(riga.getCoefConv());
        parametri.setPrezzo(riga.getPrezzoUnitario());
        parametri.setPrezzoRet(fatturaOrdine.getPrezzoUnitarioRett());
        parametri.setSconto1(riga.getSconto1());
        parametri.setSconto1Ret(fatturaOrdine.getSconto1Rett());
        parametri.setSconto2(riga.getSconto2());
        parametri.setSconto2Ret(fatturaOrdine.getSconto2Rett());
        parametri.setSconto3(riga.getSconto3());
        parametri.setSconto3Ret(fatturaOrdine.getSconto3Rett());
        parametri.setImponibileErratoPerNotaCredito(fatturaOrdine.getImponibileErrato());
        Voce_ivaHome voce_ivaHome = (Voce_ivaHome) getHome(userContext, Voce_ivaBulk.class);
        Voce_ivaBulk voce_iva;
        try {
            voce_iva = (Voce_ivaBulk) voce_ivaHome.findByPrimaryKey(riga.getVoceIva());
        } catch (PersistencyException e) {
            throw new ComponentException(e);
        }
        parametri.setVoceIva(voce_iva);

        impostoVoceIvaRett(userContext, fatturaOrdine, parametri);

        OrdineAcqConsegnaBulk cons = new OrdineAcqConsegnaBulk(fatturaOrdine.getCdCdsOrdine(), fatturaOrdine.getCdUnitaOperativa(), fatturaOrdine.getEsercizioOrdine(), fatturaOrdine.getCdNumeratore(), fatturaOrdine.getNumero(), fatturaOrdine.getRiga(), fatturaOrdine.getConsegna());
        OrdineAcqConsegnaHome ordineAcqConsegnaHome = (OrdineAcqConsegnaHome) getHome(userContext, OrdineAcqConsegnaBulk.class);
        try {
            cons = (OrdineAcqConsegnaBulk) ordineAcqConsegnaHome.findByPrimaryKey(cons);
        } catch (PersistencyException e) {
            throw new ComponentException(e);
        }
        parametri.setQtaOrd(cons.getQuantita());
        parametri.setArrAliIva(cons.getArrAliIva());

        ImportoOrdine importo = magazzino ? calcoloImportoPerMagazzino(parametri) : calcoloImporto(parametri);
        fatturaOrdine.setImImponibile(importo.getImponibile());
        fatturaOrdine.setImImponibileDivisa(importo.getImponibile());
        fatturaOrdine.setImIva(importo.getImportoIva());
        fatturaOrdine.setImIvaDivisa(importo.getImportoIva());
        fatturaOrdine.setImIvaD(importo.getImportoIvaDetraibile());
        fatturaOrdine.setImTotaleConsegna(importo.getTotale());
        fatturaOrdine.setImIvaNd(importo.getImportoIvaInd());

        fatturaOrdine.setImponibilePerNotaCredito(Utility.nvl(importo.getImponibilePerNotaCredito(), fatturaOrdine.getImImponibile()));
        fatturaOrdine.setImportoIvaPerNotaCredito(Utility.nvl(importo.getImportoIvaPerNotaCredito(), fatturaOrdine.getImIva()));
        fatturaOrdine.setImportoIvaIndPerNotaCredito(Utility.nvl(importo.getImportoIvaIndPerNotaCredito(), fatturaOrdine.getImIvaNd()));
        fatturaOrdine.setImportoIvaDetraibilePerNotaCredito(Utility.nvl(importo.getImportoIvaDetraibilePerNotaCredito(), fatturaOrdine.getImIvaD()));
        fatturaOrdine.setTotaleConsegnaPerNotaCredito(Utility.nvl(importo.getTotalePerNotaCredito(), fatturaOrdine.getImTotaleConsegna()));

        return fatturaOrdine;
    }

    private void impostoVoceIvaRett(UserContext userContext, FatturaOrdineBulk fatturaOrdine, ParametriCalcoloImportoOrdine parametri) throws ComponentException {
        Voce_ivaHome voce_ivaHome = (Voce_ivaHome) getHome(userContext, Voce_ivaBulk.class);
        if (fatturaOrdine.getCdVoceIvaRett() != null) {
            Voce_ivaBulk voce_ivaBulk = new Voce_ivaBulk(fatturaOrdine.getCdVoceIvaRett());
            try {
                voce_ivaBulk = (Voce_ivaBulk) voce_ivaHome.findByPrimaryKey(voce_ivaBulk);
            } catch (PersistencyException e) {
                throw new ComponentException(e);
            }
            parametri.setVoceIvaRet(voce_ivaBulk);
        }
    }

    public void impostaTotaliOrdine(OrdineAcqBulk ordine) {
        BigDecimal imponibile = BigDecimal.ZERO, iva = BigDecimal.ZERO, ivaD = BigDecimal.ZERO, totale = BigDecimal.ZERO;
        for (OrdineAcqRigaBulk riga : ordine.getRigheOrdineColl()) {
            if (riga != null) {
                for (OrdineAcqConsegnaBulk cons : riga.getRigheConsegnaColl()) {
                    imponibile = imponibile.add(cons.getImImponibile());
                    iva = iva.add(cons.getImIva());
                    ivaD = ivaD.add(cons.getImIvaD());
                    totale = totale.add(cons.getImTotaleConsegna());
                }
            }
        }
        ordine.setImImponibile(imponibile);
        ordine.setImIva(iva);
        ordine.setImIvaD(ivaD);
        ordine.setImTotaleOrdine(totale);
        ordine.setToBeUpdated();
    }

    private void validaOrdine(it.cnr.jada.UserContext userContext, OrdineAcqBulk ordine) throws it.cnr.jada.comp.ComponentException {
        controlloEsistenzaRigheOrdine(ordine);
        for (OrdineAcqRigaBulk riga : ordine.getRigheOrdineColl()) {
            if (riga != null) {
                gestioneSalvataggioRigaConsegnaSingola(riga);
                for (OrdineAcqConsegnaBulk cons : riga.getRigheConsegnaColl()) {
                    if ((cons.getObbligazioneScadenzario() == null || cons.getObbligazioneScadenzario().getPg_obbligazione() == null) &&
                            cons.getStato().equals(OrdineAcqConsegnaBulk.STATO_INSERITA)) {
                        cons.setObbligazioneScadenzario(riga.getDspObbligazioneScadenzario());
                    }
                    controlliValiditaConsegna(userContext, cons);
                }
            }
        }
        controllaQuadraturaObbligazioni(userContext, ordine);
        controlloCongruenzaObbligazioni(userContext, ordine);
        controlloCongruenzaFornitoreContratto(userContext, ordine);
    }

    private void controlloEsistenzaRigheOrdine(OrdineAcqBulk ordine) throws ApplicationException {
        if (ordine.getRigheOrdineColl() == null || ordine.getRigheOrdineColl().isEmpty()) {
            throw new ApplicationException("Non è possibile salvare un ordine senza dettagli.");
        } else {
            boolean esisteRigaValida = false;
            for (OrdineAcqRigaBulk riga : ordine.getRigheOrdineColl()) {
                if (!riga.isToBeDeleted())
                    esisteRigaValida = true;
            }
            if (!esisteRigaValida)
                throw new ApplicationException("Non è possibile salvare un ordine senza dettagli.");
        }
    }

    private void controlliValiditaConsegna(UserContext userContext, OrdineAcqConsegnaBulk consegna) throws it.cnr.jada.comp.ComponentException {
        if (consegna.getMagazzino() == null || consegna.getMagazzino().getCdMagazzino() == null)
            throw new ApplicationException("E' necessario indicare il magazzino sulla riga di consegna " + consegna.getConsegna() + " della riga d'ordine " + consegna.getRiga() + ".");

        if (consegna.getLuogoConsegnaMag() == null || consegna.getLuogoConsegnaMag().getCdLuogoConsegna() == null)
            throw new ApplicationException("E' necessario indicare il luogo di consegna sulla riga di consegna " + consegna.getConsegna() + " della riga d'ordine " + consegna.getRiga() + ".");

        if (!consegna.isConsegnaMagazzino()) {
            if (consegna.getCdUopDest() == null)
                throw new ApplicationException("E' necessario indicare l'unità operativa di destinazione per la riga di consegna " + consegna.getConsegna() + " della riga d'ordine " + consegna.getRiga() + ".");
        } else {
            if (consegna.getCdUopDest() != null)
                throw new ApplicationException("Per una consegna a magazzino non è possibile selezionare l'unità operativa di destinazione per la riga di consegna " + consegna.getConsegna() + " della riga d'ordine " + consegna.getRiga() + ".");
        }
        if(consegna.getQuantita()!=null && !Utility.isInteger(consegna.getQuantita()) && consegna.getOrdineAcqRiga().getBeneServizio().getFl_gestione_inventario()){
            throw new ApplicationException("Per una consegna a magazzino non è possibile impostare una quantità con decimali. Consegna " + consegna.getConsegna() + " della riga d'ordine " + consegna.getRiga() + ".");
        }
        if (consegna.getOrdineAcqRiga().getOrdineAcq().getDataOrdine() == null) {
            OrdineAcqHome home = (OrdineAcqHome) getHome(userContext, OrdineAcqBulk.class);
            try {
                OrdineAcqBulk ordine = (OrdineAcqBulk) home.findByPrimaryKey(consegna.getOrdineAcqRiga().getOrdineAcq());
                consegna.getOrdineAcqRiga().setOrdineAcq(ordine);
            } catch (PersistencyException e) {
                throw new ApplicationException(e);
            }

        }

        if (consegna.getDtPrevConsegna() != null && consegna.getDtPrevConsegna().before(consegna.getOrdineAcqRiga().getOrdineAcq().getDataOrdine()))
            throw new ApplicationException("La data di prevista consegna non può essere precedente alla data dell'ordine per la riga di consegna " + consegna.getConsegna() + " della riga d'ordine " + consegna.getRiga() + ".");

        try {
            if (Utility.createConfigurazioneCnrComponentSession().isAttivaEconomica(userContext, consegna.getEsercizio()) && (consegna.getContoBulk() == null || consegna.getContoBulk().getCd_voce_ep() == null))
                throw new ApplicationException("E' necessario indicare il conto di Economico Patrimoniale sulla riga di consegna " + consegna.getConsegna() + " della riga d'ordine " + consegna.getRiga() + ".");
        } catch (RemoteException e) {
            throw new ComponentException(e);
        }
    }

    private void controlloCongruenzaFornitoreContratto(it.cnr.jada.UserContext userContext, OrdineAcqBulk ordine)
            throws ComponentException {
        if (ordine.getPgContratto() != null) {
            ContrattoBulk contratto;
            try {
                contratto = retrieveContratto(userContext, ordine);
                if (contratto != null) {
                    if (!ordine.getFornitore().equalsByPrimaryKey(contratto.getFigura_giuridica_esterna())) {
                        throw new ApplicationException("Fornitore del contratto " + contratto.getFig_giu_esterna_codice() + " diverso dal fornitore indicato sull'ordine.");
                    }
                }
            } catch (PersistencyException e) {
                throw new ComponentException(e);
            }
        }
    }

    private void controlloCongruenzaObbligazioni(it.cnr.jada.UserContext userContext, OrdineAcqBulk ordine)
            throws ComponentException {
        for (OrdineAcqRigaBulk riga : ordine.getRigheOrdineColl()) {
            if (riga != null) {
                for (OrdineAcqConsegnaBulk cons : riga.getRigheConsegnaColl()) {
                    if (cons.getPgObbligazioneScad() != null) {
                        try {
                            ObbligazioneBulk obb = retrieveObbligazione(userContext, cons);
                            if (obb != null) {
                                if (obb.getPg_contratto() != null) {
                                    if (!obb.getContratto().equalsByPrimaryKey(ordine.getContratto())) {
                                        throw new ApplicationException("Contratto dell'impegno " + obb.getEsercizio_originale() + "/" + obb.getPg_obbligazione() + " diverso dal contratto indicato sull'ordine.");
                                    }
                                }
                                controlloCongruenzaUoImpegno(userContext, cons, obb);
                            }
                        } catch (PersistencyException e) {
                            throw new ComponentException(e);
                        }
                    }
                }
            }
        }
    }

    private void controlloCongruenzaUoImpegno(it.cnr.jada.UserContext userContext,
                                              OrdineAcqConsegnaBulk cons, ObbligazioneBulk obb)
            throws ComponentException, PersistencyException {
        if (obb.getCd_unita_organizzativa() != null) {
            Unita_organizzativaBulk uoOrdine = recuperoUoPerImpegno(userContext, cons);
            if (uoOrdine != null && !uoOrdine.getCd_unita_organizzativa().equals(obb.getCd_unita_organizzativa()))
                throw new ApplicationException("Per la consegna " + cons.getConsegna() + " della riga " + cons.getRiga() + " la uo dell'obbligazione non è corretta.");
        }
    }

    private void gestioneSalvataggioRigaConsegnaSingola(OrdineAcqRigaBulk riga) throws ApplicationException {
        if (riga.isToBeCreated() && (riga.getRigheConsegnaColl() == null || riga.getRigheConsegnaColl().isEmpty())) {
            if (riga.getDspQuantita() == null)
                throw new ApplicationException("E' necessario indicare la quantità sulla riga "+riga.getRigaOrdineString()+".");
            if (riga.getDspDtPrevConsegna() == null)
                throw new ApplicationException("E' necessario indicare la data di prevista consegna sulla riga "+riga.getRigaOrdineString()+".");
            if (riga.getDspTipoConsegna() == null)
                throw new ApplicationException("E' necessario indicare il tipo di consegna sulla riga "+riga.getRigaOrdineString()+".");
            if (riga.getPrezzoUnitario() == null)
                throw new ApplicationException("E' necessario indicare il prezzo unitario sulla riga "+riga.getRigaOrdineString()+".");
            else {
                if ((!riga.getDspTipoConsegna().equals(Bene_servizioBulk.TIPO_CONSEGNA_MAGAZZINO)) && (riga.getDspUopDest() == null || riga.getDspUopDest().getCdUnitaOperativa() == null))
                    throw new ApplicationException("E' necessario indicare l'unità operativa per i tipi consegna in 'Transito' o 'Fuori Magazzino' sulla riga "+riga.getRigaOrdineString()+".");
            }
            if (riga.getDspMagazzino() == null || riga.getDspMagazzino().getCdMagazzino() == null)
                throw new ApplicationException("E' necessario indicare il magazzino sulla riga "+riga.getRigaOrdineString()+".");
            if (riga.getDspLuogoConsegna() == null || riga.getDspLuogoConsegna().getCdLuogoConsegna() == null)
                throw new ApplicationException("E' necessario indicare il luogo di consegna sulla riga "+riga.getRigaOrdineString()+".");
            OrdineAcqConsegnaBulk consegna;
            if (riga.isToBeCreated()) {
                consegna = new OrdineAcqConsegnaBulk();
                consegna.setOrdineAcqRiga(riga);
                consegna.setStato(OrdineAcqConsegnaBulk.STATO_INSERITA);
                consegna.setRiga(riga.getRiga());
                consegna.setConsegna(1);
                consegna.setToBeCreated();
            } else {
                consegna = riga.getRigheConsegnaColl().get(0);
                riga.getRigheConsegnaColl().remove(consegna);
                consegna.setToBeUpdated();
            }
            consegna.setStato(OrdineAcqConsegnaBulk.STATO_INSERITA);
            consegna.setStatoFatt(OrdineAcqConsegnaBulk.STATO_FATT_NON_ASSOCIATA);
            consegna.setImImponibile(riga.getImImponibile());
            consegna.setImImponibileDivisa(riga.getImImponibileDivisa());
            consegna.setImIva(riga.getImIva());
            consegna.setImIvaDivisa(riga.getImIvaDivisa());
            consegna.setImTotaleConsegna(riga.getImTotaleRiga());
            consegna.setImIvaD(riga.getImIvaD());
            consegna.setImIvaNd(riga.getImIvaNd());

            consegna.setLuogoConsegnaMag(riga.getDspLuogoConsegna());
            consegna.setMagazzino(riga.getDspMagazzino());
            consegna.setDtPrevConsegna(riga.getDspDtPrevConsegna());
            consegna.setQuantita(riga.getDspQuantita());
            consegna.setTipoConsegna(riga.getDspTipoConsegna());
            consegna.setUnitaOperativaOrd(riga.getDspUopDest());
            riga.getRigheConsegnaColl().add(consegna);
        }
    }

    public it.cnr.jada.bulk.OggettoBulk stampaConBulk(it.cnr.jada.UserContext aUC, it.cnr.jada.bulk.OggettoBulk bulk) throws it.cnr.jada.comp.ComponentException {
        return bulk;
    }

    @Override
    public OggettoBulk inizializzaBulkPerInserimento(UserContext usercontext, OggettoBulk oggettobulk)
            throws ComponentException {
        OggettoBulk oggetto = super.inizializzaBulkPerInserimento(usercontext, oggettobulk);
        return inizializzaOrdine(usercontext, oggetto, true);
    }

    @Override
    public OggettoBulk inizializzaBulkPerModifica(UserContext usercontext, OggettoBulk oggettobulk)
            throws ComponentException {
        OrdineAcqBulk ordine = (OrdineAcqBulk) super.inizializzaBulkPerModifica(usercontext, oggettobulk);
        ordine.setUnicoMagazzinoAbilitato(unicoMagazzinoAbilitato(usercontext, ordine));

        try {
            OrdineAcqHome homeOrdine = (OrdineAcqHome)getHome(usercontext, OrdineAcqBulk.class);
            OrdineAcqRigaHome homeRigaOrdine = (OrdineAcqRigaHome)getHome(usercontext, OrdineAcqRigaBulk.class);
            OrdineAcqConsegnaHome homeConsegnaOrdine = (OrdineAcqConsegnaHome) getHome(usercontext, OrdineAcqConsegnaBulk.class);
            FatturaOrdineHome homeFatturaOrdine = (FatturaOrdineHome) getHome(usercontext, FatturaOrdineBulk.class);
            Fattura_passiva_rigaHome homeFatturaRiga = (Fattura_passiva_rigaHome) getHome(usercontext, Fattura_passiva_rigaBulk.class);

            ordine.setRigheOrdineColl(new BulkList<>(homeOrdine.findOrdineRigheList(ordine)));
            ordine.setRigheEconomica(new BulkList<>(homeOrdine.findOrdineAcqEcoList(ordine)));

            Boolean isOrdineCompletamenteContabilizzato=true;

            for (OrdineAcqRigaBulk riga : ordine.getRigheOrdineColl()) {
                riga.setRigheConsegnaColl(new BulkList(homeRigaOrdine.findOrdineRigheConsegnaList(riga)));
                riga.setRigheEconomica(new BulkList<>(homeRigaOrdine.findOrdineAcqRigaEcoList(riga)));

                getHomeCache(usercontext).fetchAll(usercontext);

                Obbligazione_scadenzarioBulk scadenzaComune = null;
                boolean esisteScadenzaComune = false;

                for (OrdineAcqConsegnaBulk cons : riga.getRigheConsegnaColl()) {
                    cons.setRigheEconomica(new BulkList<>(homeConsegnaOrdine.findOrdineAcqConsegnaEcoList(cons)));
                    cons.setFatturaOrdineBulk(homeFatturaOrdine.findFatturaByRigaConsegna(cons));

                    if (cons.getFatturaOrdineBulk()!=null)
                        cons.getFatturaOrdineBulk().getFatturaPassivaRiga()
                                .setRigheEconomica(new BulkList<>(homeFatturaRiga.findFatturaPassivaRigheEcoList(cons.getFatturaOrdineBulk().getFatturaPassivaRiga())));

                    if (cons.getObbligazioneScadenzario() != null) {
                        Obbligazione_scadenzarioBulk scad = cons.getObbligazioneScadenzario();
                        if (scadenzaComune == null || scadenzaComune.equalsByPrimaryKey(scad)) {
                            esisteScadenzaComune = true;
                            scadenzaComune = scad;
                        } else {
                            esisteScadenzaComune = false;
                        }
                    } else {
                        esisteScadenzaComune = false;
                        isOrdineCompletamenteContabilizzato = false;
                    }
                    if (cons.getUnitaOperativaOrd() != null) {
                        UnitaOperativaOrdBulk uop = recuperoUopDest(usercontext, cons);
                        cons.setUnitaOperativaOrd(uop);
                    }
                }
                impostaCampiDspRiga(usercontext, riga);
                if (esisteScadenzaComune)
                    riga.setDspObbligazioneScadenzario(scadenzaComune);
            }
            ordine.setOrdineContabilizzato(isOrdineCompletamenteContabilizzato);

        } catch (PersistencyException e) {
            throw handleException(e);
        }

        //    impostaTotaliOrdine(ordine);
        rebuildObbligazioni(usercontext, ordine);

        return inizializzaOrdine(usercontext, ordine, false);
    }

    protected void impostaCampiDspRiga(UserContext userContext, OrdineAcqRigaBulk riga) throws ComponentException {
        try {
            boolean isAttivaFinanziaria = Utility.createConfigurazioneCnrComponentSession().isAttivaFinanziaria(userContext, riga.getEsercizio());
            riga.setDspStatoContabilizzazione(OrdineAcqRigaBulk.StatoContabilizzazione.NON_CONTABILIZZATA);

            if (!isAttivaFinanziaria && riga.getImCostoEcoDaRipartireConsegne().compareTo(BigDecimal.ZERO)==0)
                riga.setDspStatoContabilizzazione(OrdineAcqRigaBulk.StatoContabilizzazione.CONTABILIZZATA);

            if (riga.getRigheConsegnaColl().size() == 1) {
                OrdineAcqConsegnaBulk cons = riga.getRigheConsegnaColl().iterator().next();
                riga.setDspDtPrevConsegna(cons.getDtPrevConsegna());
                riga.setDspLuogoConsegna(cons.getLuogoConsegnaMag());
                riga.setDspMagazzino(cons.getMagazzino());
                riga.setDspQuantita(cons.getQuantita());
                riga.setDspTipoConsegna(cons.getTipoConsegna());
                riga.setDspUopDest(cons.getUnitaOperativaOrd());
                riga.setDspConto(cons.getContoBulk());
                riga.setDspStato(cons.getStato());
                if (isAttivaFinanziaria && Optional.ofNullable(cons.getObbligazioneScadenzario()).isPresent())
                    riga.setDspStatoContabilizzazione(OrdineAcqRigaBulk.StatoContabilizzazione.CONTABILIZZATA);
            } else if (riga.getRigheConsegnaColl().size() > 1) {
                riga.setDspQuantita(riga.getQuantitaConsegneColl());
                String stato = null;
                boolean primoGiro = true;
                for (OrdineAcqConsegnaBulk cons : riga.getRigheConsegnaColl()) {
                    if (!primoGiro && !cons.getStato().equals(stato)) {
                        stato = null;
                    } else {
                        stato = cons.getStato();
                        primoGiro = false;
                    }
                }
                riga.setDspStato(stato);
                if (isAttivaFinanziaria) {
                    final boolean contabilizzate = riga.getRigheConsegnaColl()
                            .stream()
                            .anyMatch(ordineAcqConsegnaBulk -> Optional.ofNullable(ordineAcqConsegnaBulk.getObbligazioneScadenzario()).isPresent());
                    final boolean notContabilizzate = riga.getRigheConsegnaColl()
                            .stream()
                            .anyMatch(ordineAcqConsegnaBulk -> !Optional.ofNullable(ordineAcqConsegnaBulk.getObbligazioneScadenzario()).isPresent());
                    if (notContabilizzate && contabilizzate)
                        riga.setDspStatoContabilizzazione(OrdineAcqRigaBulk.StatoContabilizzazione.PARZIALMENTE_CONTABILIZZATA);
                    if (!notContabilizzate && contabilizzate)
                        riga.setDspStatoContabilizzazione(OrdineAcqRigaBulk.StatoContabilizzazione.CONTABILIZZATA);
                }
            }
        } catch (RemoteException e) {
            throw new ComponentException(e);
        }
    }

    private MagazzinoBulk recuperoMagazzino(UserContext usercontext, OrdineAcqConsegnaBulk cons) throws ComponentException, PersistencyException {
        MagazzinoHome home = (MagazzinoHome) getHome(usercontext, MagazzinoBulk.class);
        return (MagazzinoBulk) home.findByPrimaryKey(new MagazzinoBulk(cons.getCdCdsMag(), cons.getCdMagazzino()));
    }

    private UnitaOperativaOrdBulk recuperoUopDest(UserContext usercontext, OrdineAcqConsegnaBulk cons)
            throws ComponentException, PersistencyException {
        UnitaOperativaOrdHome home = (UnitaOperativaOrdHome) getHome(usercontext, UnitaOperativaOrdBulk.class);
        return (UnitaOperativaOrdBulk) home.findByPrimaryKey(new UnitaOperativaOrdBulk(cons.getCdUopDest()));
    }

    private UnitaOperativaOrdBulk recuperoUop(UserContext usercontext, UnitaOperativaOrdBulk uop)
            throws ComponentException, PersistencyException {
        UnitaOperativaOrdHome home = (UnitaOperativaOrdHome) getHome(usercontext, UnitaOperativaOrdBulk.class);
        return (UnitaOperativaOrdBulk) home.findByPrimaryKey(new UnitaOperativaOrdBulk(uop.getCdUnitaOperativa()));
    }

    private Obbligazione_scadenzarioBulk retrieveObbligazioneScadenzario(UserContext usercontext,
                                                                         OrdineAcqConsegnaBulk cons) throws ComponentException, PersistencyException {
        Obbligazione_scadenzarioHome home = (Obbligazione_scadenzarioHome) getHome(usercontext, Obbligazione_scadenzarioBulk.class);
        return (Obbligazione_scadenzarioBulk) home.findByPrimaryKey(new Obbligazione_scadenzarioBulk(cons.getCdCdsObbl(), cons.getEsercizioObbl(), cons.getEsercizioOrigObbl(), cons.getPgObbligazione(), cons.getPgObbligazioneScad()));
    }

    private ContrattoBulk retrieveContratto(UserContext usercontext,
                                            OrdineAcqBulk ordine) throws ComponentException, PersistencyException {
        ContrattoHome home = (ContrattoHome) getHome(usercontext, ContrattoBulk.class);
        return (ContrattoBulk) home.findByPrimaryKey(new ContrattoBulk(ordine.getEsercizioContratto(), ordine.getStato(), ordine.getPgContratto()));
    }

    private ObbligazioneBulk retrieveObbligazione(UserContext usercontext,
                                                  OrdineAcqConsegnaBulk cons) throws ComponentException, PersistencyException {
        Obbligazione_scadenzarioBulk scad = retrieveObbligazioneScadenzario(usercontext, cons);
        ObbligazioneHome home = (ObbligazioneHome) getHome(usercontext, ObbligazioneBulk.class);
        return (ObbligazioneBulk) home.findByPrimaryKey(new ObbligazioneBulk(scad.getCd_cds(), scad.getEsercizio(), scad.getEsercizio_originale(), scad.getPg_obbligazione()));
    }

    @Override
    public OggettoBulk inizializzaBulkPerRicerca(UserContext usercontext, OggettoBulk oggettobulk)
            throws ComponentException {
        oggettobulk = super.inizializzaBulkPerRicerca(usercontext, oggettobulk);
        return inizializzaOrdine(usercontext, oggettobulk, false);
    }

    @Override
    public OggettoBulk inizializzaBulkPerRicercaLibera(UserContext usercontext, OggettoBulk oggettobulk)
            throws ComponentException {
        OggettoBulk oggetto = super.inizializzaBulkPerRicercaLibera(usercontext, oggettobulk);
        return inizializzaOrdine(usercontext, oggetto, false);
    }

    public SQLBuilder selectBeneServizioByClause(UserContext userContext, OrdineAcqRigaBulk riga,
                                                 Bene_servizioBulk bene,
                                                 CompoundFindClause compoundfindclause) throws PersistencyException, ComponentException {
        Bene_servizioHome beneHome = (Bene_servizioHome) getHome(userContext, Bene_servizioBulk.class);
        SQLBuilder sql = beneHome.selectByClause(userContext, compoundfindclause);
        sql.addSQLClause(FindClause.AND, "FL_VALIDO", SQLBuilder.EQUALS, Bene_servizioBulk.STATO_VALIDO);
        ContrattoBulk contrattoBulk = riga.getOrdineAcq().getContratto();
        if (riga.getOrdineAcq() != null && contrattoBulk != null && contrattoBulk.getTipo_dettaglio_contratto() != null) {
            sql.addTableToHeader("DETTAGLIO_CONTRATTO");
            if (contrattoBulk.isDettaglioContrattoPerArticoli()) {
                sql.addSQLJoin("BENE_SERVIZIO.CD_BENE_SERVIZIO", SQLBuilder.EQUALS, "DETTAGLIO_CONTRATTO.CD_BENE_SERVIZIO");

            } else if (contrattoBulk.isDettaglioContrattoPerCategoriaGruppo()) {
                sql.addSQLJoin("BENE_SERVIZIO.CD_CATEGORIA_GRUPPO", SQLBuilder.EQUALS, "DETTAGLIO_CONTRATTO.CD_CATEGORIA_GRUPPO");
            }
            sql.addSQLClause(FindClause.AND, "DETTAGLIO_CONTRATTO.STATO", SQLBuilder.NOT_EQUALS, Dettaglio_contrattoBulk.STATO_ANNULLATO);
            sql.addSQLClause(FindClause.AND, "DETTAGLIO_CONTRATTO.PG_CONTRATTO", SQLBuilder.EQUALS, riga.getOrdineAcq().getContratto().getPg_contratto());
            sql.addSQLClause(FindClause.AND, "DETTAGLIO_CONTRATTO.ESERCIZIO_CONTRATTO", SQLBuilder.EQUALS, riga.getOrdineAcq().getContratto().getEsercizio());
            sql.addSQLClause(FindClause.AND, "DETTAGLIO_CONTRATTO.STATO_CONTRATTO", SQLBuilder.EQUALS, riga.getOrdineAcq().getContratto().getStato());
        }

        return sql;
    }

    public SQLBuilder selectObbligazioneScadenzarioByClause(UserContext userContext, OrdineAcqConsegnaBulk consegna,
                                                            Obbligazione_scadenzarioBulk obblScad,
                                                            CompoundFindClause compoundfindclause) throws PersistencyException, ComponentException, IntrospectionException, RemoteException {

        Obbligazione_scadenzarioHome obblScadHome = (Obbligazione_scadenzarioHome) getHome(userContext, Obbligazione_scadenzarioBulk.class);
        Filtro_ricerca_obbligazioniVBulk filtro = new Filtro_ricerca_obbligazioniVBulk();
        filtro.setFornitore(consegna.getOrdineAcqRiga().getOrdineAcq().getFornitore());
        filtro.setIm_importo(consegna.getImTotaleConsegna());

        java.util.List listaCapitoli = recuperoListaCapitoliSelezionabili(userContext, consegna);
        filtro.setListaVociSelezionabili(listaCapitoli);
        filtro.setContratto(consegna.getOrdineAcqRiga().getOrdineAcq().getContratto());
        Unita_organizzativaBulk uo = recuperoUoPerImpegno(userContext, consegna);
        if (uo != null && uo.getCd_unita_organizzativa() != null) {
            filtro.setCd_unita_organizzativa(uo.getCd_unita_organizzativa());
        } else {
            throw new it.cnr.jada.comp.ApplicationException("Non è stato possibile individuare l'unità organizzativa.");
        }
        filtro.setFl_importo(Boolean.FALSE);

        return ricercaObbligazioni(userContext, filtro, obblScadHome);

    }

    private java.util.List recuperoListaCapitoliSelezionabili(UserContext userContext, OrdineAcqConsegnaBulk consegna)
            throws ComponentException, PersistencyException, IntrospectionException, RemoteException {
        CategoriaGruppoInventComponentSession session = EJBCommonServices.createEJB(
                "CNRDOCAMM00_EJB_CategoriaGruppoInventComponentSession",
                CategoriaGruppoInventComponentSession.class);

        java.util.List listaCapitoli = new ArrayList<>();
        if (consegna.getOrdineAcqRiga().getCdBeneServizio() != null) {
            Bene_servizioBulk bene = recuperoBeneServizio(userContext, consegna.getOrdineAcqRiga().getCdBeneServizio());
            if (bene != null) {
                java.util.List titoliCapitoliCatGrp = session.findAssVoceFList(userContext, bene.getCategoria_gruppo());
                if (titoliCapitoliCatGrp == null)
                    throw new it.cnr.jada.comp.ApplicationException("Alla categoria " + bene.getCategoria_gruppo().getCd_categoria_gruppo() + "\" non è stato attribuita l'associazione al capitolo di spesa");
                for (java.util.Iterator k = titoliCapitoliCatGrp.iterator(); k.hasNext(); ) {
                    Categoria_gruppo_voceBulk assVoce = (Categoria_gruppo_voceBulk) k.next();
                    listaCapitoli.add(assVoce.getElemento_voce());
                }
            } else {
                throw new it.cnr.jada.comp.ApplicationException("Bene/servizio non valido per il dettaglio " + ((consegna.getOrdineAcqRiga().getRiga() == null) ? "" : "\"" + consegna.getOrdineAcqRiga().getRiga() + "\"") + "!");
            }
        } else {
            throw new it.cnr.jada.comp.ApplicationException("Valorizzare il bene/servizio per il dettaglio " + ((consegna.getOrdineAcqRiga().getRiga() == null) ? "" : "\"" + consegna.getOrdineAcqRiga().getRiga() + "\"") + "!");
        }
        return listaCapitoli;
    }

    public SQLBuilder selectDspMagazzinoByClause(UserContext userContext, OrdineAcqRigaBulk riga,
                                                 MagazzinoBulk mag,
                                                 CompoundFindClause compoundfindclause) throws PersistencyException, ComponentException {
        MagazzinoHome magHome = (MagazzinoHome) getHome(userContext, MagazzinoBulk.class);
        return magHome.selectMagazziniAbilitatiByClause(userContext, riga.getOrdineAcq().getUnitaOperativaOrd(), TipoOperazioneOrdBulk.OPERAZIONE_ORDINE, compoundfindclause, riga.getDspTipoConsegna());
    }

    public SQLBuilder selectMagazzinoByClause(UserContext userContext, OrdineAcqConsegnaBulk cons,
                                              MagazzinoBulk mag,
                                              CompoundFindClause compoundfindclause) throws PersistencyException, ComponentException {
        MagazzinoHome magHome = (MagazzinoHome) getHome(userContext, MagazzinoBulk.class);
        return magHome.selectMagazziniAbilitatiByClause(userContext, cons.getOrdineAcqRiga().getOrdineAcq().getUnitaOperativaOrd(), TipoOperazioneOrdBulk.OPERAZIONE_ORDINE, compoundfindclause, cons.getTipoConsegna());
    }

    public SQLBuilder selectVoceIvaByClause(UserContext userContext, OrdineAcqRigaBulk riga,
                                            Voce_ivaBulk voceIva,
                                            CompoundFindClause compoundfindclause) throws PersistencyException, ComponentException {
        Voce_ivaHome voceIvaHome = (Voce_ivaHome) getHome(userContext, Voce_ivaBulk.class);
        SQLBuilder sql = voceIvaHome.selectByClause(userContext, compoundfindclause);
        if (riga.getBeneServizio() == null)
            throw new it.cnr.jada.comp.ApplicationException("Impossibile caricare il Codice Iva! E' necessario prima selezionare il bene/servizio.");
        return sql;
    }

    public SQLBuilder selectLuogoConsegnaMagByClause(UserContext userContext, OrdineAcqConsegnaBulk cons,
                                                     LuogoConsegnaMagBulk luogo,
                                                     CompoundFindClause compoundfindclause) throws PersistencyException, ComponentException {
        LuogoConsegnaMagHome luogoHome = (LuogoConsegnaMagHome) getHome(userContext, LuogoConsegnaMagBulk.class);
        SQLBuilder sql = luogoHome.selectByClause(userContext, compoundfindclause);
        if (cons.getMagazzino() == null)
            throw new it.cnr.jada.comp.ApplicationException("Impossibile caricare il Codice Iva! E' necessario prima selezionare il bene/servizio.");
        MagazzinoBulk mag = cons.getMagazzino();
        if (mag.getLuogoConsegnaMag() == null || mag.getLuogoConsegnaMag().getCdLuogoConsegna() == null) {
            MagazzinoHome magHome = (MagazzinoHome) getHome(userContext, MagazzinoBulk.class);
            try {
                mag = (MagazzinoBulk) magHome.findByPrimaryKey(userContext, mag);
            } catch (PersistencyException e) {
                throw new ComponentException(e);
            }
        }

        sql.addSQLClause(FindClause.AND, "CD_LUOGO_CONSEGNA", SQLBuilder.EQUALS, mag.getCdLuogoConsegna());

        return sql;
    }

    public SQLBuilder selectTipoOrdineByClause(UserContext userContext, OrdineAcqBulk ord,
                                               TipoOrdineBulk tipo,
                                               CompoundFindClause compoundfindclause) throws PersistencyException, ComponentException {
        TipoOrdineHome tipoHome = (TipoOrdineHome) getHome(userContext, TipoOrdineBulk.class);
        return tipoHome.selectByClause(userContext, compoundfindclause);
    }

    protected Query select(UserContext userContext, CompoundFindClause clauses, OggettoBulk bulk) throws ComponentException, it.cnr.jada.persistency.PersistencyException {
        SQLBuilder sql = (SQLBuilder) super.select(userContext, clauses, bulk);
        AbilUtenteUopOperHome abilHome = (AbilUtenteUopOperHome) getHomeCache(userContext).getHome(AbilUtenteUopOperBulk.class);
        OrdineAcqBulk ordineAcqBulk = (OrdineAcqBulk) bulk;

        UtenteBulk utente = (UtenteBulk) (getHome(userContext, UtenteBulk.class).findByPrimaryKey(new UtenteBulk(CNRUserContext.getUser(userContext))));

        if (!utente.isSupervisore()) {
            SQLBuilder sqlExists = abilHome.createSQLBuilder();
            sqlExists.addSQLJoin("ORDINE_ACQ.CD_UNITA_OPERATIVA", "ABIL_UTENTE_UOP_OPER.CD_UNITA_OPERATIVA");
            if (!ordineAcqBulk.getIsForFirma()) {
                sqlExists.openParenthesis(FindClause.AND);
                sqlExists.addSQLClause(FindClause.OR, "ABIL_UTENTE_UOP_OPER.CD_TIPO_OPERAZIONE", SQLBuilder.EQUALS, TipoOperazioneOrdBulk.OPERAZIONE_ORDINE);
                sqlExists.addSQLClause(FindClause.OR, "ABIL_UTENTE_UOP_OPER.CD_TIPO_OPERAZIONE", SQLBuilder.EQUALS, TipoOperazioneOrdBulk.OPERAZIONE_APPROVAZIONE_ORDINE);
                sqlExists.closeParenthesis();
            } else {
                sqlExists.addSQLClause(FindClause.AND, "ABIL_UTENTE_UOP_OPER.CD_TIPO_OPERAZIONE", SQLBuilder.EQUALS, TipoOperazioneOrdBulk.OPERAZIONE_FIRMA_ORDINE);
                sql.openParenthesis(FindClause.AND);
                sql.addSQLClause(FindClause.OR, "ORDINE_ACQ.STATO", SQLBuilder.EQUALS, OrdineAcqBulk.STATO_ALLA_FIRMA);
                sql.addSQLClause(FindClause.OR, "ORDINE_ACQ.STATO", SQLBuilder.EQUALS, OrdineAcqBulk.STATO_DEFINITIVO);
                sql.addSQLClause(FindClause.OR, "ORDINE_ACQ.STATO", SQLBuilder.EQUALS, OrdineAcqBulk.STATO_INVIATO_ORDINE);
                sql.closeParenthesis();
            }
            sqlExists.addSQLClause(FindClause.AND, "ABIL_UTENTE_UOP_OPER.CD_UTENTE", SQLBuilder.EQUALS, userContext.getUser());

            sql.addSQLExistsClause(FindClause.AND, sqlExists);
        }
        sql.addSQLClause(FindClause.AND, "ORDINE_ACQ.STATO", SQLBuilder.NOT_EQUALS, OrdineAcqBulk.STATO_ANNULLATO);
        sql.addOrderBy("cd_cds");
        sql.addOrderBy("cd_unita_operativa");
        sql.addOrderBy("esercizio");
        sql.addOrderBy("cd_numeratore");
        sql.addOrderBy("numero");
        return sql;
    }

    private OggettoBulk inizializzaOrdine(UserContext usercontext, OggettoBulk oggettobulk, Boolean daInserimento)
            throws ComponentException {
        OrdineAcqBulk ordine = (OrdineAcqBulk) oggettobulk;
        try {
            if (daInserimento) {
                impostaDatiDivisaCambioDefault(usercontext, ordine);
            }
            OrdineAcqHome home = (OrdineAcqHome) getHomeCache(usercontext).getHome(OrdineAcqBulk.class);
            if (daInserimento) {
                ordine.setCdCds(((CNRUserContext) usercontext).getCd_cds());
            }
            if (ordine.getCdUopOrdine() == null) {
                UnitaOperativaOrdHome uopHome = (UnitaOperativaOrdHome) getHome(usercontext, UnitaOperativaOrdBulk.class);
                SQLBuilder sql = home.selectUnitaOperativaOrdByClause(usercontext, ordine, uopHome, new UnitaOperativaOrdBulk(), new CompoundFindClause());
                List listUop = uopHome.fetchAll(sql);
                if (listUop != null && (listUop.size() == 1 || isPresenteUnaUop(listUop))) {
                    ordine.setUnitaOperativaOrd((UnitaOperativaOrdBulk) listUop.get(0));
                    ordine.setUnicoMagazzinoAbilitato(unicoMagazzinoAbilitato(usercontext, ordine));
                }
            }
            assegnaNumeratoreOrd(usercontext, ordine, home);
            ordine.setStatoOriginale(ordine.getStato());
        } catch (PersistencyException e) {
            throw new ComponentException(e);
        }
        return ordine;
    }

    private Boolean isPresenteUnaUop(List listUop) {
        UnitaOperativaOrdKey key = null;
        for (Object oggettoBulk : listUop) {
            UnitaOperativaOrdBulk uop = (UnitaOperativaOrdBulk) oggettoBulk;
            if (key == null) {
                key = (UnitaOperativaOrdKey) uop.getKey();
            } else {
                if (!key.equals(uop.getKey())) {
                    return false;
                }
            }
        }
        return key != null;
    }

    private void impostaDatiDivisaCambioDefault(UserContext usercontext, OrdineAcqBulk ordine) throws ComponentException {
        ordine.setDivisa(getEuro(usercontext));
        ordine.setCambio(BigDecimal.ONE);
    }

    private MagazzinoBulk unicoMagazzinoAbilitato(UserContext userContext, OrdineAcqBulk ordine) throws ComponentException {

        MagazzinoHome home = (MagazzinoHome) getHome(userContext, MagazzinoBulk.class);

        try {
            SQLBuilder sql = home.selectMagazziniAbilitatiByClause(userContext, ordine.getUnitaOperativaOrd(), TipoOperazioneOrdBulk.OPERAZIONE_ORDINE, new CompoundFindClause());
            List listMag = home.fetchAll(sql);
            if (listMag != null && listMag.size() == 1) {
                MagazzinoBulk mag = (MagazzinoBulk) listMag.get(0);
                if (mag.getLuogoConsegnaMag() != null) {
                    LuogoConsegnaMagHome luogoHome = (LuogoConsegnaMagHome) getHome(userContext, LuogoConsegnaMagBulk.class);
                    LuogoConsegnaMagBulk luogo = (LuogoConsegnaMagBulk) luogoHome.findByPrimaryKey(userContext, mag.getLuogoConsegnaMag());
                    if (luogo != null) {
                        mag.setLuogoConsegnaMag(luogo);
                    }

                }
                return mag;
            }
        } catch (PersistencyException e) {
            throw new ComponentException(e);
        }
        return null;
    }

    private void assegnaNumeratoreOrd(UserContext usercontext, OrdineAcqBulk ordine, OrdineAcqHome home)
            throws PersistencyException, ComponentException {
        if (ordine.getCdNumeratore() == null && ordine.getCdUopOrdine() != null) {
            NumerazioneOrdHome numerazioneHome = (NumerazioneOrdHome) getHome(usercontext, NumerazioneOrdBulk.class);
            SQLBuilder sql = home.selectNumerazioneOrdByClause(usercontext, ordine, numerazioneHome, new NumerazioneOrdBulk(), new CompoundFindClause());
            List listNum = numerazioneHome.fetchAll(sql);
            if (listNum != null && listNum.size() == 1) {
                ordine.setNumerazioneOrd((NumerazioneOrdBulk) listNum.get(0));
                ordine.setPercProrata(((NumerazioneOrdBulk) listNum.get(0)).getPercProrata());
                ordine.setTiAttivita(((NumerazioneOrdBulk) listNum.get(0)).getTi_istituz_commerc());
            }
        }
    }

    public Boolean isUtenteAbilitatoOrdine(UserContext usercontext, OrdineAcqBulk ordine) throws ComponentException, PersistencyException {
        return isUtenteAbilitato(usercontext, ordine, TipoOperazioneOrdBulk.OPERAZIONE_ORDINE);
    }

    public Boolean isUtenteAbilitatoValidazioneOrdine(UserContext usercontext, OrdineAcqBulk ordine) throws ComponentException, PersistencyException {
        return isUtenteAbilitato(usercontext, ordine, TipoOperazioneOrdBulk.OPERAZIONE_APPROVAZIONE_ORDINE);
    }

    private Boolean isUtenteAbilitato(UserContext usercontext, OrdineAcqBulk ordine, String tipoOperazione) throws ComponentException {
        if (ordine.getCdUnitaOperativa() != null) {
            AbilUtenteUopOperBulk abil = recuperoAbilUtenteUo(usercontext, ordine, tipoOperazione);
            return abil != null;
        }
        return true;
    }

    private AbilUtenteUopOperBulk recuperoAbilUtenteUo(UserContext userContext, OrdineAcqBulk ordine, String tipoOperazione) throws ComponentException {
        if (ordine.getCdUopOrdine() != null) {
            AbilUtenteUopOperHome abilHome = (AbilUtenteUopOperHome) getHome(userContext, AbilUtenteUopOperBulk.class);
            AbilUtenteUopOperBulk abil = new AbilUtenteUopOperBulk(userContext.getUser(), ordine.getCdUopOrdine(), tipoOperazione);
            try {
                return (AbilUtenteUopOperBulk) abilHome.findByPrimaryKey(userContext, abil);
            } catch (PersistencyException e) {
                throw new ComponentException(e);
            }
        }
        return null;
    }

    public void completaOrdine(UserContext userContext, OrdineAcqBulk ordine) throws PersistencyException, ComponentException {
        OrdineAcqHome home = (OrdineAcqHome) getHomeCache(userContext).getHome(OrdineAcqBulk.class);
        assegnaNumeratoreOrd(userContext, ordine, home);
    }

    public it.cnr.jada.bulk.OggettoBulk modificaConBulk(it.cnr.jada.UserContext userContext, it.cnr.jada.bulk.OggettoBulk bulk, it.cnr.contab.doccont00.core.bulk.OptionRequestParameter status)
            throws it.cnr.jada.comp.ComponentException {
        OrdineAcqBulk ordine = (OrdineAcqBulk) bulk;
        boolean aggiornaAnalitica = (ordine.isStatoAllaFirma() || (ordine.isOrdineMepa() && ordine.isStatoDefinitivo())) &&
                !ordine.getStato().equals(ordine.getStatoOriginale());
        validaOrdine(userContext, ordine);
        controlliCambioStato(userContext, ordine);
        calcolaImportoOrdine(userContext, ordine);
        manageDeletedElements(userContext, ordine, status);
        aggiornaObbligazioni(userContext, ordine, status);
        verificaCoperturaContratto(userContext, ordine);
        if (aggiornaAnalitica)
            aggiornaAnaliticaConsegne(userContext, (OrdineAcqBulk) bulk);
        return super.modificaConBulk(userContext, bulk);
    }

    private void controlliCambioStato(UserContext usercontext, OrdineAcqBulk ordine) throws ComponentException {
        OrdineAcqBulk ordineDB;
        try {
            ordineDB = (OrdineAcqBulk) getTempHome(usercontext, OrdineAcqBulk.class).findByPrimaryKey(
                    new OrdineAcqBulk(
                            ordine.getCdCds(),
                            ordine.getCdUnitaOperativa(),
                            ordine.getEsercizio(),
                            ordine.getCdNumeratore(),
                            ordine.getNumero()
                    ));
            if (ordineDB != null && !ordineDB.getStato().equals(ordine.getStato())) {
                if (ordineDB.isOrdineInserito()) {
                    if (!ordine.isOrdineInviatoApprovazione() && (!ordine.isOrdineAnnullato())) {
                        throw new it.cnr.jada.comp.ApplicationException("Non è possibile indicare uno stato diverso da 'in approvazione'");
                    }
                } else if (ordineDB.isOrdineDefinitivo()) {
                    if (!ordine.isOrdineInviatoFornitore()) {
                        throw new it.cnr.jada.comp.ApplicationException("Non è possibile indicare uno stato diverso da inviato al fornitore");
                    }
                } else if (ordineDB.isOrdineAllaFirma()) {
                    if (!(ordine.isStatoDefinitivo() || ordine.isStatoInApprovazione())) {
                        throw new it.cnr.jada.comp.ApplicationException("Non è possibile indicare uno stato diverso da definito o in approvazione");
                    }
                } else if (ordineDB.isOrdineInviatoApprovazione()) {
                    AbilUtenteUopOperHome abilHome = (AbilUtenteUopOperHome) getHomeCache(usercontext).getHome(AbilUtenteUopOperBulk.class);
                    if (!abilHome.isUtenteAbilitato(usercontext, TipoOperazioneOrdBulk.OPERAZIONE_APPROVAZIONE_ORDINE, ordine.getCdUnitaOperativa())) {
                        throw new it.cnr.jada.comp.ApplicationException("Utente non abilitato ad operare su ordini in approvazione");
                    }

                    if ((!ordine.isOrdineMepa()) && (!(ordine.isStatoAllaFirma() || ordine.isStatoInserito()))) {
                        throw new it.cnr.jada.comp.ApplicationException("Non è possibile indicare uno stato diverso da inserito o alla firma");
                    }
                    if (ordine.isOrdineMepa() && (!(ordine.isStatoDefinitivo() || ordine.isStatoInserito()))) {
                        throw new it.cnr.jada.comp.ApplicationException("Non è possibile indicare uno stato diverso da inserito o Definitivo");
                    }
                    if (ordine.isStatoAllaFirma() || (ordine.isOrdineMepa() && ordine.isStatoDefinitivo())) {
                        for (OrdineAcqRigaBulk riga : ordine.getRigheOrdineColl()) {
                            if (Utility.createConfigurazioneCnrComponentSession().isAttivaAnalitica(usercontext, riga.getEsercizio())) {
                                if (riga.getImCostoEcoDaRipartire().compareTo(BigDecimal.ZERO)!=0)
                                    throw new it.cnr.jada.comp.ApplicationException("Sulla riga " + riga.getRigaOrdineString() + " non risulta ripartita totalmente l'analitica.");
                            }
                            if (Utility.createConfigurazioneCnrComponentSession().isAttivaFinanziaria(usercontext, riga.getEsercizio())) {
                                for (OrdineAcqConsegnaBulk cons : riga.getRigheConsegnaColl()) {
                                    if (cons.getObbligazioneScadenzario() == null || cons.getObbligazioneScadenzario().getPg_obbligazione() == null)
                                        throw new it.cnr.jada.comp.ApplicationException("Sulla consegna " + cons.getConsegnaOrdineString() + " non è indicata l'obbligazione");
                                }
                            }
                        }
                    }
                } else if (ordineDB.isOrdineInviatoFornitore()) {
                    throw new it.cnr.jada.comp.ApplicationException("Non è possibile cambiare lo stato di un ordine inviato al fornitore");
                }
            }
        } catch (PersistencyException | IntrospectionException | RemoteException e) {
            throw new ComponentException(e);
        }
    }

    @Override
    public OggettoBulk modificaConBulk(UserContext usercontext, OggettoBulk oggettobulk) throws ComponentException {
        return modificaConBulk(usercontext, oggettobulk, null);
    }


    private Boolean isUoImpegnoDaUopDestinazione(UserContext userContext) throws ComponentException {
        String uoDestinazione;
        try {
            uoDestinazione = EJBCommonServices.createEJB("CNRCONFIG00_EJB_Configurazione_cnrComponentSession", it.cnr.contab.config00.ejb.Configurazione_cnrComponentSession.class).getVal01(userContext, CNRUserContext.getEsercizio(userContext), "*", Configurazione_cnrBulk.PK_ORDINI, Configurazione_cnrBulk.SK_ORDINE_IMPEGNO_UO_DESTINAZIONE);
            if (uoDestinazione != null && uoDestinazione.equals("Y"))
                return true;
        } catch (javax.ejb.EJBException | RemoteException e) {
            throw  handleException(e);
        }
        return false;
    }

    private DivisaBulk getEuro(UserContext userContext) throws ComponentException {
        try {
            DivisaBulk divisaDefault = ((DivisaHome) getHome(userContext, DivisaBulk.class)).getDivisaDefault(userContext);
            Optional.ofNullable(divisaDefault)
                    .map(DivisaBulk::getCd_divisa)
                    .orElseThrow(() -> new it.cnr.jada.comp.ApplicationException("Impossibile caricare la valuta di default! Prima di poter inserire un ordine, immettere tale valore."));
            return divisaDefault;
        } catch (javax.ejb.EJBException | PersistencyException e) {
            throw handleException(e);
        }
    }

    /**
     * Pre:  Ricerca CIG
     * Post: Il CIG può essere collegato ad un contratto solo se vengono rispettate le seguenti regole:
     * CD_TERZO_RUP del CIG è il medesimo del contratto che si sta inserendo quindi :
     * CIG. CD_TERZO_RUP = CONTRATTO. CD_TERZO_RESP
     * Il CIG non deve risultare associato ad altri contratti.
     */
    public SQLBuilder selectCigByClause(UserContext userContext, OrdineAcqBulk ordine, CigBulk cig, CompoundFindClause clause) throws ComponentException, PersistencyException {
        if (clause == null)
            clause = cig.buildFindClauses(null);
        SQLBuilder sql = getHome(userContext, cig).createSQLBuilder();
        if (ordine.getResponsabileProcPers() == null || ordine.getResponsabileProcPers().getCd_terzo() == null)
            throw new ApplicationException("Per effettuare la ricerca valorizzare il campo Responsabile!");
        sql.addSQLClause(FindClause.AND, "CD_TERZO_RUP", SQLBuilder.EQUALS, ordine.getResponsabileProcPers().getCd_terzo());
        sql.addClause(FindClause.AND, "FL_VALIDO", SQLBuilder.EQUALS, Boolean.TRUE);
        if (clause != null)
            sql.addClause(clause);
        return sql;
    }

    /**
     * Pre:  Ricerca Figura giuridica interna
     * Post: Limitazione ai terzi di tipo Unità Organizzativa
     */
    public SQLBuilder selectTerzoCdrByClause(UserContext userContext, OggettoBulk bulk, TerzoBulk terzo, CompoundFindClause clause) throws ComponentException, PersistencyException {
        if (clause == null)
            clause = terzo.buildFindClauses(null);
        SQLBuilder sql = getHome(userContext, terzo).createSQLBuilder();
        sql.addSQLClause(FindClause.AND, "CD_UNITA_ORGANIZZATIVA", SQLBuilder.ISNOTNULL, null);
        // Se uo 999.000 in scrivania: visualizza tutti i progetti
        Unita_organizzativa_enteBulk ente = (Unita_organizzativa_enteBulk) getHome(userContext, Unita_organizzativa_enteBulk.class).findAll().get(0);

        if (!((CNRUserContext) userContext).getCd_unita_organizzativa().equals(ente.getCd_unita_organizzativa()))
            sql.addSQLClause(FindClause.AND, "CD_UNITA_ORGANIZZATIVA", SQLBuilder.EQUALS, CNRUserContext.getCd_unita_organizzativa(userContext));

        if (clause != null)
            sql.addClause(clause);
        return sql;
    }

    public SQLBuilder selectFornitoreByClause(UserContext userContext, OrdineAcqBulk bulk, TerzoBulk terzo, CompoundFindClause clauses) throws ComponentException {

        TerzoHome home = (TerzoHome) getHome(userContext, TerzoBulk.class, "V_TERZO_CF_PI");
        SQLBuilder sql = home.createSQLBuilder();
        sql.addSQLClause(FindClause.AND, "DT_FINE_RAPPORTO", SQLBuilder.ISNULL, null);
        sql.addSQLClause(FindClause.AND, "CD_UNITA_ORGANIZZATIVA", SQLBuilder.ISNULL, null);
        sql.addClause(clauses);

        final Optional<TerzoBulk> optionalTerzoContrattoBulk = Optional.ofNullable(bulk)
                .flatMap(o -> Optional.ofNullable(o.getContratto()))
                .flatMap(c -> Optional.ofNullable(c.getFigura_giuridica_esterna()));
        if (optionalTerzoContrattoBulk.isPresent()) {
            if (optionalTerzoContrattoBulk
                    .flatMap(t -> Optional.ofNullable(t.getAnagrafico()))
                    .flatMap(a -> Optional.ofNullable(a.getFl_studio_associato()))
                    .orElse(Boolean.FALSE)
            ) {
                sql.setHeader("select distinct V_TERZO_CF_PI.*");
                sql.addTableToHeader("ANAGRAFICO_TERZO");
                sql.openParenthesis(FindClause.AND);
                sql.openParenthesis(FindClause.AND);
                sql.addSQLJoin("V_TERZO_CF_PI.CD_TERZO", "ANAGRAFICO_TERZO.CD_TERZO");
                sql.addSQLClause(FindClause.AND, "ANAGRAFICO_TERZO.CD_ANAG", SQLBuilder.EQUALS, optionalTerzoContrattoBulk.get().getCd_anag());
                sql.openParenthesis(FindClause.AND);
                sql.addSQLClause(FindClause.AND, "ANAGRAFICO_TERZO.DT_CANC", SQLBuilder.ISNULL, null);
                sql.addSQLClause(FindClause.OR, "ANAGRAFICO_TERZO.DT_CANC", SQLBuilder.GREATER_EQUALS, EJBCommonServices.getServerDate());
                sql.closeParenthesis();
                sql.closeParenthesis();
                sql.addSQLClause(FindClause.OR, "V_TERZO_CF_PI.CD_TERZO", SQLBuilder.EQUALS, optionalTerzoContrattoBulk.get().getCd_terzo());
                sql.closeParenthesis();
            } else {
                sql.addSQLClause(FindClause.AND, "V_TERZO_CF_PI.CD_TERZO", SQLBuilder.EQUALS, optionalTerzoContrattoBulk.get().getCd_terzo());
            }
        }
        return sql;
    }

    /**
     * Pre:  Ricerca Tipo Provvedimento
     * Post: Limitazione ai tipi non annullati
     */
    public SQLBuilder selectProcedureAmministrativeByClause(UserContext userContext, OggettoBulk bulk, Procedure_amministrativeBulk procedura_amministrativa, CompoundFindClause clause) throws ComponentException, PersistencyException {
        if (clause == null)
            clause = procedura_amministrativa.buildFindClauses(null);
        SQLBuilder sql = getHome(userContext, procedura_amministrativa).createSQLBuilder();
        sql.openParenthesis(FindClause.AND);
        sql.addClause(FindClause.OR, "ti_proc_amm", SQLBuilder.EQUALS, Procedure_amministrativeBulk.TIPO_FORNITURA_SERVIZI);
        sql.addClause(FindClause.OR, "ti_proc_amm", SQLBuilder.EQUALS, Procedure_amministrativeBulk.TIPO_GENERICA);
        sql.closeParenthesis();
        sql.addClause(FindClause.AND, "fl_cancellato", SQLBuilder.EQUALS, Boolean.FALSE);
        if (clause != null)
            sql.addClause(clause);
        return sql;
    }

    public RemoteIterator cercaObbligazioni(UserContext context, Filtro_ricerca_obbligazioniVBulk filtro)
            throws ComponentException {

        Obbligazione_scadenzarioHome home = (Obbligazione_scadenzarioHome) getHome(context, Obbligazione_scadenzarioBulk.class);
        it.cnr.jada.persistency.sql.SQLBuilder sql = ricercaObbligazioni(context, filtro, home);

        return iterator(
                context,
                sql,
                Obbligazione_scadenzarioBulk.class,
                "default");
    }

    private it.cnr.jada.persistency.sql.SQLBuilder ricercaObbligazioni(UserContext context,
                                                                       Filtro_ricerca_obbligazioniVBulk filtro, Obbligazione_scadenzarioHome home) {
        it.cnr.jada.persistency.sql.SQLBuilder sql = home.createSQLBuilder();
        sql.setDistinctClause(true);
        sql.addTableToHeader("OBBLIGAZIONE");
        sql.addSQLClause(FindClause.AND, "OBBLIGAZIONE.STATO_OBBLIGAZIONE", SQLBuilder.EQUALS, ObbligazioneBulk.STATO_OBB_DEFINITIVO);
        sql.addSQLJoin("OBBLIGAZIONE_SCADENZARIO.CD_CDS", "OBBLIGAZIONE.CD_CDS");
        sql.addSQLJoin("OBBLIGAZIONE_SCADENZARIO.ESERCIZIO", "OBBLIGAZIONE.ESERCIZIO");
        sql.addSQLJoin("OBBLIGAZIONE_SCADENZARIO.ESERCIZIO_ORIGINALE", "OBBLIGAZIONE.ESERCIZIO_ORIGINALE");
        sql.addSQLJoin("OBBLIGAZIONE_SCADENZARIO.PG_OBBLIGAZIONE", "OBBLIGAZIONE.PG_OBBLIGAZIONE");

        sql.addTableToHeader("ELEMENTO_VOCE");
        sql.addSQLJoin("OBBLIGAZIONE.CD_ELEMENTO_VOCE", "ELEMENTO_VOCE.CD_ELEMENTO_VOCE");
        sql.addSQLJoin("OBBLIGAZIONE.TI_APPARTENENZA", "ELEMENTO_VOCE.TI_APPARTENENZA");
        sql.addSQLJoin("OBBLIGAZIONE.TI_GESTIONE", "ELEMENTO_VOCE.TI_GESTIONE");
        sql.addSQLJoin("OBBLIGAZIONE.ESERCIZIO", "ELEMENTO_VOCE.ESERCIZIO");

        sql.addSQLClause(FindClause.AND, "OBBLIGAZIONE.ESERCIZIO", SQLBuilder.EQUALS, it.cnr.contab.utenze00.bp.CNRUserContext.getEsercizio(context));
        sql.addSQLClause(FindClause.AND, "OBBLIGAZIONE.RIPORTATO", SQLBuilder.EQUALS, "N");
        sql.addSQLClause(FindClause.AND, "OBBLIGAZIONE.DT_CANCELLAZIONE", SQLBuilder.ISNULL, null);
        sql.addSQLClause(FindClause.AND, "OBBLIGAZIONE_SCADENZARIO.IM_SCADENZA", SQLBuilder.NOT_EQUALS, new java.math.BigDecimal(0));
        sql.addSQLClause(FindClause.AND, "OBBLIGAZIONE_SCADENZARIO.IM_ASSOCIATO_DOC_AMM = ? OR OBBLIGAZIONE_SCADENZARIO.IM_ASSOCIATO_DOC_AMM IS NULL");
        sql.addParameter(new java.math.BigDecimal(0).setScale(2, RoundingMode.HALF_UP), java.sql.Types.DECIMAL, 2);
        sql.addSQLClause(FindClause.AND, "OBBLIGAZIONE_SCADENZARIO.IM_ASSOCIATO_DOC_CONTABILE = ? OR OBBLIGAZIONE_SCADENZARIO.IM_ASSOCIATO_DOC_CONTABILE IS NULL");
        sql.addParameter(new java.math.BigDecimal(0).setScale(2, RoundingMode.HALF_UP), java.sql.Types.DECIMAL, 2);
        sql.addSQLClause(FindClause.AND, "OBBLIGAZIONE.CD_UNITA_ORGANIZZATIVA", SQLBuilder.EQUALS, filtro.getCd_unita_organizzativa());

        if (filtro.getElemento_voce() != null) {
            sql.addSQLClause(FindClause.AND, "OBBLIGAZIONE.CD_ELEMENTO_VOCE", SQLBuilder.STARTSWITH, filtro.getElemento_voce().getCd_elemento_voce());
            sql.addSQLClause(FindClause.AND, "OBBLIGAZIONE.TI_APPARTENENZA", SQLBuilder.EQUALS, filtro.getElemento_voce().getTi_appartenenza());
            sql.addSQLClause(FindClause.AND, "OBBLIGAZIONE.TI_GESTIONE", SQLBuilder.EQUALS, filtro.getElemento_voce().getTi_gestione());
            sql.addSQLClause(FindClause.AND, "OBBLIGAZIONE.ESERCIZIO", SQLBuilder.EQUALS, filtro.getElemento_voce().getEsercizio());
        }

        if (filtro.getListaVociSelezionabili() != null && !filtro.getListaVociSelezionabili().isEmpty()) {
            sql.openParenthesis(FindClause.AND);
            for (Elemento_voceBulk voce : filtro.getListaVociSelezionabili()) {
                sql.openParenthesis(FindClause.OR);
                sql.addSQLClause(FindClause.AND, "OBBLIGAZIONE.CD_ELEMENTO_VOCE", SQLBuilder.EQUALS, voce.getCd_elemento_voce());
                sql.addSQLClause(FindClause.AND, "OBBLIGAZIONE.TI_APPARTENENZA", SQLBuilder.EQUALS, voce.getTi_appartenenza());
                sql.addSQLClause(FindClause.AND, "OBBLIGAZIONE.TI_GESTIONE", SQLBuilder.EQUALS, voce.getTi_gestione());
                sql.addSQLClause(FindClause.AND, "OBBLIGAZIONE.ESERCIZIO", SQLBuilder.EQUALS, voce.getEsercizio());
                sql.closeParenthesis();

            }
            sql.closeParenthesis();
        }

        if (filtro.getContratto() != null && filtro.getContratto().getPg_contratto() != null) {
            sql.addSQLClause(FindClause.AND, "OBBLIGAZIONE.PG_CONTRATTO", SQLBuilder.EQUALS, filtro.getContratto().getPg_contratto());
            sql.addSQLClause(FindClause.AND, "OBBLIGAZIONE.ESERCIZIO_CONTRATTO", SQLBuilder.EQUALS, filtro.getContratto().getEsercizio());
            sql.addSQLClause(FindClause.AND, "OBBLIGAZIONE.STATO_CONTRATTO", SQLBuilder.EQUALS, filtro.getContratto().getStato());
        }

        sql.addSQLClause(FindClause.AND, "OBBLIGAZIONE.FL_PGIRO", SQLBuilder.EQUALS, "N");

        if (!filtro.getFl_fornitore()) {
            sql.addTableToHeader("TERZO");
            sql.addTableToHeader("ANAGRAFICO");
            sql.addSQLJoin("OBBLIGAZIONE.CD_TERZO", "TERZO.CD_TERZO");
            sql.addSQLJoin("TERZO.CD_ANAG", "ANAGRAFICO.CD_ANAG");
            sql.addSQLClause(FindClause.AND, "(OBBLIGAZIONE.CD_TERZO = ? OR ANAGRAFICO.TI_ENTITA = ?)");
            sql.addParameter(filtro.getFornitore().getCd_terzo(), java.sql.Types.INTEGER, 0);
            sql.addParameter(AnagraficoBulk.DIVERSI, java.sql.Types.VARCHAR, 0);
        } else {
            sql.addSQLClause(FindClause.AND, "OBBLIGAZIONE.CD_TERZO", SQLBuilder.EQUALS, filtro.getFornitore().getCd_terzo());
        }

        if (filtro.getFl_data_scadenziario() && filtro.getData_scadenziario() != null)
            sql.addSQLClause(FindClause.AND, "OBBLIGAZIONE_SCADENZARIO.DT_SCADENZA", SQLBuilder.EQUALS, filtro.getData_scadenziario());
        if (filtro.getFl_importo() && filtro.getIm_importo() != null)
            sql.addSQLClause(FindClause.AND, "OBBLIGAZIONE_SCADENZARIO.IM_SCADENZA", SQLBuilder.GREATER_EQUALS, filtro.getIm_importo());

        //filtro su Tipo obbligazione
        if (filtro.getFl_nr_obbligazione() && filtro.getTipo_obbligazione() != null) {
            if (ObbligazioneBulk.TIPO_COMPETENZA.equals(filtro.getTipo_obbligazione()))
                sql.addSQLClause(FindClause.AND, "OBBLIGAZIONE.CD_TIPO_DOCUMENTO_CONT", SQLBuilder.EQUALS, Numerazione_doc_contBulk.TIPO_OBB);
            else if (ObbligazioneBulk.TIPO_RESIDUO_PROPRIO.equals(filtro.getTipo_obbligazione()))
                sql.addSQLClause(FindClause.AND, "OBBLIGAZIONE.CD_TIPO_DOCUMENTO_CONT", SQLBuilder.EQUALS, Numerazione_doc_contBulk.TIPO_OBB_RES);
            else if (ObbligazioneBulk.TIPO_RESIDUO_IMPROPRIO.equals(filtro.getTipo_obbligazione()))
                sql.addSQLClause(FindClause.AND, "OBBLIGAZIONE.CD_TIPO_DOCUMENTO_CONT", SQLBuilder.EQUALS, Numerazione_doc_contBulk.TIPO_OBB_RES_IMPROPRIA);
        }

        //filtro su Anno Residuo obbligazione
        if (filtro.getFl_nr_obbligazione() && filtro.getEsercizio_ori_obbligazione() != null)
            sql.addSQLClause(FindClause.AND, "OBBLIGAZIONE.ESERCIZIO_ORIGINALE", SQLBuilder.EQUALS, filtro.getEsercizio_ori_obbligazione());

        //filtro su Numero obbligazione
        if (filtro.getFl_nr_obbligazione() && filtro.getNr_obbligazione() != null)
            sql.addSQLClause(FindClause.AND, "OBBLIGAZIONE.PG_OBBLIGAZIONE", SQLBuilder.EQUALS, filtro.getNr_obbligazione());
        return sql;
    }

    public OrdineAcqBulk contabilizzaDettagliSelezionati(
            UserContext context,
            OrdineAcqBulk ordine,
            java.util.Collection dettagliSelezionati,
            it.cnr.contab.doccont00.core.bulk.Obbligazione_scadenzarioBulk obbligazioneSelezionata)
            throws ComponentException {

        if (obbligazioneSelezionata != null && dettagliSelezionati != null) {
            if (!dettagliSelezionati.isEmpty()) {
                for (java.util.Iterator i = dettagliSelezionati.iterator(); i.hasNext(); ) {
                    OrdineAcqRigaBulk riga = (OrdineAcqRigaBulk) i.next();

                    validaScadenze(ordine, obbligazioneSelezionata);

                    riga.setDspObbligazioneScadenzario(obbligazioneSelezionata);
                    riga.setToBeUpdated();
                    for (OrdineAcqConsegnaBulk cons : riga.getRigheConsegnaColl()) {
                        if (cons.getObbligazioneScadenzario() == null || cons.getEsercizioOrigObbl() == null || cons.getObbligazioneScadenzario().equalsByPrimaryKey(riga.getDspObbligazioneScadenzario()))
                            ordine.addToOrdineObbligazioniHash(obbligazioneSelezionata, cons);
                        else
                            throw new it.cnr.jada.DetailedRuntimeException("L'impegno sulla riga di consegna è diverso dall'impegno indicato sulla riga d'ordine.");
                    }
                }
                ordine.addToOrdineAss_totaliMap(obbligazioneSelezionata, calcolaTotalePer(
                        context, (Vector) ordine.getOrdineObbligazioniHash().get(obbligazioneSelezionata),
                        false));
            } else {
                ordine.addToOrdineObbligazioniHash(obbligazioneSelezionata, null);
            }
            try {
                ObbligazioneAbstractComponentSession session = EJBCommonServices.createEJB(
                        "CNRDOCCONT00_EJB_ObbligazioneAbstractComponentSession",
                        ObbligazioneAbstractComponentSession.class);
                session.lockScadenza(context, obbligazioneSelezionata);
            } catch (Throwable t) {
                throw handleException(ordine, t);
            }
        }
        return ordine;
    }

    public OrdineAcqBulk contabilizzaConsegneSelezionate(
            UserContext context,
            OrdineAcqBulk ordine,
            java.util.Collection dettagliSelezionati,
            it.cnr.contab.doccont00.core.bulk.Obbligazione_scadenzarioBulk obbligazioneSelezionata)
            throws ComponentException {

        if (obbligazioneSelezionata != null && dettagliSelezionati != null) {
            if (!dettagliSelezionati.isEmpty()) {
                for (java.util.Iterator i = dettagliSelezionati.iterator(); i.hasNext(); ) {
                    OrdineAcqConsegnaBulk cons = (OrdineAcqConsegnaBulk) i.next();

                    validaScadenze(ordine, obbligazioneSelezionata);

                    cons.setObbligazioneScadenzario(obbligazioneSelezionata);
                    cons.setToBeUpdated();
                    if (cons.getObbligazioneScadenzario() != null && cons.getEsercizioOrigObbl() != null) {
                        ordine.addToOrdineObbligazioniHash(obbligazioneSelezionata, cons);
                    }
                    ordine.addToOrdineAss_totaliMap(obbligazioneSelezionata, calcolaTotalePer(
                            context, (Vector) ordine.getOrdineObbligazioniHash().get(obbligazioneSelezionata),
                            false));
                }
                try {
                    ObbligazioneAbstractComponentSession session = EJBCommonServices.createEJB(
                            "CNRDOCCONT00_EJB_ObbligazioneAbstractComponentSession",
                            ObbligazioneAbstractComponentSession.class);
                    session.lockScadenza(context, obbligazioneSelezionata);
                } catch (Throwable t) {
                    throw handleException(ordine, t);
                }
            }
        }
        return ordine;
    }

    private java.math.BigDecimal calcolaTotalePer(
            UserContext userContext,
            java.util.List selectedModels,
            boolean escludiIVA)
            throws it.cnr.jada.comp.ComponentException {

        java.math.BigDecimal importo = new java.math.BigDecimal(0);
        //RP 20/03/2015
        boolean escludiIVAOld = escludiIVA;
        if (selectedModels != null) {
            for (java.util.Iterator i = selectedModels.iterator(); i.hasNext(); ) {
                escludiIVA = escludiIVAOld;
                OrdineAcqConsegnaBulk consegna = (OrdineAcqConsegnaBulk) i.next();
                if (!consegna.isConsegna0()) {
                    importo = importo.add(
                            (escludiIVA) ?
                                    consegna.getImImponibile() :
                                    consegna.getImTotaleConsegna());
                }
            }
        }

        importo = importo.setScale(2, RoundingMode.HALF_UP);
        return importo;
    }

    private void validaScadenze(OrdineAcqBulk ordine, Obbligazione_scadenzarioBulk newScad) throws ComponentException {
        Iterator it;

        Vector scadCanc = ordine.getDocumentiContabiliCancellati();
        if (scadCanc != null) {
            it = scadCanc.iterator();

            while (it.hasNext()) {
                Obbligazione_scadenzarioBulk scad = (Obbligazione_scadenzarioBulk) it.next();
                if (scad.getObbligazione() instanceof ObbligazioneResBulk) {
                    if (scad.getObbligazione().equalsByPrimaryKey(newScad.getObbligazione()) && ((ObbligazioneResBulk) scad.getObbligazione()).getObbligazione_modifica() != null
                            && ((ObbligazioneResBulk) scad.getObbligazione()).getObbligazione_modifica().getPg_modifica() != null) {
                        throw new it.cnr.jada.comp.ApplicationException("Impossibile collegare una scadenza dell'impegno residuo " + scad.getPg_obbligazione() + " poichè é stata effettuata una modifica in questo ordine!");
                    }
                }
            }
        }

        ObbligazioniTable obbligazioniHash = ordine.getObbligazioniHash();
        if (obbligazioniHash != null && !obbligazioniHash.isEmpty()) {

            for (java.util.Enumeration e = obbligazioniHash.keys(); e.hasMoreElements(); ) {
                Obbligazione_scadenzarioBulk scad = (Obbligazione_scadenzarioBulk) e.nextElement();
                if (scad.getObbligazione() instanceof ObbligazioneResBulk) {
                    if (scad.getObbligazione().equalsByPrimaryKey(newScad.getObbligazione()) && ((ObbligazioneResBulk) scad.getObbligazione()).getObbligazione_modifica() != null
                            && ((ObbligazioneResBulk) scad.getObbligazione()).getObbligazione_modifica().getPg_modifica() != null) {
                        throw new it.cnr.jada.comp.ApplicationException("Impossibile collegare una scadenza dell'impegno residuo " + scad.getPg_obbligazione() + " poichè è stata effettuata una modifica in questo ordine!");
                    }
                }
            }
        }
    }

    private void gestioneImpegnoChiusuraForzataOrdineRiduzione(UserContext userContext, Obbligazione_scadenzarioBulk obbligazione_scadenzario, BigDecimal differenza) throws ComponentException, PersistencyException, RemoteException {
        ObbligazioneComponentSession obbligComp = (ObbligazioneComponentSession) EJBCommonServices.createEJB("CNRDOCCONT00_EJB_ObbligazioneComponentSession");
        DatiFinanziariScadenzeDTO dati = new DatiFinanziariScadenzeDTO();
        dati.setNuovoImportoScadenzaVecchia(obbligazione_scadenzario.getIm_scadenza().subtract(differenza));
        obbligComp.sdoppiaScadenzaInAutomaticoLight(userContext, obbligazione_scadenzario, dati);
        /*
          Aggiorno l'importo associato a documenti amministrativi sulla vecchia scadenza
         */
        Obbligazione_scadenzarioBulk oldScadenza = (Obbligazione_scadenzarioBulk) findByPrimaryKey(userContext, obbligazione_scadenzario);
        oldScadenza.setIm_associato_doc_amm(oldScadenza.getIm_scadenza());
        oldScadenza.setToBeUpdated();
        super.modificaConBulk(userContext, oldScadenza);

    }

    private Boolean existScandenzaLibera(UserContext userContext, Obbligazione_scadenzarioBulk scadenza) throws ComponentException, PersistencyException {
        List<Obbligazione_scadenzarioBulk> l = recuperoScadenzaLibera(userContext, scadenza);
        return (l != null && !l.isEmpty());
    }

    private List<Obbligazione_scadenzarioBulk> recuperoScadenzaLibera(UserContext userContext, Obbligazione_scadenzarioBulk scadenza) throws ComponentException, PersistencyException {
        Obbligazione_scadenzarioHome obbligazioneScadenzarioHome = (Obbligazione_scadenzarioHome) getHome(userContext, Obbligazione_scadenzarioBulk.class);
        SQLBuilder sql = obbligazioneScadenzarioHome.createSQLBuilder();
        sql.addClause(FindClause.AND, "cdCds", SQLBuilder.EQUALS, scadenza.getCd_cds());
        sql.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, scadenza.getEsercizio());
        sql.addClause(FindClause.AND, "esercizio_originale", SQLBuilder.EQUALS, scadenza.getEsercizio_originale());
        sql.addClause(FindClause.AND, "pg_obbligazione", SQLBuilder.EQUALS, scadenza.getPg_obbligazione());
        sql.addClause(FindClause.AND, "im_associato_doc_amm", SQLBuilder.EQUALS, BigDecimal.ZERO);
        sql.addClause(FindClause.AND, "im_scadenza", SQLBuilder.GREATER, BigDecimal.ZERO);
        return (List<Obbligazione_scadenzarioBulk>) obbligazioneScadenzarioHome.fetchAll(sql);
    }

    private void aumentoAutomaticoScadenzaModificaOrdine(UserContext userContext, Obbligazione_scadenzarioBulk obbligazione_scadenzario, BigDecimal importoDaAggiungere) throws ComponentException, PersistencyException, RemoteException {

        ObbligazioneBulk obbligazioneBulk = obbligazione_scadenzario.getObbligazione();
        BigDecimal importoVecchioScadenza = obbligazione_scadenzario.getIm_scadenza();
        List<Integer> scadenzeDaEliminare = new ArrayList<>();
        int index = 0;
        for (Obbligazione_scadenzarioBulk scadenza : obbligazioneBulk.getObbligazione_scadenzarioColl()) {
            if (!scadenza.equalsByPrimaryKey(obbligazione_scadenzario) && scadenza.getImportoDisponibile().compareTo(BigDecimal.ZERO) > 0) {
                if (importoDaAggiungere.compareTo(scadenza.getImportoDisponibile()) < 0) {
                    obbligazione_scadenzario.setIm_scadenza(obbligazione_scadenzario.getIm_scadenza().add(importoDaAggiungere));
                    allineaScadVoce(obbligazione_scadenzario, importoVecchioScadenza);
                    importoVecchioScadenza = scadenza.getIm_scadenza();
                    scadenza.setIm_scadenza(scadenza.getIm_scadenza().subtract(importoDaAggiungere));
                    allineaScadVoce(scadenza, importoVecchioScadenza);
                    importoDaAggiungere = BigDecimal.ZERO;
                    scadenza.setToBeUpdated();
                } else {
                    importoDaAggiungere = importoDaAggiungere.subtract(scadenza.getImportoDisponibile());
                    obbligazione_scadenzario.setIm_scadenza(obbligazione_scadenzario.getIm_scadenza().add(scadenza.getIm_scadenza()));
                    allineaScadVoce(obbligazione_scadenzario, importoVecchioScadenza);
                    scadenza.setToBeDeleted();
                    scadenzeDaEliminare.add(index);
                }
                if (importoDaAggiungere.compareTo(BigDecimal.ZERO) == 0) {
                    break;
                }
            } else if (scadenza.getIm_scadenza().compareTo(scadenza.getIm_associato_doc_amm()) != 0) {
                scadenza.setToBeUpdated();
            }
            index++;
        }
        for (Integer ind : scadenzeDaEliminare) {
            obbligazioneBulk.removeFromObbligazione_scadenzarioColl(ind);
        }

        if (importoDaAggiungere.compareTo(BigDecimal.ZERO) > 0) {
            throw new ApplicationMessageFormatException("L''impegno {0}/{1}/{2}" +
                    " non ha disponibilità sufficiente per la modifica dell''importo dell''ordine per {3}!",
                    String.valueOf(obbligazione_scadenzario.getEsercizio_originale()),
                    String.valueOf(obbligazione_scadenzario.getPg_obbligazione()),
                    String.valueOf(obbligazione_scadenzario.getPg_obbligazione_scadenzario()),
                    new EuroFormat().format(importoDaAggiungere)
            );
        }

        ObbligazioneComponentSession obbligComp = (ObbligazioneComponentSession) EJBCommonServices.createEJB("CNRDOCCONT00_EJB_ObbligazioneComponentSession");
        obbligazioneBulk.setToBeUpdated();
        obbligComp.modificaConBulk(userContext, obbligazioneBulk);
    }

    private void allineaScadVoce(Obbligazione_scadenzarioBulk obbligazione_scadenzario, BigDecimal importoVecchioScadenza) {
        BigDecimal importoScadVoce = BigDecimal.ZERO;
        for (Obbligazione_scad_voceBulk osv : obbligazione_scadenzario.getObbligazione_scad_voceColl()) {
            if (osv.getIm_voce().compareTo(importoVecchioScadenza) == 0) {
                osv.setIm_voce(obbligazione_scadenzario.getIm_scadenza());
                osv.setToBeUpdated();
                importoScadVoce = osv.getIm_voce();
                break;
            } else {
                BigDecimal nuovoImporto = osv.getIm_voce().multiply(obbligazione_scadenzario.getIm_scadenza()).divide(importoVecchioScadenza, 2, RoundingMode.HALF_UP);
                osv.setIm_voce(nuovoImporto);
                osv.setToBeUpdated();
                importoScadVoce = importoScadVoce.add(osv.getIm_voce());
            }
        }
        BigDecimal diff = obbligazione_scadenzario.getIm_scadenza().subtract(importoScadVoce);
        if (diff.compareTo(BigDecimal.ZERO) != 0) {
            for (Obbligazione_scad_voceBulk osv : obbligazione_scadenzario.getObbligazione_scad_voceColl()) {
                osv.setIm_voce(osv.getIm_voce().add(diff));
                break;
            }
        }
    }

    private void riduzioneAutomaticaScadenzaModificaOrdine(UserContext userContext, Obbligazione_scadenzarioBulk obbligazione_scadenzario, BigDecimal importoDaTogliere) throws ComponentException, PersistencyException, RemoteException {
        BigDecimal nuovoImporto = obbligazione_scadenzario.getIm_scadenza().subtract(importoDaTogliere);
        ObbligazioneComponentSession obbligComp = (ObbligazioneComponentSession) EJBCommonServices.createEJB("CNRDOCCONT00_EJB_ObbligazioneComponentSession");
        if (obbligazione_scadenzario.getEsercizio().compareTo(obbligazione_scadenzario.getEsercizio_originale()) == 0 && existScandenzaLibera(userContext, obbligazione_scadenzario)) {
            obbligazione_scadenzario.getObbligazione().setCheckDisponibilitaContrattoEseguito(Boolean.TRUE);
            obbligComp.modificaScadenzaInAutomatico(userContext, obbligazione_scadenzario, nuovoImporto, true);
        } else {
            DatiFinanziariScadenzeDTO dati = new DatiFinanziariScadenzeDTO();
            dati.setNuovoImportoScadenzaVecchia(nuovoImporto);
            dati.setMantieniImportoAssociatoScadenza(true);

            obbligComp.sdoppiaScadenzaInAutomatico(userContext, obbligazione_scadenzario, dati);
        }
    }

    private Boolean isImpegnoCollegatoInteramenteOrdine(UserContext userContex, OrdineAcqBulk ordine, Obbligazione_scadenzarioBulk obbligazione_scadenzario) throws ComponentException, PersistencyException {
        OrdineAcqConsegnaHome ordineAcqConsegnaHome = (OrdineAcqConsegnaHome) getHome(userContex, OrdineAcqConsegnaBulk.class);

        SQLBuilder sql = ordineAcqConsegnaHome.createSQLBuilder();
        sql.resetColumns();
        sql.addColumn("SUM(IM_TOTALE_CONSEGNA) IM_TOTALE_CONSEGNA");
        sql.addClause(FindClause.AND, "cdCds", SQLBuilder.EQUALS, ordine.getCdCds());
        sql.addClause(FindClause.AND, "cdUnitaOperativa", SQLBuilder.EQUALS, ordine.getCdUnitaOperativa());
        sql.addClause(FindClause.AND, "esercizio", SQLBuilder.EQUALS, ordine.getEsercizio());
        sql.addClause(FindClause.AND, "cdNumeratore", SQLBuilder.EQUALS, ordine.getCdNumeratore());
        sql.addClause(FindClause.AND, "numero", SQLBuilder.EQUALS, ordine.getNumero());

        sql.addClause(FindClause.AND, "esercizioObbl", SQLBuilder.EQUALS, obbligazione_scadenzario.getEsercizio());
        sql.addClause(FindClause.AND, "esercizioOrigObbl", SQLBuilder.EQUALS, obbligazione_scadenzario.getEsercizio_originale());
        sql.addClause(FindClause.AND, "pgObbligazione", SQLBuilder.EQUALS, obbligazione_scadenzario.getPg_obbligazione());
        sql.addClause(FindClause.AND, "cdCdsObbl", SQLBuilder.EQUALS, obbligazione_scadenzario.getCd_cds());
        final SQLBroker broker = ordineAcqConsegnaHome.createBroker(sql);
        if (broker.next()) {
            final BigDecimal importoOrdineImpegno = Optional.ofNullable(broker.fetchPropertyValue("imTotaleConsegna", OrdineAcqConsegnaBulk.class))
                    .filter(BigDecimal.class::isInstance)
                    .map(BigDecimal.class::cast)
                    .orElse(BigDecimal.ZERO);
            return importoOrdineImpegno.compareTo(obbligazione_scadenzario.getObbligazione().getIm_obbligazione()) == 0;
        }
        return Boolean.TRUE;
    }

    private boolean isModificaImpegniConRiduzioneImporto(UserContext userContext, OrdineAcqBulk ordine, Obbligazione_scadenzarioBulk obbligazione_scadenzario) throws ComponentException, PersistencyException {
        try {
            Boolean isResiduoConRiduzione = Boolean.FALSE;
            // Serve a capire se l'impegno è usato solo per quest'ordine o anche per altri ordini. Se è usato anche per altri non riduco l'impegno a prescindere, ma riduco solo l'importo collegato all'ordine.
            Boolean isSameImportOrdineImpegno = isImpegnoCollegatoInteramenteOrdine(userContext, ordine, obbligazione_scadenzario);
            if (!isSameImportOrdineImpegno)
                isResiduoConRiduzione = Boolean.FALSE;
            else {
                if (
                        (obbligazione_scadenzario.getEsercizio().compareTo(obbligazione_scadenzario.getEsercizio_originale()) == 0)) {
                    try {
                        isResiduoConRiduzione = Utility.createConfigurazioneCnrComponentSession().getGestioneImpegnoChiusuraForzataCompetenza(userContext);
                    } catch (Exception e) {
                        throw new ApplicationException("E' necessario indicare la gestione degli impegni per l'Evasione Forzata a Competenza.");
                    }

                }
                if (obbligazione_scadenzario.getEsercizio().compareTo(obbligazione_scadenzario.getEsercizio_originale()) != 0) {
                    try {
                        isResiduoConRiduzione = Utility.createConfigurazioneCnrComponentSession().getGestioneImpegnoChiusuraForzataResiduo(userContext);
                    } catch (Exception e) {
                        throw new ApplicationException("E' necessario indicare la gestione degli impegni per l'Evasione Forzata a Residuo.");
                    }
                }
            }
            return isResiduoConRiduzione;
        } catch (Exception e) {
            throw new ComponentException(e);
        }
    }

    private void aggiornaObbligazioni(
            UserContext userContext,
            OrdineAcqBulk ordine,
            OptionRequestParameter status)
            throws ComponentException {

        if (ordine != null) {
            ObbligazioniTable obbligazioniHash = ordine.getOrdineObbligazioniHash();
            if (obbligazioniHash != null && !obbligazioniHash.isEmpty()) {
                //Aggiorna i saldi per le obbligazioni NON temporanee
                for (java.util.Enumeration e = getDocumentiContabiliNonTemporanei(((ObbligazioniTable) obbligazioniHash.clone()).keys()).keys(); e.hasMoreElements(); )
                    aggiornaSaldi(
                            userContext,
                            ordine,
                            (IDocumentoContabileBulk) e.nextElement(),
                            status);

                PrimaryKeyHashtable obblTemporanee = getDocumentiContabiliTemporanei(((ObbligazioniTable) obbligazioniHash.clone()).keys());
                for (java.util.Enumeration e = obblTemporanee.keys(); e.hasMoreElements(); ) {
                    ObbligazioneBulk obblT = (ObbligazioneBulk) e.nextElement();

                    //Aggiorna i saldi per le obbligazioni temporanee
                    //DEVE ESSERE FATTO PRIMA DELL'AGGIORNAMENTO A DEFINITIVA
                    aggiornaSaldi(userContext, ordine, obblT, status);

                    aggiornaObbligazioniTemporanee(userContext, obblT);
                    obblTemporanee = new it.cnr.jada.bulk.PrimaryKeyHashtable(obblTemporanee);
                    for (Iterator i = ((Vector) obblTemporanee.get(obblT)).iterator(); i.hasNext(); )
                        ((ObbligazioneBulk) i.next()).setPg_obbligazione(obblT.getPg_obbligazione());
                }
                ObbligazioniTable newObbligazioniHash = new ObbligazioniTable(obbligazioniHash);

                ordine.setOrdineObbligazioniHash(newObbligazioniHash);
                for (java.util.Enumeration e = ((ObbligazioniTable) newObbligazioniHash.clone()).keys(); e.hasMoreElements(); ) {
                    Obbligazione_scadenzarioBulk scadenza = (Obbligazione_scadenzarioBulk) e.nextElement();
                    it.cnr.jada.bulk.BulkHome homeObbligazione = getHome(userContext, ObbligazioneBulk.class);
                    ObbligazioneBulk obbl;
                    try {
                        obbl = (ObbligazioneBulk) homeObbligazione.findByPrimaryKey(scadenza.getObbligazione());
                    } catch (PersistencyException e1) {
                        throw handleException(e1);
                    }
                    scadenza.setObbligazione(obbl);

                    java.math.BigDecimal im_ass = calcolaTotaleObbligazione(userContext, scadenza, ordine);
                    scadenza.setFlAssociataOrdine(true);
                    scadenza.setIm_associato_doc_amm(im_ass);
                    if (ordine.getAggiornaImpegniInAutomatico()) {
                        Obbligazione_scadenzarioHome osHome = (Obbligazione_scadenzarioHome) getHome(userContext, Obbligazione_scadenzarioBulk.class);
                        try {
                            scadenza.setObbligazione_scad_voceColl(new BulkList<>(osHome.findObbligazione_scad_voceList(userContext, scadenza)));
                        } catch (PersistencyException persistencyException) {
                            throw new ComponentException(persistencyException);
                        }

                        ObbligazioneComponentSession obbligComp = (ObbligazioneComponentSession) EJBCommonServices.createEJB("CNRDOCCONT00_EJB_ObbligazioneComponentSession");
                        try {
                            obbl = (ObbligazioneBulk) obbligComp.inizializzaBulkPerModifica(userContext, scadenza.getObbligazione());
                            if(!ordine.isVerificaContratto()){
                                obbl.setCheckDisponibilitaContrattoEseguito(true);
                            }
                            scadenza.setObbligazione(obbl);
                        } catch (RemoteException remoteException) {
                            throw new ComponentException(remoteException);
                        }

                        BulkList<Obbligazione_scadenzarioBulk> listaScadenza = new BulkList<>();
                        int i = 0;
                        for (Obbligazione_scadenzarioBulk scad : scadenza.getObbligazione().getObbligazione_scadenzarioColl()) {
                            if (scad.equalsByPrimaryKey(scadenza)) {
                                listaScadenza.add(i, scadenza);
                            } else {
                                listaScadenza.add(i, scad);
                            }
                            i++;
                        }

                        scadenza.getObbligazione().setObbligazione_scadenzarioColl(listaScadenza);
                        BigDecimal differenzaDaAggiornare = scadenza.getIm_scadenza().subtract(im_ass);
                        if (differenzaDaAggiornare.compareTo(BigDecimal.ZERO) > 0) {
                            try {
                                if (isModificaImpegniConRiduzioneImporto(userContext, ordine, scadenza)) {
                                    gestioneImpegnoChiusuraForzataOrdineRiduzione(userContext, scadenza, differenzaDaAggiornare);
                                } else {
                                    riduzioneAutomaticaScadenzaModificaOrdine(userContext, scadenza, differenzaDaAggiornare);
                                }
                            } catch (PersistencyException | RemoteException persistencyException) {
                                throw new ComponentException(persistencyException);
                            }
                        } else if (differenzaDaAggiornare.compareTo(BigDecimal.ZERO) < 0) {
                            try {
                                aumentoAutomaticoScadenzaModificaOrdine(userContext, scadenza, differenzaDaAggiornare.abs());
                            } catch (RemoteException | PersistencyException exception) {
                                throw new ComponentException(exception);
                            }
                        }
                    } else {
                        scadenza.setToBeUpdated();
                        updateImportoAssociatoDocAmm(userContext, scadenza);
                    }
                }
            }
        }
    }

    private it.cnr.jada.bulk.PrimaryKeyHashtable getDocumentiContabiliNonTemporanei(java.util.Enumeration scadenze)  {

        it.cnr.jada.bulk.PrimaryKeyHashtable documentiContabiliNonTemporanei = new it.cnr.jada.bulk.PrimaryKeyHashtable();
        if (scadenze != null)
            while (scadenze.hasMoreElements()) {
                IScadenzaDocumentoContabileBulk scadenza = (IScadenzaDocumentoContabileBulk) scadenze.nextElement();
                if (!scadenza.getFather().isTemporaneo()) {
                    if (!documentiContabiliNonTemporanei.containsKey(scadenza.getFather())) {
                        Vector allInstances = new java.util.Vector();
                        allInstances.addElement(scadenza.getFather());
                        documentiContabiliNonTemporanei.put(scadenza.getFather(), allInstances);
                    } else {
                        ((Vector) documentiContabiliNonTemporanei.get(scadenza.getFather())).add(scadenza.getFather());
                    }
                }
            }
        return documentiContabiliNonTemporanei;
    }

    private it.cnr.jada.bulk.PrimaryKeyHashtable getDocumentiContabiliTemporanei(java.util.Enumeration scadenze) {
        it.cnr.jada.bulk.PrimaryKeyHashtable documentiContabiliTemporanei = new it.cnr.jada.bulk.PrimaryKeyHashtable();
        if (scadenze != null)
            while (scadenze.hasMoreElements()) {
                IScadenzaDocumentoContabileBulk scadenza = (IScadenzaDocumentoContabileBulk) scadenze.nextElement();
                if (scadenza.getFather().isTemporaneo()) {
                    if (!documentiContabiliTemporanei.containsKey(scadenza.getFather())) {
                        Vector allInstances = new java.util.Vector();
                        allInstances.addElement(scadenza.getFather());
                        documentiContabiliTemporanei.put(scadenza.getFather(), allInstances);
                    } else {
                        ((Vector) documentiContabiliTemporanei.get(scadenza.getFather())).add(scadenza.getFather());
                    }
                }
            }
        return documentiContabiliTemporanei;
    }

    private void aggiornaSaldi(
            it.cnr.jada.UserContext uc,
            OrdineAcqBulk ordine,
            IDocumentoContabileBulk docCont,
            OptionRequestParameter status)
            throws ComponentException {

        try {
            if (docCont != null && ordine != null && ordine.getDefferredSaldi() != null) {
                IDocumentoContabileBulk key = ordine.getDefferredSaldoFor(docCont);
                if (key != null) {
                    java.util.Map values = (java.util.Map) ordine.getDefferredSaldi().get(key);
                    //QUI chiamare component del documento contabile interessato
                    DocumentoContabileComponentSession session = null;
                    if (docCont instanceof ObbligazioneBulk) {
                        String  jndiName = "CNRDOCCONT00_EJB_ObbligazioneAbstractComponentSession";
                        session = EJBCommonServices.createEJB(
                                        jndiName, ObbligazioneAbstractComponentSession.class);
                    }
                    if (session != null) {
                        session.aggiornaSaldiInDifferita(uc, key, values, status);
                        //NON Differibile: si rischia di riprocessare i saldi impropriamente
                        ordine.getDefferredSaldi().remove(key);
                    }
                }
            }
        } catch (EJBException | RemoteException e) {
            throw handleException(ordine, e);
        }
    }

    private void aggiornaObbligazioniTemporanee(UserContext userContext, ObbligazioneBulk obbligazioneTemporanea) throws ComponentException {

        try {
            Numerazione_doc_contHome numHome = (Numerazione_doc_contHome) getHomeCache(userContext).getHome(Numerazione_doc_contBulk.class);
            Long pg = numHome.getNextPg(userContext,
                    obbligazioneTemporanea.getEsercizio(),
                    obbligazioneTemporanea.getCd_cds(),
                    obbligazioneTemporanea.getCd_tipo_documento_cont(),
                    obbligazioneTemporanea.getUser());
            ObbligazioneHome home = (ObbligazioneHome) getHome(userContext, obbligazioneTemporanea);
            home.confirmObbligazioneTemporanea(userContext, obbligazioneTemporanea, pg);
        } catch (PersistencyException | IntrospectionException e) {
            throw handleException(obbligazioneTemporanea, e);
        }
    }

    private java.math.BigDecimal calcolaTotaleObbligazione(
            it.cnr.jada.UserContext userContext,
            Obbligazione_scadenzarioBulk scadenza,
            OrdineAcqBulk ordine)
            throws it.cnr.jada.comp.ComponentException {

        ObbligazioniTable obbligazioniHash = ordine.getOrdineObbligazioniHash();
        Vector dettagli = (Vector) obbligazioniHash.get(scadenza);
        return calcolaTotalePer(userContext, dettagli, ordine.isCommerciale());
    }

    public IScadenzaDocumentoContabileBulk updateImportoAssociatoDocAmm(
            it.cnr.jada.UserContext userContext,
            IScadenzaDocumentoContabileBulk scadenza)
            throws it.cnr.jada.comp.ComponentException {

        try {
            ((IScadenzaDocumentoContabileHome) getHome(userContext, scadenza.getClass())).aggiornaImportoAssociatoADocAmm(userContext, scadenza);
        } catch (PersistencyException | BusyResourceException | OutdatedResourceException exc) {
            throw handleException((OggettoBulk) scadenza, exc);
        }

        return scadenza;
    }

    private void rebuildObbligazioni(UserContext aUC, OrdineAcqBulk ordine) throws ComponentException {
        if (ordine == null) return;

        BulkList<OrdineAcqRigaBulk> righe = ordine.getRigheOrdineColl();
        if (righe != null) {
            for (OrdineAcqRigaBulk riga : righe) {
                for (OrdineAcqConsegnaBulk cons : riga.getRigheConsegnaColl()) {
                    if (!cons.isConsegna0()) {
                        Obbligazione_scadenzarioBulk scadenza = cons.getObbligazioneScadenzario();
                        if (cons.getObbligazioneScadenzario() != null) {
                            if (ordine.getOrdineObbligazioniHash() == null ||
                                    ordine.getOrdineObbligazioniHash().getKey(scadenza) == null) {
                                scadenza = caricaScadenzaObbligazionePer(aUC, scadenza);
                            }else {
                                scadenza = ordine.getOrdineObbligazioniHash().getKey(scadenza);
                            }
                            ordine.addToOrdineObbligazioniHash(scadenza, cons);
                        }
                    }
                }
            }
        }
        try {
            getHomeCache(aUC).fetchAll(aUC);
        } catch (it.cnr.jada.persistency.PersistencyException e) {
            throw handleException(ordine, e);
        }
    }

    private Obbligazione_scadenzarioBulk caricaScadenzaObbligazionePer(
            UserContext context,
            Obbligazione_scadenzarioBulk scadenza)
            throws ComponentException {

        if (scadenza != null) {
            try {
                it.cnr.contab.doccont00.ejb.ObbligazioneAbstractComponentSession h = EJBCommonServices.createEJB(
                        "CNRDOCCONT00_EJB_ObbligazioneAbstractComponentSession",
                        ObbligazioneAbstractComponentSession.class);
                ObbligazioneBulk obbligazione = (ObbligazioneBulk) h.inizializzaBulkPerModifica(context, scadenza.getObbligazione());
                BulkList<Obbligazione_scadenzarioBulk> scadenze = obbligazione.getObbligazione_scadenzarioColl();
                scadenza = scadenze.get(scadenze.indexOfByPrimaryKey(scadenza));
            } catch (RemoteException | EJBException e) {
                throw handleException(scadenza, e);
            }
            return scadenza;
        }
        return null;
    }

    private void manageDocumentiContabiliCancellati(
            UserContext userContext,
            OrdineAcqBulk ordine,
            OptionRequestParameter status)
            throws ComponentException {

        if (ordine != null) {
            if (ordine.getDocumentiContabiliCancellati() != null &&
                    !ordine.getDocumentiContabiliCancellati().isEmpty()) {

                PrimaryKeyHashtable scadenzeConfermateTemporanee = getDocumentiContabiliTemporanei(
                        ordine.getOrdineObbligazioniHash().keys());
                Vector scadenzeConfermate = new Vector();
                java.util.Enumeration e = scadenzeConfermateTemporanee.keys();
                while (e.hasMoreElements()) {
                    OggettoBulk obj = (OggettoBulk) e.nextElement();
                    if (obj instanceof ObbligazioneBulk)
                        scadenzeConfermate.add(obj);
                }
                aggiornaObbligazioniSuCancellazione(
                        userContext,
                        ordine,
                        ordine.getDocumentiContabiliCancellati().elements(),
                        scadenzeConfermate,
                        status);
            }
        }
    }

    private void aggiornaObbligazioniSuCancellazione(
            UserContext userContext,
            OrdineAcqBulk ordine,
            java.util.Enumeration scadenzeDaCancellare,
            java.util.Collection scadenzeConfermate,
            OptionRequestParameter status)
            throws ComponentException {

        if (scadenzeDaCancellare != null) {

            it.cnr.jada.bulk.PrimaryKeyHashtable obblTemporanee = new it.cnr.jada.bulk.PrimaryKeyHashtable();
            for (java.util.Enumeration e = scadenzeDaCancellare; e.hasMoreElements(); ) {
                OggettoBulk oggettoBulk = (OggettoBulk) e.nextElement();
                if (oggettoBulk instanceof Obbligazione_scadenzarioBulk) {
                    Obbligazione_scadenzarioBulk scadenza = (Obbligazione_scadenzarioBulk) oggettoBulk;
                    if (scadenza.getObbligazione().isTemporaneo()) {
                        if (!obblTemporanee.containsKey(scadenza.getObbligazione())) {
                            Vector allInstances = new java.util.Vector();
                            allInstances.addElement(scadenza);
                            obblTemporanee.put(scadenza.getObbligazione(), allInstances);
                        } else {
                            ((Vector) obblTemporanee.get(scadenza.getObbligazione())).add(scadenza);
                        }
                    } else if (!ordine.isToBeCreated() && OggettoBulk.NORMAL == scadenza.getCrudStatus()) {
                        PrimaryKeyHashtable obbligs = getDocumentiContabiliNonTemporanei(ordine.getObbligazioniHash().keys());
                        if (!obbligs.containsKey(scadenza.getObbligazione()))
                            aggiornaSaldi(
                                    userContext,
                                    ordine,
                                    scadenza.getObbligazione(),
                                    status);
                        scadenza.setIm_associato_doc_amm(new java.math.BigDecimal(0).setScale(2, RoundingMode.HALF_UP));
                        scadenza.setFlAssociataOrdine(false);
                        updateImportoAssociatoDocAmm(userContext, scadenza);
                    }
                    /**
                     * Devo aggiornare i Saldi per quelle scadenze modificate e riportate
                     * ma poi scollegate dal documento
                     * Marco Spasiano 05/05/2006
                     */
                    aggiornaSaldi(userContext, ordine, scadenza.getObbligazione(), status);
                }
            }
            for (java.util.Enumeration e = obblTemporanee.keys(); e.hasMoreElements(); ) {
                ObbligazioneBulk obblT = (ObbligazioneBulk) e.nextElement();

                //Aggiorna i saldi per le obbligazioni temporanee
                //DEVE ESSERE FATTO PRIMA DELL'AGGIORNAMENTO A DEFINITIVA
                PrimaryKeyHashtable obbligs = getDocumentiContabiliTemporanei(ordine.getObbligazioniHash().keys());
                if (!obbligs.containsKey(obblT))
                    aggiornaSaldi(
                            userContext,
                            ordine,
                            obblT,
                            status);

                if (scadenzeConfermate == null || !it.cnr.jada.bulk.BulkCollections.containsByPrimaryKey(scadenzeConfermate, obblT))
                    aggiornaObbligazioniTemporanee(userContext, obblT);
            }
        }
    }

    private void manageDeletedElements(
            UserContext userContext,
            OrdineAcqBulk ordine,
            OptionRequestParameter status)
            throws ComponentException {
        if (ordine != null)
            manageDocumentiContabiliCancellati(userContext, ordine, status);
    }

    public void controllaQuadraturaObbligazioni(UserContext aUC, OrdineAcqBulk ordine)
            throws ComponentException {

        if (ordine != null) {
            ObbligazioniTable obbligazioniHash = ordine.getOrdineObbligazioniHash();
            if (obbligazioniHash != null) {
                for (java.util.Enumeration e = obbligazioniHash.keys(); e.hasMoreElements(); ) {
                    Obbligazione_scadenzarioBulk scadenza = (Obbligazione_scadenzarioBulk) e.nextElement();

                    try {
                        scadenza = (Obbligazione_scadenzarioBulk) getTempHome(aUC, Obbligazione_scadenzarioBulk.class).findByPrimaryKey(
                                new Obbligazione_scadenzarioBulk(
                                        scadenza.getCd_cds(),
                                        scadenza.getEsercizio(),
                                        scadenza.getEsercizio_originale(),
                                        scadenza.getPg_obbligazione(),
                                        scadenza.getPg_obbligazione_scadenzario()
                                ));
                    } catch (PersistencyException e1) {
                        throw new ComponentException(e1);
                    }

                    if (!ordine.getAggiornaImpegniInAutomatico()) {
                        java.math.BigDecimal totale = calcolaTotaleObbligazione(aUC, scadenza, ordine);
                        java.math.BigDecimal delta = scadenza.getIm_scadenza().subtract(totale);
                        if (delta.compareTo(new java.math.BigDecimal(0)) > 0) {
                            String sb = "Attenzione: La scadenza " +
                                    scadenza.getDs_scadenza() +
                                    " di " + scadenza.getIm_scadenza().doubleValue() + " EUR" +
                                    " è stata coperta solo per " +
                                    totale.doubleValue() + " EUR!";
                            throw new it.cnr.jada.comp.ApplicationException(sb);
                        } else if (delta.compareTo(new java.math.BigDecimal(0)) < 0) {
                            String sb = "Attenzione: La scadenza " +
                                    scadenza.getDs_scadenza() +
                                    " di " + scadenza.getIm_scadenza().doubleValue() + " EUR" +
                                    " è scoperta per " +
                                    delta.abs().doubleValue() + " EUR!";
                            throw new it.cnr.jada.comp.ApplicationException(sb);
                        }
                    }
                }
            }
        }
    }

    protected Bene_servizioBulk recuperoBeneServizio(it.cnr.jada.UserContext userContext, String cdBeneServizio)
            throws ComponentException, PersistencyException {
        Bene_servizioHome home = (Bene_servizioHome) getHome(userContext, Bene_servizioBulk.class);
        return (Bene_servizioBulk) home.findByPrimaryKey(new Bene_servizioBulk(cdBeneServizio));
    }

    public SQLBuilder selectContrattoByClause(UserContext userContext, OrdineAcqBulk ordine, ContrattoBulk contratto, CompoundFindClause clauses) throws ComponentException, it.cnr.jada.persistency.PersistencyException {
        Parametri_cdsHome paramHome = (Parametri_cdsHome) getHome(userContext, Parametri_cdsBulk.class);
        Parametri_cdsBulk param_cds;
        try {
            param_cds =
                    (Parametri_cdsBulk) paramHome.findByPrimaryKey(
                            new Parametri_cdsBulk(
                                    ordine.getCd_cds(),
                                    ordine.getEsercizio()));
        } catch (PersistencyException e) {
            throw new ComponentException(e);
        }

        SQLBuilder sql = getHome(userContext, ContrattoBulk.class).createSQLBuilder();

        if (clauses != null)
            sql.addClause(clauses);
        sql.openParenthesis(FindClause.AND);
        sql.addSQLClause(FindClause.AND, "NATURA_CONTABILE", SQLBuilder.EQUALS, ContrattoBulk.NATURA_CONTABILE_PASSIVO);
        sql.addSQLClause(FindClause.OR, "NATURA_CONTABILE", SQLBuilder.EQUALS, ContrattoBulk.NATURA_CONTABILE_ATTIVO_E_PASSIVO);
        sql.closeParenthesis();

        if (param_cds != null && param_cds.getFl_contratto_cessato()) {
            sql.openParenthesis(FindClause.AND);
            sql.addSQLClause(FindClause.AND, "STATO", SQLBuilder.EQUALS, ContrattoBulk.STATO_DEFINITIVO);
            sql.addSQLClause(FindClause.OR, "STATO", SQLBuilder.EQUALS, ContrattoBulk.STATO_CESSSATO);
            sql.closeParenthesis();
        } else
            sql.addSQLClause(FindClause.AND, "STATO", SQLBuilder.EQUALS, ContrattoBulk.STATO_DEFINITIVO);

        sql.addSQLClause(FindClause.AND, "CONTRATTO.CD_UNITA_ORGANIZZATIVA", SQLBuilder.EQUALS, CNRUserContext.getCd_unita_organizzativa(userContext));

        sql.addTableToHeader("TERZO");
        sql.addSQLJoin("CONTRATTO.FIG_GIUR_EST", SQLBuilder.EQUALS, "TERZO.CD_TERZO");
        sql.addSQLClause(FindClause.AND, "TERZO.DT_FINE_RAPPORTO", SQLBuilder.ISNULL, null);

        if ((ordine.getFornitore() != null && ordine.getFornitore().getCd_terzo() != null)) {
            sql.openParenthesis(FindClause.AND);
            sql.openParenthesis(FindClause.AND);
            sql.addSQLClause(FindClause.AND, "FIG_GIUR_EST", SQLBuilder.EQUALS, ordine.getFornitore().getCd_terzo());
            AnagraficoHome anagraficoHome = (AnagraficoHome) getHome(userContext, AnagraficoBulk.class);
            sql.closeParenthesis();
            try {
                for (Iterator<Anagrafico_terzoBulk> i = anagraficoHome.findAssociatiStudio(ordine.getFornitore().getAnagrafico()).iterator(); i.hasNext(); ) {
                    sql.openParenthesis(FindClause.OR);
                    Anagrafico_terzoBulk associato = i.next();
                    sql.addSQLClause(FindClause.OR, "CONTRATTO.FIG_GIUR_EST", SQLBuilder.EQUALS, associato.getCd_terzo());
                    sql.closeParenthesis();
                }
            } catch (IntrospectionException ignored) {
            }
            sql.closeParenthesis();
        }
        sql.addOrderBy("esercizio");
        sql.addOrderBy("pg_contratto");
        return sql;
    }

    public void verificaCoperturaContratto(UserContext aUC, OrdineAcqBulk ordine, int flag) throws ComponentException {
        if (ordine.getContratto() != null && ordine.getContratto().getPg_contratto() != null) {
            try {
                ContrattoHome contrattoHome = (ContrattoHome) getHome(aUC, ContrattoBulk.class);
                SQLBuilder sql = contrattoHome.calcolaTotOrdini(aUC, ordine.getContratto());
                BigDecimal totale = BigDecimal.ZERO;
                try {
                    java.sql.ResultSet rs = null;
                    LoggableStatement ps = null;
                    try {
                        ps = sql.prepareStatement(getConnection(aUC));
                        try {
                            rs = ps.executeQuery();
                            if (rs.next())
                                totale = Optional.ofNullable(rs.getBigDecimal(1)).orElse(BigDecimal.ZERO);
                        } catch (java.sql.SQLException e) {
                            throw handleSQLException(e);
                        } finally {
                            if (rs != null) try {
                                rs.close();
                            } catch (java.sql.SQLException ignored) {
                            }
                        }
                    } finally {
                        if (ps != null) try {
                            ps.close();
                        } catch (java.sql.SQLException ignored) {
                        }
                    }
                } catch (java.sql.SQLException ex) {
                    throw handleException(ex);
                }
                if (flag == INSERIMENTO) {
                    totale = Utility.nvl(totale).add(ordine.getImTotaleOrdine());
                } else if (flag == MODIFICA) {
                    OrdineAcqBulk ordineDB;
                    try {
                        ordineDB = (OrdineAcqBulk) getTempHome(aUC, OrdineAcqBulk.class).findByPrimaryKey(
                                new OrdineAcqBulk(
                                        ordine.getCdCds(),
                                        ordine.getCdUnitaOperativa(),
                                        ordine.getEsercizio(),
                                        ordine.getCdNumeratore(),
                                        ordine.getNumero()
                                ));
                    } catch (PersistencyException e) {
                        throw new ComponentException(e);
                    }
                    if (ordineDB != null)
                        totale = totale.subtract(Utility.nvl(ordineDB.getImTotaleOrdine())).add(Utility.nvl(ordine.getImTotaleOrdine()));
                }
                if (totale.compareTo(ordine.getContratto().getIm_contratto_passivo()) > 0)
                    throw handleException(new ApplicationException("La somma degli ordini associati " + totale.doubleValue() + " supera l'importo definito nel contratto " + ordine.getContratto().getIm_contratto_passivo()));

                if (ordine.getContratto().isDettaglioContrattoPerArticoli() || ordine.getContratto().isDettaglioContrattoPerCategoriaGruppo()) {
                    List<Dettaglio_contrattoBulk> dettagliContrattoDaAggiornare = new ArrayList<>();
                    for (java.util.Iterator i = ordine.getDettagliCancellati().iterator(); i.hasNext(); ) {
                        OrdineAcqRigaBulk riga = (OrdineAcqRigaBulk) i.next();
                        gestioneAggiornamentoDettaglioContratto(dettagliContrattoDaAggiornare, riga.getDettaglioContratto(), riga.getImTotaleRiga().negate(), riga.getQuantitaConsegneColl().negate());
                    }

                    for (OrdineAcqRigaBulk riga : ordine.getRigheOrdineColl()) {
                        if (riga.getDettaglioContratto() == null)
                            throw handleException(new ApplicationException("La riga " + riga.getRiga() + " non è collegata ad un dettaglio del contratto indicato"));

                        if (riga.isToBeCreated())
                            gestioneAggiornamentoDettaglioContratto(dettagliContrattoDaAggiornare, riga.getDettaglioContratto(), riga.getImTotaleRiga(), riga.getQuantitaConsegneColl());
                        else if (riga.isToBeUpdated()) {
                            OrdineAcqRigaBulk rigaDB;
                            try {
                                rigaDB = (OrdineAcqRigaBulk) getTempHome(aUC, OrdineAcqRigaBulk.class).findByPrimaryKey(
                                        new OrdineAcqRigaBulk(
                                                ordine.getCdCds(),
                                                ordine.getCdUnitaOperativa(),
                                                ordine.getEsercizio(),
                                                ordine.getCdNumeratore(),
                                                ordine.getNumero(),
                                                riga.getRiga()
                                        ));
                                rigaDB.setRigheConsegnaColl(new BulkList<>(((OrdineAcqRigaHome)getTempHome(aUC, OrdineAcqRigaBulk.class)).findOrdineRigheConsegnaList(rigaDB)));
                            } catch (PersistencyException e) {
                                throw new ComponentException(e);
                            }
                            if (rigaDB.getDettaglioContratto() != null) {
                                Dettaglio_contrattoBulk dettaglio_contrattoDB = (Dettaglio_contrattoBulk) getTempHome(aUC, Dettaglio_contrattoBulk.class).findByPrimaryKey(rigaDB.getDettaglioContratto());
                                if (dettaglio_contrattoDB.equalsByPrimaryKey(riga.getDettaglioContratto())) {
                                    gestioneAggiornamentoDettaglioContratto(dettagliContrattoDaAggiornare, riga.getDettaglioContratto(), riga.getImTotaleRiga().subtract(rigaDB.getImTotaleRiga()), riga.getQuantitaConsegneColl().subtract(rigaDB.getQuantitaConsegneColl()));
                                } else {
                                    gestioneAggiornamentoDettaglioContratto(dettagliContrattoDaAggiornare, rigaDB.getDettaglioContratto(), rigaDB.getImTotaleRiga().negate(), rigaDB.getQuantitaConsegneColl().negate());
                                    gestioneAggiornamentoDettaglioContratto(dettagliContrattoDaAggiornare, riga.getDettaglioContratto(), riga.getImTotaleRiga(), riga.getQuantitaConsegneColl());
                                }
                            } else {
                                gestioneAggiornamentoDettaglioContratto(dettagliContrattoDaAggiornare, riga.getDettaglioContratto(), riga.getImTotaleRiga(), riga.getQuantitaConsegneColl());
                            }
                        }
                    }
                    for (Dettaglio_contrattoBulk dettaglioContratto : dettagliContrattoDaAggiornare) {
                        if (dettaglioContratto.getCdBeneServizio() != null) {
                            if (dettaglioContratto.getQuantitaMin() != null && Utility.nvl(dettaglioContratto.getQuantitaOrdinata()).compareTo(dettaglioContratto.getQuantitaMin()) < 0)
                                throw handleException(new ApplicationException("La quantità minima ordinabile sul contratto per l'articolo " + dettaglioContratto.getCdBeneServizio() + " è di " + dettaglioContratto.getQuantitaMin()));
                            if (dettaglioContratto.getQuantitaMax() != null && Utility.nvl(dettaglioContratto.getQuantitaOrdinata()).compareTo(dettaglioContratto.getQuantitaMax()) > 0)
                                throw handleException(new ApplicationException("Sfondamento di " + dettaglioContratto.getQuantitaOrdinata().subtract(dettaglioContratto.getQuantitaMax()) + " della quantità massima ordinabile sul contratto per l'articolo " + dettaglioContratto.getCdBeneServizio()));
                        }
                        super.updateBulk(aUC, dettaglioContratto);
                    }
                }
            } catch (IntrospectionException | PersistencyException e1) {
                throw new it.cnr.jada.comp.ComponentException(e1);
            }
        }
    }

    private void gestioneAggiornamentoDettaglioContratto(List<Dettaglio_contrattoBulk> dettagliContrattoDaAggiornare, Dettaglio_contrattoBulk dettaglio_contratto, BigDecimal importoDaAggiungere, BigDecimal quantitaDaAggiungere) {
        boolean contrattoTrovato = false;
        for (Dettaglio_contrattoBulk dettaglio_contrattoBulk : dettagliContrattoDaAggiornare) {
            if (dettaglio_contratto.equalsByPrimaryKey(dettaglio_contrattoBulk)) {
                dettaglio_contrattoBulk.setImportoOrdinato(Utility.nvl(dettaglio_contrattoBulk.getImportoOrdinato()).add(importoDaAggiungere));
                if (dettaglio_contrattoBulk.getCdBeneServizio() != null) {
                    dettaglio_contrattoBulk.setQuantitaOrdinata(Utility.nvl(dettaglio_contrattoBulk.getQuantitaOrdinata()).add(quantitaDaAggiungere));
                }
                contrattoTrovato = true;
            }
        }
        if (!contrattoTrovato) {
            dettaglio_contratto.setImportoOrdinato(Utility.nvl(dettaglio_contratto.getImportoOrdinato()).add(importoDaAggiungere));
            if (dettaglio_contratto.getCdBeneServizio() != null) {
                dettaglio_contratto.setQuantitaOrdinata(Utility.nvl(dettaglio_contratto.getQuantitaOrdinata()).add(quantitaDaAggiungere));
            }
            dettaglio_contratto.setToBeUpdated();
            dettagliContrattoDaAggiornare.add(dettaglio_contratto);
        }
    }

    public void verificaCoperturaContratto(UserContext aUC, OrdineAcqBulk ordine) throws ComponentException {
        verificaCoperturaContratto(aUC, ordine, MODIFICA);
    }

    public void eliminaConBulk(UserContext userContext, OggettoBulk bulk) throws ComponentException {
        try {
            OrdineAcqBulk ordine = ( OrdineAcqBulk)  bulk;
            if (!ordine.isStatoInserito())
                throw new ApplicationException("Non è possibile cancellare un ordine in stato diverso da inserito");

            for (OrdineAcqRigaBulk riga : ordine.getRigheOrdineColl()) {
                if (riga.getDspObbligazioneScadenzario() != null && riga.getDspObbligazioneScadenzario().getPg_obbligazione() != null)
                    throw new ApplicationException("Scollegare prima gli impegni collegati all'ordine prima di procedere alla cancellazione.");
                for (OrdineAcqConsegnaBulk cons : riga.getRigheConsegnaColl()) {
                     if (cons.getObbligazioneScadenzario() != null && cons.getObbligazioneScadenzario().getPg_obbligazione() != null)
                        throw new ApplicationException("Scollegare prima gli impegni collegati all'ordine prima di procedere alla cancellazione.");
                }
            }
            ordine.cancellaLogicamente();
            ordine.setToBeUpdated();
            this.modificaConBulk(userContext, ordine);
        } catch (Exception e) {
            throw new ComponentException(e);
        }
    }


    public Unita_organizzativaBulk recuperoUoPerImpegno
    (
            UserContext aUC,
            OrdineAcqConsegnaBulk consegna)
            throws ComponentException, PersistencyException {

        if (!isUoImpegnoDaUopDestinazione(aUC)) {
            return recuperoUoOrdinante(aUC, consegna);
        } else {
            if (!consegna.isToBeDeleted()) {
                if (!consegna.isConsegnaMagazzino()) {
                    if (consegna.getCdUopDest() != null) {
                        UnitaOperativaOrdBulk uop = recuperoUopDest(aUC, consegna);
                        if (uop != null) {
                            return uop.getUnitaOrganizzativa();
                        }
                    } else {
                        throw new ApplicationException("E' necessario indicare l'unità operativa per la consegna " + consegna.getConsegna() + " della riga " + consegna.getRiga());
                    }
                } else {
                    if (consegna.getCdMagazzino() != null && consegna.getCdCdsMag() != null) {
                        MagazzinoBulk magazzino = recuperoMagazzino(aUC, consegna);
                        if (magazzino != null && magazzino.getCdUnitaOperativa() != null) {
                            UnitaOperativaOrdBulk uop = recuperoUop(aUC, magazzino.getUnitaOperativaOrd());
                            if (uop != null) {
                                return uop.getUnitaOrganizzativa();
                            }
                        } else {
                            return recuperoUoOrdinante(aUC, consegna);
                        }
                    } else {
                        throw new ApplicationException("E' necessario indicare il magazzino per la consegna " + consegna.getConsegna() + " della riga " + consegna.getRiga());
                    }
                }
            }
        }

        return null;
    }

    private Unita_organizzativaBulk recuperoUoOrdinante(UserContext aUC, OrdineAcqConsegnaBulk consegna)
            throws ComponentException, PersistencyException {
        UnitaOperativaOrdBulk uop = recuperoUop(aUC, consegna.getOrdineAcqRiga().getOrdineAcq().getUnitaOperativaOrd());
        if (uop != null) {
            return uop.getUnitaOrganizzativa();
        }
        throw new ApplicationException("Non è stato possibile recuperare l'unita' organizzativa ordinante");
    }

    public AbilitazioneOrdiniAcqBulk initializeAbilitazioneOrdiniAcq(UserContext usercontext, AbilitazioneOrdiniAcqBulk abilitazioneOrdiniAcqBulk) throws PersistencyException, ComponentException {
        AbilitazioneOrdiniAcqHome abilitazioneOrdiniAcqHome = (AbilitazioneOrdiniAcqHome) getHome(usercontext, abilitazioneOrdiniAcqBulk.getClass());
        UnitaOperativaOrdHome unitaOperativaHome = (UnitaOperativaOrdHome) getHome(usercontext, UnitaOperativaOrdBulk.class);

        SQLBuilder sqlUop = abilitazioneOrdiniAcqHome.selectUnitaOperativaAbilitataByClause(usercontext, abilitazioneOrdiniAcqBulk,
                unitaOperativaHome, new UnitaOperativaOrdBulk(), new CompoundFindClause());
        List<UnitaOperativaOrdBulk> listUop = unitaOperativaHome.fetchAll(sqlUop);
        abilitazioneOrdiniAcqBulk.setUnitaOperativaAbilitata(Optional.ofNullable(listUop)
                .map(e -> {
                    if ((long) e.size() > 1) {
                        return null;
                    }
                    return e.stream().findFirst().orElse(null);
                }).orElse(null));


        MagazzinoHome magazzinoHome = (MagazzinoHome) getHome(usercontext, MagazzinoBulk.class);
        SQLBuilder sqlMagazzino = abilitazioneOrdiniAcqHome.selectMagazzinoAbilitatoByClause(usercontext, abilitazioneOrdiniAcqBulk,
                magazzinoHome, new MagazzinoBulk(), new CompoundFindClause());
        List<MagazzinoBulk> listMagazzino = magazzinoHome.fetchAll(sqlMagazzino);
        abilitazioneOrdiniAcqBulk.setMagazzinoAbilitato(Optional.ofNullable(listMagazzino)
                .map(e -> {
                    if ((long) e.size() > 1) {
                        return null;
                    }
                    return e.stream().findFirst().orElse(null);
                }).orElse(null));

        return abilitazioneOrdiniAcqBulk;
    }

    private SQLBuilder builderRicercaOrdineFromUserLogged(UserContext userContext, String tipoSelesione) throws ComponentException {

        OrdineAcqConsegnaHome ordineAcqConsegnaHome = (OrdineAcqConsegnaHome) getHome(userContext, OrdineAcqConsegnaBulk.class,
                Optional.ofNullable(tipoSelesione)
                        .filter(s -> s.equalsIgnoreCase(ParametriSelezioneOrdiniAcqBP.VIS_ORDINI_RIGA_CONS))
                        .map(s -> "CONSULTAZIONE_DATI_ORDINI_COMPLETI")
                        .orElse("default")
        );
        SQLBuilder sql = ordineAcqConsegnaHome.createSQLBuilder();
        sql.addClause(FindClause.AND, "stato", SQLBuilder.NOT_EQUALS, OrdineAcqConsegnaBulk.STATO_ANNULLATA);
        sql.generateJoin(OrdineAcqConsegnaBulk.class, OrdineAcqRigaBulk.class, "ordineAcqRiga", "ORDINE_ACQ_RIGA");
        sql.generateJoin(OrdineAcqRigaBulk.class, OrdineAcqBulk.class, "ordineAcq", "ORDINE_ACQ");
        sql.generateJoin(OrdineAcqRigaBulk.class, Bene_servizioBulk.class, "beneServizio", "BENE_SERVIZIO");
        sql.generateJoin(OrdineAcqRigaBulk.class, UnitaMisuraBulk.class, "unitaMisura", "UNITA_MISURA");

        if (ParametriSelezioneOrdiniAcqBP.VIS_ORDINI_RIGA_CONS.equalsIgnoreCase(tipoSelesione)) {
            Unita_organizzativa_enteHome home = (Unita_organizzativa_enteHome) getHome(userContext, Unita_organizzativa_enteBulk.class);
            Boolean isUoEnte = home.isUoEnte(userContext);
            if (!isUoEnte) {
                boolean uteAbilOrdine = ((AbilUtenteUopOperHome) getHome(userContext, AbilUtenteUopOperBulk.class)).isUtenteAbilitatoTipoOperazione(userContext, TipoOperazioneOrdBulk.OPERAZIONE_ORDINE);
                if (uteAbilOrdine) {
                    sql.addTableToHeader("ABIL_UTENTE_UOP_OPER", "ABIL_UTENTE_UOP_OPER");
                    sql.addSQLJoin("ORDINE_ACQ.CD_UNITA_OPERATIVA", "ABIL_UTENTE_UOP_OPER.CD_UNITA_OPERATIVA");
                    sql.addSQLClause(FindClause.AND, "ABIL_UTENTE_UOP_OPER.CD_UTENTE", SQLBuilder.EQUALS, userContext.getUser());
                    sql.addSQLClause(FindClause.AND, "ABIL_UTENTE_UOP_OPER.CD_TIPO_OPERAZIONE", SQLBuilder.EQUALS, TipoOperazioneOrdBulk.OPERAZIONE_ORDINE);
                }
            }
        }

        return sql;
    }

    public RemoteIterator ricercaOrdiniAcqCons(UserContext userContext, ParametriSelezioneOrdiniAcqBulk parametri, String tipoSelesione) throws ComponentException {
        SQLBuilder sql = builderRicercaOrdineFromUserLogged(userContext, tipoSelesione);
        sql.addClause(FindClause.AND, "stato", SQLBuilder.NOT_EQUALS, OrdineAcqConsegnaBulk.STATO_ANNULLATA);
        if (ParametriSelezioneOrdiniAcqBP.EVA_FORZATA_ORDINI.equalsIgnoreCase(tipoSelesione)) {
            sql.addClause(FindClause.AND, "stato", SQLBuilder.EQUALS, OrdineAcqConsegnaBulk.STATO_INSERITA);
            sql.addSQLClause(FindClause.AND, "ORDINE_ACQ.STATO", SQLBuilder.EQUALS, OrdineAcqBulk.STATO_DEFINITIVO);
        }

        sql.generateJoin(OrdineAcqRigaBulk.class, Voce_ivaBulk.class, "voceIva", "VOCE_IVA");
        sql.generateJoin(OrdineAcqBulk.class, TerzoBulk.class, "fornitore", "fornitore");


        Optional.ofNullable(parametri.getUnitaOperativaAbilitata()).map(UnitaOperativaOrdBulk::getCdUnitaOperativa)
                .ifPresent(e -> sql.addSQLClause(FindClause.AND, "ORDINE_ACQ.CD_UNITA_OPERATIVA", SQLBuilder.EQUALS, e));


        Optional.ofNullable(parametri.getMagazzinoAbilitato()).map(MagazzinoBulk::getCdMagazzino)
                .ifPresent(e -> {
                    sql.addClause(FindClause.AND, "cdMagazzino", SQLBuilder.EQUALS, e);
                    sql.addClause(FindClause.AND, "cdCdsMag", SQLBuilder.EQUALS, Optional.ofNullable(parametri.getMagazzinoAbilitato().getCdCds()).get());
                });

        Optional.ofNullable(parametri.getNumerazioneOrd()).map(NumerazioneOrdBulk::getCdNumeratore)
                .ifPresent(e -> sql.addClause(FindClause.AND, "cdNumeratore", SQLBuilder.EQUALS, e));


        Optional.ofNullable(parametri.getDaDataOrdine())
                .ifPresent(e -> sql.addSQLClause(FindClause.AND, "ORDINE_ACQ.DATA_ORDINE", SQLBuilder.GREATER_EQUALS, e));

        Optional.ofNullable(parametri.getaDataOrdine())
                .ifPresent(e -> sql.addSQLClause(FindClause.AND, "ORDINE_ACQ.DATA_ORDINE", SQLBuilder.LESS_EQUALS, e));

        Optional.ofNullable(parametri.getDaNumeroOrdine())
                .ifPresent(e -> sql.addClause(FindClause.AND, "numero", SQLBuilder.GREATER_EQUALS, e));

        Optional.ofNullable(parametri.getaNumeroOrdine())
                .ifPresent(e -> sql.addClause(FindClause.AND, "numero", SQLBuilder.LESS_EQUALS, e));

        Optional.ofNullable(parametri.getTerzo()).map(TerzoBulk::getCd_terzo)
                .ifPresent(e -> sql.addSQLClause(FindClause.AND, "ORDINE_ACQ.CD_TERZO", SQLBuilder.EQUALS, e));

        Optional.ofNullable(parametri.getDaDataOrdineDef())
                .ifPresent(e -> sql.addSQLClause(FindClause.AND, "ORDINE_ACQ.DATA_ORDINE_DEF", SQLBuilder.GREATER_EQUALS, e));

        Optional.ofNullable(parametri.getaDataOrdineDef())
                .ifPresent(e -> sql.addSQLClause(FindClause.AND, "ORDINE_ACQ.DATA_ORDINE_DEF", SQLBuilder.LESS_EQUALS, e));

        Optional.ofNullable(parametri.getDaBeneServizio()).map(Bene_servizioBulk::getCd_bene_servizio)
                .ifPresent(e -> sql.addSQLClause(FindClause.AND, "ORDINE_ACQ_RIGA.CD_BENE_SERVIZIO", SQLBuilder.GREATER_EQUALS, e));

        Optional.ofNullable(parametri.getaBeneServizio()).map(Bene_servizioBulk::getCd_bene_servizio)
                .ifPresent(e -> sql.addSQLClause(FindClause.AND, "ORDINE_ACQ_RIGA.CD_BENE_SERVIZIO", SQLBuilder.LESS_EQUALS, e));

        Optional.ofNullable(parametri.getaDataPrevConsegna())
                .ifPresent(e -> sql.addClause(FindClause.AND, "dtPrevConsegna", SQLBuilder.GREATER_EQUALS, e));

        Optional.ofNullable(parametri.getaDataPrevConsegna())
                .ifPresent(e -> sql.addClause(FindClause.AND, "dtPrevConsegna", SQLBuilder.LESS_EQUALS, e));

        Optional.ofNullable(parametri.getUnitaOperativaRicevente()).map(UnitaOperativaOrdBulk::getCdUnitaOrganizzativa)
                .ifPresent(e -> sql.addClause(FindClause.AND, "cdUopDest", SQLBuilder.EQUALS, e));

        Optional.ofNullable(parametri.getTipoConsegna())
                .ifPresent(e -> sql.addClause(FindClause.AND, "tipoConsegna", SQLBuilder.EQUALS, e));

        Optional.ofNullable(parametri.getStatoConsegna())
                .ifPresent(e -> sql.addClause(FindClause.AND, "stato", SQLBuilder.EQUALS, e));

        Optional.ofNullable(parametri.getStatoOrdine())
                .ifPresent(e -> sql.addSQLClause(FindClause.AND, "ORDINE_ACQ.STATO", SQLBuilder.EQUALS, e));


        Optional.ofNullable(parametri.getImpegno()).map(Obbligazione_scadenzarioBulk::getPg_obbligazione)
                .ifPresent(e -> {
                    sql.addClause(FindClause.AND, "pgObbligazione", SQLBuilder.EQUALS, e);
                    sql.addClause(FindClause.AND, "cdCdsObbl", SQLBuilder.EQUALS, Optional.ofNullable(parametri.getImpegno().getCd_cds()).get());
                    sql.addClause(FindClause.AND, "esercizioObbl", SQLBuilder.EQUALS, Optional.ofNullable(parametri.getImpegno().getEsercizio()).get());
                    sql.addClause(FindClause.AND, "esercizioOrigObbl", SQLBuilder.EQUALS, Optional.ofNullable(parametri.getImpegno().getEsercizio_originale()).get());
                });


        Optional.ofNullable(parametri.getContratto()).map(ContrattoBulk::getPg_contratto)
                .ifPresent(e -> {
                    sql.addSQLClause(FindClause.AND, "ORDINE_ACQ.PG_CONTRATTO", SQLBuilder.EQUALS, e);
                    sql.addSQLClause(FindClause.AND, "ORDINE_ACQ.ESERCIZIO_CONTRATTO", SQLBuilder.EQUALS, Optional.ofNullable(parametri.getContratto().getEsercizio()).get());
                    sql.addSQLClause(FindClause.AND, "ORDINE_ACQ.STATO_CONTRATTO", SQLBuilder.EQUALS, Optional.ofNullable(parametri.getContratto().getStato()).get());
                });

        Optional.ofNullable(parametri.getCig()).map(CigBulk::getCdCig).
                ifPresent(e -> {
                    sql.addSQLClause(FindClause.AND, "ORDINE_ACQ.CD_CIG", SQLBuilder.EQUALS, e);
                });

        Optional.ofNullable(parametri.getCup()).map(CupBulk::getCdCup)
                .ifPresent(e -> {
                    sql.addSQLClause(FindClause.AND, "ORDINE_ACQ.CD_CUP", SQLBuilder.EQUALS, e);
                });

        Optional.ofNullable(parametri.getRup()).map(V_persona_fisicaBulk::getCd_terzo)
                .ifPresent(e -> {
                    sql.addSQLClause(FindClause.AND, "ORDINE_ACQ.RESPONSABILE_PROC", SQLBuilder.EQUALS, e);
                });

        Optional.ofNullable(parametri.getTipoOrdine()).map(TipoOrdineBulk::getCdTipoOrdine)
                .ifPresent(e -> {
                    sql.addSQLClause(FindClause.AND, "ORDINE_ACQ.CD_TIPO_ORDINE", SQLBuilder.EQUALS, e);
                });

        return iterator(userContext, sql, OrdineAcqConsegnaBulk.class, null);
    }


    public void chiusuraForzataOrdini(UserContext userContext, OrdineAcqConsegnaBulk ordineEvasioneForzata) throws ComponentException, PersistencyException {

        BulkList<OrdineAcqConsegnaBulk> listaConsegneDaChiudere = new BulkList<>();
        listaConsegneDaChiudere.add(0, ordineEvasioneForzata);

        final Supplier<Stream<OrdineAcqConsegnaBulk>> consegneDaChiudere = () ->
                Optional.of(listaConsegneDaChiudere)
                        .filter(list -> !list.isEmpty())
                        .map(Collection::stream)
                        .orElse(Stream.empty());

        final Map<OrdineAcqBulk, Map<OrdineAcqRigaBulk, List<OrdineAcqConsegnaBulk>>> mapOrdine =
                consegneDaChiudere.get().collect(Collectors.groupingBy(o -> o.getOrdineAcqRiga().getOrdineAcq(),
                        Collectors.groupingBy(OrdineAcqConsegnaBulk::getOrdineAcqRiga)));
        try {
            for (OrdineAcqBulk ordine : mapOrdine.keySet()) {
                try {
                    OrdineAcqBulk ordineComp = (OrdineAcqBulk) inizializzaBulkPerModifica(userContext, ordine);
                    for (OrdineAcqRigaBulk ordineRiga : mapOrdine.get(ordine).keySet()) {
                        //recupero la riga di ordine dall'oggetto proveniente dal Component
                        OrdineAcqRigaBulk ordineRigaComp =
                                Optional.ofNullable(ordineComp.getRigheOrdineColl())
                                        .filter(list -> !list.isEmpty())
                                        .map(list -> list.get(list.indexOfByPrimaryKey(ordineRiga)))
                                        .orElseThrow(() -> new DetailedRuntimeException("Errore nell'individuazione della riga " + ordineRiga.getRigaOrdineString() + "."));

                        //ciclo sulle righe di consegna
                        for (OrdineAcqConsegnaBulk ordineConsegna:mapOrdine.get(ordine).get(ordineRiga)) {
                            //recupero la riga di consegna dall'oggetto proveniente dal Component
                            OrdineAcqConsegnaBulk ordineConsegnaComp =
                                    Optional.ofNullable(ordineRigaComp.getRigheConsegnaColl())
                                            .filter(list -> !list.isEmpty())
                                            .map(list -> list.get(list.indexOfByPrimaryKey(ordineConsegna)))
                                            .orElseThrow(() -> new DetailedRuntimeException("Errore nell'individuazione della consegna " + ordineConsegna.getConsegnaOrdineString() + "."));

                            if (ordineConsegnaComp.getStato().equals(OrdineAcqConsegnaBulk.STATO_EVASA))
                                throw new DetailedRuntimeException("La consegna " + ordineConsegnaComp.getConsegnaOrdineString() + " è stata già evasa");

                            ordineRigaComp.setToBeUpdated();
                            ordineComp.setToBeUpdated();

                            if (Utility.createConfigurazioneCnrComponentSession().isAttivaFinanziaria(userContext, ordineConsegnaComp.getEsercizio())) {
                                if (ordineConsegnaComp.getObbligazioneScadenzario() != null)
                                    ordineConsegnaComp.setStato(OrdineAcqConsegnaBulk.STATO_EVASA_FORZATAMENTE);

                                ordineComp.sostituisciConsegnaFromObbligazioniHash(ordineConsegnaComp);
                                ordineComp.setAggiornaImpegniInAutomatico(true);
                            } else {
                                ordineConsegnaComp.setStato(OrdineAcqConsegnaBulk.STATO_EVASA_FORZATAMENTE);
                                ordineComp.setAggiornaImpegniInAutomatico(false);
                            }

                            //cancello l'analitica sulla consegna da evadere forzatamente
                            for (OrdineAcqConsegnaEcoBulk consegnaEco : ordineConsegnaComp.getRigheEconomica())
                                consegnaEco.setToBeDeleted();

                            ordineConsegnaComp.setToBeUpdated();
                            ordineComp.setVerificaContratto(false);
                        }
                    }
                    modificaConBulk(userContext, ordineComp);
                } catch (ComponentException e) {
                    throw new DetailedRuntimeException(e);
                }
            }
        } catch (DetailedRuntimeException | RemoteException _ex) {
            throw handleException(_ex);
        }
    }

    public SQLBuilder selectNumerazioneOrdByClause(UserContext userContext, OggettoBulk bulk, NumerazioneOrdBulk numerazioneOrdBulk, CompoundFindClause clauses) throws ComponentException {

        OrdineAcqHome home = (OrdineAcqHome) getHome(userContext, OrdineAcqBulk.class);
        NumerazioneOrdHome numerazioneHome = (NumerazioneOrdHome) getHome(userContext, NumerazioneOrdBulk.class);
        SQLBuilder sql = null;
        try {
            if (bulk instanceof OrdineAcqBulk)
                sql = home.selectNumerazioneOrdByClause(userContext, (OrdineAcqBulk) bulk, numerazioneHome, new NumerazioneOrdBulk(), new CompoundFindClause());
            else if (bulk instanceof ParametriSelezioneOrdiniAcqBulk)
                sql = home.selectNumerazioneOrdByClause(userContext, (ParametriSelezioneOrdiniAcqBulk) bulk, numerazioneHome, new NumerazioneOrdBulk(), new CompoundFindClause());
        } catch (PersistencyException e) {
            throw new ComponentException(e);
        }
        sql.addClause(clauses);
        sql.addOrderBy("cd_numeratore");
        return sql;
    }

    public SQLBuilder selectContoBulkByClause(UserContext userContext, OrdineAcqConsegnaBulk bulk, ContoBulk contoBulk, CompoundFindClause clauses) throws PersistencyException, ComponentException {
        try {
            ContoHome home = (ContoHome) getHome(userContext, ContoBulk.class);
            return home.selectContiAssociatiACategoria(new CompoundFindClause(), bulk.getEsercizio() == null ? CNRUserContext.getEsercizio(userContext) : bulk.getEsercizio(),
                    bulk.getOrdineAcqRiga().getBeneServizio().getCategoria_gruppo());
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new PersistencyException(e);
        }
    }

    public SQLBuilder selectContoByClause(UserContext userContext, OggettoBulk bulk, ContoBulk contoBulk, CompoundFindClause clauses) throws PersistencyException, ComponentException {
        try {
            ContoHome home = (ContoHome) getHome(userContext, ContoBulk.class);
            OrdineAcqConsegnaBulk consegna = (OrdineAcqConsegnaBulk) bulk;
            return home.selectContiAssociatiACategoria(new CompoundFindClause(), consegna.getEsercizio() == null ? CNRUserContext.getEsercizio(userContext) : consegna.getEsercizio(),
                    consegna.getOrdineAcqRiga().getBeneServizio().getCategoria_gruppo());
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new PersistencyException(e);
        }
    }

    public SQLBuilder selectDspContoByClause(UserContext userContext, OrdineAcqRigaBulk riga,
                                             ContoBulk contoBulk,
                                             CompoundFindClause compoundfindclause) throws PersistencyException, ComponentException {
        ContoHome contoHome = (ContoHome) getHome(userContext, ContoBulk.class);
        SQLBuilder sql = null;
        try {
            sql = contoHome.selectContiAssociatiACategoria(compoundfindclause, riga.getEsercizio() == null ? CNRUserContext.getEsercizio(userContext) : riga.getEsercizio(), riga.getBeneServizio().getCategoria_gruppo());
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new PersistencyException(e);
        }

        return sql;
    }

    public SQLBuilder selectVoce_epByClause(UserContext userContext, OrdineAcqRigaBulk riga,
                                             ContoBulk contoBulk,
                                             CompoundFindClause compoundfindclause) throws PersistencyException, ComponentException {
        return selectDspContoByClause(userContext,riga,contoBulk,compoundfindclause);
    }

    public ContoBulk recuperoContoDefault(UserContext userContext, Categoria_gruppo_inventBulk categoria_gruppo_inventBulk) throws PersistencyException, ComponentException {
        ContoHome contoHome = (ContoHome) getHome(userContext, ContoBulk.class);
        try {
            SQLBuilder sql = contoHome.selectContoDefaultAssociatoACategoria(new CompoundFindClause(), CNRUserContext.getEsercizio(userContext), categoria_gruppo_inventBulk);
            List conti = contoHome.fetchAll(sql);
            if (conti != null && !conti.isEmpty())
                return (ContoBulk) conti.get(0);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new PersistencyException(e);
        }
        return null;
    }

    public Dettaglio_contrattoBulk recuperoDettaglioContratto(UserContext userContext, OrdineAcqRigaBulk riga) throws PersistencyException, ComponentException {
        OrdineAcqRigaHome home = (OrdineAcqRigaHome) getHome(userContext, OrdineAcqRigaBulk.class);
        return home.recuperoDettaglioContratto(riga);
    }

    public java.util.Collection findModalita(UserContext userContext, OggettoBulk bulk) throws ComponentException {
        try {
            OrdineAcqHome home = (OrdineAcqHome) getHome(userContext, OrdineAcqBulk.class);
            return home.findModalita(userContext, (OrdineAcqBulk) bulk);
        } catch (PersistencyException ex) {
            throw handleException(bulk, ex);
        }
    }

    /**
     * Viene richiesta la lista delle Banche associate ad un Terzo
     * <p>
     * Pre-post-conditions:
     * <p>
     * Nome: Terzo NON selezionato
     * Pre: Non è stato selezionato un Terzo per l'ordine
     * Post: Non vengono caricate le banche.
     * <p>
     * Nome: Terzo selezionato
     * Pre: E' stato selezionato un Terzo valido per l'ordine
     * Post: Viene restituita la lista delle Banche associate al Terzo
     *
     * @param userContext lo UserContext che ha generato la richiesta
     * @param ordine      l'OggettoBulk da completare
     * @return La lista delle banche associate al terzo
     **/
    public java.util.List findListabanche(UserContext userContext, OrdineAcqBulk ordine) throws ComponentException {

        try {
            if (ordine.getFornitore() == null)
                return null;
            return getHome(userContext, BancaBulk.class).fetchAll(selectBancaByClause(userContext, ordine, null, null));
        } catch (it.cnr.jada.persistency.PersistencyException ex) {
            throw handleException(ex);
        }
    }

    /**
     * Aggiunge una clausola a tutte le operazioni di ricerca eseguite sulle Banche
     * <p>
     * Nome: Richiesta di ricerca di una banca
     * Pre: E' stata generata la richiesta di ricerca delle banche
     * Post: Viene restituito l'SQLBuilder per filtrare le banche NON CANCELLATE,
     * associate al TERZO selezionato e al TIPO PAGAMENTO selezionato
     **/
    public SQLBuilder selectBancaByClause(UserContext userContext, OrdineAcqBulk ordine, BancaBulk banca, CompoundFindClause clauses) throws ComponentException {

        BancaHome bancaHome = (BancaHome) getHome(userContext, it.cnr.contab.anagraf00.core.bulk.BancaBulk.class);
        return bancaHome.selectBancaFor(ordine.getModalitaPagamento(), ordine.getFornitore().getCd_terzo());
    }

    public SQLBuilder selectVoce_epByClause(UserContext userContext, OrdineAcqBulk ordineAcqBulk, ContoBulk contoBulk, CompoundFindClause clause)  throws ComponentException, EJBException, RemoteException {
        ContoHome contoHome = (ContoHome) getHome(userContext, ContoBulk.class);
        SQLBuilder sql = contoHome.createSQLBuilder();
        sql.addSQLClause(FindClause.AND,"ESERCIZIO",SQLBuilder.EQUALS, CNRUserContext.getEsercizio(userContext));
        sql.addClause(clause);
        return sql;
    }

    public SQLBuilder selectVoce_epByClause(UserContext userContext, OrdineAcqEcoBulk ordineAcqEcoBulk, ContoBulk contoBulk, CompoundFindClause clause)  throws ComponentException, EJBException, RemoteException {
        ContoHome contoHome = (ContoHome) getHome(userContext, ContoBulk.class);
        SQLBuilder sql = contoHome.createSQLBuilder();
        sql.addSQLClause(FindClause.AND,"ESERCIZIO",SQLBuilder.EQUALS, ordineAcqEcoBulk.getEsercizio());
        sql.addClause(clause);

        if (ordineAcqEcoBulk.getVoce_analitica()!=null && ordineAcqEcoBulk.getVoce_analitica().getCd_voce_ana()!=null) {
            SQLBuilder sqlVa = this.selectVoce_analiticaByClause(userContext, ordineAcqEcoBulk, ordineAcqEcoBulk.getVoce_analitica(), null);
            sqlVa.addSQLClause(FindClause.AND, "VOCE_ANALITICA.ESERCIZIO_VOCE_EP=VOCE_EP.ESERCIZIO");
            sqlVa.addSQLClause(FindClause.AND, "VOCE_ANALITICA.CD_VOCE_EP=VOCE_EP.CD_VOCE_EP");
            sql.addSQLExistsClause(FindClause.AND, sqlVa);
        }
        return sql;
    }

    public SQLBuilder selectVoce_analiticaByClause(UserContext userContext, OrdineAcqEcoBulk ordineAcqEcoBulk, Voce_analiticaBulk voceAnaliticaBulk, CompoundFindClause clause)  throws ComponentException, EJBException, RemoteException {
        Voce_analiticaHome voceAnaliticaHome = (Voce_analiticaHome) getHome(userContext, Voce_analiticaBulk.class);
        SQLBuilder sql = voceAnaliticaHome.createSQLBuilder();
        sql.addSQLClause(FindClause.AND,"ESERCIZIO",SQLBuilder.EQUALS, ordineAcqEcoBulk.getEsercizio());

        if (voceAnaliticaBulk!=null && voceAnaliticaBulk.getCd_voce_ana()!=null)
            sql.addSQLClause(FindClause.AND,"CD_VOCE_ANA",SQLBuilder.EQUALS, voceAnaliticaBulk.getCd_voce_ana());

        if (ordineAcqEcoBulk.getVoce_ep()!=null && ordineAcqEcoBulk.getVoce_ep().getCd_voce_ep()!=null) {
            sql.addSQLClause(FindClause.AND,"ESERCIZIO_VOCE_EP",SQLBuilder.EQUALS, ordineAcqEcoBulk.getVoce_ep().getEsercizio());
            sql.addSQLClause(FindClause.AND,"CD_VOCE_EP",SQLBuilder.EQUALS, ordineAcqEcoBulk.getVoce_ep().getCd_voce_ep());
        }

        if (clause!=null)
            sql.addClause(clause);
        return sql;
    }

    public SQLBuilder selectVoce_analiticaByClause(UserContext userContext, OrdineAcqRigaEcoBulk ordineAcqRigaEcoBulk, Voce_analiticaBulk voceAnaliticaBulk, CompoundFindClause clause)  throws ComponentException, EJBException, RemoteException {
        Voce_analiticaHome voceAnaliticaHome = (Voce_analiticaHome) getHome(userContext, Voce_analiticaBulk.class);
        SQLBuilder sql = voceAnaliticaHome.createSQLBuilder();
        sql.addSQLClause(FindClause.AND,"ESERCIZIO",SQLBuilder.EQUALS, CNRUserContext.getEsercizio(userContext));
        if (ordineAcqRigaEcoBulk.getOrdineAcqRiga()!=null && ordineAcqRigaEcoBulk.getOrdineAcqRiga().getVoce_ep()!= null &&
                ordineAcqRigaEcoBulk.getOrdineAcqRiga().getEsercizio_voce_ep()!=null &&
                ordineAcqRigaEcoBulk.getOrdineAcqRiga().getCd_voce_ep()!=null ) {
            sql.addSQLClause(FindClause.AND, "ESERCIZIO_VOCE_EP", SQLBuilder.EQUALS, ordineAcqRigaEcoBulk.getOrdineAcqRiga().getEsercizio_voce_ep());
            sql.addSQLClause(FindClause.AND, "CD_VOCE_EP", SQLBuilder.EQUALS, ordineAcqRigaEcoBulk.getOrdineAcqRiga().getCd_voce_ep());
        } else
            sql.addSQLClause(FindClause.AND, "1!=1");
        sql.addClause(clause);
        return sql;
    }

    public SQLBuilder selectLinea_attivitaByClause(UserContext userContext, OrdineAcqEcoBulk ordineAcqEcoBulk, WorkpackageBulk latt, CompoundFindClause clauses ) throws ComponentException
    {
        SQLBuilder sql = getHome( userContext, latt.getClass(),"V_LINEA_ATTIVITA_VALIDA" ).createSQLBuilder();
        sql.addTableToHeader( "CDR");
        sql.addSQLJoin( "CDR.CD_CENTRO_RESPONSABILITA", "V_LINEA_ATTIVITA_VALIDA.CD_CENTRO_RESPONSABILITA" );
        sql.addSQLClause( FindClause.AND, "V_LINEA_ATTIVITA_VALIDA.ESERCIZIO", SQLBuilder.EQUALS, ((it.cnr.contab.utenze00.bp.CNRUserContext)userContext).getEsercizio());
        sql.addSQLClause( FindClause.AND, "CDR.CD_UNITA_ORGANIZZATIVA", SQLBuilder.EQUALS, ((it.cnr.contab.utenze00.bp.CNRUserContext)userContext).getCd_unita_organizzativa());
        sql.addClause( clauses );
        return sql;
    }

    public SQLBuilder selectLinea_attivitaByClause(UserContext userContext, OrdineAcqRigaEcoBulk ordineAcqRigaEcoBulk, WorkpackageBulk latt, CompoundFindClause clauses ) throws ComponentException
    {
        SQLBuilder sql = getHome( userContext, latt.getClass(),"V_LINEA_ATTIVITA_VALIDA" ).createSQLBuilder();
        sql.addTableToHeader( "CDR");
        sql.addSQLJoin( "CDR.CD_CENTRO_RESPONSABILITA", "V_LINEA_ATTIVITA_VALIDA.CD_CENTRO_RESPONSABILITA" );
        sql.addSQLClause( FindClause.AND, "V_LINEA_ATTIVITA_VALIDA.ESERCIZIO", SQLBuilder.EQUALS, ((it.cnr.contab.utenze00.bp.CNRUserContext)userContext).getEsercizio());
        sql.addSQLClause( FindClause.AND, "CDR.CD_UNITA_ORGANIZZATIVA", SQLBuilder.EQUALS, ((it.cnr.contab.utenze00.bp.CNRUserContext)userContext).getCd_unita_organizzativa());
        sql.addClause( clauses );
        return sql;
    }

    private void aggiornaAnaliticaConsegne(UserContext userContext, OrdineAcqBulk ordine) throws ComponentException {
        try {
            for (OrdineAcqRigaBulk rigaOrdine:ordine.getRigheOrdineColl()) {
                for (OrdineAcqConsegnaBulk consegnaOrdine:rigaOrdine.getRigheConsegnaColl()) {
                    //prima cancello le eventuali righe eco presenti
                    for (OrdineAcqConsegnaEcoBulk consEco : consegnaOrdine.getRigheEconomica()) {
                        consEco.setToBeDeleted();
                        deleteBulk(userContext, consEco);
                    }
                    consegnaOrdine.setRigheEconomica(new BulkList<>());
                }
                //quindi inserisco
                Map<OggettoBulk,BigDecimal> source = new HashMap<>();
                for (OrdineAcqRigaEcoBulk rigaEco : rigaOrdine.getRigheEconomica())
                    source.put(rigaEco,rigaEco.getImporto());

                Map<OggettoBulk,BigDecimal> destination = new HashMap<>();
                for (OrdineAcqConsegnaBulk rigaCons : rigaOrdine.getRigheConsegnaColl())
                    destination.put(rigaCons,rigaCons.getImCostoEco());

                Map<OggettoBulk,Map<OggettoBulk,BigDecimal>> result = Utility.spalmaImporti(source,destination);

                for (OggettoBulk obj : result.keySet()) {
                    OrdineAcqConsegnaBulk rigaCons = (OrdineAcqConsegnaBulk)obj;
                    Map<OggettoBulk,BigDecimal> result2 = result.get(obj);
                    for (OggettoBulk obj2 : result2.keySet()) {
                        OrdineAcqRigaEcoBulk rigaEco = (OrdineAcqRigaEcoBulk)obj2;
                        OrdineAcqConsegnaEcoBulk consEco = new OrdineAcqConsegnaEcoBulk();
                        consEco.setOrdineAcqConsegna(rigaCons);
                        consEco.setVoce_analitica(rigaEco.getVoce_analitica());
                        consEco.setLinea_attivita(rigaEco.getLinea_attivita());
                        consEco.setImporto(result2.get(obj2));
                        consEco.setToBeCreated();
                        rigaCons.getRigheEconomica().add(consEco);
                    }
                }
            }
        } catch (it.cnr.jada.persistency.PersistencyException ex) {
            throw handleException(ex);
        }
    }
}

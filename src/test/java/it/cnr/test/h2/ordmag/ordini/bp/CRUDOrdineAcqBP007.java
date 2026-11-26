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

package it.cnr.test.h2.ordmag.ordini.bp;

import it.cnr.test.h2.utenze.action.ActionDeployments;
import it.cnr.test.util.AlertMessage;
import it.cnr.test.util.SharedResource;
import it.cnr.test.util.StopOnFirstFailure;
import org.jboss.arquillian.graphene.GrapheneElement;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Test di REVERSE CHARGE:
 * 1) creazione ordine commerciale con 1 riga consegna con 3 beni inventariali con codice iva 22E non detraibile
 * 2) evasione riga consegna di tutti e 3 beni inventariali
 * 3) verifica inserimento beni in transito e completamento per successiva inventariazione del primo bene
 * 4) Inventariazione primo bene
 * 5) registrazione fattura con riscontro valore su riga consegna dei 3 beni di cui uno inventariato e 2 in transito. Cambio prezzo a 140.
 * 6) Verifica che il valore del bene inventariato si sia aggiornato dopo il riscontro a valore.
 * 7) Completamento per successiva inventariazione del secondo bene di cui risulta già pervenuta fattura. Verifica che il valore del bene in transito si sia aggiornato dopo il riscontro a valore.
 * 8) Inventariazione secondo bene di cui risulta già pervenuta fattura.
 * 9) Verifica che il valore del secondo bene inventariato sia corretto con quello rideterminato con il riscontro a valore..
 * 10) verifica scritture economica/analitica su consegna e movimenti di magazzino
 */
@ExtendWith(StopOnFirstFailure.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Order(Order.DEFAULT + 7) // dopo le classi senza @Order, come con la vecchia suite
public class CRUDOrdineAcqBP007 extends ActionDeployments {
    private static final SharedResource sharedResource = new SharedResource();

    public static final String USERNAME = "ENTETEST";
    public static final String PASSWORD = "PASSTEST";

    public static final String ORD = "0.ORD";
    public static final String ORD_ORDACQ = "0.ORD.ORDACQ";
    public static final String ORD_ORDACQ_M = "0.ORD.ORDACQ.M";
    public static final String ORD_EVAORD = "0.ORD.EVAORD";

    public static final String ORD_CON = "0.ORD.CON";
    public static final String ORD_CON_VISORDCONSEGNA = "0.ORD.CON.VISORDCONSEGNA";

    public static final String AMM = "0.AMM";
    public static final String AMM_FATTUR = "0.AMM.FATTUR";
    public static final String AMM_FATTUR_FATPAS = "0.AMM.FATTUR.FATPAS";
    public static final String AMM_FATTUR_FATPAS_ELE = "0.AMM.FATTUR.FATPAS.ELE";

    public static final String AMM_INVENT = "0.AMM.INVENT";
    public static final String AMM_INVENT_ORDINI = "0.AMM.INVENT.ORDINI";
    public static final String AMM_INVENT_ORDINI_TRANSITO = "0.AMM.INVENT.ORDINI.BENI_TRANSITO";
    public static final String AMM_INVENT_ORDINI_INVENTARIAZIONE = "0.AMM.INVENT.ORDINI.INVENTARIAZIONE";
    public static final String AMM_INVENT_GESTBE = "0.AMM.INVENT.GESTBE";
    public static final String AMM_INVENT_GESTBE_M = "0.AMM.INVENT.GESTBE.M";


    public static final String MAG = "0.MAG";
    public static final String MAG_ANNULLAMENTO = "0.MAG.ANNULLAMENTO";
    public static final String MAG_ANNULLAMENTO_M = "0.MAG.ANNULLAMENTO.GESTIONE";

    public static final String CD_UNITA_OPERATIVA = "DRUE";
    public static final String CD_NUMERATORE = "DSC";
    public static final String CD_MAGAZZINO = "PT";
    public static final String UO = "000.000";
    public static final String CDR = "000.000.000";

    public static final String CONTRATTO_ESERCIZIO = "2025";
    public static final String CONTRATTO_NUMERO = "3";

    public static final String BENE_SERVIZIO_CODICE_01 = "19001";

    public static final String DATA_ODIERNA = "25102025";

    @Test
    @Order(1)
    public void testLogin() throws Exception {
        doLogin(USERNAME, PASSWORD);
        doLoginUO(UO, CDR);
    }

    /**
     * 1) creazione ordine commerciale con 1 riga consegna con 3 beni inventariali con codice iva 22E non detraibile
     */
    @Test
    @Order(2)
    public void testCreaOrdine() {
        switchToFrameMenu();
        doApriMenu(ORD);
        doApriMenu(ORD_ORDACQ);
        doSelezionaMenu(ORD_ORDACQ_M);

        switchToFrameWorkspace();

        // Indico tipo commerciale
        Select select = new Select(getGrapheneElement("main.tiAttivita"));
        select.selectByValue("C");

        //Indico Unità Operativa: DRUE
        doWriteIntoElement("main.findUnitaOperativaOrd.cdUnitaOperativa", CD_UNITA_OPERATIVA);
        doClickButton("doSearch(main.findUnitaOperativaOrd)");

        //Indico Numeratore: DSA
        doWriteIntoElement("main.findNumerazioneOrd.cdNumeratore", CD_NUMERATORE);
        doClickButton("doSearch(main.findNumerazioneOrd)");

        //Indico Contratto: 2025 1
        doWriteIntoElement("main.find_contratto.esercizio", CONTRATTO_ESERCIZIO);
        doWriteIntoElement("main.find_contratto.pg_contratto", CONTRATTO_NUMERO);
        doClickButton("doSearch(main.find_contratto)");

        //Indico nota: Prova per documentazione
        doWriteIntoElement("main.nota", "Prova per documentazione con bene inventariale - Ordine Commerciale");

        //Passo sulla tab ‘Fornitore’ ed il terzo è proposto in automatico
        doClickButton("doTab('tab','tabOrdineFornitore')");
        Assertions.assertEquals("101", getGrapheneElement("main.findFornitore.cd_terzo").getAttribute("value"));

        //Passo sulla tab ‘Dettaglio’
        doClickButton("doTab('tab','tabOrdineAcqDettaglio')");

        //Inserisco un nuovo dettaglio
        doClickButton("doAddToCRUD(main.Righe)");

        //Indico codice articolo: 19001
        doWriteIntoElement("main.Righe.findBeneServizio.cd_bene_servizio", BENE_SERVIZIO_CODICE_01);
        doClickButton("doSearch(main.Righe.findBeneServizio)");

        //Indico codice iva non detraibile: 22RC6c
        doClickButton("doBlankSearch(main.Righe.voce_iva)");
        doWriteIntoElement("main.Righe.voce_iva.cd_voce_iva", "22RC6c");
        doClickButton("doSearch(main.Righe.voce_iva)");

        //Indico Quantità: 1
        doWriteIntoElement("main.Righe.dspQuantita", "3");
        doClickButton("doOnDspQuantitaChange");

        //Lascio gli importi proposti (Totale ordine: 183,00 – 	Imponibile 150,00 	IVA:33,00)
        Assertions.assertEquals("150,000000", getGrapheneElement("main.Righe.prezzoUnitario").getAttribute("value"));

        // Indico tipo consegna: Magazzino
        select = new Select(getGrapheneElement("main.Righe.tipoConsegna"));
        select.selectByValue("MAG");

        //Lascio la ‘Data prevista consegna’
        Assertions.assertNotNull(getGrapheneElement("main.Righe.dtPrevConsegna").getText());

        //Indico il codice magazzino: PT
        doClickButton("doBlankSearch(main.Righe.findMagazzino)");
        doWriteIntoElement("main.Righe.findMagazzino.cdMagazzino", CD_MAGAZZINO);
        doClickButton("doSearch(main.Righe.findMagazzino)");

        //Passo sulla tab ‘Dati coge/coan’ e verifico che il conto di costo è quello legato alla categoria gruppo del mio articolo (8.1) ed è: A22012.
        //L’analitica correttamente non c’è perchè trattasi di conto patrimoniale
        doClickButton("doTab('tabOrdineAcqDettagli','tabDatiCogeCoan')");

        //Verifico che il conto di costo è quello legato alla categoria gruppo del mio articolo (8.1) ed è: A22012.
        Assertions.assertEquals("A22012", getGrapheneElement("main.Righe.find_voce_ep.cd_voce_ep").getAttribute("value"));

        // L’importo è 549 pari a 183*3 beni
        Assertions.assertEquals("549,00",getGrapheneElement("imCostoEcoDaRipartire").getAttribute("value"));

        //Ritorno sulla prima tab ‘ordine d’acquisto’
        doClickButton("doTab('tab','tabOrdineAcq')");

        //Salvo
        doClickButton("doSalva()");

        //Restituisce messaggio di creazione eseguita
        Assertions.assertEquals(AlertMessage.CREAZIONE_ESEGUITA.value(), handleTextAlert(browser));

        String pgOrdineCreated = getGrapheneElement("main.numero").getAttribute("value");
        sharedResource.setVal01(pgOrdineCreated);

        //modifico lo stato portandolo ‘In Approvazione’
        select = new Select(getGrapheneElement("main.statoForUpdate"));
        select.selectByValue("APP");

        //Salvo
        doClickButton("doSalva()");

        //Restituisce messaggio di salvataggio eseguito
        Assertions.assertEquals(AlertMessage.SALVATAGGIO_ESEGUITO.value(), handleTextAlert(browser));

        //modifico lo stato portandolo ‘Definitivo’
        select.selectByValue("DEF");

        //Salvo
        doClickButton("doSalva()");

        //Restituisce messaggio di assenza obbligazione
        Assertions.assertEquals("Sulla consegna 2025/"+CD_NUMERATORE+"/"+pgOrdineCreated+"/1/1 non è indicata l'obbligazione", handleTextAlert(browser));

        //Ritorno sulla tab delle righe ordine
        doClickButton("doTab('tab','tabOrdineAcqDettaglio')");

        //Seleziono la prima riga
        doSelectTableRow("main.Righe",0);

        //Vado sul pulsante ‘crea/associa impegni’
        doClickButton("doRicercaObbligazione");

        //Sulla mappa di ricerca impegni scelgo di creare un nuovo impegno
        doClickButton("submitForm('doOpenObbligazioniWindow')");

        //specifico solo la voce: 22010
        getGrapheneElement("main.find_elemento_voce.searchtool_cd_elemento_voce").clear();
        doWriteIntoElement("main.find_elemento_voce.searchtool_cd_elemento_voce", "22010");
        doClickButton("submitForm('doSearch(main.find_elemento_voce)')");

        //Entro su ‘disponibilità’
        doClickButton("submitForm('doConsultaInserisciVoce')");

        //Scelgo la prima Gae presentata
        doSelectTableRow("mainTable",0);

        //Conferma la scelta sulla mappa delle disponibilità
        doClickButton("submitForm('doConferma')");

        //Riporto l'impegno sulla riga di consegna
        doClickButton("doRiporta()");

        //Salvo l'ordine
        doClickButton("doSalva()");

        Assertions.assertEquals(AlertMessage.SALVATAGGIO_ESEGUITO.value(), handleTextAlert(browser));
    }

    /**
     * 2) evasione riga consegna di tutti e 3 beni inventariali
     */
    @Test
    @Order(3)
    public void testEvasioneConsegna001() throws IOException {
        switchToFrameMenu();
        doSelezionaMenu(ORD_EVAORD);

        switchToFrameWorkspace();

        //Scelgo Unità operativa: DRUE
        doWriteIntoElement("main.findUnitaOperativaOrd.cdUnitaOperativa", CD_UNITA_OPERATIVA);
        doClickButton("doSearch(main.findUnitaOperativaOrd)");

        //Scelgo Magazzino: PT
        doClickButton("doBlankSearch(main.findMagazzino)");
        doWriteIntoElement("main.findMagazzino.cdMagazzino", CD_MAGAZZINO);
        doClickButton("doSearch(main.findMagazzino)");

        //Data Bolla: Oggi
        doWriteIntoElement("main.dataBolla", DATA_ODIERNA);
        doClickButton("doOnDtBollaChange");

        //Numero Bolla: 7
        doWriteIntoElement("main.numeroBolla", "123");

        //Data Consegna: Oggi
        doWriteIntoElement("main.dataConsegna", DATA_ODIERNA);
        doClickButton("doOnDtConsegnaChange");

        //Eseguo la ricerca
        doClickButton("doCercaConsegneDaEvadere");

        String pgOrdineCreated = sharedResource.getVal01();

        //Trovo le mie due righe di consegna dell’ordine creato con test precedente
        GrapheneElement rowElement1 = null;
        if (getTableColumnElement("main.ConsegneDaEvadere", 0, 1).getText().equals("2025/" + CD_NUMERATORE + "/" + pgOrdineCreated + "/1/1"))
            rowElement1 = getTableRowElement("main.ConsegneDaEvadere", 0);
        else if (getTableColumnElement("main.ConsegneDaEvadere", 1, 1).getText().equals("2025/" + CD_NUMERATORE + "/" + pgOrdineCreated + "/1/1"))
            rowElement1 = getTableRowElement("main.ConsegneDaEvadere", 1);
        else
            Assertions.fail("Riga consegna 1 non individuata");

        assertTableRowAbsent("main.ConsegneDaEvadere",1);

        //Scelgo la prima consegna
        rowElement1.click();
        doSelectTableRow(rowElement1, "main.ConsegneDaEvadere");

        //Salvo
        doClickButton("doSalva()");
        String textAlert = handleTextAlert(browser);
        Assertions.assertEquals("Attenzione: è obbligatorio allegare il Documento di Trasporto (DDT).", textAlert);

        File original = new File("src/test/resources/contratto.pdf");
        File file = new File(original.getParentFile(), "contratto".concat(UUID.randomUUID().toString()).concat(".pdf"));
        Files.copy(original.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        doClickButton("doTab('tab','tabAllegati')");
        doClickButton("doAddToCRUD(main.ArchivioAllegati)");
        Select select = new Select(getGrapheneElement("main.ArchivioAllegati.aspectName"));
        select.selectByValue("P:sigla_evasione_attachment:ddt");
        doWriteIntoElement("main.ArchivioAllegati.descrizione", "TEST");
        getGrapheneElement("main.ArchivioAllegati.file").sendKeys(file.getAbsolutePath());
        getTableRowElement("main.ArchivioAllegati", 0).click();
        doClickButton("doTab('tab','tabEvasioneConsegne')");

        doClickButton("doSalva()");

        Assertions.assertEquals(AlertMessage.OPERAZIONE_EFFETTUATA.value(), handleTextAlert(browser));
    }

    /**
     * 3) verifica inserimento beni in transito e completamento per successiva inventariazione del primo bene
     */
    @Test
    @Order(4)
    public void testVerificaBeneInTransito001() {
        //Verifico che il bene sia in transito
        switchToFrameMenu();
        doApriMenu(AMM);
        doApriMenu(AMM_INVENT);
        doApriMenu(AMM_INVENT_ORDINI);
        doSelezionaMenu(AMM_INVENT_ORDINI_TRANSITO);

        switchToFrameWorkspace();

        //Ricerco l’unico bene in transito
        doClickButton("doCerca()");

        //Trovo le mie due righe di consegna dell’ordine creato con liquibase 2024/DSA/1/1/2
        String pgOrdineCreated = sharedResource.getVal01();

        GrapheneElement rowElement=null;
        for (int riga = 0; riga < 10; riga++) {
            try {
                if (getTableColumnElementFast("mainTable", riga, 11).getText().equals("2025") &&
                        getTableColumnElementFast("mainTable", riga, 12).getText().equals(CD_NUMERATORE) &&
                        getTableColumnElementFast("mainTable", riga, 13).getText().equals(pgOrdineCreated)) {
                    rowElement = getTableRowElementFast("mainTable", riga);
                    break;
                }
            } catch (RuntimeException ignored) {
            }
        }

        //Scelgo la consegna
        rowElement.click();

        //Modifico il campo condizioni bene mettendolo a Ottimo (0)
        Select select = new Select(getGrapheneElement("main.condizioni"));
        select.selectByValue("0");

        //Valorizzo il campo ubicazione con il valore 1
        doClickButton("doBlankSearch(main.find_ubicazione)");
        doWriteIntoElement("main.find_ubicazione.cd_ubicazione", "1");
        doClickButton("doSearch(main.find_ubicazione)");

        //Valorizzo il campo Assegnatario con il valore 1
        doClickButton("doBlankSearch(main.findAssegnatario)");
        doWriteIntoElement("main.findAssegnatario.cd_terzo", "103");
        doClickButton("doSearch(main.findAssegnatario)");

        //Salvo
        doClickButton("doSalva()");

        Assertions.assertEquals(AlertMessage.SALVATAGGIO_ESEGUITO.value(), handleTextAlert(browser));

        //Il transito del bene deve acquisita stato "Completo"
        Assertions.assertEquals("Completo", getGrapheneElement("main.stato").getAttribute("value"));

        doClickButton("doChiudiForm()");
    }

    /**
     * 4) Inventariazione primo bene
     */
    @Test
    @Order(5)
    public void testInventariazioneBeneInTransito001() {
        //Verifico che il bene sia in transito
        switchToFrameMenu();
        doSelezionaMenu(AMM_INVENT_ORDINI_INVENTARIAZIONE);

        switchToFrameWorkspace();

        String pgOrdineCreated = sharedResource.getVal01();

        //Seleziono il bene messo in transito dalla consegna n. 1
        By locator = By.xpath("//tr" +
                "[contains(normalize-space(td[13]//span),'2025') " +
                "and normalize-space(td[14]//span)='" + CD_NUMERATORE + "' " +
                "and normalize-space(td[15]//span)='" + pgOrdineCreated + "' " +
                "and normalize-space(td[16]//span)='1' " +
                "and normalize-space(td[17]//span)='1']");

        GrapheneElement rowElement = getGrapheneElement(locator);

        rowElement.findElement(By.name("mainTable.selection")).click();

        doClickButton("doInventaria");

        //Data Carico: Oggi
        doWriteIntoElement("main.data_registrazione", DATA_ODIERNA);
        doClickButton("doOnData_registrazioneChange");

        doWriteIntoElement("main.ds_buono_carico_scarico", "Inventariazione di test");

        //Salvo
        doClickButton("doSalva()");

        Assertions.assertEquals(AlertMessage.CREAZIONE_ESEGUITA.value(), handleTextAlert(browser));

        //Passo sulla tab ‘Fornitore’ ed il terzo è proposto in automatico
        doClickButton("doTab('tab','tabCaricoInventarioDettaglio')");

        //Seleziono la prima riga
        doSelectTableRow("main.Dettaglio",0);

        String textCodiceBene = getTableColumnElement("main.Dettaglio",0, 1).getText();
        String cdBeneCreated = textCodiceBene.substring(0,textCodiceBene.lastIndexOf("-"));

        sharedResource.setVal03(cdBeneCreated);

        doClickButton("doChiudiForm()");
        doClickButton("doChiudiForm()");
    }

    /*
     * 5) registrazione fattura con riscontro valore su riga consegna dei 3 beni di cui uno inventariato e 2 in transito. Cambio prezzo a 140.
     */
    @Test
    @Order(6)
    public void testRiscontroValore001() {
        switchToFrameMenu();
        doApriMenu(AMM_FATTUR);
        doApriMenu(AMM_FATTUR_FATPAS);
        doSelezionaMenu(AMM_FATTUR_FATPAS_ELE);

        switchToFrameWorkspace();

        //Ricerco la fattura SDI: 90000000002
        doWriteIntoElement("main.identificativoSdi", "90000000011");

        Select select = new Select(getGrapheneElement("main.statoDocumento"));
        select.selectByValue("");

        doClickButton("doCerca()");

        Assertions.assertEquals(AlertMessage.MESSAGE_RICERCA_MONO_RECORD.value(), handleTextAlert(browser));

        //Clicco sul pulsante ‘Compila fattura’;
        doClickButton("submitForm('doCompilaFattura')");

        //Passo alla maschera di Registrazione Fattura
        getGrapheneElement("comando.doYes").click();

        //Sulla testata fattura indico lo stato ‘Liquidabile
        select = new Select(getGrapheneElement("main.stato_liquidazione"));
        select.selectByValue("LIQ");

        //Sulla testata fattura indico lo stato ‘Liquidabile
        select = new Select(getGrapheneElement("main.ti_istituz_commerc"));
        select.selectByValue("C");

        //Aggiorno l'importo della fattura aumentandola dell'importo iva
        doWriteIntoElement("main.im_totale_fattura", "512,40");
        doClickButton("confirmModalInputChange(this,'main.im_totale_fattura','doCalcolaTotaleFatturaFornitoreInEur')");

        //Verifico che fattura da Ordini=Si
        Assertions.assertTrue(getGrapheneElement("main.flDaOrdini").isSelected());

        doWriteIntoElement("main.ds_fattura_passiva", "RISCONTRO VALORE TEST BENE INVENTARIALE FATTURA COMMERCIALE REVERSE CHARGE");

        //Passo alla tab ‘ordini’
        doClickButton("doTab('tab','tabFatturaPassivaOrdini')");

        //Effettuo la ricerca
        doClickButton("submitForm('doSelezionaOrdini')");

        String pgOrdineCreated = sharedResource.getVal01();

        //Seleziono l'ordine creato
        By locator = By.xpath("//tr" +
                "[contains(normalize-space(td[4]//span),'2025') " +
                "and normalize-space(td[5]//span)='" + CD_NUMERATORE + "' " +
                "and normalize-space(td[6]//span)='" + pgOrdineCreated + "' " +
                "and normalize-space(td[7]//span)='1' " +
                "and normalize-space(td[8]//span)='1']");

        GrapheneElement rowElement = getGrapheneElement(locator);

        rowElement.findElement(By.name("mainTable.selection")).click();

        doClickButton("submitForm('doMultipleSelection')");

        //Ritorno sulla tab ordini, vedo la squadratura di 36,60 euro della fattura (in più) rispetto all’ordine;
        doSelectTableRow("main.Ordini",0);

        //Inserisco nella sezione ‘Rettifiche’ nel campo ‘Prezzzo Unitario’ l’importo: 140,00;
        doWriteIntoElement("main.Ordini.prezzoUnitarioRett", "140,00");
        doClickButton("confirmModalInputChange(this,'main.Ordini.prezzoUnitarioRett','doRettificaConsegna')");

        //Digito ‘Fine riscontro a valore’ e mi sposto sulla tab ‘dettaglio’ dove mi è stato creato il dettaglio pari alla riga ordine (420 imponibile + 92,40 IVA).
        doClickButton("submitForm('doConfermaRiscontroAValore')");

        Assertions.assertEquals(AlertMessage.OPERAZIONE_EFFETTUATA.value(), handleTextAlert(browser));

        doClickButton("doSalva()");

        Assertions.assertEquals(AlertMessage.CREAZIONE_ESEGUITA.value(), handleTextAlert(browser));

        //Vado sulla tab principale
        doClickButton("doTab('tab','tabFatturaPassiva')");

        //Registro il numero fattura creata
        String pgFatturaCreated = getGrapheneElement("main.pg_fattura_passiva").getAttribute("value");
        sharedResource.setVal02(pgFatturaCreated);

        //Vado sulla tab economica per controllare scrittura
        doClickButton("doTab('tab','tabEconomica')");
        Assertions.assertEquals("A00068C", getTableColumnElement("main.Movimenti Dare",0,1).getText());
        Assertions.assertEquals("92,40", getTableColumnElement("main.Movimenti Dare",0,5).getText());

        Assertions.assertEquals("P00047", getTableColumnElement("main.Movimenti Dare",1,1).getText());
        Assertions.assertEquals("420,00", getTableColumnElement("main.Movimenti Dare",1,5).getText());

        assertTableRowAbsent("main.Movimenti Dare",2);

        Assertions.assertEquals("P22012", getTableColumnElement("main.Movimenti Avere",0,1).getText());
        Assertions.assertEquals("420,00", getTableColumnElement("main.Movimenti Avere",0,5).getText());

        Assertions.assertEquals("P00044C", getTableColumnElement("main.Movimenti Avere",1,1).getText());
        Assertions.assertEquals("92,40", getTableColumnElement("main.Movimenti Avere",1,5).getText());

        assertTableRowAbsent("main.Movimenti Avere",2);

        //Vado sulla tab analitica per controllare scrittura che non deve esistere dato che la movimentazione avviene sulla consegna
        doClickButton("doTab('tab','tabAnalitica')");
        assertTableRowAbsent("main.Movimenti Analitici",0);

        doClickButton("doChiudiForm()");
        doClickButton("doChiudiForm()");
    }

    /*
     * 6) Verifica che il valore del bene inventariato si sia aggiornato dopo il riscontro a valore.
     */
    @Test
    @Order(7)
    public void testVerificoValoreInizialeBene001() {
        switchToFrameMenu();
        doApriMenu(AMM_INVENT_GESTBE);
        doSelezionaMenu(AMM_INVENT_GESTBE_M);

        switchToFrameWorkspace();

        doClickButton("doNuovaRicerca()");

        String cdBeneCreated = sharedResource.getVal03();

        doWriteIntoElement("main.nr_inventario", cdBeneCreated);

        doClickButton("doCerca()");

        Assertions.assertEquals(AlertMessage.MESSAGE_RICERCA_MONO_RECORD.value(), handleTextAlert(browser));

        //Verifico che il valore unitario del bene si sia aggiornato a 140
        Assertions.assertEquals("140,00", getGrapheneElement("main.valore_unitario").getAttribute("value"));

        doClickButton("doChiudiForm()");
    }

    /**
     * 7) Completamento per successiva inventariazione del secondo bene di cui risulta già pervenuta fattura.
     *    Verifica che il valore del bene in transito si sia aggiornato dopo il riscontro a valore.
     */
    @Test
    @Order(8)
    public void testVerificaBeneInTransito002() {
        //Verifico che il bene sia in transito
        switchToFrameMenu();
        doSelezionaMenu(AMM_INVENT_ORDINI_TRANSITO);

        switchToFrameWorkspace();

        //Ricerco l’unico bene in transito
        doClickButton("doCerca()");

        //Trovo le mie due righe di consegna dell’ordine creato con liquibase 2024/DSA/1/1/2
        String pgOrdineCreated = sharedResource.getVal01();

        GrapheneElement rowElement=null;
        for (int riga = 0; riga < 10; riga++) {
            try {
                if (getTableColumnElementFast("mainTable", riga, 11).getText().equals("2025") &&
                        getTableColumnElementFast("mainTable", riga, 12).getText().equals(CD_NUMERATORE) &&
                        getTableColumnElementFast("mainTable", riga, 13).getText().equals(pgOrdineCreated)) {
                    rowElement = getTableRowElementFast("mainTable", riga);
                    break;
                }
            } catch (RuntimeException ignored) {
            }
        }

        //Scelgo la consegna
        rowElement.click();

        //Modifico il campo condizioni bene mettendolo a Ottimo (0)
        Select select = new Select(getGrapheneElement("main.condizioni"));
        select.selectByValue("0");

        //Valorizzo il campo ubicazione con il valore 1
        doClickButton("doBlankSearch(main.find_ubicazione)");
        doWriteIntoElement("main.find_ubicazione.cd_ubicazione", "1");
        doClickButton("doSearch(main.find_ubicazione)");

        //Valorizzo il campo Assegnatario con il valore 1
        doClickButton("doBlankSearch(main.findAssegnatario)");
        doWriteIntoElement("main.findAssegnatario.cd_terzo", "103");
        doClickButton("doSearch(main.findAssegnatario)");

        //Verifica che il valore del bene in transito si sia aggiornato dopo il riscontro a valore
        Assertions.assertEquals("140,00", getGrapheneElement("main.valore_iniziale").getAttribute("value"));

        //Salvo
        doClickButton("doSalva()");

        Assertions.assertEquals(AlertMessage.SALVATAGGIO_ESEGUITO.value(), handleTextAlert(browser));

        //Il transito del bene deve acquisita stato "Completo"
        Assertions.assertEquals("Completo", getGrapheneElement("main.stato").getAttribute("value"));

        doClickButton("doChiudiForm()");
    }

    /**
     * 8) Inventariazione secondo bene di cui risulta già pervenuta fattura.
     */
    @Test
    @Order(9)
    public void testInventariazioneBeneInTransito002() {
        //Verifico che il bene sia in transito
        switchToFrameMenu();
        doSelezionaMenu(AMM_INVENT_ORDINI_INVENTARIAZIONE);

        switchToFrameWorkspace();

        String pgOrdineCreated = sharedResource.getVal01();

        //Seleziono il bene messo in transito dalla consegna n. 1
        By locator = By.xpath("//tr" +
                "[contains(normalize-space(td[13]//span),'2025') " +
                "and normalize-space(td[14]//span)='" + CD_NUMERATORE + "' " +
                "and normalize-space(td[15]//span)='" + pgOrdineCreated + "' " +
                "and normalize-space(td[16]//span)='1' " +
                "and normalize-space(td[17]//span)='1']");

        GrapheneElement rowElement = getGrapheneElement(locator);

        rowElement.findElement(By.name("mainTable.selection")).click();

        doClickButton("doInventaria");

        //Data Carico: Oggi
        doWriteIntoElement("main.data_registrazione", DATA_ODIERNA);
        doClickButton("doOnData_registrazioneChange");

        doWriteIntoElement("main.ds_buono_carico_scarico", "Inventariazione di test");

        //Salvo
        doClickButton("doSalva()");

        Assertions.assertEquals(AlertMessage.CREAZIONE_ESEGUITA.value(), handleTextAlert(browser));

        //Passo sulla tab ‘Fornitore’ ed il terzo è proposto in automatico
        doClickButton("doTab('tab','tabCaricoInventarioDettaglio')");

        //Seleziono la prima riga
        doSelectTableRow("main.Dettaglio",0);

        String textCodiceBene = getTableColumnElement("main.Dettaglio",0, 1).getText();
        String cdBeneCreated = textCodiceBene.substring(0,textCodiceBene.lastIndexOf("-"));

        sharedResource.setVal04(cdBeneCreated);

        doClickButton("doChiudiForm()");
        doClickButton("doChiudiForm()");
    }

    /**
     * 9) Verifica che il valore del secondo bene inventariato sia corretto con quello rideterminato con il riscontro a valore..
     */
    @Test
    @Order(10)
    public void testVerificoValoreInizialeBene002() {
        switchToFrameMenu();
        doSelezionaMenu(AMM_INVENT_GESTBE_M);

        switchToFrameWorkspace();

        doClickButton("doNuovaRicerca()");

        String cdBeneCreated = sharedResource.getVal04();

        doWriteIntoElement("main.nr_inventario", cdBeneCreated);

        doClickButton("doCerca()");

        Assertions.assertEquals(AlertMessage.MESSAGE_RICERCA_MONO_RECORD.value(), handleTextAlert(browser));

        //Verifico che il valore unitario del bene si sia aggiornato a 140
        Assertions.assertEquals("140,00", getGrapheneElement("main.valore_unitario").getAttribute("value"));

        doClickButton("doChiudiForm()");
    }

    /**
     * 10) verifica scritture economica/analitica su consegna e movimenti di magazzino
     */
    @Test
    @Order(11)
    public void testVerificaScritturaOrdine001() {
        //Verifico che la scrittura sull'ordine sia stata eseguita correttamente
        switchToFrameMenu();
        doSelezionaMenu(ORD_ORDACQ_M);

        switchToFrameWorkspace();

        //Ricerco l’ordine
        doClickButton("doNuovaRicerca()");

        doClickButton("doBlankSearch(main.findUnitaOperativaOrd)");
        doWriteIntoElement("main.findUnitaOperativaOrd.cdUnitaOperativa", CD_UNITA_OPERATIVA);
        doClickButton("doSearch(main.findUnitaOperativaOrd)");

        doClickButton("doBlankSearch(main.findNumerazioneOrd)");
        doWriteIntoElement("main.findNumerazioneOrd.cdNumeratore", CD_NUMERATORE);
        doClickButton("doSearch(main.findNumerazioneOrd)");

        String pgOrdineCreated = sharedResource.getVal01();
        doWriteIntoElement("main.numero", pgOrdineCreated);

        doClickButton("doCerca()");

        Assertions.assertEquals(AlertMessage.MESSAGE_RICERCA_MONO_RECORD.value(), handleTextAlert(browser));

        //Vado sul dettaglio analitico a livello di Ordine
        doClickButton("doTab('tab','tabOrdineResultDetailEcoCoge')");
        doSelectTableRow("main.Dati Analitici",0);

        Assertions.assertEquals("A22012", getTableColumnElement("main.Dati Analitici",0,1).getText());
        Assertions.assertEquals(" ", getTableColumnElement("main.Dati Analitici",0,2).getText());
        Assertions.assertEquals("000.000.000", getTableColumnElement("main.Dati Analitici",0,3).getText());
        Assertions.assertEquals("PTEST001", getTableColumnElement("main.Dati Analitici",0,4).getText());
        Assertions.assertEquals("420,00", getTableColumnElement("main.Dati Analitici",0,5).getText());
        assertTableRowAbsent("main.Dati Analitici",1);

        //Vado sul dettaglio a livello riga Ordine
        doClickButton("doTab('tab','tabOrdineAcqDettaglio')");
        doSelectTableRow("main.Righe",0);

        doClickButton("doTab('tabOrdineAcqDettagli','tabOrdineRigaResultDetailEcoCoge')");

        Assertions.assertEquals("A22012", getGrapheneElement("main.Righe.find_voce_ep.cd_voce_ep").getAttribute("value"));
        Assertions.assertEquals("420,00", getGrapheneElement("imCostoEcoConsegne").getAttribute("value"));
        Assertions.assertEquals("420,00", getGrapheneElement("imCostoEcoRipartitoConsegne").getAttribute("value"));
        Assertions.assertEquals("0,00", getGrapheneElement("imCostoEcoDaRipartireConsegne").getAttribute("value"));

        doSelectTableRow("main.Righe.Dati Coge/Coan",0);

        Assertions.assertEquals("PTEST001", getTableColumnElement("main.Righe.Dati Coge/Coan",0,1).getText());
        Assertions.assertEquals("000.000.000", getTableColumnElement("main.Righe.Dati Coge/Coan",0,2).getText());
        Assertions.assertEquals("420,00", getTableColumnElement("main.Righe.Dati Coge/Coan",0,3).getText());
        assertTableRowAbsent("main.Righe.Dati Coge/Coan",1);

        //Vado sulla tab ‘consegne’
        doClickButton("doTab('tabOrdineAcqDettagli','tabOrdineConsegna')");

        //Vado sulla tab ‘consegne’ e verifico che la riga consegna n. 1 sia in stato ‘EVASA’ e la consegna
        GrapheneElement rowElement1 = null;
        if (getTableColumnElement("main.Righe.Consegne",0, 1).getText().equals("1"))
            rowElement1 = getTableRowElement("main.Righe.Consegne",0);
        else if (getTableColumnElement("main.Righe.Consegne",1, 1).getText().equals("1"))
            rowElement1 = getTableRowElement("main.Righe.Consegne",1);
        else
            Assertions.fail("Riga consegna 1 non individuata");

        //Verifico che la riga consegna n. 1 sia in stato ‘EVASA’ e la consegna n. 2 ancora in stato INSERITA. Entrambe ‘Non Associate’.
        Assertions.assertEquals("Evasa", getTableColumnElement(rowElement1,5).getText());
        Assertions.assertEquals("Associata Totalmente", getTableColumnElement(rowElement1,6).getText());

        //Mi posiziono sulla prima consegna
        rowElement1.click();

        //Entro su ‘scrittura economica’.
        doClickButton("submitForm('doVisualizzaEconomica');");

        //Seleziono la prima scrittura nata con l'evasione dell'ordine
        getTableRowElement("mainTable",0).click();

        Assertions.assertEquals("Y", getGrapheneElement("main.attiva").getAttribute("value"));
        Assertions.assertEquals("450,00", getGrapheneElement("main.imTotaleDare").getAttribute("value"));
        Assertions.assertEquals("450,00", getGrapheneElement("main.imTotaleAvere").getAttribute("value"));

        //Nella sezione ‘Dare’ è presente correttamente il conto: A22012
        doClickButton("doTab('tab','tabDare')");
        Assertions.assertEquals("A22012", getTableColumnElement("main.MovimentiDare",0,1).getText());
        Assertions.assertEquals("450,00", getTableColumnElement("main.MovimentiDare",0,5).getText());
        assertTableRowAbsent("main.MovimentiDare",1);

        //Nella sezione ‘Avere’ è presente correttamente il conto fatture da ricevere: P00047.
        doClickButton("doTab('tab','tabAvere')");
        Assertions.assertEquals("P00047", getTableColumnElement("main.MovimentiAvere",0,1).getText());
        Assertions.assertEquals("450,00", getTableColumnElement("main.MovimentiAvere",0,5).getText());
        assertTableRowAbsent("main.MovimentiAvere",1);

        doClickButton("doChiudiForm()");

        //Entro nuovamente su ‘scrittura economica’.
        doClickButton("submitForm('doVisualizzaEconomica');");

        //Seleziono la seconda scrittura nata con il riscontro a valore
        getTableRowElement("mainTable",1).click();

        Assertions.assertEquals("Y", getGrapheneElement("main.attiva").getAttribute("value"));
        Assertions.assertEquals("30,00", getGrapheneElement("main.imTotaleDare").getAttribute("value"));
        Assertions.assertEquals("30,00", getGrapheneElement("main.imTotaleAvere").getAttribute("value"));

        //Nella sezione ‘Dare’ è presente correttamente il conto fatture da ricevere: P00047
        doClickButton("doTab('tab','tabDare')");
        Assertions.assertEquals("P00047", getTableColumnElement("main.MovimentiDare",0,1).getText());
        Assertions.assertEquals("30,00", getTableColumnElement("main.MovimentiDare",0,5).getText());
        assertTableRowAbsent("main.MovimentiDare",1);

        //Nella sezione ‘Avere’ è presente correttamente il conto: A22012.
        doClickButton("doTab('tab','tabAvere')");
        Assertions.assertEquals("A22012", getTableColumnElement("main.MovimentiAvere",0,1).getText());
        Assertions.assertEquals("30,00", getTableColumnElement("main.MovimentiAvere",0,5).getText());
        assertTableRowAbsent("main.MovimentiAvere",1);

        doClickButton("doChiudiForm()");

        //Entro su ‘Scrittura analitica’.
        doClickButton("submitForm('doVisualizzaAnalitica');");

        //Restituisce messaggio di "Scrittura Analitica non presente!"
        Assertions.assertEquals("Scrittura Analitica non presente!", handleTextAlert(browser));

        //Entro su ‘Visualizza movimenti magazzino’.
        doClickButton("submitForm('doVisualizzaMovimento');");

        Assertions.assertEquals("C01", getTableColumnElement("mainTable",0,3).getText());
        Assertions.assertEquals("3", getTableColumnElement("mainTable",0,8).getText());
        Assertions.assertEquals("150", getTableColumnElement("mainTable",0,9).getText());

        Assertions.assertEquals("C20", getTableColumnElement("mainTable",1,3).getText());
        Assertions.assertEquals("1", getTableColumnElement("mainTable",1,8).getText());
        Assertions.assertEquals("10", getTableColumnElement("mainTable",1,9).getText());

        assertTableRowAbsent("mainTable",2);

        doClickButton("doChiudiForm()");
    }
}

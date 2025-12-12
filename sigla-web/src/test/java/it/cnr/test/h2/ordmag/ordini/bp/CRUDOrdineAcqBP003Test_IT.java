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
import org.jboss.arquillian.graphene.GrapheneElement;
import org.junit.jupiter.api.*;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;
import org.wildfly.common.Assert;

import java.util.Optional;

/**
 * Test di:
 * 1) creazione ordine con 1 riga consegna con bene inventariale
 * 2) evasione riga consegna
 * 3) verifica scrittura economica/analitica su consegna
 * 4) verifica inserimento bene in transito
 * 5) annullamento evasione riga consegna
 * 6) verifica annullamento bene in transito
 * 7) nuova evasione riga consegna
 * 8) verifica inserimento bene in transito e completamento per successiva inventariazione
 * 9) inventariazione bene in transito
 * 10) annullamento evasione riga consegna non consentita per bene inventariato
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CRUDOrdineAcqBP003Test_IT extends ActionDeployments {
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

    public static final String AMM_FATTUR_FATPAS_M = "0.AMM.FATTUR.FATPAS.M";

    public static final String AMM_INVENT = "0.AMM.INVENT";
    public static final String AMM_INVENT_ORDINI = "0.AMM.INVENT.ORDINI";
    public static final String AMM_INVENT_ORDINI_TRANSITO = "0.AMM.INVENT.ORDINI.BENI_TRANSITO";
    public static final String AMM_INVENT_ORDINI_INVENTARIAZIONE = "0.AMM.INVENT.ORDINI.INVENTARIAZIONE";

    public static final String MAG = "0.MAG";
    public static final String MAG_ANNULLAMENTO = "0.MAG.ANNULLAMENTO";
    public static final String MAG_ANNULLAMENTO_M = "0.MAG.ANNULLAMENTO.GESTIONE";

    public static final String CD_UNITA_OPERATIVA = "DRUE";
    public static final String CD_NUMERATORE = "DSA";
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
     * 1) creazione ordine con 1 riga consegna con bene inventariale
     */
    @Test
    @Order(2)
    public void testCreaOrdine() {
        switchToFrameMenu();
        doApriMenu(ORD);
        doApriMenu(ORD_ORDACQ);
        doSelezionaMenu(ORD_ORDACQ_M);

        switchToFrameWorkspace();

        //Indico Unità Operativa: DRUE
        getGrapheneElement("main.findUnitaOperativaOrd.cdUnitaOperativa").writeIntoElement(CD_UNITA_OPERATIVA);
        doClickButton("doSearch(main.findUnitaOperativaOrd)");

        //Indico Numeratore: DSA
        getGrapheneElement("main.findNumerazioneOrd.cdNumeratore").writeIntoElement(CD_NUMERATORE);
        doClickButton("doSearch(main.findNumerazioneOrd)");

        //Indico Contratto: 2025 1
        getGrapheneElement("main.find_contratto.esercizio").writeIntoElement(CONTRATTO_ESERCIZIO);
        getGrapheneElement("main.find_contratto.pg_contratto").writeIntoElement(CONTRATTO_NUMERO);
        doClickButton("doSearch(main.find_contratto)");

        //Indico nota: Prova per documentazione
        getGrapheneElement("main.nota").writeIntoElement("Prova per documentazione con bene inventariale");

        //Passo sulla tab ‘Fornitore’ ed il terzo è proposto in automatico
        doClickButton("doTab('tab','tabOrdineFornitore')");
        Assertions.assertEquals("101", getGrapheneElement("main.findFornitore.cd_terzo").getAttribute("value"));

        //Passo sulla tab ‘Dettaglio’
        doClickButton("doTab('tab','tabOrdineAcqDettaglio')");

        //Inserisco un nuovo dettaglio
        doClickButton("doAddToCRUD(main.Righe)");

        //Indico codice articolo: 19001
        getGrapheneElement("main.Righe.findBeneServizio.cd_bene_servizio").writeIntoElement(BENE_SERVIZIO_CODICE_01);
        doClickButton("doSearch(main.Righe.findBeneServizio)");

        //Indico Quantità: 1
        getGrapheneElement("main.Righe.dspQuantita").writeIntoElement("1");
        doClickButton("doOnDspQuantitaChange");

        //Lascio gli importi proposti (Totale ordine: 183,00 – 	Imponibile 150,00 	IVA:33,00)
        Assertions.assertEquals("150,000000", getGrapheneElement("main.Righe.prezzoUnitario").getAttribute("value"));

        // Indico tipo consegna: Magazzino
        Select select = new Select(getGrapheneElement("main.Righe.tipoConsegna"));
        select.selectByValue("MAG");

        //Lascio la ‘Data prevista consegna’
        Assertions.assertNotNull(getGrapheneElement("main.Righe.dtPrevConsegna").getText());

        //Indico il codice magazzino: PT
        doClickButton("doBlankSearch(main.Righe.findMagazzino)");
        getGrapheneElement("main.Righe.findMagazzino.cdMagazzino").writeIntoElement(CD_MAGAZZINO);
        doClickButton("doSearch(main.Righe.findMagazzino)");

        //Passo sulla tab ‘Dati coge/coan’ e verifico che il conto di costo è quello legato alla categoria gruppo del mio articolo (8.1) ed è: A22012.
        //L’analitica correttamente non c’è perchè trattasi di conto patrimoniale
        doClickButton("doTab('tabOrdineAcqDettagli','tabDatiCogeCoan')");

        //Verifico che il conto di costo è quello legato alla categoria gruppo del mio articolo (8.1) ed è: A22012.
        Assertions.assertEquals("A22012", getGrapheneElement("main.Righe.find_voce_ep.cd_voce_ep").getAttribute("value"));

        // L’analitica correttamente non c’è perchè trattasi di conto patrimoniale
        Assertions.assertEquals("183,00",getGrapheneElement("imCostoEcoDaRipartire").getAttribute("value"));

        //Ritorno sulla prima tab ‘ordine d’acquisto’
        doClickButton("doTab('tab','tabOrdineAcq')");

        //Salvo
        doClickButton("doSalva()");

        //Restituisce messaggio di creazione eseguita
        Alert alert = browser.switchTo().alert();
        Assertions.assertEquals(AlertMessage.CREAZIONE_ESEGUITA.value(), alert.getText());
        alert.accept();

        String pgOrdineCreated = getGrapheneElement("main.numero").getAttribute("value");
        sharedResource.setVal01(pgOrdineCreated);

        //modifico lo stato portandolo ‘In Approvazione’
        select = new Select(getGrapheneElement("main.statoForUpdate"));
        select.selectByValue("APP");

        //Salvo
        doClickButton("doSalva()");

        //Restituisce messaggio di salvataggio eseguito
        alert = browser.switchTo().alert();
        Assertions.assertEquals(AlertMessage.SALVATAGGIO_ESEGUITO.value(), alert.getText());
        alert.accept();

        //modifico lo stato portandolo ‘Definitivo’
        select.selectByValue("DEF");

        //Salvo
        doClickButton("doSalva()");

        //Restituisce messaggio di assenza obbligazione
        alert = browser.switchTo().alert();
        Assertions.assertEquals("Sulla consegna 2025/"+CD_NUMERATORE+"/"+pgOrdineCreated+"/1/1 non è indicata l'obbligazione", alert.getText());
        alert.accept();

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
        getGrapheneElement("main.find_elemento_voce.searchtool_cd_elemento_voce").writeIntoElement("22010");
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

        alert = browser.switchTo().alert();
        Assertions.assertEquals(AlertMessage.SALVATAGGIO_ESEGUITO.value(), alert.getText());
        alert.accept();
    }

    /**
     * 2) evasione riga consegna
     */
    @Test
    @Order(3)
    public void testEvasioneConsegna001() {
        switchToFrameMenu();
        doSelezionaMenu(ORD_EVAORD);

        switchToFrameWorkspace();

        //Scelgo Unità operativa: DRUE
        getGrapheneElement("main.findUnitaOperativaOrd.cdUnitaOperativa").writeIntoElement(CD_UNITA_OPERATIVA);
        doClickButton("doSearch(main.findUnitaOperativaOrd)");

        //Scelgo Magazzino: PT
        doClickButton("doBlankSearch(main.findMagazzino)");
        getGrapheneElement("main.findMagazzino.cdMagazzino").writeIntoElement(CD_MAGAZZINO);
        doClickButton("doSearch(main.findMagazzino)");

        //Data Bolla: Oggi
        getGrapheneElement("main.dataBolla").writeIntoElement(DATA_ODIERNA);
        doClickButton("doOnDtBollaChange");

        //Numero Bolla: 7
        getGrapheneElement("main.numeroBolla").writeIntoElement("10");

        //Data Consegna: Oggi
        getGrapheneElement("main.dataConsegna").writeIntoElement(DATA_ODIERNA);
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

        Assertions.assertThrows(RuntimeException.class, () -> getTableRowElement("main.ConsegneDaEvadere", 1),"Cannot find Element <tr> with tableName main.ConsegneDaEvadere and numberRow: 1");

        //Scelgo la prima consegna
        rowElement1.click();
        doSelectTableRow(rowElement1, "main.ConsegneDaEvadere");

        //Salvo
        doClickButton("doSalva()");
        Alert alert = browser.switchTo().alert();
        Assertions.assertEquals(AlertMessage.OPERAZIONE_EFFETTUATA.value(), alert.getText());
        alert.accept();
    }

    /**
     * 3) verifica scrittura economica/analitica su consegna
     */
    @Test
    @Order(4)
    public void testVerificaScritturaOrdine001() {
        //Verifico che la scrittura sull'ordine sia stata eseguita correttamente
        switchToFrameMenu();
        doSelezionaMenu(ORD_ORDACQ_M);

        switchToFrameWorkspace();

        //Ricerco l’ordine
        doClickButton("doNuovaRicerca()");

        doClickButton("doBlankSearch(main.findUnitaOperativaOrd)");
        getGrapheneElement("main.findUnitaOperativaOrd.cdUnitaOperativa").writeIntoElement(CD_UNITA_OPERATIVA);
        doClickButton("doSearch(main.findUnitaOperativaOrd)");

        doClickButton("doBlankSearch(main.findNumerazioneOrd)");
        getGrapheneElement("main.findNumerazioneOrd.cdNumeratore").writeIntoElement(CD_NUMERATORE);
        doClickButton("doSearch(main.findNumerazioneOrd)");

        String pgOrdineCreated = sharedResource.getVal01();
        getGrapheneElement("main.numero").writeIntoElement(pgOrdineCreated);

        doClickButton("doCerca()");
        Alert alert = browser.switchTo().alert();
        Assertions.assertEquals(AlertMessage.MESSAGE_RICERCA_MONO_RECORD.value(), alert.getText());
        alert.accept();

        //Vado sul dettaglio analitico a livello di Ordine
        doClickButton("doTab('tab','tabOrdineResultDetailEcoCoge')");
        doSelectTableRow("main.Dati Analitici",0);

        Assertions.assertEquals("A22012", getTableColumnElement("main.Dati Analitici",0,1).getText());
        Assertions.assertEquals(" ", getTableColumnElement("main.Dati Analitici",0,2).getText());
        Assertions.assertEquals("000.000.000", getTableColumnElement("main.Dati Analitici",0,3).getText());
        Assertions.assertEquals("PTEST001", getTableColumnElement("main.Dati Analitici",0,4).getText());
        Assertions.assertEquals("183,00", getTableColumnElement("main.Dati Analitici",0,5).getText());
        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.Dati Analitici",1),"Cannot find Element <tr> with tableName main.Dati Analitici and numberRow: 1");

        //Vado sul dettaglio a livello riga Ordine
        doClickButton("doTab('tab','tabOrdineAcqDettaglio')");
        doSelectTableRow("main.Righe",0);

        doClickButton("doTab('tabOrdineAcqDettagli','tabOrdineRigaResultDetailEcoCoge')");

        Assertions.assertEquals("A22012", getGrapheneElement("main.Righe.find_voce_ep.cd_voce_ep").getAttribute("value"));
        Assertions.assertEquals("183,00", getGrapheneElement("imCostoEcoConsegne").getAttribute("value"));
        Assertions.assertEquals("183,00", getGrapheneElement("imCostoEcoRipartitoConsegne").getAttribute("value"));
        Assertions.assertEquals("0,00", getGrapheneElement("imCostoEcoDaRipartireConsegne").getAttribute("value"));

        doSelectTableRow("main.Righe.Dati Coge/Coan",0);

        Assertions.assertEquals("PTEST001", getTableColumnElement("main.Righe.Dati Coge/Coan",0,1).getText());
        Assertions.assertEquals("000.000.000", getTableColumnElement("main.Righe.Dati Coge/Coan",0,2).getText());
        Assertions.assertEquals("183,00", getTableColumnElement("main.Righe.Dati Coge/Coan",0,3).getText());
        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.Righe.Dati Coge/Coan",1),"Cannot find Element <tr> with tableName main.Righe.Dati Coge/Coan and numberRow: 1");

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
        Assertions.assertEquals("Non Associata", getTableColumnElement(rowElement1,6).getText());

        //Mi posiziono sulla prima consegna
        rowElement1.click();

        //Entro su ‘scrittura economica’.
        doClickButton("submitForm('doVisualizzaEconomica');");

        Assertions.assertEquals("Si", getGrapheneElement("main.attiva").getText());
        Assertions.assertEquals("183,00", getGrapheneElement("main.imTotaleDare").getAttribute("value"));
        Assertions.assertEquals("183,00", getGrapheneElement("main.imTotaleAvere").getAttribute("value"));

        //Nella sezione ‘Dare’ è presente correttamente il conto di costo: C13003
        doClickButton("doTab('tab','tabDare')");
        Assertions.assertEquals("A22012", getTableColumnElement("main.MovimentiDare",0,1).getText());
        Assertions.assertEquals("183,00", getTableColumnElement("main.MovimentiDare",0,5).getText());
        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.MovimentiDare",1),"Cannot find Element <tr> with tableName main.MovimentiDare and numberRow: 1");

        //Nella sezione ‘Avere’ è presente correttamente il conto fatture da ricevere: P00047.
        doClickButton("doTab('tab','tabAvere')");
        Assertions.assertEquals("P00047", getTableColumnElement("main.MovimentiAvere",0,1).getText());
        Assertions.assertEquals("183,00", getTableColumnElement("main.MovimentiAvere",0,5).getText());
        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.MovimentiAvere",1),"Cannot find Element <tr> with tableName main.MovimentiAvere and numberRow: 1");

        doClickButton("doChiudiForm()");
        doClickButton("submitForm('doVisualizzaAnalitica');");

        //Restituisce messaggio di "Scrittura Analitica non presente!"
        alert = browser.switchTo().alert();
        Assertions.assertEquals("Scrittura Analitica non presente!", alert.getText());
        alert.accept();

        doClickButton("doChiudiForm()");
    }

    /**
     * 4) verifica inserimento bene in transito
     */
    @Test
    @Order(5)
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
        Alert alert = browser.switchTo().alert();
        Assertions.assertEquals(AlertMessage.MESSAGE_RICERCA_MONO_RECORD.value(), alert.getText());
        alert.accept();

        Assertions.assertEquals("Inserito", getGrapheneElement("main.stato").getAttribute("value"));

        String pgOrdineCreated = sharedResource.getVal01();
        Assertions.assertEquals("2025/" + CD_NUMERATORE + "/" + pgOrdineCreated + "/1/1", getGrapheneElement("main.ordine").getAttribute("value"));

        //Modifico il campo condizioni bene mettendolo a Ottimo (0)
        Select select = new Select(getGrapheneElement("main.condizioni"));
        select.selectByValue("0");

        //Valorizzo il campo ubicazione con il valore 1
        doClickButton("doBlankSearch(main.find_ubicazione)");
        getGrapheneElement("main.find_ubicazione.cd_ubicazione").writeIntoElement("1");
        doClickButton("doSearch(main.find_ubicazione)");

        //Valorizzo il campo Assegnatario con il valore 1
        doClickButton("doBlankSearch(main.find_assegnatario)");
        getGrapheneElement("main.find_assegnatario.cd_terzo").writeIntoElement("1");
        doClickButton("doSearch(main.find_assegnatario)");

        //Salvo
        doClickButton("doSalva()");
        alert = browser.switchTo().alert();
        Assertions.assertEquals(AlertMessage.SALVATAGGIO_ESEGUITO.value(), alert.getText());
        alert.accept();

        //Il transito del bene deve acquisita stato "Completo"
        Assertions.assertEquals("Completo", getGrapheneElement("main.stato").getAttribute("value"));

        doClickButton("doChiudiForm()");
    }

    /**
     * 5) annullamento evasione riga consegna
     */
    @Test
    @Order(6)
    public void testAnnullaEvasioneConsegna001() {
        switchToFrameMenu();
        doApriMenu(MAG);
        doApriMenu(MAG_ANNULLAMENTO);
        doSelezionaMenu(MAG_ANNULLAMENTO_M);

        switchToFrameWorkspace();

        //Scelgo Unità operativa: DRUE
        doClickButton("doBlankSearch(main.findUnitaOperativaOrd)");
        getGrapheneElement("main.findUnitaOperativaOrd.cdUnitaOperativa").writeIntoElement(CD_UNITA_OPERATIVA);
        doClickButton("doSearch(main.findUnitaOperativaOrd)");

        //Scelgo Magazzino: PT
        doClickButton("doBlankSearch(main.findMagazzino)");
        getGrapheneElement("main.findMagazzino.cdMagazzino").writeIntoElement(CD_MAGAZZINO);
        doClickButton("doSearch(main.findMagazzino)");

        String pgOrdineCreated = sharedResource.getVal01();
        getGrapheneElement("main.daNumeroOrdine").writeIntoElement(pgOrdineCreated);
        getGrapheneElement("main.aNumeroOrdine").writeIntoElement(pgOrdineCreated);

        doClickButton("doCerca");

        //Seleziono la prima riga ordini
        Optional<GrapheneElement> element = browser.findElements(By.tagName("tr"))
                .stream()
                .filter(GrapheneElement.class::isInstance)
                .map(GrapheneElement.class::cast)
                .filter(rowElement -> {
                    try {
                        return getTableColumnElement(rowElement, 17).getText().contains("2025") &&
                                CD_NUMERATORE.equals(getTableColumnElement(rowElement, 18).getText()) &&
                                pgOrdineCreated.equals(getTableColumnElement(rowElement, 19).getText()) &&
                                "1".equals(getTableColumnElement(rowElement, 20).getText()) &&
                                "1".equals(getTableColumnElement(rowElement, 21).getText());
                    } catch (java.lang.RuntimeException ex) {
                        return false;
                    }
                })
                .findAny();

        Assert.assertTrue(element.isPresent());

        element.get().findElement(By.name("mainTable.selection")).click();

        //Digito il pulsante ‘Annulla Movimenti Selezionati’.
        doClickButton("submitForm('doAnnullaMovimenti');");

        //Chiudo la form (2 volte perchè sono 2 BP aperti)
        doClickButton("doChiudiForm()");
        doClickButton("doChiudiForm()");
    }

    /**
     * 6) verifica annullamento bene in transito
     */
    @Test
    @Order(7)
    public void testVerificaBeneInTransito002() {
        //Verifico che il bene sia in transito
        switchToFrameMenu();
        doSelezionaMenu(AMM_INVENT_ORDINI_TRANSITO);

        switchToFrameWorkspace();

        //Ricerco l’unico bene in transito
        doClickButton("doCerca()");
        Alert alert = browser.switchTo().alert();
        Assertions.assertEquals(AlertMessage.MESSAGE_RICERCA_NO_RECORD.value(), alert.getText());
        alert.accept();

        doClickButton("doChiudiForm()");
    }

    /**
     * 7) nuova evasione riga consegna
     */
    @Test
    @Order(8)
    public void testEvasioneConsegna002() {
        switchToFrameMenu();
        doSelezionaMenu(ORD_EVAORD);

        switchToFrameWorkspace();

        //Scelgo Unità operativa: DRUE
        getGrapheneElement("main.findUnitaOperativaOrd.cdUnitaOperativa").writeIntoElement(CD_UNITA_OPERATIVA);
        doClickButton("doSearch(main.findUnitaOperativaOrd)");

        //Scelgo Magazzino: PT
        doClickButton("doBlankSearch(main.findMagazzino)");
        getGrapheneElement("main.findMagazzino.cdMagazzino").writeIntoElement(CD_MAGAZZINO);
        doClickButton("doSearch(main.findMagazzino)");

        //Data Bolla: Oggi
        getGrapheneElement("main.dataBolla").writeIntoElement(DATA_ODIERNA);
        doClickButton("doOnDtBollaChange");

        //Numero Bolla: 7
        getGrapheneElement("main.numeroBolla").writeIntoElement("10");

        //Data Consegna: Oggi
        getGrapheneElement("main.dataConsegna").writeIntoElement(DATA_ODIERNA);
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

        Assertions.assertThrows(RuntimeException.class, () -> getTableRowElement("main.ConsegneDaEvadere", 1),"Cannot find Element <tr> with tableName main.ConsegneDaEvadere and numberRow: 1");

        //Scelgo la prima consegna
        rowElement1.click();
        doSelectTableRow(rowElement1, "main.ConsegneDaEvadere");

        //Salvo
        doClickButton("doSalva()");
        Alert alert = browser.switchTo().alert();
        Assertions.assertEquals(AlertMessage.OPERAZIONE_EFFETTUATA.value(), alert.getText());
        alert.accept();
    }

    /**
     * 8) verifica inserimento bene in transito e completamento per successiva inventariazione
     */
    @Test
    @Order(9)
    public void testCompletaBeneInTransito001() {
        //Verifico che il bene sia in transito
        switchToFrameMenu();
        doSelezionaMenu(AMM_INVENT_ORDINI_TRANSITO);

        switchToFrameWorkspace();

        //Ricerco l’unico bene in transito
        doClickButton("doCerca()");
        Alert alert = browser.switchTo().alert();
        Assertions.assertEquals(AlertMessage.MESSAGE_RICERCA_MONO_RECORD.value(), alert.getText());
        alert.accept();

        Assertions.assertEquals("Inserito", getGrapheneElement("main.stato").getAttribute("value"));

        String pgOrdineCreated = sharedResource.getVal01();
        Assertions.assertEquals("2025/" + CD_NUMERATORE + "/" + pgOrdineCreated + "/1/1", getGrapheneElement("main.ordine").getAttribute("value"));

        //Modifico il campo condizioni bene mettendolo a Ottimo (0)
        Select select = new Select(getGrapheneElement("main.condizioni"));
        select.selectByValue("0");

        //Valorizzo il campo ubicazione con il valore 1
        doClickButton("doBlankSearch(main.find_ubicazione)");
        getGrapheneElement("main.find_ubicazione.cd_ubicazione").writeIntoElement("1");
        doClickButton("doSearch(main.find_ubicazione)");

        //Valorizzo il campo Assegnatario con il valore 1
        doClickButton("doBlankSearch(main.find_assegnatario)");
        getGrapheneElement("main.find_assegnatario.cd_terzo").writeIntoElement("1");
        doClickButton("doSearch(main.find_assegnatario)");

        //Salvo
        doClickButton("doSalva()");
        alert = browser.switchTo().alert();
        Assertions.assertEquals(AlertMessage.SALVATAGGIO_ESEGUITO.value(), alert.getText());
        alert.accept();

        //Il transito del bene deve acquisita stato "Completo"
        Assertions.assertEquals("Completo", getGrapheneElement("main.stato").getAttribute("value"));

        doClickButton("doChiudiForm()");
    }

    /**
     * 9) verifica inserimento bene in transito e completamento per successiva inventariazione
     */
    @Test
    @Order(10)
    public void testInventariazioneBeneInTransito001() {
        //Verifico che il bene sia in transito
        switchToFrameMenu();
        doSelezionaMenu(AMM_INVENT_ORDINI_INVENTARIAZIONE);

        switchToFrameWorkspace();

        String pgOrdineCreated = sharedResource.getVal01();

        //Seleziono il bene messo in transito dalla consegna n. 1
        Optional<GrapheneElement> element = browser.findElements(By.tagName("tr"))
                .stream()
                .filter(GrapheneElement.class::isInstance)
                .map(GrapheneElement.class::cast)
                .filter(rowElement -> {
                    try {
                        return getTableColumnElement(rowElement, 12).getText().contains("2025") &&
                                CD_NUMERATORE.equals(getTableColumnElement(rowElement, 13).getText()) &&
                                pgOrdineCreated.equals(getTableColumnElement(rowElement, 14).getText()) &&
                                "1".equals(getTableColumnElement(rowElement, 15).getText()) &&
                                "1".equals(getTableColumnElement(rowElement, 16).getText());
                    } catch (java.lang.RuntimeException ex) {
                        return false;
                    }
                })
                .findAny();

        Assert.assertTrue(element.isPresent());

        element.get().findElement(By.name("mainTable.selection")).click();

        doClickButton("doInventaria");

        //Data Carico: Oggi
        getGrapheneElement("main.data_registrazione").writeIntoElement(DATA_ODIERNA);
        doClickButton("doOnData_registrazioneChange");

        getGrapheneElement("main.ds_buono_carico_scarico").writeIntoElement("Inventariazione di test");

        //Salvo
        doClickButton("doSalva()");

        Alert alert = browser.switchTo().alert();
        Assertions.assertEquals(AlertMessage.CREAZIONE_ESEGUITA.value(), alert.getText());
        alert.accept();

        doClickButton("doChiudiForm()");
        doClickButton("doChiudiForm()");
    }

    /**
     * 10) annullamento evasione riga consegna non consentita per bene inventariato
     */
    @Test
    @Order(11)
    public void testAnnullaEvasioneConsegna002() {
        switchToFrameMenu();
        doSelezionaMenu(MAG_ANNULLAMENTO_M);

        switchToFrameWorkspace();

        //Scelgo Unità operativa: DRUE
        doClickButton("doBlankSearch(main.findUnitaOperativaOrd)");
        getGrapheneElement("main.findUnitaOperativaOrd.cdUnitaOperativa").writeIntoElement(CD_UNITA_OPERATIVA);
        doClickButton("doSearch(main.findUnitaOperativaOrd)");

        //Scelgo Magazzino: PT
        doClickButton("doBlankSearch(main.findMagazzino)");
        getGrapheneElement("main.findMagazzino.cdMagazzino").writeIntoElement(CD_MAGAZZINO);
        doClickButton("doSearch(main.findMagazzino)");

        String pgOrdineCreated = sharedResource.getVal01();
        getGrapheneElement("main.daNumeroOrdine").writeIntoElement(pgOrdineCreated);
        getGrapheneElement("main.aNumeroOrdine").writeIntoElement(pgOrdineCreated);

        doClickButton("doCerca");

        //Seleziono la prima riga ordini
        Optional<GrapheneElement> element = browser.findElements(By.tagName("tr"))
                .stream()
                .filter(GrapheneElement.class::isInstance)
                .map(GrapheneElement.class::cast)
                .filter(rowElement -> {
                    try {
                        return getTableColumnElement(rowElement, 17).getText().contains("2025") &&
                                CD_NUMERATORE.equals(getTableColumnElement(rowElement, 18).getText()) &&
                                pgOrdineCreated.equals(getTableColumnElement(rowElement, 19).getText()) &&
                                "1".equals(getTableColumnElement(rowElement, 20).getText()) &&
                                "1".equals(getTableColumnElement(rowElement, 21).getText());
                    } catch (java.lang.RuntimeException ex) {
                        return false;
                    }
                })
                .findAny();

        Assert.assertTrue(element.isPresent());

        element.get().findElement(By.name("mainTable.selection")).click();

        //Digito il pulsante ‘Annulla Movimenti Selezionati’.
        doClickButton("submitForm('doAnnullaMovimenti');");

        Alert alert = browser.switchTo().alert();
        Assertions.assertEquals("Operazione non possibile! Il bene associato al movimento " + getTableColumnElement(element.get(), 1).getText() + " risulta essere inventariato.", alert.getText());
        alert.accept();

        //Chiudo la form (2 volte perchè sono 2 BP aperti)
        doClickButton("doChiudiForm()");
        doClickButton("doChiudiForm()");
    }

    @Test
    @Order(12)
    public void testRiscontroValore001() {
        switchToFrameMenu();
        doApriMenu(AMM_FATTUR);
        doApriMenu(AMM_FATTUR_FATPAS);
        doSelezionaMenu(AMM_FATTUR_FATPAS_ELE);

        switchToFrameWorkspace();

        //Ricerco la fattura SDI: 90000000002
        getGrapheneElement("main.identificativoSdi").writeIntoElement("90000000004");

        Select select = new Select(getGrapheneElement("main.statoDocumento"));
        select.selectByValue("");

        doClickButton("doCerca()");
        Alert alert = browser.switchTo().alert();
        Assertions.assertEquals(AlertMessage.MESSAGE_RICERCA_MONO_RECORD.value(), alert.getText());
        alert.accept();

        //Clicco sul pulsante ‘Compila fattura’;
        doClickButton("submitForm('doCompilaFattura')");

        //Passo alla maschera di Registrazione Fattura
        getGrapheneElement("comando.doYes").click();

        //Sulla testata fattura indico lo stato ‘Liquidabile
        select = new Select(getGrapheneElement("main.stato_liquidazione"));
        select.selectByValue("LIQ");

        //Verifico che fattura da Ordini=Si
        Assertions.assertTrue(getGrapheneElement("main.flDaOrdini").isSelected());

        getGrapheneElement("main.ds_fattura_passiva").writeIntoElement("RISCONTRO VALORE TEST BENE INVENTARIALE");

        //Passo alla tab ‘ordini’
        doClickButton("doTab('tab','tabFatturaPassivaOrdini')");

        //Effettuo la ricerca
        doClickButton("submitForm('doSelezionaOrdini')");

        String pgOrdineCreated = sharedResource.getVal01();

        //Seleziono l'ordine creato
        Optional<GrapheneElement> element = browser.findElements(By.tagName("tr"))
                .stream()
                .filter(GrapheneElement.class::isInstance)
                .map(GrapheneElement.class::cast)
                .filter(rowElement -> {
                    try {
                        return getTableColumnElement(rowElement, 3).getText().contains("2025") &&
                                CD_NUMERATORE.equals(getTableColumnElement(rowElement, 4).getText()) &&
                                pgOrdineCreated.equals(getTableColumnElement(rowElement, 5).getText()) &&
                                "1".equals(getTableColumnElement(rowElement, 6).getText()) &&
                                "1".equals(getTableColumnElement(rowElement, 7).getText());
                    } catch (java.lang.RuntimeException ex) {
                        return false;
                    }
                })
                .findAny();

        Assert.assertTrue(element.isPresent());

        element.get().findElement(By.name("mainTable.selection")).click();

        doClickButton("submitForm('doMultipleSelection')");

        //Ritorno sulla tab ordini, vedo la squadratura di 1,22 euro della fattura (in più) rispetto all’ordine;
        doSelectTableRow("main.Ordini",0);

        //Inserisco nella sezione ‘Rettifiche’ nel campo ‘Prezzzo Unitario’ l’importo: 149,00;
        getGrapheneElement("main.Ordini.prezzoUnitarioRett").writeIntoElement("149,00");
        doClickButton("confirmModalInputChange(this,'main.Ordini.prezzoUnitarioRett','doRettificaConsegna')");

        //Digito ‘Fine riscontro a valore’ e mi sposto sulla tab ‘dettaglio’ dove mi è stato creato il dettaglio pari alla riga ordine (200 imponibile + 44 IVA).
        doClickButton("submitForm('doConfermaRiscontroAValore')");

        alert = browser.switchTo().alert();
        Assertions.assertEquals(AlertMessage.OPERAZIONE_EFFETTUATA.value(), alert.getText());
        alert.accept();

        doClickButton("doSalva()");

        alert = browser.switchTo().alert();
        Assertions.assertEquals(AlertMessage.CREAZIONE_ESEGUITA.value(), alert.getText());
        alert.accept();

        //Vado sulla tab principale
        doClickButton("doTab('tab','tabFatturaPassiva')");

        //Registro il numero fattura creata
        String pgFatturaCreated = getGrapheneElement("main.pg_fattura_passiva").getAttribute("value");
        sharedResource.setVal02(pgFatturaCreated);

        //Vado sulla tab economica per controllare scrittura
        doClickButton("doTab('tab','tabEconomica')");
        Assertions.assertEquals("P00047", getTableColumnElement("main.Movimenti Dare",0,1).getText());
        Assertions.assertEquals("181,78", getTableColumnElement("main.Movimenti Dare",0,5).getText());

        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.Movimenti Dare",1),"Cannot find Element <tr> with tableName 'main.Movimenti Dare' and numberRow: 1");

        Assertions.assertEquals("P22012", getTableColumnElement("main.Movimenti Avere",0,1).getText());
        Assertions.assertEquals("149,00", getTableColumnElement("main.Movimenti Avere",0,5).getText());

        Assertions.assertEquals("P71012I", getTableColumnElement("main.Movimenti Avere",1,1).getText());
        Assertions.assertEquals("32,78", getTableColumnElement("main.Movimenti Avere",1,5).getText());

        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.Movimenti Avere",2),"Cannot find Element <tr> with tableName 'main.Movimenti Avere' and numberRow: 2");

        //Vado sulla tab analitica per controllare scrittura che non deve esistere dato che la movimentazione avviene sulla consegna
        doClickButton("doTab('tab','tabAnalitica')");
        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.Movimenti Analitici",0),"Cannot find Element <tr> with tableName 'main.Movimenti Analitici' and numberRow: 0");

        doClickButton("doChiudiForm()");
    }

    @Test
    @Order(13)
    public void testVerificaScritturaOrdine002() {
        //Verifico che la scrittura sull'ordine sia stata eseguita correttamente
        switchToFrameMenu();
        doSelezionaMenu(ORD_ORDACQ_M);

        switchToFrameWorkspace();

        //Ricerco l’ordine
        doClickButton("doNuovaRicerca()");

        doClickButton("doBlankSearch(main.findUnitaOperativaOrd)");
        getGrapheneElement("main.findUnitaOperativaOrd.cdUnitaOperativa").writeIntoElement(CD_UNITA_OPERATIVA);
        doClickButton("doSearch(main.findUnitaOperativaOrd)");

        doClickButton("doBlankSearch(main.findNumerazioneOrd)");
        getGrapheneElement("main.findNumerazioneOrd.cdNumeratore").writeIntoElement(CD_NUMERATORE);
        doClickButton("doSearch(main.findNumerazioneOrd)");

        String pgOrdineCreated = sharedResource.getVal01();
        getGrapheneElement("main.numero").writeIntoElement(pgOrdineCreated);

        doClickButton("doCerca()");
        Alert alert = browser.switchTo().alert();
        Assertions.assertEquals(AlertMessage.MESSAGE_RICERCA_MONO_RECORD.value(), alert.getText());
        alert.accept();

        //Vado sul dettaglio
        doClickButton("doTab('tab','tabOrdineAcqDettaglio')");
        doSelectTableRow("main.Righe",0);

        doClickButton("doTab('tabOrdineAcqDettagli','tabOrdineRigaResultDetailEcoCoge')");

        Assertions.assertEquals("A22012", getGrapheneElement("main.Righe.find_voce_ep.cd_voce_ep").getAttribute("value"));
        Assertions.assertEquals("181,78", getGrapheneElement("imCostoEcoConsegne").getAttribute("value"));
        Assertions.assertEquals("181,78", getGrapheneElement("imCostoEcoRipartitoConsegne").getAttribute("value"));
        Assertions.assertEquals("0,00", getGrapheneElement("imCostoEcoDaRipartireConsegne").getAttribute("value"));

        doSelectTableRow("main.Righe.Dati Coge/Coan",0);

        Assertions.assertEquals("PTEST001", getTableColumnElement("main.Righe.Dati Coge/Coan",0,1).getText());
        Assertions.assertEquals("000.000.000", getTableColumnElement("main.Righe.Dati Coge/Coan",0,2).getText());
        Assertions.assertEquals("181,78", getTableColumnElement("main.Righe.Dati Coge/Coan",0,3).getText());
        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.Righe.Dati Coge/Coan",1),"Cannot find Element <tr> with tableName main.Righe.Dati Coge/Coan and numberRow: 1");

        //Vado sulla tab ‘consegne’
        doClickButton("doTab('tabOrdineAcqDettagli','tabOrdineConsegna')");

        //Vado sulla tab ‘consegne’ e individuo la riga consegna n. 1
        GrapheneElement rowElement1 = null;
        if (getTableColumnElement("main.Righe.Consegne",0, 1).getText().equals("1"))
            rowElement1 = getTableRowElement("main.Righe.Consegne",0);
        else if (getTableColumnElement("main.Righe.Consegne",1, 1).getText().equals("1"))
            rowElement1 = getTableRowElement("main.Righe.Consegne",1);
        else
            Assertions.fail("Riga consegna 1 non individuata");

        //Verifico che le righe di consegna n. 1 sia in stato ‘EVASA’ e ‘Associata Totalmente’ a Fattura.
        Assertions.assertEquals("Evasa", getTableColumnElement(rowElement1,5).getText());
        Assertions.assertEquals("Associata Totalmente", getTableColumnElement(rowElement1,6).getText());

        rowElement1.click();

        //Visualizzo le scritture di economica associate
        doClickButton("submitForm('doVisualizzaEconomica');");

        //Seleziono l'ultima scrittura che dovrebbe essere la rettifica valore
        getTableRowElement("mainTable",2).click();

        Assertions.assertEquals("Si", getGrapheneElement("main.attiva").getText());
        Assertions.assertEquals("1,22", getGrapheneElement("main.imTotaleDare").getAttribute("value"));
        Assertions.assertEquals("1,22", getGrapheneElement("main.imTotaleAvere").getAttribute("value"));

        doClickButton("doTab('tab','tabDare')");
        Assertions.assertEquals("P00047", getTableColumnElement("main.MovimentiDare",0,1).getText());
        Assertions.assertEquals("1,22", getTableColumnElement("main.MovimentiDare",0,5).getText());
        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.MovimentiDare",1),"Cannot find Element <tr> with tableName main.MovimentiDare and numberRow: 1");

        doClickButton("doTab('tab','tabAvere')");
        Assertions.assertEquals("A22012", getTableColumnElement("main.MovimentiAvere",0,1).getText());
        Assertions.assertEquals("1,22", getTableColumnElement("main.MovimentiAvere",0,5).getText());
        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.MovimentiAvere",1),"Cannot find Element <tr> with tableName main.MovimentiAvere and numberRow: 1");

        doClickButton("doChiudiForm()");

        //Passo alla ‘visualizzazione fattura collegata’
        doClickButton("submitForm('doVisualizzaFattura');");

        String pgFatturaCreated = sharedResource.getVal02();
        Assertions.assertEquals(pgFatturaCreated, getGrapheneElement("main.pg_fattura_passiva").getAttribute("value"));

        doClickButton("doChiudiForm()");

        //Passo alla ‘visualizzazione dei movimenti’
        doClickButton("submitForm('doVisualizzaMovimento');");

        Assertions.assertEquals("C01", getTableColumnElement("mainTable",0,3).getText());
        Assertions.assertEquals("183", getTableColumnElement("mainTable",0,9).getText());

        Assertions.assertEquals("C20", getTableColumnElement("mainTable",1,3).getText());
        Assertions.assertEquals("1,22", getTableColumnElement("mainTable",1,9).getText());

        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("mainTable",2),"Cannot find Element <tr> with tableName 'mainTable' and numberRow: 2");

        doClickButton("doChiudiForm()");
    }

}
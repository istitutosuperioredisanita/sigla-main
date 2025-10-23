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
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.graphene.GrapheneElement;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;

import java.util.GregorianCalendar;
import java.util.Optional;

import static org.junit.Assert.*;

public class CRUDOrdineAcqBP002Test extends ActionDeployments {
    private static SharedResource sharedResource;

    public static final String USERNAME = "ENTETEST";
    public static final String PASSWORD = "PASSTEST";

    public static final String ORD = "0.ORD";
    public static final String ORD_ORDACQ = "0.ORD.ORDACQ";
    public static final String ORD_ORDACQ_M = "0.ORD.ORDACQ.M";
    public static final String ORD_EVAORD = "0.ORD.EVAORD";

    public static final String AMM = "0.AMM";
    public static final String AMM_FATTUR = "0.AMM.FATTUR";
    public static final String AMM_FATTUR_FATPAS = "0.AMM.FATTUR.FATPAS";
    public static final String AMM_FATTUR_FATPAS_ELE = "0.AMM.FATTUR.FATPAS.ELE";

    public static final String MAG = "0.MAG";
    public static final String MAG_ANNULLAMENTO = "0.MAG.ANNULLAMENTO";
    public static final String MAG_ANNULLAMENTO_M = "0.MAG.ANNULLAMENTO.GESTIONE";

    public static final String CD_UNITA_OPERATIVA = "DRUE";
    public static final String CD_NUMERATORE = "DSA";
    public static final String CD_MAGAZZINO = "PT";
    public static final String UO = "000.000";
    public static final String CDR = "000.000.000";

    public static final String CONTRATTO_ESERCIZIO = "2025";
    public static final String CONTRATTO_NUMERO = "2";

    public static final String BENE_SERVIZIO_CODICE_01 = "191202";

    @BeforeClass
    public static void initClass() {
        if (sharedResource == null)
            sharedResource = new SharedResource();
    }

    @Test
    @RunAsClient
    @OperateOnDeployment(TEST_H2)
    @InSequence(1)
    public void testLogin() throws Exception {
        doLogin(USERNAME, PASSWORD);
        doLoginUO(UO, CDR);
    }

    /**
     * Test Creazione Progetto
     */
    @Test
    @RunAsClient
    @OperateOnDeployment(TEST_H2)
    @InSequence(2)
    public void testCreaOrdine() {
        switchToFrameDesktop();
        switchToFrameMenu();
        doApriMenu(ORD);
        doApriMenu(ORD_ORDACQ);
        doSelezionaMenu(ORD_ORDACQ_M);

        browser.switchTo().parentFrame();
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
        getGrapheneElement("main.nota").writeIntoElement("Prova per documentazione");

        //Passo sulla tab ‘Fornitore’ ed il terzo è proposto in automatico
        doClickButton("doTab('tab','tabOrdineFornitore')");
        assertEquals("101", getGrapheneElement("main.findFornitore.cd_terzo").getAttribute("value"));

        //Passo sulla tab ‘Dettaglio’
        doClickButton("doTab('tab','tabOrdineAcqDettaglio')");

        //Inserisco un nuovo dettaglio
        doClickButton("doAddToCRUD(main.Righe)");

        //Indico codice articolo: 191202
        getGrapheneElement("main.Righe.findBeneServizio.cd_bene_servizio").writeIntoElement(BENE_SERVIZIO_CODICE_01);
        doClickButton("doSearch(main.Righe.findBeneServizio)");

        //Indico Quantità: 2
        getGrapheneElement("main.Righe.dspQuantita").writeIntoElement("2");
        doClickButton("doOnDspQuantitaChange");

        //Indico Prezzo: 200
        getGrapheneElement("main.Righe.prezzoUnitario").writeIntoElement("200");
        doClickButton("doOnImportoChange");

        //Lascio la proposta di ‘Tipo magazzino’ e ‘Data prevista consegna’
        Select select = new Select(getGrapheneElement("main.Righe.tipoConsegna"));
        assertEquals("MAG", select.getFirstSelectedOption().getAttribute("value"));

        //Lascio la ‘Data prevista consegna’
        assertNotNull(getGrapheneElement("main.Righe.dtPrevConsegna").getText());

        //Indico il codice magazzino: PT
        doClickButton("doBlankSearch(main.Righe.findMagazzino)");
        getGrapheneElement("main.Righe.findMagazzino.cdMagazzino").writeIntoElement(CD_MAGAZZINO);
        doClickButton("doSearch(main.Righe.findMagazzino)");

        //Passo sulla tab ‘consegne’
        doClickButton("doTab('tabOrdineAcqDettagli','tabOrdineConsegna')");

        //Selezione l'unica consegna
        doSelectTableRow("main.Righe.Consegne",0);

        //modifico la quantità della riga consegna (da 2 passo ad 1)
        getGrapheneElement("main.Righe.Consegne.quantita").clear();
        getGrapheneElement("main.Righe.Consegne.quantita").writeIntoElement("1");
        doClickButton("confirmModalInputChange(this,'main.Righe.Consegne.quantita','doOnQuantitaChange')");

        //Creo la seconda consegna
        doClickButton("doAddToCRUD(main.Righe.Consegne)");
        //Selezione la nuova consegna
        doSelectTableRow("main.Righe.Consegne",1);

        //indicando la quantità 1
        getGrapheneElement("main.Righe.Consegne.quantita").writeIntoElement("1");
        doClickButton("confirmModalInputChange(this,'main.Righe.Consegne.quantita','doOnQuantitaChange')");

        //magazzino: PT
        doClickButton("doBlankSearch(main.Righe.Consegne.findMagazzino)");
        getGrapheneElement("main.Righe.Consegne.findMagazzino.cdMagazzino").writeIntoElement(CD_MAGAZZINO);
        doClickButton("doSearch(main.Righe.Consegne.findMagazzino)");

        //Passo sulla tab ‘Dati coge/coan’ e verifico che il conto di costo è quello legato alla categoria gruppo del mio articolo (8.1) ed è: C13003. L’analitica correttamente non c’è (non ho ancora indicato l’impegno).
        doClickButton("doTab('tabOrdineAcqDettagli','tabDatiCogeCoan')");

        //Verifico che il conto di costo è quello legato alla categoria gruppo del mio articolo (8.1) ed è: C13003.
        assertEquals("C13003", getGrapheneElement("main.Righe.find_voce_ep.cd_voce_ep").getAttribute("value"));

        // L’analitica correttamente non c’è (non ho ancora indicato l’impegno).
        assertEquals("488,00",getGrapheneElement("imCostoEcoDaRipartire").getAttribute("value"));

        //Ritorno sulla prima tab ‘ordine d’acquisto’
        doClickButton("doTab('tab','tabOrdineAcq')");

        //Indico motivo di assenza CIG: SPESE ECONOMALI
        select = new Select(getGrapheneElement("main.motivoAssenzaCig"));
        select.selectByValue("SPESE_ECONOMALI");

        //Salvo
        doClickButton("doSalva()");

        //Restituisce messaggio di creazione eseguita
        Alert alert = browser.switchTo().alert();
        assertEquals(AlertMessage.CREAZIONE_ESEGUITA.value(), alert.getText());
        alert.accept();

        String pgProgettoCreated = getGrapheneElement("main.numero").getAttribute("value");
        sharedResource.setVal01(pgProgettoCreated);

        //modifico lo stato portandolo ‘In Approvazione’
        select = new Select(getGrapheneElement("main.statoForUpdate"));
        select.selectByValue("APP");

        //Salvo
        doClickButton("doSalva()");

        //Restituisce messaggio di salvataggio eseguito
        alert = browser.switchTo().alert();
        assertEquals(AlertMessage.SALVATAGGIO_ESEGUITO.value(), alert.getText());
        alert.accept();

        //modifico lo stato portandolo ‘Definitivo’
        select.selectByValue("DEF");

        //Salvo
        doClickButton("doSalva()");

        //Restituisce messaggio di assenza obbligazione
        alert = browser.switchTo().alert();
        assertEquals("Sulla consegna 2025/"+CD_NUMERATORE+"/"+pgProgettoCreated+"/1/1 non è indicata l'obbligazione", alert.getText());
        alert.accept();

        //Ritorno sulla tab delle righe ordine
        doClickButton("doTab('tab','tabOrdineAcqDettaglio')");

        //Seleziono la prima riga
        doSelectTableRow("main.Righe",0);

        //Vado sul pulsante ‘crea/associa impegni’
        doClickButton("doRicercaObbligazione");

        //Sulla mappa di ricerca impegni scelgo di creare un nuovo impegno
        doClickButton("submitForm('doOpenObbligazioniWindow')");

        //specifico solo la voce: 13017
        getGrapheneElement("main.find_elemento_voce.searchtool_cd_elemento_voce").clear();
        getGrapheneElement("main.find_elemento_voce.searchtool_cd_elemento_voce").writeIntoElement("13017");
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
        assertEquals(AlertMessage.SALVATAGGIO_ESEGUITO.value(), alert.getText());
        alert.accept();
    }

    @Test
    @RunAsClient
    @OperateOnDeployment(TEST_H2)
    @InSequence(3)
    public void testEvasioneConsegna001() {
        browser.switchTo().parentFrame();
        switchToFrameMenu();
        doSelezionaMenu(ORD_EVAORD);

        browser.switchTo().parentFrame();
        switchToFrameWorkspace();

        //Scelgo Unità operativa: DRUE
        getGrapheneElement("main.findUnitaOperativaOrd.cdUnitaOperativa").writeIntoElement(CD_UNITA_OPERATIVA);
        doClickButton("doSearch(main.findUnitaOperativaOrd)");

        //Scelgo Magazzino: PT
        doClickButton("doBlankSearch(main.findMagazzino)");
        getGrapheneElement("main.findMagazzino.cdMagazzino").writeIntoElement(CD_MAGAZZINO);
        doClickButton("doSearch(main.findMagazzino)");

        //Data Bolla: Oggi
        GregorianCalendar dataBollaConsegna = (GregorianCalendar) GregorianCalendar.getInstance();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");

        getGrapheneElement("main.dataBolla").writeIntoElement(sdf.format(dataBollaConsegna.getTime().getTime()));
        doClickButton("doOnDtBollaChange");

        //Numero Bolla: 7
        getGrapheneElement("main.numeroBolla").writeIntoElement("7");

        //Data Consegna: Oggi
        getGrapheneElement("main.dataConsegna").writeIntoElement(sdf.format(dataBollaConsegna.getTime().getTime()));
        doClickButton("doOnDtConsegnaChange");

        //Eseguo la ricerca
        doClickButton("doCercaConsegneDaEvadere");

        String pgProgettoCreated = sharedResource.getVal01();

        //Trovo le mie due righe di consegna dell’ordine creato con test precedente
        GrapheneElement rowElement1=null;
        if (getTableColumnElement("main.ConsegneDaEvadere",0, 1).getText().equals("2025/"+CD_NUMERATORE+"/"+pgProgettoCreated+"/1/1"))
            rowElement1 = getTableRowElement("main.ConsegneDaEvadere",0);
        else if (getTableColumnElement("main.ConsegneDaEvadere",1, 1).getText().equals("2025/"+CD_NUMERATORE+"/"+pgProgettoCreated+"/1/1"))
            rowElement1 = getTableRowElement("main.ConsegneDaEvadere",1);
        else
            Assert.fail("Riga consegna 1 non individuata");

        GrapheneElement rowElement2=null;
        if (getTableColumnElement("main.ConsegneDaEvadere",0, 1).getText().equals("2025/"+CD_NUMERATORE+"/"+pgProgettoCreated+"/1/2"))
            rowElement2 = getTableRowElement("main.ConsegneDaEvadere",0);
        else if (getTableColumnElement("main.ConsegneDaEvadere",1, 1).getText().equals("2025/"+CD_NUMERATORE+"/"+pgProgettoCreated+"/1/2"))
            rowElement2 = getTableRowElement("main.ConsegneDaEvadere",1);
        else
            Assert.fail("Riga consegna 2 non individuata");

        assertThrows("Cannot find Element <tr> with tableName main.ConsegneDaEvadere and numberRow: 2", RuntimeException.class, ()->getTableRowElement("main.ConsegneDaEvadere",2));

        //Scelgo la prima consegna
        rowElement1.click();
        doSelectTableRow(rowElement1, "main.ConsegneDaEvadere");

        //Salvo
        doClickButton("doSalva()");
        Alert alert = browser.switchTo().alert();
        assertEquals(AlertMessage.OPERAZIONE_EFFETTUATA.value(), alert.getText());
        alert.accept();

        //Verifico che la scrittura sull'ordine sia stata eseguita correttamente
        browser.switchTo().parentFrame();
        switchToFrameMenu();

        doSelezionaMenu(ORD_ORDACQ_M);

        browser.switchTo().parentFrame();
        switchToFrameWorkspace();

        //Ricerco l’ordine
        doClickButton("doNuovaRicerca()");

        doClickButton("doBlankSearch(main.findUnitaOperativaOrd)");
        getGrapheneElement("main.findUnitaOperativaOrd.cdUnitaOperativa").writeIntoElement(CD_UNITA_OPERATIVA);
        doClickButton("doSearch(main.findUnitaOperativaOrd)");

        doClickButton("doBlankSearch(main.findNumerazioneOrd)");
        getGrapheneElement("main.findNumerazioneOrd.cdNumeratore").writeIntoElement(CD_NUMERATORE);
        doClickButton("doSearch(main.findNumerazioneOrd)");

        getGrapheneElement("main.numero").writeIntoElement(pgProgettoCreated);

        doClickButton("doCerca()");
        alert = browser.switchTo().alert();
        assertEquals(AlertMessage.MESSAGE_RICERCA_MONO_RECORD.value(), alert.getText());
        alert.accept();

        //Vado sul dettaglio
        doClickButton("doTab('tab','tabOrdineAcqDettaglio')");
        doSelectTableRow("main.Righe",0);

        doClickButton("doTab('tabOrdineAcqDettagli','tabOrdineRigaResultDetailEcoCoge')");

        assertEquals("C13003", getGrapheneElement("main.Righe.find_voce_ep.cd_voce_ep").getAttribute("value"));

        doSelectTableRow("main.Righe.Dati Coge/Coan",0);

        assertEquals("C13003", getTableColumnElement("main.Righe.Dati Coge/Coan",0,1).getText());
        assertEquals("PTEST001", getTableColumnElement("main.Righe.Dati Coge/Coan",0,2).getText());
        assertEquals("000.000.000", getTableColumnElement("main.Righe.Dati Coge/Coan",0,3).getText());
        assertEquals("488,00", getTableColumnElement("main.Righe.Dati Coge/Coan",0,4).getText());
        assertThrows("Cannot find Element <tr> with tableName main.Righe.Dati Coge/Coan and numberRow: 1", RuntimeException.class, ()->getTableRowElement("main.Righe.Dati Coge/Coan",1));

        //Vado sulla tab ‘consegne’
        doClickButton("doTab('tabOrdineAcqDettagli','tabOrdineConsegna')");

        //Vado sulla tab ‘consegne’ e verifico che la riga consegna n. 1 sia in stato ‘EVASA’ e la consegna
        if (getTableColumnElement("main.Righe.Consegne",0, 1).getText().equals("1"))
            rowElement1 = getTableRowElement("main.Righe.Consegne",0);
        else if (getTableColumnElement("main.Righe.Consegne",1, 1).getText().equals("1"))
            rowElement1 = getTableRowElement("main.Righe.Consegne",1);
        else
            Assert.fail("Riga consegna 1 non individuata");

        if (getTableColumnElement("main.Righe.Consegne",0, 1).getText().equals("2"))
            rowElement2 = getTableRowElement("main.Righe.Consegne",0);
        else if (getTableColumnElement("main.Righe.Consegne",1, 1).getText().equals("2"))
            rowElement2 = getTableRowElement("main.Righe.Consegne",1);
        else
            Assert.fail("Riga consegna 2 non individuata");

        //Verifico che la riga consegna n. 1 sia in stato ‘EVASA’ e la consegna n. 2 ancora in stato INSERITA. Entrambe ‘Non Associate’.
        assertEquals("Evasa", getTableColumnElement(rowElement1,5).getText());
        assertEquals("Inserita", getTableColumnElement(rowElement2,5).getText());
        assertEquals("Non Associata", getTableColumnElement(rowElement1,6).getText());
        assertEquals("Non Associata", getTableColumnElement(rowElement2,6).getText());

        //Mi posiziono sulla prima consegna
        rowElement1.click();

        //Entro su ‘scrittura economica’.
        doClickButton("submitForm('doVisualizzaEconomica');");

        assertEquals("Si", getGrapheneElement("main.attiva").getText());
        assertEquals("244,00", getGrapheneElement("main.imTotaleDare").getAttribute("value"));
        assertEquals("244,00", getGrapheneElement("main.imTotaleAvere").getAttribute("value"));

        //Nella sezione ‘Dare’ è presente correttamente il conto di costo: C13003
        doClickButton("doTab('tab','tabDare')");
        assertEquals("C13003", getTableColumnElement("main.MovimentiDare",0,1).getText());
        assertEquals("244,00", getTableColumnElement("main.MovimentiDare",0,5).getText());
        assertThrows("Cannot find Element <tr> with tableName main.MovimentiDare and numberRow: 1", RuntimeException.class, ()->getTableRowElement("main.MovimentiDare",1));

        //Nella sezione ‘Avere’ è presente correttamente il conto fatture da ricevere: P00047.
        doClickButton("doTab('tab','tabAvere')");
        assertEquals("P00047", getTableColumnElement("main.MovimentiAvere",0,1).getText());
        assertEquals("244,00", getTableColumnElement("main.MovimentiAvere",0,5).getText());
        assertThrows("Cannot find Element <tr> with tableName main.MovimentiAvere and numberRow: 1", RuntimeException.class, ()->getTableRowElement("main.MovimentiAvere",1));

        doClickButton("doChiudiForm()");
        doClickButton("submitForm('doVisualizzaAnalitica');");

        assertEquals("Y", getGrapheneElement("main.attiva").getText());
        assertEquals("244,00", getGrapheneElement("main.imTotaleMov").getAttribute("value"));

        doClickButton("doTab('tab','tabMovimenti')");
        assertEquals("C13003", getTableColumnElement("main.Movimenti",0,1).getText());
        assertEquals("Dare", getTableColumnElement("main.Movimenti",0,2).getText());
        assertEquals("000.000.000", getTableColumnElement("main.Movimenti",0,3).getText());
        assertEquals("PTEST001", getTableColumnElement("main.Movimenti",0,4).getText());
        assertEquals("244,00", getTableColumnElement("main.Movimenti",0,5).getText());
        assertThrows("Cannot find Element <tr> with tableName main.Movimenti and numberRow: 1", RuntimeException.class, ()->getTableRowElement("main.Movimenti",1));

        doClickButton("doChiudiForm()");
    }

    /**
     * Test Evasione Consegne di Progetto creato con test precedente
     */
    @Test
    @RunAsClient
    @OperateOnDeployment(TEST_H2)
    @InSequence(4)
    public void testAnnullaEvasioneConsegna() {
        browser.switchTo().parentFrame();
        switchToFrameMenu();
        doApriMenu(MAG);
        doApriMenu(MAG_ANNULLAMENTO);
        doSelezionaMenu(MAG_ANNULLAMENTO_M);

        browser.switchTo().parentFrame();
        switchToFrameWorkspace();

        //Scelgo Unità operativa: DRUE
        doClickButton("doBlankSearch(main.findUnitaOperativaOrd)");
        getGrapheneElement("main.findUnitaOperativaOrd.cdUnitaOperativa").writeIntoElement(CD_UNITA_OPERATIVA);
        doClickButton("doSearch(main.findUnitaOperativaOrd)");

        //Scelgo Magazzino: PT
        doClickButton("doBlankSearch(main.findMagazzino)");
        getGrapheneElement("main.findMagazzino.cdMagazzino").writeIntoElement(CD_MAGAZZINO);
        doClickButton("doSearch(main.findMagazzino)");

        String pgProgettoCreated = sharedResource.getVal01();
        getGrapheneElement("main.daNumeroOrdine").writeIntoElement(pgProgettoCreated);
        getGrapheneElement("main.aNumeroOrdine").writeIntoElement(pgProgettoCreated);

        doClickButton("doCerca");

        //Seleziono la prima riga ordini
        Optional<GrapheneElement> element = browser.findElements(By.tagName("tr"))
                .stream()
                .filter(GrapheneElement.class::isInstance)
                .map(GrapheneElement.class::cast)
                .filter(rowElement -> {
                    try {
                        return  CD_NUMERATORE.equals(getTableColumnElement(rowElement, 18).getText()) &&
                                pgProgettoCreated.equals(getTableColumnElement(rowElement, 19).getText()) &&
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

        //Verifico che la scrittura sull'ordine sia stata eseguita correttamente
        browser.switchTo().parentFrame();
        switchToFrameMenu();

        doSelezionaMenu(ORD_ORDACQ_M);

        browser.switchTo().parentFrame();
        switchToFrameWorkspace();

        //Ricerco l’ordine
        doClickButton("doNuovaRicerca()");

        doClickButton("doBlankSearch(main.findUnitaOperativaOrd)");
        getGrapheneElement("main.findUnitaOperativaOrd.cdUnitaOperativa").writeIntoElement(CD_UNITA_OPERATIVA);
        doClickButton("doSearch(main.findUnitaOperativaOrd)");

        doClickButton("doBlankSearch(main.findNumerazioneOrd)");
        getGrapheneElement("main.findNumerazioneOrd.cdNumeratore").writeIntoElement(CD_NUMERATORE);
        doClickButton("doSearch(main.findNumerazioneOrd)");

        getGrapheneElement("main.numero").writeIntoElement(pgProgettoCreated);

        doClickButton("doCerca()");
        Alert alert = browser.switchTo().alert();
        assertEquals(AlertMessage.MESSAGE_RICERCA_MONO_RECORD.value(), alert.getText());
        alert.accept();

        //Vado sul dettaglio
        doClickButton("doTab('tab','tabOrdineAcqDettaglio')");
        doSelectTableRow("main.Righe",0);

        //Vado sulla tab ‘consegne’
        doClickButton("doTab('tabOrdineAcqDettagli','tabOrdineConsegna')");

        //Vado sulla tab ‘consegne’
        GrapheneElement rowElement1=null;
        if (getTableColumnElement("main.Righe.Consegne",0, 1).getText().equals("1"))
            rowElement1 = getTableRowElement("main.Righe.Consegne",0);
        else if (getTableColumnElement("main.Righe.Consegne",1, 1).getText().equals("1"))
            rowElement1 = getTableRowElement("main.Righe.Consegne",1);
        else
            Assert.fail("Riga consegna 1 non individuata");

        GrapheneElement rowElement2=null;
        if (getTableColumnElement("main.Righe.Consegne",0, 1).getText().equals("2"))
            rowElement2 = getTableRowElement("main.Righe.Consegne",0);
        else if (getTableColumnElement("main.Righe.Consegne",1, 1).getText().equals("2"))
            rowElement2 = getTableRowElement("main.Righe.Consegne",1);
        else
            Assert.fail("Riga consegna 2 non individuata");

        //Verifico che la riga consegna n. 1 sia ritornata in stato ‘INSERITA’ e la consegna n. 2 sia ancora in stato INSERITA. Entrambe ‘Non Associate’.
        assertEquals("Inserita", getTableColumnElement(rowElement1,5).getText());
        assertEquals("Inserita", getTableColumnElement(rowElement2,5).getText());
        assertEquals("Non Associata", getTableColumnElement(rowElement1,6).getText());
        assertEquals("Non Associata", getTableColumnElement(rowElement2,6).getText());

        doClickButton("doChiudiForm()");
    }

    @Test
    @RunAsClient
    @OperateOnDeployment(TEST_H2)
    @InSequence(5)
    public void testEvasioneConsegna002() {
        //Rifaccio l’evasione come al testEvasioneConsegna001
        browser.switchTo().parentFrame();
        switchToFrameMenu();
        doSelezionaMenu(ORD_EVAORD);

        browser.switchTo().parentFrame();
        switchToFrameWorkspace();

        //Scelgo Unità operativa: DRUE
        doClickButton("doBlankSearch(main.findUnitaOperativaOrd)");
        getGrapheneElement("main.findUnitaOperativaOrd.cdUnitaOperativa").writeIntoElement(CD_UNITA_OPERATIVA);
        doClickButton("doSearch(main.findUnitaOperativaOrd)");

        //Scelgo Magazzino: PT
        doClickButton("doBlankSearch(main.findMagazzino)");
        getGrapheneElement("main.findMagazzino.cdMagazzino").writeIntoElement(CD_MAGAZZINO);
        doClickButton("doSearch(main.findMagazzino)");

        //Data Bolla: Oggi
        GregorianCalendar dataBollaConsegna = (GregorianCalendar) GregorianCalendar.getInstance();
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");

        getGrapheneElement("main.dataBolla").writeIntoElement(sdf.format(dataBollaConsegna.getTime().getTime()));
        doClickButton("doOnDtBollaChange");

        //Numero Bolla: 8
        getGrapheneElement("main.numeroBolla").writeIntoElement("8");

        //Data Consegna: Oggi
        getGrapheneElement("main.dataConsegna").writeIntoElement(sdf.format(dataBollaConsegna.getTime().getTime()));
        doClickButton("doOnDtConsegnaChange");

        //Eseguo la ricerca
        doClickButton("doCercaConsegneDaEvadere");

        String pgProgettoCreated = sharedResource.getVal01();

        //Trovo le mie due righe di consegna dell’ordine creato con test precedente
        GrapheneElement rowElement1=null;
        if (getTableColumnElement("main.ConsegneDaEvadere",0, 1).getText().equals("2025/"+CD_NUMERATORE+"/"+pgProgettoCreated+"/1/1"))
            rowElement1 = getTableRowElement("main.ConsegneDaEvadere",0);
        else if (getTableColumnElement("main.ConsegneDaEvadere",1, 1).getText().equals("2025/"+CD_NUMERATORE+"/"+pgProgettoCreated+"/1/1"))
            rowElement1 = getTableRowElement("main.ConsegneDaEvadere",1);
        else
            Assert.fail("Riga consegna 1 non individuata");

        GrapheneElement rowElement2=null;
        if (getTableColumnElement("main.ConsegneDaEvadere",0, 1).getText().equals("2025/"+CD_NUMERATORE+"/"+pgProgettoCreated+"/1/2"))
            rowElement2 = getTableRowElement("main.ConsegneDaEvadere",0);
        else if (getTableColumnElement("main.ConsegneDaEvadere",1, 1).getText().equals("2025/"+CD_NUMERATORE+"/"+pgProgettoCreated+"/1/2"))
            rowElement2 = getTableRowElement("main.ConsegneDaEvadere",1);
        else
            Assert.fail("Riga consegna 2 non individuata");

        assertThrows("Cannot find Element <tr> with tableName main.ConsegneDaEvadere and numberRow: 2", RuntimeException.class, ()->getTableRowElement("main.ConsegneDaEvadere",2));

        //Scelgo la prima consegna
        rowElement1.click();
        doSelectTableRow(rowElement1, "main.ConsegneDaEvadere");

        //Salvo
        doClickButton("doSalva()");
        Alert alert = browser.switchTo().alert();
        assertEquals(AlertMessage.OPERAZIONE_EFFETTUATA.value(), alert.getText());
        alert.accept();

        //Verifico che la scrittura sull'ordine sia stata eseguita correttamente
        browser.switchTo().parentFrame();
        switchToFrameMenu();

        doSelezionaMenu(ORD_ORDACQ_M);

        browser.switchTo().parentFrame();
        switchToFrameWorkspace();

        //Ricerco l’ordine
        doClickButton("doNuovaRicerca()");

        doClickButton("doBlankSearch(main.findUnitaOperativaOrd)");
        getGrapheneElement("main.findUnitaOperativaOrd.cdUnitaOperativa").writeIntoElement(CD_UNITA_OPERATIVA);
        doClickButton("doSearch(main.findUnitaOperativaOrd)");

        doClickButton("doBlankSearch(main.findNumerazioneOrd)");
        getGrapheneElement("main.findNumerazioneOrd.cdNumeratore").writeIntoElement(CD_NUMERATORE);
        doClickButton("doSearch(main.findNumerazioneOrd)");

        getGrapheneElement("main.numero").writeIntoElement(pgProgettoCreated);

        doClickButton("doCerca()");
        alert = browser.switchTo().alert();
        assertEquals(AlertMessage.MESSAGE_RICERCA_MONO_RECORD.value(), alert.getText());
        alert.accept();

        //Vado sul dettaglio
        doClickButton("doTab('tab','tabOrdineAcqDettaglio')");
        doSelectTableRow("main.Righe",0);

        doClickButton("doTab('tabOrdineAcqDettagli','tabOrdineRigaResultDetailEcoCoge')");

        assertEquals("C13003", getGrapheneElement("main.Righe.find_voce_ep.cd_voce_ep").getAttribute("value"));

        doSelectTableRow("main.Righe.Dati Coge/Coan",0);

        assertEquals("C13003", getTableColumnElement("main.Righe.Dati Coge/Coan",0,1).getText());
        assertEquals("PTEST001", getTableColumnElement("main.Righe.Dati Coge/Coan",0,2).getText());
        assertEquals("000.000.000", getTableColumnElement("main.Righe.Dati Coge/Coan",0,3).getText());
        assertEquals("488,00", getTableColumnElement("main.Righe.Dati Coge/Coan",0,4).getText());
        assertThrows("Cannot find Element <tr> with tableName main.Righe.Dati Coge/Coan and numberRow: 1", RuntimeException.class, ()->getTableRowElement("main.Righe.Dati Coge/Coan",1));

        //Vado sulla tab ‘consegne’
        doClickButton("doTab('tabOrdineAcqDettagli','tabOrdineConsegna')");

        //Vado sulla tab ‘consegne’ e verifico che la riga consegna n. 1 sia in stato ‘EVASA’ e la consegna
        if (getTableColumnElement("main.Righe.Consegne",0, 1).getText().equals("1"))
            rowElement1 = getTableRowElement("main.Righe.Consegne",0);
        else if (getTableColumnElement("main.Righe.Consegne",1, 1).getText().equals("1"))
            rowElement1 = getTableRowElement("main.Righe.Consegne",1);
        else
            Assert.fail("Riga consegna 1 non individuata");

        if (getTableColumnElement("main.Righe.Consegne",0, 1).getText().equals("2"))
            rowElement2 = getTableRowElement("main.Righe.Consegne",0);
        else if (getTableColumnElement("main.Righe.Consegne",1, 1).getText().equals("2"))
            rowElement2 = getTableRowElement("main.Righe.Consegne",1);
        else
            Assert.fail("Riga consegna 2 non individuata");

        //Verifico che la riga consegna n. 1 sia in stato ‘EVASA’ e la consegna n. 2 ancora in stato INSERITA. Entrambe ‘Non Associate’.
        assertEquals("Evasa", getTableColumnElement(rowElement1,5).getText());
        assertEquals("Inserita", getTableColumnElement(rowElement2,5).getText());
        assertEquals("Non Associata", getTableColumnElement(rowElement1,6).getText());
        assertEquals("Non Associata", getTableColumnElement(rowElement2,6).getText());

        //Mi posiziono sulla prima consegna
        rowElement1.click();

        //Entro su ‘scrittura economica’.
        doClickButton("submitForm('doVisualizzaEconomica');");

        //Seleziono la prima scrittura che deve essere annullata dopo l'annullamento dell'evasione
        getTableRowElement("mainTable",0).click();
        assertEquals("No", getGrapheneElement("main.attiva").getText());

        doClickButton("doChiudiForm()");

        //Visualizzo le scritture di economica associate
        doClickButton("submitForm('doVisualizzaEconomica');");

        //Seleziono la seconda scrittura nata con la successiva evasione
        getTableRowElement("mainTable",1).click();

        assertEquals("Si", getGrapheneElement("main.attiva").getText());
        assertEquals("244,00", getGrapheneElement("main.imTotaleDare").getAttribute("value"));
        assertEquals("244,00", getGrapheneElement("main.imTotaleAvere").getAttribute("value"));

        //Nella sezione ‘Dare’ è presente correttamente il conto di costo: C13003
        doClickButton("doTab('tab','tabDare')");
        assertEquals("C13003", getTableColumnElement("main.MovimentiDare",0,1).getText());
        assertEquals("244,00", getTableColumnElement("main.MovimentiDare",0,5).getText());
        assertThrows("Cannot find Element <tr> with tableName main.MovimentiDare and numberRow: 1", RuntimeException.class, ()->getTableRowElement("main.MovimentiDare",1));

        //Nella sezione ‘Avere’ è presente correttamente il conto fatture da ricevere: P00047.
        doClickButton("doTab('tab','tabAvere')");
        assertEquals("P00047", getTableColumnElement("main.MovimentiAvere",0,1).getText());
        assertEquals("244,00", getTableColumnElement("main.MovimentiAvere",0,5).getText());
        assertThrows("Cannot find Element <tr> with tableName main.MovimentiAvere and numberRow: 1", RuntimeException.class, ()->getTableRowElement("main.MovimentiAvere",1));

        doClickButton("doChiudiForm()");

        //Entro su ‘scrittura analitica’.
        doClickButton("submitForm('doVisualizzaAnalitica');");

        //Seleziono la prima scrittura che deve essere annullata dopo l'annullamento dell'evasione
        getTableRowElement("mainTable",0).click();
        assertEquals("N", getGrapheneElement("main.attiva").getText());

        doClickButton("doChiudiForm()");

        //Visualizzo le scritture di analitica associate
        doClickButton("submitForm('doVisualizzaAnalitica');");

        //Seleziono la seconda scrittura nata con la successiva evasione
        getTableRowElement("mainTable",1).click();

        assertEquals("Y", getGrapheneElement("main.attiva").getText());
        assertEquals("244,00", getGrapheneElement("main.imTotaleMov").getAttribute("value"));

        doClickButton("doTab('tab','tabMovimenti')");
        assertEquals("C13003", getTableColumnElement("main.Movimenti",0,1).getText());
        assertEquals("Dare", getTableColumnElement("main.Movimenti",0,2).getText());
        assertEquals("000.000.000", getTableColumnElement("main.Movimenti",0,3).getText());
        assertEquals("PTEST001", getTableColumnElement("main.Movimenti",0,4).getText());
        assertEquals("244,00", getTableColumnElement("main.Movimenti",0,5).getText());
        assertThrows("Cannot find Element <tr> with tableName main.Movimenti and numberRow: 1", RuntimeException.class, ()->getTableRowElement("main.Movimenti",1));

        doClickButton("doChiudiForm()");
    }
}
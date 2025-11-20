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

import java.util.Optional;

import static org.junit.Assert.*;

/**
 * Test di:
 * 1) creazione ordine con 2 righe consegna
 * 2) evasione riga consegna nr.1
 * 3) annullamento evasione riga consegna nr.1
 * 4) nuova evasione riga consegna nr.1
 * 5) evasione riga consegna nr.2
 * 6) registrazione fattura con riscontro valore su riga consegna nr.2 con Fattura di importo superiore all’ordine e dettaglio fatt. in attesa nota credito
 * 7) modifica fattura con annullamento riscontro valore
 * 8) registrazione nota credito di annullamento totale fattura creata
 */
public class CRUDOrdineAcqBP003Test_IT extends ActionDeployments {
    private static SharedResource sharedResource;

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

    public static final String DATA_ODIERNA = "25/10/2025";

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
    @InSequence(302)
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
        getGrapheneElement("main.nota").writeIntoElement("Prova per documentazione con bene inventariale");

        //Passo sulla tab ‘Fornitore’ ed il terzo è proposto in automatico
        doClickButton("doTab('tab','tabOrdineFornitore')");
        assertEquals("101", getGrapheneElement("main.findFornitore.cd_terzo").getAttribute("value"));

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
        assertEquals("150,000000", getGrapheneElement("main.Righe.prezzoUnitario").getAttribute("value"));

        // Indico tipo consegna: Magazzino
        Select select = new Select(getGrapheneElement("main.Righe.tipoConsegna"));
        select.selectByValue("MAG");

        //Lascio la ‘Data prevista consegna’
        assertNotNull(getGrapheneElement("main.Righe.dtPrevConsegna").getText());

        //Indico il codice magazzino: PT
        doClickButton("doBlankSearch(main.Righe.findMagazzino)");
        getGrapheneElement("main.Righe.findMagazzino.cdMagazzino").writeIntoElement(CD_MAGAZZINO);
        doClickButton("doSearch(main.Righe.findMagazzino)");

        //Passo sulla tab ‘Dati coge/coan’ e verifico che il conto di costo è quello legato alla categoria gruppo del mio articolo (8.1) ed è: A22012.
        //L’analitica correttamente non c’è perchè trattasi di conto patrimoniale
        doClickButton("doTab('tabOrdineAcqDettagli','tabDatiCogeCoan')");

        //Verifico che il conto di costo è quello legato alla categoria gruppo del mio articolo (8.1) ed è: A22012.
        assertEquals("A22012", getGrapheneElement("main.Righe.find_voce_ep.cd_voce_ep").getAttribute("value"));

        // L’analitica correttamente non c’è perchè trattasi di conto patrimoniale
        assertEquals("183,00",getGrapheneElement("imCostoEcoDaRipartire").getAttribute("value"));

        //Ritorno sulla prima tab ‘ordine d’acquisto’
        doClickButton("doTab('tab','tabOrdineAcq')");

        //Salvo
        doClickButton("doSalva()");

        //Restituisce messaggio di creazione eseguita
        Alert alert = browser.switchTo().alert();
        assertEquals(AlertMessage.CREAZIONE_ESEGUITA.value(), alert.getText());
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
        assertEquals(AlertMessage.SALVATAGGIO_ESEGUITO.value(), alert.getText());
        alert.accept();

        //modifico lo stato portandolo ‘Definitivo’
        select.selectByValue("DEF");

        //Salvo
        doClickButton("doSalva()");

        //Restituisce messaggio di assenza obbligazione
        alert = browser.switchTo().alert();
        assertEquals("Sulla consegna 2025/"+CD_NUMERATORE+"/"+pgOrdineCreated+"/1/1 non è indicata l'obbligazione", alert.getText());
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
        assertEquals(AlertMessage.SALVATAGGIO_ESEGUITO.value(), alert.getText());
        alert.accept();
    }

    @Test
    @RunAsClient
    @OperateOnDeployment(TEST_H2)
    @InSequence(303)
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
            Assert.fail("Riga consegna 1 non individuata");

        assertThrows("Cannot find Element <tr> with tableName main.ConsegneDaEvadere and numberRow: 1", RuntimeException.class, () -> getTableRowElement("main.ConsegneDaEvadere", 1));

        //Scelgo la prima consegna
        rowElement1.click();
        doSelectTableRow(rowElement1, "main.ConsegneDaEvadere");

        //Salvo
        doClickButton("doSalva()");
        Alert alert = browser.switchTo().alert();
        assertEquals(AlertMessage.OPERAZIONE_EFFETTUATA.value(), alert.getText());
        alert.accept();
    }

    @Test
    @RunAsClient
    @OperateOnDeployment(TEST_H2)
    @InSequence(304)
    public void testVerificaScritturaOrdine001() {
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

        String pgOrdineCreated = sharedResource.getVal01();
        getGrapheneElement("main.numero").writeIntoElement(pgOrdineCreated);

        doClickButton("doCerca()");
        Alert alert = browser.switchTo().alert();
        assertEquals(AlertMessage.MESSAGE_RICERCA_MONO_RECORD.value(), alert.getText());
        alert.accept();

        //Vado sul dettaglio
        doClickButton("doTab('tab','tabOrdineAcqDettaglio')");
        doSelectTableRow("main.Righe",0);

        doClickButton("doTab('tabOrdineAcqDettagli','tabOrdineRigaResultDetailEcoCoge')");

        assertEquals("A22012", getGrapheneElement("main.Righe.find_voce_ep.cd_voce_ep").getAttribute("value"));

        assertThrows("Cannot find Element <tr> with tableName main.Righe.Dati Coge/Coan and numberRow: 0", RuntimeException.class, () -> getTableRowElement("main.Righe.Dati Coge/Coan", 0));

        //Vado sulla tab ‘consegne’
        doClickButton("doTab('tabOrdineAcqDettagli','tabOrdineConsegna')");

        //Vado sulla tab ‘consegne’ e verifico che la riga consegna n. 1 sia in stato ‘EVASA’ e la consegna
        GrapheneElement rowElement1 = null;
        if (getTableColumnElement("main.Righe.Consegne",0, 1).getText().equals("1"))
            rowElement1 = getTableRowElement("main.Righe.Consegne",0);
        else if (getTableColumnElement("main.Righe.Consegne",1, 1).getText().equals("1"))
            rowElement1 = getTableRowElement("main.Righe.Consegne",1);
        else
            Assert.fail("Riga consegna 1 non individuata");

        //Verifico che la riga consegna n. 1 sia in stato ‘EVASA’ e la consegna n. 2 ancora in stato INSERITA. Entrambe ‘Non Associate’.
        assertEquals("Evasa", getTableColumnElement(rowElement1,5).getText());
        assertEquals("Non Associata", getTableColumnElement(rowElement1,6).getText());

        //Mi posiziono sulla prima consegna
        rowElement1.click();

        //Entro su ‘scrittura economica’.
        doClickButton("submitForm('doVisualizzaEconomica');");

        assertEquals("Si", getGrapheneElement("main.attiva").getText());
        assertEquals("183,00", getGrapheneElement("main.imTotaleDare").getAttribute("value"));
        assertEquals("183,00", getGrapheneElement("main.imTotaleAvere").getAttribute("value"));

        //Nella sezione ‘Dare’ è presente correttamente il conto di costo: C13003
        doClickButton("doTab('tab','tabDare')");
        assertEquals("A22012", getTableColumnElement("main.MovimentiDare",0,1).getText());
        assertEquals("183,00", getTableColumnElement("main.MovimentiDare",0,5).getText());
        assertThrows("Cannot find Element <tr> with tableName main.MovimentiDare and numberRow: 1", RuntimeException.class, ()->getTableRowElement("main.MovimentiDare",1));

        //Nella sezione ‘Avere’ è presente correttamente il conto fatture da ricevere: P00047.
        doClickButton("doTab('tab','tabAvere')");
        assertEquals("P00047", getTableColumnElement("main.MovimentiAvere",0,1).getText());
        assertEquals("183,00", getTableColumnElement("main.MovimentiAvere",0,5).getText());
        assertThrows("Cannot find Element <tr> with tableName main.MovimentiAvere and numberRow: 1", RuntimeException.class, ()->getTableRowElement("main.MovimentiAvere",1));

        doClickButton("doChiudiForm()");
        doClickButton("submitForm('doVisualizzaAnalitica');");

        //Restituisce messaggio di "Scrittura Analitica non presente!"
        alert = browser.switchTo().alert();
        assertEquals("Scrittura Analitica non presente!", alert.getText());
        alert.accept();

        doClickButton("doChiudiForm()");
    }

    @Test
    @RunAsClient
    @OperateOnDeployment(TEST_H2)
    @InSequence(305)
    public void testVerificaBeneInTransito001() {
        //Verifico che il bene sia in transito
        browser.switchTo().parentFrame();
        switchToFrameMenu();

        doApriMenu(AMM);
        doApriMenu(AMM_INVENT);
        doApriMenu(AMM_INVENT_ORDINI);

        doSelezionaMenu(AMM_INVENT_ORDINI_TRANSITO);

        browser.switchTo().parentFrame();
        switchToFrameWorkspace();

        //Ricerco l’unico bene in transito
        doClickButton("doCerca()");
        Alert alert = browser.switchTo().alert();
        assertEquals(AlertMessage.MESSAGE_RICERCA_MONO_RECORD.value(), alert.getText());
        alert.accept();

        assertEquals("Inserito", getGrapheneElement("main.stato").getAttribute("value"));

        String pgOrdineCreated = sharedResource.getVal01();
        assertEquals("2025/" + CD_NUMERATORE + "/" + pgOrdineCreated + "/1/1", getGrapheneElement("main.ordine").getAttribute("value"));

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
        assertEquals(AlertMessage.SALVATAGGIO_ESEGUITO.value(), alert.getText());
        alert.accept();

        //Il transito del bene deve acquisita stato "Completo"
        assertEquals("Completo", getGrapheneElement("main.stato").getAttribute("value"));

        doClickButton("doChiudiForm()");
    }

    /**
     * Test Evasione Consegne di Progetto creato con test precedente
     */
    @Test
    @RunAsClient
    @OperateOnDeployment(TEST_H2)
    @InSequence(306)
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
                        return CD_NUMERATORE.equals(getTableColumnElement(rowElement, 18).getText()) &&
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
}
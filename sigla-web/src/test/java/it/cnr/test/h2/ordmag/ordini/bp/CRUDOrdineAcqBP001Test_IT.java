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
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.webdriver.htmlunit.DroneHtmlUnitDriver;
import org.jboss.arquillian.graphene.GrapheneElement;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Assert;
import org.junit.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;

import java.util.*;

import static org.junit.Assert.*;

public class CRUDOrdineAcqBP001Test_IT extends ActionDeployments {
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

    public static final String CD_UNITA_OPERATIVA = "DRUE";
    public static final String CD_NUMERATORE = "DSA";
    public static final String CD_MAGAZZINO = "CS";
    public static final String UO = "000.000";
    public static final String CDR = "000.000.000";

    public static final String CONTRATTO_ESERCIZIO = "2025";
    public static final String CONTRATTO_NUMERO = "1";

    public static final String BENE_SERVIZIO_CODICE_01 = "191202";

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
    public void testCreaProgetto() {
        switchToFrameDesktop();
        switchToFrameMenu();
        doApriMenu(ORD);
        doApriMenu(ORD_ORDACQ);
        doSelezionaMenu(ORD_ORDACQ_M);

        browser.switchTo().parentFrame();
        switchToFrameWorkspace();

        getGrapheneElement("main.findUnitaOperativaOrd.cdUnitaOperativa").writeIntoElement(CD_UNITA_OPERATIVA);
        doClickButton("doSearch(main.findUnitaOperativaOrd)");

        getGrapheneElement("main.findNumerazioneOrd.cdNumeratore").writeIntoElement(CD_NUMERATORE);
        doClickButton("doSearch(main.findNumerazioneOrd)");

        getGrapheneElement("main.find_contratto.esercizio").writeIntoElement(CONTRATTO_ESERCIZIO);
        getGrapheneElement("main.find_contratto.pg_contratto").writeIntoElement(CONTRATTO_NUMERO);
        doClickButton("doSearch(main.find_contratto)");

        doClickButton("doTab('tab','tabOrdineAcqDettaglio')");

        doClickButton("doAddToCRUD(main.Righe)");

        getGrapheneElement("main.Righe.findBeneServizio.cd_bene_servizio").writeIntoElement(BENE_SERVIZIO_CODICE_01);
        doClickButton("doSearch(main.Righe.findBeneServizio)");

        getGrapheneElement("main.Righe.prezzoUnitario").writeIntoElement("100");
        doClickButton("doOnImportoChange");

        getGrapheneElement("main.Righe.dspQuantita").writeIntoElement("10");
        doClickButton("doOnDspQuantitaChange");

        doClickButton("doBlankSearch(main.Righe.findMagazzino)");
        getGrapheneElement("main.Righe.findMagazzino.cdMagazzino").writeIntoElement(CD_MAGAZZINO);
        doClickButton("doSearch(main.Righe.findMagazzino)");

        doClickButton("doTab('tabOrdineAcqDettagli','tabOrdineConsegna')");

        doSelectTableRow("main.Righe.Consegne",0);

        doClickButton("doSalva()");

        Alert alert = browser.switchTo().alert();
        assertEquals(AlertMessage.CREAZIONE_ESEGUITA.value(), alert.getText());
        alert.accept();

        String pgProgetto = getGrapheneElement("main.numero").getAttribute("value");

        Select select = new Select(getGrapheneElement("main.statoForUpdate"));
        select.selectByValue("APP");

        doClickButton("doSalva()");

        alert = browser.switchTo().alert();
        assertEquals(AlertMessage.SALVATAGGIO_ESEGUITO.value(), alert.getText());
        alert.accept();

        select.selectByValue("DEF");

        doClickButton("doSalva()");

        alert = browser.switchTo().alert();
        assertEquals("Sulla consegna 2025/"+CD_NUMERATORE+"/"+pgProgetto+"/1/1 non è indicata l'obbligazione", alert.getText());
        alert.accept();

        doClickButton("doTab('tab','tabOrdineAcqDettaglio')");

        doSelectTableRow("main.Righe",0);

        doClickButton("doRicercaObbligazione");
        doClickButton("doCerca");

        ((DroneHtmlUnitDriver) browser).findElementByClassName("TableRow").click();
        doClickButton("doSalva()");

        alert = browser.switchTo().alert();
        assertEquals(AlertMessage.SALVATAGGIO_ESEGUITO.value(), alert.getText());
        alert.accept();
    }

    /**
     * Test Evasione Consegne di 2 Progetto precaricati
     */
    @Test
    @RunAsClient
    @OperateOnDeployment(TEST_H2)
    @InSequence(3)
    public void testEvasioneConsegna() {
        browser.switchTo().parentFrame();
        switchToFrameMenu();
        doSelezionaMenu(ORD_EVAORD);

        browser.switchTo().parentFrame();
        switchToFrameWorkspace();

        doClickButton("doBlankSearch(main.findUnitaOperativaOrd)");
        getGrapheneElement("main.findUnitaOperativaOrd.cdUnitaOperativa").writeIntoElement(CD_UNITA_OPERATIVA);
        doClickButton("doSearch(main.findUnitaOperativaOrd)");

        doClickButton("doBlankSearch(main.findMagazzino)");
        getGrapheneElement("main.findMagazzino.cdMagazzino").writeIntoElement(CD_MAGAZZINO);
        doClickButton("doSearch(main.findMagazzino)");

        GregorianCalendar dataBollaConsegna = (GregorianCalendar) GregorianCalendar.getInstance();
        dataBollaConsegna.set(Calendar.DAY_OF_MONTH,3);
        dataBollaConsegna.set(Calendar.MONTH,Calendar.JANUARY);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");

        getGrapheneElement("main.dataBolla").writeIntoElement(sdf.format(dataBollaConsegna.getTime().getTime()));
        doClickButton("doOnDtBollaChange");

        getGrapheneElement("main.numeroBolla").writeIntoElement("1");

        getGrapheneElement("main.dataConsegna").writeIntoElement(sdf.format(dataBollaConsegna.getTime().getTime()));
        doClickButton("doOnDtConsegnaChange");

        doClickButton("doCercaConsegneDaEvadere");

        //Trovo la riga della consegna 1 e 2 dell'ordine 2025/"+CD_NUMERATORE+"/2/1/1
        String keyRowElement1 = "2025/"+CD_NUMERATORE+"/1/1/1";
        String keyRowElement2 = "2025/"+CD_NUMERATORE+"/2/1/1";

        GrapheneElement rowElement1=null, rowElement2=null;
        for (int riga = 0; riga < 10; riga++) {
            try {
                if (getTableColumnElement("main.ConsegneDaEvadere", riga, 1).getText().equals(keyRowElement1))
                    rowElement1 = getTableRowElement("main.ConsegneDaEvadere", riga);
                else if (getTableColumnElement("main.ConsegneDaEvadere", riga, 1).getText().equals(keyRowElement2))
                    rowElement2 = getTableRowElement("main.ConsegneDaEvadere", riga);
            } catch (RuntimeException ignored) {
            }
        }

        assertNotNull("Riga Consegna " + keyRowElement1 + " non individuata", rowElement1);
        assertNotNull("Riga Consegna " + keyRowElement2 + " non individuata", rowElement2);

        //Seleziono la prima consegna
        rowElement1.click();
        doSelectTableRow(rowElement1, "main.ConsegneDaEvadere");

        getGrapheneElement("main.ConsegneDaEvadere.quantitaEvasa").clear();
        getGrapheneElement("main.ConsegneDaEvadere.quantitaEvasa").writeIntoElement("1");
        doClickButton("confirmModalInputChange(this,'main.ConsegneDaEvadere.quantitaEvasa','doDefault')");

        //Seleziono la seconda consegna
        rowElement2.click();
        doSelectTableRow(rowElement2, "main.ConsegneDaEvadere");

        getGrapheneElement("main.ConsegneDaEvadere.quantitaEvasa").clear();
        getGrapheneElement("main.ConsegneDaEvadere.quantitaEvasa").writeIntoElement("1");
        doClickButton("confirmModalInputChange(this,'main.ConsegneDaEvadere.quantitaEvasa','doDefault')");

        doClickButton("doSalva()");
        Alert alert = browser.switchTo().alert();
        assertEquals("Per la consegna " + keyRowElement2 + " è necessario indicare se bisogna solo sdoppiare la riga o anche evaderla forzatamente", alert.getText());
        alert.accept();

        rowElement2.click();
        Select select = new Select(getGrapheneElement("main.ConsegneDaEvadere.operazioneQuantitaEvasaMinore"));
        select.selectByValue("C");

        doClickButton("doSalva()");
        alert = browser.switchTo().alert();
        assertEquals("Per la consegna " + keyRowElement1 + " è necessario indicare se bisogna solo sdoppiare la riga o anche evaderla forzatamente", alert.getText());
        alert.accept();

        rowElement1.click();
        select = new Select(getGrapheneElement("main.ConsegneDaEvadere.operazioneQuantitaEvasaMinore"));
        select.selectByValue("C");

        doClickButton("doSalva()");
        alert = browser.switchTo().alert();
            assertEquals(AlertMessage.OPERAZIONE_EFFETTUATA.value(), alert.getText());
        alert.accept();

        //Verifico che la scrittura sull'ordine nr. 1 sia stata eseguita correttamente
        browser.switchTo().parentFrame();
        switchToFrameMenu();

        doSelezionaMenu(ORD_ORDACQ_M);

        browser.switchTo().parentFrame();
        switchToFrameWorkspace();

        doClickButton("doNuovaRicerca()");

        getGrapheneElement("main.findUnitaOperativaOrd.cdUnitaOperativa").writeIntoElement(CD_UNITA_OPERATIVA);
        doClickButton("doSearch(main.findUnitaOperativaOrd)");

        getGrapheneElement("main.findNumerazioneOrd.cdNumeratore").writeIntoElement(CD_NUMERATORE);
        doClickButton("doSearch(main.findNumerazioneOrd)");

        getGrapheneElement("main.numero").writeIntoElement("1");

        doClickButton("doCerca()");
        alert = browser.switchTo().alert();
        assertEquals(AlertMessage.MESSAGE_RICERCA_MONO_RECORD.value(), alert.getText());
        alert.accept();

        doClickButton("doTab('tab','tabOrdineAcqDettaglio')");
        doSelectTableRow("main.Righe",0);

        doClickButton("doTab('tabOrdineAcqDettagli','tabOrdineRigaResultDetailEcoCoge')");

        assertEquals("C13003", getGrapheneElement("main.Righe.find_voce_ep.cd_voce_ep").getAttribute("value"));

        doSelectTableRow("main.Righe.Dati Coge/Coan",0);

        assertEquals("C13003", getTableColumnElement("main.Righe.Dati Coge/Coan",0,1).getText());
        assertEquals("PTEST002", getTableColumnElement("main.Righe.Dati Coge/Coan",0,2).getText());
        assertEquals("000.000.000", getTableColumnElement("main.Righe.Dati Coge/Coan",0,3).getText());
        assertEquals("1.220,00", getTableColumnElement("main.Righe.Dati Coge/Coan",0,4).getText());
        assertThrows("Cannot find Element <tr> with tableName main.Righe.Dati Coge/Coan and numberRow: 1", RuntimeException.class, ()->getTableRowElement("main.Righe.Dati Coge/Coan",1));

        doClickButton("doTab('tabOrdineAcqDettagli','tabOrdineConsegna')");
        doSelectTableRow("main.Righe.Consegne",0);

        doClickButton("submitForm('doVisualizzaEconomica');");

        assertEquals("Si", getGrapheneElement("main.attiva").getText());
        assertEquals("122,00", getGrapheneElement("main.imTotaleDare").getAttribute("value"));
        assertEquals("122,00", getGrapheneElement("main.imTotaleAvere").getAttribute("value"));

        doClickButton("doTab('tab','tabDare')");
        assertEquals("C13003", getTableColumnElement("main.MovimentiDare",0,1).getText());
        assertEquals("122,00", getTableColumnElement("main.MovimentiDare",0,5).getText());
        assertThrows("Cannot find Element <tr> with tableName main.MovimentiDare and numberRow: 1", RuntimeException.class, ()->getTableRowElement("main.MovimentiDare",1));

        doClickButton("doTab('tab','tabAvere')");
        assertEquals("P00047", getTableColumnElement("main.MovimentiAvere",0,1).getText());
        assertEquals("122,00", getTableColumnElement("main.MovimentiAvere",0,5).getText());
        assertThrows("Cannot find Element <tr> with tableName main.MovimentiAvere and numberRow: 1", RuntimeException.class, ()->getTableRowElement("main.MovimentiAvere",1));

        doClickButton("doChiudiForm()");
        doClickButton("submitForm('doVisualizzaAnalitica');");

        assertEquals("Y", getGrapheneElement("main.attiva").getText());
        assertEquals("122,00", getGrapheneElement("main.imTotaleMov").getAttribute("value"));

        doClickButton("doTab('tab','tabMovimenti')");
        assertEquals("C13003", getTableColumnElement("main.Movimenti",0,1).getText());
        assertEquals("Dare", getTableColumnElement("main.Movimenti",0,2).getText());
        assertEquals("000.000.000", getTableColumnElement("main.Movimenti",0,3).getText());
        assertEquals("PTEST002", getTableColumnElement("main.Movimenti",0,4).getText());
        assertEquals("122,00", getTableColumnElement("main.Movimenti",0,5).getText());
        assertThrows("Cannot find Element <tr> with tableName main.Movimenti and numberRow: 1", RuntimeException.class, ()->getTableRowElement("main.Movimenti",1));

        doClickButton("doChiudiForm()");

        //Verifico che la scrittura sull'ordine nr. 2 sia stata eseguita correttamente
        doClickButton("doNuovaRicerca()");

        getGrapheneElement("main.findUnitaOperativaOrd.cdUnitaOperativa").writeIntoElement(CD_UNITA_OPERATIVA);
        doClickButton("doSearch(main.findUnitaOperativaOrd)");

        getGrapheneElement("main.findNumerazioneOrd.cdNumeratore").writeIntoElement(CD_NUMERATORE);
        doClickButton("doSearch(main.findNumerazioneOrd)");

        getGrapheneElement("main.numero").writeIntoElement("2");

        doClickButton("doCerca()");
        alert = browser.switchTo().alert();
        assertEquals(AlertMessage.MESSAGE_RICERCA_MONO_RECORD.value(), alert.getText());
        alert.accept();

        doClickButton("doTab('tab','tabOrdineAcqDettaglio')");
        doSelectTableRow("main.Righe",0);

        doClickButton("doTab('tabOrdineAcqDettagli','tabOrdineRigaResultDetailEcoCoge')");

        assertEquals("C13001", getGrapheneElement("main.Righe.find_voce_ep.cd_voce_ep").getAttribute("value"));

        doSelectTableRow("main.Righe.Dati Coge/Coan",0);

        assertEquals("C13001", getTableColumnElement("main.Righe.Dati Coge/Coan",0,1).getText());
        assertEquals("PTEST002", getTableColumnElement("main.Righe.Dati Coge/Coan",0,2).getText());
        assertEquals("000.000.000", getTableColumnElement("main.Righe.Dati Coge/Coan",0,3).getText());
        assertEquals("1.220,00", getTableColumnElement("main.Righe.Dati Coge/Coan",0,4).getText());
        assertThrows("Cannot find Element <tr> with tableName main.Righe.Dati Coge/Coan and numberRow: 1", RuntimeException.class, ()->getTableRowElement("main.Righe.Dati Coge/Coan",1));

        doClickButton("doTab('tabOrdineAcqDettagli','tabOrdineConsegna')");
        doSelectTableRow("main.Righe.Consegne",0);

        doClickButton("submitForm('doVisualizzaEconomica');");

        assertEquals("Si", getGrapheneElement("main.attiva").getText());
        assertEquals("122,00", getGrapheneElement("main.imTotaleDare").getAttribute("value"));
        assertEquals("122,00", getGrapheneElement("main.imTotaleAvere").getAttribute("value"));

        doClickButton("doTab('tab','tabDare')");
        assertEquals("C13001", getTableColumnElement("main.MovimentiDare",0,1).getText());
        assertEquals("122,00", getTableColumnElement("main.MovimentiDare",0,5).getText());
        assertThrows("Cannot find Element <tr> with tableName main.MovimentiDare and numberRow: 1", RuntimeException.class, ()->getTableRowElement("main.MovimentiDare",1));

        doClickButton("doTab('tab','tabAvere')");
        assertEquals("P00047", getTableColumnElement("main.MovimentiAvere",0,1).getText());
        assertEquals("122,00", getTableColumnElement("main.MovimentiAvere",0,5).getText());
        assertThrows("Cannot find Element <tr> with tableName main.MovimentiAvere and numberRow: 1", RuntimeException.class, ()->getTableRowElement("main.MovimentiAvere",1));

        doClickButton("doChiudiForm()");
        doClickButton("submitForm('doVisualizzaAnalitica');");

        assertEquals("Y", getGrapheneElement("main.attiva").getText());
        assertEquals("122,00", getGrapheneElement("main.imTotaleMov").getAttribute("value"));

        doClickButton("doTab('tab','tabMovimenti')");
        assertEquals("C13001", getTableColumnElement("main.Movimenti",0,1).getText());
        assertEquals("Dare", getTableColumnElement("main.Movimenti",0,2).getText());
        assertEquals("000.000.000", getTableColumnElement("main.Movimenti",0,3).getText());
        assertEquals("PTEST002", getTableColumnElement("main.Movimenti",0,4).getText());
        assertEquals("122,00", getTableColumnElement("main.Movimenti",0,5).getText());
        assertThrows("Cannot find Element <tr> with tableName main.Movimenti and numberRow: 1", RuntimeException.class, ()->getTableRowElement("main.Movimenti",1));

        doClickButton("doChiudiForm()");
    }

    @Test
    @RunAsClient
    @OperateOnDeployment(TEST_H2)
    @InSequence(4)
    public void testRiscontroValore001() {
        browser.switchTo().parentFrame();
        switchToFrameMenu();
        doApriMenu(AMM);
        doApriMenu(AMM_FATTUR);
        doApriMenu(AMM_FATTUR_FATPAS);
        doSelezionaMenu(AMM_FATTUR_FATPAS_ELE);

        browser.switchTo().parentFrame();
        switchToFrameWorkspace();

        getGrapheneElement("main.identificativoSdi").writeIntoElement("90000000000");

        Select select = new Select(getGrapheneElement("main.statoDocumento"));
        select.selectByValue("");

        doClickButton("doCerca()");
        Alert alert = browser.switchTo().alert();
        assertEquals(AlertMessage.MESSAGE_RICERCA_MONO_RECORD.value(), alert.getText());
        alert.accept();

        doClickButton("submitForm('doCompilaFattura')");

        getGrapheneElement("comando.doYes").click();

        select = new Select(getGrapheneElement("main.stato_liquidazione"));
        select.selectByValue("LIQ");

        getGrapheneElement("main.flDaOrdini").click();

        getGrapheneElement("main.ds_fattura_passiva").writeIntoElement("RISCONTRO VALORE TEST");

        doClickButton("doTab('tab','tabFatturaPassivaOrdini')");

        System.out.println("1 - " +browser.getPageSource().contains("Fattura Passiva - Inserimento"));

        doClickButton("submitForm('doSelezionaOrdini')");

        //Seleziono la prima riga ordini
        Optional<GrapheneElement> element = browser.findElements(By.tagName("tr"))
                .stream()
                .filter(GrapheneElement.class::isInstance)
                .map(GrapheneElement.class::cast)
                .filter(rowElement -> {
                    try {
                        return getTableColumnElement(rowElement, 3).getText().contains("2025") &&
                                CD_NUMERATORE.equals(getTableColumnElement(rowElement, 4).getText()) &&
                                "1".equals(getTableColumnElement(rowElement, 5).getText()) &&
                                "1".equals(getTableColumnElement(rowElement, 6).getText()) &&
                                "1".equals(getTableColumnElement(rowElement, 7).getText());
                    } catch (java.lang.RuntimeException ex) {
                        return false;
                    }
                })
                .findAny();

        Assert.assertTrue(element.isPresent());

        element.get().findElement(By.name("mainTable.selection")).click();

        //Verifico che esiste anche l'altra consegna
        Assert.assertTrue(browser.findElements(By.tagName("tr"))
                .stream()
                .filter(GrapheneElement.class::isInstance)
                .map(GrapheneElement.class::cast)
                .anyMatch(rowElement -> {
                    try {
                        return getTableColumnElement(rowElement, 3).getText().contains("2025") &&
                                CD_NUMERATORE.equals(getTableColumnElement(rowElement, 4).getText()) &&
                                "2".equals(getTableColumnElement(rowElement, 5).getText()) &&
                                "1".equals(getTableColumnElement(rowElement, 6).getText()) &&
                                "1".equals(getTableColumnElement(rowElement, 7).getText());
                    } catch (java.lang.RuntimeException ex) {
                        return false;
                    }
                }));

        doClickButton("submitForm('doMultipleSelection')");

        doSelectTableRow("main.Ordini",0);

        getGrapheneElement("main.Ordini.prezzoUnitarioRett").writeIntoElement("99");
        doClickButton("doRettificaConsegna");

        doClickButton("submitForm('doConfermaRiscontroAValore')");

        alert = browser.switchTo().alert();
        assertEquals(AlertMessage.OPERAZIONE_EFFETTUATA.value(), alert.getText());
        alert.accept();

        doClickButton("doSalva()");

        alert = browser.switchTo().alert();
        assertEquals(AlertMessage.CREAZIONE_ESEGUITA.value(), alert.getText());
        alert.accept();

        doClickButton("doTab('tab','tabEconomica')");
        assertEquals("P00047", getTableColumnElement("main.Movimenti Dare",0,1).getText());
        assertEquals("120,78", getTableColumnElement("main.Movimenti Dare",0,5).getText());
        assertThrows("Cannot find Element <tr> with tableName 'main.Movimenti Dare' and numberRow: 1", RuntimeException.class, ()->getTableRowElement("main.Movimenti Dare",1));

        assertEquals("P13003", getTableColumnElement("main.Movimenti Avere",0,1).getText());
        assertEquals("99,00", getTableColumnElement("main.Movimenti Avere",0,5).getText());

        assertEquals("P71012I", getTableColumnElement("main.Movimenti Avere",1,1).getText());
        assertEquals("21,78", getTableColumnElement("main.Movimenti Avere",1,5).getText());

        assertThrows("Cannot find Element <tr> with tableName 'main.Movimenti Avere' and numberRow: 1", RuntimeException.class, ()->getTableRowElement("main.Movimenti Avere",2));

        doClickButton("doTab('tab','tabAnalitica')");
        assertThrows("Cannot find Element <tr> with tableName 'main.Movimenti Analitici' and numberRow: 0", RuntimeException.class, ()->getTableRowElement("main.Movimenti Analitici",0));
    }

    @Test
    @RunAsClient
    @OperateOnDeployment(TEST_H2)
    @InSequence(5)
    public void testVerificaScrittureOrdine() {
        //Verifico che la scrittura sull'ordine sia stata eseguita correttamente
        browser.switchTo().parentFrame();
        switchToFrameMenu();

        doSelezionaMenu(ORD_ORDACQ_M);

        browser.switchTo().parentFrame();
        switchToFrameWorkspace();

        doClickButton("doNuovaRicerca()");

        getGrapheneElement("main.findUnitaOperativaOrd.cdUnitaOperativa").writeIntoElement(CD_UNITA_OPERATIVA);
        doClickButton("doSearch(main.findUnitaOperativaOrd)");

        getGrapheneElement("main.findNumerazioneOrd.cdNumeratore").writeIntoElement(CD_NUMERATORE);
        doClickButton("doSearch(main.findNumerazioneOrd)");

        getGrapheneElement("main.numero").writeIntoElement("1");

        doClickButton("doCerca()");
        Alert alert = browser.switchTo().alert();
        assertEquals(AlertMessage.MESSAGE_RICERCA_MONO_RECORD.value(), alert.getText());
        alert.accept();

        doClickButton("doTab('tab','tabOrdineAcqDettaglio')");
        doSelectTableRow("main.Righe",0);

        doClickButton("doTab('tabOrdineAcqDettagli','tabOrdineRigaResultDetailEcoCoge')");

        assertEquals("C13003", getGrapheneElement("main.Righe.find_voce_ep.cd_voce_ep").getAttribute("value"));

        doSelectTableRow("main.Righe.Dati Coge/Coan",0);

        assertEquals("C13003", getTableColumnElement("main.Righe.Dati Coge/Coan",0,1).getText());
        assertEquals("PTEST002", getTableColumnElement("main.Righe.Dati Coge/Coan",0,2).getText());
        assertEquals("000.000.000", getTableColumnElement("main.Righe.Dati Coge/Coan",0,3).getText());
        assertEquals("1.218,78", getTableColumnElement("main.Righe.Dati Coge/Coan",0,4).getText());
        assertThrows("Cannot find Element <tr> with tableName main.Righe.Dati Coge/Coan and numberRow: 1", RuntimeException.class, ()->getTableRowElement("main.Righe.Dati Coge/Coan",1));

        doClickButton("doTab('tabOrdineAcqDettagli','tabOrdineConsegna')");
        doSelectTableRow("main.Righe.Consegne",0);

        doClickButton("submitForm('doVisualizzaEconomica');");
        getTableRowElement("mainTable",0).click();

        assertEquals("Si", getGrapheneElement("main.attiva").getText());
        assertEquals("122,00", getGrapheneElement("main.imTotaleDare").getAttribute("value"));
        assertEquals("122,00", getGrapheneElement("main.imTotaleAvere").getAttribute("value"));

        doClickButton("doTab('tab','tabDare')");
        assertEquals("C13003", getTableColumnElement("main.MovimentiDare",0,1).getText());
        assertEquals("122,00", getTableColumnElement("main.MovimentiDare",0,5).getText());
        assertThrows("Cannot find Element <tr> with tableName main.MovimentiDare and numberRow: 1", RuntimeException.class, ()->getTableRowElement("main.MovimentiDare",1));

        doClickButton("doTab('tab','tabAvere')");
        assertEquals("P00047", getTableColumnElement("main.MovimentiAvere",0,1).getText());
        assertEquals("122,00", getTableColumnElement("main.MovimentiAvere",0,5).getText());
        assertThrows("Cannot find Element <tr> with tableName main.MovimentiAvere and numberRow: 1", RuntimeException.class, ()->getTableRowElement("main.MovimentiAvere",1));

        doClickButton("doChiudiForm()");

        doClickButton("submitForm('doVisualizzaEconomica');");
        getTableRowElement("mainTable",1).click();

        assertEquals("Si", getGrapheneElement("main.attiva").getText());
        assertEquals("1,22", getGrapheneElement("main.imTotaleDare").getAttribute("value"));
        assertEquals("1,22", getGrapheneElement("main.imTotaleAvere").getAttribute("value"));

        doClickButton("doTab('tab','tabDare')");
        assertEquals("P00047", getTableColumnElement("main.MovimentiDare",0,1).getText());
        assertEquals("1,22", getTableColumnElement("main.MovimentiDare",0,5).getText());
        assertThrows("Cannot find Element <tr> with tableName main.MovimentiDare and numberRow: 1", RuntimeException.class, ()->getTableRowElement("main.MovimentiDare",1));

        doClickButton("doTab('tab','tabAvere')");
        assertEquals("C13003", getTableColumnElement("main.MovimentiAvere",0,1).getText());
        assertEquals("1,22", getTableColumnElement("main.MovimentiAvere",0,5).getText());
        assertThrows("Cannot find Element <tr> with tableName main.MovimentiAvere and numberRow: 1", RuntimeException.class, ()->getTableRowElement("main.MovimentiAvere",1));

        doClickButton("doChiudiForm()");

        doClickButton("submitForm('doVisualizzaEconomica');");
        assertThrows("Cannot find Element <tr> with tableName 'mainTable' and numberRow: 2", RuntimeException.class, ()->getTableRowElement("mainTable",2));

        doClickButton("doChiudiForm()");

        doClickButton("submitForm('doVisualizzaAnalitica');");
        getTableRowElement("mainTable",0).click();

        assertEquals("Y", getGrapheneElement("main.attiva").getText());
        assertEquals("122,00", getGrapheneElement("main.imTotaleMov").getAttribute("value"));

        doClickButton("doTab('tab','tabMovimenti')");
        assertEquals("C13003", getTableColumnElement("main.Movimenti",0,1).getText());
        assertEquals("Dare", getTableColumnElement("main.Movimenti",0,2).getText());
        assertEquals("000.000.000", getTableColumnElement("main.Movimenti",0,3).getText());
        assertEquals("PTEST002", getTableColumnElement("main.Movimenti",0,4).getText());
        assertEquals("122,00", getTableColumnElement("main.Movimenti",0,5).getText());
        assertThrows("Cannot find Element <tr> with tableName main.Movimenti and numberRow: 1", RuntimeException.class, ()->getTableRowElement("main.Movimenti",1));

        doClickButton("doChiudiForm()");

        doClickButton("submitForm('doVisualizzaAnalitica');");
        getTableRowElement("mainTable",1).click();

        assertEquals("Y", getGrapheneElement("main.attiva").getText());
        assertEquals("1,22", getGrapheneElement("main.imTotaleMov").getAttribute("value"));

        doClickButton("doTab('tab','tabMovimenti')");
        assertEquals("C13003", getTableColumnElement("main.Movimenti",0,1).getText());
        assertEquals("Avere", getTableColumnElement("main.Movimenti",0,2).getText());
        assertEquals("000.000.000", getTableColumnElement("main.Movimenti",0,3).getText());
        assertEquals("PTEST002", getTableColumnElement("main.Movimenti",0,4).getText());
        assertEquals("1,22", getTableColumnElement("main.Movimenti",0,5).getText());
        assertThrows("Cannot find Element <tr> with tableName main.Movimenti and numberRow: 1", RuntimeException.class, ()->getTableRowElement("main.Movimenti",1));

        doClickButton("doChiudiForm()");
        doClickButton("submitForm('doVisualizzaMovimento');");

        assertEquals("C01", getTableColumnElement("mainTable",0,3).getText());
        assertEquals("122", getTableColumnElement("mainTable",0,9).getText());

        assertEquals("C20", getTableColumnElement("mainTable",1,3).getText());
        assertEquals("1,22", getTableColumnElement("mainTable",1,9).getText());

        assertThrows("Cannot find Element <tr> with tableName 'mainTable' and numberRow: 2", RuntimeException.class, ()->getTableRowElement("mainTable",2));
    }

    @Test
    @RunAsClient
    @OperateOnDeployment(TEST_H2)
    @InSequence(6)
    public void testModificaRiscontroValore() {
        browser.switchTo().parentFrame();
        switchToFrameMenu();
        doSelezionaMenu(AMM_FATTUR_FATPAS_ELE);

        browser.switchTo().parentFrame();
        switchToFrameWorkspace();

        getGrapheneElement("main.identificativoSdi").writeIntoElement("90000000000");

        Select select = new Select(getGrapheneElement("main.statoDocumento"));
        select.selectByValue("");

        doClickButton("doCerca()");
        Alert alert = browser.switchTo().alert();
        assertEquals(AlertMessage.MESSAGE_RICERCA_MONO_RECORD.value(), alert.getText());
        alert.accept();

        doClickButton("submitForm('doVisualizzaFattura')");

        doClickButton("doTab('tab','tabFatturaPassivaOrdini')");

        //Seleziono la consegna DSA/1/1/1 collegata alla fattura
        doSelectTableRow("main.Ordini",0);

        //Rimuovo la consegna DSA/1/1/1 collegata alla fattura
        doClickButton("submitForm('doRemoveFromCRUD(main.Ordini)')");

        doClickButton("submitForm('doSelezionaOrdini')");

        //Cerco la consegna DSA/2/1/1 proposta da collegare
        Optional<GrapheneElement> element = browser.findElements(By.tagName("tr"))
                .stream()
                .filter(GrapheneElement.class::isInstance)
                .map(GrapheneElement.class::cast)
                .filter(rowElement -> {
                    try {
                        return getTableColumnElement(rowElement, 3).getText().contains("2025") &&
                                CD_NUMERATORE.equals(getTableColumnElement(rowElement, 4).getText()) &&
                                "2".equals(getTableColumnElement(rowElement, 5).getText()) &&
                                "1".equals(getTableColumnElement(rowElement, 6).getText()) &&
                                "1".equals(getTableColumnElement(rowElement, 7).getText());
                    } catch (java.lang.RuntimeException ex) {
                        return false;
                    }
                })
                .findAny();

        Assert.assertTrue(element.isPresent());

        //Seleziono la consegna DSA/2/1/1
        element.get().findElement(By.name("mainTable.selection")).click();

        //Verifico che sia proposta anche la consegna DSA/1/1/1 che non collego
        Assert.assertTrue(browser.findElements(By.tagName("tr"))
                .stream()
                .filter(GrapheneElement.class::isInstance)
                .map(GrapheneElement.class::cast)
                .anyMatch(rowElement -> {
                    try {
                        return getTableColumnElement(rowElement, 3).getText().contains("2025") &&
                                CD_NUMERATORE.equals(getTableColumnElement(rowElement, 4).getText()) &&
                                "1".equals(getTableColumnElement(rowElement, 5).getText()) &&
                                "1".equals(getTableColumnElement(rowElement, 6).getText()) &&
                                "1".equals(getTableColumnElement(rowElement, 7).getText());
                    } catch (java.lang.RuntimeException ex) {
                        return false;
                    }
                }));

        //Collego la consegna DSA/2/1/1 alla fattura
        doClickButton("submitForm('doMultipleSelection')");

        doSelectTableRow("main.Ordini",0);

        //Rettifico il presso della consegna DSA/2/1/1
        getGrapheneElement("main.Ordini.prezzoUnitarioRett").writeIntoElement("99");
        doClickButton("doRettificaConsegna");

        //Confermo il riscontro a valore
        doClickButton("submitForm('doConfermaRiscontroAValore')");

        alert = browser.switchTo().alert();
        assertEquals(AlertMessage.OPERAZIONE_EFFETTUATA.value(), alert.getText());
        alert.accept();

        doClickButton("doSalva()");

        alert = browser.switchTo().alert();
        assertEquals(AlertMessage.SALVATAGGIO_ESEGUITO.value(), alert.getText());
        alert.accept();

        doClickButton("doTab('tab','tabEconomica')");
        assertEquals("P00047", getTableColumnElement("main.Movimenti Dare",0,1).getText());
        assertEquals("120,78", getTableColumnElement("main.Movimenti Dare",0,5).getText());
        assertThrows("Cannot find Element <tr> with tableName 'main.Movimenti Dare' and numberRow: 1", RuntimeException.class, ()->getTableRowElement("main.Movimenti Dare",1));

        assertEquals("P13001", getTableColumnElement("main.Movimenti Avere",0,1).getText());
        assertEquals("99,00", getTableColumnElement("main.Movimenti Avere",0,5).getText());

        assertEquals("P71012I", getTableColumnElement("main.Movimenti Avere",1,1).getText());
        assertEquals("21,78", getTableColumnElement("main.Movimenti Avere",1,5).getText());

        assertThrows("Cannot find Element <tr> with tableName 'main.Movimenti Avere' and numberRow: 1", RuntimeException.class, ()->getTableRowElement("main.Movimenti Avere",2));
    }

    @Test
    @RunAsClient
    @OperateOnDeployment(TEST_H2)
    @InSequence(7)
    public void testVerificaScrittureOrdineBis() {
        //Verifico che la scrittura sull'ordine sia stata eseguita correttamente
        browser.switchTo().parentFrame();
        switchToFrameMenu();

        doSelezionaMenu(ORD_ORDACQ_M);

        browser.switchTo().parentFrame();
        switchToFrameWorkspace();

        //Cerco l'ordine DSA/1/1/1 per verificare che l'annullamento del riscontro a valore abbia rimesso i valori corretti
        doClickButton("doNuovaRicerca()");

        getGrapheneElement("main.findUnitaOperativaOrd.cdUnitaOperativa").writeIntoElement(CD_UNITA_OPERATIVA);
        doClickButton("doSearch(main.findUnitaOperativaOrd)");

        getGrapheneElement("main.findNumerazioneOrd.cdNumeratore").writeIntoElement(CD_NUMERATORE);
        doClickButton("doSearch(main.findNumerazioneOrd)");

        getGrapheneElement("main.numero").writeIntoElement("1");

        doClickButton("doCerca()");
        Alert alert = browser.switchTo().alert();
        assertEquals(AlertMessage.MESSAGE_RICERCA_MONO_RECORD.value(), alert.getText());
        alert.accept();

        doClickButton("doTab('tab','tabOrdineAcqDettaglio')");
        doSelectTableRow("main.Righe",0);

        doClickButton("doTab('tabOrdineAcqDettagli','tabOrdineRigaResultDetailEcoCoge')");

        assertEquals("C13003", getGrapheneElement("main.Righe.find_voce_ep.cd_voce_ep").getAttribute("value"));

        doSelectTableRow("main.Righe.Dati Coge/Coan",0);

        //Verifico che i dati analitici della riga dell'ordine DSA/1/1/1 siano ritornati al valore originario (1220,00) dopo che la consegna è stata scollegata dalla fattura
        assertEquals("C13003", getTableColumnElement("main.Righe.Dati Coge/Coan",0,1).getText());
        assertEquals("PTEST002", getTableColumnElement("main.Righe.Dati Coge/Coan",0,2).getText());
        assertEquals("000.000.000", getTableColumnElement("main.Righe.Dati Coge/Coan",0,3).getText());
        assertEquals("1.220,00", getTableColumnElement("main.Righe.Dati Coge/Coan",0,4).getText());
        assertThrows("Cannot find Element <tr> with tableName main.Righe.Dati Coge/Coan and numberRow: 1", RuntimeException.class, ()->getTableRowElement("main.Righe.Dati Coge/Coan",1));

        //Seleziono la consegna DSA/1/1/1 scollegata dalla fattura
        doClickButton("doTab('tabOrdineAcqDettagli','tabOrdineConsegna')");
        doSelectTableRow("main.Righe.Consegne",0);

        //Visualizzo le scritture di economica associate
        doClickButton("submitForm('doVisualizzaEconomica');");

        //Seleziono la prima scrittura nata in fase di evasione che deve essere rimasta costante
        getTableRowElement("mainTable",0).click();

        assertEquals("Si", getGrapheneElement("main.attiva").getText());
        assertEquals("122,00", getGrapheneElement("main.imTotaleDare").getAttribute("value"));
        assertEquals("122,00", getGrapheneElement("main.imTotaleAvere").getAttribute("value"));

        doClickButton("doTab('tab','tabDare')");
        assertEquals("C13003", getTableColumnElement("main.MovimentiDare",0,1).getText());
        assertEquals("122,00", getTableColumnElement("main.MovimentiDare",0,5).getText());
        assertThrows("Cannot find Element <tr> with tableName main.MovimentiDare and numberRow: 1", RuntimeException.class, ()->getTableRowElement("main.MovimentiDare",1));

        doClickButton("doTab('tab','tabAvere')");
        assertEquals("P00047", getTableColumnElement("main.MovimentiAvere",0,1).getText());
        assertEquals("122,00", getTableColumnElement("main.MovimentiAvere",0,5).getText());
        assertThrows("Cannot find Element <tr> with tableName main.MovimentiAvere and numberRow: 1", RuntimeException.class, ()->getTableRowElement("main.MovimentiAvere",1));

        doClickButton("doChiudiForm()");

        //Visualizzo le scritture di economica associate
        doClickButton("submitForm('doVisualizzaEconomica');");

        //Seleziono la seconda scrittura nata in fase di riscontro a valore che deve essere stata annullata dopo lo scollegamento dalla fattura
        getTableRowElement("mainTable",1).click();

        assertEquals("No", getGrapheneElement("main.attiva").getText());
        assertEquals("1,22", getGrapheneElement("main.imTotaleDare").getAttribute("value"));
        assertEquals("1,22", getGrapheneElement("main.imTotaleAvere").getAttribute("value"));

        doClickButton("doTab('tab','tabDare')");
        assertEquals("P00047", getTableColumnElement("main.MovimentiDare",0,1).getText());
        assertEquals("1,22", getTableColumnElement("main.MovimentiDare",0,5).getText());
        assertThrows("Cannot find Element <tr> with tableName main.MovimentiDare and numberRow: 1", RuntimeException.class, ()->getTableRowElement("main.MovimentiDare",1));

        doClickButton("doTab('tab','tabAvere')");
        assertEquals("C13003", getTableColumnElement("main.MovimentiAvere",0,1).getText());
        assertEquals("1,22", getTableColumnElement("main.MovimentiAvere",0,5).getText());
        assertThrows("Cannot find Element <tr> with tableName main.MovimentiAvere and numberRow: 1", RuntimeException.class, ()->getTableRowElement("main.MovimentiAvere",1));

        doClickButton("doChiudiForm()");

        doClickButton("submitForm('doVisualizzaEconomica');");
        assertThrows("Cannot find Element <tr> with tableName 'mainTable' and numberRow: 2", RuntimeException.class, ()->getTableRowElement("mainTable",2));

        doClickButton("doChiudiForm()");

        //Visualizzo le scritture di analitica associate
        doClickButton("submitForm('doVisualizzaAnalitica');");

        //Seleziono la prima scrittura nata in fase di evasione che deve essere rimasta costante
        getTableRowElement("mainTable",0).click();

        assertEquals("Y", getGrapheneElement("main.attiva").getText());
        assertEquals("122,00", getGrapheneElement("main.imTotaleMov").getAttribute("value"));

        doClickButton("doTab('tab','tabMovimenti')");
        assertEquals("C13003", getTableColumnElement("main.Movimenti",0,1).getText());
        assertEquals("Dare", getTableColumnElement("main.Movimenti",0,2).getText());
        assertEquals("000.000.000", getTableColumnElement("main.Movimenti",0,3).getText());
        assertEquals("PTEST002", getTableColumnElement("main.Movimenti",0,4).getText());
        assertEquals("122,00", getTableColumnElement("main.Movimenti",0,5).getText());
        assertThrows("Cannot find Element <tr> with tableName main.Movimenti and numberRow: 1", RuntimeException.class, ()->getTableRowElement("main.Movimenti",1));

        doClickButton("doChiudiForm()");

        doClickButton("submitForm('doVisualizzaAnalitica');");

        //Seleziono la seconda scrittura nata in fase di riscontro a valore che deve essere stata annullata dopo lo scollegamento dalla fattura
        getTableRowElement("mainTable",1).click();

        assertEquals("N", getGrapheneElement("main.attiva").getText());
        assertEquals("1,22", getGrapheneElement("main.imTotaleMov").getAttribute("value"));

        doClickButton("doTab('tab','tabMovimenti')");
        assertEquals("C13003", getTableColumnElement("main.Movimenti",0,1).getText());
        assertEquals("Avere", getTableColumnElement("main.Movimenti",0,2).getText());
        assertEquals("000.000.000", getTableColumnElement("main.Movimenti",0,3).getText());
        assertEquals("PTEST002", getTableColumnElement("main.Movimenti",0,4).getText());
        assertEquals("1,22", getTableColumnElement("main.Movimenti",0,5).getText());
        assertThrows("Cannot find Element <tr> with tableName main.Movimenti and numberRow: 1", RuntimeException.class, ()->getTableRowElement("main.Movimenti",1));

        doClickButton("doChiudiForm()");
        doClickButton("submitForm('doVisualizzaMovimento');");

        assertEquals("C01", getTableColumnElement("mainTable",0,3).getText());
        assertEquals("122", getTableColumnElement("mainTable",0,9).getText());

        assertEquals("C20", getTableColumnElement("mainTable",1,3).getText());
        assertEquals("1,22", getTableColumnElement("mainTable",1,9).getText());

        assertEquals("C19", getTableColumnElement("mainTable",2,3).getText());
        assertEquals("1,22", getTableColumnElement("mainTable",2,9).getText());

        assertThrows("Cannot find Element <tr> with tableName 'mainTable' and numberRow: 3", RuntimeException.class, ()->getTableRowElement("mainTable",3));

        doClickButton("doChiudiForm()");

        //Cerco l'ordine DSA/2/1/1 per verificare che il riscontro a valore abbia messo i valori corretti
        doClickButton("doNuovaRicerca()");

        getGrapheneElement("main.findUnitaOperativaOrd.cdUnitaOperativa").writeIntoElement(CD_UNITA_OPERATIVA);
        doClickButton("doSearch(main.findUnitaOperativaOrd)");

        getGrapheneElement("main.findNumerazioneOrd.cdNumeratore").writeIntoElement(CD_NUMERATORE);
        doClickButton("doSearch(main.findNumerazioneOrd)");

        getGrapheneElement("main.numero").writeIntoElement("2");

        doClickButton("doCerca()");
        alert = browser.switchTo().alert();
        assertEquals(AlertMessage.MESSAGE_RICERCA_MONO_RECORD.value(), alert.getText());
        alert.accept();

        doClickButton("doTab('tab','tabOrdineAcqDettaglio')");
        doSelectTableRow("main.Righe",0);

        doClickButton("doTab('tabOrdineAcqDettagli','tabOrdineRigaResultDetailEcoCoge')");

        assertEquals("C13001", getGrapheneElement("main.Righe.find_voce_ep.cd_voce_ep").getAttribute("value"));

        doSelectTableRow("main.Righe.Dati Coge/Coan",0);

        //Verifico che i dati analitici della riga dell'ordine DSA/2/1/1 siano stati ridotti dopo il riscontro a valore
        assertEquals("C13001", getTableColumnElement("main.Righe.Dati Coge/Coan",0,1).getText());
        assertEquals("PTEST002", getTableColumnElement("main.Righe.Dati Coge/Coan",0,2).getText());
        assertEquals("000.000.000", getTableColumnElement("main.Righe.Dati Coge/Coan",0,3).getText());
        assertEquals("1.218,78", getTableColumnElement("main.Righe.Dati Coge/Coan",0,4).getText());
        assertThrows("Cannot find Element <tr> with tableName main.Righe.Dati Coge/Coan and numberRow: 1", RuntimeException.class, ()->getTableRowElement("main.Righe.Dati Coge/Coan",1));

        //Seleziono la consegna DSA/2/1/1 collegata dalla fattura
        doClickButton("doTab('tabOrdineAcqDettagli','tabOrdineConsegna')");
        doSelectTableRow("main.Righe.Consegne",0);

        //Visualizzo le scritture di economica associate
        doClickButton("submitForm('doVisualizzaEconomica');");

        //Seleziono la prima scrittura nata in fase di evasione che deve essere rimasta costante
        getTableRowElement("mainTable",0).click();

        assertEquals("Si", getGrapheneElement("main.attiva").getText());
        assertEquals("122,00", getGrapheneElement("main.imTotaleDare").getAttribute("value"));
        assertEquals("122,00", getGrapheneElement("main.imTotaleAvere").getAttribute("value"));

        doClickButton("doTab('tab','tabDare')");
        assertEquals("C13001", getTableColumnElement("main.MovimentiDare",0,1).getText());
        assertEquals("122,00", getTableColumnElement("main.MovimentiDare",0,5).getText());
        assertThrows("Cannot find Element <tr> with tableName main.MovimentiDare and numberRow: 1", RuntimeException.class, ()->getTableRowElement("main.MovimentiDare",1));

        doClickButton("doTab('tab','tabAvere')");
        assertEquals("P00047", getTableColumnElement("main.MovimentiAvere",0,1).getText());
        assertEquals("122,00", getTableColumnElement("main.MovimentiAvere",0,5).getText());
        assertThrows("Cannot find Element <tr> with tableName main.MovimentiAvere and numberRow: 1", RuntimeException.class, ()->getTableRowElement("main.MovimentiAvere",1));

        doClickButton("doChiudiForm()");

        //Visualizzo le scritture di economica associate
        doClickButton("submitForm('doVisualizzaEconomica');");

        //Seleziono la seconda scrittura nata in fase di riscontro a valore
        getTableRowElement("mainTable",1).click();

        assertEquals("Si", getGrapheneElement("main.attiva").getText());
        assertEquals("1,22", getGrapheneElement("main.imTotaleDare").getAttribute("value"));
        assertEquals("1,22", getGrapheneElement("main.imTotaleAvere").getAttribute("value"));

        doClickButton("doTab('tab','tabDare')");
        assertEquals("P00047", getTableColumnElement("main.MovimentiDare",0,1).getText());
        assertEquals("1,22", getTableColumnElement("main.MovimentiDare",0,5).getText());
        assertThrows("Cannot find Element <tr> with tableName main.MovimentiDare and numberRow: 1", RuntimeException.class, ()->getTableRowElement("main.MovimentiDare",1));

        doClickButton("doTab('tab','tabAvere')");
        assertEquals("C13001", getTableColumnElement("main.MovimentiAvere",0,1).getText());
        assertEquals("1,22", getTableColumnElement("main.MovimentiAvere",0,5).getText());
        assertThrows("Cannot find Element <tr> with tableName main.MovimentiAvere and numberRow: 1", RuntimeException.class, ()->getTableRowElement("main.MovimentiAvere",1));

        doClickButton("doChiudiForm()");

        doClickButton("submitForm('doVisualizzaEconomica');");
        assertThrows("Cannot find Element <tr> with tableName 'mainTable' and numberRow: 2", RuntimeException.class, ()->getTableRowElement("mainTable",2));

        doClickButton("doChiudiForm()");

        //Visualizzo le scritture di analitica associate
        doClickButton("submitForm('doVisualizzaAnalitica');");

        //Seleziono la prima scrittura nata in fase di evasione che deve essere rimasta costante
        getTableRowElement("mainTable",0).click();

        assertEquals("Y", getGrapheneElement("main.attiva").getText());
        assertEquals("122,00", getGrapheneElement("main.imTotaleMov").getAttribute("value"));

        doClickButton("doTab('tab','tabMovimenti')");
        assertEquals("C13001", getTableColumnElement("main.Movimenti",0,1).getText());
        assertEquals("Dare", getTableColumnElement("main.Movimenti",0,2).getText());
        assertEquals("000.000.000", getTableColumnElement("main.Movimenti",0,3).getText());
        assertEquals("PTEST002", getTableColumnElement("main.Movimenti",0,4).getText());
        assertEquals("122,00", getTableColumnElement("main.Movimenti",0,5).getText());
        assertThrows("Cannot find Element <tr> with tableName main.Movimenti and numberRow: 1", RuntimeException.class, ()->getTableRowElement("main.Movimenti",1));

        doClickButton("doChiudiForm()");

        doClickButton("submitForm('doVisualizzaAnalitica');");

        //Seleziono la seconda scrittura nata in fase di riscontro a valore
        getTableRowElement("mainTable",1).click();

        assertEquals("Y", getGrapheneElement("main.attiva").getText());
        assertEquals("1,22", getGrapheneElement("main.imTotaleMov").getAttribute("value"));

        doClickButton("doTab('tab','tabMovimenti')");
        assertEquals("C13001", getTableColumnElement("main.Movimenti",0,1).getText());
        assertEquals("Avere", getTableColumnElement("main.Movimenti",0,2).getText());
        assertEquals("000.000.000", getTableColumnElement("main.Movimenti",0,3).getText());
        assertEquals("PTEST002", getTableColumnElement("main.Movimenti",0,4).getText());
        assertEquals("1,22", getTableColumnElement("main.Movimenti",0,5).getText());
        assertThrows("Cannot find Element <tr> with tableName main.Movimenti and numberRow: 1", RuntimeException.class, ()->getTableRowElement("main.Movimenti",1));

        doClickButton("doChiudiForm()");
        doClickButton("submitForm('doVisualizzaMovimento');");

        assertEquals("C01", getTableColumnElement("mainTable",0,3).getText());
        assertEquals("122", getTableColumnElement("mainTable",0,9).getText());

        assertEquals("C20", getTableColumnElement("mainTable",1,3).getText());
        assertEquals("1,22", getTableColumnElement("mainTable",1,9).getText());

        assertThrows("Cannot find Element <tr> with tableName 'mainTable' and numberRow: 2", RuntimeException.class, ()->getTableRowElement("mainTable",2));
    }

    @Test
    @RunAsClient
    @OperateOnDeployment(TEST_H2)
    @InSequence(8)
    public void testRiscontroValore002() {
        browser.switchTo().parentFrame();
        switchToFrameMenu();
        doSelezionaMenu(AMM_FATTUR_FATPAS_ELE);

        browser.switchTo().parentFrame();
        switchToFrameWorkspace();

        getGrapheneElement("main.identificativoSdi").writeIntoElement("90000000001");

        Select select = new Select(getGrapheneElement("main.statoDocumento"));
        select.selectByValue("");

        doClickButton("doCerca()");
        Alert alert = browser.switchTo().alert();
        assertEquals(AlertMessage.MESSAGE_RICERCA_MONO_RECORD.value(), alert.getText());
        alert.accept();

        doClickButton("submitForm('doCompilaFattura')");

        assertTrue(browser.getPageSource().contains("La compilazione della Fattura e il suo successivo salvataggio, comporta l'accettazione del documento elettronico."));

        getGrapheneElement("comando.doYes").click();

        System.out.println("1 - " +browser.getPageSource().contains("Fattura Passiva - Inserimento"));

        select = new Select(getGrapheneElement("main.stato_liquidazione"));
        select.selectByValue("NOLIQ");

        select = new Select(getGrapheneElement("main.causale"));
        select.selectByValue("ATTNC");

        getGrapheneElement("main.flDaOrdini").click();

        getGrapheneElement("main.ds_fattura_passiva").writeIntoElement("RISCONTRO VALORE TEST");

        doClickButton("doTab('tab','tabFatturaPassivaOrdini')");

        doClickButton("submitForm('doSelezionaOrdini')");

        //Seleziono la prima riga ordini
        Optional<GrapheneElement> element = browser.findElements(By.tagName("tr"))
                .stream()
                .filter(GrapheneElement.class::isInstance)
                .map(GrapheneElement.class::cast)
                .filter(rowElement -> {
                    try {
                        return getTableColumnElement(rowElement, 3).getText().contains("2025") &&
                                CD_NUMERATORE.equals(getTableColumnElement(rowElement, 4).getText()) &&
                                "1".equals(getTableColumnElement(rowElement, 5).getText()) &&
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

        doSelectTableRow("main.Ordini",0);

        getGrapheneElement("main.Ordini.imponibileErrato").writeIntoElement("103");
        doClickButton("confirmModalInputChange(this,'main.Ordini.imponibileErrato','doRettificaConsegna')");

        doClickButton("submitForm('doConfermaRiscontroAValore')");

        alert = browser.switchTo().alert();
        assertEquals("Attenzione ci sono dettagli di fattura da contabilizzare.", alert.getText());
        alert.accept();

        doSelectTableRow("main.Dettaglio",1);

        getGrapheneElement("main.Dettaglio.im_iva").clear();
        getGrapheneElement("main.Dettaglio.im_iva").writeIntoElement("0");
        doClickButton("confirmModalInputChange(this,'main.Dettaglio.im_iva','doForzaIVA')");

        doClickButton("doSalva()");

        alert = browser.switchTo().alert();
        assertEquals(AlertMessage.CREAZIONE_ESEGUITA.value(), alert.getText());
        alert.accept();

        doClickButton("doTab('tab','tabEconomica')");
        assertEquals("P00047", getTableColumnElement("main.Movimenti Dare",0,1).getText());
        assertEquals("122,00", getTableColumnElement("main.Movimenti Dare",0,5).getText());

        assertEquals("C13003", getTableColumnElement("main.Movimenti Dare",1,1).getText());
        assertEquals("3,00", getTableColumnElement("main.Movimenti Dare",1,5).getText());

        assertThrows("Cannot find Element <tr> with tableName 'main.Movimenti Dare' and numberRow: 2", RuntimeException.class, ()->getTableRowElement("main.Movimenti Dare",2));

        assertEquals("P13003", getTableColumnElement("main.Movimenti Avere",0,1).getText());
        assertEquals("103,00", getTableColumnElement("main.Movimenti Avere",0,5).getText());

        assertEquals("P71012I", getTableColumnElement("main.Movimenti Avere",1,1).getText());
        assertEquals("22,00", getTableColumnElement("main.Movimenti Avere",1,5).getText());

        assertThrows("Cannot find Element <tr> with tableName 'main.Movimenti Avere' and numberRow: 2", RuntimeException.class, ()->getTableRowElement("main.Movimenti Avere",2));

        doClickButton("doTab('tab','tabAnalitica')");
        assertEquals("C13003", getTableColumnElement("main.Movimenti Analitici",0,1).getText());
        assertEquals("Dare", getTableColumnElement("main.Movimenti Analitici",0,2).getText());
        assertEquals("C13003", getTableColumnElement("main.Movimenti Analitici",0,3).getText());
        assertEquals("000.000.000", getTableColumnElement("main.Movimenti Analitici",0,5).getText());
        assertEquals("PTEST003", getTableColumnElement("main.Movimenti Analitici",0,6).getText());
        assertEquals("D", getTableColumnElement("main.Movimenti Analitici",0,7).getText());
        assertEquals("3,00", getTableColumnElement("main.Movimenti Analitici",0,8).getText());

        assertThrows("Cannot find Element <tr> with tableName 'main.Movimenti Analitici' and numberRow: 1", RuntimeException.class, ()->getTableRowElement("main.Movimenti Analitici",1));
    }
}
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

package it.cnr.test.h2.coepcoan.component.scritture;

import it.cnr.test.h2.utenze.action.ActionDeployments;
import it.cnr.test.h2.utenze.action.LoginTest;
import it.cnr.test.util.AlertMessage;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.webdriver.htmlunit.DroneHtmlUnitDriver;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CRUDOrdineAcqBPTest extends ActionDeployments {
    public static final String USERNAME = "ENTETEST";
    public static final String PASSWORD = "PASSTEST";
    public static final String CD_UNITA_OPERATIVA = "DRUE";
    public static final String CD_NUMERATORE = "DSA";
    public static final String CD_MAGAZZINO = "CS";
    public static final String UO = "000.000";
    public static final String CDR = "000.000.000";
    public static final String ORD = "0.ORD";
    public static final String ORD_ORDACQ = "0.ORD.ORDACQ";
    public static final String ORD_ORDACQ_M = "0.ORD.ORDACQ.M";
    public static final String ORD_EVAORD = "0.ORD.EVAORD";
    private transient final static Logger LOGGER = LoggerFactory.getLogger(LoginTest.class);

    @Test
    @RunAsClient
    @OperateOnDeployment(TEST_H2)
    @InSequence(1)
    public void testLogin() throws Exception {
        doLogin(USERNAME, PASSWORD);
        doLoginUO(UO, CDR);
    }

    @Test
    @RunAsClient
    @OperateOnDeployment(TEST_H2)
    @InSequence(2)
    public void testCreaProgetto() throws Exception {
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

        getGrapheneElement("main.find_contratto.esercizio").writeIntoElement("2025");
        getGrapheneElement("main.find_contratto.pg_contratto").writeIntoElement("1");
        doClickButton("doSearch(main.find_contratto)");

        doClickButton("doTab('tab','tabOrdineAcqDettaglio')");

        doClickButton("doAddToCRUD(main.Righe)");

        getGrapheneElement("main.Righe.findBeneServizio.cd_bene_servizio").writeIntoElement("191202");
        doClickButton("doSearch(main.Righe.findBeneServizio)");

        getGrapheneElement("main.Righe.prezzoUnitario").writeIntoElement("100");
        doClickButton("doOnImportoChange");

        getGrapheneElement("main.Righe.dspQuantita").writeIntoElement("10");
        doClickButton("doOnDspQuantitaChange");

        doClickButton("doBlankSearch(main.Righe.findMagazzino)");
        getGrapheneElement("main.Righe.findMagazzino.cdMagazzino").writeIntoElement(CD_MAGAZZINO);
        doClickButton("doSearch(main.Righe.findMagazzino)");

        doClickButton("doTab('tabOrdineAcqDettagli','tabOrdineConsegna')");

        getGrapheneElement("main.Righe.Consegne.selection").click();

        doClickButton("doSalva()");

        Alert alert = browser.switchTo().alert();
        assertEquals(AlertMessage.CREAZIONE_ESEGUITA.value(), alert.getText());

        alert.accept();

        Select select = new Select(getGrapheneElement("main.statoForUpdate"));
        select.selectByValue("APP");

        doClickButton("doSalva()");

        alert = browser.switchTo().alert();
        assertEquals(AlertMessage.SALVATAGGIO_ESEGUITO.value(), alert.getText());

        alert.accept();

        select.selectByValue("DEF");

        doClickButton("doSalva()");

        alert = browser.switchTo().alert();
        assertEquals("Sulla consegna 2025/DSA/1/1/1 non è indicata l'obbligazione", alert.getText());
        alert.accept();

        doClickButton("doTab('tab','tabOrdineAcqDettaglio')");
        getGrapheneElement("main.Righe.selection").click();
        doClickButton("doRicercaObbligazione");
        doClickButton("doCerca");

        ((DroneHtmlUnitDriver) browser).findElementByClassName("TableRow").click();
        doClickButton("doSalva()");

        alert = browser.switchTo().alert();
        assertEquals(AlertMessage.SALVATAGGIO_ESEGUITO.value(), alert.getText());
        alert.accept();
    }

    @Test
    @RunAsClient
    @OperateOnDeployment(TEST_H2)
    @InSequence(3)
    public void testEvasioneConsegna() throws Exception {
        browser.switchTo().parentFrame();
        switchToFrameMenu();
        doSelezionaMenu(ORD_EVAORD);

        browser.switchTo().parentFrame();
        switchToFrameWorkspace();

        getGrapheneElement("main.findUnitaOperativaOrd.cdUnitaOperativa").writeIntoElement(CD_UNITA_OPERATIVA);
        doClickButton("doSearch(main.findUnitaOperativaOrd)");

        getGrapheneElement("main.findMagazzino.cdMagazzino").writeIntoElement(CD_MAGAZZINO);
        doClickButton("doSearch(main.findMagazzino)");

        GregorianCalendar dataBollaConsegna = (GregorianCalendar) GregorianCalendar.getInstance();
        dataBollaConsegna.add(Calendar.DAY_OF_YEAR,1);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");

        getGrapheneElement("main.dataBolla").writeIntoElement(sdf.format(dataBollaConsegna.getTime().getTime()));
        doClickButton("doOnDtBollaChange");

        getGrapheneElement("main.numeroBolla").writeIntoElement("1");

        getGrapheneElement("main.dataConsegna").writeIntoElement(sdf.format(dataBollaConsegna.getTime().getTime()));
        doClickButton("doOnDtConsegnaChange");

        doClickButton("doCercaConsegneDaEvadere");

        getGrapheneElement("main.ConsegneDaEvadere.selection").click();

        getGrapheneElement("main.ConsegneDaEvadere.quantitaEvasa").clear();
        getGrapheneElement("main.ConsegneDaEvadere.quantitaEvasa").writeIntoElement("1");
        doClickButton("confirmModalInputChange(this,'main.ConsegneDaEvadere.quantitaEvasa','doDefault')");

        doClickButton("doSalva()");
        Alert alert = browser.switchTo().alert();
        assertEquals("Per la consegna 2025/DSA/1/1/1 è necessario indicare se bisogna solo sdoppiare la riga o anche evaderla forzatamente", alert.getText());
        alert.accept();

        Select select = new Select(getGrapheneElement("main.ConsegneDaEvadere.operazioneQuantitaEvasaMinore"));
        select.selectByValue("C");

        doClickButton("doSalva()");
        alert = browser.switchTo().alert();
        assertEquals(AlertMessage.OPERAZIONE_EFFETTUATA.value(), alert.getText());
        alert.accept();

        //Verifico che la scrittura sull'ordine sia stata eseguita correttaqmente
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
        getGrapheneElement("main.Righe.selection").click();

        doClickButton("doTab('tabOrdineAcqDettagli','tabOrdineRigaResultDetailEcoCoge')");

        assertEquals("C13003", getGrapheneElement("main.Righe.find_voce_ep.cd_voce_ep").getAttribute("value"));

        getGrapheneElement("main.Righe.Dati Coge/Coan.selection").click();

        assertTrue(browser.findElement(By.tagName("tbody")).getText().contains("C13003 PTEST002 000.000.000 1.220,00"));
    }
}

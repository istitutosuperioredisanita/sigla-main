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
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

public class CRUDOrdineAcqBPTest extends ActionDeployments {
    public static final String USERNAME = "ENTETEST";
    public static final String PASSWORD = "PASSTEST";
    public static final String CD_UNITA_OPERATIVA = "DRUE";
    public static final String CD_NUMERATORE = "DSA";
    public static final String UO = "000.000";
    public static final String CDR = "000.000.000";
    public static final String ORD = "0.ORD";
    public static final String ORD_ORDACQ = "0.ORD.ORDACQ";
    public static final String ORD_ORDACQ_M = "0.ORD.ORDACQ.M";
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

        getGrapheneElement("main.findNumerazioneOrd.cdNumeratore").writeIntoElement(CD_UNITA_OPERATIVA);
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
        getGrapheneElement("main.Righe.findMagazzino.cdMagazzino").writeIntoElement("CS");
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

    }
}

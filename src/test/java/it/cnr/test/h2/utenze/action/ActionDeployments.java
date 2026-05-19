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

package it.cnr.test.h2.utenze.action;

import it.cnr.test.h2.DeploymentsH2;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.GrapheneElement;

import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ActionDeployments extends DeploymentsH2 {
    private transient final static Logger LOGGER = LoggerFactory.getLogger(ActionDeployments.class);
    public static final int ITERATE_MAX = 50;
    public static final String FRAME_DESKTOP = "desktop", FRAME_WORKSPACE = "workspace", FRAME_MENU = "menu_tree";

    /** Timeout standard per presenza/clickability degli elementi (secondi) */
    private static final int ELEMENT_TIMEOUT_SECONDS =
            Integer.parseInt(System.getProperty("test.element.timeout", "10"));

    /**
     * Timeout breve usato SOLO per verificare l'ASSENZA di un elemento.
     * Usare questo al posto di ELEMENT_TIMEOUT_SECONDS negli assertThrows/assertAbsent
     * evita di aspettare 10s per ogni check negativo.
     */
    private static final int ABSENT_TIMEOUT_SECONDS =
            Integer.parseInt(System.getProperty("test.absent.timeout", "1"));

    /** Intervallo di polling del FluentWait (millisecondi) */
    private static final long POLLING_MILLIS = 200;

    @Drone
    protected WebDriver browser;

    //@ArquillianResource
    protected URL deploymentURL;
    {
        try {
            deploymentURL = new URL("http://localhost:8080/SIGLA");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    protected void logPageSource() {
        LOGGER.trace(browser.getPageSource());
    }

    protected void doLogin(String user, String password) throws Exception {
        browser.navigate().to(deploymentURL.toString().concat("/Login.do"));
        switchToFrameDesktop();
        final WebElement comandoEntra = browser.findElement(By.name("comando.doEntra"));
        assertEquals(Boolean.TRUE, Optional.ofNullable(comandoEntra).isPresent());

        getGrapheneElement("j_username").writeIntoElement(user);
        getGrapheneElement("j_password").writeIntoElement(password);

        comandoEntra.click();
    }

    protected void doLoginUO(String uo, String cdr) throws Exception {
        switchToFrameWorkspace();

        getGrapheneElement("main.find_uo.cd_unita_organizzativa").writeIntoElement(uo);
        doClickButton("doSearch(main.find_uo)");

        getGrapheneElement("main.find_cdr.searchtool_cd_centro_responsabilita").writeIntoElement(cdr);
        doClickButton("submitForm('doSelezionaCds')");
    }

    /**
     * Attende la presenza dell'elemento con FluentWait (polling 200ms)
     * ed lo restituisce. Un solo wait invece del doppio waitGui precedente.
     */
    protected WebElement getWebElement(By elementLocator) {
        return new FluentWait<>(browser)
                .withTimeout(Duration.ofSeconds(ELEMENT_TIMEOUT_SECONDS))
                .pollingEvery(Duration.ofMillis(POLLING_MILLIS))
                .ignoring(StaleElementReferenceException.class)
                .ignoring(NoSuchElementException.class)
                .until(ExpectedConditions.presenceOfElementLocated(elementLocator));
    }

    protected GrapheneElement getGrapheneElement(By elementLocator) {
        return Optional.ofNullable(getWebElement(elementLocator))
                .filter(GrapheneElement.class::isInstance)
                .map(GrapheneElement.class::cast)
                .orElseThrow(() -> new RuntimeException("Cannot find GrapheneElement with elementLocator " + elementLocator.toString()));
    }

    protected GrapheneElement getGrapheneElement(String element) {
        return this.getGrapheneElement(By.name(element));
    }

    private void switchToDefaultContent() {
        browser.switchTo().defaultContent();
        // logPageSource() rimosso: serializzava l'intero DOM ad ogni switch
    }

    protected void switchToFrameMenu() {
        switchToDefaultContent();
        switchToFrameDesktop();
        switchToFrame(FRAME_MENU);
    }

    protected void switchToFrameWorkspace() {
        switchToDefaultContent();
        switchToFrameDesktop();
        switchToFrame(FRAME_WORKSPACE);
    }

    private void switchToFrameDesktop() {
        switchToFrame(FRAME_DESKTOP);
    }

    public void switchToFrame(String frameNameOrId) {
        switchToFrame(frameNameOrId, 3);
    }

    public void switchToFrame(String frameNameOrId, int timeoutSeconds) {
        // logPageSource() rimosso: serializzava l'intero DOM ad ogni switch
        WebDriverWait wait = new WebDriverWait(browser, Duration.ofSeconds(timeoutSeconds));
        wait.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(frameNameOrId));
    }

    protected void doSelezionaMenu(String menuId) {
        doClickTree("selezionaMenu('".concat(menuId).concat("')"));
    }

    protected void doApriMenu(String menuId) {
        doClickTree("apriMenu('".concat(menuId).concat("')"));
    }

    protected void doClickTree(String onclick) {
        this.findAndClickButton(By.xpath("//span[contains(@onclick, \"" + onclick + "\")]"));
    }

    public void doClickButton(String onclick) {
        this.findAndClickButton(By.xpath("//button[contains(@onclick, \"" + onclick + "\")]"));
    }

    /**
     * Attende che il bottone sia clickable (presenza + visibilità + enabled)
     * con un singolo FluentWait — elimina il doppio wait precedente
     * (getWebElement + waitGui clickable) che raddoppiava i tempi di attesa.
     */
    private void findAndClickButton(By buttonLocator) {
        WebElement button = new FluentWait<>(browser)
                .withTimeout(Duration.ofSeconds(ELEMENT_TIMEOUT_SECONDS))
                .pollingEvery(Duration.ofMillis(POLLING_MILLIS))
                .ignoring(StaleElementReferenceException.class)
                .ignoring(NoSuchElementException.class)
                .until(ExpectedConditions.elementToBeClickable(buttonLocator));
        button.click();
    }

    protected GrapheneElement getTableRowElement(String tableName, int numberRow) {
        String onclick = "javascript:select('" + tableName + "'," + numberRow + ")";
        return Optional.ofNullable(this.getWebElement(By.xpath("//tr[contains(@onclick, \"" + onclick + "\")]")))
                .filter(GrapheneElement.class::isInstance)
                .map(GrapheneElement.class::cast)
                .orElseThrow(() -> new RuntimeException("Cannot find Element <tr> with tableName " + tableName +
                        " and numberRow: " + numberRow));
    }

    /**
     * Versione "fast" di getTableRowElement: usa timeout breve.
     * Usare nei loop di scansione righe per evitare di aspettare 10s
     * per ogni riga non trovata (es. loop for riga 0..9 su tabella con 2 righe).
     */
    protected GrapheneElement getTableRowElementFast(String tableName, int numberRow) {
        String onclick = "javascript:select('" + tableName + "'," + numberRow + ")";
        By locator = By.xpath("//tr[contains(@onclick, \"" + onclick + "\")]");
        WebElement el;
        try {
            el = getWebElementFast(locator);
        } catch (TimeoutException e) {
            throw new RuntimeException("Cannot find Element <tr> with tableName " + tableName +
                    " and numberRow: " + numberRow);
        }
        return Optional.ofNullable(el)
                .filter(GrapheneElement.class::isInstance)
                .map(GrapheneElement.class::cast)
                .orElseThrow(() -> new RuntimeException("Cannot find Element <tr> with tableName " + tableName +
                        " and numberRow: " + numberRow));
    }

    /**
     * Versione "fast" di getTableColumnElement: usa getTableRowElementFast internamente.
     * Usare nei loop di scansione per evitare attese inutili su righe non esistenti.
     */
    protected GrapheneElement getTableColumnElementFast(String tableName, int numberRow, int numberColumn) {
        GrapheneElement rowElement = getTableRowElementFast(tableName, numberRow);
        return getTableColumnElement(rowElement, numberColumn);
    }

    /**
     * Cerca un elemento con timeout breve (ABSENT_TIMEOUT_SECONDS).
     * Usare SOLO per verificare l'assenza di elementi: evita di aspettare
     * ELEMENT_TIMEOUT_SECONDS (10s) per ogni check negativo negli assertThrows.
     */
    private WebElement getWebElementFast(By elementLocator) {
        return new FluentWait<>(browser)
                .withTimeout(Duration.ofSeconds(ABSENT_TIMEOUT_SECONDS))
                .pollingEvery(Duration.ofMillis(POLLING_MILLIS))
                .ignoring(StaleElementReferenceException.class)
                .ignoring(NoSuchElementException.class)
                .until(ExpectedConditions.presenceOfElementLocated(elementLocator));
    }

    /**
     * Verifica che una riga di tabella NON esista.
     * Sostituisce il pattern: Assertions.assertThrows(RuntimeException.class,
     *   () -> getTableRowElement(tableName, row), "...").
     * Usa un timeout breve (1s) invece di quello standard (10s).
     */
    protected void assertTableRowAbsent(String tableName, int numberRow) {
        String onclick = "javascript:select('" + tableName + "'," + numberRow + ")";
        By locator = By.xpath("//tr[contains(@onclick, \"" + onclick + "\")]");
        boolean found;
        try {
            getWebElementFast(locator);
            found = true;
        } catch (TimeoutException e) {
            found = false;
        }
        Assertions.assertFalse(found,
                "Expected row " + numberRow + " of table '" + tableName + "' to be absent, but it was found");
    }

    /**
     * Verifica che un elemento generico (By) NON esista.
     * Usa un timeout breve (1s) invece di quello standard (10s).
     */
    protected void assertElementAbsent(By locator) {
        boolean found;
        try {
            getWebElementFast(locator);
            found = true;
        } catch (TimeoutException e) {
            found = false;
        }
        Assertions.assertFalse(found,
                "Expected element " + locator + " to be absent, but it was found");
    }

    protected GrapheneElement getTableColumnElement(String tableName, int numberRow, int numberColumn) {
        GrapheneElement rowElement = getTableRowElement(tableName, numberRow);
        return getTableColumnElement(rowElement, numberColumn);
    }

    protected GrapheneElement getTableColumnElement(GrapheneElement rowElement, int numberColumn) {
        try {
            return Optional.ofNullable(rowElement.findElements(By.tagName("td")).get(numberColumn))
                    .filter(GrapheneElement.class::isInstance)
                    .map(GrapheneElement.class::cast)
                    .orElseThrow(() -> new RuntimeException("Cannot find Element <td> with rowElement " + rowElement.getText() +
                            " and numberColumn: " + numberColumn));
        } catch (IndexOutOfBoundsException ex) {
            throw new RuntimeException("Cannot find Element <td> with rowElement " + rowElement.getText() +
                    " and numberColumn: " + numberColumn);
        }
    }

    protected void doSelectTableRow(String elementName, int numberRow) {
        String name = elementName + ".selection";
        Optional.ofNullable(getTableRowElement(elementName, numberRow).findElement(By.name(name)))
                .orElseThrow(() -> new RuntimeException("Cannot find Element selection in Element <tr> with name " + elementName +
                        " and numberRow: " + numberRow))
                .click();
    }

    protected void doSelectTableRow(GrapheneElement rowElement, String elementName) {
        String name = elementName + ".selection";
        Optional.ofNullable(rowElement.findElement(By.name(name)))
                .orElseThrow(() -> new RuntimeException("Cannot find Element selection in Element <tr> with name " + elementName))
                .click();
    }

    public String handleTextAlert(WebDriver driver) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            Alert alert = wait.ignoring(UnhandledAlertException.class)
                    .until(ExpectedConditions.alertIsPresent());

            String alertText = alert.getText();

            alert.accept(); // Clicca OK

            return alertText;
        } catch (NoAlertPresentException | TimeoutException e) {
            LOGGER.error("Nessun alert presente");
            return null;
        }
    }
}

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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ActionDeployments extends DeploymentsH2 {
    private transient final static Logger LOGGER = LoggerFactory.getLogger(ActionDeployments.class);
    public static final int ITERATE_MAX = 50;
    public static final String FRAME_DESKTOP = "desktop", FRAME_WORKSPACE = "workspace", FRAME_MENU = "menu_tree";

    /** Timeout standard per presenza/clickability degli elementi (secondi) */
    private static final int ELEMENT_TIMEOUT_SECONDS =
            Integer.parseInt(System.getProperty("test.element.timeout", "30"));

    /**
     * Timeout breve usato SOLO per verificare l'ASSENZA di un elemento.
     * Usare questo al posto di ELEMENT_TIMEOUT_SECONDS negli assertThrows/assertAbsent
     * evita di aspettare 10s per ogni check negativo.
     */
    private static final int ABSENT_TIMEOUT_SECONDS =
            Integer.parseInt(System.getProperty("test.absent.timeout", "1"));

    /** Intervallo di polling del FluentWait (millisecondi) */
    private static final long POLLING_MILLIS = 200;

    /** Dopo quanti ms senza esito si rientra nel frame corrente (vedi waitInCurrentFrame) */
    private static final long FRAME_REENTRY_MILLIS = 3000;

    /** Ultimo frame applicativo in cui si e' entrati (menu_tree o workspace), null se nessuno */
    private String currentFrame;

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
        return waitInCurrentFrame(ExpectedConditions.presenceOfElementLocated(elementLocator));
    }

    /**
     * Attende la condizione fino a ELEMENT_TIMEOUT_SECONDS, ma a finestre brevi:
     * se allo scadere di una finestra la condizione non e' soddisfatta, rientra nel
     * frame corrente (default -> desktop -> menu/workspace) e riprova.
     * Serve quando un submit ricarica il frame "desktop" (es. doSelezionaCds):
     * il driver resta agganciato al frame vecchio, ormai staccato dal DOM, e
     * non troverebbe mai l'elemento anche se nella pagina e' visibile.
     */
    private <T> T waitInCurrentFrame(Function<? super WebDriver, T> condition) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(ELEMENT_TIMEOUT_SECONDS);
        while (true) {
            long remainingMillis = TimeUnit.NANOSECONDS.toMillis(deadline - System.nanoTime());
            long sliceMillis = Math.max(POLLING_MILLIS, Math.min(FRAME_REENTRY_MILLIS, remainingMillis));
            try {
                return new FluentWait<WebDriver>(browser)
                        .withTimeout(Duration.ofMillis(sliceMillis))
                        .pollingEvery(Duration.ofMillis(POLLING_MILLIS))
                        .ignoring(StaleElementReferenceException.class)
                        .ignoring(NoSuchElementException.class)
                        .until(condition);
            } catch (TimeoutException | NoSuchFrameException e) {
                if (System.nanoTime() >= deadline) {
                    throw e;
                }
                reenterCurrentFrame();
            }
        }
    }

    private void reenterCurrentFrame() {
        if (currentFrame == null) {
            return;
        }
        try {
            enterFrame(currentFrame);
        } catch (WebDriverException ignored) {
            // il frame potrebbe essere ancora in caricamento: si riprova alla prossima finestra
        }
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

    /**
     * Localizza l'elemento e ci scrive dentro come operazione atomica con retry.
     * A differenza di getGrapheneElement(...).writeIntoElement(...), se il DOM
     * cambia tra la localizzazione e la scrittura (es. dopo un submit/AJAX che
     * ricrea il form), ri-localizza l'elemento da capo invece di affidarsi al
     * solo retry interno di Graphene.
     */
    protected void doWriteIntoElement(String element, String value) {
        doWriteIntoElement(By.name(element), value);
    }

    protected void doWriteIntoElement(By locator, String value) {
        int attempts = 0;
        while (true) {
            try {
                getGrapheneElement(locator).writeIntoElement(value);
                return;
            } catch (StaleElementReferenceException e) {
                if (++attempts >= 3) {
                    LOGGER.error("writeIntoElement fallito dopo {} tentativi su {}", attempts, locator, e);
                    throw e;
                }
            }
        }
    }

    private void switchToDefaultContent() {
        browser.switchTo().defaultContent();
        // logPageSource() rimosso: serializzava l'intero DOM ad ogni switch
    }

    protected void switchToFrameMenu() {
        enterFrame(FRAME_MENU);
    }

    protected void switchToFrameWorkspace() {
        enterFrame(FRAME_WORKSPACE);
    }

    /** Entra in menu_tree o workspace partendo dalla radice e lo ricorda per il rientro. */
    private void enterFrame(String frame) {
        switchToDefaultContent();
        switchToFrameDesktop();
        switchToFrame(frame);
        currentFrame = frame;
    }

    private void switchToFrameDesktop() {
        switchToFrame(FRAME_DESKTOP);
    }

    public void switchToFrame(String frameNameOrId) {
        switchToFrame(frameNameOrId, 3);
    }

    public void switchToFrame(String frameNameOrId, int timeoutSeconds) {
        // un cambio di frame "manuale" invalida il frame da ripristinare in caso di reload
        currentFrame = null;
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
     * In caso di StaleElementReferenceException tra la elementToBeClickable()
     * e il click() (race dovuta a un re-render nel frattempo), ri-localizza
     * e ri-prova invece di ingoiare silenziosamente l'eccezione: un click
     * "perso" senza retry lascia l'app in uno stato inatteso e fa fallire
     * a cascata tutte le asserzioni successive del test.
     */
    private void findAndClickButton(By buttonLocator) {
        int attempts = 0;
        while (true) {
            try {
                WebElement button = waitInCurrentFrame(ExpectedConditions.elementToBeClickable(buttonLocator));
                button.click();
                return;
            } catch (TimeoutException e) {
                LOGGER.error("Cannot find button {} dopo {} tentativi timeout", buttonLocator, attempts, e);
                salvaScreenshot("findAndClickButton_" + buttonLocator);
                try {
                    Files.writeString(Path.of("target/page.html"), browser.getPageSource());
                    Files.writeString(Path.of("target/desktop.html"), browser.getPageSource());
                    Files.writeString(Path.of("target/menu_tree.html"), browser.getPageSource());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                ((TakesScreenshot) browser).getScreenshotAs(OutputType.FILE)
                        .renameTo(new File("target/fail.png"));
                throw e;
            } catch (StaleElementReferenceException e) {
                LOGGER.warn("Cannot find button {} dopo {} tentativi", buttonLocator, attempts, e);
                if (++attempts >= 3) {
                    LOGGER.error("Cannot find button {} dopo {} tentativi", buttonLocator, attempts, e);
                    throw e;
                }
            }
        }
    }

    private void salvaScreenshot(String label) {
        try {
            File screenshot = ((TakesScreenshot) browser).getScreenshotAs(OutputType.FILE);
            Path targetDir = Paths.get("target/screenshots");
            Files.createDirectories(targetDir);
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            Path target = targetDir.resolve("findAndClickButton_" + timestamp + ".png");
            Files.copy(screenshot.toPath(), target, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("label: " + label);
            System.out.println("Screenshot salvato: " + target.toAbsolutePath());
        } catch (Exception ex) {
            System.err.println("Screenshot fallito: " + ex.getMessage());
        }
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
        if (isTableRendered(tableName)) {
            // La tabella e' gia' nel DOM: la riga c'e' o non c'e', non serve aspettare.
            List<WebElement> rows = browser.findElements(locator);
            if (rows.isEmpty()) {
                throw new RuntimeException("Cannot find Element <tr> with tableName " + tableName +
                        " and numberRow: " + numberRow);
            }
            el = rows.get(0);
        } else {
            try {
                el = getWebElementFast(locator);
            } catch (TimeoutException e) {
                throw new RuntimeException("Cannot find Element <tr> with tableName " + tableName +
                        " and numberRow: " + numberRow);
            }
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
     * true se nel DOM corrente c'e' almeno una riga della tabella indicata.
     * Il match su "select('nome'," evita falsi positivi con tabelle dal nome
     * piu' lungo (es. 'main.Righe' vs 'main.Righe.Consegne').
     */
    private boolean isTableRendered(String tableName) {
        String prefix = "javascript:select('" + tableName + "',";
        return !browser.findElements(By.xpath("//tr[contains(@onclick, \"" + prefix + "\")]")).isEmpty();
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
        if (isTableRendered(tableName)) {
            // La tabella e' gia' nel DOM (di solito si sono appena lette le righe precedenti):
            // l'assenza si verifica subito, senza consumare l'intero timeout.
            found = !browser.findElements(locator).isEmpty();
        } else {
            try {
                getWebElementFast(locator);
                found = true;
            } catch (TimeoutException e) {
                found = false;
            }
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

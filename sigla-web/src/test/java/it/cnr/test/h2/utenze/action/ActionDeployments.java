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
import org.apache.http.HttpStatus;
import org.jboss.arquillian.container.test.api.BeforeDeployment;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.drone.api.annotation.Drone;
import org.jboss.arquillian.graphene.GrapheneElement;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ActionDeployments extends DeploymentsH2 {
    private transient final static Logger LOGGER = LoggerFactory.getLogger(ActionDeployments.class);
    public static final int ITERATE_MAX = 50;
    public static final String FRAME_DESKTOP = "desktop", FRAME_WORKSPACE = "workspace", FRAME_MENU = "menu_tree";

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

    protected WebElement getWebElement(String element) {
        List<WebElement> webElements = getWebElements(element);
        if (webElements.size()>1)
            throw new RuntimeException("Find more than one WebElement with name "+element);
        return webElements.stream().findAny()
                .orElseThrow(()->new RuntimeException("Cannot find WebElement with name "+element));
    }

    protected List<WebElement> getWebElements(String element) {
        return browser.findElements(By.name(element));
    }

    protected GrapheneElement getGrapheneElement(String element) {
        List<GrapheneElement> grapheneElements = getGrapheneElements(element);
        if (grapheneElements.size()>1)
            throw new RuntimeException("Find more than one GrapheneElement with name "+element);
        return grapheneElements.stream().findAny()
                .orElseThrow(()->new RuntimeException("Cannot find GrapheneElement with name "+element));
    }

    protected List<GrapheneElement> getGrapheneElements(String element) {
        return getWebElements(element).stream()
                .filter(GrapheneElement.class::isInstance)
                .map(GrapheneElement.class::cast)
                .collect(Collectors.toList());
    }

    protected void switchTodefaultContent() {
        browser.switchTo().defaultContent();
        logPageSource();
    }

    protected void switchToFrameMenu() {
        switchToFrame(FRAME_MENU);
    }

    protected void switchToFrameWorkspace() {
        switchToFrame(FRAME_WORKSPACE);
    }

    protected void switchToFrameDesktop() {
        switchToFrame(FRAME_DESKTOP);
    }

    public void switchToFrame(String frameNameOrId) {
        switchToFrame(frameNameOrId, 3);
    }

    public void switchToFrame(String frameNameOrId, int timeoutSeconds) {
        logPageSource();
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
        browser.findElements(By.tagName("span"))
                .stream()
                .filter(webElement -> Optional.ofNullable(webElement.getAttribute("onclick")).filter(s -> !s.isEmpty()).isPresent())
                .filter(webElement -> webElement.getAttribute("onclick").contains(onclick))
                .findAny()
                .orElseThrow(() -> new RuntimeException("Cannot find element " + onclick)).click();
    }

    protected void doClickButton(String onclick) {
        browser.findElements(By.tagName("button"))
                .stream()
                .filter(webElement -> Optional.ofNullable(webElement.getAttribute("onclick")).filter(s -> !s.isEmpty()).isPresent())
                .filter(webElement -> webElement.getAttribute("onclick").contains(onclick))
                .findAny()
                .orElseThrow(() -> new RuntimeException("Cannot find element " + onclick)).click();
    }

    protected GrapheneElement getTableRowElement(String tableName, int numberRow) {
        String onclick = "javascript:select('"+tableName+"',"+numberRow+")";
        return browser.findElements(By.tagName("tr"))
                .stream()
                .filter(GrapheneElement.class::isInstance)
                .map(GrapheneElement.class::cast)
                .filter(el -> onclick.equals(el.getAttribute("onclick")))
                .findAny()
                .orElseThrow(() -> new RuntimeException("Cannot find Element <tr> with tableName " + tableName +
                        " and numberRow: " + numberRow));
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
                            " and numberColumn: "+numberColumn));
        } catch (IndexOutOfBoundsException ex) {
            throw new RuntimeException("Cannot find Element <td> with rowElement " + rowElement.getText() +
                    " and numberColumn: "+numberColumn);
        }
    }

    protected void doSelectTableRow(String elementName, int numberRow) {
        String name = elementName+".selection";
        Optional.ofNullable(getTableRowElement(elementName,numberRow).findElement(By.name(name)))
                .orElseThrow(() -> new RuntimeException("Cannot find Element selection in Element <tr> with name " + elementName +
                        " and numberRow: " + numberRow))
                .click();
    }

    protected void doSelectTableRow(GrapheneElement rowElement, String elementName) {
        String name = elementName+".selection";
        Optional.ofNullable(rowElement.findElement(By.name(name)))
                .orElseThrow(() -> new RuntimeException("Cannot find Element selection in Element <tr> with name " + elementName))
                .click();
    }
}

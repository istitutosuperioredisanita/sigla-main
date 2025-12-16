package it.cnr.test.h2.docamm;

import it.cnr.test.h2.utenze.action.ActionDeployments;
import it.cnr.test.util.AlertMessage;
import org.junit.jupiter.api.*;

import java.io.File;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FatturaElettronicaTest extends ActionDeployments {
    public static final String USERNAME = "ENTETEST";
    public static final String PASSWORD = "PASSTEST";
    public static final String UO = "999.000";
    public static final String CDR = "999.000.000";
    public static final String AMM = "0.AMM";
    public static final String AMM_FATTUR = String.join(".", AMM, "FATTUR");
    public static final String AMM_FATTUR_FATPAS = String.join(".", AMM_FATTUR, "FATPAS");
    public static final String AMM_FATTUR_FATPAS_ELEADM = String.join(".", AMM_FATTUR_FATPAS, "ELEADM");
    public static final String AMM_FATTUR_FATPAS_ELE = String.join(".", AMM_FATTUR_FATPAS, "ELE");

    @Test
    @Order(1)
    public void testLogin() throws Exception {
        doLogin(USERNAME, PASSWORD);
        doLoginUO(UO, CDR);
    }

    @Test
    @Order(2)
    public void caricaFatturaElettronica() {
        switchToFrameMenu();
        doApriMenu(AMM);
        doApriMenu(AMM_FATTUR);
        doApriMenu(AMM_FATTUR_FATPAS);
        doSelezionaMenu(AMM_FATTUR_FATPAS_ELEADM);
        switchToFrameWorkspace();
        File file = new File("src/test/resources/fattura_elettronica.xml");
        getGrapheneElement("main.identificativoSdI").writeIntoElement("90000000011");
        getGrapheneElement("main.file").sendKeys(file.getAbsolutePath());
        doClickButton("doCaricaFattura");
        Assertions.assertEquals("File caricato correttamente", handleTextAlert(browser));
        doClickButton("doChiudiForm");

        switchToFrameMenu();
        doSelezionaMenu(AMM_FATTUR_FATPAS_ELE);

        switchToFrameWorkspace();
        getGrapheneElement("main.identificativoSdi").writeIntoElement("90000000011");
        doClickButton("doCerca");
        Assertions.assertEquals(AlertMessage.MESSAGE_RICERCA_MONO_RECORD.value(), handleTextAlert(browser));
    }

}

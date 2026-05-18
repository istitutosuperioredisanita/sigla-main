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
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;
import org.wildfly.common.Assert;

import java.io.File;

/**
 * Test di:
 * 1) creazione ordine con 1 riga consegna
 * 2) evasione riga consegna nr.1
 * 3) registrazione fattura con riscontro valore su riga consegna nr.1 con Fattura di importo uguale all’ordine
 * 4) registrazione nota credito di annullamento totale fattura creata
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Order(5)
public class CRUDOrdineAcqBP005 extends ActionDeployments {
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

    public static final String DATA_ODIERNA = "25102025";

    @Test
    @Order(1)
    public void testLogin() throws Exception {
        doLogin(USERNAME, PASSWORD);
        doLoginUO(UO, CDR);
    }

    /**
     * 1) creazione ordine con 1 riga consegna
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
        getGrapheneElement("main.nota").writeIntoElement("Prova per documentazione");

        //Passo sulla tab ‘Fornitore’ ed il terzo è proposto in automatico
        doClickButton("doTab('tab','tabOrdineFornitore')");
        Assertions.assertEquals("101", getGrapheneElement("main.findFornitore.cd_terzo").getAttribute("value"));

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
        Assertions.assertEquals("MAG", select.getFirstSelectedOption().getAttribute("value"));

        //Lascio la ‘Data prevista consegna’
        Assertions.assertNotNull(getGrapheneElement("main.Righe.dtPrevConsegna").getText());

        //Indico il codice magazzino: PT
        doClickButton("doBlankSearch(main.Righe.findMagazzino)");
        getGrapheneElement("main.Righe.findMagazzino.cdMagazzino").writeIntoElement(CD_MAGAZZINO);
        doClickButton("doSearch(main.Righe.findMagazzino)");

        //Ritorno sulla prima tab ‘ordine d’acquisto’
        doClickButton("doTab('tab','tabOrdineAcq')");

        //Indico motivo di assenza CIG: SPESE ECONOMALI
        select = new Select(getGrapheneElement("main.motivoAssenzaCig"));
        select.selectByValue("SPESE_ECONOMALI");

        //Salvo
        doClickButton("doSalva()");

        //Restituisce messaggio di creazione eseguita
        Assertions.assertEquals(AlertMessage.CREAZIONE_ESEGUITA.value(), handleTextAlert(browser));

        String pgOrdineCreated = getGrapheneElement("main.numero").getAttribute("value");
        sharedResource.setVal01(pgOrdineCreated);

        //modifico lo stato portandolo ‘In Approvazione’
        select = new Select(getGrapheneElement("main.statoForUpdate"));
        select.selectByValue("APP");

        //Salvo
        doClickButton("doSalva()");

        //Restituisce messaggio di salvataggio eseguito
        Assertions.assertEquals(AlertMessage.SALVATAGGIO_ESEGUITO.value(), handleTextAlert(browser));

        //modifico lo stato portandolo ‘Definitivo’
        select.selectByValue("DEF");

        //Salvo
        doClickButton("doSalva()");

        //Restituisce messaggio di assenza obbligazione
        Assertions.assertEquals("Sulla consegna 2025/"+CD_NUMERATORE+"/"+pgOrdineCreated+"/1/1 non è indicata l'obbligazione", handleTextAlert(browser));

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

        Assertions.assertEquals(AlertMessage.SALVATAGGIO_ESEGUITO.value(), handleTextAlert(browser));

    }

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
        getGrapheneElement("main.numeroBolla").writeIntoElement("7");

        //Data Consegna: Oggi
        getGrapheneElement("main.dataConsegna").writeIntoElement(DATA_ODIERNA);
        doClickButton("doOnDtConsegnaChange");

        //Eseguo la ricerca
        doClickButton("doCercaConsegneDaEvadere");

        String pgOrdineCreated = sharedResource.getVal01();

        //Trovo le mie riga di consegna dell’ordine creato con test precedente
        GrapheneElement rowElement1 = null;
        if (getTableColumnElement("main.ConsegneDaEvadere", 0, 1).getText().equals("2025/" + CD_NUMERATORE + "/" + pgOrdineCreated + "/1/1"))
            rowElement1 = getTableRowElement("main.ConsegneDaEvadere", 0);
        else
            Assertions.fail("Riga consegna 1 non individuata");

        assertTableRowAbsent("main.ConsegneDaEvadere",1);

        //Scelgo la prima consegna
        rowElement1.click();
        doSelectTableRow(rowElement1, "main.ConsegneDaEvadere");

        //Salvo
        doClickButton("doSalva()");
        String textAlert = handleTextAlert(browser);
        Assertions.assertEquals("Attenzione: è obbligatorio allegare il Documento di Trasporto (DDT).", textAlert);

        File file = new File("src/test/resources/contratto.pdf");
        doClickButton("doTab('tab','tabAllegati')");
        doClickButton("doAddToCRUD(main.ArchivioAllegati)");
        Select select = new Select(getGrapheneElement("main.ArchivioAllegati.aspectName"));
        select.selectByValue("P:sigla_evasione_attachment:ddt");
        getGrapheneElement("main.ArchivioAllegati.descrizione").writeIntoElement("TEST");
        getGrapheneElement("main.ArchivioAllegati.file").sendKeys(file.getAbsolutePath());
        getTableRowElement("main.ArchivioAllegati", 0).click();
        doClickButton("doTab('tab','tabEvasioneConsegne')");

        doClickButton("doSalva()");

        Assertions.assertEquals(AlertMessage.OPERAZIONE_EFFETTUATA.value(), handleTextAlert(browser));
    }

    @Test
    @Order(4)
    public void testRiscontroValore001() {
        switchToFrameMenu();
        doApriMenu(AMM);
        doApriMenu(AMM_FATTUR);
        doApriMenu(AMM_FATTUR_FATPAS);
        doSelezionaMenu(AMM_FATTUR_FATPAS_ELE);

        switchToFrameWorkspace();

        //Ricerco la fattura SDI: 90000000002
        getGrapheneElement("main.identificativoSdi").writeIntoElement("90000000007");

        Select select = new Select(getGrapheneElement("main.statoDocumento"));
        select.selectByValue("");

        doClickButton("doCerca()");

        Assertions.assertEquals(AlertMessage.MESSAGE_RICERCA_MONO_RECORD.value(), handleTextAlert(browser));

        //Clicco sul pulsante ‘Compila fattura’;
        doClickButton("submitForm('doCompilaFattura')");

        //Passo alla maschera di Registrazione Fattura
        getGrapheneElement("comando.doYes").click();

        //Sulla testata fattura indico lo stato ‘Liquidabile
        select = new Select(getGrapheneElement("main.stato_liquidazione"));
        select.selectByValue("LIQ");

        //Verifico che fattura da Ordini=Si
        Assertions.assertTrue(getGrapheneElement("main.flDaOrdini").isSelected());

        getGrapheneElement("main.ds_fattura_passiva").writeIntoElement("RISCONTRO VALORE TEST");

        //Passo alla tab ‘ordini’
        doClickButton("doTab('tab','tabFatturaPassivaOrdini')");

        //Effettuo la ricerca
        doClickButton("submitForm('doSelezionaOrdini')");

        String pgOrdineCreated = sharedResource.getVal01();

        //Seleziono la consegna n. 1
        By locator = By.xpath("//tr" +
                "[contains(normalize-space(td[4]//span),'2025') " +
                "and normalize-space(td[5]//span)='" + CD_NUMERATORE + "' " +
                "and normalize-space(td[6]//span)='" + pgOrdineCreated + "' " +
                "and normalize-space(td[7]//span)='1' " +
                "and normalize-space(td[8]//span)='1']");

        GrapheneElement rowElement = getGrapheneElement(locator);

        rowElement.findElement(By.name("mainTable.selection")).click();

        doClickButton("submitForm('doMultipleSelection')");

        //Digito ‘Fine riscontro a valore’ e mi sposto sulla tab ‘dettaglio’
        doClickButton("submitForm('doConfermaRiscontroAValore')");

        Assertions.assertEquals(AlertMessage.OPERAZIONE_EFFETTUATA.value(), handleTextAlert(browser));

        doClickButton("doSalva()");

        Assertions.assertEquals("Inserire il CIG o il motivo di assenza dello stesso!", handleTextAlert(browser));

        //Vado sulla tab obbligazioni
        doClickButton("doTab('tab','tabFatturaPassivaObbligazioni')");

        //Seleziono l'unica obbligazione presente
        doSelectTableRow("main.Obbligazioni",0);

        //Indico il motivo di assenza CIG
        select = new Select(getGrapheneElement("main.Obbligazioni.motivo_assenza_cig"));
        select.selectByValue("IMPRESA_COLLEGATA");

        doClickButton("doSalva()");

        Assertions.assertEquals(AlertMessage.CREAZIONE_ESEGUITA.value(), handleTextAlert(browser));

        //Vado sulla tab principale
        doClickButton("doTab('tab','tabFatturaPassiva')");

        //Registro il numero fattura creata
        String pgFatturaCreated = getGrapheneElement("main.pg_fattura_passiva").getAttribute("value");
        sharedResource.setVal02(pgFatturaCreated);

        doClickButton("doChiudiForm()");
    }

    @Test
    @Order(15)
    public void testAssociaNotaCredito001() {
        switchToFrameMenu();
        doSelezionaMenu(AMM_FATTUR_FATPAS_ELE);

        switchToFrameWorkspace();

        //Ricerco la fattura SDI: 90000000003 - Nota Credito
        getGrapheneElement("main.identificativoSdi").writeIntoElement("90000000008");

        Select select = new Select(getGrapheneElement("main.statoDocumento"));
        select.selectByValue("");

        doClickButton("doCerca()");

        Assertions.assertEquals(AlertMessage.MESSAGE_RICERCA_MONO_RECORD.value(), handleTextAlert(browser));

        //Clicco sul pulsante ‘Compila fattura’;
        doClickButton("submitForm('doCompilaFattura')");

        //Passo alla maschera di Registrazione Fattura
        getGrapheneElement("comando.doYes").click();

        getGrapheneElement("main.ds_fattura_passiva").writeIntoElement("NOTA CREDITO DI ANNULLAMENTO FATTURA");

        //Passo alla tab ‘Dettaglio’
        doClickButton("doTab('tab','tabFatturaPassivaDettaglio')");

        //Aggiungo un dettaglio
        doClickButton("submitForm('doAddToCRUD(main.Dettaglio)')");

        //Mi apre lista fatture associabili a nota credito. Cerco quella creata
        String pgFatturaCreated = sharedResource.getVal02();

        //Trovo la riga della fattura creata con test precedente
        GrapheneElement rowElement1=null;
        for (int riga = 0; riga < 10; riga++) {
            try {
                if (getTableColumnElementFast("mainTable", riga, 3).getText().equals(pgFatturaCreated))
                    rowElement1 = getTableRowElementFast("mainTable", riga);
            } catch (RuntimeException ignored) {
            }
        }

        Assertions.assertNotNull(rowElement1,"Fattura " + pgFatturaCreated + " da associare a Nota Credito non trovata non individuata");

        //Seleziono la nota
        rowElement1.click();

        //Seleziono l'unica riga proposta della fattura creata
        browser.findElement(By.name("mainTable.selection")).click();

        doClickButton("submitForm('doMultipleSelection')");

        doClickButton("doSalva()");

        Assertions.assertEquals(AlertMessage.CREAZIONE_ESEGUITA.value(), handleTextAlert(browser));

        //Vado sulla tab principale
        doClickButton("doTab('tab','tabFatturaPassiva')");

        //Registro il numero della nota creata
        String pgNotaCreated = getGrapheneElement("main.pg_fattura_passiva").getAttribute("value");
        sharedResource.setVal03(pgNotaCreated);

        //Vado sulla tab economica per controllare scrittura
        doClickButton("doTab('tab','tabEconomica')");
        //Il conto di costo P00047 (fatture da ricevere in quanto nota credito di fattura con ordine) in Avere per 488,00;
        Assertions.assertEquals("P00047", getTableColumnElement("main.Movimenti Avere",0,1).getText());
        Assertions.assertEquals("488,00", getTableColumnElement("main.Movimenti Avere",0,5).getText());
        assertTableRowAbsent("main.Movimenti Avere",1);

        //Il controconto di debito P13012 in Dare per 205,00;
        Assertions.assertEquals("P13003", getTableColumnElement("main.Movimenti Dare",0,1).getText());
        Assertions.assertEquals("400,00", getTableColumnElement("main.Movimenti Dare",0,5).getText());

        //Il conto IVA Split P71012I in Avere per 44,00
        Assertions.assertEquals("P71012I", getTableColumnElement("main.Movimenti Dare",1,1).getText());
        Assertions.assertEquals("88,00", getTableColumnElement("main.Movimenti Dare",1,5).getText());

        assertTableRowAbsent("main.Movimenti Dare",2);

        //Vado sulla tab analitica per controllare scrittura
        doClickButton("doTab('tab','tabAnalitica')");
        assertTableRowAbsent("main.Movimenti Analitici",0);

        doClickButton("doChiudiForm()");
    }
}

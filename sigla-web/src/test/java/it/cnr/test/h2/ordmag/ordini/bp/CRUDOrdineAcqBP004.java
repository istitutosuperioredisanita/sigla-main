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
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;
import org.wildfly.common.Assert;

import java.util.Optional;

/**
 * Test di:
 * 1) evasione riga consegna ordine creato in anno precedente
 * 2) verifica scrittura economica/analitica su consegna
 * 3) annullamento evasione riga consegna
 * 4) nuova evasione riga consegna
 * 5) verifica scrittura economica/analitica su consegna
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Order(4)
public class CRUDOrdineAcqBP004 extends ActionDeployments {
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

    public static final String AMM_INVENT = "0.AMM.INVENT";
    public static final String AMM_INVENT_ORDINI = "0.AMM.INVENT.ORDINI";
    public static final String AMM_INVENT_ORDINI_TRANSITO = "0.AMM.INVENT.ORDINI.BENI_TRANSITO";
    public static final String AMM_INVENT_ORDINI_INVENTARIAZIONE = "0.AMM.INVENT.ORDINI.INVENTARIAZIONE";

    public static final String MAG = "0.MAG";
    public static final String MAG_ANNULLAMENTO = "0.MAG.ANNULLAMENTO";
    public static final String MAG_ANNULLAMENTO_M = "0.MAG.ANNULLAMENTO.GESTIONE";

    public static final String CD_UNITA_OPERATIVA = "DRUE";
    public static final String CD_NUMERATORE = "DSA";
    public static final String CD_MAGAZZINO = "CS";
    public static final String UO = "000.000";
    public static final String CDR = "000.000.000";

    public static final String CONTRATTO_ESERCIZIO = "2025";
    public static final String CONTRATTO_NUMERO = "3";

    public static final String BENE_SERVIZIO_CODICE_01 = "19001";

    public static final String DATA_ODIERNA = "25102025";

    @Test
    @Order(1)
    public void testLogin() throws Exception {
        doLogin(USERNAME, PASSWORD);
        doLoginUO(UO, CDR);
    }

    /**
     * 1) evasione riga consegna ordine creato in anno precedente
     */
    @Test
    @Order(2)
    public void testEvasioneConsegna001() {
        switchToFrameMenu();
        doApriMenu(ORD);
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
        getGrapheneElement("main.numeroBolla").writeIntoElement("11");

        //Data Consegna: Oggi
        getGrapheneElement("main.dataConsegna").writeIntoElement(DATA_ODIERNA);
        doClickButton("doOnDtConsegnaChange");

        //Eseguo la ricerca
        doClickButton("doCercaConsegneDaEvadere");

        //Trovo le mie due righe di consegna dell’ordine creato con liquibase 2024/DSA/1/1/2
        String keyRowElement = "2024/"+CD_NUMERATORE+"/1/1/2";

        GrapheneElement rowElement=null;
        for (int riga = 0; riga < 10; riga++) {
            try {
                if (getTableColumnElement("main.ConsegneDaEvadere", riga, 1).getText().equals(keyRowElement))
                    rowElement = getTableRowElement("main.ConsegneDaEvadere", riga);
            } catch (RuntimeException ignored) {
            }
        }

        Assertions.assertNotNull(rowElement,"Riga Consegna " + keyRowElement + " non individuata");

        //Scelgo la consegna
        rowElement.click();
        doSelectTableRow(rowElement, "main.ConsegneDaEvadere");

        //Salvo
        doClickButton("doSalva()");
        
        Assertions.assertEquals(AlertMessage.OPERAZIONE_EFFETTUATA.value(), handleTextAlert(browser));

    }

    /**
     * 2) verifica scrittura economica/analitica su consegna
     */
    @Test
    @Order(3)
    public void testVerificaScritturaOrdine001() {
        //Verifico che la scrittura sull'ordine sia stata eseguita correttamente
        switchToFrameMenu();
        doApriMenu(ORD_ORDACQ);
        doSelezionaMenu(ORD_ORDACQ_M);

        switchToFrameWorkspace();

        //Ricerco l’ordine
        doClickButton("doNuovaRicerca()");

        doClickButton("doBlankSearch(main.findUnitaOperativaOrd)");
        getGrapheneElement("main.findUnitaOperativaOrd.cdUnitaOperativa").writeIntoElement(CD_UNITA_OPERATIVA);
        doClickButton("doSearch(main.findUnitaOperativaOrd)");

        doClickButton("doBlankSearch(main.findNumerazioneOrd)");
        getGrapheneElement("main.findNumerazioneOrd.cdNumeratore").writeIntoElement(CD_NUMERATORE);
        doClickButton("doSearch(main.findNumerazioneOrd)");

        getGrapheneElement("main.esercizio").clear();
        getGrapheneElement("main.esercizio").writeIntoElement("2024");
        getGrapheneElement("main.numero").writeIntoElement("1");

        doClickButton("doCerca()");
        
        Assertions.assertEquals(AlertMessage.MESSAGE_RICERCA_MONO_RECORD.value(), handleTextAlert(browser));


        //Vado sul dettaglio analitico a livello di Ordine
        doClickButton("doTab('tab','tabOrdineResultDetailEcoCoge')");
        doSelectTableRow("main.Dati Analitici",0);

        Assertions.assertEquals("C13024", getTableColumnElement("main.Dati Analitici",0,1).getText());
        Assertions.assertEquals("C13024", getTableColumnElement("main.Dati Analitici",0,2).getText());
        Assertions.assertEquals("000.000.000", getTableColumnElement("main.Dati Analitici",0,3).getText());
        Assertions.assertEquals("PTEST001", getTableColumnElement("main.Dati Analitici",0,4).getText());
        Assertions.assertEquals("122,00", getTableColumnElement("main.Dati Analitici",0,5).getText());

        Assertions.assertEquals("C13024", getTableColumnElement("main.Dati Analitici",1,1).getText());
        Assertions.assertEquals(" ", getTableColumnElement("main.Dati Analitici",1,2).getText());
        Assertions.assertEquals(" ", getTableColumnElement("main.Dati Analitici",1,3).getText());
        Assertions.assertEquals(" ", getTableColumnElement("main.Dati Analitici",1,4).getText());
        Assertions.assertEquals("122,00", getTableColumnElement("main.Dati Analitici",1,5).getText());

        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.Dati Analitici",2),"Cannot find Element <tr> with tableName main.Dati Analitici and numberRow: 2");

        //Vado sul dettaglio a livello riga Ordine
        doClickButton("doTab('tab','tabOrdineAcqDettaglio')");
        doSelectTableRow("main.Righe",0);

        doClickButton("doTab('tabOrdineAcqDettagli','tabOrdineRigaResultDetailEcoCoge')");

        Assertions.assertEquals("C13024", getGrapheneElement("main.Righe.find_voce_ep.cd_voce_ep").getAttribute("value"));
        Assertions.assertEquals("244,00", getGrapheneElement("imCostoEcoConsegne").getAttribute("value"));
        Assertions.assertEquals("122,00", getGrapheneElement("imCostoEcoRipartitoConsegne").getAttribute("value"));
        Assertions.assertEquals("122,00", getGrapheneElement("imCostoEcoDaRipartireConsegne").getAttribute("value"));

        doSelectTableRow("main.Righe.Dati Coge/Coan",0);

        Assertions.assertEquals("C13024", getTableColumnElement("main.Righe.Dati Coge/Coan",0,1).getText());
        Assertions.assertEquals("PTEST001", getTableColumnElement("main.Righe.Dati Coge/Coan",0,2).getText());
        Assertions.assertEquals("000.000.000", getTableColumnElement("main.Righe.Dati Coge/Coan",0,3).getText());
        Assertions.assertEquals("122,00", getTableColumnElement("main.Righe.Dati Coge/Coan",0,4).getText());
        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.Righe.Dati Coge/Coan",1),"Cannot find Element <tr> with tableName main.Righe.Dati Coge/Coan and numberRow: 1");

        //Vado sulla tab ‘consegne’
        doClickButton("doTab('tabOrdineAcqDettagli','tabOrdineConsegna')");

        //Vado sulla tab ‘consegne’ e verifico che la riga consegna n. 2 sia in stato ‘EVASA’ e la consegna
        GrapheneElement rowElement = null;
        if (getTableColumnElement("main.Righe.Consegne",0, 1).getText().equals("2"))
            rowElement = getTableRowElement("main.Righe.Consegne",0);
        else if (getTableColumnElement("main.Righe.Consegne",1, 1).getText().equals("2"))
            rowElement = getTableRowElement("main.Righe.Consegne",1);
        else
            Assertions.fail("Riga consegna 2 non individuata");

        //Verifico che la riga consegna n. 1 sia in stato ‘EVASA’ e la consegna n. 2 ancora in stato INSERITA. Entrambe ‘Non Associate’.
        Assertions.assertEquals("Evasa", getTableColumnElement(rowElement,5).getText());
        Assertions.assertEquals("Non Associata", getTableColumnElement(rowElement,6).getText());

        //Mi posiziono sulla consegna
        rowElement.click();

        //Entro su ‘scrittura economica’.
        doClickButton("submitForm('doVisualizzaEconomica');");

        Assertions.assertEquals("2025", getGrapheneElement("main.esercizio").getAttribute("value"));
        Assertions.assertEquals("Y", getGrapheneElement("main.attiva").getAttribute("value"));
        Assertions.assertEquals("122,00", getGrapheneElement("main.imTotaleDare").getAttribute("value"));
        Assertions.assertEquals("122,00", getGrapheneElement("main.imTotaleAvere").getAttribute("value"));

        //Nella sezione ‘Dare’ è presente correttamente il conto di costo: C13003
        doClickButton("doTab('tab','tabDare')");
        Assertions.assertEquals("C13024", getTableColumnElement("main.MovimentiDare",0,1).getText());
        Assertions.assertEquals("122,00", getTableColumnElement("main.MovimentiDare",0,5).getText());
        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.MovimentiDare",1),"Cannot find Element <tr> with tableName main.MovimentiDare and numberRow: 1");

        //Nella sezione ‘Avere’ è presente correttamente il conto fatture da ricevere: P00047.
        doClickButton("doTab('tab','tabAvere')");
        Assertions.assertEquals("P00047", getTableColumnElement("main.MovimentiAvere",0,1).getText());
        Assertions.assertEquals("122,00", getTableColumnElement("main.MovimentiAvere",0,5).getText());
        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.MovimentiAvere",1),"Cannot find Element <tr> with tableName main.MovimentiAvere and numberRow: 1");

        doClickButton("doChiudiForm()");
        doClickButton("submitForm('doVisualizzaAnalitica');");

        Assertions.assertEquals("2025", getGrapheneElement("main.esercizio").getAttribute("value"));
        Assertions.assertEquals("Y", getGrapheneElement("main.attiva").getAttribute("value"));
        Assertions.assertEquals("122,00", getGrapheneElement("main.imTotaleMov").getAttribute("value"));

        doClickButton("doTab('tab','tabMovimenti')");
        Assertions.assertEquals("C13024", getTableColumnElement("main.Movimenti",0,1).getText());
        Assertions.assertEquals("Dare", getTableColumnElement("main.Movimenti",0,2).getText());
        Assertions.assertEquals("000.000.000", getTableColumnElement("main.Movimenti",0,3).getText());
        Assertions.assertEquals("PTEST001", getTableColumnElement("main.Movimenti",0,4).getText());
        Assertions.assertEquals("122,00", getTableColumnElement("main.Movimenti",0,5).getText());
        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.Movimenti",1),"Cannot find Element <tr> with tableName main.Movimenti and numberRow: 1");

        doClickButton("doChiudiForm()");
    }

    /**
     * 3) annullamento evasione riga consegna
     */
    @Test
    @Order(4)
    public void testAnnullaEvasioneConsegna001() {
        switchToFrameMenu();
        doApriMenu(MAG);
        doApriMenu(MAG_ANNULLAMENTO);
        doSelezionaMenu(MAG_ANNULLAMENTO_M);

        switchToFrameWorkspace();

        //Scelgo Unità operativa: DRUE
        doClickButton("doBlankSearch(main.findUnitaOperativaOrd)");
        getGrapheneElement("main.findUnitaOperativaOrd.cdUnitaOperativa").writeIntoElement(CD_UNITA_OPERATIVA);
        doClickButton("doSearch(main.findUnitaOperativaOrd)");

        //Scelgo Magazzino: PT
        doClickButton("doBlankSearch(main.findMagazzino)");
        getGrapheneElement("main.findMagazzino.cdMagazzino").writeIntoElement(CD_MAGAZZINO);
        doClickButton("doSearch(main.findMagazzino)");

        getGrapheneElement("main.daNumeroOrdine").writeIntoElement("1");
        getGrapheneElement("main.aNumeroOrdine").writeIntoElement("1");

        doClickButton("doCerca");

        //Seleziono la consegna 2
        Optional<GrapheneElement> element = browser.findElements(By.tagName("tr"))
                .stream()
                .filter(GrapheneElement.class::isInstance)
                .map(GrapheneElement.class::cast)
                .filter(rowElement -> {
                    try {
                        return getTableColumnElement(rowElement, 17).getText().contains("2024") &&
                                CD_NUMERATORE.equals(getTableColumnElement(rowElement, 18).getText()) &&
                                "1".equals(getTableColumnElement(rowElement, 19).getText()) &&
                                "1".equals(getTableColumnElement(rowElement, 20).getText()) &&
                                "2".equals(getTableColumnElement(rowElement, 21).getText());
                    } catch (RuntimeException ex) {
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

    /**
     * 4) nuova evasione riga consegna
     */
    @Test
    @Order(5)
    public void testEvasioneConsegna002() {
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
        getGrapheneElement("main.numeroBolla").writeIntoElement("11");

        //Data Consegna: Oggi
        getGrapheneElement("main.dataConsegna").writeIntoElement(DATA_ODIERNA);
        doClickButton("doOnDtConsegnaChange");

        //Eseguo la ricerca
        doClickButton("doCercaConsegneDaEvadere");

        //Trovo le mie due righe di consegna dell’ordine creato con liquibase 2024/DSA/1/1/2
        String keyRowElement = "2024/"+CD_NUMERATORE+"/1/1/2";

        GrapheneElement rowElement=null;
        for (int riga = 0; riga < 10; riga++) {
            try {
                if (getTableColumnElement("main.ConsegneDaEvadere", riga, 1).getText().equals(keyRowElement))
                    rowElement = getTableRowElement("main.ConsegneDaEvadere", riga);
            } catch (RuntimeException ignored) {
            }
        }

        Assertions.assertNotNull(rowElement,"Riga Consegna " + keyRowElement + " non individuata");

        //Scelgo la consegna
        rowElement.click();
        doSelectTableRow(rowElement, "main.ConsegneDaEvadere");

        //Salvo
        doClickButton("doSalva()");
        
        Assertions.assertEquals(AlertMessage.OPERAZIONE_EFFETTUATA.value(), handleTextAlert(browser));

    }

    /**
     * 5) verifica scrittura economica/analitica su consegna
     */
    @Test
    @Order(6)
    public void testVerificaScrittureOrdine002() {
        //Verifico che la scrittura sull'ordine sia stata eseguita correttamente
        switchToFrameMenu();
        doSelezionaMenu(ORD_ORDACQ_M);

        switchToFrameWorkspace();

        //Cerco l'ordine DSA/1/1/1 per verificare che l'annullamento del riscontro a valore abbia rimesso i valori corretti
        doClickButton("doNuovaRicerca()");

        getGrapheneElement("main.findUnitaOperativaOrd.cdUnitaOperativa").writeIntoElement(CD_UNITA_OPERATIVA);
        doClickButton("doSearch(main.findUnitaOperativaOrd)");

        getGrapheneElement("main.findNumerazioneOrd.cdNumeratore").writeIntoElement(CD_NUMERATORE);
        doClickButton("doSearch(main.findNumerazioneOrd)");

        getGrapheneElement("main.esercizio").clear();
        getGrapheneElement("main.esercizio").writeIntoElement("2024");
        getGrapheneElement("main.numero").writeIntoElement("1");

        doClickButton("doCerca()");
        
        Assertions.assertEquals(AlertMessage.MESSAGE_RICERCA_MONO_RECORD.value(), handleTextAlert(browser));


        //Vado sul dettaglio analitico a livello di Ordine
        doClickButton("doTab('tab','tabOrdineResultDetailEcoCoge')");
        doSelectTableRow("main.Dati Analitici",0);

        Assertions.assertEquals("C13024", getTableColumnElement("main.Dati Analitici",0,1).getText());
        Assertions.assertEquals("C13024", getTableColumnElement("main.Dati Analitici",0,2).getText());
        Assertions.assertEquals("000.000.000", getTableColumnElement("main.Dati Analitici",0,3).getText());
        Assertions.assertEquals("PTEST001", getTableColumnElement("main.Dati Analitici",0,4).getText());
        Assertions.assertEquals("122,00", getTableColumnElement("main.Dati Analitici",0,5).getText());

        Assertions.assertEquals("C13024", getTableColumnElement("main.Dati Analitici",1,1).getText());
        Assertions.assertEquals(" ", getTableColumnElement("main.Dati Analitici",1,2).getText());
        Assertions.assertEquals(" ", getTableColumnElement("main.Dati Analitici",1,3).getText());
        Assertions.assertEquals(" ", getTableColumnElement("main.Dati Analitici",1,4).getText());
        Assertions.assertEquals("122,00", getTableColumnElement("main.Dati Analitici",1,5).getText());

        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.Dati Analitici",2),"Cannot find Element <tr> with tableName main.Dati Analitici and numberRow: 2");

        //Vado sul dettaglio a livello riga Ordine
        doClickButton("doTab('tab','tabOrdineAcqDettaglio')");
        doSelectTableRow("main.Righe",0);

        doClickButton("doTab('tabOrdineAcqDettagli','tabOrdineRigaResultDetailEcoCoge')");

        Assertions.assertEquals("C13024", getGrapheneElement("main.Righe.find_voce_ep.cd_voce_ep").getAttribute("value"));
        Assertions.assertEquals("244,00", getGrapheneElement("imCostoEcoConsegne").getAttribute("value"));
        Assertions.assertEquals("122,00", getGrapheneElement("imCostoEcoRipartitoConsegne").getAttribute("value"));
        Assertions.assertEquals("122,00", getGrapheneElement("imCostoEcoDaRipartireConsegne").getAttribute("value"));

        doSelectTableRow("main.Righe.Dati Coge/Coan",0);

        Assertions.assertEquals("C13024", getTableColumnElement("main.Righe.Dati Coge/Coan",0,1).getText());
        Assertions.assertEquals("PTEST001", getTableColumnElement("main.Righe.Dati Coge/Coan",0,2).getText());
        Assertions.assertEquals("000.000.000", getTableColumnElement("main.Righe.Dati Coge/Coan",0,3).getText());
        Assertions.assertEquals("122,00", getTableColumnElement("main.Righe.Dati Coge/Coan",0,4).getText());
        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.Righe.Dati Coge/Coan",1),"Cannot find Element <tr> with tableName main.Righe.Dati Coge/Coan and numberRow: 1");

        //Vado sulla tab ‘consegne’
        doClickButton("doTab('tabOrdineAcqDettagli','tabOrdineConsegna')");

        //Vado sulla tab ‘consegne’
        //Trovo le mie due righe di consegna dell’ordine creato con liquibase 2024/DSA/1/1/2
        GrapheneElement rowElement1 = null, rowElement2 = null;
        for (int riga = 0; riga < 10; riga++) {
            try {
                if (getTableColumnElement("main.Righe.Consegne", riga, 1).getText().equals("1"))
                    rowElement1 = getTableRowElement("main.Righe.Consegne", riga);
                else if (getTableColumnElement("main.Righe.Consegne", riga, 1).getText().equals("2"))
                    rowElement2 = getTableRowElement("main.Righe.Consegne", riga);
            } catch (RuntimeException ignored) {
            }
        }
        Assertions.assertNotNull(rowElement1,"Riga Consegna 1 non individuata");
        Assertions.assertNotNull(rowElement2,"Riga Consegna 2 non individuata");

        //Verifico che la riga consegna n. 1 e 2 siano in stato ‘EVASA’ ed entrambe ‘Non Associate’.
        Assertions.assertEquals("Evasa", getTableColumnElement(rowElement1,5).getText());
        Assertions.assertEquals("Non Associata", getTableColumnElement(rowElement1,6).getText());

        Assertions.assertEquals("Evasa", getTableColumnElement(rowElement2,5).getText());
        Assertions.assertEquals("Non Associata", getTableColumnElement(rowElement2,6).getText());

        doClickButton("doChiudiForm()");
    }

    /**
     * 6) registrazione fattura con riscontro valore su riga consegna nr.1 evasa in anni precedenti
     */
    @Test
    @Order(7)
    public void testRiscontroValore001() {
        switchToFrameMenu();
        doApriMenu(AMM);
        doApriMenu(AMM_FATTUR);
        doApriMenu(AMM_FATTUR_FATPAS);
        doSelezionaMenu(AMM_FATTUR_FATPAS_ELE);

        switchToFrameWorkspace();

        //Ricerco la fattura SDI: 90000000002
        getGrapheneElement("main.identificativoSdi").writeIntoElement("90000000005");

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

        getGrapheneElement("main.ds_fattura_passiva").writeIntoElement("RISCONTRO VALORE TEST ORDINE EVASO ANNO PRECEDENTE");

        //Passo alla tab ‘ordini’
        doClickButton("doTab('tab','tabFatturaPassivaOrdini')");

        //Effettuo la ricerca
        doClickButton("submitForm('doSelezionaOrdini')");

        //Seleziono l'ordine creato
        Optional<GrapheneElement> element = browser.findElements(By.tagName("tr"))
                .stream()
                .filter(GrapheneElement.class::isInstance)
                .map(GrapheneElement.class::cast)
                .filter(rowElement -> {
                    try {
                        return getTableColumnElement(rowElement, 3).getText().contains("2024") &&
                                CD_NUMERATORE.equals(getTableColumnElement(rowElement, 4).getText()) &&
                                "1".equals(getTableColumnElement(rowElement, 5).getText()) &&
                                "1".equals(getTableColumnElement(rowElement, 6).getText()) &&
                                "1".equals(getTableColumnElement(rowElement, 7).getText());
                    } catch (RuntimeException ex) {
                        return false;
                    }
                })
                .findAny();

        Assert.assertTrue(element.isPresent());

        element.get().findElement(By.name("mainTable.selection")).click();

        doClickButton("submitForm('doMultipleSelection')");

        //Digito ‘Fine riscontro a valore’
        doClickButton("submitForm('doConfermaRiscontroAValore')");

        
        Assertions.assertEquals(AlertMessage.OPERAZIONE_EFFETTUATA.value(), handleTextAlert(browser));


        doClickButton("doSalva()");

        
        Assertions.assertEquals(AlertMessage.CREAZIONE_ESEGUITA.value(), handleTextAlert(browser));


        //Vado sulla tab principale
        doClickButton("doTab('tab','tabFatturaPassiva')");

        //Registro il numero fattura creata
        String pgFatturaCreated = getGrapheneElement("main.pg_fattura_passiva").getAttribute("value");
        sharedResource.setVal02(pgFatturaCreated);

        //Vado sulla tab economica per controllare scrittura
        doClickButton("doTab('tab','tabEconomica')");
        Assertions.assertEquals("P00047", getTableColumnElement("main.Movimenti Dare",0,1).getText());
        Assertions.assertEquals("122,00", getTableColumnElement("main.Movimenti Dare",0,5).getText());

        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.Movimenti Dare",1),"Cannot find Element <tr> with tableName 'main.Movimenti Dare' and numberRow: 1");

        Assertions.assertEquals("P13024", getTableColumnElement("main.Movimenti Avere",0,1).getText());
        Assertions.assertEquals("100,00", getTableColumnElement("main.Movimenti Avere",0,5).getText());

        Assertions.assertEquals("P71012I", getTableColumnElement("main.Movimenti Avere",1,1).getText());
        Assertions.assertEquals("22,00", getTableColumnElement("main.Movimenti Avere",1,5).getText());

        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.Movimenti Avere",2),"Cannot find Element <tr> with tableName 'main.Movimenti Avere' and numberRow: 2");

        //Vado sulla tab analitica
        doClickButton("doTab('tab','tabAnalitica')");
        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.Movimenti Analitici",0),"Cannot find Element <tr> with tableName 'main.Movimenti Analitici' and numberRow: 0");

        doClickButton("doChiudiForm()");
    }

    /**
     * 7) registrazione fattura con riscontro valore su riga consegna nr.2 evasa in anno corrente
     */
    @Test
    @Order(8)
    public void testRiscontroValore002() {
        switchToFrameMenu();
        doSelezionaMenu(AMM_FATTUR_FATPAS_ELE);

        switchToFrameWorkspace();

        //Ricerco la fattura SDI: 90000000002
        getGrapheneElement("main.identificativoSdi").writeIntoElement("90000000006");

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

        getGrapheneElement("main.ds_fattura_passiva").writeIntoElement("RISCONTRO VALORE TEST ORDINE EVASO ANNO PRECEDENTE");

        //Passo alla tab ‘ordini’
        doClickButton("doTab('tab','tabFatturaPassivaOrdini')");

        //Effettuo la ricerca
        doClickButton("submitForm('doSelezionaOrdini')");

        //Seleziono l'ordine creato
        Optional<GrapheneElement> element = browser.findElements(By.tagName("tr"))
                .stream()
                .filter(GrapheneElement.class::isInstance)
                .map(GrapheneElement.class::cast)
                .filter(rowElement -> {
                    try {
                        return getTableColumnElement(rowElement, 3).getText().contains("2024") &&
                                CD_NUMERATORE.equals(getTableColumnElement(rowElement, 4).getText()) &&
                                "1".equals(getTableColumnElement(rowElement, 5).getText()) &&
                                "1".equals(getTableColumnElement(rowElement, 6).getText()) &&
                                "2".equals(getTableColumnElement(rowElement, 7).getText());
                    } catch (RuntimeException ex) {
                        return false;
                    }
                })
                .findAny();

        Assert.assertTrue(element.isPresent());

        element.get().findElement(By.name("mainTable.selection")).click();

        doClickButton("submitForm('doMultipleSelection')");

        //Digito ‘Fine riscontro a valore’
        doClickButton("submitForm('doConfermaRiscontroAValore')");

        
        Assertions.assertEquals(AlertMessage.OPERAZIONE_EFFETTUATA.value(), handleTextAlert(browser));


        doClickButton("doSalva()");

        
        Assertions.assertEquals(AlertMessage.CREAZIONE_ESEGUITA.value(), handleTextAlert(browser));


        //Vado sulla tab principale
        doClickButton("doTab('tab','tabFatturaPassiva')");

        //Registro il numero fattura creata
        String pgFatturaCreated = getGrapheneElement("main.pg_fattura_passiva").getAttribute("value");
        sharedResource.setVal02(pgFatturaCreated);

        //Vado sulla tab economica per controllare scrittura
        doClickButton("doTab('tab','tabEconomica')");
        Assertions.assertEquals("P00047", getTableColumnElement("main.Movimenti Dare",0,1).getText());
        Assertions.assertEquals("122,00", getTableColumnElement("main.Movimenti Dare",0,5).getText());

        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.Movimenti Dare",1),"Cannot find Element <tr> with tableName 'main.Movimenti Dare' and numberRow: 1");

        Assertions.assertEquals("P13024", getTableColumnElement("main.Movimenti Avere",0,1).getText());
        Assertions.assertEquals("100,00", getTableColumnElement("main.Movimenti Avere",0,5).getText());

        Assertions.assertEquals("P71012I", getTableColumnElement("main.Movimenti Avere",1,1).getText());
        Assertions.assertEquals("22,00", getTableColumnElement("main.Movimenti Avere",1,5).getText());

        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.Movimenti Avere",2),"Cannot find Element <tr> with tableName 'main.Movimenti Avere' and numberRow: 2");

        //Vado sulla tab analitica
        doClickButton("doTab('tab','tabAnalitica')");
        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.Movimenti Analitici",0),"Cannot find Element <tr> with tableName 'main.Movimenti Analitici' and numberRow: 0");

        doClickButton("doChiudiForm()");
    }

    @Test
    @Order(9)
    public void testVerificaScritturaOrdine003() {
        //Verifico che la scrittura sull'ordine sia stata eseguita correttamente
        switchToFrameMenu();
        doSelezionaMenu(ORD_ORDACQ_M);

        switchToFrameWorkspace();

        //Ricerco l’ordine
        doClickButton("doNuovaRicerca()");

        doClickButton("doBlankSearch(main.findUnitaOperativaOrd)");
        getGrapheneElement("main.findUnitaOperativaOrd.cdUnitaOperativa").writeIntoElement(CD_UNITA_OPERATIVA);
        doClickButton("doSearch(main.findUnitaOperativaOrd)");

        doClickButton("doBlankSearch(main.findNumerazioneOrd)");
        getGrapheneElement("main.findNumerazioneOrd.cdNumeratore").writeIntoElement(CD_NUMERATORE);
        doClickButton("doSearch(main.findNumerazioneOrd)");

        getGrapheneElement("main.esercizio").clear();
        getGrapheneElement("main.esercizio").writeIntoElement("2024");
        getGrapheneElement("main.numero").writeIntoElement("1");

        doClickButton("doCerca()");
        
        Assertions.assertEquals(AlertMessage.MESSAGE_RICERCA_MONO_RECORD.value(), handleTextAlert(browser));


        //Vado sul dettaglio analitico a livello di Ordine
        doClickButton("doTab('tab','tabOrdineResultDetailEcoCoge')");
        doSelectTableRow("main.Dati Analitici",0);

        Assertions.assertEquals("C13024", getTableColumnElement("main.Dati Analitici",0,1).getText());
        Assertions.assertEquals("C13024", getTableColumnElement("main.Dati Analitici",0,2).getText());
        Assertions.assertEquals("000.000.000", getTableColumnElement("main.Dati Analitici",0,3).getText());
        Assertions.assertEquals("PTEST001", getTableColumnElement("main.Dati Analitici",0,4).getText());
        Assertions.assertEquals("244,00", getTableColumnElement("main.Dati Analitici",0,5).getText());

        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.Dati Analitici",1),"Cannot find Element <tr> with tableName main.Dati Analitici and numberRow: 1");

        //Vado sul dettaglio a livello riga Ordine
        doClickButton("doTab('tab','tabOrdineAcqDettaglio')");
        doSelectTableRow("main.Righe",0);

        doClickButton("doTab('tabOrdineAcqDettagli','tabOrdineRigaResultDetailEcoCoge')");

        Assertions.assertEquals("C13024", getGrapheneElement("main.Righe.find_voce_ep.cd_voce_ep").getAttribute("value"));
        Assertions.assertEquals("244,00", getGrapheneElement("imCostoEcoConsegne").getAttribute("value"));
        Assertions.assertEquals("244,00", getGrapheneElement("imCostoEcoRipartitoConsegne").getAttribute("value"));
        Assertions.assertEquals("0,00", getGrapheneElement("imCostoEcoDaRipartireConsegne").getAttribute("value"));

        doSelectTableRow("main.Righe.Dati Coge/Coan",0);

        Assertions.assertEquals("C13024", getTableColumnElement("main.Righe.Dati Coge/Coan",0,1).getText());
        Assertions.assertEquals("PTEST001", getTableColumnElement("main.Righe.Dati Coge/Coan",0,2).getText());
        Assertions.assertEquals("000.000.000", getTableColumnElement("main.Righe.Dati Coge/Coan",0,3).getText());
        Assertions.assertEquals("244,00", getTableColumnElement("main.Righe.Dati Coge/Coan",0,4).getText());
        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.Righe.Dati Coge/Coan",1),"Cannot find Element <tr> with tableName main.Righe.Dati Coge/Coan and numberRow: 1");

        //Vado sulla tab ‘consegne’
        doClickButton("doTab('tabOrdineAcqDettagli','tabOrdineConsegna')");

        //Vado sulla tab ‘consegne’
        //Trovo le mie due righe di consegna dell’ordine creato con liquibase 2024/DSA/1/1/2
        GrapheneElement rowElement1 = null, rowElement2 = null;
        for (int riga = 0; riga < 10; riga++) {
            try {
                if (getTableColumnElement("main.Righe.Consegne", riga, 1).getText().equals("1"))
                    rowElement1 = getTableRowElement("main.Righe.Consegne", riga);
                else if (getTableColumnElement("main.Righe.Consegne", riga, 1).getText().equals("2"))
                    rowElement2 = getTableRowElement("main.Righe.Consegne", riga);
            } catch (RuntimeException ignored) {
            }
        }
        Assertions.assertNotNull(rowElement1,"Riga Consegna 1 non individuata");
        Assertions.assertNotNull(rowElement2,"Riga Consegna 2 non individuata");

        //Verifico che la riga consegna n. 1 sia in stato ‘EVASA’ e la consegna n. 2 ancora in stato INSERITA. Entrambe ‘Non Associate’.
        Assertions.assertEquals("Evasa", getTableColumnElement(rowElement1,5).getText());
        Assertions.assertEquals("Associata Totalmente", getTableColumnElement(rowElement1,6).getText());

        Assertions.assertEquals("Evasa", getTableColumnElement(rowElement2,5).getText());
        Assertions.assertEquals("Associata Totalmente", getTableColumnElement(rowElement2,6).getText());

        //Mi posiziono sulla prima consegna
        rowElement1.click();

        //Entro su ‘scrittura economica’.
        doClickButton("submitForm('doVisualizzaEconomica');");

        //Mi ritorna "Scrittura Economica non presente!" perchè avvenuta in periodo precedente
        //dove l'evasione non ha generato scritture
        
        Assertions.assertEquals("Scrittura Economica non presente!", handleTextAlert(browser));


        //Entro su ‘scrittura analitica’.
        doClickButton("submitForm('doVisualizzaAnalitica');");

        //Mi ritorna "Scrittura Analitica non presente!" perchè avvenuta in periodo precedente
        //dove l'evasione non ha generato scritture
        
        Assertions.assertEquals("Scrittura Analitica non presente!", handleTextAlert(browser));


        //Mi posiziono sulla consegna 3
        rowElement2.click();

        //Entro su ‘scrittura economica’.
        doClickButton("submitForm('doVisualizzaEconomica');");

        //Seleziono la prima scrittura nata con la prima evasione che è stata annullata
        getTableRowElement("mainTable",0).click();

        Assertions.assertEquals("N", getGrapheneElement("main.attiva").getAttribute("value"));
        Assertions.assertEquals("122,00", getGrapheneElement("main.imTotaleDare").getAttribute("value"));
        Assertions.assertEquals("122,00", getGrapheneElement("main.imTotaleAvere").getAttribute("value"));

        doClickButton("doTab('tab','tabDare')");
        Assertions.assertEquals("C13024", getTableColumnElement("main.MovimentiDare",0,1).getText());
        Assertions.assertEquals("122,00", getTableColumnElement("main.MovimentiDare",0,5).getText());
        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.MovimentiDare",1),"Cannot find Element <tr> with tableName main.MovimentiDare and numberRow: 1");

        doClickButton("doTab('tab','tabAvere')");
        Assertions.assertEquals("P00047", getTableColumnElement("main.MovimentiAvere",0,1).getText());
        Assertions.assertEquals("122,00", getTableColumnElement("main.MovimentiAvere",0,5).getText());
        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.MovimentiAvere",1),"Cannot find Element <tr> with tableName main.MovimentiAvere and numberRow: 1");

        doClickButton("doChiudiForm()");

        //Visualizzo le scritture di economica associate
        doClickButton("submitForm('doVisualizzaEconomica');");

        //Seleziono la seconda scrittura nata in fase di seconda evasione che deve essere valida
        getTableRowElement("mainTable",1).click();

        Assertions.assertEquals("Y", getGrapheneElement("main.attiva").getAttribute("value"));
        Assertions.assertEquals("122,00", getGrapheneElement("main.imTotaleDare").getAttribute("value"));
        Assertions.assertEquals("122,00", getGrapheneElement("main.imTotaleAvere").getAttribute("value"));

        doClickButton("doTab('tab','tabDare')");
        Assertions.assertEquals("C13024", getTableColumnElement("main.MovimentiDare",0,1).getText());
        Assertions.assertEquals("122,00", getTableColumnElement("main.MovimentiDare",0,5).getText());
        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.MovimentiDare",1),"Cannot find Element <tr> with tableName main.MovimentiDare and numberRow: 1");

        doClickButton("doTab('tab','tabAvere')");
        Assertions.assertEquals("P00047", getTableColumnElement("main.MovimentiAvere",0,1).getText());
        Assertions.assertEquals("122,00", getTableColumnElement("main.MovimentiAvere",0,5).getText());
        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.MovimentiAvere",1),"Cannot find Element <tr> with tableName main.MovimentiAvere and numberRow: 1");

        doClickButton("doChiudiForm()");

        doClickButton("submitForm('doVisualizzaEconomica');");
        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("mainTable",2),"Cannot find Element <tr> with tableName 'mainTable' and numberRow: 2");

        doClickButton("doChiudiForm()");

        //Visualizzo le scritture di analitica associate
        doClickButton("submitForm('doVisualizzaAnalitica');");

        //Seleziono la prima scrittura nata con la prima evasione che è stata annullata
        getTableRowElement("mainTable",0).click();

        Assertions.assertEquals("N", getGrapheneElement("main.attiva").getAttribute("value"));
        Assertions.assertEquals("122,00", getGrapheneElement("main.imTotaleMov").getAttribute("value"));

        doClickButton("doTab('tab','tabMovimenti')");
        Assertions.assertEquals("C13024", getTableColumnElement("main.Movimenti",0,1).getText());
        Assertions.assertEquals("Dare", getTableColumnElement("main.Movimenti",0,2).getText());
        Assertions.assertEquals("000.000.000", getTableColumnElement("main.Movimenti",0,3).getText());
        Assertions.assertEquals("PTEST001", getTableColumnElement("main.Movimenti",0,4).getText());
        Assertions.assertEquals("122,00", getTableColumnElement("main.Movimenti",0,5).getText());
        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.Movimenti",1),"Cannot find Element <tr> with tableName main.Movimenti and numberRow: 1");

        doClickButton("doChiudiForm()");

        doClickButton("submitForm('doVisualizzaAnalitica');");

        //Seleziono la seconda scrittura nata in fase di seconda evasione che deve essere valida
        getTableRowElement("mainTable",1).click();

        Assertions.assertEquals("Y", getGrapheneElement("main.attiva").getAttribute("value"));
        Assertions.assertEquals("122,00", getGrapheneElement("main.imTotaleMov").getAttribute("value"));

        doClickButton("doTab('tab','tabMovimenti')");
        Assertions.assertEquals("C13024", getTableColumnElement("main.Movimenti",0,1).getText());
        Assertions.assertEquals("Dare", getTableColumnElement("main.Movimenti",0,2).getText());
        Assertions.assertEquals("000.000.000", getTableColumnElement("main.Movimenti",0,3).getText());
        Assertions.assertEquals("PTEST001", getTableColumnElement("main.Movimenti",0,4).getText());
        Assertions.assertEquals("122,00", getTableColumnElement("main.Movimenti",0,5).getText());
        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.Movimenti",1),"Cannot find Element <tr> with tableName main.Movimenti and numberRow: 1");

        doClickButton("doChiudiForm()");
        doClickButton("submitForm('doVisualizzaMovimento');");

        Assertions.assertEquals("C01", getTableColumnElement("mainTable",0,3).getText());
        Assertions.assertEquals("122", getTableColumnElement("mainTable",0,9).getText());

        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("mainTable",1),"Cannot find Element <tr> with tableName 'mainTable' and numberRow: 1");

        doClickButton("doChiudiForm()");
    }

}
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
 * 1) creazione ordine con 2 righe consegna
 * 2) evasione riga consegna nr.1
 * 3) annullamento evasione riga consegna nr.1
 * 4) nuova evasione riga consegna nr.1
 * 5) evasione riga consegna nr.2
 * 6) registrazione fattura con riscontro valore su riga consegna nr.2 con Fattura di importo superiore all’ordine e dettaglio fatt. in attesa nota credito
 * 7) modifica fattura con annullamento riscontro valore
 * 8) registrazione nota credito di annullamento totale fattura creata
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Order(2)
public class CRUDOrdineAcqBP002 extends ActionDeployments {
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
     * Test Creazione Progetto
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

        //Passo sulla tab ‘consegne’
        doClickButton("doTab('tabOrdineAcqDettagli','tabOrdineConsegna')");

        //Selezione l'unica consegna
        doSelectTableRow("main.Righe.Consegne",0);

        //modifico la quantità della riga consegna (da 2 passo ad 1)
        getGrapheneElement("main.Righe.Consegne.quantita").clear();
        getGrapheneElement("main.Righe.Consegne.quantita").writeIntoElement("1");
        doClickButton("confirmModalInputChange(this,'main.Righe.Consegne.quantita','doOnQuantitaChange')");

        //Creo la seconda consegna
        doClickButton("doAddToCRUD(main.Righe.Consegne)");
        //Selezione la nuova consegna
        doSelectTableRow("main.Righe.Consegne",1);

        //indicando la quantità 1
        getGrapheneElement("main.Righe.Consegne.quantita").writeIntoElement("1");
        doClickButton("confirmModalInputChange(this,'main.Righe.Consegne.quantita','doOnQuantitaChange')");

        //magazzino: PT
        doClickButton("doBlankSearch(main.Righe.Consegne.findMagazzino)");
        getGrapheneElement("main.Righe.Consegne.findMagazzino.cdMagazzino").writeIntoElement(CD_MAGAZZINO);
        doClickButton("doSearch(main.Righe.Consegne.findMagazzino)");

        //Passo sulla tab ‘Dati coge/coan’ e verifico che il conto di costo è quello legato alla categoria gruppo del mio articolo (8.1) ed è: C13003. L’analitica correttamente non c’è (non ho ancora indicato l’impegno).
        doClickButton("doTab('tabOrdineAcqDettagli','tabDatiCogeCoan')");

        //Verifico che il conto di costo è quello legato alla categoria gruppo del mio articolo (8.1) ed è: C13003.
        Assertions.assertEquals("C13003", getGrapheneElement("main.Righe.find_voce_ep.cd_voce_ep").getAttribute("value"));

        // L’analitica correttamente non c’è (non ho ancora indicato l’impegno).
        Assertions.assertEquals("488,00",getGrapheneElement("imCostoEcoDaRipartire").getAttribute("value"));

        //Ritorno sulla prima tab ‘ordine d’acquisto’
        doClickButton("doTab('tab','tabOrdineAcq')");

        //Indico motivo di assenza CIG: SPESE ECONOMALI
        select = new Select(getGrapheneElement("main.motivoAssenzaCig"));
        select.selectByValue("SPESE_ECONOMALI");

        //Salvo
        doClickButton("doSalva()");

        //Restituisce messaggio di creazione eseguita
        Alert alert = browser.switchTo().alert();
        Assertions.assertEquals(AlertMessage.CREAZIONE_ESEGUITA.value(), alert.getText());
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
        Assertions.assertEquals(AlertMessage.SALVATAGGIO_ESEGUITO.value(), alert.getText());
        alert.accept();

        //modifico lo stato portandolo ‘Definitivo’
        select.selectByValue("DEF");

        //Salvo
        doClickButton("doSalva()");

        //Restituisce messaggio di assenza obbligazione
        alert = browser.switchTo().alert();
        Assertions.assertEquals("Sulla consegna 2025/"+CD_NUMERATORE+"/"+pgOrdineCreated+"/1/1 non è indicata l'obbligazione", alert.getText());
        alert.accept();

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

        alert = browser.switchTo().alert();
        Assertions.assertEquals(AlertMessage.SALVATAGGIO_ESEGUITO.value(), alert.getText());
        alert.accept();
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

        //Trovo le mie due righe di consegna dell’ordine creato con test precedente
        GrapheneElement rowElement1 = null;
        if (getTableColumnElement("main.ConsegneDaEvadere", 0, 1).getText().equals("2025/" + CD_NUMERATORE + "/" + pgOrdineCreated + "/1/1"))
            rowElement1 = getTableRowElement("main.ConsegneDaEvadere", 0);
        else if (getTableColumnElement("main.ConsegneDaEvadere", 1, 1).getText().equals("2025/" + CD_NUMERATORE + "/" + pgOrdineCreated + "/1/1"))
            rowElement1 = getTableRowElement("main.ConsegneDaEvadere", 1);
        else
            Assertions.fail("Riga consegna 1 non individuata");

        GrapheneElement rowElement2 = null;
        if (getTableColumnElement("main.ConsegneDaEvadere", 0, 1).getText().equals("2025/" + CD_NUMERATORE + "/" + pgOrdineCreated + "/1/2"))
            rowElement2 = getTableRowElement("main.ConsegneDaEvadere", 0);
        else if (getTableColumnElement("main.ConsegneDaEvadere", 1, 1).getText().equals("2025/" + CD_NUMERATORE + "/" + pgOrdineCreated + "/1/2"))
            rowElement2 = getTableRowElement("main.ConsegneDaEvadere", 1);
        else
            Assertions.fail("Riga consegna 2 non individuata");

        Assertions.assertThrows(RuntimeException.class, () -> getTableRowElement("main.ConsegneDaEvadere", 2),"Cannot find Element <tr> with tableName main.ConsegneDaEvadere and numberRow: 2");

        //Scelgo la prima consegna
        rowElement1.click();
        doSelectTableRow(rowElement1, "main.ConsegneDaEvadere");

        //Salvo
        doClickButton("doSalva()");
        Alert alert = browser.switchTo().alert();
        Assertions.assertEquals(AlertMessage.OPERAZIONE_EFFETTUATA.value(), alert.getText());
        alert.accept();
    }

    @Test
    @Order(4)
    public void testVerificaScritturaOrdine001() {
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

        String pgOrdineCreated = sharedResource.getVal01();
        getGrapheneElement("main.numero").writeIntoElement(pgOrdineCreated);

        doClickButton("doCerca()");
        Alert alert = browser.switchTo().alert();
        Assertions.assertEquals(AlertMessage.MESSAGE_RICERCA_MONO_RECORD.value(), alert.getText());
        alert.accept();

        //Vado sul dettaglio
        doClickButton("doTab('tab','tabOrdineAcqDettaglio')");
        doSelectTableRow("main.Righe",0);

        doClickButton("doTab('tabOrdineAcqDettagli','tabOrdineRigaResultDetailEcoCoge')");

        Assertions.assertEquals("C13003", getGrapheneElement("main.Righe.find_voce_ep.cd_voce_ep").getAttribute("value"));

        doSelectTableRow("main.Righe.Dati Coge/Coan",0);

        Assertions.assertEquals("C13003", getTableColumnElement("main.Righe.Dati Coge/Coan",0,1).getText());
        Assertions.assertEquals("PTEST001", getTableColumnElement("main.Righe.Dati Coge/Coan",0,2).getText());
        Assertions.assertEquals("000.000.000", getTableColumnElement("main.Righe.Dati Coge/Coan",0,3).getText());
        Assertions.assertEquals("488,00", getTableColumnElement("main.Righe.Dati Coge/Coan",0,4).getText());
        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.Righe.Dati Coge/Coan",1),"Cannot find Element <tr> with tableName main.Righe.Dati Coge/Coan and numberRow: 1");

        //Vado sulla tab ‘consegne’
        doClickButton("doTab('tabOrdineAcqDettagli','tabOrdineConsegna')");

        //Vado sulla tab ‘consegne’ e verifico che la riga consegna n. 1 sia in stato ‘EVASA’ e la consegna
        GrapheneElement rowElement1 = null;
        if (getTableColumnElement("main.Righe.Consegne",0, 1).getText().equals("1"))
            rowElement1 = getTableRowElement("main.Righe.Consegne",0);
        else if (getTableColumnElement("main.Righe.Consegne",1, 1).getText().equals("1"))
            rowElement1 = getTableRowElement("main.Righe.Consegne",1);
        else
            Assertions.fail("Riga consegna 1 non individuata");

        GrapheneElement rowElement2 = null;
        if (getTableColumnElement("main.Righe.Consegne",0, 1).getText().equals("2"))
            rowElement2 = getTableRowElement("main.Righe.Consegne",0);
        else if (getTableColumnElement("main.Righe.Consegne",1, 1).getText().equals("2"))
            rowElement2 = getTableRowElement("main.Righe.Consegne",1);
        else
            Assertions.fail("Riga consegna 2 non individuata");

        //Verifico che la riga consegna n. 1 sia in stato ‘EVASA’ e la consegna n. 2 ancora in stato INSERITA. Entrambe ‘Non Associate’.
        Assertions.assertEquals("Evasa", getTableColumnElement(rowElement1,5).getText());
        Assertions.assertEquals("Inserita", getTableColumnElement(rowElement2,5).getText());
        Assertions.assertEquals("Non Associata", getTableColumnElement(rowElement1,6).getText());
        Assertions.assertEquals("Non Associata", getTableColumnElement(rowElement2,6).getText());

        //Mi posiziono sulla prima consegna
        rowElement1.click();

        //Entro su ‘scrittura economica’.
        doClickButton("submitForm('doVisualizzaEconomica');");

        Assertions.assertEquals("Si", getGrapheneElement("main.attiva").getText());
        Assertions.assertEquals("244,00", getGrapheneElement("main.imTotaleDare").getAttribute("value"));
        Assertions.assertEquals("244,00", getGrapheneElement("main.imTotaleAvere").getAttribute("value"));

        //Nella sezione ‘Dare’ è presente correttamente il conto di costo: C13003
        doClickButton("doTab('tab','tabDare')");
        Assertions.assertEquals("C13003", getTableColumnElement("main.MovimentiDare",0,1).getText());
        Assertions.assertEquals("244,00", getTableColumnElement("main.MovimentiDare",0,5).getText());
        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.MovimentiDare",1),"Cannot find Element <tr> with tableName main.MovimentiDare and numberRow: 1");

        //Nella sezione ‘Avere’ è presente correttamente il conto fatture da ricevere: P00047.
        doClickButton("doTab('tab','tabAvere')");
        Assertions.assertEquals("P00047", getTableColumnElement("main.MovimentiAvere",0,1).getText());
        Assertions.assertEquals("244,00", getTableColumnElement("main.MovimentiAvere",0,5).getText());
        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.MovimentiAvere",1),"Cannot find Element <tr> with tableName main.MovimentiAvere and numberRow: 1");

        doClickButton("doChiudiForm()");
        doClickButton("submitForm('doVisualizzaAnalitica');");

        Assertions.assertEquals("Y", getGrapheneElement("main.attiva").getAttribute("value"));
        Assertions.assertEquals("244,00", getGrapheneElement("main.imTotaleMov").getAttribute("value"));

        doClickButton("doTab('tab','tabMovimenti')");
        Assertions.assertEquals("C13003", getTableColumnElement("main.Movimenti",0,1).getText());
        Assertions.assertEquals("Dare", getTableColumnElement("main.Movimenti",0,2).getText());
        Assertions.assertEquals("000.000.000", getTableColumnElement("main.Movimenti",0,3).getText());
        Assertions.assertEquals("PTEST001", getTableColumnElement("main.Movimenti",0,4).getText());
        Assertions.assertEquals("244,00", getTableColumnElement("main.Movimenti",0,5).getText());
        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.Movimenti",1),"Cannot find Element <tr> with tableName main.Movimenti and numberRow: 1");

        doClickButton("doChiudiForm()");
    }

    /**
     * Test Evasione Consegne di Progetto creato con test precedente
     */
    @Test
    @Order(5)
    public void testAnnullaEvasioneConsegna() {
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
                        return getTableColumnElement(rowElement, 17).getText().contains("2025") &&
                                CD_NUMERATORE.equals(getTableColumnElement(rowElement, 18).getText()) &&
                                pgOrdineCreated.equals(getTableColumnElement(rowElement, 19).getText()) &&
                                "1".equals(getTableColumnElement(rowElement, 20).getText()) &&
                                "1".equals(getTableColumnElement(rowElement, 21).getText());
                    } catch (java.lang.RuntimeException ex) {
                        return false;
                    }
                })
                .findAny();

        Assertions.assertTrue(element.isPresent());

        element.get().findElement(By.name("mainTable.selection")).click();

        //Digito il pulsante ‘Annulla Movimenti Selezionati’.
        doClickButton("submitForm('doAnnullaMovimenti');");

        //Chiudo la form (2 volte perchè sono 2 BP aperti)
        doClickButton("doChiudiForm()");
        doClickButton("doChiudiForm()");
    }

    @Test
    @Order(6)
    public void testVerificaScritturaOrdine002() {
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

        String pgOrdineCreated = sharedResource.getVal01();
        getGrapheneElement("main.numero").writeIntoElement(pgOrdineCreated);

        doClickButton("doCerca()");
        Alert alert = browser.switchTo().alert();
        Assertions.assertEquals(AlertMessage.MESSAGE_RICERCA_MONO_RECORD.value(), alert.getText());
        alert.accept();

        //Vado sul dettaglio
        doClickButton("doTab('tab','tabOrdineAcqDettaglio')");
        doSelectTableRow("main.Righe",0);

        //Vado sulla tab ‘consegne’
        doClickButton("doTab('tabOrdineAcqDettagli','tabOrdineConsegna')");

        //Vado sulla tab ‘consegne’
        GrapheneElement rowElement1=null;
        if (getTableColumnElement("main.Righe.Consegne",0, 1).getText().equals("1"))
            rowElement1 = getTableRowElement("main.Righe.Consegne",0);
        else if (getTableColumnElement("main.Righe.Consegne",1, 1).getText().equals("1"))
            rowElement1 = getTableRowElement("main.Righe.Consegne",1);
        else
            Assertions.fail("Riga consegna 1 non individuata");

        GrapheneElement rowElement2=null;
        if (getTableColumnElement("main.Righe.Consegne",0, 1).getText().equals("2"))
            rowElement2 = getTableRowElement("main.Righe.Consegne",0);
        else if (getTableColumnElement("main.Righe.Consegne",1, 1).getText().equals("2"))
            rowElement2 = getTableRowElement("main.Righe.Consegne",1);
        else
            Assertions.fail("Riga consegna 2 non individuata");

        //Verifico che la riga consegna n. 1 sia ritornata in stato ‘INSERITA’ e la consegna n. 2 sia ancora in stato INSERITA. Entrambe ‘Non Associate’.
        Assertions.assertEquals("Inserita", getTableColumnElement(rowElement1,5).getText());
        Assertions.assertEquals("Inserita", getTableColumnElement(rowElement2,5).getText());
        Assertions.assertEquals("Non Associata", getTableColumnElement(rowElement1,6).getText());
        Assertions.assertEquals("Non Associata", getTableColumnElement(rowElement2,6).getText());

        doClickButton("doChiudiForm()");
    }

    @Test
    @Order(7)
    public void testEvasioneConsegna002() {
        //Rifaccio l’evasione come al testEvasioneConsegna001
        switchToFrameMenu();
        doSelezionaMenu(ORD_EVAORD);

        switchToFrameWorkspace();

        //Scelgo Unità operativa: DRUE
        doClickButton("doBlankSearch(main.findUnitaOperativaOrd)");
        getGrapheneElement("main.findUnitaOperativaOrd.cdUnitaOperativa").writeIntoElement(CD_UNITA_OPERATIVA);
        doClickButton("doSearch(main.findUnitaOperativaOrd)");

        //Scelgo Magazzino: PT
        doClickButton("doBlankSearch(main.findMagazzino)");
        getGrapheneElement("main.findMagazzino.cdMagazzino").writeIntoElement(CD_MAGAZZINO);
        doClickButton("doSearch(main.findMagazzino)");

        //Data Bolla: Oggi
        getGrapheneElement("main.dataBolla").writeIntoElement(DATA_ODIERNA);
        doClickButton("doOnDtBollaChange");

        //Numero Bolla: 8
        getGrapheneElement("main.numeroBolla").writeIntoElement("8");

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
            Assertions.fail("Riga consegna 1 non individuata");

        GrapheneElement rowElement2 = null;
        if (getTableColumnElement("main.ConsegneDaEvadere", 0, 1).getText().equals("2025/" + CD_NUMERATORE + "/" + pgOrdineCreated + "/1/2"))
            rowElement2 = getTableRowElement("main.ConsegneDaEvadere", 0);
        else if (getTableColumnElement("main.ConsegneDaEvadere", 1, 1).getText().equals("2025/" + CD_NUMERATORE + "/" + pgOrdineCreated + "/1/2"))
            rowElement2 = getTableRowElement("main.ConsegneDaEvadere", 1);
        else
            Assertions.fail("Riga consegna 2 non individuata");

        Assertions.assertThrows(RuntimeException.class, () -> getTableRowElement("main.ConsegneDaEvadere", 2),"Cannot find Element <tr> with tableName main.ConsegneDaEvadere and numberRow: 2");

        //Scelgo la prima consegna
        rowElement1.click();
        doSelectTableRow(rowElement1, "main.ConsegneDaEvadere");

        //Salvo
        doClickButton("doSalva()");
        Alert alert = browser.switchTo().alert();
        Assertions.assertEquals(AlertMessage.OPERAZIONE_EFFETTUATA.value(), alert.getText());
        alert.accept();

        doClickButton("doChiudiForm()");
    }

    @Test
    @Order(8)
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

        String pgOrdineCreated = sharedResource.getVal01();
        getGrapheneElement("main.numero").writeIntoElement(pgOrdineCreated);

        doClickButton("doCerca()");
        Alert alert = browser.switchTo().alert();
        Assertions.assertEquals(AlertMessage.MESSAGE_RICERCA_MONO_RECORD.value(), alert.getText());
        alert.accept();

        //Vado sul dettaglio
        doClickButton("doTab('tab','tabOrdineAcqDettaglio')");
        doSelectTableRow("main.Righe",0);

        doClickButton("doTab('tabOrdineAcqDettagli','tabOrdineRigaResultDetailEcoCoge')");

        Assertions.assertEquals("C13003", getGrapheneElement("main.Righe.find_voce_ep.cd_voce_ep").getAttribute("value"));

        doSelectTableRow("main.Righe.Dati Coge/Coan",0);

        Assertions.assertEquals("C13003", getTableColumnElement("main.Righe.Dati Coge/Coan",0,1).getText());
        Assertions.assertEquals("PTEST001", getTableColumnElement("main.Righe.Dati Coge/Coan",0,2).getText());
        Assertions.assertEquals("000.000.000", getTableColumnElement("main.Righe.Dati Coge/Coan",0,3).getText());
        Assertions.assertEquals("488,00", getTableColumnElement("main.Righe.Dati Coge/Coan",0,4).getText());
        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.Righe.Dati Coge/Coan",1),"Cannot find Element <tr> with tableName main.Righe.Dati Coge/Coan and numberRow: 1");

        //Vado sulla tab ‘consegne’
        doClickButton("doTab('tabOrdineAcqDettagli','tabOrdineConsegna')");

        //Vado sulla tab ‘consegne’ e verifico che la riga consegna n. 1 sia in stato ‘EVASA’ e la consegna
        GrapheneElement rowElement1 = null;
        if (getTableColumnElement("main.Righe.Consegne",0, 1).getText().equals("1"))
            rowElement1 = getTableRowElement("main.Righe.Consegne",0);
        else if (getTableColumnElement("main.Righe.Consegne",1, 1).getText().equals("1"))
            rowElement1 = getTableRowElement("main.Righe.Consegne",1);
        else
            Assertions.fail("Riga consegna 1 non individuata");

        GrapheneElement rowElement2 = null;
        if (getTableColumnElement("main.Righe.Consegne",0, 1).getText().equals("2"))
            rowElement2 = getTableRowElement("main.Righe.Consegne",0);
        else if (getTableColumnElement("main.Righe.Consegne",1, 1).getText().equals("2"))
            rowElement2 = getTableRowElement("main.Righe.Consegne",1);
        else
            Assertions.fail("Riga consegna 2 non individuata");

        //Verifico che la riga consegna n. 1 sia in stato ‘EVASA’ e la consegna n. 2 ancora in stato INSERITA. Entrambe ‘Non Associate’.
        Assertions.assertEquals("Evasa", getTableColumnElement(rowElement1,5).getText());
        Assertions.assertEquals("Inserita", getTableColumnElement(rowElement2,5).getText());
        Assertions.assertEquals("Non Associata", getTableColumnElement(rowElement1,6).getText());
        Assertions.assertEquals("Non Associata", getTableColumnElement(rowElement2,6).getText());

        //Mi posiziono sulla prima consegna
        rowElement1.click();

        //Entro su ‘scrittura economica’.
        doClickButton("submitForm('doVisualizzaEconomica');");

        //Seleziono la prima scrittura che deve essere annullata dopo l'annullamento dell'evasione
        getTableRowElement("mainTable",0).click();
        Assertions.assertEquals("No", getGrapheneElement("main.attiva").getText());

        doClickButton("doChiudiForm()");

        //Visualizzo le scritture di economica associate
        doClickButton("submitForm('doVisualizzaEconomica');");

        //Seleziono la seconda scrittura nata con la successiva evasione
        getTableRowElement("mainTable",1).click();

        Assertions.assertEquals("Si", getGrapheneElement("main.attiva").getText());
        Assertions.assertEquals("244,00", getGrapheneElement("main.imTotaleDare").getAttribute("value"));
        Assertions.assertEquals("244,00", getGrapheneElement("main.imTotaleAvere").getAttribute("value"));

        //Nella sezione ‘Dare’ è presente correttamente il conto di costo: C13003
        doClickButton("doTab('tab','tabDare')");
        Assertions.assertEquals("C13003", getTableColumnElement("main.MovimentiDare",0,1).getText());
        Assertions.assertEquals("244,00", getTableColumnElement("main.MovimentiDare",0,5).getText());
        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.MovimentiDare",1),"Cannot find Element <tr> with tableName main.MovimentiDare and numberRow: 1");

        //Nella sezione ‘Avere’ è presente correttamente il conto fatture da ricevere: P00047.
        doClickButton("doTab('tab','tabAvere')");
        Assertions.assertEquals("P00047", getTableColumnElement("main.MovimentiAvere",0,1).getText());
        Assertions.assertEquals("244,00", getTableColumnElement("main.MovimentiAvere",0,5).getText());
        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.MovimentiAvere",1),"Cannot find Element <tr> with tableName main.MovimentiAvere and numberRow: 1");

        doClickButton("doChiudiForm()");

        //Entro su ‘scrittura analitica’.
        doClickButton("submitForm('doVisualizzaAnalitica');");

        //Seleziono la prima scrittura che deve essere annullata dopo l'annullamento dell'evasione
        getTableRowElement("mainTable",0).click();
        Assertions.assertEquals("N", getGrapheneElement("main.attiva").getAttribute("value"));

        doClickButton("doChiudiForm()");

        //Visualizzo le scritture di analitica associate
        doClickButton("submitForm('doVisualizzaAnalitica');");

        //Seleziono la seconda scrittura nata con la successiva evasione
        getTableRowElement("mainTable",1).click();

        Assertions.assertEquals("Y", getGrapheneElement("main.attiva").getAttribute("value"));
        Assertions.assertEquals("244,00", getGrapheneElement("main.imTotaleMov").getAttribute("value"));

        doClickButton("doTab('tab','tabMovimenti')");
        Assertions.assertEquals("C13003", getTableColumnElement("main.Movimenti",0,1).getText());
        Assertions.assertEquals("Dare", getTableColumnElement("main.Movimenti",0,2).getText());
        Assertions.assertEquals("000.000.000", getTableColumnElement("main.Movimenti",0,3).getText());
        Assertions.assertEquals("PTEST001", getTableColumnElement("main.Movimenti",0,4).getText());
        Assertions.assertEquals("244,00", getTableColumnElement("main.Movimenti",0,5).getText());
        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.Movimenti",1),"Cannot find Element <tr> with tableName main.Movimenti and numberRow: 1");

        doClickButton("doChiudiForm()");
    }

    @Test
    @Order(9)
    public void testControlloDatiEvasioneConsegna001() {
        //Rifaccio l’evasione come al testEvasioneConsegna001
        switchToFrameMenu();
        doApriMenu(ORD_CON);
        doSelezionaMenu(ORD_CON_VISORDCONSEGNA);

        switchToFrameWorkspace();

        //Scelgo Unità operativa: DRUE
        doClickButton("doBlankSearch(main.findUnitaOperativaOrd)");
        getGrapheneElement("main.findUnitaOperativaOrd.cdUnitaOperativa").writeIntoElement(CD_UNITA_OPERATIVA);
        doClickButton("doSearch(main.findUnitaOperativaOrd)");

        //Sul campo numeratore indico: DSA
        doClickButton("doBlankSearch(main.findNumerazioneOrd)");
        getGrapheneElement("main.findNumerazioneOrd.cdNumeratore").writeIntoElement(CD_NUMERATORE);
        doClickButton("doSearch(main.findNumerazioneOrd)");

        String pgOrdineCreated = sharedResource.getVal01();

        //Indico da numero ordine a numero ordine: il numero ordine creato
        getGrapheneElement("main.daNumeroOrdine").writeIntoElement(pgOrdineCreated);
        getGrapheneElement("main.aNumeroOrdine").writeIntoElement(pgOrdineCreated);

        //Eseguo la ricerca e vedo le mie due consegne
        doClickButton("doCerca");

        //Trovo le mie due righe di consegna dell’ordine creato con test precedente
        GrapheneElement rowElement1=null;
        if (getTableColumnElement("mainTable",0, 4).getText().equals("1"))
            rowElement1 = getTableRowElement("mainTable",0);
        else if (getTableColumnElement("mainTable",1, 4).getText().equals("1"))
            rowElement1 = getTableRowElement("mainTable",1);
        else
            Assertions.fail("Riga consegna 1 non individuata");

        GrapheneElement rowElement2=null;
        if (getTableColumnElement("mainTable",0, 4).getText().equals("2"))
            rowElement2 = getTableRowElement("mainTable",0);
        else if (getTableColumnElement("mainTable",1, 4).getText().equals("2"))
            rowElement2 = getTableRowElement("mainTable",1);
        else
            Assertions.fail("Riga consegna 2 non individuata");

        //Verifico che la riga consegna n. 1 sia in stato ‘EVASA’ e la consegna n. 2 ancora in stato INSERITA. Entrambe ‘Non Associate’.
        Assertions.assertEquals("Evasa", getTableColumnElement(rowElement1,14).getText());
        Assertions.assertEquals("Inserita", getTableColumnElement(rowElement2,14).getText());
        Assertions.assertEquals("Non Associata", getTableColumnElement(rowElement1,15).getText());
        Assertions.assertEquals("Non Associata", getTableColumnElement(rowElement2,15).getText());

        //Seleziono la consegna 1 EVASA
        rowElement1.click();

        //Visualizzo le righe di evasione
        doClickButton("submitForm('doVisualizzaEvasione')");

        //Cerco quella creata con bolla 7
        if (getTableColumnElement("mainTable",0, 1).getText().equals("7"))
            rowElement1 = getTableRowElement("mainTable",0);
        else if (getTableColumnElement("mainTable",1, 1).getText().equals("7"))
            rowElement1 = getTableRowElement("mainTable",1);
        else
            Assertions.fail("Riga evasione con numero bolla 7 non individuata");

        //Cerco quella creata con bolla 8
        if (getTableColumnElement("mainTable",0, 1).getText().equals("8"))
            rowElement2 = getTableRowElement("mainTable",0);
        else if (getTableColumnElement("mainTable",1, 1).getText().equals("8"))
            rowElement2 = getTableRowElement("mainTable",1);
        else
            Assertions.fail("Riga evasione con numero bolla 8 non individuata");

        //Verifico che la riga di evasione con riga 7 sia annullata e quella con riga 8 Inserita
        Assertions.assertEquals("Annullata", getTableColumnElement(rowElement1,2).getText());
        Assertions.assertEquals("Inserita", getTableColumnElement(rowElement2,2).getText());

        doClickButton("doChiudiForm()");
        doClickButton("doChiudiForm()");
        doClickButton("doChiudiForm()");
    }

    @Test
    @Order(10)
    public void testEvasioneConsegna003() {
        //Rifaccio l’evasione come al testEvasioneConsegna001
        switchToFrameMenu();
        doSelezionaMenu(ORD_EVAORD);

        switchToFrameWorkspace();

        //Scelgo Unità operativa: DRUE
        doClickButton("doBlankSearch(main.findUnitaOperativaOrd)");
        getGrapheneElement("main.findUnitaOperativaOrd.cdUnitaOperativa").writeIntoElement(CD_UNITA_OPERATIVA);
        doClickButton("doSearch(main.findUnitaOperativaOrd)");

        //Scelgo Magazzino: PT
        doClickButton("doBlankSearch(main.findMagazzino)");
        getGrapheneElement("main.findMagazzino.cdMagazzino").writeIntoElement(CD_MAGAZZINO);
        doClickButton("doSearch(main.findMagazzino)");

        //Data Bolla: Oggi
        getGrapheneElement("main.dataBolla").writeIntoElement(DATA_ODIERNA);
        doClickButton("doOnDtBollaChange");

        //Numero Bolla: 8
        getGrapheneElement("main.numeroBolla").writeIntoElement("8");

        //Data Consegna: Oggi
        getGrapheneElement("main.dataConsegna").writeIntoElement(DATA_ODIERNA);
        doClickButton("doOnDtConsegnaChange");

        //Eseguo la ricerca
        doClickButton("doCercaConsegneDaEvadere");

        String pgOrdineCreated = sharedResource.getVal01();

        //Trovo la riga di consegna 2 da evadere dell’ordine creato con test precedente
        GrapheneElement rowElement1 = null;
        if (getTableColumnElement("main.ConsegneDaEvadere", 0, 1).getText().equals("2025/" + CD_NUMERATORE + "/" + pgOrdineCreated + "/1/2"))
            rowElement1 = getTableRowElement("main.ConsegneDaEvadere", 0);
        else
            Assertions.fail("Riga consegna 2 non individuata");

        Assertions.assertThrows(RuntimeException.class, () -> getTableRowElement("main.ConsegneDaEvadere", 1),"Cannot find Element <tr> with tableName main.ConsegneDaEvadere and numberRow: 1");

        //Seleziono la consegna
        rowElement1.click();
        doSelectTableRow(rowElement1, "main.ConsegneDaEvadere");

        //Salvo
        doClickButton("doSalva()");
        Alert alert = browser.switchTo().alert();
        Assertions.assertEquals(AlertMessage.OPERAZIONE_EFFETTUATA.value(), alert.getText());
        alert.accept();

        doClickButton("doChiudiForm()");
    }

    @Test
    @Order(11)
    public void testVerificaScritturaOrdine004() {
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

        String pgOrdineCreated = sharedResource.getVal01();
        getGrapheneElement("main.numero").writeIntoElement(pgOrdineCreated);

        doClickButton("doCerca()");
        Alert alert = browser.switchTo().alert();
        Assertions.assertEquals(AlertMessage.MESSAGE_RICERCA_MONO_RECORD.value(), alert.getText());
        alert.accept();

        //Vado sul dettaglio
        doClickButton("doTab('tab','tabOrdineAcqDettaglio')");
        doSelectTableRow("main.Righe",0);

        doClickButton("doTab('tabOrdineAcqDettagli','tabOrdineRigaResultDetailEcoCoge')");

        Assertions.assertEquals("C13003", getGrapheneElement("main.Righe.find_voce_ep.cd_voce_ep").getAttribute("value"));

        doSelectTableRow("main.Righe.Dati Coge/Coan",0);

        Assertions.assertEquals("C13003", getTableColumnElement("main.Righe.Dati Coge/Coan",0,1).getText());
        Assertions.assertEquals("PTEST001", getTableColumnElement("main.Righe.Dati Coge/Coan",0,2).getText());
        Assertions.assertEquals("000.000.000", getTableColumnElement("main.Righe.Dati Coge/Coan",0,3).getText());
        Assertions.assertEquals("488,00", getTableColumnElement("main.Righe.Dati Coge/Coan",0,4).getText());
        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.Righe.Dati Coge/Coan",1),"Cannot find Element <tr> with tableName main.Righe.Dati Coge/Coan and numberRow: 1");

        //Vado sulla tab ‘consegne’
        doClickButton("doTab('tabOrdineAcqDettagli','tabOrdineConsegna')");

        //Vado sulla tab ‘consegne’ e verifico che la riga consegna n. 1 sia in stato ‘EVASA’ e la consegna
        GrapheneElement rowElement1 = null;
        if (getTableColumnElement("main.Righe.Consegne",0, 1).getText().equals("1"))
            rowElement1 = getTableRowElement("main.Righe.Consegne",0);
        else if (getTableColumnElement("main.Righe.Consegne",1, 1).getText().equals("1"))
            rowElement1 = getTableRowElement("main.Righe.Consegne",1);
        else
            Assertions.fail("Riga consegna 1 non individuata");

        GrapheneElement rowElement2 = null;
        if (getTableColumnElement("main.Righe.Consegne",0, 1).getText().equals("2"))
            rowElement2 = getTableRowElement("main.Righe.Consegne",0);
        else if (getTableColumnElement("main.Righe.Consegne",1, 1).getText().equals("2"))
            rowElement2 = getTableRowElement("main.Righe.Consegne",1);
        else
            Assertions.fail("Riga consegna 2 non individuata");

        //Verifico che la riga consegna n. 1 sia in stato ‘EVASA’ e la consegna n. 2 ancora in stato INSERITA. Entrambe ‘Non Associate’.
        Assertions.assertEquals("Evasa", getTableColumnElement(rowElement1,5).getText());
        Assertions.assertEquals("Evasa", getTableColumnElement(rowElement2,5).getText());
        Assertions.assertEquals("Non Associata", getTableColumnElement(rowElement1,6).getText());
        Assertions.assertEquals("Non Associata", getTableColumnElement(rowElement2,6).getText());

        //Mi posiziono sulla seconda consegna
        rowElement2.click();

        //Entro su ‘scrittura economica’.
        doClickButton("submitForm('doVisualizzaEconomica');");

        Assertions.assertEquals("Si", getGrapheneElement("main.attiva").getText());
        Assertions.assertEquals("244,00", getGrapheneElement("main.imTotaleDare").getAttribute("value"));
        Assertions.assertEquals("244,00", getGrapheneElement("main.imTotaleAvere").getAttribute("value"));

        //Nella sezione ‘Dare’ è presente correttamente il conto di costo: C13003
        doClickButton("doTab('tab','tabDare')");
        Assertions.assertEquals("C13003", getTableColumnElement("main.MovimentiDare",0,1).getText());
        Assertions.assertEquals("244,00", getTableColumnElement("main.MovimentiDare",0,5).getText());
        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.MovimentiDare",1),"Cannot find Element <tr> with tableName main.MovimentiDare and numberRow: 1");

        //Nella sezione ‘Avere’ è presente correttamente il conto fatture da ricevere: P00047.
        doClickButton("doTab('tab','tabAvere')");
        Assertions.assertEquals("P00047", getTableColumnElement("main.MovimentiAvere",0,1).getText());
        Assertions.assertEquals("244,00", getTableColumnElement("main.MovimentiAvere",0,5).getText());
        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.MovimentiAvere",1),"Cannot find Element <tr> with tableName main.MovimentiAvere and numberRow: 1");

        doClickButton("doChiudiForm()");
        doClickButton("submitForm('doVisualizzaAnalitica');");

        Assertions.assertEquals("Y", getGrapheneElement("main.attiva").getAttribute("value"));
        Assertions.assertEquals("244,00", getGrapheneElement("main.imTotaleMov").getAttribute("value"));

        doClickButton("doTab('tab','tabMovimenti')");
        Assertions.assertEquals("C13003", getTableColumnElement("main.Movimenti",0,1).getText());
        Assertions.assertEquals("Dare", getTableColumnElement("main.Movimenti",0,2).getText());
        Assertions.assertEquals("000.000.000", getTableColumnElement("main.Movimenti",0,3).getText());
        Assertions.assertEquals("PTEST001", getTableColumnElement("main.Movimenti",0,4).getText());
        Assertions.assertEquals("244,00", getTableColumnElement("main.Movimenti",0,5).getText());
        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.Movimenti",1),"Cannot find Element <tr> with tableName main.Movimenti and numberRow: 1");

        doClickButton("doChiudiForm()");
    }

    @Test
    @Order(12)
    public void testRiscontroValore001() {
        switchToFrameMenu();
        doApriMenu(AMM);
        doApriMenu(AMM_FATTUR);
        doApriMenu(AMM_FATTUR_FATPAS);
        doSelezionaMenu(AMM_FATTUR_FATPAS_ELE);

        switchToFrameWorkspace();

        //Ricerco la fattura SDI: 90000000002
        getGrapheneElement("main.identificativoSdi").writeIntoElement("90000000002");

        Select select = new Select(getGrapheneElement("main.statoDocumento"));
        select.selectByValue("");

        doClickButton("doCerca()");
        Alert alert = browser.switchTo().alert();
        Assertions.assertEquals(AlertMessage.MESSAGE_RICERCA_MONO_RECORD.value(), alert.getText());
        alert.accept();

        //Clicco sul pulsante ‘Compila fattura’;
        doClickButton("submitForm('doCompilaFattura')");

        //Passo alla maschera di Registrazione Fattura
        getGrapheneElement("comando.doYes").click();

        //Sulla testata fattura indico lo stato ‘Non liquidabile
        select = new Select(getGrapheneElement("main.stato_liquidazione"));
        select.selectByValue("NOLIQ");

        //Sulla testata fattura indico la Causale: In attesa di nota credito, indico il motivo di assenza CIG
        select = new Select(getGrapheneElement("main.causale"));
        select.selectByValue("ATTNC");

        //Verifico che fattura da Ordini=Si
        Assertions.assertTrue(getGrapheneElement("main.flDaOrdini").isSelected());

        getGrapheneElement("main.ds_fattura_passiva").writeIntoElement("RISCONTRO VALORE TEST");

        //Passo alla tab ‘ordini’
        doClickButton("doTab('tab','tabFatturaPassivaOrdini')");

        //Effettuo la ricerca
        doClickButton("submitForm('doSelezionaOrdini')");

        String pgOrdineCreated = sharedResource.getVal01();

        //Seleziono la consegna n. 2
        Optional<GrapheneElement> element = browser.findElements(By.tagName("tr"))
                .stream()
                .filter(GrapheneElement.class::isInstance)
                .map(GrapheneElement.class::cast)
                .filter(rowElement -> {
                    try {
                        return getTableColumnElement(rowElement, 3).getText().contains("2025") &&
                                CD_NUMERATORE.equals(getTableColumnElement(rowElement, 4).getText()) &&
                                pgOrdineCreated.equals(getTableColumnElement(rowElement, 5).getText()) &&
                                "1".equals(getTableColumnElement(rowElement, 6).getText()) &&
                                "2".equals(getTableColumnElement(rowElement, 7).getText());
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
                                pgOrdineCreated.equals(getTableColumnElement(rowElement, 5).getText()) &&
                                "1".equals(getTableColumnElement(rowElement, 6).getText()) &&
                                "1".equals(getTableColumnElement(rowElement, 7).getText());
                    } catch (java.lang.RuntimeException ex) {
                        return false;
                    }
                }));

        doClickButton("submitForm('doMultipleSelection')");

        //Ritorno sulla tab ordini, vedo la squadratura di 5 euro della fattura (in più) rispetto all’ordine;
        doSelectTableRow("main.Ordini",0);

        //Inserisco nella sezione ‘Rettifiche’ nel campo ‘Imponibile per nota credito’ l’importo: 205,00;
        getGrapheneElement("main.Ordini.imponibileErrato").writeIntoElement("205");
        doClickButton("confirmModalInputChange(this,'main.Ordini.imponibileErrato','doRettificaConsegna')");

        //Digito ‘Fine riscontro a valore’ e mi sposto sulla tab ‘dettaglio’ dove mi è stato creato il dettaglio pari alla riga ordine (200 imponibile + 44 IVA).
        doClickButton("submitForm('doConfermaRiscontroAValore')");

        alert = browser.switchTo().alert();
        Assertions.assertEquals("Attenzione ci sono dettagli di fattura da contabilizzare.", alert.getText());
        alert.accept();

        doSelectTableRow("main.Dettaglio",1);

        getGrapheneElement("main.Dettaglio.im_iva").clear();
        getGrapheneElement("main.Dettaglio.im_iva").writeIntoElement("0");
        doClickButton("confirmModalInputChange(this,'main.Dettaglio.im_iva','doForzaIVA')");

        doClickButton("doSalva()");

        alert = browser.switchTo().alert();
        Assertions.assertEquals("Inserire il CIG o il motivo di assenza dello stesso!", alert.getText());
        alert.accept();

        //Vado sulla tab obbligazioni
        doClickButton("doTab('tab','tabFatturaPassivaObbligazioni')");

        //Seleziono l'unica obbligazione presente
        doSelectTableRow("main.Obbligazioni",0);

        //Indico il motivo di assenza CIG
        select = new Select(getGrapheneElement("main.Obbligazioni.motivo_assenza_cig"));
        select.selectByValue("IMPRESA_COLLEGATA");

        doClickButton("doSalva()");

        alert = browser.switchTo().alert();
        Assertions.assertEquals(AlertMessage.CREAZIONE_ESEGUITA.value(), alert.getText());
        alert.accept();

        //Vado sulla tab principale
        doClickButton("doTab('tab','tabFatturaPassiva')");

        //Registro il numero fattura creata
        String pgFatturaCreated = getGrapheneElement("main.pg_fattura_passiva").getAttribute("value");
        sharedResource.setVal02(pgFatturaCreated);

        //Vado sulla tab economica per controllare scrittura
        doClickButton("doTab('tab','tabEconomica')");
        Assertions.assertEquals("P00047", getTableColumnElement("main.Movimenti Dare",0,1).getText());
        Assertions.assertEquals("244,00", getTableColumnElement("main.Movimenti Dare",0,5).getText());

        Assertions.assertEquals("C13003", getTableColumnElement("main.Movimenti Dare",1,1).getText());
        Assertions.assertEquals("5,00", getTableColumnElement("main.Movimenti Dare",1,5).getText());

        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.Movimenti Dare",2),"Cannot find Element <tr> with tableName 'main.Movimenti Dare' and numberRow: 2");

        Assertions.assertEquals("P13003", getTableColumnElement("main.Movimenti Avere",0,1).getText());
        Assertions.assertEquals("205,00", getTableColumnElement("main.Movimenti Avere",0,5).getText());

        Assertions.assertEquals("P71012I", getTableColumnElement("main.Movimenti Avere",1,1).getText());
        Assertions.assertEquals("44,00", getTableColumnElement("main.Movimenti Avere",1,5).getText());

        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.Movimenti Avere",2),"Cannot find Element <tr> with tableName 'main.Movimenti Avere' and numberRow: 2");

        //Vado sulla tab analitica per controllare scrittura analitica per maggior costo rilevato rispetto ordine
        doClickButton("doTab('tab','tabAnalitica')");
        Assertions.assertEquals("C13003", getTableColumnElement("main.Movimenti Analitici",0,1).getText());
        Assertions.assertEquals("Dare", getTableColumnElement("main.Movimenti Analitici",0,2).getText());
        Assertions.assertEquals("C13003", getTableColumnElement("main.Movimenti Analitici",0,3).getText());
        Assertions.assertEquals("000.000.000", getTableColumnElement("main.Movimenti Analitici",0,5).getText());
        Assertions.assertEquals("PTEST003", getTableColumnElement("main.Movimenti Analitici",0,6).getText());
        Assertions.assertEquals("D", getTableColumnElement("main.Movimenti Analitici",0,7).getText());
        Assertions.assertEquals("5,00", getTableColumnElement("main.Movimenti Analitici",0,8).getText());

        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.Movimenti Analitici",1),"Cannot find Element <tr> with tableName 'main.Movimenti Analitici' and numberRow: 1");

        doClickButton("doChiudiForm()");
    }

    @Test
    @Order(13)
    public void testVerificaScritturaOrdine005() {
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

        String pgOrdineCreated = sharedResource.getVal01();
        getGrapheneElement("main.numero").writeIntoElement(pgOrdineCreated);

        doClickButton("doCerca()");
        Alert alert = browser.switchTo().alert();
        Assertions.assertEquals(AlertMessage.MESSAGE_RICERCA_MONO_RECORD.value(), alert.getText());
        alert.accept();

        //Vado sul dettaglio
        doClickButton("doTab('tab','tabOrdineAcqDettaglio')");
        doSelectTableRow("main.Righe",0);

        doClickButton("doTab('tabOrdineAcqDettagli','tabOrdineRigaResultDetailEcoCoge')");

        Assertions.assertEquals("C13003", getGrapheneElement("main.Righe.find_voce_ep.cd_voce_ep").getAttribute("value"));

        doSelectTableRow("main.Righe.Dati Coge/Coan",0);

        Assertions.assertEquals("C13003", getTableColumnElement("main.Righe.Dati Coge/Coan",0,1).getText());
        Assertions.assertEquals("PTEST001", getTableColumnElement("main.Righe.Dati Coge/Coan",0,2).getText());
        Assertions.assertEquals("000.000.000", getTableColumnElement("main.Righe.Dati Coge/Coan",0,3).getText());
        Assertions.assertEquals("488,00", getTableColumnElement("main.Righe.Dati Coge/Coan",0,4).getText());
        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.Righe.Dati Coge/Coan",1),"Cannot find Element <tr> with tableName main.Righe.Dati Coge/Coan and numberRow: 1");

        //Vado sulla tab ‘consegne’
        doClickButton("doTab('tabOrdineAcqDettagli','tabOrdineConsegna')");

        //Vado sulla tab ‘consegne’ e individuo la riga consegna n. 1
        GrapheneElement rowElement1 = null;
        if (getTableColumnElement("main.Righe.Consegne",0, 1).getText().equals("1"))
            rowElement1 = getTableRowElement("main.Righe.Consegne",0);
        else if (getTableColumnElement("main.Righe.Consegne",1, 1).getText().equals("1"))
            rowElement1 = getTableRowElement("main.Righe.Consegne",1);
        else
            Assertions.fail("Riga consegna 1 non individuata");

        //Vado sulla tab ‘consegne’ e individuo la riga consegna n. 2
        GrapheneElement rowElement2 = null;
        if (getTableColumnElement("main.Righe.Consegne",0, 1).getText().equals("2"))
            rowElement2 = getTableRowElement("main.Righe.Consegne",0);
        else if (getTableColumnElement("main.Righe.Consegne",1, 1).getText().equals("2"))
            rowElement2 = getTableRowElement("main.Righe.Consegne",1);
        else
            Assertions.fail("Riga consegna 2 non individuata");

        //Verifico che le righe di consegna n. 1 e 2 siano in stato ‘EVASA’. La consegna n. 2 deve essere ‘Associata Totalmente’ a Fattura.
        Assertions.assertEquals("Evasa", getTableColumnElement(rowElement1,5).getText());
        Assertions.assertEquals("Evasa", getTableColumnElement(rowElement2,5).getText());
        Assertions.assertEquals("Non Associata", getTableColumnElement(rowElement1,6).getText());
        Assertions.assertEquals("Associata Totalmente", getTableColumnElement(rowElement2,6).getText());

        //Mi posiziono sulla seconda consegna
        rowElement2.click();

        //Passo alla ‘visualizzazione fattura collegata’
        doClickButton("submitForm('doVisualizzaFattura');");

        String pgFatturaCreated = sharedResource.getVal02();
        Assertions.assertEquals(pgFatturaCreated, getGrapheneElement("main.pg_fattura_passiva").getAttribute("value"));

        doClickButton("doChiudiForm()");
    }

    @Test
    @Order(14)
    public void testAnnullaRiscontroValore() {
        switchToFrameMenu();
        doSelezionaMenu(AMM_FATTUR_FATPAS_M);

        switchToFrameWorkspace();

        doClickButton("doNuovaRicerca()");

        String pgFatturaCreated = sharedResource.getVal02();
        getGrapheneElement("main.pg_fattura_passiva").writeIntoElement(pgFatturaCreated);

        doClickButton("doCerca()");

        Alert alert = browser.switchTo().alert();
        Assertions.assertEquals(AlertMessage.MESSAGE_RICERCA_MONO_RECORD.value(), alert.getText());
        alert.accept();

        doClickButton("doTab('tab','tabFatturaPassivaOrdini')");

        //Seleziono la consegna collegata alla fattura
        doSelectTableRow("main.Ordini",0);

        doClickButton("submitForm('doRemoveFromCRUD(main.Ordini)')");

        doClickButton("doSalva()");

        alert = browser.switchTo().alert();
        Assertions.assertEquals("Squadratura dettagli IVA con la fattura elettronica per le aliquote IVA: 22.00!", alert.getText());
        alert.accept();

        //Vado sulla tab ‘Dettaglio’
        doClickButton("doTab('tab','tabFatturaPassivaDettaglio')");

        //Elimino l’unico dettaglio presente sulla fattura.
        doSelectTableRow("main.Dettaglio",0);
        doClickButton("submitForm('doRemoveFromCRUD(main.Dettaglio)')");

        //Vado sulla testata fattura e
        doClickButton("doTab('tab','tabFatturaPassiva')");

        alert = browser.switchTo().alert();
        Assertions.assertEquals("Attenzione: il totale dei dettagli di 0.00 (Imponibile + IVA) non corrisponde al totale di 249.00 EUR della testata fattura!", alert.getText());
        alert.accept();

        //Indico che è ‘non da ordini’.
        getGrapheneElement("main.flDaOrdini").click();

        //Torno sulla tab ‘Dettaglio’ e trovo la proposta del dettaglio (proveniente da fatturazione elettronica) da completare.
        doClickButton("doTab('tab','tabFatturaPassivaDettaglio')");

        //Indico il codice articolo "AA00026"
        doClickButton("doBlankSearch(main.Dettaglio.bene_servizio)");
        getGrapheneElement("main.Dettaglio.bene_servizio.cd_bene_servizio").writeIntoElement("AA00026");
        doClickButton("doSearch(main.Dettaglio.bene_servizio)");

        //Indico il prezzo unitario, 205,00 forzo l’IVA a 44,00
        getGrapheneElement("main.Dettaglio.prezzo_unitario").clear();
        getGrapheneElement("main.Dettaglio.prezzo_unitario").writeIntoElement("205");
        doClickButton("confirmModalInputChange(this,'main.Dettaglio.prezzo_unitario','doCalcolaTotaliDiRiga')");

        getGrapheneElement("main.Dettaglio.im_iva").clear();
        getGrapheneElement("main.Dettaglio.im_iva").writeIntoElement("44");
        doClickButton("confirmModalInputChange(this,'main.Dettaglio.im_iva','doForzaIVA')");

        //Torno sulla tab ‘Dati Coge/Coan’
        doClickButton("doTab('tabFatturaPassivaDettaglio','tabFatturaPassivaDettaglioDetail2')");
        Assertions.assertEquals("C13012", getGrapheneElement("main.Dettaglio.find_voce_ep.cd_voce_ep").getAttribute("value"));

        //Mi sposto sulla testata, indico lo stato ‘Non liquidabile’ e salvo.
        doClickButton("doTab('tab','tabFatturaPassiva')");

        doClickButton("doSalva()");

        alert = browser.switchTo().alert();
        Assertions.assertEquals(AlertMessage.SALVATAGGIO_ESEGUITO.value(), alert.getText());
        alert.accept();

        //La scrittura contabile riporta (correttamente):
        doClickButton("doTab('tab','tabEconomica')");

        //Il conto di costo C13012 (associato alla categoria del bene scelto) in Dare per 249,00;
        Assertions.assertEquals("C13012", getTableColumnElement("main.Movimenti Dare",0,1).getText());
        Assertions.assertEquals("249,00", getTableColumnElement("main.Movimenti Dare",0,5).getText());
        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.Movimenti Dare",1),"Cannot find Element <tr> with tableName 'main.Movimenti Dare' and numberRow: 1");

        //Il controconto di debito P13012 in Avere per 205,00;
        Assertions.assertEquals("P13012", getTableColumnElement("main.Movimenti Avere",0,1).getText());
        Assertions.assertEquals("205,00", getTableColumnElement("main.Movimenti Avere",0,5).getText());

        //Il conto IVA Split P71012I in Avere per 44,00
        Assertions.assertEquals("P71012I", getTableColumnElement("main.Movimenti Avere",1,1).getText());
        Assertions.assertEquals("44,00", getTableColumnElement("main.Movimenti Avere",1,5).getText());

        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.Movimenti Avere",2),"Cannot find Element <tr> with tableName 'main.Movimenti Avere' and numberRow: 2");

        //Vado sulla tab analitica per controllare scrittura
        doClickButton("doTab('tab','tabAnalitica')");
        Assertions.assertEquals("C13012", getTableColumnElement("main.Movimenti Analitici",0,1).getText());
        Assertions.assertEquals("Dare", getTableColumnElement("main.Movimenti Analitici",0,2).getText());
        Assertions.assertEquals("C13012", getTableColumnElement("main.Movimenti Analitici",0,3).getText());
        Assertions.assertEquals("000.000.000", getTableColumnElement("main.Movimenti Analitici",0,5).getText());
        Assertions.assertEquals("PTEST003", getTableColumnElement("main.Movimenti Analitici",0,6).getText());
        Assertions.assertEquals("D", getTableColumnElement("main.Movimenti Analitici",0,7).getText());
        Assertions.assertEquals("249,00", getTableColumnElement("main.Movimenti Analitici",0,8).getText());

        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.Movimenti Analitici",1),"Cannot find Element <tr> with tableName 'main.Movimenti Analitici' and numberRow: 1");

        doClickButton("doChiudiForm()");
    }

    @Test
    @Order(15)
    public void testAssociaNotaCredito001() {
        switchToFrameMenu();
        doSelezionaMenu(AMM_FATTUR_FATPAS_ELE);

        switchToFrameWorkspace();

        //Ricerco la fattura SDI: 90000000003 - Nota Credito
        getGrapheneElement("main.identificativoSdi").writeIntoElement("90000000003");

        Select select = new Select(getGrapheneElement("main.statoDocumento"));
        select.selectByValue("");

        doClickButton("doCerca()");
        Alert alert = browser.switchTo().alert();
        Assertions.assertEquals(AlertMessage.MESSAGE_RICERCA_MONO_RECORD.value(), alert.getText());
        alert.accept();

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
                if (getTableColumnElement("mainTable", riga, 3).getText().equals(pgFatturaCreated))
                    rowElement1 = getTableRowElement("mainTable", riga);
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

        alert = browser.switchTo().alert();
        Assertions.assertEquals(AlertMessage.CREAZIONE_ESEGUITA.value(), alert.getText());
        alert.accept();

        //Vado sulla tab principale
        doClickButton("doTab('tab','tabFatturaPassiva')");

        //Registro il numero della nota creata
        String pgNotaCreated = getGrapheneElement("main.pg_fattura_passiva").getAttribute("value");
        sharedResource.setVal03(pgNotaCreated);

        //Vado sulla tab economica per controllare scrittura
        doClickButton("doTab('tab','tabEconomica')");
        //Il conto di costo C13012 (associato alla categoria del bene scelto) in Avere per 249,00;
        Assertions.assertEquals("C13012", getTableColumnElement("main.Movimenti Avere",0,1).getText());
        Assertions.assertEquals("249,00", getTableColumnElement("main.Movimenti Avere",0,5).getText());
        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.Movimenti Avere",1),"Cannot find Element <tr> with tableName 'main.Movimenti Avere' and numberRow: 1");

        //Il controconto di debito P13012 in Dare per 205,00;
        Assertions.assertEquals("P13012", getTableColumnElement("main.Movimenti Dare",0,1).getText());
        Assertions.assertEquals("205,00", getTableColumnElement("main.Movimenti Dare",0,5).getText());

        //Il conto IVA Split P71012I in Avere per 44,00
        Assertions.assertEquals("P71012I", getTableColumnElement("main.Movimenti Dare",1,1).getText());
        Assertions.assertEquals("44,00", getTableColumnElement("main.Movimenti Dare",1,5).getText());

        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.Movimenti Dare",2),"Cannot find Element <tr> with tableName 'main.Movimenti Dare' and numberRow: 2");

        //Vado sulla tab analitica per controllare scrittura
        doClickButton("doTab('tab','tabAnalitica')");
        Assertions.assertEquals("C13012", getTableColumnElement("main.Movimenti Analitici",0,1).getText());
        Assertions.assertEquals("Avere", getTableColumnElement("main.Movimenti Analitici",0,2).getText());
        Assertions.assertEquals("C13012", getTableColumnElement("main.Movimenti Analitici",0,3).getText());
        Assertions.assertEquals("000.000.000", getTableColumnElement("main.Movimenti Analitici",0,5).getText());
        Assertions.assertEquals("PTEST003", getTableColumnElement("main.Movimenti Analitici",0,6).getText());
        Assertions.assertEquals("D", getTableColumnElement("main.Movimenti Analitici",0,7).getText());
        Assertions.assertEquals("249,00", getTableColumnElement("main.Movimenti Analitici",0,8).getText());

        Assertions.assertThrows(RuntimeException.class, ()->getTableRowElement("main.Movimenti Analitici",1),"Cannot find Element <tr> with tableName 'main.Movimenti Analitici' and numberRow: 1");

        doClickButton("doChiudiForm()");
    }
}
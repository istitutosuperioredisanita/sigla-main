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
import org.jboss.arquillian.junit.InSequence;
import org.junit.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.support.ui.Select;

import static org.junit.Assert.*;

public class CRUDOrdineAcqBPTest002 extends ActionDeployments {
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
    public static final String CD_MAGAZZINO = "PT";
    public static final String UO = "000.000";
    public static final String CDR = "000.000.000";

    public static final String CONTRATTO_ESERCIZIO = "2025";
    public static final String CONTRATTO_NUMERO = "2";

    public static final String BENE_SERVIZIO_CODICE_01 = "191202";

    @Test
    @RunAsClient
    @OperateOnDeployment(TEST_H2)
    @InSequence(100)
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
        assertEquals("101", getGrapheneElement("main.findFornitore.cd_terzo").getAttribute("value"));

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
        assertEquals("MAG", select.getFirstSelectedOption().getAttribute("value"));

        //Lascio la ‘Data prevista consegna’
        assertNotNull(getGrapheneElement("main.Righe.dtPrevConsegna").getText());

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
        assertEquals("C13003", getGrapheneElement("main.Righe.find_voce_ep.cd_voce_ep").getAttribute("value"));

        // L’analitica correttamente non c’è (non ho ancora indicato l’impegno).
        assertEquals("488,00",getGrapheneElement("imCostoEcoDaRipartire").getAttribute("value"));

        //Ritorno sulla prima tab ‘ordine d’acquisto’
        doClickButton("doTab('tab','tabOrdineAcq')");

        //Indico motivo di assenza CIG: SPESE ECONOMALI
        select = new Select(getGrapheneElement("main.motivoAssenzaCig"));
        select.selectByValue("SPESE_ECONOMALI");

        //Salvo
        doClickButton("doSalva()");

        //Restituisce messaggio di creazione eseguita
        Alert alert = browser.switchTo().alert();
        assertEquals(AlertMessage.CREAZIONE_ESEGUITA.value(), alert.getText());
        alert.accept();

        String pgProgetto = getGrapheneElement("main.numero").getAttribute("value");

        //modifico lo stato portandolo ‘In Approvazione’
        select = new Select(getGrapheneElement("main.statoForUpdate"));
        select.selectByValue("APP");

        //Salvo
        doClickButton("doSalva()");

        //Restituisce messaggio di salvataggio eseguito
        alert = browser.switchTo().alert();
        assertEquals(AlertMessage.SALVATAGGIO_ESEGUITO.value(), alert.getText());
        alert.accept();

        //modifico lo stato portandolo ‘Definitivo’
        select.selectByValue("DEF");

        //Salvo
        doClickButton("doSalva()");

        //Restituisce messaggio di assenza obbligazione
        alert = browser.switchTo().alert();
        assertEquals("Sulla consegna 2025/DSA/"+pgProgetto+"/1/1 non è indicata l'obbligazione", alert.getText());
        alert.accept();

        //Ritorno sulla tab delle righe ordine
        doClickButton("doTab('tab','tabOrdineAcqDettaglio')");

        //Seleziono la prima riga
        doSelectTableRow("main.Righe",0);

        //Vado sul pulsante ‘crea/associa impegni’
        doClickButton("doRicercaObbligazione");

        doClickButton("submitForm('doOpenObbligazioniWindow')");

        getGrapheneElement("main.find_elemento_voce.searchtool_cd_elemento_voce").clear();
        getGrapheneElement("main.find_elemento_voce.searchtool_cd_elemento_voce").writeIntoElement("13017");
        doClickButton("submitForm('doSearch(main.find_elemento_voce)')");

        doClickButton("submitForm('doConsultaInserisciVoce')");

        doSelectTableRow("mainTable",0);

        doClickButton("submitForm('doConferma')");

        doClickButton("doRiporta()");

        doClickButton("doSalva()");

        alert = browser.switchTo().alert();
        assertEquals(AlertMessage.SALVATAGGIO_ESEGUITO.value(), alert.getText());
        alert.accept();
    }
}
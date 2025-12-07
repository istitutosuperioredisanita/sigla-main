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

import it.cnr.contab.docamm00.docs.bulk.Documento_genericoBulk;
import it.cnr.contab.util.TestUserContext;
import it.cnr.jada.ejb.CRUDComponentSession;
import it.cnr.test.h2.DeploymentsH2;
import org.junit.jupiter.api.Order;

import jakarta.ejb.EJB;
import org.junit.jupiter.api.Test;

import java.util.Optional;


public class DocumentoStornoScrittureTest extends DeploymentsH2 {
    @EJB
    private CRUDComponentSession crudComponentSession;

    /**
     * Documento Generico {@code con Causale} su mono voce {@code Stornato} da altro Documento Generico:
     * <p><b>Dati Documento Generico</b>
     * <pre>
     * Voce Bilancio: 22010 - Attrezzature scientifiche
     * Importo: 1000,00
     * Causale: Rimborso02
     * Configurazione Causale:
     *       D - P22035
     *       A - A21015
     * </pre></p>
     * <b>Scrittura Economica Documento Generico Principale</b>
     * <pre>
     *     Sezione   Importo      Conto
     *        D      1000,00      P22035 - Debiti verso fornitori per acquisto o
     *                                     costruzione di fabbricati strumentali
     *        A      1000,00      A21015 - Crediti per altri finanziamenti e
     *                                     contributi ministeriali
     * </pre>
     * <b>Scrittura Economica Documento Generico di Storno</b>
     * <pre>
     *     Sezione   Importo      Conto
     *        D      1000,00      A21015 - Crediti per altri finanziamenti e
     *                                     contributi ministeriali
     *        A      1000,00      P22035 - Debiti verso fornitori per acquisto o
     *                                     costruzione di fabbricati strumentali
     * </pre>
     */
    @Test
    
    @Order(1)
    public void testDocumentoGenericoStorno001() throws Exception {
        {
            Documento_genericoBulk documentoCogeBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(new TestUserContext(),
                            new Documento_genericoBulk("000", "GENERICO_S", "000.000", 2025,
                                    3L)))
                    .filter(Documento_genericoBulk.class::isInstance)
                    .map(Documento_genericoBulk.class::cast)
                    .orElse(null);
            /*
            ResultScrittureContabili result = Utility.createProposeScritturaComponentSession().proposeScrittureContabili(
                    new TestUserContext(),
                    documentoCogeBulk);
            assertEquals(new BigDecimal("1000.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getIm_scrittura).orElse(null));
            assertEquals(new BigDecimal("1000.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getImTotaleDare).orElse(null));
            assertEquals(new BigDecimal("1000.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getImTotaleAvere).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiDare = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                    .orElse(new BulkList<>());
            assertEquals(1, movimentiDare.size());

            Optional<Movimento_cogeBulk> rigaDare = movimentiDare.stream().findAny();
            assertTrue("Riga dare non presente.", rigaDare.isPresent());
            assertEquals("P22035", rigaDare.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            assertEquals(new BigDecimal("1000.00"), rigaDare.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                    .orElse(new BulkList<>());
            assertEquals(1, movimentiAvere.size());

            Optional<Movimento_cogeBulk> rigaAvere = movimentiAvere.stream().findAny();
            assertTrue("Riga avere non presente.", rigaAvere.isPresent());
            assertEquals("A21015", rigaAvere.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            assertEquals(new BigDecimal("1000.00"), rigaAvere.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            Utility.createScritturaPartitaDoppiaFromDocumentoComponentSession().modificaConBulk(new TestUserContext(), documentoCogeBulk);

             */
        }
        //DOCUMENTO DI STORNO
        {
            Documento_genericoBulk documentoCogeBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(new TestUserContext(),
                            new Documento_genericoBulk("000", "GENERICO_S", "000.000", 2025,
                                    4L)))
                    .filter(Documento_genericoBulk.class::isInstance)
                    .map(Documento_genericoBulk.class::cast)
                    .orElse(null);
            /*
            ResultScrittureContabili result = Utility.createProposeScritturaComponentSession().proposeScrittureContabili(
                    new TestUserContext(),
                    documentoCogeBulk);
            assertEquals(new BigDecimal("1000.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getIm_scrittura).orElse(null));
            assertEquals(new BigDecimal("1000.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getImTotaleDare).orElse(null));
            assertEquals(new BigDecimal("1000.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getImTotaleAvere).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiDare = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                    .orElse(new BulkList<>());
            assertEquals(1, movimentiDare.size());

            Optional<Movimento_cogeBulk> rigaDare = movimentiDare.stream().findAny();
            assertTrue("Riga dare non presente.", rigaDare.isPresent());
            assertEquals("A21015", rigaDare.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            assertEquals(new BigDecimal("1000.00"), rigaDare.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                    .orElse(new BulkList<>());
            assertEquals(1, movimentiAvere.size());

            Optional<Movimento_cogeBulk> rigaAvere = movimentiAvere.stream().findAny();
            assertTrue("Riga avere non presente.", rigaAvere.isPresent());
            assertEquals("P22035", rigaAvere.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            assertEquals(new BigDecimal("1000.00"), rigaAvere.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            Utility.createScritturaPartitaDoppiaFromDocumentoComponentSession().modificaConBulk(new TestUserContext(), documentoCogeBulk);

             */
        }
    }

    /**
     * {@code 2} Documenti Generici {@code Stornati} da altri {@code 2} Documenti di Storno:
     * <p><b>Dati Primo Documento Generico (A)</b>
     * <pre>
     *     <b>Riga 1A</b>
     *        Voce Bilancio: 22011 - Attrezzature sanitarie
     *        Importo:       30,00
     *     <b>Riga 2A</b>
     *        Voce Bilancio: 22010 - Attrezzature scientifiche
     *        Importo:       70,00
     * </pre>
     * <b>Scrittura Economica</b>
     * <pre>
     *     Sezione   Importo      Conto
     *        D        30,00      A22011 - Attrezzature sanitarie
     *        D        70,00      A22010 - Attrezzature scientifiche
     *        A        30,00      P22011 - Debiti verso fornitori per acquisto di
     *                                     attrezzature sanitarie
     *        A        70,00      P22010 - Debiti verso fornitori per acquisto di
     *                                     attrezzature scientifiche
     * </pre></p>
     * <p><b>Dati Secondo Documento Generico (B)</b>
     * <pre>
     *     <b>Riga 1B</b>
     *        Voce Bilancio: 22011 - Attrezzature sanitarie
     *        Importo:      100,00
     * </pre>
     * <b>Scrittura Economica</b>
     * <pre>
     *     Sezione   Importo      Conto
     *        D       100,00      A22011 - Attrezzature sanitarie
     *        A       100,00      P22011 - Debiti verso fornitori per acquisto di
     *                                     attrezzature sanitarie
     * </pre></p>
     * <p><b>Dati Primo Documento Generico di Storno (C)</b>
     * <pre>
     *     <b>Riga 1C - storna riga 2A</b>
     *        Voce Bilancio: 22010 - Attrezzature scientifiche
     *        Importo:       70,00
     *     <b>Riga 2C - storna riga 1B</b>
     *        Voce Bilancio: 22011 - Attrezzature sanitarie
     *        Importo:      100,00
     * </pre>
     * <b>Scrittura Economica</b>
     * <pre>
     *     Sezione   Importo      Conto
     *        D        70,00      P22010 - Debiti verso fornitori per acquisto di
     *                                     attrezzature scientifiche
     *        D       100,00      P22011 - Debiti verso fornitori per acquisto di
     *                                     attrezzature sanitarie
     *        A        70,00      A22010 - Attrezzature scientifiche
     *        A       100,00      A22011 - Attrezzature sanitarie
     * </pre>
     * </p>
     * <p><b>Dati Secondo Documento Generico di Storno (D)</b>
     * <pre>
     *     <b>Riga 1D - storna riga 1A</b>
     *        Voce Bilancio: 22011 - Attrezzature sanitarie
     *        Importo:       30,00
     * </pre>
     * <b>Scrittura Economica</b>
     * <pre>
     *     Sezione   Importo      Conto
     *        D        30,00      P22011 - Debiti verso fornitori per acquisto di
     *                                     attrezzature sanitarie
     *        A        30,00      A22011 - Attrezzature sanitarie
     * </pre>
     * </p>
     */
    @Test
    
    @Order(2)
    public void testDocumentoGenericoStorno002() throws Exception {
        {
            Documento_genericoBulk documentoCogeBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(new TestUserContext(),
                            new Documento_genericoBulk("000", "GENERICO_S", "000.000", 2025,
                                    5L)))
                    .filter(Documento_genericoBulk.class::isInstance)
                    .map(Documento_genericoBulk.class::cast)
                    .orElse(null);
            /*
            ResultScrittureContabili result = Utility.createProposeScritturaComponentSession().proposeScrittureContabili(
                    new TestUserContext(),
                    documentoCogeBulk);
            assertEquals(new BigDecimal("100.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getIm_scrittura).orElse(null));
            assertEquals(new BigDecimal("100.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getImTotaleDare).orElse(null));
            assertEquals(new BigDecimal("100.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getImTotaleAvere).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiDare = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                    .orElse(new BulkList<>());
            assertEquals(2, movimentiDare.size());

            Optional<Movimento_cogeBulk> rigaTipoAttivita1 = movimentiDare.stream().filter(el->"A22011".equals(el.getCd_voce_ep())).findAny();
            assertTrue("Conto A22011 non presente.", rigaTipoAttivita1.isPresent());
            assertTrue("Riga tipo attività non presente.", rigaTipoAttivita1.filter(Movimento_cogeBulk::isRigaTipoAttivita).isPresent());
            assertEquals(new BigDecimal("30.00"), rigaTipoAttivita1.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            Optional<Movimento_cogeBulk> rigaTipoAttivita2 = movimentiDare.stream().filter(el->"A22010".equals(el.getCd_voce_ep())).findAny();
            assertTrue("Conto A22010 non presente.", rigaTipoAttivita2.isPresent());
            assertTrue("Riga tipo attività non presente.", rigaTipoAttivita2.filter(Movimento_cogeBulk::isRigaTipoAttivita).isPresent());
            assertEquals(new BigDecimal("70.00"), rigaTipoAttivita2.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                    .orElse(new BulkList<>());
            assertEquals(2, movimentiAvere.size());

            Optional<Movimento_cogeBulk> rigaTipoDebito1 = movimentiAvere.stream().filter(el->"P22011".equals(el.getCd_voce_ep())).findAny();
            assertTrue("Conto P22011 non presente.", rigaTipoDebito1.isPresent());
            assertTrue("Riga tipo debito non presente.", rigaTipoDebito1.filter(Movimento_cogeBulk::isRigaTipoDebito).isPresent());
            assertEquals(new BigDecimal("30.00"), rigaTipoDebito1.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            Optional<Movimento_cogeBulk> rigaTipoDebito2 = movimentiAvere.stream().filter(el->"P22010".equals(el.getCd_voce_ep())).findAny();
            assertTrue("Conto P22010 non presente.", rigaTipoDebito2.isPresent());
            assertTrue("Riga tipo debito non presente.", rigaTipoDebito2.filter(Movimento_cogeBulk::isRigaTipoDebito).isPresent());
            assertEquals(new BigDecimal("70.00"), rigaTipoDebito2.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            Utility.createScritturaPartitaDoppiaFromDocumentoComponentSession().modificaConBulk(new TestUserContext(), documentoCogeBulk);

             */
        }
        {
            Documento_genericoBulk documentoCogeBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(new TestUserContext(),
                            new Documento_genericoBulk("000", "GENERICO_S", "000.000", 2025,
                                    6L)))
                    .filter(Documento_genericoBulk.class::isInstance)
                    .map(Documento_genericoBulk.class::cast)
                    .orElse(null);
            /*
            ResultScrittureContabili result = Utility.createProposeScritturaComponentSession().proposeScrittureContabili(
                    new TestUserContext(),
                    documentoCogeBulk);
            assertEquals(new BigDecimal("100.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getIm_scrittura).orElse(null));
            assertEquals(new BigDecimal("100.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getImTotaleDare).orElse(null));
            assertEquals(new BigDecimal("100.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getImTotaleAvere).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiDare = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                    .orElse(new BulkList<>());
            assertEquals(1, movimentiDare.size());

            Optional<Movimento_cogeBulk> rigaTipoAttivita1 = movimentiDare.stream().filter(el->"A22011".equals(el.getCd_voce_ep())).findAny();
            assertTrue("Conto A22011 non presente.", rigaTipoAttivita1.isPresent());
            assertTrue("Riga tipo attività non presente.", rigaTipoAttivita1.filter(Movimento_cogeBulk::isRigaTipoAttivita).isPresent());
            assertEquals(new BigDecimal("100.00"), rigaTipoAttivita1.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                    .orElse(new BulkList<>());
            assertEquals(1, movimentiAvere.size());

            Optional<Movimento_cogeBulk> rigaTipoDebito1 = movimentiAvere.stream().filter(el->"P22011".equals(el.getCd_voce_ep())).findAny();
            assertTrue("Conto P22011 non presente.", rigaTipoDebito1.isPresent());
            assertTrue("Riga tipo debito non presente.", rigaTipoDebito1.filter(Movimento_cogeBulk::isRigaTipoDebito).isPresent());
            assertEquals(new BigDecimal("100.00"), rigaTipoDebito1.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            Utility.createScritturaPartitaDoppiaFromDocumentoComponentSession().modificaConBulk(new TestUserContext(), documentoCogeBulk);

             */
        }
        {
            Documento_genericoBulk documentoCogeBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(new TestUserContext(),
                            new Documento_genericoBulk("000", "GENERICO_S", "000.000", 2025,
                                    7L)))
                    .filter(Documento_genericoBulk.class::isInstance)
                    .map(Documento_genericoBulk.class::cast)
                    .orElse(null);
            /*
            ResultScrittureContabili result = Utility.createProposeScritturaComponentSession().proposeScrittureContabili(
                    new TestUserContext(),
                    documentoCogeBulk);
            assertEquals(new BigDecimal("170.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getIm_scrittura).orElse(null));
            assertEquals(new BigDecimal("170.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getImTotaleDare).orElse(null));
            assertEquals(new BigDecimal("170.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getImTotaleAvere).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                    .orElse(new BulkList<>());
            assertEquals(2, movimentiAvere.size());

            Optional<Movimento_cogeBulk> rigaTipoAttivita1 = movimentiAvere.stream().filter(el->"A22011".equals(el.getCd_voce_ep())).findAny();
            assertTrue("Conto A22011 non presente.", rigaTipoAttivita1.isPresent());
            assertTrue("Riga tipo attività non presente.", rigaTipoAttivita1.filter(Movimento_cogeBulk::isRigaTipoAttivita).isPresent());
            assertEquals(new BigDecimal("100.00"), rigaTipoAttivita1.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            Optional<Movimento_cogeBulk> rigaTipoAttivita2 = movimentiAvere.stream().filter(el->"A22010".equals(el.getCd_voce_ep())).findAny();
            assertTrue("Conto A22010 non presente.", rigaTipoAttivita2.isPresent());
            assertTrue("Riga tipo attività non presente.", rigaTipoAttivita2.filter(Movimento_cogeBulk::isRigaTipoAttivita).isPresent());
            assertEquals(new BigDecimal("70.00"), rigaTipoAttivita2.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiDare = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                    .orElse(new BulkList<>());
            assertEquals(2, movimentiDare.size());

            Optional<Movimento_cogeBulk> rigaTipoDebito1 = movimentiDare.stream().filter(el->"P22011".equals(el.getCd_voce_ep())).findAny();
            assertTrue("Conto P22011 non presente.", rigaTipoDebito1.isPresent());
            assertTrue("Riga tipo debito non presente.", rigaTipoDebito1.filter(Movimento_cogeBulk::isRigaTipoDebito).isPresent());
            assertEquals(new BigDecimal("100.00"), rigaTipoDebito1.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            Optional<Movimento_cogeBulk> rigaTipoDebito2 = movimentiDare.stream().filter(el->"P22010".equals(el.getCd_voce_ep())).findAny();
            assertTrue("Conto P22010 non presente.", rigaTipoDebito2.isPresent());
            assertTrue("Riga tipo debito non presente.", rigaTipoDebito2.filter(Movimento_cogeBulk::isRigaTipoDebito).isPresent());
            assertEquals(new BigDecimal("70.00"), rigaTipoDebito2.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            Utility.createScritturaPartitaDoppiaFromDocumentoComponentSession().modificaConBulk(new TestUserContext(), documentoCogeBulk);

             */
        }
        {
            Documento_genericoBulk documentoCogeBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(new TestUserContext(),
                            new Documento_genericoBulk("000", "GENERICO_S", "000.000", 2025,
                                    8L)))
                    .filter(Documento_genericoBulk.class::isInstance)
                    .map(Documento_genericoBulk.class::cast)
                    .orElse(null);
            /*
            ResultScrittureContabili result = Utility.createProposeScritturaComponentSession().proposeScrittureContabili(
                    new TestUserContext(),
                    documentoCogeBulk);
            assertEquals(new BigDecimal("30.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getIm_scrittura).orElse(null));
            assertEquals(new BigDecimal("30.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getImTotaleDare).orElse(null));
            assertEquals(new BigDecimal("30.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getImTotaleAvere).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                    .orElse(new BulkList<>());
            assertEquals(1, movimentiAvere.size());

            Optional<Movimento_cogeBulk> rigaTipoAttivita1 = movimentiAvere.stream().filter(el->"A22011".equals(el.getCd_voce_ep())).findAny();
            assertTrue("Conto A22011 non presente.", rigaTipoAttivita1.isPresent());
            assertTrue("Riga tipo attività non presente.", rigaTipoAttivita1.filter(Movimento_cogeBulk::isRigaTipoAttivita).isPresent());
            assertEquals(new BigDecimal("30.00"), rigaTipoAttivita1.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiDare = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                    .orElse(new BulkList<>());
            assertEquals(1, movimentiDare.size());

            Optional<Movimento_cogeBulk> rigaTipoDebito1 = movimentiDare.stream().filter(el->"P22011".equals(el.getCd_voce_ep())).findAny();
            assertTrue("Conto P22011 non presente.", rigaTipoDebito1.isPresent());
            assertTrue("Riga tipo debito non presente.", rigaTipoDebito1.filter(Movimento_cogeBulk::isRigaTipoDebito).isPresent());
            assertEquals(new BigDecimal("30.00"), rigaTipoDebito1.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            Utility.createScritturaPartitaDoppiaFromDocumentoComponentSession().modificaConBulk(new TestUserContext(), documentoCogeBulk);

             */
        }
    }
}

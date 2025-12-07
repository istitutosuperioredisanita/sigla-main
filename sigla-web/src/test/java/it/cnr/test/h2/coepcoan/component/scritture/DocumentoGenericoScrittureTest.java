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
import it.cnr.contab.doccont00.core.bulk.MandatoBulk;
import it.cnr.contab.doccont00.core.bulk.MandatoIBulk;
import it.cnr.contab.util.TestUserContext;
import it.cnr.jada.ejb.CRUDComponentSession;
import it.cnr.test.h2.DeploymentsH2;
import org.junit.jupiter.api.Order;

import jakarta.ejb.EJB;
import org.junit.jupiter.api.Test;

import java.util.Optional;


public class DocumentoGenericoScrittureTest extends DeploymentsH2 {

    /**
     * Documento Generico {@code Semplice} su mono voce {@code Liquidato} mandato di pagamento:
     * <p><b>Dati Documento Generico</b>
     * <pre>
     * Voce Bilancio: 22010 - Attrezzature scientifiche
     * Importo:     1000,00
     * </pre></p>
     * <b>Scrittura Economica Documento Generico</b>
     * <pre>
     *     Sezione   Importo      Conto
     *        D      1000,00      A22010 - Attrezzature scientifiche
     *        A      1000,00      P22010 - Debiti verso fornitori per acquisto di
     *                                     attrezzature scientifiche
     * </pre>
     * <b>Scrittura Economica Mandato</b>
     * <pre>
     *     Sezione   Importo      Conto
     *        D      1000,00      P22010 - Debiti verso fornitori per acquisto di
     *                                     attrezzature scientifiche
     *        A      1000,00      A00053 - Istituto tesoriere/cassiere
     * </pre>
     */
    @Test
    
    @Order(1)
    public void testDocumentoGenerico001() throws Exception {
        Documento_genericoBulk documentoCogeBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(new TestUserContext(),
                        new Documento_genericoBulk("000","GENERICO_S","000.000",2025,
                                1L)))
                .filter(Documento_genericoBulk.class::isInstance)
                .map(Documento_genericoBulk.class::cast)
                .orElse(null);
        /*
        ResultScrittureContabili result = Utility.createProposeScritturaComponentSession().proposeScrittureContabili(
                new TestUserContext(),
                documentoCogeBulk);
        assertTrue(Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                .filter(el->el.getIm_scrittura().compareTo(new BigDecimal(1000))==0)
                .filter(el->el.getImTotaleDare().compareTo(new BigDecimal(1000))==0)
                .filter(el->el.getImTotaleAvere().compareTo(new BigDecimal(1000))==0)
                .isPresent());
        assertEquals(1, Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                .orElse(new BulkList<>())
                .size());
        assertEquals(1, Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                .orElse(new BulkList<>())
                .size());
        assertTrue(Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                .orElse(new BulkList<>())
                .stream().findFirst()
                .filter(Movimento_cogeBulk::isSezioneDare)
                .filter(el->"A22010".equals(el.getCd_voce_ep()))
                .filter(el->el.getIm_movimento().compareTo(new BigDecimal(1000))==0)
                .isPresent());
        assertTrue(Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                .orElse(new BulkList<>())
                .stream().findFirst()
                .filter(Movimento_cogeBulk::isSezioneAvere)
                .filter(el->"P22010".equals(el.getCd_voce_ep()))
                .filter(el->el.getIm_movimento().compareTo(new BigDecimal(1000))==0)
                .isPresent());

        Utility.createScritturaPartitaDoppiaFromDocumentoComponentSession().modificaConBulk(new TestUserContext(), documentoCogeBulk);

        MandatoBulk mandatoBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(new TestUserContext(),
                        new MandatoIBulk("000",2025,1L)))
                .filter(MandatoBulk.class::isInstance)
                .map(MandatoBulk.class::cast)
                .orElse(null);

        result = Utility.createProposeScritturaComponentSession().proposeScrittureContabili(
                new TestUserContext(),
                mandatoBulk);

        assertTrue(Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                .filter(el->el.getIm_scrittura().compareTo(new BigDecimal(1000))==0)
                .filter(el->el.getImTotaleDare().compareTo(new BigDecimal(1000))==0)
                .filter(el->el.getImTotaleAvere().compareTo(new BigDecimal(1000))==0)
                .isPresent());
        assertEquals(1, Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                .orElse(new BulkList<>())
                .size());
        assertEquals(1, Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                .orElse(new BulkList<>())
                .size());
        assertTrue(Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                .orElse(new BulkList<>())
                .stream().findFirst()
                .filter(Movimento_cogeBulk::isSezioneDare)
                .filter(el->"P22010".equals(el.getCd_voce_ep()))
                .filter(el->el.getIm_movimento().compareTo(new BigDecimal(1000))==0)
                .isPresent());
        assertTrue(Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                .orElse(new BulkList<>())
                .stream().findFirst()
                .filter(Movimento_cogeBulk::isSezioneAvere)
                .filter(el->"A00053".equals(el.getCd_voce_ep()))
                .filter(el->el.getIm_movimento().compareTo(new BigDecimal(1000))==0)
                .isPresent());

         */
    }

    /**
     * Documento Generico {@code con Causale} su mono voce {@code Liquidato} con mandato di pagamento:
     * <p><b>Dati Documento Generico</b>
     * <pre>
     * Voce Bilancio: 22010 - Attrezzature scientifiche
     * Importo: 1000,00
     * Causale: Rimborso01
     * Configurazione Causale:
     *       D - P22034
     *       A - A21014
     * </pre></p>
     * <b>Scrittura Economica Documento Generico</b>
     * <pre>
     *     Sezione   Importo      Conto
     *        D      1000,00      P22034 - Debiti verso fornitori per acquisto o
     *                                     costruzione di fabbricati strumentali
     *        A      1000,00      A21014 - Crediti per altri finanziamenti e
     *                                     contributi ministeriali
     * </pre>
     * <b>Scrittura Economica Mandato</b>
     * <pre>
     *     Sezione   Importo      Conto
     *        D      1000,00      A21014 - Crediti per altri finanziamenti e
     *                                     contributi ministeriali
     *        A      1000,00      A00053 - Istituto tesoriere/cassiere
     * </pre>
     */
    @Test
    
    @Order(2)
    public void testDocumentoGenerico002() throws Exception {
        {
            Documento_genericoBulk documentoCogeBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(new TestUserContext(),
                            new Documento_genericoBulk("000", "GENERICO_S", "000.000", 2025,
                                    2L)))
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
            assertEquals("P22034", rigaDare.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            assertEquals(new BigDecimal("1000.00"), rigaDare.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                    .orElse(new BulkList<>());
            assertEquals(1, movimentiAvere.size());

            Optional<Movimento_cogeBulk> rigaAvere = movimentiAvere.stream().findAny();
            assertTrue("Riga avere non presente.", rigaAvere.isPresent());
            assertEquals("A21014", rigaAvere.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            assertEquals(new BigDecimal("1000.00"), rigaAvere.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            Utility.createScritturaPartitaDoppiaFromDocumentoComponentSession().modificaConBulk(new TestUserContext(), documentoCogeBulk);

             */
        }
        {
            MandatoBulk mandatoBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(new TestUserContext(),
                            new MandatoIBulk("000", 2025, 5L)))
                    .filter(MandatoBulk.class::isInstance)
                    .map(MandatoBulk.class::cast)
                    .orElse(null);
            /*
            ResultScrittureContabili result = Utility.createProposeScritturaComponentSession().proposeScrittureContabili(
                    new TestUserContext(),
                    mandatoBulk);

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
            assertEquals("A21014", rigaDare.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            assertEquals(new BigDecimal("1000.00"), rigaDare.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                    .orElse(new BulkList<>());
            assertEquals(1, movimentiAvere.size());

            Optional<Movimento_cogeBulk> rigaAvere = movimentiAvere.stream().findAny();
            assertTrue("Riga avere non presente.", rigaAvere.isPresent());
            assertEquals("A00053", rigaAvere.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            assertEquals(new BigDecimal("1000.00"), rigaAvere.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

             */
        }
    }

    /**
     * Documento Generico {@code Residuo} di un {@code Anno Chiuso} su mono voce {@code Liquidato} con mandato di pagamento:
     * <p><b>Dati Documento Generico</b>
     * <pre>
     * Esercizio: 2023
     * Esercizio contabile chiuso
     * Voce Bilancio: 22010 - Attrezzature scientifiche
     * Importo: 1000,00
     * </pre></p>
     * <b>Scrittura Economica Documento Generico</b>
     * <pre>
     *     Errore: Scrittura Economica non generabile/modificabile.
     *     L'esercizio contabile 2023 per il cds 000 risulta essere non aperto
     * </pre>
     * <b>Scrittura Economica Mandato</b>
     * <pre>
     *     Sezione   Importo      Conto
     *        D      1000,00      P22010 - Crediti per altri finanziamenti e
     *                                     contributi ministeriali
     *        A      1000,00      A00053 - Istituto tesoriere/cassiere
     * </pre>
     */
    @Test
    
    @Order(3)
    public void testDocumentoGenerico003() throws Exception {
        {
            Documento_genericoBulk documentoCogeBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(new TestUserContext(),
                            new Documento_genericoBulk("000", "GENERICO_S", "000.000", 2024,
                                    1L)))
                    .filter(Documento_genericoBulk.class::isInstance)
                    .map(Documento_genericoBulk.class::cast)
                    .orElse(null);
/*
            assertThrows("Scrittura Economica non generabile/modificabile. L'esercizio contabile 2023 per il cds 000 risulta essere non aperto.",
                        ScritturaPartitaDoppiaNotRequiredException.class, () ->Utility.createProposeScritturaComponentSession().proposeScrittureContabili(
                        new TestUserContext(),
                        documentoCogeBulk));

 */
        }
        {
            MandatoBulk mandatoBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(new TestUserContext(),
                            new MandatoIBulk("000", 2025, 6L)))
                    .filter(MandatoBulk.class::isInstance)
                    .map(MandatoBulk.class::cast)
                    .orElse(null);
            /*
            ResultScrittureContabili result = Utility.createProposeScritturaComponentSession().proposeScrittureContabili(
                    new TestUserContext(),
                    mandatoBulk);

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
            assertEquals("P22010", rigaDare.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            assertEquals(new BigDecimal("1000.00"), rigaDare.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                    .orElse(new BulkList<>());
            assertEquals(1, movimentiAvere.size());

            Optional<Movimento_cogeBulk> rigaAvere = movimentiAvere.stream().findAny();
            assertTrue("Riga avere non presente.", rigaAvere.isPresent());
            assertEquals("A00053", rigaAvere.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            assertEquals(new BigDecimal("1000.00"), rigaAvere.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

             */
        }
    }
}

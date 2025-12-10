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

import it.cnr.contab.coepcoan00.core.bulk.*;
import it.cnr.contab.doccont00.core.bulk.MandatoBulk;
import it.cnr.contab.doccont00.core.bulk.MandatoIBulk;
import it.cnr.contab.missioni00.docs.bulk.AnticipoBulk;
import it.cnr.contab.missioni00.docs.bulk.MissioneBulk;
import it.cnr.contab.util.TestUserContext;
import it.cnr.contab.util.Utility;
import it.cnr.jada.bulk.BulkList;
import org.junit.jupiter.api.*;
import it.cnr.test.h2.DeploymentsH2;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class AnticipoMissioneRimborsoScrittureTest extends DeploymentsH2 {
    /**
     * Missione
     * <p><b>Dati Missione</b>
     * <pre>
     * Voce Bilancio: 13024 - Prodotti chimici
     * CDR/GAE: 000.000.000/PTEST001
     * Importo: 100,00
     * </pre></p>
     * <b>Scrittura Economica Missione</b>
     * <pre>
     *     Sezione   Importo      Conto
     *        D      100,00       C13024 - Prodotti chimici
     *        A      100,00       P13024 - Debiti verso fornitori - Prodotti chimici
     * </pre>
     * <b>Scrittura Analitica Missione</b>
     * <pre>
     *     Sezione   Importo      CDR/GAE                   Conto Analitico
     *        D      100,00       000.000.000/PTEST001      C13024 - Prodotti chimici
     * </pre>
     * <b>Scrittura Economica Mandato</b>
     * <pre>
     *     Sezione   Importo      Conto
     *        D      100,00       P13024 - Debiti verso fornitori - Prodotti chimici
     *        A      100,00       A00053 - Istituto tesoriere/cassiere
     * </pre>
     */
    @Test
    @Order(1)
    public void testMissioneSenzaAnticipo001() throws Exception {
        {
            MissioneBulk missioneBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(new TestUserContext(),
                            new MissioneBulk("000", "000.000", 2025, 1L)))
                    .filter(MissioneBulk.class::isInstance)
                    .map(MissioneBulk.class::cast)
                    .orElse(null);
            ResultScrittureContabili result = Utility.createProposeScritturaComponentSession().proposeScrittureContabili(
                    new TestUserContext(),
                    missioneBulk);

            //CONTROLLO ECONOMICA
            {
                Assertions.assertEquals(new BigDecimal("100.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getIm_scrittura).orElse(null));
                Assertions.assertEquals(new BigDecimal("100.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getImTotaleDare).orElse(null));
                Assertions.assertEquals(new BigDecimal("100.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getImTotaleAvere).orElse(null));

                BulkList<Movimento_cogeBulk> movimentiDare = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(1, movimentiDare.size());

                Optional<Movimento_cogeBulk> rigaDare = movimentiDare.stream().findAny();
                Assertions.assertTrue(rigaDare.isPresent(),"Riga dare non presente.");
                Assertions.assertEquals("C13024", rigaDare.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                Assertions.assertEquals(new BigDecimal("100.00"), rigaDare.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

                BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(1, movimentiAvere.size());

                Optional<Movimento_cogeBulk> rigaAvere = movimentiAvere.stream().findAny();
                Assertions.assertTrue(rigaAvere.isPresent(),"Riga avere non presente.");
                Assertions.assertEquals("P13024", rigaAvere.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                Assertions.assertEquals(new BigDecimal("100.00"), rigaAvere.map(Movimento_cogeBulk::getIm_movimento).orElse(null));
            }
            //CONTROLLO ANALITICA
            {
                Assertions.assertEquals(new BigDecimal("100.00"), Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getIm_scrittura).orElse(null));
                Assertions.assertEquals(new BigDecimal("100.00"), Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getImTotaleDare).orElse(null));
                Assertions.assertEquals(new BigDecimal("0"), Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getImTotaleAvere).orElse(null));

                List<Movimento_coanBulk> movimentiDare = Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getMovimentiDareColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(1, movimentiDare.size());

                Optional<Movimento_coanBulk> rigaDare1 = movimentiDare.stream()
                        .filter(el->"PTEST001".equals(el.getCd_linea_attivita()))
                        .findAny();
                Assertions.assertTrue(rigaDare1.isPresent(),"Riga dare analitica su GAE PTEST001 non presente.");
                Assertions.assertEquals("C13024", rigaDare1.map(Movimento_coanBulk::getCd_voce_ana).orElse(null));
                Assertions.assertEquals("000.000.000", rigaDare1.map(Movimento_coanBulk::getCd_centro_responsabilita).orElse(null));
                Assertions.assertEquals("PTEST001", rigaDare1.map(Movimento_coanBulk::getCd_linea_attivita).orElse(null));
                Assertions.assertEquals(new BigDecimal("100.00"), rigaDare1.map(Movimento_coanBulk::getIm_movimento).orElse(null));

                List<Movimento_coanBulk> movimentiAvere = Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getMovimentiAvereColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(0, movimentiAvere.size());
            }

            //Richiamo l'anticipo perchè aggiunto valore voce_ep su testata
            missioneBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(new TestUserContext(),
                            new MissioneBulk("000", "000.000", 2025, 1L)))
                    .filter(MissioneBulk.class::isInstance)
                    .map(MissioneBulk.class::cast)
                    .orElse(null);
            Utility.createScritturaPartitaDoppiaFromDocumentoComponentSession().modificaConBulk(new TestUserContext(), missioneBulk);
        }
        {
            MandatoBulk mandatoBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(new TestUserContext(),
                            new MandatoIBulk("000",2025,13L)))
                    .filter(MandatoBulk.class::isInstance)
                    .map(MandatoBulk.class::cast)
                    .orElse(null);

            ResultScrittureContabili result = Utility.createProposeScritturaComponentSession().proposeScrittureContabili(
                    new TestUserContext(),
                    mandatoBulk);

            Assertions.assertEquals(new BigDecimal("100.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getIm_scrittura).orElse(null));
            Assertions.assertEquals(new BigDecimal("100.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getImTotaleDare).orElse(null));
            Assertions.assertEquals(new BigDecimal("100.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getImTotaleAvere).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiDare = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                    .orElse(new BulkList<>());
            Assertions.assertEquals(1, movimentiDare.size());

            Optional<Movimento_cogeBulk> rigaTipoDebito = movimentiDare.stream().filter(Movimento_cogeBulk::isRigaTipoDebito).findAny();
            Assertions.assertTrue(rigaTipoDebito.isPresent(),"Riga tipo debito non presente.");
            Assertions.assertEquals("P13024", rigaTipoDebito.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            Assertions.assertEquals(new BigDecimal("100.00"), rigaTipoDebito.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                    .orElse(new BulkList<>());
            Assertions.assertEquals(1, movimentiAvere.size());

            Optional<Movimento_cogeBulk> rigaTipoTesoreria = movimentiAvere.stream().filter(Movimento_cogeBulk::isRigaTipoTesoreria).findAny();
            Assertions.assertTrue(rigaTipoTesoreria.isPresent(),"Riga tipo tesoreria non presente.");
            Assertions.assertEquals("A00053", rigaTipoTesoreria.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            Assertions.assertEquals(new BigDecimal("100.00"), rigaTipoTesoreria.map(Movimento_cogeBulk::getIm_movimento).orElse(null));
        }
    }

    /**
     *
     * <p><b>Dati Anticipo</b>
     * <pre>
     * Voce Bilancio: 13024 - Prodotti chimici
     * CDR/GAE: 000.000.000/PTEST001
     * Importo: 100,00
     * </pre></p>
     * <b>Scrittura Economica Anticipo</b>
     * <pre>
     *     Sezione   Importo      Conto
     *        D      100,00       A71001 - Crediti diversi derivanti da anticipazioni di cassa
     *        A      100,00       P71001 - Debiti per anticipazioni
     * </pre>
     * <b>Scrittura Analitica Anticipo</b>
     * <pre>
     *     Scrittura da non generare
     * </pre>
     * <b>Scrittura Economica Mandato Anticipo</b>
     * <pre>
     *     Sezione   Importo      Conto
     *        D      100,00       P71001 - Debiti per anticipazioni
     *        A      100,00       A00053 - Istituto tesoriere/cassiere
     * </pre>
     *
     * <p><b>Dati Missione legata ad Anticipo</b>
     * <pre>
     * Voce Bilancio: 13024 - Prodotti chimici
     * CDR/GAE: 000.000.000/PTEST002
     * Importo Missione: 300,00
     *
     * <b>Scrittura Economica Missione</b>
     * <pre>
     *     Sezione   Importo      Conto
     *        D      300,00       C13024 - Prodotti chimici
     *        D      100,00       P13024 - Debiti verso fornitori - Prodotti chimici
     *        A      300,00       P13024 - Debiti verso fornitori - Prodotti chimici
     *        A      100,00       A71001 - Crediti diversi derivanti da anticipazioni di cassa
     * </pre>
     * <b>Scrittura Analitica Missione</b>
     * <pre>
     *     Sezione   Importo      CDR/GAE                   Conto Analitico
     *        D      100,00       000.000.000/PTEST001      C13024 - Prodotti chimici
     *        D      200,00       000.000.000/PTEST002      C13024 - Prodotti chimici
     * </pre>
     *
     * <b>Scrittura Economica Mandato Missione</b>
     * <pre>
     *     Sezione   Importo      Conto
     *        D      200,00       P13024 - Debiti verso fornitori - Prodotti chimici
     *        A      200,00       A00053 - Istituto tesoriere/cassiere
     * </pre>
     */
    @Test
    @Order(2)
    public void testMissioneConAnticipo001() throws Exception {
        {
            AnticipoBulk anticipoBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(new TestUserContext(),
                            new AnticipoBulk("000", "000.000", 2025, 1L)))
                    .filter(AnticipoBulk.class::isInstance)
                    .map(AnticipoBulk.class::cast)
                    .orElse(null);
            ResultScrittureContabili result = Utility.createProposeScritturaComponentSession().proposeScrittureContabili(
                    new TestUserContext(),
                    anticipoBulk);

            //CONTROLLO ECONOMICA
            {
                Assertions.assertEquals(new BigDecimal("100.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getIm_scrittura).orElse(null));
                Assertions.assertEquals(new BigDecimal("100.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getImTotaleDare).orElse(null));
                Assertions.assertEquals(new BigDecimal("100.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getImTotaleAvere).orElse(null));

                BulkList<Movimento_cogeBulk> movimentiDare = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(1, movimentiDare.size());

                Optional<Movimento_cogeBulk> rigaDare = movimentiDare.stream().findAny();
                Assertions.assertTrue(rigaDare.isPresent(),"Riga dare non presente.");
                Assertions.assertEquals("A71001", rigaDare.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                Assertions.assertEquals(new BigDecimal("100.00"), rigaDare.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

                BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(1, movimentiAvere.size());

                Optional<Movimento_cogeBulk> rigaAvere = movimentiAvere.stream().findAny();
                Assertions.assertTrue(rigaAvere.isPresent(),"Riga avere non presente.");
                Assertions.assertEquals("P71001", rigaAvere.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                Assertions.assertEquals(new BigDecimal("100.00"), rigaAvere.map(Movimento_cogeBulk::getIm_movimento).orElse(null));
            }

            //CONTROLLO ANALITICA
            {
                Assertions.assertFalse(Optional.ofNullable(result.getScritturaAnaliticaBulk()).isPresent(),"Scrittura analitica presente.");
            }

            //Richiamo l'anticipo perchè aggiunto valore voce_ep su testata
            anticipoBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(new TestUserContext(),
                            new AnticipoBulk("000", "000.000", 2025, 1L)))
                    .filter(AnticipoBulk.class::isInstance)
                    .map(AnticipoBulk.class::cast)
                    .orElse(null);
            Utility.createScritturaPartitaDoppiaFromDocumentoComponentSession().modificaConBulk(new TestUserContext(), anticipoBulk);
        }
        {
            MandatoBulk mandatoBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(new TestUserContext(),
                            new MandatoIBulk("000",2025,12L)))
                    .filter(MandatoBulk.class::isInstance)
                    .map(MandatoBulk.class::cast)
                    .orElse(null);

            ResultScrittureContabili result = Utility.createProposeScritturaComponentSession().proposeScrittureContabili(
                    new TestUserContext(),
                    mandatoBulk);

            Assertions.assertEquals(new BigDecimal("100.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getIm_scrittura).orElse(null));
            Assertions.assertEquals(new BigDecimal("100.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getImTotaleDare).orElse(null));
            Assertions.assertEquals(new BigDecimal("100.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getImTotaleAvere).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiDare = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                    .orElse(new BulkList<>());
            Assertions.assertEquals(1, movimentiDare.size());

            Optional<Movimento_cogeBulk> rigaTipoDebito = movimentiDare.stream().filter(Movimento_cogeBulk::isRigaTipoDebito).findAny();
            Assertions.assertTrue(rigaTipoDebito.isPresent(),"Riga tipo debito non presente.");
            Assertions.assertEquals("P71001", rigaTipoDebito.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            Assertions.assertEquals(new BigDecimal("100.00"), rigaTipoDebito.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                    .orElse(new BulkList<>());
            Assertions.assertEquals(1, movimentiAvere.size());

            Optional<Movimento_cogeBulk> rigaTipoTesoreria = movimentiAvere.stream().filter(Movimento_cogeBulk::isRigaTipoTesoreria).findAny();
            Assertions.assertTrue(rigaTipoTesoreria.isPresent(),"Riga tipo tesoreria non presente.");
            Assertions.assertEquals("A00053", rigaTipoTesoreria.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            Assertions.assertEquals(new BigDecimal("100.00"), rigaTipoTesoreria.map(Movimento_cogeBulk::getIm_movimento).orElse(null));
        }
        {
            MissioneBulk missioneBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(new TestUserContext(),
                            new MissioneBulk("000", "000.000", 2025, 2L)))
                    .filter(MissioneBulk.class::isInstance)
                    .map(MissioneBulk.class::cast)
                    .orElse(null);
            ResultScrittureContabili result = Utility.createProposeScritturaComponentSession().proposeScrittureContabili(
                    new TestUserContext(),
                    missioneBulk);
            //CONTROLLO ECONOMICA
            {
                Assertions.assertEquals(new BigDecimal("400.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getIm_scrittura).orElse(null));
                Assertions.assertEquals(new BigDecimal("400.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getImTotaleDare).orElse(null));
                Assertions.assertEquals(new BigDecimal("400.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getImTotaleAvere).orElse(null));

                BulkList<Movimento_cogeBulk> movimentiDare = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(2, movimentiDare.size());

                Optional<Movimento_cogeBulk> rigaDare1 = movimentiDare.stream().filter(Movimento_cogeBulk::isRigaTipoCosto).findAny();
                Assertions.assertTrue(rigaDare1.isPresent(),"Riga dare di tipo costo non presente.");
                Assertions.assertEquals("C13024", rigaDare1.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                Assertions.assertEquals(new BigDecimal("300.00"), rigaDare1.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

                Optional<Movimento_cogeBulk> rigaDare2 = movimentiDare.stream().filter(Movimento_cogeBulk::isRigaTipoDebito).findAny();
                Assertions.assertTrue(rigaDare2.isPresent(),"Riga dare di tipo debito non presente.");
                Assertions.assertEquals("P13024", rigaDare2.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                Assertions.assertEquals(new BigDecimal("100.00"), rigaDare2.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

                BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(2, movimentiAvere.size());

                Optional<Movimento_cogeBulk> rigaAvere1 = movimentiAvere.stream().filter(Movimento_cogeBulk::isRigaTipoCredito).findAny();
                Assertions.assertTrue(rigaAvere1.isPresent(),"Riga avere di tipo credito non presente.");
                Assertions.assertEquals("A71001", rigaAvere1.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                Assertions.assertEquals(new BigDecimal("100.00"), rigaAvere1.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

                Optional<Movimento_cogeBulk> rigaAvere2 = movimentiAvere.stream().filter(Movimento_cogeBulk::isRigaTipoDebito).findAny();
                Assertions.assertTrue(rigaAvere2.isPresent(),"Riga avere di tipo debito non presente.");
                Assertions.assertEquals("P13024", rigaAvere2.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                Assertions.assertEquals(new BigDecimal("300.00"), rigaAvere2.map(Movimento_cogeBulk::getIm_movimento).orElse(null));
            }
            //CONTROLLO ANALITICA
            {
                Assertions.assertEquals(new BigDecimal("300.00"), Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getIm_scrittura).orElse(null));
                Assertions.assertEquals(new BigDecimal("300.00"), Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getImTotaleDare).orElse(null));
                Assertions.assertEquals(new BigDecimal("0"), Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getImTotaleAvere).orElse(null));

                List<Movimento_coanBulk> movimentiDare = Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getMovimentiDareColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(2, movimentiDare.size());

                Optional<Movimento_coanBulk> rigaDare1 = movimentiDare.stream()
                        .filter(el->"PTEST001".equals(el.getCd_linea_attivita()))
                        .findAny();
                Assertions.assertTrue(rigaDare1.isPresent(),"Riga dare analitica su GAE PTEST001 non presente.");
                Assertions.assertEquals("C13024", rigaDare1.map(Movimento_coanBulk::getCd_voce_ana).orElse(null));
                Assertions.assertEquals("000.000.000", rigaDare1.map(Movimento_coanBulk::getCd_centro_responsabilita).orElse(null));
                Assertions.assertEquals("PTEST001", rigaDare1.map(Movimento_coanBulk::getCd_linea_attivita).orElse(null));
                Assertions.assertEquals(new BigDecimal("100.00"), rigaDare1.map(Movimento_coanBulk::getIm_movimento).orElse(null));

                Optional<Movimento_coanBulk> rigaDare2 = movimentiDare.stream()
                        .filter(el->"PTEST002".equals(el.getCd_linea_attivita()))
                        .findAny();
                Assertions.assertTrue(rigaDare2.isPresent(),"Riga dare analitica su GAE PTEST002 non presente.");
                Assertions.assertEquals("C13024", rigaDare2.map(Movimento_coanBulk::getCd_voce_ana).orElse(null));
                Assertions.assertEquals("000.000.000", rigaDare2.map(Movimento_coanBulk::getCd_centro_responsabilita).orElse(null));
                Assertions.assertEquals("PTEST002", rigaDare2.map(Movimento_coanBulk::getCd_linea_attivita).orElse(null));
                Assertions.assertEquals(new BigDecimal("200.00"), rigaDare2.map(Movimento_coanBulk::getIm_movimento).orElse(null));

                List<Movimento_coanBulk> movimentiAvere = Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getMovimentiAvereColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(0, movimentiAvere.size());
            }

            //Richiamo l'anticipo perchè aggiunto valore voce_ep su testata
            missioneBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(new TestUserContext(),
                            new MissioneBulk("000", "000.000", 2025, 1L)))
                    .filter(MissioneBulk.class::isInstance)
                    .map(MissioneBulk.class::cast)
                    .orElse(null);
            Utility.createScritturaPartitaDoppiaFromDocumentoComponentSession().modificaConBulk(new TestUserContext(), missioneBulk);
        }
        {
            MandatoBulk mandatoBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(new TestUserContext(),
                            new MandatoIBulk("000",2025,14L)))
                    .filter(MandatoBulk.class::isInstance)
                    .map(MandatoBulk.class::cast)
                    .orElse(null);

            ResultScrittureContabili result = Utility.createProposeScritturaComponentSession().proposeScrittureContabili(
                    new TestUserContext(),
                    mandatoBulk);

            Assertions.assertEquals(new BigDecimal("200.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getIm_scrittura).orElse(null));
            Assertions.assertEquals(new BigDecimal("200.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getImTotaleDare).orElse(null));
            Assertions.assertEquals(new BigDecimal("200.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getImTotaleAvere).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiDare = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                    .orElse(new BulkList<>());
            Assertions.assertEquals(1, movimentiDare.size());

            Optional<Movimento_cogeBulk> rigaTipoDebito = movimentiDare.stream().filter(Movimento_cogeBulk::isRigaTipoDebito).findAny();
            Assertions.assertTrue(rigaTipoDebito.isPresent(),"Riga tipo debito non presente.");
            Assertions.assertEquals("P13024", rigaTipoDebito.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            Assertions.assertEquals(new BigDecimal("200.00"), rigaTipoDebito.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                    .orElse(new BulkList<>());
            Assertions.assertEquals(1, movimentiAvere.size());

            Optional<Movimento_cogeBulk> rigaTipoTesoreria = movimentiAvere.stream().filter(Movimento_cogeBulk::isRigaTipoTesoreria).findAny();
            Assertions.assertTrue(rigaTipoTesoreria.isPresent(),"Riga tipo tesoreria non presente.");
            Assertions.assertEquals("A00053", rigaTipoTesoreria.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            Assertions.assertEquals(new BigDecimal("200.00"), rigaTipoTesoreria.map(Movimento_cogeBulk::getIm_movimento).orElse(null));
        }
    }

    /**
     * <b>Missione con Anticipo e Rimborso</b>
     *
     * <p><b>Dati Anticipo</b>
     * <pre>
     * Voce Bilancio: 13024 - Prodotti chimici
     * CDR/GAE: 000.000.000/PTEST001
     * Importo: 300,00
     * </pre></p>
     * <b>Scrittura Economica Anticipo</b>
     * <pre>
     *     Sezione   Importo      Conto
     *        D      300,00       A71001 - Crediti diversi derivanti da anticipazioni di cassa
     *        A      300,00       P71001 - Debiti per anticipazioni
     * </pre>
     * <b>Scrittura Analitica Anticipo</b>
     * <pre>
     *     Scrittura da non generare
     * </pre>
     * <b>Scrittura Economica Mandato Anticipo</b>
     * <pre>
     *     Sezione   Importo      Conto
     *        D      300,00       P71001 - Debiti per anticipazioni
     *        A      300,00       A00053 - Istituto tesoriere/cassiere
     * </pre>
     *
     * <p><b>Dati Missione legata ad Anticipo</b>
     * <pre>
     * Voce Bilancio: Nessun impegno collegato
     * CDR/GAE: Nessun impegno collegato
     * Importo Missione: 200,00
     *
     * <b>Scrittura Economica Missione</b>
     * <pre>
     *     Sezione   Importo      Conto
     *        D      200,00       C13024 - Prodotti chimici
     *        D      200,00       P13024 - Debiti verso fornitori - Prodotti chimici
     *        A      200,00       P13024 - Debiti verso fornitori - Prodotti chimici
     *        A      200,00       A71001 - Crediti diversi derivanti da anticipazioni di cassa
     * </pre>
     * <b>Scrittura Analitica Missione</b>
     * <pre>
     *     Sezione   Importo      CDR/GAE                   Conto Analitico
     *        D      200,00       000.000.000/PTEST001      C13024 - Prodotti chimici
     * </pre>
     *
     */
    @Test
    @Order(3)
    public void testMissioneConAnticipoRimborso002() throws Exception {
        {
            AnticipoBulk anticipoBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(new TestUserContext(),
                            new AnticipoBulk("000", "000.000", 2025, 2L)))
                    .filter(AnticipoBulk.class::isInstance)
                    .map(AnticipoBulk.class::cast)
                    .orElse(null);
            ResultScrittureContabili result = Utility.createProposeScritturaComponentSession().proposeScrittureContabili(
                    new TestUserContext(),
                    anticipoBulk);

            //CONTROLLO ECONOMICA
            {
                Assertions.assertEquals(new BigDecimal("300.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getIm_scrittura).orElse(null));
                Assertions.assertEquals(new BigDecimal("300.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getImTotaleDare).orElse(null));
                Assertions.assertEquals(new BigDecimal("300.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getImTotaleAvere).orElse(null));

                BulkList<Movimento_cogeBulk> movimentiDare = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(1, movimentiDare.size());

                Optional<Movimento_cogeBulk> rigaDare = movimentiDare.stream().findAny();
                Assertions.assertTrue(rigaDare.isPresent(),"Riga dare non presente.");
                Assertions.assertEquals("A71001", rigaDare.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                Assertions.assertEquals(new BigDecimal("300.00"), rigaDare.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

                BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(1, movimentiAvere.size());

                Optional<Movimento_cogeBulk> rigaAvere = movimentiAvere.stream().findAny();
                Assertions.assertTrue(rigaAvere.isPresent(),"Riga avere non presente.");
                Assertions.assertEquals("P71001", rigaAvere.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                Assertions.assertEquals(new BigDecimal("300.00"), rigaAvere.map(Movimento_cogeBulk::getIm_movimento).orElse(null));
            }
            //CONTROLLO ANALITICA
            {
                Assertions.assertFalse(Optional.ofNullable(result.getScritturaAnaliticaBulk()).isPresent(),"Scrittura analitica presente.");
            }

            //Richiamo l'anticipo perchè aggiunto valore voce_ep su testata
            anticipoBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(new TestUserContext(),
                            new AnticipoBulk("000", "000.000", 2025, 2L)))
                    .filter(AnticipoBulk.class::isInstance)
                    .map(AnticipoBulk.class::cast)
                    .orElse(null);
            Utility.createScritturaPartitaDoppiaFromDocumentoComponentSession().modificaConBulk(new TestUserContext(), anticipoBulk);
        }
        {
            MandatoBulk mandatoBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(new TestUserContext(),
                            new MandatoIBulk("000",2025,15L)))
                    .filter(MandatoBulk.class::isInstance)
                    .map(MandatoBulk.class::cast)
                    .orElse(null);

            ResultScrittureContabili result = Utility.createProposeScritturaComponentSession().proposeScrittureContabili(
                    new TestUserContext(),
                    mandatoBulk);

            Assertions.assertEquals(new BigDecimal("300.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getIm_scrittura).orElse(null));
            Assertions.assertEquals(new BigDecimal("300.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getImTotaleDare).orElse(null));
            Assertions.assertEquals(new BigDecimal("300.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getImTotaleAvere).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiDare = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                    .orElse(new BulkList<>());
            Assertions.assertEquals(1, movimentiDare.size());

            Optional<Movimento_cogeBulk> rigaTipoDebito = movimentiDare.stream().filter(Movimento_cogeBulk::isRigaTipoDebito).findAny();
            Assertions.assertTrue(rigaTipoDebito.isPresent(),"Riga tipo debito non presente.");
            Assertions.assertEquals("P71001", rigaTipoDebito.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            Assertions.assertEquals(new BigDecimal("300.00"), rigaTipoDebito.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                    .orElse(new BulkList<>());
            Assertions.assertEquals(1, movimentiAvere.size());

            Optional<Movimento_cogeBulk> rigaTipoTesoreria = movimentiAvere.stream().filter(Movimento_cogeBulk::isRigaTipoTesoreria).findAny();
            Assertions.assertTrue(rigaTipoTesoreria.isPresent(),"Riga tipo tesoreria non presente.");
            Assertions.assertEquals("A00053", rigaTipoTesoreria.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            Assertions.assertEquals(new BigDecimal("300.00"), rigaTipoTesoreria.map(Movimento_cogeBulk::getIm_movimento).orElse(null));
        }
        {
            MissioneBulk missioneBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(new TestUserContext(),
                            new MissioneBulk("000", "000.000", 2025, 3L)))
                    .filter(MissioneBulk.class::isInstance)
                    .map(MissioneBulk.class::cast)
                    .orElse(null);
            ResultScrittureContabili result = Utility.createProposeScritturaComponentSession().proposeScrittureContabili(
                    new TestUserContext(),
                    missioneBulk);
            //CONTROLLO ECONOMICA
            {
                Assertions.assertEquals(new BigDecimal("400.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getIm_scrittura).orElse(null));
                Assertions.assertEquals(new BigDecimal("400.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getImTotaleDare).orElse(null));
                Assertions.assertEquals(new BigDecimal("400.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getImTotaleAvere).orElse(null));

                BulkList<Movimento_cogeBulk> movimentiDare = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(2, movimentiDare.size());

                Optional<Movimento_cogeBulk> rigaDare1 = movimentiDare.stream().filter(Movimento_cogeBulk::isRigaTipoCosto).findAny();
                Assertions.assertTrue(rigaDare1.isPresent(),"Riga dare di tipo costo non presente.");
                Assertions.assertEquals("C13024", rigaDare1.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                Assertions.assertEquals(new BigDecimal("200.00"), rigaDare1.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

                Optional<Movimento_cogeBulk> rigaDare2 = movimentiDare.stream().filter(Movimento_cogeBulk::isRigaTipoDebito).findAny();
                Assertions.assertTrue(rigaDare2.isPresent(),"Riga dare di tipo debito non presente.");
                Assertions.assertEquals("P13024", rigaDare2.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                Assertions.assertEquals(new BigDecimal("200.00"), rigaDare2.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

                BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(2, movimentiAvere.size());

                Optional<Movimento_cogeBulk> rigaAvere1 = movimentiAvere.stream().filter(Movimento_cogeBulk::isRigaTipoCredito).findAny();
                Assertions.assertTrue(rigaAvere1.isPresent(),"Riga avere di tipo credito non presente.");
                Assertions.assertEquals("A71001", rigaAvere1.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                Assertions.assertEquals(new BigDecimal("200.00"), rigaAvere1.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

                Optional<Movimento_cogeBulk> rigaAvere2 = movimentiAvere.stream().filter(Movimento_cogeBulk::isRigaTipoDebito).findAny();
                Assertions.assertTrue(rigaAvere2.isPresent(),"Riga avere di tipo debito non presente.");
                Assertions.assertEquals("P13024", rigaAvere2.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                Assertions.assertEquals(new BigDecimal("200.00"), rigaAvere2.map(Movimento_cogeBulk::getIm_movimento).orElse(null));
            }
            //CONTROLLO ANALITICA
            {
                Assertions.assertEquals(new BigDecimal("200.00"), Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getIm_scrittura).orElse(null));
                Assertions.assertEquals(new BigDecimal("200.00"), Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getImTotaleDare).orElse(null));
                Assertions.assertEquals(new BigDecimal("0"), Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getImTotaleAvere).orElse(null));

                List<Movimento_coanBulk> movimentiDare = Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getMovimentiDareColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(1, movimentiDare.size());

                Optional<Movimento_coanBulk> rigaDare1 = movimentiDare.stream()
                        .filter(el->"PTEST001".equals(el.getCd_linea_attivita()))
                        .findAny();
                Assertions.assertTrue(rigaDare1.isPresent(),"Riga dare analitica su GAE PTEST001 non presente.");
                Assertions.assertEquals("C13024", rigaDare1.map(Movimento_coanBulk::getCd_voce_ana).orElse(null));
                Assertions.assertEquals("000.000.000", rigaDare1.map(Movimento_coanBulk::getCd_centro_responsabilita).orElse(null));
                Assertions.assertEquals("PTEST001", rigaDare1.map(Movimento_coanBulk::getCd_linea_attivita).orElse(null));
                Assertions.assertEquals(new BigDecimal("200.00"), rigaDare1.map(Movimento_coanBulk::getIm_movimento).orElse(null));

                List<Movimento_coanBulk> movimentiAvere = Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getMovimentiAvereColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(0, movimentiAvere.size());
            }
            //Richiamo l'anticipo perchè aggiunto valore voce_ep su testata
            missioneBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(new TestUserContext(),
                            new MissioneBulk("000", "000.000", 2025, 3L)))
                    .filter(MissioneBulk.class::isInstance)
                    .map(MissioneBulk.class::cast)
                    .orElse(null);
            Utility.createScritturaPartitaDoppiaFromDocumentoComponentSession().modificaConBulk(new TestUserContext(), missioneBulk);
        }
        /*
        {
            MandatoBulk mandatoBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(new TestUserContext(),
                            new MandatoIBulk("000",2025,16L)))
                    .filter(MandatoBulk.class::isInstance)
                    .map(MandatoBulk.class::cast)
                    .orElse(null);

            ResultScrittureContabili result = Utility.createProposeScritturaComponentSession().proposeScrittureContabili(
                    new TestUserContext(),
                    mandatoBulk);

            Assertions.assertEquals(new BigDecimal("200.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getIm_scrittura).orElse(null));
            Assertions.assertEquals(new BigDecimal("200.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getImTotaleDare).orElse(null));
            Assertions.assertEquals(new BigDecimal("200.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getImTotaleAvere).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiDare = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                    .orElse(new BulkList<>());
            Assertions.assertEquals(1, movimentiDare.size());

            Optional<Movimento_cogeBulk> rigaTipoDebito = movimentiDare.stream().filter(Movimento_cogeBulk::isRigaTipoDebito).findAny();
            Assertions.assertTrue("Riga tipo debito non presente.", rigaTipoDebito.isPresent());
            Assertions.assertEquals("P13024", rigaTipoDebito.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            Assertions.assertEquals(new BigDecimal("200.00"), rigaTipoDebito.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                    .orElse(new BulkList<>());
            Assertions.assertEquals(1, movimentiAvere.size());

            Optional<Movimento_cogeBulk> rigaTipoTesoreria = movimentiAvere.stream().filter(Movimento_cogeBulk::isRigaTipoTesoreria).findAny();
            Assertions.assertTrue("Riga tipo tesoreria non presente.", rigaTipoTesoreria.isPresent());
            Assertions.assertEquals("A00053", rigaTipoTesoreria.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            Assertions.assertEquals(new BigDecimal("200.00"), rigaTipoTesoreria.map(Movimento_cogeBulk::getIm_movimento).orElse(null));
        }
         */
    }

}

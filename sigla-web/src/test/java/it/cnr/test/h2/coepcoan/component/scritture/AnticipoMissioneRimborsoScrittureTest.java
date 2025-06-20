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

import it.cnr.contab.coepcoan00.core.bulk.Movimento_cogeBulk;
import it.cnr.contab.coepcoan00.core.bulk.ResultScrittureContabili;
import it.cnr.contab.coepcoan00.core.bulk.Scrittura_partita_doppiaBulk;
import it.cnr.contab.doccont00.core.bulk.MandatoBulk;
import it.cnr.contab.doccont00.core.bulk.MandatoIBulk;
import it.cnr.contab.missioni00.docs.bulk.AnticipoBulk;
import it.cnr.contab.missioni00.docs.bulk.MissioneBulk;
import it.cnr.contab.util.Utility;
import it.cnr.jada.bulk.BulkList;
import it.cnr.jada.ejb.CRUDComponentSession;
import it.cnr.test.h2.DeploymentsH2;
import it.cnr.test.util.TestUserContext;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Test;

import javax.ejb.EJB;
import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AnticipoMissioneRimborsoScrittureTest extends DeploymentsH2 {
    @EJB
    private CRUDComponentSession crudComponentSession;

    /**
     * Missione
     * <p><b>Dati Missione</b>
     * <pre>
     * Voce Bilancio: 13024 - Prodotti chimici
     * Importo: 100,00
     * </pre></p>
     * <b>Scrittura Economica Missione</b>
     * <pre>
     *     Sezione   Importo      Conto
     *        D      100,00       C13024 - Prodotti chimici
     *        A      100,00       P13024 - Debiti verso fornitori - Prodotti chimici
     * </pre>
     * <b>Scrittura Economica Mandato</b>
     * <pre>
     *     Sezione   Importo      Conto
     *        D      100,00       P13024 - Debiti verso fornitori - Prodotti chimici
     *        A      100,00       A00053 - Istituto tesoriere/cassiere
     * </pre>
     */
    @Test
    @OperateOnDeployment(TEST_H2)
    @InSequence(1)
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

            Optional<Movimento_cogeBulk> rigaDare = movimentiDare.stream().findAny();
            assertTrue("Riga dare non presente.", rigaDare.isPresent());
            assertEquals("C13024", rigaDare.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            assertEquals(new BigDecimal("100.00"), rigaDare.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                    .orElse(new BulkList<>());
            assertEquals(1, movimentiAvere.size());

            Optional<Movimento_cogeBulk> rigaAvere = movimentiAvere.stream().findAny();
            assertTrue("Riga avere non presente.", rigaAvere.isPresent());
            assertEquals("P13024", rigaAvere.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            assertEquals(new BigDecimal("100.00"), rigaAvere.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

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

            Optional<Movimento_cogeBulk> rigaTipoDebito = movimentiDare.stream().filter(Movimento_cogeBulk::isRigaTipoDebito).findAny();
            assertTrue("Riga tipo debito non presente.", rigaTipoDebito.isPresent());
            assertEquals("P13024", rigaTipoDebito.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            assertEquals(new BigDecimal("100.00"), rigaTipoDebito.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                    .orElse(new BulkList<>());
            assertEquals(1, movimentiAvere.size());

            Optional<Movimento_cogeBulk> rigaTipoTesoreria = movimentiAvere.stream().filter(Movimento_cogeBulk::isRigaTipoTesoreria).findAny();
            assertTrue("Riga tipo tesoreria non presente.", rigaTipoTesoreria.isPresent());
            assertEquals("A00053", rigaTipoTesoreria.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            assertEquals(new BigDecimal("100.00"), rigaTipoTesoreria.map(Movimento_cogeBulk::getIm_movimento).orElse(null));
        }
    }

    /**
     *
     * <p><b>Dati Anticipo</b>
     * <pre>
     * Voce Bilancio: 13024 - Prodotti chimici
     * Importo: 100,00
     * </pre></p>
     * <b>Scrittura Economica Anticipo</b>
     * <pre>
     *     Sezione   Importo      Conto
     *        D      100,00       A71001 - Crediti diversi derivanti da anticipazioni di cassa
     *        A      100,00       P71001 - Debiti per anticipazioni
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
     *
     * <b>Scrittura Economica Mandato Missione</b>
     * <pre>
     *     Sezione   Importo      Conto
     *        D      200,00       P13024 - Debiti verso fornitori - Prodotti chimici
     *        A      200,00       A00053 - Istituto tesoriere/cassiere
     * </pre>
     */
    @Test
    @OperateOnDeployment(TEST_H2)
    @InSequence(2)
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

            Optional<Movimento_cogeBulk> rigaDare = movimentiDare.stream().findAny();
            assertTrue("Riga dare non presente.", rigaDare.isPresent());
            assertEquals("A71001", rigaDare.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            assertEquals(new BigDecimal("100.00"), rigaDare.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                    .orElse(new BulkList<>());
            assertEquals(1, movimentiAvere.size());

            Optional<Movimento_cogeBulk> rigaAvere = movimentiAvere.stream().findAny();
            assertTrue("Riga avere non presente.", rigaAvere.isPresent());
            assertEquals("P71001", rigaAvere.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            assertEquals(new BigDecimal("100.00"), rigaAvere.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

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

            Optional<Movimento_cogeBulk> rigaTipoDebito = movimentiDare.stream().filter(Movimento_cogeBulk::isRigaTipoDebito).findAny();
            assertTrue("Riga tipo debito non presente.", rigaTipoDebito.isPresent());
            assertEquals("P71001", rigaTipoDebito.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            assertEquals(new BigDecimal("100.00"), rigaTipoDebito.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                    .orElse(new BulkList<>());
            assertEquals(1, movimentiAvere.size());

            Optional<Movimento_cogeBulk> rigaTipoTesoreria = movimentiAvere.stream().filter(Movimento_cogeBulk::isRigaTipoTesoreria).findAny();
            assertTrue("Riga tipo tesoreria non presente.", rigaTipoTesoreria.isPresent());
            assertEquals("A00053", rigaTipoTesoreria.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            assertEquals(new BigDecimal("100.00"), rigaTipoTesoreria.map(Movimento_cogeBulk::getIm_movimento).orElse(null));
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
            assertEquals(new BigDecimal("400.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getIm_scrittura).orElse(null));
            assertEquals(new BigDecimal("400.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getImTotaleDare).orElse(null));
            assertEquals(new BigDecimal("400.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getImTotaleAvere).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiDare = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                    .orElse(new BulkList<>());
            assertEquals(2, movimentiDare.size());

            Optional<Movimento_cogeBulk> rigaDare1 = movimentiDare.stream().filter(Movimento_cogeBulk::isRigaTipoCosto).findAny();
            assertTrue("Riga dare di tipo costo non presente.", rigaDare1.isPresent());
            assertEquals("C13024", rigaDare1.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            assertEquals(new BigDecimal("300.00"), rigaDare1.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            Optional<Movimento_cogeBulk> rigaDare2 = movimentiDare.stream().filter(Movimento_cogeBulk::isRigaTipoDebito).findAny();
            assertTrue("Riga dare di tipo debito non presente.", rigaDare2.isPresent());
            assertEquals("P13024", rigaDare2.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            assertEquals(new BigDecimal("100.00"), rigaDare2.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                    .orElse(new BulkList<>());
            assertEquals(2, movimentiAvere.size());

            Optional<Movimento_cogeBulk> rigaAvere1 = movimentiAvere.stream().filter(Movimento_cogeBulk::isRigaTipoCredito).findAny();
            assertTrue("Riga avere di tipo credito non presente.", rigaAvere1.isPresent());
            assertEquals("A71001", rigaAvere1.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            assertEquals(new BigDecimal("100.00"), rigaAvere1.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            Optional<Movimento_cogeBulk> rigaAvere2 = movimentiAvere.stream().filter(Movimento_cogeBulk::isRigaTipoDebito).findAny();
            assertTrue("Riga avere di tipo debito non presente.", rigaAvere2.isPresent());
            assertEquals("P13024", rigaAvere2.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            assertEquals(new BigDecimal("300.00"), rigaAvere2.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

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

            assertEquals(new BigDecimal("200.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getIm_scrittura).orElse(null));
            assertEquals(new BigDecimal("200.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getImTotaleDare).orElse(null));
            assertEquals(new BigDecimal("200.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getImTotaleAvere).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiDare = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                    .orElse(new BulkList<>());
            assertEquals(1, movimentiDare.size());

            Optional<Movimento_cogeBulk> rigaTipoDebito = movimentiDare.stream().filter(Movimento_cogeBulk::isRigaTipoDebito).findAny();
            assertTrue("Riga tipo debito non presente.", rigaTipoDebito.isPresent());
            assertEquals("P13024", rigaTipoDebito.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            assertEquals(new BigDecimal("200.00"), rigaTipoDebito.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                    .orElse(new BulkList<>());
            assertEquals(1, movimentiAvere.size());

            Optional<Movimento_cogeBulk> rigaTipoTesoreria = movimentiAvere.stream().filter(Movimento_cogeBulk::isRigaTipoTesoreria).findAny();
            assertTrue("Riga tipo tesoreria non presente.", rigaTipoTesoreria.isPresent());
            assertEquals("A00053", rigaTipoTesoreria.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            assertEquals(new BigDecimal("200.00"), rigaTipoTesoreria.map(Movimento_cogeBulk::getIm_movimento).orElse(null));
        }
    }

    /**
     * <b>Missione con Anticipo e Rimborso</b>
     *
     * <p><b>Dati Anticipo</b>
     * <pre>
     * Voce Bilancio: 13024 - Prodotti chimici
     * Importo: 300,00
     * </pre></p>
     * <b>Scrittura Economica Anticipo</b>
     * <pre>
     *     Sezione   Importo      Conto
     *        D      300,00       A71001 - Crediti diversi derivanti da anticipazioni di cassa
     *        A      300,00       P71001 - Debiti per anticipazioni
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
     * Voce Bilancio: 13024 - Prodotti chimici
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
     *
     */
    @Test
    @OperateOnDeployment(TEST_H2)
    @InSequence(3)
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
            assertEquals(new BigDecimal("300.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getIm_scrittura).orElse(null));
            assertEquals(new BigDecimal("300.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getImTotaleDare).orElse(null));
            assertEquals(new BigDecimal("300.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getImTotaleAvere).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiDare = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                    .orElse(new BulkList<>());
            assertEquals(1, movimentiDare.size());

            Optional<Movimento_cogeBulk> rigaDare = movimentiDare.stream().findAny();
            assertTrue("Riga dare non presente.", rigaDare.isPresent());
            assertEquals("A71001", rigaDare.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            assertEquals(new BigDecimal("300.00"), rigaDare.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                    .orElse(new BulkList<>());
            assertEquals(1, movimentiAvere.size());

            Optional<Movimento_cogeBulk> rigaAvere = movimentiAvere.stream().findAny();
            assertTrue("Riga avere non presente.", rigaAvere.isPresent());
            assertEquals("P71001", rigaAvere.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            assertEquals(new BigDecimal("300.00"), rigaAvere.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

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

            assertEquals(new BigDecimal("300.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getIm_scrittura).orElse(null));
            assertEquals(new BigDecimal("300.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getImTotaleDare).orElse(null));
            assertEquals(new BigDecimal("300.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getImTotaleAvere).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiDare = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                    .orElse(new BulkList<>());
            assertEquals(1, movimentiDare.size());

            Optional<Movimento_cogeBulk> rigaTipoDebito = movimentiDare.stream().filter(Movimento_cogeBulk::isRigaTipoDebito).findAny();
            assertTrue("Riga tipo debito non presente.", rigaTipoDebito.isPresent());
            assertEquals("P71001", rigaTipoDebito.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            assertEquals(new BigDecimal("300.00"), rigaTipoDebito.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                    .orElse(new BulkList<>());
            assertEquals(1, movimentiAvere.size());

            Optional<Movimento_cogeBulk> rigaTipoTesoreria = movimentiAvere.stream().filter(Movimento_cogeBulk::isRigaTipoTesoreria).findAny();
            assertTrue("Riga tipo tesoreria non presente.", rigaTipoTesoreria.isPresent());
            assertEquals("A00053", rigaTipoTesoreria.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            assertEquals(new BigDecimal("300.00"), rigaTipoTesoreria.map(Movimento_cogeBulk::getIm_movimento).orElse(null));
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
            assertEquals(new BigDecimal("400.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getIm_scrittura).orElse(null));
            assertEquals(new BigDecimal("400.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getImTotaleDare).orElse(null));
            assertEquals(new BigDecimal("400.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getImTotaleAvere).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiDare = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                    .orElse(new BulkList<>());
            assertEquals(2, movimentiDare.size());

            Optional<Movimento_cogeBulk> rigaDare1 = movimentiDare.stream().filter(Movimento_cogeBulk::isRigaTipoCosto).findAny();
            assertTrue("Riga dare di tipo costo non presente.", rigaDare1.isPresent());
            assertEquals("C13024", rigaDare1.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            assertEquals(new BigDecimal("200.00"), rigaDare1.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            Optional<Movimento_cogeBulk> rigaDare2 = movimentiDare.stream().filter(Movimento_cogeBulk::isRigaTipoDebito).findAny();
            assertTrue("Riga dare di tipo debito non presente.", rigaDare2.isPresent());
            assertEquals("P13024", rigaDare2.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            assertEquals(new BigDecimal("200.00"), rigaDare2.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                    .orElse(new BulkList<>());
            assertEquals(2, movimentiAvere.size());

            Optional<Movimento_cogeBulk> rigaAvere1 = movimentiAvere.stream().filter(Movimento_cogeBulk::isRigaTipoCredito).findAny();
            assertTrue("Riga avere di tipo credito non presente.", rigaAvere1.isPresent());
            assertEquals("A71001", rigaAvere1.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            assertEquals(new BigDecimal("200.00"), rigaAvere1.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            Optional<Movimento_cogeBulk> rigaAvere2 = movimentiAvere.stream().filter(Movimento_cogeBulk::isRigaTipoDebito).findAny();
            assertTrue("Riga avere di tipo debito non presente.", rigaAvere2.isPresent());
            assertEquals("P13024", rigaAvere2.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            assertEquals(new BigDecimal("200.00"), rigaAvere2.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

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

            assertEquals(new BigDecimal("200.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getIm_scrittura).orElse(null));
            assertEquals(new BigDecimal("200.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getImTotaleDare).orElse(null));
            assertEquals(new BigDecimal("200.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getImTotaleAvere).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiDare = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                    .orElse(new BulkList<>());
            assertEquals(1, movimentiDare.size());

            Optional<Movimento_cogeBulk> rigaTipoDebito = movimentiDare.stream().filter(Movimento_cogeBulk::isRigaTipoDebito).findAny();
            assertTrue("Riga tipo debito non presente.", rigaTipoDebito.isPresent());
            assertEquals("P13024", rigaTipoDebito.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            assertEquals(new BigDecimal("200.00"), rigaTipoDebito.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                    .orElse(new BulkList<>());
            assertEquals(1, movimentiAvere.size());

            Optional<Movimento_cogeBulk> rigaTipoTesoreria = movimentiAvere.stream().filter(Movimento_cogeBulk::isRigaTipoTesoreria).findAny();
            assertTrue("Riga tipo tesoreria non presente.", rigaTipoTesoreria.isPresent());
            assertEquals("A00053", rigaTipoTesoreria.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            assertEquals(new BigDecimal("200.00"), rigaTipoTesoreria.map(Movimento_cogeBulk::getIm_movimento).orElse(null));
        }
         */
    }

}

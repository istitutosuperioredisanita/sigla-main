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
import it.cnr.contab.docamm00.docs.bulk.Fattura_passivaBulk;
import it.cnr.contab.docamm00.docs.bulk.Fattura_passiva_IBulk;
import it.cnr.contab.doccont00.core.bulk.MandatoBulk;
import it.cnr.contab.doccont00.core.bulk.MandatoIBulk;
import it.cnr.contab.util.Utility;
import it.cnr.jada.bulk.BulkList;
import it.cnr.jada.ejb.CRUDComponentSession;
import it.cnr.test.h2.DeploymentsH2;
import it.cnr.test.util.TestUserContext;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;
import org.jboss.arquillian.junit.InSequence;
import org.junit.Test;

import jakarta.ejb.EJB;
import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FatturaPassivaIstituzionaleScrittureTest extends DeploymentsH2 {
    @EJB
    private CRUDComponentSession crudComponentSession;

    /**
     * Fattura {@code Istituzionale} {@code Split Payment} su mono voce con mandato di pagamento:
     * <p><b>Dati Fattura</b>
     * <pre>
     * Voce Bilancio: 22010 - Attrezzature scientifiche
     * Imponibile:    18,67
     * Imposta:        1,70
     * </pre></p>
     * <pre>
     *     Sezione   Importo      Conto
     *        D       18,67       A22010  - Attrezzature scientifiche
     *        A       16,97       P22010  - Debiti verso fornitori per acquisto di
     *                                      attrezzature scientifiche
     *        A        1,70       P71012I - Debito per versamento delle ritenute
     *                                      per scissione contabile IVA COMMERCIALE
     *                                      (Split Payment)
     * </pre>
     * <b>Scrittura Economica Mandato</b>
     * <pre>
     *     Sezione   Importo      Conto
     *        D       16,97       P22010 - Debiti verso fornitori per acquisto di
     *                                     attrezzature scientifiche
     *        A       16,97       A00053 - Istituto tesoriere/cassiere
     * </pre>
     */
    @Test
    @OperateOnDeployment(TEST_H2)
    @InSequence(1)
    public void testIstituzionale001() throws Exception {
        //Registrazione fattura
        {
            Fattura_passivaBulk fatturaPassivaBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(new TestUserContext(),
                            new Fattura_passiva_IBulk("000", "000.000", 2025, 1L)))
                    .filter(Fattura_passivaBulk.class::isInstance)
                    .map(Fattura_passivaBulk.class::cast)
                    .orElse(null);

            ResultScrittureContabili result = Utility.createProposeScritturaComponentSession().proposeScrittureContabili(
                    new TestUserContext(),
                    fatturaPassivaBulk);
            assertEquals(new BigDecimal("18.67"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getIm_scrittura).orElse(null));
            assertEquals(new BigDecimal("18.67"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getImTotaleDare).orElse(null));
            assertEquals(new BigDecimal("18.67"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getImTotaleAvere).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiDare = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                    .orElse(new BulkList<>());
            assertEquals(1, movimentiDare.size());

            Optional<Movimento_cogeBulk> rigaTipoAttivita = movimentiDare.stream().filter(Movimento_cogeBulk::isRigaTipoAttivita).findAny();
            assertTrue("Riga tipo attivita non presente.", rigaTipoAttivita.isPresent());
            assertEquals("A22010", rigaTipoAttivita.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            assertEquals(new BigDecimal("18.67"), rigaTipoAttivita.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                    .orElse(new BulkList<>());
            assertEquals(2, movimentiAvere.size());

            Optional<Movimento_cogeBulk> rigaTipoDebito = movimentiAvere.stream().filter(Movimento_cogeBulk::isRigaTipoDebito).findAny();
            assertTrue("Riga tipo debito non presente.", rigaTipoDebito.isPresent());
            assertEquals("P22010", rigaTipoDebito.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            assertEquals(new BigDecimal("16.97"), rigaTipoDebito.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            Optional<Movimento_cogeBulk> rigaTipoIva = movimentiAvere.stream().filter(Movimento_cogeBulk::isRigaTipoIvaAcquistoSplit).findAny();
            assertTrue("Riga tipo iva non presente.", rigaTipoIva.isPresent());
            assertEquals("P71012I", rigaTipoIva.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            assertEquals(new BigDecimal("1.70"), rigaTipoIva.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            Utility.createScritturaPartitaDoppiaFromDocumentoComponentSession().modificaConBulk(new TestUserContext(), fatturaPassivaBulk);
        }
        //Registrazione mandato
        {
            MandatoBulk mandatoBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(new TestUserContext(),
                            new MandatoIBulk("000", 2025, 2L)))
                    .filter(MandatoBulk.class::isInstance)
                    .map(MandatoBulk.class::cast)
                    .orElse(null);

            ResultScrittureContabili result = Utility.createProposeScritturaComponentSession().proposeScrittureContabili(
                    new TestUserContext(),
                    mandatoBulk);

            assertEquals(new BigDecimal("16.97"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getIm_scrittura).orElse(null));
            assertEquals(new BigDecimal("16.97"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getImTotaleDare).orElse(null));
            assertEquals(new BigDecimal("16.97"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getImTotaleAvere).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiDare = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                    .orElse(new BulkList<>());
            assertEquals(1, movimentiDare.size());

            Optional<Movimento_cogeBulk> rigaTipoDebito = movimentiDare.stream().filter(Movimento_cogeBulk::isRigaTipoDebito).findAny();
            assertTrue("Riga tipo debito non presente.", rigaTipoDebito.isPresent());
            assertEquals("P22010", rigaTipoDebito.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            assertEquals(new BigDecimal("16.97"), rigaTipoDebito.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                    .orElse(new BulkList<>());
            assertEquals(1, movimentiAvere.size());

            Optional<Movimento_cogeBulk> rigaTipoTesoreria = movimentiAvere.stream().filter(Movimento_cogeBulk::isRigaTipoTesoreria).findAny();
            assertTrue("Riga tipo tesoreria non presente.", rigaTipoTesoreria.isPresent());
            assertEquals("A00053", rigaTipoTesoreria.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            assertEquals(new BigDecimal("16.97"), rigaTipoTesoreria.map(Movimento_cogeBulk::getIm_movimento).orElse(null));
        }
    }

    /**
     * Fattura {@code Istituzionale} {@code No Split Payment} su mono voce con mandato di pagamento:
     * <p><b>Dati Fattura</b>
     * <pre>
     * Voce Bilancio: 22010 - Attrezzature scientifiche
     * Imponibile:    18,67
     * Imposta:        0,00
     * </pre></p>
     * <b>Scrittura Economica Fattura</b>
     * <pre>
     *     Sezione   Importo      Conto
     *        D       18,67       A22010  - Attrezzature scientifiche
     *        A       18,67       P22010  - Debiti verso fornitori per acquisto di
     *                                      attrezzature scientifiche
     * </pre>
     * <b>Scrittura Economica Mandato</b>
     * <pre>
     *     Sezione   Importo      Conto
     *        D       18,67       P22010 - Debiti verso fornitori per acquisto di
     *                                     attrezzature scientifiche
     *        A       18,67       A00053 - Istituto tesoriere/cassiere
     * </pre>
     */
    @Test
    @OperateOnDeployment(TEST_H2)
    @InSequence(2)
    public void testIstituzionale002() throws Exception {
        //Registrazione fattura
        {
            Fattura_passivaBulk fatturaPassivaBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(new TestUserContext(),
                            new Fattura_passiva_IBulk("000", "000.000", 2025, 2L)))
                    .filter(Fattura_passivaBulk.class::isInstance)
                    .map(Fattura_passivaBulk.class::cast)
                    .orElse(null);

            ResultScrittureContabili result = Utility.createProposeScritturaComponentSession().proposeScrittureContabili(
                    new TestUserContext(),
                    fatturaPassivaBulk);
            assertEquals(new BigDecimal("18.67"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getIm_scrittura).orElse(null));
            assertEquals(new BigDecimal("18.67"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getImTotaleDare).orElse(null));
            assertEquals(new BigDecimal("18.67"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getImTotaleAvere).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiDare = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                    .orElse(new BulkList<>());
            assertEquals(1, movimentiDare.size());

            Optional<Movimento_cogeBulk> rigaTipoAttivita = movimentiDare.stream().filter(Movimento_cogeBulk::isRigaTipoAttivita).findAny();
            assertTrue("Riga tipo attivita non presente.", rigaTipoAttivita.isPresent());
            assertEquals("A22010", rigaTipoAttivita.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            assertEquals(new BigDecimal("18.67"), rigaTipoAttivita.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                    .orElse(new BulkList<>());
            assertEquals(1, movimentiAvere.size());

            Optional<Movimento_cogeBulk> rigaTipoDebito = movimentiAvere.stream().filter(Movimento_cogeBulk::isRigaTipoDebito).findAny();
            assertTrue("Riga tipo debito non presente.", rigaTipoDebito.isPresent());
            assertEquals("P22010", rigaTipoDebito.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            assertEquals(new BigDecimal("18.67"), rigaTipoDebito.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            Utility.createScritturaPartitaDoppiaFromDocumentoComponentSession().modificaConBulk(new TestUserContext(), fatturaPassivaBulk);
        }
        //Registrazione mandato
        {
            MandatoBulk mandatoBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(new TestUserContext(),
                            new MandatoIBulk("000", 2025, 3L)))
                    .filter(MandatoBulk.class::isInstance)
                    .map(MandatoBulk.class::cast)
                    .orElse(null);

            ResultScrittureContabili result = Utility.createProposeScritturaComponentSession().proposeScrittureContabili(
                    new TestUserContext(),
                    mandatoBulk);

            assertEquals(new BigDecimal("18.67"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getIm_scrittura).orElse(null));
            assertEquals(new BigDecimal("18.67"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getImTotaleDare).orElse(null));
            assertEquals(new BigDecimal("18.67"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getImTotaleAvere).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiDare = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                    .orElse(new BulkList<>());
            assertEquals(1, movimentiDare.size());

            Optional<Movimento_cogeBulk> rigaTipoDebito = movimentiDare.stream().filter(Movimento_cogeBulk::isRigaTipoDebito).findAny();
            assertTrue("Riga tipo debito non presente.", rigaTipoDebito.isPresent());
            assertEquals("P22010", rigaTipoDebito.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            assertEquals(new BigDecimal("18.67"), rigaTipoDebito.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                    .orElse(new BulkList<>());
            assertEquals(1, movimentiAvere.size());

            Optional<Movimento_cogeBulk> rigaTipoTesoreria = movimentiAvere.stream().filter(Movimento_cogeBulk::isRigaTipoTesoreria).findAny();
            assertTrue("Riga tipo tesoreria non presente.", rigaTipoTesoreria.isPresent());
            assertEquals("A00053", rigaTipoTesoreria.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            assertEquals(new BigDecimal("18.67"), rigaTipoTesoreria.map(Movimento_cogeBulk::getIm_movimento).orElse(null));
        }
    }

    /**
     * Fattura {@code Istituzionale} {@code No Split Payment} {@code No Ordini} {@code Bene Inventariabile}
     * su mono voce di 2 righe pagate con unico mandato di pagamento:
     * <p><b>Dati Fattura</b>
     * <pre>
     *     <b>Riga 1</b>
     *        Bene Inventariato: 19001 - VENTILATORI (INV.)
     *        Categoria Gruppo: 1.9001
     *        Conto Associato Gruppo: A22012 - Macchine per ufficio
     *        Voce Bilancio: 22010 - Attrezzature scientifiche
     *        Imponibile:    18,67
     *        Imposta:        1,70
     *     <b>Riga 2</b>
     *        Bene non Inventariato: BENE
     *        Voce Bilancio: 22010 - Attrezzature scientifiche
     *        Imponibile:    10,00
     *        Imposta:        0,00
     * </pre></p>
     * <b>Scrittura Economica Fattura</b>
     * <pre>
     *     Sezione   Importo      Conto
     *        D       18,67       A22012  - Macchine per ufficio
     *        D       10,00       A22010  - Attrezzature scientifiche
     *        A       16,97       P22012  - Debiti verso fornitori per acquisto di
     *                                      macchine per ufficio
     *        A       10,00       P22010  - Debiti verso fornitori per acquisto di
     *                                      attrezzature scientifiche
     *        A        1,70       P71012I - Debito per versamento delle ritenute
     *                                      per scissione contabile IVA COMMERCIALE
     *                                      (Split Payment)
     * </pre>
     * <b>Scrittura Economica Mandato</b>
     * <pre>
     *     Sezione   Importo      Conto
     *        D       16,97       P22012 - Debiti verso fornitori per acquisto di
     *                                     macchine per ufficio
     *        D       10,00       P22010 - Debiti verso fornitori per acquisto di
     *                                      attrezzature scientifiche
     *        A       26,97       A00053 - Istituto tesoriere/cassiere
     * </pre>
     */
    @Test
    @OperateOnDeployment(TEST_H2)
    @InSequence(3)
    public void testIstituzionale003() throws Exception {
        //Registrazione fattura
        {
            Fattura_passivaBulk fatturaPassivaBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(new TestUserContext(),
                            new Fattura_passiva_IBulk("000", "000.000", 2025, 9L)))
                    .filter(Fattura_passivaBulk.class::isInstance)
                    .map(Fattura_passivaBulk.class::cast)
                    .orElse(null);

            ResultScrittureContabili result = Utility.createProposeScritturaComponentSession().proposeScrittureContabili(
                    new TestUserContext(),
                    fatturaPassivaBulk);
            assertEquals(new BigDecimal("28.67"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getIm_scrittura).orElse(null));
            assertEquals(new BigDecimal("28.67"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getImTotaleDare).orElse(null));
            assertEquals(new BigDecimal("28.67"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getImTotaleAvere).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiDare = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                    .orElse(new BulkList<>());
            assertEquals(2, movimentiDare.size());

            Optional<Movimento_cogeBulk> rigaTipoAttivita1 = movimentiDare.stream().filter(el->"A22012".equals(el.getCd_voce_ep())).findAny();
            assertTrue("Conto A22012 non presente.", rigaTipoAttivita1.isPresent());
            assertTrue("Riga tipo attivita non presente.", rigaTipoAttivita1.filter(Movimento_cogeBulk::isRigaTipoAttivita).isPresent());
            assertEquals(new BigDecimal("18.67"), rigaTipoAttivita1.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            Optional<Movimento_cogeBulk> rigaTipoAttivita2 = movimentiDare.stream().filter(el->"A22010".equals(el.getCd_voce_ep())).findAny();
            assertTrue("Conto A22010 non presente.", rigaTipoAttivita2.isPresent());
            assertTrue("Riga tipo attivita non presente.", rigaTipoAttivita2.filter(Movimento_cogeBulk::isRigaTipoAttivita).isPresent());
            assertEquals(new BigDecimal("10.00"), rigaTipoAttivita2.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                    .orElse(new BulkList<>());
            assertEquals(3, movimentiAvere.size());

            Optional<Movimento_cogeBulk> rigaTipoDebito1 = movimentiAvere.stream().filter(el->"P22012".equals(el.getCd_voce_ep())).findAny();
            assertTrue("Conto P22012 non presente.", rigaTipoDebito1.isPresent());
            assertTrue("Riga tipo debito non presente.", rigaTipoDebito1.filter(Movimento_cogeBulk::isRigaTipoDebito).isPresent());
            assertEquals(new BigDecimal("16.97"), rigaTipoDebito1.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            Optional<Movimento_cogeBulk> rigaTipoDebito2 = movimentiAvere.stream().filter(el->"P22010".equals(el.getCd_voce_ep())).findAny();
            assertTrue("Conto P22010 non presente.", rigaTipoDebito2.isPresent());
            assertTrue("Riga tipo debito non presente.", rigaTipoDebito2.filter(Movimento_cogeBulk::isRigaTipoDebito).isPresent());
            assertEquals(new BigDecimal("10.00"), rigaTipoDebito2.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            Optional<Movimento_cogeBulk> rigaTipoIva = movimentiAvere.stream().filter(Movimento_cogeBulk::isRigaTipoIvaAcquistoSplit).findAny();
            assertTrue("Riga tipo iva non presente.", rigaTipoIva.isPresent());
            assertEquals("P71012I", rigaTipoIva.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            assertEquals(new BigDecimal("1.70"), rigaTipoIva.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            Utility.createScritturaPartitaDoppiaFromDocumentoComponentSession().modificaConBulk(new TestUserContext(), fatturaPassivaBulk);
        }
        //Registrazione mandato
        {
            MandatoBulk mandatoBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(new TestUserContext(),
                            new MandatoIBulk("000", 2025, 8L)))
                    .filter(MandatoBulk.class::isInstance)
                    .map(MandatoBulk.class::cast)
                    .orElse(null);

            ResultScrittureContabili result = Utility.createProposeScritturaComponentSession().proposeScrittureContabili(
                    new TestUserContext(),
                    mandatoBulk);

            assertEquals(new BigDecimal("26.97"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getIm_scrittura).orElse(null));
            assertEquals(new BigDecimal("26.97"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getImTotaleDare).orElse(null));
            assertEquals(new BigDecimal("26.97"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getImTotaleAvere).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiDare = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                    .orElse(new BulkList<>());
            assertEquals(2, movimentiDare.size());

            Optional<Movimento_cogeBulk> rigaTipoDebito1 = movimentiDare.stream().filter(el->"P22012".equals(el.getCd_voce_ep())).findAny();
            assertTrue("Conto P22012 non presente.", rigaTipoDebito1.isPresent());
            assertTrue("Riga tipo debito non presente.", rigaTipoDebito1.filter(Movimento_cogeBulk::isRigaTipoDebito).isPresent());
            assertEquals(new BigDecimal("16.97"), rigaTipoDebito1.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            Optional<Movimento_cogeBulk> rigaTipoDebito2 = movimentiDare.stream().filter(el->"P22010".equals(el.getCd_voce_ep())).findAny();
            assertTrue("Conto P22010 non presente.", rigaTipoDebito2.isPresent());
            assertTrue("Riga tipo debito non presente.", rigaTipoDebito2.filter(Movimento_cogeBulk::isRigaTipoDebito).isPresent());
            assertEquals(new BigDecimal("10.00"), rigaTipoDebito2.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

            BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                    .orElse(new BulkList<>());
            assertEquals(1, movimentiAvere.size());

            Optional<Movimento_cogeBulk> rigaTipoTesoreria = movimentiAvere.stream().filter(Movimento_cogeBulk::isRigaTipoTesoreria).findAny();
            assertTrue("Riga tipo tesoreria non presente.", rigaTipoTesoreria.isPresent());
            assertEquals("A00053", rigaTipoTesoreria.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
            assertEquals(new BigDecimal("26.97"), rigaTipoTesoreria.map(Movimento_cogeBulk::getIm_movimento).orElse(null));
        }
    }
}

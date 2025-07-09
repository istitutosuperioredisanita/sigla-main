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
import it.cnr.contab.compensi00.docs.bulk.CompensoBulk;
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

import javax.ejb.EJB;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class CompensoScrittureTest extends DeploymentsH2 {
    @EJB
    private CRUDComponentSession crudComponentSession;

    /**
     * Compenso occasionale
     * <p><b>Dati Compenso</b>
     * <pre>
     * Voce Bilancio: 13024 - Prodotti Chimici
     * CDR/GAE: 000.000.000/PTEST001
     * Importo: 500,00 - IRPEF: 100
     * </pre></p>
     * <b>Scrittura Economica Compenso</b>
     * <pre>
     *     Sezione   Importo      Conto
     *        D      500,00       C13024 - Prodotti chimici
     *        A      500,00       P13024 - Debiti verso fornitori - Prodotti chimici
     * </pre>
     * <b>Scrittura Analitica Compenso</b>
     * <pre>
     *     Sezione   Importo      Conto
     *        D      500,00       000.000.000/PTEST001      C13024 - Prodotti chimici
     * </pre>
     * <b>Scrittura Economica Mandato</b>
     * <pre>
     *     Sezione   Importo      Conto
     *        D      500,00       P13024 - Debiti verso fornitori - Prodotti chimici
     *        A      100,00       A91006 - Crediti diversi derivanti da ritenute erariali su redditi da lavoro autonomo per conto terzi
     *        A      400,00       A00053 - Istituto tesoriere/cassiere
     * </pre>
     */
    @Test
    @OperateOnDeployment(TEST_H2)
    @InSequence(1)
    public void testCompenso001() throws Exception {
        {
            CompensoBulk compensoBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(new TestUserContext(),
                            new CompensoBulk("000", "000.000", 2025, 1L)))
                    .filter(CompensoBulk.class::isInstance)
                    .map(CompensoBulk.class::cast)
                    .orElse(null);
            compensoBulk = Utility.createCompensoComponentSession().loadContributiERitenute(new TestUserContext(), compensoBulk);

            ResultScrittureContabili result = Utility.createProposeScritturaComponentSession().proposeScrittureContabili(
                    new TestUserContext(),
                    compensoBulk);

            //CONTROLLO ECONOMICA
            {
                assertEquals(new BigDecimal("500.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getIm_scrittura).orElse(null));
                assertEquals(new BigDecimal("500.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getImTotaleDare).orElse(null));
                assertEquals(new BigDecimal("500.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getImTotaleAvere).orElse(null));

                BulkList<Movimento_cogeBulk> movimentiDare = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                        .orElse(new BulkList<>());
                assertEquals(1, movimentiDare.size());

                Optional<Movimento_cogeBulk> rigaDare = movimentiDare.stream().findAny();
                assertTrue("Riga dare non presente.", rigaDare.isPresent());
                assertEquals("C13024", rigaDare.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                assertEquals(new BigDecimal("500.00"), rigaDare.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

                BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                        .orElse(new BulkList<>());
                assertEquals(1, movimentiAvere.size());

                Optional<Movimento_cogeBulk> rigaAvere = movimentiAvere.stream().findAny();
                assertTrue("Riga avere non presente.", rigaAvere.isPresent());
                assertEquals("P13024", rigaAvere.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                assertEquals(new BigDecimal("500.00"), rigaAvere.map(Movimento_cogeBulk::getIm_movimento).orElse(null));
            }

            //CONTROLLO ANALITICA
            {
                assertEquals(new BigDecimal("500.00"), Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getIm_scrittura).orElse(null));
                assertEquals(new BigDecimal("500.00"), Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getImTotaleDare).orElse(null));
                assertEquals(new BigDecimal("0"), Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getImTotaleAvere).orElse(null));

                List<Movimento_coanBulk> movimentiDare = Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getMovimentiDareColl)
                        .orElse(new BulkList<>());
                assertEquals(1, movimentiDare.size());

                Optional<Movimento_coanBulk> rigaDare = movimentiDare.stream().findAny();
                assertTrue("Riga dare analitica non presente.", rigaDare.isPresent());
                assertEquals("C13024", rigaDare.map(Movimento_coanBulk::getCd_voce_ana).orElse(null));
                assertEquals("000.000.000", rigaDare.map(Movimento_coanBulk::getCd_centro_responsabilita).orElse(null));
                assertEquals("PTEST001", rigaDare.map(Movimento_coanBulk::getCd_linea_attivita).orElse(null));
                assertEquals(new BigDecimal("500.00"), rigaDare.map(Movimento_coanBulk::getIm_movimento).orElse(null));

                List<Movimento_coanBulk> movimentiAvere = Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getMovimentiAvereColl)
                        .orElse(new BulkList<>());
                assertEquals(0, movimentiAvere.size());
            }

            //Richiamo il compenso perchè aggiunto valore voce_ep su testata
            compensoBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(new TestUserContext(),
                            new CompensoBulk("000", "000.000", 2025, 1L)))
                    .filter(CompensoBulk.class::isInstance)
                    .map(CompensoBulk.class::cast)
                    .orElse(null);
            compensoBulk = Utility.createCompensoComponentSession().loadContributiERitenute(new TestUserContext(), compensoBulk);
            Utility.createScritturaPartitaDoppiaFromDocumentoComponentSession().modificaConBulk(new TestUserContext(), compensoBulk);
        }
        {
            MandatoBulk mandatoBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(new TestUserContext(),
                            new MandatoIBulk("000",2025,11L)))
                    .filter(MandatoBulk.class::isInstance)
                    .map(MandatoBulk.class::cast)
                    .orElse(null);

            ResultScrittureContabili result = Utility.createProposeScritturaComponentSession().proposeScrittureContabili(
                    new TestUserContext(),
                    mandatoBulk);

            //CONTROLLO ECONOMICA
            {
                assertEquals(new BigDecimal("500.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getIm_scrittura).orElse(null));
                assertEquals(new BigDecimal("500.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getImTotaleDare).orElse(null));
                assertEquals(new BigDecimal("500.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getImTotaleAvere).orElse(null));

                BulkList<Movimento_cogeBulk> movimentiDare = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                        .orElse(new BulkList<>());
                assertEquals(1, movimentiDare.size());

                Optional<Movimento_cogeBulk> rigaTipoDebito = movimentiDare.stream().filter(Movimento_cogeBulk::isRigaTipoDebito).findAny();
                assertTrue("Riga tipo debito non presente.", rigaTipoDebito.isPresent());
                assertEquals("P13024", rigaTipoDebito.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                assertEquals(new BigDecimal("500.00"), rigaTipoDebito.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

                BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                        .orElse(new BulkList<>());
                assertEquals(2, movimentiAvere.size());

                Optional<Movimento_cogeBulk> rigaTipoRitenute = movimentiAvere.stream().filter(Movimento_cogeBulk::isRigaTipoDebito).findAny();
                assertTrue("Riga tipo debito per ritenute non presente.", rigaTipoRitenute.isPresent());
                assertEquals("RAIRPEF", rigaTipoRitenute.map(Movimento_cogeBulk::getCd_contributo_ritenuta).orElse(null));
                assertEquals("A91006", rigaTipoRitenute.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                assertEquals(new BigDecimal("100.00"), rigaTipoRitenute.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

                Optional<Movimento_cogeBulk> rigaTipoTesoreria = movimentiAvere.stream().filter(Movimento_cogeBulk::isRigaTipoTesoreria).findAny();
                assertTrue("Riga tipo tesoreria non presente.", rigaTipoTesoreria.isPresent());
                assertEquals("A00053", rigaTipoTesoreria.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                assertEquals(new BigDecimal("400.00"), rigaTipoTesoreria.map(Movimento_cogeBulk::getIm_movimento).orElse(null));
            }

            //CONTROLLO ANALITICA
            {
                assertFalse("Scrittura analitica presente.", Optional.ofNullable(result.getScritturaAnaliticaBulk()).isPresent());
            }
        }
    }
}

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
import it.cnr.contab.compensi00.docs.bulk.CompensoBulk;
import it.cnr.contab.compensi00.docs.bulk.Contributo_ritenutaBulk;
import it.cnr.contab.compensi00.docs.bulk.Contributo_ritenutaHome;
import it.cnr.contab.config00.bulk.AssCausaleVoceEPBulk;
import it.cnr.contab.config00.bulk.CausaleContabileBulk;
import it.cnr.contab.docamm00.docs.bulk.Documento_genericoBulk;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CompensoScrittureTest extends DeploymentsH2 {
    @EJB
    private CRUDComponentSession crudComponentSession;

    /**
     * Compenso occasionale
     * <p><b>Dati Compenso</b>
     * <pre>
     * Voce Bilancio: 13024 - Prodotti Chimici
     * Importo: 500,00 - IRPEF: 100
     * </pre></p>
     * <b>Scrittura Economica Compenso</b>
     * <pre>
     *     Sezione   Importo      Conto
     *        D      500,00       C13024 - Prodotti chimici
     *        A      1000,00      P13024 - Debiti verso fornitori - Prodotti chimici
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
    @OperateOnDeployment(TEST_H2)
    @InSequence(1)
    public void testCompenso001() throws Exception {
        {
            CompensoBulk compensoBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(new TestUserContext(),
                            new CompensoBulk("000", "000.000", 2025,1L)))
                    .filter(CompensoBulk.class::isInstance)
                    .map(CompensoBulk.class::cast)
                    .orElse(null);
            ResultScrittureContabili result = Utility.createProposeScritturaComponentSession().proposeScrittureContabili(
                    new TestUserContext(),
                    compensoBulk);
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

            compensoBulk = (CompensoBulk)Utility.createCompensoComponentSession().loadContributiERitenute(new TestUserContext(), compensoBulk);
            Utility.createScritturaPartitaDoppiaFromDocumentoComponentSession().modificaConBulk(new TestUserContext(), compensoBulk);
        }
    }
}

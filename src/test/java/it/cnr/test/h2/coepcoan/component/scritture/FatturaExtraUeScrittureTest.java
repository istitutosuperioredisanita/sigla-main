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
import it.cnr.contab.coepcoan00.ejb.ProposeScritturaComponentSession;
import it.cnr.contab.coepcoan00.ejb.ScritturaPartitaDoppiaFromDocumentoComponentSession;
import it.cnr.contab.docamm00.docs.bulk.Fattura_passivaBulk;
import it.cnr.contab.docamm00.docs.bulk.Fattura_passiva_IBulk;
import it.cnr.contab.docamm00.docs.bulk.Nota_di_creditoBulk;
import it.cnr.contab.doccont00.core.bulk.MandatoBulk;
import it.cnr.contab.doccont00.core.bulk.MandatoIBulk;
import it.cnr.jada.bulk.BulkList;
import it.cnr.test.h2.DeploymentsH2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import javax.naming.NamingException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class FatturaExtraUeScrittureTest extends DeploymentsH2 {
    private ProposeScritturaComponentSession proposeScritturaComponentSession;
    private ScritturaPartitaDoppiaFromDocumentoComponentSession scritturaPartitaDoppiaFromDocumentoComponentSession;

    @BeforeEach
    public void lookupRemoteEJBs() throws NamingException {
        super.lookupRemoteEJBs();
        proposeScritturaComponentSession = lookup("CNRCOEPCOAN00_EJB_ProposeScritturaComponentSession", ProposeScritturaComponentSession.class);
        scritturaPartitaDoppiaFromDocumentoComponentSession = lookup("CNRCOEPCOAN00_EJB_ScritturaPartitaDoppiaFromDocumentoComponentSession", ScritturaPartitaDoppiaFromDocumentoComponentSession.class);
    }
    
    /**
     * Fattura {@code Istituzionale} {@code ExtraUe} {@code No Ordini} {@code Lettera Pagamento}
     * su mono voce di 1 riga:
     * <p><b>Dati Fattura</b>
     * <pre>
     *     <b>Riga 1</b>
     *        Voce Bilancio: 22010 - Attrezzature scientifiche
     *        Imponibile:    16,97
     *        Imposta:        1,70
     *     <b>Lettera Pagamento</b>
     *        Imponibile:    19,00
     * </pre></p>
     * <b>Scrittura Economica Fattura</b>
     * <pre>
     *     Sezione   Importo      Conto
     *        D        2,03       C16003  - Perdite su Cambi
     *        A       18,67       A22010  - Attrezzature scientifiche
     *        A       20,70       P22010  - Debiti verso fornitori per acquisto di
     *                                      attrezzature scientifiche
     * </pre>
     * <b>Scrittura Analitica Fattura</b>
     * <pre>
     * <pre>
     *     Sezione   Importo      Conto
     *        D        2,03       C00069  - Altre sopravvenienze passive
     * </pre>
     */
    @Test
    @Order(1)
    public void testFattureExtraUe001() throws Exception {
        //Registrazione fattura
        {
            Fattura_passivaBulk fatturaPassivaBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(getUserContext(),
                            new Fattura_passiva_IBulk("000", "000.000", 2025, 18L)))
                    .filter(Fattura_passivaBulk.class::isInstance)
                    .map(Fattura_passivaBulk.class::cast)
                    .orElse(null);

            ResultScrittureContabili result = proposeScritturaComponentSession.proposeScrittureContabili(
                    getUserContext(),
                    fatturaPassivaBulk);

            //CONTROLLO ECONOMICA
            {
                Assertions.assertEquals(new BigDecimal("20.70"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getIm_scrittura).orElse(null));
                Assertions.assertEquals(new BigDecimal("20.70"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getImTotaleDare).orElse(null));
                Assertions.assertEquals(new BigDecimal("20.70"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getImTotaleAvere).orElse(null));

                BulkList<Movimento_cogeBulk> movimentiDare = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(2, movimentiDare.size());

                Optional<Movimento_cogeBulk> rigaDare1 = movimentiDare.stream().filter(el -> "A22010".equals(el.getCd_voce_ep())).findAny();
                Assertions.assertTrue(rigaDare1.isPresent(),"Conto A22010 non presente.");
                Assertions.assertTrue(rigaDare1.filter(Movimento_cogeBulk::isRigaTipoAttivita).isPresent(),"Riga tipo attivita' non presente.");
                Assertions.assertEquals(new BigDecimal("18.67"), rigaDare1.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

                Optional<Movimento_cogeBulk> rigaDare2 = movimentiDare.stream().filter(el -> "C16003".equals(el.getCd_voce_ep())).findAny();
                Assertions.assertTrue(rigaDare2.isPresent(),"Conto C16003 non presente.");
                Assertions.assertTrue(rigaDare2.filter(Movimento_cogeBulk::isRigaTipoCosto).isPresent(),"Riga tipo perdita da cambio non presente.");
                Assertions.assertEquals(new BigDecimal("2.03"), rigaDare2.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

                BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(1, movimentiAvere.size());

                Optional<Movimento_cogeBulk> rigaTipoDebito1 = movimentiAvere.stream().filter(el -> "P22010".equals(el.getCd_voce_ep())).findAny();
                Assertions.assertTrue(rigaTipoDebito1.isPresent(),"Conto P22010 non presente.");
                Assertions.assertTrue(rigaTipoDebito1.filter(Movimento_cogeBulk::isRigaTipoDebito).isPresent(),"Riga tipo debito non presente.");
                Assertions.assertEquals(new BigDecimal("20.70"), rigaTipoDebito1.map(Movimento_cogeBulk::getIm_movimento).orElse(null));
            }

            //CONTROLLO ANALITICA
            {
                Assertions.assertEquals(new BigDecimal("2.03"), Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getIm_scrittura).orElse(null));
                Assertions.assertEquals(new BigDecimal("2.03"), Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getImTotaleDare).orElse(null));
                Assertions.assertEquals(new BigDecimal("0"), Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getImTotaleAvere).orElse(null));

                List<Movimento_coanBulk> movimentiDare = Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getMovimentiDareColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(1, movimentiDare.size());

                Optional<Movimento_coanBulk> rigaDare1 = movimentiDare.stream()
                        .filter(el->"PTEST003".equals(el.getCd_linea_attivita()))
                        .findAny();
                Assertions.assertTrue(rigaDare1.isPresent(),"Riga dare analitica su GAE PTEST003 non presente.");
                Assertions.assertEquals("C16003", rigaDare1.map(Movimento_coanBulk::getCd_voce_ana).orElse(null));
                Assertions.assertEquals("000.000.000", rigaDare1.map(Movimento_coanBulk::getCd_centro_responsabilita).orElse(null));
                Assertions.assertEquals("PTEST003", rigaDare1.map(Movimento_coanBulk::getCd_linea_attivita).orElse(null));
                Assertions.assertEquals(new BigDecimal("2.03"), rigaDare1.map(Movimento_coanBulk::getIm_movimento).orElse(null));

                List<Movimento_coanBulk> movimentiAvere = Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getMovimentiAvereColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(0, movimentiAvere.size());
            }

            scritturaPartitaDoppiaFromDocumentoComponentSession.modificaConBulk(getUserContext(), fatturaPassivaBulk);
        }
    }
}

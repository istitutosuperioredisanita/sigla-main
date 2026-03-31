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
import it.cnr.contab.util.TestUserContext;
import it.cnr.jada.bulk.BulkList;
import org.junit.jupiter.api.*;
import it.cnr.test.h2.DeploymentsH2;

import javax.naming.NamingException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class NotaCreditoScrittureTest extends DeploymentsH2 {
    private ProposeScritturaComponentSession proposeScritturaComponentSession;
    private ScritturaPartitaDoppiaFromDocumentoComponentSession scritturaPartitaDoppiaFromDocumentoComponentSession;

    @BeforeEach
    public void lookupRemoteEJBs() throws NamingException {
        super.lookupRemoteEJBs();
        proposeScritturaComponentSession = lookup("CNRCOEPCOAN00_EJB_ProposeScritturaComponentSession", ProposeScritturaComponentSession.class);
        scritturaPartitaDoppiaFromDocumentoComponentSession = lookup("CNRCOEPCOAN00_EJB_ScritturaPartitaDoppiaFromDocumentoComponentSession", ScritturaPartitaDoppiaFromDocumentoComponentSession.class);
    }
    
    /**
     * Fattura {@code Istituzionale Split Payment} di {@code Competenza} stornata da {@code Nota Credito} su mono voce:
     * <p><b>Dati Fattura</b>
     * <pre>
     * Voce Bilancio: 22010 - Attrezzature scientifiche
     * Competenza Fattura: Anno registrazione
     * Imponibile:    18,67
     * Imposta:        1,70
     * </pre></p>
     * <b>Scrittura Economica Fattura</b>
     * <pre>
     *     Sezione   Importo      Conto
     *        D       18,67       A22010  - Attrezzature scientifiche
     *        A       16,97       P22010  - Debiti verso fornitori per acquisto di
     *                                      attrezzature scientifiche
     *        A        1,70       P71012I - Debito per versamento delle ritenute
     *                                      per scissione contabile IVA COMMERCIALE
     *                                      (Split Payment)
     * </pre>
     * <b>Scrittura Analitica Fattura</b>
     * <pre>
     *     Scrittura da non generare
     * </pre>
     * <b>Scrittura Economica Nota Credito</b>
     * <pre>
     *     Sezione   Importo      Conto
     *        A       18,67       A22010  - Attrezzature scientifiche
     *        D       16,97       P22010  - Debiti verso fornitori per acquisto di
     *                                      attrezzature scientifiche
     *        D        1,70       P71012I - Debito per versamento delle ritenute
     *                                      per scissione contabile IVA COMMERCIALE
     *                                      (Split Payment)
     * </pre>
     * <b>Scrittura Analitica Nota Credito</b>
     * <pre>
     *     Scrittura da non generare
     * </pre>
     */
    @Test
    @Order(1)
    public void testNotaCredito001() throws Exception {
        //Registrazione fattura
        {
            Fattura_passivaBulk fatturaPassivaBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(getUserContext(),
                            new Fattura_passiva_IBulk("000", "000.000", 2025, 5L)))
                    .filter(Fattura_passivaBulk.class::isInstance)
                    .map(Fattura_passivaBulk.class::cast)
                    .orElse(null);

            ResultScrittureContabili result = proposeScritturaComponentSession.proposeScrittureContabili(
                    getUserContext(),
                    fatturaPassivaBulk);

            //CONTROLLO ECONOMICA
            {
                Assertions.assertEquals(new BigDecimal("18.67"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getIm_scrittura).orElse(null));
                Assertions.assertEquals(new BigDecimal("18.67"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getImTotaleDare).orElse(null));
                Assertions.assertEquals(new BigDecimal("18.67"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getImTotaleAvere).orElse(null));

                BulkList<Movimento_cogeBulk> movimentiDare = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(1, movimentiDare.size());

                Optional<Movimento_cogeBulk> rigaTipoAttivita = movimentiDare.stream().filter(Movimento_cogeBulk::isRigaTipoAttivita).findAny();
                Assertions.assertTrue(rigaTipoAttivita.isPresent(),"Riga tipo attivita non presente.");
                Assertions.assertEquals("A22010", rigaTipoAttivita.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                Assertions.assertEquals(new BigDecimal("18.67"), rigaTipoAttivita.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

                BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(2, movimentiAvere.size());

                Optional<Movimento_cogeBulk> rigaTipoDebito = movimentiAvere.stream().filter(Movimento_cogeBulk::isRigaTipoDebito).findAny();
                Assertions.assertTrue(rigaTipoDebito.isPresent(),"Riga tipo debito non presente.");
                Assertions.assertEquals("P22010", rigaTipoDebito.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                Assertions.assertEquals(new BigDecimal("16.97"), rigaTipoDebito.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

                Optional<Movimento_cogeBulk> rigaTipoIva = movimentiAvere.stream().filter(Movimento_cogeBulk::isRigaTipoIvaAcquistoSplit).findAny();
                Assertions.assertTrue(rigaTipoIva.isPresent(),"Riga tipo iva non presente.");
                Assertions.assertEquals("P71012I", rigaTipoIva.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                Assertions.assertEquals(new BigDecimal("1.70"), rigaTipoIva.map(Movimento_cogeBulk::getIm_movimento).orElse(null));
            }

            //CONTROLLO ANALITICA
            {
                Assertions.assertFalse(Optional.ofNullable(result.getScritturaAnaliticaBulk()).isPresent(),"Scrittura analitica presente.");
            }

            scritturaPartitaDoppiaFromDocumentoComponentSession.modificaConBulk(getUserContext(), fatturaPassivaBulk);
        }
        //Registrazione nota credito
        {
            Nota_di_creditoBulk notaCreditoBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(getUserContext(),
                            new Nota_di_creditoBulk("000", "000.000", 2025, 6L)))
                    .filter(Nota_di_creditoBulk.class::isInstance)
                    .map(Nota_di_creditoBulk.class::cast)
                    .orElse(null);

            ResultScrittureContabili result = proposeScritturaComponentSession.proposeScrittureContabili(
                    getUserContext(),
                    notaCreditoBulk);

            //CONTROLLO ECONOMICA
            {
                Assertions.assertEquals(new BigDecimal("18.67"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getIm_scrittura).orElse(null));
                Assertions.assertEquals(new BigDecimal("18.67"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getImTotaleDare).orElse(null));
                Assertions.assertEquals(new BigDecimal("18.67"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getImTotaleAvere).orElse(null));

                BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(1, movimentiAvere.size());

                Optional<Movimento_cogeBulk> rigaTipoAttivita = movimentiAvere.stream().filter(Movimento_cogeBulk::isRigaTipoAttivita).findAny();
                Assertions.assertTrue(rigaTipoAttivita.isPresent(),"Riga tipo attivita non presente.");
                Assertions.assertEquals("A22010", rigaTipoAttivita.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                Assertions.assertEquals(new BigDecimal("18.67"), rigaTipoAttivita.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

                BulkList<Movimento_cogeBulk> movimentiDare = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(2, movimentiDare.size());

                Optional<Movimento_cogeBulk> rigaTipoDebito = movimentiDare.stream().filter(Movimento_cogeBulk::isRigaTipoDebito).findAny();
                Assertions.assertTrue(rigaTipoDebito.isPresent(),"Riga tipo debito non presente.");
                Assertions.assertEquals("P22010", rigaTipoDebito.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                Assertions.assertEquals(new BigDecimal("16.97"), rigaTipoDebito.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

                Optional<Movimento_cogeBulk> rigaTipoIva = movimentiDare.stream().filter(Movimento_cogeBulk::isRigaTipoIvaAcquistoSplit).findAny();
                Assertions.assertTrue(rigaTipoIva.isPresent(),"Riga tipo iva non presente.");
                Assertions.assertEquals("P71012I", rigaTipoIva.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                Assertions.assertEquals(new BigDecimal("1.70"), rigaTipoIva.map(Movimento_cogeBulk::getIm_movimento).orElse(null));
            }

            //CONTROLLO ANALITICA
            {
                Assertions.assertFalse(Optional.ofNullable(result.getScritturaAnaliticaBulk()).isPresent(),"Scrittura analitica presente.");
            }
        }
    }

    /**
     * Fattura {@code Istituzionale Split Payment} di {@code Competenza} e {@code Senza Impegno} stornata da {@code Nota Credito} su mono voce:
     * <p><b>Dati Fattura</b>
     * <pre>
     * Competenza Fattura: Anno registrazione
     * Non Liquidabile (Senza Impegno)
     * ConfigurazioneCNR: VOCEEP_SPECIALE-COSTO_DOC_NON_LIQUIDABILE-VOCE: C00066 - GAE: PTEST003
     * Conto di contropartita di C00066: P43001 - Altri debiti
     * Imponibile:    18,67
     * Imposta:        1,70
     * </pre></p>
     * <b>Scrittura Economica Fattura</b>
     * <pre>
     *     Sezione   Importo      Conto
     *        D       18,67       C00066  - Altri accontonamenti n.a.c
     *        A       16,97       P43001  - Altri debiti
     *        A        1,70       P71012I - Debito per versamento delle ritenute
     *                                      per scissione contabile IVA COMMERCIALE
     *                                      (Split Payment)
     * </pre>
     * <b>Scrittura Analitica Fattura</b>
     * <pre>
     *     Sezione   Importo      CDR/GAE                   Conto Analitico
     *        D      18,67        000.000.000/PTEST003      C00066  - Altri accontonamenti n.a.c
     * </pre>
     * <b>Scrittura Economica Nota Credito</b>
     * <pre>
     *     Sezione   Importo      Conto
     *        A       18,67       C00066  - Altri accontonamenti n.a.c
     *        D       16,97       P43001  - Altri debiti
     *        D        1,70       P71012I - Debito per versamento delle ritenute
     *                                      per scissione contabile IVA COMMERCIALE
     *                                      (Split Payment)
     * </pre>
     * <b>Scrittura Analitica Nota Credito</b>
     * <pre>
     *     Sezione   Importo      CDR/GAE                   Conto Analitico
     *        A      18,67        000.000.000/PTEST003      C00066  - Altri accontonamenti n.a.c
     * </pre>
     */
    @Test
    @Order(2)
    public void testNotaCredito002() throws Exception {
        //Registrazione fattura
        {
            Fattura_passivaBulk fatturaPassivaBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(getUserContext(),
                            new Fattura_passiva_IBulk("000", "000.000", 2025, 7L)))
                    .filter(Fattura_passivaBulk.class::isInstance)
                    .map(Fattura_passivaBulk.class::cast)
                    .orElse(null);

            ResultScrittureContabili result = proposeScritturaComponentSession.proposeScrittureContabili(
                    getUserContext(),
                    fatturaPassivaBulk);
            //CONTROLLO ECONOMICA
            {
                Assertions.assertEquals(new BigDecimal("18.67"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getIm_scrittura).orElse(null));
                Assertions.assertEquals(new BigDecimal("18.67"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getImTotaleDare).orElse(null));
                Assertions.assertEquals(new BigDecimal("18.67"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getImTotaleAvere).orElse(null));

                BulkList<Movimento_cogeBulk> movimentiDare = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(1, movimentiDare.size());

                Optional<Movimento_cogeBulk> rigaTipoCosto = movimentiDare.stream().filter(Movimento_cogeBulk::isRigaTipoCosto).findAny();
                Assertions.assertTrue(rigaTipoCosto.isPresent(),"Riga tipo costo non presente.");
                Assertions.assertEquals("C00066", rigaTipoCosto.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                Assertions.assertEquals(new BigDecimal("18.67"), rigaTipoCosto.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

                BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(2, movimentiAvere.size());

                Optional<Movimento_cogeBulk> rigaTipoDebito = movimentiAvere.stream().filter(Movimento_cogeBulk::isRigaTipoDebito).findAny();
                Assertions.assertTrue(rigaTipoDebito.isPresent(),"Riga tipo debito non presente.");
                Assertions.assertEquals("P43001", rigaTipoDebito.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                Assertions.assertEquals(new BigDecimal("16.97"), rigaTipoDebito.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

                Optional<Movimento_cogeBulk> rigaTipoIva = movimentiAvere.stream().filter(Movimento_cogeBulk::isRigaTipoIvaAcquistoSplit).findAny();
                Assertions.assertTrue(rigaTipoIva.isPresent(),"Riga tipo iva non presente.");
                Assertions.assertEquals("P71012I", rigaTipoIva.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                Assertions.assertEquals(new BigDecimal("1.70"), rigaTipoIva.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

                scritturaPartitaDoppiaFromDocumentoComponentSession.modificaConBulk(getUserContext(), fatturaPassivaBulk);
            }

            //CONTROLLO ANALITICA
            {
                Assertions.assertEquals(new BigDecimal("18.67"), Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getIm_scrittura).orElse(null));
                Assertions.assertEquals(new BigDecimal("18.67"), Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getImTotaleDare).orElse(null));
                Assertions.assertEquals(new BigDecimal("0"), Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getImTotaleAvere).orElse(null));

                List<Movimento_coanBulk> movimentiDare = Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getMovimentiDareColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(1, movimentiDare.size());

                Optional<Movimento_coanBulk> rigaDare = movimentiDare.stream().findAny();
                Assertions.assertTrue(rigaDare.isPresent(),"Riga analitica dare non presente.");
                Assertions.assertEquals("C00066", rigaDare.map(Movimento_coanBulk::getCd_voce_ana).orElse(null));
                Assertions.assertEquals("000.000.000", rigaDare.map(Movimento_coanBulk::getCd_centro_responsabilita).orElse(null));
                Assertions.assertEquals("PTEST003", rigaDare.map(Movimento_coanBulk::getCd_linea_attivita).orElse(null));
                Assertions.assertEquals(new BigDecimal("18.67"), rigaDare.map(Movimento_coanBulk::getIm_movimento).orElse(null));

                List<Movimento_coanBulk> movimentiAvere = Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getMovimentiAvereColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(0, movimentiAvere.size());
            }
        }
        //Registrazione nota credito
        {
            Nota_di_creditoBulk notaCreditoBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(getUserContext(),
                            new Nota_di_creditoBulk("000", "000.000", 2025, 8L)))
                    .filter(Nota_di_creditoBulk.class::isInstance)
                    .map(Nota_di_creditoBulk.class::cast)
                    .orElse(null);

            ResultScrittureContabili result = proposeScritturaComponentSession.proposeScrittureContabili(
                    getUserContext(),
                    notaCreditoBulk);

            //CONTROLLO ECONOMICA
            {
                Assertions.assertEquals(new BigDecimal("18.67"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getIm_scrittura).orElse(null));
                Assertions.assertEquals(new BigDecimal("18.67"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getImTotaleDare).orElse(null));
                Assertions.assertEquals(new BigDecimal("18.67"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getImTotaleAvere).orElse(null));

                BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(1, movimentiAvere.size());

                Optional<Movimento_cogeBulk> rigaTipoCosto = movimentiAvere.stream().filter(Movimento_cogeBulk::isRigaTipoCosto).findAny();
                Assertions.assertTrue(rigaTipoCosto.isPresent(),"Riga tipo costo non presente.");
                Assertions.assertEquals("C00066", rigaTipoCosto.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                Assertions.assertEquals(new BigDecimal("18.67"), rigaTipoCosto.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

                BulkList<Movimento_cogeBulk> movimentiDare = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(2, movimentiDare.size());

                Optional<Movimento_cogeBulk> rigaTipoDebito = movimentiDare.stream().filter(Movimento_cogeBulk::isRigaTipoDebito).findAny();
                Assertions.assertTrue(rigaTipoDebito.isPresent(),"Riga tipo debito non presente.");
                Assertions.assertEquals("P43001", rigaTipoDebito.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                Assertions.assertEquals(new BigDecimal("16.97"), rigaTipoDebito.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

                Optional<Movimento_cogeBulk> rigaTipoIva = movimentiDare.stream().filter(Movimento_cogeBulk::isRigaTipoIvaAcquistoSplit).findAny();
                Assertions.assertTrue(rigaTipoIva.isPresent(),"Riga tipo iva non presente.");
                Assertions.assertEquals("P71012I", rigaTipoIva.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                Assertions.assertEquals(new BigDecimal("1.70"), rigaTipoIva.map(Movimento_cogeBulk::getIm_movimento).orElse(null));
            }

            //CONTROLLO ANALITICA
            {
                Assertions.assertEquals(new BigDecimal("18.67"), Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getIm_scrittura).orElse(null));
                Assertions.assertEquals(new BigDecimal("0"), Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getImTotaleDare).orElse(null));
                Assertions.assertEquals(new BigDecimal("18.67"), Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getImTotaleAvere).orElse(null));

                List<Movimento_coanBulk> movimentiDare = Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getMovimentiDareColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(0, movimentiDare.size());

                List<Movimento_coanBulk> movimentiAvere = Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getMovimentiAvereColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(1, movimentiAvere.size());

                Optional<Movimento_coanBulk> rigaAvere = movimentiAvere.stream().findAny();
                Assertions.assertTrue(rigaAvere.isPresent(),"Riga analitica avere non presente.");
                Assertions.assertEquals("C00066", rigaAvere.map(Movimento_coanBulk::getCd_voce_ana).orElse(null));
                Assertions.assertEquals("000.000.000", rigaAvere.map(Movimento_coanBulk::getCd_centro_responsabilita).orElse(null));
                Assertions.assertEquals("PTEST003", rigaAvere.map(Movimento_coanBulk::getCd_linea_attivita).orElse(null));
                Assertions.assertEquals(new BigDecimal("18.67"), rigaAvere.map(Movimento_coanBulk::getIm_movimento).orElse(null));
            }
        }
    }

    /**
     * Fattura {@code Istituzionale} {@code No Split Payment} {@code No Ordini} {@code Bene Inventariabile}
     * su mono voce di 2 righe stornate con 2 note credito:
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
     * <b>Scrittura Analitica Fattura</b>
     * <pre>
     *     Scrittura da non generare
     * </pre>
     * <b>Scrittura Economica Nota Credito Riga 1</b>
     * <pre>
     *     Sezione   Importo      Conto
     *        A       18,67       A22012  - Macchine per ufficio
     *        D       16,97       P22012  - Debiti verso fornitori per acquisto di
     *                                      macchine per ufficio
     *        D        1,70       P71012I - Debito per versamento delle ritenute
     *                                      per scissione contabile IVA COMMERCIALE
     *                                      (Split Payment)
     * </pre>
     * <b>Scrittura Analitica Nota Credito Riga 1</b>
     * <pre>
     *     Scrittura da non generare
     * </pre>
     * <b>Scrittura Economica Nota Credito Riga 2</b>
     * <pre>
     *     Sezione   Importo      Conto
     *        A       10,00       A22010  - Attrezzature scientifiche
     *        D       10,00       P22010  - Debiti verso fornitori per acquisto di
     *                                      attrezzature scientifiche
     * </pre>
     * <b>Scrittura Analitica Nota Credito Riga 2</b>
     * <pre>
     *     Scrittura da non generare
     * </pre>
     */
    @Test
    @Order(3)
    public void testNotaCredito003() throws Exception {
        //Registrazione fattura
        {
            Fattura_passivaBulk fatturaPassivaBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(getUserContext(),
                            new Fattura_passiva_IBulk("000", "000.000", 2025, 11L)))
                    .filter(Fattura_passivaBulk.class::isInstance)
                    .map(Fattura_passivaBulk.class::cast)
                    .orElse(null);

            ResultScrittureContabili result = proposeScritturaComponentSession.proposeScrittureContabili(
                    getUserContext(),
                    fatturaPassivaBulk);

            //CONTROLLO ECONOMICA
            {
                Assertions.assertEquals(new BigDecimal("28.67"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getIm_scrittura).orElse(null));
                Assertions.assertEquals(new BigDecimal("28.67"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getImTotaleDare).orElse(null));
                Assertions.assertEquals(new BigDecimal("28.67"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getImTotaleAvere).orElse(null));

                BulkList<Movimento_cogeBulk> movimentiDare = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(2, movimentiDare.size());

                Optional<Movimento_cogeBulk> rigaTipoAttivita1 = movimentiDare.stream().filter(el -> "A22012".equals(el.getCd_voce_ep())).findAny();
                Assertions.assertTrue(rigaTipoAttivita1.isPresent(),"Conto A22012 non presente.");
                Assertions.assertTrue(rigaTipoAttivita1.filter(Movimento_cogeBulk::isRigaTipoAttivita).isPresent(),"Riga tipo attivita non presente.");
                Assertions.assertEquals(new BigDecimal("18.67"), rigaTipoAttivita1.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

                Optional<Movimento_cogeBulk> rigaTipoAttivita2 = movimentiDare.stream().filter(el -> "A22010".equals(el.getCd_voce_ep())).findAny();
                Assertions.assertTrue(rigaTipoAttivita2.isPresent(),"Conto A22010 non presente.");
                Assertions.assertTrue(rigaTipoAttivita2.filter(Movimento_cogeBulk::isRigaTipoAttivita).isPresent(),"Riga tipo attivita non presente.");
                Assertions.assertEquals(new BigDecimal("10.00"), rigaTipoAttivita2.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

                BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(3, movimentiAvere.size());

                Optional<Movimento_cogeBulk> rigaTipoDebito1 = movimentiAvere.stream().filter(el -> "P22012".equals(el.getCd_voce_ep())).findAny();
                Assertions.assertTrue(rigaTipoDebito1.isPresent(),"Conto P22012 non presente.");
                Assertions.assertTrue(rigaTipoDebito1.filter(Movimento_cogeBulk::isRigaTipoDebito).isPresent(),"Riga tipo debito non presente.");
                Assertions.assertEquals(new BigDecimal("16.97"), rigaTipoDebito1.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

                Optional<Movimento_cogeBulk> rigaTipoDebito2 = movimentiAvere.stream().filter(el -> "P22010".equals(el.getCd_voce_ep())).findAny();
                Assertions.assertTrue(rigaTipoDebito2.isPresent(),"Conto P22010 non presente.");
                Assertions.assertTrue(rigaTipoDebito2.filter(Movimento_cogeBulk::isRigaTipoDebito).isPresent(),"Riga tipo debito non presente.");
                Assertions.assertEquals(new BigDecimal("10.00"), rigaTipoDebito2.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

                Optional<Movimento_cogeBulk> rigaTipoIva = movimentiAvere.stream().filter(Movimento_cogeBulk::isRigaTipoIvaAcquistoSplit).findAny();
                Assertions.assertTrue(rigaTipoIva.isPresent(),"Riga tipo iva non presente.");
                Assertions.assertEquals("P71012I", rigaTipoIva.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                Assertions.assertEquals(new BigDecimal("1.70"), rigaTipoIva.map(Movimento_cogeBulk::getIm_movimento).orElse(null));
            }

            //CONTROLLO ANALITICA
            {
                Assertions.assertFalse(Optional.ofNullable(result.getScritturaAnaliticaBulk()).isPresent(),"Scrittura analitica presente.");
            }

            scritturaPartitaDoppiaFromDocumentoComponentSession.modificaConBulk(getUserContext(), fatturaPassivaBulk);
        }
        //Registrazione nota credito riga 1
        {
            Nota_di_creditoBulk notaCreditoBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(getUserContext(),
                            new Nota_di_creditoBulk("000", "000.000", 2025, 12L)))
                    .filter(Nota_di_creditoBulk.class::isInstance)
                    .map(Nota_di_creditoBulk.class::cast)
                    .orElse(null);

            ResultScrittureContabili result = proposeScritturaComponentSession.proposeScrittureContabili(
                    getUserContext(),
                    notaCreditoBulk);

            //CONTROLLO ECONOMICA
            {
                Assertions.assertEquals(new BigDecimal("18.67"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getIm_scrittura).orElse(null));
                Assertions.assertEquals(new BigDecimal("18.67"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getImTotaleDare).orElse(null));
                Assertions.assertEquals(new BigDecimal("18.67"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getImTotaleAvere).orElse(null));

                BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(1, movimentiAvere.size());

                Optional<Movimento_cogeBulk> rigaTipoAttivita = movimentiAvere.stream().filter(Movimento_cogeBulk::isRigaTipoAttivita).findAny();
                Assertions.assertTrue(rigaTipoAttivita.isPresent(),"Riga tipo attività non presente.");
                Assertions.assertEquals("A22012", rigaTipoAttivita.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                Assertions.assertEquals(new BigDecimal("18.67"), rigaTipoAttivita.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

                BulkList<Movimento_cogeBulk> movimentiDare = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(2, movimentiDare.size());

                Optional<Movimento_cogeBulk> rigaTipoDebito = movimentiDare.stream().filter(Movimento_cogeBulk::isRigaTipoDebito).findAny();
                Assertions.assertTrue(rigaTipoDebito.isPresent(),"Riga tipo debito non presente.");
                Assertions.assertEquals("P22012", rigaTipoDebito.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                Assertions.assertEquals(new BigDecimal("16.97"), rigaTipoDebito.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

                Optional<Movimento_cogeBulk> rigaTipoIva = movimentiDare.stream().filter(Movimento_cogeBulk::isRigaTipoIvaAcquistoSplit).findAny();
                Assertions.assertTrue(rigaTipoIva.isPresent(),"Riga tipo iva non presente.");
                Assertions.assertEquals("P71012I", rigaTipoIva.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                Assertions.assertEquals(new BigDecimal("1.70"), rigaTipoIva.map(Movimento_cogeBulk::getIm_movimento).orElse(null));
            }

            //CONTROLLO ANALITICA
            {
                Assertions.assertFalse(Optional.ofNullable(result.getScritturaAnaliticaBulk()).isPresent(),"Scrittura analitica presente.");
            }
        }
        //Registrazione nota credito riga 2
        {
            Nota_di_creditoBulk notaCreditoBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(getUserContext(),
                            new Nota_di_creditoBulk("000", "000.000", 2025, 13L)))
                    .filter(Nota_di_creditoBulk.class::isInstance)
                    .map(Nota_di_creditoBulk.class::cast)
                    .orElse(null);

            ResultScrittureContabili result = proposeScritturaComponentSession.proposeScrittureContabili(
                    getUserContext(),
                    notaCreditoBulk);

            //CONTROLLO ECONOMICA
            {
                Assertions.assertEquals(new BigDecimal("10.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getIm_scrittura).orElse(null));
                Assertions.assertEquals(new BigDecimal("10.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getImTotaleDare).orElse(null));
                Assertions.assertEquals(new BigDecimal("10.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getImTotaleAvere).orElse(null));

                BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(1, movimentiAvere.size());

                Optional<Movimento_cogeBulk> rigaTipoAttivita = movimentiAvere.stream().filter(Movimento_cogeBulk::isRigaTipoAttivita).findAny();
                Assertions.assertTrue(rigaTipoAttivita.isPresent(),"Riga tipo attività non presente.");
                Assertions.assertEquals("A22010", rigaTipoAttivita.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                Assertions.assertEquals(new BigDecimal("10.00"), rigaTipoAttivita.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

                BulkList<Movimento_cogeBulk> movimentiDare = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(1, movimentiDare.size());

                Optional<Movimento_cogeBulk> rigaTipoDebito = movimentiDare.stream().filter(Movimento_cogeBulk::isRigaTipoDebito).findAny();
                Assertions.assertTrue(rigaTipoDebito.isPresent(),"Riga tipo debito non presente.");
                Assertions.assertEquals("P22010", rigaTipoDebito.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                Assertions.assertEquals(new BigDecimal("10.00"), rigaTipoDebito.map(Movimento_cogeBulk::getIm_movimento).orElse(null));
            }

            //CONTROLLO ANALITICA
            {
                Assertions.assertFalse(Optional.ofNullable(result.getScritturaAnaliticaBulk()).isPresent(),"Scrittura analitica presente.");
            }
        }
    }

    /**
     * Fattura {@code Istituzionale Split Payment} di {@code Competenza} stornata da {@code Nota Credito} su mono voce:
     * <p><b>Dati Fattura</b>
     * <pre>
     * Voce Bilancio: 13024 - Prodotti chimici
     * CDR/GAE: 000.000.000/PTEST002
     * Competenza Fattura: Anno registrazione
     * Imponibile:    18,67
     * Imposta:        1,70
     * </pre></p>
     * <b>Scrittura Economica Fattura</b>
     * <pre>
     *     Sezione   Importo      Conto
     *        D       18,67       C13024  - Prodotti chimici
     *        A       16,97       P13024  - Debiti verso Prodotti chimici
     *        A        1,70       P71012I - Debito per versamento delle ritenute
     *                                      per scissione contabile IVA COMMERCIALE
     *                                      (Split Payment)
     * </pre>
     * <b>Scrittura Analitica Fattura</b>
     * <pre>
     *     Sezione   Importo      CDR/GAE                   Conto Analitico
     *        D      18,67        000.000.000/PTEST002      C13024 - Prodotti chimici
     * </pre>
     * <b>Scrittura Economica Nota Credito</b>
     * <pre>
     *     Sezione   Importo      Conto
     *        A       18,67       C13024  - Prodotti chimici
     *        D       16,97       P13024  - Debiti verso Prodotti chimici
     *        D        1,70       P71012I - Debito per versamento delle ritenute
     *                                      per scissione contabile IVA COMMERCIALE
     *                                      (Split Payment)
     * </pre>
     * <b>Scrittura Analitica Nota Credito</b>
     * <pre>
     *     Sezione   Importo      CDR/GAE                   Conto Analitico
     *        A      18,67        000.000.000/PTEST002      C13024 - Prodotti chimici
     * </pre>
     */
    @Test
    @Order(4)
    public void testNotaCredito004() throws Exception {
        //Registrazione fattura
        {
            Fattura_passivaBulk fatturaPassivaBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(getUserContext(),
                            new Fattura_passiva_IBulk("000", "000.000", 2025, 16L)))
                    .filter(Fattura_passivaBulk.class::isInstance)
                    .map(Fattura_passivaBulk.class::cast)
                    .orElse(null);

            ResultScrittureContabili result = proposeScritturaComponentSession.proposeScrittureContabili(
                    getUserContext(),
                    fatturaPassivaBulk);

            //CONTROLLO ECONOMICA
            {
                Assertions.assertEquals(new BigDecimal("18.67"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getIm_scrittura).orElse(null));
                Assertions.assertEquals(new BigDecimal("18.67"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getImTotaleDare).orElse(null));
                Assertions.assertEquals(new BigDecimal("18.67"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getImTotaleAvere).orElse(null));

                BulkList<Movimento_cogeBulk> movimentiDare = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(1, movimentiDare.size());

                Optional<Movimento_cogeBulk> rigaTipoCosto = movimentiDare.stream().filter(Movimento_cogeBulk::isRigaTipoCosto).findAny();
                Assertions.assertTrue(rigaTipoCosto.isPresent(),"Riga tipo costo non presente.");
                Assertions.assertEquals("C13024", rigaTipoCosto.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                Assertions.assertEquals(new BigDecimal("18.67"), rigaTipoCosto.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

                BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(2, movimentiAvere.size());

                Optional<Movimento_cogeBulk> rigaTipoDebito = movimentiAvere.stream().filter(Movimento_cogeBulk::isRigaTipoDebito).findAny();
                Assertions.assertTrue(rigaTipoDebito.isPresent(),"Riga tipo debito non presente.");
                Assertions.assertEquals("P13024", rigaTipoDebito.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                Assertions.assertEquals(new BigDecimal("16.97"), rigaTipoDebito.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

                Optional<Movimento_cogeBulk> rigaTipoIva = movimentiAvere.stream().filter(Movimento_cogeBulk::isRigaTipoIvaAcquistoSplit).findAny();
                Assertions.assertTrue(rigaTipoIva.isPresent(),"Riga tipo iva non presente.");
                Assertions.assertEquals("P71012I", rigaTipoIva.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                Assertions.assertEquals(new BigDecimal("1.70"), rigaTipoIva.map(Movimento_cogeBulk::getIm_movimento).orElse(null));
            }

            //CONTROLLO ANALITICA
            {
                Assertions.assertEquals(new BigDecimal("18.67"), Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getIm_scrittura).orElse(null));
                Assertions.assertEquals(new BigDecimal("18.67"), Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getImTotaleDare).orElse(null));
                Assertions.assertEquals(new BigDecimal("0"), Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getImTotaleAvere).orElse(null));

                List<Movimento_coanBulk> movimentiDare = Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getMovimentiDareColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(1, movimentiDare.size());

                Optional<Movimento_coanBulk> rigaDare = movimentiDare.stream().findAny();
                Assertions.assertTrue(rigaDare.isPresent(),"Riga dare analitica non presente.");
                Assertions.assertEquals("C13024", rigaDare.map(Movimento_coanBulk::getCd_voce_ana).orElse(null));
                Assertions.assertEquals("000.000.000", rigaDare.map(Movimento_coanBulk::getCd_centro_responsabilita).orElse(null));
                Assertions.assertEquals("PTEST002", rigaDare.map(Movimento_coanBulk::getCd_linea_attivita).orElse(null));
                Assertions.assertEquals(new BigDecimal("18.67"), rigaDare.map(Movimento_coanBulk::getIm_movimento).orElse(null));

                List<Movimento_coanBulk> movimentiAvere = Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getMovimentiAvereColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(0, movimentiAvere.size());
            }

            scritturaPartitaDoppiaFromDocumentoComponentSession.modificaConBulk(getUserContext(), fatturaPassivaBulk);
        }
        //Registrazione nota credito
        {
            Nota_di_creditoBulk notaCreditoBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(getUserContext(),
                            new Nota_di_creditoBulk("000", "000.000", 2025, 17L)))
                    .filter(Nota_di_creditoBulk.class::isInstance)
                    .map(Nota_di_creditoBulk.class::cast)
                    .orElse(null);

            ResultScrittureContabili result = proposeScritturaComponentSession.proposeScrittureContabili(
                    getUserContext(),
                    notaCreditoBulk);

            //CONTROLLO ECONOMICA
            {
                Assertions.assertEquals(new BigDecimal("18.67"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getIm_scrittura).orElse(null));
                Assertions.assertEquals(new BigDecimal("18.67"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getImTotaleDare).orElse(null));
                Assertions.assertEquals(new BigDecimal("18.67"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getImTotaleAvere).orElse(null));

                BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(1, movimentiAvere.size());

                Optional<Movimento_cogeBulk> rigaTipoCosto = movimentiAvere.stream().filter(Movimento_cogeBulk::isRigaTipoCosto).findAny();
                Assertions.assertTrue(rigaTipoCosto.isPresent(),"Riga tipo costo non presente.");
                Assertions.assertEquals("C13024", rigaTipoCosto.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                Assertions.assertEquals(new BigDecimal("18.67"), rigaTipoCosto.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

                BulkList<Movimento_cogeBulk> movimentiDare = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(2, movimentiDare.size());

                Optional<Movimento_cogeBulk> rigaTipoDebito = movimentiDare.stream().filter(Movimento_cogeBulk::isRigaTipoDebito).findAny();
                Assertions.assertTrue(rigaTipoDebito.isPresent(),"Riga tipo debito non presente.");
                Assertions.assertEquals("P13024", rigaTipoDebito.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                Assertions.assertEquals(new BigDecimal("16.97"), rigaTipoDebito.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

                Optional<Movimento_cogeBulk> rigaTipoIva = movimentiDare.stream().filter(Movimento_cogeBulk::isRigaTipoIvaAcquistoSplit).findAny();
                Assertions.assertTrue(rigaTipoIva.isPresent(),"Riga tipo iva non presente.");
                Assertions.assertEquals("P71012I", rigaTipoIva.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                Assertions.assertEquals(new BigDecimal("1.70"), rigaTipoIva.map(Movimento_cogeBulk::getIm_movimento).orElse(null));
            }

            //CONTROLLO ANALITICA
            {
                Assertions.assertEquals(new BigDecimal("18.67"), Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getIm_scrittura).orElse(null));
                Assertions.assertEquals(new BigDecimal("0"), Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getImTotaleDare).orElse(null));
                Assertions.assertEquals(new BigDecimal("18.67"), Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getImTotaleAvere).orElse(null));

                List<Movimento_coanBulk> movimentiDare = Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getMovimentiDareColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(0, movimentiDare.size());

                List<Movimento_coanBulk> movimentiAvere = Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getMovimentiAvereColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(1, movimentiAvere.size());

                Optional<Movimento_coanBulk> rigaAvere = movimentiAvere.stream().findAny();
                Assertions.assertTrue(rigaAvere.isPresent(),"Riga avere analitica non presente.");
                Assertions.assertEquals("C13024", rigaAvere.map(Movimento_coanBulk::getCd_voce_ana).orElse(null));
                Assertions.assertEquals("000.000.000", rigaAvere.map(Movimento_coanBulk::getCd_centro_responsabilita).orElse(null));
                Assertions.assertEquals("PTEST002", rigaAvere.map(Movimento_coanBulk::getCd_linea_attivita).orElse(null));
                Assertions.assertEquals(new BigDecimal("18.67"), rigaAvere.map(Movimento_coanBulk::getIm_movimento).orElse(null));
            }
        }
    }
}

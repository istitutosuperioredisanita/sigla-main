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

import it.cnr.contab.coepcoan00.comp.ScritturaPartitaDoppiaNotEnabledException;
import it.cnr.contab.coepcoan00.core.bulk.*;
import it.cnr.contab.coepcoan00.ejb.ProposeScritturaComponentSession;
import it.cnr.contab.coepcoan00.ejb.ScritturaPartitaDoppiaFromDocumentoComponentSession;
import it.cnr.contab.docamm00.docs.bulk.Documento_genericoBulk;
import it.cnr.contab.doccont00.core.bulk.MandatoBulk;
import it.cnr.contab.doccont00.core.bulk.MandatoIBulk;
import it.cnr.contab.util.TestUserContext;
import it.cnr.jada.bulk.BulkList;
import org.junit.jupiter.api.*;
import it.cnr.test.h2.DeploymentsH2;
import org.junit.jupiter.api.Order;

import javax.naming.NamingException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class DocumentoGenericoScrittureTest extends DeploymentsH2 {
    private ProposeScritturaComponentSession proposeScritturaComponentSession;
    private ScritturaPartitaDoppiaFromDocumentoComponentSession scritturaPartitaDoppiaFromDocumentoComponentSession;

    @BeforeEach
    public void lookupRemoteEJBs() throws NamingException {
        super.lookupRemoteEJBs();
        proposeScritturaComponentSession = lookup("CNRCOEPCOAN00_EJB_ProposeScritturaComponentSession", ProposeScritturaComponentSession.class);
        scritturaPartitaDoppiaFromDocumentoComponentSession = lookup("CNRCOEPCOAN00_EJB_ScritturaPartitaDoppiaFromDocumentoComponentSession", ScritturaPartitaDoppiaFromDocumentoComponentSession.class);
    }

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
        Documento_genericoBulk documentoCogeBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(getUserContext(),
                        new Documento_genericoBulk("000","GENERICO_S","000.000",2025,
                                1L)))
                .filter(Documento_genericoBulk.class::isInstance)
                .map(Documento_genericoBulk.class::cast)
                .orElse(null);
        ResultScrittureContabili result = proposeScritturaComponentSession.proposeScrittureContabili(
                getUserContext(),
                documentoCogeBulk);

        //CONTROLLO ECONOMICA
        {
            Assertions.assertTrue(Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .filter(el -> el.getIm_scrittura().compareTo(new BigDecimal(1000)) == 0)
                    .filter(el -> el.getImTotaleDare().compareTo(new BigDecimal(1000)) == 0)
                    .filter(el -> el.getImTotaleAvere().compareTo(new BigDecimal(1000)) == 0)
                    .isPresent());
            Assertions.assertEquals(1, Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                    .orElse(new BulkList<>())
                    .size());
            Assertions.assertEquals(1, Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                    .orElse(new BulkList<>())
                    .size());
            Assertions.assertTrue(Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                    .orElse(new BulkList<>())
                    .stream().findFirst()
                    .filter(Movimento_cogeBulk::isSezioneDare)
                    .filter(el -> "A22010".equals(el.getCd_voce_ep()))
                    .filter(el -> el.getIm_movimento().compareTo(new BigDecimal(1000)) == 0)
                    .isPresent());
            Assertions.assertTrue(Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                    .orElse(new BulkList<>())
                    .stream().findFirst()
                    .filter(Movimento_cogeBulk::isSezioneAvere)
                    .filter(el -> "P22010".equals(el.getCd_voce_ep()))
                    .filter(el -> el.getIm_movimento().compareTo(new BigDecimal(1000)) == 0)
                    .isPresent());
        }

        //CONTROLLO ANALITICA
        {
            Assertions.assertFalse(Optional.ofNullable(result.getScritturaAnaliticaBulk()).isPresent(),"Scrittura analitica presente.");
        }

        scritturaPartitaDoppiaFromDocumentoComponentSession.modificaConBulk(getUserContext(), documentoCogeBulk);

        MandatoBulk mandatoBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(getUserContext(),
                        new MandatoIBulk("000",2025,1L)))
                .filter(MandatoBulk.class::isInstance)
                .map(MandatoBulk.class::cast)
                .orElse(null);

        result = proposeScritturaComponentSession.proposeScrittureContabili(
                getUserContext(),
                mandatoBulk);

        //CONTROLLO ECONOMICA
        {
            Assertions.assertTrue(Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .filter(el -> el.getIm_scrittura().compareTo(new BigDecimal(1000)) == 0)
                    .filter(el -> el.getImTotaleDare().compareTo(new BigDecimal(1000)) == 0)
                    .filter(el -> el.getImTotaleAvere().compareTo(new BigDecimal(1000)) == 0)
                    .isPresent());
            Assertions.assertEquals(1, Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                    .orElse(new BulkList<>())
                    .size());
            Assertions.assertEquals(1, Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                    .orElse(new BulkList<>())
                    .size());
            Assertions.assertTrue(Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                    .orElse(new BulkList<>())
                    .stream().findFirst()
                    .filter(Movimento_cogeBulk::isSezioneDare)
                    .filter(el -> "P22010".equals(el.getCd_voce_ep()))
                    .filter(el -> el.getIm_movimento().compareTo(new BigDecimal(1000)) == 0)
                    .isPresent());
            Assertions.assertTrue(Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                    .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                    .orElse(new BulkList<>())
                    .stream().findFirst()
                    .filter(Movimento_cogeBulk::isSezioneAvere)
                    .filter(el -> "A00053".equals(el.getCd_voce_ep()))
                    .filter(el -> el.getIm_movimento().compareTo(new BigDecimal(1000)) == 0)
                    .isPresent());
        }

        //CONTROLLO ANALITICA
        {
            Assertions.assertFalse(Optional.ofNullable(result.getScritturaAnaliticaBulk()).isPresent(),"Scrittura analitica presente.");
        }
    }

    /**
     * Documento Generico {@code con Causale} su mono voce {@code Liquidato} con mandato di pagamento:
     * <p><b>Dati Documento Generico</b>
     * <pre>
     * Voce Bilancio: 22010 - Attrezzature scientifiche
     * CDR/GAE: 000.000.000/PTEST001
     * Importo: 1000,00
     * Causale: Rimborso01
     * Configurazione Causale:
     *       D - C13003 - Carta, cancelleria e stampati
     *       A - P11027 - Altri debiti verso il personale dipendente
     * </pre></p>
     * <b>Scrittura Economica Documento Generico</b>
     * <pre>
     *     Sezione   Importo      Conto
     *        D      1000,00      C13003 - Carta, cancelleria e stampati
     *        A      1000,00      P11027 - Altri debiti verso il personale dipendente
     * </pre>
     * <b>Scrittura Analitica Documento Generico</b>
     * <pre>
     *     Sezione   Importo      CDR/GAE                   Conto Analitico
     *        D      1000,00      000.000.000/PTEST001      C13003 - Carta, cancelleria e stampati
     * </pre>
     * <b>Scrittura Economica Mandato</b>
     * <pre>
     *     Sezione   Importo      Conto
     *        D      1000,00      P11027 - Altri debiti verso il personale dipendente
     *        A      1000,00      A00053 - Istituto tesoriere/cassiere
     * </pre>
     */
    @Test
    @Order(2)
    public void testDocumentoGenerico002() throws Exception {
        {
            Documento_genericoBulk documentoCogeBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(getUserContext(),
                            new Documento_genericoBulk("000", "GENERICO_S", "000.000", 2025,
                                    2L)))
                    .filter(Documento_genericoBulk.class::isInstance)
                    .map(Documento_genericoBulk.class::cast)
                    .orElse(null);
            ResultScrittureContabili result = proposeScritturaComponentSession.proposeScrittureContabili(
                    getUserContext(),
                    documentoCogeBulk);

            //CONTROLLO ECONOMICA
            {
                Assertions.assertEquals(new BigDecimal("1000.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getIm_scrittura).orElse(null));
                Assertions.assertEquals(new BigDecimal("1000.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getImTotaleDare).orElse(null));
                Assertions.assertEquals(new BigDecimal("1000.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getImTotaleAvere).orElse(null));

                BulkList<Movimento_cogeBulk> movimentiDare = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(1, movimentiDare.size());

                Optional<Movimento_cogeBulk> rigaDare = movimentiDare.stream().findAny();
                Assertions.assertTrue(rigaDare.isPresent(),"Riga dare non presente.");
                Assertions.assertEquals("C13003", rigaDare.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                Assertions.assertEquals(new BigDecimal("1000.00"), rigaDare.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

                BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(1, movimentiAvere.size());

                Optional<Movimento_cogeBulk> rigaAvere = movimentiAvere.stream().findAny();
                Assertions.assertTrue(rigaAvere.isPresent(),"Riga avere non presente.");
                Assertions.assertEquals("P11027", rigaAvere.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                Assertions.assertEquals(new BigDecimal("1000.00"), rigaAvere.map(Movimento_cogeBulk::getIm_movimento).orElse(null));
            }

            //CONTROLLO ANALITICA
            {
                Assertions.assertEquals(new BigDecimal("1000.00"), Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getIm_scrittura).orElse(null));
                Assertions.assertEquals(new BigDecimal("1000.00"), Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getImTotaleDare).orElse(null));
                Assertions.assertEquals(new BigDecimal("0"), Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getImTotaleAvere).orElse(null));

                List<Movimento_coanBulk> movimentiDare = Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getMovimentiDareColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(1, movimentiDare.size());

                Optional<Movimento_coanBulk> rigaDare = movimentiDare.stream().findAny();
                Assertions.assertTrue(rigaDare.isPresent(),"Riga dare analitica non presente.");
                Assertions.assertEquals("C13003", rigaDare.map(Movimento_coanBulk::getCd_voce_ana).orElse(null));
                Assertions.assertEquals("000.000.000", rigaDare.map(Movimento_coanBulk::getCd_centro_responsabilita).orElse(null));
                Assertions.assertEquals("PTEST001", rigaDare.map(Movimento_coanBulk::getCd_linea_attivita).orElse(null));
                Assertions.assertEquals(new BigDecimal("1000.00"), rigaDare.map(Movimento_coanBulk::getIm_movimento).orElse(null));

                List<Movimento_coanBulk> movimentiAvere = Optional.ofNullable(result.getScritturaAnaliticaBulk())
                        .map(Scrittura_analiticaBulk::getMovimentiAvereColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(0, movimentiAvere.size());
            }

            scritturaPartitaDoppiaFromDocumentoComponentSession.modificaConBulk(getUserContext(), documentoCogeBulk);
        }
        {
            MandatoBulk mandatoBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(getUserContext(),
                            new MandatoIBulk("000", 2025, 5L)))
                    .filter(MandatoBulk.class::isInstance)
                    .map(MandatoBulk.class::cast)
                    .orElse(null);

            ResultScrittureContabili result = proposeScritturaComponentSession.proposeScrittureContabili(
                    getUserContext(),
                    mandatoBulk);

            //CONTROLLO ECONOMICA
            {
                Assertions.assertEquals(new BigDecimal("1000.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getIm_scrittura).orElse(null));
                Assertions.assertEquals(new BigDecimal("1000.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getImTotaleDare).orElse(null));
                Assertions.assertEquals(new BigDecimal("1000.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getImTotaleAvere).orElse(null));

                BulkList<Movimento_cogeBulk> movimentiDare = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(1, movimentiDare.size());

                Optional<Movimento_cogeBulk> rigaDare = movimentiDare.stream().findAny();
                Assertions.assertTrue(rigaDare.isPresent(),"Riga dare non presente.");
                Assertions.assertEquals("P11027", rigaDare.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                Assertions.assertEquals(new BigDecimal("1000.00"), rigaDare.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

                BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(1, movimentiAvere.size());

                Optional<Movimento_cogeBulk> rigaAvere = movimentiAvere.stream().findAny();
                Assertions.assertTrue(rigaAvere.isPresent(),"Riga avere non presente.");
                Assertions.assertEquals("A00053", rigaAvere.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                Assertions.assertEquals(new BigDecimal("1000.00"), rigaAvere.map(Movimento_cogeBulk::getIm_movimento).orElse(null));
            }

            //CONTROLLO ANALITICA
            {
                Assertions.assertFalse(Optional.ofNullable(result.getScritturaAnaliticaBulk()).isPresent(),"Scrittura analitica presente.");
            }
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
            Documento_genericoBulk documentoCogeBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(getUserContext(),
                            new Documento_genericoBulk("000", "GENERICO_S", "000.000", 2024,
                                    1L)))
                    .filter(Documento_genericoBulk.class::isInstance)
                    .map(Documento_genericoBulk.class::cast)
                    .orElse(null);

            Assertions.assertThrows(ScritturaPartitaDoppiaNotEnabledException.class, () ->proposeScritturaComponentSession.proposeScrittureContabili(
                        getUserContext(),
                        documentoCogeBulk),
                    "Scrittura Economica non generabile/modificabile. L'esercizio contabile 2023 per il cds 000 risulta essere non aperto.");
        }
        {
            MandatoBulk mandatoBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(getUserContext(),
                            new MandatoIBulk("000", 2025, 6L)))
                    .filter(MandatoBulk.class::isInstance)
                    .map(MandatoBulk.class::cast)
                    .orElse(null);

            ResultScrittureContabili result = proposeScritturaComponentSession.proposeScrittureContabili(
                    getUserContext(),
                    mandatoBulk);

            //CONTROLLO ECONOMICA
            {
                Assertions.assertEquals(new BigDecimal("1000.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getIm_scrittura).orElse(null));
                Assertions.assertEquals(new BigDecimal("1000.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getImTotaleDare).orElse(null));
                Assertions.assertEquals(new BigDecimal("1000.00"), Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getImTotaleAvere).orElse(null));

                BulkList<Movimento_cogeBulk> movimentiDare = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(1, movimentiDare.size());

                Optional<Movimento_cogeBulk> rigaDare = movimentiDare.stream().findAny();
                Assertions.assertTrue(rigaDare.isPresent(),"Riga dare non presente.");
                Assertions.assertEquals("P22010", rigaDare.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                Assertions.assertEquals(new BigDecimal("1000.00"), rigaDare.map(Movimento_cogeBulk::getIm_movimento).orElse(null));

                BulkList<Movimento_cogeBulk> movimentiAvere = Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                        .map(Scrittura_partita_doppiaBulk::getMovimentiAvereColl)
                        .orElse(new BulkList<>());
                Assertions.assertEquals(1, movimentiAvere.size());

                Optional<Movimento_cogeBulk> rigaAvere = movimentiAvere.stream().findAny();
                Assertions.assertTrue(rigaAvere.isPresent(),"Riga avere non presente.");
                Assertions.assertEquals("A00053", rigaAvere.map(Movimento_cogeBulk::getCd_voce_ep).orElse(null));
                Assertions.assertEquals(new BigDecimal("1000.00"), rigaAvere.map(Movimento_cogeBulk::getIm_movimento).orElse(null));
            }

            //CONTROLLO ANALITICA
            {
                Assertions.assertFalse(Optional.ofNullable(result.getScritturaAnaliticaBulk()).isPresent(),"Scrittura analitica presente.");
            }
        }
    }
}

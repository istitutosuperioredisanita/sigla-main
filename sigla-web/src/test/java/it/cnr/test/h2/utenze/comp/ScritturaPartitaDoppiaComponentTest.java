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

package it.cnr.test.h2.utenze.comp;

import it.cnr.contab.coepcoan00.core.bulk.Movimento_cogeBulk;
import it.cnr.contab.coepcoan00.core.bulk.ResultScrittureContabili;
import it.cnr.contab.coepcoan00.core.bulk.Scrittura_partita_doppiaBulk;
import it.cnr.contab.docamm00.docs.bulk.Documento_genericoBulk;
import it.cnr.contab.utente00.nav.ejb.GestioneLoginComponentSession;
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

import static org.junit.Assert.*;

public class ScritturaPartitaDoppiaComponentTest extends DeploymentsH2 {
    @EJB
    private CRUDComponentSession crudComponentSession;
    @EJB
    private GestioneLoginComponentSession loginComponentSession;

    @Test
    @OperateOnDeployment(TEST_H2)
    @InSequence(1)
    public void testDocumentoGenericoSemplice() throws Exception {
        Documento_genericoBulk documentoCogeBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(new TestUserContext(),
                        new Documento_genericoBulk("000","GENERICO_S","000.000",2025,
                                1L)))
                .filter(Documento_genericoBulk.class::isInstance)
                .map(Documento_genericoBulk.class::cast)
                .orElse(null);
        ResultScrittureContabili result = Utility.createScritturaPartitaDoppiaComponentSession().proposeScrittureContabili(
                new TestUserContext(),
                documentoCogeBulk);
        assertTrue(Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                .filter(el->el.getIm_scrittura().compareTo(new BigDecimal(1000))==0)
                .filter(el->el.getImTotaleDare().compareTo(new BigDecimal(1000))==0)
                .filter(el->el.getImTotaleAvere().compareTo(new BigDecimal(1000))==0)
                .isPresent());
        assertEquals(1, (long) Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                .map(Scrittura_partita_doppiaBulk::getMovimentiDareColl)
                .orElse(new BulkList<>())
                .size());
        assertEquals(1, (long) Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
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
/*
        Utility.createScritturaPartitaDoppiaFromDocumentoComponentSession().modificaConBulk(new TestUserContext(), documentoCogeBulk);

        MandatoBulk mandatoBulk = Optional.ofNullable(crudComponentSession.findByPrimaryKey(new TestUserContext(),
                        new MandatoBulk("000",2025,1L)))
                .filter(MandatoBulk.class::isInstance)
                .map(MandatoBulk.class::cast)
                .orElse(null);
        result = Utility.createScritturaPartitaDoppiaComponentSession().proposeScrittureContabili(
                new TestUserContext(),
                documentoCogeBulk);
        assertTrue(Optional.ofNullable(result.getScritturaPartitaDoppiaBulk())
                .filter(el->el.getIm_scrittura().compareTo(new BigDecimal(1000))==0)
                .filter(el->el.getImTotaleDare().compareTo(new BigDecimal(1000))==0)
                .filter(el->el.getImTotaleAvere().compareTo(new BigDecimal(1000))==0)
                .isPresent());
 */
    }
}

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

package it.cnr.test.oracle.doccont.comp;

import it.cnr.contab.doccont00.core.bulk.MandatoComunicaDatiBulk;
import it.cnr.contab.util.TestUserContext;
import it.cnr.jada.ejb.CRUDComponentSession;
import it.cnr.test.oracle.DeploymentsOracle;
import org.jboss.arquillian.container.test.api.OperateOnDeployment;

import jakarta.ejb.EJB;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class ComunicaDatiPagamentiTest extends DeploymentsOracle {

    @Test
    public void test() throws Exception {
        final TestUserContext testUserContext = new TestUserContext();
        final MandatoComunicaDatiBulk mandatoComunicaDatiBulk = new MandatoComunicaDatiBulk();
        mandatoComunicaDatiBulk.setEsercizio(2020);
        mandatoComunicaDatiBulk.setCd_cds("035");
        mandatoComunicaDatiBulk.setPg_mandato(Long.valueOf(3146));

        List<MandatoComunicaDatiBulk> dati =
                crudComponentSession.find(testUserContext, MandatoComunicaDatiBulk.class, "recuperoDati", testUserContext, mandatoComunicaDatiBulk, null, null);
        Assertions.assertNotNull(dati);
        Assertions.assertEquals(Boolean.TRUE, !dati.isEmpty());
    }
}
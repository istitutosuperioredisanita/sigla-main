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

package it.cnr.test.oracle.config00.comp;

import it.cnr.contab.config00.contratto.bulk.ContrattoBulk;
import it.cnr.contab.config00.contratto.bulk.ContrattoDatiSintesiBulk;
import it.cnr.contab.util.TestUserContext;
import it.cnr.test.oracle.DeploymentsOracle;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;


public class ContrattoDatiSintesiRestTest extends DeploymentsOracle {

    @Test
    public void test() throws Exception {
        final ContrattoDatiSintesiBulk contrattoDatiSintesiBulk = new ContrattoDatiSintesiBulk();

        List<ContrattoDatiSintesiBulk> dati =
                crudComponentSession.find(getUserContext(), ContrattoDatiSintesiBulk.class, "recuperoDati", getUserContext(), contrattoDatiSintesiBulk, ContrattoBulk.NATURA_CONTABILE_PASSIVO, 63470, "123.005");
        Assertions.assertNotNull(dati);
        Assertions.assertEquals(Boolean.TRUE,!dati.isEmpty());
    }
}
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

import it.cnr.contab.utente00.nav.ejb.GestioneLoginComponentSession;
import it.cnr.contab.utenze00.bulk.UtenteBulk;
import it.cnr.contab.util.TestUserContext;
import it.cnr.contab.web.rest.local.config00.ContextRemote;
import it.cnr.test.util.Deployments;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.naming.NamingException;
import java.util.Optional;

@ExtendWith(ArquillianExtension.class)
public class LoginComponentTest extends Deployments {
    private GestioneLoginComponentSession loginComponentSession;
    private ContextRemote contextRemote;
    @BeforeEach
    public void lookupRemoteEJBs() throws NamingException {
        super.lookupRemoteEJBs();
        loginComponentSession = lookup("CNRUTENZE00_NAV_EJB_GestioneLoginComponentSession", GestioneLoginComponentSession.class);
        contextRemote = lookup("ContextResource", ContextRemote.class);
    }

    @Test
    @Order(1)
    public void testEsercizio() throws Exception {
        UtenteBulk utente = Optional.ofNullable(crudComponentSession.findByPrimaryKey(getUserContext(), new UtenteBulk("TEST")))
                .filter(UtenteBulk.class::isInstance)
                .map(UtenteBulk.class::cast)
                .orElse(null);
        Assertions.assertEquals(Boolean.TRUE, Optional.ofNullable(utente).isPresent());
        Assertions.assertEquals("Utenza di TEST", utente.getDs_utente());

        java.lang.Integer[] listaEsercizio = loginComponentSession.listaEserciziPerUtente(getUserContext(), utente);
        java.lang.Integer[] espected = {contextRemote.getLiquibasBootstrapEsercizio()};
        Assertions.assertArrayEquals(espected, listaEsercizio);
    }
}

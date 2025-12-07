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

package it.cnr.test.util;

import it.cnr.contab.utente00.nav.ejb.GestioneLoginComponentSession;
import it.cnr.jada.ejb.CRUDComponentSession;
import it.cnr.jada.ejb.GenericComponentSession;
import org.jboss.arquillian.container.test.api.ContainerController;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;


@ExtendWith(ArquillianExtension.class)
public abstract class Deployments {
    public static final String TEST_H2 = "test-h2",
            TEST_ORACLE = "test-oracle", CONTAINER_NAME = "wildfly-bootable";
    private final static Logger LOGGER = LoggerFactory.getLogger(Deployments.class);
    @ArquillianResource
    protected static ContainerController controller;
    @ArquillianResource
    protected Deployer deployer;
    protected static Context context;
    protected CRUDComponentSession crudComponentSession;

    @BeforeAll
    public static void setupRemoteConnection() throws Exception {
        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, "org.wildfly.naming.client.WildFlyInitialContextFactory");
        props.put(Context.PROVIDER_URL, "http-remoting://localhost:8080");
        context = new InitialContext(props);
    }

    @BeforeEach
    public void lookupRemoteEJBs() throws NamingException {
        crudComponentSession = lookup("JADAEJB_CRUDComponentSession", CRUDComponentSession.class);
    }

    protected <T extends Object> T lookup(String name, Class<T> clazz) throws NamingException {
        return (T) context.lookup(String.format("sigla/%s!%s", name, clazz.getName()));
    }

}

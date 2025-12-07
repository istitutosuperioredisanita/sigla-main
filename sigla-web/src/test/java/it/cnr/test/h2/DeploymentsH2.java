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

package it.cnr.test.h2;

import it.cnr.test.util.Deployments;

public class DeploymentsH2 extends Deployments {
    /*
    @Test
    @Order(-1)
    
    public void startupH2() {
        controller.start(CONTAINER_NAME,
                Stream.of(
                        new AbstractMap.SimpleEntry<>("port", "12347"),
                        new AbstractMap.SimpleEntry<>("javaVmArguments",
                                "-agentlib:jdwp=transport=dt_socket,address=8789,server=y,suspend=n " +
                                " -Xmx1024m" +
                                " -Djava.net.preferIPv4Stack=true" +
                                " -Dsigla.db.driver=h2" +
                                " -Dsigla.db.url=jdbc:h2:mem:sigladb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE" +
                                " -Dsigla.db.user=sa" +
                                " -Dsigla.db.password=sa" +
                                " -Dspring.profiles.active=liquibase" +
                                " -Dliquibase.bootstrap.esercizio=2024")
                ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
        );
        deployer.deploy(TEST_H2);
    }
     */
}

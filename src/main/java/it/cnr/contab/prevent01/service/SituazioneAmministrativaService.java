/*
 * Copyright (C) 2019  Consiglio Nazionale delle Ricerche
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

package it.cnr.contab.prevent01.service;

import it.cnr.contab.prevent01.ejb.SituazioneAmministrativaComponentSession;
import it.cnr.contab.utenze00.bp.CNRUserContext;
import it.cnr.jada.DetailedRuntimeException;
import it.cnr.jada.UserContext;
import it.cnr.jada.util.ejb.EJBCommonServices;
import jakarta.mail.*;
import jakarta.mail.search.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class SituazioneAmministrativaService implements InitializingBean {
    private static final Logger logger = LoggerFactory.getLogger(SituazioneAmministrativaService.class);
    private SituazioneAmministrativaComponentSession situazioneAmministrativaComponentSession;
    @Autowired
    Environment env;

    @Value("${sitamm.sendAlertMail.cron.enabled}")
    private Boolean sendAlertMailCronEnable;
    @Value("${sitamm.sendAlertMail.cron.expression}")
    private String sendAlertMailCronExpression;
    @Value("${sitamm.sendAlertMail.cron.mail.to}")
    private String sendAlertMailCronMailTo;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.situazioneAmministrativaComponentSession = Optional.ofNullable(EJBCommonServices.createEJB("CNRPREVENT01_EJB_SituazioneAmministrativaComponentSession"))
                .filter(SituazioneAmministrativaComponentSession.class::isInstance)
                .map(SituazioneAmministrativaComponentSession.class::cast)
                .orElseThrow(() -> new DetailedRuntimeException("cannot find ejb CNRPREVENT01_EJB_SituazioneAmministrativaComponentSession"));
    }

    public void executeSendAlertMail() {
        try {
            if (sendAlertMailCronEnable) {
                logger.info("Situazione Amministrativa Service: sendAlertMail started at: {}", LocalDateTime.now());

                UserContext userContext = new CNRUserContext("MESSAGGI", null, LocalDate.now().getYear(), null, null, null);
                situazioneAmministrativaComponentSession.sendAlertMail(userContext);

                logger.info("Situazione Amministrativa Service: sendAlertMail end at: {}", LocalDateTime.now());
            } else {
                logger.info("Situazione Amministrativa Service: sendAlertMail disabled");
            }
        } catch (Throwable _ex) {
            logger.error("Situazione Amministrativa Service: sendAlertMail ScheduleExecutor error", _ex);
        }
    }


}

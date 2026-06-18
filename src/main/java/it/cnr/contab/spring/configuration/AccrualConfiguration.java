package it.cnr.contab.spring.configuration;

import it.iss.accrual.xbrl.AccrualService;
import it.iss.accrual.xbrl.AccrualServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AccrualConfiguration {
    private final static Logger log = LoggerFactory.getLogger(AccrualConfiguration.class);
    @Bean
    public AccrualService accrualService() {
        log.info("Creazione AccrualService");
       return new AccrualServiceImpl();
    }

}

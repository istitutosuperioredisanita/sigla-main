package it.cnr.contab.config;

import jakarta.annotation.Resource;
import org.infinispan.Cache;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.spring.embedded.provider.SpringEmbeddedCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableCaching
@Configuration
public class CacheConfiguration {

    private final static Logger LOG = LoggerFactory.getLogger(CacheConfiguration.class);

    @Resource(lookup = "java:jboss/infinispan/container/server")
    private EmbeddedCacheManager wildflyCacheContainer;

    @Resource(lookup = "java:jboss/infinispan/cache/server/accessi")
    private Cache<?, ?> accessi;

    @Resource(lookup = "java:jboss/infinispan/cache/server/tree")
    private Cache<?, ?> tree;

    @Bean
    public CacheManager cacheManager() {
        LOG.info("Inizializzazione SpringEmbeddedCacheManager (Infinispan 16.0.11) per Spring 7.");

        if (wildflyCacheContainer != null) {
            LOG.info("Cache registrate nel container di WildFly: {}", wildflyCacheContainer.getCacheConfigurationNames());
            // Il costruttore ufficiale in Infinispan 16 accetta direttamente l'EmbeddedCacheManager
            return new SpringEmbeddedCacheManager(wildflyCacheContainer);
        }

        throw new IllegalStateException("Impossibile recuperare il container JNDI 'java:jboss/infinispan/container/server'");
    }
}
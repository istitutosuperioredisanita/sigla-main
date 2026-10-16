/*
 * Copyright (C) 2022  Consiglio Nazionale delle Ricerche
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

/**
 * Le cache "accessi" e "tree" sono definite nel subsystem infinispan di WildFly
 * (cache-container "server", vedi configure-wildfly.cli).
 * Il cache container appartiene al server e sopravvive ai redeploy, quindi qui
 * NON si chiama defineConfiguration (ISPN000453 se la cache esiste già).
 */
@EnableCaching
@Configuration
public class CacheConfiguration {

    private final static Logger LOG = LoggerFactory.getLogger(CacheConfiguration.class);

    @Resource(lookup = "java:jboss/infinispan/container/server")
    private EmbeddedCacheManager cacheManager;

    // I servizi delle cache del subsystem partono on-demand: il lookup del binding
    // le avvia e le registra nel container prima che Spring crei il CacheManager.
    // Se una cache manca nel CLI, il deploy fallisce qui con il nome del binding.
    @Resource(lookup = "java:jboss/infinispan/cache/server/accessi")
    private Cache<?, ?> accessi;

    @Resource(lookup = "java:jboss/infinispan/cache/server/tree")
    private Cache<?, ?> tree;

    @Bean
    public CacheManager cacheManager() {
        LOG.info("Cache Infinispan disponibili: {}", cacheManager.getCacheConfigurationNames());
        return new SpringEmbeddedCacheManager(cacheManager);
    }

}

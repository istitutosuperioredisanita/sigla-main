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
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.spring.embedded.provider.SpringEmbeddedCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@EnableCaching
@Configuration
public class CacheConfiguration {

    private final static Logger LOG = LoggerFactory.getLogger(CacheConfiguration.class);

    @Resource(lookup = "java:jboss/infinispan/container/server")
    private EmbeddedCacheManager cacheManager;

    @Bean
    public CacheManager cacheManager() {
        // Define configuration for missing caches
        org.infinispan.configuration.cache.Configuration config =
                new org.infinispan.configuration.cache.ConfigurationBuilder()
                        .memory()
                        .maxCount(10000)
                        .expiration()
                        .lifespan(1, TimeUnit.HOURS)
                        .maxIdle(30, TimeUnit.MINUTES)
                        .encoding()
                        .mediaType("application/x-java-object") // Use Java serialization
                        .build();

        // Define the cache if it doesn't exist
        cacheManager.defineConfiguration("accessi", config);
        cacheManager.defineConfiguration("tree", config);

        return new SpringEmbeddedCacheManager(cacheManager);
    }

}

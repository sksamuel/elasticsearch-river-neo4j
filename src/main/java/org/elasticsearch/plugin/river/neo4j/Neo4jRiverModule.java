package org.elasticsearch.plugin.river.neo4j;

import org.elasticsearch.common.inject.AbstractModule;
import org.elasticsearch.river.River;

/**
 * @author Stephen Samuel
 */
public class Neo4jRiverModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(River.class).to(Neo4jDriver.class).asEagerSingleton();
    }
}
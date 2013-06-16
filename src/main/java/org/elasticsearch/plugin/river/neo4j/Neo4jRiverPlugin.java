package org.elasticsearch.plugin.river.neo4j;

import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.plugins.AbstractPlugin;
import org.elasticsearch.river.RiversModule;

/**
 * @author Stephen Samuel
 */
public class Neo4jRiverPlugin extends AbstractPlugin {

    @Inject
    public Neo4jRiverPlugin() {
    }

    @Override
    public String name() {
        return "river-neo4j";
    }

    @Override
    public String description() {
        return "River Neo4j Plugin";
    }

    public void onModule(RiversModule module) {
        module.registerRiver("neo4j", Neo4jRiverModule.class);
    }
}

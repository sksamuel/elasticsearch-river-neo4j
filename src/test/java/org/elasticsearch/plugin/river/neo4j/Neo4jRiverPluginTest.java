package org.elasticsearch.plugin.river.neo4j;

import org.elasticsearch.river.RiversModule;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author Stephen Samuel
 */
public class Neo4jRiverPluginTest {

    @Test
    public void thatModuleIsRegistered() {
        RiversModule module = mock(RiversModule.class);
        Neo4jRiverPlugin plugin = new Neo4jRiverPlugin();
        assertEquals("river-neo4j", plugin.name());
        plugin.onModule(module);
        verify(module).registerRiver("neo4j", Neo4jRiverModule.class);
    }
}

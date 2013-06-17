package org.elasticsearch.plugin.river.neo4j;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.river.RiverName;
import org.elasticsearch.river.RiverSettings;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.elasticsearch.common.settings.ImmutableSettings.settingsBuilder;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * @author Stephen Samuel
 */
public class Neo4jDriverTest {

    RiverName name = new RiverName("type", "neo4j");
    Client client = mock(Client.class);

    @Test
    public void settingsAreTakenFromNeo4jObjectIfSet() {

        Settings globalSettings = settingsBuilder().loadFromClasspath("settings.yml").build();
        Map map = settingsBuilder().loadFromClasspath("dummy_river_settings.json").build().getAsMap();
        RiverSettings riverSettings = new RiverSettings(globalSettings, map);
        Neo4jDriver driver = new Neo4jDriver(name, riverSettings, "myindex", client);

        assertEquals(7654, driver.getPort());
        assertEquals("time", driver.getTimestampField());
        assertEquals("neoindex", driver.getIndex());
        assertEquals(500, driver.getInterval());
        assertEquals("1.2.3.4", driver.getHostname());
    }

    @Test
    public void settingsAreDefaultsIfJsonObjectNotSet() {

        Map<String, Object> map = new HashMap<String, Object>();
        RiverSettings settings = new RiverSettings(mock(Settings.class), map);
        Neo4jDriver driver = new Neo4jDriver(name, settings, "myindex", client);

        assertEquals(Neo4jDriver.DEFAULT_NEO_HOSTNAME, driver.getHostname());
        assertEquals(Neo4jDriver.DEFAULT_NEO_PORT, driver.getPort());
        assertEquals(Neo4jDriver.DEFAULT_NEO_TIMESTAMP_FIELD, driver.getTimestampField());
        assertEquals(Neo4jDriver.DEFAULT_NEO_INDEX, driver.getIndex());
        assertEquals(Neo4jDriver.DEFAULT_NEO_INTERVAL, driver.getInterval());
    }
}

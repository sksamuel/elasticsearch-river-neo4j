package org.elasticsearch.plugin.river.neo4j;

import java.io.IOException;
import java.io.InputStream;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.river.RiverName;
import org.elasticsearch.river.RiverSettings;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.common.Classes;
import org.elasticsearch.common.io.Streams;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.xcontent.XContentHelper;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * @author Stephen Samuel
 * @author Andre Crouch
 */
public class Neo4jDriverTest {

    RiverName name = new RiverName("type", "neo4j");
    Client client = mock(Client.class);
    private ClassLoader classLoader = Classes.getDefaultClassLoader();

    @Test
    public void settingsAreTakenFromNeo4jObjectIfSet() throws IOException {
        InputStream in = this.classLoader.getResourceAsStream("dummy_river_settings.json");
        RiverSettings riverSettings = new RiverSettings(ImmutableSettings.settingsBuilder().build(), XContentHelper.convertToMap(
                    Streams.copyToByteArray(in), false).v2());
        Neo4jDriver driver = new Neo4jDriver(name, riverSettings, "myindex", client);

        assertEquals("time", driver.getTimestampField());
        assertEquals("neoindex", driver.getIndex());
        assertEquals(500, driver.getInterval());
        assertEquals("http://192.56.57.89:7888/db/data", driver.getUri());
        assertEquals("turtle", driver.getType());
    }

    @Test
    public void settingsAreDefaultsIfJsonObjectNotSet() {

        Map<String, Object> map = new HashMap<String, Object>();
        RiverSettings settings = new RiverSettings(mock(Settings.class), map);
        Neo4jDriver driver = new Neo4jDriver(name, settings, "myindex", client);

        assertEquals(Neo4jDriver.DEFAULT_NEO_URI, driver.getUri());
        assertEquals(Neo4jDriver.DEFAULT_NEO_TIMESTAMP_FIELD, driver.getTimestampField());
        assertEquals(Neo4jDriver.DEFAULT_NEO_INDEX, driver.getIndex());
        assertEquals(Neo4jDriver.DEFAULT_NEO_INTERVAL, driver.getInterval());
        assertEquals(Neo4jDriver.DEFAULT_NEO_TYPE, driver.getType());
    }

    @Test
    public void closingRiverShutsDownExecutor() {

        Map<String, Object> map = new HashMap<String, Object>();
        RiverSettings settings = new RiverSettings(mock(Settings.class), map);
        Neo4jDriver driver = new Neo4jDriver(name, settings, "myindex", client);

        assertNull(driver.executor);
        driver.start();
        assertNotNull(driver.executor);
        assertFalse(driver.executor.isShutdown());
        driver.close();
        assertTrue(driver.executor.isShutdown());
    }

    @Test
    public void closingNonStartedRiverShutsDownWithoutException() {

        Map<String, Object> map = new HashMap<String, Object>();
        RiverSettings settings = new RiverSettings(mock(Settings.class), map);
        Neo4jDriver driver = new Neo4jDriver(name, settings, "myindex", client);
        driver.close();
    }
}

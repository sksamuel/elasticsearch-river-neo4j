package org.elasticsearch.plugin.river.neo4j;

import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.io.FileSystemUtils;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.internal.InternalSettingsPerparer;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.neo4j.rest.SpringCypherRestGraphDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static org.elasticsearch.client.Requests.clusterHealthRequest;
import static org.elasticsearch.client.Requests.countRequest;
import static org.elasticsearch.common.io.Streams.copyToStringFromClasspath;
import static org.elasticsearch.common.settings.ImmutableSettings.settingsBuilder;
import static org.elasticsearch.index.query.QueryBuilders.queryString;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Stephen Samuel
 */
public class Neo4jRiverIntTest {

    private static Logger logger = LoggerFactory.getLogger(Neo4jRiverIntTest.class);

    private Node node;
    private String river = "neo4j-river-" + UUID.randomUUID().toString();
    private String index;
    private SpringCypherRestGraphDatabase db;
    private String type;

    public void shutdown() {
        logger.debug("Shutdown elastic...");
        node.close();
        logger.debug("...goodbye elastic");
    }

    @Before
    public void setupElasticAndNeo4jClient() throws Exception {

        Settings globalSettings = settingsBuilder().loadFromClasspath("settings.yml").build();
        String json = copyToStringFromClasspath("/neo4j-inttest-river.json");
        Settings riverSettings = settingsBuilder().loadFromSource(json).build();

        index = riverSettings.get("index.name");
        type = riverSettings.get("index.type");
        String uri = riverSettings.get("neo4j.uri");

        logger.debug("Connecting to neo4j @ {}", uri);
        db = new SpringCypherRestGraphDatabase(uri);

        logger.debug("Starting local elastic...");
        Tuple<Settings, Environment> initialSettings = InternalSettingsPerparer.prepareSettings(globalSettings, true);

        if (!initialSettings.v2().configFile().exists()) {
            FileSystemUtils.mkdirs(initialSettings.v2().configFile());
        }

        if (!initialSettings.v2().logsFile().exists()) {
            FileSystemUtils.mkdirs(initialSettings.v2().logsFile());
        }

        node = nodeBuilder().local(true).settings(globalSettings).node();

        logger.info("Create river [{}]", river);
        node.client().prepareIndex("_river", river, "_meta").setSource(json).execute().actionGet();

        logger.debug("Running Cluster Health");
        ClusterHealthResponse clusterHealth = node.client().admin().cluster()
                .health(clusterHealthRequest().waitForGreenStatus())
                .actionGet();
        logger.info("Done Cluster Health, status " + clusterHealth.getStatus());

        GetResponse response = node.client().prepareGet("_river", river, "_meta").execute().actionGet();
        assertTrue(response.isExists());

        logger.debug("...elasticized ok");
    }

    @Test
    public void testAddAndRemoveNodes() throws InterruptedException {

        Thread.sleep(200); // allow river to start
        String name = UUID.randomUUID().toString();

        // add node to neo4j
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("name", name);
        Transaction tx = db.beginTx();
        ArrayList<String> labels = new ArrayList<String>();
		org.neo4j.graphdb.Node n = db.createNode(map, labels);
        tx.success();

        CountResponse resp = null;

        int k = 0;
        while (k++ < 100) {

            Thread.sleep(1000); // time for poller to index
            refreshIndex();

            logger.debug("Count request [index={}, type={}, name={}]", new Object[]{index, type, name});
            resp = node.client().count(countRequest(index).types(type).source(queryString(name).defaultField("name").toString())).actionGet();
            if (1 == resp.getCount())
                break;

        }
        assertEquals(1, resp.getCount());

        db.remove(n);

        k = 0;
        while (k++ < 100) {

            Thread.sleep(1000); // time for poller to index
            refreshIndex();

            logger.debug("Count request [index={}, type={}, name={}]", new Object[]{index, type, name});
            resp = node.client().count(countRequest(index).types(type).source(queryString(name).defaultField("name").toString())).actionGet();
            if (0 == resp.getCount())
                break;

        }

        assertEquals(0, resp.getCount());

        shutdown();
    }

    /**
     * I use this test for a shell like experience, has to be cancelled manually.
     */
    // @Test
    public void infiniteTest() throws InterruptedException {
        Thread.sleep(200); // allow river to start
        while (true) {
            Thread.sleep(200); // time for poller to index
            refreshIndex();
            CountResponse
                    resp =
                    node.client().count(countRequest(index).types(type).source(queryString("mowbray").defaultField("manager").toString())).actionGet();
            logger.debug("How many moggas? {} !", resp.getCount());
        }
    }

    private void refreshIndex() {
        node.client().admin().indices().prepareRefresh(index).execute().actionGet();
    }
}

package org.elasticsearch.plugin.river.neo4j;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.river.AbstractRiverComponent;
import org.elasticsearch.river.River;
import org.elasticsearch.river.RiverIndexName;
import org.elasticsearch.river.RiverName;
import org.elasticsearch.river.RiverSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.elasticsearch.common.xcontent.support.XContentMapValues.*;

/**
 * @author Stephen Samuel
 */
public class Neo4jDriver extends AbstractRiverComponent implements River {

    static final int DEFAULT_NEO_INTERVAL = 60000;
    static final String DEFAULT_NEO_INDEX = "neo4j-index";
    static final String DEFAULT_NEO_TYPE = "node";
    static final String DEFAULT_NEO_URI = "http://localhost:7474/db/data";
    static final String DEFAULT_NEO_TIMESTAMP_FIELD = "timestamp";

    private static Logger logger = LoggerFactory.getLogger(Neo4jDriver.class);

    private final String uri;
    private final String index;
    private final String timestampField;
    private final int interval;
    private final RiverSettings settings;
    private final Client client;
    private final String type;
    private ExecutorService executor;
    private Neo4jIndexer indexer;
    private Neo4jPoller poller;

    @Inject
    public Neo4jDriver(RiverName riverName, RiverSettings settings, @RiverIndexName final String riverIndexName, final Client client) {
        super(riverName, settings);
        this.settings = settings;
        this.client = client;

        uri = nodeStringValue(extractValue("neo4j.uri", settings.settings()), DEFAULT_NEO_URI);
        timestampField = nodeStringValue(extractValue("neo4j.timestampField", settings.settings()), DEFAULT_NEO_TIMESTAMP_FIELD);
        interval = nodeIntegerValue(extractValue("neo4j.interval", settings.settings()), DEFAULT_NEO_INTERVAL);
        index = nodeStringValue(extractValue("index.name", settings.settings()), DEFAULT_NEO_INDEX);
        type = nodeStringValue(extractValue("index.name", settings.settings()), DEFAULT_NEO_TYPE);

        logger.debug("Neo4j settings [uri={}]", new Object[]{uri});
        logger.debug("River settings [indexName={}, interval={}, timestampField={}]", new Object[]{index, interval, timestampField});

    }

    @Override
    public void start() {
        logger.info("Starting neo4j river");

        SimpleIndexingStrategy strategy = new SimpleIndexingStrategy();
        indexer = new Neo4jIndexer(client, index, type, strategy);
        Neo4jClient neo4j = new Neo4jClient(uri, indexer);
        poller = new Neo4jPoller(neo4j, interval);

        executor = Executors.newFixedThreadPool(2);
        executor.submit(indexer);
        executor.submit(poller);

        logger.debug("Neo4j river started");
    }

    @Override
    public void close() {
        if (executor == null)
            return;

        logger.debug("Shutting down executor");
        executor.shutdown();
        logger.debug("Will wait 30 seconds for previous tasks to finish...");
        try {
            executor.awaitTermination(30, TimeUnit.SECONDS);
            logger.debug("...termination completed cleanly");
        } catch (InterruptedException e) {
            logger.debug("Shutdown was interrupted. I guess someone didn't want to wait");
        }
    }

    public String getUri() {
        return uri;
    }

    public String getIndex() {
        return index;
    }

    public String getType() {
        return type;
    }

    public String getTimestampField() {
        return timestampField;
    }

    public int getInterval() {
        return interval;
    }
}

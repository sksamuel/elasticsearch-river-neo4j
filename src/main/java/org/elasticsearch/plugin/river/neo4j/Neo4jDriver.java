package org.elasticsearch.plugin.river.neo4j;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.xcontent.support.XContentMapValues;
import org.elasticsearch.river.AbstractRiverComponent;
import org.elasticsearch.river.River;
import org.elasticsearch.river.RiverIndexName;
import org.elasticsearch.river.RiverName;
import org.elasticsearch.river.RiverSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stephen Samuel
 */
public class Neo4jDriver extends AbstractRiverComponent implements River {

    static final int DEFAULT_NEO_PORT = 7474;
    static final int DEFAULT_NEO_INTERVAL = 60000;
    static final String DEFAULT_NEO_INDEX = "neo4j-index";
    static final String DEFAULT_NEO_HOSTNAME = "localhost";
    static final String DEFAULT_NEO_TIMESTAMP_FIELD = "timestmap";

    private static Logger logger = LoggerFactory.getLogger(Neo4jDriver.class);

    private final String hostname;
    private final String index;
    private final int port;
    private final String timestampField;
    private final int interval;

    private final RiverSettings settings;
    private final Client client;

    @Inject
    public Neo4jDriver(RiverName riverName, RiverSettings settings, @RiverIndexName final String riverIndexName, final Client client) {
        super(riverName, settings);
        this.settings = settings;
        this.client = client;

        hostname =
                XContentMapValues.nodeStringValue(XContentMapValues.extractValue("neo4j.hostname", settings.settings()),
                        DEFAULT_NEO_HOSTNAME);
        port = XContentMapValues.nodeIntegerValue(XContentMapValues.extractValue("neo4j.port", settings.settings()), DEFAULT_NEO_PORT);
        timestampField =
                XContentMapValues.nodeStringValue(XContentMapValues.extractValue("neo4j.timestampField", settings.settings()),
                        DEFAULT_NEO_TIMESTAMP_FIELD);
        interval = XContentMapValues.nodeIntegerValue(XContentMapValues.extractValue("neo4j.interval", settings.settings()),
                DEFAULT_NEO_INTERVAL);
        index = XContentMapValues.nodeStringValue(XContentMapValues.extractValue("index.name", settings.settings()), DEFAULT_NEO_INDEX);

        logger.debug("Neo4j settings [hostname={}, port={}]", new Object[]{hostname, port});
        logger.debug("River settings [indexName={}, interval={}, timestampField={}]", new Object[]{index, interval, timestampField});
    }

    @Override
    public void start() {
        logger.info("Starting neo4j river driver");
    }

    @Override
    public void close() {
    }

    public String getHostname() {
        return hostname;
    }

    public String getIndex() {
        return index;
    }

    public int getPort() {
        return port;
    }

    public String getTimestampField() {
        return timestampField;
    }

    public int getInterval() {
        return interval;
    }
}

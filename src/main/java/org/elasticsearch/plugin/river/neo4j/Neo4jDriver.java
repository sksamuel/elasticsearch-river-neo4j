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

import java.util.Map;

/**
 * @author Stephen Samuel
 */
public class Neo4jDriver extends AbstractRiverComponent implements River {

    static final int DEFAULT_NEO_PORT = 7474;
    static final String DEFAULT_NEO_INDEX = "neo4j-index";
    static final String DEFAULT_NEO_HOSTNAME = "localhost";

    private static Logger logger = LoggerFactory.getLogger(Neo4jDriver.class);

    private final String hostname;
    private final String index;
    private final int port;

    private final RiverSettings settings;
    private final Client client;

    @Inject
    public Neo4jDriver(RiverName riverName, RiverSettings settings, @RiverIndexName final String riverIndexName, final Client client) {
        super(riverName, settings);
        this.settings = settings;
        this.client = client;

        if (settings.settings().containsKey("redis")) {
            Map<String, Object> redisSettings = (Map<String, Object>) settings.settings().get("redis");
            hostname = XContentMapValues.nodeStringValue(redisSettings.get("hostname"), DEFAULT_NEO_HOSTNAME);
            port = XContentMapValues.nodeIntegerValue(redisSettings.get("port"), DEFAULT_NEO_PORT);
        } else {
            hostname = DEFAULT_NEO_HOSTNAME;
            port = DEFAULT_NEO_PORT;
        }

        if (settings.settings().containsKey("index")) {
            Map<String, Object> redisSettings = (Map<String, Object>) settings.settings().get("index");
            index = XContentMapValues.nodeStringValue(redisSettings.get("name"), DEFAULT_NEO_INDEX);
        } else {
            index = DEFAULT_NEO_INDEX;
        }

        logger.debug("Neo4j settings [hostname={}, port={}]", new Object[]{hostname, port});
        logger.debug("River settings [indexName={}]", new Object[]{index});
    }

    @Override
    public void start() {
        logger.info("Starting neo4j river driver");

    }

    @Override
    public void close() {
    }

}

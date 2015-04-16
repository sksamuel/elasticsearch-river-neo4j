package org.elasticsearch.plugin.river.neo4j;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.xcontent.support.XContentMapValues;
import org.elasticsearch.river.AbstractRiverComponent;
import org.elasticsearch.river.River;
import org.elasticsearch.river.RiverIndexName;
import org.elasticsearch.river.RiverName;
import org.elasticsearch.river.RiverSettings;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.neo4j.rest.SpringCypherRestGraphDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Stephen Samuel
 * @author Andre Crouch
 */
public class Neo4jDriver extends AbstractRiverComponent implements River {

    static final int DEFAULT_NEO_INTERVAL = 60000;
    static final String DEFAULT_NEO_INDEX = "neo4j-index";
    static final String DEFAULT_NEO_TYPE = "node";
    static final String DEFAULT_NEO_URI = "http://localhost:7474/db/data";
    static final String DEFAULT_NEO_TIMESTAMP_FIELD = "timestamp";

    private static Logger logger = LoggerFactory.getLogger(Neo4jDriver.class);

    private final String uri;
    private final List<Label> labels = new ArrayList<>();
    private final String index;
    private final String timestampField;
    private final int interval;
    private final Client client;
    private final String indexFromLabel;
    private final String typeFromLabel;
    private final String type;
    ExecutorService executor;

    private SimpleIndexingStrategy indexingStrategy = new SimpleIndexingStrategy();
    private SimpleDeletingStrategy deletingStategy = new SimpleDeletingStrategy();

    @Inject
    public Neo4jDriver(RiverName riverName, RiverSettings settings, @RiverIndexName final String riverIndexName, final Client client) {
        super(riverName, settings);
        this.client = client;

        uri = XContentMapValues.nodeStringValue(XContentMapValues.extractValue("neo4j.uri", settings.settings()), DEFAULT_NEO_URI);
        List<Object> neo4jLabels = XContentMapValues.extractRawValues("neo4j.labels", settings.settings());
        String label;
        if(XContentMapValues.isArray(neo4jLabels)) {
            for (Object neo4jLabel : neo4jLabels) {
                label = XContentMapValues.nodeStringValue(neo4jLabel, null);
                labels.add(DynamicLabel.label(label));
            }
        }
        timestampField = XContentMapValues.nodeStringValue(XContentMapValues.extractValue("neo4j.timestampField", settings.settings()), DEFAULT_NEO_TIMESTAMP_FIELD);
        interval = XContentMapValues.nodeIntegerValue(XContentMapValues.extractValue("neo4j.interval", settings.settings()), DEFAULT_NEO_INTERVAL);
        index = XContentMapValues.nodeStringValue(XContentMapValues.extractValue("index.name", settings.settings()), DEFAULT_NEO_INDEX);
        type = XContentMapValues.nodeStringValue(XContentMapValues.extractValue("index.type", settings.settings()), DEFAULT_NEO_TYPE);
        indexFromLabel = XContentMapValues.nodeStringValue(XContentMapValues.extractValue("index.name.label",
            settings.settings()), null);
        typeFromLabel = XContentMapValues.nodeStringValue(XContentMapValues.extractValue("index.type.label",
            settings.settings()), null);

        logger.debug("Neo4j settings [uri={}]", new Object[]{uri});
        logger.debug("River settings [indexName={}, type={}, interval={}, timestampField={}, indexLabel={}, " +
                "typelabel={}]",
            new Object[]{index,
                type,
                interval,
                timestampField,
                indexFromLabel,
                typeFromLabel}
        );

    }

    @Override
    public void start() {
        logger.info("Starting neo4j river");

        ElasticOperationWorker worker = new ElasticOperationWorker(client);
        SpringCypherRestGraphDatabase db = new SpringCypherRestGraphDatabase(uri);
        Neo4jIndexer indexer = new Neo4jIndexer(db, worker, indexingStrategy, deletingStategy, index, type, labels);
        Neo4jPoller poller = new Neo4jPoller(indexer, interval);

        executor = Executors.newFixedThreadPool(2);
        executor.submit(worker);
        executor.submit(poller);

        logger.debug("Neo4j river started");
    }

    @Override
    public void close() {
        if (executor == null)
            return;

        logger.debug("Shutting down executor");
        executor.shutdownNow();
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
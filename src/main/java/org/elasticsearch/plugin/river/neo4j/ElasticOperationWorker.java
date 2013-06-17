package org.elasticsearch.plugin.river.neo4j;

import org.elasticsearch.action.deletebyquery.DeleteByQueryRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Stephen Samuel
 */
public class ElasticOperationWorker implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(ElasticOperationWorker.class);
    private static final ElasticOperation POISON = new NoopOperation();

    private final BlockingQueue<ElasticOperation> queue = new LinkedBlockingQueue<ElasticOperation>();
    private final Client client;

    public ElasticOperationWorker(Client client) {
        this.client = client;
    }

    public void queue(ElasticOperation op) {
        logger.debug("Queuing op: [{}]", op);
        queue.offer(op);
    }

    void shutdown() {
        queue.offer(POISON);
    }

    @Override
    public void run() {
        logger.debug("Starting indexer");
        while (true) {
            try {

                ElasticOperation op = queue.take();

                if (op == POISON) {
                    logger.info("Poison pill eaten - yum yum - exiting indexer");
                    break;
                }

                logger.debug("Exec elastic op={}", op);
                op.run(client);

                if (Thread.currentThread().isInterrupted()) {
                    logger.info("Indexer interrupted, safely shutting down");
                    shutdown();
                }

            } catch (InterruptedException ignored) {
                logger.info("Indexer rudely interrupted, safely shutting down");
                shutdown();

            } catch (Exception e) {
                logger.info("Error running op {}", e);
            }
        }
        logger.debug("Indexer shutdown");
    }
}

interface ElasticOperation {
    void run(Client client);
}

class NoopOperation implements ElasticOperation {

    @Override
    public void run(Client client) {
    }
}

class IndexOperation implements ElasticOperation {

    private static Logger logger = LoggerFactory.getLogger(IndexOperation.class);

    private final IndexingStrategy strategy;
    private final String index;
    private final String type;
    private final Node node;
    private final long version;

    public IndexOperation(IndexingStrategy strategy, String index, String type, Node node, long version) {
        this.strategy = strategy;
        this.index = index;
        this.type = type;
        this.node = node;
        this.version = version;
    }

    @Override
    public void run(Client client) {
        try {
            IndexRequest req = strategy.build(index, type, node, version);
            client.index(req).actionGet();
        } catch (IOException e) {
            logger.error("Error indexing [{}]", e);
        }
    }

    @Override
    public String toString() {
        return "IndexOperation{" +
                "index='" + index + '\'' +
                ", type='" + type + '\'' +
                ", node=" + node +
                ", version=" + version +
                '}';
    }
}

class ExpungeOperation implements ElasticOperation {

    private static Logger logger = LoggerFactory.getLogger(ExpungeOperation.class);

    private final DeletingStategy strategy;
    private final String index;
    private final String type;
    private final long version;

    public ExpungeOperation(DeletingStategy strategy, String index, String type, long version) {
        this.strategy = strategy;
        this.index = index;
        this.type = type;
        this.version = version;
    }

    @Override
    public void run(Client client) {
        try {
            DeleteByQueryRequest req = strategy.build(index, type, version);
            client.deleteByQuery(req).actionGet();
        } catch (RuntimeException e) {
            logger.error("Error expunging [{}]", e);
        }
    }

    @Override
    public String toString() {
        return "ExpungeOperation{" +
                "type='" + type + '\'' +
                ", index='" + index + '\'' +
                ", version=" + version +
                '}';
    }
}
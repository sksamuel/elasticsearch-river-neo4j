package org.elasticsearch.plugin.river.neo4j;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.neo4j.graphdb.Node;
import org.neo4j.rest.graphdb.entity.RestNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Stephen Samuel
 */
public class Neo4jIndexer implements Runnable {

    private static final Node POISON = new RestNode("", null);
    private static Logger logger = LoggerFactory.getLogger(Neo4jIndexer.class);

    private final IndexingStrategy strategy;
    private final Client client;
    private final String index;
    private final String type;
    private final BlockingQueue<Node> queue = new LinkedBlockingQueue<Node>();

    public Neo4jIndexer(Client client, String index, String type, IndexingStrategy strategy) {
        this.type = type;
        this.strategy = strategy;
        if (client == null) throw new IllegalStateException();
        if (index == null) throw new IllegalStateException();
        this.client = client;
        this.index = index;
    }

    public void index(Node node) {
        logger.debug("Queuing node: [{}]", node);
        queue.offer(node);
    }

    public void shutdown() {
        queue.offer(POISON);
    }

    @Override
    public void run() {
        logger.debug("Starting indexer");
        while (true) {
            try {
                Node node = queue.take();
                if (node == POISON) {
                    logger.info("Poison pill eaten - exiting indexer");
                    return;
                }
                try {
                    IndexRequest req = strategy.build(index, type, node);
                    client.index(req).actionGet();
                    logger.debug("...indexed");
                } catch (Exception e) {
                    logger.warn("{}", e);
                }
            } catch (InterruptedException ignored) {
                logger.info("Indexer rudely interrupted, safely shutting down");
                shutdown();
            }
        }
    }

}

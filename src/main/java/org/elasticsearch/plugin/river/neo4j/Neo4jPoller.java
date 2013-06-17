package org.elasticsearch.plugin.river.neo4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stephen Samuel
 */
public class Neo4jPoller implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(Neo4jPoller.class);

    private final int interval;
    private final Neo4jIndexer indexer;
    private volatile boolean running = true;

    public Neo4jPoller(Neo4jIndexer indexer, int interval) {
        this.indexer = indexer;
        this.interval = interval;
    }

    public void shutdown() {
        running = false;
    }

    @Override
    public void run() {
        while (running) {
            try {

                logger.debug("Sleeping for {}ms", interval);
                Thread.sleep(interval);

                indexer.index();

                if (Thread.interrupted())
                    shutdown();

            } catch (InterruptedException ignored) {
                logger.debug("Poller rudely interrupted, safely shutting down");
                shutdown();
            }
        }
    }

}

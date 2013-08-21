package org.elasticsearch.plugin.river.neo4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stephen Samuel
 */
public class Neo4jPoller implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(Neo4jPoller.class);

    private final long interval;
    private final Neo4jIndexer indexer;
    private volatile boolean running = true;

    public Neo4jPoller(Neo4jIndexer indexer, long interval) {
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

                logger.debug("Sleeping for {}s", interval);
                Thread.sleep(interval*1000);

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

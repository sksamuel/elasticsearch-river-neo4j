package org.elasticsearch.plugin.river.neo4j;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stephen Samuel
 */
public class Neo4jPoller implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(Neo4jPoller.class);

    private final TimeUnit intervalUnit;
    private final long interval;
    private final Neo4jIndexer indexer;
    private volatile boolean running = true;
    
    public Neo4jPoller(Neo4jIndexer indexer, long interval) {
        this(indexed, interval, TimeUnit.MILLISECONDS)
    }

    public Neo4jPoller(Neo4jIndexer indexer, long interval, TimeUnit intervalUnit) {
        this.indexer = indexer;
        this.interval = interval;
        this.intervalUnit = intervalUnit;
    }

    public void shutdown() {
        running = false;
    }

    @Override
    public void run() {
        while (running) {
            try {

                logger.debug("Sleeping for {} {}", interval, intervalUnit);
                long msInterval = TimeUnit.MILLISECONDS.convert(interval, intervalUnit);
                Thread.sleep(intervalUnit);

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

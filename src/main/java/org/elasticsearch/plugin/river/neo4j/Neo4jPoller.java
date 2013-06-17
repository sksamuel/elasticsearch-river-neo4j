package org.elasticsearch.plugin.river.neo4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Stephen Samuel
 */
public class Neo4jPoller implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(Neo4jPoller.class);

    private final Neo4jClient client;
    private final int interval;
    private volatile boolean running = true;

    public Neo4jPoller(Neo4jClient client, int interval) {
        if (client == null) throw new IllegalStateException();
        this.client = client;
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
                logger.debug("Awake and about to poll...");
                client.poll();
                logger.debug("...polling completed");

                if (Thread.interrupted())
                    shutdown();

            } catch (InterruptedException ignored) {
                logger.debug("Poller rudely interrupted, safely shutting down");
                shutdown();
            }
        }
    }
}

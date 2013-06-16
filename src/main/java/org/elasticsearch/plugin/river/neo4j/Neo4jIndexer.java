package org.elasticsearch.plugin.river.neo4j;

import org.elasticsearch.client.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Stephen Samuel
 */
public class Neo4jIndexer implements Runnable {

    private static final String[] POISON = new String[]{"jekkyl", "hyde"};
    private static Logger logger = LoggerFactory.getLogger(Neo4jIndexer.class);

    private final Client client;
    private final String index;
    private BlockingQueue<String[]> queue = new LinkedBlockingQueue<String[]>();

    public Neo4jIndexer(Client client, String index) {
        this.client = client;
        this.index = index;
    }

    public void shutdown() {
        queue.offer(POISON);
    }

    @Override
    public void run() {
        logger.debug("Starting indexer");
        while (true) {
            try {
                String[] msg = queue.take();
                if (msg == POISON) {
                    logger.info("Poison pill eaten - shutting down indexer thread");
                    return;
                }
                try {
//                    client.prepareIndex(index, type).setSource(source).execute().actionGet();
                    logger.debug("...indexed");
                } catch (Exception e) {
                    logger.warn("{}", e);
                }
            } catch (InterruptedException ignored) {
            }
        }
    }
}
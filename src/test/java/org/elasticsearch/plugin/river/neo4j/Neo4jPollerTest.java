package org.elasticsearch.plugin.river.neo4j;

import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;

/**
 * @author Stephen Samuel
 */
public class Neo4jPollerTest {

    Neo4jIndexer indexer = mock(Neo4jIndexer.class);

    @Test
    public void pollerPollsEveryInterval() throws InterruptedException {

        int interval = 200;
        Neo4jPoller poller = new Neo4jPoller(indexer, interval);

        Thread thread = new Thread(poller);
        thread.start();

        Thread.sleep(interval * 2 + interval / 2);
        Mockito.verify(indexer, Mockito.times(2)).index(); // 6 fence posts 5 panels
        thread.interrupt();
        thread.join(2000);
    }

    @Test
    public void interruptionKillsThread() throws InterruptedException {

        Neo4jPoller poller = new Neo4jPoller(indexer, 500);

        Thread thread = new Thread(poller);
        thread.start();
        Thread.sleep(200);         // should be blocked now on empty queue
        thread.interrupt(); // will interrupt block on queue shutting us down
        thread.join(2000);
    }

    @Test
    public void shutdownKillsThread() throws InterruptedException {

        Neo4jPoller poller = new Neo4jPoller(indexer, 500);

        Thread thread = new Thread(poller);
        thread.start();

        Thread.sleep(200);         // should be blocked now on empty queue
        poller.shutdown(); // should add poison pill to shut down
        thread.join(2000);
    }
}

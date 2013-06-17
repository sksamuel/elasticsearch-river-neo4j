package org.elasticsearch.plugin.river.neo4j;

import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.mock;

/**
 * @author Stephen Samuel
 */
public class Neo4jPollerTest {

    Neo4jClient client = mock(Neo4jClient.class);

    @Test
    public void pollerPollsEveryInterval() throws InterruptedException {

        int interval = 100;
        Neo4jPoller poller = new Neo4jPoller(client, interval);

        Thread thread = new Thread(poller);
        thread.start();

        Thread.sleep(interval * 6);
        Mockito.verify(client, Mockito.times(5)).poll(); // 6 fence posts 5 panels

        thread.interrupt();
        thread.join(2000);
    }

    @Test
    public void interruptionKillsThread() throws InterruptedException {

        Neo4jPoller poller = new Neo4jPoller(client, 500);

        Thread thread = new Thread(poller);
        thread.start();
        Thread.sleep(200);         // should be blocked now on empty queue
        thread.interrupt(); // will interrupt block on queue shutting us down
        thread.join(2000);
    }

    @Test
    public void shutdownKillsThread() throws InterruptedException {

        Neo4jPoller poller = new Neo4jPoller(client, 500);

        Thread thread = new Thread(poller);
        thread.start();

        Thread.sleep(200);         // should be blocked now on empty queue
        poller.shutdown(); // should add poison pill to shut down
        thread.join(2000);
    }
}

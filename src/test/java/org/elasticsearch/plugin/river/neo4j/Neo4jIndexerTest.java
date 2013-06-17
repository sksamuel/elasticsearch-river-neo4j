package org.elasticsearch.plugin.river.neo4j;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Client;
import org.junit.Test;
import org.mockito.Mockito;
import org.neo4j.graphdb.Node;

import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author Stephen Samuel
 */
public class Neo4jIndexerTest {

    Client client = mock(Client.class);
    IndexingStrategy strategy = mock(IndexingStrategy.class);

    @Test
    public void messageIsIndexedUsingStrategy() throws InterruptedException, IOException {

        Node node = mock(Node.class);
        Neo4jIndexer indexer = new Neo4jIndexer(client, "myindex", "mytype", strategy);
        IndexRequest req = new IndexRequest("myindex", "mytype");
        Mockito.when(strategy.build("myindex", "mytype", node)).thenReturn(req);

        Thread thread = new Thread(indexer);
        thread.start();

        indexer.index(node);
        indexer.shutdown();
        thread.join();

        verify(client).index(req);
    }

    @Test
    public void interruptionKillsThread() throws InterruptedException {

        Neo4jIndexer indexer = new Neo4jIndexer(client, "myindex", "mytype", strategy);

        Thread thread = new Thread(indexer);
        thread.start();
        Thread.sleep(200);         // should be blocked now on empty queue
        thread.interrupt(); // will interrupt block on queue shutting us down
        thread.join(2000);
    }

    @Test
    public void shutdownKillsThread() throws InterruptedException {

        Neo4jIndexer indexer = new Neo4jIndexer(client, "myindex", "mytype", strategy);

        Thread thread = new Thread(indexer);
        thread.start();

        Thread.sleep(200);         // should be blocked now on empty queue
        indexer.shutdown(); // should add poison pill to shut down
        thread.join(2000);
    }
}

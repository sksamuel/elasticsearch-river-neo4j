package org.elasticsearch.plugin.river.neo4j;

import org.elasticsearch.client.Client;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author Stephen Samuel
 */
public class ElasticOperationWorkerTest {

    private static Logger logger = LoggerFactory.getLogger(ElasticOperationWorkerTest.class);

    Client client = mock(Client.class);

    @Test
    public void queuedIndexOperationIsExecuted() throws InterruptedException, IOException {

        ElasticOperationWorker worker = new ElasticOperationWorker(client);

        Thread thread = new Thread(worker);
        thread.start();

        IndexOperation op = mock(IndexOperation.class);
        worker.queue(op);
        worker.shutdown();
        thread.join();

        verify(op).run(client);
    }

    @Test
    public void queuedExpungeOperationIsExecuted() throws InterruptedException, IOException {

        ElasticOperationWorker worker = new ElasticOperationWorker(client);

        Thread thread = new Thread(worker);
        thread.start();

        ExpungeOperation op = mock(ExpungeOperation.class);
        worker.queue(op);
        worker.shutdown();
        thread.join();

        verify(op).run(client);
    }

    @Test
    public void interruptionOnBlockedKillsThread() throws InterruptedException {

        ElasticOperationWorker worker = new ElasticOperationWorker(client);

        Thread thread = new Thread(worker);
        thread.start();
        Thread.sleep(200);         // should be blocked now on empty queue
        thread.interrupt(); // will interrupt block on queue shutting us down
        thread.join(2000);
    }

    @Test
    public void interruptionOnNonBlockedKillsThread() throws IOException, InterruptedException {

        ElasticOperationWorker worker = new ElasticOperationWorker(client);

        Thread thread = new Thread(worker);
        thread.start();

        // need to fill up queue with some busy work
        // so that it is busy (not blocked) by the time we call interrupt.
        for (int k = 0; k < 10; k++) {

            ElasticOperation op = mock(ElasticOperation.class);
            Mockito.doAnswer(new Answer<Void>() {

                @Override
                public Void answer(InvocationOnMock invocation) throws Throwable {
                    long sum = 0;
                    for (int j = 0; j < 100000000; j++) {
                        sum = sum + j;
                    }
                    logger.debug("Wasted time counting to " + sum);
                    return null;
                }

            }).when(op).run(client);
            worker.queue(op);
        }
        thread.interrupt(); // will interrupt a non blocked process and add posion
        thread.join(2000);
        assertFalse(thread.isAlive());
    }
}

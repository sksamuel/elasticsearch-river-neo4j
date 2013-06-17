package org.elasticsearch.plugin.river.neo4j;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.neo4j.rest.SpringRestGraphDatabase;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * @author Stephen Samuel
 */
public class Neo4jIndexerTest {

    private static final Logger logger = LoggerFactory.getLogger(Neo4jIndexerTest.class);

    ElasticOperationWorker worker;
    SpringRestGraphDatabase db;
    Neo4jIndexer indexer;

    @Before
    public void before() {
        worker = mock(ElasticOperationWorker.class);
        db = mock(SpringRestGraphDatabase.class);
        indexer = new Neo4jIndexer(db, worker, new SimpleIndexingStrategy(), new SimpleDeletingStrategy(), "myindex", "mytype");
    }

    @Test
    public void thatAllNodesAreQueuedAndExpunged() {
        Node node1 = mock(Node.class);
        Node node2 = mock(Node.class);
        Node node3 = mock(Node.class);
        Mockito.when(db.getAllNodes()).thenReturn(Arrays.asList(node1, node2, node3));
        indexer.index();
        verify(worker, times(4)).queue(Matchers.any(IndexOperation.class)); // 3itimes for index and once for expunge
    }

    @Test
    public void thatExpungeIsCalledOnEmptyRequests() {
        Mockito.when(db.getAllNodes()).thenReturn(Collections.<Node>emptyList());
        indexer.index();
        ArgumentCaptor<ExpungeOperation> captor = ArgumentCaptor.forClass(ExpungeOperation.class);
        verify(worker).queue(captor.capture());
        assertEquals(captor.getValue().getClass(), ExpungeOperation.class);
    }

    @Test
    public void thatVersionReturnsLogicalIncreaseNeverLessThanCurrentTime() {
        indexer.version = 10;
        assertTrue(indexer.getVersion() >= System.currentTimeMillis());
    }
}

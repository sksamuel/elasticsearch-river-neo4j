package org.elasticsearch.plugin.river.neo4j;

import java.util.ArrayList;
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
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.Label;

/**
 * @author Stephen Samuel
 * @author Andre Crouch
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
        List<Label> labels = new ArrayList<>();
        labels.add(DynamicLabel.label("User"));
        indexer = new Neo4jIndexer(db, worker, new SimpleIndexingStrategy(), new SimpleDeletingStrategy(), "myindex", "mytype", labels);
    }

    @Test
    public void thatAllNodesAreQueuedAndExpunged() {     
        Node node1 = mock(Node.class);
        Node node2 = mock(Node.class);
        Node node3 = mock(Node.class);
        List<Node> nodes = Arrays.asList(node1, node2, node3);
        for (Node node : nodes) {
            Mockito.when(node.hasLabel(DynamicLabel.label("User"))).thenReturn(true);
        }
        Mockito.when(db.getAllNodes()).thenReturn(nodes);
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

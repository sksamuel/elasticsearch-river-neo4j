package org.elasticsearch.plugin.river.neo4j;

import org.junit.Test;
import org.mockito.Mockito;
import org.neo4j.graphdb.Node;
import org.springframework.data.neo4j.rest.SpringRestGraphDatabase;

import java.util.Arrays;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * @author Stephen Samuel
 */
public class Neo4jClientTest {

    Neo4jIndexer indexer = mock(Neo4jIndexer.class);
    SpringRestGraphDatabase db = mock(SpringRestGraphDatabase.class);

    @Test
    public void thatClientPollsAndIndexesAllNodes() {
        Node node1 = mock(Node.class);
        Node node2 = mock(Node.class);
        Node node3 = mock(Node.class);
        Mockito.when(db.getAllNodes()).thenReturn(Arrays.asList(node1, node2, node3));
        Neo4jClient client = new Neo4jClient(db, indexer);
        client.poll();
        verify(indexer).index(node1);
        verify(indexer).index(node2);
        verify(indexer).index(node3);
    }
}

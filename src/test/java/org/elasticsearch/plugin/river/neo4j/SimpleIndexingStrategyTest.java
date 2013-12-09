package org.elasticsearch.plugin.river.neo4j;

import org.elasticsearch.action.index.IndexRequest;
import org.junit.Test;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.ResourceIterable;
import org.neo4j.graphdb.ResourceIterator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.neo4j.graphdb.DynamicLabel;

/**
 * @author Stephen Samuel
 * @author Andre Crouch
 */
public class SimpleIndexingStrategyTest {

    SimpleIndexingStrategy s = new SimpleIndexingStrategy();

    @Test
    public void thatNodePropertiesAreUsedAsFieldValues() throws IOException {

        long id = new Random().nextInt(50000) + 1;
        Node node = mock(Node.class);
        when(node.getId()).thenReturn(id);
        when(node.getPropertyKeys()).thenReturn(Arrays.asList("name", "location", "band"));
        when(node.getProperty("name")).thenReturn("chris martin");
        when(node.getProperty("location")).thenReturn("hampstead");
        when(node.getProperty("band")).thenReturn("coldplay");
        when(node.hasLabel(DynamicLabel.label("User"))).thenReturn(true);
        when(node.getLabels()).thenReturn(new ResourceIterable<Label>() {
            @Override
            public ResourceIterator<Label> iterator() {
                return new ResourceIterator<Label>() {
                    private List<Label> labels = Arrays.asList(
                        DynamicLabel.label("User"),
                        DynamicLabel.label("Swedish")
                    );
                    
                    private int position = 0;

                    @Override
                    public void close() {

                    }
                    @Override
                    public boolean hasNext() {
                        boolean result = false;
                        if(position < labels.size()) {
                            result = true;
                        }
                        return result;
                    }
                    @Override
                    public Label next() {
                        Label label = labels.get(position);
                        position++;
                        return label;
                    }
                    @Override
                    public void remove() {

                    }
                };
            }
        });
        
        IndexRequest req = s.build("neo4j-index", "node", node, 12);
        assertEquals("neo4j-index", req.index());
        assertEquals("node", req.type());
        assertEquals(12, req.sourceAsMap().get("version"));
        assertEquals(String.valueOf(id), req.id());
        assertEquals(new ArrayList<String>() {{
            add("User");
            add("Swedish");
        }}, req.sourceAsMap().get("labels"));
        assertEquals("coldplay", req.sourceAsMap().get("band"));
        assertEquals("hampstead", req.sourceAsMap().get("location"));
        assertEquals("chris martin", req.sourceAsMap().get("name"));
    }
}

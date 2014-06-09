package org.elasticsearch.plugin.river.neo4j;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Stephen Samuel
 * @author Andre Crouch
 */
public class SimpleIndexingStrategy implements IndexingStrategy {

    @Override
    public IndexRequest build(String index, String type, Node node, long version) throws IOException {
        XContentBuilder src = XContentFactory.jsonBuilder().startObject();
        // Store version
        src.field("version", version);   
        // Store node labels as array
        ArrayList<String> nodeLabels = new ArrayList<>();
        for (Label key : node.getLabels()) {
            nodeLabels.add(key.name());
        }
        if(nodeLabels.size() > 0) {
            src.array("labels", nodeLabels.toArray());
        }
        for (String key : node.getPropertyKeys()) {
            Object value = node.getProperty(key);
            if (value instanceof Object[]) {
                Object[] array = (Object[]) value;
                src.field(key, array);
            } else {
                src.field(key, value.toString());
            }
        }
        
        return new IndexRequest(index, type, String.valueOf(node.getId())).source(src);
    }
}

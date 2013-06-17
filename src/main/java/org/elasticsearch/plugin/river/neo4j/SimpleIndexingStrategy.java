package org.elasticsearch.plugin.river.neo4j;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.neo4j.graphdb.Node;

import java.io.IOException;

/**
 * @author Stephen Samuel
 */
public class SimpleIndexingStrategy implements IndexingStrategy {

    @Override
    public IndexRequest build(String index, String type, Node node) throws IOException {

        XContentBuilder src = XContentFactory.jsonBuilder().startObject();
        IndexRequest req = new IndexRequest(index, type, String.valueOf(node.getId()));
        for (String key : node.getPropertyKeys()) {
            String value = node.getProperty(key).toString();
            src.field(key, value);
        }
        return req;
    }
}

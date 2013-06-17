package org.elasticsearch.plugin.river.neo4j;

import org.elasticsearch.action.index.IndexRequest;
import org.neo4j.graphdb.Node;

import java.io.IOException;

/**
 * Pluggable strategies for how to convert a neo4j node into an elastic document
 *
 * @author Stephen Samuel
 */
public interface IndexingStrategy {
    IndexRequest build(String index, String type, Node node) throws IOException;
}

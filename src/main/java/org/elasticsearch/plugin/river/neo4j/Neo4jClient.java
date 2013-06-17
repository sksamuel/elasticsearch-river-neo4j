package org.elasticsearch.plugin.river.neo4j;

import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.neo4j.rest.SpringRestGraphDatabase;

/**
 * @author Stephen Samuel
 */
public class Neo4jClient {

    private static final Logger logger = LoggerFactory.getLogger(Neo4jClient.class);

    private final SpringRestGraphDatabase db;
    private final Neo4jIndexer indexer;

    public Neo4jClient(String uri, Neo4jIndexer indexer) {
        if (uri == null) throw new IllegalStateException();
        if (indexer == null) throw new IllegalStateException();
        this.indexer = indexer;
        db = new SpringRestGraphDatabase(uri);
    }

    /**
     * Retrieve data from the server
     */
    public void poll() {
        logger.debug("Hitting neo4j server...");
        Iterable<Node> r = db.getAllNodes();
        logger.debug("...nodes retrieved");
        for (Node node : r) {
            indexer.index(node);
        }
    }
}

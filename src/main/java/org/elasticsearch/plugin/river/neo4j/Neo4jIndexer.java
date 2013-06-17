package org.elasticsearch.plugin.river.neo4j;

import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.neo4j.rest.SpringRestGraphDatabase;

/**
 * @author Stephen Samuel
 */
public class Neo4jIndexer {

    private static final Logger logger = LoggerFactory.getLogger(Neo4jIndexer.class);

    long version = System.currentTimeMillis();
    private SpringRestGraphDatabase db;
    private ElasticOperationWorker worker;
    private final IndexingStrategy indexingStrategy;
    private final DeletingStategy deletingStategy;
    private final String index;
    private final String type;

    public Neo4jIndexer(SpringRestGraphDatabase db, ElasticOperationWorker worker, IndexingStrategy indexingStrategy,
                        DeletingStategy deletingStategy, String index, String type) {
        if (db == null) throw new IllegalStateException();
        if (worker == null) throw new IllegalStateException();
        if (indexingStrategy == null) throw new IllegalStateException();
        if (deletingStategy == null) throw new IllegalStateException();
        if (index == null) throw new IllegalStateException();
        if (type == null) throw new IllegalStateException();
        this.db = db;
        this.worker = worker;
        this.indexingStrategy = indexingStrategy;
        this.deletingStategy = deletingStategy;
        this.index = index;
        this.type = type;
    }

    public void index() {
        version = getVersion();

        logger.debug("Awake and about to poll...");
        Iterable<Node> r = db.getAllNodes();
        for (Node node : r) {
            worker.queue(new IndexOperation(indexingStrategy, index, type, node, version));
        }
        logger.debug("...polling completed");

        logger.debug("Deleting all nodes with version < {}", version);
        worker.queue(new ExpungeOperation(deletingStategy, index, type, version));
    }

    long getVersion() {
        return Math.max(System.currentTimeMillis(), version + 1);
    }
}

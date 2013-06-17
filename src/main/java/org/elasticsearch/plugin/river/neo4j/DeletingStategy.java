package org.elasticsearch.plugin.river.neo4j;

import org.elasticsearch.action.deletebyquery.DeleteByQueryRequest;

/**
 * @author Stephen Samuel
 */
public interface DeletingStategy {
    DeleteByQueryRequest build(String index, String type, long currentVersion);
}

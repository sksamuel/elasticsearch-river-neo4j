package org.elasticsearch.plugin.river.neo4j;

import org.elasticsearch.action.deletebyquery.DeleteByQueryRequest;
import org.elasticsearch.action.support.QuerySourceBuilder;
import org.elasticsearch.index.query.QueryBuilders;

/**
 * @author Stephen Samuel
 */
public class SimpleDeletingStrategy implements DeletingStategy {

    @Override
    public DeleteByQueryRequest build(String index, String type, long currentVersion) {
        QuerySourceBuilder querySourceBuilder = new QuerySourceBuilder().setQuery(QueryBuilders.rangeQuery("version").to(currentVersion - 1));
        return new DeleteByQueryRequest(index).types(type).source(querySourceBuilder);
    }
}

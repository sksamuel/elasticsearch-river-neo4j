package org.elasticsearch.plugin.river.neo4j;

import org.elasticsearch.action.deletebyquery.DeleteByQueryRequest;
import org.junit.Test;

import java.io.IOException;
import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * @author Stephen Samuel
 */
public class SimpleDeletingStrategyTest {

    SimpleDeletingStrategy s = new SimpleDeletingStrategy();

    @Test
    public void thatNodePropertiesAreUsedAsFieldValues() throws IOException {
        long id = new Random().nextInt(50000) + 1;
        DeleteByQueryRequest req = s.build("neo4j-index", "node", 12);
        assertEquals("neo4j-index", req.indices()[0]);
        assertEquals("[[neo4j-index]][[node]], source[{\"query\":{\"range\":{\"version\":{\"from\":null,\"to\":11," +
                "\"include_lower\":true,\"include_upper\":true}}}}]", req.toString());
    }
}

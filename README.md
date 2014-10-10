Neo4j River Plugin for ElasticSearch
=========================

Neo4j River Plugin is a river module for Elasticsearch that will continuously poll a neo4j server and index the nodes for searching inside elastic search. The period betweens polls can be configured. All nodes will be updated on each poll, and any deleted nodes since the last poll will be removed from the index. Versioning is used to identify which nodes have been removed since the last poll.

The nodes will be indexed using the properties set on them, with the neo4j node id being the id used inside elastic. So if you have a node of _id=7_ with the properties _name=sam_ and _location=london_ then the elastic search index request would look like

```json
{
 "_id" : 7,
 "name" : "sam",
 "location" : "london",
 "version" : 1234567
}
```



## Plugin history

| Neo4j Driver Plugin | ElasticSearch | Neo4j |
| ------ | --------- | --------- |
| 1.2.1 | 1.2.1 | 2.0.1
| 1.1.0 | 1.1.0 | 2.0.1
| 1.0.2 | 1.0.2 | 2.0.1
| 0.90.1.x | 0.90.1 | 1.8.x |
| 0.90.7.0 | 0.90.7 | 2.0.x |
| 0.90.9.0 | 0.90.9 | 2.0.x |

[![Build Status](https://travis-ci.org/sksamuel/elasticsearch-river-neo4j.png)](https://travis-ci.org/sksamuel/elasticsearch-river-neo4j)



## How to use

Start the neo4j river by curling a document like the following to the river index.

```
curl -XPUT 'http://localhost:9200/_river/my_neo_river/_meta' -d '{
    "type": "neo4j",
    "neo4j": {
        "uri": "<NEO4J_URI>",
        "interval": <some interval in ms (only the number)>
    },
    "index": {
        "name": "<INDEX_NAME>",
        "type": "<TYPE>"
    }
}'
```
Label(s) index example.

```
curl -XPUT 'http://localhost:9200/_river/my_neo_river/_meta' -d '{
    "type": "neo4j",
    "neo4j": {
        "uri": "<NEO4J_URI>",
        "labels": [
            "User",
            "Swedish"
        ],
        "interval": <some interval in ms (only the number)>
    },
    "index": {
        "name": "<INDEX_NAME>",
        "type": "<TYPE>"
    }
}'
```

The following parameters are available in the neo4j river document.

| Parameter | Description |
| ------ | --------- |
| neo4j.uri | The full URI to the neo4j server, eg http://localhost:7474/db/data |
| neo4j.interval | The time (in ms) between polling the neo4j instance. The greater this value, the lower the load on the server but the longer between updates in neo4j being reflected inside elastic |
| index.name | The name of the index to index nodes into |
| index.type | The type to use for indexing |



## Integration Tests

Make sure you are running a neo4j server on localhost:7474 or alternatively update the neo4j-inttest-river.json settings inside src/test/resources
to point to your local server.

Then execute:
```mvn -Pint-test clean install```


## How to install

The plugin is available on maven central.

```xml
<dependency>
    <groupId>com.sksamuel.elasticsearch</groupId>
    <artifactId>elasticsearch-river-neo4j</artifactId>
    <version>0.90.7.8</version>
</dependency>
```

To install run:
```
$ bin/plugin -install com.sksamuel.elasticsearch/elasticsearch-river-neo4j/1.2.1.1
```



## License
```
This software is licensed under the Apache 2 license, quoted below.

Copyright 2013 Stephen Samuel

Licensed under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License. You may obtain a copy of
the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
License for the specific language governing permissions and limitations under
the License.
```

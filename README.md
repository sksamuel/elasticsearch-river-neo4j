Neo4j River Plugin for ElasticSearch
=========================

| Neo4j Driver Plugin | ElasticSearch | Neo4j |
| ------ | --------- | --------- |
| 0.90.1.x | 0.90.1 | 1.9.x |

[![Build Status](https://travis-ci.org/sksamuel/elasticsearch-river-neo4j.png)](https://travis-ci.org/sksamuel/elasticsearch-river-neo4j)

## How to use

Start the neo4j river by curling a document like the following to the river index.

```
curl -XPUT 'http://localhost:9200/_river/my_neo_river/_meta' -d '{
    "type": "redis",
    "redis": {
        "hostname": "NEO4J_HOSTNAME",
        "port" : "NEO4J_PORT_OPTIONAL",
    },
    "index": {
        "name": "INDEX_NAME",
    }
}'
```

The following parameters are available in the rediss river document.

| Parameter | Description |
| ------ | --------- |

## Example



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
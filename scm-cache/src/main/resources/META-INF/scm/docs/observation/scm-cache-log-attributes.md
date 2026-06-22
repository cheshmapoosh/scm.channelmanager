# SCM Cache LOG Attributes

| name | Java type | Elasticsearch type | owner | streams | presence | sensitivity | visiblePrefixLength | visibleSuffixLength | description |
| --- | --- | --- | --- | --- | --- | --- | ---: | ---: | --- |
| `cache.hazelcast.element.type` | String | keyword | scm-cache | LOG | EVENT_OPTIONAL | RAW | 0 | 0 | Hazelcast element type. |
| `cache.hazelcast.element.name` | String | keyword | scm-cache | LOG | EVENT_OPTIONAL | RAW | 0 | 0 | Hazelcast element name. |
| `cache.hazelcast.element.config` | String | text | scm-cache | LOG | EVENT_OPTIONAL | RAW | 0 | 0 | Selected safe Hazelcast element configuration as deterministic text. |
| `cache.hazelcast.element.count` | Integer | integer | scm-cache | LOG | EVENT_OPTIONAL | RAW | 0 | 0 | Number of Hazelcast elements registered from cache configuration. |
| `cache.hazelcast.element.summary` | String | keyword | scm-cache | LOG | EVENT_OPTIONAL | RAW | 0 | 0 | Low-cardinality summary of registered Hazelcast elements by type. |
| `cache.hazelcast.materialized.count` | Integer | integer | scm-cache | LOG | EVENT_OPTIONAL | RAW | 0 | 0 | Number of Hazelcast distributed objects materialized from cache configuration. |
| `cache.hazelcast.materialized.summary` | String | keyword | scm-cache | LOG | EVENT_OPTIONAL | RAW | 0 | 0 | Low-cardinality summary of materialized Hazelcast elements by type. |
| `cache.hazelcast.member.address` | String | keyword | scm-cache | LOG | ON_CHANGE_OPTIONAL | RAW | 0 | 0 | Hazelcast member address when the member address is created or changes. |
| `cache.hazelcast.cluster.size` | Integer | integer | scm-cache | LOG | ON_CHANGE_OPTIONAL | RAW | 0 | 0 | Hazelcast cluster size when cluster membership is created or changes. |

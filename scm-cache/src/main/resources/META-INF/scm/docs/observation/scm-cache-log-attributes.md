# SCM Cache LOG Attributes

| name | Java type | Elasticsearch type | owner | streams | presence | sensitivity | visiblePrefixLength | visibleSuffixLength | description |
| --- | --- | --- | --- | --- | --- | --- | ---: | ---: | --- |
| `cache.hazelcast.member.address` | String | keyword | scm-cache | LOG | ON_CHANGE_OPTIONAL | RAW | 0 | 0 | Hazelcast member address when the member address is created or changes. |
| `cache.hazelcast.cluster.size` | Integer | integer | scm-cache | LOG | ON_CHANGE_OPTIONAL | RAW | 0 | 0 | Hazelcast cluster size when cluster membership is created or changes. |

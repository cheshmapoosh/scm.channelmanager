# filebeat

This folder keeps Filebeat examples for SCM LOG, TRACE, and AUDIT JSONL files. JSONL is the default structured ingestion contract.

Optional simple `.log` observation files are human-readable `key=value` output. They are not JSONL-compatible and must not be added to these inputs.

Metrics are not collected by Filebeat. Metrics stay on the Actuator, Micrometer, Prometheus, and Grafana path.

SCM observation files use this directory layout:

```text
{root}/{appName}/{env}/{namespace}/{stream}/
```

Final JSONL file names use this shape:

```text
{stream}-scm-{appName}-{env}-{namespace}-{instanceId}-{yyyyMMdd-HH}-{rollIndex}.jsonl
```

Example Filebeat inputs:

```yaml
filebeat.inputs:
  - type: filestream
    id: scm-log-events
    paths:
      - /var/obs/*/*/*/log/log-scm-*.jsonl
    parsers:
      - ndjson:
          target: ""
          add_error_key: true

  - type: filestream
    id: scm-trace-events
    paths:
      - /var/obs/*/*/*/trace/trace-scm-*.jsonl
    parsers:
      - ndjson:
          target: ""
          add_error_key: true

  - type: filestream
    id: scm-audit-events
    paths:
      - /var/obs/*/*/*/audit/audit-scm-*.jsonl
    parsers:
      - ndjson:
          target: ""
          add_error_key: true
```

Do not configure these JSON parsers to read simple `.log` files, compressed observation files, or metric data.

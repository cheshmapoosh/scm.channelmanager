# AGENTS.md - SCM Observability Root

## Scope

This root guide only explains the global observability direction.

Detailed documentation must live inside each module folder.

Do not create a centralized `docs/` folder for module-specific observability documentation.

## Final Architecture

```text
Trace  -> NDJSON file -> Filebeat -> Elasticsearch -> Kibana
Log    -> NDJSON file -> Filebeat -> Elasticsearch -> Kibana
Audit  -> NDJSON file -> Filebeat -> Elasticsearch -> Kibana

Metric -> Actuator -> Micrometer -> Prometheus -> Grafana
```

## Rules

- Keep root documentation minimal.
- Put module-specific docs inside the module folder.
- Do not write metrics to file.
- Do not change the standard Actuator metric flow.
- Keep Audit model separate from Application Log.
- Keep sensitive data out of Trace, Log, Audit and Metric.

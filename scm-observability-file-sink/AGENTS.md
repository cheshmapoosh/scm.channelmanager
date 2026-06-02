# AGENTS.md - scm-observability-file-sink

## Rules

This module writes NDJSON files only for:

- Trace
- Log events
- Audit events

Never write metrics to files.

Do not depend on Elasticsearch or Filebeat APIs. The module only writes local files.

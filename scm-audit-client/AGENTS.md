# AGENTS.md - scm-audit-client

## Rules

This module publishes audit events.

In the current architecture it publishes to NDJSON file through file sink.

Do not add persistence or query logic here.

Do not mix audit events with application logs.

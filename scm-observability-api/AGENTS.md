# AGENTS.md - scm-observability-api

## Rules

This module is a pure contract module.

Allowed:

- Constants
- Lightweight enums
- Lightweight records
- Sanitization contracts

Forbidden:

- Spring Boot
- OpenTelemetry SDK implementation
- Micrometer registry implementation
- Logback
- JPA
- MQ/JMS
- File writing

Metrics are only named here. They are not written to files.

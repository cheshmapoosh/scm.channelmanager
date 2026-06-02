# AGENTS.md - scm-observability-spring-boot-starter

## Rules

This module provides Spring Boot integration only.

Allowed:

- AutoConfiguration
- Filters
- Conditional beans
- Micrometer/Actuator integration
- File sink wiring for Trace/Log/Audit

Forbidden:

- Business logic
- Audit persistence
- Metric file writing
- Direct Elasticsearch integration

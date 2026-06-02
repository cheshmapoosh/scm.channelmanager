# AGENTS.md - scm-audit-api

## Rules

This is a pure audit contract module.

Forbidden:

- JPA
- MQ/JMS
- Spring Boot runtime logic
- File writer implementation
- Elasticsearch integration

Keep audit event schema stable and versionable.

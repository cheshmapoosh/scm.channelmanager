# scm-docs-client

`scm-docs-client` is a reusable Spring Boot starter/library for exposing module documentation.

It is phase-1 only. It does not integrate `scm-web`, `scm-uaa`, `scm-cache`, or `scm-config`.

## Endpoints

The default base path is `/docs` and can be changed with `scm.docs.base-path`.

- `GET /docs` renders an HTML documentation index.
- `GET /docs/api` returns a JSON documentation index.
- `GET /docs/api/{docId}` returns the direct document content with the document `Content-Type`.

`GET /docs/api/{docId}` does not wrap content in JSON. It also returns `Content-Disposition: inline` with a filename when one is available.

## Supported Document Types

- `OPENAPI_JSON` -> `application/json`, `.json`
- `WSDL` -> `application/xml`, `.wsdl`
- `ISO8583_SCHEMA` -> `application/json`, `.json`
- `MARKDOWN` -> `text/markdown; charset=UTF-8`, `.md`
- `HTML` -> `text/html; charset=UTF-8`, `.html`

## Metadata

Document titles and descriptions support i18n maps. The display fallback order is:

1. requested language
2. `fa`
3. `en`
4. `id`

Module code is configurable with `scm.docs.module-code` and can be overridden per document.

## Classpath Resources

Phase 1 supports classpath-backed documents under `scm.docs.classpath-root`, which defaults to `scm-docs`.

Example:

```yaml
scm:
  docs:
    enabled: true
    base-path: /docs
    module-code: scm-web
    title:
      en: SCM Web Documentation
    classpath-root: scm-docs
    documents:
      - id: sample-guide
        module-code: scm-web
        type: MARKDOWN
        category: GUIDE
        title:
          en: Sample Guide
        description:
          en: Sample documentation
        classpath-location: sample.md
        file-name: sample.md
        order: 10
```

Classpath loading rejects unsafe ids, absolute paths, null bytes, and path traversal.

## Dependency Boundaries

This module does not use Camel and does not depend on `scm-core`, `scm-web`, or database modules.

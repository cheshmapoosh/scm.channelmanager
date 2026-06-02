# filebeat

## هدف

این فولدر فقط نمونه کانفیگ Filebeat مربوط به Trace/Log/Audit را نگه می‌دارد.

Metric توسط Filebeat جمع‌آوری نمی‌شود.

## نمونه inputها

```yaml
filebeat.inputs:
  - type: filestream
    id: scm-trace-events
    paths:
      - /log/scm/*/trace*.ndjson
    parsers:
      - ndjson:
          target: ""
          add_error_key: true

  - type: filestream
    id: scm-application-logs
    paths:
      - /log/scm/*/application*.ndjson
    parsers:
      - ndjson:
          target: ""
          add_error_key: true

  - type: filestream
    id: scm-audit-events
    paths:
      - /log/scm/*/audit*.ndjson
    parsers:
      - ndjson:
          target: ""
          add_error_key: true
```

## ممنوع

برای metric هیچ input تعریف نشود.

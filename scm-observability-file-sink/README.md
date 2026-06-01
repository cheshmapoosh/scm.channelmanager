# scm-observability-file-sink

## هدف

این ماژول مسئول نوشتن eventهای Observability در فایل NDJSON است.

## دامنه

این ماژول فقط برای موارد زیر استفاده می‌شود:

```text
Trace
Application Log Event
Audit
```

Metric در این ماژول نوشته نمی‌شود.

## فایل‌های خروجی پیشنهادی

```text
/log/scm/{service-name}/trace.ndjson
/log/scm/{service-name}/application.ndjson
/log/scm/{service-name}/audit.ndjson
```

## کلاس‌های پیشنهادی

```text
ObservationFileWriter
TraceFileWriter
ApplicationLogFileWriter
AuditFileWriter
ObservationJsonSerializer
SensitiveDataMasker
FileSinkProperties
```

## ممنوع

- نوشتن metric در فایل
- ارسال مستقیم به Elasticsearch
- وابستگی به Filebeat
- persistence دیتابیسی
- business logic

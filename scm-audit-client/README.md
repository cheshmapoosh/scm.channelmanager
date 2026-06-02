# scm-audit-client

## هدف

این ماژول API انتشار Audit Event را برای ماژول‌های application فراهم می‌کند.

## مسیر فاز فعلی

```text
Application Module
    ↓
ScmAuditPublisher
    ↓
audit.ndjson
    ↓
Filebeat
    ↓
Elasticsearch
    ↓
Kibana
```

## API پیشنهادی

```java
public interface ScmAuditPublisher {
    void publish(ScmAuditEvent event);
}
```

## رفتار پیشنهادی

- Audit failure معمولاً نباید request اصلی را fail کند.
- برای eventهای امنیتی بسیار حساس، رفتار fail-fast می‌تواند با config فعال شود.
- قبل از انتشار باید masking/sanitization انجام شود.

## آینده

اگر نیاز compliance جدی‌تر شد، می‌توان ماژول زیر را اضافه کرد:

```text
scm-audit-service
```

برای ذخیره append-only رسمی در DB یا storage مستقل.

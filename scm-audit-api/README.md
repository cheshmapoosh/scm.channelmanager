# scm-audit-api

## هدف

این ماژول قراردادها و مدل‌های Audit را تعریف می‌کند.

## مدل پیشنهادی

```java
public record ScmAuditEvent(
        String eventId,
        String eventType,
        String category,
        String action,
        String result,
        String actorType,
        String actorIdHash,
        String subjectType,
        String subjectId,
        String resourceType,
        String resourceId,
        String serviceCode,
        String operationCode,
        String channelCode,
        String correlationId,
        String traceId,
        String clientIp,
        Instant occurredAt,
        Map<String, Object> attributes
) {
}
```

## دسته‌بندی‌ها

```text
AUTH
ACCESS_CONTROL
CONFIG_CHANGE
ROUTE_CHANGE
PROVIDER_CALL
BUSINESS_OPERATION
SECURITY_POLICY
SYSTEM_ADMIN
```

## نتیجه‌ها

```text
SUCCESS
FAILURE
DENIED
ERROR
```

## اصول

- Audit با Log یکی نیست.
- Audit باید structured باشد.
- actorId بهتر است hash شود.
- اطلاعات حساس نباید در Audit ثبت شود.

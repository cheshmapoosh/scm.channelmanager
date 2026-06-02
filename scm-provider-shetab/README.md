# scm-provider-shetab

ماژول `scm-provider-shetab` برای ارتباط ISO8583 روی TCP استفاده می‌شود و مخصوص سناریوهای `OperationType.PROVIDER` است.

URI نمونه:

```text
shetab:request?provider=shetab7
```

## قرارداد ورودی/خروجی

ورودی provider یک `Map` یا JSON object است:

```json
{
  "mti": "1100",
  "fields": {
    "2": "5894631150168490",
    "3": "330000",
    "11": "123456",
    "37": "123456789012"
  },
  "security": {
    "pin": "1234",
    "pinRequired": true,
    "expiryDate": "2907",
    "cvv2": "639",
    "macRequired": true
  }
}
```

خروجی provider یک `Map` با `mti` و `fields` است.

## ساختار یکدست کانفیگ

مانند `scm-provider-nab`:

- `defaults` برای مقدارهای پایه
- `providers.<instance>` برای override هر instance

```yaml
scm:
  provider:
    shetab:
      enabled: true
      defaults:
        connect-timeout-ms: 3000
        socket-timeout-ms: 1000
        response-timeout-ms: 6000
        send-timeout-ms: 1000
        reconnect-delay-ms: 1000
        same-endpoint-reconnect-attempts: 3
        queue-capacity: 1000
        rate-limit:
          enabled: false
          bucket: shetab-default
          key: provider
        endpoint-lease:
          enabled: true
          ttl-ms: 30000
      providers:
        shetab7:
          endpoint: 10.10.10.10:9000
          packager-class: Shetab7AsciiXAPackager
          rate-limit:
            enabled: true
            bucket: shetab7
            key: provider-operation
          security:
            pin:
              key: ${SCM_SHETAB7_PIN_KEY}
              field: 52
              pan-field: 2
            mac:
              key: ${SCM_SHETAB7_MAC_KEY}
              field: 128
              verify-response: false
            expiry:
              field: 14
            cvv2:
              field: 48
              tag: P92
              length-digits: 3
              min-length: 3
              max-length: 4

        shetab8:
          endpoints:
            - 10.10.10.20:9000
            - 10.10.10.21:9000
          packager-class: Shetab7BinaryXAPackager
          rate-limit:
            enabled: true
            bucket: shetab8
            key: provider
```

### نکته endpoint

برای یکدستی با NAB:

- `endpoint` (تکی) پشتیبانی می‌شود.
- `endpoints` (لیست) هم پشتیبانی می‌شود.
- اگر هر دو تعریف شوند، ابتدا `endpoint` و سپس مقادیر `endpoints` استفاده می‌شوند.

## Rate Limit (مشابه NAB)

در هر provider instance:

- `rate-limit.enabled`
- `rate-limit.bucket`
- `rate-limit.key` (`provider`, `operation`, `provider-operation`)

Override در runtime:

- Header: `ShetabRateLimitEnabled`
- Header: `ShetabRateLimitBucket`
- Header: `ShetabRateLimitKey`
- URI param: `rateLimitEnabled`
- URI param: `rateLimitBucket`
- URI param: `rateLimitKey`

نمونه:

```text
shetab:request?provider=shetab7&rateLimitEnabled=true&rateLimitBucket=shetab7
```

## متریک‌ها

متریک‌های in-memory per provider:

- `submitted`
- `succeeded`
- `failed`
- `timedOut`
- `rateLimited`
- `rateLimitWaits`
- `queueRejected`
- `sent`
- `received`
- `totalLatencyMs`

## لاگ‌ها

- `INFO`: شروع/پایان درخواست، اتصال/قطع اتصال، وضعیت lease endpoint
- `DEBUG`: محتوای request/response به‌صورت mask شده
- `WARN`: خطاهای تطبیقی، rate limit reject، unmatched response
- `ERROR`: خطاهای send/connect/request

`SafeIsoLogFormatter` برای ماسک‌کردن اطلاعات حساس استفاده می‌شود (PAN، PIN، MAC، CVV2، Expiry).

## امنیت فیلدها

- `fields.52` و `fields.128` متعلق به provider است.
- `security.expiryDate` به فیلد `14` نگاشت می‌شود.
- `security.cvv2` در `field 48` با tag `P92` ساخته می‌شود.
- اگر caller در `fields.48` مقدار `P92` داده باشد، provider آن را بازسازی می‌کند.

## اجرای تست integration واقعی

```bash
SCM_SHETAB_HPS_INTEGRATION=true \
SCM_SHETAB_HPS_ENDPOINTS=10.10.10.10:9000 \
SCM_SHETAB_HPS_PIN=1234 \
SCM_SHETAB_HPS_PIN_KEY=0123456789ABCDEF \
SCM_SHETAB_HPS_MAC_KEY=0123456789ABCDEF \
./gradlew :scm-provider-shetab:test --tests '*ShetabHpsCardInquiryIntegrationTest'
```

## پیش‌نیاز deployment توزیع‌شده

- برای rate limit توزیع‌شده: `RateLimiterUtility` از `scm-cache-client`
- برای lease توزیع‌شده endpoint: `ResourceLeaseUtility` از `scm-cache-client`

در صورت نبود این utilityها:

- rate limit به حالت noop می‌رود (با WARN)
- endpoint lease به حالت local fallback می‌رود (با WARN)

# scm-provider-rest

ماژول `scm-provider-rest` یک provider عمومی برای فراخوانی HTTP/REST در `OperationType.PROVIDER` است.

URI نمونه:

```text
rest-provider:request?provider=poba-hps
```

## ساختار یکدست کانفیگ

مانند `scm-provider-nab`:

- `defaults` برای تنظیمات پایه
- `providers.<instance>` برای override

```yaml
scm:
  provider:
    rest:
      enabled: true
      defaults:
        base-url: https://example.com
        connect-timeout-ms: 3000
        response-timeout-ms: 6000
        virtual-threads-enabled: true
        insecure-ssl: false
        follow-redirects: NORMAL # NEVER | NORMAL | ALWAYS
        default-method: POST
        headers:
          Accept: application/json
          Content-Type: application/json
        rate-limit:
          enabled: false
          bucket: rest-default
          key: provider
        auth:
          type: NONE # NONE | BASIC | BEARER | JWT | API_KEY
          header-name: Authorization
          prefix: Bearer
          basic-base64: true
        token:
          enabled: false
          cache-name: rest_provider_token_cache
          cache-key: access-token
          lock-name: rest-provider-token
          early-refresh-seconds: 30
          default-expires-in-seconds: 300
          method: POST
          path: /oauth/token
          headers:
            Content-Type: application/x-www-form-urlencoded
          form:
            grant_type: client_credentials
          response-token-field: access_token
          response-expires-in-field: expires_in
          response-token-type-field: token_type
          default-token-type: Bearer
        security:
          sensitive-headers: [authorization, proxy-authorization, cookie, set-cookie]
          sensitive-body-keys: [password, token, secret, pin, cvv, pan, card]
          max-body-log-length: 400

      providers:
        poba-hps:
          base-url: https://poba-hps.example.ir
          auth:
            type: BEARER
          token:
            enabled: true
            path: /oauth/token
            form:
              grant_type: client_credentials
              client_id: ${POBA_CLIENT_ID}
              client_secret: ${POBA_CLIENT_SECRET}
          rate-limit:
            enabled: true
            bucket: poba-hps
            key: provider-operation

        nab-apirepo:
          endpoint: https://nab-apirepo.example.ir # alias of base-url
          default-method: POST
          headers:
            X-System: SCM
          rate-limit:
            enabled: true
            bucket: nab-apirepo
            key: provider
```

### نکته base-url / endpoint

برای یکدستی با سایر providerها:

- `base-url` مقدار اصلی REST است.
- `endpoint` به‌عنوان alias پشتیبانی می‌شود.
- اگر هر دو تعریف شوند، `base-url` اولویت دارد.

## قرارداد ورودی

body می‌تواند یکی از این دو حالت باشد:

1. payload ساده (کل body همان payload درخواست REST)
2. envelope با متادیتا:

```json
{
  "method": "POST",
  "path": "/api/v1/card/inquiry",
  "query": {
    "channel": "mobile"
  },
  "headers": {
    "X-Correlation-Id": "abc-123"
  },
  "body": {
    "pan": "5894631150168490"
  }
}
```

## قرارداد خروجی

```json
{
  "status": 200,
  "headers": {
    "Content-Type": "application/json"
  },
  "body": {
    "resultCode": "00"
  }
}
```

## Rate Limit (مشابه NAB)

پشتیبانی کامل برای:

- `rate-limit.enabled`
- `rate-limit.bucket`
- `rate-limit.key` (`provider`, `operation`, `provider-operation`)

Override در runtime:

- Header: `RestProviderRateLimitEnabled`
- Header: `RestProviderRateLimitBucket`
- Header: `RestProviderRateLimitKey`
- URI param: `rateLimitEnabled`
- URI param: `rateLimitBucket`
- URI param: `rateLimitKey`

## Header/URI Override های runtime

- `RestProvider` (provider name)
- `RestProviderMethod`
- `RestProviderUrl`
- `RestProviderPath`
- `RestProviderTimeoutMs`
- `RestProviderRateLimitEnabled`
- `RestProviderRateLimitBucket`
- `RestProviderRateLimitKey`

## Token Management (چند پاد)

وقتی `token.enabled=true`:

1. ابتدا token از cache خوانده می‌شود.
2. اگر معتبر بود reuse می‌شود.
3. در غیر این صورت lock توزیع‌شده (`lock-name::provider`) گرفته می‌شود.
4. فقط یک worker token را refresh می‌کند.
5. token با TTL مناسب در cache ذخیره می‌شود.

این رفتار برای Kubernetes و multi-pod مناسب است.

## متریک‌ها

متریک‌های in-memory per provider:

- `submitted`
- `succeeded`
- `failed`
- `clientErrors`
- `serverErrors`
- `timedOut`
- `rateLimited`
- `rateLimitWaits`
- `totalLatencyMs`
- `tokenCacheHits`
- `tokenRefreshes`
- `tokenRefreshFailures`

## لاگ‌ها

- `INFO`: ارسال/دریافت HTTP با provider/method/url/status/elapsed
- `DEBUG`: جزئیات token/cache hit و جزئیات sanitize شده
- `WARN`: rate-limit reject، حذف auth headerهای request-level، timeout/error
- `ERROR`: خطاهای refresh token

بدنه/هدر با `RestProviderLogSanitizer` sanitize می‌شود.

## پیش‌نیاز deployment توزیع‌شده

برای بهترین عملکرد در چند پاد:

- `CacheManager` برای token cache
- `LockUtility` برای token single-flight
- `RateLimiterUtility` برای rate limit توزیع‌شده

اگر `RateLimiterUtility` نباشد، rate-limit به noop fallback می‌رود (با WARN).

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
        customizers:
          authentication: false
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
          cache:
            enabled: true
            mode: centralized
            key-prefix: provider-token
            refresh-skew: 30s
            ttl-skew: 5s
          lock:
            enabled: true
            key-prefix: provider-token-refresh-lock
            wait-timeout: 3s
            lease-time: 10s
            retry-delay: 100ms
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
          apply:
            location: header
            name: Authorization
            format: "{tokenType} {accessToken}"
        security:
          sensitive-headers: [authorization, proxy-authorization, cookie, set-cookie]
          sensitive-body-keys: [password, token, secret, pin, cvv, pan, card]
          max-body-log-length: 400

      providers:
        poba-hps:
          base-url: https://poba-hps.example.ir
          customizers:
            authentication: true
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

## ProviderMessageCustomizer

`ProviderMessageCustomizer` قرارداد مشترک آماده‌سازی پیام provider است. خود interface در `scm-common` قرار دارد و Spring-independent است؛ implementationها می‌توانند Spring bean باشند.

Producer REST در این lifecycle آن را اجرا می‌کند:

1. `ProviderExchange` ساخته می‌شود.
2. customizerها با `supports(context)` فیلتر می‌شوند.
3. customizerها با `order()` مرتب می‌شوند.
4. `beforeSend(exchange)` اجرا می‌شود.
5. درخواست نهایی به transport ارسال می‌شود.
6. response داخل `ProviderExchange` قرار می‌گیرد.
7. `afterReceive(exchange)` اجرا می‌شود.
8. response نهایی به Camel برگردانده می‌شود.

قواعد:

- customizer باید stateless باشد.
- state مربوط به request فقط داخل `ProviderExchange`, `ProviderMessageCustomizerContext` یا `exchange.attributes()` قرار بگیرد.
- constructor injection استفاده کنید.
- REST provider فیلد MAC ندارد. برای REST هیچ MAC customizer فعال نکنید.

Orderهای پیشنهادی:

- `100..999`: field enrichment مثل `outlet`, `terminalId`, `merchantId`
- `5000`: authentication
- `8000`: pin-block در providerهایی که نیاز دارند
- `10000`: MAC فقط برای ISO8583/Shetab، نه REST
- `20000`: response enrichment

نمونه customizer:

```java
import ir.daneshrefah.scm.common.provider.message.ProviderExchange;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizer;
import ir.daneshrefah.scm.common.provider.message.ProviderMessageCustomizerContext;
import org.springframework.stereotype.Component;

@Component
public class OutletProviderMessageCustomizer implements ProviderMessageCustomizer {

    @Override
    public boolean supports(ProviderMessageCustomizerContext context) {
        return context.providerConfig().containsKey("outlet");
    }

    @Override
    public int order() {
        return 100;
    }

    @Override
    public void beforeSend(ProviderExchange exchange) {
        String outlet = exchange.context()
                .providerConfig()
                .get("outlet")
                .toString();

        exchange.request().putField("outlet", outlet);
    }
}
```

## REST Authentication Customizer

`RestAuthenticationProviderMessageCustomizer` جایگزین اجرای مستقیم token داخل producer شده است.

این customizer:

- فقط `transportType=rest` را support می‌کند.
- فقط وقتی `token.enabled=true` و `customizers.authentication=true` باشد اجرا می‌شود.
- auth-url را مستقیم صدا نمی‌زند.
- token را از `ProviderAuthTokenProvider` / `RestProviderTokenManager` می‌گیرد.
- token را طبق `token.apply` روی header/body/query قرار می‌دهد.

نمونه:

```yaml
scm:
  provider:
    rest:
      providers:
        hps-rest:
          type: rest
          transport: rest
          base-url: http://provider
          customizers:
            authentication: true
          auth:
            type: BEARER
            header-name: Authorization
          token:
            enabled: true
            url: http://provider/auth/token
            method: POST
            form:
              grant_type: client_credentials
              client_id: ${HPS_CLIENT_ID}
              client_secret: ${HPS_CLIENT_SECRET}
            cache:
              enabled: true
              mode: centralized
              key-prefix: provider-token
              refresh-skew: 60s
              ttl-skew: 5s
            lock:
              enabled: true
              key-prefix: provider-token-refresh-lock
              wait-timeout: 3s
              lease-time: 10s
              retry-delay: 100ms
            response-token-field: access_token
            response-expires-in-field: expires_in
            response-token-type-field: token_type
            default-token-type: Bearer
            apply:
              location: header
              name: Authorization
              format: "{tokenType} {accessToken}"
```

Token apply modes:

- `location: header`, مثل `Authorization` یا `X-Auth-Token`
- `location: body`, برای providerهایی که token را در body می‌خواهند
- `location: query`, فقط اگر provider config قبلا چنین رفتاری را پشتیبانی می‌کرده باشد

`Authorization` hardcode نشده است. اگر provider از `X-Auth-Token` استفاده می‌کند، `token.apply.name` را همان مقدار بگذارید.

## Centralized Token Cache And Distributed Lock

وقتی `token.enabled=true`:

1. token key با provider/authProfile/channel/credential ساخته می‌شود.
2. centralized cache اول خوانده می‌شود.
3. اگر token معتبر باشد و هنوز وارد refresh skew نشده باشد، reuse می‌شود.
4. اگر token missing/expired باشد، distributed lock گرفته می‌شود.
5. بعد از گرفتن lock، cache دوباره خوانده می‌شود.
6. فقط lock owner auth-url را صدا می‌زند.
7. token جدید با TTL برابر expiry منهای `ttl-skew` در cache ذخیره می‌شود.
8. lock در utility موجود به‌صورت safe آزاد می‌شود.
9. اگر lock گرفته نشود، producer تا `lock.wait-timeout` با فاصله `lock.retry-delay` cache را poll می‌کند.
10. اگر token ظاهر نشود، fault امن مثل `PROVIDER_AUTH_LOCK_TIMEOUT` یا `PROVIDER_AUTH_TOKEN_UNAVAILABLE` پرتاب می‌شود.

Keyهای پیش‌فرض:

```text
provider-token:<providerCode>:<authProfile>:<channelCode>:<credentialKey>
provider-token-refresh-lock:<providerCode>:<authProfile>:<channelCode>:<credentialKey>
```

Backward compatibility:

- `token.cache-name`, `token.cache-key`, `token.lock-name`, `token.early-refresh-seconds`, `token.default-expires-in-seconds`, `token.response-token-field`, `token.response-expires-in-field`, `token.response-token-type-field` هنوز پشتیبانی می‌شوند.
- اگر `token.lock-name` صریح تنظیم شده باشد، به عنوان lock prefix استفاده می‌شود.
- `early-refresh-seconds` به `cache.refresh-skew` نگاشت می‌شود، مگر اینکه `cache.refresh-skew` صریح تنظیم شود.
- auth endpoint credential باید در `token.auth`, `token.headers`, `token.form` یا `token.body` تنظیم شود. final provider auth به auth-url تزریق نمی‌شود.

پیش‌نیاز deployment توزیع‌شده:

- `CacheManager` از `scm-cache-client` برای token cache
- `LockUtility` از `scm-cache-client` برای distributed single-flight
- `RateLimiterUtility` برای rate limit توزیع‌شده

برای token cache centralized، local-only fallback استفاده نمی‌شود. اگر cache یا lock موجود نباشد، token flow با fault امن fail می‌شود.

## Security Rules

- username/password/client-secret/access-token/refresh-token لاگ نشوند.
- token، password، client-secret، PIN، PIN block و MAC داخل trace attribute قرار نگیرند.
- REST provider MAC ندارد؛ MAC را برای REST فعال نکنید.
- request-level `Authorization` و `Proxy-Authorization` از caller پذیرفته نمی‌شود.
- header/body با `RestProviderLogSanitizer` sanitize می‌شود.

## Observability

Trace:

- span برای call اصلی provider وجود دارد.
- هر customizer یک span با `customizerName`, `phase`, `providerCode`, `serviceCode`, `operationCode`, `channelCode`, `transportType` دارد.
- token manager روی span فعال eventهایی مثل cache hit/miss، lock acquired/timeout، auth request و token refresh ثبت می‌کند.
- secret/token/PIN/MAC در span ثبت نمی‌شود.

Log:

- `DEBUG`: customizerهای match شده، order اجرا، token cache hit/miss، lock acquire، token refresh
- `WARN`: rate-limit reject، حذف auth headerهای request-level، lock timeout، fallbackهای غیرایده‌آل
- `ERROR`: auth-url failure، token parsing failure، cache failure، lock failure

Metric counters/timers موجود در module:

- provider request: `submitted`, `succeeded`, `failed`, `clientErrors`, `serverErrors`, `timedOut`, `totalLatencyMs`
- rate limit: `rateLimited`, `rateLimitWaits`
- customizer: `customizerExecutions`, `customizerErrors`
- auth token: `tokenCacheHits`, `tokenCacheMisses`, `tokenCachePuts`, `tokenLockAcquired`, `tokenLockTimeouts`, `tokenRefreshes`, `tokenRefreshFailures`, `tokenRequestLatencyMs`

در deployment نهایی، این مقادیر باید طبق مسیر استاندارد SCM از Actuator/Micrometer به Prometheus/Grafana expose شوند؛ metric در فایل نوشته نمی‌شود.

## Troubleshooting

`auth token not refreshed`

- `token.enabled=true` و `customizers.authentication=true` را بررسی کنید.
- `token.response-token-field` و `token.response-expires-in-field` را با response واقعی auth-url تطبیق دهید.
- مطمئن شوید `cache.refresh-skew` از expiry token بزرگ‌تر نیست.

`distributed lock timeout`

- `LockUtility` از `scm-cache-client` باید در runtime موجود باشد.
- `token.lock.wait-timeout` و `token.lock.retry-delay` را بررسی کنید.
- اگر چند pod دارید، lock backend باید remote باشد، نه local.

`centralized cache unavailable`

- `CacheManager` باید cache name مثل `rest_provider_token_cache` را resolve کند.
- برای token cache centralized، نبود cache باعث fault می‌شود و local-only fallback نداریم.

`customizer not executed`

- customizer باید Spring bean باشد.
- `supports(context)` باید `true` برگرداند.
- برای REST auth، `transportType` باید `rest` باشد و `auth.type` یکی از `BEARER`, `JWT`, `API_KEY`.

`wrong order`

- `order()` را با rangeهای پیشنهادی تنظیم کنید.
- authentication معمولاً `5000` است و بعد از field enrichment اجرا می‌شود.

`missing Authorization header`

- اگر header می‌خواهید، `token.apply.location=header` و `token.apply.name=Authorization` را تنظیم کنید.
- اگر provider از `X-Auth-Token` استفاده می‌کند، دنبال `Authorization` نگردید؛ `token.apply.name` همان header نهایی است.

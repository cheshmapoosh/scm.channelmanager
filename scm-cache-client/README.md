# scm-cache-client

این ماژول از ۳ backend برای cache پشتیبانی می‌کند:

1. `LOCAL` با Caffeine  
2. `REMOTE` با Hazelcast (اتصال به `scm-cache`)  
3. `NEAR` (روی remote map با Near Cache فعال)

## تنظیمات

```yaml
scm:
  cache:
    client:
      distributed: true
      default-type: remote
      default-ttl: 30m
      default-maximum-size: 10000
      utilities:
        rate-limit: remote # local | remote
        lock: remote       # local | remote
        concurrency-limit: remote # local | remote
        resource-lease: remote # local | remote
        rate-limit-names:
          login: local
        lock-names:
          user-update: remote
        concurrency-limit-names:
          otp-send: remote
        resource-lease-names:
          shetab-local-port: remote
      caches:
        user_cache:
          type: near
        session_cache:
          type: local
          ttl: 20m
          maximum-size: 50000
        authority_cache:
          type: remote
          remote-name: security_authority_map

      # client config for hazelcast
      config:
        cluster-name: dev
        network:
          cluster-members:
            - 127.0.0.1:5701
```

## استفاده با Spring Cache Annotation

```java
@Service
public class UserService {

    @Cacheable(cacheNames = "user_cache", key = "#username")
    public UserDto findUser(String username) {
        return loadFromDatabase(username);
    }

    @CacheEvict(cacheNames = "user_cache", key = "#username")
    public void invalidateUser(String username) {
    }
}
```

Routing بر اساس `cacheNames` انجام می‌شود. یعنی هر cache name به صورت مستقل می‌تواند `local/remote/near` باشد.

## استفاده با CacheTemplate

```java
cacheTemplate.putInCache("session_cache", sessionKey, userAuth);
UserAuthentication auth = (UserAuthentication) cacheTemplate.getFromCache("session_cache", sessionKey);
```

## Cache لایه دیتابیس در Spring

برای cache دیتابیس (service/repository method cache) فقط کافی است annotationهای Spring (`@Cacheable`, `@CachePut`, `@CacheEvict`) را استفاده کنید و برای هر `cacheNames` نوع backend را در تنظیمات بالا تعیین کنید.

## Utility های آماده برای Rate Limit / Lock / Concurrency Limit / Resource Lease

این ماژول utility های زیر را به صورت bean در اختیار شما قرار می‌دهد:

- `RateLimiterUtility` (روی `bucket4j` + backend قابل انتخاب `local/remote`)
- `LockUtility` (روی backend قابل انتخاب `local/remote`)
- `ConcurrencyLimiterUtility` (روی backend قابل انتخاب `local/remote`)
- `ResourceLeaseUtility` (روی backend قابل انتخاب `local/remote`)

### انتخاب backend برای Utility ها

```yaml
scm:
  cache:
    client:
      utilities:
        rate-limit: remote # local | remote
        lock: remote       # local | remote
        concurrency-limit: remote # local | remote
        resource-lease: remote # local | remote
        rate-limit-names:
          login: local
        lock-names:
          user-update: remote
        concurrency-limit-names:
          otp-send: remote
        resource-lease-names:
          shetab-local-port: remote
```

- `local`: داخل همان JVM/Pod نگهداری می‌شود و بین پادها مشترک نیست.
- `remote`: روی Hazelcast اجرا می‌شود و برای محدودیت/lock/concurrency-limit توزیع‌شده بین چند پاد مناسب است.
- مقدارهای `rate-limit`, `lock`, `concurrency-limit`, `resource-lease` پیش‌فرض همان utility هستند.
- mapهای `*-names` برای override بر اساس `name` استفاده می‌شوند. اگر نام نهایی به فرم `name::keyPart` باشد، ابتدا exact match و بعد بخش قبل از `::` بررسی می‌شود.
- برای نیازهایی مثل اخذ پورت منحصر به فرد بین پادها، از backend `remote` برای `resource-lease` استفاده کنید.

### Resource Lease برای منابع انحصاری (مثل local port)

```java
import ir.daneshrefah.scm.cache.client.utility.resourcelease.ResourceLease;
import ir.daneshrefah.scm.cache.client.utility.resourcelease.ResourceLeaseUtility;

try (ResourceLease lease = resourceLeaseUtility.acquire(
        "shetab-local-port::0.0.0.0",
        List.of("40001", "40002", "40003"),
        Duration.ofSeconds(30)
)) {
    int selectedPort = Integer.parseInt(lease.resourceName());
    // use selectedPort
}
```

ویژگی‌ها:

- تا وقتی lease باز است، utility به شکل heartbeat آن را refresh می‌کند.
- اگر pod/release crash شود، بعد از TTL منقضی می‌شود و resource به pool برمی‌گردد.
- با `close()` یا پایان `try-with-resources`، resource بلافاصله آزاد می‌شود.

### Lock با Annotation استاندارد ماژول

```java
import ir.daneshrefah.scm.cache.client.utility.lock.annotation.WithLock;

@WithLock(name = "user-update", key = "'uid::' + #p1.id", waitMillis = 1200)
public void updateUser(String actor, User user) {
    // ...
}
```

- نام annotation: `@WithLock`
- `name`: نام پایه lock
- `key`: کلید سفارشی با SpEL (مثل `#p0.id`, `#p1.profile.customerId`)
- `perUser`: اگر `true` باشد و `key` خالی باشد، کلید بر اساس کاربر جاری ساخته می‌شود
- `global`: اگر `true` باشد و `key` خالی باشد، کلید ثابت `global` استفاده می‌شود
- `waitMillis`: رفتار انتظار برای lock:
  - منفی: بلاک تا آزاد شدن lock
  - صفر: تلاش فوری (`tryLock`)
  - مثبت: انتظار تا مدت مشخص

قاعده کلید نهایی lock:

- خروجی نهایی همیشه به فرم `name::keyPart` ساخته می‌شود.
- اگر `key` تنظیم شده باشد، `keyPart` از `evaluateExpressionKey` می‌آید.
- اگر `key` خالی باشد:
  - `global=true` -> `keyPart = global`
  - `perUser=true` -> `keyPart = class::method::uid::username`
  - در غیر این صورت -> `keyPart = class::method`

نکته:

- حالت `perUser=true` و `global=true` همزمان نامعتبر است و خطا می‌دهد.

نمونه Utility برای lock به‌صورت بلاکی:

```java
lockUtility.runWithLock("ledger::sync", true, () -> {
    // critical section
});
```

### Concurrency Limit با Annotation استاندارد ماژول

```java
import ir.daneshrefah.scm.cache.client.utility.concurrencylimit.annotation.WithConcurrencyLimit;

@WithConcurrencyLimit(name = "otp-send", key = "'uid::' + #p1.id", maxConcurrentExecutions = 3, waitMillis = -1)
public void sendOtp(String actor, User user) {
    // ...
}
```

- نام annotation: `@WithConcurrencyLimit`
- `name`: نام پایه concurrency limit
- `maxConcurrentExecutions`: حداکثر اجرای همزمان
- `key`: کلید سفارشی با SpEL (مثل `#p0.id`, `#p1.profile.customerId`)
- `perUser`: اگر `true` باشد و `key` خالی باشد، کلید بر اساس کاربر جاری ساخته می‌شود
- `global`: اگر `true` باشد و `key` خالی باشد، کلید ثابت `global` استفاده می‌شود
- `waitMillis`: رفتار انتظار برای slot:
  - منفی: بلاک تا آزاد شدن slot
  - صفر: تلاش فوری (`tryAcquire`)
  - مثبت: انتظار تا مدت مشخص

قاعده کلید نهایی concurrency limit:

- خروجی نهایی همیشه به فرم `name::keyPart` ساخته می‌شود.
- اگر `key` تنظیم شده باشد، `keyPart` از `evaluateExpressionKey` می‌آید.
- اگر `key` خالی باشد:
  - `global=true` -> `keyPart = global`
  - `perUser=true` -> `keyPart = class::method::uid::username`
  - در غیر این صورت -> `keyPart = class::method`

### الگوی Functional یکسان برای هر ۳ Utility + کنترل خطا

الگوی پیشنهادی یکسان:

- مسیر موفق: `execute...(..., () -> business)`
- مسیر خطا: `execute...(..., () -> business, exception -> fallback)`
- برای متدهای `void`: `run...(..., () -> business, exception -> handle)`

نمونه `RateLimiterUtility`:

```java
String result = rateLimiterUtility.executeRateLimited(
        "uaa_nib_activation",
        "uid::" + userId,
        () -> activationService.activate(userId),
        exception -> "fallback-response"
);
```

نمونه `LockUtility`:

```java
String result = lockUtility.executeWithLock(
        "ledger::settlement",
        true,
        () -> settlementService.run(),
        exception -> "fallback-response"
);
```

نمونه `ConcurrencyLimiterUtility`:

```java
String result = concurrencyLimiterUtility.executeWithConcurrencyLimit(
        "otp-send-queue",
        20,
        null,
        () -> otpService.send(userId),
        exception -> "fallback-response"
);
```

نکته:

- برای lock/concurrency-limit/rate-limit اگر callback خطا ندهید، خطا به‌صورت exception پرتاب می‌شود.

### Rate limit با Annotation استاندارد ماژول

```java
import ir.daneshrefah.scm.cache.client.utility.ratelimit.annotation.WithRateLimit;

@WithRateLimit(name = "uaa_nib_activation", perUser = true)
public void activate() {
    // ...
}
```

و تعریف bucket در تنظیمات مرکزی:

```yaml
scm:
  rate-limit:
    config:
      definitions:
        uaa_nib_activation:
          token-capacity: 5
          refill-intervally:
            token: 5
            period-seconds: 30
```

- نام annotation: `@WithRateLimit`
- `name`: نام rate-limit bucket
- `perUser`: کلید به‌صورت per-user
- `global`: کلید سراسری ثابت (`global`)
- `key`: کلید سفارشی با SpEL (اولویت بالاتر از موارد بالا)

نمونه `CUSTOM`:

```java
@WithRateLimit(name = "login", key = "'uid::' + #authenticationName")
public void login() {
    // ...
}
```

### راهنمای دقیق `@WithRateLimit` و `evaluateExpressionKey`

ترتیب تعیین `key` در runtime به این صورت است:

1. اگر `key` در annotation مقدار داشته باشد، متد `evaluateExpressionKey(...)` اجرا می‌شود.
2. اگر همزمان `perUser=true` و `global=true` باشد، خطا (`IllegalArgumentException`) رخ می‌دهد.
3. اگر `global=true` باشد، کلید ثابت `global` استفاده می‌شود.
4. اگر `perUser=true` باشد، کلید به شکل `class::method::uid::username` ساخته می‌شود.
5. در حالت پیش‌فرض (بدون `key`/`perUser`/`global`)، کلید متد (`class::method`) استفاده می‌شود.

متد `evaluateExpressionKey(...)` چه می‌کند:

- عبارت `SpEL` را از فیلد `key` می‌گیرد و parse می‌کند.
- با `MethodBasedEvaluationContext`، context اجرای همان متد را می‌سازد (target + method + args).
- دو متغیر کمکی داخل context قرار می‌دهد:
  - `#methodName`
  - `#authenticationName`
- عبارت را evaluate می‌کند و خروجی را به `String` تبدیل می‌کند.
- اگر خروجی `null` یا رشته خالی باشد، خطا می‌دهد (`IllegalArgumentException`) تا کلید نامعتبر وارد سیستم نشود.

متغیرهای قابل استفاده در `key`:

- `#methodName`
- `#authenticationName`
- آرگومان‌های متد: `#p0`, `#p1`, ... (و در صورت قابل‌دسترسی بودن نام پارامترها، مثل `#terminalCode`)

نمونه‌های کاربردی `key`:

```java
@WithRateLimit(name = "otp_send", key = "'terminal::' + #p0")
public void sendOtp(String terminalCode) {
    // ...
}
```

```java
@WithRateLimit(name = "user_login", key = "'uid::' + #authenticationName + '::m::' + #methodName")
public void login() {
    // ...
}
```

```java
@WithRateLimit(name = "global_health_check", global = true)
public void healthCheck() {
    // ...
}
```

نکته عملی:

- اگر `Spring Security` در context نباشد یا کاربر قابل تشخیص نباشد، مقدار `#authenticationName` برابر `anonymous` می‌شود.

### نمونه Rate Limit برای Camel Processor

```java
@Component
@RequiredArgsConstructor
public class CamelRateLimitProcessor implements Processor {

    private final RateLimiterUtility rateLimiterUtility;

    @Override
    public void process(Exchange exchange) {
        String terminalCode = exchange.getIn().getHeader("terminalCode", String.class);
        RateLimitResult result = rateLimiterUtility.tryConsume("uaa_nib_activation", "terminal::" + terminalCode);

        // useful headers for 429 response or observability
        exchange.getIn().getHeaders().putAll(result.toHeaders());

        if (!result.allowed()) {
            exchange.getMessage().setHeader(Exchange.HTTP_RESPONSE_CODE, 429);
            throw new RuntimeException("rate limit exceeded");
        }
    }
}
```

### تنظیم رفتار bucket تعریف‌نشده

```yaml
scm:
  rate-limit:
    config:
      # REJECT (default) | ALLOW
      missing-bucket-policy: reject
```

### تنظیم رفتار در زمان رسیدن به سقف

```yaml
scm:
  rate-limit:
    config:
      # REJECT (default) | WAIT
      overflow-policy: wait
      # global fallback max wait time
      max-wait-duration: 3s
      definitions:
        uaa_nib_activation:
          token-capacity: 10
          refill-intervally:
            token: 10
            period-seconds: 60
          # per-bucket override (optional)
          max-wait-duration: 8s
```

در حالت `WAIT`، درخواست reject فوری نمی‌شود و thread تا مدت مشخص‌شده منتظر refill می‌ماند و دوباره consume را امتحان می‌کند.  
اگر برای bucket مقدار اختصاصی تعریف شود، همان استفاده می‌شود؛ در غیر این صورت مقدار سراسری fallback می‌شود. اگر در این بازه token آزاد نشود، نتیجه نهایی همان reject خواهد بود.

### اجرای صف‌محور واقعی (بدون sleep ثابت)

اگر می‌خواهید «به محض آزاد شدن یک slot، درخواست بعدی اجرا شود»، از `ConcurrencyLimiterUtility` استفاده کنید:

```java
@Component
@RequiredArgsConstructor
public class QueuedProcessor implements Processor {

    private final ConcurrencyLimiterUtility concurrencyLimiterUtility;

    @Override
    public void process(Exchange exchange) {
        concurrencyLimiterUtility.runWithConcurrencyLimit("camel-queue::activation", 20, null, () -> {
            // business logic
        });
    }
}
```

در مثال بالا:

- `20` یعنی حداکثر اجرای همزمان.
- `waitTime = null` یعنی تا آزاد شدن slot منتظر بماند (queue-like behavior).
- ترد بعدی بلافاصله بعد از `release` شدن slot اجرا می‌شود.

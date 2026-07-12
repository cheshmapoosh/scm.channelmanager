# راهنمای فنی `scm-provider-shetab`

## ۱. هدف و مرز ماژول

`scm-provider-shetab` آداپتر ISO 8583 روی TCP برای operationهای نوع `OperationType.PROVIDER` است.

URI هر provider از الگوی زیر پیروی می‌کند:

```text
<scheme>:<providerCode>
```

نمونه:

```text
scm-shetab:hps-shetab7
```

این ماژول مسئول موارد زیر است:

- resolve و اعتبارسنجی تنظیمات provider؛
- ساخت و اجرای pipeline مربوط به `ProviderMessageCustomizer`؛
- pack/unpack پیام ISO 8583 با jPOS؛
- نگهداری یک TCP client پایدار برای هر provider در هر JVM؛
- مدیریت صف ارسال، correlation، timeout و پاسخ‌های هم‌زمان؛
- مدیریت endpoint lease در deployment توزیع‌شده؛
- reconnect محدود روی endpoint فعلی و re-lease پس از شکست‌های متوالی؛
- metric و log عملیاتی provider.

این ماژول مسئول business routing، تعریف Service، تبدیل مدل Gateway، retry تراکنش مالی یا تصمیم‌گیری دربارهٔ idempotency نیست.

---

## ۲. نمای سریع معماری

```text
Camel operation
    ↓
Shetab producer
    ↓
ShetabTcpClientRegistry
    ↓ one client per normalized provider code / JVM
ShetabIsoChannelClient
    ├── send queue
    ├── sender thread
    ├── receiver thread
    ├── ShetabChannelSessionManager
    │     ├── endpoint lease
    │     ├── active ChannelSession
    │     ├── generation
    │     └── reconnect / re-lease policy
    └── ShetabResponseRegistry
          ├── exact STAN+RRN index
          ├── STAN fallback index
          ├── RRN fallback index
          └── pending response lifecycle
```

اصل معماری:

```text
یک provider در یک JVM
    → یک ShetabIsoChannelClient
    → حداکثر یک ChannelSession فعال
    → یک socket مشترک برای send و receive
```

در Kubernetes هر Pod یک JVM مستقل دارد. جلوگیری از رزرو هم‌زمان یک endpoint توسط چند Pod بر عهدهٔ endpoint lease توزیع‌شده است.

---

## ۳. invariantهای حیاتی

تغییرات آینده نباید این قواعد را نقض کنند:

1. `ShetabTcpClientRegistry` برای هر provider نرمال‌شده فقط یک client ایجاد می‌کند.
2. در هر client فقط یک session فعال وجود دارد.
3. sender و receiver از همان socket و همان generation استفاده می‌کنند.
4. فقط sender مجاز به acquire lease، ساخت channel و connect/reconnect است.
5. receiver هرگز connection ایجاد نمی‌کند و فقط منتظر session منتشرشده می‌ماند.
6. lease متعلق به endpoint است؛ چند generation متوالی می‌توانند از همان lease استفاده کنند.
7. موفقیت `connect()` به‌تنهایی endpoint را سالم اثبات نمی‌کند.
8. `sameEndpointFailureCount` فقط بعد از دریافت و match شدن پاسخ معتبر صفر می‌شود.
9. پس از ورود به `ISOChannel.send()`، request هرگز requeue یا resend نمی‌شود.
10. retry ارسال پیام قابل تنظیم نیست و propertyای با نام `request-send-retry-attempts` نباید اضافه شود.
11. requestهای ارسال‌شده روی generation خراب fail می‌شوند؛ requestهای واقعاً `QUEUED` می‌توانند پس از reconnect ادامه دهند.
12. correlation دقیق `STAN + RRN` مرجع اصلی است و fallback مبهم مجاز نیست.
13. تمام waitهای قابل‌کنترل از یک deadline یکنواخت استفاده می‌کنند.
14. thread یا callback مربوط به generation قدیمی نباید session جدید را invalidate یا close کند.

---

## ۴. مدل اصلی پیکربندی

Providerها در registry مشترک پیکربندی می‌شوند:

```yaml
scm:
  providers:
    hps-shetab7:
      scheme: scm-shetab
      enabled: true

      endpoints:
        - 10.10.10.11:9000
        - 10.10.10.12:9000

      packager-class: Shetab7AsciiXAPackager

      connect-timeout-ms: 3000
      socket-timeout-ms: 0
      keep-alive: true
      response-timeout-ms: 6000
      send-timeout-ms: 1000
      reconnect-delay-ms: 1000
      same-endpoint-reconnect-attempts: 3
      queue-capacity: 1000

      rate-limit:
        enabled: true
        bucket: hps-shetab7
        key: provider-operation

      endpoint-lease:
        enabled: true
        ttl-ms: 30000

      message-customizers:
        - type: hps-shetab-outlet
          config:
            field: 42
            value: "123456789012345"

        - type: hps-shetab-terminal
          config:
            field: 41
            value: "12345678"

        - type: shetab-expiry
          config:
            field: 14
            source: security.expiryDate

        - type: shetab-cvv2
          config:
            field: 48
            tag: P92
            source: security.cvv2
            length-digits: 3
            min-length: 3
            max-length: 4

        - type: shetab-pin-block
          config:
            key: ${SCM_HPS_SHETAB7_PIN_KEY}
            field: 52
            pan-field: 2
            pin-source: security.pin

        - type: shetab-mac
          config:
            key: ${SCM_HPS_SHETAB7_MAC_KEY}
            field: 128
            verify-response: false
```

### معنای timeoutها

| تنظیم | مسئولیت |
| --- | --- |
| `connect-timeout-ms` | سقف هر تلاش TCP connect؛ همیشه به زمان باقی‌ماندهٔ request محدود می‌شود |
| `socket-timeout-ms` | timeout عملیات receive؛ مقدار پیش‌فرض `0` یعنی receiver تا زمان دریافت یا failure به‌صورت blocking باقی می‌ماند |
| `keep-alive` | فعال‌سازی TCP keep-alive در channel؛ پیش‌فرض `true` است |
| `response-timeout-ms` | deadline caller برای کل lifecycle درخواست |
| `send-timeout-ms` | حداکثر انتظار برای ورود به send queue، محدود به deadline باقی‌مانده |
| `reconnect-delay-ms` | فاصلهٔ reconnect؛ sleep نباید زیر session lock انجام شود |
| `same-endpoint-reconnect-attempts` | تعداد failureهای متوالی قابل تحمل روی lease فعلی قبل از release و acquire مجدد |

### قواعد configuration

- block عمومی `defaults` برای provider وجود ندارد؛ هر instance باید تنظیمات اصلی خود را صریح داشته باشد.
- `scheme` برای این provider باید `scm-shetab` باشد.
- `endpoints` مدل اصلی است؛ `endpoint` فقط alias تک-endpoint است.
- `message-customizers` اختیاری است و customizer پیش‌فرضی فعال نمی‌شود.
- PIN block، MAC، expiry و CVV2 فقط از طریق customizerها اعمال می‌شوند.
- secretها باید از environment یا secret store وارد شوند؛ مقدار secret نباید داخل repository قرار گیرد.
- تغییر runtime config برای providerای که client آن قبلاً ساخته شده است نباید silently نادیده گرفته شود؛ Registry باید config ناسازگار را fail-fast کند یا lifecycle صریح reload داشته باشد.

---

## ۵. مسئولیت کلاس‌های اصلی

### `ShetabConfigResolver`

- provider reference را normalize می‌کند؛
- config را از registry مشترک bind می‌کند؛
- scheme، endpoint و packager را validate می‌کند؛
- customizer pipeline immutable را یک‌بار می‌سازد؛
- defaultهای فنی مانند timeout و queue capacity را resolve می‌کند.

### `ShetabTcpClientRegistry`

- کلید provider را با `trim + lowercase` نرمال می‌کند؛
- با `computeIfAbsent` یک client برای هر provider در هر JVM می‌سازد؛
- lifecycle clientها را در shutdown متوقف می‌کند؛
- دریافت config متفاوت برای همان provider را باید به‌صورت صریح مدیریت کند، نه اینکه config جدید را بی‌صدا نادیده بگیرد.

### `ShetabIsoChannelClient`

Orchestrator اصلی است و فقط این مسئولیت‌ها را نگه می‌دارد:

- API هم‌زمان `request()`؛
- send queue؛
- sender loop؛
- receiver loop؛
- start/stop؛
- اتصال metric و log به lifecycle درخواست.

منطق lease/session و correlation نباید دوباره داخل این کلاس پراکنده شود.

### `ShetabChannelSessionManager`

کلاس package-private مسئول:

- acquire/release endpoint lease؛
- نگهداری تنها `ChannelSession` فعال؛
- generation صعودی؛
- connect/reconnect؛
- invalidate فقط برای generation مطابق؛
- `sameEndpointFailureCount`؛
- شمارندهٔ response timeoutهای متوالی؛
- signal کردن receiver هنگام publish یا invalidate session.

### `ShetabResponseRegistry`

کلاس package-private مسئول:

- ثبت tracker قبل از enqueue؛
- جلوگیری از duplicate exact correlation؛
- indexهای exact، STAN و RRN؛
- match امن پاسخ؛
- completion/failure خارج از lock داخلی registry؛
- fail کردن requestهای in-flight یک generation؛
- cleanup trackerهای منقضی‌شده.

---

## ۶. مدل Thread و Connection

Client دو thread daemon دارد:

```text
sender thread
    مسئول dequeue، connect/reconnect و send

receiver thread
    مسئول wait session، receive و match response
```

### چرا فقط sender connect می‌کند؟

اگر sender و receiver هر دو connection بسازند، برای جلوگیری از دو socket هم‌زمان به single-flight و synchronization پیچیده نیاز است. مالکیت connection توسط sender این پیچیدگی را حذف و invariant تک‌سوکت را واضح می‌کند.

### قاعدهٔ lock

عملیات زیر نباید زیر session lock اجرا شوند:

```text
lease acquire
channel creation
channel.connect()
reconnect sleep
channel.disconnect()
```

Lock فقط برای تغییرات کوتاه state استفاده می‌شود:

```text
publish activeSession
invalidate activeSession
update lease reference
update failure counters
signal receiver
```

---

## ۷. `ChannelSession` و Generation

Session یک value object immutable است:

```java
record ChannelSession(
        long generation,
        ISOChannel channel,
        String endpoint
) {
}
```

تنها منبع حقیقت اتصال:

```text
activeSession == null     → disconnected
activeSession != null     → published session
```

فیلدهای جداگانه‌ای مانند `channel` و `connected` نباید دو منبع state مستقل ایجاد کنند.

هر session منتشرشده generation جدید می‌گیرد:

```text
lease endpoint A
    generation 11
    generation 12
    generation 13

release lease A
acquire endpoint B
    generation 14
```

Receiver یا sender قدیمی فقط زمانی مجاز به invalidate است که generation آن دقیقاً با `activeSession.generation` برابر باشد.

---

## ۸. lifecycle اتصال، reconnect و lease

State منطقی:

```text
NO_LEASE
    ↓ acquire
LEASED_DISCONNECTED
    ↓ connect
CONNECTED(generation=N)
    ↓ socket/connect/send/receive failure
LEASED_DISCONNECTED
    ↓ reconnect روی همان endpoint

اگر failure count به limit برسد:
    release lease
    ↓
NO_LEASE
    ↓ acquire مجدد
```

### شمارندهٔ endpoint failure

این failureها شمارنده را افزایش می‌دهند:

- TCP connect failure؛
- send failure؛
- receive failure؛
- suspect شدن connection پس از response timeoutهای متوالی.

موفقیت `channel.connect()` شمارنده را صفر نمی‌کند. تنها دریافت و match شدن پاسخ معتبر روی generation فعال، موارد زیر را reset می‌کند:

```text
sameEndpointFailureCount = 0
consecutiveResponseTimeouts = 0
lastConnectionFailure = null
```

با مقدار زیر:

```yaml
same-endpoint-reconnect-attempts: 3
```

سه failure متوالی روی endpoint فعلی باعث release lease و acquire مجدد می‌شود.

---

## ۹. lifecycle درخواست و Deadline

هر request یک deadline مبتنی بر `System.nanoTime()` دارد. ساعت wall-clock برای کنترل timeout مناسب نیست، زیرا ممکن است تغییر کند.

Deadline تمام مراحل قابل‌کنترل را پوشش می‌دهد:

```text
queue admission
connection/reconnection
reconnect delay
validation immediately before send
response wait
```

پس از هر عملیات blocking باید این موارد دوباره بررسی شوند:

- deadline منقضی نشده باشد؛
- tracker هنوز ثبت شده باشد؛
- future قبلاً complete نشده باشد؛
- session هنوز generation فعال باشد.

`response-timeout-ms` deadline caller است. اگر send شروع شده باشد و سپس timeout یا قطع connection رخ دهد، وضعیت تحویل ممکن است نامشخص باشد؛ caller باید failure مناسب دریافت کند و request نباید resend شود.

---

## ۱۰. قانون قطعی No-Resend

دو فاز delivery وجود دارد:

```text
QUEUED
SENDING
SENT
```

تا پیش از ورود به `ISOChannel.send()`، request هنوز ارسال نشده است و می‌تواند برای session جدید منتظر بماند.

از لحظهٔ ورود به `ISOChannel.send()`:

- هر exception به معنی delivery نامشخص است؛
- tracker fail می‌شود؛
- generation مربوط invalidate می‌شود؛
- request از registry حذف می‌شود؛
- requeue و resend مطلقاً ممنوع است.

این رفتار یک invariant ایمنی مالی است، نه policy قابل تنظیم. بنابراین property زیر نباید وجود داشته باشد:

```text
request-send-retry-attempts
```

Retry در لایهٔ بالاتر نیز فقط با تحلیل idempotency و وضعیت مبهم delivery مجاز است؛ این provider نباید خودکار تصمیم به تکرار تراکنش بگیرد.

---

## ۱۱. Response Tracker و Correlation

هر request باید حداقل STAN یا RRN داشته باشد. مدل correlation:

```text
STAN + RRN  → exact composite key
STAN only   → STAN fallback index
RRN only    → RRN fallback index
```

### Policy registration

- duplicate دقیق composite قبل از enqueue رد می‌شود؛
- STAN یکسان با RRN متفاوت مجاز است؛
- RRN یکسان با STAN متفاوت مجاز است؛
- indexها باید در یک critical section کوتاه و اتمیک به‌روزرسانی شوند.

### Policy matching

اگر response هم STAN و هم RRN دارد:

```text
فقط exact composite match
```

در این حالت fallback به STAN یا RRN مجاز نیست؛ mismatch باید unmatched بماند.

اگر response فقط یکی از آن‌ها را دارد، fallback فقط زمانی مجاز است که دقیقاً یک candidate pending وجود داشته باشد.

```text
0 candidate     → unmatched
1 candidate     → matched
>1 candidates   → ambiguous, unmatched
```

انتخاب تصادفی یا اولین candidate ممنوع است.

### Tracker state

`ResponseTracker` class است، نه record، چون state دارد. Terminal state از `CompletableFuture` مشخص می‌شود و delivery phase فقط این سه مقدار را دارد:

```text
QUEUED
SENDING
SENT
```

هر tracker generationی را که send روی آن شروع شده است نگه می‌دارد.

هنگام invalidate شدن generation:

- trackerهای `SENDING` یا `SENT` همان generation fail می‌شوند؛
- trackerهای `QUEUED` همچنان می‌توانند بعد از reconnect پردازش شوند؛
- هیچ tracker ارسال‌شده‌ای requeue نمی‌شود.

---

## ۱۲. Connection Suspect و Response Timeout

`SocketTimeoutException` در receiver یک connection failure محسوب می‌شود، generation فعال را invalidate می‌کند و reconnect عادی را آغاز می‌کند. با مقدار پیش‌فرض `socket-timeout-ms: 0` این exception در idle عادی رخ نمی‌دهد.

اما چند request ارسال‌شدهٔ متوالی که روی generation فعال timeout شوند، connection را suspect می‌کنند.

Policy فعلی:

```text
3 consecutive response timeouts
    → invalidate active generation
    → increment same-endpoint failure count
    → reconnect با policy عادی
```

فقط timeout trackerهایی در این شمارنده اثر دارد که واقعاً روی generation فعلی ارسال شده‌اند. timeout requestهای queue‌شده یا generation قدیمی نباید session جدید را suspect کنند.

اولین پاسخ معتبر شمارندهٔ timeout را صفر می‌کند.

---

## ۱۳. Request و Response Model

ورودی یک `Map` یا JSON object است:

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
    "expiryDate": "2907",
    "cvv2": "639"
  }
}
```

خروجی یک `Map` شامل `mti` و `fields` است.

`security` ورودی transient pipeline است و customizerهای امنیتی آن را به fieldهای ISO تبدیل می‌کنند. این object نباید به response یا context عمومی business نشت کند.

---

## ۱۴. `ProviderMessageCustomizer`

`ProviderMessageCustomizerFactory` extension point Spring است. YAML از `type` پایدار استفاده می‌کند، نه bean name.

Resolver pipeline را یک‌بار برای provider می‌سازد و producer فقط همان pipeline immutable را اجرا می‌کند.

ترتیب convention:

| بازه | کاربرد |
| --- | --- |
| `100..999` | outlet، terminal، merchant، expiry، CVV2 |
| `5000` | authentication احتمالی آینده |
| `8000` | PIN block |
| `10000` | MAC |
| `20000` | response enrichment |

Customizerهای فعلی:

```text
hps-shetab-outlet
hps-shetab-terminal
shetab-expiry
shetab-cvv2
shetab-pin-block
shetab-mac
```

قواعد:

- MAC باید بعد از نهایی‌شدن تمام fieldهای request اجرا شود.
- PIN block باید قبل از MAC اجرا شود.
- expiry، CVV2، outlet، terminal و merchant enrichment باید قبل از MAC باشند.
- `shetab-mac` فقط برای provider ISO/Shetab معتبر است و نباید provider REST را support کند.
- customizer باید stateless یا immutable باشد، مگر state آن صریحاً thread-safe طراحی شده باشد.
- خطای customizer قبل از شروع send به معنی request ارسال‌نشده است.

---

## ۱۵. Endpoint Lease در deployment توزیع‌شده

برای چند endpoint و چند Pod، lease را فعال کنید:

```yaml
scm:
  providers:
    hps-shetab7:
      scheme: scm-shetab
      endpoints:
        - 10.10.10.11:9000
        - 10.10.10.12:9000
      endpoint-lease:
        enabled: true
        ttl-ms: 30000
```

رفتار:

- pool lease شامل `providerCode` است؛
- هر endpoint یک candidate مستقل است؛
- کلید توزیع‌شده از الگوی زیر استفاده می‌کند:

```text
shetab-endpoint-lease::<providerCode>::<endpoint>
```

- اگر lease فعال و endpointها متعدد باشند، `ResourceLeaseUtility` توزیع‌شده الزامی است؛
- نبود lease utility در این حالت باید startup/runtime را fail کند و نباید silently endpoint اول را انتخاب کند؛
- اگر lease غیرفعال باشد، endpoint اول استفاده می‌شود؛
- release lease فقط بعد از رسیدن failure count به limit، shutdown یا تصمیم lifecycle صریح انجام می‌شود.

---

## ۱۶. Rate Limit

Rate limit توزیع‌شده از `RateLimiterUtility` در `scm-cache-starter` استفاده می‌کند.

```yaml
rate-limit:
  enabled: true
  bucket: hps-shetab7
  key: provider-operation
```

در نبود `RateLimiterUtility`، رفتار فعلی fallback به noop همراه WARN است. این fallback باید در محیط production آگاهانه بررسی شود؛ نبود rate limit نباید بدون visibility باقی بماند.

---

## ۱۷. Observability و Logging

هر تلاش واقعی transport برای request دقیقاً دو event مرتب در scope صریح
`scm.observation.scope.operation` ایجاد می‌کند: `provider.request` هنگام شروع تلاش و
`provider.response` دقیقاً یک‌بار هنگام پایان آن. فعالیت provider و customizer child span
ایجاد نمی‌کند و ردشدن rate limit یا request پیش از شروع transport هیچ‌یک از این دو event را
نمی‌سازد. transport production فقط lifecycle اجباری trace-aware را استفاده می‌کند تا هیچ
`provider.request` بدون `provider.response` متناظر باقی نماند.

مطابق قرارداد no-resend، هر request حداکثر یک تلاش transport دارد و مقدار
`provider.attempt` برای آن `1` است. reconnectهای قبل از ورود به `ISOChannel.send()` بخشی از
همان تلاش هستند؛ پس از شروع send، failure تلاش را می‌بندد و request هرگز دوباره enqueue یا
ارسال نمی‌شود. تلاش موفق تا دریافت response، اجرای customizerهای after-receive و تبدیل ISO
باز می‌ماند تا timeout، connection، validation و customizer failure همگی یک
`provider.response` ناموفق تولید کنند. `provider.duration_ms` فقط روی `provider.response`
ثبت می‌شود و با `System.nanoTime()` و مقدار nonnegative محاسبه می‌شود.

schema امن event شامل `provider.name`، `provider.code`، `provider.type`،
`provider.scheme`، `provider.operation`، `provider.endpoint`، `provider.attempt`،
`provider.duration_ms`، `provider.response_code`، `provider.error_code`، `event.outcome`،
`error.type` و `error.code` است. `ShetabProviderTraceAttributeContributor` extension point
افزودن attributeهای request/response است و هر field جدید باید صریحاً در قرارداد
`ObservationAttributeContributor` ثبت شود. `provider.endpoint` فقط وقتی ثبت می‌شود که endpoint
واقعی session متصل هنگام شروع تلاش مشخص باشد؛ reconnect و connection failure به‌جای fallback
به اولین endpoint پیکربندی‌شده، آن را حذف می‌کنند.

Trace و eventهای provider باید فقط metadata کنترل‌شده مانند موارد زیر را حمل کنند:

```text
providerCode
scheme
providerUri
serviceCode
operationCode
channelCode
customizerType
phase
outcome
error type/code
```

Metricهای موجود شامل موارد زیر هستند:

```text
provider.request.duration
provider.request.error
provider.customizer.execution
provider.customizer.error
```

Logهای connection برای troubleshooting باید حداقل این context را داشته باشند:

```text
provider
endpoint
lease state
generation
same-endpoint failure count
request correlation summary
last sent/received time
failure type
```

### Packed ISO DEBUG log

این نسخه packed ISO را به‌صورت hex در سطح DEBUG ثبت می‌کند. این خروجی می‌تواند شامل PAN، PIN block، CVV2، expiry، MAC و سایر fieldهای حساس باشد.

بنابراین:

- DEBUG این package در production نباید به‌صورت عمومی فعال باشد؛
- دسترسی، retention و انتقال فایل‌های DEBUG باید محدود باشد؛
- log نباید به محیط غیرقابل اعتماد یا سامانهٔ عمومی log ارسال شود؛
- تغییر یا حذف این رفتار نیازمند تصمیم صریح عملیاتی است.

Log ساختاری عادی باید همچنان از `SafeIsoLogFormatter` استفاده کند.

---

## ۱۸. Failure semantics

Failureهای مهم باید از هم قابل تشخیص باشند:

| وضعیت | معنا | resend داخل provider |
| --- | --- | --- |
| queue full | request وارد صف نشده است | خیر؛ caller تصمیم می‌گیرد |
| deadline before send | request ارسال نشده است | خیر؛ caller تصمیم می‌گیرد |
| connect failure | request هنوز ارسال نشده است | reconnect تا deadline مجاز است |
| failure داخل/بعد از `send()` | delivery نامشخص است | مطلقاً ممنوع |
| connection lost after send | delivery یا response نامشخص است | مطلقاً ممنوع |
| response timeout | ممکن است provider request را پردازش کرده باشد | مطلقاً ممنوع |
| ambiguous correlation | پاسخ به tracker مشخصی قابل انتساب نیست | خیر |
| duplicate exact correlation | request قبل از enqueue رد می‌شود | خیر |

Exceptionهای domain-specific باید تفاوت این حالت‌ها را برای caller و monitoring حفظ کنند؛ مخصوصاً delivery مبهم نباید با یک connection failure ساده یکسان نمایش داده شود.

---

## ۱۹. Shutdown

در shutdown:

1. پذیرش request جدید متوقف می‌شود؛
2. sender و receiver interrupt می‌شوند؛
3. session فعال invalidate و channel بسته می‌شود؛
4. lease آزاد می‌شود؛
5. trackerهای pending fail می‌شوند؛
6. send queue drain می‌شود؛
7. threadها با timeout محدود join می‌شوند.

Shutdown نباید منتظر reconnect loop نامحدود یا lockی باشد که حین connect/sleep نگه داشته شده است.

---

## ۲۰. مسیر توسعه و تغییر امن

### افزودن packager جدید

1. packager class یا XML را اضافه کنید؛
2. framing چهاررقمی ASCII را بررسی کنید؛
3. charset و field definitions را با host هماهنگ کنید؛
4. رفتار STAN/RRN پاسخ را بررسی کنید؛
5. config provider و این سند را به‌روزرسانی کنید.

### افزودن customizer جدید

1. `type` پایدار انتخاب کنید؛
2. factory با config typed ایجاد کنید؛
3. order را بر اساس وابستگی fieldها تعیین کنید؛
4. output را immutable/thread-safe نگه دارید؛
5. secret را در `toString`, exception یا log وارد نکنید؛
6. README را به‌روزرسانی کنید.

### تغییر correlation

هر تغییر باید این موارد را حفظ کند:

- exact composite source of truth؛
- fallback فقط در حالت یک candidate؛
- registration و removal اتمیک؛
- عدم match arbitrary؛
- cleanup تمام indexها در completion/failure.

### تغییر reconnect

هر تغییر باید ثابت کند:

- بیش از یک session publish نمی‌شود؛
- generation قدیمی session جدید را نمی‌بندد؛
- lease قبل از limit بی‌دلیل آزاد نمی‌شود؛
- connect موفق failure count را reset نمی‌کند؛
- valid response شمارنده‌ها را reset می‌کند؛
- request ارسال‌شده resend نمی‌شود؛
- deadline request در reconnect loop رعایت می‌شود.

---

## ۲۱. راهنمای عیب‌یابی

### Queue full

بررسی کنید:

```text
queue-capacity
send-timeout-ms
provider latency
connection availability
request burst rate
```

افزایش queue بدون تحلیل latency می‌تواند فقط timeout را به تعویق بیندازد و مصرف حافظه را افزایش دهد.

### Connect loop روی یک endpoint

بررسی کنید:

```text
same-endpoint-reconnect-attempts
lease release log
acquire endpoint log
connect timeout
network route/firewall
endpoint accepts TCP but closes immediately
```

اگر TCP connect موفق ولی response معتبر دریافت نمی‌شود، failure count نباید صرفاً با connect صفر شود.

### Unmatched response

بررسی کنید:

```text
field 11 / STAN
field 37 / RRN
response packager
composite mismatch
ambiguous fallback candidates
request timeout and tracker cleanup
```

### Request timeout با احتمال انجام تراکنش

اگر request وارد `send()` شده باشد، timeout می‌تواند delivery مبهم باشد. request را بدون reconciliation یا idempotency business تکرار نکنید.

### Client config mismatch

اگر همان provider code با endpoint، packager یا timeout متفاوت resolve شود، Registry نباید config جدید را silently نادیده بگیرد. علت duplicate provider definition یا reload ناقص را بررسی کنید.

---

## ۲۲. چک‌لیست Code Review

- آیا برای هر provider در Registry فقط یک client ساخته می‌شود؟
- آیا provider key نرمال شده است؟
- آیا فقط sender connection ایجاد می‌کند؟
- آیا `activeSession` تنها منبع truth اتصال است؟
- آیا generation در تمام send/receive/invalidate pathها بررسی می‌شود؟
- آیا connect، disconnect و sleep خارج lock هستند؟
- آیا deadline قبل و بعد از عملیات blocking بررسی می‌شود؟
- آیا هیچ مسیر resend بعد از شروع `send()` وجود ندارد؟
- آیا `sameEndpointFailureCount` فقط با response معتبر reset می‌شود؟
- آیا response timeoutهای متوالی فقط برای generation فعال شمرده می‌شوند؟
- آیا exact correlation قبل از fallback بررسی می‌شود؟
- آیا fallback مبهم unmatched می‌ماند؟
- آیا trackerهای generation خراب fail و از تمام indexها حذف می‌شوند؟
- آیا shutdown بدون deadlock و reconnect نامحدود تمام می‌شود؟
- آیا customizer order و thread-safety حفظ شده است؟
- آیا DEBUG packed ISO فقط در محیط کنترل‌شده فعال است؟

این invariantها قرارداد نگهداری ماژول هستند. تغییر performance، pooling، multiplexing یا retry بدون بازنگری این قراردادها می‌تواند باعث duplicate مالی، پاسخ اشتباه یا اشغال دائمی endpoint lease شود.

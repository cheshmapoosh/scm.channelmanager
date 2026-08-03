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
ساخت routeهای فعال
    → ProviderRuntimeLifecycle.registerEffectiveUsage(...)
    → یک runtime برای هر provider مؤثر
    → snapshot اولیه DISCONNECTED / readiness DOWN

ApplicationReadyEvent
    → connection worker اختصاصی
    → lease acquire/validate
    → connect و publish اتمیک ChannelSession
    → continuous recovery در پس‌زمینه

درخواست Camel
    → فقط session معتبر و از قبل publish‌شده
    → fail-fast در نبود session
    → send queue / sender
    → receiver / correlation registry
```

مدل نهایی همواره این است و mode دیگری ندارد:

```text
Asynchronous eager connection
+ Fail-fast request admission
+ Continuous background recovery
```

شروع application منتظر شبکهٔ شتاب نمی‌ماند. runtime و snapshot پیش از
`ApplicationReadyEvent` ثبت می‌شوند، اما اتصال TCP فقط پس از آن و روی worker اختصاصی انجام
می‌شود. در Kubernetes هر Pod یک runtime مستقل دارد و endpoint lease از مالکیت هم‌زمان یک
endpoint توسط چند Pod جلوگیری می‌کند.

---

## ۳. invariantهای حیاتی

تغییرات آینده نباید این قواعد را نقض کنند:

1. فقط provider فعال که حداقل یک Service مؤثر و فعال به آن ارجاع می‌دهد runtime می‌سازد.
2. `ShetabTcpClientRegistry` برای هر provider مؤثر فقط یک client ایجاد می‌کند.
3. در هر client فقط یک connection worker و حداکثر یک `activeSession` وجود دارد.
4. فقط connection worker مجاز به acquire/release lease، `connect()`، `disconnect()` و publish session است.
5. sender، receiver، timeout و request thread فقط generation خود را invalidate و recovery را signal می‌کنند.
6. recovery با state سطحی `recoveryRequired` نگهداری می‌شود و به queue signal یا درخواست بعدی وابسته نیست.
7. request بدون session معتبر فوراً با `SHETAB_CONNECTION_UNAVAILABLE` رد می‌شود و enqueue نمی‌شود.
8. tracker `QUEUED` متعلق به generation خراب fail می‌شود و برای connection بعدی نگه داشته نمی‌شود.
9. پس از ورود به `ISOChannel.send()`، request هرگز requeue یا resend نمی‌شود.
10. `activeSession` منبع حقیقت connection است؛ snapshot فقط view immutable برای support/readiness است.
11. generation قدیمی هرگز session جدید را invalidate، close یا unhealthy نمی‌کند.
12. پاسخ معتبر روی generation جاری `verified=true` می‌کند؛ `verified=false` به‌تنهایی connection منتشرشده را unhealthy نمی‌کند.
13. correlation دقیق `STAN + RRN` مرجع اصلی است و fallback مبهم مجاز نیست.
14. health/readiness فقط snapshot را می‌خواند و هیچ side effect شبکه‌ای یا lease ندارد.

---

## ۴. مدل اصلی پیکربندی

### Provider مؤثر

یک Shetab provider فقط وقتی مؤثر است که هر دو شرط برقرار باشند:

```text
provider.enabled = true
و
Active Service → active ServiceOperation → active Operation
               → active OperationProvider با scheme برابر scm-shetab
```

| enabled | ارجاع Service فعال | رفتار |
| --- | --- | --- |
| false | ندارد | startup موفق؛ بدون runtime و بدون readiness contributor |
| false | دارد | startup fail-fast با نام service، operation و provider |
| true | ندارد | startup موفق؛ log با event=`SHETAB_PROVIDER_ENABLED_BUT_UNUSED`؛ بدون runtime |
| true | دارد | runtime ساخته می‌شود و پس از آماده‌شدن application به‌صورت async متصل می‌شود |

اعتبارسنجی provider غیرفعال در ساخت operation route و پیش از resolve شدن endpoint Camel
انجام می‌شود. خطاهای binding تنظیمات همچنان startup را fail می‌کنند.

Providerها در registry مشترک پیکربندی می‌شوند:

```yaml
scm:
  providers:
    hps-shetab7:
      scheme: scm-shetab
      enabled: false

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
| `connect-timeout-ms` | سقف هر تلاش TCP connect روی connection worker مستقل از request |
| `socket-timeout-ms` | timeout عملیات receive؛ مقدار پیش‌فرض `0` یعنی receiver تا زمان دریافت یا failure به‌صورت blocking باقی می‌ماند |
| `keep-alive` | فعال‌سازی TCP keep-alive در channel؛ پیش‌فرض `true` است |
| `response-timeout-ms` | deadline caller برای کل lifecycle درخواست |
| `send-timeout-ms` | حداکثر انتظار برای ورود به send queue پس از پذیرش روی یک session معتبر |
| `reconnect-delay-ms` | فاصلهٔ reconnect؛ sleep نباید زیر session lock انجام شود |
| `same-endpoint-reconnect-attempts` | تعداد failureهای متوالی قابل تحمل روی lease فعلی قبل از release و acquire مجدد |

### قواعد configuration

- block عمومی `defaults` برای provider وجود ندارد؛ هر instance باید تنظیمات اصلی خود را صریح داشته باشد.
- مقدار پیش‌فرض `enabled` برابر `false` است.
- مقدار پیش‌فرض `socket-timeout-ms` برابر `0` است.
- `scheme` برای این provider باید `scm-shetab` باشد.
- `endpoints` مدل اصلی است؛ `endpoint` فقط alias تک-endpoint است.
- `message-customizers` اختیاری است و customizer پیش‌فرضی فعال نمی‌شود.
- PIN block، MAC، expiry و CVV2 فقط از طریق customizerها اعمال می‌شوند.
- secretها باید از environment یا secret store وارد شوند؛ مقدار secret نباید داخل repository قرار گیرد.
- تغییر runtime config برای providerای که client آن قبلاً ساخته شده است نباید silently نادیده گرفته شود؛ Registry باید config ناسازگار را fail-fast کند یا lifecycle صریح reload داشته باشد.
- هیچ `startup-mode`، `EAGER` یا `LAZY` وجود ندارد؛ startup اتصال برای provider مؤثر همیشه asynchronous/eager است.

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
- فقط usageهایی را می‌پذیرد که core از Serviceهای مؤثر گزارش کرده است؛
- برای هر provider مؤثر client و snapshot اولیه را پیش از application-ready می‌سازد؛
- در `ApplicationReadyEvent` workerها را بدون انتظار برای اتصال فعال می‌کند؛
- provider فعال ولی بدون usage را با `SHETAB_PROVIDER_ENABLED_BUT_UNUSED` گزارش می‌کند؛
- snapshotهای immutable providerهای مؤثر را از طریق `ProviderReadinessContributor` ارائه می‌کند؛
- lifecycle clientها را در shutdown متوقف می‌کند؛
- دریافت config متفاوت برای همان provider را باید به‌صورت صریح مدیریت کند، نه اینکه config جدید را بی‌صدا نادیده بگیرد.

### `ShetabIsoChannelClient`

Orchestrator اصلی است و فقط این مسئولیت‌ها را نگه می‌دارد:

- API هم‌زمان `request()`؛
- admission فوری فقط روی session از قبل منتشرشده؛
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
- connection worker اختصاصی و level-triggered recovery؛
- connect/reconnect و publish اتمیک session؛
- invalidate فقط برای generation مطابق؛
- `sameEndpointFailureCount`؛
- snapshot immutable و transition متمرکز؛
- signal کردن receiver و worker هنگام publish یا invalidate session.

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

Client سه thread daemon دارد:

```text
connection worker
    تنها مالک lease، connect، disconnect و replacement session

sender thread
    فقط dequeue و send روی session پذیرفته‌شده

receiver thread
    مسئول wait session، receive و match response
```

### چرا worker مستقل لازم است؟

اتصال نباید به ورود request یا ظرفیت send queue وابسته باشد. worker مستقل باعث می‌شود startup
منتظر شبکه نماند، request در حالت قطع فوراً fail شود و recovery تا موفقیت یا shutdown ادامه پیدا
کند. signalهای تکراری فقط `recoveryRequired=true` را تثبیت و worker را بیدار می‌کنند؛ signal
گم‌شده در queue نمی‌تواند recovery را متوقف کند.

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

Sender/receiver ممکن است generation خود را اتمیک detach کنند، metadata failure را ثبت کنند و
worker را بیدار کنند؛ بستن channel یا reconnect مستقیم از این threadها ممنوع است.

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

برای هر provider مؤثر یک `ShetabConnectionSnapshot` immutable وجود دارد. stateهای آن عبارت‌اند از:

```text
DISCONNECTED
CONNECTING
CONNECTED
RECONNECTING
FAILED
STOPPED
```

Snapshot شامل provider، endpoint، generation، وضعیت lease، worker، recovery، attemptها،
timestampها و failure metadata پاک‌سازی‌شده است. `Throwable` یا stack trace در آن نگهداری
نمی‌شود. timestamp نمایشی از wall clock و deadline/retry از `System.nanoTime()` استفاده می‌کند.
تمام تغییرات از transition مرکزی عبور می‌کنند تا log و readiness از state جدا نشوند.

هر generation با `verified=false` منتشر می‌شود و فقط match شدن یک response معتبر همان
generation آن را `true` می‌کند. از آنجا که health probe پیام آزمایشی نمی‌فرستد،
`verified=false` به‌تنهایی readiness یک session برقرار را `DOWN` نمی‌کند.

---

## ۸. lifecycle اتصال، reconnect و lease

برای provider مؤثر، snapshot اولیه قبل از ready شدن application به شکل زیر است:

```text
DISCONNECTED
recoveryRequired = true
connectionWorkerAlive = false
readiness = DOWN
```

پس از `ApplicationReadyEvent`، worker شروع می‌شود و این چرخه را تا موفقیت یا stop ادامه می‌دهد:

```text
acquire/confirm lease
    → CONNECTING یا RECONNECTING
    → TCP connect
    → publish اتمیک activeSession(generation=N)
    → CONNECTED

failure/loss/timeout
    → detach فقط generation جاری
    → readiness DOWN
    → recoveryRequired=true
    → worker channel قدیمی را می‌بندد
    → در صورت نیاز lease را release/reacquire می‌کند
    → retry با reconnect-delay-ms
```

هیچ request جدیدی برای ادامهٔ recovery لازم نیست و در هر provider حداکثر یک connection
attempt هم‌زمان وجود دارد. termination غیرمنتظرهٔ worker در snapshot و readiness به‌صورت
`FAILED`/`connectionWorkerAlive=false` و با event=`SHETAB_CONNECTION_WORKER_TERMINATED`
نمایان می‌شود.

### شمارندهٔ endpoint failure

این failureها شمارنده را افزایش می‌دهند:

- TCP connect failure؛
- send failure؛
- receive failure؛
- response timeout پس از شروع send.

موفقیت `channel.connect()` شمارنده را صفر نمی‌کند. تنها دریافت و match شدن پاسخ معتبر روی generation فعال، موارد زیر را reset می‌کند:

```text
sameEndpointFailureCount = 0
verified = true
```

با مقدار زیر:

```yaml
same-endpoint-reconnect-attempts: 3
```

سه failure متوالی روی endpoint فعلی باعث release lease و acquire مجدد می‌شود.
`RECEIVE_IDLE_TIMEOUT` و `LEASE_LOST` بدون انتظار برای این limit، lease فعلی را برای
reacquire علامت‌گذاری می‌کنند.

---

## ۹. lifecycle درخواست و Deadline

هر request یک deadline مبتنی بر `System.nanoTime()` دارد. ساعت wall-clock برای کنترل timeout مناسب نیست، زیرا ممکن است تغییر کند.

Deadline تمام مراحل قابل‌کنترل را پوشش می‌دهد:

```text
queue admission
validation immediately before send
response wait
```

Connection/reconnection متعلق به worker مستقل است و داخل deadline درخواست اجرا نمی‌شود.
اگر هنگام admission session معتبر وجود نداشته باشد، request بدون wait، بدون enqueue و بدون
فراخوانی `connect()` با reason زیر fail می‌شود:

```text
SHETAB_CONNECTION_UNAVAILABLE
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

تا پیش از ورود به `ISOChannel.send()`، delivery رخ نداده است. با این حال request پذیرفته‌شده
به generation مشخص تعلق دارد؛ اگر آن generation پیش از send invalidate شود، tracker
`QUEUED` با `SHETAB_CONNECTION_UNAVAILABLE` fail می‌شود و برای session جدید نگه داشته یا
requeue نمی‌شود.

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

هر tracker از لحظهٔ admission به یک generation مشخص متصل است.

هنگام invalidate شدن generation:

- trackerهای `SENDING` یا `SENT` همان generation fail می‌شوند؛
- trackerهای `QUEUED` همان generation به‌عنوان not-delivered fail می‌شوند؛
- هیچ tracker ارسال‌شده‌ای requeue نمی‌شود.

---

## ۱۲. Request timeout و Receive idle timeout

این دو timeout مستقل‌اند:

```text
response-timeout-ms
    → deadline یک request مشخص

socket-timeout-ms
    → حداکثر سکوت receive روی کل TCP connection
```

رفتار `response-timeout-ms`:

- timeout در `QUEUED` connection را invalidate نمی‌کند؛
- timeout در `SENDING` یا `SENT` برای generation جاری delivery مبهم دارد، readiness را
  `DOWN` می‌کند و recovery را signal می‌کند؛
- timeout مربوط به generation قدیمی روی session جدید اثر ندارد؛
- timeout هرگز resend خودکار ایجاد نمی‌کند؛
- reason فنی caller برابر `SHETAB_RESPONSE_TIMEOUT` باقی می‌ماند.

رفتار `socket-timeout-ms`:

- مقدار `0` receive-idle recovery را غیرفعال می‌کند و receiver می‌تواند نامحدود منتظر بماند؛
- مقدار مثبت و وقوع `SocketTimeoutException` با `reasonCode=RECEIVE_IDLE_TIMEOUT` و
  `phase=RECEIVE` ثبت می‌شود؛
- receiver فقط generation خودش را detach و worker را بیدار می‌کند؛
- worker channel را می‌بندد، lease فعال را release/reacquire و بعد از delay reconnect می‌کند؛
- readiness تا publish شدن generation جدید `DOWN` می‌ماند؛
- timeout دیررس generation قدیمی هیچ تغییری در generation جدید ایجاد نمی‌کند.

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

- اگر lease فعال باشد، `ResourceLeaseUtility` با وضعیت مالکیت قابل مشاهده الزامی است؛
- نبود lease utility نباید silently endpoint اول را به‌عنوان lease معتبر انتخاب کند؛
- اگر lease غیرفعال باشد، endpoint اول استفاده می‌شود؛
- object غیر-null دلیل مالکیت نیست؛ `isValid()` آخرین وضعیت معتبر/نامعتبر را گزارش می‌کند؛
- refresh failure، ownership loss یا وضعیت unknown فوراً callback invalidation را فعال می‌کند؛
- callback فقط generation را unavailable و worker را بیدار می‌کند؛ release/reacquire فقط روی worker است؛
- release lease بعد از رسیدن failure count به limit، receive-idle timeout، lease loss، shutdown یا تصمیم lifecycle صریح انجام می‌شود؛
- leasing غیرفعال در snapshot با `NOT_REQUIRED` و ownership معتبر با `VALID` نمایش داده می‌شود.

---

## ۱۶. Readiness و Liveness

`scm-web` یک aggregate ثابت با نام `providerReadiness` دارد. فقط providerهای مؤثر در آن
حضور دارند؛ disabled و enabled-but-unused حذف می‌شوند. health فقط immutable snapshot را
می‌خواند و هرگز client، worker، lease، socket یا probe ISO ایجاد نمی‌کند.

| state | readiness |
| --- | --- |
| `DISCONNECTED` | `DOWN` |
| `CONNECTING` | `DOWN` |
| `CONNECTED` + session منتشرشده + worker زنده + بدون recovery + lease `VALID`/`NOT_REQUIRED` | `UP` |
| `RECONNECTING` | `DOWN` |
| `FAILED` | `DOWN` |
| `STOPPED` | `DOWN` |

اگر provider مؤثری وجود نداشته باشد aggregate برابر `UP` است. جزئیات پاک‌سازی‌شده فقط
طبق policy `when_authorized` نمایش داده می‌شوند. endpointهای Kubernetes عبارت‌اند از:

```text
/actuator/health/liveness
/actuator/health/readiness
```

`liveness` فقط process-level است و به Shetab، worker، lease، cache یا شبکه وابسته نیست.
دسترسی anonymous فقط برای همین دو مسیر probe مجاز است؛ سایر endpointهای Actuator احراز هویت
می‌خواهند.

Defaultهای source configuration:

```text
SCM_HPS_SHETAB7_ENABLED=false
SCM_HPS_SHETAB7_SOCKET_TIMEOUT_MS=0
SCM_MANAGEMENT_READINESS_SHOW_DETAILS=when_authorized
```

---

## ۱۷. Rate Limit

Rate limit توزیع‌شده از `RateLimiterUtility` در `scm-cache-starter` استفاده می‌کند.

```yaml
rate-limit:
  enabled: true
  bucket: hps-shetab7
  key: provider-operation
```

در نبود `RateLimiterUtility`، رفتار فعلی fallback به noop همراه WARN است. این fallback باید در محیط production آگاهانه بررسی شود؛ نبود rate limit نباید بدون visibility باقی بماند.

---

## ۱۸. Observability و Logging

هر تلاش واقعی transport برای request دقیقاً دو event مرتب در scope صریح
`scm.observation.scope.operation` ایجاد می‌کند: `provider.request` هنگام شروع تلاش و
`provider.response` دقیقاً یک‌بار هنگام پایان آن. فعالیت provider و customizer child span
ایجاد نمی‌کند و ردشدن rate limit یا request پیش از شروع transport هیچ‌یک از این دو event را
نمی‌سازد. transport production فقط lifecycle اجباری trace-aware را استفاده می‌کند تا هیچ
`provider.request` بدون `provider.response` متناظر باقی نماند.

مطابق قرارداد no-resend، هر request حداکثر یک تلاش transport دارد و مقدار
`provider.attempt` برای آن `1` است. request در حالت disconnected اصلاً وارد transport attempt
نمی‌شود. پس از شروع send، failure تلاش را می‌بندد و request هرگز دوباره enqueue یا ارسال
نمی‌شود. تلاش موفق تا دریافت response، اجرای customizerهای after-receive و تبدیل ISO
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

Eventهای lifecycle پایدار شامل این مواردند:

```text
SHETAB_CONNECTION_WORKER_STARTED
SHETAB_CONNECTION_STATE_CHANGED
SHETAB_CONNECTION_ATTEMPT_STARTED
SHETAB_CONNECTION_ATTEMPT_FAILED
SHETAB_CONNECTION_RECOVERY_SCHEDULED
SHETAB_CONNECTION_ESTABLISHED
SHETAB_CONNECTION_INVALIDATED
SHETAB_RECEIVE_IDLE_TIMEOUT
SHETAB_ENDPOINT_LEASE_LOST
SHETAB_CONNECTION_WORKER_STOPPED
SHETAB_CONNECTION_WORKER_TERMINATED
```

Failure messageها sanitize و truncate می‌شوند. failure تکراری اول/تغییرکرده و خلاصه‌های دوره‌ای
در `WARN` و موارد میانی یکسان در `DEBUG` ثبت می‌شوند. هیچ event اتصال شامل payload ISO،
PAN، PIN، CVV2، MAC، token، credential، stack trace یا correlation ساختگی نیست. packed ISO
به‌صورت hex log نمی‌شود و formatter موجود فقط metadata/نمای sanitize‌شده را به کار می‌برد.

---

## ۱۹. Failure semantics

Failureهای مهم باید از هم قابل تشخیص باشند:

| وضعیت | معنا | resend داخل provider |
| --- | --- | --- |
| no valid session | request enqueue نشده؛ `SHETAB_CONNECTION_UNAVAILABLE` | خیر |
| queue full | request وارد صف نشده است | خیر؛ caller تصمیم می‌گیرد |
| deadline before send | request ارسال نشده است | خیر؛ caller تصمیم می‌گیرد |
| background connect failure | request جدید پذیرفته نمی‌شود؛ worker retry می‌کند | نامرتبط |
| failure داخل/بعد از `send()` | `SHETAB_CONNECTION_LOST_AFTER_SEND`؛ delivery نامشخص است | مطلقاً ممنوع |
| connection lost after send | `SHETAB_CONNECTION_LOST_AFTER_SEND`؛ delivery یا response نامشخص است | مطلقاً ممنوع |
| response timeout | `SHETAB_RESPONSE_TIMEOUT`؛ ممکن است provider request را پردازش کرده باشد | مطلقاً ممنوع |
| ambiguous correlation | پاسخ به tracker مشخصی قابل انتساب نیست | خیر |
| duplicate exact correlation | request قبل از enqueue رد می‌شود | خیر |

Exceptionهای domain-specific باید تفاوت این حالت‌ها را برای caller و monitoring حفظ کنند؛ مخصوصاً delivery مبهم نباید با یک connection failure ساده یکسان نمایش داده شود.

---

## ۲۰. Shutdown

در shutdown:

1. پذیرش request جدید متوقف می‌شود؛
2. connection worker متوقف و session جاری detach می‌شود؛
3. خود worker channel را می‌بندد و lease را آزاد می‌کند؛
4. sender و receiver interrupt می‌شوند؛
5. trackerهای pending fail می‌شوند؛
6. send queue drain می‌شود؛
7. threadها با timeout محدود join می‌شوند.

Shutdown نباید منتظر reconnect loop نامحدود یا lockی باشد که حین connect/sleep نگه داشته شده است.

---

## ۲۱. مسیر توسعه و تغییر امن

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
- reconnect فقط توسط worker انجام می‌شود؛
- recovery به signal queue یا request بعدی وابسته نیست؛
- request disconnected بدون wait fail می‌شود.

---

## ۲۲. راهنمای عیب‌یابی

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

### Readiness همیشه `DOWN`

جزئیات authorized مسیر readiness را بررسی کنید:

```text
connectionState
connectionWorkerAlive
recoveryRequired
leaseState
lastFailureCode
lastFailureType
```

`LEASE_ACQUIRE_FAILED` معمولاً به نبود cache/lease backend، endpointهای اشغال یا تنظیمات
lease مربوط است. `TCP_CONNECT_FAILED` نشان‌دهندهٔ failure شبکه/host است. health endpoint هیچ
تلاشی برای ترمیم انجام نمی‌دهد؛ worker باید مستقل در حال retry باشد.

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

## ۲۳. چک‌لیست Code Review

- آیا برای هر provider در Registry فقط یک client ساخته می‌شود؟
- آیا فقط providerهای مؤثر runtime و readiness دارند؟
- آیا provider key نرمال شده است؟
- آیا فقط connection worker lease و connection را مدیریت می‌کند؟
- آیا `activeSession` تنها منبع truth اتصال است؟
- آیا snapshot فقط immutable view است و health side effect ندارد؟
- آیا generation در تمام send/receive/invalidate pathها بررسی می‌شود؟
- آیا connect، disconnect و sleep خارج lock هستند؟
- آیا deadline قبل و بعد از عملیات blocking بررسی می‌شود؟
- آیا request disconnected بدون wait و enqueue fail می‌شود؟
- آیا هیچ مسیر resend بعد از شروع `send()` وجود ندارد؟
- آیا `sameEndpointFailureCount` فقط با response معتبر reset می‌شود؟
- آیا response timeout ارسال‌شده فقط generation فعال را invalidate می‌کند؟
- آیا tracker `QUEUED` generation خراب fail می‌شود و برای generation بعدی حفظ نمی‌شود؟
- آیا receive idle timeout صفر غیرفعال و مقدار مثبت recovery کامل ایجاد می‌کند؟
- آیا lease `UNKNOWN` یا lost readiness را `DOWN` می‌کند؟
- آیا exact correlation قبل از fallback بررسی می‌شود؟
- آیا fallback مبهم unmatched می‌ماند؟
- آیا trackerهای generation خراب fail و از تمام indexها حذف می‌شوند؟
- آیا shutdown بدون deadlock و reconnect نامحدود تمام می‌شود؟
- آیا customizer order و thread-safety حفظ شده است؟
- آیا DEBUG packed ISO فقط در محیط کنترل‌شده فعال است؟

این invariantها قرارداد نگهداری ماژول هستند. تغییر performance، pooling، multiplexing یا retry بدون بازنگری این قراردادها می‌تواند باعث duplicate مالی، پاسخ اشتباه یا اشغال دائمی endpoint lease شود.

# راهنمای Authentication در `scm-uaa`

این فایل برای توسعه‌دهنده‌های تازه‌وارد نوشته شده است. هدف این است که بدانید هر مسیر لاگین از کجا وارد می‌شود، کدام کلاس‌ها مسئول چه کاری هستند، کجا باید کد اضافه کنید، و چطور خروجی‌های JSONL برای LOG و TRACE تولید می‌شوند.

## ۱. لاگین قدیمی NIB - صفحه لاگین در فرانت NIB و دریافت توکن به صورت back-to-back

در این مسیر کاربر `username` و `password` را در فرانت NIB وارد می‌کند. سپس بک‌اند NIB به صورت back-to-back به token endpoint در `scm-uaa` درخواست می‌زند. این مسیر legacy و deprecated است و فقط برای سازگاری نگه داشته شده است. جایگزین هدف، UAA-hosted login و authorization-code flow است.

در این مسیر PWA cookie ساخته نمی‌شود. توکن از همان response قدیمی token endpoint برمی‌گردد.

```mermaid
sequenceDiagram
    participant User as User
    participant NibFront as NIB Frontend
    participant NibBack as NIB Backend
    participant UAA as scm-uaa

    User->>NibFront: وارد کردن username/password
    NibFront->>NibBack: ارسال اطلاعات لاگین
    NibBack->>UAA: token endpoint back-to-back
    UAA->>UAA: LegacyPasswordGrantAuthenticationConverter
    UAA->>UAA: LegacyClientTypeResolver = NIB
    UAA->>UAA: LegacyPasswordGrantAuthenticationProvider
    UAA->>UAA: UaaPasswordAuthenticationFlowService
    UAA->>UAA: AuthenticationResponseTokenGenerator
    UAA-->>NibBack: token response قدیمی
```

جریان کلاس‌ها:

- درخواست token endpoint وارد `AuthorizationServerSecurityConfig` می‌شود.
- `LegacyPasswordGrantAuthenticationConverter` درخواست HTTP را به `LegacyPasswordGrantAuthenticationToken` تبدیل می‌کند.
- نرمال‌سازی و map کردن پارامترها در کلاس‌های `LegacyPasswordGrantRequestMapper` و `LegacyDefaultGrantRequestMapper` انجام می‌شود.
- `LegacyClientTypeResolver` نوع کلاینت را با اتکا به `client_id` یا تنظیمات کلاینت، و نه فقط request hint، به NIB تشخیص می‌دهد.
- `LegacyPasswordGrantAuthenticationProvider` فقط adapter مربوط به Spring Authorization Server است.
- `UaaPasswordAuthenticationFlowService` سیاست‌های کلاینت، IP، نسخه، activation، load شدن user، و روش password/OTP را هماهنگ می‌کند.
- `AuthenticationResponseTokenGenerator` پاسخ OAuth2 token را می‌سازد.
- `LegacyPwaOauthLoginResponseProxyAdvisor` شکل response قدیمی را نگه می‌دارد، اما برای NIB cookie نمی‌سازد.
- `UaaObservation` و observation starter خروجی‌های JSONL برای LOG و TRACE تولید می‌کنند.

TRACE/LOG مهم:

- root HTTP span توسط filter در `scm-observation-starter` ساخته می‌شود.
- spanهای داخلی می‌توانند نام‌هایی مثل `uaa.legacy.nib.login` و `uaa.token.issue` داشته باشند.
- JSONL نباید شامل password، JWT خام، Authorization header، mobile خام، national code خام، OTP یا cookie value باشد.

## ۲. لاگین جدید NIB - صفحه لاگین داخل scm-uaa

در مسیر جدید، کاربر از NIB به authorization endpoint در UAA redirect می‌شود. سپس UAA صفحه لاگین را با theme/design مخصوص NIB نمایش می‌دهد. کاربر username/password را مستقیم داخل UAA وارد می‌کند، بنابراین بک‌اند NIB password کاربر را دریافت نمی‌کند.

این مسیر architecture هدف است و باید با Spring Authorization Server authorization-code flow سازگار بماند. این مسیر هیچ dependency به packageهای legacy ندارد.

```mermaid
sequenceDiagram
    participant Browser as Browser
    participant UAA as scm-uaa
    participant SAS as Spring Authorization Server

    Browser->>UAA: /oauth2/authorize?client_id=<client-id>
    SAS->>Browser: redirect به /login
    Browser->>UAA: GET /login
    UAA->>UAA: LoginPageController
    UAA->>UAA: ClientLoginThemeResolver
    UAA-->>Browser: login page با theme NIB
    Browser->>UAA: POST /login
    UAA->>UAA: UaaFormLoginAuthenticationProvider
    UAA->>UAA: UaaPasswordAuthenticationFlowService
    SAS->>SAS: ادامه authorization-code flow
```

جریان کلاس‌ها:

- مرورگر به `/oauth2/authorize` می‌زند.
- Spring Authorization Server کاربر را به `/login` هدایت می‌کند.
- `LoginPageController` صفحه لاگین را render می‌کند.
- `ClientLoginThemeResolver` theme مربوط به NIB را از `client_id` یا config تشخیص می‌دهد.
- `UaaFormLoginAuthenticationProvider` فقط integration مربوط به Spring form login است.
- `UaaPasswordAuthenticationFlowService` احراز هویت user/password را انجام می‌دهد.
- پس از موفقیت، Spring Authorization Server authorization-code flow را ادامه می‌دهد.
- token بعدا از مسیر استاندارد token endpoint صادر می‌شود.

TRACE/LOG مهم:

- رویدادها و spanهای اصلی: `uaa.login.page`، `uaa.login.submit`، `uaa.token.issue`
- فیلدهای JSONL باید شامل مواردی مثل `flow.name`، `auth.flow`، `client.id`، `event.outcome`، `status.code`، `correlation.id` و `trace.id` باشند.
- مقدارهای حساس باید mask یا حذف شوند.

## ۳. لاگین و activation در PWA - توکن و کد activation از طریق cookie

PWA هنوز از legacy activation/login flow استفاده می‌کند. PWA و MB Android business flow مشترک دارند؛ تفاوت فقط در token delivery است. PWA برای سازگاری قدیمی JWT را از طریق secure cookie دریافت می‌کند.

این cookie legacy و deprecated است. cookie فقط وقتی ساخته می‌شود که تنظیمات کلاینت و policy اجازه بدهند. request hint به تنهایی اجازه ساخت cookie نمی‌دهد. هدف آینده، authorization-code flow یا opaque server-side session است.

```mermaid
sequenceDiagram
    participant PWA as PWA
    participant UAA as scm-uaa
    participant RS as Resource Server

    PWA->>UAA: legacy activation/login request
    UAA->>UAA: LegacyPasswordGrantAuthenticationConverter
    UAA->>UAA: LegacyClientTypeResolver = PWA
    UAA->>UAA: RegisteredClientLegacyPolicy
    UAA->>UAA: UaaPasswordAuthenticationFlowService
    UAA->>UAA: PwaAuthenticationService
    UAA->>UAA: LegacyPwaOauthLoginResponseProxyAdvisor
    UAA->>UAA: LegacyPwaCookieTokenDeliveryStrategy
    UAA-->>PWA: response + secure cookie
    PWA->>RS: request با cookie
    RS->>RS: ScmBearerTokenResolver
```

جریان کلاس‌ها:

- درخواست وارد token endpoint یا legacy activation/login endpoint می‌شود.
- `LegacyPasswordGrantAuthenticationConverter` و mapperهای آن پارامترها و headerهای قدیمی را می‌خوانند.
- `LegacyClientTypeResolver` نوع کلاینت را بر اساس client config یا `client_id` به PWA تشخیص می‌دهد؛ request hint فقط کمک سازگاری است.
- `RegisteredClientLegacyPolicy` و `LegacyCookiePolicy` تصمیم می‌گیرند آیا PWA cookie مجاز است یا نه.
- `UaaPasswordAuthenticationFlowService` checkهای activation/login را هماهنگ می‌کند.
- `PwaAuthenticationService` compatibility مخصوص PWA را قبل/بعد از authentication انجام می‌دهد.
- `LegacyPwaOauthLoginResponseProxyAdvisor` response body قدیمی را نگه می‌دارد.
- `LegacyPwaCookieTokenDeliveryStrategy` تنها کلاس مجاز برای ساخت cookie توکن legacy است.
- resource serverهایی مثل `scm-web` از `scm-uaa-starter` استفاده می‌کنند.
- `ScmBearerTokenResolver` وقتی cookie support فعال باشد، توکن را از cookie می‌خواند.
- Authorization header همچنان default است مگر اینکه config چیز دیگری بگوید.

قانون‌های cookie:

- نام cookie قابل تنظیم است.
- cookie باید `HttpOnly` باشد.
- در production باید `Secure` باشد.
- `SameSite`، `Path` و `Max-Age` از config می‌آیند.
- cookie نباید password، OTP، activation code خام یا state نامرتبط داشته باشد.
- اگر JWT در cookie نگه داشته شده، فقط برای legacy compatibility است.

TRACE/LOG مهم:

- span/eventهای اصلی: `uaa.legacy.pwa.activation`، `uaa.legacy.pwa.login`، `uaa.legacy.token.delivery`، `uaa.cookie.create`، `uaa.token.issue`
- JSONL نباید JWT خام، cookie value، password، OTP، mobile خام یا national code خام داشته باشد.

## ۴. لاگین و activation در MB Android - توکن و کد activation از طریق header

MB Android همان business flow قدیمی PWA را استفاده می‌کند. تفاوت فقط token delivery است. MB هرگز نباید cookie دریافت کند. رفتار backward-compatible فعلی با `Authorization: Bearer ...` و token response/header باید حفظ شود.

PWA cookie strategy نباید برای MB انتخاب شود. request hint هم نباید MB را مجبور به گرفتن cookie کند.

```mermaid
sequenceDiagram
    participant MB as MB Android
    participant UAA as scm-uaa
    participant RS as Resource Server

    MB->>UAA: legacy activation/login request
    UAA->>UAA: legacy converter/request mapper
    UAA->>UAA: LegacyClientTypeResolver = MB
    UAA->>UAA: UaaPasswordAuthenticationFlowService
    UAA->>UAA: AuthenticationResponseTokenGenerator
    UAA-->>MB: token response/header قدیمی، بدون cookie
    MB->>RS: Authorization: Bearer <token>
    RS->>RS: ScmBearerTokenResolver / HeaderBearerTokenResolver
```

جریان کلاس‌ها:

- درخواست وارد token endpoint یا legacy activation/login endpoint می‌شود.
- converter و request mapper قدیمی headerها/پارامترهای MB را می‌خوانند.
- `LegacyClientTypeResolver` با `client_id` یا config نوع کلاینت را MB تشخیص می‌دهد؛ app version فقط hint سازگاری است.
- `UaaPasswordAuthenticationFlowService` client check، activation policy، user loading و روش password/OTP را انجام می‌دهد.
- token از مسیر response/header قدیمی برمی‌گردد.
- هیچ token delivery strategy برای MB اجرا نمی‌شود.
- resource serverها توکن MB را از Authorization header با `ScmBearerTokenResolver` و `HeaderBearerTokenResolver` اعتبارسنجی می‌کنند.

TRACE/LOG مهم:

- span/eventهای اصلی: `uaa.legacy.mb.activation`، `uaa.legacy.mb.login`، `uaa.token.issue`
- وقتی مرتبط است، attributeای مثل `cookie.created=false` باید قابل مشاهده باشد.
- برای MB نباید `Set-Cookie` ساخته شود.
- هیچ مقدار خام حساس نباید وارد JSONL شود.

## قانون طلایی امنیتی

هیچ‌وقت این مقدارها را log، trace یا داخل error message ننویسید:

- password
- OTP
- JWT خام
- cookie value
- Authorization header
- mobile number خام
- national code خام
- username خام اگر در context حساس است

اگر لازم بود شناسه‌ای برای troubleshooting داشته باشید، آن را mask کنید. برای مثال فقط prefix/suffix کوتاه را نگه دارید و بقیه را با `***` جایگزین کنید.

## LOG و TRACE چگونه JSONL می‌شوند؟

`scm-uaa` از `scm-observation-starter` استفاده می‌کند. هر inbound HTTP request یک root trace span دارد. کلاس‌های authentication و security برای operationهای داخلی child/application span یا structured event تولید می‌کنند.

خروجی LOG و TRACE به صورت JSONL است: هر خط دقیقا یک JSON object فشرده است. pretty print، JSON array و multiline stack trace در فایل‌ها مجاز نیست. Filebeat، Kafka یا Logstash می‌توانند این فایل‌های JSONL را line by line مصرف کنند.

توسعه‌دهنده‌ها نباید writer دستی مثل `FileWriter`، `BufferedWriter` یا `ObjectMapper` line writer بسازند. برای log/trace از abstractionهای موجود مثل `UaaObservation` و زیرساخت observation استفاده کنید.

## کجا کد اضافه کنیم؟

- تغییرات UI لاگین استاندارد UAA: `ir.daneshrefah.scm.uaa.web.login`
- تغییرات legacy token endpoint برای NIB/PWA/MB: `ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.*`
- تغییرات PWA cookie delivery: `ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.delivery`
- تغییرات response body قدیمی: `ir.daneshrefah.scm.uaa.security.oauth2.grant.legacy.response`
- تغییرات client policy: `ir.daneshrefah.scm.uaa.security.oauth2.policy`
- تغییرات SMS OTP grant: `ir.daneshrefah.scm.uaa.security.oauth2.grant.smsotp`
- تغییرات Shahkar: `ir.daneshrefah.scm.uaa.security.oauth2.grant.shahkar`
- تغییرات استخراج token در resource serverها: `scm-uaa-starter/.../resource`

## کجا کد اضافه نکنیم؟

- package قدیمی `security.authenticationProvider` را دوباره نسازید.
- empty marker service اضافه نکنید.
- no-op strategy اضافه نکنید.
- legacy response code را داخل package عمومی `service.proxy` نگذارید.
- cookie creation را داخل authentication provider نگذارید.
- business logic را داخل converter نگذارید.
- مقدار حساس را به log، trace یا error اضافه نکنید.

## خلاصه مسئولیت کلاس‌های اصلی

- `AuthorizationServerSecurityConfig`: registration مربوط به Spring Authorization Server و extension grantها.
- `LoginPageController`: نمایش صفحه لاگین UAA-hosted.
- `ClientLoginThemeResolver`: تشخیص theme بر اساس client.
- `UaaFormLoginAuthenticationProvider`: integration فرم لاگین با Spring Security.
- `UaaPasswordAuthenticationFlowService`: جریان مشترک user/password authentication برای login استاندارد و legacy password grant.
- `LegacyPasswordGrantAuthenticationConverter`: تبدیل request قدیمی token endpoint به authentication token.
- `LegacyPasswordGrantRequestMapper`: map کردن پارامترهای legacy request.
- `LegacyPasswordGrantAuthenticationProvider`: adapter مربوط به Spring Authorization Server برای legacy password grant.
- `RegisteredClientLegacyPolicy`: تصمیم‌های client-aware برای فعال بودن legacy grant و cookie.
- `LegacyCookiePolicy`: تصمیم نهایی برای مجاز بودن cookie legacy PWA.
- `LegacyPwaOauthLoginResponseProxyAdvisor`: نگه داشتن shape قدیمی response برای PWA/MB.
- `LegacyPwaCookieTokenDeliveryStrategy`: تنها محل ساخت cookie توکن PWA legacy.
- `ScmBearerTokenResolver`: استخراج token در resource server از header یا cookie طبق config.

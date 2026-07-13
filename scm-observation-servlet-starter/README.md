# راهنمای `scm-observation-servlet-starter`

## هدف

این ماژول adapter اختیاری Servlet برای هستهٔ مستقل `scm-observation-starter` است و مسئول موارد زیر است:

- ایجاد correlation محلی برای request؛
- قرار دادن metadata request در MDC؛
- ایجاد اختیاری HTTP server span؛
- استخراج امن method، path، route، status و client address؛
- ثبت filterها با order مشخص.

هستهٔ Observation به این adapter وابسته نیست. ماژول‌های غیر Servlet نباید این dependency را اضافه کنند.

## Dependency

```gradle
implementation project(':scm-observation-servlet-starter')
```

افزودن dependency هیچ signalی را فعال نمی‌کند. فعال‌سازی همچنان تابع variableهای هسته است.

## Variableهای Servlet Observation

قرارداد بیرونی فقط از طریق environment variable است:

| Variable | Default | توضیح |
| --- | --- | --- |
| `SCM_OBS_HTTP_SERVER_ENABLED` | `false` | اجازهٔ ایجاد generic HTTP server span |
| `SCM_OBS_HTTP_SERVER_MODE` | `channel-only` | policy ایجاد span؛ مقدار پیش‌فرض generic HTTP span را غیرفعال نگه می‌دارد |
| `SCM_OBS_HTTP_SERVER_SPAN_NAME` | `http.server.request` | نام span در صورت فعال بودن |

نمونهٔ یک میزبان عمومی HTTP:

```bash
export SCM_OBS_ENABLED=true
export SCM_OBS_TRACE_ENABLED=true
export SCM_OBS_HTTP_SERVER_ENABLED=true
export SCM_OBS_HTTP_SERVER_MODE=all
export SCM_OBS_HTTP_SERVER_SPAN_NAME=admin.http.request
```

نمونهٔ میزبانی که business trace را در لایهٔ اختصاصی Gateway می‌سازد:

```bash
export SCM_OBS_HTTP_SERVER_ENABLED=false
export SCM_OBS_HTTP_SERVER_MODE=channel-only
```

در حالت دوم adapter می‌تواند correlation/MDC را فراهم کند، اما generic HTTP business span ایجاد نمی‌کند.

## Signal gating

`ObservationMdcFilter` فقط زمانی فعال است که هر دو variable زیر true باشند:

```text
SCM_OBS_ENABLED=true
SCM_OBS_LOG_ENABLED=true
```

`HttpServerObservationFilter` فقط زمانی span ایجاد می‌کند که همهٔ شرط‌های زیر برقرار باشند:

```text
SCM_OBS_ENABLED=true
SCM_OBS_TRACE_ENABLED=true
SCM_OBS_HTTP_SERVER_ENABLED=true
SCM_OBS_HTTP_SERVER_MODE != channel-only
```

فعال‌کردن HTTP adapter به‌تنهایی TRACE را فعال نمی‌کند.

## رفتار Filterها

### `ObservationMdcFilter`

- روی `/*` و با order برابر `Ordered.HIGHEST_PRECEDENCE + 20` اجرا می‌شود؛
- ابتدا `X-Correlation-ID` غیرخالی را می‌خواند؛
- در نبود آن correlation id محلی تولید می‌کند؛
- `correlationId`, application name و profile را در MDC قرار می‌دهد؛
- پس از پایان request مقادیر قبلی MDC را restore می‌کند.

### `HttpServerObservationFilter`

- با order برابر `Ordered.HIGHEST_PRECEDENCE + 30` اجرا می‌شود؛
- server span با `correlation.type=request` می‌سازد؛
- method، URI، best matching route، status و client IP را ثبت می‌کند؛
- client IP را به ترتیب از اولین `X-Forwarded-For`، سپس `X-Real-IP` و سپس remote address می‌گیرد؛
- exception یا status برابر 400 و بیشتر را failure ثبت می‌کند؛
- async و error dispatch را دوباره observe نمی‌کند؛
- برای جلوگیری از duplicate از request attribute داخلی استفاده می‌کند.

Exception handler می‌تواند از API زیر استفاده کند تا exception ترجمه‌شده به HTTP response نیز روی span ثبت شود:

```java
HttpServerObservationFilter.recordException(request, throwable);
```

## مرز امنیتی

Adapter نباید raw headerهای زیر را در Observation قرار دهد:

```text
Authorization
Cookie
Set-Cookie
API key
Token
Password
OTP
```

هر attribute تولیدشده از مسیر registry، validation و sanitizer هسته عبور می‌کند.

## مسیر توسعه

برای افزودن metadata جدید Servlet:

1. semantic field را مشخص کنید؛
2. attribute را در ماژول مالک register کنید؛
3. extraction را در adapter با allowlist محدود اضافه کنید؛
4. از ثبت مقدار آزاد header جلوگیری کنید؛
5. مستند variable یا attribute جدید را به‌روزرسانی کنید.

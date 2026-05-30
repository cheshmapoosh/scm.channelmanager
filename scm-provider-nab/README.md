# scm-provider-nab

این ماژول برای فراخوانی سامانه NAB از Channel Manager است. NAB با TCP کار می‌کند و پیام‌ها fixed-length هستند. هدف این provider این است که تیم سرویس و plugin لازم نباشد برای هر command یک کلاس Java mapper بنویسد؛ ساختار request و response داخل JSON اعلام می‌شود و provider همان ساختار را encode/decode می‌کند.

## روش استفاده در route

برای operation از نوع provider، آدرس provider را مثل زیر تنظیم کنید:

```text
nab:request?provider=core
```

یا provider را با header بدهید:

```text
NabProvider = core
```

## شکل ورودی

بدنه ورودی باید `JsonNode` یا یک JSON object قابل تبدیل به `JsonNode` باشد:

```json
{
  "command": {
    "code": "27",
    "protocol": "ATPI"
  },
  "header": {
    "terminalType": "ATM",
    "channelCode": "MOBILE",
    "clientAddress": "10.1.1.10"
  },
  "data": {
    "customerId": "123456",
    "generalAccount": "0"
  },
  "request": {
    "fields": [
      { "name": "customerId", "length": 12, "required": true },
      { "name": "generalAccount", "length": 4 }
    ]
  },
  "response": {
    "recordSeparator": "\n",
    "fields": [
      { "name": "accountNo", "length": 18 },
      { "name": "accountType", "length": 2 }
    ]
  }
}
```

## Header

این provider خودش header NAB را می‌سازد. ترتیب resolve مقدارها:

- `serviceCode`: اگر در `header.serviceCode` آمده باشد همان استفاده می‌شود. در غیر این صورت اول از `terminalType` و بعد از `channelCode` در config lookup می‌شود. اگر پیدا نشود `defaultServiceCode` استفاده می‌شود.
- `userId` و `password`: اگر در ورودی آمده باشند override می‌شوند؛ در غیر این صورت مقدار ثابت provider instance استفاده می‌شود.
- `rqUid`: اگر در ورودی آمده باشد override می‌شود؛ در غیر این صورت داخل provider تولید می‌شود.
- `dateTime`: اگر در ورودی آمده باشد override می‌شود؛ در غیر این صورت timestamp شمسی با طول ۱۴ تولید می‌شود.
- `clientAddress`: برای پروتکل `ATPI` الزامی است و بلافاصله بعد از `nabProtocol` در header ارسال می‌شود. پروتکل‌های دیگر در تنظیم پیش‌فرض این فیلد را ندارند.

فیلدهای header هم مثل `request.fields` و `response.fields` تعریف می‌شوند و ترتیب array همان ترتیب ارسال به NAB است. تنظیم پیش‌فرض:

```text
ATPS: nabProtocol, command, serviceCode, dateTime, userId, password, rqUid
ATPI: nabProtocol, clientAddress, command, serviceCode, dateTime, userId, password, rqUid
MIRS: nabProtocol, command, serviceCode, dateTime, userId, password, rqUid
```

اگر ترتیب یا طول فیلدهای header در NAB تغییر کرد، آن را در config override کنید. فیلد اول باید `nabProtocol` باشد چون ابتدا ۴ بایت protocol جداگانه روی socket ارسال می‌شود.

## Field Spec

هم `request.fields` و هم `response.fields` از یک schema استفاده می‌کنند:

```json
{
  "name": "amount",
  "length": 18,
  "path": "/transfer/amount",
  "type": "NUMBER",
  "required": true,
  "converter": "TRIM",
  "padding": "LEFT_ZERO",
  "overflow": "ERROR",
  "trim": true
}
```

قواعد مهم:

- `name` اجباری است.
- `length` اجباری است.
- `type` اختیاری است و پیش‌فرض آن `STRING` است.
- `required` اختیاری است و پیش‌فرض آن `false` است.
- اگر `path` داده نشود، provider از `name` به عنوان JSON Pointer استفاده می‌کند. مثلا `customerId` یعنی `/customerId`.
- padding پیش‌فرض `RIGHT_SPACE` است.
- overflow پیش‌فرض `ERROR` است. اگر مقدار طولانی‌تر از `length` باشد خطا می‌گیریم. برای truncate صریحا `overflow: "TRUNCATE"` بگذارید.

typeهای پشتیبانی‌شده:

```text
STRING, NUMBER, DECIMAL, BOOLEAN, DATE, DATETIME, RAW
```

converterهای داخلی:

```text
NONE, TRIM, UPPERCASE, LOWERCASE, BOOLEAN_1_0,
PERSIAN_DIGITS_TO_ENGLISH, ENGLISH_DIGITS_TO_PERSIAN,
ARABIC_TO_PERSIAN_CHARS, PERSIAN_TO_ARABIC_CHARS
```

## Response

خروجی provider همیشه `JsonNode` است و raw response برنمی‌گرداند.

NAB در ابتدای response یک action code دارد. پیش‌فرض طول action code برابر ۵ است:

- `00000`: موفق، response یک object است و در `data` قرار می‌گیرد.
- `10000`: موفق از نوع list، هر line یک record است و هر line خودش با `10000` شروع می‌شود. records در `records` قرار می‌گیرند.
- هر مقدار دیگر: خطا است. در این حالت فقط `status` برمی‌گردد و دیتای response parse نمی‌شود.

اگر action code طول یا مقدار متفاوتی داشت، آن را در `response.status` تنظیم کنید:

```json
{
  "response": {
    "status": {
      "field": { "name": "actionCode", "length": 5 },
      "successCode": "00000",
      "successListCode": "10000"
    },
    "fields": [
      { "name": "accountNo", "length": 18 }
    ]
  }
}
```

خروجی موفق تک‌رکوردی:

```json
{
  "status": {
    "code": "00000",
    "success": true,
    "list": false
  },
  "data": {
    "accountNo": "000000000000123456"
  },
  "rqUid": "1234567890123456",
  "command": "27",
  "protocol": "ATPI"
}
```

خروجی موفق لیستی:

```json
{
  "status": {
    "code": "10000",
    "success": true,
    "list": true
  },
  "records": [
    { "accountNo": "000000000000123456" },
    { "accountNo": "000000000000654321" }
  ],
  "rqUid": "1234567890123456",
  "command": "27",
  "protocol": "ATPI"
}
```

خروجی خطا:

```json
{
  "status": {
    "code": "12345",
    "success": false,
    "list": false
  },
  "rqUid": "1234567890123456",
  "command": "27",
  "protocol": "ATPI"
}
```

## تنظیمات

```yaml
scm:
  provider:
    nab:
      enabled: true
      defaults:
        connect-timeout-ms: 3000
        socket-timeout-ms: 1000
        response-timeout-ms: 6000
        ack-length-bytes: 5
        charset: windows-1252
        default-service-code: "99"
        rq-uid:
          length: 16
          type: NUMERIC
        character-normalization:
          enabled: true
          replacements:
            "ي": "ی"
            "ك": "ک"
      providers:
        atps:
          protocol: ATPS
          endpoint: 10.10.10.10:3080
          user-id: "999998"
          password: "${NAB_PASSWORD}"
          rate-limit:
            enabled: true
            bucket: "nab-atps"
            key: "provider-operation"
        atpi:
          protocol: ATPI
          endpoint: 10.10.10.11:3080
          user-id: "999998"
          password: "${NAB_PASSWORD}"
          header-fields:
            - { name: "protocol", length: 4, required: true }
            - { name: "clientAddress", length: 64, required: true }
            - { name: "command", length: 2, required: true }
            - { name: "serviceCode", length: 2, required: true }
            - { name: "dateTime", length: 14, required: true }
            - { name: "userId", length: 10, required: true }
            - { name: "password", length: 10, required: true }
            - { name: "rqUid", length: 16, required: true }
          rate-limit:
            enabled: true
            bucket: "nab-atpi"
            key: "provider"
        mirs:
          protocol: MIRS
          endpoint: 10.10.10.12:3080
          user-id: "999998"
          password: "${NAB_PASSWORD}"
          service-codes-by-terminal-type:
            ATM: "01"
            WEB: "02"
          service-codes-by-channel-code:
            MOBILE: "03"
```

`header-fields` باید زیر همان provider instance تعریف شود (`providers.<instance>`). تعریف آن زیر `defaults` اعمال نمی‌شود.

برای سازگاری با نسخه‌های قبلی، `header-fields-by-protocol` هنوز پشتیبانی می‌شود؛ اما اگر هر دو تعریف شوند، `header-fields` اولویت دارد.

نکته encoding: مقدار پیش‌فرض طبق درخواست فعلی `windows-1252` است، اما اگر response یا request واقعا متن فارسی کامل دارد، باید با نمونه byte-level از NAB بررسی شود که charset درست `windows-1252` است یا `windows-1256`. این مقدار در config قابل تغییر است.

## Connection Strategy

با توجه به رفتار NAB (هر connection فقط یک command)، provider برای هر request یک socket جدید باز می‌کند و پس از دریافت response آن را می‌بندد:

```text
open socket
send protocol
read ack frame (5 bytes length + ack payload)
send request body
read response frame (5 bytes length + response payload)
close socket
```

برای response لیستی، provider چند frame پشت سر هم را می‌خواند:

- هر frame رکورد با `actionCode=10000` شروع می‌شود.
- frame خاتمه لیست برابر `00005 + 00000` است (یعنی payload فقط `00000`).
- خروجی این frameها به صورت line-based در parser ارسال می‌شود.

برای کنترل load در چند پاد Kubernetes، rate limit توزیع‌شده را فعال کنید:

- `rate-limit.enabled`
- `rate-limit.bucket`
- `rate-limit.key` (`provider`, `operation`, `provider-operation`)

کلیدهای override در endpoint/header نیز در دسترس هستند: `NabRateLimitEnabled`, `NabRateLimitBucket`, `NabRateLimitKey`.

`ack-length-bytes` طول prefix پیام NAB است (پیش‌فرض: `5`) و برای هر دو `ack` و `response` استفاده می‌شود.

## لاگ‌ها

provider در سطح‌های مختلف لاگ می‌نویسد:

- `INFO`: شروع/پایان call، endpoint، command، protocol، و محتوای wire ارسالی/دریافتی.
- `DEBUG`: ورودی و خروجی JSON و پیام کامل ساخته‌شده.
- `TRACE`: encode/decode هر field با offset و length.
- `WARN`: timeout، action code ناموفق، یا رکورد list با action code غیرمنتظره.
- `ERROR`: خطای اتصال یا خطای فراخوانی TCP.

اگر لاگ محتوای wire در محیط production حساس است، این گزینه را false کنید:

```yaml
scm.provider.nab.providers.core.wire-log-enabled: false
```

## اجرای تست

```bash
./gradlew :scm-provider-nab:test
```

تست `NabProviderServiceTcpIntegrationTest` یک NAB fake روی TCP بالا می‌آورد و مسیر واقعی protocol، ack، body و response را اجرا می‌کند.

برای فراخوانی واقعی NAB تست زیر اضافه شده است:

`ir.daneshrefah.scm.provider.nab.scenario.NabActiveAccountsInqRealIntegrationTest`

اجرای نمونه:

```bash
SCM_NAB_INTEGRATION=true \
SCM_NAB_ACTIVE_ACCOUNTS_CUSTOMER_ID=123456 \
./gradlew :scm-provider-nab:test --tests 'ir.daneshrefah.scm.provider.nab.scenario.NabActiveAccountsInqRealIntegrationTest'
```

متغیرهای مهم:

- `SCM_NAB_ENDPOINT` (پیش‌فرض: `10.15.27.12:3080`)
- `SCM_NAB_USER_ID` (پیش‌فرض: `999998`)
- `SCM_NAB_PASSWORD` (پیش‌فرض: `1234567890`)
- `SCM_NAB_TERMINAL_TYPE` (پیش‌فرض: `ATM`)
- `SCM_NAB_CHANNEL_CODE` (پیش‌فرض: `MOBILE`)
- `SCM_NAB_CLIENT_ADDRESS` (پیش‌فرض: `127.0.0.1`)
- `SCM_NAB_ACTIVE_ACCOUNTS_GENERAL_ACCOUNT` (اختیاری)

نکته: property اصلی از این نسخه `endpoint` است. property قدیمی `endpoints` فقط برای سازگاری خوانده می‌شود و باید دقیقا یک مقدار داشته باشد؛ اگر بیشتر از یک endpoint بدهید خطا می‌گیرید.

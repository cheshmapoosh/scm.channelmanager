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
        response-idle-timeout-ms: 100
        ack-length-bytes: 5
        charset: windows-1252
        default-service-code: "99"
        rq-uid:
          length: 16
          type: NUMERIC
        header-fields-by-protocol:
          ATPI:
            - { name: "nabProtocol", length: 4, required: true }
            - { name: "clientAddress", length: 64, required: true }
            - { name: "command", length: 2, required: true }
            - { name: "serviceCode", length: 2, required: true }
            - { name: "dateTime", length: 14, required: true }
            - { name: "userId", length: 10, required: true }
            - { name: "password", length: 10, required: true }
            - { name: "rqUid", length: 16, required: true }
        connection-pool:
          enabled: true
          max-total: 16
          min-idle: 0
          max-idle: 16
          max-wait-ms: 1000
          min-evictable-idle-time-ms: 60000
          soft-min-evictable-idle-time-ms: 60000
          time-between-eviction-runs-ms: 30000
          max-life-time-ms: 300000
          test-on-borrow: true
          test-on-return: false
          test-while-idle: true
          block-when-exhausted: true
          lifo: true
          prefill: false
        character-normalization:
          enabled: true
          replacements:
            "ي": "ی"
            "ك": "ک"
      providers:
        core:
          endpoints:
            - 10.10.10.10:3080
          user-id: "999998"
          password: "${NAB_PASSWORD}"
          service-codes-by-terminal-type:
            ATM: "01"
            WEB: "02"
          service-codes-by-channel-code:
            MOBILE: "03"
```

نکته encoding: مقدار پیش‌فرض طبق درخواست فعلی `windows-1252` است، اما اگر response یا request واقعا متن فارسی کامل دارد، باید با نمونه byte-level از NAB بررسی شود که charset درست `windows-1252` است یا `windows-1256`. این مقدار در config قابل تغییر است.

## Connection Pool

برای throughput بالا، provider به صورت پیش‌فرض از `Apache Commons Pool2` استفاده می‌کند. هر request یک connection را از pool می‌گیرد، کل چرخه زیر را به صورت blocking روی همان connection انجام می‌دهد، و بعد connection را به pool برمی‌گرداند:

```text
borrow connection
send protocol
read ack
send request body
read response
return connection
```

تا وقتی یک request روی connection در حال اجرا است، request دیگری از همان connection استفاده نمی‌کند. بنابراین ترتیب request/response روی هر socket حفظ می‌شود.

پارامترهای pool:

- `enabled`: اگر `false` شود، هر request یک socket جدید باز و بعد close می‌کند.
- `max-total`: بیشترین تعداد connection همزمان برای یک provider instance.
- `min-idle`: حداقل connection idle که pool تلاش می‌کند نگه دارد.
- `max-idle`: بیشترین connection idle که بعد از برگشت به pool نگه داشته می‌شود.
- `max-wait-ms`: حداکثر زمان انتظار برای گرفتن connection از pool. اگر همه connectionها مشغول باشند و زمان تمام شود، request خطا می‌گیرد.
- `min-evictable-idle-time-ms`: اگر connection بیش از این زمان idle بماند، eviction thread می‌تواند آن را ببندد.
- `soft-min-evictable-idle-time-ms`: eviction نرم‌تر که با حفظ `min-idle` کار می‌کند.
- `time-between-eviction-runs-ms`: فاصله اجرای eviction thread.
- `max-life-time-ms`: بیشترین عمر یک connection. بعد از این زمان connection بازنشسته می‌شود.
- `test-on-borrow`: قبل از borrow وضعیت socket و عمر connection بررسی می‌شود. validation غیرمخرب است و پیام probe به NAB نمی‌فرستد.
- `test-on-return`: هنگام برگشت connection به pool validate انجام می‌شود.
- `test-while-idle`: connectionهای idle در eviction run validate می‌شوند.
- `block-when-exhausted`: اگر pool پر بود، request تا `max-wait-ms` منتظر می‌ماند.
- `lifo`: اگر `true` باشد آخرین connection برگشتی زودتر دوباره استفاده می‌شود.
- `prefill`: اگر `true` باشد، در زمان ساخت pool به اندازه `min-idle` connection ساخته می‌شود. اگر NAB در زمان startup در دسترس نباشد، بهتر است `false` بماند.

برای شروع production، `max-total` را برابر حداکثر concurrency مجاز سمت NAB بگذارید، نه صرفا تعداد threadهای برنامه. virtual thread می‌تواند درخواست‌های زیادی بسازد، اما pool باید فشار روی NAB را کنترل کند.

برای سازگاری با نسخه اولیه provider، نام‌های قدیمی `max-size`، `borrow-timeout-ms`، `max-idle-time-ms` و `validation-enabled` هنوز به عنوان alias خوانده می‌شوند؛ برای config جدید از نام‌های بالا استفاده کنید.

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

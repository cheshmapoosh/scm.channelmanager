# راهنمای تنظیمات scm-logging

## 1. هدف این فایل

| کلید | عنوان | توضیحات |
|---|---|---|
| `scm-logging` | هدف راهنما | این فایل برای پشتیبانی توضیح میدهد تنظیمات سرویس persistence لاگ، MQ، converterها، scheduler و observation چگونه مقداردهی میشوند. |
| `application.yml` | نقشه اصلی | propertyهای واقعی سرویس در این فایل تعریف میشوند و مقدار آنها از متغیرها خوانده میشود. |

## 2. قانون کلی تنظیمات

| کلید | عنوان | توضیحات |
|---|---|---|
| `dev,test,pilot,prod` | profileهای مجاز | فقط این چهار profile مجاز هستند و profileهای قدیمی نباید فعال شوند. |
| `SCM_LOGGING_*` | متغیرهای datasource و MQ | مقدارهای دیتابیس و MQ باید از این متغیرها یا Secret Management بیایند. |
| `scm.log.*` | namespace داخلی سرویس | این namespace در `scm-logging` قدیمی نیست و مربوط به processing، converter و scheduler همین سرویس است. |
| `Credential` | مقدار حساس | رمزهای دیتابیس و MQ حساس هستند و نباید در ticket، screenshot یا chat ارسال شوند. |

## 3. فایلهای مهم

| کلید | عنوان | توضیحات |
|---|---|---|
| `src/main/resources/application.yml` | فایل اصلی تنظیمات | mapper مرکزی سرویس است و datasourceها، MQ، converterها و observation را از متغیرها میخواند. |
| `src/main/resources/application-dev.yml` | تنظیمات dev | Config Server را خاموش میکند و مقدارهای local/dev را تعریف میکند. |
| `src/main/resources/application-test.yml` | تنظیمات test | فقط Config Server را bootstrap میکند و مقدارها باید از Config Server بیایند. |
| `src/main/resources/application-pilot.yml` | تنظیمات pilot | فقط Config Server را bootstrap میکند و نبود Config Server باید startup را شکست دهد. |
| `src/main/resources/application-prod.yml` | تنظیمات prod | فقط Config Server را bootstrap میکند و مقدارهای حساس باید از Secret Management بیایند. |
| `src/main/resources/logback-spring.xml` | تنظیم logback | خروجی application log را با observation هماهنگ میکند و جایگزین `scm.log.app.*` است. |

## 4. متغیرهای عمومی

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_ENV` | نام محیط | محیط اجرای سرویس است. مقدار اشتباه باعث label اشتباه در log و trace میشود. |
| `SCM_APP` | نام برنامه | نام Spring application است و معمولاً باید `scm-logging` باشد. |
| `SCM_METADATA_NAMESPACE` | namespace اجرا | در Kubernetes از metadata.namespace میآید و در dev مقدار local دارد. |
| `SCM_METADATA_INSTANCE_ID` | شناسه نمونه | شناسه node یا pod است و برای عیب یابی مصرف MQ و insertها استفاده میشود. |
| `SCM_METADATA_TIME_ZONE` | timezone فایل | اگر خالی باشد timezone سیستم JVM استفاده میشود و فقط برای نام فایلهای observation است. |
| `SCM_SERVER_PORT` | پورت سرویس | پورت HTTP سرویس است. مقدار اشتباه باعث شکست health check میشود. |
| `SCM_CACHE_CLUSTER_NAME` | نام کلاستر کش | اگر cache client فعال باشد باید با محیط هماهنگ باشد، مثل `scm-cache-prod`. |
| `SCM_CACHE_ADDRESS` | آدرس کش | آدرس cache server است. مقدار اشتباه باعث کار نکردن cache client میشود. |

## 5. متغیرهای اختصاصی همان سرویس

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_LOGGING_JMS_DESTINATION` | مقصد JMS | نام queue یا destination مصرف لاگ است. مقدار اشتباه باعث خوانده نشدن پیامها میشود. |
| `SCM_LOGGING_MQ_QUEUE_MANAGER` | Queue Manager | نام queue manager است. مقدار اشتباه باعث شکست اتصال MQ میشود. |
| `SCM_LOGGING_MQ_CHANNEL` | کانال MQ | نام channel اتصال MQ است. مقدار اشتباه باعث خطای authorization یا connection میشود. |
| `SCM_LOGGING_MQ_HOST` | میزبان MQ | آدرس MQ است. مقدار اشتباه باعث timeout یا اتصال به مقصد نادرست میشود. |
| `SCM_LOGGING_MQ_PORT` | پورت MQ | پورت MQ است. مقدار اشتباه باعث شکست اتصال میشود. |
| `SCM_LOGGING_MQ_USERNAME` | کاربر MQ | نام کاربری MQ است و حساس محسوب میشود. نباید در ticket، screenshot یا chat ارسال شود. |
| `SCM_LOGGING_MQ_PASSWORD` | رمز MQ | رمز MQ است و کاملاً حساس است. باید از Secret یا محیط امن تأمین شود. |
| `SCM_LOGGING_TRANSACTION_DB_URL` | دیتابیس transaction | آدرس دیتابیس transaction log است. مقدار اشتباه باعث insert نشدن transaction log میشود. |
| `SCM_LOGGING_TRANSACTION_DB_USERNAME` | کاربر transaction DB | نام کاربری دیتابیس transaction است و حساس محسوب میشود. |
| `SCM_LOGGING_TRANSACTION_DB_PASSWORD` | رمز transaction DB | رمز دیتابیس transaction است و کاملاً حساس است. نباید در ticket، screenshot یا chat ارسال شود. |
| `SCM_LOGGING_TRACE_DB_URL` | دیتابیس trace | آدرس دیتابیس trace است. مقدار اشتباه باعث ثبت نشدن traceهای legacy میشود. |
| `SCM_LOGGING_TRACE_DB_USERNAME` | کاربر trace DB | نام کاربری دیتابیس trace است و حساس محسوب میشود. |
| `SCM_LOGGING_TRACE_DB_PASSWORD` | رمز trace DB | رمز دیتابیس trace است و کاملاً حساس است. |
| `SCM_LOGGING_MESSAGE_DB_URL` | دیتابیس message | آدرس دیتابیس message log است. مقدار اشتباه باعث ثبت نشدن message log میشود. |
| `SCM_LOGGING_MESSAGE_DB_USERNAME` | کاربر message DB | نام کاربری دیتابیس message است و حساس محسوب میشود. |
| `SCM_LOGGING_MESSAGE_DB_PASSWORD` | رمز message DB | رمز دیتابیس message است و کاملاً حساس است. |
| `SCM_LOG_CONVERTER_ENABLED` | فعال بودن converter | معادل متغیر برای `scm.log.converter.enabled` است. تغییر اشتباه پردازش لاگها را متوقف میکند. |
| `SCM_LOG_CHUNK_SIZE` | اندازه batch | اندازه پردازش batch لاگها است. مقدار خیلی بزرگ یا کوچک روی کارایی اثر میگذارد. |

## 6. متغیرهای Observation

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_OBS_ROOT_DIR` | مسیر اصلی observation | مسیر پایه log، trace و audit است. مقدار اشتباه باعث نوشته نشدن فایلها در مسیر مورد انتظار میشود. |
| `SCM_OBS_LOG_CONSOLE_ENABLED` | لاگ کنسول | لاگ کنسول را روشن یا خاموش میکند. در dev معمولاً روشن است. |
| `SCM_OBS_LOG_FILE_ENABLED` | لاگ فایل | فایل application log را روشن یا خاموش میکند. |
| `SCM_OBS_TRACE_FILE_ENABLED` | فایل trace | فایل trace را کنترل میکند و برای عیب یابی پردازش پیامها مفید است. |
| `SCM_OBS_AUDIT_ENABLED` | audit | audit را کنترل میکند و باید جدا از application log باقی بماند. |

## 7. متغیرهای اتصال به Config Server

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_CONFIG_SERVER_URL` | آدرس Config Server | آدرس دریافت تنظیمات مرکزی است. مقدار اشتباه در test/pilot/prod مانع startup درست میشود. |
| `SCM_LABEL` | label کانفیگ | label مربوط به Spring Cloud Config است و میتواند branch، tag یا commit باشد. |
| `SCM_CONFIG_USERNAME` | کاربر Config Server | نام کاربری Config Server است و حساس محسوب میشود. نباید در ticket، screenshot یا chat ارسال شود. |
| `SCM_CONFIG_PASSWORD` | رمز Config Server | رمز Config Server است و حساس است. باید از Secret یا محیط امن تأمین شود. |
| `SCM_CONFIG_FAIL_FAST` | شکست سریع | در pilot/prod باید فعال باشد تا سرویس با config ناقص بالا نیاید. |

## 8. مواردی که نباید دوباره استفاده شوند

| کلید | عنوان | توضیحات |
|---|---|---|
| `ib4dev,ib4test,scm4dev,scm4test,mb4dev,mb4test,pwa4dev,pwa4test` | profileهای قدیمی | این profileها نباید برای logging service فعال شوند. |
| `scm.log.app.*` | تنظیم قدیمی logback | این گروه قدیمی برای مسیر فایل application log نباید استفاده شود و مسیرها باید از observation بیایند. |
| `scm.log.*` | استثنای logging service | این namespace در همین سرویس مربوط به converter، scheduler و processing است و در این task نباید rename یا حذف شود. |
| `scm.logging.*` | refactor ممنوع | تبدیل `scm.log.*` به `scm.logging.*` در این task انجام نمیشود و باید در task جدا بررسی شود. |
| `نام مبهم cache` | نام غیرمجاز | نباید به عنوان نام کلاستر cache استفاده شود. مقدار درست dev برابر `scm-cache-dev` است. |

## 9. نکات خاص هر محیط

| کلید | عنوان | توضیحات |
|---|---|---|
| `dev` | محیط توسعه | Config Server خاموش است و مقدارهای MQ و دیتابیس از `application-dev.yml` خوانده میشوند. |
| `test` | محیط تست | مقدارها باید از Config Server بیایند و cache cluster باید `scm-cache-test` باشد. |
| `pilot` | محیط پایلوت | رمزهای دیتابیس و MQ باید از Secret یا تنظیمات امن محیط بیایند و نباید hardcode شوند. |
| `prod` | محیط عملیاتی | اگر Config Server یا Secretها در دسترس نباشند، سرویس نباید با config ناقص بالا بیاید. |

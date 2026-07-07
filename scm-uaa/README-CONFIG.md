# راهنمای تنظیمات scm-uaa

## 1. هدف این فایل

| کلید | عنوان | توضیحات |
|---|---|---|
| `scm-uaa` | هدف راهنما | این فایل برای پشتیبانی توضیح میدهد تنظیمات احراز هویت، token، OTP، datasource و observation از چه متغیرهایی میآیند. |
| `application.yml` | نقشه اصلی | propertyهای واقعی UAA در این فایل تعریف میشوند و مقدار آنها از متغیرهای profile یا Config Server خوانده میشود. |

## 2. قانون کلی تنظیمات

| کلید | عنوان | توضیحات |
|---|---|---|
| `dev,test,pilot,prod` | profileهای مجاز | فقط این چهار profile مجاز هستند و profileهای قدیمی نباید فعال شوند. |
| `SCM_UAA_BASE_URL` | آدرس واحد UAA | base-url، issuer-url و jwk-set-url همگی از همین متغیر ساخته میشوند. |
| `متغیر جداگانه issuer` | متغیر غیرمجاز | نباید در profileها تعریف شود مگر قرارداد کد جدید و مستند اضافه شود. |
| `متغیر جداگانه JWK` | متغیر غیرمجاز | نباید در profileها تعریف شود و JWK از `SCM_UAA_BASE_URL/oauth2/jwks` ساخته میشود. |
| `Credential` | مقدار حساس | رمزها، secretها، tokenها و کلیدها حساس هستند و نباید در ticket، screenshot یا chat ارسال شوند. |

## 3. فایلهای مهم

| کلید | عنوان | توضیحات |
|---|---|---|
| `src/main/resources/application.yml` | فایل اصلی تنظیمات | mapper مرکزی UAA است و propertyهای واقعی را از متغیرها میخواند. |
| `src/main/resources/application-dev.yml` | تنظیمات dev | Config Server را خاموش میکند و مقدارهای local/dev را تعریف میکند. |
| `src/main/resources/application-test.yml` | تنظیمات test | فقط Config Server را bootstrap میکند و مقدارهای UAA باید از Config Server بیایند. |
| `src/main/resources/application-pilot.yml` | تنظیمات pilot | فقط Config Server را bootstrap میکند و نبود Config Server باید startup را شکست دهد. |
| `src/main/resources/application-prod.yml` | تنظیمات prod | فقط Config Server را bootstrap میکند و مقدارهای حساس باید از Secret Management بیایند. |
| `scm-config/config-repo/scm-uaa/application-*.yml` | فایلهای متغیر Config Server | متغیرهای محیطی UAA مثل `SCM_UAA_BASE_URL` و cache client را به صورت flat نگه میدارند. |

## 4. متغیرهای عمومی

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_ENV` | نام محیط | محیط اجرای سرویس است. مقدار اشتباه باعث label اشتباه در log و trace میشود. |
| `SCM_APPLICATION_NAME` | نام برنامه | نام Spring application است و معمولاً باید `scm-uaa` باشد. |
| `SCM_INSTANCE_ID` | شناسه نمونه | شناسه node یا pod است و برای عیب یابی استفاده میشود. |
| `SCM_SERVER_PORT` | پورت سرویس | پورت HTTP UAA است. مقدار اشتباه باعث شکست route یا health check میشود. |
| `SCM_UAA_BASE_URL` | آدرس پایه UAA | آدرس issuer و JWK از همین مقدار ساخته میشود. مقدار اشتباه باعث نامعتبر شدن tokenها میشود. |
| `SCM_CACHE_CLUSTER_NAME` | نام کلاستر کش | نام کلاستر cache client است و باید با محیط هماهنگ باشد. |
| `SCM_CACHE_ADDRESS` | آدرس کش | آدرس cache server است. مقدار اشتباه باعث کار نکردن cache session یا user میشود. |

## 5. متغیرهای اختصاصی همان سرویس

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_DB_URL` | آدرس دیتابیس اصلی | آدرس JDBC دیتابیس اصلی UAA است. مقدار اشتباه باعث بالا نیامدن سرویس یا خطای login میشود. |
| `SCM_DB_USERNAME` | کاربر دیتابیس اصلی | نام کاربری دیتابیس است و حساس محسوب میشود. نباید در ticket، screenshot یا chat ارسال شود. |
| `SCM_DB_PASSWORD` | رمز دیتابیس اصلی | رمز دیتابیس است و کاملاً حساس است. در pilot/prod باید از Secret Management بیاید. |
| `SCM_DB_SCHEMA` | schema اصلی | schema پیشفرض دیتابیس اصلی است. مقدار اشتباه باعث خطای جدول میشود. |
| `SCM_UAA_ACTIVATION_DB_URL` | آدرس دیتابیس activation | آدرس دیتابیس activation است. مقدار اشتباه باعث خطای فعالسازی کاربر میشود. |
| `SCM_UAA_ACTIVATION_DB_USERNAME` | کاربر activation | نام کاربری دیتابیس activation است و حساس محسوب میشود. نباید در ticket، screenshot یا chat ارسال شود. |
| `SCM_UAA_ACTIVATION_DB_PASSWORD` | رمز activation | رمز دیتابیس activation است و کاملاً حساس است. |
| `SCM_OTP_TTL` | زمان اعتبار OTP | مدت اعتبار OTP است. مقدار خیلی کم یا زیاد باعث مشکل عملیاتی در ورود کاربران میشود. |
| `SCM_OTP_AVACAS_HOST` | میزبان AVACAS | آدرس MQ یا سرویس مرتبط با OTP است. مقدار اشتباه باعث ارسال نشدن OTP میشود. |
| `SCM_OTP_AVACAS_PASSWORD` | رمز AVACAS | رمز اتصال OTP/MQ است و حساس است. نباید در ticket، screenshot یا chat ارسال شود. |
| `SCM_SECURITY_CORS_ALLOWED_ORIGINS` | مبداهای مجاز CORS | فهرست originهای مجاز است. مقدار اشتباه باعث block شدن درخواستهای frontend میشود. |
| `SCM_CAPTCHA_BORDER_ENABLED` | captcha | تنظیمات captcha login را کنترل میکند. تغییر اشتباه ممکن است login را مختل کند. |

## 6. متغیرهای Observation

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_OBS_ROOT_DIR` | مسیر اصلی observation | مسیر پایه log، trace و audit است. مقدار اشتباه باعث نوشته نشدن فایلها در مسیر مورد انتظار میشود. |
| `SCM_OBS_LOG_CONSOLE_ENABLED` | لاگ کنسول | لاگ کنسول را روشن یا خاموش میکند. در dev معمولاً روشن است. |
| `SCM_OBS_LOG_FILE_ENABLED` | لاگ فایل | فایل log را روشن یا خاموش میکند. خاموش بودن اشتباه باعث از دست رفتن لاگ عملیاتی میشود. |
| `SCM_OBS_TRACE_FILE_ENABLED` | فایل trace | فایل trace را کنترل میکند و برای عیب یابی login و token مهم است. |
| `SCM_UAA_AUTH_TRACE_ASPECT_ENABLED` | trace احراز هویت | trace aspect مربوط به authentication را کنترل میکند. مقدار اشتباه عیب یابی login را سخت میکند. |
| `SCM_OBS_AUDIT_ENABLED` | audit | audit را کنترل میکند و باید جدا از application log باقی بماند. |

## 7. متغیرهای اتصال به Config Server

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_CONFIG_SERVER_URL` | آدرس Config Server | آدرس دریافت تنظیمات مرکزی است. مقدار اشتباه در test/pilot/prod مانع startup درست میشود. |
| `SCM_CONFIG_USERNAME` | کاربر Config Server | نام کاربری Config Server است و حساس محسوب میشود. نباید در ticket، screenshot یا chat ارسال شود. |
| `SCM_CONFIG_PASSWORD` | رمز Config Server | رمز Config Server است و حساس است. باید از Secret یا محیط امن تأمین شود. |
| `SCM_CONFIG_FAIL_FAST` | شکست سریع | در pilot/prod باید فعال باشد تا سرویس با config ناقص بالا نیاید. |

## 8. مواردی که نباید دوباره استفاده شوند

| کلید | عنوان | توضیحات |
|---|---|---|
| `ib4dev,ib4test,scm4dev,scm4test,mb4dev,mb4test,pwa4dev,pwa4test` | profileهای قدیمی | این profileها نباید برای UAA فعال شوند. |
| `متغیر جداگانه issuer` | متغیر حذف شده | نباید در profileها تعریف شود. issuer از `SCM_UAA_BASE_URL` ساخته میشود. |
| `متغیر جداگانه JWK` | متغیر حذف شده | نباید در profileها تعریف شود. JWK از `SCM_UAA_BASE_URL/oauth2/jwks` ساخته میشود. |
| `نام مبهم cache` | نام غیرمجاز | نباید به عنوان نام کلاستر cache استفاده شود. مقدار درست dev برابر `scm-cache-dev` است. |
| `scm.log.app.*` | تنظیم قدیمی logback | مسیرهای log باید از observation و `SCM_OBS_ROOT_DIR` ساخته شوند. |

## 9. نکات خاص هر محیط

| کلید | عنوان | توضیحات |
|---|---|---|
| `dev` | محیط توسعه | Config Server خاموش است و `SCM_UAA_BASE_URL` از `application-dev.yml` خوانده میشود. |
| `test` | محیط تست | مقدار `SCM_UAA_BASE_URL` باید آدرس UAA تست باشد و cache cluster باید `scm-cache-test` باشد. |
| `pilot` | محیط پایلوت | مقدارهای حساس باید از Secret یا تنظیمات امن محیط بیایند و نباید hardcode شوند. |
| `prod` | محیط عملیاتی | اگر Config Server یا Secretها در دسترس نباشند، سرویس نباید با config ناقص بالا بیاید. |

# راهنمای تنظیمات scm-web

## 1. هدف این فایل

| کلید | عنوان | توضیحات |
|---|---|---|
| `scm-web` | هدف راهنما | این فایل برای تیم پشتیبانی توضیح میدهد هر متغیر چه کاری انجام میدهد و اگر مقدار آن اشتباه باشد کدام بخش سرویس دچار مشکل میشود. |
| `application.yml` | نقشه اصلی | propertyهای واقعی برنامه فقط در این فایل تعریف میشوند و مقدار آنها از متغیرها خوانده میشود. |

## 2. قانون کلی تنظیمات

| کلید | عنوان | توضیحات |
|---|---|---|
| `dev,test,pilot,prod` | profileهای مجاز | فقط این چهار profile مجاز هستند. profileهای قدیمی کانالی نباید دوباره فعال شوند. |
| `SCM_*` | منبع مقداردهی | در profileها و Config Server فقط متغیر تعریف میشود، نه ساختار nested مثل `scm.providers...`. |
| `SCM_UAA_BASE_URL` | آدرس واحد UAA | همه آدرسهای issuer و JWK از همین متغیر ساخته میشوند. متغیر جداگانه برای issuer یا JWK نباید تعریف شود. |
| `Credential` | مقدار حساس | هر username، password، key، token، PIN، MAC و user id حساس است و نباید در ticket، screenshot یا chat ارسال شود. |

## 3. فایلهای مهم

| کلید | عنوان | توضیحات |
|---|---|---|
| `src/main/resources/application.yml` | فایل اصلی تنظیمات | mapper مرکزی سرویس است و propertyهای واقعی Spring و SCM را از متغیرها میخواند. |
| `src/main/resources/application-dev.yml` | تنظیمات dev | Config Server را خاموش میکند و متغیرهای local/dev را مقداردهی میکند. |
| `src/main/resources/application-test.yml` | تنظیمات test | فقط اتصال به Config Server را bootstrap میکند و مقدارهای سرویس باید از Config Server بیایند. |
| `src/main/resources/application-pilot.yml` | تنظیمات pilot | فقط اتصال اجباری به Config Server را bootstrap میکند. اگر Config Server در دسترس نباشد سرویس نباید با config ناقص بالا بیاید. |
| `src/main/resources/application-prod.yml` | تنظیمات prod | فقط اتصال اجباری به Config Server را bootstrap میکند و مقدارهای حساس باید از Secret یا محیط امن تأمین شوند. |
| `scm-config/config-repo/scm-web/application-*.yml` | فایلهای متغیر Config Server | مقدارهای profileهای test، pilot و prod را به صورت متغیرهای flat نگه میدارند. |

## 4. متغیرهای عمومی

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_ENV` | نام محیط | محیط اجرای سرویس است. اگر اشتباه باشد labelهای لاگ و trace و رفتارهای محیطی اشتباه ثبت میشوند. |
| `SCM_APP` | نام برنامه | نام Spring application است و معمولاً باید `scm-web` باشد. |
| `SCM_METADATA_NAMESPACE` | namespace اجرا | در Kubernetes از metadata.namespace میآید و در dev مقدار local دارد. |
| `SCM_METADATA_INSTANCE_ID` | شناسه نمونه | شناسه node یا pod است. اگر تکراری باشد تشخیص مشکل در لاگ و trace سخت میشود. |
| `SCM_METADATA_TIME_ZONE` | timezone فایل | اگر خالی باشد timezone سیستم JVM استفاده میشود و فقط برای نام فایلهای observation است. |
| `SCM_CHANNEL_CODE` | کد کانال | کد کانال فعال را مشخص میکند. مقدار اشتباه باعث route شدن درخواستها به کانال نادرست میشود. |
| `SCM_GATEWAY_NAME` | نام gateway | نام عملیاتی gateway است. اگر اشتباه باشد هویت سرویس در runtime و گزارشها اشتباه میشود. |
| `SCM_DB_URL` | آدرس دیتابیس | آدرس JDBC دیتابیس اصلی است. اگر اشتباه باشد سرویس بالا نمیآید یا داده نمیخواند. |
| `SCM_DB_USERNAME` | کاربر دیتابیس | نام کاربری دیتابیس است و مقدار حساس محسوب میشود. نباید در ticket، screenshot یا chat ارسال شود. |
| `SCM_DB_PASSWORD` | رمز دیتابیس | رمز دیتابیس است و کاملاً حساس است. در pilot/prod باید از Secret Management تأمین شود. |
| `SCM_DB_SCHEMA` | schema دیتابیس | schema پیشفرض دیتابیس است. مقدار اشتباه باعث خطای جدول یا query میشود. |
| `SCM_UAA_BASE_URL` | آدرس UAA | آدرس پایه UAA است. اگر اشتباه باشد اعتبارسنجی token و عملیات OTP دچار مشکل میشود. |
| `SCM_CACHE_CLUSTER_NAME` | نام کلاستر کش | نام کلاستر Hazelcast است و باید با محیط هماهنگ باشد، مثل `scm-cache-test`. |
| `SCM_CACHE_ADDRESS` | آدرس کش | آدرس node یا service کش است. مقدار اشتباه باعث قطع cache distributed میشود. |

## 5. متغیرهای اختصاصی همین سرویس

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_HPS_BASE_URL` | آدرس پایه HPS | آدرس پایه سرویس HPS است. اگر اشتباه باشد providerهای وابسته به HPS پاسخ نمیگیرند. |
| `SCM_HPS_REST_BASE_URL` | آدرس HPS REST | آدرس endpoint REST HPS است. در هر محیط باید به سرویس همان محیط اشاره کند. |
| `SCM_HPS_REST_USERNAME` | کاربر HPS REST | نام کاربری اتصال HPS REST است و حساس محسوب میشود. نباید در ticket، screenshot یا chat ارسال شود. |
| `SCM_HPS_REST_PASSWORD` | رمز HPS REST | رمز اتصال HPS REST است و کاملاً حساس است. در pilot/prod باید از Secret Management بیاید. |
| `SCM_HPS_SHETAB7_ENDPOINTS` | endpointهای Shetab7 | فهرست endpointهای TCP/ISO است. مقدار اشتباه باعث timeout یا ارسال به مقصد نادرست میشود. |
| `SCM_HPS_SHETAB7_PIN_KEY` | کلید PIN | کلید رمزنگاری PIN است و کاملاً حساس است. نباید در ticket، screenshot یا chat ارسال شود. |
| `SCM_HPS_SHETAB7_MAC_KEY` | کلید MAC | کلید MAC است و کاملاً حساس است. مقدار اشتباه باعث reject شدن پیامهای Shetab میشود. |
| `SCM_API_REPO_BASE_URL` | آدرس API Repo | آدرس API Repo است. مقدار اشتباه باعث خطای دریافت اطلاعات API میشود. |
| `SCM_NAB_ATPS_ENDPOINT` | endpoint NAB ATPS | آدرس و پورت NAB ATPS است. مقدار اشتباه باعث timeout یا اتصال به مقصد نادرست میشود. |
| `SCM_NAB_ATPS_USER_ID` | user id NAB | شناسه کاربر NAB ATPS است و حساس محسوب میشود. نباید در ticket، screenshot یا chat ارسال شود. |
| `SCM_NAB_ATPS_PASSWORD` | رمز NAB | رمز NAB ATPS است و کاملاً حساس است. در pilot/prod باید از Secret Management بیاید. |

## 6. متغیرهای Observation

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_OBS_ROOT_DIR` | مسیر اصلی observation | مسیر پایه فایلهای log، trace و audit است. اگر اشتباه باشد فایلها در مسیر نادرست نوشته میشوند یا نوشته نمیشوند. |
| `SCM_OBS_LOG_CONSOLE_ENABLED` | لاگ کنسول | فقط در profile `dev` باید روشن باشد. در test، pilot و prod خاموش است. |
| `SCM_OBS_LOG_FILE_ENABLED` | لاگ فایل | روشن یا خاموش بودن فایل log را مشخص میکند. خاموش بودن اشتباه باعث از دست رفتن لاگ عملیاتی میشود. |
| `SCM_OBS_TRACE_ENABLED` | trace برنامه | به صورت پیشفرض `true` است. اگر `false` شود trace کلی خاموش میشود. |
| `SCM_OBS_TRACE_FILE_ENABLED` | فایل trace | مقصد فایل trace به صورت پیشفرض فعال است، اما فقط وقتی global observation و خود سیگنال trace فعال باشند خروجی تولید میکند. |
| `SCM_OBS_AUDIT_ENABLED` | audit | روشن یا خاموش بودن audit را کنترل میکند. audit باید جدا از application log باقی بماند. |

## 7. متغیرهای اتصال به Config Server

| کلید | عنوان | توضیحات |
|---|---|---|
| `SCM_CONFIG_SERVER_URL` | آدرس Config Server | آدرس Config Server است. در test/pilot/prod اگر اشتباه باشد سرویس config را دریافت نمیکند. |
| `SCM_LABEL` | label کانفیگ | label مربوط به Spring Cloud Config است و میتواند branch، tag یا commit باشد. |
| `SCM_CONFIG_USERNAME` | کاربر Config Server | نام کاربری Config Server است و حساس محسوب میشود. نباید در ticket، screenshot یا chat ارسال شود. |
| `SCM_CONFIG_PASSWORD` | رمز Config Server | رمز Config Server است و حساس است. در pilot/prod باید از Secret Management تأمین شود. |
| `SCM_CONFIG_FAIL_FAST` | توقف هنگام خطا | در pilot/prod باید باعث توقف سرویس هنگام نبود Config Server شود تا برنامه با config ناقص بالا نیاید. |

## 8. مواردی که نباید دوباره استفاده شوند

| کلید | عنوان | توضیحات |
|---|---|---|
| `ib4dev,ib4test,scm4dev,scm4test,mb4dev,mb4test,pwa4dev,pwa4test` | profileهای قدیمی | این profileها نباید دوباره ساخته یا فعال شوند. به جای آنها از profile و متغیر کانال استفاده میشود. |
| `متغیر جداگانه issuer` | متغیر حذف شده | نباید در profileها تعریف شود. issuer از `SCM_UAA_BASE_URL` ساخته میشود. |
| `متغیر جداگانه JWK` | متغیر حذف شده | نباید در profileها تعریف شود. JWK از `SCM_UAA_BASE_URL` و مسیر `/oauth2/jwks` ساخته میشود. |
| `scm.log.app.*` | تنظیم قدیمی logback | مسیرهای log باید از `scm.observation.*` و `SCM_OBS_ROOT_DIR` بیایند. |
| `scm.logging.datasource.*` | دیتابیس logging مستقیم | این سرویس نباید مستقیم به دیتابیس legacy logging وصل شود. log، trace و audit باید از مسیر observation بروند. |
| `scm.datasource.secondary.*` | دیتابیس ثانویه | نباید برای `scm-web` تعریف شود مگر قرارداد جدید و مستند اضافه شود. |

## 9. نکات خاص هر محیط

| کلید | عنوان | توضیحات |
|---|---|---|
| `dev` | محیط توسعه | Config Server خاموش است و مقدارها از `application-dev.yml` خوانده میشوند. |
| `test` | محیط تست | مقدارها از Config Server میآیند و باید به سرویسهای تست اشاره کنند. |
| `pilot` | محیط پایلوت | مقدارهای حساس باید از Secret یا تنظیمات امن محیط بیایند و نباید hardcode شوند. |
| `prod` | محیط عملیاتی | اگر Config Server یا Secretها در دسترس نباشند، سرویس نباید با config ناقص بالا بیاید. |

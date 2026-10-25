# مطالعه — اپلیکیشن ثبت مطالعه با تقویم شمسی

اپ اندروید بومی (Kotlin + Jetpack Compose) برای ثبت جلسه‌های مطالعه روی **تقویم شمسی (جلالی)**.
طراحی با تم **سفید + صورتی** و زبان بصری **macOS جدید (Liquid Glass)**: سطوح نیمه‌شفاف،
گوشه‌های نرم، سایه‌های محو صورتی و فونت وزیرمتن.

## امکانات

| بخش | توضیح |
|---|---|
| **امروز** | حلقهٔ پیشرفت هدف روزانه (پیش‌فرض ۲ ساعت)، مجموع دقیقه‌های امروز، نمودار ۷ روز گذشته، فهرست جلسه‌های امروز و حذف هر جلسه |
| **تقویم** | تقویم شمسی ماه‌به‌ماه (شنبه‌اول)، نقطهٔ صورتی روی روزهای دارای مطالعه، انتخاب روز، رفتن به امروز، نمایش جلسه‌های همان روز |
| **آمار** | مجموع مطالعه، روزهای فعال، روزهای پیاپی (streak)، میانگین روزانه، تفکیک بر اساس درس، بهترین روز |
| **ثبت جلسه** | دیالوگ شیشه‌ای با «درس/موضوع» + «مدت» و دکمه‌های سریع ۱۵/۳۰/۴۵/۶۰/۹۰/۱۲۰ دقیقه |

داده‌ها محلی و روی خود دستگاه (SharedPreferences + JSON) ذخیره می‌شوند؛ اپ به اینترنت نیاز ندارد.

## اجرا و ساخت

```bash
# نیازمندی: JDK 17 و Android SDK (platform 34 + build-tools 34.0.0)
# مسیر SDK را در local.properties بگذارید:  sdk.dir=/path/to/Android/sdk

./gradlew :app:testDebugUnitTest   # تست‌های تقویم شمسی روی JVM
./gradlew :app:assembleDebug       # ساخت APK دیباگ
# خروجی: app/build/outputs/apk/debug/app-debug.apk
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

APK آمادهٔ نصب (دیباگ) در [`dist/motalee-debug.apk`](dist/motalee-debug.apk) هم موجود است:

```bash
adb install -r dist/motalee-debug.apk
```

یا پروژه را با **Android Studio** باز کنید (File ▸ Open) و Run بزنید.

## ساختار پروژه

```
app/src/main/java/com/example/motalee/
├── MainActivity.kt              # تک‌اکتیویتی + چیدمان کلی، تب‌ها، دیالوگ
├── data/StudyRepository.kt      # ذخیره/بازیابی جلسه‌ها با JSON در SharedPreferences
├── model/
│   ├── JalaliDate.kt            # تبدیل دقیق شمسی ↔ میلادی (پورت الگوریتم jalaali-js)
│   ├── StudySession.kt          # مدل یک جلسهٔ مطالعه
│   └── Fa.kt                    # ارقام فارسی، قالب‌بندی دقیقه/ساعت
└── ui/
    ├── StudyViewModel.kt        # state و کوئری‌های آماری (بدون وابستگی به UI)
    ├── TodayScreen.kt / CalendarScreen.kt / StatsScreen.kt
    ├── AddSessionDialog.kt
    ├── components/              # GlassCard، ProgressRing، GlassTabBar، SessionRow…
    └── theme/Theme.kt           # پالت سفید/صورتی، تایپوگرافی وزیرمتن، شکل‌ها
app/src/test/java/com/example/motalee/model/JalaliDateTest.kt
```

## تقویم شمسی

تبدیل تاریخ با ریاضی صحیح (بدون کتابخانهٔ اضافه) پیاده شده و با تست واحد اعتبارسنجی می‌شود:

* ۸ تاریخ مرجع (نوروز ۱۴۰۰/۱۴۰۳/۱۴۰۵، ۲۲ بهمن ۱۳۵۷، …) در هر دو جهت
* رفت‌وبرگشت **هر روزِ** یک بازهٔ ۲۰ ساله
* طول ماه‌ها، سال کبیسه (۱۴۰۳ کبیسه، ۱۴۰۴ نه)، شنبه‌اول بودن هفته و ارقام فارسی

## تست‌ها (۲۳ تست، بدون نیاز به امولاتور)

```bash
./gradlew :app:testDebugUnitTest
```

| فایل | تعداد | چه چیزی را تضمین می‌کند |
|---|---|---|
| `JalaliDateTest` | ۷ | تبدیل دوطرفهٔ تاریخ شمسی/میلادی، ۷۳۰۵ روز رفت‌وبرگشت، کبیسه‌ها، شنبه‌اول، ارقام فارسی |
| `StudyViewModelTest` | ۱۳ | افزودن/حذف جلسه، ذخیره‌سازی، آمار هفته، streak، بهترین روز، ناوبری ماه‌ها |
| `MainActivityTest` | ۳ | بالا آمدن واقعی Activity با **Robolectric**، رندر سه تب، و فلو کامل «افزودن جلسه → دیده‌شدن در امروز/تقویم/آمار» |

> همین تست‌ها یک کرش واقعی را پیدا کردند: `AppBackground` برای هاله‌های پس‌زمینه از
> `padding` با مقدار منفی استفاده می‌کرد و اپ **در لحظهٔ باز شدن** با
> `IllegalArgumentException: Padding must be non-negative` کرش می‌کرد.
> با `Modifier.offset` جایگزین شد و تست رگرسیون دارد.

## نکات فنی

* `minSdk = 26` تا از `java.time` بدون desugaring استفاده شود؛ `compileSdk/targetSdk = 34`
* Compose BOM `2024.06.00`، Kotlin `1.9.24`، AGP `8.5.2`، Gradle `8.7`
* لایهٔ UI فقط از `StudyViewModel` می‌خواند؛ ذخیره‌سازی قابل تعویض با Room بدون تغییر UI است
* متن‌های رابط کاربری فعلاً داخل کدِ Compose هستند؛ برای چندزبانه‌کردن کافی است به `strings.xml` منتقل شوند

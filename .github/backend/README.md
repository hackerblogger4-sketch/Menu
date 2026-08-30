# Lagan QR MVP

Multi-restoran QR buyurtma tizimi:

- `backend/` — REST API, Socket.IO real-time, SQLite, Telegram xabarlari.
- `web-menu/` — QR orqali ochiladigan mijoz HTML menyu.
- `android-receiver/` — buyurtma qabul qiluvchi Android APK manbasi.

## Tez ishga tushirish

1. `backend/.env.example` faylini `backend/.env` qilib nusxalang va qiymatlarni kiriting.
2. `cd backend`, `npm install`, keyin `npm start`.
3. Brauzerda `http://localhost:3000/r/lagan/menu?table=1` manzilini oching.
4. Android ilovada restoran slug'i `lagan`, API URL esa server manzili bo‘lsin.

Android emulyatorida standart URL `http://10.0.2.2:3000/`. Haqiqiy qurilma yoki release build uchun URLni quyidagicha bering: `gradle assembleDebug -PAPI_BASE_URL=https://api.sizning-domeningiz.uz/`. Ishlab chiqarishda faqat HTTPS ishlating.

## GitHub Actions

`.github/workflows/build.yml` backend testlarini va debug APKni yig‘adi. Android build natijasi Actions artifact sifatida yuklab olinadi.

## Render'ga joylash

1. Repository'ni GitHub'ga yuboring va Render'da **New → Blueprint** tanlang.
2. Repository'ni ulang; Render `render.yaml` faylini o‘qiydi.
3. `DEFAULT_ADMIN_PIN`, `TELEGRAM_BOT_TOKEN` va `TELEGRAM_CHAT_ID` qiymatlarini Render dashboardida kiriting.
4. Deploydan keyin berilgan `https://...onrender.com/` manzilini GitHub `API_BASE_URL` variable qiymatiga yozing va Android APKni qayta build qiling.

`/health` manzili `{"ok":true}` qaytarsa server tayyor. Mijoz menyusi: `https://SERVER-URL/r/lagan/menu?table=1`.

Render Persistent Disk SQLite buyurtma bazasini saqlash uchun kerak. Diskni o‘chirib yubormang.

Boshlang‘ich admin PIN: `.env` dagi `DEFAULT_ADMIN_PIN` (namunada `1234`). Ishlab chiqarishda uni albatta almashtiring.

## API qisqacha

- `GET /api/restaurants/:slug/menu` — mijoz menyusi va stollar.
- `POST /api/restaurants/:slug/orders` — HTML menyudan yangi buyurtma.
- `POST /api/auth/pin` — admin PIN kirishi.
- `GET /api/restaurants/:slug/tables` — APK stollar ro‘yxati.
- `GET /api/tables/:id/orders/active` — stolning faol buyurtmalari.
- `POST /api/orders/:id/seen` — buyurtmani ko‘rilgan qilish.
- `POST /api/admin/tables` — stol qo‘shish.
- `PATCH /api/admin/tables/:id` — stolni tartiblash/yangilash.
- `GET /api/admin/reports/orders.xlsx?from=2026-08-01&to=2026-08-31` — PIN bilan kirgandan keyin Excel hisobot yuklash.

Har restoran Socket.IO `restaurant:<slug>` xonasiga ulanadi. Hodisalar: `order:new`, `order:seen`, `tables:changed`.

## Excel va Telegram hisobotlari

Buyurtma tarixi bazada saqlanadi va admin Excel faylini xohlagan vaqtda yuklay oladi. `REPORT_CRON=55 23 * * *`, `REPORT_TIMEZONE=Asia/Tashkent`, `REPORT_PERIOD=daily` bo‘lsa, Telegram guruhiga har kuni 23:55 da Excel hisoboti yuboriladi. Oylik hisobot uchun `REPORT_PERIOD=monthly` va `REPORT_CRON=5 0 1 * *` qiling; bunda avvalgi oy yuboriladi.

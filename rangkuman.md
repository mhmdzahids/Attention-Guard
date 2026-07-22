# Rangkuman Pengembangan - Attention Guard

Berikut adalah daftar pekerjaan yang telah berhasil diimplementasikan (Selesai) dan catatan perbaikan terbaru untuk proyek digital wellbeing Attention Guard.

---

## 1. Fitur Utama yang Telah Berhasil Dibuat (Selesai)

*   **Penyambungan Sensor Sistem Riil**:
    *   Mengintegrasikan `UsageStatsManager` untuk mengambil durasi waktu nyata penggunaan harian aplikasi target: **TikTok** (mendukung package global dan regional SE Asia/Indonesia: `com.ss.android.ugc.aweme`), **YouTube**, dan **Instagram**.
    *   Mengintegrasikan `AccessibilityEvent` untuk mendeteksi gestur usapan layar guna menghitung kecepatan scroll (`px/sec`) dan skip rate (persentase video yang dilompati dengan usapan cepat).
    *   Mengintegrasikan `UsageEvents` untuk melacak frekuensi perpindahan antar-aplikasi (*inter-app switching*) per jam.
*   **Pencegahan Gangguan Gestur & Penyelarasan Sensor**:
    *   **Filter Paket Eksklusif**: Accessibility Service hanya merekam gerakan usapan di dalam aplikasi target (TikTok, Instagram, YouTube) dan secara otomatis mengabaikan guliran layar di luar itu (seperti saat berada di dalam menu setelan atau dashboard Attention Guard sendiri).
    *   **Peralihan Rata-rata Kumulatif (Stable EMA)**: Mengubah pencatatan kecepatan gulir instan menjadi kecepatan rata-rata kumulatif harian agar API Score stabil dan tidak fluktuatif/melompat turun saat ponsel sedang didiamkan.
*   **Penyelarasan & Akurasi Grafik (Chart Alignment)**:
    *   Menyelaraskan sumbu horizontal diagram hourly dengan label waktu `12 AM`, `6 AM`, `12 PM`, `6 PM`, `12 AM` secara kronologis.
    *   Membuat indikator titik bulat puncak (*peak activity marker*) secara vertikal lurus di atas label waktu yang tepat.
*   **Efek Animasi Premium (Meta Design System)**:
    *   **Animasi Bagan (Insights Chart)**: Grafik meluncur naik secara halus saat halaman dimuat, serta menyusut ke tanah kemudian tumbuh tegak kembali secara bertahap saat berganti jenis bagan (Hourly $\leftrightarrow$ Weekly). Lingkaran penanda puncak ikut membesar (*pop-up*) secara elastis.
    *   **Animasi Indikator Home (Circular API Gauge)**: Busur lingkaran API Score dan pencatat angka digital di tengahnya berputar serta menghitung naik (*counting up*) secara halus sejak halaman dibuka.
    *   **Animasi Progress Bar**: Bar pengisi *Session Dynamics* dan detail kontribusi aplikasi mengisi maju secara perlahan dari kiri ke kanan saat tab dibuka.
*   **Bottom Navigation Bar Kustom**:
    *   Membuat bilah menu bawah yang kustom sesuai gambar referensi: berlatar belakang putih, garis hairline pemisah tipis di atas, dan lencana oval aktif abu-abu lembut (`SurfaceSoft`) yang membungkus ikon serta teks label secara utuh untuk 5 tab menu (Today, Insights, Alerts, Meditate, Profile).
*   **Utilitas Diagnostik**:
    *   Lencana info sumber data (`Source: Simulated Data` vs `Source: Real-world Database`).
    *   Tombol "Seed 8 Hourly Logs" untuk menyemai log acak guna menguji keakuratan grafik database lokal Room.

---

## 2. Catatan Kerja Terperinci & Perbaikan Bug (Rabu, 22 Juli 2026)

Hari ini telah diselesaikan serangkaian optimalisasi kode dan perbaikan bug arsitektural untuk menstabilkan sensor, menyelaraskan UI dengan Digital Wellbeing Android, dan merestrukturisasi sistem notifikasi peringatan.

### Laporan Perbaikan Berdasarkan Garis Waktu (Timeline)

#### 🕒 Pukul 10:00 - 10:30 WIB | Crash Fix & NaN Safeguards
*   **Masalah**: Slider di menu Settings mendadak mengalami *crash* akibat penyebaran nilai `Float.NaN` dari Accessibility Service. Ini disebabkan oleh perhitungan jarak scroll (`dy * dy + dx * dx`) yang mengalami integer overflow menjadi negatif sehingga `Math.sqrt` mengembalikan `NaN`.
*   **Perbaikan**: 
    *   Mengonversi variabel jarak ke tipe `Long` di [AttentionAccessibilityService.kt](file:///c:/Users/Zahid/Attention-Guard/app/src/main/java/com/attentionguard/service/AttentionAccessibilityService.kt) sebelum dikalikan untuk mencegah overflow.
    *   Menambahkan fungsi pelindung `.isNaN()` dan `.isInfinite()` di dalam [AttentionMonitoringService.kt](file:///c:/Users/Zahid/Attention-Guard/app/src/main/java/com/attentionguard/service/AttentionMonitoringService.kt) dan [SettingsScreen.kt](file:///c:/Users/Zahid/Attention-Guard/app/src/main/java/com/attentionguard/ui/screens/SettingsScreen.kt) untuk memaksa data kembali ke nilai default/baseline jika terdeteksi tidak valid.

#### 🕒 Pukul 10:30 - 11:30 WIB | Sinkronisasi UI & Penyusunan Struktur DB Terpadu
*   **Masalah**: Rata-rata pada card *Session Dynamics* dan data breakdown aplikasi di tab Insights tidak berubah (statis) walau grafik tren berubah total dan log database bertambah.
*   **Perbaikan**: 
    *   Menyatukan seluruh *state* di [InsightsScreen.kt](file:///c:/Users/Zahid/Attention-Guard/app/src/main/java/com/attentionguard/ui/screens/InsightsScreen.kt) agar diturunkan secara terpadu dari database Room (`todayLogs` dan `latestLog`) saat dalam mode real-world.
    *   Mengaktifkan penulisan log sensor aktif secara berkala setiap 2 menit di [AttentionMonitoringService.kt](file:///c:/Users/Zahid/Attention-Guard/app/src/main/java/com/attentionguard/service/AttentionMonitoringService.kt) sehingga penambahan log berjalan secara konsisten seiring jalannya waktu.

#### 🕒 Pukul 17:10 - 17:35 WIB | Sinkronisasi Screen Time Riil (Digital Wellbeing Alignment)
*   **Masalah**: Durasi penggunaan TikTok/YouTube di aplikasi sangat kecil (misal: 37 menit) dibandingkan catatan Digital Wellbeing Android yang asli (3 jam 12 menit). Hal ini disebabkan oleh delay cache penulisan database `UsageStatsManager` dari OS Android pada perangkat OEM.
*   **Perbaikan**:
    *   Membangun ulang logika pencatatan durasi di `queryMetricsDirectly()` dengan mem-parser aliran data mentah **`UsageEvents`** secara dinamis.
    *   Melacak selisih milidetik dari stempel waktu event `ACTIVITY_RESUMED` dan `ACTIVITY_PAUSED` secara real-time. Ini secara instan mem-bypass delay cache sistem operasi dan menghasilkan data durasi yang akurat.

#### 🕒 Pukul 17:35 - 18:00 WIB | Resolusi Leakage Durasi & Kalibrasi Metrik Interaksi
*   **Masalah**: 
    1.  *Leakage Durasi*: Durasi YouTube dan TikTok melonjak drastis dan anjlok bersamaan. Ini disebabkan oleh penanganan sesi gantung (*unmatched event*) yang mengasumsikan setiap aplikasi tanpa event pause dianggap masih berjalan di foreground.
    2.  *Scroll Speed & Skip Rate Tidak Wajar*: Kecepatan scroll melesat hingga 2356 px/s dan Skip Rate tertahan di 100%.
*   **Perbaikan**:
    *   **Penyaringan Sesi Aktif**: Melacak `lastResumedPackage` di [AttentionMonitoringService.kt](file:///c:/Users/Zahid/Attention-Guard/app/src/main/java/com/attentionguard/service/AttentionMonitoringService.kt). Sesi gantung hanya dihitung berjalan jika ia merupakan aplikasi terakhir yang dibuka dan layar dalam keadaan aktif. Sesi gantung dari aplikasi non-aktif diabaikan.
    *   **Stabilisasi Kecepatan Gulir**: Membatasi interval pembagi minimal ke 80ms dan melakukan *clamping* kecepatan gulir maksimal di angka 1000 px/s pada [AttentionAccessibilityService.kt](file:///c:/Users/Zahid/Attention-Guard/app/src/main/java/com/attentionguard/service/AttentionAccessibilityService.kt).
    *   **Penyaringan Skip Rate Selektif**: Skip rate dikalibrasi ulang hanya mendeteksi gerakan fling cepat berkecepatan tinggi (> 450 px/s), memisahkannya dari gerakan membaca biasa.
    *   **Outlier Filter**: Ditambahkan saringan pengabaian data rusak dari database lama (>1000 px/s) di UI [InsightsScreen.kt](file:///c:/Users/Zahid/Attention-Guard/app/src/main/java/com/attentionguard/ui/screens/InsightsScreen.kt).

#### 🕒 Pukul 18:00 - 18:15 WIB | Restrukturisasi Sistem Alerts (Pemberantasan Spam Jitter)
*   **Masalah**: Tab Alerts mendaftarkan log rutin per 2 menit sebagai alert baru sehingga membanjiri daftar alert, serta memicu teks "Late-Night" di siang hari.
*   **Perbaikan**:
    *   **Unifikasi Database Alert (`is_alert_event`)**: Menambahkan kolom `is_alert_event: Boolean` pada entitas Room [AttentionLog.kt](file:///c:/Users/Zahid/Attention-Guard/app/src/main/java/com/attentionguard/data/AttentionLog.kt) dan menaikkan versi skema database ke **version `2`** di [AppDatabase.kt](file:///c:/Users/Zahid/Attention-Guard/app/src/main/java/com/attentionguard/data/AppDatabase.kt).
    *   **Saringan Cooldown 10 Menit**: Menambahkan pembatas waktu (*cooldown*) 10 menit di Service sebelum menetapkan log baru sebagai alert. Ini meniadakan peringatan berulang (*flip-flop/jitter*) saat skor berosilasi tipis di garis batas risiko (0.35 / 0.65).
    *   **Custom Context-Aware Messages**: Judul dan deskripsi alert kini dievaluasi secara dinamis dari sensor yang memiliki beban kontribusi terbesar (misal: memicu alert berpindah aplikasi `"High Task-Switching Alert"` di siang hari secara cerdas, dan `"Late-Night Scroll Alert"` hanya saat sensor malam mendominasi).
    *   **Room DB Limit**: Membatasi query database `getAllLogsFlow` ke `LIMIT 150` log terbaru untuk menjamin efisiensi memori jangka panjang.

---

## 3. Hal-hal yang Dapat Disempurnakan di Masa Mendatang (Future Improvements)

*   **Pembersihan Log Database Berkala**: Mekanisme penghapusan log lokal Room yang berusia lebih dari 30 hari secara otomatis melalui WorkManager.
*   **Model CNN-LSTM On-Device**: Pengeksporan model deep learning CNN-LSTM berbasis TensorFlow Lite (TFLite) untuk dijalankan secara lokal di perangkat guna mendeteksi status atensi kognitif runut-waktu secara cerdas.
*   **Analisis Sub-Activity Aplikasi**: Membedakan kelas layar jendela (Accessibility Window) untuk mendeteksi secara presisi perbedaan antara menonton video panjang YouTube biasa dengan video pendek YouTube Shorts.

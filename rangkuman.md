# Rangkuman Pengembangan - Attention Guard

Berikut adalah daftar pekerjaan yang telah berhasil diimplementasikan (Selesai) dan daftar hal-hal yang dapat terus disempurnakan di masa mendatang (Future Improvements) untuk proyek digital wellbeing Attention Guard:

---

## 1. Fitur yang Telah Berhasil Dibuat (Selesai)

*   **Penyambungan Sensor Sistem Riil**:
    *   Mengintegrasikan `UsageStatsManager` untuk mengambil durasi waktu nyata penggunaan harian aplikasi target: **TikTok** (mendukung package global dan regional SE Asia/Indonesia: `com.ss.android.ugc.aweme`), **YouTube**, dan **Instagram**.
    *   Mengintegrasikan `AccessibilityEvent` untuk mendeteksi gestur usapan layar guna menghitung kecepatan scroll (`px/sec`) dan skip rate (persentase video yang dilompati dengan usapan cepat di atas 200px).
    *   Mengintegrasikan `UsageEvents` untuk melacak frekuensi perpindahan antar-aplikasi (*inter-app switching*) per jam.
*   **Pencegahan Gangguan Gestur & Penyelarasan Sensor**:
    *   **Filter Paket Eksklusif**: Accessibility Service sekarang hanya merekam gerakan usapan di dalam aplikasi target (TikTok, Instagram, YouTube) dan secara otomatis mengabaikan guliran layar di luar itu (seperti saat berada di dalam menu setelan atau dashboard Attention Guard sendiri).
    *   **Peralihan Rata-rata Kumulatif (Stable EMA)**: Mengubah pencatatan kecepatan gulir instan menjadi kecepatan rata-rata kumulatif harian agar API Score stabil (misalnya kokoh pada tingkat `0.70`) dan tidak fluktuatif/melompat turun ke `0.50` saat ponsel sedang didiamkan.
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

## 2. Hal-hal yang Belum / Dapat Disempurnakan (Future Improvements)

*   **Pembersihan Log Database Berkala**:
    *   Log tersimpan di database lokal Room secara berkala setiap jam melalui `WorkManager`. Untuk mencegah database membengkak dalam jangka panjang, perlu ditambahkan mekanisme *clean-up* otomatis untuk menghapus entri logs yang usianya sudah lebih dari 30 hari.
*   **Kalibrasi Algoritma CNN-LSTM**:
    *   Saat ini perhitungan API Score masih didasarkan pada rumus pembobotan statis linier:
        $$\text{API Score} = 0.30 \cdot N_{\text{session}} + 0.20 \cdot N_{\text{scroll}} + 0.30 \cdot N_{\text{switch}} + 0.20 \cdot N_{\text{night}}$$
        Untuk menyempurnakan klasifikasi kognitif yang sebenarnya, model CNN-LSTM berbasis TensorFlow Lite (TFLite) dapat diekspor dan dijalankan langsung di perangkat untuk menyaring data runut-waktu mentah ini secara cerdas.
*   **Pengenalan Sub-Aplikasi yang Lebih Detail**:
    *   Saat ini durasi dihitung per paket aplikasi (misal, seluruh aplikasi YouTube). Pengenalan bisa disempurnakan dengan menganalisis sub-activity kelas jendela layar (Accessibility Window) untuk membedakan secara spesifik antara menonton video panjang YouTube biasa dengan menonton video pendek YouTube Shorts.
*   **Dukungan Multi-bahasa (Localization)**:
    *   Penerjemahan teks antarmuka dan dashboard ke bahasa lokal (Bahasa Indonesia) secara menyeluruh agar konsisten dengan panduan penelitian lokal.

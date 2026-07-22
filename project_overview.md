# Project Overview & Team Structure: Attention Guard 🛡️

Dokumen ini mendeskripsikan ringkasan proyek, cakupan pengembangan (*project scope*), serta pembagian peran dan tanggung jawab (*job descriptions*) bagi tim pengembangan **Attention Guard**. Tujuannya adalah memberikan peta jalan (*roadmap*) dan pembagian kerja yang jelas agar riset dan produk dapat berjalan beriringan secara terstruktur.

---

## 1. Ringkasan Proyek (Project Overview)

**Attention Guard** adalah sebuah framework kesejahteraan digital (*digital wellbeing*) pasif berbasis Android yang bertujuan untuk mendeteksi kelelahan perhatian (*attention fragmentation*), distraksi kognitif, dan perilaku scrolling kompulsif (*doomscrolling*) secara real-time. 

Aplikasi ini menggunakan sensor latar belakang (*passive sensing*) seperti Accessibility Service dan Usage Stats untuk memantau indikator perilaku pengguna (kecepatan scroll, pergantian aplikasi, dan durasi penggunaan). Data ini diolah untuk menghasilkan **API Score (Attention Performance Indicator)** yang diklasifikasikan ke dalam 3 tingkat risiko (Low, Moderate, High). Ketika risiko meningkat, sistem akan memberikan intervensi berupa dorongan halus (*micro-nudge*), sesi meditasi, atau game latihan fokus untuk memulihkan kapasitas perhatian pengguna.

---

## 2. Cakupan Proyek (Project Scope)

Pengembangan Attention Guard dibagi menjadi beberapa modul utama:

```
  +-----------------------------------------------------------------+
  |                        ATTENTION GUARD                          |
  +-----------------------------------------------------------------+
                                   |
         +-------------------------+-------------------------+
         |                         |                         |
         v                         v                         v
+------------------+     +------------------+     +------------------+
|   Core App &     |     |    AI Engine     |     |   Focus Games    |
|   Telemetry      |     |    CNN-LSTM      |     |  Honing System   |
+------------------+     +------------------+     +------------------+
| - Accessibility  |     | - Preprocessing  |     | - Unity/Compose  |
| - UsageStats DB  |     | - CNN-LSTM Train |     | - Focus Training |
| - Interventions  |     | - TFLite Export  |     | - Score Logging  |
| - Room Database  |     | - Inference Pipeline|  | - Stress Relief  |
+------------------+     +------------------+     +------------------+
```

1. **Modul Telemetri & Data Latar Belakang (Passive Sensing)**: Mengumpulkan data perilaku interaksi mikro-pengguna secara real-time.
2. **Modul Klasifikasi Risiko AI**: Menganalisis data deret waktu (*time-series sequences*) untuk memprediksi apakah pengguna sedang fokus atau terdistraksi secara kompulsif.
3. **Modul Intervensi & Gamifikasi**: Memberikan intervensi interaktif berupa game yang melatih kembali kontrol kognitif pengguna ketika konsentrasi mereka terfragmentasi.
4. **Modul Dashboard & Insights**: Menampilkan analisis visual performa perhatian harian dan mingguan kepada pengguna (termasuk fitur *pinch-to-zoom* detail jam).

---

## 3. Pembagian Kerja & Job Descriptions (Tim 3 Orang)

Untuk menyukseskan proyek ini, pekerjaan dibagi secara spesifik berdasarkan keahlian masing-masing peran:

### Peran 1: Fullstack Developer (Anda)
Fokus utama Anda adalah membangun fondasi aplikasi Android, antarmuka pengguna (UI/UX), pemrosesan sensor sistem Android, penyimpanan data lokal, dan mengintegrasikan modul AI serta modul Game ke dalam satu kesatuan aplikasi.

*   **Tanggung Jawab Utama**:
    *   **Android Telemetry**: Mengembangkan dan memelihara `AttentionAccessibilityService` (untuk melacak kecepatan gulir dan skip rate) serta mengintegrasikan `UsageStatsManager` dan `ActivityManager`.
    *   **UI/UX & Core Engine**: Membangun tampilan utama (Dashboard hari ini, Insights pola perilaku dengan grafik zoomable, Alerts history, dan layar Meditasi) menggunakan Jetpack Compose.
    *   **Local Database**: Mengelola database Room (`AppDatabase`) untuk menyimpan log perhatian secara efisien.
    *   **Integration Lead**:
        *   Mengintegrasikan model kecerdasan buatan (format TensorFlow Lite atau PyTorch Mobile) hasil kerja **AI Engineer** ke dalam pipeline deteksi latar belakang (`AttentionCalculationWorker`).
        *   Mengintegrasikan game pengasah fokus buatan **Game Developer** ke dalam layar meditasi atau overlay intervensi (Prevention Plan).
    *   **Deployment**: Mengelola konfigurasi build Gradle, izin (*permissions*) sistem Android, dan instalasi debugging pada perangkat fisik.

---

### Peran 2: AI Engineer (Teman 1)
Fokus utama peran ini adalah melatih, mengevaluasi, dan mengekspor model kecerdasan buatan (CNN-LSTM) yang mampu mendeteksi penurunan konsentrasi pengguna berdasarkan riset paper Anda.

*   **Tanggung Jawab Utama**:
    *   **Model Research & Design**: Menerjemahkan metodologi pada riset paper Anda ke dalam arsitektur model AI **CNN-LSTM** (CNN untuk ekstraksi fitur spasial interaksi mikro, LSTM untuk analisis ketergantungan temporal jangka panjang).
    *   **Data Preprocessing Pipeline**: Merancang transformasi data deret waktu mentah dari sensor (kecepatan scroll, durasi sentuhan, transisi aplikasi) menjadi format tensor masukan yang siap diklasifikasi oleh model.
    *   **Model Training & Tuning**: Melatih model menggunakan dataset (baik lokal maupun publik yang relevan) dan mengoptimalkan metrik evaluasi (F1-score, Akurasi, dan Latensi inferensi).
    *   **Export & Optimization**: Mengonversi model yang sudah terlatih menjadi format yang ramah perangkat seluler (seperti **TensorFlow Lite - `.tflite`** atau **PyTorch Mobile**) dengan teknik kuantisasi agar inferensi berjalan cepat dan hemat baterai di Android.
    *   **Integration Support**: Bekerja sama dengan Anda untuk memastikan API inferensi model menerima masukan sensor yang cocok dengan format latihannya.

---

### Peran 3: Game Developer (Teman 2)
Fokus utama peran ini adalah merancang dan mengembangkan mini-game interaktif yang dirancang khusus untuk melatih kapasitas fokus kognitif, ketahanan distraksi, dan koordinasi motorik pengguna.

*   **Tanggung Jawab Utama**:
    *   **Game Design for Attention**: Merancang gameplay yang secara ilmiah mengasah konsentrasi (misalnya: *sustained attention tasks*, *inhibitory control games* seperti memilah target dengan cepat tanpa terpancing distraktor, atau *working memory mini-games*).
    *   **Game Development**: Membangun mini-game menggunakan tool yang disepakati (misalnya Unity dengan integrasi Android library, Godot, atau canvas custom native di Android Jetpack Compose).
    *   **Feedback & Telemetry Integration**:
        *   Mengekspor game agar dapat dimuat dengan lancar di dalam aktivitas Android utama.
        *   Menghasilkan skor performa game (misalnya tingkat akurasi reaksi, durasi bermain, atau skor stabilitas perhatian) untuk dikirim kembali ke modul database utama milik Fullstack Developer.
    *   **User Engagement**: Memastikan estetika visual game memberikan rasa tenang (*mindfulness*) dan santai sebagai pereda stres akibat scrolling kompulsif (*anti-doomscrolling*).

---

## 4. Alur Kerja Kolaborasi & Integrasi

Agar integrasi berjalan mulus di akhir, tim direkomendasikan mengikuti protokol pertukaran data berikut:

1.  **Sensor Telemetri (Fullstack) -> Input Model AI (AI Engineer)**:
    Fullstack developer mencatat data periodik (misal per jendela waktu 1-5 menit) dan menyimpannya dalam database lokal. AI Engineer harus merancang model yang menerima tensor berbentuk `[Jendela Waktu, Jumlah Fitur Sensor]` (misalnya sequence 30 data points dengan fitur: session_duration, scroll_velocity, switch_freq, skip_rate).
2.  **Klasifikasi AI (AI Engineer) -> Pemicu Intervensi (Fullstack)**:
    Inferensi model akan menghasilkan output berupa probabilitas distraksi/kelelahan. Jika model mengeluarkan tingkat risiko **High**, Fullstack App akan memicu intervensi dengan memunculkan Overlay Prevention Plan.
3.  **Halaman Meditasi (Fullstack) -> Game Fokus (Game Developer)**:
    Pengguna dipandu masuk ke sesi game latihan fokus. Setelah game selesai, modul game memanggil fungsi callback (misal: `onGameFinished(score: Float, durationMs: Long)`) untuk mencatat performa pemulihan kognitif pengguna di database utama.

# Laporan Investigasi Ketidakcocokan Data Sensor Attention Guard

**Tanggal:** 27 Juli 2026  
**Status Service:** Sensor Active ("On")  
**Tujuan:** Investigasi & Analisis Akar Masalah (Tanpa Perubahan Kode)

---

## 1. Masalah 1: Total "Session Dynamics" Lebih Rendah dari Digital Wellbeing

### Deskripsi Masalah
Pada pengujian jam 14:16, aplikasi menampilkan `"Session Dynamics: 1h 26m Total"` (dengan label di atasnya bertuliskan `"Average usage duration today"`).  
Pada jam 14:18, Digital Wellbeing menunjukkan total pemakaian TikTok (1h 4m) + Instagram (49m) = **1h 53m** (belum termasuk YouTube). Terdapat selisih **~27 menit** (angka Attention Guard lebih rendah).

### Hasil Investigasi & Konfirmasi Hipotesis
> [!NOTE]
> **Konfirmasi Hipotesis:** **100% BENAR**

Nilai `activeSessionDuration` yang ditampilkan pada kartu *"Session Dynamics"* dihitung dengan melakukan `.average()` terhadap seluruh log hari ini (`todayLogs`). Namun, bidang `sessionDuration` pada setiap entry `AttentionLog` menyimpan **total waktu pemakaian kumulatif hari ini (sejak midnight 00:00)** hingga timestamp log tersebut dibuat.

Meng-average-kan angka kumulatif yang terus meningkat sepanjang hari secara matematis menarik nilai akhir ke bawah karena log-log awal hari (saat akumulasi pemakaian masih kecil) ikut dihitung dalam rata-rata.

### Bukti Kode & Kutipan File

1. **Perhitungan Rata-Rata Kumulatif di UI:**  
   [InsightsScreen.kt:L116-L126](file:///c:/Users/Zahid/Attention-Guard/app/src/main/java/com/attentionguard/ui/screens/InsightsScreen.kt#L116-L126)
   ```kotlin
   val activeSessionDuration = remember(todayLogs, sessionDuration, useSimulatedData) {
       if (!useSimulatedData) {
           if (todayLogs.isNotEmpty()) {
               todayLogs.map { it.sessionDuration }.average().toFloat() // <-- AKAR MASALAH: Menggunakan .average()
           } else {
               0f
           }
       } else {
           sessionDuration
       }
   }
   ```

2. **Perhitungan Waktu Kumulatif Harian (Sejak Midnight):**  
   [AttentionMonitoringService.kt:L300-L327](file:///c:/Users/Zahid/Attention-Guard/app/src/main/java/com/attentionguard/service/AttentionMonitoringService.kt#L300-L327) & [AttentionMonitoringService.kt:L395](file:///c:/Users/Zahid/Attention-Guard/app/src/main/java/com/attentionguard/service/AttentionMonitoringService.kt#L395)
   ```kotlin
   val startTime = ZonedDateTime.now(ZoneId.systemDefault())
       .truncatedTo(ChronoUnit.DAYS) // Midnight 00:00 hari ini
       .toInstant()
       .toEpochMilli()

   val dailyStats = usageStatsManager.queryAndAggregateUsageStats(startTime, endTime)
   // ... akumulasi durasi dari midnight ...
   val sessionHours = totalDailyMs.toFloat() / 3600000f // Total jam kumulatif hari ini
   ```

3. **Inkonsistensi Teks Label vs Nilai:**  
   [InsightsScreen.kt:L758-L760](file:///c:/Users/Zahid/Attention-Guard/app/src/main/java/com/attentionguard/ui/screens/InsightsScreen.kt#L758-L760) & [InsightsScreen.kt:L810](file:///c:/Users/Zahid/Attention-Guard/app/src/main/java/com/attentionguard/ui/screens/InsightsScreen.kt#L810)
   ```kotlin
   Text(text = "Average usage duration today", ...) // Label bertuliskan Average
   ...
   Text(text = "${formatUsageDuration(activeSessionDuration)} Total", ...) // Nilai diberi sufiks "Total"
   ```

### Rekomendasi Solusi Logic
* Mengganti logika `todayLogs.map { it.sessionDuration }.average()` menjadi mengambil **nilai log terbaru hari ini** (`todayLogs.maxByOrNull { it.timestamp }?.sessionDuration` atau `latestLog?.sessionDuration`).
* Catatan: `latestLog` sudah di-compute di [InsightsScreen.kt:L112-L114](file:///c:/Users/Zahid/Attention-Guard/app/src/main/java/com/attentionguard/ui/screens/InsightsScreen.kt#L112-L114) dan grafik mingguan di [InsightsScreen.kt:L257](file:///c:/Users/Zahid/Attention-Guard/app/src/main/java/com/attentionguard/ui/screens/InsightsScreen.kt#L257) sudah menggunakan `maxOf { it.sessionDuration }`.

---

## 2. Masalah 2: Grafik "Peak Activity" (Hourly) Rata Datar 9 Jam

### Deskripsi Masalah
Screenshot grafik pada jam 14:16 menunjukkan garis rata datar di level `"Mod"` dari jam 5 AM sampai jam 2 PM (9 jam) tanpa variasi. Padahal Digital Wellbeing menunjukkan pemakaian yang bursty (lonjakan di jam-jam tertentu dan kosong di jam lain).

### Hasil Investigasi & Konfirmasi Hipotesis
> [!IMPORTANT]
> **Konfirmasi Hipotesis:** **SEBAGIAN BENAR**  
> Mekanisme utamanya **bukan interpolasi/carry-forward** antar jam (karena `else -> 0.0f` untuk jam tanpa log sudah diterapkan di `InsightsViewModel.kt`), melainkan **penulisan log periodik oleh WorkManager yang menggunakan metrik kumulatif harian + nilai baseline bawaan**.

### Rincian Penyebab Grafik Flat

1. **Background Worker Menulis Log Tiap Jam:**  
   [MainActivity.kt:L74-L84](file:///c:/Users/Zahid/Attention-Guard/app/src/main/java/com/attentionguard/MainActivity.kt#L74-L84) me-schedule `AttentionCalculationWorker` setiap 1 jam. Worker ini memanggil `queryMetricsDirectly()` dan memasukkan log baru ke Room DB di [AttentionCalculationWorker.kt:L48-L57](file:///c:/Users/Zahid/Attention-Guard/app/src/main/java/com/attentionguard/service/AttentionCalculationWorker.kt#L48-L57) pada jam-jam sepi sekalipun.

2. **Perhitungan `apiScore` Menggunakan Metrik Kumulatif Harian (Bukan Delta Hourly):**  
   Di [AttentionMonitoringService.kt:L300-L450](file:///c:/Users/Zahid/Attention-Guard/app/src/main/java/com/attentionguard/service/AttentionMonitoringService.kt#L300-L450):
   * `session` dihitung dari 00:00 hingga saat ini (kumulatif harian).
   * `switches` dihitung dari `switchCount / elapsedHrs` (kumulatif rata-rata harian).
   * `scroll` menggunakan `averageVelocity` dengan nilai baseline `142f` ([AttentionAccessibilityService.kt:L89](file:///c:/Users/Zahid/Attention-Guard/app/src/main/java/com/attentionguard/service/AttentionAccessibilityService.kt#L89)).
   * Rumus API Score: `0.30 * N(session) + 0.20 * N(scroll) + 0.30 * N(switch) + 0.20 * N(night)`.
   * Untuk pemakaian normal sehari-hari, kombinasi metrik kumulatif ini **selalu menghasilkan nilai di kisaran 0.35 s/d 0.45 (Tier "Mod")**.

3. **Bucket Jam Selalu Terisi Log Berskor "Mod":**  
   Di [InsightsViewModel.kt:L97-L105](file:///c:/Users/Zahid/Attention-Guard/app/src/main/java/com/attentionguard/ui/viewmodel/InsightsViewModel.kt#L97-L105):
   ```kotlin
   val hourLogs = todayLogs.filter { it.timestamp in sTime..eTime }
   val apiScore = when {
       h > currentHour -> 0.0f
       hourLogs.isNotEmpty() -> hourLogs.map { it.apiScore }.average().toFloat().coerceIn(0.0f, 1.0f)
       else -> 0.0f
   }
   ```
   Karena background worker selalu menyimpankan log di setiap jam, `hourLogs.isNotEmpty()` **selalu bernilai true** untuk jam 5 AM - 2 PM, dan skor rata-ratanya konsisten di angka ~0.40.

4. **Penghubung Kurva Mulus (Cubic Bezier):**  
   Di [InsightsScreen.kt:L494-L503](file:///c:/Users/Zahid/Attention-Guard/app/src/main/java/com/attentionguard/ui/screens/InsightsScreen.kt#L494-L503), titik-titik jam yang bernilai identik ~0.40 ini dihubungkan dengan `cubicTo`, membentuk garis lurus rata di level "Mod".

### Rekomendasi Solusi Logic
* **Opsi A (Delta Hourly):** Mengubah perhitungan skor per jam agar menghitung durasi/aktivitas *delta* spesifik dalam rentang jam tersebut (`sTime..eTime`), bukan metrik kumulatif harian dari 00:00.
* **Opsi B (Kondisi Log Worker):** Mencegah worker menulis log saat tidak ada aktivitas aplikasi target dalam interval jam tersebut, atau memberikan penanda khusus untuk log idle.

---

## Matriks Ringkasan Evaluasi

| Masalah | File Code Utama | Status Hipotesis | Solusi yang Disarankan |
| :--- | :--- | :--- | :--- |
| **1. Session Dynamics Lower** | [InsightsScreen.kt:L119](file:///c:/Users/Zahid/Attention-Guard/app/src/main/java/com/attentionguard/ui/screens/InsightsScreen.kt#L119) | **100% BENAR** | Gunakan `latestLog?.sessionDuration` (nilai log paling akhir) alih-alih `.average()`. |
| **2. Peak Activity Hourly Flat** | [InsightsViewModel.kt:L103](file:///c:/Users/Zahid/Attention-Guard/app/src/main/java/com/attentionguard/ui/viewmodel/InsightsViewModel.kt#L103) & [AttentionCalculationWorker.kt:L48](file:///c:/Users/Zahid/Attention-Guard/app/src/main/java/com/attentionguard/service/AttentionCalculationWorker.kt#L48) | **SEBAGIAN BENAR** (Bukan interpolasi UI, tapi akibat log periodik worker yang berisi metrik kumulatif harian) | Hitung skor hourly berdasarkan delta aktivitas dalam interval jam tersebut atau batasi penulisan log idle. |

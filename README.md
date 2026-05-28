<div align="center">

<img src="Documentation/devspark_app_logo.png" alt="DevSpark Logo" width="100" style="border-radius: 20px; box-shadow: 0 4px 10px #05976A;" />

# ⚡ DevSpark

**Sistem Kasir Pintar (Point of Sale) Multi-Cabang & Real-Time Berbasis Android**

[![Kotlin](https://img.shields.io/badge/Kotlin-v2.2.10-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Android SDK](https://img.shields.io/badge/Android_SDK-Target_36-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com)
[![Firebase](https://img.shields.io/badge/Firebase-Realtime_DB-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)](https://firebase.google.com)
[![Material Design 3](https://img.shields.io/badge/Material_Design-3_v1.13.0-757575?style=for-the-badge&logo=materialdesign&logoColor=white)](https://m3.material.io)
[![Glide](https://img.shields.io/badge/Glide-v4.16.0-4285F4?style=for-the-badge&logo=google-chrome&logoColor=white)](https://github.com/bumptech/glide)

*Solusi kasir terpadu dan modern untuk mengelola transaksi penjualan, memantau multi-cabang bisnis, melacak inventori stok, dan menganalisis profitabilitas usaha secara instan.*

</div>

---

## 📋 Deskripsi

**DevSpark** adalah aplikasi **Point of Sale (POS) / Sistem Kasir** berbasis Android modern yang dirancang khusus untuk memenuhi kebutuhan bisnis retail, F&B, dan UMKM berskala multi-cabang. Dibangun menggunakan bahasa pemrograman **Kotlin** dan diintegrasikan dengan **Firebase Realtime Database** dan **Firebase Authentication**, DevSpark menyajikan sinkronisasi data yang instan, responsif, dan aman antara kasir di toko fisik dengan pemilik usaha.

Aplikasi ini menyederhanakan alur transaksi harian, mengotomatiskan pengelolaan stok barang, meminimalisir kesalahan manusia (*human error*), serta memberikan wawasan keuntungan (*profit analytics*) secara *real-time*. Dengan antarmuka berstandar **Material Design 3**, DevSpark menghadirkan pengalaman pengguna yang premium, estetik, dan interaktif.

---

## ✨ Fitur Unggulan Kompleks

Aplikasi DevSpark dilengkapi dengan serangkaian fitur tingkat lanjut yang dirancang untuk mendukung operasional bisnis secara menyeluruh:

### 1. 🔄 Sinkronisasi Transaksi & Pengurangan Stok Aman (*Concurrency-Safe*)
* Menggunakan fitur **Firebase Transactions (`runTransaction`)** untuk memperbarui kuantitas stok produk saat transaksi diselesaikan. Proses ini menjamin tidak terjadi *data race* (perselisihan jumlah stok) ketika beberapa kasir di cabang yang sama melakukan checkout produk yang sama secara bersamaan.
* Deteksi otomatis jenis produk tanpa batas (*unlimited*), membebaskan produk digital atau jasa dari pengurangan kuantitas stok fisik.

### 🏢 2. Arsitektur Multi-Cabang Terintegrasi (*Multi-Branch Scoping*)
* Seluruh entitas data utama seperti **Kategori**, **Produk/Menu**, **Pegawai (Kasir)**, dan **Pelanggan** di-scope secara spesifik berdasarkan ID Cabang (`idCabang`).
* Memungkinkan satu sistem basis data Firebase melayani banyak lokasi cabang sekaligus tanpa terjadi kebocoran data antar cabang. Pemilik bisnis dapat memantau seluruh cabang dari satu dasbor terpusat.

### 💰 3. Pelacakan Margin Keuntungan Otomatis (*Profit Tracking*)
* Sistem secara otomatis menghitung keuntungan kotor dan bersih secara dinamis pada setiap transaksi berdasarkan selisih harga jual dan harga beli dikalikan dengan kuantitas:
  $$\text{Keuntungan} = (\text{Harga Jual} - \text{Harga Beli}) \times \text{Qty}$$
* Membantu pemilik toko melihat margin keuntungan bersih secara langsung tanpa harus melakukan kalkulasi manual di akhir bulan.

### 🚨 4. Peringatan Stok Rendah & Notifikasi Pintar (*Smart Alert System*)
* Sistem memantau batas minimum stok barang secara *real-time*. Jika persediaan produk jatuh di bawah ambang batas kritis (stok < 5 unit), Firebase secara otomatis memicu generator notifikasi lokal untuk memperingatkan kasir agar segera melakukan *restock*.
* Riwayat aktivitas kasir direkam secara transparan di bawah sub-node `notifications` dan `histori` untuk keperluan audit operasional.

### 💳 5. Kalkulator Kasir Cepat & Multi Metode Pembayaran
* Dilengkapi antarmuka kalkulator kasir yang efisien dengan dukungan **Quick Cash Chips** (Uang Pas, Rp50.000, Rp100.000) untuk mempercepat transaksi tunai.
* Mendukung pencatatan berbagai metode pembayaran modern lainnya seperti **QRIS** (dilengkapi simulasi scan kode QR) dan **E-Wallet** terkemuka (GoPay, DANA, OVO, ShopeePay).

### 🖨️ 6. Pencetakan Struk / Nota Instan & Digital Receipt
* Setelah pembayaran dikonfirmasi, aplikasi menghasilkan struk belanja terstruktur yang dapat langsung dicetak menggunakan printer termal bluetooth atau disimpan sebagai bukti transaksi digital yang rapi.

### 🌓 7. Dual-Theme Engine (Dukungan Native Dark & Light Mode)
* Terintegrasi penuh menggunakan Android Material Components (`values-night`). Aplikasi mendukung perubahan tema secara dinamis mengikuti preferensi sistem operasi, memberikan kenyamanan mata kasir di kondisi pencahayaan rendah serta menghemat daya baterai perangkat OLED.

### 🌐 8. Bilingual Translation System (Bahasa Indonesia & Inggris)
* Didukung oleh lokalisasi bahasa bawaan (`values` untuk Bahasa Indonesia dan `values-en` untuk Bahasa Inggris). Aplikasi secara cerdas mendeteksi bahasa aktif pada sistem operasi Android perangkat dan secara otomatis menerjemahkan antarmuka pengguna tanpa memutus sesi transaksi.

### 🔋 9. Edge-to-Edge UI & Animasi Launching Interaktif
* Memanfaatkan teknologi Android modern `enableEdgeToEdge()` dan pengaturan *window insets* agar tampilan menyatu sempurna di belakang status bar dan bar navigasi sistem.
* Diperkaya dengan **Animasi Meluncur & Wobble Recoil** pada *Splash Screen* menggunakan gabungan `ObjectAnimator` dan `OvershootInterpolator` yang dinamis dan berkelas premium.

### 🔌 10. Konektivitas Printer Thermal Bluetooth & Cetak Nota Nirkabel
* Dilengkapi utilitas nirkabel khusus (`BluetoothPrinterHelper` & `BluetoothPermissionHelper`) yang mendukung pencetakan nota fisik secara instan lewat printer thermal Bluetooth 58mm/80mm. Menyediakan tata letak struk otomatis, garis pemisah dinamis, dan penanganan status koneksi printer secara *real-time* untuk operasional kasir yang mulus.

---

## 🛠️ Tech Stack

### Mobile Client (Android)
| Teknologi | Versi | Peran |
|-----------|-------|-------|
| **Kotlin** | v2.2.10 | Bahasa pemrograman utama berkinerja tinggi |
| **Android SDK** | Target 36 (Android 14/15/Q) | Landasan sistem operasi seluler |
| **Material Design 3** | v1.13.0 | Desain UI modern, komponen estetik, dan responsif |
| **AndroidX Core KTX** | v1.10.1 | Ekstensi Kotlin untuk pengembangan Android yang bersih |
| **Glide** | v4.16.0 | *Library* pemuatan dan pengoptimalan gambar produk |
| **AppCompat** | v1.6.1 | Menjamin kompatibilitas antarmuka pada OS versi terdahulu |
| **ConstraintLayout** | v2.1.4 | Pembuatan tata letak UI yang kompleks dan fleksibel |

### Backend & Cloud Infrastructure
| Teknologi | Peran |
|-----------|-------|
| **Firebase Realtime Database** | Penyimpanan basis data NoSQL berlatensi rendah untuk sinkronisasi data kasir secara langsung |
| **Firebase Authentication** | Pengamanan akun pegawai/kasir dengan sistem otentikasi terenkripsi cloud |
| **SaldoManager Utility** | Pengelolaan kalkulasi akumulasi saldo toko secara otomatis saat pembayaran sukses |

---

## 🚀 Cara Menjalankan Proyek

### Prasyarat Pengembangan
* Komputer dengan OS **Windows** (atau macOS/Linux).
* **Android Studio** versi terbaru (Ladybug atau yang lebih baru direkomendasikan).
* **JDK 17** atau versi terbaru.
* Koneksi internet aktif untuk sinkronisasi Gradle dan Firebase.

### 1. Kloning Repositori
Jalankan perintah berikut di terminal Anda untuk menyalin proyek ke lokal komputer:
```bash
git clone https://github.com/Byatarade/DevSpark.git
cd DevSpark
```

### 2. Hubungkan dengan Firebase Anda
1. Buka [Firebase Console](https://console.firebase.google.com/).
2. Buat proyek baru dengan nama **DevSpark** (atau nama pilihan Anda).
3. Daftarkan aplikasi Android baru dengan Package Name: `com.byatara.penjualandev`.
4. Unduh file konfigurasi **`google-services.json`**.
5. Letakkan file `google-services.json` tersebut ke dalam folder `/app/` proyek Anda:
   ```
   DevSpark/
   └── 📁 app/
       └── google-services.json  <-- Letakkan di sini
   ```
6. Di Firebase Console, aktifkan fitur **Authentication** (metode Email/Password) dan **Realtime Database**.

### 3. Konfigurasi Awal Gradle
Buka proyek **DevSpark** menggunakan **Android Studio**. IDE akan secara otomatis memicu proses *Gradle Sync* untuk mengunduh seluruh dependensi yang tertera di file `build.gradle.kts` dan `gradle/libs.versions.toml`.

### 4. Menjalankan Aplikasi
* Hubungkan perangkat Android fisik via USB Debugging, atau aktifkan Android Virtual Device (Emulator) di Android Studio.
* Klik tombol **Run 'app'** (`Shift + F10`) di bagian atas bar Android Studio.
* Aplikasi siap digunakan!

---

## 🔑 Akses Aplikasi

Aplikasi memiliki alur otentikasi mandiri terpadu demi keamanan data transaksi cabang:
1. **Registrasi Akun Baru**: Pengguna mendaftarkan akun pegawai/kasir melalui halaman *Register* dengan melengkapi nama, email, nomor telepon, password, serta menentukan cabang tempat bertugas.
2. **Login Akun**: Masuk menggunakan kredensial terdaftar untuk memuat data khusus sesuai ID Cabang pegawai secara otomatis.
3. **Penyimpanan Status Sesi**: Menggunakan proteksi Firebase Auth session, sehingga kasir tidak perlu berulang kali memasukkan password setiap kali membuka aplikasi.

---

## 📁 Struktur Folder Proyek

```
DevSpark/
├── 📁 app/
│   ├── 📁 src/
│   │   ├── 📁 main/
│   │   │   ├── 📁 java/com/byatara/penjualandev/
│   │   │   │   ├── 📁 adapter/          # Adapter RecyclerView untuk produk, keranjang, order, dll.
│   │   │   │   ├── 📁 cabang/           # Aktivitas dan dialog manajemen data Cabang Toko
│   │   │   │   ├── 📁 kategori/         # Aktivitas CRUD kategori produk
│   │   │   │   ├── 📁 model/            # Entitas model Firebase (Cabang, Order, Produk, dll.)
│   │   │   │   │   ├── ModelCabang.kt
│   │   │   │   │   ├── ModelHistori.kt
│   │   │   │   │   ├── ModelKategori.kt
│   │   │   │   │   ├── ModelNotification.kt
│   │   │   │   │   ├── ModelOrder.kt
│   │   │   │   │   ├── ModelOrderItem.kt
│   │   │   │   │   ├── ModelPegawai.kt
│   │   │   │   │   ├── ModelPelanggan.kt
│   │   │   │   │   └── ModelProduk.kt
│   │   │   │   ├── 📁 pegawai/          # Manajemen data staf / kasir toko
│   │   │   │   ├── 📁 pelanggan/        # Manajemen data pelanggan setia
│   │   │   │   ├── 📁 produk/           # Manajemen katalog produk / menu makanan & minuman
│   │   │   │   ├── 📁 util/             # Utilitas formatting Rupiah & asisten umum
│   │   │   │   ├── 📁 utils/            # SaldoManager dan utilitas status transaksi
│   │   │   │   ├── 📁 viewmodel/        # Manajemen state data dinamis UI
│   │   │   │   │
│   │   │   │   ├── Exit.kt              # Utilitas konfirmasi keluar aplikasi
│   │   │   │   ├── HistoriActivity.kt   # Riwayat jejak log aktivitas cabang
│   │   │   │   ├── LaporanActivity.kt   # Grafik & ringkasan omset penjualan harian/bulanan
│   │   │   │   ├── LoginActivity.kt     # Halaman otentikasi login masuk kasir
│   │   │   │   ├── MainActivity.kt      # Beranda Dasbor utama dan navigasi menu utama
│   │   │   │   ├── NotificationActivity.kt # Pusat notifikasi peringatan stok dan transaksi
│   │   │   │   ├── PembayaranActivity.kt # Proses checkout kasir dengan kalkulator tunai & e-wallet
│   │   │   │   ├── PrintHistoryActivity.kt # Riwayat struk tercetak
│   │   │   │   ├── ProfileActivity.kt   # Pengaturan data profil kasir aktif
│   │   │   │   ├── ReceiptActivity.kt   # Halaman struk digital akhir & simulasi print
│   │   │   │   ├── RegisterActivity.kt  # Halaman registrasi staf kasir baru
│   │   │   │   ├── SplashActivity.kt    # Layar sambutan awal aplikasi
│   │   │   │   └── TransaksiActivity.kt # Halaman utama transaksi POS (pilih produk & keranjang)
│   │   │   │
│   │   │   └── 📁 res/
│   │   │       ├── 📁 layout/           # Desain antarmuka XML layout (Activity, Row, Dialog)
│   │   │       └── 📁 values/           # Resourcestring, warna dinamis, dan tema Material 3
│   │   │
│   │   └── google-services.json         # File kredensial Firebase (sensitif)
│   └── build.gradle.kts                 # Konfigurasi dependensi modul aplikasi
│
├── 📁 Documentation/                    # Kumpulan tangkapan layar dokumentasi aplikasi
├── build.gradle.kts                     # Konfigurasi project-level gradle
├── settings.gradle.kts                  # Pendaftaran modul & repository gradle
└── gradle/libs.versions.toml            # Manajemen versi pustaka eksternal terpusat
```

---

## 🗄️ Arsitektur Database & Keamanan

### Entity Relationship Diagram (ERD) - Firebase Realtime Database
Meskipun Firebase menggunakan struktur NoSQL JSON Tree, hubungan relasional antar entitas di DevSpark dirancang dengan ketat demi menjaga keutuhan relasi master-transaksi:

```mermaid
erDiagram
    CABANG ||--o{ PEGAWAI : "memiliki_staf"
    CABANG ||--o{ PELANGGAN : "terdaftar_di"
    CABANG ||--o{ KATEGORI : "mengelompokkan"
    CABANG ||--o{ PRODUK : "menyimpan_stok"
    CABANG ||--o{ ORDER : "mencatat_transaksi"
    
    KATEGORI ||--o{ PRODUK : "kategori_barang"
    
    ORDER ||--|{ ORDER_ITEM : "berisi"
    ORDER_ITEM }|--|| PRODUK : "membeli_produk"
    
    ORDER ||--o{ HISTORI : "mencatat_log"
    PRODUK ||--o{ NOTIFICATION : "memicu_peringatan_stok"

    CABANG {
        string idCabang PK
        string namaCabang
        string alamatCabang
        string teleponCabang
    }

    PEGAWAI {
        string idPegawai PK
        string idCabang FK
        string namaPegawai
        string emailPegawai
        string teleponPegawai
        string rolePegawai
    }

    PELANGGAN {
        string idPelanggan PK
        string idCabang FK
        string namaPelanggan
        string emailPelanggan
        string teleponPelanggan
        string alamatPelanggan
    }

    KATEGORI {
        string idKategori PK
        string idCabang FK
        string namaKategori
        string deskripsiKategori
    }

    PRODUK {
        string idProduk PK
        string idCabang FK
        string idKategori FK
        string namaProduk
        string fotoProduk
        string deskripsiProduk
        int stokProduk
        string tanpaBatas
        int hargaBeli
        int hargaJual
        string tipeKeuntungan
        string manajemenStok
        string statusProduk
        string createdAt
        string updatedAt
        int jumlahTerjual
    }

    ORDER {
        string idOrder PK
        string idCabang FK
        string namaKasir
        string namaPelanggan
        string nomorMeja
        string catatan
        string metodeBayar
        int subtotal
        int pajak
        int totalHarga
        int uangDiterima
        int kembalian
        string status
        long timestamp
        string tanggalWaktu
        int keuntungan
    }

    ORDER_ITEM {
        string idProduk PK, FK
        string namaProduk
        string fotoProduk
        int hargaJual
        int hargaBeli
        int qty
        int subtotal
        string tanpaBatas
    }

    NOTIFICATION {
        string id PK
        string targetId FK
        string type
        string title
        string message
        long timestamp
        boolean isRead
    }

    HISTORI {
        string idHistori PK
        string judul
        string deskripsi
        string tipe
        long timestamp
        string tanggalWaktu
    }
```

### 🔐 Sistem Keamanan Aplikasi
1. **Otentikasi Aman**: Registrasi dan verifikasi identitas kasir diproteksi sepenuhnya di sisi server oleh **Firebase Authentication**.
2. **Isolasi ID Cabang (*Data Isolation*)**: Query data pada aplikasi disaring ketat berdasarkan `idCabang` yang melekat pada akun kasir yang masuk. Hal ini mencegah kasir dari Cabang A untuk membaca atau memodifikasi data keuangan dan inventori di Cabang B.
3. **Integritas Stok via Firebase Database Rules**: Firebase Realtime Database dapat disematkan aturan validasi untuk memastikan nilai `stokProduk` tidak pernah diisi dengan nilai minus (`.validate: "newData.val() >= 0"`).

---

## 📸 Dokumentasi

Berikut adalah visualisasi antarmuka dari aplikasi **DevSpark** yang dikelompokkan secara terstruktur mulai dari halaman otentikasi hingga transaksi:

### 🔐 1. Autentikasi
Sistem otentikasi modern untuk memproteksi masuknya pegawai ke dasbor kasir.
<table>
  <tr>
    <td align="center" width="15%">
      <img src="Documentation/Auth/Login.jpg" width="100%" alt="Login DevSpark" />
      <br/><sub><b>Halaman Login</b></sub>
    </td>
    <td align="center" width="15%">
      <img src="Documentation/Auth/Register.jpg" width="100%" alt="Register DevSpark" />
      <br/><sub><b>Halaman Registrasi Kasir</b></sub>
    </td>
  </tr>
</table>

### 🌐 2. Beranda View
Dasbor ringkasan performa cabang kasir, log histori, dan pusat notifikasi.
<table>
  <tr>
    <td align="center" width="20%">
      <img src="Documentation/Beranda View/Beranda.jpg" width="100%" alt="Beranda Utama" />
      <br/><sub><b>Beranda Utama</b></sub>
    </td>
    <td align="center" width="20%">
      <img src="Documentation/Beranda View/Histori.jpg" width="100%" alt="Histori Aktivitas" />
      <br/><sub><b>Log Histori</b></sub>
    </td>
    <td align="center" width="20%">
      <img src="Documentation/Beranda View/Notifikasi.jpg" width="100%" alt="Pusat Notifikasi" />
      <br/><sub><b>Pusat Notifikasi</b></sub>
    </td>
    <td align="center" width="20%">
      <img src="Documentation/Beranda View/Pengaturan Profil.jpg" width="100%" alt="Pengaturan Profil" />
      <br/><sub><b>Profil Pengguna</b></sub>
    </td>
    <td align="center" width="20%">
      <img src="Documentation/Beranda View/Validation Out.jpg" width="100%" alt="Validation Keluar" />
      <br/><sub><b>Konfirmasi Keluar</b></sub>
    </td>
  </tr>
</table>

### 🏢 3. Manajemen Cabang
Pengelolaan data lokasi fisik dari cabang-cabang bisnis.
<table>
  <tr>
    <td align="center" width="17%">
      <img src="Documentation/Cabang/Cabang.jpg" width="100%" alt="Daftar Cabang" />
      <br/><sub><b>Daftar Cabang</b></sub>
    </td>
    <td align="center" width="17%">
      <img src="Documentation/Cabang/Show cabang.jpg" width="100%" alt="Detail Cabang" />
      <br/><sub><b>Detail Informasi Cabang</b></sub>
    </td>
    <td align="center" width="17%">
      <img src="Documentation/Cabang/Tambah cabang.jpg" width="100%" alt="Tambah Cabang" />
      <br/><sub><b>Tambah Cabang Baru</b></sub>
    </td>
  </tr>
</table>

### 🏷️ 4. Manajemen Kategori
Pengorganisasian produk berdasarkan klasifikasi/kategori.
<table>
  <tr>
    <td align="center" width="17%">
      <img src="Documentation/Kategori/Kategori.jpg" width="100%" alt="Daftar Kategori" />
      <br/><sub><b>Daftar Kategori</b></sub>
    </td>
    <td align="center" width="17%">
      <img src="Documentation/Kategori/Show kategori.jpg" width="100%" alt="Detail Kategori" />
      <br/><sub><b>Detail Kategori</b></sub>
    </td>
    <td align="center" width="17%">
      <img src="Documentation/Kategori/Tambah kategori.jpg" width="100%" alt="Tambah Kategori" />
      <br/><sub><b>Tambah Kategori Baru</b></sub>
    </td>
  </tr>
</table>

### 🍔 5. Manajemen Menu (Produk)
Katalog menu/produk dagang yang dijual lengkap dengan harga beli, harga jual, dan kontrol stok.
<table>
  <tr>
    <td align="center" width="17%">
      <img src="Documentation/Menu/Menu.jpeg" width="100%" alt="Daftar Menu" />
      <br/><sub><b>Daftar Menu & Produk</b></sub>
    </td>
    <td align="center" width="17%">
      <img src="Documentation/Menu/Show menu.jpeg" width="100%" alt="Detail Menu" />
      <br/><sub><b>Detail Informasi Menu</b></sub>
    </td>
    <td align="center" width="17%">
      <img src="Documentation/Menu/Tambah menu.jpeg" width="100%" alt="Tambah Menu" />
      <br/><sub><b>Tambah Menu Baru</b></sub>
    </td>
  </tr>
</table>

### 👥 6. Manajemen Pegawai
Pengaturan keanggotaan staf kasir di setiap cabang bisnis.
<table>
  <tr>
    <td align="center" width="17%">
      <img src="Documentation/Pegawai/Pegawai.jpg" width="100%" alt="Daftar Pegawai" />
      <br/><sub><b>Daftar Pegawai</b></sub>
    </td>
    <td align="center" width="17%">
      <img src="Documentation/Pegawai/Show pegawai.jpg" width="100%" alt="Detail Pegawai" />
      <br/><sub><b>Detail Data Staf</b></sub>
    </td>
    <td align="center" width="17%">
      <img src="Documentation/Pegawai/Tambah pegawai.jpg" width="100%" alt="Tambah Pegawai" />
      <br/><sub><b>Daftarkan Pegawai Baru</b></sub>
    </td>
  </tr>
</table>

### 🤝 7. Manajemen Pelanggan
Basis data identitas pelanggan loyal pendukung program loyalitas atau pencatatan transaksi terarah.
<table>
  <tr>
    <td align="center" width="17%">
      <img src="Documentation/Pelanggan/Pelanggan.jpg" width="100%" alt="Daftar Pelanggan" />
      <br/><sub><b>Daftar Pelanggan</b></sub>
    </td>
    <td align="center" width="17%">
      <img src="Documentation/Pelanggan/Show Pelanggan.jpg" width="100%" alt="Detail Pelanggan" />
      <br/><sub><b>Detail Pelanggan</b></sub>
    </td>
    <td align="center" width="17%">
      <img src="Documentation/Pelanggan/Tambah Pelanggan.jpg" width="100%" alt="Tambah Pelanggan" />
      <br/><sub><b>Tambah Pelanggan Baru</b></sub>
    </td>
  </tr>
</table>

### 🛒 8. Alur Transaksi & Pembayaran
Proses POS yang sangat komprehensif mulai dari pemilihan produk, pengaturan profil kasir/pelanggan, hingga konfirmasi pembayaran multi-metode (Tunai/QRIS/E-Wallet).
<table>
  <tr>
    <td align="center" width="20%">
      <img src="Documentation/Transaksi/Transaksi.jpg" width="100%" alt="Halaman Transaksi" />
      <br/><sub><b>Katalog Belanja POS</b></sub>
    </td>
    <td align="center" width="20%">
      <img src="Documentation/Transaksi/Pilih Pelanggan.jpg" width="100%" alt="Pilih Pelanggan" />
      <br/><sub><b>Pilih Pelanggan</b></sub>
    </td>
    <td align="center" width="20%">
      <img src="Documentation/Transaksi/Pilih Pegawai.jpg" width="100%" alt="Pilih Kasir" />
      <br/><sub><b>Pilih Pegawai</b></sub>
    </td>
    <td align="center" width="20%">
      <img src="Documentation/Transaksi/Transaksi - checkout.jpg" width="100%" alt="Keranjang Checkout" />
      <br/><sub><b>Keranjang Belanja</b></sub>
    </td>
    <td align="center" width="20%">
      <img src="Documentation/Transaksi/Checkout.jpg" width="100%" alt="Checkout Konfirmasi" />
      <br/><sub><b>Metode Pembayaran</b></sub>
    </td>
  </tr>
  <tr>
    <td align="center" width="20%">
      <img src="Documentation/Transaksi/Checkout-tunai.jpg" width="100%" alt="Checkout Tunai" />
      <br/><sub><b>Kalkulator Tunai</b></sub>
    </td>
    <td align="center" width="20%">
      <img src="Documentation/Transaksi/Checkout-qris.jpg" width="100%" alt="Checkout QRIS" />
      <br/><sub><b>Simulasi Pembayaran QRIS</b></sub>
    </td>
    <td align="center" width="20%">
      <img src="Documentation/Transaksi/Checkout-e wallet.jpg" width="100%" alt="Checkout E-Wallet" />
      <br/><sub><b>Pilihan E-Wallet</b></sub>
    </td>
    <td align="center" width="20%">
      <img src="Documentation/Transaksi/Edit pelanggan & pegawai.jpg" width="100%" alt="Edit Pelanggan Pegawai" />
      <br/><sub><b>Ubah Staf/Pelanggan</b></sub>
    </td>
    <td align="center" width="20%"></td>
  </tr>
</table>

### 📊 9. Laporan & Rekap Penjualan
Statistik performa penjualan cabang harian/bulanan beserta omset kumulatif yang terdokumentasi.
<table>
  <tr>
    <td align="center" width="15%">
      <img src="Documentation/Laporan/Laporan.jpg" width="100%" alt="Grafik Omset Penjualan" />
      <br/><sub><b>Dasbor Ringkasan Laporan</b></sub>
    </td>
    <td align="center" width="15%">
      <img src="Documentation/Laporan/Laporan-tanggal.jpg" width="100%" alt="Laporan Berdasarkan Tanggal" />
      <br/><sub><b>Filter Laporan per Rentang Tanggal</b></sub>
    </td>
  </tr>
</table>

### 🖨️ 10. Cetak Nota / Struk Bukti Bayar
Simulasi struk penjualan terstruktur siap cetak.
<table>
  <tr>
    <td align="center" width="15%">
      <img src="Documentation/Print/Print.jpg" width="100%" alt="Simulasi Cetak Struk" />
      <br/><sub><b>Halaman Struk Pembayaran</b></sub>
    </td>
    <td align="center" width="15%">
      <img src="Documentation/Print/Print Nota.jpg" width="100%" alt="Tampilan Struk Nota" />
      <br/><sub><b>Struk Belanja Terstruktur</b></sub>
    </td>
  </tr>
</table>

---

## 🤝 Kontribusi

Kontribusi dari seluruh pengembang sangat kami hargai! Jika Anda ingin meningkatkan fungsionalitas DevSpark:

1. **Fork** repositori ini ke akun Anda: [Byatarade/DevSpark](https://github.com/Byatarade/DevSpark)
2. Buat **branch** fitur baru: `git checkout -b feature/NamaFiturKeren`
3. **Commit** perubahan Anda dengan pesan yang jelas: `git commit -m 'feat: menambahkan integrasi printer thermal bluetooth'`
4. **Push** ke branch Anda: `git push origin feature/NamaFiturKeren`
5. Ajukan **Pull Request (PR)** dan tunggu ulasan dari maintainer.

> Harap patuhi konvensi penulisan commit berstandar [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/).

---

<div align="center">

Dibuat dengan dedikasi penuh menggunakan &nbsp;
![Kotlin](https://img.shields.io/badge/Kotlin-v2.2.10-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
&nbsp; dan &nbsp;
![Firebase](https://img.shields.io/badge/Firebase-Realtime_DB-FFCA28?style=flat-square&logo=firebase&logoColor=black)
&nbsp; serta &nbsp;
![Material Design 3](https://img.shields.io/badge/Material_Design-3_v1.13.0-757575?style=flat-square&logo=materialdesign&logoColor=white)

**Created with ❤️ by [Byatarade](https://github.com/Byatarade)**
*Menghadirkan efisiensi dalam setiap kedipan transaksi.*

</div>

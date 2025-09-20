# INVOICE MANAGEMENT SYSTEM INTEGERATED WITH ORDERKUOTA

![photo_2025-07-13_15-31-07](https://github.com/user-attachments/assets/73b6b170-56ab-4963-86bb-2ff7988c6d6d)

DONATE IF THIS REPO HELP YOU!!!!


## 1. Deskripsi
> Aplikasi ini adalah sistem maajement invoice yang terintegerasi dengan Order Kuota
> Aplikasi ini dibuat untuk mempermudah dalam mengelola invoice yang dihasilkan dari transaksi yang dilakukan melalui Order Kuota.
> Aplikasi ini dibuat menggunakan Java Spring Boot 3.


## 2. Fitur
- **Login**: Pengguna dapat masuk ke dalam aplikasi menggunakan akun yang telah terdaftar dalam sistem Order Kuota.
- **Mutasi**: Pengguna dapat melakukan mutasi untuk melihat transaksi yang telah dilakukan.
- **Invoice**: Pengguna dapat membuat, mengelola, dan melihat invoice yang dihasilkan dari transaksi.
- **QRIS Generator**: Pengguna dapat membuat QRIS untuk memudahkan pembayaran invoice.
- **Check Akun Bank**: Pengguna dapat memeriksa apakah rekening bank.
- **Notifikasi**: Pengguna akan menerima notifikasi melalui callback URL yang telah ditentukan ketika ada perubahan status pada invoice.
- **Pengaturan Transaksi**: Pengguna dapat mengatur jenis ReferensiID atau kode unik untuk setiap transaksi yang dilakukan, agar tidak ada yang sama.

## 3. Deployment

### Pre-requisites
- OpenJDK 21 atau distribusi Java Development Kit (JDK) lainnya yang sama (Dapat menggunakan OpenJDK 21, Oracle JDK 21, Amazon Corretto 21, dll).
- Apache Maven 3.9.0 atau versi yang lebih baru.
- Database MySQL 8.0 atau versi yang lebih baru.
- VPS dengan distribusi apapun yang kompatibel dengan Java dan Maven.
- Akun Order Kuota untuk mendapatkan API Key dan Secret Key.
- WebServer lainnya untuk menerima callback data * Optional

### Langkah-langkah Deployment
1. **Clone Repository**
> Anda bisa melakukan clone repository ini atau mengunduhnya sebagai ZIP file lalu mengekstraknya ke sebuah direktori di server Anda.

2. **Konfigurasi Aplikasi**
> Semua konfigurasi aplikasi dapat ditemukan dalam file aplikasi `application.properties` yang terletak di dalam direktori `src/main/resources/`. Anda perlu mengatur:
```properties
spring.application.name=Integerasi Order Kuota
server.port=8080
spring.datasource.url=jdbc:mysql://localhost:3306/cek
spring.datasource.username=root
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=update
spring.web.resources.add-mappings=false
spring.datasource.hikari.minimum-idle=10
spring.datasource.hikari.maximum-pool-size=100
application.config.random.reff.id=false
```

| Parameter                                    | Deskripsi                                                             |
|----------------------------------------------|-----------------------------------------------------------------------|
| `spring.application.name`                    | Nama aplikasi yang akan ditampilkan di konsol                         |
| `server.port`                                | Port yang akan digunakan oleh aplikasi (default: 8080)                |
| `spring.datasource.url`                      | URL koneksi ke database MySQL                                         |
| `spring.datasource.username`                 | Username untuk koneksi ke database MySQL                              |
| `spring.datasource.password`                 | Password untuk koneksi ke database MySQL                              |
| `spring.jpa.hibernate.ddl-auto`              | Opsi untuk mengelola skema database (default: `update`)               |
| `spring.web.resources.add-mappings`          | Menonaktifkan penambahan mapping                                      |
| `spring.datasource.hikari.minimum-idle`      | Jumlah thread minimum yang idle dalam pool koneksi                    |
| `spring.datasource.hikari.maximum-pool-size` | Jumlah maksimum thread dalam pool koneksi                             |
| `application.config.random.reff.id`          | Mengatur apakah ReferensiID akan diacak atau tidak (default: `false`) |

> Pastikan untuk mengganti nilai `spring.datasource.url`, `spring.datasource.username`, dan `spring.datasource.password` sesuai dengan konfigurasi database MySQL Anda.
> Tabel tabel yang diperlukan akan dibuat secara otomatis jika anda mengatur `spring.jpa.hibernate.ddl-auto=update`. Jika Anda ingin mengelola skema database secara manual, Anda bisa mengubah nilai ini menjadi `none` atau `validate`.
> Pastikan juga database MySQL Anda sudah dibuat dan berjalan dan dapat diakses dari server tempat aplikasi ini akan dijalankan.

3. **Jalankan Aplikasi**
> Setelah konfigurasi selesai, Anda dapat menjalankan aplikasi dengan perintah berikut:
  - Navigasikan ke direktori proyek Anda.
  - Untuk menginstall semua dependensi yang diperlukan, jalankan:
    ```bash
    mvn clean install
    ```
    - Untuk mengcompile aplikasi menjadi file JAR, jalankan:
    ```bash
    mvn package
    ```
  - Atau jika ingin rebuild aplikasi setelah mengubah konfigurasi, Anda bisa menggunakan:
    ```bash
    mvn clean package
    ```
  - File JAR akan dihasilkan di dalam direktori `target/`.
  - Untuk menjalankan aplikasi, gunakan perintah:
    ```bash
    java -jar target/invoice-management-system.jar
    ```
    nama file bisa berbeda tergantung pada nama aplikasi Anda, dan versi yang digunakan.
  - Aplikasi akan berjalan pada port yang telah Anda tentukan di `application.properties`.
  - Bisa dijalankan dengan docker lewat `docker build -t invoice-management-system .` dan `docker run -p 8080:8080 invoice-management-system`.

## 4. **API DOCUMENTATION**

### 1. Daftar Pengguna API

```markdown
# 📄 Daftar Pengguna API

> **Catatan:** Tidak dianjurkan menggunakan API ini secara langsung.  
> Sebaiknya gunakan aplikasi resmi yang telah disediakan.

---

## 🔑 Autentikasi Pengguna

**Endpoint:**  
```

POST /api/v2/users/auth

````

**Request Body:**
```json
{
  "username": "string",
  "password": "string"
}
````

---

### 📌 Response - Username/Password Salah

**Status Code:** `200 OK`

```json
{
   "status": null,
   "data": {
      "email": {
         "status": "Error",
         "data": {
            "error": "Username or password Wrong"
         }
      }
   }
}
```

---

### 📌 Response - Parameter Tidak Sesuai

**Status Code:** `200 OK`

```json
{
  "status": null,
  "data": {
    "email": {
      "status": "Success",
      "data": {
        "email": "Bidang Kata Sandi dibutuhkan, belum di centang."
      }
    }
  }
}
```

---

### 📌 Response - Berhasil

**Status Code:** `200 OK`

```json
{
  "status": null,
  "data": {
    "email": {
      "status": "Success",
      "data": {
        "email": "vian************mail.com"
      }
    }
  }
}
```


### 2. Claim OTP
```markdown
# 📄 Daftar Claim OTP

> **Catatan:** Ini Mandatory untuk mendapatkan akses ke API lainnya.
---

**Endpoint:**  
```

POST /api/v2/users/auth/otp

````
**Request Body:**
```json
{
   "username": "string",
  "otp": "string"
}
````

### 📌 Response - OTP Salah

**Status Code:** `400 BAD REQUEST`

```json
{
   "status": "Error",
   "data": {
      "error": "OTP was wrong"
   }
}
```

---

### 📌 Response - Parameter Tidak Sesuai

**Status Code:** `400 BAD REQUEST`

```json
{
   "status": "Error",
   "data": {
      "error": "OTP was wrong"
   }
}
```

---

### 📌 Response - Berhasil

**Status Code:** `200 OK`

```json
{
  "status": "OK",
  "data": {
    "token": "d52dae4a-615b-4d72-ac7b-d63b4677a38c",
    "username": "viandrastefani",
    "email": "email"
  }
}
```
Simpan token ini untuk digunakan pada API lainnya.

### 3. Details User

GET /api/v2/users/details/{username}


**Headers:**
```http
Authorization: Bearer <token>
```


### 📌 Response - User Tidak Ditemukan / Username dan Bearer Token Tidak Sesuai

**Status Code:** `404 BAD REQUEST`

```json
{
  "timestamp": "2025-08-12T19:06:37.9715696",
  "status": 404,
  "error": "Not Found",
  "message": "Cannot invoke \"org.yann.integerasiorderkuota.orderkuota.entity.User.getUsername()\" because \"user\" is null",
  "path": "/api/v2/users/details/viandrastefa"
}
```

---

---

### 📌 Response - Berhasil

**Status Code:** `200 OK`

```json
{
  "username": "viandrastefani",
  "email": "email",
  "token": "d52dae4a-615b-4d72-ac7b-d63b4677a38c",
  "qrcode": "data:image/png;base64,iVBO....5CYII=",
  "callback_url": null,
  "qris_string": "000201010...6304A0D7"
}
```

### 4. Update User

POST /api/v2/users/update
**Headers:**
```http
Authorization : Bearer <token>
```
**Request Body:**
```json
{
  "username": "string",
  "email": "string",
  "callback_url": "string"
}
```
username adalah mandatory, sedangkan email dan callback_url adalah optional.

### 📌 Response - Berhasil
**Status Code:** `200 OK`

```json
{
  "status": "OK",
  "data": {
    "username": "viandrastefani",
    "email": "email",
    "callback_url": null
  }
}
```
### 📌 Response - Parameter Tidak Sesuai
**Status Code:** `400 BAD REQUEST`
```json

{
  "status": "Error",
  "data": {
    "email": "Invalid email"
  }
}
```

### 4. Get Mutasi

GET /api/v2/statements/{username}
**Headers:**
```http
Authorization : Bearer <token>
Content-Type: application/json
```

Query Parameter:
```http
page : integer (optional, default: 0)
size : integer (optional, default: 10)
```

### 📌 Response - User Tidak Ditemukan
**Status Code:** `204 No Content`

```json
```

### 📌 Response - Token Tidak Sesuai
**Status Code:** `401 Unauthorized`

```json
{
  "message": "Token Invalid"
}
```

### 📌 Response - Berhasil
**Status Code:** `200 OK`
```json
{
  "page": 0,
  "size": 2,
  "data": [
    {
      "id": 162052951,
      "username": "viandrastefani",
      "debet": 0,
      "kredit": 15000,
      "keterangan": "NOBU / KR***********************",
      "status": "IN",
      "statement_status": "NOT_CLAIMED",
      "transfer_time": "2025-08-01T00:56:00"
    },
    {
      "id": 163113424,
      "username": "viandrastefani",
      "debet": 0,
      "kredit": 167,
      "keterangan": "NOBU / VI*************",
      "status": "IN",
      "statement_status": "NOT_CLAIMED",
      "transfer_time": "2025-08-05T19:23:00"
    }
  ],
  "total_content": 21,
  "total_pages": 11,
  "has_next": true,
  "has_previous": false,
  "total_data": 2
}
```

### 5. Membuat Invoice

POST /api/v2/invoices/create
**Headers:**
```http
Authorization : Bearer <token>
Content-Type: application/json
```

**Request Body:**
```json
{
  "notes": "Pembayaran untuk layanan",
  "amount": 10,
  "expires_at": 3
}
```
Expires_at adalah waktu dalam detik, setelah itu invoice akan kadaluarsa.

### 📌 Response - Berhasil
**Status Code:** `200 OK`
```json
{
  "username": "viandrastefani",
  "amount": 10,
  "status": "PENDING",
  "note": "Tok Kontol Konto",
  "expires_at": 1755001134825,
  "created_at": "2025-08-12T12:18:51.825423576",
  "qris_string": "00020101021226670016COM.NOBUBANK.WWW01189360050300000879140214206473089492430303UMI51440014ID.CO.QRIS.WWW0215ID20253829566420303UMI5204541153033605402105802ID5919TOKO VIAN OK23056296011PURBALINGGA61055331162070703A01630495B0",
  "invoice_id": "ad411c75-f6e3-49cc-b92a-195e9afd1a7b"
}
```
Expires_at akan dikonversi menjadi timestamp dalam time milis.

### 📌 Response - Token Tidak Sesuai
**Status Code:** `401 Unauthorized`
```json
{
  "message": "Token Invalid"
}
```

### 6. Details Invoice
GET /api/v2/invoices/details/{invoice_id}
**Headers:**
```http
Authorization: Bearer <token>
Accept: application/json
Content-Type: application/json
```

### 📌 Response - Berhasil
```json
{
  "username": "viandrastefani",
  "amount": 10,
  "status": "EXPIRED",
  "note": "Tok Kontol Konto",
  "expires_at": 1754820766998,
  "created_at": "2025-08-10T10:12:43.998006",
  "qris_string": "00020101021226670016COM.NOBUBANK.WWW01189360050300000879140214206473089492430303UMI51440014ID.CO.QRIS.WWW0215ID20253829566420303UMI5204541153033605402105802ID5919TOKO VIAN OK23056296011PURBALINGGA61055331162070703A01630495B0",
  "invoice_id": "6f467369-09d3-4d20-a70b-7ebcaed0bfb8"
}
```
Status invoice dapat berupa:
- `PENDING`: Invoice belum dibayar.
- 'PAID': Invoice sudah dibayar.
- 'EXPIRED': Invoice sudah kadaluarsa.

### 📌 Response - Token Tidak Sesuai
**Status Code:** `401 Unauthorized`
```json
{
  "message": "Token Invalid"
}
```

### 📌 Response - Invoice Tidak Ditemukan

**Status Code:** `404 Not Found`
```json
{
  "timestamp": "2025-08-12T19:24:28.6993629",
  "status": 400,
  "error": "Bad Request",
  "message": "Invoice Not Found",
  "path": "/api/v2/invoices/details/6f467369-09d3-4d20-a70b-7ebcaed0bf"
}
```

### 8. Generate QRIS
GET /api/v2/invoices/qris/{invoice_id}

Query Parameter:
```http
height : integer (optional, default: 1080)
width : integer (optional, default: 1080)
```

**Headers:**
```http
Authorization: Bearer <token>
Accept: image/png
```
### 📌 Response - Berhasil
**Status Code:** `200 OK`
```http
HTTP/1.1 200 OK
Content-Type: image/png
```
> Gambar QRIS akan dikirimkan sebagai response body.

### 9. Callback Data

Method POST

### Paid Invoices
```json
{
  "id": "6434e9b7-7ba6-4160-b9c3-030529dda148",
  "status": "PAID",
  "amount": 101,
  "note": "Ini Budi Ini bapak Budi",
  "created_at": "2025-08-10T01:23:37.463178",
  "paid_at": 1754763948300
}
```

### Expired Invoices
```json
{
  "id": "6434e9b7-7ba6-4160-b9c3-030529dda148",
  "status": "EXPIRED",
  "amount": 101,
  "note": "Ini Budi Ini bapak Budi",
  "created_at": "2025-08-10T01:23:37.463178",
  "expired_at": 1754763948300
}
```

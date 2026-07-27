package com.byatara.penjualandev.utils

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.BarcodeEncoder

object QrisHelper {

    /**
     * Memodifikasi string QRIS statis menjadi dinamis dengan menambahkan nominal (Tag 54).
     * @param staticQris String QRIS statis yang di-scan dari merchant
     * @param amount Jumlah nominal yang harus dibayar
     * @return String QRIS dinamis yang valid beserta CRC16 baru
     */
    fun generateDynamicQris(staticQris: String, amount: Int): String {
        try {
            // Hapus CRC lama (4 karakter terakhir beserta Tag 63 dan panjangnya '04')
            // Tag 63 CRC selalu ada di akhir: "6304" + 4 digit hex = 8 karakter
            val qrisWithoutCrc = if (staticQris.length > 8 && staticQris.substring(staticQris.length - 8, staticQris.length - 4) == "63") {
                staticQris.substring(0, staticQris.length - 8)
            } else {
                // Asumsi jika format sedikit beda, potong sampai sebelum tag 63
                val index63 = staticQris.lastIndexOf("6304")
                if (index63 != -1) {
                    staticQris.substring(0, index63)
                } else {
                    staticQris // Tidak ada tag 63? aneh, tapi kembalikan saja
                }
            }

            // Siapkan tag 54 (Transaction Amount)
            val amountStr = amount.toString()
            val amountLength = String.format("%02d", amountStr.length)
            val tag54 = "54$amountLength$amountStr"

            // Pastikan tidak ada tag 54 ganda (idealnya QRIS statis tidak punya tag 54)
            // Jika ada, ini butuh parser EMVCo yang lengkap. 
            // Untuk metode simpel, kita tambahkan saja di akhir sebelum tag 63
            var modifiedQris = qrisWithoutCrc
            
            // Cek apakah sudah ada tag 54. Sangat jarang QRIS statis punya tag 54, tapi jika ada,
            // untuk case sederhana kita replace (namun butuh regex kompleks karena urutan tag acak).
            // Di sini kita asumsikan QRIS statis murni tanpa Tag 54.
            modifiedQris += tag54 

            // Tambahkan "6304" untuk persiapan menghitung CRC
            modifiedQris += "6304"

            // Hitung CRC16 CCITT-FALSE
            val crc = calculateCRC16CCITT(modifiedQris)

            // Gabungkan string dengan CRC
            return modifiedQris + crc
        } catch (e: Exception) {
            e.printStackTrace()
            return staticQris // Fallback ke statis jika gagal
        }
    }

    /**
     * Menghasilkan gambar Bitmap QR Code dari String
     */
    fun getQrCodeBitmap(content: String, size: Int = 800): Bitmap? {
        return try {
            val barcodeEncoder = BarcodeEncoder()
            barcodeEncoder.encodeBitmap(content, BarcodeFormat.QR_CODE, size, size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Kalkulasi CRC16 CCITT-FALSE
     */
    private fun calculateCRC16CCITT(payload: String): String {
        var crc = 0xFFFF
        val polynomial = 0x1021

        val bytes = payload.toByteArray(Charsets.UTF_8)
        for (b in bytes) {
            for (i in 0..7) {
                val bit = b.toInt() shr (7 - i) and 1 == 1
                val c15 = crc shr 15 and 1 == 1
                crc = crc shl 1
                if (c15 != bit) crc = crc xor polynomial
            }
        }
        crc = crc and 0xFFFF
        return String.format("%04X", crc)
    }
}

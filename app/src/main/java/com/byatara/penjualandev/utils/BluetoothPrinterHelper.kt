package com.byatara.penjualandev.utils

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.os.Handler
import android.os.Looper
import com.byatara.penjualandev.model.ModelOrder
import java.io.IOException
import java.nio.charset.Charset
import java.text.NumberFormat
import java.util.Locale
import java.util.UUID

object BluetoothPrinterHelper {

    private val printerMAC = "DC:0D:51:A7:FF:7A"
    private val printerUUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")

    private val INIT = byteArrayOf(0x1B, 0x40)
    private val ALIGN_CENTER = byteArrayOf(0x1B, 0x61, 0x01)
    private val ALIGN_LEFT = byteArrayOf(0x1B, 0x61, 0x00)
    private val BOLD_ON = byteArrayOf(0x1B, 0x45, 0x01)
    private val BOLD_OFF = byteArrayOf(0x1B, 0x45, 0x00)
    private val CUT_PAPER = byteArrayOf(0x1D, 0x56, 0x00)

    fun printReceipt(
        order: ModelOrder,
        onComplete: (success: Boolean, message: String) -> Unit
    ) {
        Thread {
            val (success, message) = try {
                val adapter = BluetoothAdapter.getDefaultAdapter()
                    ?: throw Exception("Bluetooth tidak tersedia di perangkat ini")

                if (!adapter.isEnabled) {
                    throw Exception("Nyalakan Bluetooth terlebih dahulu")
                }

                val device = adapter.getRemoteDevice(printerMAC)
                val socket = connectSocket(device)
                try {
                    val output = socket.outputStream
                    output.write(buildReceiptBytes(order))
                    output.flush()
                    Thread.sleep(300)
                    true to "Struk berhasil dicetak"
                } finally {
                    try {
                        socket.close()
                    } catch (_: IOException) {
                    }
                }
            } catch (e: SecurityException) {
                false to "Izin Bluetooth diperlukan"
            } catch (e: Exception) {
                false to (e.message ?: "Gagal cetak")
            }

            Handler(Looper.getMainLooper()).post {
                onComplete(success, message)
            }
        }.start()
    }

    @Suppress("DEPRECATION")
    private fun connectSocket(device: android.bluetooth.BluetoothDevice): BluetoothSocket {
        val uuid = printerUUID
        try {
            val socket = device.createRfcommSocketToServiceRecord(uuid)
            BluetoothAdapter.getDefaultAdapter()?.cancelDiscovery()
            socket.connect()
            return socket
        } catch (primary: IOException) {
            try {
                val method = device.javaClass.getMethod("createRfcommSocket", Int::class.javaPrimitiveType)
                val fallback = method.invoke(device, 1) as BluetoothSocket
                fallback.connect()
                return fallback
            } catch (fallback: Exception) {
                throw primary
            }
        }
    }

    private fun buildReceiptBytes(order: ModelOrder): ByteArray {
        val charset = Charset.forName("UTF-8")
        val buffer = mutableListOf<Byte>()

        fun write(bytes: ByteArray) = buffer.addAll(bytes.toList())
        fun writeLine(text: String = "") = write("$text\n".toByteArray(charset))

        write(INIT)
        write(ALIGN_CENTER)
        write(BOLD_ON)
        writeLine("DEVSPARK POS")
        write(BOLD_OFF)
        writeLine("Solusi Point of Sale Pintar")
        writeLine("================================")

        write(ALIGN_LEFT)
        writeLine("Order ID : ${order.idOrder ?: "-"}")
        writeLine("Waktu    : ${order.tanggalWaktu ?: "-"}")
        writeLine("Kasir    : ${order.namaKasir ?: "-"}")
        val pelanggan = if (order.namaPelanggan.isNullOrEmpty()) "Umum" else order.namaPelanggan
        writeLine("Pelanggan: $pelanggan")
        writeLine("--------------------------------")
        writeLine("Daftar Item:")

        order.items?.forEach { item ->
            val nama = item.namaProduk ?: "-"
            val qty = item.qty ?: 0
            val harga = formatRupiah(item.hargaJual ?: 0)
            val subtotal = formatRupiah(item.subtotal ?: 0)
            writeLine(nama)
            writeLine("  $qty x $harga = $subtotal")
        }

        writeLine("--------------------------------")
        writeLine("Subtotal : ${formatRupiah(order.subtotal ?: 0)}")
        writeLine("PPN 11%  : ${formatRupiah(order.pajak ?: 0)}")
        write(BOLD_ON)
        writeLine("TOTAL    : ${formatRupiah(order.totalHarga ?: 0)}")
        write(BOLD_OFF)

        val method = order.metodeBayar ?: "Tunai"
        writeLine("Metode   : $method")

        val isCash = method.lowercase(Locale.getDefault()) == "tunai"
        val bayar = if (isCash) order.uangDiterima ?: 0 else order.totalHarga ?: 0
        val kembali = if (isCash) order.kembalian ?: 0 else 0
        writeLine("Bayar    : ${formatRupiah(bayar)}")
        writeLine("Kembali  : ${formatRupiah(kembali)}")

        writeLine("================================")
        write(ALIGN_CENTER)
        writeLine("Terima Kasih")
        writeLine("Atas Kunjungan Anda")
        writeLine("Powered by DevSpark POS")
        writeLine()
        writeLine()
        write(CUT_PAPER)

        return buffer.toByteArray()
    }

    private fun formatRupiah(amount: Int): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return format.format(amount).replace(",00", "")
    }
}

package com.byatara.penjualandev.utils

import java.time.ZoneId
import java.time.ZonedDateTime

object GreetingHelper {

    private val zoneWib: ZoneId = ZoneId.of("Asia/Jakarta")

    /** Contoh: "Halo, Selamat Pagi" — berdasarkan jam WIB. */
    fun getHaloSalamWib(): String {
        val hour = ZonedDateTime.now(zoneWib).hour
        val salam = when (hour) {
            in 5..10 -> "Selamat Pagi"
            in 11..14 -> "Selamat Siang"
            else -> "Selamat Malam"
        }
        return "Halo, $salam"
    }
}

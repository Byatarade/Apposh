package com.byatara.penjualandev.utils

import com.byatara.penjualandev.R
import java.time.ZoneId
import java.time.ZonedDateTime

object GreetingHelper {

    private val zoneWib: ZoneId = ZoneId.of("Asia/Jakarta")

    /** Mendapatkan resource ID untuk salam berdasarkan jam WIB. */
    fun getGreetingResId(): Int {
        val hour = ZonedDateTime.now(zoneWib).hour
        return when (hour) {
            in 5..10 -> R.string.pagi
            in 11..14 -> R.string.siang
            else -> R.string.malam
        }
    }
}

package com.byatara.penjualandev.util

import java.text.NumberFormat
import java.util.Locale

/**
 * Utility function to format a number into Indonesian Rupiah (IDR) currency format.
 */
fun formatRupiah(amount: Number): String {
    val localeID = Locale("id", "ID")
    val format = NumberFormat.getCurrencyInstance(localeID)
    return format.format(amount.toLong()).replace(",00", "")
}

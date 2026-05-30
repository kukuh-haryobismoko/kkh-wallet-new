package com.kkh.wallet.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Indonesian Rupiah formatter. We format as "Rp1.234.567" (no decimals by default)
 * which matches consumer banking apps in Indonesia.
 */
object CurrencyFormatter {

    private val symbols = DecimalFormatSymbols(Locale("in", "ID")).apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }
    private val noDecimals: DecimalFormat = DecimalFormat("#,##0", symbols).apply {
        isGroupingUsed = true
    }
    private val twoDecimals: DecimalFormat = DecimalFormat("#,##0.00", symbols).apply {
        isGroupingUsed = true
    }

    /** "Rp1.250.000" — primary format used across the app. */
    fun formatRp(amount: Double): String = "Rp" + noDecimals.format(amount)

    /** Signed form, used for transaction rows. */
    fun formatSignedRp(amount: Double, isPositive: Boolean): String {
        val prefix = if (isPositive) "+ Rp" else "- Rp"
        return prefix + noDecimals.format(kotlin.math.abs(amount))
    }

    fun formatRpWithDecimals(amount: Double): String = "Rp" + twoDecimals.format(amount)

    /** Parses user input like "Rp1.250.000" or "1250000" back into a Double. */
    fun parseRp(input: String): Double {
        val cleaned = input.replace("Rp", "", ignoreCase = true)
            .replace(".", "")
            .replace(",", ".")
            .replace(Regex("[^0-9.\\-]"), "")
            .trim()
        return cleaned.toDoubleOrNull() ?: 0.0
    }
}

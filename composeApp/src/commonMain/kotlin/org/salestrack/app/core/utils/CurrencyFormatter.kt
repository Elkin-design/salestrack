package org.salestrack.app.core.utils

import kotlin.math.abs
import kotlin.math.roundToLong

fun formatMoney(value: Double): String {
    val cents = (value * 100.0).roundToLong()
    val absCents = abs(cents)
    val whole = absCents / 100
    val fractional = (absCents % 100).toString().padStart(2, '0')
    val sign = if (cents < 0) "-" else ""
    return "$sign$whole.$fractional"
}


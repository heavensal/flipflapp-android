package fr.flipflapp.android.core.util

import java.math.RoundingMode
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object MoneyFormat {
    fun formatEuros(value: String, locale: Locale = Locale.FRANCE): String {
        val amount = value.toBigDecimalOrNull()?.setScale(2, RoundingMode.HALF_UP)
            ?: return "$value €"
        val format = NumberFormat.getCurrencyInstance(locale).apply {
            currency = Currency.getInstance("EUR")
            maximumFractionDigits = if (amount.stripTrailingZeros().scale() <= 0) 0 else 2
            minimumFractionDigits = if (amount.stripTrailingZeros().scale() <= 0) 0 else 2
        }
        return format.format(amount)
    }

    fun normalizeWholeEuros(value: String): String {
        val amount = value.trim().replace(',', '.').toBigDecimalOrNull() ?: return value.trim()
        return amount.setScale(0, RoundingMode.HALF_UP).toPlainString()
    }
}

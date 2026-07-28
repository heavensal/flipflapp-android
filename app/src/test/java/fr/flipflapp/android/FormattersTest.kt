package fr.flipflapp.android

import fr.flipflapp.android.core.util.DateTimeFormat
import fr.flipflapp.android.core.util.MoneyFormat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class FormattersTest {
    @Test
    fun formatsEventDateTimeWithSeparator() {
        val formatted = DateTimeFormat.formatEventDateTime(
            value = "2026-08-01T10:00:00.000Z",
            locale = Locale.FRANCE,
        )
        assertTrue(formatted.contains("·"))
        assertTrue(formatted.isNotBlank())
    }

    @Test
    fun parsesAndSplitsIsoOffset() {
        val parts = DateTimeFormat.splitToLocalDateTime("2026-08-01T19:30:00+02:00")
        assertNotNull(parts)
        assertEquals(2026, parts!!.first.year)
        assertEquals(8, parts.first.monthValue)
        assertEquals(19, parts.second.hour)
        assertEquals(30, parts.second.minute)
    }

    @Test
    fun normalizesWholeEuros() {
        assertEquals("0", MoneyFormat.normalizeWholeEuros("0.0"))
        assertEquals("12", MoneyFormat.normalizeWholeEuros("12.4"))
        assertEquals("5", MoneyFormat.normalizeWholeEuros("5"))
    }

    @Test
    fun formatsEurosContainsCurrencySymbol() {
        val formatted = MoneyFormat.formatEuros("5.0", Locale.FRANCE)
        assertTrue(formatted.contains("5"))
        assertTrue(formatted.contains("€"))
    }

    @Test
    fun defaultFutureStartIsIso() {
        val value = DateTimeFormat.defaultFutureStart()
        assertNotNull(DateTimeFormat.parseInstant(value))
    }
}

package fr.flipflapp.android.core.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.FormatStyle
import java.util.Locale

object DateTimeFormat {
    private val zone: ZoneId get() = ZoneId.systemDefault()

    fun parseInstant(value: String): Instant? = try {
        OffsetDateTime.parse(value).toInstant()
    } catch (_: DateTimeParseException) {
        try {
            Instant.parse(value)
        } catch (_: DateTimeParseException) {
            null
        }
    }

    fun formatEventDateTime(value: String, locale: Locale = Locale.getDefault()): String {
        val instant = parseInstant(value) ?: return value
        val zoned = instant.atZone(zone)
        val date = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)
        val time = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale)
        return "${date.format(zoned)} · ${time.format(zoned)}"
    }

    fun formatEventDate(value: String, locale: Locale = Locale.getDefault()): String {
        val instant = parseInstant(value) ?: return value
        return DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
            .withLocale(locale)
            .format(instant.atZone(zone))
    }

    fun formatEventTime(value: String, locale: Locale = Locale.getDefault()): String {
        val instant = parseInstant(value) ?: return value
        return DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
            .withLocale(locale)
            .format(instant.atZone(zone))
    }

    fun formatNotificationTime(value: String, locale: Locale = Locale.getDefault()): String {
        val instant = parseInstant(value) ?: return value
        return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
            .withLocale(locale)
            .format(instant.atZone(zone))
    }

    fun toApiOffsetDateTime(date: LocalDate, time: LocalTime): String {
        val zoned = ZonedDateTime.of(LocalDateTime.of(date, time), zone)
        return zoned.toOffsetDateTime().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }

    fun splitToLocalDateTime(value: String): Pair<LocalDate, LocalTime>? {
        val instant = parseInstant(value) ?: return null
        val zoned = instant.atZone(zone)
        return zoned.toLocalDate() to zoned.toLocalTime().withSecond(0).withNano(0)
    }

    fun defaultFutureStart(): String {
        val tomorrowEvening = LocalDate.now(zone).plusDays(1).atTime(19, 0)
        return ZonedDateTime.of(tomorrowEvening, zone)
            .toOffsetDateTime()
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
    }
}

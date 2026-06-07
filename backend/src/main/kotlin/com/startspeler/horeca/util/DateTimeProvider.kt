package com.startspeler.horeca.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

object DateTimeProvider {
    private val utcZone = ZoneOffset.UTC
    private val brusselsZone = ZoneId.of("Europe/Brussels")

    fun nowUtc(): LocalDateTime =
        LocalDateTime.now(utcZone)

    fun nowBrussels(): LocalDateTime =
        LocalDateTime.now(brusselsZone)

    fun utcToBrussels(value: LocalDateTime): LocalDateTime =
        value.atZone(utcZone)
            .withZoneSameInstant(brusselsZone)
            .toLocalDateTime()

    fun brusselsToUtc(value: LocalDateTime): LocalDateTime =
        value.atZone(brusselsZone)
            .withZoneSameInstant(utcZone)
            .toLocalDateTime()

    fun brusselsDateStartToUtc(date: LocalDate): LocalDateTime =
        brusselsToUtc(date.atStartOfDay())

    fun brusselsDateEndToUtc(date: LocalDate): LocalDateTime =
        brusselsToUtc(LocalDateTime.of(date, LocalTime.MAX))
}
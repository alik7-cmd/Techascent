package org.techascent.muslim.prayer.util

import kotlinx.datetime.Instant
import org.techascent.muslim.prayer.uimodel.PrayerNameEnum
import org.techascent.muslim.prayer.uimodel.PrayerTimeUiModel

/**
 * Determines the phase of the day based on prayer times and current time.
 *
 * NIGHT : after Isha or before Fajr
 * DAWN  : Fajr → Sunrise  (brightening twilight)
 * DAY   : Sunrise → Sunset
 * DUSK  : Sunset → Isha    (dimming twilight)
 */
internal enum class DayPhase { NIGHT, DAWN, DAY, DUSK }

/**
 * Result of a sun-phase computation. Contains everything the UI needs;
 * carries no Compose dependency so it is freely unit-testable.
 */
internal data class SunPhaseResult(
    val phase: DayPhase,
    /** Progress [0..1] through the current phase arc. */
    val progress: Float,
) {
    val isNight: Boolean get() = phase == DayPhase.NIGHT
    val isDusk: Boolean get() = phase == DayPhase.DUSK
    val isDawn: Boolean get() = phase == DayPhase.DAWN
    val isDay: Boolean get() = phase == DayPhase.DAY
}

/**
 * Pure-logic calculator for the sun/moon hero card.
 * All inputs are plain data — no Compose, no side-effects.
 */
internal object SunPhaseCalculator {

    fun compute(uiModel: PrayerTimeUiModel, now: Instant): SunPhaseResult {
        val sunriseInstant = uiModel.sunriseInstant
        val sunsetInstant = uiModel.sunsetInstant
        val fajrStart = uiModel.intervals
            .firstOrNull { it.name == PrayerNameEnum.FAJR }?.startTimeInstant
        val ishaStart = uiModel.intervals
            .firstOrNull { it.name == PrayerNameEnum.ISHA }?.startTimeInstant

        // For the NIGHT arc: when currentPrayer is Isha (possibly yesterday's),
        // prefer its instants so the progress bar is meaningful after midnight.
        val isCurrentIsha = uiModel.currentPrayer?.name == PrayerNameEnum.ISHA
        val ishaEnd = if (isCurrentIsha)
            uiModel.currentPrayer!!.endTimeInstant
        else
            uiModel.intervals.firstOrNull { it.name == PrayerNameEnum.ISHA }?.endTimeInstant
        val nightIshaStart = if (isCurrentIsha)
            uiModel.currentPrayer!!.startTimeInstant ?: ishaStart
        else
            ishaStart

        val phase = resolvePhase(now, sunriseInstant, sunsetInstant, fajrStart, ishaStart)

        val progress = when (phase) {
            DayPhase.DAWN  -> progressBetween(fajrStart, sunriseInstant, now)
            DayPhase.DAY   -> progressBetween(sunriseInstant, sunsetInstant, now)
            DayPhase.DUSK  -> progressBetween(sunsetInstant, ishaStart, now)
            DayPhase.NIGHT -> progressBetween(nightIshaStart, ishaEnd, now)
        }

        return SunPhaseResult(phase = phase, progress = progress)
    }

    private fun resolvePhase(
        now: Instant,
        sunrise: Instant?,
        sunset: Instant?,
        fajrStart: Instant?,
        ishaStart: Instant?,
    ): DayPhase = when {
        sunrise == null || sunset == null -> DayPhase.DAY
        fajrStart != null && now >= fajrStart && now < sunrise -> DayPhase.DAWN
        now >= sunrise && now < sunset -> DayPhase.DAY
        ishaStart != null && now >= sunset && now < ishaStart -> DayPhase.DUSK
        else -> DayPhase.NIGHT
    }

    /** Returns a [0f..1f] fraction of elapsed time between [start] and [end]. */
    private fun progressBetween(start: Instant?, end: Instant?, now: Instant): Float {
        if (start == null || end == null || end <= start) return 0.5f
        val total = (end - start).inWholeMilliseconds.toFloat()
        val elapsed = (now - start).inWholeMilliseconds.toFloat()
        return (elapsed / total).coerceIn(0f, 1f)
    }
}


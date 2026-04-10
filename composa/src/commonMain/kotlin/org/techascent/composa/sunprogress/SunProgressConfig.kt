package org.techascent.composa.sunprogress

import androidx.compose.ui.graphics.Color

/**
 * Configuration for the [SunProgressCard] visual appearance.
 *
 * All colour and progress values are computed externally (e.g. from prayer
 * model data) so the card itself is fully generic and reusable.
 */
data class SunProgressConfig(
    /** Vertical gradient colours for the sky background (top → bottom). */
    val skyGradient: List<Color>,
    /** Progress along the arc, 0 = left horizon, 1 = right horizon. */
    val dayProgress: Float,
    /** Whether the current phase is full night (Isha → Fajr). */
    val isNight: Boolean = false,
    /** Whether the current phase is dusk / twilight (Sunset → Isha). */
    val isDusk: Boolean = false,
    /** Whether the current phase is dawn / pre-sunrise (Fajr → Sunrise). */
    val isDawn: Boolean = false,
    /** Colour of the celestial body (sun or moon). */
    val bodyColor: Color,
    /** Colour of the glow rings around the body. */
    val glowColor: Color,
    /** Colour of the horizon line. */
    val horizonColor: Color,
    /** Colour of the already-traveled part of the arc. */
    val arcAccentColor: Color,
    /** Colour of the full arc track. */
    val arcTrackColor: Color,
    /** Colour used for stars during night / dusk / dawn. */
    val starColor: Color = Color.White,
    /** Colour of the moon craters (only used when isNight or isDusk). */
    val craterColor: Color = Color(0x30A0A080),
    /** Night-phase sky start colour, used for the crescent shadow. */
    val nightSkyStartColor: Color = Color(0xFF0D1B2A),
    /** Twilight-phase sky start colour, used for the crescent shadow. */
    val twilightSkyStartColor: Color = Color(0xFF1A237E),
)


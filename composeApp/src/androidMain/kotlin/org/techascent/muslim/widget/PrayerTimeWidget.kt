package org.techascent.muslim.widget

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.layout.wrapContentHeight
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import org.techascent.muslim.MainActivity

/**
 * Glance App Widget — responsive: snaps between compact (header only) and
 * standard (header + all prayer times) layouts based on widget size.
 */
class PrayerTimeWidget : GlanceAppWidget() {

    companion object {
        /** Compact — current prayer name/time/date only. */
        val COMPACT = DpSize(180.dp, 100.dp)
        /** Standard — header + full prayer list. */
        val STANDARD = DpSize(250.dp, 220.dp)
    }

    /**
     * Responsive mode: Android renders the widget for each defined size and
     * picks the closest match for the actual widget dimensions.
     */
    override val sizeMode: SizeMode = SizeMode.Responsive(setOf(COMPACT, STANDARD))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = try {
            loadWidgetData(context)
        } catch (e: Exception) {
            Log.e("PrayerTimeWidget", "Failed to load widget data", e)
            null
        }
        provideContent {
            GlanceTheme {
                val size = LocalSize.current
                if (data != null) {
                    if (size.height >= STANDARD.height) {
                        StandardWidgetContent(data, context)
                    } else {
                        CompactWidgetContent(data, context)
                    }
                } else {
                    EmptyWidgetContent(context)
                }
            }
        }
    }
}

// ── Shared helper ──────────────────────────────────────────────────────────

private fun prayerSkyColor(name: String) = when (name) {
    "Fajr"    -> Color(0xFF1A237E)
    "Isha"    -> Color(0xFF0D1B2A)
    "Maghrib" -> Color(0xFF4A148C)
    "Asr"     -> Color(0xFF1565C0)
    "Dhuhr"   -> Color(0xFF0288D1)
    "Duha"    -> Color(0xFF0277BD)
    else      -> Color(0xFF1A237E)
}

// ═══════════════════════════════════════════════════════════════════════════
//  Compact layout  (small widget — current prayer header only)
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun CompactWidgetContent(data: WidgetPrayerData, context: Context) {
    val textColor = ColorProvider(Color.White)
    val subtleColor = ColorProvider(Color.White.copy(alpha = 0.7f))

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(prayerSkyColor(data.currentPrayerName)))
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java)))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Column(modifier = GlanceModifier.wrapContentHeight()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = data.currentPrayerEmoji, style = TextStyle(fontSize = 18.sp))
                Spacer(GlanceModifier.width(8.dp))
                Text(
                    text = data.currentPrayerName,
                    style = TextStyle(
                        color = textColor,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }
            if (data.currentPrayerStart.isNotEmpty()) {
                Spacer(GlanceModifier.height(2.dp))
                Text(
                    text = "${data.currentPrayerStart} – ${data.currentPrayerEnd}",
                    style = TextStyle(color = subtleColor, fontSize = 12.sp),
                )
            }
            Spacer(GlanceModifier.height(4.dp))
            Text(
                text = "${data.currentDate}  ·  ${data.locationLabel}",
                style = TextStyle(color = subtleColor, fontSize = 10.sp),
                maxLines = 1,
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  Standard layout  (larger widget — header + all prayer times)
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun StandardWidgetContent(data: WidgetPrayerData, context: Context) {
    val textColor = ColorProvider(Color.White)
    val subtleColor = ColorProvider(Color.White.copy(alpha = 0.7f))
    val highlightBg = Color.White.copy(alpha = 0.15f)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(prayerSkyColor(data.currentPrayerName)))
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java))),
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(10.dp),
        ) {
            // ── Header ──────────────────────────────────────────────
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = data.currentPrayerEmoji + "  " + data.currentPrayerName,
                        style = TextStyle(
                            color = textColor,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                    if (data.currentPrayerStart.isNotEmpty()) {
                        Text(
                            text = "${data.currentPrayerStart} – ${data.currentPrayerEnd}",
                            style = TextStyle(color = subtleColor, fontSize = 11.sp),
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = data.currentDate,
                        style = TextStyle(
                            color = textColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                        ),
                    )
                    if (data.hijriDate.isNotEmpty()) {
                        Text(
                            text = data.hijriDate,
                            style = TextStyle(
                                color = subtleColor,
                                fontSize = 9.sp,
                                textAlign = TextAlign.End,
                            ),
                        )
                    }
                    Text(
                        text = data.locationLabel,
                        style = TextStyle(
                            color = subtleColor,
                            fontSize = 9.sp,
                            textAlign = TextAlign.End,
                        ),
                        maxLines = 1,
                    )
                }
            }

            Spacer(GlanceModifier.height(5.dp))

            // ── Divider ─────────────────────────────────────────────
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(ColorProvider(Color.White.copy(alpha = 0.2f)))
            ) {}

            Spacer(GlanceModifier.height(4.dp))

            // ── Prayer rows ─────────────────────────────────────────
            data.allPrayers.forEach { prayer ->
                PrayerRowItem(
                    prayer = prayer,
                    textColor = textColor,
                    subtleColor = subtleColor,
                    highlightBg = highlightBg,
                )
            }
        }
    }
}

@Composable
private fun PrayerRowItem(
    prayer: WidgetPrayerRow,
    textColor: ColorProvider,
    subtleColor: ColorProvider,
    highlightBg: Color,
) {
    val rowBg = if (prayer.isCurrent) ColorProvider(highlightBg) else ColorProvider(Color.Transparent)
    val nameColor = if (prayer.isCurrent) textColor else subtleColor
    val timeColor = if (prayer.isCurrent) textColor else subtleColor
    val weight = if (prayer.isCurrent) FontWeight.Bold else FontWeight.Normal

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(rowBg)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = prayer.emoji, style = TextStyle(fontSize = 11.sp))
        Spacer(GlanceModifier.width(5.dp))
        Text(
            text = prayer.name,
            modifier = GlanceModifier.defaultWeight(),
            style = TextStyle(color = nameColor, fontSize = 11.sp, fontWeight = weight),
        )
        Text(
            text = prayer.time,
            style = TextStyle(color = timeColor, fontSize = 11.sp, fontWeight = weight),
        )
    }
}

// ── Empty state ────────────────────────────────────────────────────────────

@Composable
private fun EmptyWidgetContent(context: Context) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(Color(0xFF1A237E)))
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java)))
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "🕌", style = TextStyle(fontSize = 24.sp))
            Spacer(GlanceModifier.height(8.dp))
            Text(
                text = "Open app to load prayer times",
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                ),
            )
        }
    }
}

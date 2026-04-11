package org.techascent.muslim.widget

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
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
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import org.techascent.muslim.MainActivity

/**
 * Glance App Widget — shows current waqt (focused) + all prayer times.
 */
class PrayerTimeWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val data = try {
            loadWidgetData(context)
        } catch (e: Exception) {
            Log.e("PrayerTimeWidget", "Failed to load widget data", e)
            null
        }
        provideContent {
            GlanceTheme {
                if (data != null) {
                    PrayerWidgetContent(data, context)
                } else {
                    EmptyWidgetContent(context)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════
//  Widget content
// ═══════════════════════════════════════════════════════════════════════════

@Composable
private fun PrayerWidgetContent(data: WidgetPrayerData, context: Context) {
    val skyColor = when (data.currentPrayerName) {
        "Fajr"    -> Color(0xFF1A237E)
        "Isha"    -> Color(0xFF0D1B2A)
        "Maghrib" -> Color(0xFF4A148C)
        "Asr"     -> Color(0xFF1565C0)
        "Dhuhr"   -> Color(0xFF0288D1)
        "Duha"    -> Color(0xFF0277BD)
        else      -> Color(0xFF1A237E)
    }

    val textColor = ColorProvider(Color.White)
    val subtleColor = ColorProvider(Color.White.copy(alpha = 0.7f))
    val highlightBg = Color.White.copy(alpha = 0.15f)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(ColorProvider(skyColor))
            .clickable(actionStartActivity(Intent(context, MainActivity::class.java))),
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(12.dp),
        ) {
            // ── Header: Current waqt (focused) ──────────────────────
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = data.currentPrayerName,
                        style = TextStyle(
                            color = textColor,
                            fontSize = 35.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                    if (data.currentPrayerStart.isNotEmpty()) {
                        Text(
                            text = "${data.currentPrayerStart} – ${data.currentPrayerEnd}",
                            style = TextStyle(
                                color = subtleColor,
                                fontSize = 11.sp,
                            ),
                        )
                    }
                }
                Column {
                    Text(
                        text = data.currentDate,
                        style = TextStyle(
                            color = textColor,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                    if (data.hijriDate.isNotEmpty()) {
                        Text(
                            text = data.hijriDate,
                            style = TextStyle(
                                color = subtleColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Normal,
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

            Spacer(GlanceModifier.height(6.dp))

            // ── Divider line ────────────────────────────────────────
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(ColorProvider(Color.White.copy(alpha = 0.2f)))
            ) {}

            Spacer(GlanceModifier.height(4.dp))

            // ── All prayer times ────────────────────────────────────
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
    val rowBg = if (prayer.isCurrent)
        ColorProvider(highlightBg)
    else
        ColorProvider(Color.Transparent)
    val nameColor = if (prayer.isCurrent) textColor else subtleColor
    val timeColor = if (prayer.isCurrent) textColor else subtleColor
    val weight = if (prayer.isCurrent) FontWeight.Bold else FontWeight.Normal

    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .background(rowBg)
            .padding(horizontal = 4.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = prayer.emoji,
            style = TextStyle(fontSize = 12.sp),
        )
        Spacer(GlanceModifier.width(6.dp))
        Text(
            text = prayer.name,
            modifier = GlanceModifier.defaultWeight(),
            style = TextStyle(
                color = nameColor,
                fontSize = 12.sp,
                fontWeight = weight,
            ),
        )
        Text(
            text = prayer.time,
            style = TextStyle(
                color = timeColor,
                fontSize = 12.sp,
                fontWeight = weight,
            ),
        )
    }
}

// ── Empty state ────────────────────────────────────────────────────────

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
            Text(
                text = "🕌",
                style = TextStyle(fontSize = 28.sp),
            )
            Spacer(GlanceModifier.height(8.dp))
            Text(
                text = "Open app to load prayer times",
                style = TextStyle(
                    color = ColorProvider(Color.White),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                ),
            )
        }
    }
}

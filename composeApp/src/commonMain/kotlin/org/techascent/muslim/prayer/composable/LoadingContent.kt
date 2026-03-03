package org.techascent.muslim.prayer.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.shimmer.shimmerEffect
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.prayer.tags.PrayerTags

fun LazyListScope.loadingContent() {
    // 1 — Sun progress hero skeleton
    item { SunCardSkeleton() }

    // 2 — Countdown row skeleton (two side by side)
    item { CountdownRowSkeleton() }

    // 3 — Prayer + Fasting side by side skeleton
    item { PrayerFastingRowSkeleton() }

    // 4 — Announcement skeleton
    item { AnnouncementSkeleton() }
}

// ─── 1. Sun Progress Hero Skeleton ──────────────────────────────────────────────

@Composable
private fun SunCardSkeleton() {
    val skyStart = ComposaTheme.color.prayer.skyStart
    val skyEnd = ComposaTheme.color.prayer.skyEnd

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ComposaSpacing.Medium)
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.verticalGradient(listOf(skyStart, skyEnd)))
            .padding(ComposaSpacing.Medium)
            .testTag(PrayerTags.PRAYER_TIME_LOADING),
    ) {
        // Location line
        ShimmerBar(width = 160.dp, height = 12.dp)
        Spacer(Modifier.height(6.dp))
        // Prayer name
        ShimmerBar(width = 120.dp, height = 20.dp)
        Spacer(Modifier.height(4.dp))
        // Time range
        ShimmerBar(width = 100.dp, height = 14.dp)

        Spacer(Modifier.height(ComposaSpacing.Medium))

        // Arc placeholder — a wide rounded bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(16.dp))
                .shimmerEffect(true)
        )

        Spacer(Modifier.height(ComposaSpacing.Small))

        // Sunrise / Sunset row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            ShimmerBar(width = 90.dp, height = 12.dp)
            ShimmerBar(width = 90.dp, height = 12.dp)
        }
    }
}

// ─── 2. Countdown Row Skeleton ──────────────────────────────────────────────────

@Composable
private fun CountdownRowSkeleton() {
    val accent = ComposaTheme.color.prayer.timerAccent
    val fasting = ComposaTheme.color.prayer.fastingAccent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ComposaSpacing.Medium)
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(ComposaSpacing.Small),
    ) {
        // Waqt countdown
        CountdownCardSkeleton(modifier = Modifier.weight(1f), accentColor = accent)
        // Fasting countdown
        CountdownCardSkeleton(modifier = Modifier.weight(1f), accentColor = fasting)
    }
}

@Composable
private fun CountdownCardSkeleton(
    modifier: Modifier = Modifier,
    accentColor: androidx.compose.ui.graphics.Color,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(accentColor.copy(alpha = 0.08f))
            .padding(ComposaSpacing.Medium),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Circle placeholder
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .shimmerEffect(true)
        )
        Spacer(Modifier.height(8.dp))
        // Timer text
        ShimmerBar(width = 80.dp, height = 18.dp)
        Spacer(Modifier.height(4.dp))
        // Label
        ShimmerBar(width = 60.dp, height = 12.dp)
    }
}

// ─── 3. Prayer + Fasting Row Skeleton ───────────────────────────────────────────

@Composable
private fun PrayerFastingRowSkeleton() {
    val cardBg = ComposaTheme.color.prayer.cardBg

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ComposaSpacing.Medium)
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(ComposaSpacing.Small),
    ) {
        // Prayer list skeleton (left — wider)
        Column(
            modifier = Modifier
                .weight(1.6f)
                .clip(RoundedCornerShape(20.dp))
                .background(cardBg),
        ) {
            // Header
            SectionHeaderSkeleton()
            // 5 prayer rows
            repeat(5) { idx ->
                PrayerRowSkeleton()
                if (idx < 4) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .height(0.5.dp)
                            .background(ComposaTheme.color.strokeNeutralSubtle.copy(0.3f))
                    )
                }
            }
        }

        // Fasting skeleton (right — narrower)
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(20.dp))
                .background(cardBg),
        ) {
            // Header
            SectionHeaderSkeleton()
            // Iftar row
            FastingRowSkeleton()
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .height(0.5.dp)
                    .background(ComposaTheme.color.strokeNeutralSubtle.copy(0.3f))
            )
            // Suhur row
            FastingRowSkeleton()
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .height(0.5.dp)
                    .background(ComposaTheme.color.strokeNeutralSubtle.copy(0.3f))
            )
            // Countdown placeholder
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 11.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ShimmerBar(width = 60.dp, height = 12.dp)
                Spacer(Modifier.height(4.dp))
                ShimmerBar(width = 80.dp, height = 16.dp)
            }
        }
    }
}

@Composable
private fun SectionHeaderSkeleton() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .shimmerEffect(true)
        )
        Spacer(Modifier.width(8.dp))
        ShimmerBar(width = 80.dp, height = 14.dp)
    }
    Box(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .height(0.5.dp)
            .background(ComposaTheme.color.strokeNeutralSubtle.copy(0.4f))
    )
}

@Composable
private fun PrayerRowSkeleton() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Emoji placeholder
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .shimmerEffect(true)
        )
        Spacer(Modifier.width(6.dp))
        // Name
        ShimmerBar(width = 56.dp, height = 14.dp, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(4.dp))
        // Time
        ShimmerBar(width = 40.dp, height = 14.dp)
        Spacer(Modifier.width(6.dp))
        // Bell icon
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .shimmerEffect(true)
        )
    }
}

@Composable
private fun FastingRowSkeleton() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .shimmerEffect(true)
        )
        Spacer(Modifier.width(6.dp))
        ShimmerBar(width = 40.dp, height = 14.dp, modifier = Modifier.weight(1f))
        ShimmerBar(width = 40.dp, height = 14.dp)
    }
}

// ─── 4. Announcement Skeleton ───────────────────────────────────────────────────

@Composable
private fun AnnouncementSkeleton() {
    val cardBg = ComposaTheme.color.prayer.cardBg

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ComposaSpacing.Medium)
            .clip(RoundedCornerShape(20.dp))
            .background(cardBg),
    ) {
        SectionHeaderSkeleton()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ComposaSpacing.Medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .shimmerEffect(true)
            )
            Spacer(Modifier.width(12.dp))
            ShimmerBar(width = 180.dp, height = 14.dp)
        }
    }
}

// ─── Reusable shimmer bar ───────────────────────────────────────────────────────

@Composable
private fun ShimmerBar(
    width: androidx.compose.ui.unit.Dp,
    height: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(width = width, height = height)
            .clip(RoundedCornerShape(6.dp))
            .shimmerEffect(true)
    )
}
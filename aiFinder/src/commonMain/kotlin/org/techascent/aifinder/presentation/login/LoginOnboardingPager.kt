package org.techascent.aifinder.presentation.login

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apphub.aifinder.generated.resources.*
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.techascent.aifinder.presentation.components.SectionTitle
import org.techascent.aifinder.presentation.components.WelcomeIllustration
import org.techascent.aifinder.presentation.theme.AiFinderPalette

private enum class OnboardingSlide(val title: StringResource, val description: StringResource) {
    Welcome(Res.string.welcome_title, Res.string.welcome_description),
    Scan(Res.string.login_slide_scan_title, Res.string.login_slide_scan_description),
    Find(Res.string.login_slide_find_title, Res.string.login_slide_find_description),
}

@Composable
internal fun LoginOnboardingPager(modifier: Modifier = Modifier) {
    val slides = OnboardingSlide.entries
    val pagerState = rememberPagerState { slides.size }

    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        // An invisible stack of every slide sizes this Box to the tallest one; the pager
        // then fills that fixed height so the indicator never moves while swiping.
        Box(Modifier.fillMaxWidth()) {
            slides.forEach { slide ->
                SlideContent(slide, Modifier.alpha(0f).clearAndSetSemantics {})
            }
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.matchParentSize(),
                pageSpacing = 16.dp,
                verticalAlignment = Alignment.Top
            ) { page ->
                SlideContent(slides[page])
            }
        }
        Spacer(Modifier.height(20.dp))
        val pagerDescription = stringResource(
            Res.string.login_slide_position,
            pagerState.currentPage + 1,
            slides.size
        )
        Row(
            Modifier.semantics { contentDescription = pagerDescription },
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(slides.size) { index ->
                val selected = pagerState.currentPage == index
                val width by animateDpAsState(if (selected) 24.dp else 8.dp)
                val color by animateColorAsState(
                    if (selected) AiFinderPalette.Teal else AiFinderPalette.Teal.copy(alpha = 0.25f)
                )
                Box(Modifier.height(8.dp).width(width).background(color, CircleShape))
            }
        }
    }
}

@Composable
private fun SlideContent(slide: OnboardingSlide, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth()) {
        val illustrationModifier = Modifier.align(Alignment.CenterHorizontally)
            .widthIn(max = 480.dp).fillMaxWidth()
            .aspectRatio(770f / 520f)
            .clip(RoundedCornerShape(28.dp))
            .background(AiFinderPalette.IllustrationMint)
        when (slide) {
            OnboardingSlide.Welcome -> WelcomeIllustration(illustrationModifier.padding(12.dp))
            OnboardingSlide.Scan -> {
                val description = stringResource(Res.string.login_slide_scan_illustration_description)
                ScanIllustration(illustrationModifier.semantics(mergeDescendants = true) {
                    contentDescription = description
                })
            }
            OnboardingSlide.Find -> {
                val description = stringResource(Res.string.login_slide_find_illustration_description)
                FindIllustration(illustrationModifier.semantics(mergeDescendants = true) {
                    contentDescription = description
                })
            }
        }
        Spacer(Modifier.height(32.dp))
        SlideText(slide)
    }
}

@Composable
private fun SlideText(slide: OnboardingSlide, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth()) {
        SectionTitle(stringResource(slide.title), fontSize = 28.sp, lineHeight = 36.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            stringResource(slide.description),
            color = AiFinderPalette.Muted,
            fontSize = 16.sp,
            lineHeight = 23.sp
        )
    }
}

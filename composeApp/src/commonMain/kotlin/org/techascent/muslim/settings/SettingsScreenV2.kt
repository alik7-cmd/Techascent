package org.techascent.muslim.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.text_adhan_notification
import apphub.composeapp.generated.resources.text_adhan_notification_desc
import apphub.composeapp.generated.resources.text_school_of
import apphub.composeapp.generated.resources.text_school_suggestion
import apphub.composeapp.generated.resources.text_settings_about
import apphub.composeapp.generated.resources.text_settings_experience
import apphub.composeapp.generated.resources.text_settings_experience_desc
import apphub.composeapp.generated.resources.text_settings_prayer
import apphub.composeapp.generated.resources.text_settings_prayer_desc
import apphub.composeapp.generated.resources.title_settings
import apphub.composeapp.generated.resources.haptic_title
import apphub.composeapp.generated.resources.haptic_label
import apphub.composeapp.generated.resources.text_24_hr
import apphub.composeapp.generated.resources.text_24_hr_desc
import apphub.composeapp.generated.resources.text_language
import apphub.composeapp.generated.resources.text_our_mission
import apphub.composeapp.generated.resources.text_settings_about_this_app
import org.jetbrains.compose.resources.stringResource
import org.techascent.composa.cell.Cell
import org.techascent.composa.cell.center.CenterSlot
import org.techascent.composa.cell.left.LeftSlot
import org.techascent.composa.cell.right.RightSlot
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.theming.ComposaTheme
import org.techascent.composa.theming.color.ComposaBlue500
import org.techascent.composa.theming.color.ComposaBlue700
import org.techascent.composa.theming.color.ComposaPurple700
import org.techascent.muslim.AppLang
import org.techascent.muslim.rememberUrlLauncher
import org.techascent.muslim.common.toTextRes
import org.techascent.muslim.common.toVisibility
import org.techascent.muslim.settings.event.SettingsEvent
import org.techascent.muslim.settings.state.NavigationType
import org.techascent.muslim.settings.state.SettingsUiState
import org.techascent.shared.data.enum.School

// ─── V2 Settings screen ─────────────────────────────────────────────────────────

@Composable
internal fun SettingsScreenV2(
    uiState: SettingsUiState,
    schoolPreference: Int,
    hapticPreference: Boolean,
    adhanPreference: Boolean,
    is24HourFormat: Boolean,
    languagePreference: String,
    onUpdateSchool: (Int) -> Unit,
    onUpdateAdhanNotification: (Boolean) -> Unit,
    onUpdateTimePreference: (Boolean) -> Unit,
    onUpdateHaptic: (Boolean) -> Unit,
    onUpdateLanguage: (String) -> Unit,   // reserved for future in-app language picker
    onHandleEvent: (SettingsEvent) -> Unit,
    onNavigateAbout: () -> Unit,
    innerPadding: PaddingValues,
) {
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier
            .fillMaxSize()
            .background(ComposaTheme.color.backgroundAppBackground),
    ) { scaffoldPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = scaffoldPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding() + ComposaSpacing.ExtraLarge,
                start = innerPadding.calculateLeftPadding(LayoutDirection.Ltr),
                end = innerPadding.calculateRightPadding(LayoutDirection.Ltr),
            ),
            verticalArrangement = Arrangement.spacedBy(ComposaSpacing.Medium),
        ) {
            // ── Page header ─────────────────────────────────────────────
            item {
                SettingsHeader()
            }

            // ── Prayer section ──────────────────────────────────────────
            item {
                SectionCard(
                    emoji = "🕌",
                    title = stringResource(Res.string.text_settings_prayer),
                    subtitle = stringResource(Res.string.text_settings_prayer_desc),
                    accentColor = ComposaBlue700,
                ) {
                    Cell(
                        leftSlot = LeftSlot.Emoji(emoji = "🔊"),
                        centerSlot = CenterSlot.TitleWithLabel(
                            title = stringResource(Res.string.text_adhan_notification),
                            label = stringResource(Res.string.text_adhan_notification_desc),
                        ),
                        rightSlot = RightSlot.Switch(
                            checked = adhanPreference,
                            onCheckedChange = onUpdateAdhanNotification,
                        ),
                        backgroundColor = Color.Transparent,
                    )
                    ThinDivider()
                    Cell(
                        leftSlot = LeftSlot.Emoji(emoji = "🕑"),
                        centerSlot = CenterSlot.TitleWithLabel(
                            title = stringResource(Res.string.text_24_hr),
                            label = stringResource(Res.string.text_24_hr_desc),
                        ),
                        rightSlot = RightSlot.Switch(
                            checked = is24HourFormat,
                            onCheckedChange = onUpdateTimePreference,
                        ),
                        backgroundColor = Color.Transparent,
                    )

                    ThinDivider()

                    Cell(
                        leftSlot = LeftSlot.Emoji(emoji = "📐"),
                        centerSlot = CenterSlot.TitleWithLabel(
                            title = stringResource(Res.string.text_school_of),
                            label = stringResource(
                                School.fromCode(schoolPreference).toTextRes()
                            ),
                        ),
                        rightSlot = RightSlot.Switch(
                            checked = School.fromCode(schoolPreference).toVisibility(),
                            onCheckedChange = { isChecked ->
                                val code = if (isChecked) School.HANAFI.code else School.SHAFI.code
                                onUpdateSchool(code)
                            },
                        ),
                        backgroundColor = Color.Transparent,
                    )

                    // Hint box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = ComposaSpacing.Medium,
                                end = ComposaSpacing.Medium,
                                bottom = ComposaSpacing.Medium,
                            )
                            .clip(RoundedCornerShape(12.dp))
                            .background(ComposaTheme.color.backgroundInfoSubtle)
                            .padding(ComposaSpacing.Small),
                    ) {
                        Text(
                            text = stringResource(Res.string.text_school_suggestion),
                            style = ComposaTheme.typography.caption,
                            color = ComposaTheme.color.textNeutral,
                        )
                    }
                }
            }

            // ── Experience section ──────────────────────────────────────
            item {
                SectionCard(
                    emoji = "✨",
                    title = stringResource(Res.string.text_settings_experience),
                    subtitle = stringResource(Res.string.text_settings_experience_desc),
                    accentColor = ComposaPurple700,
                ) {
                    Cell(
                        leftSlot = LeftSlot.Emoji(emoji = "📳"),
                        centerSlot = CenterSlot.TitleWithLabel(
                            title = stringResource(Res.string.haptic_title),
                            label = stringResource(Res.string.haptic_label),
                        ),
                        rightSlot = RightSlot.Switch(
                            checked = hapticPreference,
                            onCheckedChange = onUpdateHaptic,
                        ),
                        backgroundColor = Color.Transparent,
                    )

                    ThinDivider()

                    val urlLauncher = rememberUrlLauncher()
                    val languages = AppLang.entries
                    val selectedLang = languages.find { it.code == languagePreference } ?: AppLang.English

                    Cell(
                        leftSlot = LeftSlot.Emoji(emoji = "🌐"),
                        centerSlot = CenterSlot.TitleWithLabel(
                            title = stringResource(Res.string.text_language),
                            label = stringResource(selectedLang.stringRes),
                        ),
                        rightSlot = RightSlot.Chevron(),
                        backgroundColor = Color.Transparent,
                        onClick = { urlLauncher.openLanguageSettings() },
                    )
                }
            }

            // ── About / Links section ───────────────────────────────────
            item {
                SectionCard(
                    emoji = "ℹ️",
                    title = stringResource(Res.string.text_settings_about),
                    subtitle = null,
                    accentColor = ComposaBlue500,
                ) {
                    // About This App row
                    Cell(
                        leftSlot = LeftSlot.Emoji(emoji = "💚"),
                        centerSlot = CenterSlot.TitleWithLabel(
                            title = stringResource(Res.string.text_settings_about_this_app),
                            label = stringResource(Res.string.text_our_mission),
                        ),
                        rightSlot = RightSlot.Chevron(),
                        backgroundColor = Color.Transparent,
                        onClick = onNavigateAbout,
                    )

                    ThinDivider()

                    uiState.links.listOfElements.forEachIndexed { index, item ->
                        val isExternal = item.navigationType == NavigationType.EXTERNAL
                        Cell(
                            leftSlot = LeftSlot.Emoji(
                                emoji = if (isExternal) "🔗" else "📄",
                            ),
                            centerSlot = if (item.subtitle != null) {
                                CenterSlot.TitleWithLabel(
                                    title = stringResource(item.title),
                                    label = stringResource(item.subtitle),
                                )
                            } else {
                                CenterSlot.Title(
                                    title = stringResource(item.title),
                                )
                            },
                            rightSlot = RightSlot.Chevron(
                                text = if (isExternal) "↗" else "›",
                            ),
                            backgroundColor = Color.Transparent,
                            onClick = {
                                onHandleEvent(
                                    SettingsEvent.OpenExternalLink(url = "https://aladhan.com")
                                )
                            },
                        )
                        if (index < uiState.links.listOfElements.lastIndex) {
                            ThinDivider()
                        }
                    }
                }
            }
        }
    }
}

// ─── Page Header ────────────────────────────────────────────────────────────────

@Composable
private fun SettingsHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ComposaSpacing.Medium)
            .padding(top = ComposaSpacing.Small),
    ) {
        Text(
            text = stringResource(Res.string.title_settings),
            style = ComposaTheme.typography.titleMediumEmphasized,
            color = ComposaTheme.color.textNeutral,
        )
    }
}

// ─── Section Card ───────────────────────────────────────────────────────────────

@Composable
private fun SectionCard(
    emoji: String,
    title: String,
    subtitle: String?,
    accentColor: Color,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ComposaSpacing.Medium)
            .clip(RoundedCornerShape(20.dp))
            .background(ComposaTheme.color.strokeNeutralSubtle.copy(alpha = 0.10f)),
    ) {
        // Section header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ComposaSpacing.Medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = emoji, fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = ComposaTheme.typography.subheadEmphasized,
                    color = ComposaTheme.color.textNeutral,
                )
                subtitle?.let {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = it,
                        style = ComposaTheme.typography.caption,
                        color = ComposaTheme.color.textNeutralSubtle,
                    )
                }
            }
        }

        ThinDivider()

        // Section body
        content()
    }
}


// ─── Thin Divider ───────────────────────────────────────────────────────────────

@Composable
private fun ThinDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = ComposaSpacing.Medium),
        thickness = 0.5.dp,
        color = ComposaTheme.color.strokeNeutralSubtle.copy(alpha = 0.4f),
    )
}


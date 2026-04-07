package org.techascent.muslim.settings

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
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
import apphub.composeapp.generated.resources.text_language
import apphub.composeapp.generated.resources.text_our_mission
import apphub.composeapp.generated.resources.text_settings_about_this_app
import org.jetbrains.compose.resources.stringResource
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.AppLang
import org.techascent.muslim.rememberUrlLauncher
import org.techascent.muslim.common.toTextRes
import org.techascent.muslim.common.toVisibility
import org.techascent.muslim.settings.event.SettingsEvent
import org.techascent.muslim.settings.state.NavigationType
import org.techascent.muslim.settings.state.SettingsItem
import org.techascent.muslim.settings.state.SettingsUiState
import org.techascent.shared.data.enum.School

// ─── V2 Settings screen ─────────────────────────────────────────────────────────

@Composable
internal fun SettingsScreenV2(
    uiState: SettingsUiState,
    schoolPreference: Int,
    hapticPreference: Boolean,
    adhanPreference: Boolean,
    languagePreference: String,
    onUpdateSchool: (Int) -> Unit,
    onUpdateAdhanNotification: (Boolean) -> Unit,
    onUpdateHaptic: (Boolean) -> Unit,
    onUpdateLanguage: (String) -> Unit,
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
                    accentColor = Color(0xFF1565C0),
                ) {
                    SettingsToggleRow(
                        emoji = "🔊",
                        title = stringResource(Res.string.text_adhan_notification),
                        description = stringResource(Res.string.text_adhan_notification_desc),
                        checked = adhanPreference,
                        onCheckedChange = onUpdateAdhanNotification,
                    )

                    ThinDivider()

                    SettingsToggleRow(
                        emoji = "📐",
                        title = stringResource(Res.string.text_school_of),
                        description = stringResource(
                            School.fromCode(schoolPreference).toTextRes()
                        ),
                        checked = School.fromCode(schoolPreference).toVisibility(),
                        onCheckedChange = { isChecked ->
                            val code = if (isChecked) School.HANAFI.code else School.SHAFI.code
                            onUpdateSchool(code)
                        },
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
                    accentColor = Color(0xFF7B1FA2),
                ) {
                    SettingsToggleRow(
                        emoji = "📳",
                        title = stringResource(Res.string.haptic_title),
                        description = stringResource(Res.string.haptic_label),
                        checked = hapticPreference,
                        onCheckedChange = onUpdateHaptic,
                    )

                    ThinDivider()

                    val urlLauncher = rememberUrlLauncher()
                    LanguagePickerRow(
                        currentLangCode = languagePreference,
                        onLanguageClicked = { urlLauncher.openLanguageSettings() },
                    )
                }
            }

            // ── About / Links section ───────────────────────────────────
            item {
                SectionCard(
                    emoji = "ℹ️",
                    title = stringResource(Res.string.text_settings_about),
                    subtitle = null,
                    accentColor = Color(0xFF00838F),
                ) {
                    // About This App row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(onClick = onNavigateAbout)
                            .padding(horizontal = ComposaSpacing.Medium, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = "💚", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(Res.string.text_settings_about_this_app),
                                style = ComposaTheme.typography.subhead,
                                color = ComposaTheme.color.textNeutral,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = stringResource(Res.string.text_our_mission),
                                style = ComposaTheme.typography.caption,
                                color = ComposaTheme.color.textNeutralSubtle,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        Spacer(modifier = Modifier.width(ComposaSpacing.Small))
                        Text(
                            text = "›",
                            fontSize = 18.sp,
                            color = ComposaTheme.color.textNeutralSubtle,
                        )
                    }

                    ThinDivider()

                    uiState.links.listOfElements.forEachIndexed { index, item ->
                        LinkRow(
                            item = item,
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

            item { Spacer(modifier = Modifier.size(ComposaSpacing.Large)) }
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

// ─── Toggle Row ─────────────────────────────────────────────────────────────────

@Composable
private fun SettingsToggleRow(
    emoji: String,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val trackColor by animateColorAsState(
        targetValue = if (checked)
            ComposaTheme.color.backgroundAction
        else
            ComposaTheme.color.strokeNeutralSubtle,
        animationSpec = tween(250),
        label = "switchTrack",
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = ComposaSpacing.Medium, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = emoji, fontSize = 22.sp)

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = ComposaTheme.typography.subhead,
                color = ComposaTheme.color.textNeutral,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = ComposaTheme.typography.caption,
                color = ComposaTheme.color.textNeutralSubtle,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(modifier = Modifier.width(ComposaSpacing.Small))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = trackColor,
                uncheckedTrackColor = trackColor,
            ),
        )
    }
}

// ─── Link Row ───────────────────────────────────────────────────────────────────

@Composable
private fun LinkRow(
    item: SettingsItem,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = ComposaSpacing.Medium, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val isExternal = item.navigationType == NavigationType.EXTERNAL
        Text(text = if (isExternal) "🔗" else "📄", fontSize = 20.sp)

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(item.title),
                style = ComposaTheme.typography.subhead,
                color = ComposaTheme.color.textNeutral,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            item.subtitle?.let { subtitleRes ->
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(subtitleRes),
                    style = ComposaTheme.typography.caption,
                    color = ComposaTheme.color.textNeutralSubtle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(modifier = Modifier.width(ComposaSpacing.Small))

        // Chevron
        Text(
            text = if (isExternal) "↗" else "›",
            fontSize = 18.sp,
            color = ComposaTheme.color.textNeutralSubtle,
        )
    }
}

// ─── Language Picker Row ────────────────────────────────────────────────────────

@Composable
private fun LanguagePickerRow(
    currentLangCode: String,
    onLanguageClicked: () -> Unit,
) {
    val languages = AppLang.entries
    val selectedLang = languages.find { it.code == currentLangCode } ?: AppLang.English

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onLanguageClicked)
            .padding(horizontal = ComposaSpacing.Medium, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = "🌐", fontSize = 22.sp)

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(Res.string.text_language),
                style = ComposaTheme.typography.subhead,
                color = ComposaTheme.color.textNeutral,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = stringResource(selectedLang.stringRes),
                style = ComposaTheme.typography.caption,
                color = ComposaTheme.color.textNeutralSubtle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(modifier = Modifier.width(ComposaSpacing.Small))

        Text(
            text = "›",
            fontSize = 18.sp,
            color = ComposaTheme.color.textNeutralSubtle,
        )
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


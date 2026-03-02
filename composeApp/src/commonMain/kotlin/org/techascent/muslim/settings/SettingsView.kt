package org.techascent.muslim.settings

import Header
import SettingsCell
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.unit.LayoutDirection
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.haptic_label
import apphub.composeapp.generated.resources.haptic_title
import apphub.composeapp.generated.resources.text_adhan_notification
import apphub.composeapp.generated.resources.text_adhan_notification_desc
import apphub.composeapp.generated.resources.text_school_of
import apphub.composeapp.generated.resources.text_school_suggestion
import apphub.composeapp.generated.resources.title_settings
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.techascent.composa.appbar.TopAppBar
import org.techascent.composa.card.ComposaCardFrame
import org.techascent.composa.cell.Cell
import org.techascent.composa.cell.center.CenterSlot
import org.techascent.composa.cell.left.LeftSlot
import org.techascent.composa.cell.right.RightSlot
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.messabebox.MessageBox
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.common.toTextRes
import org.techascent.muslim.common.toVisibility
import org.techascent.muslim.settings.event.SettingsEvent
import org.techascent.muslim.settings.state.SettingsUiState
import org.techascent.shared.data.enum.School

@OptIn(KoinExperimentalAPI::class)
@Composable
fun SettingsView(
    innerPadding: PaddingValues
) {
    val viewModel = koinViewModel<SettingsViewModel>()

    ComposaTheme {
        val uriHandler = LocalUriHandler.current
        LaunchedEffect(key1 = Unit) {
            viewModel.event.collect {
                handleEvent(
                    event = it,
                    uriHandler = uriHandler
                )
            }
        }

        SettingsScreen(
            viewModel = viewModel,
            innerPadding = innerPadding,
            onHandleEvent = viewModel::onHandleEvent
        )
    }
}

@Composable
private fun SettingsScreen(
    viewModel: SettingsViewModel,
    innerPadding: PaddingValues,
    onHandleEvent: (SettingsEvent) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val schoolPreference by viewModel.schoolPreference.collectAsState()
    val hapticPreference by viewModel.hapticPreference.collectAsState()
    val adhanPreference by viewModel.adhanPreference.collectAsState()
    SettingsContent(
        uiState = uiState,
        schoolPreference = schoolPreference,
        hapticPreference = hapticPreference,
        adhanPreference = adhanPreference,
        innerPadding = innerPadding,
        onUpdateSchool = viewModel::updateSchoolPreference,
        onUpdateHaptic = viewModel::onUpdateHaptic,
        onUpdateAdhanNotification = viewModel::onUpdateAdhanNotification,
        onHandleEvent = onHandleEvent
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    uiState: SettingsUiState,
    schoolPreference: Int,
    hapticPreference: Boolean,
    adhanPreference: Boolean,
    onUpdateSchool: (Int) -> Unit,
    onUpdateAdhanNotification: (Boolean) -> Unit,
    onUpdateHaptic: (Boolean) -> Unit,
    onHandleEvent: (SettingsEvent) -> Unit,
    innerPadding: PaddingValues
) {
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier
            .fillMaxSize()
            .background(color = ComposaTheme.color.backgroundAppBackground),
        topBar = {
            TopAppBar(
                title = stringResource(Res.string.title_settings),
                navigationIcon = null,
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = it.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding(),
                start = innerPadding.calculateLeftPadding(LayoutDirection.Ltr),
                end = innerPadding.calculateRightPadding(LayoutDirection.Ltr)
            ),
            verticalArrangement = spacedBy(ComposaSpacing.Medium)
        ) {

            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = ComposaSpacing.Medium),
                    verticalArrangement = Arrangement.spacedBy(ComposaSpacing.Small)
                ) {
                    ComposaCardFrame(
                        borderColor = ComposaTheme.color.strokeNeutralSubtle,
                        content = {
                            Cell(
                                leftSlot = LeftSlot.None,
                                centerSlot = CenterSlot.TitleWithLabel(
                                    title = stringResource(Res.string.haptic_title),
                                    label = stringResource(Res.string.haptic_label),
                                ),
                                rightSlot = RightSlot.Switch(
                                    checked = hapticPreference,
                                    onCheckedChange = { isChecked ->
                                        onUpdateHaptic(isChecked)
                                    }
                                )
                            )
                            Cell(
                                leftSlot = LeftSlot.None,
                                centerSlot = CenterSlot.TitleWithLabel(
                                    title = stringResource(Res.string.text_adhan_notification),
                                    label = stringResource(Res.string.text_adhan_notification_desc)
                                ),
                                rightSlot = RightSlot.Switch(
                                    checked = adhanPreference,
                                    onCheckedChange = { isChecked ->
                                        onUpdateAdhanNotification(isChecked)
                                    }
                                )
                            )
                            Cell(
                                leftSlot = LeftSlot.None,
                                centerSlot = CenterSlot.TitleWithLabel(
                                    label = stringResource(Res.string.text_school_of),
                                    title = stringResource(
                                        School.fromCode(schoolPreference).toTextRes()
                                    ),

                                    ),
                                rightSlot = RightSlot.Switch(
                                    checked = School.fromCode(schoolPreference).toVisibility(),
                                    onCheckedChange = { isChecked ->
                                        val code =
                                            if (isChecked) School.HANAFI.code else School.SHAFI.code
                                        onUpdateSchool(code)
                                    }
                                )
                            )
                        }
                    )
                    MessageBox(
                        modifier = Modifier.fillMaxWidth(),
                        message = stringResource(resource = Res.string.text_school_suggestion),
                    )
                }
            }

            item {
                Header(text = stringResource(resource = uiState.links.title))
                ComposaCardFrame(
                    modifier = Modifier.padding(horizontal = ComposaSpacing.Medium),
                    borderColor = ComposaTheme.color.strokeNeutralSubtle,
                    content = {
                        uiState.links.listOfElements.forEach { item ->
                            SettingsCell(
                                item = item,
                                onClick = { onHandleEvent(SettingsEvent.OpenExternalLink(url = "https://aladhan.com")) }
                            )
                        }
                    }
                )
            }
        }
    }

}

private fun handleEvent(event: SettingsEvent, uriHandler: UriHandler) {
    when (event) {
        is SettingsEvent.OpenExternalLink -> uriHandler.openUri(event.url)
    }
}
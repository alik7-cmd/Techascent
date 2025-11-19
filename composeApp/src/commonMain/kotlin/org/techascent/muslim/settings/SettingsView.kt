package org.techascent.muslim.settings

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.LayoutDirection
import apphub.composeapp.generated.resources.Res
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
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.common.toTextRes
import org.techascent.muslim.common.toVisibility
import org.techascent.shared.data.enum.School

@OptIn(KoinExperimentalAPI::class)
@Composable
fun SettingsView(
    innerPadding: PaddingValues
) {
    val viewModel = koinViewModel<SettingsViewModel>()

    ComposaTheme {
        SettingsScreen(
            viewModel = viewModel,
            innerPadding = innerPadding
        )
    }
}

@Composable
private fun SettingsScreen(
    viewModel: SettingsViewModel,
    innerPadding: PaddingValues
) {
    val uiState by viewModel.uiState.collectAsState()
    val schoolPreference by viewModel.schoolPreference.collectAsState()
    SettingsContent(
        schoolPreference = schoolPreference,
        innerPadding = innerPadding,
        onUpdateSchool = viewModel::updateSchoolPreference
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    schoolPreference: Int,
    onUpdateSchool: (Int) -> Unit,
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
                                    title = stringResource(Res.string.text_school_of),
                                    label = stringResource(
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
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(resource = Res.string.text_school_suggestion),
                        style = ComposaTheme.typography.footnote,
                        color = ComposaTheme.color.textNeutral
                    )
                }

            }
            //appearanceContent(appearanceUiModel = uiState.appearanceUiModel)
            //ratingContent(aboutUsUiModel = uiState.aboutUsUiModel)
        }
    }

}
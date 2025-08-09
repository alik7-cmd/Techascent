package org.techascent.muslim.method

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.method.state.MethodUiState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import org.techascent.shared.data.enum.PrayerCalculationMethod

@OptIn(KoinExperimentalAPI::class)
@Composable
fun MethodSelectionView() {
    val viewModel = koinViewModel<MethodViewModel>()

    ComposaTheme {
        MethodSelectionScreen(
            viewModel = viewModel
        )
    }
}

@Composable
private fun MethodSelectionScreen(
    viewModel: MethodViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    MethodSelectionContent(
        uiState = uiState
    )
}

@Composable
private fun MethodSelectionContent(
    uiState: MethodUiState
) {
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier
            .fillMaxSize()
            .background(color = ComposaTheme.color.backgroundAppBackground),
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = innerPadding,
            verticalArrangement = spacedBy(ComposaSpacing.Medium)
        ) {
            item {
                MyRadioGroup(
                    listOfMethods = uiState.listOfMethods
                )
            }

        }
    }

}

@Composable
fun MyRadioGroup(
    listOfMethods: List<PrayerCalculationMethod>
) {

    val (selectedOption, onOptionSelected) = remember { mutableStateOf(listOfMethods[0]) }

    Column(Modifier.selectableGroup()) {
        listOfMethods.forEach { text ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .selectable(
                        selected = (text == selectedOption),
                        onClick = { onOptionSelected(text) },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (text == selectedOption),
                    onClick = null // null recommended for accessibility with selectable
                )
                Text(
                    text = text.toString(),
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}
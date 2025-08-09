package org.techascent.muslim.city

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.city.state.CityPickerUiState

@OptIn(KoinExperimentalAPI::class)
@Composable
fun CityPickerView() {
    val viewModel = koinViewModel<CityPickerViewModel>()
    CityPickerScreen(viewModel = viewModel)
}

@Composable
private fun CityPickerScreen(
    viewModel: CityPickerViewModel
) {
    val state by viewModel.uiState.collectAsState()
    CityPickerContent(uiState = state)
}

@Composable
private fun CityPickerContent(
    uiState: CityPickerUiState
){
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
            when (uiState) {
                is CityPickerUiState.Loading -> Unit
                is CityPickerUiState.Success -> cityPickerContent(uiState = uiState)
            }
        }
    }

}

private fun LazyListScope.cityPickerContent(
    uiState: CityPickerUiState.Success
){
    item {
        Text(
            text = "country found ${uiState.data.size}"
        )
    }
}
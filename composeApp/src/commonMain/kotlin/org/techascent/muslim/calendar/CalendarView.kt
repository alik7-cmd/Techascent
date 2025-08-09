package org.techascent.muslim.calendar

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.datetime.LocalDate
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.calendar.composable.horizontalDayPicker
import org.techascent.muslim.calendar.composable.loadingScreen
import org.techascent.muslim.calendar.composable.successContent
import org.techascent.muslim.calendar.state.CalendarUiState
import org.techascent.muslim.common.currentDate
import org.techascent.muslim.prayer.composable.errorContent

@OptIn(KoinExperimentalAPI::class)
@Composable
fun CalendarView() {
    val viewModel = koinViewModel<CalendarViewModel>()
    ComposaTheme {
        CalendarScreen(
            viewModel = viewModel
        )
    }
}

@Composable
private fun CalendarScreen(
    viewModel: CalendarViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    CalendarContent(
        uiState = uiState,
        onFetchPrayer = viewModel::fetchPrayer
    )
}

@Composable
private fun CalendarContent(
    uiState: CalendarUiState,
    onFetchPrayer: (String) -> Unit = {},

    ) {
    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        modifier = Modifier
            .fillMaxSize()
            .background(color = ComposaTheme.color.backgroundAppBackground),
    ) {
        var selectedDate by remember { mutableStateOf<LocalDate?>(currentDate) }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = it,
            verticalArrangement = spacedBy(ComposaSpacing.Medium)
        ) {
            horizontalDayPicker(
                year = 2025,
                month = 8,
                selectedDate = selectedDate,
                onDateSelected = {
                    selectedDate = it
                },
                onFetchPrayer = onFetchPrayer
            )
            when (uiState) {
                is CalendarUiState.Error -> errorContent()
                is CalendarUiState.Loading -> loadingScreen()
                is CalendarUiState.Success -> successContent(
                    uiModel = uiState.data
                )
            }
        }
    }
}




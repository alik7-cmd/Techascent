package org.techascent.muslim.calendar.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.datetime.until
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.theming.ComposaTheme
import org.techascent.muslim.common.lengthOfMonth
import org.techascent.muslim.common.toDayMonthYearString
import org.techascent.muslim.common.yearMonth

private val DATE_CELL_WIDTH = 60.dp

internal fun LazyListScope.horizontalDayPicker(
    year: Int,
    month: Int,
    modifier: Modifier = Modifier,
    selectedDate: LocalDate? = null,
    onDateSelected: (LocalDate) -> Unit,
    onFetchPrayer: (String) -> Unit = {},
) {
    item {
        val context = LocalDensity.current
        val daysInMonth = remember(year, month) {
            yearMonth(year, month).lengthOfMonth()
        }

        val startDate = LocalDate(year, month, 1)
        var selected by remember { mutableStateOf(selectedDate ?: startDate) }

        val scrollState = rememberScrollState()

        LaunchedEffect(selected) {
            val index = startDate.until(selected, DateTimeUnit.DAY)
            val itemWidth = 60.dp + ComposaSpacing.ExtraSmall * 2
            val offset = with(receiver = context) { itemWidth.toPx() * index }
            scrollState.animateScrollTo(offset.toInt())
        }

        Row(
            modifier = modifier
                .horizontalScroll(scrollState)
                .padding(ComposaSpacing.Small)
        ) {
            for (i in 0 until daysInMonth) {
                val date = startDate.plus(i, DateTimeUnit.DAY)
                val dayName = date.dayOfWeek.name.take(3)
                val isSelected = date == selected

                Column(
                    modifier = Modifier
                        .padding(horizontal = ComposaSpacing.ExtraSmall)
                        .width(width = DATE_CELL_WIDTH)
                        .clip(shape = RoundedCornerShape(ComposaSpacing.Small))
                        .background(
                            if (isSelected)
                                ComposaTheme.color.backgroundActionAlternative
                            else
                                ComposaTheme.color.strokeNeutralSubtle
                        )
                        .clickable {
                            selected = date
                            onDateSelected(date)
                            onFetchPrayer(date.toDayMonthYearString())
                        }
                        .padding(vertical = ComposaSpacing.Small),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = dayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = ComposaTheme.color.textNeutralOnDark,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(ComposaSpacing.ExtraSmall))
                    Text(
                        text = date.dayOfMonth.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = ComposaTheme.color.textNeutralOnDark,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}


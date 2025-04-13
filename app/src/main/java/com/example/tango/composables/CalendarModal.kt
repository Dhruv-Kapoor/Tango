package com.example.tango.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.tango.R
import com.example.tango.utils.Utils.conditional
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.core.yearMonth
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale
import kotlin.random.Random

@Composable
fun DaysOfWeekTitle(daysOfWeek: List<DayOfWeek>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        for (dayOfWeek in daysOfWeek) {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                color = Color.Gray,
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
            )
        }
    }
}

@Composable
fun Day(
    day: CalendarDay,
    isSelected: Boolean = false,
    isAttempted: Boolean = Random.nextBoolean(),
    isEnabled: Boolean = true,
    number: Int = 123,
    onDayClick: (Int) -> Unit
) {
    val shape by remember { mutableStateOf(RoundedCornerShape(8.dp)) }
    if (day.position == DayPosition.MonthDate) {
        Box(
            modifier = Modifier
                .aspectRatio(0.68f)
                .padding(1.dp)
                .clip(shape)
                .conditional(isSelected) {
                    border(2.dp, MaterialTheme.colorScheme.primary, shape)
                }
                .conditional(isAttempted) {
                    background(color = MaterialTheme.colorScheme.primaryContainer)
                }
                .conditional(!isAttempted) {
                    background(color = MaterialTheme.colorScheme.onPrimary)
                }
                .conditional(!isEnabled) {
                    background(color = colorResource(R.color.disabled_bg))
                }
                .clickable(
                    enabled = isEnabled,
                    onClick = { onDayClick(number) }
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(text = day.date.dayOfMonth.toString(), color = Color.Black)
                if (isEnabled) {
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(text = "#$number", fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun Calendar(
    selectedNumber: Int,
    minDate: LocalDate,
    maxDate: LocalDate,
    maxNumber: Int,
    attemptedNumbers: Set<Int>,
    onDayClick: (Int) -> Unit,
) {
    fun getNumber(date: LocalDate): Int {
        return maxNumber - (maxDate.dayOfYear - date.dayOfYear)
    }

    fun getDateFromNumber(number: Int): LocalDate {
        return maxDate.plusDays((maxNumber - number).toLong())
    }

    val currentMonth = remember { getDateFromNumber(selectedNumber).yearMonth }
    val startMonth = remember { minDate.yearMonth }
    val endMonth = remember { maxDate.yearMonth }
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() }

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek
    )
    val daysOfWeek = remember { daysOfWeek() }

    Column(modifier = Modifier.padding(24.dp)) {
        HorizontalCalendar(
            state = state,
            dayContent = { day ->
                val number = remember { getNumber(day.date) }
                Day(
                    day, isSelected = selectedNumber == number, onDayClick = onDayClick,
                    isEnabled = day.position == DayPosition.MonthDate && day.date >= minDate && day.date <= maxDate,
                    number = number, isAttempted = number in attemptedNumbers
                )
            },
            monthHeader = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${it.yearMonth.month}, ${it.yearMonth.year}")
                    Spacer(Modifier.size(16.dp))
                    DaysOfWeekTitle(daysOfWeek = daysOfWeek)
                }
            },
        )
    }

}

@Composable
fun CalendarModal(
    selectedNumber: Int,
    minDate: LocalDate,
    maxDate: LocalDate,
    maxNumber: Int,
    attemptedNumbers: Set<Int>,
    onDismissRequest: () -> Unit,
    onDayClick: (Int) -> Unit
) {
    Dialog(
        onDismissRequest = { onDismissRequest() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background,
            )
        ) {
            Calendar(
                minDate = minDate,
                maxDate = maxDate,
                maxNumber = maxNumber,
                onDayClick = onDayClick,
                attemptedNumbers = attemptedNumbers,
                selectedNumber = selectedNumber
            )
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun CalendarModalPreview() {
    CalendarModal(
        onDismissRequest = {},
        onDayClick = {},
        minDate = LocalDate.of(2025, 4, 3),
        maxDate = LocalDate.of(2025, 4, 13),
        maxNumber = 347,
        selectedNumber = 344,
        attemptedNumbers = setOf(347, 346)
    )
}
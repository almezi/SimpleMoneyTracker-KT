package id.almezi.simplemoneytracker_kt.ui.transaction

import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import id.almezi.simplemoneytracker_kt.SakuStrings
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import id.almezi.simplemoneytracker_kt.ui.designsystem.JAKARTA

/**
 * Cross-platform replacement for the old `android.app.DatePickerDialog`.
 *
 * Compose Multiplatform ships a composable `DatePickerDialog`, so the platform
 * dialog, its `Context`, and the `java.util.Calendar` arithmetic behind it are
 * all gone.
 *
 * Timezone is the subtle part. The Material date picker works in **UTC**
 * milliseconds, while Saku stores **midnight Asia/Jakarta**. Converting the
 * millis naively shifts the date by a day either side of UTC midnight, so the
 * selected UTC calendar date is read explicitly and re-anchored to Jakarta.
 *
 * No future dates: [maxMillis] bounds [SelectableDates.isSelectableDate].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SakuDatePickerDialog(
    initialMillis: Long,
    maxMillis: Long,
    onDismiss: () -> Unit,
    onDateSelected: (Long) -> Unit,
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                utcTimeMillis <= maxMillis + UTC_DAY_MILLIS
        },
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    state.selectedDateMillis?.let { selected ->
                        onDateSelected(jakartaMidnightOf(selected))
                    }
                    onDismiss()
                }
            ) { Text(SakuStrings.ok) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(SakuStrings.cancel) }
        },
    ) {
        DatePicker(state = state)
    }
}

/**
 * Converts the picker's UTC milliseconds into the instant Saku stores:
 * midnight of the *same calendar date* in Asia/Jakarta.
 */
private fun jakartaMidnightOf(utcMillis: Long): Long {
    val utcDate = Instant.fromEpochMilliseconds(utcMillis).toLocalDateTime(TimeZone.UTC).date
    return LocalDate(utcDate.year, utcDate.monthNumber, utcDate.dayOfMonth)
        .atStartOfDayIn(JAKARTA)
        .toEpochMilliseconds()
}

/** One day of slack so the last selectable day is not rejected at UTC midnight. */
private const val UTC_DAY_MILLIS = 24L * 60L * 60L * 1000L
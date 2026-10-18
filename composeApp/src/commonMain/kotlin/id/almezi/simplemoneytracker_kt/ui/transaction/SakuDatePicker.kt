package id.almezi.simplemoneytracker_kt.ui.transaction

import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import id.almezi.simplemoneytracker_kt.SakuStrings
import id.almezi.simplemoneytracker_kt.TestTags
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
 * No future dates: [maxMillis] is the current instant, and
 * [isSelectableUtcDate] rejects every UTC calendar date after the Jakarta date
 * it falls on.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SakuDatePickerDialog(
    initialMillis: Long,
    maxMillis: Long,
    onDismiss: () -> Unit,
    onDateSelected: (Long) -> Unit,
    testTag: String = TestTags.TAMBAH_DATE_PICKER,
) {
    val state = rememberDatePickerState(
        // Saku stores Jakarta midnight; the picker reads a UTC date out of this,
        // so without the conversion it opened on the previous day every morning.
        initialSelectedDateMillis = utcMidnightOf(initialMillis),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                isSelectableUtcDate(maxMillis, utcTimeMillis)
        },
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag(testTag),
        confirmButton = {
            TextButton(
                onClick = {
                    state.selectedDateMillis?.let { selected ->
                        onDateSelected(jakartaMidnightOf(selected))
                    }
                    onDismiss()
                },
                modifier = Modifier.testTag(TestTags.TAMBAH_DATE_PICKER_OK),
            ) { Text(SakuStrings.ok) }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag(TestTags.TAMBAH_DATE_PICKER_CANCEL),
            ) { Text(SakuStrings.cancel) }
        },
    ) {
        DatePicker(state = state)
    }
}

/**
 * Converts the picker's UTC milliseconds into the instant Saku stores:
 * midnight of the *same calendar date* in Asia/Jakarta.
 */
internal fun jakartaMidnightOf(utcMillis: Long): Long {
    val utcDate = Instant.fromEpochMilliseconds(utcMillis).toLocalDateTime(TimeZone.UTC).date
    return LocalDate(utcDate.year, utcDate.monthNumber, utcDate.dayOfMonth)
        .atStartOfDayIn(JAKARTA)
        .toEpochMilliseconds()
}

/** The UTC midnight of the same calendar date the instant falls on in Jakarta. */
internal fun utcMidnightOf(jakartaMillis: Long): Long {
    val jakartaDate = Instant.fromEpochMilliseconds(jakartaMillis).toLocalDateTime(JAKARTA).date
    return LocalDate(jakartaDate.year, jakartaDate.monthNumber, jakartaDate.dayOfMonth)
        .atStartOfDayIn(TimeZone.UTC)
        .toEpochMilliseconds()
}

// No future dates (`UI_SPEC.md:13`). The picker reports UTC midnights and the
// rule is stated in Jakarta days, so both sides become a date first. The old
// bound was `maxMillis + one day`, which let tomorrow through for most of the
// Jakarta day.
internal fun isSelectableUtcDate(nowMillis: Long, utcTimeMillis: Long): Boolean {
    val jakartaToday = Instant.fromEpochMilliseconds(nowMillis).toLocalDateTime(JAKARTA).date
    val utcDate = Instant.fromEpochMilliseconds(utcTimeMillis).toLocalDateTime(TimeZone.UTC).date
    return utcDate <= jakartaToday
}
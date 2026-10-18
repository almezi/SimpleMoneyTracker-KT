package id.almezi.simplemoneytracker_kt.ui.transaction

import id.almezi.simplemoneytracker_kt.ui.designsystem.JAKARTA
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

/**
 * Guards `UI_SPEC.md:13`: no future dates.
 *
 * The Material date picker hands out **UTC** midnights while Saku works in
 * Asia/Jakarta, so the bound has to be a Jakarta date compared against a UTC
 * calendar date. The old code compared the picker against the current *instant*
 * plus a day of slack, which from 07:00 Jakarta onwards made tomorrow selectable.
 */
class SakuDatePickerBoundsTest {

    /** 2026-10-17 10:00 Asia/Jakarta, i.e. 03:00 UTC. */
    private val lateMorning = utcMillisOf(2026, 10, 17, 3, 0)

    private fun utcMillisOf(year: Int, month: Int, day: Int, hour: Int = 0, minute: Int = 0): Long =
        pickerMillis(year, month, day) + hour * 3_600_000L + minute * 60_000L

    /** The picker's UTC midnight for a calendar date, which is what it reports. */
    private fun pickerMillis(year: Int, month: Int, day: Int): Long =
        LocalDate(year, month, day).atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()

    @Test
    fun `today is selectable`() {
        assertTrue(isSelectableUtcDate(lateMorning, pickerMillis(2026, 10, 17)))
    }

    @Test
    fun `tomorrow is not selectable late in the Jakarta day`() {
        assertFalse(isSelectableUtcDate(lateMorning, pickerMillis(2026, 10, 18)))
    }

    @Test
    fun `tomorrow is not selectable at the end of the Jakarta day`() {
        val lateEvening = utcMillisOf(2026, 10, 16, 16, 0) // 2026-10-16 23:00 UTC = 17 Oct 06:00 Jakarta
        assertFalse(isSelectableUtcDate(lateEvening, pickerMillis(2026, 10, 18)))
    }

    @Test
    fun `tomorrow is not selectable right after Jakarta midnight`() {
        val justAfterMidnight = utcMillisOf(2026, 10, 16, 17, 30) // 17 Oct 00:30 Jakarta
        assertTrue(isSelectableUtcDate(justAfterMidnight, pickerMillis(2026, 10, 17)))
        assertFalse(isSelectableUtcDate(justAfterMidnight, pickerMillis(2026, 10, 18)))
    }

    @Test
    fun `yesterday and earlier stay selectable`() {
        assertTrue(isSelectableUtcDate(lateMorning, pickerMillis(2026, 10, 16)))
        assertTrue(isSelectableUtcDate(lateMorning, pickerMillis(2026, 1, 1)))
    }

    @Test
    fun `the bound is the Jakarta date regardless of the hour of the day`() {
        val beforeJakartaMidnight = utcMillisOf(2026, 10, 16, 15, 0) // 16 Oct 22:00 Jakarta
        // Still 16 Oct in Jakarta, so the 17 Oct cell must already be rejected.
        assertFalse(isSelectableUtcDate(beforeJakartaMidnight, pickerMillis(2026, 10, 17)))
        assertTrue(isSelectableUtcDate(beforeJakartaMidnight, pickerMillis(2026, 10, 16)))
    }

    @Test
    fun `a picked UTC midnight converts to Jakarta midnight of the same date`() {
        assertEquals(
            LocalDate(2026, 10, 17).atStartOfDayIn(JAKARTA).toEpochMilliseconds(),
            jakartaMidnightOf(pickerMillis(2026, 10, 17)),
        )
    }

    @Test
    fun `the Jakarta date of an instant drives the bound`() {
        val instant = Instant.fromEpochMilliseconds(lateMorning).toLocalDateTime(JAKARTA)
        assertEquals(LocalDate(2026, 10, 17), instant.date)
    }
}
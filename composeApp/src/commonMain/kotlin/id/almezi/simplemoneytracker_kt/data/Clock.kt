package id.almezi.simplemoneytracker_kt.data

/**
 * The single time source for the whole app.
 *
 * `AGENTS.md` forbids calling the system clock directly: every timestamp must
 * come through this interface so tests can pin it. The only implementation that
 * touches the system clock lives here.
 */
interface Clock {
    fun currentTimeMillis(): Long
}

/**
 * Production clock.
 *
 * Uses `kotlin.time.Clock.System` from the Kotlin standard library rather than
 * `System.currentTimeMillis()`, so it compiles on both JVM and Kotlin/Native.
 * (`kotlinx.datetime.Clock` does not exist in the pinned 0.6.0, and importing
 * it would shadow this file's own `Clock` interface anyway.)
 */
object RealClock : Clock {
    override fun currentTimeMillis(): Long =
        kotlin.time.Clock.System.now().toEpochMilliseconds()
}
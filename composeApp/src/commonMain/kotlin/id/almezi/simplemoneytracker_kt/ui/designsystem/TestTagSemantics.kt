package id.almezi.simplemoneytracker_kt.ui.designsystem

import androidx.compose.ui.Modifier

/**
 * Re-arms `testTagsAsResourceId` for compositions rooted outside
 * MainActivity (bottom sheets, dialogs), where the flag set at the app root
 * does not reach and testTags would vanish for automation.
 *
 * Android needs the semantics flag; iOS maps testTag to
 * accessibilityIdentifier natively, so it is a no-op there.
 */
expect fun Modifier.automationTestTags(): Modifier

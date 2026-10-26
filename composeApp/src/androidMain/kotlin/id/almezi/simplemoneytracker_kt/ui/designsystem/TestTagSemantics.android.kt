package id.almezi.simplemoneytracker_kt.ui.designsystem

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId

actual fun Modifier.automationTestTags(): Modifier =
    semantics { testTagsAsResourceId = true }

package id.almezi.simplemoneytracker_kt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuTheme

/**
 * Android entry point.
 *
 * Deliberately thin: everything the user sees lives in the shared `SakuApp` in
 * :composeApp, so iOS runs the identical shell. This class only supplies the
 * container and the ready flag.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val app = LocalContext.current.applicationContext as SimpleMoneyTrackerApp
            val isReady by app.isReady.collectAsState()

            // Screens reach the object graph through this CompositionLocal
            // rather than casting LocalContext to the Application, which cannot
            // compile in commonMain.
            ProvideSakuContainer(container = app.container) {
                SakuTheme {
                    // Compose exposes testTag as an Android resource-id only when
                    // this flag is on, which is what lets the automation suite
                    // resolve the tags as accessibility IDs on Android. iOS maps
                    // testTag to accessibilityIdentifier without any help, so this
                    // exists purely to keep the two platforms on one locator rule.
                    Box(modifier = Modifier.semantics { testTagsAsResourceId = true }) {
                        SakuApp(ready = isReady)
                    }
                }
            }
        }
    }
}
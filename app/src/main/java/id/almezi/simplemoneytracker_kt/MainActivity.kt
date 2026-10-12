package id.almezi.simplemoneytracker_kt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import id.almezi.simplemoneytracker_kt.ui.cadangan.CadanganScreen
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuTheme

/**
 * Android entry point.
 *
 * Deliberately thin: everything the user sees lives in the shared `SakuApp` in
 * :composeApp, so iOS runs the identical shell. This class only supplies the
 * Android-specific pieces — the Application container, the ready flag, and the
 * Android-only backup screen.
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
                    SakuApp(
                        ready = isReady,
                        cadanganContent = { onBack, onAddTransaction ->
                            CadanganScreen(
                                onBack = onBack,
                                onAddTransaction = onAddTransaction,
                            )
                        },
                    )
                }
            }
        }
    }
}
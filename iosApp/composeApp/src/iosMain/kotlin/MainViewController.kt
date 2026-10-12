package spike

import androidx.compose.ui.window.ComposeUIViewController
import saku.SakuDaftarScreen
import saku.SakuTheme

// Called from Swift: MainViewControllerKt.MainViewController()
fun MainViewController() = ComposeUIViewController {
    SakuTheme {
        SakuDaftarScreen()
    }
}
package id.almezi.simplemoneytracker_kt.ui.tambah

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import id.almezi.simplemoneytracker_kt.R
import id.almezi.simplemoneytracker_kt.TestTags
import id.almezi.simplemoneytracker_kt.ui.components.EmptyState
import id.almezi.simplemoneytracker_kt.ui.components.ScreenHeader
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuSize
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuSpace
import id.almezi.simplemoneytracker_kt.ui.designsystem.SakuTheme

@Composable
fun KelolaKategoriPlaceholderScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SakuTheme.colors.bg)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = SakuSpace.screenHorizontal)
            .padding(bottom = SakuSpace.screenBottom),
    ) {
        ScreenHeader(
            title = stringResource(R.string.kelola_kategori),
            onBack = onBack,
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = SakuSize.navReserve),
            verticalArrangement = Arrangement.Center,
        ) {
            EmptyState(
                title = stringResource(R.string.kelola_kategori),
                body = stringResource(R.string.kelola_kategori_placeholder_body),
                modifier = Modifier.testTag(TestTags.TAMBAH_LINK_KELOLA_KATEGORI),
            )
        }
    }
}

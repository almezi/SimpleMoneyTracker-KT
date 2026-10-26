package id.almezi.simplemoneytracker_kt.ui.cadangan

import id.almezi.simplemoneytracker_kt.SakuStrings
import id.almezi.simplemoneytracker_kt.format
import id.almezi.simplemoneytracker_kt.TestTags
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.List as AutoMirroredList
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ScreenBg = Color(0xFF0D0C11)
private val CardBg = Color(0xFF15141A)
private val TileBg = Color(0xFF1B1A22)
private val TextPrimary = Color(0xFFF2F1F5)
private val TextSecondary = Color(0xFF75747F)
private val TextTertiary = Color(0xFF5E5D6A)
private val Hairline = Color(0x14FFFFFF)
private val Accent = Color(0xFF5B4BDB)
private val Success = Color(0xFF4ADE9B)
private val Warning = Color(0xFFE0A458)
private val LinkAccent = Color(0xFF8B7FF0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CadanganScreen(
    transactionCount: Int,
    lastBackupDate: String?,
    onNavigateBack: () -> Unit,
    onExportCsv: () -> Unit,
    onExportJson: () -> Unit,
    onImportBackup: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val showDetailsState = remember { mutableStateOf(false) }

    val pillTint = if (lastBackupDate != null) Success else Warning
    val pillLabel = if (lastBackupDate != null) SakuStrings.cadangan_status_safe else SakuStrings.cadangan_status_none
    val dateValue = lastBackupDate ?: SakuStrings.cadangan_last_backup_none_short

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScreenBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // 1. Top bar
        Row(
            modifier = Modifier.padding(bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clickable { onNavigateBack() }
                    .background(TileBg, RoundedCornerShape(14.dp))
                    .border(1.dp, Hairline, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    modifier = Modifier.size(18.dp),
                    tint = TextPrimary,
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = SakuStrings.cadangan_title,
                style = TextStyle(
                    fontSize = 21.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                ),
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clickable { showDetailsState.value = true }
                    .background(Color.Transparent, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    modifier = Modifier.size(19.dp),
                    tint = TextTertiary,
                )
            }
        }

        // 2. Backup status card
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(CardBg)
                .border(1.dp, Hairline, RoundedCornerShape(20.dp))
                .padding(16.dp),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFF0F2A21), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Success,
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = SakuStrings.cadangan_last_backup_label,
                        style = TextStyle(
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextTertiary,
                            letterSpacing = 0.7.sp,
                        ),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dateValue,
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                        ),
                    )
                }
                Box(
                    modifier = Modifier
                        .background(Color(pillTint.red, pillTint.green, pillTint.blue, 0.10f), CircleShape)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(4.dp).background(pillTint, CircleShape))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = pillLabel,
                            style = TextStyle(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = pillTint,
                            ),
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Hairline))
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = buildStatLine(transactionCount),
                    style = TextStyle(fontSize = 11.5.sp, color = TextSecondary),
                )
            }
        }

        // 3. Export section
        Text(
            text = SakuStrings.cadangan_section_export,
            style = TextStyle(
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextTertiary,
                letterSpacing = 1.sp,
            ),
            modifier = Modifier.padding(start = 4.dp, top = 24.dp, bottom = 8.dp),
        )
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(CardBg)
                .border(1.dp, Hairline, RoundedCornerShape(20.dp)),
        ) {
            ActionRow(
                icon = Icons.Filled.List,
                iconTint = Color(0xFF7BA6F0),
                iconBg = Color(0xFF1B2740),
                title = SakuStrings.cadangan_export_csv,
                chip = "CSV",
                meta = SakuStrings.cadangan_export_csv_meta,
                testTag = TestTags.CADANGAN_EXPORT_CSV,
                onClick = onExportCsv,
            )
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Hairline))
            ActionRow(
                icon = Icons.Filled.Lock,
                iconTint = Color(0xFFA899F5),
                iconBg = Color(0xFF241F3D),
                title = SakuStrings.cadangan_export_json,
                chip = "JSON",
                meta = SakuStrings.cadangan_export_json_meta,
                testTag = TestTags.CADANGAN_EXPORT_JSON,
                onClick = onExportJson,
            )
        }

        // 4. Primary action
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp)
                .clickable { onImportBackup() },
            color = Accent,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
        ) {
            Row(
                modifier = Modifier
                    .height(50.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp),
                    tint = Color.White,
                )
                Text(
                    text = SakuStrings.cadangan_btn_import,
                    style = TextStyle(
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                    ),
                )
            }
        }

        // 5. Safety strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 12.dp)
                .clickable { showDetailsState.value = true },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = Color(0xFF54535F),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = SakuStrings.cadangan_local_note,
                style = TextStyle(fontSize = 11.5.sp, color = TextSecondary),
                modifier = Modifier.weight(1f),
            )
            Text(
                text = SakuStrings.cadangan_detail,
                style = TextStyle(
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = LinkAccent,
                    textDecoration = TextDecoration.Underline,
                ),
            )
        }
    }

    // Detail sheet
    if (showDetailsState.value) {
        ModalBottomSheet(
            onDismissRequest = { showDetailsState.value = false },
            containerColor = CardBg,
            shape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp),
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(
                    modifier = Modifier.padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = SakuStrings.cadangan_sheet_title,
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                        ),
                        modifier = Modifier.weight(1f),
                    )
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { showDetailsState.value = false }
                            .background(Color(0x14FFFFFF), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color(0xFF9C9BA8),
                        )
                    }
                }

                Text(
                    text = SakuStrings.cadangan_export_info,
                    style = TextStyle(fontSize = 12.sp, lineHeight = 18.sp, color = TextSecondary),
                )

                val blocks = listOf(
                    SakuStrings.cadangan_sheet_csv_title to SakuStrings.cadangan_export_transaksi_info,
                    SakuStrings.cadangan_sheet_json_title to SakuStrings.cadangan_export_lengkap_info,
                    SakuStrings.cadangan_sheet_restore_title to SakuStrings.cadangan_import_note,
                    SakuStrings.cadangan_sheet_local_title to SakuStrings.cadangan_local_storage_warning,
                )
                blocks.forEachIndexed { index, (title, body) ->
                    if (index > 0) {
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Hairline))
                    }
                    Column(modifier = Modifier.padding(vertical = 14.dp)) {
                        Text(
                            text = title,
                            style = TextStyle(
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFE9E8EF),
                            ),
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = body,
                            style = TextStyle(fontSize = 12.sp, lineHeight = 18.sp, color = Color(0xFF83828F)),
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 24.dp)
                        .fillMaxWidth()
                        .clickable { showDetailsState.value = false },
                    contentAlignment = Alignment.Center,
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .background(Color(0x14FFFFFF), RoundedCornerShape(14.dp)),
                        color = Color(0x14FFFFFF),
                        contentColor = TextPrimary,
                    ) {
                        Text(
                            text = SakuStrings.cadangan_sheet_close,
                            style = TextStyle(
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    chip: String,
    meta: String,
    testTag: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag(testTag)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(36.dp).background(iconBg, RoundedCornerShape(11.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(17.dp),
                tint = iconTint,
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    lineHeight = 18.sp,
                ),
                maxLines = 1,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(Color(0x14FFFFFF), RoundedCornerShape(5.dp))
                        .padding(horizontal = 6.dp, vertical = 1.dp),
                ) {
                    Text(
                        text = chip,
                        style = TextStyle(
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF9C9BA8),
                            letterSpacing = 0.4.sp,
                        ),
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = meta,
                    style = TextStyle(fontSize = 11.5.sp, color = TextSecondary),
                    maxLines = 1,
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = Color(0xFF484753),
        )
    }
}

private fun buildStatLine(transactionCount: Int): String {
    return "${SakuStrings.cadangan_stat_transactions.format(transactionCount)}  ·  ${SakuStrings.cadangan_stat_accounts.format(2)}  ·  ${SakuStrings.cadangan_stat_categories.format(6)}"
}

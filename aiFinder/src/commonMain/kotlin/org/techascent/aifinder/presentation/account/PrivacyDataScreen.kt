package org.techascent.aifinder.presentation.account

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import apphub.aifinder.generated.resources.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.techascent.aifinder.domain.inventory.InventorySnapshot
import org.techascent.aifinder.domain.inventory.usecase.ObserveInventoryUseCase
import org.techascent.aifinder.presentation.components.*
import org.techascent.aifinder.presentation.theme.AiFinderPalette
import org.techascent.aifinder.util.*

internal fun csvCell(value: String): String {
    val safe = if (value.trimStart().firstOrNull() in listOf('=', '+', '-', '@') ||
        value.startsWith('\t') || value.startsWith('\r')) "'$value" else value
    return "\"${safe.replace("\"", "\"\"")}\""
}

internal fun InventorySnapshot.exportCsv(): String = buildString {
    append("Name,Category,Place,Notes,Aliases,CreatedAtEpochMillis,ConfirmedAtEpochMillis\r\n")
    items.forEach { entry ->
        append(listOf(
            entry.item.name, entry.item.category, entry.path, entry.item.description,
            entry.item.tags.joinToString("; "), entry.createdAt.toString(), entry.confirmedAt.toString(),
        ).joinToString(",") { csvCell(it) }).append("\r\n")
    }
}

@Composable
fun ExportInventoryScreen(onBack: () -> Unit) {
    val inventory = koinInject<ObserveInventoryUseCase>()
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val emptyMessage = stringResource(Res.string.data_export_empty)
    val title = stringResource(Res.string.export_title)
    val export: (String) -> Unit = { format ->
        if (busy == null) scope.launch {
            busy = format; error = null
            try {
                val snapshot = inventory().first()
                check(snapshot.items.isNotEmpty()) { emptyMessage }
                val data = if (format == "csv") snapshot.exportCsv().encodeToByteArray() else {
                    createInventoryPdf(listOf(title, "") + snapshot.items.flatMap {
                        listOf(
                            it.item.name, "${it.item.category} | ${it.path}", it.item.description,
                            "Aliases: ${it.item.tags.joinToString(", ")}",
                            "Last confirmed: ${org.techascent.aifinder.data.subscription.formatPurchaseDate(it.confirmedAt)}", "",
                        )
                    })
                }
                shareFile("inventory.$format", data, if (format == "csv") "text/csv" else "application/pdf")
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) { error = e.message ?: "Couldn't export inventory." }
            finally { busy = null }
        }
    }
    ScreenLayout(topBar = { ScreenTopBar(title, onBack) }) {
        item {
            InfoCard(
                title = stringResource(Res.string.export_intro),
                subtitle = stringResource(Res.string.export_intro_hint),
                leadingContent = { Icon(Icons.Outlined.IosShare, null, tint = AiFinderPalette.Teal) },
            )
        }
        item {
            ExportFormatCard(
                Icons.Outlined.TableChart,
                stringResource(Res.string.export_csv_title),
                stringResource(Res.string.export_csv_hint),
                stringResource(Res.string.data_export_csv),
                busy == "csv", busy == null,
            ) { export("csv") }
        }
        item {
            ExportFormatCard(
                Icons.Outlined.PictureAsPdf,
                stringResource(Res.string.export_pdf_title),
                stringResource(Res.string.export_pdf_hint),
                stringResource(Res.string.data_export_pdf),
                busy == "pdf", busy == null,
            ) { export("pdf") }
        }
        error?.let { message ->
            item {
                Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(16.dp)) {
                    Text(message, Modifier.fillMaxWidth().padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(stringResource(Res.string.export_before_title), color = AiFinderPalette.Ink,
                    fontWeight = FontWeight.SemiBold)
                Text(stringResource(Res.string.data_export_notice), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun ExportFormatCard(
    icon: ImageVector,
    title: String,
    description: String,
    action: String,
    loading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(color = AiFinderPalette.Surface, shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(color = AiFinderPalette.Mint, shape = RoundedCornerShape(12.dp)) {
                    Icon(icon, null, Modifier.padding(12.dp).size(24.dp), tint = AiFinderPalette.Teal)
                }
                Text(title, Modifier.weight(1f), color = AiFinderPalette.Ink,
                    style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
            Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant)
            PrimaryAction(action, onClick, enabled = enabled, isLoading = loading)
        }
    }
}

@Composable
fun PrivacyDataScreen(onBack: () -> Unit) {
    ScreenLayout(topBar = { ScreenTopBar(stringResource(Res.string.data_title), onBack) }) {
        item { SectionTitle(stringResource(Res.string.data_intro)) }
        item {
            InfoCard(
                stringResource(Res.string.privacy_control_title),
                stringResource(Res.string.privacy_control_hint),
            )
        }
        item {
            InfoCard(
                stringResource(Res.string.privacy_sharing_title),
                stringResource(Res.string.privacy_sharing_hint),
            )
        }
    }
}

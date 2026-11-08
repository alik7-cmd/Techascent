package org.techascent.aifinder.presentation.account

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
fun PrivacyDataScreen(onBack: () -> Unit) {
    val inventory = koinInject<ObserveInventoryUseCase>()
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    val emptyMessage = stringResource(Res.string.data_export_empty)
    val title = stringResource(Res.string.data_title)
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
        item { SectionTitle(stringResource(Res.string.data_intro)) }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(stringResource(Res.string.data_photo_notice), color = AiFinderPalette.Ink)
                Text(stringResource(Res.string.data_export_notice), color = AiFinderPalette.Muted)
                PrimaryAction(stringResource(Res.string.data_export_csv), { export("csv") },
                    enabled = busy == null, isLoading = busy == "csv")
                PrimaryAction(stringResource(Res.string.data_export_pdf), { export("pdf") },
                    enabled = busy == null, isLoading = busy == "pdf")
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Text(stringResource(Res.string.data_delete_notice), color = AiFinderPalette.Muted)
            }
        }
    }
}

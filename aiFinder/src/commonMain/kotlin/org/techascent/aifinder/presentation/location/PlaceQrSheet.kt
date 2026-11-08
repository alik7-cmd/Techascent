package org.techascent.aifinder.presentation.location

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import apphub.aifinder.generated.resources.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.jetbrains.compose.resources.stringResource
import org.techascent.aifinder.presentation.components.*
import org.techascent.aifinder.presentation.scan.CameraCaptureController
import org.techascent.aifinder.presentation.scan.ScanCameraCard
import org.techascent.aifinder.presentation.scan.rememberCameraPermissions
import org.techascent.aifinder.presentation.theme.AiFinderPalette
import org.techascent.aifinder.util.*

internal fun placeQrValue(id: String): String = "aifinder://place/$id"
internal fun placeIdFromQr(value: String): String? =
    value.takeIf { it.startsWith("aifinder://place/") }?.removePrefix("aifinder://place/")
        ?.takeIf { it.isNotBlank() && it.length <= 768 && it.none { c -> c in ".#$[]/\\?%" } }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceQrSheet(
    placeId: String?,
    placePath: String,
    knownPlaceIds: Set<String>,
    onOpenPlace: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val camera = remember { CameraCaptureController() }
    // Bind to the host before entering the sheet's themed dialog context.
    val permissions = if (placeId == null) rememberCameraPermissions() else null
    var png by remember { mutableStateOf<ByteArray?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    var retry by remember { mutableStateOf(0) }
    val notFound = stringResource(Res.string.qr_no_match)
    LaunchedEffect(placeId, placePath, retry) {
        if (placeId != null) {
            busy = true
            try { png = generateQrPng(placeQrValue(placeId), placePath) }
            catch (e: kotlinx.coroutines.CancellationException) { throw e }
            catch (e: Exception) { error = e.message ?: "Couldn't generate QR label." }
            finally { busy = false }
        }
    }
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = AiFinderPalette.Surface,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            SectionTitle(stringResource(if (placeId == null) Res.string.qr_scan else Res.string.qr_label))
            Text(
                if (placeId == null) stringResource(Res.string.qr_scan_hint) else placePath,
                color = AiFinderPalette.Muted,
            )
            if (placeId == null) {
                ScanCameraCard(camera, false, permissions = checkNotNull(permissions),
                    framingHint = stringResource(Res.string.qr_frame_hint))
                PrimaryAction(stringResource(Res.string.qr_scan), onClick = {
                    if (!busy) scope.launch {
                        busy = true; error = null
                        try {
                            val id = decodeQrPhoto(camera.capture().jpegBytes)?.let(::placeIdFromQr)
                            if (id == null || id !in knownPlaceIds) error = notFound
                            else onOpenPlace(id)
                        } catch (e: kotlinx.coroutines.CancellationException) { throw e }
                        catch (e: Exception) { error = e.message ?: notFound }
                        finally { busy = false }
                    }
                }, isLoading = busy)
            } else {
                val bitmap = remember(png) { png?.decodeToImageBitmap() }
                if (bitmap != null) Image(bitmap, stringResource(Res.string.qr_label),
                    modifier = Modifier.fillMaxWidth().height(260.dp))
                else ShimmerBlock(Modifier.fillMaxWidth().height(260.dp))
                Text(stringResource(Res.string.qr_private_notice), color = AiFinderPalette.Muted)
                PrimaryAction(stringResource(Res.string.qr_share), onClick = {
                    png?.let { bytes -> scope.launch {
                        try { shareFile("place-label.png", bytes, "image/png") }
                        catch (e: kotlinx.coroutines.CancellationException) { throw e }
                        catch (e: Exception) { error = e.message ?: "Couldn't share label." }
                    } }
                }, enabled = png != null, isLoading = busy)
            }
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                if (placeId != null) TextButton(onClick = { error = null; retry++ }) {
                    Text(stringResource(Res.string.location_retry))
                }
            }
        }
    }
}

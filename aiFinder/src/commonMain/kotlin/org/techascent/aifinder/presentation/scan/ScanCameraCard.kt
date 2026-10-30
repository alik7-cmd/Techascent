package org.techascent.aifinder.presentation.scan

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.NoPhotography
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.icerock.moko.permissions.DeniedAlwaysException
import dev.icerock.moko.permissions.DeniedException
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.RequestCanceledException
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import kotlinx.coroutines.launch
import org.techascent.aifinder.presentation.theme.AiFinderPalette

private enum class CameraAccess { Checking, Granted, Denied, DeniedAlways, Unavailable }

@Composable
fun ScanCameraCard(modifier: Modifier = Modifier) {
    val factory = rememberPermissionsControllerFactory()
    val controller = remember(factory) { factory.createPermissionsController() }
    BindEffect(controller)

    val scope = rememberCoroutineScope()
    var access by remember { mutableStateOf(CameraAccess.Checking) }

    LaunchedEffect(controller) {
        access = when (controller.getPermissionState(Permission.CAMERA)) {
            PermissionState.Granted -> CameraAccess.Granted
            PermissionState.DeniedAlways -> CameraAccess.DeniedAlways
            else -> controller.request()
        }
    }

    Box(
        modifier.fillMaxWidth().height(310.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(if (access == CameraAccess.Granted) Color.Black else AiFinderPalette.Mint),
        contentAlignment = Alignment.Center
    ) {
        when (access) {
            CameraAccess.Checking -> CircularProgressIndicator(color = AiFinderPalette.Teal)
            CameraAccess.Granted -> {
                CameraPreview(Modifier.fillMaxSize(), onUnavailable = { access = CameraAccess.Unavailable })
                ViewfinderOverlay()
            }
            CameraAccess.Denied -> CameraMessage(
                icon = Icons.Outlined.CameraAlt,
                title = "Camera access needed",
                body = "Allow camera access to take a photo of your space.",
                action = "Allow camera",
                onAction = { scope.launch { access = controller.request() } }
            )
            CameraAccess.DeniedAlways -> CameraMessage(
                icon = Icons.Outlined.NoPhotography,
                title = "Camera access is off",
                body = "Turn on camera access in Settings to scan your space.",
                action = "Open settings",
                onAction = controller::openAppSettings
            )
            CameraAccess.Unavailable -> CameraMessage(
                icon = Icons.Outlined.NoPhotography,
                title = "Camera unavailable",
                body = "We couldn't start the camera on this device."
            )
        }
    }
}

private suspend fun PermissionsController.request(): CameraAccess = try {
    providePermission(Permission.CAMERA)
    CameraAccess.Granted
} catch (_: DeniedAlwaysException) {
    CameraAccess.DeniedAlways
} catch (_: DeniedException) {
    CameraAccess.Denied
} catch (_: RequestCanceledException) {
    CameraAccess.Denied
}

@Composable
private fun ViewfinderOverlay() {
    Box(Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize().padding(20.dp)) {
            val len = 28.dp.toPx()
            val stroke = 4.dp.toPx()
            val w = size.width
            val h = size.height
            listOf(
                Offset(0f, 0f) to (1 to 1), Offset(w, 0f) to (-1 to 1),
                Offset(0f, h) to (1 to -1), Offset(w, h) to (-1 to -1)
            ).forEach { (corner, dir) ->
                drawLine(Color.White, corner, corner.copy(x = corner.x + dir.first * len), stroke, StrokeCap.Round)
                drawLine(Color.White, corner, corner.copy(y = corner.y + dir.second * len), stroke, StrokeCap.Round)
            }
        }
        Text(
            "Fit the whole space in the frame",
            color = Color.White,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp)
                .background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(50))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun CameraMessage(
    icon: ImageVector,
    title: String,
    body: String,
    action: String? = null,
    onAction: () -> Unit = {}
) {
    Column(
        Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            Modifier.size(64.dp).background(Color.White.copy(alpha = 0.7f), CircleShape),
            contentAlignment = Alignment.Center
        ) { Icon(icon, contentDescription = null, tint = AiFinderPalette.Teal, modifier = Modifier.size(30.dp)) }
        Text(title, color = AiFinderPalette.Ink, fontWeight = FontWeight.SemiBold)
        Text(body, color = AiFinderPalette.Muted, textAlign = TextAlign.Center)
        if (action != null) {
            TextButton(onClick = onAction) {
                Text(action, color = AiFinderPalette.Teal, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

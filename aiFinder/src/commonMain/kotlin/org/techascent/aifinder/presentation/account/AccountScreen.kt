package org.techascent.aifinder.presentation.account

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.PrivacyTip
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apphub.aifinder.generated.resources.*
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.techascent.aifinder.domain.auth.AuthProvider
import org.techascent.aifinder.presentation.components.ScreenTopBar
import org.techascent.aifinder.presentation.components.NativeConfirmDialog
import org.techascent.aifinder.presentation.components.ScreenLayout
import org.techascent.aifinder.presentation.theme.AiFinderPalette

@Composable
fun AccountScreen(
    viewModel: AccountViewModel,
    onBack: () -> Unit,
    onSignedOut: () -> Unit,
    onSubscription: () -> Unit,
    onPrivacy: () -> Unit,
    onExport: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(Unit) { viewModel.load() }
    LaunchedEffect(state) {
        when (val current = state) {
            AccountUiState.SignedOut -> onSignedOut()
            is AccountUiState.Success -> if (current.logoutFailed) {
                snackbar.showSnackbar(getString(Res.string.account_logout_error))
                viewModel.dismissLogoutError()
            }
            AccountUiState.Loading -> Unit
        }
    }
    val success = state as? AccountUiState.Success ?: return
    if (success.showLogoutConfirmation) {
        NativeConfirmDialog(
            title = stringResource(Res.string.account_logout_title),
            message = stringResource(Res.string.account_logout_message),
            confirmLabel = stringResource(Res.string.account_logout),
            cancelLabel = stringResource(Res.string.account_logout_cancel),
            onConfirm = viewModel::confirmLogout,
            onDismiss = viewModel::dismissLogout,
        )
    }
    ScreenLayout(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = { ScreenTopBar(stringResource(Res.string.account_title), onBack) },
    ) {
        item(key = "profile") { ProfileCard(success.profile) }
        item(key = "preferences") {
            SettingsSection(stringResource(Res.string.account_section_preferences)) {
                SettingsCell(
                    Icons.Outlined.AutoAwesome,
                    stringResource(Res.string.history_title),
                    onClick = onSubscription,
                )
                CellDivider()
                SettingsCell(Icons.Outlined.FileDownload, stringResource(Res.string.export_title), onClick = onExport)
                CellDivider()
                SettingsCell(Icons.Outlined.PrivacyTip, stringResource(Res.string.account_privacy), onClick = onPrivacy)
            }
        }
        item(key = "account") {
            SettingsSection(stringResource(Res.string.account_section_account)) {
                SettingsCell(
                    icon = Icons.AutoMirrored.Outlined.Logout,
                    title = stringResource(Res.string.account_logout),
                    contentColor = LogoutRed,
                    iconBackground = LogoutRed.copy(alpha = 0.1f),
                    showChevron = false,
                    isLoading = success.isSigningOut,
                    onClick = viewModel::requestLogout,
                )
            }
        }
        item(key = "footer") {
            Text(
                stringResource(Res.string.account_footer),
                color = AiFinderPalette.Muted,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
            )
        }
    }
}

private val LogoutRed = Color(0xFFB04A4A)

@Composable
private fun ProfileCard(profile: AccountProfile) {
    val name = profile.name ?: profile.email ?: stringResource(Res.string.account_fallback_name)
    val provider = stringResource(
        when (profile.provider) {
            AuthProvider.Google -> Res.string.account_signed_in_google
            AuthProvider.Apple -> Res.string.account_signed_in_apple
            null -> Res.string.account_signed_in_unknown
        }
    )
    Row(
        Modifier.fillMaxWidth().clip(CardShape).background(AiFinderPalette.Surface)
            .border(1.dp, AiFinderPalette.Border, CardShape).padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            Modifier.size(56.dp).clip(CircleShape).background(AiFinderPalette.Mint),
            contentAlignment = Alignment.Center
        ) {
            Text(profile.initial, color = AiFinderPalette.Teal, fontWeight = FontWeight.Bold, fontSize = 22.sp)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                name, color = AiFinderPalette.Ink, fontWeight = FontWeight.Bold, fontSize = 18.sp,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            // Avoid repeating the email when it's already used as the title.
            profile.email?.takeIf { profile.name != null }?.let {
                Text(it, color = AiFinderPalette.Muted, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(provider, color = AiFinderPalette.Muted, fontSize = 13.sp)
        }
    }
}

private val CardShape = RoundedCornerShape(18.dp)

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            title, color = AiFinderPalette.Muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.2.sp, modifier = Modifier.padding(start = 4.dp)
        )
        Column(
            Modifier.fillMaxWidth().clip(CardShape).background(AiFinderPalette.Surface)
                .border(1.dp, AiFinderPalette.Border, CardShape),
            content = content
        )
    }
}

@Composable
private fun CellDivider() = HorizontalDivider(Modifier.padding(start = 68.dp), color = AiFinderPalette.Border)

@Composable
private fun SettingsCell(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    contentColor: Color = AiFinderPalette.Ink,
    iconBackground: Color = AiFinderPalette.Mint,
    showChevron: Boolean = true,
    isLoading: Boolean = false,
) {
    Row(
        Modifier.fillMaxWidth().clickable(enabled = !isLoading, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(iconBackground),
            contentAlignment = Alignment.Center
        ) {
            val tint = if (contentColor == AiFinderPalette.Ink) AiFinderPalette.Teal else contentColor
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        }
        Text(title, color = contentColor, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, modifier = Modifier.weight(1f))
        when {
            isLoading -> CircularProgressIndicator(Modifier.size(18.dp), color = contentColor, strokeWidth = 2.dp)
            showChevron -> Icon(
                Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = AiFinderPalette.Muted
            )
        }
    }
}

package org.techascent.aifinder.presentation.login

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apphub.aifinder.generated.resources.*
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.techascent.aifinder.domain.auth.AuthProvider
import org.techascent.aifinder.presentation.components.SectionTitle
import org.techascent.aifinder.presentation.theme.AiFinderPalette

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onSignedIn: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(state.isSignedIn) {
        if (state.isSignedIn) {
            viewModel.onSignedInHandled()
            onSignedIn()
        }
    }
    LoginContent(state, onSignIn = viewModel::onSignIn)
}

@Composable
private fun LoginContent(
    state: LoginUiState,
    onSignIn: (AuthProvider) -> Unit,
) {
    Scaffold(containerColor = AiFinderPalette.Background) { padding ->
        Column(
            Modifier.fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Spacer(Modifier.height(24.dp))
            Box(
                Modifier.align(Alignment.CenterHorizontally)
                    .size(140.dp)
                    .background(AiFinderPalette.Mint, RoundedCornerShape(36.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painterResource(Res.drawable.ic_full_box),
                    contentDescription = null,
                    modifier = Modifier.size(92.dp)
                )
            }
            Spacer(Modifier.height(40.dp))
            SectionTitle(stringResource(Res.string.login_title), fontSize = 30.sp, lineHeight = 38.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(Res.string.login_subtitle),
                color = AiFinderPalette.Muted,
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(64.dp))
            Text(
                stringResource(Res.string.login_choose),
                color = AiFinderPalette.Muted,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(36.dp))
            ProviderButton(
                label = stringResource(Res.string.login_continue_apple),
                icon = painterResource(Res.drawable.ic_apple),
                containerColor = Color.Black,
                contentColor = Color.White,
                border = null,
                isLoading = state.loadingProvider == AuthProvider.Apple,
                enabled = !state.isLoading,
                onClick = { onSignIn(AuthProvider.Apple) }
            )
            Spacer(Modifier.height(16.dp))
            ProviderButton(
                label = stringResource(Res.string.login_continue_google),
                icon = painterResource(Res.drawable.ic_google),
                containerColor = AiFinderPalette.Surface,
                contentColor = AiFinderPalette.Ink,
                border = BorderStroke(1.dp, AiFinderPalette.Border),
                isLoading = state.loadingProvider == AuthProvider.Google,
                enabled = !state.isLoading,
                onClick = { onSignIn(AuthProvider.Google) }
            )
            state.errorRes?.let {
                Spacer(Modifier.height(16.dp))
                Text(
                    stringResource(it),
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(48.dp))
            Text(
                stringResource(Res.string.login_terms),
                color = AiFinderPalette.Muted,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
            )
            Spacer(Modifier.height(36.dp))
            Text(
                stringResource(Res.string.login_tagline),
                color = AiFinderPalette.Muted,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ProviderButton(
    label: String,
    icon: Painter,
    containerColor: Color,
    contentColor: Color,
    border: BorderStroke?,
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp),
        shape = RoundedCornerShape(14.dp),
        border = border,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.7f),
            disabledContentColor = contentColor.copy(alpha = 0.7f),
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp)
    ) {
        Box(Modifier.size(20.dp), contentAlignment = Alignment.Center) {
            if (isLoading) {
                CircularProgressIndicator(Modifier.size(18.dp), color = contentColor, strokeWidth = 2.dp)
            } else {
                Image(icon, contentDescription = null, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.width(14.dp))
        Text(label, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

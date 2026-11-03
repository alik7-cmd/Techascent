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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import org.techascent.aifinder.util.Platform
import org.techascent.aifinder.util.getPlatform

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onSignedIn: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(state) {
        if (state is LoginUiState.SignedIn) {
            viewModel.onSignedInHandled()
            onSignedIn()
        }
    }
    LoginContent(state, onSignIn = viewModel::onSignIn, onDismissError = viewModel::dismissError)
}

@Composable
private fun LoginContent(
    state: LoginUiState,
    onSignIn: (AuthProvider) -> Unit,
    onDismissError: () -> Unit,
) {
    val platform = remember { getPlatform() }
    var selectedProvider by remember { mutableStateOf<AuthProvider?>(null) }
    val isLoading = state is LoginUiState.Loading
    val onProviderSelected: (AuthProvider) -> Unit = { provider ->
        selectedProvider = provider
        onSignIn(provider)
    }
    Scaffold(containerColor = AiFinderPalette.Background) { padding ->
        BoxWithConstraints(Modifier.fillMaxSize().padding(padding)) {
            Column(
                Modifier.fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = maxHeight)
                    .padding(horizontal = 28.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        stringResource(Res.string.welcome_app_name),
                        modifier = Modifier.fillMaxWidth(),
                        color = AiFinderPalette.Teal,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(32.dp))
                    Image(
                        painter = painterResource(Res.drawable.welcome_illustration),
                        contentDescription = stringResource(Res.string.welcome_illustration_description),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                            .widthIn(max = 480.dp).fillMaxWidth()
                            .aspectRatio(770f / 520f)
                            .clip(RoundedCornerShape(28.dp))
                            .background(AiFinderPalette.IllustrationMint)
                            .padding(12.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(Modifier.height(40.dp))
                    SectionTitle(stringResource(Res.string.login_title), fontSize = 30.sp, lineHeight = 38.sp)
                    Spacer(Modifier.height(14.dp))
                    Text(
                        stringResource(Res.string.login_subtitle),
                        color = AiFinderPalette.Muted,
                        fontSize = 17.sp,
                        lineHeight = 24.sp
                    )
                }
                Column(
                    Modifier.padding(top = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LoginErrorBanner(
                        errorRes = (state as? LoginUiState.Error)?.errorRes,
                        onDismiss = onDismissError,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    if (platform == Platform.IOS) {
                        ProviderButton(
                            label = stringResource(Res.string.login_continue_apple),
                            icon = painterResource(Res.drawable.ic_apple),
                            containerColor = Color.Black,
                            contentColor = Color.White,
                            border = null,
                            isLoading = isLoading && selectedProvider == AuthProvider.Apple,
                            enabled = !isLoading,
                            onClick = { onProviderSelected(AuthProvider.Apple) }
                        )
                    }
                    ProviderButton(
                        label = stringResource(Res.string.login_continue_google),
                        icon = painterResource(Res.drawable.ic_google),
                        containerColor = AiFinderPalette.Surface,
                        contentColor = AiFinderPalette.Ink,
                        border = BorderStroke(1.dp, AiFinderPalette.Border),
                        isLoading = isLoading && selectedProvider == AuthProvider.Google,
                        enabled = !isLoading,
                        onClick = { onProviderSelected(AuthProvider.Google) }
                    )
                    Text(
                        stringResource(Res.string.login_terms),
                        color = AiFinderPalette.Muted,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, start = 12.dp, end = 12.dp)
                    )
                }
            }
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
        shape = RoundedCornerShape(28.dp),
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

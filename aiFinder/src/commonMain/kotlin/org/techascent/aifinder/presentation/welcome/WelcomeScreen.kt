package org.techascent.aifinder.presentation.welcome

import org.techascent.aifinder.presentation.theme.AiFinderSpacing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apphub.aifinder.generated.resources.Res
import apphub.aifinder.generated.resources.welcome_app_name
import apphub.aifinder.generated.resources.welcome_tagline
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.techascent.aifinder.presentation.components.*
import org.techascent.aifinder.presentation.theme.AiFinderPalette
import org.techascent.aifinder.domain.auth.WelcomeDestination

@OptIn(KoinExperimentalAPI::class)
@Composable
fun WelcomeScreen(
    viewModel: WelcomeViewModel = koinViewModel<WelcomeViewModel>(),
    onNavigate: (WelcomeDestination) -> Unit,
    onContinue: () -> Unit
) {
    val uiState by viewModel.state.collectAsState()
    LaunchedEffect(uiState) {
        (uiState as? WelcomeUiState.Navigate)?.let { onNavigate(it.destination) }
    }
    Scaffold(containerColor = AiFinderPalette.Background) { padding ->
        BoxWithConstraints(Modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                WelcomeUiState.Loading, is WelcomeUiState.Navigate -> Unit

                is WelcomeUiState.Error -> Box(Modifier.padding(AiFinderSpacing.dp28)) {
                    ErrorContent(state.message, viewModel::retry)
                }

                is WelcomeUiState.Success -> {
                    val content = state.data
                    val viewportHeight = maxHeight
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .heightIn(min = viewportHeight)
                            .padding(horizontal = AiFinderSpacing.dp28, vertical = AiFinderSpacing.dp24),
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
                            Spacer(Modifier.height(AiFinderSpacing.dp32))
                            WelcomeIllustration(
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                                    .widthIn(max = 480.dp).fillMaxWidth()
                                    .aspectRatio(770f / 520f)
                                    .clip(RoundedCornerShape(28.dp))
                                    .background(AiFinderPalette.IllustrationMint)
                                    .padding(AiFinderSpacing.dp12),
                            )
                            Spacer(Modifier.height(AiFinderSpacing.dp40))
                            SectionTitle(
                                stringResource(content.welcomeTitleRes),
                                fontSize = 30.sp,
                                lineHeight = 38.sp
                            )
                            Spacer(Modifier.height(AiFinderSpacing.dp14))
                            Text(
                                stringResource(content.descriptionRes),
                                color = AiFinderPalette.Muted,
                                fontSize = 17.sp,
                                lineHeight = 24.sp
                            )
                        }
                        Column(
                            modifier = Modifier.padding(top = AiFinderSpacing.dp40),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(AiFinderSpacing.dp16)
                        ) {
                            PrimaryAction(
                                stringResource(content.buttonTextRes),
                                onContinue,
                                modifier = Modifier.heightIn(min = 54.dp)
                            )
                            Text(
                                stringResource(Res.string.welcome_tagline),
                                color = AiFinderPalette.Muted,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

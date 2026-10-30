package org.techascent.aifinder.presentation.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apphub.aifinder.generated.resources.Res
import apphub.aifinder.generated.resources.welcome_tagline
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.techascent.aifinder.presentation.components.*
import org.techascent.aifinder.presentation.theme.AiFinderPalette

@OptIn(KoinExperimentalAPI::class)
@Composable
fun WelcomeScreen(
    viewModel: WelcomeViewModel = koinViewModel<WelcomeViewModel>(),
    onContinue: () -> Unit
) {
    val uiState by viewModel.state.collectAsState()
    Scaffold(containerColor = AiFinderPalette.Background) { padding ->
        BoxWithConstraints(Modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                is WelcomeUiState.Loading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { LoadingContent() }
                is WelcomeUiState.Error -> Box(Modifier.padding(28.dp)) {
                    ErrorContent(state.message, viewModel::retry)
                }
                is WelcomeUiState.Success -> {
                    val content = state.data
                    val viewportHeight = maxHeight
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .heightIn(min = viewportHeight)
                            .padding(horizontal = 28.dp, vertical = 28.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Spacer(Modifier.height(viewportHeight * 0.1f))
                            Box(
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                                    .widthIn(max = 300.dp).fillMaxWidth().aspectRatio(1f)
                                    .background(AiFinderPalette.Mint, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    modifier = Modifier.fillMaxSize().padding(42.dp),
                                    painter = painterResource(content.boxRes),
                                    contentDescription = null,
                                    contentScale = ContentScale.Fit
                                )
                            }
                            Spacer(Modifier.height(56.dp))
                            SectionTitle(
                                stringResource(content.welcomeTitleRes),
                                fontSize = 30.sp,
                                lineHeight = 39.sp
                            )
                            Spacer(Modifier.height(20.dp))
                            Text(
                                stringResource(content.descriptionRes),
                                color = AiFinderPalette.Muted,
                                fontSize = 16.sp,
                                lineHeight = 21.sp
                            )
                        }
                        Column(
                            modifier = Modifier.padding(top = 56.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            PrimaryAction(
                                stringResource(content.buttonTextRes),
                                onContinue,
                                modifier = Modifier.heightIn(min = 50.dp)
                            )
                            Text(
                                stringResource(Res.string.welcome_tagline),
                                color = AiFinderPalette.Muted,
                                fontSize = 13.sp
                            )
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                }
            }
        }
    }
}

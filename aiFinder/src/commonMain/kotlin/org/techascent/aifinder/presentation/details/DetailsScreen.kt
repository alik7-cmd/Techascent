package org.techascent.aifinder.presentation.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apphub.aifinder.generated.resources.*
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.techascent.aifinder.presentation.components.*
import org.techascent.aifinder.presentation.theme.AiFinderPalette

@Composable
fun DetailsScreen(state: DetailsUiState, onBack: () -> Unit, onRetry: () -> Unit) {
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val unavailableMessage = stringResource(Res.string.details_actions_unavailable)

    ScreenLayout(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            Surface(color = AiFinderPalette.Background) {
                Row(
                    modifier = Modifier.fillMaxWidth().statusBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 4.dp)
                ) {
                    BackButton(onBack)
                }
            }
        }
    ) {
        item(key = "title") { SectionTitle(stringResource(Res.string.details_title)) }
        when (state) {
            is DetailsUiState.Loading -> item { LoadingContent() }
            is DetailsUiState.Error -> item { ErrorContent(state.message, onRetry) }
            is DetailsUiState.Success -> {
                val entry = state.data.entry
                if (entry == null) {
                    item { Text(stringResource(Res.string.details_not_found), color = AiFinderPalette.Muted) }
                } else {
                    item(key = "preview") {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(280.dp)
                                .background(AiFinderPalette.Mint, RoundedCornerShape(24.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (entry.item.name.contains("HDMI", ignoreCase = true)) {
                                    stringResource(Res.string.details_hdmi_preview)
                                } else {
                                    entry.item.name
                                },
                                modifier = Modifier.padding(24.dp)
                                    .background(
                                        AiFinderPalette.Teal.copy(alpha = 0.3f),
                                        RoundedCornerShape(32.dp)
                                    ).padding(horizontal = 40.dp, vertical = 28.dp),
                                color = AiFinderPalette.Teal,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            )
                        }
                    }
                    item(key = "item") {
                        Column(
                            modifier = Modifier.padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            SectionTitle(entry.item.name)
                            Text(entry.item.category, color = AiFinderPalette.Muted, fontSize = 16.sp)
                        }
                    }
                    item(key = "location") {
                        val path = entry.path.split("›").map(String::trim).filter(String::isNotEmpty)
                        Column(
                            modifier = Modifier.padding(top = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                stringResource(Res.string.details_last_location),
                                color = AiFinderPalette.Muted,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Column(
                                modifier = Modifier.fillMaxWidth()
                                    .background(AiFinderPalette.Surface, RoundedCornerShape(18.dp))
                                    .padding(24.dp),
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Outlined.MyLocation,
                                        contentDescription = null,
                                        tint = AiFinderPalette.Ink,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        path.firstOrNull() ?: entry.location.name,
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AiFinderPalette.Ink
                                    )
                                }
                                if (path.size > 1) {
                                    Text(
                                        path.drop(1).joinToString("  ›  "),
                                        modifier = Modifier.padding(start = 26.dp),
                                        color = AiFinderPalette.Teal,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                    item(key = "actions") {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = { scope.launch { snackbar.showSnackbar(unavailableMessage) } },
                                modifier = Modifier.weight(1f).heightIn(min = 50.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AiFinderPalette.Mint,
                                    contentColor = AiFinderPalette.Teal
                                )
                            ) {
                                Text(stringResource(Res.string.details_move), fontWeight = FontWeight.Bold)
                            }
                            PrimaryAction(
                                stringResource(Res.string.details_edit),
                                onClick = { scope.launch { snackbar.showSnackbar(unavailableMessage) } },
                                modifier = Modifier.weight(1f).heightIn(min = 50.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

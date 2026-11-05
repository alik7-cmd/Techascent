package org.techascent.aifinder.presentation.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.LocationOn
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
    val entry = (state as? DetailsUiState.Success)?.data?.entry

    ScreenLayout(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = { ScreenTopBar(stringResource(Res.string.details_title), onBack) },
        bottomBar = {
            if (entry != null) {
                Surface(color = AiFinderPalette.Background) {
                    Row(
                        Modifier.fillMaxWidth().navigationBarsPadding()
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        OutlinedButton(
                            onClick = { scope.launch { snackbar.showSnackbar(unavailableMessage) } },
                            modifier = Modifier.weight(1f).heightIn(min = 50.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = AiFinderPalette.Teal),
                        ) {
                            Text(stringResource(Res.string.details_move), fontWeight = FontWeight.SemiBold)
                        }
                        PrimaryAction(
                            stringResource(Res.string.details_edit),
                            onClick = { scope.launch { snackbar.showSnackbar(unavailableMessage) } },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        },
    ) {
        when (state) {
            is DetailsUiState.Loading -> item { LoadingContent() }
            is DetailsUiState.Error -> item {
                ErrorCard(
                    title = stringResource(Res.string.details_error_title),
                    message = state.message,
                    onRetry = onRetry,
                )
            }
            is DetailsUiState.Success -> {
                if (entry == null) {
                    item {
                        EmptyListCard(
                            titleRes = Res.string.details_not_found,
                            descriptionRes = Res.string.details_missing_message,
                        )
                    }
                } else {
                    item(key = "identity") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                entry.item.name,
                                color = AiFinderPalette.Ink,
                                fontSize = 28.sp,
                                lineHeight = 34.sp,
                                fontWeight = FontWeight.Bold,
                            )
                            Surface(color = AiFinderPalette.Mint, shape = RoundedCornerShape(12.dp)) {
                                Text(
                                    entry.item.category,
                                    Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    color = AiFinderPalette.Teal,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                    item(key = "preview") {
                        Surface(color = AiFinderPalette.Surface, shape = RoundedCornerShape(24.dp)) {
                            Column(Modifier.fillMaxWidth().padding(12.dp)) {
                                ItemPhoto(
                                    photoId = entry.item.photoId,
                                    contentDescription = stringResource(Res.string.details_photo_description, entry.item.name),
                                    shape = RoundedCornerShape(18.dp),
                                    modifier = Modifier.fillMaxWidth().aspectRatio(4f / 3f),
                                ) {
                                    Column(
                                        Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                    ) {
                                        Icon(
                                            Icons.Outlined.Image, null,
                                            tint = AiFinderPalette.Teal, modifier = Modifier.size(40.dp),
                                        )
                                        Text(
                                            stringResource(Res.string.details_photo_placeholder),
                                            color = AiFinderPalette.Teal,
                                            fontSize = 14.sp,
                                        )
                                    }
                                }
                                Text(
                                    stringResource(Res.string.details_source_photo),
                                    Modifier.padding(horizontal = 4.dp, vertical = 12.dp),
                                    color = AiFinderPalette.Muted,
                                    fontSize = 13.sp,
                                )
                            }
                        }
                    }
                    item(key = "location") {
                        val path = entry.path.split("›").map(String::trim).filter(String::isNotEmpty)
                            .ifEmpty { listOf(entry.location.name) }
                        Surface(color = AiFinderPalette.Surface, shape = RoundedCornerShape(24.dp)) {
                            Column(
                                Modifier.fillMaxWidth().padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    Icon(Icons.Outlined.LocationOn, null, tint = AiFinderPalette.Teal)
                                    Text(
                                        stringResource(Res.string.details_location_heading),
                                        color = AiFinderPalette.Ink,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                }
                                path.forEachIndexed { index, name ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    ) {
                                        Box(
                                            Modifier.size(28.dp).background(AiFinderPalette.Mint, RoundedCornerShape(8.dp)),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text("${index + 1}", color = AiFinderPalette.Teal, fontSize = 12.sp)
                                        }
                                        Text(
                                            name,
                                            Modifier.weight(1f),
                                            color = if (index == path.lastIndex) AiFinderPalette.Teal else AiFinderPalette.Ink,
                                            fontSize = 15.sp,
                                            fontWeight = if (index == path.lastIndex) FontWeight.SemiBold else FontWeight.Normal,
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (entry.item.description.isNotBlank()) {
                        item(key = "description") {
                            Surface(color = AiFinderPalette.Surface, shape = RoundedCornerShape(24.dp)) {
                                Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text(
                                        stringResource(Res.string.details_notes),
                                        color = AiFinderPalette.Ink,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 16.sp,
                                    )
                                    Text(entry.item.description, color = AiFinderPalette.Muted, fontSize = 15.sp, lineHeight = 22.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

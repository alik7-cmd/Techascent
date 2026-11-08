package org.techascent.aifinder.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import apphub.aifinder.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.techascent.aifinder.presentation.components.SectionTitle
import org.techascent.aifinder.presentation.theme.AiFinderPalette

@Composable
internal fun SpacesHeader(
    onSeeAll: () -> Unit,
    isEmpty: Boolean = false,
    isLoading: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.weight(1f)) {
            SectionTitle(stringResource(Res.string.home_spaces_title))
        }
        TextButton(onClick = onSeeAll, enabled = !isLoading) {
            if (isLoading) {
                CircularProgressIndicator(Modifier.size(18.dp), color = AiFinderPalette.Teal, strokeWidth = 2.dp)
            } else {
                Text(
                    stringResource(if (isEmpty) Res.string.home_add_location else Res.string.home_see_all),
                    color = AiFinderPalette.Teal,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

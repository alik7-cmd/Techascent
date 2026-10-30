package org.techascent.aifinder.presentation.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
internal fun SpacesHeader(onSeeAll: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SectionTitle(stringResource(Res.string.home_spaces_title))
        TextButton(onClick = onSeeAll) {
            Text(
                stringResource(Res.string.home_see_all),
                color = AiFinderPalette.Teal,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

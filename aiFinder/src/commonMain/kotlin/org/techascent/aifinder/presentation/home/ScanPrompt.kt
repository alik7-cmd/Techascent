package org.techascent.aifinder.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import apphub.aifinder.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import org.techascent.aifinder.presentation.components.SectionTitle
import org.techascent.aifinder.presentation.theme.AiFinderPalette

@Composable
internal fun ScanPrompt(onScan: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(top = 6.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(AiFinderPalette.Mint)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SectionTitle(stringResource(Res.string.home_scan_title))
        Text(
            stringResource(Res.string.home_scan_description),
            color = AiFinderPalette.Muted,
            fontSize = 16.sp,
            lineHeight = 24.sp
        )
        Button(
            onClick = onScan,
            modifier = Modifier.widthIn(min = 220.dp).heightIn(min = 50.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AiFinderPalette.Teal,
                contentColor = AiFinderPalette.Surface
            )
        ) {
            Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                stringResource(Res.string.home_scan_action),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

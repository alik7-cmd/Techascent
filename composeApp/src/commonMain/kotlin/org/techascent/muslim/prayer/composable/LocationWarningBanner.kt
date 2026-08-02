package org.techascent.muslim.prayer.composable

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import apphub.composeapp.generated.resources.Res
import apphub.composeapp.generated.resources.warning_location_gps_off
import org.jetbrains.compose.resources.stringResource
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.messabebox.MessageBox
import org.techascent.composa.messabebox.MessageType

@Composable
internal fun LocationWarningBanner(cityName: String) {
    MessageBox(
        message = stringResource(Res.string.warning_location_gps_off, cityName),
        messageType = MessageType.Warning,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ComposaSpacing.Medium)
            .padding(top = ComposaSpacing.Small)
            .clip(RoundedCornerShape(12.dp)),
    )
}


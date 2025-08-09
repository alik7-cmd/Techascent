package org.techascent.composa.cell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.techascent.composa.cell.center.CenterSlot
import org.techascent.composa.cell.left.LeftSlot
import org.techascent.composa.cell.right.RightSlot
import org.techascent.composa.common.ComposaSpacing
import org.techascent.composa.common.clickModifier
import org.techascent.composa.theming.ComposaTheme

@Composable
fun Cell(
    leftSlot: LeftSlot,
    centerSlot: CenterSlot,
    rightSlot: RightSlot,
    modifier: Modifier = Modifier,
    backgroundColor: Color = ComposaTheme.color.backgroundNeutral,
    onClickLabel: String? = null,
    onClick: (() -> Unit)? = null,
) {
    CellInternal(
        leftSlot = leftSlot,
        centerSlot = centerSlot,
        rightSlot = rightSlot,
        modifier = modifier
            .background(backgroundColor)
            .clickModifier(onClickLabel, onClick)
            .padding(ComposaSpacing.Medium),
    )

}

@Composable
private fun CellInternal(
    leftSlot: LeftSlot,
    centerSlot: CenterSlot,
    rightSlot: RightSlot,
    modifier: Modifier = Modifier,
) {

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = CenterVertically

    ) {
        LeftSlot(
            leftSlot = leftSlot,
            modifier = Modifier.padding(end = ComposaSpacing.Medium)
        )

        CenterSlot(
            centerSlot = centerSlot,
            modifier = Modifier
        )
        val weightModifier =
            if (rightSlot.weight != null) Modifier.weight(rightSlot.weight!!) else Modifier

        RightSlot(
            rightSlot = rightSlot,
            modifier = Modifier.then(weightModifier)
        )

    }

}


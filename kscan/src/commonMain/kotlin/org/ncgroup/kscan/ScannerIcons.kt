package org.ncgroup.kscan

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Lightweight programmatic icon set for the scanner UI.
 * Built with [ImageVector.Builder] using only compose.ui — no material-icons dependency needed.
 */
internal object ScannerIcons {

    /** Plus / Add icon — used for zoom-in */
    val Add: ImageVector by lazy {
        ImageVector.Builder(
            name = "ScannerAdd",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).path(fill = SolidColor(Color.Black)) {
            moveTo(19f, 13f)
            lineTo(13f, 13f)
            lineTo(13f, 19f)
            lineTo(11f, 19f)
            lineTo(11f, 13f)
            lineTo(5f, 13f)
            lineTo(5f, 11f)
            lineTo(11f, 11f)
            lineTo(11f, 5f)
            lineTo(13f, 5f)
            lineTo(13f, 11f)
            lineTo(19f, 11f)
            close()
        }.build()
    }

    /** Minus / Remove icon — used for zoom-out */
    val Remove: ImageVector by lazy {
        ImageVector.Builder(
            name = "ScannerRemove",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).path(fill = SolidColor(Color.Black)) {
            moveTo(19f, 13f)
            lineTo(5f, 13f)
            lineTo(5f, 11f)
            lineTo(19f, 11f)
            close()
        }.build()
    }

    /** × Close / Cancel icon — used for dismiss */
    val Close: ImageVector by lazy {
        ImageVector.Builder(
            name = "ScannerClose",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).path(fill = SolidColor(Color.Black)) {
            moveTo(19f, 6.41f)
            lineTo(17.59f, 5f)
            lineTo(12f, 10.59f)
            lineTo(6.41f, 5f)
            lineTo(5f, 6.41f)
            lineTo(10.59f, 12f)
            lineTo(5f, 17.59f)
            lineTo(6.41f, 19f)
            lineTo(12f, 13.41f)
            lineTo(17.59f, 19f)
            lineTo(19f, 17.59f)
            lineTo(13.41f, 12f)
            close()
        }.build()
    }

    /** Lightning bolt — torch ON */
    val FlashOn: ImageVector by lazy {
        ImageVector.Builder(
            name = "ScannerFlashOn",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).path(fill = SolidColor(Color.Black)) {
            moveTo(7f, 2f)
            lineTo(7f, 13f)
            lineTo(10f, 13f)
            lineTo(10f, 22f)
            lineTo(17f, 11f)
            lineTo(13f, 11f)
            lineTo(17f, 2f)
            close()
        }.build()
    }

    /** Lightning bolt with diagonal slash — torch OFF */
    val FlashOff: ImageVector by lazy {
        ImageVector.Builder(
            name = "ScannerFlashOff",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).path(fill = SolidColor(Color.Black)) {
            // Bolt top half
            moveTo(17f, 2f)
            lineTo(7f, 2f)
            lineTo(7f, 10.58f)
            lineTo(15.56f, 19.14f)
            lineTo(17f, 11f)
            lineTo(13f, 11f)
            close()
        }.path(fill = SolidColor(Color.Black)) {
            // Bolt bottom half (right of slash)
            moveTo(10.62f, 13.62f)
            lineTo(10f, 13f)
            lineTo(10f, 22f)
            lineTo(14.42f, 15.44f)
            close()
        }.path(
            fill = SolidColor(Color.Black),
            stroke = SolidColor(Color.Black),
            strokeLineWidth = 2f,
        ) {
            // Diagonal slash line
            moveTo(3f, 4f)
            lineTo(20f, 21f)
        }.build()
    }
}


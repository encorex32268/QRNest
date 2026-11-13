package com.lihan.qrnest.feature.preview.data

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import io.github.alexzhirkevich.qrose.QrCodePainter
import io.github.alexzhirkevich.qrose.options.QrBackground
import io.github.alexzhirkevich.qrose.options.QrBrush
import io.github.alexzhirkevich.qrose.options.QrColors
import io.github.alexzhirkevich.qrose.options.QrOptions
import io.github.alexzhirkevich.qrose.options.solid

internal object QrPngRenderer {

    private const val QR_SIZE_PX = 512

    fun renderImageBitmap(content: String): ImageBitmap {
        val painter = QrCodePainter(
            data = content,
            options = QrOptions(
                background = QrBackground(
                    fill = SolidColor(Color.White)
                ),
                colors = QrColors(
                    dark = QrBrush.solid(Color.Black),
                    light = QrBrush.solid(Color.White)
                )
            )
        )
        val bitmap = ImageBitmap(QR_SIZE_PX, QR_SIZE_PX)
        val canvas = Canvas(bitmap)
        CanvasDrawScope().draw(
            density = Density(1f),
            layoutDirection = LayoutDirection.Ltr,
            canvas = canvas,
            size = Size(QR_SIZE_PX.toFloat(), QR_SIZE_PX.toFloat())
        ) {
            with(painter) {
                draw(size)
            }
        }
        return bitmap
    }
}

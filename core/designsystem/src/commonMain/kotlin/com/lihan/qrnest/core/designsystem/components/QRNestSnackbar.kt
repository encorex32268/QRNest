package com.lihan.qrnest.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lihan.qrnest.core.designsystem.icon.QRNestIcons
import com.lihan.qrnest.core.designsystem.theme.QRNestTheme
import com.lihan.qrnest.core.designsystem.theme.appColors

@Composable
fun QRNestSnackbar(
    text: String,
    containerColor: Color,
    modifier: Modifier = Modifier,
    contentColor: Color = MaterialTheme.colorScheme.appColors.onOverlay,
    shape: Shape = RoundedCornerShape(12.dp),
    leadingIcon: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .clip(shape)
            .background(
                color = containerColor,
                shape = shape
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = contentColor
        )
    }
}

@Preview
@Composable
private fun QRNestSnackbarPreview() {
    QRNestTheme {
        QRNestSnackbar(
            modifier = Modifier.fillMaxWidth(),
            text = "Image saved to Downloads",
            containerColor = MaterialTheme.colorScheme.appColors.success,
            leadingIcon = {
                Icon(
                    imageVector = QRNestIcons.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        )
    }
}

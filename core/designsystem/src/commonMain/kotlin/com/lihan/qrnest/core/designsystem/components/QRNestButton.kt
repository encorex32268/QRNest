package com.lihan.qrnest.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lihan.qrnest.core.designsystem.icon.QRNestIcons
import com.lihan.qrnest.core.designsystem.theme.PrimaryButtonText
import com.lihan.qrnest.core.designsystem.theme.QRNestTheme
import com.lihan.qrnest.core.designsystem.theme.appColors

@Composable
fun QRNestButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    textColor: Color = MaterialTheme.colorScheme.onPrimary,
    containerColor: Color = Color.Transparent
) {
    Button(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minHeight = 48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            disabledContainerColor = if (containerColor != MaterialTheme.colorScheme.primary) {
                Color.Transparent
            } else {
                MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
            },
            disabledContentColor = MaterialTheme.colorScheme.appColors.onSurfaceDisabled
        )
    ) {
        Row(
            modifier = Modifier.padding(4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                leadingIcon()
                Spacer(Modifier.width(8.dp))
            }
            if (text != null) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (enabled) {
                        textColor
                    } else {
                        MaterialTheme.colorScheme.appColors.onSurfaceDisabled
                    }
                )
            }
            if (trailingIcon != null) {
                Spacer(Modifier.width(8.dp))
                trailingIcon()
            }
        }
    }
}

@Preview
@Composable
private fun QRNestButtonPreview() {
    QRNestTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QRNestButton(
                text = "Primary Enabled",
                onClick = {},
                containerColor = MaterialTheme.colorScheme.primary,
                leadingIcon = {
                    Icon(
                        imageVector = QRNestIcons.Scan,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            )
            QRNestButton(
                text = "Primary Disabled",
                onClick = {},
                containerColor = MaterialTheme.colorScheme.primary,
                enabled = false
            )
            QRNestButton(
                text = "Error Text Button",
                onClick = {},
                textColor = MaterialTheme.colorScheme.error
            )
        }
    }
}

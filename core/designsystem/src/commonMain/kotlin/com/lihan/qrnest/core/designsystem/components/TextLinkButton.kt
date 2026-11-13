package com.lihan.qrnest.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lihan.qrnest.core.designsystem.theme.QRNestTheme
import com.lihan.qrnest.core.designsystem.theme.appColors

@Composable
fun TextLinkButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.appColors.link,
    background: Color = MaterialTheme.colorScheme.appColors.linkBG
) {
    TextButton(
        modifier = modifier,
        onClick = onClick
    ) {
        SelectionContainer {
            Text(
                modifier = Modifier
                    .background(color = background, shape = RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = color
            )
        }
    }
}

@Preview
@Composable
private fun TextLinkButtonPreview() {
    QRNestTheme {
        TextLinkButton(
            text = "https://www.google.com/maps",
            onClick = {}
        )
    }
}

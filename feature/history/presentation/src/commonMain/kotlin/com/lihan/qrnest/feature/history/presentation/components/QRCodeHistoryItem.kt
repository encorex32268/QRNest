@file:OptIn(ExperimentalFoundationApi::class)

package com.lihan.qrnest.feature.history.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lihan.qrnest.core.designsystem.components.QRNestIconButton
import com.lihan.qrnest.core.designsystem.icon.QRNestIcons
import com.lihan.qrnest.core.designsystem.resources.Res
import com.lihan.qrnest.core.designsystem.resources.favorite
import com.lihan.qrnest.core.designsystem.theme.QRNestTheme
import com.lihan.qrnest.core.designsystem.theme.appColors
import com.lihan.qrnest.core.domain.model.QRCodeType
import com.lihan.qrnest.core.presentation.model.QRCodeHistoryUi
import com.lihan.qrnest.core.presentation.util.asString
import com.lihan.qrnest.core.presentation.util.toBackgroundColor
import com.lihan.qrnest.core.presentation.util.toIconTint
import com.lihan.qrnest.core.presentation.util.toImageVector
import org.jetbrains.compose.resources.stringResource

@Composable
fun QRCodeHistoryItem(
    item: QRCodeHistoryUi,
    onItemClick: () -> Unit,
    onItemLongClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val qrCodeType = QRCodeType.fromTypeCode(item.type)

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .combinedClickable(
                onClick = onItemClick,
                onLongClick = onItemLongClick
            ),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.appColors.surfaceHigher,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(qrCodeType.toBackgroundColor()),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        modifier = Modifier.size(18.dp),
                        imageVector = qrCodeType.toImageVector(),
                        contentDescription = null,
                        tint = qrCodeType.toIconTint()
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 40.dp)
                            .basicMarquee(),
                        text = item.title?.takeIf { it.isNotBlank() } ?: qrCodeType.asString(),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = item.content,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.appColors.onSurfaceAlt
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = item.formattedCreatedAt,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.appColors.onSurfaceDisabled
                    )
                }
            }
            QRNestIconButton(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 4.dp, end = 4.dp),
                imageVector = if (item.isFavorite) {
                    QRNestIcons.StarFill
                } else {
                    QRNestIcons.Star
                },
                contentDescription = stringResource(Res.string.favorite),
                onClick = onFavoriteClick,
                containerColor = Color.Transparent,
                tintColor = if (item.isFavorite) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.appColors.onSurfaceDisabled
                }
            )
        }
    }
}

@Preview
@Composable
private fun QRCodeHistoryItemPreview() {
    QRNestTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            QRCodeHistoryItem(
                item = QRCodeHistoryUi(
                    id = 1L,
                    title = null,
                    type = QRCodeType.Link.typeCode,
                    content = "https://www.google.com/maps",
                    createdAt = 1750746960000L,
                    formattedCreatedAt = "24 Jun 2025, 13:16",
                    isGenerated = false,
                    isFavorite = true
                ),
                onItemClick = {},
                onItemLongClick = {},
                onFavoriteClick = {}
            )

            QRCodeHistoryItem(
                item = QRCodeHistoryUi(
                    id = 2L,
                    title = "Meeting Notes",
                    type = QRCodeType.Text.typeCode,
                    content = "Long sample text content that spans across multiple lines to verify truncation behavior in the card.",
                    createdAt = 1750746960000L,
                    formattedCreatedAt = "24 Jun 2025, 13:16",
                    isGenerated = true,
                    isFavorite = false
                ),
                onItemClick = {},
                onItemLongClick = {},
                onFavoriteClick = {}
            )
        }
    }
}

package com.lihan.qrnest.feature.history.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lihan.qrnest.core.designsystem.icon.QRNestIcons
import com.lihan.qrnest.core.designsystem.resources.Res
import com.lihan.qrnest.core.designsystem.resources.history_empty
import com.lihan.qrnest.core.designsystem.resources.history_empty_hint
import com.lihan.qrnest.core.designsystem.theme.appColors
import com.lihan.qrnest.core.presentation.model.QRCodeHistoryUi
import org.jetbrains.compose.resources.stringResource

@Composable
fun QRCodeHistoryList(
    items: List<QRCodeHistoryUi>,
    onItemClick: (Long?) -> Unit,
    onItemLongClick: (Long?) -> Unit,
    onFavoriteClick: (Long?, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    isWideScreen: Boolean = false
) {
    if (items.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.appColors.surfaceHigher)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = QRNestIcons.ClockRefresh,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.appColors.onSurfaceAlt
                    )
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    text = stringResource(Res.string.history_empty),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(Res.string.history_empty_hint),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.appColors.onSurfaceAlt,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else if (isWideScreen) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            items(
                items = items,
                key = { item -> item.id ?: "${item.createdAt}_${item.content.hashCode()}" }
            ) { item ->
                QRCodeHistoryItem(
                    modifier = Modifier.fillMaxWidth(),
                    item = item,
                    onItemClick = { onItemClick(item.id) },
                    onItemLongClick = { onItemLongClick(item.id) },
                    onFavoriteClick = { onFavoriteClick(item.id, item.isFavorite) }
                )
            }
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            items(
                items = items,
                key = { item -> item.id ?: "${item.createdAt}_${item.content.hashCode()}" }
            ) { item ->
                QRCodeHistoryItem(
                    modifier = Modifier.fillMaxWidth(),
                    item = item,
                    onItemClick = { onItemClick(item.id) },
                    onItemLongClick = { onItemLongClick(item.id) },
                    onFavoriteClick = { onFavoriteClick(item.id, item.isFavorite) }
                )
            }
        }
    }
}

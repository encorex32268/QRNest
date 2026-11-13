package com.lihan.qrnest.feature.history.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lihan.qrnest.core.designsystem.resources.Res
import com.lihan.qrnest.core.designsystem.resources.history_empty
import com.lihan.qrnest.core.designsystem.theme.appColors
import com.lihan.qrnest.core.presentation.model.QRCodeHistoryUi
import org.jetbrains.compose.resources.stringResource

@Composable
fun QRCodeHistoryList(
    items: List<QRCodeHistoryUi>,
    onItemClick: (Long?) -> Unit,
    onItemLongClick: (Long?) -> Unit,
    onFavoriteClick: (Long?, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(Res.string.history_empty),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.appColors.onSurfaceAlt
            )
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

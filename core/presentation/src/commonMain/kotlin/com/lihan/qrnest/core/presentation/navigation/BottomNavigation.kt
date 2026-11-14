package com.lihan.qrnest.core.presentation.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lihan.qrnest.core.designsystem.icon.QRNestIcons
import com.lihan.qrnest.core.designsystem.theme.QRNestTheme
import com.lihan.qrnest.core.designsystem.theme.appColors
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun BottomNavigation(
    selectedDestination: TopLevelDestination?,
    onItemClick: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
    items: List<TopLevelDestination> = TopLevelDestination.entries
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.appColors.surfaceHigher,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items.forEach { item ->
                val isSelected = selectedDestination == item
                val isScan = item == TopLevelDestination.SCAN
                val containerColor = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isScan -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    else -> Color.Transparent
                }
                val iconTint = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    isScan -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.appColors.onSurfaceAlt
                }

                Box(
                    modifier = Modifier
                        .width(if (isScan) 64.dp else 52.dp)
                        .height(48.dp)
                        .clip(CircleShape)
                        .background(color = containerColor)
                        .selectable(
                            selected = isSelected,
                            role = Role.Tab,
                            onClick = { onItemClick(item) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        modifier = Modifier.size(if (isScan) 24.dp else 22.dp),
                        imageVector = if (isScan) QRNestIcons.Scan else vectorResource(item.iconRes),
                        contentDescription = stringResource(item.labelRes),
                        tint = iconTint
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun BottomNavigationPreview() {
    QRNestTheme {
        BottomNavigation(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            selectedDestination = TopLevelDestination.SCAN,
            onItemClick = {}
        )
    }
}

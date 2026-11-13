package com.lihan.qrnest.core.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lihan.qrnest.core.designsystem.icon.QRNestIcons
import com.lihan.qrnest.core.designsystem.theme.PrimaryButtonText
import com.lihan.qrnest.core.designsystem.theme.QRNestTheme
import com.lihan.qrnest.core.designsystem.theme.appColors
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun AdaptiveNavigationRail(
    selectedDestination: TopLevelDestination?,
    onItemClick: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier,
    items: List<TopLevelDestination> = TopLevelDestination.entries
) {
    val scanInteractionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.appColors.surfaceHigher,
                    shape = RoundedCornerShape(100)
                )
                .clip(RoundedCornerShape(100))
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items.forEach { item ->
                val isSelected = selectedDestination == item
                if (item == TopLevelDestination.SCAN) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(64.dp)
                            .background(color = MaterialTheme.colorScheme.primary)
                            .clickable(
                                indication = null,
                                interactionSource = scanInteractionSource
                            ) {
                                onItemClick(item)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            modifier = Modifier.size(28.dp),
                            imageVector = QRNestIcons.Scan,
                            contentDescription = stringResource(item.labelRes),
                            tint = PrimaryButtonText
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(44.dp)
                            .clickable { onItemClick(item) }
                            .background(
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            modifier = Modifier.size(22.dp),
                            imageVector = vectorResource(item.iconRes),
                            contentDescription = stringResource(item.labelRes),
                            tint = if (isSelected) PrimaryButtonText else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun AdaptiveNavigationRailPreview() {
    QRNestTheme {
        AdaptiveNavigationRail(
            modifier = Modifier.padding(16.dp),
            selectedDestination = TopLevelDestination.SCAN,
            onItemClick = {}
        )
    }
}

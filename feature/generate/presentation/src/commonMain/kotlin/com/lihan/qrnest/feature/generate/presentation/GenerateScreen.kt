@file:OptIn(ExperimentalMaterial3Api::class)

package com.lihan.qrnest.feature.generate.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lihan.qrnest.core.designsystem.resources.Res
import com.lihan.qrnest.core.designsystem.resources.create_qr
import com.lihan.qrnest.core.designsystem.theme.QRNestTheme
import com.lihan.qrnest.core.domain.model.QRCodeType
import com.lihan.qrnest.core.presentation.navigation.MainAdaptiveLayout
import com.lihan.qrnest.core.presentation.navigation.TopLevelDestination
import com.lihan.qrnest.core.presentation.util.asString
import com.lihan.qrnest.core.presentation.util.toBackgroundColor
import com.lihan.qrnest.core.presentation.util.toIconTint
import com.lihan.qrnest.core.presentation.util.toImageVector
import com.lihan.qrnest.feature.generate.presentation.components.GenerateTypeCard
import org.jetbrains.compose.resources.stringResource

@Composable
fun GenerateScreenRoot(
    onItemClick: (QRCodeType) -> Unit,
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    GenerateScreen(
        onItemClick = onItemClick,
        onNavigateToDestination = onNavigateToDestination,
        modifier = modifier
    )
}

@Composable
fun GenerateScreen(
    onItemClick: (QRCodeType) -> Unit,
    onNavigateToDestination: (TopLevelDestination) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val items = remember { QRCodeType.entries }

    MainAdaptiveLayout(
        selectedDestination = TopLevelDestination.GENERATE,
        onNavigateToDestination = onNavigateToDestination,
        modifier = modifier
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .padding(innerPadding)
        ) {
            val isWideDevice = maxWidth >= 600.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    title = {
                        Text(
                            text = stringResource(Res.string.create_qr),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                )
                Spacer(Modifier.height(16.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(if (isWideDevice) 3 else 2),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(
                        items = items,
                        key = { it.typeCode }
                    ) { qrCodeType ->
                        GenerateTypeCard(
                            text = qrCodeType.asString(),
                            imageVector = qrCodeType.toImageVector(),
                            iconTintColor = qrCodeType.toIconTint(),
                            iconBackgroundColor = qrCodeType.toBackgroundColor(),
                            onItemClick = { onItemClick(qrCodeType) }
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun GenerateScreenPreview() {
    QRNestTheme {
        GenerateScreen(
            onItemClick = {}
        )
    }
}

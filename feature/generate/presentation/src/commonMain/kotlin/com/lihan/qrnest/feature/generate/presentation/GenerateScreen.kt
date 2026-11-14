package com.lihan.qrnest.feature.generate.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lihan.qrnest.core.designsystem.resources.Res
import com.lihan.qrnest.core.designsystem.resources.create_qr
import com.lihan.qrnest.core.designsystem.resources.generate_subtitle
import com.lihan.qrnest.core.designsystem.resources.type_desc_contact
import com.lihan.qrnest.core.designsystem.resources.type_desc_geo
import com.lihan.qrnest.core.designsystem.resources.type_desc_link
import com.lihan.qrnest.core.designsystem.resources.type_desc_phone
import com.lihan.qrnest.core.designsystem.resources.type_desc_text
import com.lihan.qrnest.core.designsystem.resources.type_desc_wifi
import com.lihan.qrnest.core.designsystem.theme.QRNestTheme
import com.lihan.qrnest.core.designsystem.theme.appColors
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
                    .statusBarsPadding()
                    .displayCutoutPadding()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(20.dp))
                Text(
                    text = stringResource(Res.string.create_qr),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(Res.string.generate_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.appColors.onSurfaceAlt
                )
                Spacer(Modifier.height(20.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(if (isWideDevice) 3 else 2),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(
                        items = items,
                        key = { it.typeCode }
                    ) { qrCodeType ->
                        GenerateTypeCard(
                            text = qrCodeType.asString(),
                            description = qrCodeType.descriptionString(),
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

@Composable
private fun QRCodeType.descriptionString(): String {
    return when (this) {
        QRCodeType.Text -> stringResource(Res.string.type_desc_text)
        QRCodeType.Link -> stringResource(Res.string.type_desc_link)
        QRCodeType.Contact -> stringResource(Res.string.type_desc_contact)
        QRCodeType.PhoneNumber -> stringResource(Res.string.type_desc_phone)
        QRCodeType.Geolocation -> stringResource(Res.string.type_desc_geo)
        QRCodeType.WiFi -> stringResource(Res.string.type_desc_wifi)
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

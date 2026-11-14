@file:OptIn(ExperimentalMaterial3Api::class)

package com.lihan.qrnest.feature.history.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lihan.qrnest.core.designsystem.components.QRNestButton
import com.lihan.qrnest.core.designsystem.icon.QRNestIcons
import com.lihan.qrnest.core.designsystem.resources.Res
import com.lihan.qrnest.core.designsystem.resources.delete
import com.lihan.qrnest.core.designsystem.resources.generated
import com.lihan.qrnest.core.designsystem.resources.scan_history
import com.lihan.qrnest.core.designsystem.resources.scanned
import com.lihan.qrnest.core.designsystem.resources.share
import com.lihan.qrnest.core.designsystem.theme.QRNestTheme
import com.lihan.qrnest.core.designsystem.theme.appColors
import com.lihan.qrnest.core.domain.model.QRCodeType
import com.lihan.qrnest.core.presentation.model.QRCodeHistoryUi
import com.lihan.qrnest.core.presentation.navigation.MainAdaptiveLayout
import com.lihan.qrnest.core.presentation.navigation.TopLevelDestination
import com.lihan.qrnest.core.presentation.util.ExternalAppLauncher
import com.lihan.qrnest.core.presentation.util.ObserveAsEvents
import com.lihan.qrnest.feature.history.presentation.components.QRCodeHistoryList
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

private const val TAB_SCANNED = 0
private const val TAB_GENERATED = 1

@Composable
fun ScanHistoryScreenRoot(
    onNavigateToPreview: (id: Long) -> Unit,
    onNavigateToDestination: (TopLevelDestination) -> Unit = {},
    externalAppLauncher: ExternalAppLauncher = koinInject(),
    viewModel: ScanHistoryViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ScanHistoryEvent.ShareQRCode -> externalAppLauncher.shareText(event.content)
            is ScanHistoryEvent.NavigateToPreview -> onNavigateToPreview(event.id)
        }
    }

    ScanHistoryScreen(
        state = state,
        onAction = viewModel::onAction,
        onNavigateToDestination = onNavigateToDestination
    )
}

@Composable
fun ScanHistoryScreen(
    state: ScanHistoryState,
    onAction: (ScanHistoryAction) -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToDestination: (TopLevelDestination) -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = TAB_SCANNED,
        pageCount = { 2 }
    )

    MainAdaptiveLayout(
        selectedDestination = TopLevelDestination.HISTORY,
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
                    .displayCutoutPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(20.dp))
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = stringResource(Res.string.scan_history),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(16.dp))
                Surface(
                    modifier = Modifier
                        .widthIn(max = 480.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.appColors.surfaceHigher,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        HistorySegmentTab(
                            modifier = Modifier.weight(1f),
                            title = stringResource(Res.string.scanned),
                            selected = pagerState.currentPage == TAB_SCANNED,
                            onClick = {
                                scope.launch { pagerState.animateScrollToPage(TAB_SCANNED) }
                            }
                        )
                        HistorySegmentTab(
                            modifier = Modifier.weight(1f),
                            title = stringResource(Res.string.generated),
                            selected = pagerState.currentPage == TAB_GENERATED,
                            onClick = {
                                scope.launch { pagerState.animateScrollToPage(TAB_GENERATED) }
                            }
                        )
                    }
                }
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) { page ->
                    val displayItems = if (page == TAB_SCANNED) {
                        state.scannedItems
                    } else {
                        state.generatedItems
                    }

                    QRCodeHistoryList(
                        items = displayItems,
                        isWideScreen = isWideDevice,
                        onItemClick = { onAction(ScanHistoryAction.ItemClick(it)) },
                        onItemLongClick = { onAction(ScanHistoryAction.ItemLongClick(it)) },
                        onFavoriteClick = { id, isFavorite ->
                            onAction(ScanHistoryAction.ItemFavoriteClick(id, isFavorite))
                        }
                    )
                }
            }

            if (state.isShowEditorBottomSheet) {
                ModalBottomSheet(
                    modifier = Modifier
                        .then(
                            if (isWideDevice) {
                                Modifier.widthIn(max = 412.dp)
                            } else {
                                Modifier
                            }
                        )
                        .fillMaxWidth(),
                    onDismissRequest = { onAction(ScanHistoryAction.DismissEditorBottomSheet) },
                    dragHandle = null,
                    scrimColor = Color.Black.copy(alpha = 0.32f),
                    containerColor = MaterialTheme.colorScheme.appColors.surfaceHigher,
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        QRNestButton(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(Res.string.share),
                            textColor = MaterialTheme.colorScheme.onSurface,
                            leadingIcon = {
                                Icon(
                                    imageVector = QRNestIcons.Share,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    contentDescription = stringResource(Res.string.share)
                                )
                            },
                            onClick = { onAction(ScanHistoryAction.ShareClick) }
                        )
                        QRNestButton(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(Res.string.delete),
                            textColor = MaterialTheme.colorScheme.error,
                            leadingIcon = {
                                Icon(
                                    imageVector = QRNestIcons.Trash,
                                    tint = MaterialTheme.colorScheme.error,
                                    contentDescription = stringResource(Res.string.delete)
                                )
                            },
                            onClick = { onAction(ScanHistoryAction.DeleteClick) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistorySegmentTab(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(
                color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
            )
            .selectable(
                selected = selected,
                role = Role.Tab,
                onClick = onClick
            )
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.appColors.onSurfaceAlt
            }
        )
    }
}

@Preview
@Composable
private fun ScanHistoryScreenPreview() {
    QRNestTheme {
        ScanHistoryScreen(
            state = ScanHistoryState(
                scannedItems = listOf(
                    QRCodeHistoryUi(
                        id = 1L,
                        title = null,
                        type = QRCodeType.WiFi.typeCode,
                        content = "SSID: Home\nPassword: secret\nEncryption type: WPA2",
                        createdAt = 1750746960000L,
                        formattedCreatedAt = "24 Jun 2025, 13:16",
                        isGenerated = false,
                        isFavorite = false
                    ),
                    QRCodeHistoryUi(
                        id = 2L,
                        title = "Office WiFi",
                        type = QRCodeType.WiFi.typeCode,
                        content = "SSID: Office",
                        createdAt = 1750746960000L,
                        formattedCreatedAt = "24 Jun 2025, 13:16",
                        isGenerated = false,
                        isFavorite = true
                    )
                ),
                generatedItems = listOf(
                    QRCodeHistoryUi(
                        id = 3L,
                        title = "Map",
                        type = QRCodeType.Geolocation.typeCode,
                        content = "25.033964,121.564468",
                        createdAt = 1750746960000L,
                        formattedCreatedAt = "24 Jun 2025, 13:16",
                        isGenerated = true,
                        isFavorite = false
                    )
                )
            ),
            onAction = {}
        )
    }
}

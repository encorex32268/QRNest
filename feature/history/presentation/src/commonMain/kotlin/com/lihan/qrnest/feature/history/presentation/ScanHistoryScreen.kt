@file:OptIn(ExperimentalMaterial3Api::class)

package com.lihan.qrnest.feature.history.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
                modifier = Modifier.fillMaxSize()
            ) {
                CenterAlignedTopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    title = {
                        Text(
                            text = stringResource(Res.string.scan_history),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                )
                SecondaryTabRow(
                    selectedTabIndex = pagerState.currentPage,
                    containerColor = MaterialTheme.colorScheme.surface,
                    divider = {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                    },
                    indicator = {
                        SecondaryIndicator(
                            modifier = Modifier
                                .tabIndicatorOffset(pagerState.currentPage, matchContentSize = false)
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .wrapContentSize(Alignment.BottomCenter)
                                .clip(RoundedCornerShape(100)),
                            color = MaterialTheme.colorScheme.onSurface,
                            height = 2.dp
                        )
                    }
                ) {
                    Tab(
                        selected = pagerState.currentPage == TAB_SCANNED,
                        onClick = {
                            scope.launch { pagerState.animateScrollToPage(TAB_SCANNED) }
                        },
                        selectedContentColor = MaterialTheme.colorScheme.onSurface,
                        unselectedContentColor = MaterialTheme.colorScheme.appColors.onSurfaceAlt
                    ) {
                        Text(
                            modifier = Modifier.padding(vertical = 16.dp),
                            text = stringResource(Res.string.scanned),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Tab(
                        selected = pagerState.currentPage == TAB_GENERATED,
                        onClick = {
                            scope.launch { pagerState.animateScrollToPage(TAB_GENERATED) }
                        },
                        selectedContentColor = MaterialTheme.colorScheme.onSurface,
                        unselectedContentColor = MaterialTheme.colorScheme.appColors.onSurfaceAlt
                    ) {
                        Text(
                            modifier = Modifier.padding(vertical = 16.dp),
                            text = stringResource(Res.string.generated),
                            style = MaterialTheme.typography.labelMedium
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

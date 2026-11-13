@file:OptIn(ExperimentalMaterial3Api::class)

package com.lihan.qrnest.feature.preview.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lihan.qrnest.core.designsystem.components.QRNestIconButton
import com.lihan.qrnest.core.designsystem.components.QRNestSnackbar
import com.lihan.qrnest.core.designsystem.icon.QRNestIcons
import com.lihan.qrnest.core.designsystem.resources.Res
import com.lihan.qrnest.core.designsystem.resources.back
import com.lihan.qrnest.core.designsystem.resources.favorite
import com.lihan.qrnest.core.designsystem.resources.preview
import com.lihan.qrnest.core.designsystem.resources.qr_code_not_found
import com.lihan.qrnest.core.designsystem.resources.save_qrcode_image_succeed
import com.lihan.qrnest.core.designsystem.resources.scan_result
import com.lihan.qrnest.core.designsystem.theme.QRNestTheme
import com.lihan.qrnest.core.designsystem.theme.appColors
import com.lihan.qrnest.core.domain.model.QRCodeType
import com.lihan.qrnest.core.presentation.model.QRCodeHistoryUi
import com.lihan.qrnest.core.presentation.util.ExternalAppLauncher
import com.lihan.qrnest.core.presentation.util.ObserveAsEvents
import com.lihan.qrnest.feature.preview.presentation.components.QRCodePreviewCard
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PreviewScreenRoot(
    onBack: () -> Unit,
    externalAppLauncher: ExternalAppLauncher = koinInject(),
    viewModel: PreviewViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val notFoundMessage = stringResource(Res.string.qr_code_not_found)
    val saveSuccessMessage = stringResource(Res.string.save_qrcode_image_succeed)

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            PreviewEvent.Back -> onBack()
            PreviewEvent.DataError -> {
                scope.launch { snackbarHostState.showSnackbar(notFoundMessage) }
                onBack()
            }
            PreviewEvent.SaveSucceed -> {
                scope.launch { snackbarHostState.showSnackbar(saveSuccessMessage) }
            }
            is PreviewEvent.ShowError -> {
                scope.launch {
                    val msg = event.message.asStringAsync()
                    snackbarHostState.showSnackbar(msg)
                }
            }
            is PreviewEvent.ShareQRCode -> externalAppLauncher.shareText(event.content)
            is PreviewEvent.OpenUrl -> externalAppLauncher.openUrl(event.url)
            is PreviewEvent.DialPhone -> externalAppLauncher.dialPhone(event.phone)
            is PreviewEvent.OpenGeo -> externalAppLauncher.openGeo(event.latLng)
            is PreviewEvent.InsertContact -> externalAppLauncher.insertContact(
                name = event.name,
                email = event.email,
                phone = event.phone
            )
            PreviewEvent.OpenWifiSettings -> externalAppLauncher.openWifiSettings()
        }
    }

    PreviewScreen(
        snackbarHostState = snackbarHostState,
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun PreviewScreen(
    snackbarHostState: SnackbarHostState,
    state: PreviewState,
    onAction: (PreviewAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val screenTitle = if (state.isScanResult) {
        stringResource(Res.string.scan_result)
    } else {
        stringResource(Res.string.preview)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { data ->
                    QRNestSnackbar(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(bottom = 16.dp),
                        text = data.visuals.message,
                        containerColor = MaterialTheme.colorScheme.appColors.success,
                        leadingIcon = {
                            Icon(
                                imageVector = QRNestIcons.Check,
                                tint = MaterialTheme.colorScheme.onSurface,
                                contentDescription = null
                            )
                        }
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = screenTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    QRNestIconButton(
                        imageVector = QRNestIcons.ArrowLeft,
                        contentDescription = stringResource(Res.string.back),
                        containerColor = Color.Transparent,
                        tintColor = MaterialTheme.colorScheme.onSurface,
                        iconSize = 20.dp,
                        onClick = { onAction(PreviewAction.BackClick) }
                    )
                },
                actions = {
                    QRNestIconButton(
                        imageVector = if (state.qrCodeHistoryUi?.isFavorite == true) {
                            QRNestIcons.StarFill
                        } else {
                            QRNestIcons.Star
                        },
                        contentDescription = stringResource(Res.string.favorite),
                        containerColor = Color.Transparent,
                        tintColor = MaterialTheme.colorScheme.onSurface,
                        iconSize = 24.dp,
                        onClick = { onAction(PreviewAction.FavoriteClick) }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
            Spacer(Modifier.height(28.dp))
            state.qrCodeHistoryUi?.let { item ->
                QRCodePreviewCard(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .widthIn(max = 480.dp)
                        .padding(16.dp),
                    titleInput = state.titleInput,
                    onTitleChanged = { onAction(PreviewAction.OnTitleChanged(it)) },
                    type = item.type,
                    savedTitle = item.title,
                    content = item.content,
                    onPrimaryContentClick = { onAction(PreviewAction.ContentPrimaryActionClick) },
                    onShare = { onAction(PreviewAction.ShareClick) },
                    onCopy = { onAction(PreviewAction.CopyClick) },
                    onSave = { onAction(PreviewAction.SaveClick) },
                    isSaving = state.isSaving
                )
            }
        }
    }
}

@Preview
@Composable
private fun PreviewScreenPreview() {
    QRNestTheme {
        PreviewScreen(
            snackbarHostState = remember { SnackbarHostState() },
            state = PreviewState(
                qrCodeHistoryUi = QRCodeHistoryUi(
                    id = 1L,
                    title = "QRNest Docs",
                    type = QRCodeType.Link.typeCode,
                    content = "https://kotlinlang.org",
                    createdAt = 1750746960000L,
                    formattedCreatedAt = "24 Jun 2025, 13:16",
                    isGenerated = true,
                    isFavorite = true
                ),
                titleInput = "QRNest Docs"
            ),
            onAction = {}
        )
    }
}

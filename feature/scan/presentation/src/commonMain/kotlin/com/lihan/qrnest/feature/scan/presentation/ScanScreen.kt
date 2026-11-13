package com.lihan.qrnest.feature.scan.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lihan.qrnest.core.designsystem.components.CircleIcon
import com.lihan.qrnest.core.designsystem.components.QRNestSnackbar
import com.lihan.qrnest.core.designsystem.icon.QRNestIcons
import com.lihan.qrnest.core.designsystem.resources.Res
import com.lihan.qrnest.core.designsystem.resources.camera_permission_granted
import com.lihan.qrnest.core.designsystem.resources.flashlight
import com.lihan.qrnest.core.designsystem.resources.loading
import com.lihan.qrnest.core.designsystem.resources.pick_image
import com.lihan.qrnest.core.designsystem.resources.point_your_camera_at_a_qr_code
import com.lihan.qrnest.core.designsystem.resources.qrcode_preview
import com.lihan.qrnest.core.designsystem.theme.QRNestTheme
import com.lihan.qrnest.core.designsystem.theme.appColors
import com.lihan.qrnest.core.domain.model.ScannedBarcode
import com.lihan.qrnest.core.presentation.navigation.MainAdaptiveLayout
import com.lihan.qrnest.core.presentation.navigation.TopLevelDestination
import com.lihan.qrnest.core.presentation.util.ObserveAsEvents
import com.lihan.qrnest.feature.scan.presentation.components.CameraPermissionDialog
import com.lihan.qrnest.feature.scan.presentation.components.CameraScannerView
import com.lihan.qrnest.feature.scan.presentation.components.NoQRCodeFoundDialog
import com.lihan.qrnest.feature.scan.presentation.components.ScanFrame
import com.lihan.qrnest.feature.scan.presentation.util.CameraPermissionStatus
import com.lihan.qrnest.feature.scan.presentation.util.rememberCameraPermissionController
import com.lihan.qrnest.feature.scan.presentation.util.rememberImagePickerLauncher
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ScanScreenRoot(
    onNavigateToPreview: (id: Long) -> Unit,
    onCloseApp: () -> Unit,
    onNavigateToDestination: (TopLevelDestination) -> Unit = {},
    viewModel: ScanViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val permissionGrantedMessage = stringResource(Res.string.camera_permission_granted)

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is ScanEvent.NavigateToPreview -> onNavigateToPreview(event.id)
        }
    }

    val permissionController = rememberCameraPermissionController(
        onPermissionGranted = {
            viewModel.onAction(ScanAction.DismissCameraPermissionDialog)
            scope.launch {
                snackbarHostState.showSnackbar(permissionGrantedMessage)
            }
        }
    )

    val imagePicker = rememberImagePickerLauncher { imageBytes ->
        viewModel.onAction(ScanAction.OnImagePicked(imageBytes))
    }

    LaunchedEffect(permissionController.status) {
        when (permissionController.status) {
            CameraPermissionStatus.GRANTED -> {
                viewModel.onAction(ScanAction.DismissCameraPermissionDialog)
            }
            CameraPermissionStatus.DENIED,
            CameraPermissionStatus.PERMANENTLY_DENIED -> {
                viewModel.onAction(ScanAction.ShowCameraPermissionDialog)
            }
        }
    }

    ScanScreen(
        state = state,
        permissionStatus = permissionController.status,
        snackbarHostState = snackbarHostState,
        onAction = { action ->
            when (action) {
                ScanAction.CloseAppClick -> onCloseApp()
                else -> viewModel.onAction(action)
            }
        },
        onGrantPermissionClick = {
            when (permissionController.status) {
                CameraPermissionStatus.GRANTED -> {
                    viewModel.onAction(ScanAction.DismissCameraPermissionDialog)
                }
                CameraPermissionStatus.DENIED -> {
                    permissionController.requestPermission()
                }
                CameraPermissionStatus.PERMANENTLY_DENIED -> {
                    permissionController.openSettings()
                }
            }
        },
        onPickImageClick = { imagePicker.launch() },
        onNavigateToDestination = onNavigateToDestination
    )
}

@Composable
fun ScanScreen(
    state: ScanState,
    permissionStatus: CameraPermissionStatus,
    snackbarHostState: SnackbarHostState,
    onAction: (ScanAction) -> Unit,
    onGrantPermissionClick: () -> Unit,
    onPickImageClick: () -> Unit,
    modifier: Modifier = Modifier,
    onNavigateToDestination: (TopLevelDestination) -> Unit = {}
) {
    MainAdaptiveLayout(
        selectedDestination = TopLevelDestination.SCAN,
        onNavigateToDestination = onNavigateToDestination,
        floatingBottomBar = true,
        containerColor = MaterialTheme.colorScheme.appColors.onSurfaceAlt,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { data ->
                    QRNestSnackbar(
                        modifier = Modifier
                            .navigationBarsPadding()
                            .padding(bottom = 80.dp),
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
        },
        modifier = modifier
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when {
                state.isLoading -> {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.appColors.onOverlay,
                            strokeWidth = 4.dp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(Res.string.loading),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.appColors.onOverlay
                        )
                    }
                }

                permissionStatus == CameraPermissionStatus.GRANTED -> {
                    CameraScannerContent(
                        isFlashOn = state.isOpeningFlashlight,
                        onFlashClick = { onAction(ScanAction.FlashClick) },
                        onPickImageClick = onPickImageClick,
                        onBarcodeScanned = { scanned ->
                            onAction(ScanAction.OnBarcodeScanned(scanned))
                        },
                        onScanError = { onAction(ScanAction.OnCameraScanFailed) }
                    )
                }

                else -> {
                    Image(
                        modifier = Modifier.size(320.dp),
                        painter = painterResource(Res.drawable.qrcode_preview),
                        contentDescription = null
                    )
                }
            }

            if (state.isShowCameraPermissionDialog && permissionStatus != CameraPermissionStatus.GRANTED) {
                CameraPermissionDialog(
                    onCloseApp = { onAction(ScanAction.CloseAppClick) },
                    onGrantAccess = onGrantPermissionClick
                )
            }

            if (state.isShowNoQRCodesFound) {
                NoQRCodeFoundDialog(
                    onDismiss = { onAction(ScanAction.DismissNoQRCodeFoundDialog) }
                )
            }
        }
    }
}

@Composable
private fun CameraScannerContent(
    isFlashOn: Boolean,
    onFlashClick: () -> Unit,
    onPickImageClick: () -> Unit,
    onBarcodeScanned: (ScannedBarcode) -> Unit,
    onScanError: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CameraScannerView(
            isFlashOn = isFlashOn,
            onBarcodeScanned = onBarcodeScanned,
            onScanError = onScanError,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = MaterialTheme.colorScheme.appColors.onSurfaceAlt.copy(alpha = 0.5f)
                )
        )

        CircleIcon(
            backgroundColor = if (isFlashOn) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.appColors.surfaceHigher
            },
            iconTintColor = MaterialTheme.colorScheme.onSurface,
            imageVector = if (isFlashOn) QRNestIcons.FlashOff else QRNestIcons.Flash,
            contentDescription = stringResource(Res.string.flashlight),
            modifier = Modifier
                .align(Alignment.TopStart)
                .systemBarsPadding()
                .padding(top = 20.dp, start = 24.dp)
                .size(44.dp),
            onClick = onFlashClick
        )

        CircleIcon(
            backgroundColor = MaterialTheme.colorScheme.appColors.surfaceHigher,
            iconTintColor = MaterialTheme.colorScheme.onSurface,
            imageVector = QRNestIcons.Image,
            contentDescription = stringResource(Res.string.pick_image),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .systemBarsPadding()
                .padding(top = 20.dp, end = 24.dp)
                .size(44.dp),
            onClick = onPickImageClick
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(Res.string.point_your_camera_at_a_qr_code),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.appColors.onOverlay
            )
            Spacer(Modifier.height(32.dp))
            ScanFrame(modifier = Modifier.size(320.dp))
        }
    }
}

@Preview
@Composable
private fun ScanScreenPermissionPreview() {
    QRNestTheme {
        ScanScreen(
            state = ScanState(isShowCameraPermissionDialog = true),
            permissionStatus = CameraPermissionStatus.DENIED,
            snackbarHostState = remember { SnackbarHostState() },
            onAction = {},
            onGrantPermissionClick = {},
            onPickImageClick = {}
        )
    }
}

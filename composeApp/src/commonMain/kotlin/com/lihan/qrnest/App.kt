package com.lihan.qrnest

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import com.lihan.qrnest.core.designsystem.theme.QRNestTheme
import com.lihan.qrnest.feature.generate.presentation.navigation.CreateRoute
import com.lihan.qrnest.feature.generate.presentation.navigation.generateGraph
import com.lihan.qrnest.feature.history.presentation.navigation.historyGraph
import com.lihan.qrnest.feature.preview.presentation.navigation.PreviewRoute
import com.lihan.qrnest.feature.preview.presentation.navigation.previewGraph
import com.lihan.qrnest.feature.scan.presentation.navigation.ScanRoute
import com.lihan.qrnest.feature.scan.presentation.navigation.scanGraph
import com.lihan.qrnest.navigation.rememberQRNestAppState

@Composable
fun App(
    onCloseApp: () -> Unit = {}
) {
    QRNestTheme {
        val appState = rememberQRNestAppState()

        NavHost(
            modifier = Modifier.fillMaxSize(),
            navController = appState.navController,
            startDestination = ScanRoute
        ) {
            scanGraph(
                onNavigateToDestination = appState::navigateToTopLevelDestination,
                onNavigateToPreview = { id ->
                    appState.navController.navigate(
                        PreviewRoute(id = id, isScanResult = true)
                    )
                },
                onCloseApp = onCloseApp
            )

            generateGraph(
                onNavigateToDestination = appState::navigateToTopLevelDestination,
                onNavigateToCreate = { qrCodeType ->
                    appState.navController.navigate(
                        CreateRoute(type = qrCodeType.typeCode)
                    )
                },
                onNavigateToPreview = { id ->
                    appState.navController.navigate(
                        PreviewRoute(id = id, isScanResult = false)
                    )
                },
                onBack = appState::safeNavigateUp
            )

            historyGraph(
                onNavigateToDestination = appState::navigateToTopLevelDestination,
                onNavigateToPreview = { id ->
                    appState.navController.navigate(
                        PreviewRoute(id = id, isScanResult = false)
                    )
                }
            )

            previewGraph(
                onBack = appState::safeNavigateUp
            )
        }
    }
}

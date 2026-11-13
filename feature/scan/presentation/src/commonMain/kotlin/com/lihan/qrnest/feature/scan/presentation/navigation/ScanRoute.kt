package com.lihan.qrnest.feature.scan.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.lihan.qrnest.core.presentation.navigation.TopLevelDestination
import com.lihan.qrnest.feature.scan.presentation.ScanScreenRoot
import kotlinx.serialization.Serializable

@Serializable
data object ScanRoute

fun NavGraphBuilder.scanGraph(
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    onNavigateToPreview: (id: Long) -> Unit,
    onCloseApp: () -> Unit
) {
    composable<ScanRoute> {
        ScanScreenRoot(
            onNavigateToDestination = onNavigateToDestination,
            onNavigateToPreview = onNavigateToPreview,
            onCloseApp = onCloseApp
        )
    }
}

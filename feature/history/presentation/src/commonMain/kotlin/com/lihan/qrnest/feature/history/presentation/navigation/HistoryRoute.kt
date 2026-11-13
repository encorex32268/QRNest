package com.lihan.qrnest.feature.history.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.lihan.qrnest.core.presentation.navigation.TopLevelDestination
import com.lihan.qrnest.feature.history.presentation.ScanHistoryScreenRoot
import kotlinx.serialization.Serializable

@Serializable
data object HistoryRoute

fun NavGraphBuilder.historyGraph(
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    onNavigateToPreview: (id: Long) -> Unit
) {
    composable<HistoryRoute> {
        ScanHistoryScreenRoot(
            onNavigateToDestination = onNavigateToDestination,
            onNavigateToPreview = onNavigateToPreview
        )
    }
}

package com.lihan.qrnest.feature.preview.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.lihan.qrnest.feature.preview.presentation.PreviewScreenRoot
import kotlinx.serialization.Serializable

@Serializable
data class PreviewRoute(
    val id: Long,
    val isScanResult: Boolean = false
)

fun NavGraphBuilder.previewGraph(
    onBack: () -> Unit
) {
    composable<PreviewRoute> {
        PreviewScreenRoot(
            onBack = onBack
        )
    }
}

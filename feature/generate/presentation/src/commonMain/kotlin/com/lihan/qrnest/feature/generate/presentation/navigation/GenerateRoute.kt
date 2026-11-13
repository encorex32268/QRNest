package com.lihan.qrnest.feature.generate.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.lihan.qrnest.core.domain.model.QRCodeType
import com.lihan.qrnest.core.presentation.navigation.TopLevelDestination
import com.lihan.qrnest.feature.generate.presentation.GenerateScreenRoot
import com.lihan.qrnest.feature.generate.presentation.create.CreateScreenRoot
import kotlinx.serialization.Serializable

@Serializable
data object GenerateGraphRoute

@Serializable
data object GenerateRoute

@Serializable
data class CreateRoute(val type: Int)

fun NavGraphBuilder.generateGraph(
    onNavigateToDestination: (TopLevelDestination) -> Unit,
    onNavigateToCreate: (QRCodeType) -> Unit,
    onNavigateToPreview: (id: Long) -> Unit,
    onBack: () -> Unit
) {
    navigation<GenerateGraphRoute>(
        startDestination = GenerateRoute
    ) {
        composable<GenerateRoute> {
            GenerateScreenRoot(
                onNavigateToDestination = onNavigateToDestination,
                onItemClick = onNavigateToCreate
            )
        }
        composable<CreateRoute> {
            CreateScreenRoot(
                onNavigateToPreview = onNavigateToPreview,
                onBack = onBack
            )
        }
    }
}

package com.lihan.qrnest.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.lihan.qrnest.core.presentation.navigation.TopLevelDestination
import com.lihan.qrnest.feature.generate.presentation.navigation.GenerateRoute
import com.lihan.qrnest.feature.history.presentation.navigation.HistoryRoute
import com.lihan.qrnest.feature.scan.presentation.navigation.ScanRoute

@Stable
class QRNestAppState(
    val navController: NavHostController
) {
    fun navigateToTopLevelDestination(destination: TopLevelDestination) {
        val route: Any = when (destination) {
            TopLevelDestination.HISTORY -> HistoryRoute
            TopLevelDestination.SCAN -> ScanRoute
            TopLevelDestination.GENERATE -> GenerateRoute
        }
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun safeNavigateUp() {
        if (navController.previousBackStackEntry != null) {
            navController.navigateUp()
        }
    }
}

@Composable
fun rememberQRNestAppState(
    navController: NavHostController = rememberNavController()
): QRNestAppState {
    return remember(navController) {
        QRNestAppState(navController = navController)
    }
}

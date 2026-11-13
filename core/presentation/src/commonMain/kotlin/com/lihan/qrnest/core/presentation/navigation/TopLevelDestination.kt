package com.lihan.qrnest.core.presentation.navigation

import com.lihan.qrnest.core.designsystem.resources.Res
import com.lihan.qrnest.core.designsystem.resources.clock_refresh
import com.lihan.qrnest.core.designsystem.resources.generate
import com.lihan.qrnest.core.designsystem.resources.history
import com.lihan.qrnest.core.designsystem.resources.plus_circle
import com.lihan.qrnest.core.designsystem.resources.scan
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

enum class TopLevelDestination(
    val iconRes: DrawableResource,
    val labelRes: StringResource
) {
    HISTORY(
        iconRes = Res.drawable.clock_refresh,
        labelRes = Res.string.history
    ),
    SCAN(
        iconRes = Res.drawable.scan,
        labelRes = Res.string.scan
    ),
    GENERATE(
        iconRes = Res.drawable.plus_circle,
        labelRes = Res.string.generate
    )
}

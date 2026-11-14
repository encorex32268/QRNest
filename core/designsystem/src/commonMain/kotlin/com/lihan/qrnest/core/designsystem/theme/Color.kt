package com.lihan.qrnest.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val PrimaryButtonText = Color(0xFFFFFFFF)

val LightColorScheme = lightColorScheme(
    primary = Color(0xFFB45309),
    onPrimary = Color(0xFFFFFFFF),
    surface = Color(0xFFF7F5F0),
    onSurface = Color(0xFF241D14),
    outline = Color(0xFFE6DFD3),
    error = Color(0xFFDC2626)
)

val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFF59E0B),
    onPrimary = Color(0xFF241604),
    surface = Color(0xFF15120D),
    onSurface = Color(0xFFF5EFE6),
    outline = Color(0xFF332C21),
    error = Color(0xFFF87171)
)

@Immutable
data class AppColors(
    val surfaceHigher: Color,
    val onSurfaceAlt: Color,
    val onSurfaceDisabled: Color,
    val overlay: Color,
    val onOverlay: Color,
    val link: Color,
    val linkBG: Color,
    val success: Color,
    val text: Color,
    val textBG: Color,
    val contact: Color,
    val contactBG: Color,
    val geo: Color,
    val geoBG: Color,
    val phone: Color,
    val phoneBG: Color,
    val wifi: Color,
    val wifiBG: Color
)

val LightColors = AppColors(
    surfaceHigher = Color(0xFFFFFFFF),
    onSurfaceAlt = Color(0xFF6E6151),
    onSurfaceDisabled = Color(0xFFA89C8C),
    overlay = Color(0x9915120D),
    onOverlay = Color(0xFFFFFFFF),
    link = Color(0xFFB45309),
    linkBG = Color(0x14B45309),
    success = Color(0xFF0E9F6E),
    text = Color(0xFF6938EF),
    textBG = Color(0x146938EF),
    contact = Color(0xFF0E9F6E),
    contactBG = Color(0x140E9F6E),
    geo = Color(0xFFE01E5A),
    geoBG = Color(0x14E01E5A),
    phone = Color(0xFFD95F02),
    phoneBG = Color(0x14D95F02),
    wifi = Color(0xFF0284C7),
    wifiBG = Color(0x140284C7)
)

val DarkColors = AppColors(
    surfaceHigher = Color(0xFF201B14),
    onSurfaceAlt = Color(0xFFACA08E),
    onSurfaceDisabled = Color(0xFF6B6052),
    overlay = Color(0xB315120D),
    onOverlay = Color(0xFFFFFFFF),
    link = Color(0xFFF59E0B),
    linkBG = Color(0x26F59E0B),
    success = Color(0xFF32D583),
    text = Color(0xFFA48AFB),
    textBG = Color(0x26A48AFB),
    contact = Color(0xFF47CD89),
    contactBG = Color(0x2647CD89),
    geo = Color(0xFFFD6F9E),
    geoBG = Color(0x26FD6F9E),
    phone = Color(0xFFFD853A),
    phoneBG = Color(0x26FD853A),
    wifi = Color(0xFF36BFFA),
    wifiBG = Color(0x2636BFFA)
)

val LocalAppColors = staticCompositionLocalOf { LightColors }

val ColorScheme.appColors: AppColors
    @Composable
    get() = LocalAppColors.current

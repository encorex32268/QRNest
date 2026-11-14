package com.lihan.qrnest.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.lihan.qrnest.core.designsystem.resources.Res
import com.lihan.qrnest.core.designsystem.resources.suse_medium
import com.lihan.qrnest.core.designsystem.resources.suse_regular
import com.lihan.qrnest.core.designsystem.resources.suse_semibold
import org.jetbrains.compose.resources.Font

@Composable
fun suseFontFamily(): FontFamily = FontFamily(
    Font(Res.font.suse_semibold, FontWeight.SemiBold),
    Font(Res.font.suse_medium, FontWeight.Medium),
    Font(Res.font.suse_regular, FontWeight.Normal)
)

@Composable
fun getTypography(): Typography {
    val suse = suseFontFamily()
    return Typography(
        titleMedium = TextStyle(
            fontFamily = suse,
            fontWeight = FontWeight.SemiBold,
            fontSize = 26.sp,
            lineHeight = 32.sp,
            letterSpacing = (-0.5).sp
        ),
        titleSmall = TextStyle(
            fontFamily = suse,
            fontWeight = FontWeight.SemiBold,
            fontSize = 17.sp,
            lineHeight = 22.sp,
            letterSpacing = (-0.2).sp
        ),
        labelLarge = TextStyle(
            fontFamily = suse,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.sp
        ),
        labelMedium = TextStyle(
            fontFamily = suse,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.1.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = suse,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            letterSpacing = 0.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = suse,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            letterSpacing = 0.sp
        ),
        bodySmall = TextStyle(
            fontFamily = suse,
            fontWeight = FontWeight.Normal,
            fontSize = 11.sp,
            lineHeight = 14.sp,
            letterSpacing = 0.1.sp
        )
    )
}

package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val SophisticatedDarkColorScheme = darkColorScheme(
    primary = SophisticatedPurple,
    onPrimary = DeepPurpleAccent,
    primaryContainer = DarkOutline,
    onPrimaryContainer = OffWhiteText,
    secondary = MutedPurpleText,
    onSecondary = SoftDarkBg,
    background = SoftDarkBg,
    onBackground = OffWhiteText,
    surface = DarkSurface,
    onSurface = OffWhiteText,
    outline = DarkOutline,
    surfaceVariant = DarkOutline,
    onSurfaceVariant = MutedPurpleText
)

private val WarmPaperLightColorScheme = lightColorScheme(
    primary = Color(0xFF1B365D),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF3EBD8),
    onPrimaryContainer = Color(0xFF2C1E14),
    secondary = Color(0xFFB45309),
    onSecondary = Color.White,
    background = Color(0xFFFBF7EE),
    onBackground = Color(0xFF2C1E14),
    surface = Color(0xFFFFFDF9),
    onSurface = Color(0xFF2C1E14),
    outline = Color(0xFFD7CCC8),
    surfaceVariant = Color(0xFFF3EBD8),
    onSurfaceVariant = Color(0xFF5D4037)
)

@Composable
fun MyApplicationTheme(
    isPaperTheme: Boolean = true,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        isPaperTheme -> WarmPaperLightColorScheme
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> SophisticatedDarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

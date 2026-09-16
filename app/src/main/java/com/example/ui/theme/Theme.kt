package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val NeonDarkColorScheme = darkColorScheme(
  primary = NeonGreen,
  onPrimary = Color(0xFF00381B),
  primaryContainer = Color(0xFF00532B),
  onPrimaryContainer = NeonGreen,

  secondary = NeonCyan,
  onSecondary = Color(0xFF00363D),
  secondaryContainer = Color(0xFF004F59),
  onSecondaryContainer = NeonCyan,

  tertiary = NeonPink,
  onTertiary = Color(0xFF49001F),
  tertiaryContainer = Color(0xFF6B0030),
  onTertiaryContainer = NeonPink,

  background = DarkBg,
  onBackground = TextPrimary,
  surface = DarkSurface,
  onSurface = TextPrimary,
  surfaceVariant = DarkSurfaceElevated,
  onSurfaceVariant = TextSecondary,
  outline = DarkSurfaceBorder,
  error = NeonPink,
  onError = Color.White
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force neon dark mode for fluorescent gym aesthetics
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = NeonDarkColorScheme,
    typography = Typography,
    content = content
  )
}


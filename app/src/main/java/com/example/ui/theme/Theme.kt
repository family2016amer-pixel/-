package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = ForestGreen,
  secondary = Gold,
  tertiary = LightGreen,
  background = DeepSlate,
  surface = DarkCardBg,
  onPrimary = Color.White,
  onSecondary = Color.Black,
  onBackground = Color.White,
  onSurface = Color.White,
  outline = DarkBorder
)

private val LightColorScheme = lightColorScheme(
  primary = ForestGreen,
  secondary = Gold,
  tertiary = LightGreen,
  background = MintWhite,
  surface = LightCardBg,
  onPrimary = Color.White,
  onSecondary = Color.Black,
  onBackground = DeepSlate,
  onSurface = DeepSlate,
  outline = LightBorder
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false, // Set to false to enforce our premium green/gold brand identity
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

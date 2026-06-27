package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Sports Neon Phosphor Theme Colors - NOW THE GOLDEN EDITION
val PhosphorNeon = Color(0xFFFACC15) // Gold color
val PhosphorDark = Color(0xFFCA8A04) // Darker Gold/Yellow
val NeonPink = Color(0xFFFF2A5F) // High-contrast Neon Pink (#ff2a5f)

// Aliases for compatibility
val ForestGreen = PhosphorNeon
val LightGreen = PhosphorDark

// Night Mode / Deep Cyber Charcoal
val DeepSlate = Color(0xFF050707) // Deep Black Background (#050707)
val DarkCardBg = Color(0xFF0D1211) // Zeyti/Deep Dark Grey-Green Card background (#0d1211)
val SubCardBg = Color(0xFF080B0B) // Evil background (#080b0b)
val DarkBorder = Color(0xFF1A2624) // Thin border accents

// Light Mode (retained for fallback readability)
val MintWhite = Color(0xFFF8FAFC) // Light mode background
val LightCardBg = Color(0xFFFFFFFF)
val LightBorder = Color(0xFFE2E8F0)

// Helper Status Colors
val StatusSuccess = Color(0xFF22C55E) // Success Green #22c55e
val StatusError = Color(0xFFEF4444) // Error Red #ef4444
val StatusWarning = Color(0xFFF59E0B) // Gold #f59e0b
val StatusInfo = Color(0xFF38BDF8)
val Gold = Color(0xFFF59E0B) // Gold color f59e0b

// Brand Colors requested by user
val GrassGreen = Color(0xFF2E7D32) // Grass Green #2e7d32
val DeepDarkGreen = Color(0xFF0A2E1A) // Dark background #0a2e1a
val WhatsAppGreen = Color(0xFF25D366) // WhatsApp color #25D366
val ErrorRed = Color(0xFFEF4444) // Error red #ef4444
val SuccessGreen = Color(0xFF22C55E) // Success green #22c55e

// Brand Colors and Gradients for Dark Atmosphere
val TealTurquoise = Color(0xFFF59E0B) // Replaced with Gold
val DarkGreen = Color(0xFF0A2E1A) // Deep Dark Green background

val AppTealGradient = androidx.compose.ui.graphics.Brush.verticalGradient(
  colors = listOf(
    DarkGreen,
    Color(0xFF161502),
    Color(0xFF050707)
  )
)

val AppTealButtonGradient = androidx.compose.ui.graphics.Brush.horizontalGradient(
  colors = listOf(
    Color(0xFF423B06), // Slightly lighter dark gold-brown
    TealTurquoise
  )
)



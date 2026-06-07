package com.startspeler.horeca.ui.theme.crew

import androidx.compose.ui.graphics.Color

// Crew - algemene lichte interface
val CrewBackground = Color(0xFFF4F6F8)
val CrewSurface = Color(0xFFFFFFFF)
val CrewSurfaceVariant = Color(0xFFF8FAFC)

val CrewTextPrimary = Color(0xFF1A1A1A)
val CrewTextSecondary = Color(0xFF4B5563)
val CrewTextMuted = Color(0xFF6B7280)

val CrewBorder = Color(0xFFD0D5DD)
val CrewDivider = Color(0xFFE5E7EB)

// Crew branding
val CrewPrimary = Color(0xFF251E35)        // donkerpaars
val CrewPrimaryDark = Color(0xFF2D2438)
val CrewOnPrimary = Color(0xFFFFFFFF)

// Gele accentkleur voor CTA / actieve navbaritem
val CrewAccent = Color(0xFFF2C94C)
val CrewOnAccent = Color(0xFF251E35)

// Bestelstatussen
val OrderPending = Color(0xFFEB5757)         // in behandeling
val OrderReady = Color(0xFFFF9F1C)           // klaar
val OrderDelivered = Color(0xFF27AE60)       // geleverd
val OrderCancelled = Color(0xFF2D9CDB)      // geannuleerd / issue

// Herkomst bestelling
val OrderFromCrew = Color(0xFF6B7280)
val OrderFromQr = Color(0x298B5CF6)

// Voorraadstatus
val StockAvailable = Color(0xFF27AE60)
val StockLow = Color(0xFFF2C94C)
val StockOut = Color(0xFFEB5757)

// Algemene feedback
val SuccessGreen = Color(0xFF27AE60)
val WarningOrange = Color(0xFFFF9F1C)
val ErrorRed = Color(0xFFEB5757)
val InfoBlue = Color(0xFF2D9CDB)

// Sidebar specifieke kleuren
val CrewSidebarBackground = CrewPrimary
val CrewSidebarBorder = CrewPrimaryDark
val CrewSidebarItemDefault = Color(0xFFF3F4F6)   // lichtgrijs / bijna wit
val CrewSidebarItemMuted = Color(0xFFD1D5DB)
val CrewSidebarItemActive = CrewAccent
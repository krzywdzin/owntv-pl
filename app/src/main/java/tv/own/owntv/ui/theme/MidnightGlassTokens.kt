package tv.own.owntv.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Product tokens from the Midnight Glass guide.
 *
 * Blue means focus/location; violet means active/progress. Flat app surfaces use opaque backgrounds.
 * Glass is reserved for overlays over imagery/video.
 */
object MidnightGlassColors {
    val Bg000 = Color(0xFF060711)
    val Bg100 = Color(0xFF0D101C)
    val Bg200 = Color(0xFF151827)
    val Bg300 = Color(0xFF1E2133)
    val Line = Color(0xFF2E3242)

    val Ink = Color(0xFFF2F3F9)
    val Ink2 = Color(0xFFBABDCB)
    val Ink3 = Color(0xFF8D91A3)
    val InkOff = Color(0xFF5A5D69)
    val OnInk = Bg000

    val Blue200 = Color(0xFFBBD3FF)
    val Blue300 = Color(0xFF94B9FF)
    val Blue500 = Color(0xFF5389FF)
    val Blue700 = Color(0xFF2A57C2)
    val Blue900 = Color(0xFF0D2768)

    val Violet200 = Color(0xFFD4C9FF)
    val Violet300 = Color(0xFFBEAAFF)
    val Violet500 = Color(0xFF996FFF)
    val Violet700 = Color(0xFF693EC1)
    val Violet900 = Color(0xFF331767)

    val Live = Color(0xFFF1424E)
    val Ok = Color(0xFF38C789)
    val Warn = Color(0xFFF3B94C)

    val GlassVeil = Color(0x8C0D101C)
    val GlassPane = Color(0xB8151827)
    val GlassSlab = Color(0xE0151827)
    val GlassHover = Color(0x14FFFFFF)
    val GlassEdge = Color(0x24FFFFFF)
    val GlassEdgeSoft = Color(0x0FFFFFFF)
    val ScrubTrack = Color(0x33FFFFFF)
    val ScrubBuffer = Color(0x59FFFFFF)
    val Scrim = Color(0xCC000000)
}

object MidnightGlassSpace {
    val X1 = 4.dp
    val X2 = 8.dp
    val X3 = 12.dp
    val X4 = 16.dp
    val X6 = 24.dp
    val X8 = 32.dp
    val X12 = 48.dp
}

object MidnightGlassRadii {
    val Small = 8.dp
    val Medium = 14.dp
    val Large = 22.dp
    val Full = 999.dp
}

object MidnightGlassTv {
    val SafeX = 48.dp
    val SafeY = 27.dp
    val MinimumText = 22.sp
    val ButtonHeight = 64.dp
}

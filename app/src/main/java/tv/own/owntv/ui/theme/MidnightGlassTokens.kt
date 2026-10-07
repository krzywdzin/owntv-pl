package tv.own.owntv.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Neutral product tokens from the Midnight Glass guide.
 *
 * Blue means focus/location, violet means active/progress. Flat app surfaces use opaque backgrounds;
 * glass is reserved for overlays over imagery/video.
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

    val Blue200 = Color(0xFFBBD3FF)
    val Blue300 = Color(0xFF94B9FF)
    val Blue500 = Color(0xFF5389FF)
    val Blue700 = Color(0xFF2A57C2)
    val Blue900 = Color(0xFF0D2768)

    val Violet300 = Color(0xFFBEAAFF)
    val Violet500 = Color(0xFF996FFF)
    val Violet700 = Color(0xFF693EC1)
    val Violet900 = Color(0xFF331767)

    val Live = Color(0xFFF1424E)
    val Ok = Color(0xFF38C789)
    val Warn = Color(0xFFF3B94C)
}

object MidnightGlassRadii {
    val Small = 8.dp
    val Medium = 14.dp
    val Large = 22.dp
}

object MidnightGlassTv {
    val SafeX = 48.dp
    val SafeY = 27.dp
    val MinimumText = 22.sp
}

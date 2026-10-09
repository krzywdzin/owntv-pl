package tv.own.owntv.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import tv.own.owntv.R
import tv.own.owntv.ui.theme.MidnightGlassColors

/**
 * Product-owned in-app mark used until the final launcher artwork is supplied.
 *
 * It intentionally contains no OwnTV vectors or wordmark. The launcher/banner/splash raster set is a
 * separate release asset and must be replaced with the final TV Leśnik artwork before first sale.
 */
@Composable
fun ProductBrandMark(size: Dp, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(size * 0.24f)
    Box(
        modifier = modifier
            .size(size)
            .background(MidnightGlassColors.Violet500, shape)
            .border(1.dp, MidnightGlassColors.Violet200.copy(alpha = 0.5f), shape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.brand_own),
            color = MidnightGlassColors.OnInk,
            fontSize = (size.value * 0.36f).sp,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}

@Composable
fun ProductBrandLockup(
    markSize: Dp,
    modifier: Modifier = Modifier,
    stacked: Boolean = false,
) {
    val wordmark = @Composable {
        Text(
            text = stringResource(R.string.app_name),
            color = MidnightGlassColors.Ink,
            fontSize = (markSize.value * 0.46f).sp,
            fontWeight = FontWeight.Bold,
        )
    }
    if (stacked) {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(markSize * 0.20f),
        ) {
            ProductBrandMark(markSize)
            wordmark()
        }
    } else {
        Row(
            modifier = modifier.height(markSize),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(markSize * 0.28f),
        ) {
            ProductBrandMark(markSize)
            Box(Modifier.width(markSize * 3.4f), contentAlignment = Alignment.CenterStart) { wordmark() }
        }
    }
}

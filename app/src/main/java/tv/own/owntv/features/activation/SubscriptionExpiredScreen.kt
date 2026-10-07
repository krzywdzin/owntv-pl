package tv.own.owntv.features.activation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import tv.own.owntv.R
import tv.own.owntv.ui.components.OwnTVButton
import tv.own.owntv.ui.components.OwnTVButtonStyle
import tv.own.owntv.ui.theme.MidnightGlassColors
import tv.own.owntv.ui.theme.MidnightGlassTv

@Composable
internal fun SubscriptionExpiredScreen(
    supportPhone: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightGlassColors.Bg000)
            .padding(horizontal = MidnightGlassTv.SafeX, vertical = MidnightGlassTv.SafeY),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            androidx.tv.material3.Text(
                text = stringResource(R.string.subscription_expired_title),
                color = MidnightGlassColors.Ink,
                fontSize = 48.sp,
                lineHeight = 56.sp,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(16.dp))
            androidx.tv.material3.Text(
                text = stringResource(R.string.subscription_expired_body),
                color = MidnightGlassColors.Ink2,
                fontSize = MidnightGlassTv.MinimumText,
                lineHeight = 30.sp,
            )
            if (!supportPhone.isNullOrBlank()) {
                Spacer(Modifier.height(12.dp))
                androidx.tv.material3.Text(
                    text = stringResource(R.string.subscription_support_phone, supportPhone),
                    color = MidnightGlassColors.Violet300,
                    fontSize = 24.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.height(32.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OwnTVButton(
                    text = stringResource(R.string.subscription_retry),
                    onClick = onRetry,
                    style = OwnTVButtonStyle.SECONDARY,
                )
                if (!supportPhone.isNullOrBlank()) {
                    OwnTVButton(
                        text = stringResource(R.string.subscription_call_provider),
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_DIAL,
                                Uri.fromParts(PHONE_SCHEME, supportPhone, null),
                            )
                            runCatching { context.startActivity(intent) }
                        },
                    )
                }
            }
        }
    }
}

private const val PHONE_SCHEME = "tel"

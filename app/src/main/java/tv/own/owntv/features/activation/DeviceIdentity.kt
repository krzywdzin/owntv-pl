package tv.own.owntv.features.activation

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Base64
import java.security.MessageDigest
import tv.own.owntv.BuildConfig

internal class DeviceIdentity(private val context: Context) {
    val id: String by lazy {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        val raw = androidId?.takeIf { it.isNotBlank() } ?: Build.FINGERPRINT
        val digest = MessageDigest.getInstance(HASH_ALGORITHM)
            .digest("${BuildConfig.APPLICATION_ID}:$raw".toByteArray(Charsets.UTF_8))
        Base64.encodeToString(digest, Base64.NO_WRAP or Base64.URL_SAFE)
    }

    private companion object {
        const val HASH_ALGORITHM = "SHA-256"
    }
}

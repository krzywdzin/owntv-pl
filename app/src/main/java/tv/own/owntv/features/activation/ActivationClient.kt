package tv.own.owntv.features.activation

import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

internal data class ActivationPayload(
    val server: String,
    val port: Int?,
    val https: Boolean,
    val username: String,
    val password: String,
    val expires: String?,
    val supportPhone: String?,
) {
    fun xtreamBaseUrl(): String {
        val host = server.trim().trimEnd('/')
        if (host.startsWith(HTTP_PREFIX) || host.startsWith(HTTPS_PREFIX)) return host
        val scheme = if (https) HTTPS_SCHEME else HTTP_SCHEME
        return if (port != null && port > 0) "$scheme://$host:$port" else "$scheme://$host"
    }

    private companion object {
        const val HTTP_PREFIX = "http://"
        const val HTTPS_PREFIX = "https://"
        const val HTTP_SCHEME = "http"
        const val HTTPS_SCHEME = "https"
    }
}

internal sealed interface ActivationLookup {
    data class Success(val payload: ActivationPayload) : ActivationLookup
    data object InvalidCode : ActivationLookup
    data object Expired : ActivationLookup
    data object DeviceBound : ActivationLookup
    data object RateLimited : ActivationLookup
    data object NetworkError : ActivationLookup
    data object ServerError : ActivationLookup
    data object ConfigurationMissing : ActivationLookup
}

internal class ActivationClient(private val http: OkHttpClient) {
    suspend fun activate(baseUrl: String, code: String, deviceId: String): ActivationLookup =
        withContext(Dispatchers.IO) {
            if (baseUrl.isBlank()) return@withContext ActivationLookup.ConfigurationMissing

            val encoded = URLEncoder.encode(code, StandardCharsets.UTF_8.name())
            val endpoint = baseUrl.trimEnd('/') + ACTIVATE_PATH + encoded
            val request = Request.Builder()
                .url(endpoint)
                .header(DEVICE_HEADER, deviceId)
                .get()
                .build()

            try {
                http.newCall(request).execute().use { response ->
                    when (response.code) {
                        200 -> parseSuccess(response.body?.string().orEmpty())
                        404 -> ActivationLookup.InvalidCode
                        409 -> ActivationLookup.DeviceBound
                        410 -> ActivationLookup.Expired
                        429 -> ActivationLookup.RateLimited
                        else -> ActivationLookup.ServerError
                    }
                }
            } catch (_: IOException) {
                ActivationLookup.NetworkError
            } catch (_: IllegalArgumentException) {
                ActivationLookup.ConfigurationMissing
            }
        }

    private fun parseSuccess(raw: String): ActivationLookup = runCatching {
        val json = JSONObject(raw)
        if (!json.optBoolean(KEY_OK, false)) return ActivationLookup.ServerError
        ActivationLookup.Success(
            ActivationPayload(
                server = json.getString(KEY_SERVER),
                port = json.optInt(KEY_PORT, -1).takeIf { it > 0 },
                https = json.optBoolean(KEY_HTTPS, false),
                username = json.getString(KEY_USERNAME),
                password = json.getString(KEY_PASSWORD),
                expires = json.optString(KEY_EXPIRES).takeIf { it.isNotBlank() },
                supportPhone = json.optString(KEY_SUPPORT_PHONE).takeIf { it.isNotBlank() },
            ),
        )
    }.getOrElse { ActivationLookup.ServerError }

    private companion object {
        const val ACTIVATE_PATH = "/activate/"
        const val DEVICE_HEADER = "X-Device-ID"
        const val KEY_OK = "ok"
        const val KEY_SERVER = "server"
        const val KEY_PORT = "port"
        const val KEY_HTTPS = "https"
        const val KEY_USERNAME = "username"
        const val KEY_PASSWORD = "password"
        const val KEY_EXPIRES = "expires"
        const val KEY_SUPPORT_PHONE = "support_phone"
    }
}

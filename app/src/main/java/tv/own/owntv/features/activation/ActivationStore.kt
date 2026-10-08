package tv.own.owntv.features.activation

import android.content.Context

internal data class StoredActivation(
    val code: String,
    val managedSourceId: Long,
    val expires: String?,
    val supportPhone: String?,
)

class ActivationStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun save(code: String, managedSourceId: Long, expires: String?, supportPhone: String?) {
        preferences.edit()
            .putString(KEY_CODE, code)
            .putLong(KEY_MANAGED_SOURCE_ID, managedSourceId)
            .putString(KEY_EXPIRES, expires)
            .putString(KEY_SUPPORT_PHONE, supportPhone)
            .apply()
    }

    internal fun read(): StoredActivation? {
        val code = preferences.getString(KEY_CODE, null)?.takeIf { it.isNotBlank() } ?: return null
        val sourceId = preferences.getLong(KEY_MANAGED_SOURCE_ID, -1L).takeIf { it > 0L } ?: return null
        return StoredActivation(
            code = code,
            managedSourceId = sourceId,
            expires = preferences.getString(KEY_EXPIRES, null),
            supportPhone = preferences.getString(KEY_SUPPORT_PHONE, null),
        )
    }

    fun isManagedSource(sourceId: Long): Boolean =
        preferences.getLong(KEY_MANAGED_SOURCE_ID, -1L) == sourceId

    fun updateMetadata(expires: String?, supportPhone: String?) {
        val current = read() ?: return
        save(
            code = current.code,
            managedSourceId = current.managedSourceId,
            expires = expires,
            supportPhone = supportPhone ?: current.supportPhone,
        )
    }

    fun clear() {
        preferences.edit().clear().apply()
    }

    private companion object {
        const val PREFS_NAME = "product_activation"
        const val KEY_CODE = "code"
        const val KEY_MANAGED_SOURCE_ID = "managed_source_id"
        const val KEY_EXPIRES = "expires"
        const val KEY_SUPPORT_PHONE = "support_phone"
    }
}

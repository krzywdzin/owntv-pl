package tv.own.owntv.features.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import tv.own.owntv.core.i18n.LocaleStore
import tv.own.owntv.core.i18n.SupportedLocale
import tv.own.owntv.core.i18n.SupportedLocales
import java.util.Locale

/**
 * Thin ViewModel for the in-app language picker. [LocaleStore] owns persistence and process-level
 * locale application; this only exposes the current tag, English-name-sorted picker rows, and a write entry point.
 */
class LanguageSettingsViewModel(
    private val localeStore: LocaleStore,
) : ViewModel() {

    val currentTag: StateFlow<String> = localeStore.currentTag

    /** Packaged + picker-visible catalogue rows, A–Z by English name. System default is not in this list. */
    val pickerRows: List<SupportedLocale> =
        SupportedLocales.pickerRows
            .filter { it.languageTag == PRODUCT_ENGLISH_TAG || it.languageTag == PRODUCT_POLISH_TAG }
            .sortedBy { it.englishName.lowercase(Locale.ROOT) }

    fun setLocale(tag: String) {
        if (tag != PRODUCT_ENGLISH_TAG && tag != PRODUCT_POLISH_TAG) return
        viewModelScope.launch { localeStore.set(tag) }
    }

    private companion object {
        const val PRODUCT_ENGLISH_TAG = "en-US"
        const val PRODUCT_POLISH_TAG = "pl"
    }
}

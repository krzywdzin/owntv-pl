package tv.own.owntv.features.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Session-only service mode.
 *
 * It deliberately does not persist across process restarts: a customer boot always returns to the
 * restricted product UI. Service staff unlock it again with the hidden rail gesture when needed.
 */
internal class ServiceModeStore {
    private val _enabled = MutableStateFlow(false)
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    fun activate() {
        _enabled.value = true
    }

    fun deactivate() {
        _enabled.value = false
    }
}

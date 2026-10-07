package tv.own.owntv.features.activation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.time.Instant
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tv.own.owntv.BuildConfig
import tv.own.owntv.core.database.dao.SourceDao
import tv.own.owntv.core.model.SourceType
import tv.own.owntv.core.parser.XtreamClient

internal sealed interface SubscriptionUiState {
    data object Unmanaged : SubscriptionUiState
    data object Active : SubscriptionUiState
    data class Expired(val supportPhone: String?) : SubscriptionUiState
}

internal class SubscriptionViewModel(
    private val store: ActivationStore,
    private val client: ActivationClient,
    private val deviceIdentity: DeviceIdentity,
    private val sourceDao: SourceDao,
    private val xtreamClient: XtreamClient,
) : ViewModel() {
    private val refreshMutex = Mutex()
    private var manualRefresh: Job? = null

    private val _state = kotlinx.coroutines.flow.MutableStateFlow(initialState())
    val state: kotlinx.coroutines.flow.StateFlow<SubscriptionUiState> =
        _state.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                refreshMutex.withLock { refresh() }
                delay(REFRESH_INTERVAL_MS)
            }
        }
    }

    fun refreshNow() {
        manualRefresh?.cancel()
        manualRefresh = viewModelScope.launch {
            refreshMutex.withLock { refresh() }
        }
    }

    private fun initialState(): SubscriptionUiState {
        val activation = store.read() ?: return SubscriptionUiState.Unmanaged
        return if (isExpired(activation.expires)) {
            SubscriptionUiState.Expired(activation.supportPhone)
        } else {
            SubscriptionUiState.Active
        }
    }

    private suspend fun refresh() {
        val activation = store.read()
        if (activation == null) {
            _state.value = SubscriptionUiState.Unmanaged
            return
        }

        // The provider remains authoritative for Xtream account expiry. If it is reachable and says
        // the account has expired, lock immediately even if the activation service has stale data.
        val providerExpired = sourceDao.getById(activation.managedSourceId)
            ?.takeIf { it.type == SourceType.XTREAM }
            ?.let { source -> runCatching { xtreamClient.accountExpiryMs(source) }.getOrNull() }
            ?.let { it <= System.currentTimeMillis() }
            ?: false

        when (val lookup = client.activate(BuildConfig.ACTIVATION_BASE_URL, activation.code, deviceIdentity.id)) {
            is ActivationLookup.Success -> {
                store.updateMetadata(lookup.payload.expires, lookup.payload.supportPhone)
                _state.value = if (providerExpired || isExpired(lookup.payload.expires)) {
                    SubscriptionUiState.Expired(lookup.payload.supportPhone ?: activation.supportPhone)
                } else {
                    SubscriptionUiState.Active
                }
            }
            ActivationLookup.Expired,
            ActivationLookup.InvalidCode,
            ActivationLookup.DeviceBound -> {
                _state.value = SubscriptionUiState.Expired(activation.supportPhone)
            }
            ActivationLookup.RateLimited,
            ActivationLookup.NetworkError,
            ActivationLookup.ServerError,
            ActivationLookup.ConfigurationMissing -> {
                // A temporary backend/network failure must not brick a valid subscription. A locally
                // known expiry or a provider-confirmed expiry still blocks without network.
                _state.value = if (providerExpired || isExpired(activation.expires)) {
                    SubscriptionUiState.Expired(activation.supportPhone)
                } else {
                    SubscriptionUiState.Active
                }
            }
        }
    }

    private fun isExpired(value: String?): Boolean {
        val raw = value?.trim().orEmpty()
        if (raw.isEmpty()) return false
        val numeric = raw.toLongOrNull()
        val epochMs = when {
            numeric == null -> runCatching { Instant.parse(raw).toEpochMilli() }.getOrNull()
            numeric > EPOCH_MILLIS_THRESHOLD -> numeric
            else -> numeric * 1_000L
        } ?: return false
        return epochMs <= System.currentTimeMillis()
    }

    private companion object {
        const val REFRESH_INTERVAL_MS = 60_000L
        const val EPOCH_MILLIS_THRESHOLD = 10_000_000_000L
    }
}

package tv.own.owntv.features.activation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tv.own.owntv.BuildConfig
import tv.own.owntv.R
import tv.own.owntv.core.database.dao.ProfileDao
import tv.own.owntv.core.database.entity.SourceEntity
import tv.own.owntv.core.repository.SourceRepository
import tv.own.owntv.core.setup.SourceImporter

internal enum class ActivationStage { VERIFYING, CONFIGURING }

internal enum class ActivationProblem {
    FORMAT,
    INVALID_CODE,
    EXPIRED,
    DEVICE_BOUND,
    RATE_LIMITED,
    NETWORK,
    SERVER,
    CONFIGURATION,
    SOURCE_SETUP,
}

internal sealed interface ActivationUiState {
    data object Idle : ActivationUiState
    data class Working(val stage: ActivationStage) : ActivationUiState
    data class Problem(val problem: ActivationProblem) : ActivationUiState
    data class Activated(val profileId: Long, val expires: String?) : ActivationUiState
}

internal class ActivationViewModel(
    private val context: Context,
    private val client: ActivationClient,
    private val deviceIdentity: DeviceIdentity,
    private val store: ActivationStore,
    private val importer: SourceImporter,
    private val profileDao: ProfileDao,
    private val sourceRepository: SourceRepository,
) : ViewModel() {
    private val _state = MutableStateFlow<ActivationUiState>(ActivationUiState.Idle)
    val state: StateFlow<ActivationUiState> = _state.asStateFlow()

    fun activate(rawCode: String) {
        val code = normalize(rawCode)
        if (code.length != CODE_LENGTH) {
            _state.value = ActivationUiState.Problem(ActivationProblem.FORMAT)
            return
        }
        if (_state.value is ActivationUiState.Working) return

        viewModelScope.launch {
            _state.value = ActivationUiState.Working(ActivationStage.VERIFYING)
            when (val lookup = client.activate(BuildConfig.ACTIVATION_BASE_URL, code, deviceIdentity.id)) {
                is ActivationLookup.Success -> provision(code, lookup.payload)
                ActivationLookup.InvalidCode -> problem(ActivationProblem.INVALID_CODE)
                ActivationLookup.Expired -> problem(ActivationProblem.EXPIRED)
                ActivationLookup.DeviceBound -> problem(ActivationProblem.DEVICE_BOUND)
                ActivationLookup.RateLimited -> problem(ActivationProblem.RATE_LIMITED)
                ActivationLookup.NetworkError -> problem(ActivationProblem.NETWORK)
                ActivationLookup.ServerError -> problem(ActivationProblem.SERVER)
                ActivationLookup.ConfigurationMissing -> problem(ActivationProblem.CONFIGURATION)
            }
        }
    }

    fun clearProblem() {
        if (_state.value is ActivationUiState.Problem) _state.value = ActivationUiState.Idle
    }

    private suspend fun provision(code: String, payload: ActivationPayload) {
        importer.reset()
        var createdProfileId: Long? = null
        var importedSource: SourceEntity? = null
        try {
            _state.value = ActivationUiState.Working(ActivationStage.CONFIGURING)
            createdProfileId = importer.createProfile(
                name = context.getString(R.string.activation_default_profile),
                avatarId = 0,
                isKids = false,
                pin = null,
            )
            importer.xtream(
                name = context.getString(R.string.activation_default_source),
                server = payload.xtreamBaseUrl(),
                username = payload.username,
                password = payload.password,
                makeDefault = true,
            )
            val imported = importer.state.value as? SourceImporter.ImportState.Success
            importedSource = imported?.source
            if (importedSource == null) {
                cleanup(createdProfileId, null)
                problem(ActivationProblem.SOURCE_SETUP)
                return
            }

            val activeProfileId = importer.finish()
            if (activeProfileId == null) {
                cleanup(createdProfileId, importedSource)
                problem(ActivationProblem.SOURCE_SETUP)
                return
            }

            store.save(
                code = code,
                managedSourceId = importedSource.id,
                expires = payload.expires,
                supportPhone = payload.supportPhone,
            )
            _state.value = ActivationUiState.Activated(activeProfileId, payload.expires)
        } catch (_: Exception) {
            store.clear()
            cleanup(createdProfileId, importedSource)
            problem(ActivationProblem.SOURCE_SETUP)
        }
    }

    private suspend fun cleanup(profileId: Long?, source: SourceEntity?) {
        importer.reset()
        if (source != null) runCatching { sourceRepository.deleteSource(source) }
        if (profileId != null) {
            profileDao.getById(profileId)?.let { profile ->
                runCatching { profileDao.delete(profile) }
            }
        }
    }

    private fun problem(problem: ActivationProblem) {
        _state.value = ActivationUiState.Problem(problem)
    }

    companion object {
        const val CODE_LENGTH = 8

        fun normalize(raw: String): String =
            raw.filter(Char::isLetterOrDigit)
                .uppercase(Locale.ROOT)
                .take(CODE_LENGTH)
    }
}

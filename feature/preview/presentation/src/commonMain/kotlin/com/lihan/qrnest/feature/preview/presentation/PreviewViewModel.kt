package com.lihan.qrnest.feature.preview.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.lihan.qrnest.core.domain.model.QRCodeType
import com.lihan.qrnest.core.domain.repository.HistoryRepository
import com.lihan.qrnest.core.domain.util.onFailure
import com.lihan.qrnest.core.domain.util.onSuccess
import com.lihan.qrnest.core.presentation.mapper.toDomain
import com.lihan.qrnest.core.presentation.mapper.toUi
import com.lihan.qrnest.core.presentation.util.toUiText
import com.lihan.qrnest.feature.preview.domain.ClipboardManager
import com.lihan.qrnest.feature.preview.domain.QrImageSaver
import com.lihan.qrnest.feature.preview.presentation.navigation.PreviewRoute
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PreviewViewModel(
    private val historyRepository: HistoryRepository,
    private val clipboardManager: ClipboardManager,
    private val qrImageSaver: QrImageSaver,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val historyId: Long? = savedStateHandle.get<Long>(KEY_ID)
        ?: runCatching { savedStateHandle.toRoute<PreviewRoute>().id }.getOrNull()

    private val isScanResult: Boolean = savedStateHandle.get<Boolean>(KEY_IS_SCAN_RESULT)
        ?: runCatching { savedStateHandle.toRoute<PreviewRoute>().isScanResult }.getOrDefault(false)

    private var originTitle: String = ""
    private var hasLoadedInitialData = false

    private val _events = Channel<PreviewEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val _state = MutableStateFlow(
        PreviewState(
            isScanResult = isScanResult,
            titleInput = savedStateHandle.get<String>(KEY_TITLE_INPUT).orEmpty()
        )
    )
    val state: StateFlow<PreviewState> = _state
        .onStart {
            if (!hasLoadedInitialData) {
                hasLoadedInitialData = true
                loadHistory()
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = _state.value
        )

    fun onAction(action: PreviewAction) {
        when (action) {
            PreviewAction.BackClick -> onBackClick()
            PreviewAction.FavoriteClick -> onFavoriteClick()
            PreviewAction.CopyClick -> onCopyClick()
            PreviewAction.ShareClick -> onShareClick()
            PreviewAction.SaveClick -> onSaveClick()
            PreviewAction.ContentPrimaryActionClick -> onContentPrimaryActionClick()
            is PreviewAction.OnTitleChanged -> onTitleChanged(action.value)
        }
    }

    private fun loadHistory() {
        val id = historyId
        if (id == null) {
            markNotFound()
            return
        }

        viewModelScope.launch {
            val history = historyRepository.getHistoryById(id)
            if (history == null) {
                markNotFound()
                return@launch
            }

            originTitle = history.title.orEmpty()
            val restoredTitle = savedStateHandle.get<String>(KEY_TITLE_INPUT) ?: originTitle
            savedStateHandle[KEY_TITLE_INPUT] = restoredTitle

            _state.update {
                it.copy(
                    qrCodeHistoryUi = history.toUi(),
                    titleInput = restoredTitle,
                    isNotFound = false
                )
            }
        }
    }

    private fun markNotFound() {
        _state.update { it.copy(isNotFound = true) }
        viewModelScope.launch {
            _events.send(PreviewEvent.DataError)
        }
    }

    private fun onTitleChanged(rawTitle: String) {
        val clamped = rawTitle.take(MAX_TITLE_LENGTH)
        savedStateHandle[KEY_TITLE_INPUT] = clamped
        _state.update { it.copy(titleInput = clamped) }
    }

    private fun onFavoriteClick() {
        val currentItem = _state.value.qrCodeHistoryUi ?: run {
            viewModelScope.launch { _events.send(PreviewEvent.DataError) }
            return
        }
        val itemId = currentItem.id ?: return
        val updatedFavorite = !currentItem.isFavorite

        viewModelScope.launch {
            historyRepository.updateFavoriteStatus(id = itemId, isFavorite = updatedFavorite)
            _state.update { currentState ->
                currentState.copy(
                    qrCodeHistoryUi = currentState.qrCodeHistoryUi?.copy(
                        isFavorite = updatedFavorite
                    )
                )
            }
        }
    }

    private fun onCopyClick() {
        val currentItem = _state.value.qrCodeHistoryUi ?: run {
            viewModelScope.launch { _events.send(PreviewEvent.DataError) }
            return
        }

        val textToCopy = if (currentItem.type == QRCodeType.WiFi.typeCode) {
            extractWifiPassword(currentItem.content)
        } else {
            currentItem.content
        }
        clipboardManager.copyText(textToCopy)
    }

    private fun onShareClick() {
        val currentItem = _state.value.qrCodeHistoryUi ?: return
        viewModelScope.launch {
            _events.send(PreviewEvent.ShareQRCode(content = currentItem.content))
        }
    }

    private fun onSaveClick() {
        val currentState = _state.value
        val currentItem = currentState.qrCodeHistoryUi ?: return
        if (currentState.isSaving || currentItem.content.isBlank()) return

        _state.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            try {
                qrImageSaver.saveQrCode(currentItem.content)
                    .onSuccess {
                        _events.send(PreviewEvent.SaveSucceed)
                    }
                    .onFailure { error ->
                        _events.send(PreviewEvent.ShowError(error.toUiText()))
                    }
            } finally {
                _state.update { it.copy(isSaving = false) }
            }
        }
    }

    private fun onContentPrimaryActionClick() {
        val currentItem = _state.value.qrCodeHistoryUi ?: return
        val qrCodeType = QRCodeType.fromTypeCode(currentItem.type)
        val content = currentItem.content

        viewModelScope.launch {
            when (qrCodeType) {
                QRCodeType.Link -> _events.send(PreviewEvent.OpenUrl(content))
                QRCodeType.PhoneNumber -> {
                    val phoneNumber = extractPhoneNumber(content)
                    _events.send(PreviewEvent.DialPhone(phoneNumber))
                }
                QRCodeType.Geolocation -> _events.send(PreviewEvent.OpenGeo(content))
                QRCodeType.WiFi -> {
                    clipboardManager.copyText(extractWifiPassword(content))
                    _events.send(PreviewEvent.OpenWifiSettings)
                }
                QRCodeType.Contact -> {
                    val lines = content.split('\n')
                    val name = lines.getOrNull(0).orEmpty()
                    val email = lines.getOrNull(1).orEmpty()
                    val phone = lines.getOrNull(2).orEmpty()
                    _events.send(
                        PreviewEvent.InsertContact(
                            name = name,
                            email = email,
                            phone = phone
                        )
                    )
                }
                QRCodeType.Text -> Unit
            }
        }
    }

    private fun onBackClick() {
        viewModelScope.launch {
            val currentState = _state.value
            val currentItem = currentState.qrCodeHistoryUi
            if (currentItem == null) {
                _events.send(PreviewEvent.DataError)
                return@launch
            }

            val editedTitle = currentState.titleInput
            if (editedTitle != originTitle) {
                val persistedTitle = editedTitle.ifBlank { null }
                historyRepository.upsertHistory(
                    currentItem.copy(title = persistedTitle).toDomain()
                )
            }
            _events.send(PreviewEvent.Back)
        }
    }

    private fun extractWifiPassword(content: String): String {
        val passwordLine = content.lines()
            .firstOrNull { it.trimStart().startsWith("Password:", ignoreCase = true) }
        val extracted = passwordLine?.substringAfter(":", "")?.trim()
        return extracted?.takeIf { it.isNotEmpty() } ?: content
    }

    private fun extractPhoneNumber(content: String): String {
        val trimmed = content.trim()
        return if (trimmed.startsWith("tel:", ignoreCase = true)) {
            trimmed.substring(4).trim()
        } else {
            trimmed
        }
    }

    companion object {
        const val KEY_ID = "id"
        const val KEY_IS_SCAN_RESULT = "isScanResult"
        const val KEY_TITLE_INPUT = "titleInput"
        const val MAX_TITLE_LENGTH = 32
    }
}

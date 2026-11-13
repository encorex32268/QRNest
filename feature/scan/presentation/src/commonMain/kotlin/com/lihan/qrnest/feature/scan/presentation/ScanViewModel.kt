package com.lihan.qrnest.feature.scan.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lihan.qrnest.core.domain.model.QRCodeHistory
import com.lihan.qrnest.core.domain.model.ScannedBarcode
import com.lihan.qrnest.core.domain.repository.HistoryRepository
import com.lihan.qrnest.feature.scan.domain.QrImageScanner
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock

class ScanViewModel(
    private val historyRepository: HistoryRepository,
    private val qrImageScanner: QrImageScanner,
    private val clock: Clock = Clock.System
) : ViewModel() {

    private val _events = Channel<ScanEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val _state = MutableStateFlow(ScanState())
    val state: StateFlow<ScanState> = _state.asStateFlow()

    fun onAction(action: ScanAction) {
        when (action) {
            ScanAction.CloseAppClick -> Unit
            ScanAction.ShowCameraPermissionDialog -> {
                _state.update { it.copy(isShowCameraPermissionDialog = true) }
            }
            ScanAction.DismissCameraPermissionDialog -> {
                _state.update { it.copy(isShowCameraPermissionDialog = false) }
            }
            ScanAction.FlashClick -> {
                _state.update { it.copy(isOpeningFlashlight = !it.isOpeningFlashlight) }
            }
            ScanAction.DismissNoQRCodeFoundDialog -> {
                _state.update { it.copy(isShowNoQRCodesFound = false) }
            }
            is ScanAction.OnBarcodeScanned -> handleScannedBarcode(action.barcode)
            ScanAction.OnCameraScanFailed -> {
                _state.update { it.copy(isLoading = false) }
            }
            is ScanAction.OnImagePicked -> scanPickedImage(action.imageBytes)
        }
    }

    private fun handleScannedBarcode(barcode: ScannedBarcode) {
        if (_state.value.isLoading || barcode.content.isBlank()) return

        _state.update {
            it.copy(
                isLoading = true,
                isOpeningFlashlight = false
            )
        }

        viewModelScope.launch {
            try {
                val id = saveScannedBarcode(barcode)
                _events.send(ScanEvent.NavigateToPreview(id = id))
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun scanPickedImage(imageBytes: ByteArray?) {
        if (imageBytes == null || imageBytes.isEmpty()) {
            _state.update { it.copy(isShowNoQRCodesFound = true) }
            return
        }
        if (_state.value.isLoading) return

        _state.update {
            it.copy(
                isLoading = true,
                isShowNoQRCodesFound = false
            )
        }

        viewModelScope.launch {
            try {
                val scanned = qrImageScanner.scanImage(imageBytes)
                if (scanned == null) {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            isShowNoQRCodesFound = true
                        )
                    }
                } else {
                    val id = saveScannedBarcode(scanned)
                    _state.update { it.copy(isLoading = false) }
                    _events.send(ScanEvent.NavigateToPreview(id = id))
                }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private suspend fun saveScannedBarcode(barcode: ScannedBarcode): Long {
        return historyRepository.upsertHistory(
            QRCodeHistory(
                type = barcode.type,
                content = barcode.content,
                createdAt = clock.now().toEpochMilliseconds(),
                isGenerated = false,
                isFavorite = false
            )
        )
    }
}

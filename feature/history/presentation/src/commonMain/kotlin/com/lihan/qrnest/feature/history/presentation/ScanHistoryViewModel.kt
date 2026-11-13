package com.lihan.qrnest.feature.history.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lihan.qrnest.core.domain.repository.HistoryRepository
import com.lihan.qrnest.core.presentation.mapper.toUi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ScanHistoryViewModel(
    private val historyRepository: HistoryRepository
) : ViewModel() {

    private var hasLoadedInitialData = false

    private val _events = Channel<ScanHistoryEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val _state = MutableStateFlow(ScanHistoryState())
    val state: StateFlow<ScanHistoryState> = _state
        .onStart {
            if (!hasLoadedInitialData) {
                observeHistories()
                hasLoadedInitialData = true
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ScanHistoryState()
        )

    fun onAction(action: ScanHistoryAction) {
        when (action) {
            is ScanHistoryAction.ItemClick -> itemClick(action.id)
            is ScanHistoryAction.ItemLongClick -> itemLongClick(action.id)
            is ScanHistoryAction.ItemFavoriteClick -> itemFavoriteClick(action.id, action.isFavorite)
            ScanHistoryAction.ShareClick -> shareQRCode()
            ScanHistoryAction.DeleteClick -> deleteItem()
            ScanHistoryAction.DismissEditorBottomSheet -> dismissBottomSheet()
        }
    }

    private fun dismissBottomSheet() {
        _state.update {
            it.copy(
                isShowEditorBottomSheet = false,
                selectedId = null
            )
        }
    }

    private fun itemFavoriteClick(id: Long?, isFavorite: Boolean) {
        if (id == null) return
        viewModelScope.launch {
            historyRepository.updateFavoriteStatus(id, !isFavorite)
        }
    }

    private fun shareQRCode() {
        val currentState = _state.value
        val selectedId = currentState.selectedId ?: return
        val item = (currentState.scannedItems + currentState.generatedItems)
            .find { it.id == selectedId }

        dismissBottomSheet()

        if (item == null) return

        viewModelScope.launch {
            _events.send(ScanHistoryEvent.ShareQRCode(content = item.content))
        }
    }

    private fun deleteItem() {
        val selectedId = _state.value.selectedId ?: return
        viewModelScope.launch {
            historyRepository.deleteById(selectedId)
            dismissBottomSheet()
        }
    }

    private fun itemLongClick(id: Long?) {
        if (id == null) return
        _state.update {
            it.copy(
                isShowEditorBottomSheet = true,
                selectedId = id
            )
        }
    }

    private fun itemClick(id: Long?) {
        if (id == null) return
        viewModelScope.launch {
            _events.send(ScanHistoryEvent.NavigateToPreview(id = id))
        }
    }

    private fun observeHistories() {
        historyRepository
            .getScannedHistories()
            .onEach { scannedHistories ->
                _state.update { current ->
                    current.copy(
                        scannedItems = scannedHistories.map { it.toUi() }
                    )
                }
            }
            .launchIn(viewModelScope)

        historyRepository
            .getGeneratedHistories()
            .onEach { generatedHistories ->
                _state.update { current ->
                    current.copy(
                        generatedItems = generatedHistories.map { it.toUi() }
                    )
                }
            }
            .launchIn(viewModelScope)
    }
}

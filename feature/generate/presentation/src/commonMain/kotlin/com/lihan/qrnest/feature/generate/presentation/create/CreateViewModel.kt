package com.lihan.qrnest.feature.generate.presentation.create

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.lihan.qrnest.core.domain.model.QRCodeHistory
import com.lihan.qrnest.core.domain.model.QRCodeType
import com.lihan.qrnest.core.domain.repository.HistoryRepository
import com.lihan.qrnest.feature.generate.domain.QrPayloadFormatter
import com.lihan.qrnest.feature.generate.presentation.navigation.CreateRoute
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock

class CreateViewModel(
    private val historyRepository: HistoryRepository,
    private val savedStateHandle: SavedStateHandle,
    private val clock: Clock = Clock.System
) : ViewModel() {

    private val qrCodeType: QRCodeType = run {
        val typeCode = savedStateHandle.get<Int>(KEY_TYPE)
            ?: runCatching { savedStateHandle.toRoute<CreateRoute>().type }.getOrNull()
        QRCodeType.fromTypeCode(typeCode)
    }

    private val _events = Channel<CreateEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val initialFirst = savedStateHandle.get<String>(KEY_FIRST_INPUT).orEmpty()
    private val initialSecond = savedStateHandle.get<String>(KEY_SECOND_INPUT).orEmpty()
    private val initialThird = savedStateHandle.get<String>(KEY_THIRD_INPUT).orEmpty()

    private val _state = MutableStateFlow(
        CreateState(
            qrCodeType = qrCodeType,
            firstInput = initialFirst,
            secondInput = initialSecond,
            thirdInput = initialThird,
            generateButtonEnabled = QrPayloadFormatter.isInputValid(
                qrCodeType = qrCodeType,
                first = initialFirst,
                second = initialSecond,
                third = initialThird
            )
        )
    )
    val state: StateFlow<CreateState> = _state.asStateFlow()

    fun onAction(action: CreateAction) {
        when (action) {
            CreateAction.BackClick -> Unit
            is CreateAction.OnFirstInputChanged -> updateInputs(first = action.value)
            is CreateAction.OnSecondInputChanged -> updateInputs(second = action.value)
            is CreateAction.OnThirdInputChanged -> updateInputs(third = action.value)
            CreateAction.GenerateButtonClick -> generateQRCode()
        }
    }

    private fun updateInputs(
        first: String = _state.value.firstInput,
        second: String = _state.value.secondInput,
        third: String = _state.value.thirdInput
    ) {
        savedStateHandle[KEY_FIRST_INPUT] = first
        savedStateHandle[KEY_SECOND_INPUT] = second
        savedStateHandle[KEY_THIRD_INPUT] = third

        _state.update {
            it.copy(
                firstInput = first,
                secondInput = second,
                thirdInput = third,
                generateButtonEnabled = QrPayloadFormatter.isInputValid(
                    qrCodeType = qrCodeType,
                    first = first,
                    second = second,
                    third = third
                )
            )
        }
    }

    private fun generateQRCode() {
        val current = _state.value
        if (current.isGenerating) return
        if (!QrPayloadFormatter.isInputValid(
                qrCodeType = current.qrCodeType,
                first = current.firstInput,
                second = current.secondInput,
                third = current.thirdInput
            )
        ) {
            return
        }

        _state.update { it.copy(isGenerating = true) }

        val content = QrPayloadFormatter.format(
            qrCodeType = current.qrCodeType,
            first = current.firstInput,
            second = current.secondInput,
            third = current.thirdInput
        )

        viewModelScope.launch {
            try {
                val upsertId = historyRepository.upsertHistory(
                    QRCodeHistory(
                        type = current.qrCodeType.typeCode,
                        content = content,
                        createdAt = clock.now().toEpochMilliseconds(),
                        isGenerated = true,
                        isFavorite = false
                    )
                )
                _events.send(CreateEvent.NavigateToPreview(id = upsertId))
            } finally {
                _state.update { it.copy(isGenerating = false) }
            }
        }
    }

    companion object {
        const val KEY_TYPE = "type"
        const val KEY_FIRST_INPUT = "firstInput"
        const val KEY_SECOND_INPUT = "secondInput"
        const val KEY_THIRD_INPUT = "thirdInput"
    }
}

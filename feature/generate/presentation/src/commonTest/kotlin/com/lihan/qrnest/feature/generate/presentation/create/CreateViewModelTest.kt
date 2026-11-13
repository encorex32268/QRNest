package com.lihan.qrnest.feature.generate.presentation.create

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.lihan.qrnest.core.domain.model.QRCodeHistory
import com.lihan.qrnest.core.domain.model.QRCodeType
import com.lihan.qrnest.core.domain.repository.HistoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class CreateViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeRepository: FakeHistoryRepository

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeHistoryRepository()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `single input types enable generate button only when first input is non empty`() {
        val viewModel = CreateViewModel(
            historyRepository = fakeRepository,
            savedStateHandle = SavedStateHandle(mapOf(CreateViewModel.KEY_TYPE to QRCodeType.Link.typeCode)),
            clock = FixedClock(1_700_000_000_000L)
        )

        assertThat(viewModel.state.value.generateButtonEnabled).isFalse()

        viewModel.onAction(CreateAction.OnFirstInputChanged("https://qrnest.app"))
        assertThat(viewModel.state.value.generateButtonEnabled).isTrue()

        viewModel.onAction(CreateAction.OnFirstInputChanged(""))
        assertThat(viewModel.state.value.generateButtonEnabled).isFalse()
    }

    @Test
    fun `geolocation requires both latitude and longitude before enabling generate`() {
        val viewModel = CreateViewModel(
            historyRepository = fakeRepository,
            savedStateHandle = SavedStateHandle(mapOf(CreateViewModel.KEY_TYPE to QRCodeType.Geolocation.typeCode))
        )

        viewModel.onAction(CreateAction.OnFirstInputChanged("25.033"))
        assertThat(viewModel.state.value.generateButtonEnabled).isFalse()

        viewModel.onAction(CreateAction.OnSecondInputChanged("121.565"))
        assertThat(viewModel.state.value.generateButtonEnabled).isTrue()
    }

    @Test
    fun `savedStateHandle restores previous inputs across recreation`() {
        val savedStateHandle = SavedStateHandle(
            mapOf(
                CreateViewModel.KEY_TYPE to QRCodeType.WiFi.typeCode,
                CreateViewModel.KEY_FIRST_INPUT to "OfficeNet",
                CreateViewModel.KEY_SECOND_INPUT to "pass1234",
                CreateViewModel.KEY_THIRD_INPUT to "WPA2"
            )
        )

        val viewModel = CreateViewModel(
            historyRepository = fakeRepository,
            savedStateHandle = savedStateHandle
        )

        assertThat(viewModel.state.value.qrCodeType).isEqualTo(QRCodeType.WiFi)
        assertThat(viewModel.state.value.firstInput).isEqualTo("OfficeNet")
        assertThat(viewModel.state.value.secondInput).isEqualTo("pass1234")
        assertThat(viewModel.state.value.thirdInput).isEqualTo("WPA2")
        assertThat(viewModel.state.value.generateButtonEnabled).isTrue()

        viewModel.onAction(CreateAction.OnSecondInputChanged("newPass"))
        assertThat(savedStateHandle.get<String>(CreateViewModel.KEY_SECOND_INPUT)).isEqualTo("newPass")
    }

    @Test
    fun `generate button formats payload inserts history and emits NavigateToPreview`() = runTest(testDispatcher) {
        val viewModel = CreateViewModel(
            historyRepository = fakeRepository,
            savedStateHandle = SavedStateHandle(mapOf(CreateViewModel.KEY_TYPE to QRCodeType.PhoneNumber.typeCode)),
            clock = FixedClock(1_700_000_123_000L)
        )

        viewModel.onAction(CreateAction.OnFirstInputChanged("+886912345678"))

        viewModel.events.test {
            viewModel.onAction(CreateAction.GenerateButtonClick)

            val event = awaitItem()
            assertThat(event).isEqualTo(CreateEvent.NavigateToPreview(id = 1L))
            cancelAndIgnoreRemainingEvents()
        }

        assertThat(fakeRepository.items.value).hasSize(1)
        val saved = fakeRepository.items.value.first()
        assertThat(saved.type).isEqualTo(QRCodeType.PhoneNumber.typeCode)
        assertThat(saved.content).isEqualTo("tel:+886912345678")
        assertThat(saved.createdAt).isEqualTo(1_700_000_123_000L)
        assertThat(saved.isGenerated).isTrue()
        assertThat(saved.isFavorite).isFalse()
    }

    private class FixedClock(private val epochMillis: Long) : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(epochMillis)
    }

    private class FakeHistoryRepository : HistoryRepository {
        private var nextId = 1L
        val items = MutableStateFlow<List<QRCodeHistory>>(emptyList())

        override suspend fun upsertHistory(qrCodeHistory: QRCodeHistory): Long {
            val id = qrCodeHistory.id ?: nextId++
            val saved = qrCodeHistory.copy(id = id)
            items.value = items.value.filterNot { it.id == id } + saved
            return id
        }

        override suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean) = Unit
        override fun getHistories(): Flow<List<QRCodeHistory>> = items
        override suspend fun deleteById(id: Long) = Unit
        override fun getScannedHistories(): Flow<List<QRCodeHistory>> =
            items.map { list -> list.filter { !it.isGenerated } }
        override fun getGeneratedHistories(): Flow<List<QRCodeHistory>> =
            items.map { list -> list.filter { it.isGenerated } }
        override suspend fun deleteAllScanned() = Unit
        override suspend fun deleteAllGenerated() = Unit
        override suspend fun getHistoryById(id: Long): QRCodeHistory? =
            items.value.firstOrNull { it.id == id }
    }
}

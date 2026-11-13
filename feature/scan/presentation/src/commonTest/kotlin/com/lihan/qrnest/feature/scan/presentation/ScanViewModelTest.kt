package com.lihan.qrnest.feature.scan.presentation

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.lihan.qrnest.core.domain.model.QRCodeHistory
import com.lihan.qrnest.core.domain.model.QRCodeType
import com.lihan.qrnest.core.domain.model.ScannedBarcode
import com.lihan.qrnest.core.domain.repository.HistoryRepository
import com.lihan.qrnest.feature.scan.domain.QrImageScanner
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
class ScanViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeRepository: FakeHistoryRepository
    private lateinit var fakeImageScanner: FakeQrImageScanner

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeHistoryRepository()
        fakeImageScanner = FakeQrImageScanner()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `live barcode scan saves history and emits NavigateToPreview`() = runTest(testDispatcher) {
        val viewModel = ScanViewModel(
            historyRepository = fakeRepository,
            qrImageScanner = fakeImageScanner,
            clock = FixedClock(1_700_000_555_000L)
        )

        viewModel.events.test {
            viewModel.onAction(
                ScanAction.OnBarcodeScanned(
                    ScannedBarcode(type = QRCodeType.TYPE_URL, content = "https://qrnest.dev")
                )
            )

            assertThat(awaitItem()).isEqualTo(ScanEvent.NavigateToPreview(id = 1L))
            cancelAndIgnoreRemainingEvents()
        }

        assertThat(fakeRepository.items.value).hasSize(1)
        val saved = fakeRepository.items.value.first()
        assertThat(saved.type).isEqualTo(QRCodeType.TYPE_URL)
        assertThat(saved.content).isEqualTo("https://qrnest.dev")
        assertThat(saved.isGenerated).isFalse()
        assertThat(viewModel.state.value.isLoading).isFalse()
    }

    @Test
    fun `picked image with valid QR saves history and emits NavigateToPreview`() = runTest(testDispatcher) {
        fakeImageScanner.result = ScannedBarcode(
            type = QRCodeType.TYPE_TEXT,
            content = "Hello from image!"
        )
        val viewModel = ScanViewModel(
            historyRepository = fakeRepository,
            qrImageScanner = fakeImageScanner,
            clock = FixedClock(1_700_000_666_000L)
        )

        viewModel.events.test {
            viewModel.onAction(ScanAction.OnImagePicked(byteArrayOf(1, 2, 3)))
            assertThat(awaitItem()).isEqualTo(ScanEvent.NavigateToPreview(id = 1L))
            cancelAndIgnoreRemainingEvents()
        }

        assertThat(viewModel.state.value.isShowNoQRCodesFound).isFalse()
        assertThat(fakeRepository.items.value.first().content).isEqualTo("Hello from image!")
    }

    @Test
    fun `picked image without QR code shows NoQRCodeFoundDialog and can be dismissed`() = runTest(testDispatcher) {
        fakeImageScanner.result = null
        val viewModel = ScanViewModel(
            historyRepository = fakeRepository,
            qrImageScanner = fakeImageScanner
        )

        viewModel.onAction(ScanAction.OnImagePicked(byteArrayOf(9, 8, 7)))
        assertThat(viewModel.state.value.isShowNoQRCodesFound).isTrue()
        assertThat(viewModel.state.value.isLoading).isFalse()

        viewModel.onAction(ScanAction.DismissNoQRCodeFoundDialog)
        assertThat(viewModel.state.value.isShowNoQRCodesFound).isFalse()
    }

    @Test
    fun `flashlight toggle and permission dialog actions update state`() {
        val viewModel = ScanViewModel(
            historyRepository = fakeRepository,
            qrImageScanner = fakeImageScanner
        )

        assertThat(viewModel.state.value.isOpeningFlashlight).isFalse()
        viewModel.onAction(ScanAction.FlashClick)
        assertThat(viewModel.state.value.isOpeningFlashlight).isTrue()
        viewModel.onAction(ScanAction.FlashClick)
        assertThat(viewModel.state.value.isOpeningFlashlight).isFalse()

        viewModel.onAction(ScanAction.ShowCameraPermissionDialog)
        assertThat(viewModel.state.value.isShowCameraPermissionDialog).isTrue()
        viewModel.onAction(ScanAction.DismissCameraPermissionDialog)
        assertThat(viewModel.state.value.isShowCameraPermissionDialog).isFalse()
    }

    private class FixedClock(private val epochMillis: Long) : Clock {
        override fun now(): Instant = Instant.fromEpochMilliseconds(epochMillis)
    }

    private class FakeQrImageScanner : QrImageScanner {
        var result: ScannedBarcode? = null
        override suspend fun scanImage(imageBytes: ByteArray): ScannedBarcode? = result
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

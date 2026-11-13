package com.lihan.qrnest.feature.preview.presentation

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import com.lihan.qrnest.core.domain.model.QRCodeHistory
import com.lihan.qrnest.core.domain.model.QRCodeType
import com.lihan.qrnest.core.domain.repository.HistoryRepository
import com.lihan.qrnest.core.domain.util.DataError
import com.lihan.qrnest.core.domain.util.EmptyResult
import com.lihan.qrnest.core.domain.util.Result
import com.lihan.qrnest.feature.preview.domain.ClipboardManager
import com.lihan.qrnest.feature.preview.domain.QrImageSaver
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

@OptIn(ExperimentalCoroutinesApi::class)
class PreviewViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var fakeRepository: FakeHistoryRepository
    private lateinit var fakeClipboard: FakeClipboardManager
    private lateinit var fakeSaver: FakeQrImageSaver

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeHistoryRepository()
        fakeClipboard = FakeClipboardManager()
        fakeSaver = FakeQrImageSaver()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `title edit enforces 32 char limit and saves to repository on back`() = runTest(testDispatcher) {
        fakeRepository.seed(
            QRCodeHistory(
                id = 1L,
                title = "Old",
                type = QRCodeType.Text.typeCode,
                content = "Sample content",
                createdAt = 1_700_000_000_000L,
                isGenerated = true,
                isFavorite = false
            )
        )
        val savedStateHandle = SavedStateHandle(mapOf(PreviewViewModel.KEY_ID to 1L))
        val viewModel = PreviewViewModel(fakeRepository, fakeClipboard, fakeSaver, savedStateHandle)

        viewModel.state.test {
            val loaded = awaitItem()
            assertThat(loaded.titleInput).isEqualTo("Old")

            val overLimit = "12345678901234567890123456789012_EXTRA"
            viewModel.onAction(PreviewAction.OnTitleChanged(overLimit))
            val updated = awaitItem()
            assertThat(updated.titleInput).isEqualTo("12345678901234567890123456789012")
            assertThat(updated.titleInput.length).isEqualTo(32)
            assertThat(savedStateHandle.get<String>(PreviewViewModel.KEY_TITLE_INPUT))
                .isEqualTo("12345678901234567890123456789012")

            viewModel.events.test {
                viewModel.onAction(PreviewAction.BackClick)
                assertThat(awaitItem()).isEqualTo(PreviewEvent.Back)
                cancelAndIgnoreRemainingEvents()
            }

            assertThat(fakeRepository.getHistoryById(1L)?.title)
                .isEqualTo("12345678901234567890123456789012")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `wifi copy extracts password only and primary action opens wifi settings`() = runTest(testDispatcher) {
        fakeRepository.seed(
            QRCodeHistory(
                id = 2L,
                type = QRCodeType.WiFi.typeCode,
                content = "SSID: Home_5G\nPassword: mySecretPass!\nEncryption type: WPA2",
                createdAt = 1_700_000_000_000L,
                isGenerated = false,
                isFavorite = false
            )
        )
        val viewModel = PreviewViewModel(
            fakeRepository,
            fakeClipboard,
            fakeSaver,
            SavedStateHandle(mapOf(PreviewViewModel.KEY_ID to 2L))
        )

        viewModel.state.test {
            awaitItem()

            viewModel.onAction(PreviewAction.CopyClick)
            assertThat(fakeClipboard.lastCopied).isEqualTo("mySecretPass!")

            viewModel.events.test {
                viewModel.onAction(PreviewAction.ContentPrimaryActionClick)
                assertThat(awaitItem()).isEqualTo(PreviewEvent.OpenWifiSettings)
                cancelAndIgnoreRemainingEvents()
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `favorite click toggles state and persists`() = runTest(testDispatcher) {
        fakeRepository.seed(
            QRCodeHistory(
                id = 3L,
                type = QRCodeType.Link.typeCode,
                content = "https://example.com",
                createdAt = 1_700_000_000_000L,
                isGenerated = false,
                isFavorite = false
            )
        )
        val viewModel = PreviewViewModel(
            fakeRepository,
            fakeClipboard,
            fakeSaver,
            SavedStateHandle(mapOf(PreviewViewModel.KEY_ID to 3L))
        )

        viewModel.state.test {
            assertThat(awaitItem().qrCodeHistoryUi?.isFavorite).isEqualTo(false)

            viewModel.onAction(PreviewAction.FavoriteClick)
            assertThat(awaitItem().qrCodeHistoryUi?.isFavorite).isEqualTo(true)
            assertThat(fakeRepository.getHistoryById(3L)?.isFavorite).isEqualTo(true)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `save click delegates to QrImageSaver and emits SaveSucceed`() = runTest(testDispatcher) {
        fakeRepository.seed(
            QRCodeHistory(
                id = 4L,
                type = QRCodeType.Text.typeCode,
                content = "Save me",
                createdAt = 1_700_000_000_000L,
                isGenerated = true,
                isFavorite = false
            )
        )
        val viewModel = PreviewViewModel(
            fakeRepository,
            fakeClipboard,
            fakeSaver,
            SavedStateHandle(mapOf(PreviewViewModel.KEY_ID to 4L))
        )

        viewModel.state.test {
            awaitItem()
            viewModel.events.test {
                viewModel.onAction(PreviewAction.SaveClick)
                assertThat(awaitItem()).isEqualTo(PreviewEvent.SaveSucceed)
                assertThat(fakeSaver.lastSavedContent).isEqualTo("Save me")
                cancelAndIgnoreRemainingEvents()
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `missing history id marks not found and emits DataError`() = runTest(testDispatcher) {
        val viewModel = PreviewViewModel(
            fakeRepository,
            fakeClipboard,
            fakeSaver,
            SavedStateHandle(mapOf(PreviewViewModel.KEY_ID to 999L))
        )

        viewModel.events.test {
            viewModel.state.test {
                val state = awaitItem()
                assertThat(state.isNotFound).isTrue()
                assertThat(state.qrCodeHistoryUi).isNull()
                cancelAndIgnoreRemainingEvents()
            }
            assertThat(awaitItem()).isEqualTo(PreviewEvent.DataError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private class FakeClipboardManager : ClipboardManager {
        var lastCopied: String? = null
        override fun copyText(text: String) {
            lastCopied = text
        }
    }

    private class FakeQrImageSaver : QrImageSaver {
        var lastSavedContent: String? = null
        var nextResult: EmptyResult<DataError.Local> = Result.Success(Unit)

        override suspend fun saveQrCode(content: String): EmptyResult<DataError.Local> {
            lastSavedContent = content
            return nextResult
        }
    }

    private class FakeHistoryRepository : HistoryRepository {
        private val items = MutableStateFlow<List<QRCodeHistory>>(emptyList())

        fun seed(vararg entries: QRCodeHistory) {
            items.value = entries.toList()
        }

        override suspend fun upsertHistory(qrCodeHistory: QRCodeHistory): Long {
            val id = qrCodeHistory.id ?: ((items.value.maxOfOrNull { it.id ?: 0L } ?: 0L) + 1L)
            items.value = items.value.filterNot { it.id == id } + qrCodeHistory.copy(id = id)
            return id
        }

        override suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean) {
            items.value = items.value.map {
                if (it.id == id) it.copy(isFavorite = isFavorite) else it
            }
        }

        override fun getHistories(): Flow<List<QRCodeHistory>> = items
        override suspend fun deleteById(id: Long) {
            items.value = items.value.filterNot { it.id == id }
        }
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

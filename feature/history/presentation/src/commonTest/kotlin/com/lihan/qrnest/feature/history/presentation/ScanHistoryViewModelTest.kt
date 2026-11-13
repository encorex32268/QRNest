package com.lihan.qrnest.feature.history.presentation

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
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

@OptIn(ExperimentalCoroutinesApi::class)
class ScanHistoryViewModelTest {

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
    fun `state separates scanned and generated histories`() = runTest(testDispatcher) {
        fakeRepository.seed(
            QRCodeHistory(
                id = 1L,
                type = QRCodeType.Text.typeCode,
                content = "Scanned Note",
                createdAt = 1_700_000_000_000L,
                isGenerated = false,
                isFavorite = false
            ),
            QRCodeHistory(
                id = 2L,
                type = QRCodeType.Link.typeCode,
                content = "https://qrnest.app",
                createdAt = 1_700_000_001_000L,
                isGenerated = true,
                isFavorite = true
            )
        )

        val viewModel = ScanHistoryViewModel(fakeRepository)

        viewModel.state.test {
            val current = awaitItem()
            assertThat(current.scannedItems).hasSize(1)
            assertThat(current.scannedItems.first().content).isEqualTo("Scanned Note")
            assertThat(current.generatedItems).hasSize(1)
            assertThat(current.generatedItems.first().content).isEqualTo("https://qrnest.app")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `favorite click toggles favorite status in repository`() = runTest(testDispatcher) {
        fakeRepository.seed(
            QRCodeHistory(
                id = 10L,
                type = QRCodeType.Text.typeCode,
                content = "Hello",
                createdAt = 1_700_000_000_000L,
                isGenerated = false,
                isFavorite = false
            )
        )
        val viewModel = ScanHistoryViewModel(fakeRepository)

        viewModel.state.test {
            assertThat(awaitItem().scannedItems.first().isFavorite).isFalse()

            viewModel.onAction(ScanHistoryAction.ItemFavoriteClick(id = 10L, isFavorite = false))
            assertThat(awaitItem().scannedItems.first().isFavorite).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `long click opens editor sheet and delete removes item`() = runTest(testDispatcher) {
        fakeRepository.seed(
            QRCodeHistory(
                id = 5L,
                type = QRCodeType.Text.typeCode,
                content = "To Delete",
                createdAt = 1_700_000_000_000L,
                isGenerated = false,
                isFavorite = false
            )
        )
        val viewModel = ScanHistoryViewModel(fakeRepository)

        viewModel.state.test {
            awaitItem()

            viewModel.onAction(ScanHistoryAction.ItemLongClick(5L))
            val opened = awaitItem()
            assertThat(opened.isShowEditorBottomSheet).isTrue()
            assertThat(opened.selectedId).isEqualTo(5L)

            viewModel.onAction(ScanHistoryAction.DeleteClick)
            val afterDelete = awaitItem()
            assertThat(afterDelete.isShowEditorBottomSheet).isFalse()
            assertThat(afterDelete.selectedId).isNull()
            assertThat(fakeRepository.items.value).isEmpty()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `share click emits ShareQRCode and item click emits NavigateToPreview`() = runTest(testDispatcher) {
        fakeRepository.seed(
            QRCodeHistory(
                id = 7L,
                type = QRCodeType.Link.typeCode,
                content = "https://kotlinlang.org",
                createdAt = 1_700_000_000_000L,
                isGenerated = true,
                isFavorite = false
            )
        )
        val viewModel = ScanHistoryViewModel(fakeRepository)

        viewModel.state.test {
            awaitItem()
            viewModel.events.test {
                viewModel.onAction(ScanHistoryAction.ItemLongClick(7L))
                viewModel.onAction(ScanHistoryAction.ShareClick)
                assertThat(awaitItem()).isEqualTo(ScanHistoryEvent.ShareQRCode("https://kotlinlang.org"))

                viewModel.onAction(ScanHistoryAction.ItemClick(7L))
                assertThat(awaitItem()).isEqualTo(ScanHistoryEvent.NavigateToPreview(7L))
                cancelAndIgnoreRemainingEvents()
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    private class FakeHistoryRepository : HistoryRepository {
        val items = MutableStateFlow<List<QRCodeHistory>>(emptyList())

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

        override suspend fun deleteAllScanned() {
            items.value = items.value.filter { it.isGenerated }
        }

        override suspend fun deleteAllGenerated() {
            items.value = items.value.filter { !it.isGenerated }
        }

        override suspend fun getHistoryById(id: Long): QRCodeHistory? =
            items.value.firstOrNull { it.id == id }
    }
}

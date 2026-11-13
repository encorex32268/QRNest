package com.lihan.qrnest.core.data.repository

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.lihan.qrnest.core.data.mapper.toDomain
import com.lihan.qrnest.core.data.mapper.toEntity
import com.lihan.qrnest.core.database.QRCodeHistoryDao
import com.lihan.qrnest.core.database.QRCodeHistoryEntity
import com.lihan.qrnest.core.domain.model.QRCodeHistory
import com.lihan.qrnest.core.domain.model.QRCodeType
import com.lihan.qrnest.core.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class RoomHistoryRepositoryTest {

    private lateinit var fakeDao: FakeQRCodeHistoryDao
    private lateinit var repository: RoomHistoryRepository

    @BeforeTest
    fun setUp() {
        fakeDao = FakeQRCodeHistoryDao()
        repository = RoomHistoryRepository(fakeDao)
    }

    @Test
    fun `mapper preserves all fields across entity and domain round trip`() {
        val domain = QRCodeHistory(
            id = 42L,
            title = "Office WiFi",
            type = QRCodeType.TYPE_WIFI,
            content = "SSID: Office\nPassword: secret\nEncryption type: WPA2",
            createdAt = 1_700_000_000_000L,
            isGenerated = false,
            isFavorite = true
        )

        val roundTripped = domain.toEntity().toDomain()

        assertThat(roundTripped).isEqualTo(domain)
    }

    @Test
    fun `upsertHistory assigns auto-generated Long id and getHistoryById retrieves it`() = runTest {
        val id = repository.upsertHistory(
            QRCodeHistory(
                title = "Website",
                type = QRCodeType.TYPE_URL,
                content = "https://example.com",
                createdAt = 1000L,
                isGenerated = false,
                isFavorite = false
            )
        )

        val loaded = repository.getHistoryById(id)

        assertThat(id).isEqualTo(1L)
        assertThat(loaded?.content).isEqualTo("https://example.com")
    }

    @Test
    fun `getScannedHistories and getGeneratedHistories partition and sort by favorite then createdAt desc`() = runTest {
        repository.upsertHistory(
            QRCodeHistory(id = 1L, type = QRCodeType.TYPE_TEXT, content = "scan-old", createdAt = 100L, isGenerated = false, isFavorite = false)
        )
        repository.upsertHistory(
            QRCodeHistory(id = 2L, type = QRCodeType.TYPE_TEXT, content = "scan-new", createdAt = 200L, isGenerated = false, isFavorite = false)
        )
        repository.upsertHistory(
            QRCodeHistory(id = 3L, type = QRCodeType.TYPE_TEXT, content = "scan-fav", createdAt = 50L, isGenerated = false, isFavorite = true)
        )
        repository.upsertHistory(
            QRCodeHistory(id = 4L, type = QRCodeType.TYPE_TEXT, content = "gen-only", createdAt = 300L, isGenerated = true, isFavorite = false)
        )

        repository.getScannedHistories().test {
            assertThat(awaitItem().map { it.content }).containsExactly("scan-fav", "scan-new", "scan-old")
        }

        repository.getGeneratedHistories().test {
            assertThat(awaitItem().map { it.content }).containsExactly("gen-only")
        }
    }

    @Test
    fun `updateFavoriteStatus and deleteById mutate stored entries`() = runTest {
        val id = repository.upsertHistory(
            QRCodeHistory(type = QRCodeType.TYPE_TEXT, content = "hello", createdAt = 100L, isGenerated = false, isFavorite = false)
        )

        repository.updateFavoriteStatus(id, isFavorite = true)
        assertThat(repository.getHistoryById(id)?.isFavorite).isEqualTo(true)

        repository.deleteById(id)
        assertThat(repository.getHistoryById(id)).isNull()
    }

    @Test
    fun `fakeHistoryRepository mirrors ordering and deletion contract`() = runTest {
        val fakeRepo = FakeHistoryRepository()
        val a = fakeRepo.upsertHistory(QRCodeHistory(type = 7, content = "A", createdAt = 10L, isGenerated = true, isFavorite = false))
        val b = fakeRepo.upsertHistory(QRCodeHistory(type = 7, content = "B", createdAt = 20L, isGenerated = true, isFavorite = false))
        fakeRepo.updateFavoriteStatus(a, true)

        fakeRepo.getGeneratedHistories().test {
            assertThat(awaitItem().map { it.id }).containsExactly(a, b)
        }

        fakeRepo.deleteAllGenerated()
        assertThat(fakeRepo.getHistoryById(a)).isNull()
    }
}

private class FakeQRCodeHistoryDao : QRCodeHistoryDao {
    private val items = MutableStateFlow<List<QRCodeHistoryEntity>>(emptyList())
    private var nextId = 1L

    override suspend fun upsertHistory(qrCodeHistoryEntity: QRCodeHistoryEntity): Long {
        val current = items.value.toMutableList()
        val existingId = qrCodeHistoryEntity.id
        val id = existingId ?: nextId++
        if (existingId != null && existingId >= nextId) {
            nextId = existingId + 1
        }
        val entity = qrCodeHistoryEntity.copy(id = id)
        val index = current.indexOfFirst { it.id == id }
        if (index >= 0) {
            current[index] = entity
        } else {
            current.add(entity)
        }
        items.value = current
        return id
    }

    override suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean) {
        items.value = items.value.map { if (it.id == id) it.copy(isFavorite = isFavorite) else it }
    }

    override fun getHistories(): Flow<List<QRCodeHistoryEntity>> =
        items.map { list -> list.sortedWith(compareByDescending<QRCodeHistoryEntity> { it.isFavorite }.thenByDescending { it.createdAt }) }

    override suspend fun deleteById(id: Long) {
        items.value = items.value.filterNot { it.id == id }
    }

    override fun getScannedHistories(): Flow<List<QRCodeHistoryEntity>> =
        getHistories().map { list -> list.filter { !it.isGenerated } }

    override fun getGeneratedHistories(): Flow<List<QRCodeHistoryEntity>> =
        getHistories().map { list -> list.filter { it.isGenerated } }

    override suspend fun deleteAllScanned() {
        items.value = items.value.filter { it.isGenerated }
    }

    override suspend fun deleteAllGenerated() {
        items.value = items.value.filterNot { it.isGenerated }
    }

    override suspend fun getHistoryById(id: Long): QRCodeHistoryEntity? =
        items.value.find { it.id == id }
}

private class FakeHistoryRepository : HistoryRepository {
    private val items = MutableStateFlow<List<QRCodeHistory>>(emptyList())
    private var nextId = 1L

    override suspend fun upsertHistory(history: QRCodeHistory): Long {
        val current = items.value.toMutableList()
        val id = history.id ?: nextId++
        val saved = history.copy(id = id)
        val index = current.indexOfFirst { it.id == id }
        if (index >= 0) current[index] = saved else current.add(saved)
        items.value = current
        return id
    }

    override suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean) {
        items.value = items.value.map { if (it.id == id) it.copy(isFavorite = isFavorite) else it }
    }

    override fun getHistories(): Flow<List<QRCodeHistory>> =
        items.map { list -> list.sortedWith(compareByDescending<QRCodeHistory> { it.isFavorite }.thenByDescending { it.createdAt }) }

    override suspend fun deleteById(id: Long) {
        items.value = items.value.filterNot { it.id == id }
    }

    override fun getScannedHistories(): Flow<List<QRCodeHistory>> =
        getHistories().map { list -> list.filter { !it.isGenerated } }

    override fun getGeneratedHistories(): Flow<List<QRCodeHistory>> =
        getHistories().map { list -> list.filter { it.isGenerated } }

    override suspend fun deleteAllScanned() {
        items.value = items.value.filter { it.isGenerated }
    }

    override suspend fun deleteAllGenerated() {
        items.value = items.value.filterNot { it.isGenerated }
    }

    override suspend fun getHistoryById(id: Long): QRCodeHistory? =
        items.value.find { it.id == id }
}

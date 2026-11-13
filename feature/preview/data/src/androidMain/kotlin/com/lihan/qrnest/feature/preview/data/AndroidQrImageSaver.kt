package com.lihan.qrnest.feature.preview.data

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.core.graphics.createBitmap
import com.lihan.qrnest.core.domain.util.DataError
import com.lihan.qrnest.core.domain.util.EmptyResult
import com.lihan.qrnest.core.domain.util.Result
import com.lihan.qrnest.feature.preview.domain.QrImageSaver
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import kotlin.math.roundToInt
import kotlin.time.Clock

class AndroidQrImageSaver(
    private val context: Context
) : QrImageSaver {

    override suspend fun saveQrCode(content: String): EmptyResult<DataError.Local> {
        if (content.isBlank()) {
            return Result.Failure(DataError.Local.UNKNOWN)
        }

        val paddedBytes = createPaddedPng(content)
            ?: return Result.Failure(DataError.Local.IMAGE_DECODE_FAILED)

        val fileName = "QRNest_${Clock.System.now().toEpochMilliseconds()}.png"
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveToDownloadsMediaStore(fileName, paddedBytes)
        } else {
            saveToLegacyDownloads(fileName, paddedBytes)
        }
    }

    private suspend fun createPaddedPng(content: String): ByteArray? {
        return try {
            withContext(Dispatchers.Default) {
                val srcBitmap = QrPngRenderer.renderImageBitmap(content).asAndroidBitmap()

                val paddingPx = 60f
                val stream = ByteArrayOutputStream()
                val outBitmap = createBitmap(
                    width = srcBitmap.width + (paddingPx * 2).roundToInt(),
                    height = srcBitmap.height + (paddingPx * 2).roundToInt()
                )
                try {
                    val canvas = Canvas(outBitmap)
                    canvas.drawColor(Color.WHITE)
                    canvas.drawBitmap(srcBitmap, paddingPx, paddingPx, null)
                    ensureActive()
                    outBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                    stream.toByteArray()
                } finally {
                    outBitmap.recycle()
                    srcBitmap.recycle()
                    stream.close()
                }
            }
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            null
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private suspend fun saveToDownloadsMediaStore(
        fileName: String,
        bytes: ByteArray
    ): EmptyResult<DataError.Local> {
        val resolver = context.contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            put(MediaStore.MediaColumns.IS_PENDING, 1)
        }

        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            ?: return Result.Failure(DataError.Local.UNKNOWN)

        return try {
            withContext(Dispatchers.IO) {
                resolver.openOutputStream(uri)?.use { stream ->
                    stream.write(bytes)
                } ?: return@withContext Result.Failure(DataError.Local.FILE_NOT_FOUND)

                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
                Result.Success(Unit)
            }
        } catch (e: IOException) {
            resolver.delete(uri, null, null)
            val isDiskFull = e.message?.contains("ENOSPC", ignoreCase = true) == true
            Result.Failure(if (isDiskFull) DataError.Local.DISK_FULL else DataError.Local.UNKNOWN)
        } catch (e: SecurityException) {
            resolver.delete(uri, null, null)
            Result.Failure(DataError.Local.PERMISSION_DENIED)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            resolver.delete(uri, null, null)
            Result.Failure(DataError.Local.UNKNOWN)
        }
    }

    @Suppress("DEPRECATION")
    private suspend fun saveToLegacyDownloads(
        fileName: String,
        bytes: ByteArray
    ): EmptyResult<DataError.Local> {
        return try {
            withContext(Dispatchers.IO) {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
                )
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }
                File(downloadsDir, fileName).outputStream().use { stream ->
                    stream.write(bytes)
                }
                Result.Success(Unit)
            }
        } catch (e: SecurityException) {
            Result.Failure(DataError.Local.PERMISSION_DENIED)
        } catch (e: IOException) {
            val isDiskFull = e.message?.contains("ENOSPC", ignoreCase = true) == true
            Result.Failure(if (isDiskFull) DataError.Local.DISK_FULL else DataError.Local.UNKNOWN)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.Failure(DataError.Local.UNKNOWN)
        }
    }
}

package expo.modules.gooddisplayepaper.bridge

import android.content.Context
import expo.modules.gooddisplayepaper.models.ProcessedImage
import expo.modules.gooddisplayepaper.models.ScreenConfig
import expo.modules.gooddisplayepaper.models.WriteProgress
import expo.modules.gooddisplayepaper.models.WriteResult
import expo.modules.gooddisplayepaper.protocol.EpdConfigFactory
import expo.modules.gooddisplayepaper.services.ApduTraceEntry
import expo.modules.gooddisplayepaper.services.ImageProcessor
import expo.modules.gooddisplayepaper.services.NfcEpaperWriter
import expo.modules.gooddisplayepaper.services.NfcWriteException
import expo.modules.gooddisplayepaper.services.WriteCancellation
import expo.modules.gooddisplayepaper.services.WriteCancelledException
import expo.modules.gooddisplayepaper.services.WriteOptions
import expo.modules.gooddisplayepaper.services.WriteProgressListener
import expo.modules.gooddisplayepaper.services.WriteSession
import expo.modules.kotlin.records.Field
import expo.modules.kotlin.records.Record
import java.util.concurrent.atomic.AtomicReference

class WriteToTagOptions : Record {
  @Field var imageUri: String? = null
  @Field var imageBase64: String? = null
  @Field var inchCode: Int = 0
  @Field var colorMode: String = "mono"
  @Field var tagIdHex: String? = null
  @Field var busyTimeoutMs: Int? = null
  @Field var transceiveMaxRetries: Int? = null
  @Field var emitApduTrace: Boolean = true
}

class NfcHandoffOptions : Record {
  @Field var tagIdHex: String? = null
}

typealias EventSink = (String, Map<String, Any?>) -> Unit

object WriteBridge {

  private val activeCancellation = AtomicReference<WriteCancellation?>(null)
  private val activeSession = AtomicReference<WriteSession?>(null)

  @JvmStatic
  fun registerNfcHandoff(options: NfcHandoffOptions?) {
    NfcManagerBridge.openIsoDep(options?.tagIdHex)
  }

  @JvmStatic
  fun cancelWrite(): Boolean {
    val cancellation = activeCancellation.get() ?: return false
    cancellation.cancel()
    try {
      activeSession.get()?.transceiver?.closeQuietly()
    } catch (_: Exception) {
      // best-effort disconnect
    }
    return true
  }

  @JvmStatic
  fun writeToTag(
      context: Context,
      options: WriteToTagOptions,
      events: EventSink,
  ): Map<String, Any?> {
    if (options.inchCode <= 0) {
      throw IllegalArgumentException("inchCode is required")
    }
    val colorModeInt = PanelCatalog.colorModeFromString(options.colorMode)
    if (!EpdConfigFactory.supports(colorModeInt, options.inchCode)) {
      throw IllegalArgumentException(
          "Unsupported panel color=${options.colorMode} inch=${options.inchCode}")
    }

    val screenConfig = EpdConfigFactory.create(colorModeInt, options.inchCode)
    val processedImage = processImage(context, options, screenConfig)

    val isoDep =
        NfcHandoffStore.peekIsoDep()
            ?: NfcManagerBridge.openIsoDep(options.tagIdHex)

    val cancellation = WriteCancellation()
    activeCancellation.set(cancellation)

    val writeOptionsBuilder =
        WriteOptions.builder()
            .busyTimeoutForColorMode(colorModeInt)
            .apduTraceLogging(true)
    options.busyTimeoutMs?.let { writeOptionsBuilder.busyTimeoutMs(it) }
    options.transceiveMaxRetries?.let { writeOptionsBuilder.transceiveMaxRetries(it) }
    val writeOptions = writeOptionsBuilder.build()

    var lastTraceSize = 0
    var sessionRef: WriteSession? = null

    val listener =
        object : WriteProgressListener {
          override fun onPhaseChanged(phase: WriteProgress.Phase, progress: WriteProgress) {
            events(
                "onStatus",
                mapOf("phase" to phase.name.lowercase(), "message" to progress.message))
            events("onProgress", progressToMap(progress))
            sessionRef?.let { s ->
              lastTraceSize = emitNewTraceEntries(options, events, s, lastTraceSize)
            }
          }

          override fun onProgress(progress: WriteProgress) {
            events("onProgress", progressToMap(progress))
            sessionRef?.let { s ->
              lastTraceSize = emitNewTraceEntries(options, events, s, lastTraceSize)
            }
          }
        }

    val session =
        WriteSession.create(isoDep, screenConfig, processedImage, writeOptions, listener, cancellation)
    sessionRef = session
    activeSession.set(session)

    try {
      events("onStatus", mapOf("phase" to "starting", "message" to "NFC write starting"))
      val result = NfcEpaperWriter.write(session)
      lastTraceSize = emitNewTraceEntries(options, events, session, lastTraceSize)
      events("onComplete", resultToMap(result))
      return resultToMap(result)
    } catch (e: WriteCancelledException) {
      events(
          "onError",
          mapOf(
              "code" to "CANCELLED",
              "message" to (e.message ?: "cancelled"),
              "phase" to WriteProgress.Phase.FAILED.name.lowercase(),
          ))
      throw e
    } catch (e: NfcWriteException) {
      events(
          "onError",
          mapOf(
              "code" to "NFC_WRITE",
              "message" to (e.message ?: "write failed"),
              "phase" to e.phase.name.lowercase(),
              "sw1" to (e.sw1.toInt() and 0xFF),
              "sw2" to (e.sw2.toInt() and 0xFF),
          ))
      throw e
    } finally {
      activeSession.set(null)
      activeCancellation.set(null)
      NfcHandoffStore.clear()
    }
  }

  private fun processImage(
      context: Context,
      options: WriteToTagOptions,
      screenConfig: ScreenConfig,
  ): ProcessedImage {
    val bitmap =
        when {
          !options.imageUri.isNullOrBlank() ->
              ImageProcessor.decodeBitmapFromUri(context, options.imageUri!!)
          !options.imageBase64.isNullOrBlank() ->
              ImageProcessor.decodeBitmapFromBase64(options.imageBase64!!)
          else -> throw IllegalArgumentException("imageUri or imageBase64 is required")
        }
    try {
      return ImageProcessor.process(bitmap, screenConfig.toImageProcessingConfig())
    } finally {
      if (!bitmap.isRecycled) {
        bitmap.recycle()
      }
    }
  }

  private fun emitNewTraceEntries(
      options: WriteToTagOptions,
      events: EventSink,
      session: WriteSession,
      fromIndex: Int,
  ): Int {
    if (!options.emitApduTrace) {
      return fromIndex
    }
    val trace = session.transceiver.trace
    var index = fromIndex
    while (index < trace.size) {
      events("onTrace", traceEntryToMap(trace[index]))
      index++
    }
    return index
  }

  private fun progressToMap(progress: WriteProgress): Map<String, Any?> {
    return mapOf(
        "phase" to progress.phase.name.lowercase(),
        "uploadPercent" to progress.uploadPercent,
        "packetsSent" to progress.packetsSent,
        "packetsTotal" to progress.packetsTotal,
        "dePollCount" to progress.dePollCount,
        "message" to progress.message,
        "sw1" to (progress.lastSw1.toInt() and 0xFF),
        "sw2" to (progress.lastSw2.toInt() and 0xFF),
    )
  }

  private fun resultToMap(result: WriteResult): Map<String, Any?> {
    return mapOf(
        "success" to result.isSuccess,
        "finalPhase" to result.finalPhase.name.lowercase(),
        "totalApduCount" to result.totalApduCount,
        "d2PacketCount" to result.d2PacketCount,
        "dePollCount" to result.dePollCount,
        "connectDurationMs" to result.connectDurationMs,
        "initDurationMs" to result.initDurationMs,
        "uploadDurationMs" to result.uploadDurationMs,
        "refreshDurationMs" to result.refreshDurationMs,
        "busyWaitDurationMs" to result.busyWaitDurationMs,
        "totalDurationMs" to result.totalDurationMs,
        "transceiveRetryCount" to result.transceiveRetryCount,
        "sw1" to (result.finalSw1.toInt() and 0xFF),
        "sw2" to (result.finalSw2.toInt() and 0xFF),
        "trace" to result.apduTrace.map { traceEntryToMap(it) },
    )
  }

  private fun traceEntryToMap(entry: ApduTraceEntry): Map<String, Any?> {
    return mapOf(
        "timestampMs" to entry.timestampMs,
        "phase" to entry.phase.name.lowercase(),
        "kind" to entry.kind.name,
        "commandHex" to entry.commandHex,
        "responseHex" to entry.responseHex,
        "durationMs" to entry.durationMs,
        "attempt" to entry.attempt,
        "sw1" to (entry.sw1.toInt() and 0xFF),
        "sw2" to (entry.sw2.toInt() and 0xFF),
    )
  }
}

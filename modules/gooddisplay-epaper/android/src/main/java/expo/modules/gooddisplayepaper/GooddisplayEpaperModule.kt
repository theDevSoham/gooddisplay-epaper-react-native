package expo.modules.gooddisplayepaper

import android.content.Context
import android.nfc.tech.IsoDep
import expo.modules.gooddisplayepaper.bridge.PanelCatalog
import expo.modules.gooddisplayepaper.bridge.WriteBridge
import expo.modules.gooddisplayepaper.bridge.WriteBridgeCallbacks
import expo.modules.gooddisplayepaper.bridge.WriteToTagOptions
import expo.modules.gooddisplayepaper.nfc.NfcSessionCancelledException
import expo.modules.gooddisplayepaper.nfc.NfcSessionException
import expo.modules.gooddisplayepaper.nfc.NfcSessionManager
import expo.modules.gooddisplayepaper.services.WriteCancelledException
import expo.modules.kotlin.exception.CodedException
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import java.io.IOException
import kotlinx.coroutines.runBlocking

class GooddisplayEpaperModule : Module() {

  private val nfcSessionManager = NfcSessionManager()

  override fun definition() = ModuleDefinition {
    Name("GooddisplayEpaper")

    Events("onProgress", "onStatus", "onComplete", "onError", "onTrace")

    OnNewIntent { intent -> nfcSessionManager.onNewIntent(intent) }

    OnActivityEntersForeground {
      appContext.currentActivity?.let { nfcSessionManager.onActivityResumed(it) }
    }

    OnActivityEntersBackground {
      appContext.currentActivity?.let { nfcSessionManager.onActivityPaused(it) }
    }

    Function("getProtocolVersion") {
      PanelCatalog.PROTOCOL_VERSION
    }

    Function("getSupportedPanels") {
      PanelCatalog.supportedPanels().map { panel ->
        mapOf(
            "inchCode" to panel.inchCode,
            "label" to panel.label,
            "width" to panel.width,
            "height" to panel.height,
            "colorModes" to panel.colorModes,
        )
      }
    }

    Function("cancelWrite") {
      val cancelledNfc = nfcSessionManager.cancel()
      val cancelledWrite = WriteBridge.cancelWrite()
      cancelledNfc || cancelledWrite
    }

    AsyncFunction("writeToTag") { options: WriteToTagOptions ->
      val activity =
          appContext.currentActivity
              ?: throw CodedException("E_NO_ACTIVITY", "Activity is not available")
      val context = requireContext()

      runBlocking {
        var isoDep: IsoDep? = null
        try {
          sendEvent(
              "onStatus",
              mapOf("phase" to "waiting_for_tag", "message" to "Tap the NFC tag on your device"),
          )

          isoDep = nfcSessionManager.awaitIsoDep(activity)

          sendEvent(
              "onStatus",
              mapOf("phase" to "tag_acquired", "message" to "NFC tag connected"),
          )

          val callbacks = createWriteCallbacks()
          WriteBridge.writeToTag(context, isoDep, options, callbacks)
        } catch (e: NfcSessionCancelledException) {
          sendEvent(
              "onError",
              mapOf(
                  "code" to "CANCELLED",
                  "message" to (e.message ?: "cancelled"),
                  "phase" to "waiting_for_tag",
              ),
          )
          throw bridgeException("CANCELLED", e)
        } catch (e: WriteCancelledException) {
          throw bridgeException("CANCELLED", e)
        } catch (e: NfcSessionException) {
          sendEvent(
              "onError",
              mapOf(
                  "code" to "NFC_SESSION",
                  "message" to (e.message ?: "NFC session failed"),
                  "phase" to "waiting_for_tag",
              ),
          )
          throw bridgeException("NFC_SESSION", e)
        } catch (e: Exception) {
          if (e is CodedException) {
            throw e
          }
          throw bridgeException("WRITE_FAILED", e)
        } finally {
          nfcSessionManager.cleanup(activity)
          closeIsoDepQuietly(isoDep)
        }
      }
    }
  }

  private fun createWriteCallbacks(): WriteBridgeCallbacks =
      object : WriteBridgeCallbacks {
        override fun onStatus(phase: String, message: String?) {
          sendEvent("onStatus", mapOf("phase" to phase, "message" to message))
        }

        override fun onProgress(payload: Map<String, Any?>) {
          sendEvent("onProgress", payload)
        }

        override fun onTrace(payload: Map<String, Any?>) {
          sendEvent("onTrace", payload)
        }

        override fun onComplete(payload: Map<String, Any?>) {
          sendEvent("onComplete", payload)
        }

        override fun onError(payload: Map<String, Any?>) {
          sendEvent("onError", payload)
        }
      }

  private fun requireContext(): Context {
    return appContext.reactContext
        ?: throw CodedException("E_NO_CONTEXT", "React context is not available")
  }

  private fun closeIsoDepQuietly(isoDep: IsoDep?) {
    if (isoDep == null) {
      return
    }
    try {
      if (isoDep.isConnected) {
        isoDep.close()
      }
    } catch (_: IOException) {
      // best-effort
    }
  }

  private fun bridgeException(code: String, cause: Exception): CodedException {
    return CodedException(code, cause.message ?: code, cause)
  }
}

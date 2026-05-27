package expo.modules.gooddisplayepaper

import android.content.Context
import expo.modules.gooddisplayepaper.bridge.NfcHandoffOptions
import expo.modules.gooddisplayepaper.bridge.PanelCatalog
import expo.modules.gooddisplayepaper.bridge.WriteBridge
import expo.modules.gooddisplayepaper.bridge.WriteToTagOptions
import expo.modules.kotlin.exception.CodedException
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition

class GooddisplayEpaperModule : Module() {
  override fun definition() = ModuleDefinition {
    Name("GooddisplayEpaper")

    Events("onProgress", "onStatus", "onComplete", "onError", "onTrace")

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

    AsyncFunction("registerNfcHandoff") { options: NfcHandoffOptions? ->
      try {
        WriteBridge.registerNfcHandoff(options)
        mapOf("registered" to true)
      } catch (e: Exception) {
        throw bridgeException("NFC_HANDOFF", e)
      }
    }

    Function("cancelWrite") {
      WriteBridge.cancelWrite()
    }

    AsyncFunction("writeToTag") { options: WriteToTagOptions ->
      val context = requireContext()
      try {
        WriteBridge.writeToTag(context, options) { eventName, payload ->
          sendEvent(eventName, payload)
        }
      } catch (e: Exception) {
        if (e is CodedException) {
          throw e
        }
        throw bridgeException("WRITE_FAILED", e)
      }
    }
  }

  private fun requireContext(): Context {
    return appContext.reactContext
        ?: throw CodedException("E_NO_CONTEXT", "React context is not available")
  }

  private fun bridgeException(code: String, cause: Exception): CodedException {
    return CodedException(code, cause.message ?: code, cause)
  }
}

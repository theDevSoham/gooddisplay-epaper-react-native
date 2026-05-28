package expo.modules.gooddisplayepaper.bridge

/** Event sink for write orchestration; implemented by the Expo module (not WriteBridge). */
interface WriteBridgeCallbacks {
  fun onStatus(phase: String, message: String?)

  fun onProgress(payload: Map<String, Any?>)

  fun onTrace(payload: Map<String, Any?>)

  fun onComplete(payload: Map<String, Any?>)

  fun onError(payload: Map<String, Any?>)
}

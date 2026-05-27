import ExpoModulesCore

public class GooddisplayEpaperModule: Module {
  public func definition() -> ModuleDefinition {
    Name("GooddisplayEpaper")

    Events("onProgress", "onStatus", "onComplete", "onError", "onTrace")

    Function("getProtocolVersion") {
      return "1.0.0-ios-stub"
    }

    Function("getSupportedPanels") {
      return [] as [[String: Any]]
    }

    AsyncFunction("registerNfcHandoff") { (_: [String: Any]?) in
      throw UnsupportedPlatformException("iOS NFC e-paper write is not implemented")
    }

    Function("cancelWrite") {
      return false
    }

    AsyncFunction("writeToTag") { (_: [String: Any]) in
      throw UnsupportedPlatformException("iOS NFC e-paper write is not implemented")
    }
  }
}

private struct UnsupportedPlatformException: Exception {
  let message: String
  init(_ message: String) {
    self.message = message
  }
}

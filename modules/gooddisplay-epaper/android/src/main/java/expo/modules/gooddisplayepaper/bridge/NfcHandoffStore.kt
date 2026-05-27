package expo.modules.gooddisplayepaper.bridge

import android.nfc.Tag
import android.nfc.tech.IsoDep

/** Session-scoped IsoDep handoff between react-native-nfc-manager and the write pipeline. */
object NfcHandoffStore {

  @Volatile private var isoDep: IsoDep? = null

  @Volatile private var tag: Tag? = null

  @JvmStatic
  fun register(isoDep: IsoDep, tag: Tag) {
    this.isoDep = isoDep
    this.tag = tag
  }

  @JvmStatic
  fun peekTag(): Tag? = tag

  @JvmStatic
  fun peekIsoDep(): IsoDep? = isoDep

  @JvmStatic
  fun clear() {
    isoDep = null
    tag = null
  }
}

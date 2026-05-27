package expo.modules.gooddisplayepaper.bridge

import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.util.Log
import java.util.Locale

/**
 * Lightweight adapter to obtain the active {@link Tag} held by react-native-nfc-manager after
 * {@code NfcManager.requestTechnology(NfcTech.IsoDep)}.
 *
 * Uses reflection to avoid a compile-time dependency on nfc-manager.
 */
object NfcManagerBridge {

  private const val LOG_TAG = "GooddisplayEpaper"

  private val NFC_MANAGER_CLASS_NAMES =
      listOf(
          "com.reactnativecommunity.nfcmanager.NfcManager",
          "community.revteltech.nfc.NfcManager",
          "com.github.whitedust.reactnative_nfc_manager.NfcManager")

  private val TAG_FIELD_NAMES = listOf("mTag", "tag", "currentTag")

  /**
   * Resolve IsoDep from react-native-nfc-manager's in-memory Tag, optionally validating
   * {@code expectedTagIdHex}.
   */
  @JvmStatic
  fun openIsoDep(expectedTagIdHex: String?): IsoDep {
    val tag = resolveTagFromNfcManager()
        ?: throw NfcHandoffException(
            "No NFC tag available. Call registerNfcHandoff() after " +
                "NfcManager.requestTechnology(NfcTech.IsoDep).")

    if (!expectedTagIdHex.isNullOrBlank()) {
      val expected = normalizeHex(expectedTagIdHex)
      val actual = bytesToHex(tag.id)
      if (!actual.equals(expected, ignoreCase = true)) {
        throw NfcHandoffException(
            "Tag id mismatch: expected=$expected actual=$actual")
      }
    }

    val isoDep = IsoDep.get(tag)
        ?: throw NfcHandoffException("Tag does not expose IsoDep technology")

    NfcHandoffStore.register(isoDep, tag)
    return isoDep
  }

  @JvmStatic
  fun resolveTagFromNfcManager(): Tag? {
    for (className in NFC_MANAGER_CLASS_NAMES) {
      try {
        val clazz = Class.forName(className)
        for (fieldName in TAG_FIELD_NAMES) {
          try {
            val field = clazz.getDeclaredField(fieldName)
            field.isAccessible = true
            val instance = resolveModuleInstance(clazz) ?: continue
            val value = field.get(instance)
            if (value is Tag) {
              return value
            }
          } catch (_: NoSuchFieldException) {
            // try next field
          }
        }
      } catch (_: ClassNotFoundException) {
        // try next class
      }
    }
    return NfcHandoffStore.peekTag()
  }

  private fun resolveModuleInstance(clazz: Class<*>): Any? {
    return try {
      val getInstance = clazz.getMethod("getInstance")
      getInstance.invoke(null)
    } catch (_: Exception) {
      try {
        val reactContextField = clazz.getDeclaredField("reactContext")
        reactContextField.isAccessible = true
        for (field in clazz.declaredFields) {
          if (java.lang.reflect.Modifier.isStatic(field.modifiers) &&
              field.type == clazz) {
            field.isAccessible = true
            return field.get(null)
          }
        }
        null
      } catch (_: Exception) {
        Log.w(LOG_TAG, "Unable to resolve nfc-manager module instance for ${clazz.name}")
        null
      }
    }
  }

  private fun normalizeHex(hex: String): String {
    return hex.trim().removePrefix("0x").replace(" ", "").uppercase(Locale.US)
  }

  private fun bytesToHex(bytes: ByteArray): String {
    val sb = StringBuilder(bytes.size * 2)
    for (b in bytes) {
      sb.append(String.format("%02X", b.toInt() and 0xFF))
    }
    return sb.toString()
  }
}

class NfcHandoffException(message: String) : Exception(message)

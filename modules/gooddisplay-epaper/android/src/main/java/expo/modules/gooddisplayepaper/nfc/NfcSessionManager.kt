package expo.modules.gooddisplayepaper.nfc

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Build
import java.io.IOException
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Owns foreground NFC dispatch and suspends until an IsoDep tag is available.
 *
 * Lifecycle is driven by [GooddisplayEpaperModule] via Expo activity hooks and [awaitIsoDep].
 */
class NfcSessionManager {

  private val pendingContinuation = AtomicReference<CancellableContinuation<IsoDep>?>(null)
  private val waitingActivity = AtomicReference<Activity?>(null)
  private val foregroundDispatchEnabled = AtomicReference(false)

  suspend fun awaitIsoDep(activity: Activity): IsoDep =
      suspendCancellableCoroutine { continuation ->
        if (!pendingContinuation.compareAndSet(null, continuation)) {
          continuation.resumeWithException(
              IllegalStateException("An NFC tag wait is already in progress"))
          return@suspendCancellableCoroutine
        }

        waitingActivity.set(activity)
        continuation.invokeOnCancellation { cancelAwait("NFC wait cancelled") }

        val adapter = NfcAdapter.getDefaultAdapter(activity)
        if (adapter == null) {
          clearAwait()
          continuation.resumeWithException(NfcSessionException("NFC is not supported on this device"))
          return@suspendCancellableCoroutine
        }
        if (!adapter.isEnabled) {
          clearAwait()
          continuation.resumeWithException(NfcSessionException("NFC is disabled. Enable NFC in settings."))
          return@suspendCancellableCoroutine
        }

        enableForegroundDispatch(activity)
        resolveIsoDepFromIntent(activity.intent)?.let { isoDep ->
          resumeAwait(isoDep)
        }
      }

  fun onNewIntent(intent: Intent?) {
    val isoDep = resolveIsoDepFromIntent(intent) ?: return
    resumeAwait(isoDep)
  }

  fun onActivityResumed(activity: Activity) {
    if (waitingActivity.get() === activity && pendingContinuation.get() != null) {
      enableForegroundDispatch(activity)
    }
  }

  fun onActivityPaused(activity: Activity) {
    disableForegroundDispatch(activity)
    if (waitingActivity.get() === activity && pendingContinuation.get() != null) {
      cancelAwait("Activity backgrounded while waiting for NFC tag")
    }
  }

  /** Cancels an in-flight tag wait. Returns true if a wait was active. */
  fun cancel(): Boolean {
    return cancelAwait("NFC wait cancelled")
  }

  fun cleanup(activity: Activity) {
    disableForegroundDispatch(activity)
    clearAwait()
  }

  private fun resumeAwait(isoDep: IsoDep) {
    val activity = waitingActivity.getAndSet(null)
    if (activity != null) {
      disableForegroundDispatch(activity)
    }
    val continuation = pendingContinuation.getAndSet(null) ?: return
    continuation.resume(isoDep)
  }

  private fun cancelAwait(message: String): Boolean {
    val activity = waitingActivity.getAndSet(null)
    if (activity != null) {
      disableForegroundDispatch(activity)
    }
    val continuation = pendingContinuation.getAndSet(null) ?: return false
    continuation.resumeWithException(NfcSessionCancelledException(message))
    return true
  }

  private fun clearAwait() {
    waitingActivity.set(null)
    pendingContinuation.set(null)
  }

  private fun enableForegroundDispatch(activity: Activity) {
    if (foregroundDispatchEnabled.getAndSet(true)) {
      return
    }
    val adapter = NfcAdapter.getDefaultAdapter(activity) ?: return
    val pendingIntent = createPendingIntent(activity)
    val filters =
        arrayOf(
            IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED),
            IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED),
            IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED),
        )
    val techLists = arrayOf(arrayOf(IsoDep::class.java.name))
    adapter.enableForegroundDispatch(activity, pendingIntent, filters, techLists)
  }

  private fun disableForegroundDispatch(activity: Activity) {
    if (!foregroundDispatchEnabled.getAndSet(false)) {
      return
    }
    NfcAdapter.getDefaultAdapter(activity)?.disableForegroundDispatch(activity)
  }

  private fun createPendingIntent(activity: Activity): PendingIntent {
    val intent =
        Intent(activity, activity.javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
    val flags =
        PendingIntent.FLAG_UPDATE_CURRENT or
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
              PendingIntent.FLAG_MUTABLE
            } else {
              @Suppress("DEPRECATION") PendingIntent.FLAG_MUTABLE
            }
    return PendingIntent.getActivity(activity, 0, intent, flags)
  }

  private fun resolveIsoDepFromIntent(intent: Intent?): IsoDep? {
    if (intent == null) {
      return null
    }
    val action = intent.action ?: return null
    if (action != NfcAdapter.ACTION_TAG_DISCOVERED &&
        action != NfcAdapter.ACTION_TECH_DISCOVERED &&
        action != NfcAdapter.ACTION_NDEF_DISCOVERED) {
      return null
    }

    val tag: Tag =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
        } else {
          @Suppress("DEPRECATION") intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        } ?: return null

    if (!tag.techList.contains(IsoDep::class.java.name)) {
      return null
    }

    val isoDep = IsoDep.get(tag) ?: return null
    try {
      isoDep.timeout = LEGACY_TIMEOUT_MS
      if (!isoDep.isConnected) {
        isoDep.connect()
      }
    } catch (e: IOException) {
      throw NfcSessionException("Failed to connect IsoDep: ${e.message}", e)
    }
    return isoDep
  }

  companion object {
    private const val LEGACY_TIMEOUT_MS = 50_000
  }
}

class NfcSessionException(message: String, cause: Throwable? = null) : Exception(message, cause)

class NfcSessionCancelledException(message: String) : CancellationException(message)

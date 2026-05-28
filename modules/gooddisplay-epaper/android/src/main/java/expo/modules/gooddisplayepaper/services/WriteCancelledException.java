package expo.modules.gooddisplayepaper.services;

import expo.modules.gooddisplayepaper.models.WriteProgress;

/** Thrown when {@link WriteCancellation#cancel()} is invoked during an active write. */
public final class WriteCancelledException extends NfcWriteException {

  public WriteCancelledException(String message) {
    super(
        message,
        WriteProgress.Phase.FAILED,
        (Throwable) null
    );
  }
}

package expo.modules.gooddisplayepaper.services;

import java.util.concurrent.atomic.AtomicBoolean;

/** Cooperative write cancellation token (Checkpoint 4 bridge). */
public final class WriteCancellation {

  private final AtomicBoolean cancelled = new AtomicBoolean(false);

  public void cancel() {
    cancelled.set(true);
  }

  public boolean isCancelled() {
    return cancelled.get();
  }

  public void checkNotCancelled() throws WriteCancelledException {
    if (isCancelled()) {
      throw new WriteCancelledException("Write cancelled");
    }
  }
}

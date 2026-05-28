package expo.modules.gooddisplayepaper.services;

import expo.modules.gooddisplayepaper.models.WriteProgress;

import java.io.IOException;

/** Deterministic NFC write failure with phase + status word context. */
public class NfcWriteException extends IOException {

  private final WriteProgress.Phase phase;
  private final byte sw1;
  private final byte sw2;

  public NfcWriteException(String message, WriteProgress.Phase phase, byte sw1, byte sw2) {
    super(message);
    this.phase = phase;
    this.sw1 = sw1;
    this.sw2 = sw2;
  }

  public NfcWriteException(String message, WriteProgress.Phase phase, Throwable cause) {
    super(message, cause);
    this.phase = phase;
    this.sw1 = 0;
    this.sw2 = 0;
  }

  public NfcWriteException(String message, WriteProgress.Phase phase) {
    super(message);
    this.phase = phase;
    this.sw1 = 0;
    this.sw2 = 0;
  }

  public WriteProgress.Phase getPhase() {
    return phase;
  }

  public byte getSw1() {
    return sw1;
  }

  public byte getSw2() {
    return sw2;
  }
}

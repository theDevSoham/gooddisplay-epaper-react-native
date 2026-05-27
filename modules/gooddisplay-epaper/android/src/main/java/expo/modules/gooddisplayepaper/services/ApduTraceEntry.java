package expo.modules.gooddisplayepaper.services;

import expo.modules.gooddisplayepaper.models.WriteProgress;
import expo.modules.gooddisplayepaper.protocol.ApduPacket;

/** Single APDU transceive record for transport debugging. */
public final class ApduTraceEntry {

  private final long timestampMs;
  private final WriteProgress.Phase phase;
  private final ApduPacket.Kind kind;
  private final String commandHex;
  private final String responseHex;
  private final long durationMs;
  private final int attempt;
  private final byte sw1;
  private final byte sw2;

  public ApduTraceEntry(
      long timestampMs,
      WriteProgress.Phase phase,
      ApduPacket.Kind kind,
      String commandHex,
      String responseHex,
      long durationMs,
      int attempt,
      byte sw1,
      byte sw2) {
    this.timestampMs = timestampMs;
    this.phase = phase;
    this.kind = kind;
    this.commandHex = commandHex;
    this.responseHex = responseHex;
    this.durationMs = durationMs;
    this.attempt = attempt;
    this.sw1 = sw1;
    this.sw2 = sw2;
  }

  public long getTimestampMs() {
    return timestampMs;
  }

  public WriteProgress.Phase getPhase() {
    return phase;
  }

  public ApduPacket.Kind getKind() {
    return kind;
  }

  public String getCommandHex() {
    return commandHex;
  }

  public String getResponseHex() {
    return responseHex;
  }

  public long getDurationMs() {
    return durationMs;
  }

  public int getAttempt() {
    return attempt;
  }

  public byte getSw1() {
    return sw1;
  }

  public byte getSw2() {
    return sw2;
  }
}

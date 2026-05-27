package expo.modules.gooddisplayepaper.models;

/**
 * Mutable upload / write progress snapshot (Checkpoint 4 will surface via Expo events).
 */
public final class WriteProgress {

  public enum Phase {
    IDLE,
    CONNECTING,
    INIT,
    UPLOADING,
    REFRESHING,
    POLLING_BUSY,
    COMPLETE,
    FAILED
  }

  private Phase phase = Phase.IDLE;
  private int uploadPercent;
  private int packetsSent;
  private int packetsTotal;
  private int dePollCount;
  private String message;
  private byte lastSw1;
  private byte lastSw2;

  public Phase getPhase() {
    return phase;
  }

  public void setPhase(Phase phase) {
    this.phase = phase;
  }

  public int getUploadPercent() {
    return uploadPercent;
  }

  public void setUploadPercent(int uploadPercent) {
    this.uploadPercent = Math.max(0, Math.min(100, uploadPercent));
  }

  public int getPacketsSent() {
    return packetsSent;
  }

  public void setPacketsSent(int packetsSent) {
    this.packetsSent = packetsSent;
  }

  public int getPacketsTotal() {
    return packetsTotal;
  }

  public void setPacketsTotal(int packetsTotal) {
    this.packetsTotal = packetsTotal;
  }

  public int getDePollCount() {
    return dePollCount;
  }

  public void setDePollCount(int dePollCount) {
    this.dePollCount = dePollCount;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public byte getLastSw1() {
    return lastSw1;
  }

  public byte getLastSw2() {
    return lastSw2;
  }

  public void setStatusWord(byte sw1, byte sw2) {
    this.lastSw1 = sw1;
    this.lastSw2 = sw2;
  }

  public WriteProgress snapshot() {
    WriteProgress copy = new WriteProgress();
    copy.phase = phase;
    copy.uploadPercent = uploadPercent;
    copy.packetsSent = packetsSent;
    copy.packetsTotal = packetsTotal;
    copy.dePollCount = dePollCount;
    copy.message = message;
    copy.lastSw1 = lastSw1;
    copy.lastSw2 = lastSw2;
    return copy;
  }
}

package expo.modules.gooddisplayepaper.models;

import expo.modules.gooddisplayepaper.services.ApduTraceEntry;

import java.util.Collections;
import java.util.List;

/** Outcome of a completed {@link expo.modules.gooddisplayepaper.services.NfcEpaperWriter} run. */
public final class WriteResult {

  private final boolean success;
  private final WriteProgress.Phase finalPhase;
  private final int totalApduCount;
  private final int d2PacketCount;
  private final int dePollCount;
  private final long connectDurationMs;
  private final long initDurationMs;
  private final long uploadDurationMs;
  private final long refreshDurationMs;
  private final long busyWaitDurationMs;
  private final long totalDurationMs;
  private final byte finalSw1;
  private final byte finalSw2;
  private final int transceiveRetryCount;
  private final List<ApduTraceEntry> apduTrace;

  public WriteResult(
      boolean success,
      WriteProgress.Phase finalPhase,
      int totalApduCount,
      int d2PacketCount,
      int dePollCount,
      long connectDurationMs,
      long initDurationMs,
      long uploadDurationMs,
      long refreshDurationMs,
      long busyWaitDurationMs,
      long totalDurationMs,
      byte finalSw1,
      byte finalSw2,
      int transceiveRetryCount,
      List<ApduTraceEntry> apduTrace) {
    this.success = success;
    this.finalPhase = finalPhase;
    this.totalApduCount = totalApduCount;
    this.d2PacketCount = d2PacketCount;
    this.dePollCount = dePollCount;
    this.connectDurationMs = connectDurationMs;
    this.initDurationMs = initDurationMs;
    this.uploadDurationMs = uploadDurationMs;
    this.refreshDurationMs = refreshDurationMs;
    this.busyWaitDurationMs = busyWaitDurationMs;
    this.totalDurationMs = totalDurationMs;
    this.finalSw1 = finalSw1;
    this.finalSw2 = finalSw2;
    this.transceiveRetryCount = transceiveRetryCount;
    this.apduTrace = Collections.unmodifiableList(apduTrace);
  }

  public boolean isSuccess() {
    return success;
  }

  public WriteProgress.Phase getFinalPhase() {
    return finalPhase;
  }

  public int getTotalApduCount() {
    return totalApduCount;
  }

  public int getD2PacketCount() {
    return d2PacketCount;
  }

  public int getDePollCount() {
    return dePollCount;
  }

  public long getConnectDurationMs() {
    return connectDurationMs;
  }

  public long getInitDurationMs() {
    return initDurationMs;
  }

  public long getUploadDurationMs() {
    return uploadDurationMs;
  }

  public long getRefreshDurationMs() {
    return refreshDurationMs;
  }

  public long getBusyWaitDurationMs() {
    return busyWaitDurationMs;
  }

  public long getTotalDurationMs() {
    return totalDurationMs;
  }

  public byte getFinalSw1() {
    return finalSw1;
  }

  public byte getFinalSw2() {
    return finalSw2;
  }

  public int getTransceiveRetryCount() {
    return transceiveRetryCount;
  }

  public List<ApduTraceEntry> getApduTrace() {
    return apduTrace;
  }
}

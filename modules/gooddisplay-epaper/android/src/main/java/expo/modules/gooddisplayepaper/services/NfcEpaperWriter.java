package expo.modules.gooddisplayepaper.services;

import android.util.Log;

import expo.modules.gooddisplayepaper.models.WriteProgress;
import expo.modules.gooddisplayepaper.models.WriteResult;
import expo.modules.gooddisplayepaper.protocol.ApduBuilder;
import expo.modules.gooddisplayepaper.protocol.ApduPacket;
import expo.modules.gooddisplayepaper.protocol.ApduPacket.Kind;

import java.io.IOException;
import java.util.List;

/**
 * Headless NFC write orchestration — legacy {@code activity_imageview.writeTag} sequencing without
 * UI or Activity coupling.
 */
public final class NfcEpaperWriter {

  private static final String LOG_TAG = "GooddisplayEpaper";

  private NfcEpaperWriter() {}

  /**
   * Execute full flash: IC DIY → DB → DA → D2* → D4 → legacy refresh wait (no DE).
   *
   * @return metrics + APDU trace on success
   */
  public static WriteResult write(WriteSession session) throws IOException {
    if (session == null) {
      throw new IllegalArgumentException("session is required");
    }

    long totalStart = System.nanoTime();
    long connectMs = 0;
    long initMs = 0;
    long uploadMs = 0;
    long refreshMs = 0;
    long busyMs = 0;

    ApduTransceiver transceiver = session.getTransceiver();
    WriteProgress progress = session.getProgress();
    WriteOptions options = session.getOptions();
    int d2Count = 0;

    session.markStarted();
    try {
      session.checkNotCancelled();
      session.notifyPhase(WriteProgress.Phase.CONNECTING);
      transceiver.setTracePhase(WriteProgress.Phase.CONNECTING);
      long t0 = System.nanoTime();
      transceiver.connect();
      connectMs = (System.nanoTime() - t0) / 1_000_000L;

      session.checkNotCancelled();
      session.notifyPhase(WriteProgress.Phase.INIT);
      transceiver.setTracePhase(WriteProgress.Phase.INIT);
      t0 = System.nanoTime();
      executeInit(session);
      initMs = (System.nanoTime() - t0) / 1_000_000L;

      session.checkNotCancelled();
      session.notifyPhase(WriteProgress.Phase.UPLOADING);
      transceiver.setTracePhase(WriteProgress.Phase.UPLOADING);
      t0 = System.nanoTime();
      d2Count = executeUpload(session);
      uploadMs = (System.nanoTime() - t0) / 1_000_000L;

      session.checkNotCancelled();
      session.notifyPhase(WriteProgress.Phase.REFRESHING);
      transceiver.setTracePhase(WriteProgress.Phase.REFRESHING);
      t0 = System.nanoTime();
      executeRefresh(session);
      refreshMs = (System.nanoTime() - t0) / 1_000_000L;

      session.checkNotCancelled();
      session.notifyPhase(WriteProgress.Phase.POLLING_BUSY);
      transceiver.setTracePhase(WriteProgress.Phase.POLLING_BUSY);
      t0 = System.nanoTime();
      waitForLegacyRefresh(session);
      busyMs = (System.nanoTime() - t0) / 1_000_000L;

      progress.setPhase(WriteProgress.Phase.COMPLETE);
      progress.setUploadPercent(100);
      progress.setMessage("E-paper refresh complete");
      session.notifyPhase(WriteProgress.Phase.COMPLETE);
      session.notifyProgress();

      long totalMs = (System.nanoTime() - totalStart) / 1_000_000L;
      if (options.isApduTraceLogging()) {
        Log.i(
            LOG_TAG,
            "write complete apdu="
                + transceiver.getTrace().size()
                + " d2="
                + d2Count
                + " dePolls="
                + progress.getDePollCount()
                + " totalMs="
                + totalMs);
      }

      return new WriteResult(
          true,
          WriteProgress.Phase.COMPLETE,
          transceiver.getTrace().size(),
          d2Count,
          progress.getDePollCount(),
          connectMs,
          initMs,
          uploadMs,
          refreshMs,
          busyMs,
          totalMs,
          progress.getLastSw1(),
          progress.getLastSw2(),
          transceiver.getTransceiveRetryCount(),
          transceiver.getTrace());

    } catch (IOException e) {
      progress.setPhase(WriteProgress.Phase.FAILED);
      progress.setMessage(e.getMessage());
      session.notifyPhase(WriteProgress.Phase.FAILED);
      session.notifyProgress();
      if (options.isApduTraceLogging()) {
        Log.e(LOG_TAG, "write failed: " + e.getMessage(), e);
      }
      throw e;
    } finally {
      if (options.isCloseIsoDepOnComplete()) {
        transceiver.closeQuietly();
      }
    }
  }

  private static void executeInit(WriteSession session) throws IOException {
    ApduTransceiver transceiver = session.getTransceiver();
    WriteOptions options = session.getOptions();
    List<ApduPacket> init = session.getInitSequence();

    for (int i = 0; i < init.size(); i++) {
      ApduPacket packet = init.get(i);
      byte[] response = transceiver.transceive(packet);
      if (packet.getKind() == Kind.SCREEN_TYPE && options.isRequireDaSuccess()) {
        validateLegacySuccess(response, WriteProgress.Phase.INIT, "DA screen type");
      }
      if (i < init.size() - 1) {
        transceiver.delayInit();
      }
    }
  }

  private static int executeUpload(WriteSession session) throws IOException {
    ApduTransceiver transceiver = session.getTransceiver();
    WriteProgress progress = session.getProgress();
    List<ApduPacket> uploads = session.getUploadSequence();
    int total = uploads.size();
    int sent = 0;
    int d2Count = 0;

    for (ApduPacket packet : uploads) {
      session.checkNotCancelled();
      transceiver.transceive(packet);
      sent++;
      d2Count++;
      progress.setPacketsSent(sent);
      if (total > 0) {
        int percent = (int) ((sent * 100L) / total);
        progress.setUploadPercent(percent);
      }
      session.notifyProgress();
    }
    progress.setUploadPercent(100);
    return d2Count;
  }

  private static void executeRefresh(WriteSession session) throws IOException {
    ApduPacket refresh = session.getRefreshPacket();
    byte[] response = session.getTransceiver().transceive(refresh);
    if (session.getOptions().isRequireD4Success()) {
      validateLegacySuccess(response, WriteProgress.Phase.REFRESHING, "D4 refresh");
    }
  }

  /**
   * Legacy {@code startRefreshMonitoring} parity: fixed delay after D4, 100 ms ticks, no DE APDU.
   *
   * <p>Reference: {@code activity_imageview.java} lines 904–957.
   */
  private static void waitForLegacyRefresh(WriteSession session) throws IOException {
    ApduTransceiver transceiver = session.getTransceiver();
    WriteProgress progress = session.getProgress();
    WriteOptions options = session.getOptions();
    int colorMode = session.getScreenConfig().getColorMode();
    int waitMs = options.getLegacyRefreshWaitMs(colorMode);
    int interval = options.getBusyPollIntervalMs();
    if (interval <= 0) {
      interval = 100;
    }

    long deadline = System.currentTimeMillis() + waitMs;
    int ticks = 0;

    if (options.isApduTraceLogging()) {
      Log.d(LOG_TAG, "legacy refresh wait " + waitMs + "ms (colorMode=" + colorMode + ")");
    }

    while (System.currentTimeMillis() < deadline) {
      session.checkNotCancelled();
      if (!transceiver.isConnected()) {
        throw new NfcWriteException(
            "NFC connection lost during refresh wait",
            WriteProgress.Phase.POLLING_BUSY,
            (Throwable) null);
      }
      ticks++;
      progress.setDePollCount(ticks);
      session.notifyProgress();

      long remaining = deadline - System.currentTimeMillis();
      if (remaining <= 0) {
        break;
      }
      transceiver.sleep((int) Math.min(interval, remaining));
    }
  }

  private static void validateLegacySuccess(byte[] response, WriteProgress.Phase phase, String step)
      throws NfcWriteException {
    if (!ApduResponse.isLegacySuccess(response)) {
      throw new NfcWriteException(
          step + " failed: expected SW1=90, got "
              + String.format("%02X", ApduResponse.sw1(response) & 0xFF),
          phase,
          ApduResponse.sw1(response),
          ApduResponse.sw2(response));
    }
  }
}

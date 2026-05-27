package expo.modules.gooddisplayepaper.services;

import android.nfc.tech.IsoDep;

import expo.modules.gooddisplayepaper.models.ProcessedImage;
import expo.modules.gooddisplayepaper.models.ScreenConfig;
import expo.modules.gooddisplayepaper.models.WriteProgress;
import expo.modules.gooddisplayepaper.protocol.ApduBuilder;
import expo.modules.gooddisplayepaper.protocol.ApduPacket;

import java.util.Collections;
import java.util.List;

/**
 * Per-flash session state — replaces legacy Activity fields ({@code isodep}, {@code dataNum}, etc.).
 */
public final class WriteSession {

  private final ScreenConfig screenConfig;
  private final ProcessedImage processedImage;
  private final WriteOptions options;
  private final WriteProgress progress;
  private final WriteProgressListener progressListener;
  private final ApduTransceiver transceiver;
  private final long createdAtMs;

  private final List<ApduPacket> initSequence;
  private final List<ApduPacket> uploadSequence;
  private final ApduPacket refreshPacket;

  private long startedAtMs;
  private int retryGeneration;
  private final WriteCancellation cancellation;

  private WriteSession(
      ScreenConfig screenConfig,
      ProcessedImage processedImage,
      WriteOptions options,
      WriteProgress progress,
      WriteProgressListener progressListener,
      ApduTransceiver transceiver,
      WriteCancellation cancellation) {
    this.screenConfig = screenConfig;
    this.processedImage = processedImage;
    this.options = options;
    this.progress = progress;
    this.progressListener = progressListener;
    this.transceiver = transceiver;
    this.cancellation = cancellation != null ? cancellation : new WriteCancellation();
    this.createdAtMs = System.currentTimeMillis();
    this.initSequence = ApduBuilder.buildInitSequence(screenConfig);
    this.uploadSequence = ApduBuilder.buildImageUploadSequence(screenConfig, processedImage);
    this.refreshPacket = ApduBuilder.buildRefresh(screenConfig.getColorMode());
    this.progress.setPacketsTotal(uploadSequence.size());
  }

  public static WriteSession create(
      IsoDep isoDep, ScreenConfig screenConfig, ProcessedImage processedImage) {
    return create(isoDep, screenConfig, processedImage, WriteOptions.DEFAULT, null, null);
  }

  public static WriteSession create(
      IsoDep isoDep,
      ScreenConfig screenConfig,
      ProcessedImage processedImage,
      WriteOptions options,
      WriteProgressListener listener) {
    return create(isoDep, screenConfig, processedImage, options, listener, null);
  }

  public static WriteSession create(
      IsoDep isoDep,
      ScreenConfig screenConfig,
      ProcessedImage processedImage,
      WriteOptions options,
      WriteProgressListener listener,
      WriteCancellation cancellation) {
    if (isoDep == null) {
      throw new IllegalArgumentException("isoDep is required");
    }
    if (screenConfig == null) {
      throw new IllegalArgumentException("screenConfig is required");
    }
    if (processedImage == null) {
      throw new IllegalArgumentException("processedImage is required");
    }
    WriteOptions resolved = options != null ? options : WriteOptions.DEFAULT;
    WriteProgress progress = new WriteProgress();
    ApduTransceiver transceiver =
        new ApduTransceiver(new IsoDepTransceiveChannel(isoDep), resolved, progress);
    return new WriteSession(
        screenConfig, processedImage, resolved, progress, listener, transceiver, cancellation);
  }

  /** Test / harness entry: inject a {@link TransceiveChannel} fake. */
  public static WriteSession createWithChannel(
      TransceiveChannel channel,
      ScreenConfig screenConfig,
      ProcessedImage processedImage,
      WriteOptions options) {
    WriteOptions resolved = options != null ? options : WriteOptions.DEFAULT;
    WriteProgress progress = new WriteProgress();
    ApduTransceiver transceiver = new ApduTransceiver(channel, resolved, progress);
    return new WriteSession(
        screenConfig, processedImage, resolved, progress, null, transceiver, null);
  }

  public WriteCancellation getCancellation() {
    return cancellation;
  }

  public void checkNotCancelled() throws WriteCancelledException {
    cancellation.checkNotCancelled();
  }

  public ScreenConfig getScreenConfig() {
    return screenConfig;
  }

  public ProcessedImage getProcessedImage() {
    return processedImage;
  }

  public WriteOptions getOptions() {
    return options;
  }

  public WriteProgress getProgress() {
    return progress;
  }

  public ApduTransceiver getTransceiver() {
    return transceiver;
  }

  public List<ApduPacket> getInitSequence() {
    return Collections.unmodifiableList(initSequence);
  }

  public List<ApduPacket> getUploadSequence() {
    return Collections.unmodifiableList(uploadSequence);
  }

  public ApduPacket getRefreshPacket() {
    return refreshPacket;
  }

  public long getCreatedAtMs() {
    return createdAtMs;
  }

  public long getStartedAtMs() {
    return startedAtMs;
  }

  void markStarted() {
    this.startedAtMs = System.currentTimeMillis();
  }

  public int getRetryGeneration() {
    return retryGeneration;
  }

  public void incrementRetryGeneration() {
    retryGeneration++;
  }

  void notifyPhase(WriteProgress.Phase phase) {
    progress.setPhase(phase);
    if (progressListener != null) {
      progressListener.onPhaseChanged(phase, progress.snapshot());
    }
  }

  void notifyProgress() {
    if (progressListener != null) {
      progressListener.onProgress(progress.snapshot());
    }
  }
}

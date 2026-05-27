package expo.modules.gooddisplayepaper.services;

import expo.modules.gooddisplayepaper.models.ColorMode;
import expo.modules.gooddisplayepaper.protocol.ApduConstants;

/** Transport tuning — defaults favor legacy parity with safer busy caps. */
public final class WriteOptions {

  public static final WriteOptions DEFAULT = new Builder().build();

  private final int isoDepTimeoutMs;
  private final int initDelayMs;
  private final int busyPollIntervalMs;
  private final int busyTimeoutMs;
  private final int transceiveMaxRetries;
  private final boolean requireDaSuccess;
  private final boolean requireD4Success;
  private final boolean apduTraceLogging;
  private final boolean closeIsoDepOnComplete;

  private WriteOptions(Builder builder) {
    this.isoDepTimeoutMs = builder.isoDepTimeoutMs;
    this.initDelayMs = builder.initDelayMs;
    this.busyPollIntervalMs = builder.busyPollIntervalMs;
    this.busyTimeoutMs = builder.busyTimeoutMs;
    this.transceiveMaxRetries = builder.transceiveMaxRetries;
    this.requireDaSuccess = builder.requireDaSuccess;
    this.requireD4Success = builder.requireD4Success;
    this.apduTraceLogging = builder.apduTraceLogging;
    this.closeIsoDepOnComplete = builder.closeIsoDepOnComplete;
  }

  public int getIsoDepTimeoutMs() {
    return isoDepTimeoutMs;
  }

  public int getInitDelayMs() {
    return initDelayMs;
  }

  public int getBusyPollIntervalMs() {
    return busyPollIntervalMs;
  }

  public int getBusyTimeoutMs() {
    return busyTimeoutMs;
  }

  public int getTransceiveMaxRetries() {
    return transceiveMaxRetries;
  }

  public boolean isRequireDaSuccess() {
    return requireDaSuccess;
  }

  public boolean isRequireD4Success() {
    return requireD4Success;
  }

  public boolean isApduTraceLogging() {
    return apduTraceLogging;
  }

  public boolean isCloseIsoDepOnComplete() {
    return closeIsoDepOnComplete;
  }

  /** Legacy timer caps used as DE poll safety timeout per color mode. */
  public static int defaultBusyTimeoutMs(int colorMode) {
    switch (colorMode) {
      case ColorMode.MONO:
        return 5_000;
      case ColorMode.TRI:
        return 20_000;
      case ColorMode.QUAD:
        return 25_000;
      default:
        return 25_000;
    }
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private int isoDepTimeoutMs = ApduConstants.ISODEP_TIMEOUT_MS;
    private int initDelayMs = ApduConstants.INIT_DELAY_MS;
    private int busyPollIntervalMs = 100;
    private int busyTimeoutMs = 25_000;
    private int transceiveMaxRetries = 1;
    private boolean requireDaSuccess = true;
    private boolean requireD4Success = true;
    private boolean apduTraceLogging = true;
    private boolean closeIsoDepOnComplete = true;

    public Builder isoDepTimeoutMs(int ms) {
      this.isoDepTimeoutMs = ms;
      return this;
    }

    public Builder initDelayMs(int ms) {
      this.initDelayMs = ms;
      return this;
    }

    public Builder busyPollIntervalMs(int ms) {
      this.busyPollIntervalMs = ms;
      return this;
    }

    public Builder busyTimeoutMs(int ms) {
      this.busyTimeoutMs = ms;
      return this;
    }

    public Builder busyTimeoutForColorMode(int colorMode) {
      this.busyTimeoutMs = defaultBusyTimeoutMs(colorMode);
      return this;
    }

    public Builder transceiveMaxRetries(int retries) {
      this.transceiveMaxRetries = retries;
      return this;
    }

    public Builder requireDaSuccess(boolean require) {
      this.requireDaSuccess = require;
      return this;
    }

    public Builder requireD4Success(boolean require) {
      this.requireD4Success = require;
      return this;
    }

    public Builder apduTraceLogging(boolean enabled) {
      this.apduTraceLogging = enabled;
      return this;
    }

    public Builder closeIsoDepOnComplete(boolean close) {
      this.closeIsoDepOnComplete = close;
      return this;
    }

    public WriteOptions build() {
      return new WriteOptions(this);
    }
  }
}

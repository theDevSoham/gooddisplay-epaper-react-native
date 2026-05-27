package expo.modules.gooddisplayepaper.models;

import expo.modules.gooddisplayepaper.protocol.ApduConstants;

/**
 * Immutable panel + protocol configuration replacing legacy mutable {@code epdArray}.
 */
public final class ScreenConfig {

  /** Vertical scan with mirrored X (legacy {@code GetPictureData_SSD}). */
  public enum ScanDirection {
    VERTICAL_MIRROR_LEGACY
  }

  private final int width;
  private final int height;
  private final int inchCode;
  private final int colorMode;
  private final int icModel;
  private final byte[] driverFlow;
  private final byte[] screenTypeCommand;
  private final ScanDirection scanDirection;
  private final int protocolFlags;

  private ScreenConfig(
      int width,
      int height,
      int inchCode,
      int colorMode,
      int icModel,
      byte[] driverFlow,
      byte[] screenTypeCommand,
      ScanDirection scanDirection,
      int protocolFlags) {
    this.width = width;
    this.height = height;
    this.inchCode = inchCode;
    this.colorMode = colorMode;
    this.icModel = icModel;
    this.driverFlow = driverFlow;
    this.screenTypeCommand = screenTypeCommand;
    this.scanDirection = scanDirection;
    this.protocolFlags = protocolFlags;
  }

  /** Native width from {@code epdArray} (before &gt;= 4.2&quot; processing swap). */
  public int getWidth() {
    return width;
  }

  /** Native height from {@code epdArray}. */
  public int getHeight() {
    return height;
  }

  public int getInchCode() {
    return inchCode;
  }

  public int getColorMode() {
    return colorMode;
  }

  /** Legacy {@code epdArray[5]} — SSD series = 2. */
  public int getIcModel() {
    return icModel;
  }

  /** DB driver flow APDU payload (legacy {@code setEpdInit()[0]}). */
  public byte[] getDriverConfig() {
    return driverFlow;
  }

  /** DA set screen type APDU (legacy {@code setEpdInit()[1]}). */
  public byte[] getScreenTypeCommand() {
    return screenTypeCommand;
  }

  public ScanDirection getScanDirection() {
    return scanDirection;
  }

  /** Bit flags — see {@link ProtocolFlags}. */
  public int getProtocolFlags() {
    return protocolFlags;
  }

  /**
   * Width used for image scaling ({@code activity_imageview} swaps when {@code inchCode >= 420}).
   */
  public int getProcessingWidth() {
    if (inchCode >= 420) {
      return height;
    }
    return width;
  }

  public int getProcessingHeight() {
    if (inchCode >= 420) {
      return width;
    }
    return height;
  }

  public ImageProcessingConfig toImageProcessingConfig() {
    return new ImageProcessingConfig(
        getProcessingWidth(), getProcessingHeight(), inchCode, colorMode);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class ProtocolFlags {
    public static final int NONE = 0;
    /** 3.7&quot; mono: 0xFF BW pass then data on red plane. */
    public static final int MONO_370_DUAL_PASS = 1 << 0;
    /** 4.2&quot; mono: BW + inverted red plane (tri-color style). */
    public static final int MONO_420_DUAL_PASS = 1 << 1;
    /** Tri-color and 4.2&quot; mono red plane uses {@code 0xFF - byte} on wire. */
    public static final int INVERT_RED_ON_UPLOAD = 1 << 2;

    private ProtocolFlags() {}
  }

  public static final class Builder {
    private int width;
    private int height;
    private int inchCode;
    private int colorMode;
    private int icModel = 2;
    private byte[] driverFlow;
    private byte[] screenTypeCommand;
    private ScanDirection scanDirection = ScanDirection.VERTICAL_MIRROR_LEGACY;
    private int protocolFlags = ProtocolFlags.NONE;

    public Builder width(int width) {
      this.width = width;
      return this;
    }

    public Builder height(int height) {
      this.height = height;
      return this;
    }

    public Builder inchCode(int inchCode) {
      this.inchCode = inchCode;
      return this;
    }

    public Builder colorMode(int colorMode) {
      this.colorMode = colorMode;
      return this;
    }

    public Builder icModel(int icModel) {
      this.icModel = icModel;
      return this;
    }

    public Builder driverFlow(byte[] driverFlow) {
      this.driverFlow = driverFlow;
      return this;
    }

    public Builder screenTypeCommand(byte[] screenTypeCommand) {
      this.screenTypeCommand = screenTypeCommand;
      return this;
    }

    public Builder scanDirection(ScanDirection scanDirection) {
      this.scanDirection = scanDirection;
      return this;
    }

    public Builder protocolFlags(int protocolFlags) {
      this.protocolFlags = protocolFlags;
      return this;
    }

    public ScreenConfig build() {
      if (!ColorMode.isValid(colorMode)) {
        throw new IllegalArgumentException("Invalid color mode: " + colorMode);
      }
      if (driverFlow == null || driverFlow.length == 0) {
        throw new IllegalArgumentException("driverFlow is required");
      }
      if (screenTypeCommand == null || screenTypeCommand.length == 0) {
        throw new IllegalArgumentException("screenTypeCommand is required");
      }
      if (driverFlow[0] != ApduConstants.VENDOR_PREFIX) {
        throw new IllegalArgumentException("driverFlow must start with F0");
      }
      if (screenTypeCommand[0] != ApduConstants.VENDOR_PREFIX) {
        throw new IllegalArgumentException("screenTypeCommand must start with F0");
      }
      return new ScreenConfig(
          width,
          height,
          inchCode,
          colorMode,
          icModel,
          driverFlow,
          screenTypeCommand,
          scanDirection,
          protocolFlags);
    }
  }
}

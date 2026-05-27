package expo.modules.gooddisplayepaper.protocol;

import expo.modules.gooddisplayepaper.models.ColorMode;
import expo.modules.gooddisplayepaper.models.ScreenConfig;

/**
 * Immutable screen configuration factory — replaces legacy {@code data.setEpdInit} + {@code epdArray}.
 */
public final class EpdConfigFactory {

  private EpdConfigFactory() {}

  /**
   * Create {@link ScreenConfig} for inch + color mode using legacy {@code setEpdInit} hex tables.
   */
  public static ScreenConfig create(int colorMode, int inchCode) {
    PanelGeometry geometry = PanelGeometry.forInch(inchCode);
    if (geometry == null) {
      throw new IllegalArgumentException("Unsupported inch code: " + inchCode);
    }

    String[] init = LegacyEpdInit.setEpdInit(colorMode, inchCode);
    if (init[0] == null || init[1] == null || init[0].isEmpty() || init[1].isEmpty()) {
      throw new IllegalArgumentException(
          "No protocol init for color=" + colorMode + " inch=" + inchCode);
    }

    int flags = resolveProtocolFlags(colorMode, inchCode);

    return ScreenConfig.builder()
        .width(geometry.width)
        .height(geometry.height)
        .inchCode(inchCode)
        .colorMode(colorMode)
        .icModel(geometry.icModel)
        .driverFlow(HexCodec.hexStringToBytes(init[0]))
        .screenTypeCommand(HexCodec.hexStringToBytes(init[1]))
        .protocolFlags(flags)
        .build();
  }

  /** 2.13&quot; monochrome pilot (migration plan). */
  public static ScreenConfig pilot213Mono() {
    return create(ColorMode.MONO, 213);
  }

  public static boolean supports(int colorMode, int inchCode) {
    if (PanelGeometry.forInch(inchCode) == null) {
      return false;
    }
    try {
      String[] init = LegacyEpdInit.setEpdInit(colorMode, inchCode);
      return init[0] != null && init[1] != null && !init[0].isEmpty() && !init[1].isEmpty();
    } catch (Exception e) {
      return false;
    }
  }

  private static int resolveProtocolFlags(int colorMode, int inchCode) {
    int flags = ScreenConfig.ProtocolFlags.NONE;
    if (colorMode == ColorMode.MONO && inchCode == 370) {
      flags |= ScreenConfig.ProtocolFlags.MONO_370_DUAL_PASS;
    }
    if (colorMode == ColorMode.MONO && inchCode == 420) {
      flags |= ScreenConfig.ProtocolFlags.MONO_420_DUAL_PASS;
      flags |= ScreenConfig.ProtocolFlags.INVERT_RED_ON_UPLOAD;
    }
    if (colorMode == ColorMode.TRI) {
      flags |= ScreenConfig.ProtocolFlags.INVERT_RED_ON_UPLOAD;
    }
    return flags;
  }

  /** Panel geometry from {@code MainActivity} button handlers. */
  private static final class PanelGeometry {
    final int width;
    final int height;
    final int icModel;

    PanelGeometry(int width, int height, int icModel) {
      this.width = width;
      this.height = height;
      this.icModel = icModel;
    }

    static PanelGeometry forInch(int inch) {
      switch (inch) {
        case 97:
          return new PanelGeometry(88, 184, 2);
        case 153:
          return new PanelGeometry(152, 152, 2);
        case 154:
          return new PanelGeometry(200, 200, 2);
        case 213:
          return new PanelGeometry(128, 250, 2);
        case 266:
          return new PanelGeometry(152, 296, 2);
        case 267:
          return new PanelGeometry(184, 360, 2);
        case 270:
          return new PanelGeometry(176, 264, 2);
        case 290:
          return new PanelGeometry(128, 296, 2);
        case 291:
          return new PanelGeometry(168, 384, 2);
        case 370:
          return new PanelGeometry(240, 416, 2);
        case 420:
          return new PanelGeometry(400, 300, 2);
        default:
          return null;
      }
    }
  }
}

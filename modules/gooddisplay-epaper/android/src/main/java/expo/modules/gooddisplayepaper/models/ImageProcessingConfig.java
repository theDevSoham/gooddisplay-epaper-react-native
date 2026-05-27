package expo.modules.gooddisplayepaper.models;

/**
 * Panel parameters for image packing (from legacy {@code epdArray} + {@code MainActivity}).
 *
 * <p>{@code panelWidth} / {@code panelHeight} match legacy {@code epdWidth}/{@code epdHeight}
 * after the &gt;= 4.2&quot; swap applied in {@code activity_imageview.onCreate}.
 */
public final class ImageProcessingConfig {

  private final int panelWidth;
  private final int panelHeight;
  private final int epdInch;
  private final int colorMode;

  public ImageProcessingConfig(int panelWidth, int panelHeight, int epdInch, int colorMode) {
    if (!ColorMode.isValid(colorMode)) {
      throw new IllegalArgumentException("Invalid color mode: " + colorMode);
    }
    if (panelWidth <= 0 || panelHeight <= 0) {
      throw new IllegalArgumentException("Panel dimensions must be positive");
    }
    this.panelWidth = panelWidth;
    this.panelHeight = panelHeight;
    this.epdInch = epdInch;
    this.colorMode = colorMode;
  }

  public int getPanelWidth() {
    return panelWidth;
  }

  public int getPanelHeight() {
    return panelHeight;
  }

  public int getEpdInch() {
    return epdInch;
  }

  public int getColorMode() {
    return colorMode;
  }

  /**
   * Legacy: if {@code epdInch >= 420}, width/height from {@code epdArray} are swapped before scaling.
   */
  public static ImageProcessingConfig fromEpdArray(
      int wHigh, int wLow, int hHigh, int hLow, int colorMode, int epdInch) {
    int width = wHigh * 256 + wLow;
    int height = hHigh * 256 + hLow;
    if (epdInch >= 420) {
      int tmp = width;
      width = height;
      height = tmp;
    }
    return new ImageProcessingConfig(width, height, epdInch, colorMode);
  }

  /** 2.13&quot; monochrome pilot panel ({@code MainActivity} / migration plan). */
  public static ImageProcessingConfig pilot213Mono() {
    return fromEpdArray(0, 128, 0, 250, ColorMode.MONO, 213);
  }
}

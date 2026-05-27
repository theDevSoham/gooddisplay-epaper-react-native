package expo.modules.gooddisplayepaper.models;

/**
 * Packed e-paper image buffers after rotation and vertical scan.
 *
 * <p>{@code width} / {@code height} are the rotated bitmap dimensions used for packing
 * (legacy {@code width0} / {@code height0}).
 */
public final class ProcessedImage {

  private final byte[] blackLayer;
  private final byte[] colorLayer;
  private final int width;
  private final int height;
  private final int colorMode;

  public ProcessedImage(
      byte[] blackLayer, byte[] colorLayer, int width, int height, int colorMode) {
    this.blackLayer = blackLayer;
    this.colorLayer = colorLayer;
    this.width = width;
    this.height = height;
    this.colorMode = colorMode;
  }

  /** Monochrome or BW plane for tri-color / quad black data. */
  public byte[] getBlackLayer() {
    return blackLayer;
  }

  /**
   * Red/chroma plane for tri-color ({@code GetPictureData_SSD(1)}). {@code null} for mono and quad.
   */
  public byte[] getColorLayer() {
    return colorLayer;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  /** Legacy {@code epdColor}: 2 = mono, 3 = tri, 4 = quad. */
  public int getColorMode() {
    return colorMode;
  }

  /** SSD: {@code width * height / 8}. Quad: {@code width * height / 4}. */
  public int getExpectedBlackLayerLength() {
    if (colorMode == ColorMode.QUAD) {
      return (width * height) / 4;
    }
    return (width * height) / 8;
  }
}

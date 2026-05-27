package expo.modules.gooddisplayepaper.models;

/** Legacy {@code epdArray[4]} color mode values. */
public final class ColorMode {

  public static final int MONO = 2;
  public static final int TRI = 3;
  public static final int QUAD = 4;

  private ColorMode() {}

  public static boolean isValid(int colorMode) {
    return colorMode == MONO || colorMode == TRI || colorMode == QUAD;
  }
}

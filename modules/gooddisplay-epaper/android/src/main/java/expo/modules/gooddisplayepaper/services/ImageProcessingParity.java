package expo.modules.gooddisplayepaper.services;

import expo.modules.gooddisplayepaper.models.ProcessedImage;

import java.util.Arrays;

/**
 * Deterministic helpers to validate extracted image processing against legacy expectations.
 */
public final class ImageProcessingParity {

  private ImageProcessingParity() {}

  public static int expectedSsdBufferLength(int width, int height) {
    return (width * height) / 8;
  }

  public static int expectedQuadBufferLength(int width, int height) {
    return (width * height) / 4;
  }

  /**
   * After legacy 90° rotation, packed dimensions are swapped vs panel size.
   */
  public static int rotatedWidth(int panelWidth, int panelHeight) {
    return panelHeight;
  }

  public static int rotatedHeight(int panelWidth, int panelHeight) {
    return panelWidth;
  }

  public static boolean validateProcessedImage(ProcessedImage image) {
    if (image == null) {
      return false;
    }
    if (image.getWidth() <= 0 || image.getHeight() <= 0) {
      return false;
    }
    int expectedBlack = image.getExpectedBlackLayerLength();
    if (image.getBlackLayer() == null || image.getBlackLayer().length != expectedBlack) {
      return false;
    }
    switch (image.getColorMode()) {
      case 2:
        return image.getColorLayer() == null;
      case 3:
        return image.getColorLayer() != null && image.getColorLayer().length == expectedBlack;
      case 4:
        return image.getColorLayer() == null;
      default:
        return false;
    }
  }

  public static boolean buffersEqual(byte[] expected, byte[] actual) {
    return Arrays.equals(expected, actual);
  }

  public static boolean buffersEqualPrefix(byte[] expected, byte[] actual, int length) {
    if (expected == null || actual == null) {
      return false;
    }
    if (expected.length < length || actual.length < length) {
      return false;
    }
    for (int i = 0; i < length; i++) {
      if (expected[i] != actual[i]) {
        return false;
      }
    }
    return true;
  }

  /**
   * Reference implementation mirroring legacy {@code GetPictureData_SSD} loop structure for tests.
   */
  public static byte[] legacyReferencePackSsd(
      android.graphics.Bitmap bitmap, int mode, int width0, int height0) {
    int bufferLength = (width0 * height0) / 8;
    byte[] imageBuffer = new byte[bufferLength];
    int index = 0;
    for (int i = width0 - 1; i >= 0; i--) {
      for (int j = 0; j <= height0 / 8 - 1; j++) {
        byte temp = 0;
        for (int k = 0; k < 8; k++) {
          temp = (byte) (temp * 2);
          int pixel = bitmap.getPixel(i, (j * 8) + k);
          int r = (pixel & 0xff0000) >> 16;
          int g = (pixel & 0xff00) >> 8;
          int b = (pixel & 0xff);
          if (mode == 0) {
            if (r <= 100 && g <= 100 && b <= 100) {
              temp = (byte) (temp + 0);
            } else {
              temp = (byte) (temp + 1);
            }
          } else {
            if (r >= 100 && g <= 100 && b <= 100) {
              temp = (byte) (temp + 0);
            } else {
              temp = (byte) (temp + 1);
            }
          }
        }
        imageBuffer[index] = temp;
        index++;
      }
    }
    return imageBuffer;
  }
}

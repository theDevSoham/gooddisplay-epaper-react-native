package expo.modules.gooddisplayepaper.services;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.util.Base64;

import expo.modules.gooddisplayepaper.models.ColorMode;
import expo.modules.gooddisplayepaper.models.ImageProcessingConfig;
import expo.modules.gooddisplayepaper.models.ProcessedImage;

import java.io.IOException;
import java.io.InputStream;

/**
 * Headless image processing extracted from {@code data/activity_imageview.java}.
 *
 * <p>No NFC, Activity, or UI dependencies.
 */
public final class ImageProcessor {

  /** Legacy RGB threshold for black pixels in SSD BW mode. */
  public static final int SSD_BLACK_THRESHOLD = 100;

  /** Legacy RGB threshold for red detection in SSD RW mode. */
  public static final int SSD_RED_CHANNEL_MIN = 100;

  /** Legacy rotation applied in {@code writeTag} before packing. */
  public static final float PACKING_ROTATION_DEGREES = 90f;

  private ImageProcessor() {}

  public static ProcessedImage process(Bitmap source, ImageProcessingConfig config) {
    if (source == null) {
      throw new IllegalArgumentException("source bitmap is required");
    }
    if (config == null) {
      throw new IllegalArgumentException("config is required");
    }

    Bitmap scaled =
        Bitmap.createScaledBitmap(
            source, config.getPanelWidth(), config.getPanelHeight(), true);

    Bitmap rotated = rotateBitmap(scaled, PACKING_ROTATION_DEGREES);
    if (rotated == null) {
      throw new IllegalStateException("Failed to rotate bitmap for packing");
    }

    int width0 = rotated.getWidth();
    int height0 = rotated.getHeight();
    int colorMode = config.getColorMode();

    if (colorMode == ColorMode.QUAD) {
      byte[] quad = packQuadLayer(rotated);
      return new ProcessedImage(quad, null, width0, height0, colorMode);
    }

    byte[] black = packSsdLayer(rotated, 0);
    byte[] color = null;
    if (colorMode == ColorMode.TRI) {
      color = packSsdLayer(rotated, 1);
    }

    return new ProcessedImage(black, color, width0, height0, colorMode);
  }

  public static ProcessedImage processFromUri(
      Context context, String uriString, ImageProcessingConfig config) throws IOException {
    Bitmap bitmap = decodeBitmapFromUri(context, uriString);
    try {
      return process(bitmap, config);
    } finally {
      if (!bitmap.isRecycled()) {
        bitmap.recycle();
      }
    }
  }

  public static ProcessedImage processFromBase64(String base64, ImageProcessingConfig config) {
    Bitmap bitmap = decodeBitmapFromBase64(base64);
    try {
      return process(bitmap, config);
    } finally {
      if (!bitmap.isRecycled()) {
        bitmap.recycle();
      }
    }
  }

  public static Bitmap decodeBitmapFromUri(Context context, String uriString) throws IOException {
    if (context == null) {
      throw new IllegalArgumentException("context is required");
    }
    if (uriString == null || uriString.isEmpty()) {
      throw new IllegalArgumentException("uri is required");
    }
    Uri uri = Uri.parse(uriString);
    try (InputStream stream = context.getContentResolver().openInputStream(uri)) {
      if (stream == null) {
        throw new IOException("Unable to open uri: " + uriString);
      }
      Bitmap bitmap = BitmapFactory.decodeStream(stream);
      if (bitmap == null) {
        throw new IOException("BitmapFactory.decodeStream returned null for: " + uriString);
      }
      return bitmap;
    }
  }

  public static Bitmap decodeBitmapFromBase64(String base64) {
    if (base64 == null || base64.isEmpty()) {
      throw new IllegalArgumentException("base64 is required");
    }
    String payload = base64;
    int comma = base64.indexOf(',');
    if (base64.startsWith("data:") && comma >= 0) {
      payload = base64.substring(comma + 1);
    }
    byte[] bytes = Base64.decode(payload.trim(), Base64.DEFAULT);
    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    if (bitmap == null) {
      throw new IllegalArgumentException("Invalid base64 image data");
    }
    return bitmap;
  }

  /**
   * Port of {@code activity_imageview.rotateBitmap}.
   */
  public static Bitmap rotateBitmap(Bitmap bitmap, float angle) {
    if (bitmap == null) {
      return null;
    }
    int width = bitmap.getWidth();
    int height = bitmap.getHeight();
    Matrix matrix = new Matrix();
    matrix.postRotate(angle);
    return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
  }

  /**
   * Port of {@code GetPictureData_SSD}: {@code mode=0} BW, {@code mode=1} RW (red plane).
   */
  public static byte[] packSsdLayer(Bitmap bitmap, int mode) {
    if (bitmap == null) {
      throw new IllegalArgumentException("bitmap is required");
    }
    if (mode != 0 && mode != 1) {
      throw new IllegalArgumentException("mode must be 0 (BW) or 1 (RW)");
    }

    int width0 = bitmap.getWidth();
    int height0 = bitmap.getHeight();
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
            if (r <= SSD_BLACK_THRESHOLD
                && g <= SSD_BLACK_THRESHOLD
                && b <= SSD_BLACK_THRESHOLD) {
              temp = (byte) (temp + 0);
            } else {
              temp = (byte) (temp + 1);
            }
          } else {
            if (r >= SSD_RED_CHANNEL_MIN
                && g <= SSD_BLACK_THRESHOLD
                && b <= SSD_BLACK_THRESHOLD) {
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

  /** Port of {@code GetPictureData_4G}. */
  public static byte[] packQuadLayer(Bitmap bitmap) {
    if (bitmap == null) {
      throw new IllegalArgumentException("bitmap is required");
    }

    int width0 = bitmap.getWidth();
    int height0 = bitmap.getHeight();
    int bufferLength = (width0 * height0) / 4;
    byte[] imageBuffer = new byte[bufferLength];

    int index = 0;
    for (int i = width0 - 1; i >= 0; i--) {
      for (int j = 0; j <= height0 / 4 - 1; j++) {
        byte temp = 0;
        for (int k = 0; k < 4; k++) {
          temp = (byte) (temp * 4);
          int pixel = bitmap.getPixel(i, (j * 4) + k);
          int r = (pixel & 0xff0000) >> 16;
          int g = (pixel & 0xff00) >> 8;
          int b = (pixel & 0xff);

          if (r <= SSD_BLACK_THRESHOLD
              && g <= SSD_BLACK_THRESHOLD
              && b <= SSD_BLACK_THRESHOLD) {
            temp = (byte) (temp + 0x00);
          } else if (r >= 200 && g >= 200 && b >= 200) {
            temp = (byte) (temp + 0x01);
          } else {
            int num = (r + g + b) / 3;
            if (num <= 127) {
              temp = (byte) (temp + 0x03);
            } else {
              temp = (byte) (temp + 0x02);
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

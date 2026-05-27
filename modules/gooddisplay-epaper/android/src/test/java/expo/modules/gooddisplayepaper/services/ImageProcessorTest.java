package expo.modules.gooddisplayepaper.services;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.graphics.Bitmap;
import android.graphics.Color;

import expo.modules.gooddisplayepaper.models.ColorMode;
import expo.modules.gooddisplayepaper.models.ImageProcessingConfig;
import expo.modules.gooddisplayepaper.models.ProcessedImage;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ImageProcessorTest {

  @Test
  public void packSsdLayer_allWhite_produces0xFFBytes() {
    Bitmap bitmap = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888);
    bitmap.eraseColor(Color.WHITE);

    byte[] packed = ImageProcessor.packSsdLayer(bitmap, 0);

    assertEquals(8, packed.length);
    for (byte b : packed) {
      assertEquals((byte) 0xFF, b);
    }
  }

  @Test
  public void packSsdLayer_matchesLegacyReferenceImplementation() {
    Bitmap bitmap = Bitmap.createBitmap(16, 8, Bitmap.Config.ARGB_8888);
    for (int x = 0; x < 16; x++) {
      for (int y = 0; y < 8; y++) {
        int color = (x + y) % 2 == 0 ? Color.BLACK : Color.WHITE;
        bitmap.setPixel(x, y, color);
      }
    }

    byte[] extracted = ImageProcessor.packSsdLayer(bitmap, 0);
    byte[] reference = ImageProcessingParity.legacyReferencePackSsd(bitmap, 0, 16, 8);

    assertArrayEquals(reference, extracted);
  }

  @Test
  public void packSsdLayer_redPlane_detectsRedPixels() {
    Bitmap bitmap = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888);
    bitmap.eraseColor(Color.WHITE);
    bitmap.setPixel(4, 4, Color.RED);

    byte[] packed = ImageProcessor.packSsdLayer(bitmap, 1);
    assertEquals(8, packed.length);
    assertTrue(packed[4] != (byte) 0xFF);
  }

  @Test
  public void packQuadLayer_bufferLength() {
    Bitmap bitmap = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888);
    bitmap.eraseColor(Color.WHITE);

    byte[] packed = ImageProcessor.packQuadLayer(bitmap);
    assertEquals(ImageProcessingParity.expectedQuadBufferLength(8, 8), packed.length);
  }

  @Test
  public void process_pilot213Mono_dimensionsAndBufferSize() {
    ImageProcessingConfig config = ImageProcessingConfig.pilot213Mono();
    Bitmap source = Bitmap.createBitmap(128, 250, Bitmap.Config.ARGB_8888);
    source.eraseColor(Color.WHITE);

    ProcessedImage result = ImageProcessor.process(source, config);

    assertEquals(250, result.getWidth());
    assertEquals(128, result.getHeight());
    assertEquals(ColorMode.MONO, result.getColorMode());
    assertNull(result.getColorLayer());
    assertEquals(4000, result.getBlackLayer().length);
    assertTrue(ImageProcessingParity.validateProcessedImage(result));
  }

  @Test
  public void process_triColor_producesTwoLayers() {
    ImageProcessingConfig config =
        ImageProcessingConfig.fromEpdArray(0, 128, 0, 250, ColorMode.TRI, 213);
    Bitmap source = Bitmap.createBitmap(128, 250, Bitmap.Config.ARGB_8888);
    source.eraseColor(Color.WHITE);

    ProcessedImage result = ImageProcessor.process(source, config);

    assertNotNull(result.getBlackLayer());
    assertNotNull(result.getColorLayer());
    assertEquals(result.getBlackLayer().length, result.getColorLayer().length);
    assertTrue(ImageProcessingParity.validateProcessedImage(result));
  }

  @Test
  public void process_quadColor_singleQuadBuffer() {
    ImageProcessingConfig config =
        ImageProcessingConfig.fromEpdArray(0, 128, 0, 250, ColorMode.QUAD, 213);
    Bitmap source = Bitmap.createBitmap(128, 250, Bitmap.Config.ARGB_8888);
    source.eraseColor(Color.WHITE);

    ProcessedImage result = ImageProcessor.process(source, config);

    assertNull(result.getColorLayer());
    assertEquals(8000, result.getBlackLayer().length);
    assertTrue(ImageProcessingParity.validateProcessedImage(result));
  }

  @Test
  public void rotateBitmap_90swapsDimensions() {
    Bitmap source = Bitmap.createBitmap(128, 250, Bitmap.Config.ARGB_8888);
    Bitmap rotated = ImageProcessor.rotateBitmap(source, 90f);
    assertNotNull(rotated);
    assertEquals(250, rotated.getWidth());
    assertEquals(128, rotated.getHeight());
  }

  @Test
  public void processFromBase64_decodesAndPacks() {
    Bitmap tiny = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888);
    tiny.eraseColor(Color.BLACK);
    java.io.ByteArrayOutputStream stream = new java.io.ByteArrayOutputStream();
    tiny.compress(Bitmap.CompressFormat.PNG, 100, stream);
    String base64 = android.util.Base64.encodeToString(stream.toByteArray(), android.util.Base64.NO_WRAP);

    ImageProcessingConfig config =
        new ImageProcessingConfig(8, 8, 97, ColorMode.MONO);
    ProcessedImage result = ImageProcessor.processFromBase64(base64, config);

    assertEquals(8, result.getWidth());
    assertEquals(8, result.getHeight());
    assertEquals(8, result.getBlackLayer().length);
  }
}

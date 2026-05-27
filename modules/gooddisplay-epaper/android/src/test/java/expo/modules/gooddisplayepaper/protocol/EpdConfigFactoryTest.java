package expo.modules.gooddisplayepaper.protocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import expo.modules.gooddisplayepaper.models.ColorMode;
import expo.modules.gooddisplayepaper.models.ScreenConfig;

import org.junit.Test;

public class EpdConfigFactoryTest {

  @Test
  public void pilot213Mono_daCommandHex() {
    ScreenConfig config = EpdConfigFactory.pilot213Mono();
    assertEquals(128, config.getWidth());
    assertEquals(250, config.getHeight());
    assertEquals(213, config.getInchCode());
    assertEquals(ColorMode.MONO, config.getColorMode());
    assertEquals("F0DA000003F00020", HexCodec.bytesToHexString(config.getScreenTypeCommand()));
  }

  @Test
  public void pilot213Mono_driverFlowStartsWithF0DB() {
    ScreenConfig config = EpdConfigFactory.pilot213Mono();
    byte[] db = config.getDriverConfig();
    assertTrue(db.length > 10);
    assertEquals(ApduConstants.VENDOR_PREFIX, db[0]);
    assertEquals(ApduConstants.CMD_DB, db[1]);
  }

  @Test
  public void processingDimensions_matchCheckpoint1() {
    ScreenConfig config = EpdConfigFactory.pilot213Mono();
    assertEquals(128, config.getProcessingWidth());
    assertEquals(250, config.getProcessingHeight());
    assertNotNull(config.toImageProcessingConfig());
    assertEquals(128, config.toImageProcessingConfig().getPanelWidth());
  }

  @Test
  public void inch420_processingDimensionsSwapped() {
    ScreenConfig config = EpdConfigFactory.create(ColorMode.MONO, 420);
    assertEquals(400, config.getWidth());
    assertEquals(300, config.getHeight());
    assertEquals(300, config.getProcessingWidth());
    assertEquals(400, config.getProcessingHeight());
    assertTrue((config.getProtocolFlags() & ScreenConfig.ProtocolFlags.MONO_420_DUAL_PASS) != 0);
  }

  @Test
  public void inch370_monoDualPassFlag() {
    ScreenConfig config = EpdConfigFactory.create(ColorMode.MONO, 370);
    assertTrue((config.getProtocolFlags() & ScreenConfig.ProtocolFlags.MONO_370_DUAL_PASS) != 0);
  }

  @Test
  public void supports_knownPanels() {
    assertTrue(EpdConfigFactory.supports(ColorMode.MONO, 213));
    assertTrue(EpdConfigFactory.supports(ColorMode.TRI, 290));
    assertTrue(EpdConfigFactory.supports(ColorMode.QUAD, 97));
  }
}

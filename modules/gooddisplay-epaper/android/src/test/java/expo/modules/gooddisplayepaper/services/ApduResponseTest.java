package expo.modules.gooddisplayepaper.services;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ApduResponseTest {

  @Test
  public void isLegacySuccess() {
    assertTrue(ApduResponse.isLegacySuccess(new byte[] {(byte) 0x90, 0x00}));
    assertFalse(ApduResponse.isLegacySuccess(new byte[] {(byte) 0x6F, 0x00}));
  }

  @Test
  public void isBusyIdle_firstByteZero() {
    assertTrue(ApduResponse.isBusyIdle(new byte[] {0x00}));
  }

  @Test
  public void isBusyIdle_iso9000() {
    assertTrue(ApduResponse.isBusyIdle(new byte[] {(byte) 0x90, 0x00}));
  }

  @Test
  public void isBusyIdle_stillBusy() {
    assertFalse(ApduResponse.isBusyIdle(new byte[] {(byte) 0x01}));
  }
}

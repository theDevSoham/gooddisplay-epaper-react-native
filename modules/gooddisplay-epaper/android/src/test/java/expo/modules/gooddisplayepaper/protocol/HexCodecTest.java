package expo.modules.gooddisplayepaper.protocol;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class HexCodecTest {

  @Test
  public void hexStringToBytes_roundTrip() {
    String hex = "F0D2FA00FF";
    byte[] bytes = HexCodec.hexStringToBytes(hex);
    assertEquals(hex, HexCodec.bytesToHexString(bytes));
  }

  @Test
  public void hexStringToBytes_matchesLegacyIcDiy() {
    byte[] bytes = HexCodec.hexStringToBytes("F0DB020000");
    assertEquals(5, bytes.length);
    assertEquals((byte) 0xF0, bytes[0]);
    assertEquals((byte) 0xDB, bytes[1]);
  }
}

package expo.modules.gooddisplayepaper.protocol;

/** GoodDisplay / Fudan ESL APDU constants from legacy {@code activity_imageview}. */
public final class ApduConstants {

  public static final byte VENDOR_PREFIX = (byte) 0xF0;

  public static final byte CMD_DB = (byte) 0xDB;
  public static final byte CMD_DA = (byte) 0xDA;
  public static final byte CMD_D2 = (byte) 0xD2;
  public static final byte CMD_D3 = (byte) 0xD3;
  public static final byte CMD_D4 = (byte) 0xD4;
  public static final byte CMD_DE = (byte) 0xDE;
  public static final byte CMD_D1 = (byte) 0xD1;

  /** Legacy IC DIY command before driver flow. */
  public static final String IC_DIY_HEX = "F0DB020000";

  /** Max image bytes per D2 packet (legacy {@code 0xFA}). */
  public static final int D2_MAX_PAYLOAD = 250;

  /** Total D2 APDU size: 5-byte header + 250 payload. */
  public static final int D2_APDU_LENGTH = D2_MAX_PAYLOAD + 5;

  public static final byte D2_LENGTH_MARKER = (byte) 0xFA;

  public static final byte SCREEN_INDEX_BW = 0;
  public static final byte SCREEN_INDEX_RED = 1;

  public static final byte D4_FLAG_DIRECT_RETURN = (byte) 0x80;
  public static final byte D4_DELAY_MONO_TRI = (byte) 0x05;
  public static final byte D4_DELAY_QUAD = (byte) 0x85;

  public static final byte SW1_SUCCESS = (byte) 0x90;

  /**
   * D1 read config — not used in legacy app; frame per vendor ESL defaults (verify on tag).
   */
  public static final String READ_CONFIG_HEX = "F0D1010000";

  /**
   * DE busy poll — not used by legacy app; retained for tests / future tags that support it.
   */
  public static final String BUSY_POLL_HEX = "F0DE010000";

  /** Legacy IsoDep timeout (ms). For reference in Checkpoint 3. */
  public static final int ISODEP_TIMEOUT_MS = 50000;

  /** Inter-command delay after init steps in legacy {@code writeTag}. */
  public static final int INIT_DELAY_MS = 10;

  private ApduConstants() {}
}

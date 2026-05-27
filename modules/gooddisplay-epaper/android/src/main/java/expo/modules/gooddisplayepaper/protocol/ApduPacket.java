package expo.modules.gooddisplayepaper.protocol;

/**
 * Immutable APDU frame ready for {@code IsoDep.transceive} (Checkpoint 3).
 */
public final class ApduPacket {

  public enum Kind {
    IC_DIY,
    DRIVER_FLOW,
    SCREEN_TYPE,
    IMAGE_UPLOAD,
    REFRESH,
    BUSY_POLL,
    READ_CONFIG
  }

  private final Kind kind;
  private final byte[] bytes;
  private final int screenIndex;
  private final int packetIndex;

  private ApduPacket(Kind kind, byte[] bytes, int screenIndex, int packetIndex) {
    this.kind = kind;
    this.bytes = bytes;
    this.screenIndex = screenIndex;
    this.packetIndex = packetIndex;
  }

  public static ApduPacket of(Kind kind, byte[] bytes) {
    return new ApduPacket(kind, bytes, -1, -1);
  }

  public static ApduPacket d2(int screenIndex, int packetIndex, byte[] bytes) {
    return new ApduPacket(Kind.IMAGE_UPLOAD, bytes, screenIndex, packetIndex);
  }

  public Kind getKind() {
    return kind;
  }

  public byte[] getBytes() {
    return bytes;
  }

  public int getScreenIndex() {
    return screenIndex;
  }

  public int getPacketIndex() {
    return packetIndex;
  }

  public int length() {
    return bytes.length;
  }
}

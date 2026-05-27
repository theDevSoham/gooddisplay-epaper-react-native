package expo.modules.gooddisplayepaper.protocol;

/**
 * Hex string ↔ byte[] conversion ported from {@code data/activity_imageview.java}.
 */
public final class HexCodec {

  private HexCodec() {}

  /** Legacy name: {@code HexString2Btyes}. */
  public static byte[] hexStringToBytes(String hexstr) {
    if (hexstr == null || hexstr.length() % 2 != 0) {
      throw new IllegalArgumentException("Hex string must have even length");
    }
    char[] charArray = hexstr.toCharArray();
    byte[] b = new byte[hexstr.length() / 2];
    int j = 0;
    for (int i = 0; i < b.length; i++) {
      char c0 = charArray[j++];
      char c1 = charArray[j++];
      b[i] = (byte) ((parse(c0) << 4) | parse(c1));
    }
    return b;
  }

  /** Legacy name: {@code HexToString}. */
  public static String bytesToHexString(byte[] data) {
    if (data == null) {
      return "";
    }
    StringBuilder sb = new StringBuilder(data.length * 2);
    for (byte value : data) {
      int unsigned = value & 0xFF;
      sb.append(numToString(unsigned / 16));
      sb.append(numToString(unsigned % 16));
    }
    return sb.toString();
  }

  private static int parse(char c) {
    if (c >= 'a') {
      return (c - 'a' + 10) & 0x0f;
    }
    if (c >= 'A') {
      return (c - 'A' + 10) & 0x0f;
    }
    return (c - '0') & 0x0f;
  }

  private static String numToString(int c) {
    if (c >= 10) {
      switch (c) {
        case 10:
          return "A";
        case 11:
          return "B";
        case 12:
          return "C";
        case 13:
          return "D";
        case 14:
          return "E";
        case 15:
          return "F";
        default:
          return "";
      }
    }
    return String.valueOf(c);
  }
}

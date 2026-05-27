package expo.modules.gooddisplayepaper.services;

import expo.modules.gooddisplayepaper.protocol.ApduConstants;

/** ISO7816 / legacy SW helpers. */
public final class ApduResponse {

  private ApduResponse() {}

  public static byte sw1(byte[] response) {
    if (response == null || response.length < 1) {
      return 0;
    }
    return response[0];
  }

  public static byte sw2(byte[] response) {
    if (response == null || response.length < 2) {
      return 0;
    }
    return response[1];
  }

  /** Legacy checks {@code response[0] == 0x90} after DA / D4. */
  public static boolean isLegacySuccess(byte[] response) {
    return response != null && response.length > 0 && response[0] == ApduConstants.SW1_SUCCESS;
  }

  /**
   * DE poll idle: first byte {@code 0x00} (GOODDISPLAY.md) or ISO {@code 90 00}.
   */
  public static boolean isBusyIdle(byte[] response) {
    if (response == null || response.length == 0) {
      return false;
    }
    if ((response[0] & 0xFF) == 0x00) {
      return true;
    }
    return response.length >= 2
        && response[0] == ApduConstants.SW1_SUCCESS
        && response[1] == 0x00;
  }
}

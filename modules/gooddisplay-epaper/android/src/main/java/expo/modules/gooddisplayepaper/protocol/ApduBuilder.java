package expo.modules.gooddisplayepaper.protocol;

import expo.modules.gooddisplayepaper.models.ColorMode;
import expo.modules.gooddisplayepaper.models.ProcessedImage;
import expo.modules.gooddisplayepaper.models.ScreenConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Deterministic GoodDisplay APDU frame builders (legacy {@code activity_imageview} parity). */
public final class ApduBuilder {

  private ApduBuilder() {}

  public static ApduPacket buildIcDiy() {
    return ApduPacket.of(ApduPacket.Kind.IC_DIY, HexCodec.hexStringToBytes(ApduConstants.IC_DIY_HEX));
  }

  public static ApduPacket buildDriverFlow(byte[] driverFlow) {
    return ApduPacket.of(ApduPacket.Kind.DRIVER_FLOW, Arrays.copyOf(driverFlow, driverFlow.length));
  }

  public static ApduPacket buildDriverFlow(ScreenConfig config) {
    return buildDriverFlow(config.getDriverConfig());
  }

  /** DA — set screen type (legacy {@code setEpdInit()[1]}). */
  public static ApduPacket buildScreenType(byte[] screenTypeCommand) {
    return ApduPacket.of(ApduPacket.Kind.SCREEN_TYPE, Arrays.copyOf(screenTypeCommand, screenTypeCommand.length));
  }

  public static ApduPacket buildScreenType(ScreenConfig config) {
    return buildScreenType(config.getScreenTypeCommand());
  }

  /** D1 — read config (not used in legacy; frame for future use). */
  public static ApduPacket buildReadConfig() {
    return ApduPacket.of(ApduPacket.Kind.READ_CONFIG, HexCodec.hexStringToBytes(ApduConstants.READ_CONFIG_HEX));
  }

  /** DE — busy poll (not used in legacy; target refresh flow). */
  public static ApduPacket buildBusyPoll() {
    return ApduPacket.of(ApduPacket.Kind.BUSY_POLL, HexCodec.hexStringToBytes(ApduConstants.BUSY_POLL_HEX));
  }

  /** D4 — refresh / redraw (legacy {@code refreshcmd}). */
  public static ApduPacket buildRefresh(int colorMode) {
    byte[] cmd = new byte[5];
    cmd[0] = ApduConstants.VENDOR_PREFIX;
    cmd[1] = ApduConstants.CMD_D4;
    cmd[2] =
        colorMode == ColorMode.QUAD
            ? ApduConstants.D4_DELAY_QUAD
            : ApduConstants.D4_DELAY_MONO_TRI;
    cmd[3] = ApduConstants.D4_FLAG_DIRECT_RETURN;
    cmd[4] = 0x00;
    return ApduPacket.of(ApduPacket.Kind.REFRESH, cmd);
  }

  /**
   * D2 — single upload packet (255 bytes). {@code payload} must be exactly 250 bytes; use
   * {@link #padPayloadSlice} for tail packets.
   */
  public static ApduPacket buildD2Upload(byte screenIndex, int packetIndex, byte[] payload) {
    if (payload.length != ApduConstants.D2_MAX_PAYLOAD) {
      throw new IllegalArgumentException(
          "D2 payload must be " + ApduConstants.D2_MAX_PAYLOAD + " bytes");
    }
    byte[] cmd = new byte[ApduConstants.D2_APDU_LENGTH];
    cmd[0] = ApduConstants.VENDOR_PREFIX;
    cmd[1] = ApduConstants.CMD_D2;
    cmd[2] = screenIndex;
    cmd[3] = (byte) packetIndex;
    cmd[4] = ApduConstants.D2_LENGTH_MARKER;
    System.arraycopy(payload, 0, cmd, 5, ApduConstants.D2_MAX_PAYLOAD);
    return ApduPacket.d2(screenIndex & 0xFF, packetIndex, cmd);
  }

  /** IC DIY → DB → DA init sequence (legacy {@code writeTag} steps 1–3). */
  public static List<ApduPacket> buildInitSequence(ScreenConfig config) {
    List<ApduPacket> packets = new ArrayList<>(3);
    packets.add(buildIcDiy());
    packets.add(buildDriverFlow(config));
    packets.add(buildScreenType(config));
    return Collections.unmodifiableList(packets);
  }

  /**
   * All D2 packets for a packed image (legacy upload loops). Uses {@link ProcessedImage} from
   * Checkpoint 1.
   */
  public static List<ApduPacket> buildImageUploadSequence(
      ScreenConfig config, ProcessedImage image) {
    List<D2UploadPass> passes = planUploadPasses(config, image);
    List<ApduPacket> packets = new ArrayList<>();
    for (D2UploadPass pass : passes) {
      packets.addAll(buildD2Sequence(pass.screenIndex, wirePayload(pass)));
    }
    return Collections.unmodifiableList(packets);
  }

  /**
   * Expand one layer buffer into ordered D2 packets (legacy tail rule preserved).
   */
  public static List<ApduPacket> buildD2Sequence(byte screenIndex, byte[] layerData) {
    if (layerData == null || layerData.length == 0) {
      throw new IllegalArgumentException("layerData is required");
    }
    int datas = layerData.length;
    int fullPacketCount = datas / ApduConstants.D2_MAX_PAYLOAD;
    List<ApduPacket> packets = new ArrayList<>(fullPacketCount + 1);

    for (int i = 0; i < fullPacketCount; i++) {
      byte[] chunk = padPayloadSlice(layerData, i * ApduConstants.D2_MAX_PAYLOAD);
      packets.add(buildD2Upload(screenIndex, i, chunk));

      if (i == fullPacketCount - 1 && datas % ApduConstants.D2_MAX_PAYLOAD != 0) {
        byte[] tail = padPayloadSlice(layerData, (datas / ApduConstants.D2_MAX_PAYLOAD) * ApduConstants.D2_MAX_PAYLOAD);
        packets.add(buildD2Upload(screenIndex, i + 1, tail));
      }
    }
    return Collections.unmodifiableList(packets);
  }

  /**
   * Copy up to 250 bytes from {@code layerData} at {@code offset}; remainder zero-padded (legacy
   * tail packet uses start of last partial block, not zero-fill of missing length).
   */
  public static byte[] padPayloadSlice(byte[] layerData, int offset) {
    byte[] chunk = new byte[ApduConstants.D2_MAX_PAYLOAD];
    int remaining = layerData.length - offset;
    if (remaining > 0) {
      System.arraycopy(
          layerData, offset, chunk, 0, Math.min(remaining, ApduConstants.D2_MAX_PAYLOAD));
    }
    return chunk;
  }

  static List<D2UploadPass> planUploadPasses(ScreenConfig config, ProcessedImage image) {
    int colorMode = config.getColorMode();
    List<D2UploadPass> passes = new ArrayList<>(2);

    if (colorMode == ColorMode.QUAD) {
      passes.add(new D2UploadPass(ApduConstants.SCREEN_INDEX_BW, image.getBlackLayer(), false, false));
      return passes;
    }

    if ((config.getProtocolFlags() & ScreenConfig.ProtocolFlags.MONO_370_DUAL_PASS) != 0) {
      byte[] fill = new byte[image.getBlackLayer().length];
      Arrays.fill(fill, (byte) 0xFF);
      passes.add(new D2UploadPass(ApduConstants.SCREEN_INDEX_BW, fill, false, true));
      passes.add(
          new D2UploadPass(ApduConstants.SCREEN_INDEX_RED, image.getBlackLayer(), false, false));
      return passes;
    }

    passes.add(
        new D2UploadPass(ApduConstants.SCREEN_INDEX_BW, image.getBlackLayer(), false, false));

    boolean needsRedPass =
        colorMode == ColorMode.TRI
            || (config.getProtocolFlags() & ScreenConfig.ProtocolFlags.MONO_420_DUAL_PASS) != 0;

    if (needsRedPass) {
      byte[] redSource = image.getColorLayer();
      if (redSource == null) {
        throw new IllegalArgumentException("colorLayer required for tri-color / 4.2\" mono upload");
      }
      boolean invert =
          (config.getProtocolFlags() & ScreenConfig.ProtocolFlags.INVERT_RED_ON_UPLOAD) != 0;
      passes.add(new D2UploadPass(ApduConstants.SCREEN_INDEX_RED, redSource, invert, false));
    }

    return passes;
  }

  static byte[] wirePayload(D2UploadPass pass) {
    if (pass.fillWith0xFF) {
      byte[] filled = new byte[pass.payload.length];
      Arrays.fill(filled, (byte) 0xFF);
      return filled;
    }
    if (pass.invertOnWire) {
      byte[] inverted = new byte[pass.payload.length];
      for (int i = 0; i < pass.payload.length; i++) {
        inverted[i] = (byte) (0xFF - pass.payload[i]);
      }
      return inverted;
    }
    return pass.payload;
  }

  static final class D2UploadPass {
    final byte screenIndex;
    final byte[] payload;
    final boolean invertOnWire;
    final boolean fillWith0xFF;

    D2UploadPass(byte screenIndex, byte[] payload, boolean invertOnWire, boolean fillWith0xFF) {
      this.screenIndex = screenIndex;
      this.payload = payload;
      this.invertOnWire = invertOnWire;
      this.fillWith0xFF = fillWith0xFF;
    }
  }
}

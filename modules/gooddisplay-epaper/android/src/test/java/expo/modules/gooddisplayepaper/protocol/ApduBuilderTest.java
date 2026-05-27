package expo.modules.gooddisplayepaper.protocol;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import expo.modules.gooddisplayepaper.models.ColorMode;
import expo.modules.gooddisplayepaper.models.ProcessedImage;
import expo.modules.gooddisplayepaper.models.ScreenConfig;
import expo.modules.gooddisplayepaper.services.ImageProcessingParity;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class ApduBuilderTest {

  @Test
  public void buildIcDiy_matchesLegacy() {
    ApduPacket packet = ApduBuilder.buildIcDiy();
    assertEquals("F0DB020000", HexCodec.bytesToHexString(packet.getBytes()));
    assertEquals(5, packet.length());
  }

  @Test
  public void buildD2Upload_headerAndLength() {
    byte[] payload = new byte[ApduConstants.D2_MAX_PAYLOAD];
    Arrays.fill(payload, (byte) 0xAB);
    ApduPacket packet = ApduBuilder.buildD2Upload(ApduConstants.SCREEN_INDEX_BW, 3, payload);

    assertEquals(ApduConstants.D2_APDU_LENGTH, packet.length());
    byte[] cmd = packet.getBytes();
    assertEquals(ApduConstants.VENDOR_PREFIX, cmd[0]);
    assertEquals(ApduConstants.CMD_D2, cmd[1]);
    assertEquals(ApduConstants.SCREEN_INDEX_BW, cmd[2]);
    assertEquals(3, cmd[3] & 0xFF);
    assertEquals(ApduConstants.D2_LENGTH_MARKER, cmd[4]);
    assertEquals((byte) 0xAB, cmd[5]);
    assertEquals((byte) 0xAB, cmd[254]);
  }

  @Test
  public void buildD2Sequence_pilot213Mono_sixteenPacketsNoTail() {
    int datas = ImageProcessingParity.expectedSsdBufferLength(250, 128);
    assertEquals(4000, datas);
    byte[] layer = new byte[datas];
    Arrays.fill(layer, (byte) 0x12);

    List<ApduPacket> packets = ApduBuilder.buildD2Sequence(ApduConstants.SCREEN_INDEX_BW, layer);
    assertEquals(16, packets.size());
    for (int i = 0; i < packets.size(); i++) {
      assertEquals(i, packets.get(i).getPacketIndex());
      assertEquals(ApduConstants.D2_APDU_LENGTH, packets.get(i).length());
    }
  }

  @Test
  public void buildD2Sequence_tailPacketWhenRemainder() {
    byte[] layer = new byte[300];
    for (int i = 0; i < layer.length; i++) {
      layer[i] = (byte) i;
    }
    List<ApduPacket> packets = ApduBuilder.buildD2Sequence(ApduConstants.SCREEN_INDEX_BW, layer);
    assertEquals(2, packets.size());
    assertEquals(0, packets.get(0).getPacketIndex());
    assertEquals(1, packets.get(1).getPacketIndex());
    byte[] tailPayload = ApduBuilder.padPayloadSlice(layer, 250);
    byte[] tailInApdu = Arrays.copyOfRange(packets.get(1).getBytes(), 5, 255);
    assertArrayEquals(tailPayload, tailInApdu);
  }

  @Test
  public void buildRefresh_monoVsQuad() {
    byte[] mono = ApduBuilder.buildRefresh(ColorMode.MONO).getBytes();
    byte[] quad = ApduBuilder.buildRefresh(ColorMode.QUAD).getBytes();
    assertEquals(ApduConstants.D4_DELAY_MONO_TRI, mono[2]);
    assertEquals(ApduConstants.D4_DELAY_QUAD, quad[2]);
    assertEquals(ApduConstants.D4_FLAG_DIRECT_RETURN, mono[3]);
  }

  @Test
  public void buildReadConfig_andBusyPoll_length() {
    assertEquals(5, ApduBuilder.buildReadConfig().length());
    assertEquals(5, ApduBuilder.buildBusyPoll().length());
    assertTrue(HexCodec.bytesToHexString(ApduBuilder.buildBusyPoll().getBytes()).startsWith("F0DE"));
  }

  @Test
  public void buildInitSequence_order() {
    ScreenConfig config = EpdConfigFactory.pilot213Mono();
    List<ApduPacket> init = ApduBuilder.buildInitSequence(config);
    assertEquals(3, init.size());
    assertEquals(ApduPacket.Kind.IC_DIY, init.get(0).getKind());
    assertEquals(ApduPacket.Kind.DRIVER_FLOW, init.get(1).getKind());
    assertEquals(ApduPacket.Kind.SCREEN_TYPE, init.get(2).getKind());
  }

  @Test
  public void buildImageUploadSequence_triColor_twoLayers() {
    ScreenConfig config = EpdConfigFactory.create(ColorMode.TRI, 213);
    byte[] bw = new byte[4000];
    byte[] red = new byte[4000];
    ProcessedImage image = new ProcessedImage(bw, red, 250, 128, ColorMode.TRI);

    List<ApduPacket> packets = ApduBuilder.buildImageUploadSequence(config, image);
    assertEquals(32, packets.size());
    assertEquals(0, packets.get(0).getScreenIndex());
    assertEquals(1, packets.get(16).getScreenIndex());
  }

  @Test
  public void buildImageUploadSequence_tri_invertsRedPlaneOnWire() {
    ScreenConfig config = EpdConfigFactory.create(ColorMode.TRI, 213);
    byte[] bw = new byte[250];
    byte[] red = new byte[250];
    Arrays.fill(bw, (byte) 0x00);
    Arrays.fill(red, (byte) 0x00);
    ProcessedImage image = new ProcessedImage(bw, red, 250, 1, ColorMode.TRI);

    List<ApduPacket> packets = ApduBuilder.buildImageUploadSequence(config, image);
    ApduPacket firstRed = packets.get(1);
    assertEquals(1, firstRed.getScreenIndex());
    assertEquals((byte) 0xFF, firstRed.getBytes()[5]);
  }
}

package expo.modules.gooddisplayepaper.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.graphics.Bitmap;
import android.graphics.Color;

import expo.modules.gooddisplayepaper.models.ColorMode;
import expo.modules.gooddisplayepaper.models.ProcessedImage;
import expo.modules.gooddisplayepaper.models.WriteProgress;
import expo.modules.gooddisplayepaper.models.WriteResult;
import expo.modules.gooddisplayepaper.protocol.ApduConstants;
import expo.modules.gooddisplayepaper.protocol.EpdConfigFactory;
import expo.modules.gooddisplayepaper.protocol.HexCodec;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class NfcEpaperWriterTest {

  @Test
  public void write_executesInitUploadRefreshWithoutDePoll() throws Exception {
    RecordingTransceiveChannel channel = new RecordingTransceiveChannel();
    WriteSession session = buildPilotSession(channel);

    // init(3) + upload(16) + D4 — no DE after legacy wait
    channel.enqueueSuccess();
    channel.enqueueSuccess();
    channel.enqueueSuccess();
    for (int i = 0; i < 16; i++) {
      channel.enqueueSuccess();
    }
    channel.enqueueSuccess(); // D4

    WriteOptions options =
        WriteOptions.builder()
            .apduTraceLogging(false)
            .closeIsoDepOnComplete(false)
            .initDelayMs(0)
            .busyPollIntervalMs(0)
            .legacyRefreshWaitMs(0)
            .build();

    WriteSession sessionWithOpts =
        WriteSession.createWithChannel(
            channel,
            session.getScreenConfig(),
            session.getProcessedImage(),
            options);

    WriteResult result = NfcEpaperWriter.write(sessionWithOpts);

    assertTrue(result.isSuccess());
    assertEquals(20, result.getTotalApduCount());
    assertEquals(16, result.getD2PacketCount());
    assertEquals(0, result.getDePollCount());
    for (byte[] cmd : channel.getSentCommands()) {
      assertFalse(cmd.length >= 2 && cmd[1] == ApduConstants.CMD_DE);
    }

    assertEquals(HexCodec.hexStringToBytes(ApduConstants.IC_DIY_HEX), channel.getSentCommands().get(0));
    byte[] firstD2 = channel.getSentCommands().get(3);
    assertEquals(ApduConstants.VENDOR_PREFIX, firstD2[0]);
    assertEquals(ApduConstants.CMD_D2, firstD2[1]);
  }

  @Test
  public void write_failsOnInvalidDaSw() {
    RecordingTransceiveChannel channel = new RecordingTransceiveChannel();
    WriteSession session = buildPilotSession(channel);

    channel.enqueueSuccess();
    channel.enqueueSuccess();
    channel.enqueueResponse(new byte[] {(byte) 0x6A, (byte) 0x82});

    WriteOptions options =
        WriteOptions.builder()
            .apduTraceLogging(false)
            .closeIsoDepOnComplete(false)
            .initDelayMs(0)
            .build();

    WriteSession s =
        WriteSession.createWithChannel(
            channel, session.getScreenConfig(), session.getProcessedImage(), options);

    try {
      NfcEpaperWriter.write(s);
      fail("expected NfcWriteException");
    } catch (NfcWriteException e) {
      assertEquals(WriteProgress.Phase.INIT, e.getPhase());
      assertEquals((byte) 0x6A, e.getSw1());
    }
    assertEquals(WriteProgress.Phase.FAILED, s.getProgress().getPhase());
  }

  @Test
  public void write_refreshWait_nfcDisconnectFails() {
    RecordingTransceiveChannel channel = new RecordingTransceiveChannel();
    WriteSession session = buildPilotSession(channel);

    for (int i = 0; i < 20; i++) {
      channel.enqueueSuccess();
    }
    channel.disconnectAfterSentCount(20);

    WriteOptions options =
        WriteOptions.builder()
            .apduTraceLogging(false)
            .closeIsoDepOnComplete(false)
            .initDelayMs(0)
            .busyPollIntervalMs(0)
            .legacyRefreshWaitMs(500)
            .build();

    WriteSession s =
        WriteSession.createWithChannel(
            channel, session.getScreenConfig(), session.getProcessedImage(), options);

    try {
      NfcEpaperWriter.write(s);
      fail("expected NfcWriteException");
    } catch (NfcWriteException e) {
      assertEquals(WriteProgress.Phase.POLLING_BUSY, e.getPhase());
      assertTrue(e.getMessage().contains("NFC connection lost"));
    }
  }

  @Test
  public void transceive_retriesOnFailure() throws Exception {
    RecordingTransceiveChannel channel = new RecordingTransceiveChannel();
    WriteProgress progress = new WriteProgress();
    ApduTransceiver transceiver =
        new ApduTransceiver(
            channel,
            WriteOptions.builder().transceiveMaxRetries(1).apduTraceLogging(false).build(),
            progress);
    transceiver.connect();
    channel.setFailNextTransceive(true);
    channel.enqueueSuccess();

    byte[] response =
        transceiver.transceive(
            expo.modules.gooddisplayepaper.protocol.ApduBuilder.buildIcDiy());
    assertEquals((byte) 0x90, response[0]);
    assertEquals(1, transceiver.getTransceiveRetryCount());
    assertTrue(channel.isConnected());
  }

  @Test
  public void disconnect_closesChannelOnComplete() throws Exception {
    RecordingTransceiveChannel channel = new RecordingTransceiveChannel();
    WriteSession session = buildPilotSession(channel);

    for (int i = 0; i < 20; i++) {
      channel.enqueueSuccess();
    }

    WriteOptions options =
        WriteOptions.builder()
            .apduTraceLogging(false)
            .closeIsoDepOnComplete(true)
            .initDelayMs(0)
            .busyPollIntervalMs(0)
            .legacyRefreshWaitMs(0)
            .build();

    WriteSession s =
        WriteSession.createWithChannel(
            channel, session.getScreenConfig(), session.getProcessedImage(), options);

    NfcEpaperWriter.write(s);
    assertFalse(channel.isConnected());
  }

  private static WriteSession buildPilotSession(RecordingTransceiveChannel channel) {
    Bitmap bitmap = Bitmap.createBitmap(128, 250, Bitmap.Config.ARGB_8888);
    bitmap.eraseColor(Color.WHITE);
    ProcessedImage image =
        ImageProcessor.process(bitmap, EpdConfigFactory.pilot213Mono().toImageProcessingConfig());
    return WriteSession.createWithChannel(
        channel, EpdConfigFactory.pilot213Mono(), image, WriteOptions.DEFAULT);
  }
}

package expo.modules.gooddisplayepaper.services;

import static org.junit.Assert.assertEquals;

import android.graphics.Bitmap;
import android.graphics.Color;

import expo.modules.gooddisplayepaper.models.ProcessedImage;
import expo.modules.gooddisplayepaper.protocol.ApduPacket;
import expo.modules.gooddisplayepaper.protocol.EpdConfigFactory;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class WriteSessionTest {

  @Test
  public void initSequence_order() {
    RecordingTransceiveChannel channel = new RecordingTransceiveChannel();
    WriteSession session = buildPilotSession(channel);
    assertEquals(ApduPacket.Kind.IC_DIY, session.getInitSequence().get(0).getKind());
    assertEquals(ApduPacket.Kind.DRIVER_FLOW, session.getInitSequence().get(1).getKind());
    assertEquals(ApduPacket.Kind.SCREEN_TYPE, session.getInitSequence().get(2).getKind());
    assertEquals(16, session.getUploadSequence().size());
    assertEquals(ApduPacket.Kind.REFRESH, session.getRefreshPacket().getKind());
  }

  private static WriteSession buildPilotSession(RecordingTransceiveChannel channel) {
    Bitmap bitmap = Bitmap.createBitmap(128, 250, Bitmap.Config.ARGB_8888);
    bitmap.eraseColor(Color.WHITE);
    ProcessedImage image =
        ImageProcessor.process(bitmap, EpdConfigFactory.pilot213Mono().toImageProcessingConfig());
    return WriteSession.createWithChannel(channel, EpdConfigFactory.pilot213Mono(), image, WriteOptions.DEFAULT);
  }
}

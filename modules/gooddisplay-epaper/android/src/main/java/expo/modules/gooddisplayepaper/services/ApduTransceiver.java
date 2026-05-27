package expo.modules.gooddisplayepaper.services;

import android.nfc.tech.IsoDep;
import android.util.Log;

import expo.modules.gooddisplayepaper.models.WriteProgress;
import expo.modules.gooddisplayepaper.protocol.ApduPacket;
import expo.modules.gooddisplayepaper.protocol.HexCodec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * APDU transceive wrapper with retries, tracing, and SW capture.
 */
public final class ApduTransceiver {

  private static final String LOG_TAG = "GooddisplayEpaper";

  private final TransceiveChannel channel;
  private final WriteOptions options;
  private final WriteProgress progress;
  private final List<ApduTraceEntry> trace = new ArrayList<>();

  private WriteProgress.Phase tracePhase = WriteProgress.Phase.IDLE;
  private int transceiveRetryCount;

  public ApduTransceiver(IsoDep isoDep, WriteOptions options, WriteProgress progress) {
    this(new IsoDepTransceiveChannel(isoDep), options, progress);
  }

  public ApduTransceiver(TransceiveChannel channel, WriteOptions options, WriteProgress progress) {
    if (channel == null) {
      throw new IllegalArgumentException("channel is required");
    }
    this.channel = channel;
    this.options = options != null ? options : WriteOptions.DEFAULT;
    this.progress = progress != null ? progress : new WriteProgress();
  }

  public void connect() throws IOException {
    channel.setTimeout(options.getIsoDepTimeoutMs());
    channel.connect();
    if (!channel.isConnected()) {
      throw new IOException("NFC connect completed but channel is not connected");
    }
  }

  public boolean isConnected() {
    return channel.isConnected();
  }

  public void closeQuietly() {
    channel.closeQuietly();
  }

  public List<ApduTraceEntry> getTrace() {
    return trace;
  }

  public int getTransceiveRetryCount() {
    return transceiveRetryCount;
  }

  public void setTracePhase(WriteProgress.Phase phase) {
    this.tracePhase = phase;
  }

  public byte[] transceive(ApduPacket packet) throws IOException {
    return transceive(packet, packet.getKind());
  }

  public byte[] transceive(ApduPacket packet, ApduPacket.Kind kindForTrace) throws IOException {
    if (!isConnected()) {
      throw new IOException("NFC channel not connected");
    }
    IOException lastError = null;
    int maxAttempts = 1 + options.getTransceiveMaxRetries();
    for (int attempt = 1; attempt <= maxAttempts; attempt++) {
      long start = System.nanoTime();
      try {
        byte[] response = channel.transceive(packet.getBytes());
        long durationMs = (System.nanoTime() - start) / 1_000_000L;
        recordTrace(kindForTrace, packet, response, durationMs, attempt);
        progress.setStatusWord(ApduResponse.sw1(response), ApduResponse.sw2(response));
        return response;
      } catch (IOException e) {
        lastError = e;
        if (attempt < maxAttempts) {
          transceiveRetryCount++;
          if (options.isApduTraceLogging()) {
            Log.w(LOG_TAG, "transceive retry " + attempt + " for " + kindForTrace + ": " + e.getMessage());
          }
        }
      }
    }
    throw lastError != null ? lastError : new IOException("transceive failed");
  }

  public void delayInit() throws IOException {
    sleep(options.getInitDelayMs());
  }

  public static void sleep(int ms) throws IOException {
    if (ms <= 0) {
      return;
    }
    try {
      Thread.sleep(ms);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IOException("sleep interrupted", e);
    }
  }

  private void recordTrace(
      ApduPacket.Kind kind, ApduPacket packet, byte[] response, long durationMs, int attempt) {
    byte sw1 = ApduResponse.sw1(response);
    byte sw2 = ApduResponse.sw2(response);
    ApduTraceEntry entry =
        new ApduTraceEntry(
            System.currentTimeMillis(),
            tracePhase,
            kind,
            HexCodec.bytesToHexString(packet.getBytes()),
            HexCodec.bytesToHexString(response),
            durationMs,
            attempt,
            sw1,
            sw2);
    trace.add(entry);
    if (options.isApduTraceLogging()) {
      Log.d(
          LOG_TAG,
          "APDU "
              + kind
              + " attempt="
              + attempt
              + " "
              + durationMs
              + "ms SW="
              + String.format("%02X%02X", sw1 & 0xFF, sw2 & 0xFF)
              + " cmd="
              + truncateHex(entry.getCommandHex(), 32)
              + " rsp="
              + truncateHex(entry.getResponseHex(), 16));
    }
  }

  private static String truncateHex(String hex, int maxChars) {
    if (hex == null) {
      return "";
    }
    if (hex.length() <= maxChars) {
      return hex;
    }
    return hex.substring(0, maxChars) + "...";
  }
}

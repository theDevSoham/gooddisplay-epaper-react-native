package expo.modules.gooddisplayepaper.services;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/** In-memory NFC channel for deterministic transport tests. */
public final class RecordingTransceiveChannel implements TransceiveChannel {

  private final List<byte[]> sentCommands = new ArrayList<>();
  private final Deque<byte[]> responses = new ArrayDeque<>();
  private boolean connected;
  private boolean failNextTransceive;
  private int disconnectAfterSentCount = -1;
  private int timeoutMs;

  public void enqueueResponse(byte[] response) {
    responses.addLast(response);
  }

  public void enqueueSuccess() {
    enqueueResponse(new byte[] {(byte) 0x90, 0x00});
  }

  public void enqueueBusy(byte busyByte) {
    enqueueResponse(new byte[] {busyByte});
  }

  public void setFailNextTransceive(boolean fail) {
    this.failNextTransceive = fail;
  }

  public void setConnected(boolean connected) {
    this.connected = connected;
  }

  /** Simulates tag leaving the field after the given number of sent APDUs. */
  public void disconnectAfterSentCount(int count) {
    this.disconnectAfterSentCount = count;
  }

  public List<byte[]> getSentCommands() {
    return sentCommands;
  }

  public int getSentCount() {
    return sentCommands.size();
  }

  @Override
  public void setTimeout(int timeoutMs) {
    this.timeoutMs = timeoutMs;
  }

  @Override
  public void connect() {
    connected = true;
  }

  @Override
  public boolean isConnected() {
    return connected;
  }

  @Override
  public void closeQuietly() {
    connected = false;
  }

  @Override
  public byte[] transceive(byte[] command) throws IOException {
    if (!connected) {
      throw new IOException("not connected");
    }
    if (failNextTransceive) {
      failNextTransceive = false;
      throw new IOException("simulated transceive failure");
    }
    sentCommands.add(command.clone());
    if (disconnectAfterSentCount >= 0 && sentCommands.size() >= disconnectAfterSentCount) {
      connected = false;
    }
    if (responses.isEmpty()) {
      return new byte[] {(byte) 0x90, 0x00};
    }
    return responses.removeFirst();
  }

  public int getTimeoutMs() {
    return timeoutMs;
  }
}

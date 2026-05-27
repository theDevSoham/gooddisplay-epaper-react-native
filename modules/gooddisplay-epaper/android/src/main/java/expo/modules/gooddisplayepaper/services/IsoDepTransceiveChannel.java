package expo.modules.gooddisplayepaper.services;

import android.nfc.tech.IsoDep;

import java.io.IOException;

/** Production {@link TransceiveChannel} backed by {@link IsoDep}. */
public final class IsoDepTransceiveChannel implements TransceiveChannel {

  private final IsoDep isoDep;
  private boolean openedByChannel;

  public IsoDepTransceiveChannel(IsoDep isoDep) {
    if (isoDep == null) {
      throw new IllegalArgumentException("isoDep is required");
    }
    this.isoDep = isoDep;
  }

  @Override
  public void setTimeout(int timeoutMs) throws IOException {
    isoDep.setTimeout(timeoutMs);
  }

  @Override
  public void connect() throws IOException {
    if (!isoDep.isConnected()) {
      isoDep.connect();
      openedByChannel = true;
    }
  }

  @Override
  public boolean isConnected() {
    try {
      return isoDep.isConnected();
    } catch (SecurityException e) {
      return false;
    }
  }

  @Override
  public void closeQuietly() {
    try {
      if (isoDep.isConnected()) {
        isoDep.close();
      }
    } catch (IOException ignored) {
      // best-effort
    } finally {
      openedByChannel = false;
    }
  }

  @Override
  public byte[] transceive(byte[] command) throws IOException {
    return isoDep.transceive(command);
  }

  public boolean wasOpenedByChannel() {
    return openedByChannel;
  }
}

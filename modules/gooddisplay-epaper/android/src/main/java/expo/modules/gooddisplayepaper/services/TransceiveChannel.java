package expo.modules.gooddisplayepaper.services;

import java.io.IOException;

/** Low-level NFC byte transport (IsoDep in production, fake in tests). */
public interface TransceiveChannel {

  void setTimeout(int timeoutMs) throws IOException;

  void connect() throws IOException;

  boolean isConnected();

  void closeQuietly();

  byte[] transceive(byte[] command) throws IOException;
}

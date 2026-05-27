import { registerWebModule, NativeModule } from 'expo';

import type {
  GooddisplayEpaperModuleEvents,
  NfcHandoffOptions,
  NfcHandoffResult,
  SupportedPanel,
  WriteResult,
  WriteToTagOptions,
} from './GooddisplayEpaper.types';

class GooddisplayEpaperModule extends NativeModule<GooddisplayEpaperModuleEvents> {
  getProtocolVersion(): string {
    return '1.0.0-web-stub';
  }

  getSupportedPanels(): SupportedPanel[] {
    return [];
  }

  async registerNfcHandoff(_options?: NfcHandoffOptions): Promise<NfcHandoffResult> {
    throw new Error('GooddisplayEpaper NFC is not supported on web');
  }

  cancelWrite(): boolean {
    return false;
  }

  async writeToTag(_options: WriteToTagOptions): Promise<WriteResult> {
    throw new Error('GooddisplayEpaper NFC is not supported on web');
  }
}

export default registerWebModule(GooddisplayEpaperModule, 'GooddisplayEpaperModule');

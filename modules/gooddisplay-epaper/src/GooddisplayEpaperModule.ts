import { NativeModule, requireNativeModule } from 'expo';

import type {
  GooddisplayEpaperModuleEvents,
  NfcHandoffOptions,
  NfcHandoffResult,
  SupportedPanel,
  WriteResult,
  WriteToTagOptions,
} from './GooddisplayEpaper.types';

declare class GooddisplayEpaperModule extends NativeModule<GooddisplayEpaperModuleEvents> {
  getProtocolVersion(): string;
  getSupportedPanels(): SupportedPanel[];
  registerNfcHandoff(options?: NfcHandoffOptions): Promise<NfcHandoffResult>;
  cancelWrite(): boolean;
  writeToTag(options: WriteToTagOptions): Promise<WriteResult>;
}

export default requireNativeModule<GooddisplayEpaperModule>('GooddisplayEpaper');

export * from './GooddisplayEpaper.types';

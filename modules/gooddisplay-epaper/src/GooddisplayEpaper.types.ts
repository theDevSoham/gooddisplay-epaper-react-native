/** GoodDisplay E-Paper Expo module types. */

export type ColorModeName = 'mono' | 'tri' | 'quad';

export type WritePhase =
  | 'idle'
  | 'waiting_for_tag'
  | 'tag_acquired'
  | 'connecting'
  | 'init'
  | 'uploading'
  | 'refreshing'
  | 'polling_busy'
  | 'complete'
  | 'failed'
  | 'starting';

export interface WriteToTagOptions {
  /** file:// or content:// URI */
  imageUri?: string;
  /** Raw or data-URL base64 image */
  imageBase64?: string;
  /** Panel inch code (e.g. 213 for 2.13") */
  inchCode: number;
  /** mono | tri | quad (aliases: bw, bwr, 4g) */
  colorMode: ColorModeName | string;
  busyTimeoutMs?: number;
  transceiveMaxRetries?: number;
  /** Emit onTrace events during write (default true) */
  emitApduTrace?: boolean;
}

export interface WriteProgressEvent {
  phase: WritePhase;
  uploadPercent: number;
  packetsSent: number;
  packetsTotal: number;
  dePollCount: number;
  message?: string | null;
  sw1: number;
  sw2: number;
}

export interface WriteStatusEvent {
  phase: WritePhase | string;
  message?: string | null;
}

export interface ApduTraceEvent {
  timestampMs: number;
  phase: WritePhase | string;
  kind: string;
  commandHex: string;
  responseHex: string;
  durationMs: number;
  attempt: number;
  sw1: number;
  sw2: number;
}

export interface WriteErrorEvent {
  code: string;
  message: string;
  phase?: WritePhase | string;
  sw1?: number;
  sw2?: number;
}

export interface WriteResult {
  success: boolean;
  finalPhase: WritePhase;
  totalApduCount: number;
  d2PacketCount: number;
  dePollCount: number;
  connectDurationMs: number;
  initDurationMs: number;
  uploadDurationMs: number;
  refreshDurationMs: number;
  busyWaitDurationMs: number;
  totalDurationMs: number;
  transceiveRetryCount: number;
  sw1: number;
  sw2: number;
  trace: ApduTraceEvent[];
}

export interface SupportedPanel {
  inchCode: number;
  label: string;
  width: number;
  height: number;
  /** Legacy numeric modes: 2=mono, 3=tri, 4=quad */
  colorModes: number[];
}

export type GooddisplayEpaperModuleEvents = {
  onProgress: (event: WriteProgressEvent) => void;
  onStatus: (event: WriteStatusEvent) => void;
  onComplete: (event: WriteResult) => void;
  onError: (event: WriteErrorEvent) => void;
  onTrace: (event: ApduTraceEvent) => void;
};

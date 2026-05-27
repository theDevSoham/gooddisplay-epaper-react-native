package expo.modules.gooddisplayepaper.services;

import expo.modules.gooddisplayepaper.models.WriteProgress;

/** Optional progress callback for transport observability (Checkpoint 4 events). */
public interface WriteProgressListener {

  void onPhaseChanged(WriteProgress.Phase phase, WriteProgress progress);

  void onProgress(WriteProgress progress);
}

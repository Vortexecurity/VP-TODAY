package vortex.vp_today.util;

/**
 * @author Simon Dräger
 * @version 11.3.18
 */

public interface ProgressCallback {
    void onProgress(int percent);
    void onComplete();
}

package com.vplib.vortex.vplib;

/**
 * @author Simon Dräger
 * @version 21.3.18
 */

public class OfflineManager {
    public static final int NETWORK_CONNECTED = 0;
    public static final int NETWORK_DISCONNECTED = 1;
    private NetworkStateChangeListener listener;
    private static OfflineManager thisInstance;

    public OfflineManager() {
        thisInstance = this;
    }

    public static OfflineManager getThisInstance() {
        return thisInstance;
    }

    public NetworkStateChangeListener getListener() {
        return listener;
    }

    public void setListener(NetworkStateChangeListener netcng) {
        listener = netcng;
    }

    public void unsetListener() {
        listener = null;
    }

    public boolean listenerPresent() {
        return (listener != null);
    }
}

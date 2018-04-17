package vortex.vp_today.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.vplib.vortex.vplib.OfflineManager;
import com.vplib.vortex.vplib.Util;

/**
 * @author Simon Dräger
 * @version 21.3.18
 */

public class NetworkChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Util.isAppRunning(context, "vortex.vp_today")) {
            Log.i("NetWorkChangeReceiver", "app is running");

            OfflineManager off = OfflineManager.getThisInstance();

            if (off != null && off.listenerPresent()) {
                Log.i("NetWorkChangeReceiver", "listener present");
                if (Util.isInternetConnected(context)) {
                    Log.i("NetWorkChangeReceiver", "NETW CONN");
                    OfflineManager.getThisInstance().getListener().onNetworkStateChange(OfflineManager.NETWORK_CONNECTED);
                } else {
                    Log.i("NetWorkChangeReceiver", "NETW DISCONN");
                    OfflineManager.getThisInstance().getListener().onNetworkStateChange(OfflineManager.NETWORK_DISCONNECTED);
                }
            } else {
                Log.i("NetWorkChangeReceiver", "listener not present");
            }
        } else {
            Log.i("NetWorkChangeReceiver", "app is not running");
        }
    }
}

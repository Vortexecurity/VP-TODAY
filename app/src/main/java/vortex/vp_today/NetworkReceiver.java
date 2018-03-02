package vortex.vp_today;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * @author Simon Dräger
 * @version 2.3.18
 */

public class NetworkReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Util.isInternetConnected(context)) {
        }
    }
}
package vortex.vp_today.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import vortex.vp_today.R;
import vortex.vp_today.service.MainService;
import vortex.vp_today.util.Util;

/**
 * @author Simon Dräger
 * @version 17.3.18
 */

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Util.isMainServiceRunning(context)) {
            Intent i = new Intent(context, MainService.class);
            i.putExtra("interval", context.getSharedPreferences("vortex.vp_today.app", Context.MODE_PRIVATE)
                    .getInt(context.getString(R.string.settingRefreshIntervalMin), context.getResources().getInteger(R.integer.defaultRefreshIntervalMin)));
            context.startService(i);
            Log.i("onReceive", "intent started, interval " + i.getIntExtra("interval", 45));
        }
    }
}

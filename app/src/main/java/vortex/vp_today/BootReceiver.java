package vortex.vp_today;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * @author Simon Dräger
 * @version 3.3.18
 *
 * Wird beim Systemstart aufgerufen.
 *
 */

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("BOOT", "onReceive");

        SharedPreferences prefs = context.getSharedPreferences("vortex.vp_today.app", Context.MODE_PRIVATE);

        /* Wenn der Benutzer Benachrichtigungen erhalten möchte, den Hauptservice starten. */
        if (prefs.getBoolean("receivePushes", false)) {
            prefs.edit().putBoolean("fetchHtmlPushes", true).commit();
            context.startService(new Intent(context, MainService.class));
        }
    }
}
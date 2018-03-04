package vortex.vp_today;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

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
        /* Wenn der Benutzer Benachrichtigungen erhalten möchte, den Hauptservice starten. */
        if (context.getSharedPreferences("vortex.vp_today.app", Context.MODE_PRIVATE).getBoolean("receivePushes", false)) {
            context.getSharedPreferences("vortex.vp_today.app", Context.MODE_PRIVATE).edit().putBoolean("fetchHtmlPushes", true).commit();
            context.startService(new Intent(context, MainService.class));
        }
    }
}
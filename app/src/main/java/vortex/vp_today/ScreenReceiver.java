package vortex.vp_today;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 *
 * Empfängt Bildschirm an/aus-Ereignisse
 *
 * @author Simon Dräger
 * @version 4.3.18
 */

public class ScreenReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            if (context.getApplicationContext().getSharedPreferences("vortex.vp_today.app", Context.MODE_PRIVATE).getBoolean("receivePushes", true)) {
                context.startService(new Intent(context, MainService.class));
            }
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            /* MainService stoppen */
            context.stopService(new Intent(context.getApplicationContext(), MainService.class));
        }
    }
}
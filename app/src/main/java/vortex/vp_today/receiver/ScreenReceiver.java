package vortex.vp_today.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import vortex.vp_today.MainService;
import vortex.vp_today.R;

/**
 *
 * Empfängt Bildschirm an/aus-Ereignisse
 * @todo: vorher wird schulstart angegeben -> 45 minuten pull und feste uhrzeiten zb 20 Uhr
 *
 * @author Simon Dräger
 * @version 4.3.18
 *
 */

public class ScreenReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            /* MainService starten */
            if (context.getApplicationContext().getSharedPreferences("vortex.vp_today.app", Context.MODE_PRIVATE).getBoolean("receivePushes",
                    context.getResources().getBoolean(R.bool.receivePushesEnabled))) {
                context.startService(new Intent(context, MainService.class));
            }
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            /* MainService stoppen */
            context.stopService(new Intent(context.getApplicationContext(), MainService.class));
        }
    }
}
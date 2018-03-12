package vortex.vp_today;

import android.app.IntentService;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.HashSet;

import vortex.vp_today.util.Util;

/**
 * @author Simon Dräger
 * @version 4.3.18
 *
 * Der Haupt-Service, der im Hintergrund Push-Nachrichten sendet.
 *
 */

public class MainService extends IntentService {
    private KeyguardManager keyguardManager;
    private Vibrator vibrator;
    private SharedPreferences prefs;

    public MainService() {
        super("MainService");

        keyguardManager = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
        vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        prefs = getApplicationContext().getSharedPreferences("vortex.vp_today.app", Context.MODE_PRIVATE);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (Util.D) Log.i("INFO", "Entering onHandleIntent...");

        while (
                getApplicationContext()
                .getSharedPreferences("vortex.vp_today.app", Context.MODE_PRIVATE)
                .getBoolean("fetchHtmlPushes",
                getResources().getBoolean(R.bool.defaultFetchHtml))
                ) {
            try {

                HashSet<String> known = new HashSet<>(prefs.getStringSet("knownInfos", null));

                // TODO

                /* Jeden bereits benachrichtigten Eintrag überspringen */
                /*if (known != null) {
                    for (String i : infos) {
                        if (known.contains(i))
                            infos.remove(i);
                    }
                }

                for (String str : infos) {
                    String low = str.toLowerCase();

                    if (keyguardManager.inKeyguardRestrictedInputMode() &&
                            getApplicationContext().getSharedPreferences("vortex.vp_today.app",
                                    Context.MODE_PRIVATE).getBoolean("vibrateOnPushReceiveInLS", true)) {
                        // 2 mal vibrieren, -1 für nicht wiederholen.
                        vibrator.vibrate(new long[] { 700, 700 }, -1);
                    }

                    if (known == null) {
                        known = new HashSet<>();
                    }
                    known.add(str);

                    // Die Änderungen speichern
                    getApplicationContext().getSharedPreferences("vortex.vp_today.app",
                            Context.MODE_PRIVATE).edit().putStringSet("knownInfos", known).apply();
                }*/

                /* Jede X (default 45) Minuten updaten */
                Thread.sleep(prefs.getInt(getString(R.string.settingRefreshIntervalMin), getResources().getInteger(R.integer.defaultRefreshIntervalMin)));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        getApplicationContext().getSharedPreferences("vortex.vp_today.app", Context.MODE_PRIVATE).edit().remove("fetchHtmlPushes").apply();
        if (Util.D) Log.i("INFO", "Leaving onHandleIntent...");
    }
}
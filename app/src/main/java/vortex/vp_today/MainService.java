package vortex.vp_today;

import android.app.ActivityManager;
import android.app.IntentService;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import me.leolin.shortcutbadger.ShortcutBadger;
import vortex.vp_today.logic.VPInfo;
import vortex.vp_today.logic.VPRow;
import vortex.vp_today.net.MainServiceVPTask;
import vortex.vp_today.util.Util;

/**
 * @author Simon Dräger
 * @version 4.3.18
 * Der Haupt-Service, der im Hintergrund Push-Nachrichten sendet.
 */

public class MainService extends IntentService {
    private KeyguardManager keyguardManager;
    private Vibrator vibrator;
    private SharedPreferences prefs;
    private VPInfo info;

    public MainService() {
        super("MainService");

        keyguardManager = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
        vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        prefs = getApplicationContext().getSharedPreferences("vortex.vp_today.app", Context.MODE_PRIVATE);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (Util.D) Log.i("MainService", "Entering onHandleIntent...");

        while (prefs.getBoolean("fetchHtmlPushes", getResources().getBoolean(R.bool.defaultFetchHtml)) &&
                !isForeground()) {
            try {
                info = new MainServiceVPTask().execute().get().z;

                assert info != null : "info.z darf nicht null sein";

                Context ctx = getApplicationContext();
                ArrayList<VPRow> known = Util.getGsonObject(ctx, "knownInfos", ArrayList.class);

                /* Jeden bereits benachrichtigten Eintrag überspringen */
                if (known != null) {
                    for (VPRow i : info.getRows()) {
                        if (known.contains(i))
                            info.removeRow(i);
                    }
                }

                for (VPRow row : info.getRows()) {
                    if (keyguardManager.inKeyguardRestrictedInputMode() && prefs.getBoolean("vibrateOnPushReceiveInLS", false)) {
                        // 2 mal vibrieren, -1 für nicht wiederholen.
                        vibrator.vibrate(new long[] { 700, 700 }, -1);
                    }

                    if (prefs.getBoolean(getString(R.string.settingpushes), false)) {
                        String art = row.getArt().getName();
                        String fachOrKurs = row.getFach();
                        Util.sendNotification(ctx, "VP-TODAY: " + fachOrKurs + " | " + art,
                                row.getLinearContent());

                        // Die Zahl am launcher inkrementieren
                        int badgeCount = prefs.getInt(getString(R.string.currentBadges), 0);

                        ShortcutBadger.applyCount(ctx, ++badgeCount);

                        prefs.edit().putInt(getString(R.string.currentBadges), badgeCount).apply();
                    }

                    if (known == null) {
                        known = new ArrayList<>();
                    }

                    known.add(row);

                    // Die Änderungen speichern
                    Util.putGsonObject(getApplicationContext(), "knownInfos", known);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            try {
            /* Jede X (default 45) Minuten updaten */
                Thread.sleep(prefs.getInt(getString(R.string.settingRefreshIntervalMin), getResources().getInteger(R.integer.defaultRefreshIntervalMin)));
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        getApplicationContext().getSharedPreferences("vortex.vp_today.app", Context.MODE_PRIVATE).edit().remove("fetchHtmlPushes").apply();
        if (Util.D) Log.i("MainService", "Leaving onHandleIntent...");
    }

    private boolean isForeground() {
        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager.getRunningAppProcesses();

        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                if (appProcess.processName.equals("vortex.vp_today.app")) {
                    return true;
                }
            }
        }

        return false;
    }
}
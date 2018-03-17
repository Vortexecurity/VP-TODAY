package service;

import android.app.ActivityManager;
import android.app.IntentService;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.Log;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import me.leolin.shortcutbadger.ShortcutBadger;
import vortex.vp_today.R;
import vortex.vp_today.logic.VPInfo;
import vortex.vp_today.logic.VPRow;
import vortex.vp_today.net.MainServiceVPTask;
import vortex.vp_today.util.Util;

/**
 * @author Simon Dräger
 * @version 17.3.18
 *          Der Haupt-Service, der im Hintergrund Push-Nachrichten sendet.
 */

public class MainService extends IntentService {
    private KeyguardManager keyguardManager;
    private Vibrator vibrator;
    private SharedPreferences prefs;
    private VPInfo info;
    private Timer timer;
    private TimerTask ttask;
    private int interval = 45;

    public MainService() {
        super("MainService");
    }

    protected void initializeTimer() {
        ttask = new TimerTask() {
            public void run() {
                if (prefs.getBoolean("fetchHtmlPushes", getResources().getBoolean(R.bool.defaultFetchHtml)) &&
                        !isForeground()) {

                    Log.i("initializeTimer", "if passed");

                    LocalDate ldate = LocalDate.now();

                    try {
                        info = new MainServiceVPTask().execute(
                                Util.makeDate(ldate.getDayOfMonth() + 2, ldate.getMonthOfYear() - 1, ldate.getYear()),
                                Util.getSettingStufe(getApplicationContext()),
                                Util.getSettingKlasse(getApplicationContext()),
                                Util.getSelectedKurse(getApplicationContext())
                        ).get().z;

                        assert info != null : "info.z darf nicht null sein";

                        Log.i("MainService", "info not null");
                        Log.i("MainService", "info getRows length: " + info.getRows().size());

                        Context ctx = getApplicationContext();
                        ArrayList<VPRow> known = Util.getGsonObject(ctx, "knownInfos", ArrayList.class);

                        // Jeden bereits benachrichtigten Eintrag überspringen
                        if (known != null) {
                            for (VPRow i : info.getRows()) {
                                if (known.contains(i))
                                    info.removeRow(i);
                            }
                        }

                        for (VPRow row : info.getRows()) {
                            if (keyguardManager.inKeyguardRestrictedInputMode() && prefs.getBoolean("vibrateOnPushReceiveInLS", false)) {
                                // 2 mal vibrieren, -1 für nicht wiederholen.
                                vibrator.vibrate(new long[]{700, 700}, -1);
                            }

                            if (prefs.getBoolean(getString(R.string.settingpushes), false)) {
                                Log.i("MainService", "settingpushes true");

                                String art = row.getArt().getName();
                                String fachOrKurs = row.getFach();
                                Util.sendNotification(ctx, "VP-TODAY: " + fachOrKurs + " | " + art,
                                        row.getLinearContent());

                                // Die Zahl am launcher inkrementieren
                                int badgeCount = prefs.getInt(getString(R.string.currentBadges), 0);

                                Log.i("MainService", "applyCount: " + String.valueOf(ShortcutBadger.applyCount(ctx, ++badgeCount)));

                                prefs.edit().putInt(getString(R.string.currentBadges), badgeCount).apply();
                            } else {
                                Log.i("MainService", "settingpushes false");
                            }

                            if (known == null) {
                                known = new ArrayList<>();
                            }

                            known.add(row);

                            // Die Änderungen speichern
                            Util.putGsonObject(getApplicationContext(), "knownInfos", known);

                            info = null;
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        };
    }

    protected void startTimer() {
        timer = new Timer();

        initializeTimer();

        //int interval = prefs.getInt(getString(R.string.settingRefreshIntervalMin), getResources().getInteger(R.integer.defaultRefreshIntervalMin));

        if (interval > -1) {
            timer.schedule(ttask, 1000, Util.MinsToMillis(interval));
            Log.i("startTimer", "scheduling");
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        keyguardManager = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
        vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        prefs = getApplicationContext().getSharedPreferences("vortex.vp_today.app", Context.MODE_PRIVATE);
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        startTimer();

        return START_STICKY;
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (Util.D) Log.i("MainService", "Entering onHandleIntent...");

        if (intent != null) {
            interval = intent.getIntExtra("interval", 45);
            Log.i("onHandleIntent", "interval was: " + interval);
        }

        // Sichergehen, dass der Service auch beendet
        //prefs.edit().remove("fetchHtmlPushes").apply();
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

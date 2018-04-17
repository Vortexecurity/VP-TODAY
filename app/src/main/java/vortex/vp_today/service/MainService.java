package vortex.vp_today.service;

import android.app.ActivityManager;
import android.app.IntentService;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.reflect.TypeToken;
import com.vplib.vortex.vplib.Util;
import com.vplib.vortex.vplib.logic.VPInfo;
import com.vplib.vortex.vplib.logic.VPKind;
import com.vplib.vortex.vplib.logic.VPRow;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import me.leolin.shortcutbadger.ShortcutBadger;
import vortex.vp_today.R;
import vortex.vp_today.net.MainServiceVPTask;

/**
 * Der Haupt-Service, der im Hintergrund Push-Nachrichten sendet.
 * @author Simon Dräger
 * @version 18.3.18
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

                    if (Util.isInternetConnected(getApplicationContext())) {

                        Log.i("initializeTimer", "if passed");

                        LocalDate ldate = LocalDate.now();

                        try {
                            info = new MainServiceVPTask().execute(
                                    Util.makeDate(ldate.getDayOfMonth() + 1, ldate.getMonthOfYear() - 1, ldate.getYear()),
                                    Util.getSettingStufe(getApplicationContext()),
                                    Util.getSettingKlasse(getApplicationContext()),
                                    Util.getSelectedKurse(getApplicationContext())
                            ).get().z;

                            if (info == null)
                                return;

                            Log.i("MainService", "info not null");
                            Log.i("MainService", "info getRows length: " + info.getRows().size());

                            Context ctx = getApplicationContext();
                            ArrayList<VPRow> known = Util.getGsonObject(ctx, "knownInfos", new TypeToken<ArrayList<VPRow>>() {
                            });

                            // Jeden bereits benachrichtigten Eintrag überspringen
                            if (known != null) {
                                String[] strknown = new String[known.size()];

                                for (int i = 0; i < strknown.length; i++) {
                                    strknown[i] = known.get(i).toString();
                                }

                                Log.i("MainService", TextUtils.join(" ; ", strknown));
                                for (VPRow i : new ArrayList<>(info.getRows())) {
                                    Log.i("MainService", "VPRow i: " + i.toString());
                                    if (known.contains(i)) {
                                        info.removeRow(i);
                                        Log.i("MainService", "remove row: " + i.toString());
                                    } else {
                                        Log.i("MainService", "keeping row: " + i.toString());
                                    }
                                }
                            }

                            // texts
                            ArrayList<String> content = new ArrayList<>();
                            int entfall = 0;
                            int eigenvarbeiten = 0;
                            int raumvertretung = 0;
                            int vertretung = 0;

                            for (VPRow row : info.getRows()) {
                                if (keyguardManager.inKeyguardRestrictedInputMode() && prefs.getBoolean("vibrateOnPushReceiveInLS", false)) {
                                    // Wenn Bildschirm aus ist: 2 mal vibrieren, -1 für nicht wiederholen.
                                    vibrator.vibrate(new long[]{700, 700}, -1);
                                }

                                if (prefs.getBoolean(getString(R.string.settingpushes), false)) {
                                    Log.i("MainService", "settingpushes true");

                                    VPKind art = row.getArt();

                                    if (art == VPKind.ENTFALL)
                                        entfall++;
                                    else if (art == VPKind.EIGENVARBEITEN)
                                        eigenvarbeiten++;
                                    else if (art == VPKind.RAUMVERTRETUNG)
                                        raumvertretung++;
                                    else if (art == VPKind.VERTRETUNG)
                                        vertretung++;

                                    content.add(row.getLinearContent());

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
                                Util.putGsonObject(getApplicationContext(), "knownInfos", known, new TypeToken<ArrayList<VPRow>>() {
                                });

                                info = null;
                            }

                            int sum = entfall + vertretung + raumvertretung + eigenvarbeiten;
                            String title = "VP-TODAY (" + sum + ")";

                            if (sum > 0)
                                Util.sendNotification(ctx, title, TextUtils.join("\n", content.toArray(new String[0])));

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
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

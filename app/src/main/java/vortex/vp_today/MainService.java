package vortex.vp_today;

import android.app.IntentService;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Calendar;
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

    public MainService() {
        super("MainService");

        keyguardManager = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
        vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (Util.D) Log.i("INFO", "Entering onHandleIntent...");

        while (getApplicationContext().getSharedPreferences("vortex.vp_today.app", Context.MODE_PRIVATE).getBoolean("fetchHtmlPushes",
                getResources().getBoolean(R.bool.defaultFetchHtml))) {
            try {
                Calendar c = Calendar.getInstance();
                String dt = Util.makeDate(c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH), c.get(Calendar.YEAR));
                String unf = Util.fetchUnfiltered(dt);
                //ArrayList<TriTuple> infos = new ArrayList<>(Arrays.asList(Util.filterHTML(getApplicationContext(), Jsoup.parse(unf), Util.getSettingStufe(getApplicationContext()), Util.getSettingKlasse(getApplicationContext()))));

                HashSet<String> known =
                        new HashSet<>(getApplicationContext().getSharedPreferences("vortex.vp_today.app",
                                Context.MODE_PRIVATE).getStringSet("knownInfos", null));

                /* Jeden bereits benachrichtigten Eintrag überspringen */
                /*if (known != null) {
                    for (String i : infos) {
                        if (known.contains(i))
                            infos.remove(i);
                    }
                }

                for (String str : infos) {
                    String low = str.toLowerCase();

                    // TODO: das besser überprüfen.
                    if (low.contains("entfall"))
                        Util.makePushNotification(getApplicationContext(), "Entfall:", str);
                    else if (low.contains("eigenv. arbeiten"))
                        Util.makePushNotification(getApplicationContext(), "Eigenv. Arbeiten:", str);
                    else if (low.contains("vertretung"))
                        Util.makePushNotification(getApplicationContext(), "Vertretung:", str);
                    else if (low.contains("raum-vertretung"))
                        Util.makePushNotification(getApplicationContext(), "Raum-Vertretung:", str);
                    else
                        Util.makePushNotification(getApplicationContext(), "Unbekannt:", str);

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

                /* Jede Sekunde updaten */
                Thread.sleep(1000);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        getApplicationContext().getSharedPreferences("vortex.vp_today.app", Context.MODE_PRIVATE).edit().remove("fetchHtmlPushes").apply();
        if (Util.D) Log.i("INFO", "Leaving onHandleIntent...");
    }
}
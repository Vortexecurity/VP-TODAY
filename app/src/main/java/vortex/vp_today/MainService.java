package vortex.vp_today;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import org.jsoup.Jsoup;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;

/**
 * @author Simon Dräger
 * @version 4.3.18
 *
 * Der Haupt-Service, der im Hintergrund Push-Nachrichten sendet.
 *
 */

public class MainService extends IntentService {
    public MainService() {
        super("MainService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.i("INFO", "Entering onHandleIntent...");
        while (getApplicationContext().getSharedPreferences("vortex.vp_today.app", Context.MODE_PRIVATE).getBoolean("fetchHtmlPushes", true)) {
            try {
                Calendar c = Calendar.getInstance();
                String dt = Util.makeDate(c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH), c.get(Calendar.YEAR));
                String unf = Util.fetchUnfiltered(dt);
                ArrayList<String> infos = new ArrayList<>(Arrays.asList(Util.filterHTML(Jsoup.parse(unf), Util.getSettingStufe(getApplicationContext()))));

                HashSet<String> known =
                        new HashSet<>(getApplicationContext().getSharedPreferences("vortex.vp_today.app",
                                Context.MODE_PRIVATE).getStringSet("knownInfos", null));

                /* Jeden bereits benachrichtigten Eintrag überspringen */
                if (known != null) {
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
                    else
                        Util.makePushNotification(getApplicationContext(), "Unbekannt:", str);

                    if (known == null) {
                        known = new HashSet<>();
                    }
                    known.add(str);

                    /* Die Änderungen speichern */
                    getApplicationContext().getSharedPreferences("vortex.vp_today.app",
                            Context.MODE_PRIVATE).edit().putStringSet("knownInfos", known).commit();
                }

                /* Jede Sekunde updaten */
                Thread.sleep(1000);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        getApplicationContext().getSharedPreferences("vortex.vp_today.app", Context.MODE_PRIVATE).edit().remove("fetchHtmlPushes").apply();
        Log.i("INFO", "Leaving onHandleIntent...");
    }
}
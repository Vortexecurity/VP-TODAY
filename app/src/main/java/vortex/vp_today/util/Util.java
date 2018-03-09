package vortex.vp_today.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import org.joda.time.LocalDate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import es.dmoral.toasty.Toasty;
import vortex.vp_today.R;
import vortex.vp_today.logic.VPInfo;
import vortex.vp_today.logic.VPKind;
import vortex.vp_today.logic.VPRow;
import vortex.vp_today.mail.BackgroundMail;

/**
 * @author Simon Dräger
 * @author Florian Koll
 * @version 6.3.18
 */

public final class Util {

    private static Activity activity;
    private static Context context;

    private static Random rand;
    private static final String ALPHANUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
    private static AtomicInteger atomInt;
    private static List<String> lstStufen;
    private static List<String> lstKlassen;

    static {
        rand = new Random();
        atomInt = new AtomicInteger(0);
        lstStufen = new ArrayList<>();
        lstKlassen = new ArrayList<>();

        if (lstStufen.isEmpty()) {
            lstStufen.addAll(Arrays.asList("5", "6", "7", "8", "9", "EF", "Q1", "Q2"));
        }

        if (lstKlassen.isEmpty()) {
            lstKlassen.addAll(Arrays.asList("A", "B", "C", "D"));
        }
    }

    public static void setup(Activity n){
        activity = n;
        context = activity.getApplicationContext();
        Log.i("[UTIL]","Set up >> " + activity.getLocalClassName());
    }

    /**
     * @return Die Stufenliste
     */
    @NonNull
    public static String[] getStufen() {
        return lstStufen.toArray(new String[0]);
    }

    /**
     * @return Die Klassenliste in Buchstaben
     */
    @NonNull
    public static String[] getKlassen() {
        return lstKlassen.toArray(new String[0]);
    }

    /**
     * @return Eine Liste der Kurse der Q1
     */
    @NonNull
    public static String[] getKurseQ1(@NonNull Context ctx) {
        return ctx.getResources().getStringArray(R.array.KurseQ1);
    }

    @Nullable
    public static String[] getSelectedKurse(@NonNull Context ctx) {
        Tuple<ArrayList<String>, ArrayList<Boolean>> tupSelects = Util.getGsonObject(ctx, ctx.getString(R.string.settingkurse), Tuple.class);

        try {
            if (tupSelects.x.get(0) != null) {
                Log.e("getSelectedKurse", "tupSelects.x type: " + tupSelects.x.getClass().toString());
                if (tupSelects.x.get(0).equals("")) {
                    Log.e("getSelectedKurse", "Returning null, x get 0 equals \"\"");
                    return null;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }

        ArrayList<String> selectedKurse = null;

        if (tupSelects.x != null) {
            selectedKurse = new ArrayList<>();
            for (int i = 0; i < tupSelects.x.size(); i++) {
                if (tupSelects.y.get(i)) {
                    selectedKurse.add(tupSelects.x.get(i));
                }
            }
        }

        if (tupSelects == null)
            Log.e("getSelectedKurse", "Returning null, tupSelects is null");
        else if (tupSelects.x == null)
            Log.e("getSelectedKurse", "Returning null, x is null");

        return selectedKurse.toArray(new String[0]);
    }

    public static final int getNotificationID() {
        return atomInt.incrementAndGet();
    }

    public static void putGsonObject(@NonNull Context ctx, @NonNull String tag, @Nullable Object obj) {
        SharedPreferences.Editor prefsEditor = ctx.getSharedPreferences("vortex.vp_today.app", Context.MODE_PRIVATE).edit();
        Gson gson = new Gson();
        String json = gson.toJson(obj);
        prefsEditor.putString(tag, json);
        prefsEditor.apply();
    }

    @Nullable
    public static Object getGsonObject(@NonNull Context ctx, @NonNull String tag) {
        SharedPreferences prefs = ctx.getSharedPreferences("vortex.vp_today.app", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(tag, "");
        return gson.fromJson(json, Object.class);
    }

    @Nullable
    public static <T extends Object> T getGsonObject(@NonNull Context ctx, @NonNull String tag, Class type) {
        SharedPreferences prefs = ctx.getSharedPreferences("vortex.vp_today.app", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(tag, "");
        Log.e("getGsonObject", "json: " + json);
        return ((T)gson.fromJson(json, type));
    }

    public static synchronized String makeVpDate(String actualDate) {
        try {
            SimpleDateFormat format1 = new SimpleDateFormat("d.M.yyyy", Locale.ENGLISH);
            LocalDate localDate = new LocalDate(format1.parse(actualDate));

            Log.i("makeVpDate", "localDate: " + localDate.toString());

            return localDate.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("deprecated")
    @Nullable
    public static synchronized TwoFormatDate[] getVPDates() {
        String unfiltered = getGod();
        String[] lines = unfiltered.split("\n");
        Log.i("getvpdates", "lines length: " + lines.length);
        ArrayList<TwoFormatDate> dates = new ArrayList<>();
        String href = "href=\"/god/";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-M-d", Locale.ENGLISH);

        for (String line : lines) {
            Log.i("getvpdates", "reading a line");
            if (line.contains(href)) {
                String trimmed = line.trim();
                String extractedDate = trimmed.substring(trimmed.lastIndexOf("/god/") + 5, trimmed.length() - 2);

                Log.i("getvpdates", "extractedDate: " + extractedDate);
                try {
                    LocalDate d = new LocalDate(format.parse(extractedDate));
                    TwoFormatDate tfd = new TwoFormatDate(extractedDate, d);

                    Log.i("getvpdates", "tfd: " + tfd.toString());

                    if (dates.contains(tfd))
                        continue;

                    Log.i("getvpdates", "Adding tfd: " + (tfd == null ? "null" : "not null"));

                    dates.add(tfd);
                } catch (ParseException ex) {
                    /* Datum wurde falsch geparst, anzunehmen, dass die anderen auch falsch werden */
                    ex.toString();
                    return null;
                }
            }
        }

        return dates.toArray(new TwoFormatDate[0]);
    }

    public static synchronized void ShowYesNoDialog(@NonNull String text,
                                                    @NonNull final DialogInterface.OnClickListener positiveClicked,
                                                    @NonNull final DialogInterface.OnClickListener negativeClicked) {
        try {
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            positiveClicked.onClick(dialog, which);
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            negativeClicked.onClick(dialog, which);
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage(text)
                    .setPositiveButton("Ja", listener)
                    .setNegativeButton("Nein", listener).show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * @param preselectedItems Look the positions up in strings.xml/KurseQ1 etc. (complete list)
     * @return The selected items
     * @throws AssertionError wird ausgelöst, wenn preselectedItems != null und Länge != KurseQ1.length
     */
    @Nullable
    public static Tuple<String[], Boolean[]> ShowKurseDialogQ1(@Nullable boolean[] preselectedItems,
                                                               DialogInterface.OnMultiChoiceClickListener multiListener,
                                                               DialogInterface.OnClickListener positiveClick,
                                                               DialogInterface.OnClickListener negativeClick) throws AssertionError {
        final Resources res = context.getResources();
        final int q1Len = res.getStringArray(R.array.KurseQ1).length;
        final ArrayList<String> selectedItems = new ArrayList<>(q1Len);
        final ArrayList<Boolean> boolSelectedItems = new ArrayList<>(q1Len);
        final String[] items = res.getStringArray(R.array.KurseQ1);
        final DlgResult result = new DlgResult();

        /* Wenn items ausgewählt wurden */
        if (preselectedItems != null) {
            assert preselectedItems.length == q1Len : "preselectedItems must be the length of KurseQ1!";
            for (int i = 0; i < preselectedItems.length; i++) {
                selectedItems.add(items[i]);
                boolSelectedItems.add(preselectedItems[i]);
            }
        } else {
            /* Sonst alles auf false setzen */
            Log.i("ShowKurseDialogQ1", "size: " + boolSelectedItems.size());
            for (int i = 0; i < q1Len; i++) {
                boolSelectedItems.set(i, false);
            }
        }

        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setTitle("Kurse auswählen...")
                .setMultiChoiceItems(items, preselectedItems, multiListener)
                .setPositiveButton("OK", positiveClick)
                .setNegativeButton("Abbrechen", negativeClick)
                .create();

        dialog.show();

        if (result.getResult() == DialogResult.OK)
            return new Tuple<>(selectedItems.toArray(new String[0]), boolSelectedItems.toArray(new Boolean[0]));
        return null;
    }

    @Nullable
    public static boolean[] BoolToTypeBool(@Nullable Boolean[] input) {
        if (input == null)
            return null;

        boolean[] output = new boolean[input.length];

        for (int i = 0; i < input.length; i++)
            output[i] = input[i].booleanValue();

        return output;
    }

    @Nullable
    public static boolean[] StrArrToBoolArr(@Nullable String[] input) {
        if (input == null)
            return null;

        boolean[] output = new boolean[input.length];

        for (int i = 0; i < input.length; i++)
            output[i] = Boolean.parseBoolean(input[i]);

        return output;
    }

    public static String getSettingStufe(Context ctx) {
        return ctx.getSharedPreferences("vortex.vp_today.app", Context.MODE_PRIVATE).getString("stufe", "");
    }

    public static String getSettingKlasse(Context ctx) {
        return ctx.getSharedPreferences("vortex.vp_today.app", Context.MODE_PRIVATE).getString("klasse", "");
    }

    public static boolean isInternetConnected(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public static String[] getDevEmails(Context ctx) {
        return new String[]{ctx.getResources().getString(R.string.simonemail),
                ctx.getResources().getString(R.string.melvinemail),
                ctx.getResources().getString(R.string.florianemail)
        };
    }

    public static void sendBotEmail(@NonNull String[] to,
                                    @NonNull String subj,
                                    @NonNull String body,
                                    @Nullable String sendingMessage,
                                    @Nullable BackgroundMail.OnSuccessCallback succ,
                                    @Nullable BackgroundMail.OnFailCallback fail) {
        BackgroundMail bm = new BackgroundMail(activity);
        bm.setGmailUserName(activity.getApplicationContext().getString(R.string.botemail));
        bm.setGmailPassword(activity.getApplicationContext().getString(R.string.botpwd));
        bm.setSendingMessage(sendingMessage);
        bm.setMailTo(TextUtils.join(",", to));
        bm.setFormSubject(subj);
        bm.setFormBody(body);

        if (succ != null)
            bm.setOnSuccessCallback(succ);
        if (fail != null)
            bm.setOnFailCallback(fail);

        bm.send();
    }

    public static String genRandString(int length) {
        String result = "";

        for (int i = 0; i < length; i++) {
            result += ALPHANUM.charAt(rand.nextInt(ALPHANUM.length() - 1));
        }

        return result;
    }

    public static String generateClientID() {
        return UUID.randomUUID().toString();
    }

    public static boolean anyMatch(String str, String[] items) {
        for (int i = 0; i < items.length; i++) {
            if (str.contains(items[i]))
                return true;
        }
        return false;
    }

    public static void makePushNotification(Context ctx, String title, String text) {
        try {
            NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

            Notification.Builder notify = new Notification.Builder(ctx);
            notify.setContentTitle(title);
            notify.setContentText(text);
            notify.setSmallIcon(R.mipmap.ic_launcher);

            Notification n = notify.build();

            nm.notify(getNotificationID(), n);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * @return null on error.
     */
    @Nullable
    public static String makeDate(int day, int month, int year) {
        String res = "";

        int _month = month + 1;

        if (day >= 10 && _month >= 10)
            res = year + "-" + _month + "-" + day;
        else if (day < 10 && _month >= 10)
            res = year + "-" + _month + "-0" + day;
        else if (_month < 10 && day >= 10)
            res = year + "-0" + _month + "-" + day;
        else
            res = year + "-0" + _month + "-0" + day;

        if (!res.equals(""))
            return res;

        return null;
    }

    public static synchronized String getGod() {
        try {

            String urlS = "https://vp.gymnasium-odenthal.de/god";
            String authStringEnc = "dnA6Z29kOTIwMQ==";

            Log.e("getGod", "URL: " + urlS);

            URL url = new URL(urlS);
            URLConnection urlConnection = url.openConnection();
            urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);

            InputStream is = urlConnection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);

            int numCharsRead;
            char[] charArray = new char[1024];
            StringBuffer sb = new StringBuffer();

            while ((numCharsRead = isr.read(charArray)) > 0) {
                sb.append(charArray, 0, numCharsRead);
            }

            String result = sb.toString();

            return result;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * @return null on error.
     */
    @Nullable
    public static synchronized String fetchUnfiltered(@NonNull String vpDate) throws AssertionError {
        try {
            assert !vpDate.equals("") : "vpDate kann nicht leer sein.";

            String urlS = "https://vp.gymnasium-odenthal.de/god/" + vpDate;
            String authStringEnc = "dnA6Z29kOTIwMQ==";

            Log.e("fetchUnfiltered", "URL: " + urlS);

            URL url = new URL(urlS);
            URLConnection urlConnection = url.openConnection();
            urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);

            InputStream is = urlConnection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);

            int numCharsRead;
            char[] charArray = new char[1024];
            StringBuffer sb = new StringBuffer();

            while ((numCharsRead = isr.read(charArray)) > 0) {
                sb.append(charArray, 0, numCharsRead);
            }

            String result = sb.toString();

            return result;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * @param sub Klassenbuchstabe; Bsp 6C
     * @param sub Klassenbuchstabe; Bsp 6C
     * @return TriTuple: 1 = msgotd / error string 2 = version 3 = html content
     * @author Melvin Zähl
     * @author Simon Dräger
     */
    @Nullable
    public static synchronized TriTuple<String, Integer, String[]> filterHTML(Document d, String stufe, String sub) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toasty.info(activity.getApplicationContext(), "Aktualisiere...").show();
            }
        });

        /* Hilfsvariable, sodass stufe nicht direkt verändert wird */
        String _stufe = stufe;
        String msgotd = null;
        int version = 0;
        boolean showedToast = false;

        if (_stufe == null || _stufe.equals(""))
            _stufe = "05";

        /* Wenn 5 <= stufe < EF */
        if (!Character.isLetter(_stufe.charAt(0))) {
            _stufe = "0" + _stufe;
            _stufe = _stufe + sub;
        }

        Elements elements = d.select("tr[data-index*='" + _stufe + "']");
        Element strong = d.selectFirst("strong");
        ArrayList<String> s = null;

        if (elements.first() == null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toasty.warning(activity.getApplicationContext(), "Für heute wurden keine passenden Vertretungen gefunden!").show();
                }
            });
            if (strong != null)
                version = Integer.parseInt(strong.text());
            showedToast = true;
        } else {
            Log.i("filterHTML", "s = new ArrayList");
            s = new ArrayList<>();

            Element elem = d.selectFirst("p");
            if (elem.text().equals("Für diesen Tag existiert derzeit kein Vertretungsplan. Bitten schauen Sie später nochmal vorbei!")) {
                if (elem != null) {
                    msgotd = elem.text();
                }
            } else {
                Element msg = d.selectFirst("div.alert");
                if (msg != null)
                    msgotd = msg.text();

                if (strong != null)
                    version = Integer.parseInt(strong.text());

                for (Element e : elements) {
                    /* Manchmal sind Einträge im VP mehrere Male vorhanden, also nur einmal in die Liste tun. */
                    if (e != null && !(s.contains(e.text())))
                        s.add(e.text());
                }
            }
        }

        if (!showedToast) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toasty.success(context, "Aktualisiert!").show();
                }
            });
        }

        return new TriTuple<>(msgotd, Integer.valueOf(version), s == null ? null : s.toArray(new String[0]));
    }

    /**
     * @author Simon Dräger
     */
    @Nullable
    public static synchronized TriTuple<String, Integer, String[]> filterHTML(@NonNull final Activity actv, Document d, String stufe, String[] kurse) {
        actv.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toasty.info(actv.getApplicationContext(), "Aktualisiere...").show();
            }
        });

        String msgotd = null;
        int version = 0;
        boolean showedToast = false;

        assert Character.isLetter(stufe.charAt(0)) : "Die Stufe muss EF oder höher sein.";

        Elements elements = d.select("tr[data-index*='" + stufe + "']");
        Element strong = d.selectFirst("strong");
        ArrayList<String> s = null;

        if (elements.first() == null) {
            actv.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toasty.warning(actv.getApplicationContext(), "Für heute wurden keine passenden Vertretungen gefunden!").show();
                }
            });
            if (strong != null)
                version = Integer.parseInt(strong.text());
            showedToast = true;
        } else {
            Log.i("filterHTML", "s = new ArrayList");
            s = new ArrayList<>();

            Element elem = d.selectFirst("p");
            if (elem.text().equals("Für diesen Tag existiert derzeit kein Vertretungsplan. Bitten schauen Sie später nochmal vorbei!")) {
                if (elem != null) {
                    msgotd = elem.text();
                }
            } else {
                Element msg = d.selectFirst("div.alert");
                if (msg != null)
                    msgotd = msg.text();

                if (strong != null)
                    version = Integer.parseInt(strong.text());

                Element tbody = d.selectFirst("tbody");

                Log.i("filterHTMLkurse", "kurse: " + TextUtils.join(",", kurse));

                if (tbody != null) {
                    Log.i("filterHTMLkurse", "tbody != null");
                    /* Für jeden Listeneintrag */
                    for (Element tr : tbody.children()) {
                        Log.i("filterHTMLkurse", "tr child: " + tr.text());
                        String trText = tr.text();
                        /* Wenn das Element ein unbesuchtes, gültiges ist */
                        if (tr != null && !s.contains(trText)) {
                            Log.i("filterHTMLkurse", "child not null, not contained");
                            /* Wenn das Element ein gesuchter Kurs ist */
                            if (anyMatch(trText, kurse)) {
                                /* Wenn die Stufe korrekt ist */
                                if (trText.contains(stufe)) {
                                    Log.i("filterHTMLkurse", "anyMatch success, stufe check success, adding");
                                    s.add(trText);
                                }
                            } else
                                Log.i("filterHTMLkurse", "anyMatch failure");
                        } else
                            Log.i("filterHTMLkurse", "tr null or contained");
                    }
                }
            }
        }

        if (!showedToast) {
            actv.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toasty.success(actv.getApplicationContext(), "Aktualisiert!").show();
                }
            });
        }

        return new TriTuple<>(msgotd, Integer.valueOf(version), s == null ? null : s.toArray(new String[0]));
    }

    @Nullable
    public static String[] ArrListToArr(@Nullable List<String> arr) {
        String[] out = new String[arr.size()];

        for (int i = 0; i < arr.size(); i++)
            out[i] = arr.get(i);

        return out;
    }

    /**
     * @author Simon Dräger
     */
    @Nullable
    public static synchronized VPInfo getCurrentInfo(@NonNull Document d) {
        Elements elements = d.select("tr[data-index]");

        VPInfo inf = new VPInfo();

        //Log.i("getCurrentInfo/elements", ArrListToArr(elements.eachText()));

        for (Element e : elements) {
            if (e != null && !(inf.getRows().contains(e.text()))) {
                VPRow r = new VPRow();
                Elements subs = e.getAllElements();

                Log.i("getCurrentInfo", "Element: " + e.text());

                switch (subs.select("data-type").get(0).text().toLowerCase()) {
                    case "vertretung":
                        r.setArt(VPKind.VERTRETUNG);
                        break;
                    case "entfall":
                        r.setArt(VPKind.ENTFALL);
                        break;
                    case "raum-vertretung":
                        r.setArt(VPKind.RAUMVERTRETUNG);
                        break;
                    case "eigenv. arbeiten":
                        r.setArt(VPKind.EIGENVARBEITEN);
                        break;
                }

                String art = r.getArt().getName();
                int stunde = Integer.parseInt(subs.select("data-hour").get(0).text());
                String fach = subs.select("data-subject").get(0).text().replaceAll("\\s+", " ");
                String klasse = subs.select("data-form").get(0).text().replace("", "");
                String raum = subs.select("data-room").get(0).text();
                String statt = subs.select("data-instead").get(0).text();
                String bemerkung = subs.select("data-notice").get(0).text();

                Log.i("getCurrentInfo", "art: " + art);
                Log.i("getCurrentInfo", "stunde: " + stunde);
                Log.i("getCurrentInfo", "fach: " + fach);
                Log.i("getCurrentInfo", "klasse: " + klasse);
                Log.i("getCurrentInfo", "raum: " + raum);
                Log.i("getCurrentInfo", "statt: " + statt);
                Log.i("getCurrentInfo", "bemerkung: " + bemerkung);

                r.setStunde(stunde);
                /* Unnötige Leerzeichen wegmachen */
                r.setFach(fach);
                r.setKlasse(klasse);
                r.setRaum(raum);
                r.setStatt(statt);
                r.setBemerkung(bemerkung);

                inf.addRow(r);
            }
        }

        return inf;
    }
}

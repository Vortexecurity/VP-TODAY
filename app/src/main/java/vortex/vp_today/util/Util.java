package vortex.vp_today.util;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.joda.time.LocalDate;
import org.jsoup.Jsoup;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import es.dmoral.toasty.Toasty;
import service.MainService;
import vortex.vp_today.R;
import vortex.vp_today.logic.VPInfo;
import vortex.vp_today.logic.VPKind;
import vortex.vp_today.logic.VPRow;
import vortex.vp_today.mail.BackgroundMail;

/**
 * @author Simon Dräger
 * @author Melvin Zähl
 * @author Florian Koll
 * @version 17.3.18
 */

public final class Util {

    private static Random rand;
    private static final String ALPHANUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
    private static AtomicInteger atomInt;
    private static List<String> lstStufen;
    private static List<String> lstKlassen;

    public static final boolean D = false;

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

    public static boolean isAppRunning(final Context context, final String packageName) {
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningAppProcessInfo> procInfos = activityManager.getRunningAppProcesses();

        if (procInfos != null) {
            for (final ActivityManager.RunningAppProcessInfo processInfo : procInfos) {
                if (processInfo.processName.equals(packageName)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static int MillisToSecs(int millis) {
        return (millis / 1000);
    }

    public static int SecsToMins(int secs) {
        return ((secs % 3600) / 60);
    }

    public static long MinsToMillis(int mins) {
        return TimeUnit.MINUTES.toMillis(mins);
    }

    public static boolean isMainServiceRunning(@NonNull Context ctx) {
        ActivityManager manager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (MainService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static synchronized void setProgressMax(ProgressBar pb, int max) {
        pb.setMax(max * 100);
    }

    public static void setProgressAnimate(ProgressBar pb, int progressTo) {
        ObjectAnimator animation = ObjectAnimator.ofInt(pb, "progress", pb.getProgress(), progressTo * 100);
        animation.setDuration(200);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.start();
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

        String[] kurse = tupSelects.x.toArray(new String[0]);

        Log.i("getSelectedKurse", "kurse: " + TextUtils.join(",", kurse));

        return kurse;

        /*try {
            if (tupSelects.x.get(0) != null) {
                if (Util.D)
                    Log.e("getSelectedKurse", "tupSelects.x type: " + tupSelects.x.getClass().toString());
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
            if (Util.D) Log.e("getSelectedKurse", "Returning null, tupSelects is null");
            else if (tupSelects.x == null)
                if (Util.D) Log.e("getSelectedKurse", "Returning null, x is null");

        return selectedKurse.toArray(new String[0]);*/
    }

    public static final int getNotificationID() {
        return atomInt.incrementAndGet();
    }

    public static final boolean equalsWithNulls(Object a, Object b) {
        if (a==b) return true;
        if ((a==null) || (b==null)) return false;
        return a.equals(b);
    }

    public static void putGsonObject(@NonNull Context ctx, @NonNull String tag, @Nullable Object obj, @Nullable TypeToken tok) {
        //Type fooType = new TypeToken<Foo<Bar>>() {}.getType();
        SharedPreferences.Editor prefsEditor = ctx.getSharedPreferences("vortex.vp_today.app", Context.MODE_PRIVATE).edit();
        Gson gson = new Gson();
        String json;
        if (tok != null) {
            json = gson.toJson(obj, tok.getType());
        } else {
            json = gson.toJson(obj);
        }
        prefsEditor.putString(tag, json);
        prefsEditor.apply();
    }

    @Nullable
    public static <T> T getGsonObject(@NonNull Context ctx, @NonNull String tag, @NonNull TypeToken tok) {
        SharedPreferences prefs = ctx.getSharedPreferences("vortex.vp_today.app", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = prefs.getString(tag, null);
        return gson.fromJson(json, tok.getType());
    }

    @Nullable
    public static <T extends Object> T getGsonObject(@NonNull Context ctx, @NonNull String tag, @NonNull Class type) {
        try {
            SharedPreferences prefs = ctx.getSharedPreferences("vortex.vp_today.app", Context.MODE_PRIVATE);
            Gson gson = new Gson();
            String json = prefs.getString(tag, null);
            if (D) Log.e("getGsonObject", "json: " + json);
            return ((T) gson.fromJson(json, type));
        } catch (Exception ex) {
        }
        return null;
    }

    public static synchronized String makeVpDate(String actualDate) {
        try {
            SimpleDateFormat format1 = new SimpleDateFormat("d.M.yyyy", Locale.ENGLISH);
            LocalDate localDate = new LocalDate(format1.parse(actualDate));

            if (D) Log.i("makeVpDate", "localDate: " + localDate.toString());

            return localDate.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Nullable
    public static synchronized TwoFormatDate[] getVPDates() {
        String unfiltered = getGod();
        String[] lines = unfiltered.split("\n");

        if (D) Log.i("getvpdates", "lines length: " + lines.length);

        ArrayList<TwoFormatDate> dates = new ArrayList<>();
        String href = "href=\"/god/";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-M-d", Locale.ENGLISH);

        for (String line : lines) {
            if (D) Log.i("getvpdates", "reading a line");
            if (line.contains(href)) {
                String trimmed = line.trim();
                String extractedDate = trimmed.substring(trimmed.lastIndexOf("/god/") + 5, trimmed.length() - 2);

                if (D) Log.i("getvpdates", "extractedDate: " + extractedDate);
                try {
                    LocalDate d = new LocalDate(format.parse(extractedDate));
                    TwoFormatDate tfd = new TwoFormatDate(extractedDate, d);

                    if (D) Log.i("getvpdates", "tfd: " + tfd.toString());

                    if (dates.contains(tfd))
                        continue;

                    if (D)
                        Log.i("getvpdates", "Adding tfd: " + (tfd == null ? "null" : "not null"));

                    dates.add(tfd);
                } catch (ParseException ex) {
                    /* Datum wurde falsch geparst, annehmen, dass die anderen auch falsch werden */
                    ex.toString();
                    return null;
                }
            }
        }

        return dates.toArray(new TwoFormatDate[0]);
    }

    public static synchronized void ShowLicenseDialog(@NonNull Activity actv) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(actv);
            builder.setTitle("License");

            TextView myMsg = new TextView(actv);
            myMsg.setText(actv.getString(R.string.license));
            myMsg.setGravity(Gravity.CENTER_HORIZONTAL);
            builder.setView(myMsg);

            builder.setPositiveButton("OK", null);

            builder.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static synchronized void ShowYesNoDialog(@NonNull Activity actv,
                                                    @NonNull String text,
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

            AlertDialog.Builder builder = new AlertDialog.Builder(actv);
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
    public static void ShowKurseDialogQ1
    (
            @NonNull Activity actv,
            @Nullable boolean[] preselectedItems,
            DialogInterface.OnMultiChoiceClickListener multiListener,
            DialogInterface.OnClickListener positiveClick,
            DialogInterface.OnClickListener negativeClick
    ) throws AssertionError {

        final Resources res = actv.getApplicationContext().getResources();
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
            if (D) Log.i("ShowKurseDialogQ1", "size: " + boolSelectedItems.size());
            for (int i = 0; i < q1Len; i++) {
                boolSelectedItems.set(i, false);
            }
        }

        AlertDialog dialog = new AlertDialog.Builder(actv)
                .setTitle("Kurse auswählen...")
                .setMultiChoiceItems(items, preselectedItems, multiListener)
                .setPositiveButton("OK", positiveClick)
                .setNegativeButton("Abbrechen", negativeClick)
                .create();

        dialog.show();
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

    public static String getSettingStufe(@NonNull Context ctx) {
        return ctx.getSharedPreferences("vortex.vp_today.app", Context.MODE_PRIVATE).getString("stufe", "");
    }

    public static String getSettingKlasse(@NonNull Context ctx) {
        return ctx.getSharedPreferences("vortex.vp_today.app", Context.MODE_PRIVATE).getString("klasse", "");
    }

    public static boolean isInternetConnected(@NonNull Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    public static String[] getDevEmails(@NonNull Context ctx) {
        return new String[]{ctx.getResources().getString(R.string.simonemail),
                ctx.getResources().getString(R.string.melvinemail),
                ctx.getResources().getString(R.string.florianemail)
        };
    }

    public static void sendBotEmail(@NonNull Activity actv,
                                    @NonNull String[] to,
                                    @NonNull String subj,
                                    @NonNull String body,
                                    @Nullable String sendingMessage,
                                    @Nullable BackgroundMail.OnSuccessCallback succ,
                                    @Nullable BackgroundMail.OnFailCallback fail) {
        BackgroundMail bm = new BackgroundMail(actv);
        bm.setGmailUserName(actv.getApplicationContext().getString(R.string.botemail));
        bm.setGmailPassword(actv.getApplicationContext().getString(R.string.botpwd));
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

    /**
     * Konvertiert ein Datum in ein VP-Datum
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

            if (D) Log.e("getGod", "URL: " + urlS);

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

            if (D) Log.e("fetchUnfiltered", "URL: " + urlS);

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
    public static synchronized TriTuple<String, Integer, String[]> filterHTML(@NonNull final Activity actv, Document d, String stufe, String sub) {
        actv.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toasty.info(actv.getApplicationContext(), "Aktualisiere...").show();
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
            if (D) Log.i("filterHTML", "s = new ArrayList");
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
            actv.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toasty.success(actv, "Aktualisiert!").show();
                }
            });
        }

        return new TriTuple<>(msgotd, Integer.valueOf(version), s == null ? null : s.toArray(new String[0]));
    }

    /**
     * Service-Version
     *
     * @author Simon Dräger
     */
    @Nullable
    public static synchronized TriTuple<String, Integer, String[]> filterHTMLService(Document d, String stufe, String sub) {
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
            if (strong != null)
                version = Integer.parseInt(strong.text());
        } else {
            if (D) Log.i("filterHTML", "s = new ArrayList");
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

        return new TriTuple<>(msgotd, Integer.valueOf(version), s == null ? null : s.toArray(new String[0]));
    }

    /**
     * @author Simon Dräger
     */
    @Nullable
    public static synchronized TriTuple<String, Integer, VPInfo> filterHTML(@NonNull final Activity actv,
                                                                            Document d,
                                                                            String stufe,
                                                                            String[] kurse) throws AssertionError {
        assert stufe.equalsIgnoreCase("EF") ||
                stufe.equalsIgnoreCase("Q1") ||
                stufe.equalsIgnoreCase("Q2")
                : "Die Stufe muss EF oder höher sein";

        String msgotd = null;
        int version = 0;

        Elements elements = d.select("tr[data-index*='" + stufe + "']");
        Element strong = d.selectFirst("strong");
        VPInfo info = new VPInfo();

        if (elements.first() == null) {
            actv.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toasty.warning(actv.getApplicationContext(), "Für heute wurden keine passenden Vertretungen gefunden!").show();
                }
            });
            if (strong != null)
                version = Integer.parseInt(strong.text());
        } else {
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

                if (D) Log.i("filterHTMLkurse", "kurse: " + TextUtils.join(",", kurse));

                if (tbody != null) {
                    if (D) Log.i("filterHTMLkurse", "tbody != null");

                    /* Für jeden Listeneintrag am Tag */
                    for (Element tr : tbody.select("tr")) {
                        if (D) Log.i("filterHTMLkurse", "tr child != null");
                        if (D) Log.i("filterHTMLkurse", "tr child: " + tr.text());

                        VPRow row = new VPRow();
                        boolean breakk = false;

                        for (Element td : tr.select("td")) {
                            String selector = td.cssSelector();
                            if (D) Log.i("filterHTMLkurse", "cssSelector: " + selector);

                            if (selector.contains("td.data-form")) {
                                Element dataForm = td.selectFirst("td.data-form");
                                String formTxt = dataForm.text();
                                if (!formTxt.equals(stufe)) {
                                    if (D) Log.i("filterHTMLkurse", "class was: " + formTxt);
                                    breakk = true;
                                    break;
                                }
                                if (D) Log.i("filterHTMLkurse", "correct class: " + formTxt);
                                row.setKlasse(formTxt);
                            } else if (selector.contains("td.data-type")) {
                                Element dataType = td.selectFirst("td.data-type");
                                VPKind art = VPKind.fromString(dataType.text());
                                if (art == VPKind.PAUSENAUFSICHT ||
                                        art == VPKind.KLAUSUR ||
                                        art == VPKind.BETREUUNG ||
                                        art == VPKind.LEHRERTAUSCH)
                                    continue;
                                if (D) Log.i("filterHTMLkurse", "dataType: " + dataType.text());
                                row.setArt(VPKind.fromString(dataType.text()));
                            } else if (selector.contains("td.data-hour")) {
                                Element dataHour = td.selectFirst("td.data-hour");
                                if (D) Log.i("filterHTMLkurse", "datahour " + dataHour.text());

                                String txt = dataHour.text();

                                if (txt.contains("/")) {
                                    row.setStunden(StrArrToIntArr(txt.split("/")));
                                    continue;
                                }

                                row.setStunde(Integer.parseInt(dataHour.text()));
                            } else if (selector.contains("td.data-subject")) {
                                Element dataSubject = td.selectFirst("td.data-subject");
                                String txtSubject = dataSubject.text();
                                if (D)
                                    Log.i("filterHTMLkurse", "datasubject " + dataSubject.text());

                                if (!anyMatch(txtSubject, kurse)) {
                                    if (D)
                                        Log.i("filterHTMLkurse", "not matching subject: " + txtSubject);
                                    breakk = true;
                                    break;
                                }

                                row.setFach(dataSubject.text());
                            } else if (selector.contains("td.hidden-xs data-agent")) {
                                Element dataAgent = td.selectFirst("td.hidden-xs.data-agent");
                                row.setVertreter(dataAgent.text());
                            } else if (selector.contains("td.hidden-xs.data-room")) {
                                Element dataRoom = td.selectFirst("td.hidden-xs.data-room");
                                row.setRaum(dataRoom.text());
                            } else if (selector.contains("td.hidden-xs.data-instead")) {
                                Element dataInstead = td.selectFirst("td.hidden-xs.data-instead");
                                row.setStatt(dataInstead.text());
                            } else if (selector.contains("td.hidden-xs.data-notice")) {
                                Element dataNotice = td.selectFirst("td.hidden-xs.data-notice");
                                row.setBemerkung(dataNotice.text());
                            }
                        }

                        if (tr != null && !info.contains(row) && !breakk) {
                            if (D) Log.i("filterHTMLkurse", "row not contained, adding");
                            info.addRow(row);
                        } else {
                            if (breakk) {
                                if (D) Log.i("filterHTMLkurse", "Incorrect class, skipping");
                            } else {
                                if (D) Log.i("filterHTMLkurse", "row contained, skipping");
                            }
                        }

                    }
                }
            }
        }

        if (info != null) {
            if (D) Log.i("filterHTMLkurse", "info != null");
            //if (D) Log.i("filterHTMLkurse", "info.getRows().get(0): " + info.getRows().get(0).toString());
        } else {
            if (D) Log.i("filterHTMLkurse", "info = null");
        }

        return new TriTuple<String, Integer, VPInfo>(msgotd, version, info);
    }

    /**
     * @author Simon Dräger
     */
    @Nullable
    public static synchronized TriTuple<String, Integer, VPInfo> filterHTMLService(Document d,
                                                                                   String stufe,
                                                                                   String[] kurse) throws AssertionError {
        assert stufe.equalsIgnoreCase("EF") ||
                stufe.equalsIgnoreCase("Q1") ||
                stufe.equalsIgnoreCase("Q2")
                : "Die Stufe muss EF oder höher sein";

        String msgotd = null;
        int version = 0;

        Elements elements = d.select("tr[data-index*='" + stufe + "']");
        Element strong = d.selectFirst("strong");
        VPInfo info = new VPInfo();

        if (elements.first() == null) {
            if (strong != null)
                version = Integer.parseInt(strong.text());
        } else {
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

                if (D) Log.i("filterHTMLkurse", "kurse: " + TextUtils.join(",", kurse));

                if (tbody != null) {
                    if (D) Log.i("filterHTMLkurse", "tbody != null");

                    /* Für jeden Listeneintrag am Tag */
                    for (Element tr : tbody.select("tr")) {
                        if (D) Log.i("filterHTMLkurse", "tr child != null");
                        if (D) Log.i("filterHTMLkurse", "tr child: " + tr.text());

                        VPRow row = new VPRow();
                        boolean breakk = false;

                        for (Element td : tr.select("td")) {
                            String selector = td.cssSelector();
                            if (D) Log.i("filterHTMLkurse", "cssSelector: " + selector);

                            if (selector.contains("td.data-form")) {
                                Element dataForm = td.selectFirst("td.data-form");
                                String formTxt = dataForm.text();
                                if (!formTxt.equals(stufe)) {
                                    if (D) Log.i("filterHTMLkurse", "class was: " + formTxt);
                                    breakk = true;
                                    break;
                                }
                                if (D) Log.i("filterHTMLkurse", "correct class: " + formTxt);
                                row.setKlasse(formTxt);
                            } else if (selector.contains("td.data-type")) {
                                Element dataType = td.selectFirst("td.data-type");
                                VPKind art = VPKind.fromString(dataType.text());
                                if (art == VPKind.PAUSENAUFSICHT ||
                                        art == VPKind.KLAUSUR ||
                                        art == VPKind.BETREUUNG ||
                                        art == VPKind.LEHRERTAUSCH)
                                    continue;
                                if (D) Log.i("filterHTMLkurse", "dataType: " + dataType.text());
                                row.setArt(VPKind.fromString(dataType.text()));
                            } else if (selector.contains("td.data-hour")) {
                                Element dataHour = td.selectFirst("td.data-hour");
                                if (D) Log.i("filterHTMLkurse", "datahour " + dataHour.text());

                                String txt = dataHour.text();

                                if (txt.contains("/")) {
                                    row.setStunden(StrArrToIntArr(txt.split("/")));
                                    continue;
                                }

                                row.setStunde(Integer.parseInt(dataHour.text()));
                            } else if (selector.contains("td.data-subject")) {
                                Element dataSubject = td.selectFirst("td.data-subject");
                                String txtSubject = dataSubject.text();
                                if (D)
                                    Log.i("filterHTMLkurse", "datasubject " + dataSubject.text());

                                if (!anyMatch(txtSubject, kurse)) {
                                    if (D)
                                        Log.i("filterHTMLkurse", "not matching subject: " + txtSubject);
                                    breakk = true;
                                    break;
                                }

                                row.setFach(dataSubject.text());
                            } else if (selector.contains("td.hidden-xs data-agent")) {
                                Element dataAgent = td.selectFirst("td.hidden-xs.data-agent");
                                row.setVertreter(dataAgent.text());
                            } else if (selector.contains("td.hidden-xs.data-room")) {
                                Element dataRoom = td.selectFirst("td.hidden-xs.data-room");
                                row.setRaum(dataRoom.text());
                            } else if (selector.contains("td.hidden-xs.data-instead")) {
                                Element dataInstead = td.selectFirst("td.hidden-xs.data-instead");
                                row.setStatt(dataInstead.text());
                            } else if (selector.contains("td.hidden-xs.data-notice")) {
                                Element dataNotice = td.selectFirst("td.hidden-xs.data-notice");
                                row.setBemerkung(dataNotice.text());
                            }
                        }

                        if (tr != null && !info.contains(row) && !breakk) {
                            if (D) Log.i("filterHTMLkurse", "row not contained, adding");
                            info.addRow(row);
                        } else {
                            if (breakk) {
                                if (D) Log.i("filterHTMLkurse", "Incorrect class, skipping");
                            } else {
                                if (D) Log.i("filterHTMLkurse", "row contained, skipping");
                            }
                        }

                    }
                }
            }
        }

        return new TriTuple<String, Integer, VPInfo>(msgotd, version, info);
    }

    /**
     * @author Simon Dräger
     * @author Melvin Zähl
     */
    @Nullable
    public static synchronized TriTuple<String, Integer, VPInfo> filterHTML(@NonNull final Activity actv,
                                                                            Document d,
                                                                            String stufe,
                                                                            String[] kurse,
                                                                            ProgressCallback callback) throws AssertionError {
        assert stufe.equalsIgnoreCase("EF") ||
                stufe.equalsIgnoreCase("Q1") ||
                stufe.equalsIgnoreCase("Q2")
                : "Die Stufe muss EF oder höher sein";

        String msgotd = null;
        int version = 0;

        Elements elements = d.select("tr[data-index*='" + stufe + "']");
        Element strong = d.selectFirst("strong");
        VPInfo info = new VPInfo();

        if (elements.first() == null) {
            actv.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toasty.warning(actv.getApplicationContext(), "Für heute wurden keine passenden Vertretungen gefunden!").show();
                }
            });
            if (strong != null)
                version = Integer.parseInt(strong.text());
        } else {
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

                if (D) Log.i("filterHTMLkurse", "kurse: " + TextUtils.join(",", kurse));

                if (tbody != null) {
                    if (D) Log.i("filterHTMLkurse", "tbody != null");

                    Elements trs = tbody.select("tr");
                    int totalTRs = trs.size();
                    int processedTRs = 0;

                    /* Für jeden Listeneintrag am Tag */
                    for (Element tr : trs) {
                        if (D) Log.i("filterHTMLkurse", "tr child != null");
                        if (D) Log.i("filterHTMLkurse", "tr child: " + tr.text());

                        VPRow row = new VPRow();
                        boolean breakk = false;

                        Elements tds = tr.select("td");

                        for (Element td : tds) {
                            String selector = td.cssSelector();
                            if (D) Log.i("filterHTMLkurse", "cssSelector: " + selector);

                            if (selector.contains("td.data-form")) {
                                Element dataForm = td.selectFirst("td.data-form");
                                String formTxt = dataForm.text();
                                if (!formTxt.equals(stufe)) {
                                    if (D) Log.i("filterHTMLkurse", "class was: " + formTxt);
                                    breakk = true;
                                    break;
                                }
                                if (D) Log.i("filterHTMLkurse", "correct class: " + formTxt);
                                row.setKlasse(formTxt);
                            } else if (selector.contains("td.data-type")) {
                                Element dataType = td.selectFirst("td.data-type");
                                VPKind art = VPKind.fromString(dataType.text());

                                if (art == VPKind.PAUSENAUFSICHT ||
                                        art == VPKind.KLAUSUR ||
                                        art == VPKind.BETREUUNG ||
                                        art == VPKind.LEHRERTAUSCH ||
                                        art == VPKind.SONDEREINSATZ ||
                                        art == VPKind.VORMERKUNG) {
                                    continue;
                                }

                                if (D) Log.i("filterHTMLkurse", "dataType: " + dataType.text());
                                row.setArt(VPKind.fromString(dataType.text()));
                            } else if (selector.contains("td.data-hour")) {
                                Element dataHour = td.selectFirst("td.data-hour");
                                if (D) Log.i("filterHTMLkurse", "datahour " + dataHour.text());

                                String txt = dataHour.text();

                                if (txt.contains("/")) {
                                    row.setStunden(StrArrToIntArr(txt.split("/")));
                                    continue;
                                }

                                row.setStunde(Integer.parseInt(dataHour.text()));
                            } else if (selector.contains("td.data-subject")) {
                                Element dataSubject = td.selectFirst("td.data-subject");
                                String txtSubject = dataSubject.text();
                                if (D)
                                    Log.i("filterHTMLkurse", "datasubject " + dataSubject.text());

                                if (!anyMatch(txtSubject, kurse)) {
                                    if (D)
                                        Log.i("filterHTMLkurse", "not matching subject: " + txtSubject);
                                    breakk = true;
                                    break;
                                }

                                row.setFach(dataSubject.text());
                            } else if (selector.contains("td.hidden-xs data-agent")) {
                                Element dataAgent = td.selectFirst("td.hidden-xs.data-agent");
                                row.setVertreter(dataAgent.text());
                            } else if (selector.contains("td.hidden-xs.data-room")) {
                                Element dataRoom = td.selectFirst("td.hidden-xs.data-room");
                                row.setRaum(dataRoom.text());
                            } else if (selector.contains("td.hidden-xs.data-instead")) {
                                Element dataInstead = td.selectFirst("td.hidden-xs.data-instead");
                                row.setStatt(dataInstead.text());
                            } else if (selector.contains("td.hidden-xs.data-notice")) {
                                Element dataNotice = td.selectFirst("td.hidden-xs.data-notice");
                                row.setBemerkung(dataNotice.text());
                            }
                        }

                        processedTRs++;
                        int process = (processedTRs * 100) / totalTRs;
                        if (D) Log.i("filterHTMLkurse", "processed: " + process + "%");
                        callback.onProgress(process);

                        if (tr != null && !info.contains(row) && !breakk) {
                            if (D) Log.i("filterHTMLkurse", "row not contained, adding");
                            info.addRow(row);
                        } else {
                            if (breakk) {
                                if (D) Log.i("filterHTMLkurse", "Incorrect class, skipping");
                            } else {
                                if (D) Log.i("filterHTMLkurse", "row contained, skipping");
                            }
                        }

                    }

                    callback.onComplete();

                }
            }
        }

        if (info != null) {
            if (D) Log.i("filterHTMLkurse", "info != null");
            //if (D) Log.i("filterHTMLkurse", "info.getRows().get(0): " + info.getRows().get(0).toString());
        } else {
            if (D) Log.i("filterHTMLkurse", "info = null");
        }

        return new TriTuple<String, Integer, VPInfo>(msgotd, version, info);
    }

    public static int[] StrArrToIntArr(@NonNull String[] arr) {
        int[] out = new int[arr.length];

        for (int i = 0; i < out.length; i++) {
            out[i] = Integer.parseInt(arr[i].trim());
        }

        return out;
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

        //if (D) Log.i("getCurrentInfo/elements", ArrListToArr(elements.eachText()));

        for (Element e : elements) {
            if (e != null && !(inf.getRows().contains(e.text()))) {
                VPRow r = new VPRow();
                Elements subs = e.getAllElements();

                if (D) Log.i("getCurrentInfo", "Element: " + e.text());

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

                // Begin Logging
                if (D) Log.i("getCurrentInfo", "art: " + art);
                if (D) Log.i("getCurrentInfo", "stunde: " + stunde);
                if (D) Log.i("getCurrentInfo", "fach: " + fach);
                if (D) Log.i("getCurrentInfo", "klasse: " + klasse);
                if (D) Log.i("getCurrentInfo", "raum: " + raum);
                if (D) Log.i("getCurrentInfo", "statt: " + statt);
                if (D) Log.i("getCurrentInfo", "bemerkung: " + bemerkung);
                // End Logging

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

    /**
     * @param title   Titel der Benachrichtigung
     * @param content Content der Benachrichtigung
     * @author Melvin Zähl
     */
    public static void sendNotification(@NonNull Context ctx, String title, String content) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(ctx, NotificationChannel.DEFAULT_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_vp)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setLights(Color.GREEN, 400, 400)
                .setStyle(new NotificationCompat.BigTextStyle()
                .bigText(content));

        Notification notif = mBuilder.build();
        //notif.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;

        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(ctx);
        managerCompat.notify(getNotificationID(), notif);
    }


    /**
     * Langfristig zur Ersetzung der Kurse Array Listen
     *
     * @param url
     * @return String Array mit den Kursen
     * @author Melvin Zähl, Simon Dräger
     */
    @Nullable
    public static String[] parseKurse(String url) {
        String[] out = null;
        try {
            Document doc = Jsoup.connect("http://melvinz.atwebpages.com/vortex/vp-today/data/kurseq1.txt").get();
            out = doc.wholeText().split(";");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return out;
    }
}

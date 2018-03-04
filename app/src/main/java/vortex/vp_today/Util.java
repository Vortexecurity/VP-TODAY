package vortex.vp_today;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Simon Dräger
 * @version 3.3.18
 */

public final class Util {
    private static int result;
    private static Random rand;
    private static final String ALPHANUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
    private static AtomicInteger atomInt;

    static {
        rand = new Random();
        atomInt = new AtomicInteger(0);
    }

    public static final int getNotificationID() {
        return atomInt.incrementAndGet();
    }

    public static int ShowYesNoDialog(Context ctx, String text) {
        try {
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            result = DialogInterface.BUTTON_POSITIVE;
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            result = DialogInterface.BUTTON_NEGATIVE;
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
            builder.setMessage(text)
                    .setPositiveButton("Ja", listener)
                    .setNegativeButton("Nein", listener).show();
            builder.wait();

            int res = result;

            result = 0;

            return res;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return 0;
    }

    /**
     *
     * @param ctx
     * @param preselectedItems Look the positions up in strings.xml/KurseQ1 etc.
     * @return The selected items
     */
    @Nullable
    public static String[] ShowKurseDialogQ1(Context ctx, @Nullable boolean[] preselectedItems) {
        final Resources res = ctx.getResources();
        final ArrayList<String> selectedItems = new ArrayList<>();
        final String[] items = res.getStringArray(R.array.KurseQ1);
        final DlgResult result = new DlgResult();

        AlertDialog dialog = new AlertDialog.Builder(ctx)
                .setTitle("Kurse auswählen...")
                .setMultiChoiceItems(items, preselectedItems, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int indexSelected, boolean isChecked) {
                        if (isChecked) {
                            selectedItems.add(items[indexSelected]);
                        } else {
                            selectedItems.remove(indexSelected);
                        }
                    }
                }).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        result.setResult(DialogResult.OK);
                    }
                }).setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        result.setResult(DialogResult.CANCEL);
                    }
                }).create();

        dialog.show();

        if (result.getResult() == DialogResult.OK)
            return selectedItems.toArray(new String[0]);
        return null;
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
        return new String[] { ctx.getResources().getString(R.string.melvinemail),
                              ctx.getResources().getString(R.string.simonemail)
                              // TODO: Florian
        };
    }

    public static Intent sendEmail(String[] to, String subj, String body) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL  , to);
        i.putExtra(Intent.EXTRA_SUBJECT, subj);
        i.putExtra(Intent.EXTRA_TEXT   , body);
        return Intent.createChooser(i, "Sending email...");
    }

    public static String genRandString(int length) {
        String result = "";

        for (int i = 0; i < length; i++) {
            result += ALPHANUM.charAt(rand.nextInt(ALPHANUM.length() - 1));
        }

        return result;
    }

    public static String generateClientID() {
        return "0x" + genRandString(16);
    }

    public static boolean anyMatch(String str, String[] items) {
        for(int i = 0; i < items.length; i++) {
            if(str.contains(items[i]))
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

    /**
     * @return null on error.
     */
    @Nullable
    public static synchronized String fetchUnfiltered(String date) {
        try {
            /*if (date.equals("")) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Ein Fehler ist während des Aktualisiervorgangs aufgetreten!", Toast.LENGTH_LONG);
                    }
                });
                return;
            }*/

            String urlS = "https://vp.gymnasium-odenthal.de/god/" + date;
            String authStringEnc = "dnA6Z29kOTIwMQ==";

            Log.e("LOG", urlS);

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
     * @author Melvin Zähl
     * @author Simon Dräger
     */
    public static synchronized String[] filterHTML(Document d, String stufe, String sub) {
        /* Hilfsvariable, sodass stufe nicht direkt verändert wird */
        String _stufe = stufe;

        if (_stufe == null || _stufe.equals(""))
            _stufe = "05";

        /* Wenn 5 <= stufe < EF */
        if (!Character.isLetter(_stufe.charAt(0))) {
            _stufe = "0" + _stufe;
            _stufe = _stufe + sub;
        }

        Elements elements = d.select("tr[data-index*='" + _stufe + "']");

        ArrayList<String> s = new ArrayList<>();

        for (Element e : elements) {
            /* Manchmal sind Einträge im VP mehrere Male vorhanden, also nur einmal in die Liste tun. */
            if(e != null && !(s.contains(e.text())))
                s.add(e.text())/* e.text() + "\n\n"*/;
        }

        return s.toArray(new String[0]);
    }

    /**
     * @author Simon Dräger
     */
    public static synchronized String[] filterHTML(Document d, String stufe, String[] kurse) {
        String _stufe = stufe;

        if (_stufe == null || _stufe.equals(""))
            _stufe = "05";

        if (!Character.isLetter(_stufe.charAt(0)))
            _stufe = "0" + _stufe;

        Elements elements = d.select("tr[data-index*='" + _stufe + "']");

        ArrayList<String> s = new ArrayList<>();

        for (Element e : elements) {
            if (e != null && !(s.contains(e.text())) && Util.anyMatch(e.text(), kurse))
                s.add(e.text())/* + "\n\n"*/;
        }

        return s.toArray(new String[0]);
    }
}
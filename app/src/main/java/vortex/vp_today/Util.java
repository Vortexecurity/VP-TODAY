package vortex.vp_today;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.Random;

/**
 * @author Simon Dräger
 * @version 2.3.18
 */

public final class Util {
    private static int result;
    private static Random rand;
    private static final String ALPHANUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";

    static {
        rand = new Random();
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

    public static String getSettingStufe(Activity a) {
        return a.getSharedPreferences("vortex.vp_today.app", Context.MODE_PRIVATE).getString("stufe", "5");
    }

    public static boolean isInternetConnected(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
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
}
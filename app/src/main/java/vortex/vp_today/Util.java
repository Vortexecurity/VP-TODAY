package vortex.vp_today;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;

/**
 * Created by shftr on 2/28/2018.
 */

public final class Util {
    private static int result;

    public static int ShowYesNoDialog(Context ctx, String text) {

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

        int res = result;

        result = 0;

        return res;
    }

    public static String getSettingStufe(Activity a) {
        return a.getSharedPreferences("vortex.vp_today.app", Context.MODE_PRIVATE).getString("stufe", "");
    }
}
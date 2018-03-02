package vortex.vp_today;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;

/**
 * @author Simon Dräger
 */

public final class Util {
    private static int result;

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
        return a.getSharedPreferences("vortex.vp_today.app", Context.MODE_PRIVATE).getString("stufe", "");
    }
}
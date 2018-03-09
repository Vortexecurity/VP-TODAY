package vortex.vp_today.net;

import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import es.dmoral.toasty.Toasty;
import vortex.vp_today.activity.MainActivity;
import vortex.vp_today.util.TriTuple;
import vortex.vp_today.util.Util;

/**
 * @author Simon Dräger
 * @version 7.3.18
 */

public class RetrieveVPTask extends AsyncTask<Object, Void, TriTuple<String, Integer, String[]>> {
    private Exception exception = null;
    private MainActivity main = null;

    // MainActivity, String date, String stufe, String sub
    @Override
    protected TriTuple<String, Integer, String[]> doInBackground(Object... params) {
        main = (MainActivity) params[0];

        try {
            Log.i("RetrieveVPTask", "refreshing = true");

            String unfiltered = Util.fetchUnfiltered((String) params[1]);

            Log.i("RetrieveVPTask", "got unfiltered: " + (unfiltered == null ? "null" : "not null"));

            Document doc = Jsoup.parse(unfiltered);

            Log.i("RetrieveVPTask", "got doc");

            TriTuple<String, Integer, String[]> filtered = Util.filterHTML(main, doc, ((String) params[2]), ((String) params[3]));

            if (filtered == null) {
                Log.i("doInBackground", "filtered = null");
                return null;
            } else {
                Log.i("doInBackground", "filtered != null msgotd = " + filtered.x);
            }

            if (filtered.z == null) {
                Log.i("doInBackground", "z = null, return novpexist");
                return new TriTuple<String, Integer, String[]>("novpexist", 0, null);
            }

            return filtered;
        } catch (Exception ex) {
            ex.printStackTrace();
            this.exception = ex;
        }
        return null;
    }

    @Override
    protected void onPostExecute(TriTuple<String, Integer, String[]> result) {
        Log.i("onPostExecute", "setting text to result");
        if (result != null) {
            Log.i("onPostExecute", "result != null");
            if (result.z != null)
                Log.i("doInBackground", "filtered.z.length = " + result.z.length);
            if (result.x.equals("novpexist")) {
                main.tvVers.setText("Version: 0");
                main.msgOTD.setText("Für diesen Tag gibt es noch keine Vertretungen!");
            } else if (result.x != null && result.z != null) {
                main.txt.setText(TextUtils.join("\n\n", result.z));
                main.msgOTD.setText(result.x);
                main.tvVers.setText("Version: " + result.y.intValue());
            } else {
                Log.i("onPostExecute", "in else: result.x = null -> " + (result.x == null) + " y = 0 -> " + (result.y == 0) + " z = null -> " + (result.z == null));
            }
        } else {
            Log.i("onPostExecute", "result = null");
            Toasty.error(main.getApplicationContext(), "Fehler beim Aktualisieren!").show();
        }
        Log.i("onPostExecute", "set text to result");
        main.swipe.setRefreshing(false);
        Log.i("onPostExecute", "set refreshing to false");
    }

    @Nullable
    public Exception getException() {
        return exception;
    }
}

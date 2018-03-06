package vortex.vp_today.net;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import vortex.vp_today.logic.VPInfo;
import vortex.vp_today.logic.VPRow;
import vortex.vp_today.util.Util;

/**
 * @author Simon Dräger
 * @version 6.3.18
 */

public class RetrieveVPTask extends AsyncTask<Object, Void, String> {
    private Exception exception = null;
    private SharedPreferences prefs;

    // Context, String date, String stufe, String sub
    @Override
    protected String doInBackground(Object... params) {
        prefs = ((Context)params[0]).getSharedPreferences("vortex.vp_today.app", Context.MODE_PRIVATE);
        try {
            //VPInfo output = new VPInfo();

            String unfiltered = Util.fetchUnfiltered((String)params[1]);

            Document doc = Jsoup.parse(unfiltered);

            String[] filtered = Util.filterHTML(doc, ((String)params[2]), ((String)params[3]));

            return TextUtils.join("\n\n", filtered);
        } catch (Exception e) {
            this.exception = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(String vpinfo) {
        // TODO: check this.exception
        // TODO: do something with the feed
    }

    @Nullable
    public Exception getException() {
        return exception;
    }
}

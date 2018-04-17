package vortex.vp_today.net;

import android.os.AsyncTask;

import com.vplib.vortex.vplib.Util;

/**
 * @author Simon Dräger
 * @version 16.3.18
 */

public class RetrieveKurseFromServer extends AsyncTask<Void, Void, String[]> {
    @Override
    protected String[] doInBackground(Void... voids) {
        return Util.parseKurse(null);
    }
}

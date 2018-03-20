package vortex.vp_today.net;

import android.os.AsyncTask;
import android.util.Log;

import java.util.Date;

import vortex.vp_today.activity.MainActivity;
import vortex.vp_today.util.TwoFormatDate;
import vortex.vp_today.util.Util;

/**
 * Holt die VP-Daten asynchron
 *
 * @author Simon Dräger
 * @author Melvin Zähl
 * @version 8.3.18
 */

public class RetrieveDatesTask extends AsyncTask<Object, Void, TwoFormatDate[]> {
    private Exception exception = null;
    private MainActivity main = null;

    // params: MainActivity
    @Override
    protected TwoFormatDate[] doInBackground(Object... params) {
        main = (MainActivity) params[0];

        try {
            if (Util.D) Log.i("RetrieveDatesTask", "in try block");
            main.spinDate.setEnabled(false);
            if (Util.D) Log.i("RetrieveDatesTask", "setenabled false");
            TwoFormatDate[] dates = Util.getVPDates();
            if (Util.D) Log.i("RetrieveDatesTask", "got dates: " + (dates == null ? "null" : "not null"));
            return dates;
        } catch (Exception ex) {
            ex.printStackTrace();
            this.exception = ex;
        }

        return null;
    }

    @Override
    protected void onPostExecute(TwoFormatDate[] twoFormatDates) {
        Log.i("RetrieveDatesTask", "twoFormatDates: " + (twoFormatDates == null ? "null" : "not null"));
        if (main != null) {
            Log.i("RetrieveDatesTask", "main != null");

            Date[] javaDates = new Date[twoFormatDates.length];
            for (int i = 0; i < javaDates.length; i++)
                javaDates[i] = twoFormatDates[i].getActualDate().toDate();

            main.currentDatesAvailable = javaDates;
            main.processCurrentDates(twoFormatDates);
            main.spinDate.setSelection(0);
            main.spinDate.setEnabled(true);
        }
    }

    public Exception getException() {
        return exception;
    }
}

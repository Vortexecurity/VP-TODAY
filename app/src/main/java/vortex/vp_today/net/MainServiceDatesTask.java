package vortex.vp_today.net;

import android.os.AsyncTask;
import android.support.annotation.Nullable;

import com.vplib.vortex.vplib.TwoFormatDate;
import com.vplib.vortex.vplib.Util;

/**
 * @author Simon Dräger
 * @version 17.3.18
 */

public class MainServiceDatesTask extends AsyncTask<Void, Void, TwoFormatDate[]> {
    private Exception exception = null;

    @Override
    protected TwoFormatDate[] doInBackground(Void... voids) {
        try {
            //if (Util.D) Log.i("RetrieveDatesTask", "in try block");
            //if (Util.D) Log.i("RetrieveDatesTask", "setenabled false");
            TwoFormatDate[] dates = Util.getVPDates();
            //if (Util.D) Log.i("RetrieveDatesTask", "got dates: " + (dates == null ? "null" : "not null"));
            return dates;
        } catch (Exception ex) {
            ex.printStackTrace();
            this.exception = ex;
        }

        return null;
    }

    @Nullable
    public Exception getException() {
        return exception;
    }
}

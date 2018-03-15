package vortex.vp_today.net;

import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import es.dmoral.toasty.Toasty;
import vortex.vp_today.activity.MainActivity;
import vortex.vp_today.logic.VPInfo;
import vortex.vp_today.logic.VPRow;
import vortex.vp_today.util.ProgressCallback;
import vortex.vp_today.util.TriTuple;
import vortex.vp_today.util.Util;

/**
 * @author Simon Dräger
 * @version 12.3.18
 */

// params: Object, progress: Void, return: TriTuple: String Integer String[] || String Integer VPInfo[]
public class RetrieveVPTask extends AsyncTask<Object, Integer, TriTuple<String, Integer, VPInfo>> {
    private Exception exception = null;
    private MainActivity main = null;

    @Override
    protected void onProgressUpdate(final Integer... values) {
        super.onProgressUpdate(values);
        Log.i("onProgressUpdate", "Progress is " + values[0]);
        Util.setProgressAnimate(main.progressBar, values[0]);
        /*main.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i("onProgressUpdate", "Progress is " + values[0]);
                Util.setProgressAnimate(main.progressBar, values[0]);
                //main.progressBar.setProgress(values[0]);
            }
        });*/
    }

    // params: MainActivity, String date (vp style), String stufe, String sub, String[] kurse
    @Override
    @SuppressWarnings("ResourceTypes")
    protected TriTuple<String, Integer, VPInfo> doInBackground(Object... params) {
        main = (MainActivity) params[0];
        String vpDate = (String) params[1];
        String stufe = (String) params[2];
        String sub = (String) params[3];
        String[] kurse = (String[]) params[4];

        try {
            if (Util.D) Log.i("RetrieveVPTask", "refreshing = true");

            main.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    main.progressBar.setVisibility(View.VISIBLE);
                    //if (Util.D) Log.i("RetrieveVPTask", "visibility: " + main.progressBar.getVisibility());
                }
            });

            String unfiltered = Util.fetchUnfiltered(vpDate);

            if (Util.D) Log.i("RetrieveVPTask", "got unfiltered: " + (unfiltered == null ? "null" : "not null"));

            Document doc = Jsoup.parse(unfiltered);

            if (Util.D) Log.i("RetrieveVPTask", "got doc");

            TriTuple<String, Integer, String[]> filtered = null;
            TriTuple<String, Integer, VPInfo> filteredInfo = null;

            if (kurse == null) {
                if (Util.D) Log.i("RVPT/doInBackground", "kurse = null, doing filterHTML sub");
                filtered = Util.filterHTML(main, doc, stufe, sub);
            } else {
                if (Util.D) Log.i("RVPT/doInBackground", "kurse not null, doing filterHTML kurse");
                filteredInfo = Util.filterHTML(main, doc, stufe, kurse, new ProgressCallback() {
                    @Override
                    public void onProgress(int percent) {
                        Log.i("onProgress", "in onProgress");
                        publishProgress(percent);
                    }

                    @Override
                    public void onComplete() {

                    }
                });
            }

            if (filtered == null) {
                if (filteredInfo == null) {
                    if (Util.D) Log.i("RDT/doInBackground", "filtered & filteredInfo = null");
                    return null;
                } else {
                    if (Util.D) Log.i("RDT/doInBackground", "Chose filteredInfo, not null");
                }
            } else {
                if (Util.D) Log.i("RDT/doInBackground", "filtered != null msgotd = " + filtered.x);
            }

            if (Util.D) Log.i("RDT/doInBackground", "filteredInfo" + (filteredInfo == null));

            try {
                if (filtered != null) {
                    if (filtered.z == null) {
                        if (Util.D) Log.i("doInBackground", "filtered.z = null, return novpexist");
                        return new TriTuple<String, Integer, VPInfo>("novpexist", 0, null);
                    }
                } else if (filteredInfo != null) {
                    if (filteredInfo.z == null) {
                        if (Util.D) Log.i("doInBackground", "filteredInfo.z = null, return novpexist");
                        return new TriTuple<String, Integer, VPInfo>("novpexist", 0, null);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                if (Util.D) Log.i("doInBackground", "z = null, return novpexist");
                return new TriTuple<String, Integer, VPInfo>("novpexist", 0, null);
            }

            if (filtered != null) {
                TriTuple<String, Integer, VPInfo> out = null;
                VPInfo info = new VPInfo();
                VPRow[] rows = new VPRow[filtered.z.length];

                for (int i = 0; i < rows.length; i++) {
                    VPRow r = new VPRow();
                    r.setContent(filtered.z[i]);
                    rows[i] = r;
                }

                info.addAll(rows);

                out = new TriTuple<>(filtered.x, filtered.y, info);

                return out;
            }

            return filteredInfo;
        } catch (Exception ex) {
            ex.printStackTrace();
            this.exception = ex;
        }
        return null;
    }

    @Override
    protected void onPostExecute(TriTuple<String, Integer, VPInfo> result) {
        try {
            main.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    main.progressBar.setVisibility(View.GONE);
                    main.progressBar.setProgress(0);
                }
            });

            if (Util.D) Log.i("onPostExecute", "setting text to result");

            if (result != null) {
                if (Util.D) Log.i("onPostExecute", "result != null");
                try {
                    Log.i("onPostExecute", "result: " + result.z.getRows().get(0).toString());
                } catch (Exception ex) {
                    main.txt.setText("");
                    main.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toasty.info(main.getApplicationContext(), "Du hast an diesem Tag keine Vertretungen!").show();
                        }
                    });
                    if (Util.D) Log.i("onPostExecute", "result: null");
                }

                //if (result.z != null)
                //if (Util.D) Log.i("doInBackground", "filtered.z.length = " + result.z.length);

                if (result.x.equals("novpexist")) {
                    main.tvVers.setText("Version: 0");
                    main.msgOTD.setText("Für diesen Tag gibt es noch keine Vertretungen!");
                } else if (result.x != null && result.z != null && !result.z.isEmpty()) {
                    if (Util.D) Log.i("onPostExecute", "result not empty");

                    main.txt.setText("");

                    if (result.z.assumeKursVersion()) {
                        if (Util.D) Log.i("onPostExecute", "assuming kurse version");

                        for (VPRow row : result.z.getRows()) {
                            String linearContent = row.getLinearContent();
                            if (Util.D) Log.i("onPostExecute", "adding row: " + linearContent);
                            main.txt.append(linearContent);
                        }
                        main.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toasty.success(main.getApplicationContext(), "Aktualisiert!").show();
                            }
                        });
                    } else {
                        if (Util.D) Log.i("onPostExecute", "not assuming, adding result.z.getContent");

                        main.txt.setText(TextUtils.join("\n\n", result.z.getContent()));
                    }
                } else {
                    if (Util.D) Log.i("onPostExecute", "in else: result.x = null -> " + (result.x == null) + " y = 0 -> " + (result.y == 0) + " z = null -> " + (result.z == null));
                }
            } else {
                if (Util.D) Log.i("onPostExecute", "result = null");
                main.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toasty.error(main.getApplicationContext(), "Fehler beim Aktualisieren!").show();
                    }
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        /*if (result == null) {
            main.msgOTD.setText(result.x : "result.x ist null");
            main.tvVers.setText(result.y != null ? "Version: " + result.y.intValue() : "Version: -");
        }*/
        //Util.sendNotification("VP-TODAY", "Neuer Vertretungsplan online!");

        if (Util.D) Log.i("onPostExecute", "set text to result");

        main.swipe.setRefreshing(false);

        if (Util.D) Log.i("onPostExecute", "set refreshing to false");
    }

    @Nullable
    public Exception getException() {
        return exception;
    }
}

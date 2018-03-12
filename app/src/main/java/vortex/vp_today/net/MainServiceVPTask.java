package vortex.vp_today.net;

import android.os.AsyncTask;
import android.support.annotation.Nullable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import vortex.vp_today.MainService;
import vortex.vp_today.logic.VPInfo;
import vortex.vp_today.logic.VPRow;
import vortex.vp_today.util.TriTuple;
import vortex.vp_today.util.Util;

/**
 * @author Simon Dräger
 * @version 12.3.18
 */

public class MainServiceVPTask extends AsyncTask<Object, Integer, TriTuple<String, Integer, VPInfo>> {
    private Exception exception = null;
    private MainService srvs = null;

    // params: MainActivity, String date (vp style), String stufe, String sub, String[] kurse
    @Override
    @SuppressWarnings("ResourceTypes")
    protected TriTuple<String, Integer, VPInfo> doInBackground(Object... params) {
        srvs = (MainService) params[0];
        String vpDate = (String) params[1];
        String stufe = (String) params[2];
        String sub = (String) params[3];
        String[] kurse = (String[]) params[4];

        try {
            String unfiltered = Util.fetchUnfiltered(vpDate);

            Document doc = Jsoup.parse(unfiltered);

            TriTuple<String, Integer, String[]> filtered = null;
            TriTuple<String, Integer, VPInfo> filteredInfo = null;

            if (kurse == null) {
                filtered = Util.filterHTMLService(doc, stufe, sub);
            } else {
                filteredInfo = Util.filterHTMLService(doc, stufe, kurse);
            }

            if (filtered == null) {
                if (filteredInfo == null) {
                    return null;
                }
            }

            try {
                if (filtered != null) {
                    if (filtered.z == null) {
                        return new TriTuple<String, Integer, VPInfo>("novpexist", 0, null);
                    }
                } else if (filteredInfo != null) {
                    if (filteredInfo.z == null) {
                        return new TriTuple<String, Integer, VPInfo>("novpexist", 0, null);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
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

    /*@Override
    protected void onPostExecute(TriTuple<String, Integer, VPInfo> result) {
        try {
            if (result != null) {
                if (result.x != null && result.z != null && !result.z.isEmpty()) {
                    if (Util.D) Log.i("onPostExecute", "result not empty");

                    srvs.publishInfo(result.z);

                    /+if (result.z.assumeKursVersion()) {
                        if (Util.D) Log.i("onPostExecute", "assuming kurse version");

                        for (VPRow row : result.z.getRows()) {
                            String linearContent = row.getLinearContent();
                            if (Util.D) Log.i("onPostExecute", "adding row: " + linearContent);

                            srvs.publishInfo();
                            main.txt.append(linearContent);
                        }
                        Toasty.success(main.getApplicationContext(), "Aktualisiert!").show();
                    } else {
                        if (Util.D) Log.i("onPostExecute", "not assuming, adding result.z.getContent");

                        main.txt.setText(TextUtils.join("\n\n", result.z.getContent()));
                    }+/
                } else {
                    if (Util.D) Log.i("onPostExecute", "in else: result.x = null -> " + (result.x == null) + " y = 0 -> " + (result.y == 0) + " z = null -> " + (result.z == null));
                }
            } else {
                if (Util.D) Log.i("onPostExecute", "result = null");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }*/

    @Nullable
    public Exception getException() {
        return exception;
    }
}

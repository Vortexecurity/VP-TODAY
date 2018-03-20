package vortex.vp_today.activity;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import es.dmoral.toasty.Toasty;
import vortex.vp_today.R;
import vortex.vp_today.adapter.RVAdapter;
import vortex.vp_today.logic.VPInfo;
import vortex.vp_today.net.RetrieveDatesTask;
import vortex.vp_today.net.RetrieveVPTask;
import vortex.vp_today.util.Tuple;
import vortex.vp_today.util.TwoFormatDate;
import vortex.vp_today.util.Util;

/**
 * @author Simon Dräger
 * @author Melvin Zähl
 * @version 3.3.18
 */

public class MainActivity extends AppCompatActivity {
    public TextView msgOTD;
    public TextView tvVers;
    public Spinner spinDate;
    public SwipeRefreshLayout swipe;
    public ProgressBar progressBar;
    public RecyclerView rv;
    public Date[] currentDatesAvailable;
    public VPInfo currentInfo = null;
    public ArrayList<VPInfo> lastOfflineVersions = null;
    public int currentVersion = 0;

    private static boolean activityVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sp = getSharedPreferences("vortex.vp_today.app", Context.MODE_PRIVATE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Initialize views */
        msgOTD = findViewById(R.id.msgOTD);
        spinDate = findViewById(R.id.spinDate);
        swipe = findViewById(R.id.swiperefresh);
        tvVers = findViewById(R.id.tvVers);
        progressBar = findViewById(R.id.vpProg);
        rv = findViewById(R.id.rv);
        /**/

        /* Falls dies der erste Start sein sollte eine Client ID erstellen und speichern. */
        if (sp.getString("clientid", "0x0").equals("0x0")) {
            String[] setKurse = getResources().getStringArray(R.array.KurseQ1);
            int q1Len = setKurse.length;
            Tuple<String[], Boolean[]> putTuple;

            String[] strs = new String[q1Len];
            Boolean[] bools = new Boolean[q1Len];

            for (int i = 0; i < q1Len; i++) {
                strs[i] = "";
                bools[i] = false;
            }

            putTuple = new Tuple<>(strs, bools);

            Log.i("MainActivity", "strs length: " + strs.length);
            Log.i("MainActivity", "bools length: " + bools.length);

            Util.putGsonObject(getApplicationContext(), getString(R.string.settingkurse), putTuple, new TypeToken<Tuple<String[], Boolean[]>>() {});
            sp.edit().putString("clientid", Util.generateClientID()).apply();
        }

        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setHasFixedSize(false);

        swipe.setColorSchemeResources(R.color.colorPrimaryDark);

        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    if (spinDate.getSelectedItem() == null) {
                        new RetrieveDatesTask().execute(MainActivity.this);
                    }
                    updateListFromSpinner();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        spinDate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateListFromSpinner();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                spinDate.setSelection(0);
            }
        });
        /**/

        if (Util.isInternetConnected(getApplicationContext())) {
            new RetrieveDatesTask().execute(MainActivity.this);
        } else {
            lastOfflineVersions = new ArrayList<>(Arrays.asList(Util.loadOfflineVersions(getApplicationContext())));
            currentDatesAvailable = Util.loadOfflineDates(getApplicationContext());
            if (lastOfflineVersions != null && currentDatesAvailable != null) {
                LocalDate now = LocalDate.now();
                String[] strdates = new String[currentDatesAvailable.length];
                for (int i = 0; i < strdates.length; i++) {
                    String month = "";
                    String day = "";

                    if (currentDatesAvailable[i].getMonth() < 10)
                        month = "0";
                    month += currentDatesAvailable[i].getMonth();
                    if (currentDatesAvailable[i].getDate() < 10)
                        day = "0";
                    day += currentDatesAvailable[i].getDate();
                    strdates[i] = currentDatesAvailable[i].getYear() + "-" + month + "-" + day;
                }
                String closest = Util.getClosestDate(now.getYear(), now.getMonthOfYear(), now.getDayOfMonth(), strdates);
                for (int i = 0; i < lastOfflineVersions.size(); i++) {
                    VPInfo inf = lastOfflineVersions.get(i);
                    Date d = inf.getDate();
                    String year = String.valueOf(d.getYear());
                    String month = "";
                    String day = "";

                    if (d.getMonth() < 10)
                        month = "0";
                    month += d.getMonth();

                    if (d.getDay() < 10)
                        day = "0";
                    day += d.getDate();

                    String strVpDate = year + month + day;

                    if (strVpDate.equals(closest)) {
                        RVAdapter adapter = new RVAdapter(rv, inf.getRows());
                        rv.setAdapter(adapter);
                        tvVers.setText("Version: " + currentVersion + " (Offline)");
                        break;
                    }
                }
            } else {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toasty.warning(getApplicationContext(), "Es besteht keine Internetverbindung!").show();
                    }
                });
            }
        }

        Util.setProgressMax(progressBar, 100);
    }

    public static void show(@NonNull Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static boolean isActivityVisible() {
        return activityVisible;
    }

    private static void activityResumed() {
        activityVisible = true;
    }

    private static void activityPaused() {
        activityVisible = false;
    }

    private synchronized void updateListFromSpinner() {
        if (Util.isInternetConnected(getApplicationContext())) {
            String str = spinDate.getSelectedItem().toString().substring(4).trim();

            if (Util.D) Log.i("updateListfromSpinner", "str: " + str.trim());

            String vp = Util.makeVpDate(str);

            if (Util.D) Log.i("updateListFromSpinner", "vp: " + vp);

            LocalDate ldate = new LocalDate(vp);

            if (Util.D) Log.e("LDATE", ldate.toString());
            if (Util.getSettingStufe(getApplicationContext()).equals("EF") ||
                    Util.getSettingStufe(getApplicationContext()).equals("Q1") ||
                    Util.getSettingStufe(getApplicationContext()).equals("Q2")) {
                new RetrieveVPTask().execute(
                        MainActivity.this,
                        Util.makeDate(ldate.getDayOfMonth(), ldate.getMonthOfYear() - 1, ldate.getYear()),
                        Util.getSettingStufe(getApplicationContext()),
                        Util.getSettingKlasse(getApplicationContext()),
                        Util.getSelectedKurse(getApplicationContext())
                );
            } else {
                new RetrieveVPTask().execute(
                        MainActivity.this,
                        Util.makeDate(ldate.getDayOfMonth(), ldate.getMonthOfYear() - 1, ldate.getYear()),
                        Util.getSettingStufe(getApplicationContext()),
                        Util.getSettingKlasse(getApplicationContext()),
                        null
                );
            }
        } else {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toasty.error(getApplicationContext(), "Es besteht keine Internetverbindung!").show();
                }
            });
        }
    }

    public void processCurrentDates(TwoFormatDate[] dates) {
        if (dates == null) {
            if (Util.D) Log.i("processCurrentDates", "dates = null");
            return;
        }

        String[] actuals = new String[dates.length];

        for (int i = 0; i < actuals.length; i++)
            actuals[i] = dates[i].getPrefix() + dates[i].getActualDate().getDayOfMonth() + "." + dates[i].getActualDate().getMonthOfYear() + "." + dates[i].getActualDate().getYear();

        ArrayAdapter<String> dateAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                actuals
        );

        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinDate.setAdapter(dateAdapter);

        Date[] javaDates = new Date[dates.length];
        for (int i = 0; i < javaDates.length; i++)
            javaDates[i] = dates[i].getActualDate().toDate();
        currentDatesAvailable = javaDates;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        int close = intent.getIntExtra("closeNotificationID", -1);

        if (close != -1) {
            try {
                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.cancel(close);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    protected void onPause() {
        if (currentInfo != null) {
            Log.i("MainActv", "currentInfo != null, saving values");
            lastOfflineVersions.add(currentInfo);
            Context appCtx = getApplicationContext();
            Util.saveOfflineVersions(appCtx, lastOfflineVersions.toArray(new VPInfo[0]));
            Util.saveOfflineDates(appCtx, currentDatesAvailable);
        } else {
            Log.i("Main", "currentInfo is null");
        }
        activityPaused();
        super.onPause();
    }

    @Override
    protected void onResume() {
        activityResumed();
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menuheader, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                //SettingsActivity.show(getApplicationContext());
                SettActivity.show(getApplicationContext());
                return true;
            case R.id.rate:
                RateActivity.show(getApplicationContext());
                return true;
            case R.id.about:
                AboutActivity.show(getApplicationContext());
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

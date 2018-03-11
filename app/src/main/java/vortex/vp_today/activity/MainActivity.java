package vortex.vp_today.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.joda.time.LocalDate;

import es.dmoral.toasty.Toasty;
import vortex.vp_today.R;
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
    public EditText txt;
    public ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sp = getSharedPreferences("vortex.vp_today.app", Context.MODE_PRIVATE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Initialize views */
        txt = findViewById(R.id.text);
        msgOTD = findViewById(R.id.msgOTD);
        spinDate = findViewById(R.id.spinDate);
        swipe = findViewById(R.id.swiperefresh);
        tvVers = findViewById(R.id.tvVers);
        progressBar = findViewById(R.id.vpProg);
        /**/

        Util.setup(this);

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

            Util.putGsonObject(getString(R.string.settingkurse), putTuple);
            sp.edit().putString("clientid", Util.generateClientID()).apply();
        }

        //progressBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.colorPrimaryDark), android.graphics.PorterDuff.Mode.SRC_IN);
        //progressBar.getProgressDrawable().setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);

        swipe.setColorSchemeResources(R.color.colorPrimaryDark);

        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    updateListFromSpinner();
                    /*
                     Document doc = Jsoup.parse(tmp);
                     String[] content = Util.getCurrentInfo(doc).getContent();
                     txt.setText(TextUtils.join("\n\n", content));
                     */
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

        new RetrieveDatesTask().execute(MainActivity.this);
    }

    public static void show(@NonNull Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private synchronized void updateListFromSpinner() {
        if (Util.isInternetConnected()) {
            String str = spinDate.getSelectedItem().toString().substring(4).trim();

            if (Util.D) Log.i("updateListfromSpinner", "str: " + str.trim());

            String vp = Util.makeVpDate(str);

            if (Util.D) Log.i("updateListFromSpinner", "vp: " + vp);

            LocalDate ldate = new LocalDate(vp);

            if (Util.D) Log.e("LDATE", ldate.toString());
            if (Util.getSettingStufe().equals("EF") ||
                    Util.getSettingStufe().equals("Q1") ||
                    Util.getSettingStufe().equals("Q2")) {
                new RetrieveVPTask().execute(
                        MainActivity.this,
                        Util.makeDate(ldate.getDayOfMonth(), ldate.getMonthOfYear() - 1, ldate.getYear()),
                        Util.getSettingStufe(),
                        Util.getSettingKlasse(),
                        Util.getSelectedKurse()
                );
            } else {
                new RetrieveVPTask().execute(
                        MainActivity.this,
                        Util.makeDate(ldate.getDayOfMonth(), ldate.getMonthOfYear() - 1, ldate.getYear()),
                        Util.getSettingStufe(),
                        Util.getSettingKlasse(),
                        null
                );
            }
        } else {
            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toasty.error(getApplicationContext(), "Es besteht keine Internetverbindung!", 3500).show();
                }
            });
        }
    }

    public void processCurrentDates(TwoFormatDate[] dates) {
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
                SettingsActivity.show(getApplicationContext());
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

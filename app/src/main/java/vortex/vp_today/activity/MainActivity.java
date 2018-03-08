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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.LocalDate;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Calendar;

import vortex.vp_today.R;
import vortex.vp_today.net.RetrieveDatesTask;
import vortex.vp_today.net.RetrieveVPTask;
import vortex.vp_today.util.TwoFormatDate;
import vortex.vp_today.util.Util;

/**
 * @author Simon Dräger
 * @author Melvin Zähl
 * @version 3.3.18
 */

public class MainActivity extends AppCompatActivity {
    //private DatePickerDialog.OnDateSetListener mDateSetListener;
    private String date = "";
    private TextView textView;
    public TextView msgOTD;
    public TextView tvVers;
    private volatile String tmp = "";
    public Spinner spinDate;
    public SwipeRefreshLayout swipe;
    private static final Object lockObj = new Object();
    private TwoFormatDate[] currentDates;

    public EditText txt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sp = getSharedPreferences("vortex.vp_today.app", Context.MODE_PRIVATE);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Initialize views */
        txt = findViewById(R.id.text);
        textView = findViewById(R.id.textView);
        msgOTD = findViewById(R.id.msgOTD);
        spinDate = findViewById(R.id.spinDate);
        swipe = findViewById(R.id.swiperefresh);
        tvVers = findViewById(R.id.tvVers);
        /**/

        /* Falls dies der erste Start sein sollte eine Client ID erstellen und speichern. */
        if (sp.getString("clientid", "0x0").equals("0x0")) {
            sp.edit().putString("clientid", Util.generateClientID()).apply();
        }

        /* Listener region */
        /*btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int year = cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog dialog = new DatePickerDialog(
                        MainActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener,
                        year, month, day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });*/

        /*mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month++;

                if (day >= 10 && month >= 10)
                    date = year + "-" + month + "-" + day;
                else if (day < 10 && month >= 10)
                    date = year + "-" + month + "-0" + day;
                else if (month < 10 && day >= 10)
                    date = year + "-0" + month + "-" + day;
                else
                    date = year + "-0" + month + "-0" + day;
                textView.setText("Deine Vertretungen für : " + day + "." + month + "." + year);
                //update();
                new RetrieveVPTask().execute(
                        MainActivity.this,
                        Util.makeDate(day, month, year),
                        Util.getSettingStufe(getApplicationContext()),
                        Util.getSettingKlasse(getApplicationContext())
                );
            }
        };*/

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

        Calendar c = Calendar.getInstance();
        date = Util.makeDate(c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.MONTH), c.get(Calendar.YEAR));
        new RetrieveDatesTask().execute(MainActivity.this);
    }

    public static void show(@NonNull Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private synchronized void updateListFromSpinner() {
        LocalDate ldate = new LocalDate(Util.makeVpDate(spinDate.getSelectedItem().toString()/*.replace('.', '-')*/));
        Log.e("LDATE", ldate.toString());
        new RetrieveVPTask().execute(
                MainActivity.this,
                Util.makeDate(ldate.getDayOfMonth(), ldate.getMonthOfYear() - 1, ldate.getYear()),
                Util.getSettingStufe(getApplicationContext()),
                Util.getSettingKlasse(getApplicationContext())
        );
    }

    private synchronized void getVPHTML() {
        try {
            if (date.equals("")) {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Ein Fehler ist während des Aktualisiervorgangs aufgetreten!", Toast.LENGTH_LONG);
                    }
                });
                return;
            }

            String urlS = "https://vp.gymnasium-odenthal.de/god/" + date;
            String authStringEnc = "dnA6Z29kOTIwMQ==";

            Log.e("LOG", "URL = " + urlS);

            URL url = new URL(urlS);
            URLConnection urlConnection = url.openConnection();
            urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);

            InputStream is = urlConnection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);

            int numCharsRead;
            char[] charArray = new char[1024];
            StringBuffer sb = new StringBuffer();

            while ((numCharsRead = isr.read(charArray)) > 0) {
                sb.append(charArray, 0, numCharsRead);
            }

            String result = sb.toString();

            tmp = result;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void processCurrentDates(TwoFormatDate[] dates) {
        currentDates = dates;
        String[] actuals = new String[dates.length];

        for (int i = 0; i < actuals.length; i++)
            actuals[i] = dates[i].getActualDate().getDayOfMonth() + "." + dates[i].getActualDate().getMonthOfYear() + "." + dates[i].getActualDate().getYear();

        ArrayAdapter<String> dateAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                actuals
        );

        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinDate.setAdapter(dateAdapter);
    }

    /**
     * Updates the Vertretungs-ListView
     *
     * @author Melvin Zähl
     * @author Simon Dräger
     */
    /*private synchronized void update() {
        try {

            Toast.makeText(getApplicationContext(), "Aktualisiere...", Toast.LENGTH_SHORT).show();

            getVPHTML();

            Toast.makeText(getApplicationContext(), "Aktualisiert!", Toast.LENGTH_SHORT).show();

            Document doc = Jsoup.parse(tmp);

            Log.e("STUFE", Util.getSettingStufe(this));

            String[] content;

            if (!Util.getSettingKlasse(getApplicationContext()).equals(""))
                content = Util.filterHTML(doc, Util.getSettingStufe(getApplicationContext()), Util.getSettingKlasse(getApplicationContext()));
            else
                content = Util.filterHTML(doc, Util.getSettingStufe(getApplicationContext()), "");

            txt.setText(TextUtils.join("\n\n", content));

            Elements elements = doc.select("strong");

            if (elements.first() == null) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Für heute wurden keine passenden Vertretungen gefunden!", Toast.LENGTH_SHORT).show();
                    }
                });
                tvVers.setText("Version: 0");
                msgOTD.setText("Für diesen Tag gibt es noch keine Vertretungen!");
                return;
            } else
                tvVers.setText("Version: " + elements.first().text());

            Element e = null;

            if (!doc.is("div.alert")) {
                e = doc.select("div.alert").first();
                if (e != null)
                    msgOTD.setText(e.text());
                else
                    msgOTD.setText("An diesem Tag gibt es (noch) keinen Informationstext!");
            }

            e = doc.selectFirst("p");
            if (e.text().equals("Für diesen Tag existiert derzeit kein Vertretungsplan. Bitten schauen Sie später nochmal vorbei!"))
                msgOTD.setText("Für diesen Tag existiert derzeit kein Vertretungsplan. Bitten schauen Sie später nochmal vorbei!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
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

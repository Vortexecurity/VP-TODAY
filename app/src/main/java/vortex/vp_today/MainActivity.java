package vortex.vp_today;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Calendar;

/**
 * @author Simon Dräger
 * @author Melvin Zähl
 * @version 3.3.18
 */

public class MainActivity extends AppCompatActivity {

    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private String date = "";
    private TextView textView;
    private TextView msgOTD;
    private TextView tvVers;
    private volatile String tmp = "";
    private Thread t;
    private Button btnDate;
    private SwipeRefreshLayout swipe;
    private static final Object lockObj = new Object();

    private EditText txt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Initialize views */
        txt = findViewById(R.id.text);
        textView = findViewById(R.id.textView);
        msgOTD = findViewById(R.id.msgOTD);
        btnDate = findViewById(R.id.btnDate);
        swipe = findViewById(R.id.swiperefresh);
        tvVers = findViewById(R.id.tvVers);
        /**/

        SharedPreferences sp = getSharedPreferences("vortex.vp_today.app", Context.MODE_PRIVATE);

        if (sp.getString("clientid", "0x0").equals("0x0")) {
            sp.edit().putString("clientid", Util.generateClientID()).commit();
        }

        /* Thread region */
        t = new Thread(new Runnable() {
            @Override
            public void run() {
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

                    Log.e("LOG", urlS);

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
        });
        /**/

        /* Listener region */
        btnDate.setOnClickListener(new View.OnClickListener() {
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
                        year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;

                if (day >= 10 && month >= 10)
                    date = year + "-" + month + "-" + day;
                else if (day < 10 && month >= 10)
                    date = year + "-" + month + "-0" + day;
                else if (month < 10 && day >= 10)
                    date = year + "-0" + month + "-" + day;
                else
                    date = year + "-0" + month + "-0" + day;
                textView.setText("Deine Vertretungen für : " + day + "." + month + "." + year);
                update();
            }
        };

        swipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        update();
                        swipe.setRefreshing(false);
                    }
                });
            }
        });
        /**/
    }

    public MainActivity getInst() {
        return this;
    }

    /**
     * Updates the Vertretungs-ListView
     * @author Melvin Zähl
     * @author Simon Dräger
     */
    private synchronized void update() {
        try {
            /* Auf einen Thread synchronisieren, sonst gibst Fehler */
            //synchronized (lockObj) {
                try {
                    if (!t.isAlive()) {
                        t.start();
                        Toast.makeText(getApplicationContext(), "Aktualisiere...", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                t.join();

                Toast.makeText(getApplicationContext(), "Aktualisiert!", Toast.LENGTH_SHORT).show();

                Document doc = Jsoup.parse(tmp);

                Log.e("STUFE", Util.getSettingStufe(this));

                filterHTML(doc, Util.getSettingStufe(this));

                tvVers.setText("Version: " + doc.select("strong").first().text());

                Element e = null;

                if (!doc.is("div.alert")) {
                    e = doc.select("div.alert").first();
                    if (e != null)
                        msgOTD.setText(e.text());
                    else
                        msgOTD.setText("An diesem Tag gibt es (noch) keinen Informationstext!");
                }
            //}
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menuheader, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch ( item.getItemId() ) {
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

    /**
     * @author Melvin Zähl
     * @author Simon Dräger
     */
    private synchronized void filterHTML(Document d, String stufe) {
        /* Hilfsvariable, sodass stufe nicht direkt verändert wird */
        String _stufe = stufe;

        if (_stufe == null || _stufe.equals(""))
            _stufe = "05";

        /* Wenn 5 <= stufe < EF */
        if (!Character.isLetter(_stufe.charAt(0)))
            _stufe = "0" + _stufe;

        Elements elements = d.select("tr[data-index*='" + _stufe + "']");

        String s = "";

        for (Element e : elements) {
            /* Manchmal sind Einträge im VP mehrere Male vorhanden, also nur einmal in die Liste tun. */
            if(e != null && !(s.contains(e.text())))
                s += e.text() + "\n\n";
        }

        txt.setText(s);
    }

    /**
     * @author Simon Dräger
     */
    private synchronized void filterHTML(Document d, String stufe, String[] kurse) {
        String _stufe = stufe;

        if (_stufe == null || _stufe.equals(""))
            _stufe = "05";

        if (!Character.isLetter(_stufe.charAt(0)))
            _stufe = "0" + _stufe;

        Elements elements = d.select("tr[data-index*='" + _stufe + "']");

        String s = "";

        for (Element e : elements) {
            if(e != null && !(s.contains(e.text())) && Util.anyMatch(e.text(), kurse))
                s += e.text() + "\n\n";
        }

        txt.setText(s);
    }
}
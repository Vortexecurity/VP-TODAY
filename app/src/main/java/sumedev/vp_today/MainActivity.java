package sumedev.vp_today;

import android.content.Context;
import android.content.Intent;
import android.security.NetworkSecurityPolicy;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

    TextView txt;
    String tmp = "";

    Thread t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String urlS = "https://vp.gymnasium-odenthal.de/god/2018-03-02";
                    String authStringEnc = "dnA6Z29kOTIwMQ==";
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
        Button btnUpdate = findViewById(R.id.btnUpdate);
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                update();
            }
        });

       txt = findViewById(R.id.text);


        Button btnKurse = findViewById(R.id.btnKurse);
        btnKurse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("[LOG]", "Kurse clicked");
            }
        });

        //Last
        update();
    }

    private void update(){
        t.start();
        try {
            t.join();
            txt.setText(tmp);
            Toast.makeText(getApplicationContext(), "Aktualisiert", Toast.LENGTH_SHORT).show();
        } catch (InterruptedException e) {
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
                Intent myIntent = new Intent(getBaseContext(), SettingsActivity.class);
                this.startActivity(myIntent);
                break;
        }

        return true;
    }
}
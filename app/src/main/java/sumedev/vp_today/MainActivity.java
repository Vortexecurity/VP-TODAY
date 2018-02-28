package sumedev.vp_today;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;

public class MainActivity extends AppCompatActivity {

    TextView txt;
    String tmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
    }

    private void update(){
        Log.e("[LOG]", "Update Clicked");

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String urlS = "https://vp.gymnasium-odenthal.de/god/2018-03-01";
                    String name = "vp";
                    String password = "god9201";
                    String authString = name + ":" + password;

                    //byte[] authEncBytes = Base64.getMimeEncoder().encode(authString.getBytes());
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
        t.start();

        txt.setText(tmp);
    }
}

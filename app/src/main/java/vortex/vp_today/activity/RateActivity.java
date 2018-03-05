package vortex.vp_today.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Toast;

import vortex.vp_today.R;
import vortex.vp_today.util.Util;

/**
 * @author Simon Dräger
 * @version 3.3.18
 */

public class RateActivity extends AppCompatActivity {
    private ProgressBar progBar;
    private Button btnSend;
    private EditText txtSuggest;
    private RatingBar rateBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate);

        progBar = findViewById(R.id.progressBar);
        btnSend = findViewById(R.id.btnSend);
        txtSuggest = findViewById(R.id.txtSuggest);
        rateBar = findViewById(R.id.rateBar);

        progBar.setVisibility(View.INVISIBLE);

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: check if valid
                if (txtSuggest.getText().toString().trim().equals("")) {
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Bitte geben Sie einen Bericht ein!", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    if (Util.isInternetConnected(getApplicationContext())) {
                        Util.sendBotEmail(RateActivity.this, Util.getDevEmails(getApplicationContext()), "Rating " + rateBar.getRating() + " Sterne",
                                txtSuggest.getText().toString());
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Feedback erfolgreich gesendet!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Es besteht keine Internetverbindung.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        });

        txtSuggest.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (txtSuggest.getText().toString().length() >= 1000)
                        return true;
                }
                return false;
            }
        });
    }

    public static void show(@NonNull Context context){
        Intent intent = new Intent(context, RateActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
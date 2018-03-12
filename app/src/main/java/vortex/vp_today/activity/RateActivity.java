package vortex.vp_today.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RatingBar;

import es.dmoral.toasty.Toasty;
import vortex.vp_today.R;
import vortex.vp_today.mail.BackgroundMail;
import vortex.vp_today.util.Util;

/**
 * @author Simon Dräger
 * @author Melvin Zähl
 * @version 6.3.18
 */

public class RateActivity extends AppCompatActivity {
    private ProgressBar progBar;
    private Button btnSend;
    private EditText txtSuggest;
    private RatingBar rateBar;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rate);

        prefs = getSharedPreferences("vortex.vp_today.app", Context.MODE_PRIVATE);

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
                            Toasty.info(getApplicationContext(), "Bitte geben Sie einen Bericht ein!").show();
                        }
                    });
                } else {
                    if (Util.isInternetConnected(getApplicationContext())) {
                        String cid = prefs.getString("clientid", "0x0");

                        if (cid.equals("0x0")) {
                            prefs.edit().putString("clientid", Util.generateClientID().toString()).apply();
                        }

                        cid = prefs.getString("clientid", "0x0");

                        Util.sendBotEmail(
                                RateActivity.this,
                                Util.getDevEmails(getApplicationContext()),
                                "Client " + cid + " Rating: " + rateBar.getRating() + " Sterne",
                                "Gesendete Mitteilung: \n\n" + txtSuggest.getText().toString(),
                                "Sende Feedback...",
                                new BackgroundMail.OnSuccessCallback() {
                                    @Override
                                    public void onSuccess() {
                                        successCallback();
                                    }
                                },
                                new BackgroundMail.OnFailCallback() {
                                    @Override
                                    public void onFail() {
                                        failCallback();
                                    }
                                }
                        );
                    } else {
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                Toasty.error(getApplicationContext(), "Es besteht keine Internetverbindung.").show();
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

    private void successCallback() {
        Toasty.success(getApplicationContext(), "Feedback erfolgreich gesendet!").show();
    }

    private void failCallback() {
        Toasty.error(getApplicationContext(), "Es ist ein Fehler beim Senden aufgetreten.").show();
    }

    public static void show(@NonNull Context context){
        Intent intent = new Intent(context, RateActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}